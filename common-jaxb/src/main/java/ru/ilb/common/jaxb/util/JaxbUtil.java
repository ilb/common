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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
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

    /**
     * unmarshalls object instance from String
     *
     * @param <T>
     * @param jaxbContext
     * @param string source xml/json
     * @param type deserved type (null - autodetect)
     * @param mediaType
     * @return T
     */
    public static <T> T unmarshal(JAXBContext jaxbContext, String string, Class<T> type, String mediaType) {
        return unmarshal(jaxbContext, new StreamSource(new java.io.StringReader(string)), type, mediaType);
    }

    /**
     * unmarshalls object instance from Source
     *
     * @param <T>
     * @param jaxbContext
     * @param source example from String: new StreamSource(new
     * java.io.StringReader(string)), from InputStream: new StreamSource(is)
     * @param type deserved type (null - autodetect)
     * @param mediaType application/json or application/xml
     * @return T
     */
    public static <T> T unmarshal(JAXBContext jaxbContext, Source source, Class<T> type, String mediaType) {

        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            if (unmarshaller.getClass().getName().contains("eclipse")) {
                unmarshaller.setProperty(MEDIA_TYPE, mediaType);
                unmarshaller.setProperty(JSON_INCLUDE_ROOT, false);
            }
            Object unmarshal;
            if (type != null) {
                unmarshal = unmarshaller.unmarshal(source, type);
            } else {
                unmarshal = unmarshaller.unmarshal(source);
            }
            unmarshal = JAXBIntrospector.getValue(unmarshal);

            return (T) unmarshal;

        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * marshalls object instance to String
     *
     * @param jaxbContext
     * @param object object to be marshalled
     * @param mediaType application/json or application/xml
     * @return
     */
    public static String marshal(JAXBContext jaxbContext, Object object, String mediaType) {
        StringWriter sw = new StringWriter();
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            if (marshaller.getClass().getName().contains("eclipse")) {
                marshaller.setProperty(MEDIA_TYPE, mediaType);
                if (MediaType.APPLICATION_JSON.equals(mediaType)) {
                    marshaller.setProperty(JSON_INCLUDE_ROOT, false);
                }
            }
            marshaller.marshal(object, sw);
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
        return sw.toString();
    }

    /**
     * unmarshalls collection of object instances
     *
     * @param <T>
     * @param jaxbContext
     * @param source example from String: new StreamSource(new
     * java.io.StringReader(string)), from InputStream: new StreamSource(is)
     * @param type
     * @param mediaType
     * @return List
     */
    public static <T> List<T> unmarshalCollection(JAXBContext jaxbContext, Source source, Class<T> type, String mediaType) {

        try {
            List<T> result = new ArrayList();
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            if (unmarshaller.getClass().getName().contains("eclipse")) {
                unmarshaller.setProperty(MEDIA_TYPE, mediaType);
            }
            Collection tmp = (Collection) unmarshaller.unmarshal(source, type).getValue();
            for (Object element : tmp) {
                result.add((T) JAXBIntrospector.getValue(element));
            }
            return result;

        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }
}
