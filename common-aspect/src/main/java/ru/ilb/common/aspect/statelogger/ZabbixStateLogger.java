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
package ru.ilb.common.aspect.statelogger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author shadrin_nv
 */
public class ZabbixStateLogger extends StateFileLogger {
    private static final String ZABBIX_LOG_FORMAT = "%s:::%s:::%s:::%s";
    private static final String START = "START";
    private static final String WORKING = "WORKING";
    private static final String END = "END";


    @Override
    public void controller(String controller) {
        controller = System.getProperty("catalina.base") + "/logs/zabix/" + controller + ".log";
        super.controller(controller);
    }

    @Override
    public void start() {
        saveToFile(getZabbixLogFormattedString(START, null, null));
    }

    @Override
    public void working(Long errorCode, String msg) {
        saveToFile(getZabbixLogFormattedString(WORKING, errorCode, msg));
    }

    @Override
    public void end() {
        saveToFile(getZabbixLogFormattedString(END, null, null));
    }

    private String getZabbixLogFormattedString(String state, Long errorCode, String msg) {
        /*
        * ‹DATE_ISO8601›:::‹StatusMsg›:::‹ErrorCode›:::[ErrorMsg]
        * <StatusMsg> - START, WORKING, END
        * <ErrorCode> 0 - норма, отрицательные - системные, положительные - приложения
        */
        return String.format(ZABBIX_LOG_FORMAT,
                DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()),
                state,
                errorCode == null ? 0 : errorCode,
                msg == null ? "" : msg);
    }

}
