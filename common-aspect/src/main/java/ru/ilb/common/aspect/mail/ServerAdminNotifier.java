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
package ru.ilb.common.aspect.mail;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;

/**
 *
 * @author shadrin_nv
 */
@Component
public class ServerAdminNotifier {

    private String getServerAdmin() {
        String server_admin = null;
        //из запроса, из окружения апача
        //server_admin = (String) request.getAttribute("SERVER_ADMIN");
        //из окружения приложения, у сервера или контекста приложения
        if (server_admin == null) {
            try {
                Context initCtx = new InitialContext();
                Context envCtx = (Context) initCtx.lookup("java:comp/env");
                server_admin = (String) envCtx.lookup("SERVER_ADMIN");
            } catch (Exception e) {
            }
        }
        //из системной проперти, у томката-жре
        if (server_admin == null) {
            server_admin = System.getProperty("SERVER_ADMIN");
        }
        //напоследок пытаемся из системного окружения
        if (server_admin == null) {
            server_admin = System.getenv("SERVER_ADMIN");
        }
        //если уж вообще ничего не найдем - root
        if (server_admin == null) {
            server_admin = "root";
        }
        return server_admin;
    }

    private String getMailMsg(final JoinPoint joinPoint, final Exception ex) {
        String mailMsg = "";
        try {
            // название метода, где произошла ошибка
            String methodName = joinPoint.getSignature().getName();
            //идентификатор треда совпадает с указанном в логе сервера
            String threadid = Thread.currentThread().getName();
            //уникальный идентификатор ошибки чтоб ссылаться
            String uuid = UUID.randomUUID().toString();
            String cause = ex.getCause() == null ? ex.getClass().getName() : ex.getCause().getClass().getName();
            String outMess = ex.getMessage();
            if (outMess == null) {
                outMess = "";
            }

            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            String trace = writer.toString();
            int line2 = trace.indexOf("\n");
            mailMsg = "To: " + getServerAdmin() + "\n" //From правильный sendmail сам подставит
                    + "Subject: [JAVA FATAL ERROR] in " + methodName + ": " + threadid + "\n"
                    + "Content-Type: text/plain; charset=UTF-8\n\n"
                    + cause + ": " + outMess + "\n"
                    + threadid + " " + uuid + "\n"
                    + (line2 < 0 ? trace : trace.substring(line2 + 1));
        } catch (Throwable ex_) {
            Logger.getLogger(ServerAdminNotifier.class.getName()).log(Level.SEVERE, "error constructing mailMsg", ex_);
        }
        return mailMsg;
    }

    private void sendMail(String mailMsg) {
        try {
            //http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html
            //чтобы капчурить отдельно strerr/stdout и писать stdin нужно городить отдельные треды - упрощаем
            Process p = Runtime.getRuntime().exec("/usr/sbin/sendmail -t -oi");
            OutputStream o = p.getOutputStream();
            o.write(mailMsg.getBytes("UTF-8"));
            o.flush();
            o.close();
            p.waitFor();
            if (p.exitValue() != 0) {
                throw new Exception("ServerAdminNotifier exitValue=" + p.exitValue());
            }
        } catch (Exception e) {
            //игнорируем ошибки чтобы не упасть в обморок в обработчике ошибок (срам то какой!)
            System.out.println("ServerAdminNotifier failed: " + e.getMessage() + "\n" + mailMsg);
        }
    }

    public void sendMail(final JoinPoint joinPoint, final Exception exp) {
        sendMail(getMailMsg(joinPoint, exp));
    }

}
