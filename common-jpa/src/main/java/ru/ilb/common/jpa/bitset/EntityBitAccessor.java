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
public class EntityBitAccessor implements BitAccessor {

    protected final Field idField;

    public EntityBitAccessor(Class clazz) {
        idField = findField(clazz, Id.class);
    }

    @Override
    public int getBitNum(Object o) {
        return getId(o).intValue() - 1;
    }

    private Integer getId(Object o) {
        try {
            Object obj = idField.get(o);
            if (obj instanceof Long){
                return ((Long) obj).intValue();
            } else if (obj instanceof Integer) {
                return ((Integer) obj).intValue();
            } else if (obj instanceof Byte) {
                return ((Byte) obj).intValue();
            } else if (obj instanceof Short) {
                return ((Short) obj).intValue();
            }
            return  Integer.parseInt(String.valueOf(obj));
        } catch (Throwable ex) {
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
