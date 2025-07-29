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

import java.util.concurrent.TimeUnit;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.Maps.ofMap;
import static io.microsphere.json.JSONUtils.append;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Byte.valueOf;
import static java.lang.Character.valueOf;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    public void testAppendOnFloat() {
        append(jsonBuilder, "name", 1.0f);
        assertEquals("\"name\":1.0", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnDouble() {
        append(jsonBuilder, "name", 1.0);
        assertEquals("\"name\":1.0", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnChar() {
        append(jsonBuilder, "name", 'a');
        assertEquals("\"name\":\"a\"", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnBooleanObject() {
        append(jsonBuilder, "name", TRUE);
        assertEquals("\"name\":true", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnByteObject() {
        append(jsonBuilder, "name", valueOf((byte) 1));
        assertEquals("\"name\":1", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnShortObject() {
        append(jsonBuilder, "name", Short.valueOf((short) 1));
        assertEquals("\"name\":1", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnIntegerObject() {
        append(jsonBuilder, "name", Integer.valueOf(1));
        assertEquals("\"name\":1", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnLongObject() {
        append(jsonBuilder, "name", Long.valueOf(1L));
        assertEquals("\"name\":1", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnFloatObject() {
        append(jsonBuilder, "name", Float.valueOf(1.0f));
        assertEquals("\"name\":1.0", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnDoubleObject() {
        append(jsonBuilder, "name", Double.valueOf(1.0));
        assertEquals("\"name\":1.0", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnCharacterObject() {
        append(jsonBuilder, "name", valueOf('a'));
        assertEquals("\"name\":\"a\"", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnString() {
        append(jsonBuilder, "name", "a");
        assertEquals("\"name\":\"a\"", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnType() {
        append(jsonBuilder, "name", String.class);
        assertEquals("\"name\":\"java.lang.String\"", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnBooleanObjectArray() {
        append(jsonBuilder, "name", new Boolean[]{TRUE, FALSE});
        assertEquals("\"name\":[true,false]", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnByteObjectArray() {
        append(jsonBuilder, "name", new Byte[]{(byte) 1, (byte) 2});
        assertEquals("\"name\":[1,2]", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnShortObjectArray() {
        append(jsonBuilder, "name", new Short[]{(short) 1, (short) 2});
        assertEquals("\"name\":[1,2]", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnIntegerObjectArray() {
        append(jsonBuilder, "name", new Integer[]{1, 2});
        assertEquals("\"name\":[1,2]", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnLongObjectArray() {
        append(jsonBuilder, "name", new Long[]{1L, 2L});
        assertEquals("\"name\":[1,2]", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnFloatObjectArray() {
        append(jsonBuilder, "name", new Float[]{1.0f, 2.0f});
        assertEquals("\"name\":[1.0,2.0]", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnDoubleObjectArray() {
        append(jsonBuilder, "name", new Double[]{1.0, 2.0});
        assertEquals("\"name\":[1.0,2.0]", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnCharacterObjectArray() {
        append(jsonBuilder, "name", new Character[]{'a', 'b'});
        assertEquals("\"name\":[\"a\",\"b\"]", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnBooleanArray() {
        append(jsonBuilder, "name", new boolean[]{true, false});
        assertEquals("\"name\":[true,false]", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnByteArray() {
        append(jsonBuilder, "name", new byte[]{1, 2});
        assertEquals("\"name\":[1,2]", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnShortArray() {
        append(jsonBuilder, "name", new short[]{1, 2});
        assertEquals("\"name\":[1,2]", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnIntegerArray() {
        append(jsonBuilder, "name", new int[]{1, 2});
        assertEquals("\"name\":[1,2]", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnLongArray() {
        append(jsonBuilder, "name", new long[]{1, 2});
        assertEquals("\"name\":[1,2]", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnFloatArray() {
        append(jsonBuilder, "name", new float[]{1.0f, 2.0f});
        assertEquals("\"name\":[1.0,2.0]", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnDoubleArray() {
        append(jsonBuilder, "name", new double[]{1.0, 2.0});
        assertEquals("\"name\":[1.0,2.0]", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnCharArray() {
        append(jsonBuilder, "name", new char[]{'a', 'b'});
        assertEquals("\"name\":[\"a\",\"b\"]", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnStringArray() {
        append(jsonBuilder, "name", new String[]{"a", "b"});
        assertEquals("\"name\":[\"a\",\"b\"]", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnObjectAsArray() {
        Object value = new boolean[]{TRUE, FALSE};
        append(jsonBuilder, "name", value);
        assertEquals("\"name\":[true,false]", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnObjectAsMap() {
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
    public void testAppendOnObjectAsIterable() {
        Object value = ofList(true, (byte) 1, '2', 3.0, 4.0f, 5L, 6, (short) 7, "8", ofArray("9", "10"));
        append(jsonBuilder, "name", value);
        assertEquals("\"name\":[true,1,\"2\",3.0,4.0,5,6,7,\"8\",[\"9\",\"10\"]]", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnObjectAsString() {
        Object value = "s";
        append(jsonBuilder, "name", value);
        assertEquals("\"name\":\"s\"", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnObjectAsInteger() {
        Object value = 1;
        append(jsonBuilder, "name", value);
        assertEquals("\"name\":1", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnGenericArray() {
        TimeUnit[] values = ofArray(DAYS, HOURS, MINUTES);
        append(jsonBuilder, "name", values);
        assertEquals("\"name\":[\"DAYS\",\"HOURS\",\"MINUTES\"]", jsonBuilder.toString());
    }

    @Test
    public void testAppendOnTypeArray() {
        Class<?>[] classes = ofArray(String.class, Integer.class);
        append(jsonBuilder, "name", classes);
        assertEquals("\"name\":[\"java.lang.String\",\"java.lang.Integer\"]", jsonBuilder.toString());
    }

}