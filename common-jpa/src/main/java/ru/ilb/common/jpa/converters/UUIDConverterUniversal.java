/*
 * Copyright 2021 slavb.
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
package ru.ilb.common.jpa.converters;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.DirectCollectionMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;

public class UUIDConverterUniversal implements Converter {

    private Boolean isUUIDasByteArray = true;

    @Override
    public Object convertObjectValueToDataValue(Object objectValue,
            Session session) {
        if (isUUIDasByteArray) {
            UUID uuid = (UUID) objectValue;
            if (uuid == null) {
                return null;
            }
            byte[] buffer = new byte[16];
            ByteBuffer bb = ByteBuffer.wrap(buffer);
            bb.putLong(uuid.getMostSignificantBits());
            bb.putLong(uuid.getLeastSignificantBits());
            return buffer;
        }
        return objectValue;
    }

    @Override
    public UUID convertDataValueToObjectValue(Object dataValue,
            Session session) {
        if (isUUIDasByteArray) {
            byte[] bytes = (byte[]) dataValue;
            if (bytes == null) {
                return null;
            }
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            long high = bb.getLong();
            long low = bb.getLong();
            return new UUID(high, low);
        }
        return (UUID) dataValue;
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

        if (session != null && session.getLogin() != null && session.getLogin().getPlatform() != null) {
            String platform = session.getLogin().getPlatform().getClass().getSimpleName();

            switch (platform) {
                case "PostgreSQLPlatform":
                    field.setSqlType(java.sql.Types.OTHER);
                    field.setTypeName("java.util.UUID");
                    field.setColumnDefinition("UUID");
                    isUUIDasByteArray = false;
                    break;
                case "H2Platform":
                    field.setColumnDefinition("UUID");
                    break;
                case "OraclePlatform":
                    field.setColumnDefinition("RAW(16)");
                    break;
                case "MySQLPlatform":
                    field.setColumnDefinition("BINARY(16)");
                    break;
                case "SQLServerPlatform":
                    field.setColumnDefinition("UNIQUEIDENTIFIER");
                    break;
                default:
                    break;
            }
        }

    }
}
