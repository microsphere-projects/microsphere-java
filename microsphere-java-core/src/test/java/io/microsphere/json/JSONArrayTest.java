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

import java.util.Collection;
import java.util.Enumeration;

import static io.microsphere.collection.EnumerationUtils.ofEnumeration;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.json.JSONObject.NULL;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.lang.Boolean.TRUE;
import static java.lang.Double.NaN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link JSONArray} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see JSONArray
 * @since 1.0.0
 */
class JSONArrayTest {

    private JSONArray jsonArray;

    @BeforeEach
    void setUp() {
        this.jsonArray = new JSONArray();
    }

    @Test
    void testConstructorWithCollection() throws JSONException {
        JSONArray jsonArray = new JSONArray(ofList(1, 2, 3));
        assertEquals(3, jsonArray.length());
        assertEquals(1, jsonArray.getInt(0));
        assertEquals(2, jsonArray.getInt(1));
        assertEquals(3, jsonArray.getInt(2));
    }

    @Test
    void testConstructorWithCollectionOnNull() {
        JSONArray jsonArray = new JSONArray((Collection) null);
        assertEquals(0, jsonArray.length());
    }

    @Test
    void testConstructorWithJSONArray() throws JSONException {
        JSONTokener jsonTokener = new JSONTokener("[ 1 ,2 , 3]");
        JSONArray jsonArray = new JSONArray(jsonTokener);
        assertEquals(3, jsonArray.length());
        assertEquals(1, jsonArray.getInt(0));
        assertEquals(2, jsonArray.getInt(1));
        assertEquals(3, jsonArray.getInt(2));
    }

    @Test
    void testConstructorWithEnumeration() throws JSONException {
        Enumeration<?> enumeration = ofEnumeration(1, 2, 3);
        JSONArray jsonArray = new JSONArray(enumeration);
        assertEquals(3, jsonArray.length());
        assertEquals(1, jsonArray.getInt(0));
        assertEquals(2, jsonArray.getInt(1));
        assertEquals(3, jsonArray.getInt(2));
    }

    @Test
    void testConstructorWithJSONArrayOnNotJSONArray() {
        JSONTokener jsonTokener = new JSONTokener("{}");
        assertThrows(JSONException.class, () -> new JSONArray(jsonTokener));
    }

    @Test
    void testConstructorWithJSON() throws JSONException {
        JSONArray jsonArray = new JSONArray("[ 1 ,2 , 3]");
        assertEquals(3, jsonArray.length());
        assertEquals(1, jsonArray.getInt(0));
        assertEquals(2, jsonArray.getInt(1));
        assertEquals(3, jsonArray.getInt(2));
    }

    @Test
    void testConstructorWithArray() throws JSONException {
        JSONArray jsonArray = new JSONArray(ofArray(1, 2, 3));
        assertEquals(3, jsonArray.length());
        assertEquals(1, jsonArray.getInt(0));
        assertEquals(2, jsonArray.getInt(1));
        assertEquals(3, jsonArray.getInt(2));
    }

    @Test
    void testConstructorWithArrayOnNotArray() {
        assertThrows(JSONException.class, () -> new JSONArray(new Object()));
    }

    @Test
    void testLength() {
        assertEquals(0, jsonArray.length());
    }

    @Test
    void testPut() throws JSONException {
        jsonArray.put(true).put(1).put(2L).put(3.0).put("4");
        assertEquals(true, jsonArray.getBoolean(0));
        assertEquals(1, jsonArray.getInt(1));
        assertEquals(2L, jsonArray.getLong(2));
        assertEquals(3.0, jsonArray.getDouble(3));
        assertEquals("4", jsonArray.get(4));
    }

    @Test
    void testPutWithIndex() throws JSONException {
        jsonArray.put(0, true).put(1, 1).put(2, 2L).put(3, 3.0).put(4, "4");
        assertEquals(true, jsonArray.get(0));
        assertEquals(1, jsonArray.get(1));
        assertEquals(2L, jsonArray.get(2));
        assertEquals(3.0, jsonArray.get(3));
        assertEquals("4", jsonArray.get(4));
    }

    @Test
    void testIsNull() throws JSONException {
        jsonArray.put(0, true);
        jsonArray.put(1, NULL);
        assertFalse(jsonArray.isNull(0));
        assertTrue(jsonArray.isNull(1));
        assertTrue(jsonArray.isNull(2));
    }

    @Test
    void testGet() throws JSONException {
        jsonArray.put(true);
        assertEquals(TRUE, jsonArray.get(0));
    }

    @Test
    void testGetOnNullElement() {
        jsonArray.put(null);
        assertThrows(JSONException.class, () -> jsonArray.get(0));
    }

    @Test
    void testGetOnIndexOutOfBoundsException() {
        jsonArray.put(null);
        assertThrows(JSONException.class, () -> jsonArray.get(1));
    }

    @Test
    void testOpt() {
        jsonArray.put(true);
        assertEquals(TRUE, jsonArray.opt(0));
        assertNull(jsonArray.opt(1));
        assertNull(jsonArray.opt(-1));
    }

    @Test
    void testRemove() throws JSONException {
        jsonArray.put(true);
        assertEquals(TRUE, jsonArray.get(0));
        assertEquals(TRUE, jsonArray.remove(0));
    }

    @Test
    void testRemoveOnOutOfBounds() {
        assertNull(jsonArray.remove(0));
        assertNull(jsonArray.remove(1));
        assertNull(jsonArray.remove(-1));
    }

    @Test
    void testGetBoolean() throws JSONException {
        jsonArray.put(true);
        jsonArray.put("false");
        assertTrue(jsonArray.getBoolean(0));
        assertFalse(jsonArray.getBoolean(1));
    }

    @Test
    void testGetBooleanOnNotFound() {
        jsonArray.put("Hello");
        jsonArray.put(null);
        assertThrows(JSONException.class, () -> jsonArray.getBoolean(0));
        assertThrows(JSONException.class, () -> jsonArray.getBoolean(1));
    }

    @Test
    void testOptBoolean() {
        jsonArray.put(true);
        jsonArray.put("false");
        assertTrue(jsonArray.optBoolean(0));
        assertFalse(jsonArray.optBoolean(1));
        assertFalse(jsonArray.optBoolean(2));
    }

    @Test
    void testGetDouble() throws JSONException {
        jsonArray.put(1.0);
        jsonArray.put("2.0");
        jsonArray.put(3);
        assertEquals(1.0, jsonArray.getDouble(0));
        assertEquals(2.0, jsonArray.getDouble(1));
        assertEquals(3.0, jsonArray.getDouble(2));
    }

    @Test
    void testGetDoubleOnNotFound() throws JSONException {
        jsonArray.put("false");
        // NumberFormatException
        assertThrows(JSONException.class, () -> jsonArray.getDouble(0));
        // IndexOutOfBoundsException
        assertThrows(JSONException.class, () -> jsonArray.getDouble(1));
    }

    @Test
    void testOptDouble() throws JSONException {
        jsonArray.put("false");
        jsonArray.put("true");
        jsonArray.put(3);
        assertEquals(NaN, jsonArray.optDouble(0));
        assertEquals(NaN, jsonArray.optDouble(1));
        assertEquals(3.0, jsonArray.optDouble(2));
    }

    @Test
    void testGetInt() throws JSONException {
        jsonArray.put(1);
        jsonArray.put("2");
        jsonArray.put(3.0);
        assertEquals(1, jsonArray.getInt(0));
        assertEquals(2, jsonArray.getInt(1));
        assertEquals(3, jsonArray.getInt(2));
    }

    @Test
    void testGetIntOnNotFound() throws JSONException {
        jsonArray.put("false");
        assertThrows(JSONException.class, () -> jsonArray.getInt(0));
        assertThrows(JSONException.class, () -> jsonArray.getInt(1));
        assertThrows(JSONException.class, () -> jsonArray.getInt(-1));
    }

    @Test
    void testOptInt() throws JSONException {
        jsonArray.put(1);
        jsonArray.put("2");
        jsonArray.put(3.0);
        assertEquals(1, jsonArray.optInt(0));
        assertEquals(2, jsonArray.optInt(1));
        assertEquals(3, jsonArray.optInt(2));
        assertEquals(0, jsonArray.optInt(3));
        assertEquals(0, jsonArray.optInt(-1));
    }

    @Test
    void testGetLong() throws JSONException {
        jsonArray.put(1);
        jsonArray.put("2");
        jsonArray.put(3.0);
        assertEquals(1L, jsonArray.getLong(0));
        assertEquals(2L, jsonArray.getLong(1));
        assertEquals(3L, jsonArray.getLong(2));
    }

    @Test
    void testGetLongOnNotFound() {
        jsonArray.put(false);
        assertThrows(JSONException.class, () -> jsonArray.getLong(0));
        assertThrows(JSONException.class, () -> jsonArray.getLong(1));
        assertThrows(JSONException.class, () -> jsonArray.getLong(-1));
    }

    @Test
    void testOptLong() throws JSONException {
        jsonArray.put(false);
        jsonArray.put(1L);
        jsonArray.put(3.0);

        assertEquals(0L, jsonArray.optLong(0));
        assertEquals(1L, jsonArray.optLong(1));
        assertEquals(3L, jsonArray.optLong(2));
        assertEquals(0L, jsonArray.optLong(3));
        assertEquals(0L, jsonArray.optLong(-1));
    }

    @Test
    void testGetString() throws JSONException {
        jsonArray.put("Hello");
        assertEquals("Hello", jsonArray.getString(0));
    }

    @Test
    void testGetStringOnNotFound() throws JSONException {
        assertThrows(JSONException.class, () -> jsonArray.getString(0));
        assertThrows(JSONException.class, () -> jsonArray.getString(1));
        assertThrows(JSONException.class, () -> jsonArray.getString(-1));
    }

    @Test
    void testOptString() {
        jsonArray.put("Hello");
        assertEquals("Hello", jsonArray.optString(0));
        assertEquals("", jsonArray.optString(1));
        assertEquals("", jsonArray.optString(-1));
    }

    @Test
    void testGetJSONArray() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(jsonArray);
        assertSame(jsonArray, jsonArray.getJSONArray(0));
    }

    @Test
    void testGetJSONArrayOnNotFound() {
        jsonArray.put(null);
        jsonArray.put("");
        assertThrows(JSONException.class, () -> jsonArray.getJSONArray(0));
        assertThrows(JSONException.class, () -> jsonArray.getJSONArray(1));
        assertThrows(JSONException.class, () -> jsonArray.getJSONArray(-1));
    }

    @Test
    void testOptJSONArray() {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(jsonArray);
        assertSame(jsonArray, jsonArray.optJSONArray(0));
        assertNull(jsonArray.optJSONArray(1));
        assertNull(jsonArray.optJSONArray(-1));
    }

    @Test
    void testGetJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonArray.put(jsonObject);
        assertSame(jsonObject, jsonArray.getJSONObject(0));
    }

    @Test
    void testGetJSONObjectNotFound() {
        jsonArray.put(null);
        jsonArray.put("");
        assertThrows(JSONException.class, () -> jsonArray.getJSONObject(0));
        assertThrows(JSONException.class, () -> jsonArray.getJSONObject(1));
        assertThrows(JSONException.class, () -> jsonArray.getJSONObject(-1));
    }

    @Test
    void testOptJSONObject() {
        JSONObject jsonObject = new JSONObject();
        jsonArray.put(jsonObject);
        assertSame(jsonObject, jsonArray.optJSONObject(0));
        assertNull(jsonArray.optJSONObject(1));
        assertNull(jsonArray.optJSONObject(-1));
    }

    @Test
    void testToJSONObject() throws JSONException {
        jsonArray.put("Mercy").put(18);
        JSONObject jsonObject = jsonArray.toJSONObject(new JSONArray(ofList("name", "age")));
        assertEquals("Mercy", jsonObject.getString("name"));
        assertEquals(18, jsonObject.getInt("age"));
    }

    @Test
    void testToJSONObjectOnEmptyNames() throws JSONException {
        assertNull(jsonArray.toJSONObject(null));
        assertNull(jsonArray.toJSONObject(new JSONArray()));
        assertNull(jsonArray.toJSONObject(new JSONArray(ofList(1))));
    }

    @Test
    void testJoin() throws JSONException {
        jsonArray.put(1)
                .put(2)
                .put(3);
        assertEquals("1,2,3", jsonArray.join(","));
    }

    @Test
    void testToString() throws JSONException {
        jsonArray.put(1)
                .put(2)
                .put(3);
        assertEquals("[1,2,3]", jsonArray.toString());
        assertEquals("[\n 1,\n 2,\n 3\n]", jsonArray.toString(1));
    }

    @Test
    void testWriteTo() throws JSONException {
        jsonArray.put(1)
                .put(2)
                .put(3);

        JSONStringer stringer = new JSONStringer();
        jsonArray.writeTo(stringer);

        assertEquals("[1,2,3]", stringer.toString());
    }

    @Test
    void testEquals() {
        jsonArray.put(1)
                .put(2)
                .put(3);
        assertTrue(jsonArray.equals(jsonArray));
        assertTrue(jsonArray.equals(new JSONArray().put(1).put(2).put(3)));
        assertFalse(jsonArray.equals(new Object()));
    }

    @Test
    void testHashCode() {
        jsonArray.put(1)
                .put(2)
                .put(3);
        assertEquals(jsonArray.hashCode(), jsonArray.hashCode());
        assertEquals(ofList(1, 2, 3).hashCode(), jsonArray.hashCode());
    }
}