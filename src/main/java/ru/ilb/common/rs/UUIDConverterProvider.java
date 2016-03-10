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
package ru.ilb.common.rs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.UUID;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author slavb
 */
@Provider
public class UUIDConverterProvider implements ParamConverterProvider {

    private static class UUIDParamConverter implements ParamConverter<UUID> {
        @Override
        public UUID fromString(String s) {
            return UUIDConverter.parseUUID(s);
        }

        @Override
        public String toString(UUID arg0) {
            return UUIDConverter.printUUID(arg0);
        }
    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType == UUID.class) {
            return (ParamConverter<T>) new UUIDParamConverter();
        }

        return null;
    }

}
