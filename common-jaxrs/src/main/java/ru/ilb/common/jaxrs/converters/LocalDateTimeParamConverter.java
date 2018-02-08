/*
 * Copyright 2016 slavb.
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
package ru.ilb.common.jaxrs.converters;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.ws.rs.ext.ParamConverter;

/**
 *
 * @author slavb
 */
public class LocalDateTimeParamConverter implements ParamConverter<LocalDateTime> {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public LocalDateTime fromString(String value) {
        if (value == null || value.length() == 0) {
            return null;
        }
        return LocalDateTime.parse(value, DATE_FORMAT);
    }

    @Override
    public String toString(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return DATE_FORMAT.format(value);

    }
}
