package br.com.pabloalbuquerque.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.pabloalbuquerque.todolist.user.IUserRepository;
import br.com.pabloalbuquerque.todolist.user.UserModel;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    final IUserRepository userRepository;

    public FilterTaskAuth(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();

        if (servletPath.startsWith("/tasks/")) {
            String authorization = request.getHeader("Authorization");
            String token = null;

            if (authorization != null && authorization.startsWith("Basic")) {
                token = authorization.substring("Basic".length()).trim();
            }

            byte[] authToken = Base64.getDecoder().decode(token);
            String authTokenDecoded = new String(authToken);
            String[] authTokenDecodedSplit = authTokenDecoded.split(":");

            String username = authTokenDecodedSplit[0];
            String password = authTokenDecodedSplit[1];

            //Validate user
            UserModel user = this.userRepository.findByUsername(username);
            if (user == null) {
                response.sendError(401, "Usuário não autorizado");
                return;
            }

            BCrypt.Result validatedPassword = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
            if (!validatedPassword.verified) {
                response.sendError(401, "Usuário não autorizado");
                return;
            }

            request.setAttribute("idUser", user.getId());
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
