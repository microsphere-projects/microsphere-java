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

import java.util.Iterator;
import java.util.Map;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.collection.MapUtils.ofMap;
import static io.microsphere.json.JSONObject.NEGATIVE_ZERO;
import static io.microsphere.json.JSONObject.NULL;
import static io.microsphere.json.JSONObject.numberToString;
import static io.microsphere.json.JSONObject.quote;
import static io.microsphere.json.JSONObject.wrap;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link JSONObject} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see JSONObject
 * @since 1.0.0
 */
class JSONObjectTest {

    private JSONObject jsonObject;

    @BeforeEach
    void setUp() {
        this.jsonObject = new JSONObject();
    }

    @Test
    void testConstructorWithMap() throws JSONException {
        JSONObject jsonObject = new JSONObject(ofMap("name", "Mercy", "age", 18));
        assertEquals("Mercy", jsonObject.getString("name"));
        assertEquals(18, jsonObject.getInt("age"));
    }

    @Test
    void testConstructorWithMapOnNullKey() {
        Map<String, Object> map = newHashMap();
        map.put(null, null);
        assertThrows(NullPointerException.class, () -> new JSONObject(map));
    }

    @Test
    void testConstructorWithJSONObject() throws JSONException {
        long time = currentTimeMillis();
        this.jsonObject.put("name", "Mercy")
                .put("age", 18)
                .put("workingTime", time)
                .put("sex", true)
                .put("salary", 1000.0)
                .put("salary", null)
                .put("salary", Double.valueOf(1000.0));
        JSONObject jsonObject = new JSONObject(this.jsonObject, ofArray("name", "age", "workingTime", "not-found-name", null));
        assertEquals("Mercy", jsonObject.getString("name"));
        assertEquals(18, jsonObject.getInt("age"));
        assertEquals(time, jsonObject.getLong("workingTime"));
    }

    @Test
    void testConstructorWithJSONTokener() throws JSONException {
        JSONTokener jsonTokener = new JSONTokener("{ \"name\" : \"Mercy\" }");
        JSONObject jsonObject = new JSONObject(jsonTokener);
        assertEquals("Mercy", jsonObject.getString("name"));
    }

    @Test
    void testConstructorWithJSONTokenerOnInvalidJSON() {
        assertThrows(JSONException.class, () -> new JSONObject(new JSONTokener("Hello")));
    }

    @Test
    void testConstructorWithJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject("{ \"name\" : \"Mercy\" , \"age\" = 18 }");
        assertEquals("Mercy", jsonObject.getString("name"));
        assertEquals(18, jsonObject.getInt("age"));
    }

    @Test
    void testLength() {
        assertEquals(0, jsonObject.length());
    }

    @Test
    void testPut() throws JSONException {
        long time = currentTimeMillis();
        jsonObject.put("name", "Mercy")
                .put("age", 18)
                .put("workingTime", time)
                .put("sex", true)
                .put("salary", 1000.0)
                .put("salary", null)
                .put("salary", Double.valueOf(1000.0));
        ;
        assertEquals("Mercy", jsonObject.get("name"));
        assertEquals(18, jsonObject.getInt("age"));
        assertEquals(time, jsonObject.getLong("workingTime"));
        assertEquals(1000.0, jsonObject.getDouble("salary"));
        assertTrue(jsonObject.getBoolean("sex"));
    }

    @Test
    void testPutOpt() throws JSONException {
        jsonObject.putOpt(null, null)
                .putOpt("name", null)
                .putOpt("name", "Mercy");
        assertEquals("Mercy", jsonObject.get("name"));
    }

    @Test
    void testAccumulate() throws JSONException {
        jsonObject.accumulate("name", "Mercy");
        assertEquals("Mercy", jsonObject.get("name"));

        jsonObject.accumulate("age", 18);
        assertEquals(18, jsonObject.getInt("age"));

        jsonObject.accumulate("alias", "mercyblitz");
        assertEquals("mercyblitz", jsonObject.get("alias"));

        jsonObject.accumulate("alias", "mz");
        assertEquals("mercyblitz", jsonObject.getJSONArray("alias").get(0));
        assertEquals("mz", jsonObject.getJSONArray("alias").get(1));

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(1);
        jsonObject.accumulate("order", jsonArray);
        jsonObject.accumulate("order", 2);
        jsonObject.accumulate("order", 3);

        assertEquals(1, jsonObject.getJSONArray("order").get(0));
        assertEquals(2, jsonObject.getJSONArray("order").get(1));
        assertEquals(3, jsonObject.getJSONArray("order").get(2));
    }

    @Test
    public void testCheckNameOnJSONException() {
        assertThrows(JSONException.class, () -> jsonObject.checkName(null));
    }

    @Test
    void testRemove() throws JSONException {
        jsonObject.accumulate("name", "Mercy");
        assertEquals("Mercy", jsonObject.get("name"));
        jsonObject.remove("name");
        assertNull(jsonObject.opt("name"));
    }

    @Test
    void testIsNull() throws JSONException {
        assertTrue(jsonObject.isNull("name"));

        jsonObject.put("name", NULL);
        assertTrue(jsonObject.isNull("name"));

        jsonObject.put("name", "Mercy");
        assertFalse(jsonObject.isNull("name"));
    }

    @Test
    void testHas() throws JSONException {
        assertFalse(jsonObject.has("name"));

        jsonObject.put("name", "Mercy");
        assertTrue(jsonObject.has("name"));
    }

    @Test
    void testGet() throws JSONException {
        jsonObject.put("name", "Mercy");
        assertEquals("Mercy", jsonObject.get("name"));
    }

    @Test
    void testGetBoolean() throws JSONException {
        jsonObject.put("value", TRUE);
        assertEquals(true, jsonObject.getBoolean("value"));
    }

    @Test
    void testGetBooleanOnNotFound() {
        assertThrows(JSONException.class, () -> jsonObject.getBoolean("value"));
    }

    @Test
    void testGetDouble() throws JSONException {
        jsonObject.put("value", Double.valueOf(1.0));
        assertEquals(1.0, jsonObject.getDouble("value"));
    }

    @Test
    void testGetDoubleOnNotFound() throws JSONException {
        assertThrows(JSONException.class, () -> jsonObject.getDouble("value"));
        jsonObject.put("value", "Hello");
        assertThrows(JSONException.class, () -> jsonObject.getDouble("value"));
    }

    @Test
    void testGetInt() throws JSONException {
        jsonObject.put("value", Integer.valueOf(1));
        assertEquals(1, jsonObject.getInt("value"));
    }

    @Test
    void testGetIntOnNotFound() throws JSONException {
        assertThrows(JSONException.class, () -> jsonObject.getInt("value"));
        jsonObject.put("value", "Hello");
        assertThrows(JSONException.class, () -> jsonObject.getInt("value"));
    }

    @Test
    void testGetLong() throws JSONException {
        jsonObject.put("value", Long.valueOf(1));
        assertEquals(1L, jsonObject.getLong("value"));
    }

    @Test
    void testGetLongOnNotFound() throws JSONException {
        assertThrows(JSONException.class, () -> jsonObject.getLong("value"));
        jsonObject.put("value", "Hello");
        assertThrows(JSONException.class, () -> jsonObject.getLong("value"));
    }

    @Test
    void testGetString() throws JSONException {
        jsonObject.put("name", "Mercy");
        assertEquals("Mercy", jsonObject.getString("name"));
    }

    @Test
    void testGetStringOnNotFound() throws JSONException {
        assertThrows(JSONException.class, () -> jsonObject.getString("value"));
    }

    @Test
    void testGetJSONArray() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        jsonObject.put("value", jsonArray);
        assertEquals(jsonArray, jsonObject.getJSONArray("value"));
    }

    @Test
    void testGetJSONArrayOnNotFound() throws JSONException {
        assertThrows(JSONException.class, () -> jsonObject.getJSONArray("value"));
        jsonObject.put("value", 1);
        assertThrows(JSONException.class, () -> jsonObject.getJSONArray("value"));
    }

    @Test
    void testGetJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        this.jsonObject.put("value", jsonObject);
        assertEquals(jsonObject, this.jsonObject.getJSONObject("value"));
    }

    @Test
    void testGetJSONObjectOnNotFound() throws JSONException {
        assertThrows(JSONException.class, () -> jsonObject.getJSONObject("value"));
        jsonObject.put("value", 1);
        assertThrows(JSONException.class, () -> jsonObject.getJSONObject("value"));
    }


    @Test
    void testOpt() throws JSONException {
        jsonObject.put("name", "Mercy");
        assertEquals("Mercy", jsonObject.opt("name"));
    }

    @Test
    void testOptBoolean() throws JSONException {
        jsonObject.put("value", TRUE);
        assertEquals(true, jsonObject.optBoolean("value"));
        assertEquals(false, jsonObject.optBoolean("value1"));
    }

    @Test
    void testOptDouble() throws JSONException {
        jsonObject.put("value", Double.valueOf(1.0));
        assertEquals(1.0, jsonObject.optDouble("value"));
        assertEquals(NaN, jsonObject.optDouble("value1"));
    }

    @Test
    void testOptInt() throws JSONException {
        jsonObject.put("value", Integer.valueOf(1));
        assertEquals(1, jsonObject.optInt("value"));
        assertEquals(0, jsonObject.optInt("value1"));
    }

    @Test
    void testOptLong() throws JSONException {
        jsonObject.put("value", Long.valueOf(1));
        assertEquals(1L, jsonObject.optLong("value"));
        assertEquals(0L, jsonObject.optLong("value1"));
    }

    @Test
    void testOptString() throws JSONException {
        jsonObject.put("name", "Mercy");
        assertEquals("Mercy", jsonObject.optString("name"));
        assertEquals("", jsonObject.optString("name1"));
    }


    @Test
    void testOptJSONArray() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        jsonObject.put("value", jsonArray);
        assertEquals(jsonArray, jsonObject.optJSONArray("value"));
        assertNull(this.jsonObject.optJSONArray("value1"));
    }


    @Test
    void testOptJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        this.jsonObject.put("value", jsonObject);
        assertEquals(jsonObject, this.jsonObject.optJSONObject("value"));
        assertNull(this.jsonObject.optJSONObject("value1"));
    }

    @Test
    void testToJSONArray() throws JSONException {
        long time = currentTimeMillis();
        jsonObject.put("name", "Mercy")
                .put("age", 18)
                .put("workingTime", time);

        JSONArray jsonArray = jsonObject.toJSONArray(new JSONArray().put("name").put("age").put("workingTime"));
        assertEquals("Mercy", jsonArray.getString(0));
        assertEquals(18, jsonArray.getInt(1));
        assertEquals(time, jsonArray.getLong(2));
    }

    @Test
    void testToJSONArrayOnNull() {
        assertNull(jsonObject.toJSONArray(null));
    }

    @Test
    void testToJSONArrayOnEmptyJSONArray() {
        assertNull(jsonObject.toJSONArray(new JSONArray()));
    }


    @Test
    void testKeys() throws JSONException {
        long time = currentTimeMillis();
        jsonObject.put("name", "Mercy")
                .put("age", 18)
                .put("workingTime", time);

        Iterator<String> keys = jsonObject.keys();
        assertEquals("name", keys.next());
        assertEquals("age", keys.next());
        assertEquals("workingTime", keys.next());
        assertFalse(keys.hasNext());
    }

    @Test
    void testNames() throws JSONException {
        long time = currentTimeMillis();
        jsonObject.put("name", "Mercy")
                .put("age", 18)
                .put("workingTime", time);

        JSONArray jsonArray = jsonObject.names();
        assertEquals("name", jsonArray.getString(0));
        assertEquals("age", jsonArray.getString(1));
        assertEquals("workingTime", jsonArray.getString(2));
    }

    @Test
    void testNamesOnEmpty() throws JSONException {
        assertNull(jsonObject.names());
    }

    @Test
    void testToString() throws JSONException {
        long time = currentTimeMillis();
        jsonObject.put("name", "Mercy")
                .put("age", 18)
                .put("workingTime", time);

        assertEquals("{\"name\":\"Mercy\",\"age\":18,\"workingTime\":" + time + "}", jsonObject.toString());
        assertEquals("{\n \"name\": \"Mercy\",\n \"age\": 18,\n \"workingTime\": " + time + "\n}", jsonObject.toString(1));
    }

    @Test
    void testEquals() {
        assertNotEquals(jsonObject, null);
        assertNotEquals(jsonObject, new Object());
        assertEquals(jsonObject, jsonObject);
        assertEquals(jsonObject, new JSONObject());
    }

    @Test
    void testHashCode() {
        assertEquals(jsonObject.hashCode(), jsonObject.hashCode());
    }

    @Test
    void testNumberToString() throws JSONException {
        assertEquals("0", numberToString(Double.valueOf(0.0)));
        assertEquals("0", numberToString(Integer.valueOf(0)));
        assertEquals("-0", numberToString(NEGATIVE_ZERO));
    }

    @Test
    void testNumberToStringOnNull() {
        assertThrows(JSONException.class, () -> numberToString(null));
    }

    @Test
    void testNumberToStringOnInvalidDouble() {
        assertThrows(JSONException.class, () -> numberToString(NaN));
        assertThrows(JSONException.class, () -> numberToString(POSITIVE_INFINITY));
        assertThrows(JSONException.class, () -> numberToString(NEGATIVE_INFINITY));
    }

    @Test
    void testQuote() {
        assertEquals("\"Mercy\"", quote("Mercy"));
    }

    @Test
    void testQuoteOnNull() {
        assertEquals("\"\"", quote(null));
    }

    @Test
    void testWrap() throws JSONException {
        assertEquals(NULL, wrap(null));
        assertWrap(NULL);
        assertWrap(this.jsonObject);
        assertWrap(new JSONArray());
        assertEquals(new JSONArray(ofList(1, 2, 3)), wrap(ofList(1, 2, 3)));
        assertEquals(new JSONArray(ofArray(1, 2, 3)), wrap(ofArray(1, 2, 3)));
        assertEquals(new JSONObject(ofMap("name", "Mercy")), wrap(ofMap("name", "Mercy")));
        assertWrap(TRUE);
        assertWrap(FALSE);
        assertWrap(Byte.MAX_VALUE);
        assertWrap(Byte.MIN_VALUE);
        assertWrap(Character.valueOf((char) 1));
        assertWrap(Short.MAX_VALUE);
        assertWrap(Short.MIN_VALUE);
        assertWrap(Integer.MAX_VALUE);
        assertWrap(Integer.MIN_VALUE);
        assertWrap(Long.MAX_VALUE);
        assertWrap(Long.MIN_VALUE);
        assertWrap(Float.MAX_VALUE);
        assertWrap(Float.MIN_VALUE);
        assertWrap(Double.MAX_VALUE);
        assertWrap(Double.MIN_VALUE);
        assertWrap("Hello");
        assertEquals("1.0", wrap(new StringBuilder("1.0")));
        assertEquals("java.lang.String", wrap(String.class));
        assertNull(wrap(this));
    }

    @Test
    void testWrapOnException() {
        Map<String, Object> map = newHashMap();
        map.put(null, null);
        assertNull(wrap(map));
    }

    @Test
    void testNULLEquals() {
        assertEquals(NULL, NULL);
        assertEquals(NULL, null);
        assertNotEquals(NULL, "null");
    }

    @Test
    void testNULLToString() {
        assertEquals("null", NULL.toString());
    }

    void assertWrap(Object object) {
        assertEquals(object, wrap(object));
    }
}