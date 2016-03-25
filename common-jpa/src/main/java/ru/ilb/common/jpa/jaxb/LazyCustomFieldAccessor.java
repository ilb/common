/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ilb.common.jpa.jaxb;

//import com.sun.xml.bind.api.AccessorException;
//import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.internal.bind.api.AccessorException;
import com.sun.xml.internal.bind.v2.runtime.reflect.Accessor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.persistence.indirection.IndirectContainer;

/**
 *
 * @author slavb
 * @param <BeanT>
 * @param <ValueT>
 */
public class LazyCustomFieldAccessor<BeanT, ValueT> extends Accessor<BeanT, ValueT> {

    public LazyCustomFieldAccessor(Class<ValueT> valueType) {
        super(valueType);
    }

    private Field field;

    public LazyCustomFieldAccessor(Class<ValueT> type, Field field) {
        super(type);
        this.field = field;
    }

    @Override
    public ValueT get(BeanT bean) throws AccessorException {
        try {
            Object value = field.get(bean);
            if (value != null) {
                if (!(value instanceof IndirectContainer) || ((IndirectContainer) value).isInstantiated()) {
                    return (ValueT) value;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    @Override
    public void set(BeanT bean, ValueT value) throws AccessorException {
        try {
            if (value == null) {
                value = (ValueT) uninitializedValues.get(valueType);
            }
            field.set(bean, value);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    /**
     * Uninitialized map keyed by their classes.
     */
    private static final Map<Class, Object> uninitializedValues = new HashMap<Class, Object>();

    static {
        uninitializedValues.put(byte.class, Byte.valueOf((byte) 0));
        uninitializedValues.put(boolean.class, false);
        uninitializedValues.put(char.class, Character.valueOf((char) 0));
        uninitializedValues.put(float.class, Float.valueOf(0));
        uninitializedValues.put(double.class, Double.valueOf(0));
        uninitializedValues.put(int.class, Integer.valueOf(0));
        uninitializedValues.put(long.class, Long.valueOf(0));
        uninitializedValues.put(short.class, Short.valueOf((short) 0));
    }
}
