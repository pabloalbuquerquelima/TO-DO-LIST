package br.com.pabloalbuquerque.todolist.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

public class Utils {
    public static void copyNonNullProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper sourceWrapper = new BeanWrapperImpl(source);

        PropertyDescriptor[] propertyDescriptions = sourceWrapper.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();

        for (PropertyDescriptor property : propertyDescriptions) {
            Object sourceWrapperValue = sourceWrapper.getPropertyValue(property.getName());

            if (sourceWrapperValue == null) emptyNames.add(property.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
}
