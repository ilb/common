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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author slavb
 */
public class AsyncTaskManager {

    private final Map<String, AsyncTask> tasks = new ConcurrentHashMap<>();
    private final ExecutorService taskExecutor = Executors.newCachedThreadPool();

    public void destroy() {
        for (Map.Entry<String, AsyncTask> entry : tasks.entrySet()) {
            if (!entry.getValue().getFuture().isDone()) {
                entry.getValue().getFuture().cancel(true);
            }
        }

    }

    public AsyncTask submit(Callable clbl) {
        String taskId = UUID.randomUUID().toString();
        Future future = taskExecutor.submit(clbl);
        AsyncTask task = new AsyncTask(taskId, future);
        tasks.put(taskId, task);
        return task;
    }

    public AsyncTask get(String taskId) {
        return tasks.get(taskId);
    }

    public Response execute(Callable clbl, UriInfo uriInfo) {
        Response response;
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        String mode = queryParams.getFirst("mode");
        String taskId = queryParams.getFirst("taskId");
        String i = queryParams.getFirst("i");
        if ("wait".equals(mode)) {
            response = wait(taskId, uriInfo, i);
        } else if ("cancel".equals(mode)) {
            response = cancel(taskId);
        } else {
            response = submit(clbl, uriInfo);
        }
        return response;

    }

    public Response submit(Callable clbl, UriInfo uriInfo) {
        AsyncTask task = submit(clbl);
        UriBuilder uri = uriInfo.getAbsolutePathBuilder().queryParam("mode", "wait").queryParam("taskId", task.getTaskId()).queryParam("i", 0);
        Response response = Response.seeOther(uri.build()).build();
        return response;
    }

    public Response wait(String taskId, UriInfo uriInfo, String i) {
        AsyncTask task = get(taskId);
        Response response;
        if (task == null) {
            //Returns 410, “Gone” if job doesn’t exist anymore
            response = Response.status(Response.Status.GONE).type("text/plain; charset=UTF-8").entity("Задача " + taskId + " не найдена").build();

        } else if (!task.getFuture().isDone()) {
            UriBuilder uri = uriInfo.getAbsolutePathBuilder().queryParam("mode", "wait").queryParam("taskId", taskId);
            if (i != null) {
                uri = uri.queryParam("i", Integer.parseInt(i) + 1);
            }
            response = Response.status(Response.Status.ACCEPTED).type("text/plain; charset=UTF-8").entity("Выполняется.. ").header("Refresh", "2;" + uri.build().toString()).build();
        } else {
            try {
                if (task.getFuture().get() instanceof Response) {
                    response = (Response) task.getFuture().get();
                } else {
                    response = Response.status(Response.Status.OK).entity(task.getFuture().get()).type("application/xml").build();
                }
                remove(taskId);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            } catch (ExecutionException ex) {
                if (ex.getCause() != null) {
                    if (ex.getCause() instanceof RuntimeException) {
                        throw (RuntimeException) ex.getCause();
                    } else {
                        throw new RuntimeException(ex.getCause());
                    }
                } else {
                    throw new RuntimeException(ex);
                }
            }
        }
        return response;

    }

    public Response cancel(String taskId) {
        AsyncTask task = get(taskId);
        Response response;
        if (task == null) {
            //Returns 410, “Gone” if job doesn’t exist anymore
            response = Response.status(Response.Status.GONE).type("text/plain; charset=UTF-8").entity("Задача " + taskId + " не найдена").build();

        } else if (!task.getFuture().isDone()) {
            task.getFuture().cancel(true);
            response = Response.status(Response.Status.OK).type("text/plain; charset=UTF-8").entity("Задача " + taskId + " отменена").build();
            remove(taskId);
        } else {
            response = Response.status(Response.Status.OK).type("text/plain; charset=UTF-8").entity("Задача " + taskId + " уже завершена").build();
        }
        return response;
    }

    public AsyncTask remove(String taskId) {
        return tasks.remove(taskId);
    }

}
