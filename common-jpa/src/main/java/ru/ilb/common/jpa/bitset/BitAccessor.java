/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ilb.common.jpa.bitset;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import javax.persistence.Id;

/**
 *
 * @author slavb
 */
public class BitAccessor {

    protected final Field idField;

    public BitAccessor(Class clazz) {
        idField = findField(clazz, Id.class);
    }

    public Long getBitNum(Object o) {
        return getId(o) - 1;
    }

    private Long getId(Object o) {
        try {
            return (Long) idField.get(o);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            return null;
        }
    }

    private Field findField(Class<?> classs, Class<? extends Annotation> ann) {
        Class<?> c = classs;
        while (c != null) {
            for (Field field : c.getDeclaredFields()) {
                if (field.isAnnotationPresent(ann)) {
                    field.setAccessible(true); //FIXME, переделать на что то более красивое
                    return field;
                }
            }
            c = c.getSuperclass();
        }
        return null;
    }

}
