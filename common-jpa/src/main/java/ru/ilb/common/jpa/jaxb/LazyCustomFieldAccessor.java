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

//import com.sun.xml.bind.api.AccessorException;
//import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.internal.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.internal.bind.v2.runtime.reflect.Accessor;
import java.lang.reflect.Field;
import org.eclipse.persistence.indirection.IndirectContainer;

/**
 *
 * @author slavb

 */
public class LazyCustomFieldAccessor {

    /**
     *
     * @author slavb
     * @param <BeanT>
     * @param <ValueT>
     */
    public static class FieldReflection<BeanT, ValueT> extends Accessor.FieldReflection<BeanT, ValueT> {

        public FieldReflection(Field f) {
            super(f);
        }

        public FieldReflection(Field f, boolean supressAccessorWarnings) {
            super(f, supressAccessorWarnings);
        }

        @Override
        public ValueT get(BeanT bean) {
            try {
                Object value = f.get(bean);
                if (value != null && value instanceof IndirectContainer && !((IndirectContainer) value).isInstantiated()) {
                    return null;
                } else {
                    return super.get(bean);
                }
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            }
        }

        @Override
        public void set(BeanT bean, ValueT value) {
            super.set(bean, value);
        }

        @Override
        public Accessor<BeanT, ValueT> optimize(JAXBContextImpl context) {
            return super.optimize(context);
        }

    }

    public static final class ReadOnlyFieldReflection<BeanT, ValueT> extends FieldReflection<BeanT, ValueT> {
        public ReadOnlyFieldReflection(Field f, boolean supressAccessorWarnings) {
            super(f, supressAccessorWarnings);
        }
        public ReadOnlyFieldReflection(Field f) {
            super(f);
        }

        @Override
        public void set(BeanT bean, ValueT value) {
            // noop
        }

        @Override
        public Accessor<BeanT, ValueT> optimize(JAXBContextImpl context) {
            return this;
        }
    }
}
