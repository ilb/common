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

    @Override
    public Response toResponse(WebApplicationException ex) {
        int responseStatus = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        String outMess = ex.getMessage();
        Response r = ex.getResponse();
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
            } else {
                responseStatus = ex.getResponse().getStatus(); // клиентские статусы не передаем напрямую
            }

        } catch (Throwable ex_) {
            LOG.log(Level.SEVERE, "error on getting  addinional exception info", ex_);
        }
        if (outMess == null || outMess.isEmpty()) {
            outMess = ex.toString();
        }
        LOG.log(Level.WARNING, outMess, ex);
        Response.StatusType status = Response.Status.fromStatusCode(responseStatus);
        String outMessClient = responseStatus >= Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() ? "Internal server error" : outMess;
        if (status == null) {
            status = new CustomResponseStatus(responseStatus, outMessClient);
        }

        return Response.status(status).entity(outMessClient).type(MediaType.TEXT_PLAIN).build();

    }

}
