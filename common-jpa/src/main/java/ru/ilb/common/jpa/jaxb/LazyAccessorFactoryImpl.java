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
//import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.internal.bind.AccessorFactory;
import com.sun.xml.internal.bind.v2.runtime.reflect.Accessor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 *
 * @author slavb
 */
public class LazyAccessorFactoryImpl implements AccessorFactory {

    private static boolean indirectContainer = false;
    static {
        try {
            Class ic=Class.forName("org.eclipse.persistence.indirection.IndirectContainer");
            indirectContainer=true;
        } catch (ClassNotFoundException ex) {

        }
    }

    public LazyAccessorFactoryImpl() {
    }

    @Override
    public Accessor createFieldAccessor(Class bean, Field field, boolean readOnly) {
        if (indirectContainer) {
            return readOnly
                    ? new LazyCustomFieldAccessor.ReadOnlyFieldReflection(field)
                    : new LazyCustomFieldAccessor.FieldReflection(field);
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
