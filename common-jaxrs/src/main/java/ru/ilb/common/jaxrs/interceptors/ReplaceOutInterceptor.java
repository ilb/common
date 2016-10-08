/*
 * Copyright 2016 slavb.
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
package ru.ilb.common.jaxrs.interceptors;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import ru.ilb.common.jaxrs.io.ByteArrayInOutStream;

/**
 *
 * @author slavb
 */
public class ReplaceOutInterceptor implements WriterInterceptor {

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

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        if (replacements != null && mediaType.isCompatible(context.getMediaType())) {
            OutputStream os = context.getOutputStream();
            ByteArrayInOutStream out = new ByteArrayInOutStream();
            context.setOutputStream(out);
            context.proceed();
            String contents = IOUtils.toString(out.getInputStream());
            for (Map.Entry<String, String> keyValue : replacements.entrySet()) {
                contents = contents.replaceAll(keyValue.getKey(), keyValue.getValue());
            }
            //String replaced=contents.replaceAll(regex, replacement);
            os.write(contents.getBytes(Charset.forName(encoding)));
            os.flush();
        } else {
            context.proceed();
        }
    }

}
