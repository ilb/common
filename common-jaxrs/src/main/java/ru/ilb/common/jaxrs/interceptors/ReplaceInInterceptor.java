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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;

/**
 *
 * @author slavb
 */
public class ReplaceInInterceptor implements ReaderInterceptor {

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
    public Object aroundReadFrom(ReaderInterceptorContext сontext) throws IOException, WebApplicationException {
        if (replacements != null && mediaType.isCompatible(сontext.getMediaType())) {
            InputStream inputStream = сontext.getInputStream();
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            String contents = new String(bytes);
            for (Map.Entry<String, String> keyValue : replacements.entrySet()) {
                contents = contents.replaceAll(keyValue.getKey(), keyValue.getValue());
            }
            сontext.setInputStream(new ByteArrayInputStream(contents.getBytes()));
            return сontext.proceed();
        } else {
            return сontext.proceed();
        }
    }

}
