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
package ru.ilb.common.jaxrs.exceptionhandler;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.apache.cxf.jaxrs.impl.ResponseImpl;
import org.apache.cxf.message.Message;

/**
 *
 * @author slavb
 */
@Provider
public class WebApplicationExceptionHandler implements ExceptionMapper<WebApplicationException> {

    private static final Logger LOG = Logger.getLogger(WebApplicationExceptionHandler.class.getName());

    private final String UNCLASSIFIABLE_SERVER_ERROR_TEXT = "Unclassifiable server error";
    private final int CLIENT_WEB_ERROR_START = 450;
    private final int CLIENT_ERROR_LOW_CODE = 100;

    @Override
    public Response toResponse(WebApplicationException ex) {
        // Код ответа
        Response r = ex.getResponse();
        int responseStatus;
        if (r != null) {
            responseStatus = r.getStatus();
        } else {
            responseStatus = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        }

        // Текст ошибки
        String outMess = ex.getMessage();
        try {
            if (r != null && r.getEntity() != null) {
                outMess = r.readEntity(String.class);
            }
            if (ex instanceof ClientErrorException && r instanceof ResponseImpl) {
                Message m = ((ResponseImpl) r).getOutMessage();
                if (m != null) {
                    if (m.get("org.apache.cxf.request.uri") != null) {
                        outMess += System.lineSeparator() + "Request URI: " + m.get("org.apache.cxf.request.uri");
                    }
                }
            }
        } catch (Throwable ex_) {
            LOG.log(Level.SEVERE, "error on getting  addinional exception info", ex_);
        }
        if (outMess == null || outMess.isEmpty()) {
            outMess = ex.toString();
        }

        LOG.log(Level.WARNING, outMess, ex);

        // Транслируем код ответа
        Response.StatusType status;
        String outMessClient;
        if (responseStatus >= Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() ||
                responseStatus < CLIENT_ERROR_LOW_CODE) {
            // Ошибки до 100 и после (вкл) 500 траслируем в 500
            status = Response.Status.INTERNAL_SERVER_ERROR;
            outMessClient = Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase();
        } else {
            if (ex.getCause() != null && ex.getCause() instanceof java.lang.IllegalArgumentException
                    && responseStatus == Response.Status.NOT_FOUND.getStatusCode()) {
                // если 404 из-за не корректно введеного аргумента в URL, то транслируем в 450
                outMessClient = UNCLASSIFIABLE_SERVER_ERROR_TEXT;
                status = new CustomResponseStatus(CLIENT_WEB_ERROR_START, outMessClient);
            } else {
                // Всё остальное - как есть
                outMessClient = outMess;
                status = new CustomResponseStatus(responseStatus, outMessClient);
            }

        }

        return Response.status(status).entity(outMessClient).header("Content-Type", MediaType.TEXT_PLAIN + ";charset=UTF-8").build();
    }
}
