/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ilb.common.jaxrs.interceptors;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

/**
 *
 * @author slavb
 */
public class ReplaceOutInterceptor extends AbstractPhaseInterceptor<Message> {

    String encoding = "UTF-8";

    Map<String, String> replacements;

    MediaType mediaType = new MediaType();

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setReplacements(Map<String, String> replacements) {
        this.replacements = replacements;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = JAXRSUtils.toMediaType(mediaType);
    }

    public ReplaceOutInterceptor() {
        super(Phase.PRE_STREAM);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        String contentType = (String) message.get(Message.CONTENT_TYPE);
        if (replacements!=null && mediaType.isCompatible(JAXRSUtils.toMediaType(contentType))) {
            replaceContents(message);
        }
    }

    private void replaceContents(Message message) {
        OutputStream os = message.getContent(OutputStream.class);
        CachedStream cs = new CachedStream();
        message.setContent(OutputStream.class, cs);

        message.getInterceptorChain().doIntercept(message);

        try {
            cs.flush();
            CachedOutputStream csnew = (CachedOutputStream) message.getContent(OutputStream.class);

            String contents = IOUtils.toString(csnew.getInputStream());
            for (Map.Entry<String, String> keyValue : replacements.entrySet()) {
                contents = contents.replaceAll(keyValue.getKey(), keyValue.getValue());
            }
            //String replaced=contents.replaceAll(regex, replacement);
            os.write(contents.getBytes(Charset.forName(encoding)));
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
