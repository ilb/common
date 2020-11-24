/*
 * Copyright 2017 slavb.
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
package ru.ilb.common.jaxrs.exceptionhandler;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;

/**
 *
 * @author slavb
 */
public class CustomResponseStatus implements Response.StatusType {

    private static final Map<Character, String> TRANSLIT = new HashMap<>();

    static {
        Character[] search = new Character[]{'щ', 'Щ', 'ё', 'ж', 'ч', 'ш', 'ъ', 'ы', 'э', 'ю', 'я', 'Ё', 'Ж', 'Ч', 'Ш', 'Ъ', 'Ы', 'Э', 'Ю', 'Я',
            'а', 'б', 'в', 'г', 'д', 'е', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ь', 'А', 'Б', 'В', 'Г', 'Д', 'Е', 'З', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ь'};
        String[] replace = new String[]{"shh", "Shh", "yo", "zh", "ch", "sh", "``", "y`", "e`", "yu", "ya", "Yo", "Zh", "Ch", "Sh", "``", "Y`", "E`", "Yu", "Ya",
            "a", "b", "v", "g", "d", "e", "z", "i", "j", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u", "f", "x", "c", "`", "A", "B", "V", "G", "D", "E", "Z", "I", "J", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U", "F", "X", "C", "`"};
        for (int i = 0; i < search.length; i++) {
            TRANSLIT.put(search[i], replace[i]);
        }

    }
    private final int statusCode;

    private final String reasonPhrase;

    public CustomResponseStatus(int statusCode, String reasonPhrase) {
        this.statusCode = statusCode;
        //first line
        String result = reasonPhrase.trim().split("\r\n|\r|\n", 2)[0].trim();
        result = transliterate(result);
        this.reasonPhrase = result;
    }

    private static String transliterate(char ch) {
        String result = TRANSLIT.get(ch);
        if (result == null) {
            result = String.valueOf(ch);
        }
        return result;
    }

    private static String transliterate(String s) {
        StringBuilder sb = new StringBuilder(s.length() * 2);
        for (char ch : s.toCharArray()) {
            sb.append(transliterate(ch));
        }
        return sb.toString();
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public Response.Status.Family getFamily() {
        return Response.Status.Family.familyOf(statusCode);
    }

    @Override
    public String getReasonPhrase() {
        return reasonPhrase;
    }

}
