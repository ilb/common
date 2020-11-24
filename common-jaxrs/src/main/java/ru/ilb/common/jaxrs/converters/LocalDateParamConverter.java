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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.ws.rs.ext.ParamConverter;

/**
 *
 * @author klimovskih
 */
public class LocalDateParamConverter implements ParamConverter<LocalDate> {

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public LocalDate fromString(String value) {
        if (value == null || value.length() == 0) {
            return null;
        }
        return LocalDate.parse(value, dateFormat);
    }

    @Override
    public String toString(LocalDate value) {
        if (value == null) {
            return null;
        }
        return dateFormat.format(value);

    }
}
