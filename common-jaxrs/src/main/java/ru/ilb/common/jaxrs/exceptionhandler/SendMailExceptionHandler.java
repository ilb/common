/*
 * Copyright 2016 Bystrobank.
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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author slavb
 */
@Provider
public class SendMailExceptionHandler implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(SendMailExceptionHandler.class.getName());

    /**
     * default mail recipient
     */
    String mailTo = "root";

    /**
     * default mail subject
     */
    String mailSubject = "[JAVA FATAL ERROR]";
    /**
     * default mail command
     */
    String mailCommand = "/usr/sbin/sendmail";

    /**
     * default mail command
     */
    String mailCommandParams = "-t -oi";

    public SendMailExceptionHandler() {
        mailTo = getServerAdmin();
    }

    private String getServerAdmin() {
        String serverAdmin = null;
        // from jndi
        if (serverAdmin == null) {
            try {
                Context initCtx = new InitialContext();
                Context envCtx = (Context) initCtx.lookup("java:comp/env");
                serverAdmin = (String) envCtx.lookup("SERVER_ADMIN");
            } catch (NamingException e) {
            }
        }
        // from system property
        if (serverAdmin == null) {
            serverAdmin = System.getProperty("SERVER_ADMIN");
        }
        // from system environment
        if (serverAdmin == null) {
            serverAdmin = System.getenv("SERVER_ADMIN");
        }
        //otherwise root
        if (serverAdmin == null) {
            serverAdmin = "root";
        }
        return serverAdmin;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }

    public void setMailSubject(String mailSubject) {
        this.mailSubject = mailSubject;
    }

    public void setMailCommand(String mailCommand) {
        this.mailCommand = mailCommand;
    }

    public void setMailCommandParams(String mailCommandParams) {
        this.mailCommandParams = mailCommandParams;
    }

    @Override
    public Response toResponse(Exception ex) {
        String outMess = ex.getMessage();
        if (outMess == null || outMess.isEmpty()) {
            outMess = ex.toString();
        }
        LOG.log(Level.SEVERE, outMess, ex);

        String mailMsg = getMailMsg(ex, outMess);
        sendMailCheck(mailMsg);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outMess).header("Content-Type", MediaType.TEXT_PLAIN + ";charset=UTF-8").build();
    }

    private String getMailMsg(Exception ex, String outMess) {
        String mailMsg = "";
        try {
            //thread idntifier matches value in server log
            String threadid = Thread.currentThread().getName();
            //unique error identifier
            String uuid = UUID.randomUUID().toString();
            String cause = ex.getCause() == null ? ex.getClass().getName() : ex.getCause().getClass().getName();
            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            String trace = writer.toString();
            int line2 = trace.indexOf('\n');
            mailMsg = "To: " + mailTo + "\n" //From header sendmail should generate
                    + "Subject: " + mailSubject + " " + threadid + "\n"
                    + "Content-Type: " + MediaType.TEXT_PLAIN + "; charset=UTF-8\n\n"
                    + cause + ": " + outMess + "\n"
                    + " " + threadid + " " + uuid + "\n"
                    + (line2 < 0 ? trace : trace.substring(line2 + 1));
        } catch (Throwable ex_) {
            LOG.log(Level.SEVERE, "error constructing mailMsg", ex_);
        }
        return mailMsg;
    }

    private void sendMailCheck(String mailMsg) {
        if (Files.exists(Paths.get(mailCommand))) {
            if (Files.isExecutable(Paths.get(mailCommand))) {
                sendMail(mailMsg);
            } else {
                LOG.log(Level.SEVERE, "mail command {0} not executable", mailCommand);
            }
        } else {
            LOG.log(Level.SEVERE, "mail command {0} not exists", mailCommand);
        }
    }

    private void sendMail(String mailMsg) {
        try {
            //http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html
            //to capture strerr/stdout and write to stdin need to create separate threads - simplify
            Process p = Runtime.getRuntime().exec(mailCommand + " " + mailCommandParams);
            OutputStream o = p.getOutputStream();
            o.write(mailMsg.getBytes("UTF-8"));
            o.flush();
            o.close();
            p.waitFor();
            if (p.exitValue() != 0) {
                throw new Exception("sendmail exitValue=" + p.exitValue());
            }
        } catch (Throwable ex_) {
            LOG.log(Level.SEVERE, "sendmail failed", ex_);
        }

    }

}
