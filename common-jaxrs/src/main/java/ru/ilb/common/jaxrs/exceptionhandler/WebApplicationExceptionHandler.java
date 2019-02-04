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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import org.apache.cxf.jaxrs.impl.ResponseImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import ru.ilb.common.jaxb.util.JaxbUtil;

/**
 * To catch cascade jax-rs client exceptions, set property
 * &lt;entry key="support.wae.spec.optimization" value="false"/>
 *
 * @author slavb
 *
 */
@Provider
public class WebApplicationExceptionHandler implements ExceptionMapper<WebApplicationException> {

    @Context
    private ContextResolver<JAXBContext> jaxbContextResolver;

    private static final Logger LOG = Logger.getLogger(WebApplicationExceptionHandler.class.getName());

    private final String UNCLASSIFIABLE_SERVER_ERROR_TEXT = "Unclassifiable server error";
    private final int CLIENT_HTTP_ERROR = 450;
    private final int CASCADE_HTTP_ERROR = 555;

    private static final String KEY_EXCHANGE_ID = "exchangeId";

    @Override
    public Response toResponse(WebApplicationException ex) {
        // http response code
        Response response = ex.getResponse();
        int responseStatus;
        if (response != null) {
            responseStatus = response.getStatus();
        } else {
            responseStatus = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        }
        Message currentMessage = PhaseInterceptorChain.getCurrentMessage();
        String exchangeId = currentMessage != null && currentMessage.getExchange() != null ? (String) currentMessage.getExchange().get(KEY_EXCHANGE_ID) : "(no current exchange)";

        // message should be shown to client
        StringBuilder message = new StringBuilder();
        // additional message should be logged
        StringBuilder logstr = new StringBuilder();

        try {
            // reparse error response if exists
            if (response != null && response.getEntity() != null) {
                Message outMessage = null;
                // cascade exception from jax-rs client
                boolean cascade = false;
                if (response instanceof ResponseImpl) {
                    outMessage = ((ResponseImpl) response).getOutMessage();
                    if (outMessage != null) {
                        cascade = Boolean.TRUE.equals(outMessage.get("org.apache.cxf.client"));
                    }
                }
                // cascade exception should be masked
                if (cascade) {
                    message.append("Cascade http error ").append(responseStatus);
                    message.append(" from ").append(outMessage.get("org.apache.cxf.request.uri"));

                    // original response should be logged, not shown to client
                    if (response.getEntity() != null) {
                        logstr.append("Cascade response:")
                                .append(response.readEntity(String.class));
                    }
                    //replace proxied http code
                    responseStatus = CASCADE_HTTP_ERROR;

                    if (jaxbContextResolver != null && outMessage.get("java.util.List") != null) {
                        //log request object
                        List list = (List) outMessage.get("java.util.List");
                        String contentType = (String) outMessage.get("Content-Type");
                        if (MediaType.APPLICATION_XML.equals(contentType) || MediaType.APPLICATION_JSON.equals(contentType)) {
                            for (Object object : list) {
                                logstr.append("\nObject: ").append(JaxbUtil.marshal(jaxbContextResolver.getContext(object.getClass()), object, contentType));
                            }
                        }
                    }

                } else {
                    message.append(response.readEntity(String.class));
                }
            } else {
                message.append(ex.getMessage());
            }
        } catch (Throwable ex_) {
            LOG.log(Level.SEVERE, "error on getting  addinional exception info", ex_);
        }

        if (message.length() == 0) {
            message.append(UNCLASSIFIABLE_SERVER_ERROR_TEXT);
            logstr.append(ex.toString());
        }
        if (exchangeId != null) {
            message.append("\nExchange Id: ").append(exchangeId);
        }
        if (logstr.length() != 0) {
            logstr.append("\n");
        }
        logstr.append(message);

        LOG.log(Level.WARNING, logstr.toString(), ex);
        String outMess = message.toString();

        // транслируем код ответа
        Response.StatusType status;

        if (ex.getCause() != null && ex.getCause() instanceof java.lang.IllegalArgumentException
                && responseStatus == Response.Status.NOT_FOUND.getStatusCode()) {
            // если 404 из-за не корректно введеного аргумента в URL, то транслируем в 450
            outMess = UNCLASSIFIABLE_SERVER_ERROR_TEXT;
            status = new CustomResponseStatus(CLIENT_HTTP_ERROR, outMess);
        } else {
            // всё остальное - как есть
            status = new CustomResponseStatus(responseStatus, outMess);
        }

        return Response.status(status).entity(outMess).header("Content-Type", MediaType.TEXT_PLAIN + ";charset=UTF-8").build();
    }
}
