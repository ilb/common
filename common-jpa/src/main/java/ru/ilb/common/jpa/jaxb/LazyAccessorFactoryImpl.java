/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ilb.common.jpa.jaxb;

//import com.sun.xml.bind.AccessorFactory;
//import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.internal.bind.AccessorFactory;
import com.sun.xml.internal.bind.v2.runtime.reflect.Accessor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.persistence.EntityManager;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.sessions.Session;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author slavb
 */
@Configurable
public class LazyAccessorFactoryImpl implements AccessorFactory {

    protected EntityManager entityManager;

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public LazyAccessorFactoryImpl() {
    }

    @Override
    @Transactional
    public Accessor createFieldAccessor(Class bean, Field field, boolean readOnly) {
        //- use my custom accessor
        ClassDescriptor cd = entityManager.unwrap(Session.class).getDescriptor(bean);
        DatabaseMapping dm = cd != null
                ? cd.getMappingForAttributeName(field.getName())
                : null;
        if (dm != null && dm.isLazy()) {
            return new LazyCustomFieldAccessor(bean, field);
        } else {
            return readOnly
                    ? new Accessor.ReadOnlyFieldReflection(field)
                    : new Accessor.FieldReflection(field);
        }
    }

    @Override
    public Accessor createPropertyAccessor(Class bean, Method getter, Method setter) {
        //-- Use Jaxb's default accessors.
        if (getter == null) {
            return new Accessor.SetterOnlyReflection(setter);
        }
        if (setter == null) {
            return new Accessor.GetterOnlyReflection(getter);
        }
        return new Accessor.GetterSetterReflection(getter, setter);
    }
}
