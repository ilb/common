/*
 * Copyright 2018 shadrin_nv.
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
package ru.ilb.common.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import ru.ilb.common.aspect.statelogger.BaseStateLogger;

/**
 *
 * @author shadrin_nv
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UnderLoggerControl {
    static final String DEFAULT_CONTROL_NAME = "default";

    /**
     * Класс логгер для формирования сообщений. Не обязательный
     * @return
     */
    public Class<?> loggerClass() default BaseStateLogger.class;

    /**
     * Наименование контроллера.
     * @return
     */
    public String controller() default DEFAULT_CONTROL_NAME;

    /**
     * Автоматически уведомлять "админа" об исключении
     * @return
     */
    public boolean autoAdminExceptionNotification() default true;
}
