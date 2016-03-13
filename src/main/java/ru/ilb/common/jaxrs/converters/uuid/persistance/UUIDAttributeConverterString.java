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
package ru.ilb.common.jaxrs.converters.uuid.persistance;

import java.util.UUID;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 *
 * @author rusanov
 */
@Converter
public class UUIDAttributeConverterString implements AttributeConverter<UUID, String> {

    @Override
    public String convertToDatabaseColumn(UUID uuid) {
        if(uuid == null){
            return null;
        }
        return uuid.toString().toUpperCase();
    }

    @Override
    public UUID convertToEntityAttribute(String uuid) {
        if(uuid == null || uuid.length() == 0){
            return null;
        }
        return UUID.fromString(uuid);
    }

}
