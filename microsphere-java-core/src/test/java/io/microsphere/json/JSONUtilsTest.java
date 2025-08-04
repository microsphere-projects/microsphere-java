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


import io.microsphere.beans.ConfigurationProperty;
import io.microsphere.collection.CollectionUtils;
import io.microsphere.test.Data;
import io.microsphere.test.MultipleValueData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static io.microsphere.JSONTestUtils.assertConfigurationPropertyJSON;
import static io.microsphere.JSONTestUtils.newConfigurationProperty;
import static io.microsphere.collection.EnumerationUtils.ofEnumeration;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.Maps.ofMap;
import static io.microsphere.collection.QueueUtils.ofQueue;
import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.json.JSONUtils.append;
import static io.microsphere.json.JSONUtils.jsonArray;
import static io.microsphere.json.JSONUtils.jsonObject;
import static io.microsphere.json.JSONUtils.readArray;
import static io.microsphere.json.JSONUtils.readValue;
import static io.microsphere.json.JSONUtils.readValues;
import static io.microsphere.json.JSONUtils.writeBeanAsString;
import static io.microsphere.json.JSONUtils.writeValueAsString;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Byte.valueOf;
import static java.lang.Character.valueOf;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link JSONUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see JSONUtils
 * @since 1.0.0
 */
class JSONUtilsTest {

    private StringBuilder jsonBuilder;

    @BeforeEach
    void setUp() {
        this.jsonBuilder = new StringBuilder();
    }

    @Test
    void testAppendOnBoolean() {
        append(jsonBuilder, "name", true);
        assertEquals("\"name\":true", jsonBuilder.toString());
    }

    @Test
    void testAppendOnByte() {
        append(jsonBuilder, "name", (byte) 1);
        assertEquals("\"name\":1", jsonBuilder.toString());
    }

    @Test
    void testAppendOnShort() {
        append(jsonBuilder, "name", (short) 1);
        assertEquals("\"name\":1", jsonBuilder.toString());
    }

    @Test
    void testAppendOnInt() {
        append(jsonBuilder, "name", 1);
        assertEquals("\"name\":1", jsonBuilder.toString());
    }

    @Test
    void testAppendOnLong() {
        append(jsonBuilder, "name", 1L);
        assertEquals("\"name\":1", jsonBuilder.toString());
    }

    @Test
    void testAppendOnFloat() {
        append(jsonBuilder, "name", 1.0f);
        assertEquals("\"name\":1.0", jsonBuilder.toString());
    }

    @Test
    void testAppendOnDouble() {
        append(jsonBuilder, "name", 1.0);
        assertEquals("\"name\":1.0", jsonBuilder.toString());
    }

    @Test
    void testAppendOnChar() {
        append(jsonBuilder, "name", 'a');
        assertEquals("\"name\":\"a\"", jsonBuilder.toString());
    }

    @Test
    void testAppendOnBooleanObject() {
        append(jsonBuilder, "name", TRUE);
        assertEquals("\"name\":true", jsonBuilder.toString());
    }

    @Test
    void testAppendOnByteObject() {
        append(jsonBuilder, "name", valueOf((byte) 1));
        assertEquals("\"name\":1", jsonBuilder.toString());
    }

    @Test
    void testAppendOnShortObject() {
        append(jsonBuilder, "name", Short.valueOf((short) 1));
        assertEquals("\"name\":1", jsonBuilder.toString());
    }

    @Test
    void testAppendOnIntegerObject() {
        append(jsonBuilder, "name", Integer.valueOf(1));
        assertEquals("\"name\":1", jsonBuilder.toString());
    }

    @Test
    void testAppendOnLongObject() {
        append(jsonBuilder, "name", Long.valueOf(1L));
        assertEquals("\"name\":1", jsonBuilder.toString());
    }

    @Test
    void testAppendOnFloatObject() {
        append(jsonBuilder, "name", Float.valueOf(1.0f));
        assertEquals("\"name\":1.0", jsonBuilder.toString());
    }

    @Test
    void testAppendOnDoubleObject() {
        append(jsonBuilder, "name", Double.valueOf(1.0));
        assertEquals("\"name\":1.0", jsonBuilder.toString());
    }

    @Test
    void testAppendOnCharacterObject() {
        append(jsonBuilder, "name", valueOf('a'));
        assertEquals("\"name\":\"a\"", jsonBuilder.toString());
    }

    @Test
    void testAppendOnString() {
        append(jsonBuilder, "name", "a");
        assertEquals("\"name\":\"a\"", jsonBuilder.toString());
    }

    @Test
    void testAppendOnType() {
        append(jsonBuilder, "name", String.class);
        assertEquals("\"name\":\"java.lang.String\"", jsonBuilder.toString());
    }

    @Test
    void testAppendOnBooleanObjectArray() {
        append(jsonBuilder, "name", new Boolean[]{TRUE, FALSE});
        assertEquals("\"name\":[true,false]", jsonBuilder.toString());
    }

    @Test
    void testAppendOnByteObjectArray() {
        append(jsonBuilder, "name", new Byte[]{(byte) 1, (byte) 2});
        assertEquals("\"name\":[1,2]", jsonBuilder.toString());
    }

    @Test
    void testAppendOnShortObjectArray() {
        append(jsonBuilder, "name", new Short[]{(short) 1, (short) 2});
        assertEquals("\"name\":[1,2]", jsonBuilder.toString());
    }

    @Test
    void testAppendOnIntegerObjectArray() {
        append(jsonBuilder, "name", new Integer[]{1, 2});
        assertEquals("\"name\":[1,2]", jsonBuilder.toString());
    }

    @Test
    void testAppendOnLongObjectArray() {
        append(jsonBuilder, "name", new Long[]{1L, 2L});
        assertEquals("\"name\":[1,2]", jsonBuilder.toString());
    }

    @Test
    void testAppendOnFloatObjectArray() {
        append(jsonBuilder, "name", new Float[]{1.0f, 2.0f});
        assertEquals("\"name\":[1.0,2.0]", jsonBuilder.toString());
    }

    @Test
    void testAppendOnDoubleObjectArray() {
        append(jsonBuilder, "name", new Double[]{1.0, 2.0});
        assertEquals("\"name\":[1.0,2.0]", jsonBuilder.toString());
    }

    @Test
    void testAppendOnCharacterObjectArray() {
        append(jsonBuilder, "name", new Character[]{'a', 'b'});
        assertEquals("\"name\":[\"a\",\"b\"]", jsonBuilder.toString());
    }

    @Test
    void testAppendOnBooleanArray() {
        append(jsonBuilder, "name", new boolean[]{true, false});
        assertEquals("\"name\":[true,false]", jsonBuilder.toString());
    }

    @Test
    void testAppendOnByteArray() {
        append(jsonBuilder, "name", new byte[]{1, 2});
        assertEquals("\"name\":[1,2]", jsonBuilder.toString());
    }

    @Test
    void testAppendOnShortArray() {
        append(jsonBuilder, "name", new short[]{1, 2});
        assertEquals("\"name\":[1,2]", jsonBuilder.toString());
    }

    @Test
    void testAppendOnIntegerArray() {
        append(jsonBuilder, "name", new int[]{1, 2});
        assertEquals("\"name\":[1,2]", jsonBuilder.toString());
    }

    @Test
    void testAppendOnLongArray() {
        append(jsonBuilder, "name", new long[]{1, 2});
        assertEquals("\"name\":[1,2]", jsonBuilder.toString());
    }

    @Test
    void testAppendOnFloatArray() {
        append(jsonBuilder, "name", new float[]{1.0f, 2.0f});
        assertEquals("\"name\":[1.0,2.0]", jsonBuilder.toString());
    }

    @Test
    void testAppendOnDoubleArray() {
        append(jsonBuilder, "name", new double[]{1.0, 2.0});
        assertEquals("\"name\":[1.0,2.0]", jsonBuilder.toString());
    }

    @Test
    void testAppendOnCharArray() {
        append(jsonBuilder, "name", new char[]{'a', 'b'});
        assertEquals("\"name\":[\"a\",\"b\"]", jsonBuilder.toString());
    }

    @Test
    void testAppendOnStringArray() {
        append(jsonBuilder, "name", new String[]{"a", "b"});
        assertEquals("\"name\":[\"a\",\"b\"]", jsonBuilder.toString());
    }

    @Test
    void testAppendOnObjectAsArray() {
        Object value = new boolean[]{TRUE, FALSE};
        append(jsonBuilder, "name", value);
        assertEquals("\"name\":[true,false]", jsonBuilder.toString());
    }

    @Test
    void testAppendOnObjectAsMap() {
        Object value = ofMap(
                "z", true,
                "b", (byte) 1,
                "c", '2',
                "d", 3.0,
                "f", 4.0f,
                "l", 5L,
                "i", 6,
                "s", (short) 7,
                "string", "8",
                "strings", new String[]{"9", "10"}
        );
        append(jsonBuilder, "name", value);
    }

    @Test
    void testAppendOnObjectAsIterable() {
        Object value = ofList(true, (byte) 1, '2', 3.0, 4.0f, 5L, 6, (short) 7, "8", ofArray("9", "10"));
        append(jsonBuilder, "name", value);
        assertEquals("\"name\":[true,1,\"2\",3.0,4.0,5,6,7,\"8\",[\"9\",\"10\"]]", jsonBuilder.toString());
    }

    @Test
    void testAppendOnObjectAsString() {
        Object value = "s";
        append(jsonBuilder, "name", value);
        assertEquals("\"name\":\"s\"", jsonBuilder.toString());
    }

    @Test
    void testAppendOnObjectAsInteger() {
        Object value = 1;
        append(jsonBuilder, "name", value);
        assertEquals("\"name\":1", jsonBuilder.toString());
    }

    @Test
    void testAppendOnGenericArray() {
        TimeUnit[] values = ofArray(DAYS, HOURS, MINUTES);
        append(jsonBuilder, "name", values);
        assertEquals("\"name\":[\"DAYS\",\"HOURS\",\"MINUTES\"]", jsonBuilder.toString());
    }

    @Test
    void testAppendOnTypeArray() {
        Class<?>[] classes = ofArray(String.class, Integer.class);
        append(jsonBuilder, "name", classes);
        assertEquals("\"name\":[\"java.lang.String\",\"java.lang.Integer\"]", jsonBuilder.toString());
    }

    @Test
    void testJsonObject() throws JSONException {
        String jsonString = "{\"name\":\"John\", \"age\":30}";
        JSONObject jsonObject = jsonObject(jsonString);
        assertEquals("John", jsonObject.getString("name")); // "John"
        assertEquals(30, jsonObject.getInt("age"));         // "30"
    }

    @Test
    void testJsonObjectOnInvalidContent() {
        assertThrows(IllegalArgumentException.class, () -> jsonObject("{name}"));
    }

    @Test
    void testJsonArray() throws JSONException {
        JSONArray jsonArray = jsonArray("[1,2,3]");
        assertEquals(1, jsonArray.getInt(0));
        assertEquals(2, jsonArray.getInt(1));
        assertEquals(3, jsonArray.getInt(2));
    }

    @Test
    void testJsonArrayOnInvalidContent() {
        assertThrows(IllegalArgumentException.class, () -> jsonArray("[1,2,3"));
    }

    @Test
    void testReadeValueWithSimpleData() {
        Data data = createData();
        String json = writeValueAsString(data);
        Data readValue = readValue(json, Data.class);
        assertEquals(data, readValue);
    }

    @Test
    void testReadValueWithMultipleValueData() {
        MultipleValueData md = new MultipleValueData();
        md.setStringList(ofList("a", "b", "c"));
        md.setIntegerSet(ofSet(1, 2, 3));
        md.setDataQueue(ofQueue(createData()));
        md.setClassEnumeration(ofEnumeration(String.class, Integer.class));
        md.setObjects(ofArray("Hello", 123, true, 45.67));
        String json = writeValueAsString(md);
        MultipleValueData readValue = readValue(json, MultipleValueData.class);
        assertEquals(md, readValue);
    }

    @Test
    void testReadValuesWithArray() {
        Data data = createData();
        Data[] dataArray = ofArray(data);
        String json = writeValueAsString(ofArray(data));
        Data[] dataArrayCopy = readValues(json, Data[].class, Data.class);
        assertArrayEquals(dataArray, dataArrayCopy);
        assertEquals(data, dataArray[0]);
    }

    @Test
    void testReadValuesWithList() {
        Data data = createData();
        List<Data> dataList = ofList(data);
        String json = writeValueAsString(dataList);
        List<Data> dataListCopy = readValues(json, List.class, Data.class);
        assertEquals(dataList, dataListCopy);
    }

    @Test
    void testReadValuesWithSet() {
        Data data = createData();
        Set<Data> dataSet = ofSet(data);
        String json = writeValueAsString(dataSet);
        Set<Data> dataSetCopy = readValues(json, Set.class, Data.class);
        assertEquals(dataSet, dataSetCopy);
    }

    @Test
    void testReadValuesWithQueue() {
        Data data = createData();
        Queue<Data> dataQueue = ofQueue(data);
        String json = writeValueAsString(dataQueue);
        Queue<Data> dataQueueCopy = readValues(json, Queue.class, Data.class);
        assertTrue(CollectionUtils.equals(dataQueue, dataQueueCopy));
    }

    @Test
    void testReadValuesWithEnumeration() {
        Data data = createData();
        Enumeration<Data> dataEnumeration = ofEnumeration(data);
        String json = writeValueAsString(dataEnumeration);
        Enumeration<Data> dataEnumerationCopy = readValues(json, Enumeration.class, Data.class);
        assertEquals(dataEnumeration, dataEnumerationCopy);
    }

    @Test
    void testReadValuesWithIterable() {
        Data data = createData();
        List<Data> dataList = ofList(data);
        String json = writeValueAsString(dataList);
        Iterable<Data> dataCopy = readValues(json, Iterable.class, Data.class);
        assertEquals(dataList, dataCopy);
    }

    @Test
    void testReadValuesWithUnsupportedType() {
        Data data = createData();
        List<Data> dataList = ofList(data);
        String json = writeValueAsString(dataList);
        Object dataCopy = readValues(json, Object.class, Data.class);
        assertNull(dataCopy);
    }

    @Test
    void testReadArray() {
        Data data = createData();
        Data[] dataArray = ofArray(data);
        String json = writeValueAsString(dataArray);
        Data[] dataArrayCopy = readArray(json, Data.class);
        assertArrayEquals(dataArray, dataArrayCopy);
    }

    @Test
    void testWriteValueAsStringWithJavaBean() throws JSONException {
        ConfigurationProperty configurationProperty = newConfigurationProperty();
        String json = writeValueAsString(configurationProperty);
        assertConfigurationPropertyJSON(json);
    }

    @Test
    void testWriteValueAsStringWithMap() {
        Map<String, Object> map = ofMap("key1", "value1", "key2", 2);
        String json = writeValueAsString(map);
        assertEquals(new JSONObject(map).toString(), json);
    }

    @Test
    void testWriteValueAsStringWithArray() throws JSONException {
        Object array = ofArray("value1", "value2");
        String json = writeValueAsString(array);
        assertEquals(new JSONArray(array).toString(), json);
    }

    @Test
    void testWriteValueAsStringWith() {
        String json = writeValueAsString("Hello");
        assertNull(json);
    }

    @Test
    void testWriteValueAsStringWithNull() {
        String json = writeValueAsString(null);
        assertNull(json);
    }

    @Test
    void testWriteBeanAsString() throws JSONException {
        ConfigurationProperty configurationProperty = newConfigurationProperty();
        String json = writeBeanAsString(configurationProperty);
        assertConfigurationPropertyJSON(json);
    }

    Data createData() {
        Data data = new Data();
        data.setName("Mercy");
        data.setAge(18);
        data.setMale(true);
        data.setHeight(1.78);
        data.setWeight(68.5f);
        data.setBirth(System.currentTimeMillis());
        data.setIndex((short) 1);
        data.setGrade((byte) 1);
        data.setSex('M');
        data.setObject("Testing");
        data.setNames(new String[]{"Mercy", "Mercy"});
        return data;
    }

}