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
package ru.ilb.common.jpa.converters.eclipselink;

import java.util.UUID;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.DirectCollectionMapping;
import org.eclipse.persistence.sessions.Session;
import ru.ilb.common.jpa.converters.UUIDConverter;

/**
 *
 * @author slavb
 */
public class UUIDConverterMysql implements org.eclipse.persistence.mappings.converters.Converter{ 

    @Override

    public Object convertObjectValueToDataValue(Object objectValue, Session session) {
        return UUIDConverter.uuidToBytes((UUID) objectValue);
    }

    @Override

    public UUID convertDataValueToObjectValue(Object dataValue, Session session) {
        return UUIDConverter.bytesToUUID((byte[]) dataValue);
    }

    @Override

    public boolean isMutable() {
        return true;
    }

    @Override

    public void initialize(DatabaseMapping mapping, Session session) {

        final DatabaseField field;
        if (mapping instanceof DirectCollectionMapping) {
            // handle @ElementCollection...
            field = ((DirectCollectionMapping) mapping).getDirectField();
        } else {
            field = mapping.getField();
        }
        field.setSqlType(java.sql.Types.OTHER);
        field.setTypeName("java.util.UUID");
        String columnDefinition = "BINARY(16)";
        if(!field.isNullable()) {
            columnDefinition+= " NOT NULL";
        }
        field.setColumnDefinition(columnDefinition);
    }

}
