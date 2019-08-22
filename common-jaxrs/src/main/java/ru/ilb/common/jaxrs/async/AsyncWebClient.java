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
package ru.ilb.common.jaxrs.async;

import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;

/**
 *
 * @author slavb
 */
public class AsyncWebClient {
    public static Response get(Object resource, Response res) {
        org.apache.cxf.jaxrs.client.Client client = WebClient.client(resource);
        WebClient wClient = WebClient.fromClient(client);
        return getResponse(wClient, res);
    }
    /**
     * TODO: чтение заголовка Refresh, секунд
     * @param wClient
     * @param res
     * @return
     */
    public static Response getResponse(WebClient wClient,Response res) {
        if ((res.getStatus() == 202 || res.getStatus() == 303) || res.getStatus() == 302) {
            String url = res.getLocation().toString();
            while (res.getStatus() == 202 || res.getStatus() == 303 || res.getStatus() == 302) {
                if(res.getLocation()!=null){
                    url = res.getLocation().toString();
                }
                wClient.replacePath(url);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                res = wClient.get();
            }
        }
        return res;
    }

}
