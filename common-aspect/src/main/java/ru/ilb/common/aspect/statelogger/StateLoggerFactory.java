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
package ru.ilb.common.aspect.statelogger;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 *
 * @author shadrin_nv
 */
@Component
public class StateLoggerFactory {
    Map<String, BaseStateLogger> hash = new HashMap<>();


    public BaseStateLogger getStateLogger(String controller, Class<?> clazz) {
        BaseStateLogger logger = hash.get(controller);

        if (logger == null && BaseStateLogger.class.isAssignableFrom(clazz)) {
            try {
                logger = (BaseStateLogger) clazz.newInstance();
                logger.controller(controller);
                hash.put(controller, logger);
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }

        return logger;
    }
}
