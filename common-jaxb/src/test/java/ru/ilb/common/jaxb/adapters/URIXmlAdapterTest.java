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
package ru.ilb.common.jaxb.adapters;

import java.net.URI;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author slavb
 */
public class URIXmlAdapterTest {

    public URIXmlAdapterTest() {
    }

    /**
     * Test of unmarshal method, of class URIXmlAdapter.
     */
    @Test
    public void testUnmarshal() throws Exception {
        System.out.println("unmarshal");
        String value = "https://www.w3.org/";
        URIXmlAdapter instance = new URIXmlAdapter();
        URI expResult = URI.create(value);
        URI result = instance.unmarshal(value);
        assertEquals(expResult, result);
    }

    /**
     * Test of marshal method, of class URIXmlAdapter.
     */
    @Test
    public void testMarshal() throws Exception {
        System.out.println("marshal");
        String expResult = "https://www.w3.org/";
        URI value = URI.create(expResult);
        URIXmlAdapter instance = new URIXmlAdapter();
        String result = instance.marshal(value);
        assertEquals(expResult, result);
    }

}
