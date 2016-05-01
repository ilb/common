/*
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
package ru.ilb.common.jaxrs.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import org.apache.cxf.jaxrs.utils.schemas.SchemaHandler;


@Produces({MediaType.APPLICATION_JSON, MediaType.WILDCARD, "application/x-javascript"})
@Consumes({MediaType.APPLICATION_JSON, MediaType.WILDCARD})
@Provider
public class MOXyJsonProvider extends org.eclipse.persistence.jaxb.rs.MOXyJsonProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

    private Schema schema;
    private Map<String, SchemaHandler> schemaHandlers;
    private boolean validateInputIfPossible = true;
    private boolean validateOutputIfPossible;

    public void setSchemaHandler(SchemaHandler handler) {
        setSchema(handler.getSchema());
    }

    protected void setSchema(Schema s) {
        schema = s;
    }

    protected Schema getSchema() {
        return getSchema(null);
    }

    protected Schema getSchema(Class<?> cls) {
        // deal with the typical default case first
        if (schema == null && schemaHandlers == null) {
            return null;
        }

        if (schema != null) {
            return schema;
        } else {
            SchemaHandler handler = schemaHandlers.get(cls.getName());
            return handler != null ? handler.getSchema() : null;
        }
    }

    @Override
    protected void preReadFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, Unmarshaller unmarshaller) throws JAXBException {
        super.preReadFrom(type, genericType, annotations, mediaType, httpHeaders, unmarshaller);
        if (validateInputIfPossible) {
            Schema theSchema = getSchema(type);
            if (theSchema != null) {
                unmarshaller.setSchema(theSchema);
            }
        }

    }

}
