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
package ru.ilb.common.jaxb.util;

import java.io.StringWriter;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author slavb
 */
public class JaxbUtil {
    private static final String MEDIA_TYPE = "eclipselink.media-type";
    private static final String JSON_INCLUDE_ROOT = "eclipselink.json.include-root";

    private JaxbUtil() {
    }

    public static <T> T unmarshal(JAXBContext jaxbContext, String data, Class<T> type, String mediaType) {
        return unmarshal(jaxbContext, new StreamSource(new java.io.StringReader(data)), type, mediaType);
    }
    public static <T> T unmarshal(JAXBContext jaxbContext, Source source, Class<T> type, String mediaType) {

        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setProperty(MEDIA_TYPE, mediaType);
            unmarshaller.setProperty(JSON_INCLUDE_ROOT, false);
            Object unmarshal;
            if (type != null) {
                unmarshal = unmarshaller.unmarshal(source, type);
            } else {
                unmarshal = unmarshaller.unmarshal(source);
            }
            if (unmarshal instanceof JAXBElement<?>) {
                unmarshal = ((JAXBElement<?>) unmarshal).getValue();
            }

            return (T) unmarshal;

        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String marshal(JAXBContext jaxbContext, Object obj, String mediaType) {
        StringWriter sw = new StringWriter();
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(MEDIA_TYPE, mediaType);
            if (MediaType.APPLICATION_JSON.equals(mediaType)) {
                marshaller.setProperty(JSON_INCLUDE_ROOT, false);
            }
            marshaller.marshal(obj, sw);
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
        return sw.toString();
    }

}
