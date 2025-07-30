/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.microsphere.json;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.microsphere.json.JSONStringer.Scope.EMPTY_ARRAY;
import static io.microsphere.json.JSONStringer.Scope.EMPTY_OBJECT;
import static io.microsphere.json.JSONStringer.Scope.NONEMPTY_ARRAY;
import static io.microsphere.json.JSONStringer.Scope.NONEMPTY_OBJECT;
import static io.microsphere.json.JSONStringer.Scope.NULL;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link JSONStringer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see JSONStringer
 * @since 1.0.0
 */
class JSONStringerTest {

    private JSONStringer jsonStringer;

    private JSONStringer nonIndentJsonStringer;

    @BeforeEach
    void setUp() {
        this.jsonStringer = new JSONStringer(1);
        this.nonIndentJsonStringer = new JSONStringer();
    }

    @Test
    void testArray() throws Throwable {
        assertEquals("[", jsonStringer.array().toString());
        assertEquals("[", nonIndentJsonStringer.array().toString());
    }

    @Test
    void testArrayOnJSONException() {
        jsonStringer.string("Testing");
        assertThrows(JSONException.class, jsonStringer::array);
    }

    @Test
    void testEndArray() throws Throwable {
        assertEquals("[]", jsonStringer.array().endArray().toString());
    }

    @Test
    void testEndArrayOnJSONException() {
        assertThrows(JSONException.class, jsonStringer::endArray);
    }

    @Test
    void testObject() throws Throwable {
        assertEquals("{", jsonStringer.object().toString());
    }

    @Test
    void testEndObject() throws Throwable {
        assertEquals("{}", jsonStringer.object().endObject().toString());
    }

    @Test
    void testEndObjectOnJSONException() {
        assertThrows(JSONException.class, jsonStringer::endObject);
    }

    @Test
    void testOpen() throws JSONException {
        jsonStringer.open(EMPTY_ARRAY, "[");
        jsonStringer.open(EMPTY_ARRAY, "[");
        assertEquals("[\n [", jsonStringer.toString());
    }

    @Test
    void testClose() throws JSONException {
        jsonStringer.open(EMPTY_ARRAY, "[");
        jsonStringer.close(EMPTY_ARRAY, NONEMPTY_ARRAY, "]");
        assertEquals("[]", jsonStringer.toString());
    }

    @Test
    void testCloseOnJSONException() throws JSONException {
        jsonStringer.open(EMPTY_ARRAY, "[");
        assertThrows(JSONException.class, () -> jsonStringer.close(EMPTY_OBJECT, NULL, "]"));
        assertThrows(JSONException.class, () -> jsonStringer.close(NULL, NONEMPTY_ARRAY, "]"));
    }

    @Test
    void testValue() throws JSONException {
        jsonStringer.array()
                .value(false)
                .value(1)
                .value(2.1)
                .value("3")
                .endArray();
        assertEquals("[\n false,\n 1,\n 2.1,\n \"3\"\n]", jsonStringer.toString());
    }

    @Test
    void testValueWithJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "Mercy");
        assertEquals("[\n {\n  \"name\": \"Mercy\"\n }\n]", jsonStringer.array().value(jsonObject).endArray().toString());
    }

    @Test
    void testValueWithJSONArray() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(1).put(2).put(3);
        assertEquals("[\n [\n  1,\n  2,\n  3\n ]\n]", jsonStringer.array().value(jsonArray).endArray().toString());
    }

    @Test
    void testValueWithObject() throws JSONException {
        jsonStringer.array()
                .value(null)
                .value(TRUE)
                .value(JSONObject.NULL)
                .value(Integer.valueOf(1))
                .value(Double.valueOf(2.0))
                .value(Long.valueOf(3L))
                .value(Float.valueOf(4.0f))
                .value(new StringBuilder("5"))
                .value(String.class)
                .endArray();
        assertEquals("[\n null,\n true,\n null,\n 1,\n 2,\n 3,\n 4,\n \"5\",\n \"java.lang.String\"\n]", jsonStringer.toString());

        nonIndentJsonStringer.array()
                .value(null)
                .value(TRUE)
                .value(JSONObject.NULL)
                .value(Integer.valueOf(1))
                .value(Double.valueOf(2.0))
                .value(Long.valueOf(3L))
                .value(Float.valueOf(4.0f))
                .value(new StringBuilder("5"))
                .value(String.class)
                .endArray();
        assertEquals("[null,true,null,1,2,3,4,\"5\",\"java.lang.String\"]", nonIndentJsonStringer.toString());

    }

    @Test
    void testValueOnJSONException() {
        assertThrows(JSONException.class, () -> jsonStringer.value(null));
        assertThrows(JSONException.class, () -> jsonStringer.value(false));
        assertThrows(JSONException.class, () -> jsonStringer.value(1));
        assertThrows(JSONException.class, () -> jsonStringer.value(1.0));
        assertThrows(JSONException.class, () -> jsonStringer.value("testing"));
    }

    @Test
    void testKey() throws JSONException {
        jsonStringer.object()
                .key("name")
                .value("Mercy")
                .endObject();
        assertEquals("{\n \"name\": \"Mercy\"\n}", jsonStringer.toString());
    }

    @Test
    void testKeyOnJSONException() {
        assertThrows(JSONException.class, () -> jsonStringer.key(null));
    }

    @Test
    void testString() {
        jsonStringer.string("\"\\/\t\b\n\r\fa");
        assertEquals("\"\\\"\\\\\\/\\t\\b\\n\\r\\fa\"", jsonStringer.toString());
        jsonStringer.string(Character.valueOf((char) 0x1F).toString());
        assertEquals("\"\\\"\\\\\\/\\t\\b\\n\\r\\fa\"\"\\u001f\"", jsonStringer.toString());
    }

    @Test
    void testBeforeKey() throws JSONException {
        jsonStringer.open(NONEMPTY_OBJECT, "{");
        jsonStringer.beforeKey();
        assertEquals("{,\n ", jsonStringer.toString());
    }

    @Test
    void testBeforeKeyOnJSONException() throws JSONException {
        jsonStringer.array();
        assertThrows(JSONException.class, jsonStringer::beforeKey);
    }

    @Test
    void testBeforeValue() throws JSONException {
    }

    @Test
    void testBeforeValueOnJSONException() throws JSONException {
        jsonStringer.open(EMPTY_OBJECT, "[");
        assertThrows(JSONException.class, jsonStringer::beforeValue);
    }
}