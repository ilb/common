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
package ru.ilb.common.jaxrs.async;

import java.util.Date;
import java.util.concurrent.Future;

/**
 *
 * @author slavb
 */
public class AsyncTask {
    protected String taskId;
    protected Date start;
    protected Future future;

    public AsyncTask(String taskId, Future future) {
        this.taskId = taskId;
        this.start = new Date();
        this.future = future;
    }

    public String getTaskId() {
        return taskId;
    }

    public Date getStart() {
        return start;
    }

    public Future getFuture() {
        return future;
    }



}
