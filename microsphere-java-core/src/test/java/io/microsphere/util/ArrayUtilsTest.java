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
package io.microsphere.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Parameter;

import static io.microsphere.util.ArrayUtils.EMPTY_BOOLEAN_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_BOOLEAN_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_BYTE_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_BYTE_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_CHARACTER_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_CHAR_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_DOUBLE_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_FLOAT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_FLOAT_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_INTEGER_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_INT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_LONG_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_LONG_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_PARAMETER_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_SHORT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_SHORT_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.combine;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.ArrayUtils.of;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.ArrayUtils.ofBooleans;
import static io.microsphere.util.ArrayUtils.ofBytes;
import static io.microsphere.util.ArrayUtils.ofChars;
import static io.microsphere.util.ArrayUtils.ofDoubles;
import static io.microsphere.util.ArrayUtils.ofFloats;
import static io.microsphere.util.ArrayUtils.ofInts;
import static io.microsphere.util.ArrayUtils.ofLongs;
import static io.microsphere.util.ArrayUtils.ofShorts;
import static io.microsphere.util.ArrayUtils.size;
import static io.microsphere.util.ClassUtils.getTopComponentType;
import static java.lang.reflect.Array.getLength;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * {@link ArrayUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ArrayUtilsTest {

    @Test
    public void testConstants() {

        assertEmptyArray(EMPTY_BOOLEAN_ARRAY, boolean.class);
        assertEmptyArray(EMPTY_CHAR_ARRAY, char.class);
        assertEmptyArray(EMPTY_BYTE_ARRAY, byte.class);
        assertEmptyArray(EMPTY_SHORT_ARRAY, short.class);
        assertEmptyArray(EMPTY_INT_ARRAY, int.class);
        assertEmptyArray(EMPTY_LONG_ARRAY, long.class);
        assertEmptyArray(EMPTY_FLOAT_ARRAY, float.class);
        assertEmptyArray(EMPTY_DOUBLE_ARRAY, double.class);

        assertEmptyArray(EMPTY_OBJECT_ARRAY, Object.class);
        assertEmptyArray(EMPTY_BOOLEAN_OBJECT_ARRAY, Boolean.class);
        assertEmptyArray(EMPTY_BYTE_OBJECT_ARRAY, Byte.class);
        assertEmptyArray(EMPTY_CHARACTER_OBJECT_ARRAY, Character.class);
        assertEmptyArray(EMPTY_SHORT_OBJECT_ARRAY, Short.class);
        assertEmptyArray(EMPTY_INTEGER_OBJECT_ARRAY, Integer.class);
        assertEmptyArray(EMPTY_LONG_OBJECT_ARRAY, Long.class);
        assertEmptyArray(EMPTY_FLOAT_OBJECT_ARRAY, Float.class);
        assertEmptyArray(EMPTY_DOUBLE_OBJECT_ARRAY, Double.class);
        assertEmptyArray(EMPTY_CLASS_ARRAY, Class.class);
        assertEmptyArray(EMPTY_STRING_ARRAY, String.class);
        assertEmptyArray(EMPTY_PARAMETER_ARRAY, Parameter.class);
    }

    @Test
    public void tesOf() {
        assertArrayEquals(of("A"), array("A"));
        assertArrayEquals(of("A", "B"), array("A", "B"));
        assertArrayEquals(of("A", "B", "C"), array("A", "B", "C"));
    }

    @Test
    public void testOfBooleans() {
        assertArrayEquals(new boolean[]{true}, ofBooleans(true));
        assertArrayEquals(new boolean[]{true, false}, ofBooleans(true, false));
        assertArrayEquals(new boolean[]{true, false, true}, ofBooleans(true, false, true));
    }

    @Test
    public void testOfBytes() {
        assertArrayEquals(new byte[]{1}, ofBytes((byte) 1));
        assertArrayEquals(new byte[]{1, 2}, ofBytes((byte) 1, (byte) 2));
        assertArrayEquals(new byte[]{1, 2, 3}, ofBytes((byte) 1, (byte) 2, (byte) 3));
    }

    @Test
    public void testOfChars() {
        assertArrayEquals(new char[]{1}, ofChars((char) 1));
        assertArrayEquals(new char[]{1, 2}, ofChars((char) 1, (char) 2));
        assertArrayEquals(new char[]{1, 2, 3}, ofChars((char) 1, (char) 2, (char) 3));
    }

    @Test
    public void testOfShorts() {
        assertArrayEquals(new short[]{1}, ofShorts((short) 1));
        assertArrayEquals(new short[]{1, 2}, ofShorts((short) 1, (short) 2));
        assertArrayEquals(new short[]{1, 2, 3}, ofShorts((short) 1, (short) 2, (short) 3));
    }

    @Test
    public void testOfInts() {
        assertArrayEquals(new int[]{1}, ofInts(1));
        assertArrayEquals(new int[]{1, 2}, ofInts(1, 2));
        assertArrayEquals(new int[]{1, 2, 3}, ofInts(1, 2, 3));
    }

    @Test
    public void testOfLongs() {
        assertArrayEquals(new long[]{1}, ofLongs(1L));
        assertArrayEquals(new long[]{1, 2}, ofLongs(1L, 2L));
        assertArrayEquals(new long[]{1, 2, 3}, ofLongs(1L, 2L, 3L));
    }

    @Test
    public void testOfFloats() {
        assertArrayEquals(new float[]{1f}, ofFloats(1f));
        assertArrayEquals(new float[]{1f, 2f}, ofFloats(1f, 2f));
        assertArrayEquals(new float[]{1f, 2f, 3f}, ofFloats(1f, 2f, 3f));
    }

    @Test
    public void testOfDoubles() {
        assertArrayEquals(new double[]{1d}, ofDoubles(1d));
        assertArrayEquals(new double[]{1d, 2d}, ofDoubles(1d, 2d));
        assertArrayEquals(new double[]{1d, 2d, 3d}, ofDoubles(1d, 2d, 3d));
    }

    @Test
    public void testOfArray() {
        assertArrayEquals(of("A"), ofArray("A"));
        assertArrayEquals(of("A", "B"), ofArray("A", "B"));
        assertArrayEquals(of("A", "B", "C"), ofArray("A", "B", "C"));
    }

    @Test
    public void testLength() {
        assertEquals(1, length(ofArray("A")));
        assertEquals(2, length(ofArray("A", "B")));
    }

    @Test
    public void testLengthOnNull() {
        assertEquals(0, length(ofArray((Object[]) null)));
    }

    @Test
    public void testLengthOnEmptyArray() {
        assertEquals(0, length(ofArray(EMPTY_OBJECT_ARRAY)));
    }

    @Test
    public void testSize() {
        assertEquals(1, size(ofArray("A")));
        assertEquals(2, size(ofArray("A", "B")));
    }

    @Test
    public void testSizeOnNull() {
        assertEquals(0, size(ofArray((Object[]) null)));
    }

    @Test
    public void testSizeOnEmptyArray() {
        assertEquals(0, size(ofArray(EMPTY_OBJECT_ARRAY)));
    }

    @Test
    public void testIsEmpty() {
        assertFalse(isEmpty(ofArray("A")));
    }

    @Test
    public void testIsEmptyOnEmptyArray() {
        assertTrue(isEmpty(EMPTY_OBJECT_ARRAY));
    }

    @Test
    public void testIsEmptyOnNull() {
        assertTrue(isEmpty(null));
    }

    @Test
    public void testIsNotEmpty() {
        assertTrue(isNotEmpty(ofArray("A")));
    }

    @Test
    public void testIsNotEmptyOnEmptyArray() {
        assertFalse(isNotEmpty(EMPTY_OBJECT_ARRAY));
    }

    @Test
    public void testIsNotEmptyOnNull() {
        assertFalse(isNotEmpty(null));
    }

    @Test
    public void testCombine() {
        assertArrayEquals(of("A", "B"), combine("A", of("B")));
        assertArrayEquals(of("A", "B", "C"), combine("A", of("B", "C")));
        assertArrayEquals(of("A", "B", "C"), combine(of("A"), of("B", "C")));
        assertArrayEquals(of("A", "B", "C"), combine(of("A"), of("B"), of("C")));

        assertArrayEquals(of("A", "B", "C", "D"),
                combine(of("A"), of("B"), of("C"), of("D"), of()));
        assertArrayEquals(of("A", "B", "C", "D"),
                combine(of("A"), of("B"), of("C"), of("D")));
        assertArrayEquals(of("A", "B", "C", "D"),
                combine(of("A", "B"), of("C"), of("D")));
        assertArrayEquals(of("A", "B", "C", "D"),
                combine(of("A", "B"), of("C", "D")));
        assertArrayEquals(of("A", "B", "C", "D"),
                combine(of("A", "B", "C"), of("D")));
        assertArrayEquals(of("A", "B", "C", "D"),
                combine(of("A", "B", "C", "D")));
    }

    public static <T> T[] array(T... values) {
        return values;
    }

    private void assertEmptyArray(Object array, Class<?> expectedComponentType) {
        assertEquals(0, getLength(array));
        assertEquals(expectedComponentType, getTopComponentType(array));
    }

    private <E> void assertEmptyArray(E[] array, Class<E> expectedComponentType) {
        assertEquals(0, length(array));
        assertEquals(expectedComponentType, getTopComponentType(array));
    }
}
