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
package ru.ilb.common.springboot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * @author slavb
 */
@ConfigurationProperties("ilb")
public class ILBProperties {

    private XSLTProperties xslt = new XSLTProperties();

    public XSLTProperties getXslt() {
        return xslt;
    }

    public void setXslt(XSLTProperties xslt) {
        this.xslt = xslt;
    }



    public static class XSLTProperties {
        private String produces = "application/xhtml+xml,text/csv,application/pdf";

        public String getProduces() {
            return produces;
        }

        public void setProduces(String produces) {
            this.produces = produces;
        }


    }


}
