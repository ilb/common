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

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author slavb
 */
@Provider
public class ExceptionHandler implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(ExceptionHandler.class.getName());

    /**
     * default http response status
     */
    int defaultResponseStatus = 500;

    /**
     * default content type
     */
    String contentType = "text/plain; charset=UTF-8";

    public void setDefaultResponseStatus(int defaultResponseStatus) {
        this.defaultResponseStatus = defaultResponseStatus;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @param ex
     * @return
     */
    @Override
    public Response toResponse(Exception ex) {

        int responseStatus = defaultResponseStatus;
        String outMess = ex.getMessage();
        try {
            if (ex instanceof WebApplicationException) {
                Response r = ((WebApplicationException) ex).getResponse();
                if (r != null) {
                    if (r.getEntity() != null && r.getEntity() instanceof InputStream) {
                        outMess = new java.util.Scanner((InputStream) r.getEntity(), "UTF-8").useDelimiter("\\A").next();
                    }
                }
                responseStatus = ((WebApplicationException) ex).getResponse().getStatus();
            }
            if (ex instanceof BadRequestException && ex.getCause() != null && ex.getCause().getMessage() != null) {
                outMess = ex.getCause().getMessage();
            }

        } catch (Throwable ex_) {
            LOG.log(Level.SEVERE, "error on getting  addinional exception info", ex_);
        }
        if (outMess == null) {
            outMess = "";
        }
        LOG.log(Level.SEVERE, outMess, ex);

        return Response.status(responseStatus).entity(outMess).type(contentType).build();
    }

}
