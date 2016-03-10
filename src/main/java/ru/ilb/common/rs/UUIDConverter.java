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

import java.util.UUID;

/**
 * <jaxb:javaType name="java.util.UUID"
 * parseMethod="ru.ilb.common.rs.UUIDConverter.parseUUID"
 * printMethod="ru.ilb.common.rs.UUIDConverter.printUUID"
 * />
 *
 * @author slavb
 */
public class UUIDConverter {

    public static UUID parseUUID(String s) {
        if (s == null || s.length() == 0) {
            return null;
        }
        return UUID.fromString(s);
    }

    public static String printUUID(UUID arg0) {
        if (arg0 == null) {
            return null;
        }
        return arg0.toString();
    }

}
