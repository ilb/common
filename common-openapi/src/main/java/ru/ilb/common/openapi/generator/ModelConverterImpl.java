/*
 * Copyright 2019 slavb.
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
package ru.ilb.common.openapi.generator;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Iterator;
import java.util.logging.Logger;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

/**
 *
 * @author slavb
 */
public class ModelConverterImpl implements ModelConverter {

    static final Logger LOG = Logger.getLogger(ModelConverterImpl.class.getName());

    @Override
    public Schema resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        Multipart multipart = AnnotationsUtils.getAnnotation(Multipart.class, type.getCtxAnnotations());
        // часть multipart/form-data
        if (multipart != null) {
            Schema schema = chain.next().resolve(type, context, chain);
            schema.setType("object");
            schema.setFormat(null);
            schema.addProperties(multipart.value(), new Schema().type("string").format("binary"));
            return schema;
        }
        if (chain.hasNext()) {
            return chain.next().resolve(type, context, chain);
        } else {
            return null;
        }
    }

}
