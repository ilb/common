/*
 * Copyright 2016 Bystrobank
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
package ru.ilb.common.jaxrs.providers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

/**
 * @author slavb
 */
@Provider
public class MapParamConverterProvider implements ParamConverterProvider {

    private final Map< Class< ?>, ParamConverter< ?>> converters = new HashMap<>();

    public MapParamConverterProvider() {
        this.converters.put(java.util.Date.class, new ru.ilb.common.jaxrs.converters.DateParamConverter());
        this.converters.put(java.time.LocalDate.class, new ru.ilb.common.jaxrs.converters.LocalDateParamConverter());
        this.converters.put(java.time.LocalDateTime.class, new ru.ilb.common.jaxrs.converters.LocalDateTimeParamConverter());
    }

    public void setConverters(Map<Class, Class> converters) {
        try {
            for (Map.Entry<Class, Class> entry : converters.entrySet()) {
                this.converters.put(entry.getKey(), (ParamConverter< ?>) entry.getValue().newInstance());

            }
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        for (final Class< ?> type : converters.keySet()) {
            if (type.isAssignableFrom(rawType)) {
                return (ParamConverter<T>) converters.get(type);
            }
        }

        return null;
    }

}
