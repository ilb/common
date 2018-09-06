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
package ru.ilb.common.jaxb.adapters;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author shadrin_nv
 */
public class LocalDateTimeXmlAdapter extends XmlAdapter<String, LocalDateTime> {

    @Override
    public LocalDateTime unmarshal(String value) throws Exception {
        if (value == null || value.length() == 0) {
            return null;
        }
        try {
            Calendar parsedDateTime = DatatypeConverter.parseDateTime(value);
            TimeZone tz = parsedDateTime.getTimeZone();
            ZoneId zid = tz == null ? ZoneId.systemDefault() : tz.toZoneId();
            return LocalDateTime.ofInstant(parsedDateTime.toInstant(), zid);
        } catch (Exception ex) {
        }
        try {
            /* for ISO_ZONED_DATE_TIME 	Zoned Date Time 	'2011-12-03T10:15:30+01:00[Europe/Paris]'
                   ISO_DATE_TIME 	Date and time with ZoneId 	'2011-12-03T10:15:30+01:00[Europe/Paris]'
            */
            ZonedDateTime parsedDateTime = ZonedDateTime.parse(value, DateTimeFormatter.ISO_ZONED_DATE_TIME);
            ZoneId zid = parsedDateTime.getZone() == null ? ZoneId.systemDefault() : parsedDateTime.getZone();
            return LocalDateTime.ofInstant(parsedDateTime.toInstant(), zid);
        } catch (Exception ex) {
        }
        return null;
    }

    @Override
    public String marshal(LocalDateTime value) throws Exception {
        if (value == null) {
            return null;
        }
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.of(value, OffsetDateTime.now().getOffset()));
    }

}
