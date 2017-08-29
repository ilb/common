/*
 * Copyright 2016 Bystrobank.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.ilb.common.jpa.jaxb;

//import com.sun.xml.bind.AccessorFactory;
//import com.sun.xml.bind.AccessorFactoryImpl;
//import com.sun.xml.bind.api.AccessorException;
//import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import ru.ilb.common.jpa.tools.EclipseLinkUtils;
import com.sun.xml.internal.bind.AccessorFactory;
import com.sun.xml.internal.bind.AccessorFactoryImpl;
import com.sun.xml.internal.bind.api.AccessorException;
import com.sun.xml.internal.bind.v2.runtime.reflect.Accessor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.xml.bind.JAXBException;

/**
 * https://github.com/IIIkiper/habr/blob/master/HLS/src/main/java/ru/habr/zrd/hls/jaxb/JAXBHibernateAccessorFactory.java
 *
 * @author slavb
 */
public class LazyAccessorFactoryImpl implements AccessorFactory {

    private static boolean enabled = false;

    /*
	 * Реализация AccessorFactory уже написана - AccessorFactoryImpl. Судя по всему это singleton, 
	 * и отнаследоваться от него не получится, поэтому сделаем его делегатом и напишем wrapper.
     */
    private final AccessorFactory accessorFactory = AccessorFactoryImpl.getInstance();

    static {
        try {
            Class ic = Class.forName("org.eclipse.persistence.indirection.IndirectContainer");
            enabled = true;
        } catch (ClassNotFoundException ex) {
        }
    }

    /*
	 * Также потребуется некая реализация Accessor. Поскольку больше она нигде не нужна, сделаем
	 * ее в виде private inner class, чтобы не болталась по проекту.
     */
    private static class JAXBEclipseLinkAccessor<B, V> extends Accessor<B, V> {

        private final Accessor<B, V> accessor;

        public JAXBEclipseLinkAccessor(Accessor<B, V> accessor) {
            super(accessor.getValueType());
            this.accessor = accessor;
        }

        @Override
        public V get(B bean) throws AccessorException {
            V value = accessor.get(bean);
            /*
			 * Вот оно! Ради этого весь сыр-бор. Если кому-то простое зануление может показаться неправильным,
			 * он волен сделать тут все, что захочется.
			 * Метод Hibernate.isInitialized() c одинаковым поведением присутствует и в Hibernate3,  и Hibernate4. 
             */
            return EclipseLinkUtils.isInitialized(value) ? value : null;
        }

        @Override
        public void set(B bean, V value) throws AccessorException {
            accessor.set(bean, value);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Accessor createFieldAccessor(Class bean, Field field, boolean readOnly) throws JAXBException {
        Accessor accessor = accessorFactory.createFieldAccessor(bean, field, readOnly);
        return enabled ? new JAXBEclipseLinkAccessor(accessor) : accessor;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Accessor createPropertyAccessor(Class bean, Method getter, Method setter) throws JAXBException {
        Accessor accessor = accessorFactory.createPropertyAccessor(bean, getter, setter);
        return enabled ? new JAXBEclipseLinkAccessor(accessor) : accessor;
    }


}
