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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author shadrin_nv
 */
public abstract class StateFileLogger extends BaseStateLogger {
    private String fileName;
    private File file;

    @Override
    public void controller(String controller) {
        this.fileName = controller;
        super.controller(controller);
    }

    private boolean initFile() {
        file = new File(fileName);
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            return true;
        } catch (IOException ex) {
            LOG.error("StateFileLogger: Ошибка инициализации файла " + fileName, ex);
        }
        return false;
    }

    public void saveToFile(String info) {
        if (initFile()) {
            try {
                try (FileWriter writer = new FileWriter(file.getAbsoluteFile(), true)) {
                    writer.write(info + "\n");
                    writer.flush();
                    writer.close();
                }
            } catch (IOException ex) {
                LOG.error("StateFileLogger: Ошибка записи в " + fileName, ex);
            }
        }
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return this.fileName;
    }
}
