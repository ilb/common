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
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Response;

/**
 *
 * @author slavb
 */
public class SendMailExceptionHandler extends ExceptionHandler{
    
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
    String mailCommand = "/usr/sbin/sendmail -t -oi";

    /**
     * minimum http response status to send mail, by default only 5XX errors are mailed
     */
    int mailResponseStatus = 500;
    
    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }

    public void setMailSubject(String mailSubject) {
        this.mailSubject = mailSubject;
    }

    public void setMailCommand(String mailCommand) {
        this.mailCommand = mailCommand;
    }

    public void setMailResponseStatus(int mailResponseStatus) {
        this.mailResponseStatus = mailResponseStatus;
    }

    @Override
    public Response toResponse(Exception ex) {
        Response res=super.toResponse(ex);
        if (res.getStatus() >= mailResponseStatus) {
            String mailMsg = getMailMsg(ex, res.getStatus(), (String)res.getEntity());
            sendMail(mailMsg);
        }
        
        return res;
    }
    private String getMailMsg(Exception ex, int code, String outMess) {
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
            int line2 = trace.indexOf("\n");
            mailMsg = "To: " + mailTo + "\n" //From header sendmail should generate
                    + "Subject: " + mailSubject + " " + threadid + "\n"
                    + "Content-Type: " + contentType + "\n\n"
                    + cause + ": " + outMess + "\n"
                    + code + " " + threadid + " " + uuid + "\n"
                    + (line2 < 0 ? trace : trace.substring(line2 + 1));
        } catch (Throwable ex_) {
            LOG.log(Level.SEVERE, "error constructing mailMsg", ex_);
        }
        return mailMsg;
    }

    private void sendMail(String mailMsg) {
        try {
            //http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html
            //to capture strerr/stdout and write to stdin need to create separate threads - simplify
            Process p = Runtime.getRuntime().exec(mailCommand);
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
