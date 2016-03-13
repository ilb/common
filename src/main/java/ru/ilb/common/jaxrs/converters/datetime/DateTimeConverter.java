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
package ru.ilb.common.jaxrs.converters.datetime;

import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.bind.DatatypeConverter;

/**
 * @author slavb
 */
class DateTimeConverter {

    public static Date fromString(String s) {
        if (s == null || s.length() == 0) {
            return null;
        }
        Date date;
        try {
            date = DatatypeConverter.parseDateTime(s).getTime();
        } catch (IllegalArgumentException ex) {
            date = null;
        }
        return date;
    }

    public static String toString(Date arg0) {
        if (arg0 == null) {
            return null;
        }
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(arg0);
        return DatatypeConverter.printDateTime(gc);

    }

}
