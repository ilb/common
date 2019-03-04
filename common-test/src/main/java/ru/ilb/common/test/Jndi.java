/*
 * Copyright 2018 slavb.
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
package ru.ilb.common.test;

import com.sun.java.xml.ns.javaee.EnvEntryType;
import com.sun.java.xml.ns.javaee.ResourceEnvRefType;
import com.sun.java.xml.ns.javaee.ResourceRefType;
import com.sun.java.xml.ns.javaee.WebAppType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import ru.ilb.common.jaxb.util.JaxbUtil;

/**
 *
 * @author valeev
 */
public abstract class Jndi {

    protected JAXBContext jaxbContext;

    private WebAppType webApp;

    private Map<String, Object> params = new HashMap<>();

    /**
     * Сущности, которые считываются из web.xml
     */
    private final List<String> entryList = Arrays.asList("env-entry", "resource-ref", "resource-env-ref");

    /**
     * Наименования переменных, которых не следует считывать с web.xml
     */
    protected List<String> excludeNames = new ArrayList<>();

    public final WebAppType getWebApp(){
        if (webApp != null) {
            return webApp;
        }
        try {
            jaxbContext = JAXBContext.newInstance("com.sun.java.xml.ns.javaee");
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
        try {
            String xml = new String(Files.readAllBytes(Paths.get("src/main/webapp/WEB-INF/web.xml")));
            webApp = JaxbUtil.unmarshal(jaxbContext, xml, WebAppType.class, "application/xml");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return webApp;
    }

    protected List<String> getExcludeNames() {
        return excludeNames;
    }

    protected void addExcludeName(String name) {
        excludeNames.add(name);
    }

    protected Map<String, Object> getParams() {
        if (!params.isEmpty()) {
            return params;
        }
        getWebApp().getModuleNameOrDescriptionAndDisplayName().stream().filter(el -> entryList.contains(el.getName().getLocalPart())).
                forEach(el -> {
                    String name = null;
                    Object value = null;
                    if (el.getValue() instanceof EnvEntryType) {
                        name = ((EnvEntryType) el.getValue()).getEnvEntryName().getValue();
                        value = getEnvEntryValue((EnvEntryType) el.getValue());
                    } else if (el.getValue() instanceof ResourceRefType) {
                        name = ((ResourceRefType) el.getValue()).getResRefName().getValue();
                        value = getResourceRefValue((ResourceRefType) el.getValue());
                    } else if (el.getValue() instanceof ResourceEnvRefType) {
                        name = ((ResourceEnvRefType) el.getValue()).getResourceEnvRefName().getValue();
                        value = getResourceEnvRefValue((ResourceEnvRefType) el.getValue());
                    }
                    if (name != null && !excludeNames.contains(name)){
                        params.put(name, value);
                    }
                });
        return params;
    }

    protected Object getEnvEntryValue(EnvEntryType envEntryType) {
        switch (envEntryType.getEnvEntryType().getValue()){
            case "java.lang.Boolean":
                return Boolean.valueOf(envEntryType.getEnvEntryValue().getValue());
            case "java.lang.Integer":
                return Integer.valueOf(envEntryType.getEnvEntryValue().getValue());
            case "java.lang.Double":
                return Double.valueOf(envEntryType.getEnvEntryValue().getValue());
            default:
                return envEntryType.getEnvEntryValue().getValue();
        }
    }

    protected Object getResourceRefValue(ResourceRefType resourceRefType) {
        return "";
    }

    protected Object getResourceEnvRefValue(ResourceEnvRefType resourceEnvRefType) {
        return "";
    }

}
