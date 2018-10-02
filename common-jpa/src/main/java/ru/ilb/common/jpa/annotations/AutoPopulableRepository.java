/*
 * Copyright 2017 slavb.
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
package ru.ilb.common.jpa.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import org.eclipse.persistence.mappings.DatabaseMapping;

/**
 * Репозиторий с автоматическим пополнением базы по значениям в статических полях
 * @author slavb
 */
@Target({ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoPopulableRepository {
    PopulateMode mode() default PopulateMode.SIMPLE;

    Class<? extends DatabaseMapping>[] mappingTypes() default {};
    
    public static enum PopulateMode {
        SIMPLE,
        FINDALL
    }
}

