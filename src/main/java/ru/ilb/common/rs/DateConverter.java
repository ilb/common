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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.bind.DatatypeConverter;

/**
 * @author slavb
 */
public class DateConverter {

        public static Date parseDate(String s) {
            if (s == null || s.length() == 0) {
                return null;
            }
            Date date;
            try {
                date=DatatypeConverter.parseDate(s).getTime();
            } catch (IllegalArgumentException ex){
                date=null;
            }
            return date;
        }
        public static Date parseDateTime(String s) {
            if (s == null || s.length() == 0) {
                return null;
            }
            Date date;
            try {
                date=DatatypeConverter.parseDateTime(s).getTime();
            } catch (IllegalArgumentException ex){
                date=null;
            }
            return date;
        }
        public static Date parseTime(String s) {
            if (s == null || s.length() == 0) {
                return null;
            }
            Date date;
            try {
                date=DatatypeConverter.parseTime(s).getTime();
            } catch (IllegalArgumentException ex){
                date=null;
            }
            return date;
        }

        public static String printDate(Date arg0) {
            if(arg0==null){
                return null;
            }
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            return df.format(arg0);

        }
        public static String printDateTime(Date arg0) {
            if(arg0==null){
                return null;
            }
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(arg0);
            return DatatypeConverter.printDateTime(gc);

        }
        public static String printTime(Date arg0) {
            if(arg0==null){
                return null;
            }
            DateFormat df = new SimpleDateFormat("HH:mm:ss");
            return df.format(arg0);
        }

}
