/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ilb.common.jaxrs.interceptors;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

/**
 *
 * @author slavb
 */
public class ReplaceOutInterceptor extends AbstractPhaseInterceptor<Message> {
    String encoding="UTF-8";
    
    String regex;
    
    String replacement;

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }
    
    
    public ReplaceOutInterceptor() {
        super(Phase.PRE_STREAM);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        OutputStream os = message.getContent(OutputStream.class);
        CachedStream cs = new CachedStream();
        message.setContent(OutputStream.class, cs);

        message.getInterceptorChain().doIntercept(message);

        try {
            cs.flush();
            CachedOutputStream csnew = (CachedOutputStream) message.getContent(OutputStream.class);

            String contents = IOUtils.toString(csnew.getInputStream());
            String replaced=contents.replaceAll(regex, replacement);
            os.write(replaced.getBytes(Charset.forName(encoding)));
            os.flush();
            
            message.setContent(OutputStream.class, os);
            
            
            
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    

    private class CachedStream extends CachedOutputStream {

        public CachedStream() {
            super();
        }

        @Override
        protected void doFlush() throws IOException {
            currentStream.flush();
        }

        @Override
        protected void doClose() throws IOException {
        }

        @Override
        protected void onWrite() throws IOException {
        }
    }
}
