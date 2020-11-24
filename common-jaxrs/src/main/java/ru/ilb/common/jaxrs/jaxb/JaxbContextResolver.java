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
package ru.ilb.common.jaxrs.jaxb;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

@Provider
public class JaxbContextResolver implements ContextResolver<JAXBContext> {

    private static final Logger LOG = Logger.getLogger(JaxbContextResolver.class.getName());

    protected Map<String, Object> contextProperties;
    protected final Map<String, JAXBContext> packageContexts = new HashMap<>();
    protected final Map<Class<?>, JAXBContext> classContexts = new HashMap<>();
    protected Class<?>[] extraClass;

    public void setContextProperties(Map<String, Object> contextProperties) {
        this.contextProperties = contextProperties;
    }

    public void setExtraClass(Class<?>[] extraClass) {
        this.extraClass = extraClass;
    }

    @Override
    public JAXBContext getContext(Class<?> type) {
        try {
            return getJAXBContext(type);
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

    public JAXBContext getJAXBContext(Class<?> type) throws JAXBException {

        synchronized (classContexts) {
            JAXBContext context = classContexts.get(type);
            if (context != null) {
                return context;
            }
        }
        JAXBContext context = getPackageContext(type);

        return context != null ? context : getClassContext(type);

    }

    protected JAXBContext getClassContext(Class<?> type) throws JAXBException {
        synchronized (classContexts) {
            JAXBContext context = classContexts.get(type);
            if (context == null) {
                Class<?>[] classes = null;
                if (extraClass != null) {
                    classes = new Class[extraClass.length + 1];
                    classes[0] = type;
                    System.arraycopy(extraClass, 0, classes, 1, extraClass.length);
                } else {
                    classes = new Class[]{type};
                }

                context = JAXBContext.newInstance(classes, contextProperties);
                classContexts.put(type, context);
            }
            return context;
        }
    }

    protected JAXBContext getPackageContext(Class<?> type) throws JAXBException {
        if (type == null || type == JAXBElement.class) {
            return null;
        }
        synchronized (packageContexts) {
            String packageName = getPackageName(type);
            JAXBContext context = packageContexts.get(packageName);
            if (context == null) {
                try {
                    if (type.getClassLoader() != null && objectFactoryOrIndexAvailable(type)) {
                        String contextName = packageName;
                        if (extraClass != null) {
                            StringBuilder sb = new StringBuilder(contextName);
                            for (Class<?> extra : extraClass) {
                                String extraPackage = getPackageName(extra);
                                if (!extraPackage.equals(packageName)) {
                                    sb.append(':').append(extraPackage);
                                }
                            }
                            contextName = sb.toString();
                        }
                        context = JAXBContext.newInstance(contextName, type.getClassLoader(), contextProperties);
                        packageContexts.put(packageName, context);
                    }
                } catch (JAXBException ex) {
                    LOG.log(Level.FINE, "Error creating a JAXBContext using ObjectFactory : {0}", ex.getMessage());
                    return null;
                }
            }
            return context;
        }
    }

    protected boolean objectFactoryOrIndexAvailable(Class<?> type) {
        return type.getResource("ObjectFactory.class") != null
                || type.getResource("jaxb.index") != null;
    }

    static String getPackageName(String className) {
        int pos = className.lastIndexOf('.');
        if (pos != -1) {
            return className.substring(0, pos);
        } else {
            return "";
        }
    }

    public static String getPackageName(Class<?> clazz) {
        String className = clazz.getName();
        if (className.startsWith("[L")) {
            className = className.substring(2);
        }
        return getPackageName(className);
    }
}
