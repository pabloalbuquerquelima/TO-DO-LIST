package br.com.pabloalbuquerque.todolist.task;

import br.com.pabloalbuquerque.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private static final String ID_USER_ATTRIBUTE = "idUser";


    final ITaskRepository taskRepository;

    public TaskController(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @PostMapping("/")
    public ResponseEntity<Object> createTask(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        taskModel.setIdUser((UUID) request.getAttribute(ID_USER_ATTRIBUTE));


        LocalDateTime currentDate = LocalDateTime.now();

        if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Data de inicio ou de término da tarefa nao pode ser menor que a data atual");
        }
        if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Data de inicio nao pode ser maior que a data de termino");
        }

        TaskModel taskCreated = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.OK).body(taskCreated);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateTask(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
        String idUserString = request.getAttribute(ID_USER_ATTRIBUTE).toString();
        UUID idUser = UUID.fromString(idUserString);


        var task = this.taskRepository.findById(id).orElse(null);

        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Tarefa não encontrada");
        }

        if (!task.getIdUser().equals(idUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Você não tem permissão para excluir esta tarefa");
        }

        Utils.copyNonNullProperties(taskModel, task);

        TaskModel taskUpdated = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.OK).body(taskUpdated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteTask(HttpServletRequest request, @PathVariable UUID id) {
        String idUserString = request.getAttribute(ID_USER_ATTRIBUTE).toString();
        UUID idUser = UUID.fromString(idUserString);

        TaskModel task = this.taskRepository.findById(id).orElse(null);

        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Tarefa não encontrada");
        }

        if (!task.getIdUser().equals(idUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Você não tem permissão para excluir esta tarefa");
        }

        this.taskRepository.delete(task);
        return ResponseEntity.status(HttpStatus.OK).body("Tarefa excluída com sucesso");
    }

    @GetMapping("/")
    public ResponseEntity<Object> listTask(HttpServletRequest request) {
        String idUserString = request.getAttribute(ID_USER_ATTRIBUTE).toString();
        UUID idUser = UUID.fromString(idUserString);

        List<TaskModel> tasks = this.taskRepository.findByIdUser(idUser);
        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }
}
