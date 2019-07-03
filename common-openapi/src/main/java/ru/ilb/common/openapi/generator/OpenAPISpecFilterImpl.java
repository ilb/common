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

import io.swagger.v3.core.filter.OpenAPISpecFilter;
import io.swagger.v3.core.model.ApiDescription;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author slavb
 */
public class OpenAPISpecFilterImpl implements OpenAPISpecFilter {

    static final Logger LOG = Logger.getLogger(OpenAPISpecFilterImpl.class.getName());

    @Override
    public Optional<OpenAPI> filterOpenAPI(OpenAPI openAPI, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        return Optional.of(openAPI);
    }

    @Override
    public Optional<PathItem> filterPathItem(PathItem pathItem, ApiDescription api, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        return Optional.of(pathItem);
    }

    @Override
    public Optional<Operation> filterOperation(Operation operation, ApiDescription api, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        return Optional.of(operation);
    }

    @Override
    public Optional<Parameter> filterParameter(Parameter parameter, Operation operation, ApiDescription api, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        return Optional.of(parameter);
    }

    @Override
    public Optional<RequestBody> filterRequestBody(RequestBody requestBody, Operation operation, ApiDescription api, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        //replace CXF MultipartBody with file upload
        // логичнее будет перенести в ModelConverterImpl
        MediaType mt = requestBody.getContent().get("multipart/form-data");
        if (mt != null) {
            if (mt.getSchema() != null && mt.getSchema().get$ref() != null && mt.getSchema().get$ref().contains("MultipartBody")) {
                Schema schema = mt.getSchema();
                schema.setType("object");
                schema.set$ref(null);
                // @see https://swagger.io/docs/specification/describing-request-body/file-upload/
                // single file upload
                schema.addProperties("file", new Schema().type("string").format("binary"));
                // multi file upload
                //schema.addProperties("file", new ArraySchema().type("array").items(new Schema().type("string").format("binary")));
                LOG.log(Level.INFO, () -> "filterRequestBody operationId=" + operation.getOperationId() + " replace MultipartBody with object");
            }
        }
        return Optional.of(requestBody);
    }

    /**
     * filterResponse actions
     * 1. set default type "object" for application/json
     * @param response
     * @param operation
     * @param api
     * @param params
     * @param cookies
     * @param headers
     * @return
     */
    @Override
    public Optional<ApiResponse> filterResponse(ApiResponse response, Operation operation, ApiDescription api, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        MediaType mt = response.getContent().get("application/json");
        if (mt != null) {
            if (mt.getSchema()==null) {
                Schema schema = new Schema();
                schema.setType("object");
                mt.setSchema(schema);
            }
//            if (mt.getSchema() != null && mt.getSchema().get$ref() == null && mt.getSchema().getType()==null) {
//                mt.getSchema().setType("object");
//            }
        }

        return Optional.of(response);
    }

    @Override
    public Optional<Schema> filterSchema(Schema schema, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        return Optional.of(schema);
    }

    @Override
    public Optional<Schema> filterSchemaProperty(Schema property, Schema schema, String propName, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        return Optional.of(property);
    }

    @Override
    public boolean isRemovingUnreferencedDefinitions() {
        return true;
    }

}
