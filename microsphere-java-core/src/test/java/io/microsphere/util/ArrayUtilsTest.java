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

import io.microsphere.logging.Logger;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Enumeration;

import static io.microsphere.collection.EnumerationUtils.ofEnums;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ArrayUtils.EMPTY_ANNOTATION_ARRAY;
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
import static io.microsphere.util.ArrayUtils.arrayEquals;
import static io.microsphere.util.ArrayUtils.asArray;
import static io.microsphere.util.ArrayUtils.combine;
import static io.microsphere.util.ArrayUtils.contains;
import static io.microsphere.util.ArrayUtils.forEach;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.ArrayUtils.newArray;
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

/**
 * {@link ArrayUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ArrayUtilsTest {

    private static final Logger logger = getLogger(ArrayUtilsTest.class);

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
        assertEmptyArray(EMPTY_ANNOTATION_ARRAY, Annotation.class);
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

    // Test size(...) methods

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

    // Test length(...) methods

    @Test
    public void testLengthOnBooleanArray() {
        assertEquals(1, length(ofBooleans(true)));
        assertEquals(2, length(ofBooleans(true, false)));
        assertEquals(3, length(ofBooleans(true, false, true)));
    }

    @Test
    public void testLengthOnEmptyBooleanArray() {
        assertEquals(0, length(EMPTY_BOOLEAN_ARRAY));
    }

    @Test
    public void testLengthOnNullBooleanArray() {
        assertEquals(0, length((boolean[]) null));
    }

    @Test
    public void testLengthOnByteArray() {
        assertEquals(1, length(ofBytes((byte) 1)));
        assertEquals(2, length(ofBytes((byte) 1, (byte) 2)));
        assertEquals(3, length(ofBytes((byte) 1, (byte) 2, (byte) 3)));
    }

    @Test
    public void testLengthOnEmptyByteArray() {
        assertEquals(0, length(EMPTY_BYTE_ARRAY));
    }

    @Test
    public void testLengthOnNullByteArray() {
        assertEquals(0, length((byte[]) null));
    }

    @Test
    public void testLengthOnCharArray() {
        assertEquals(1, length(ofChars((char) 1)));
        assertEquals(2, length(ofChars((char) 1, (char) 2)));
        assertEquals(3, length(ofChars((char) 1, (char) 2, (char) 3)));
    }

    @Test
    public void testLengthOnEmptyCharArray() {
        assertEquals(0, length(EMPTY_CHAR_ARRAY));
    }

    @Test
    public void testLengthOnNullCharArray() {
        assertEquals(0, length((char[]) null));
    }

    @Test
    public void testLengthOnShortArray() {
        assertEquals(1, length(ofShorts((short) 1)));
        assertEquals(2, length(ofShorts((short) 1, (short) 2)));
        assertEquals(3, length(ofShorts((short) 1, (short) 2, (short) 3)));
    }

    @Test
    public void testLengthOnEmptyShortArray() {
        assertEquals(0, length(EMPTY_SHORT_ARRAY));
    }

    @Test
    public void testLengthOnNullShortArray() {
        assertEquals(0, length((short[]) null));
    }

    @Test
    public void testLengthOnIntArray() {
        assertEquals(1, length(ofInts(1)));
        assertEquals(2, length(ofInts(1, 2)));
        assertEquals(3, length(ofInts(1, 2, 3)));
    }

    @Test
    public void testLengthOnEmptyIntArray() {
        assertEquals(0, length(EMPTY_INT_ARRAY));
    }

    @Test
    public void testLengthOnNullIntArray() {
        assertEquals(0, length((int[]) null));
    }

    @Test
    public void testLengthOnLongArray() {
        assertEquals(1, length(ofLongs(1L)));
        assertEquals(2, length(ofLongs(1L, 2L)));
        assertEquals(3, length(ofLongs(1L, 2L, 3L)));
    }

    @Test
    public void testLengthOnEmptyLongArray() {
        assertEquals(0, length(EMPTY_LONG_ARRAY));
    }

    @Test
    public void testLengthOnNullLongArray() {
        assertEquals(0, length((long[]) null));
    }

    @Test
    public void testLengthOnFloatArray() {
        assertEquals(1, length(ofFloats(1L)));
        assertEquals(2, length(ofFloats(1L, 2L)));
        assertEquals(3, length(ofFloats(1L, 2L, 3L)));
    }

    @Test
    public void testLengthOnEmptyFloatArray() {
        assertEquals(0, length(EMPTY_FLOAT_ARRAY));
    }

    @Test
    public void testLengthOnNullFloatArray() {
        assertEquals(0, length((float[]) null));
    }

    @Test
    public void testLengthOnDoubleArray() {
        assertEquals(1, length(ofDoubles(1L)));
        assertEquals(2, length(ofDoubles(1L, 2L)));
        assertEquals(3, length(ofDoubles(1L, 2L, 3L)));
    }

    @Test
    public void testLengthOnEmptyDoubleArray() {
        assertEquals(0, length(EMPTY_DOUBLE_ARRAY));
    }

    @Test
    public void testLengthOnNullDoubleArray() {
        assertEquals(0, length((double[]) null));
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

    // Test isEmpty(...) methods

    @Test
    public void testIsEmptyOnBooleanArray() {
        assertFalse(isEmpty(ofBooleans(true)));
        assertFalse(isEmpty(ofBooleans(true, false)));
        assertFalse(isEmpty(ofBooleans(true, false, true)));
    }

    @Test
    public void testIsEmptyOnEmptyBooleanArray() {
        assertTrue(isEmpty(EMPTY_BOOLEAN_ARRAY));
    }

    @Test
    public void testIsEmptyOnNullBooleanArray() {
        assertTrue(isEmpty((boolean[]) null));
    }

    @Test
    public void testIsEmptyOnByteArray() {
        assertFalse(isEmpty(ofBytes((byte) 1)));
        assertFalse(isEmpty(ofBytes((byte) 1, (byte) 2)));
        assertFalse(isEmpty(ofBytes((byte) 1, (byte) 2, (byte) 3)));
    }

    @Test
    public void testIsEmptyOnEmptyByteArray() {
        assertTrue(isEmpty(EMPTY_BYTE_ARRAY));
    }

    @Test
    public void testIsEmptyOnNullByteArray() {
        assertTrue(isEmpty((byte[]) null));
    }

    @Test
    public void testIsEmptyOnCharArray() {
        assertFalse(isEmpty(ofChars((char) 1)));
        assertFalse(isEmpty(ofChars((char) 1, (char) 2)));
        assertFalse(isEmpty(ofChars((char) 1, (char) 2, (char) 3)));
    }

    @Test
    public void testIsEmptyOnEmptyCharArray() {
        assertTrue(isEmpty(EMPTY_CHAR_ARRAY));
    }

    @Test
    public void testIsEmptyOnNullCharArray() {
        assertTrue(isEmpty((char[]) null));
    }

    @Test
    public void testIsEmptyOnShortArray() {
        assertFalse(isEmpty(ofShorts((short) 1)));
        assertFalse(isEmpty(ofShorts((short) 1, (short) 2)));
        assertFalse(isEmpty(ofShorts((short) 1, (short) 2, (short) 3)));
    }

    @Test
    public void testIsEmptyOnEmptyShortArray() {
        assertTrue(isEmpty(EMPTY_SHORT_ARRAY));
    }

    @Test
    public void testIsEmptyOnNullShortArray() {
        assertTrue(isEmpty((short[]) null));
    }

    @Test
    public void testIsEmptyOnIntArray() {
        assertFalse(isEmpty(ofInts(1)));
        assertFalse(isEmpty(ofInts(1, 2)));
        assertFalse(isEmpty(ofInts(1, 2, 3)));
    }

    @Test
    public void testIsEmptyOnEmptyIntArray() {
        assertTrue(isEmpty(EMPTY_INT_ARRAY));
    }

    @Test
    public void testIsEmptyOnNullIntArray() {
        assertTrue(isEmpty((int[]) null));
    }

    @Test
    public void testIsEmptyOnLongArray() {
        assertFalse(isEmpty(ofLongs(1L)));
        assertFalse(isEmpty(ofLongs(1L, 2L)));
        assertFalse(isEmpty(ofLongs(1L, 2L, 3L)));
    }

    @Test
    public void testIsEmptyOnEmptyLongArray() {
        assertTrue(isEmpty(EMPTY_LONG_ARRAY));
    }

    @Test
    public void testIsEmptyOnNullLongArray() {
        assertTrue(isEmpty((long[]) null));
    }

    @Test
    public void testIsEmptyOnFloatArray() {
        assertFalse(isEmpty(ofFloats(1L)));
        assertFalse(isEmpty(ofFloats(1L, 2L)));
        assertFalse(isEmpty(ofFloats(1L, 2L, 3L)));
    }

    @Test
    public void testIsEmptyOnEmptyFloatArray() {
        assertTrue(isEmpty(EMPTY_FLOAT_ARRAY));
    }

    @Test
    public void testIsEmptyOnNullFloatArray() {
        assertTrue(isEmpty((float[]) null));
    }

    @Test
    public void testIsEmptyOnDoubleArray() {
        assertFalse(isEmpty(ofDoubles(1L)));
        assertFalse(isEmpty(ofDoubles(1L, 2L)));
        assertFalse(isEmpty(ofDoubles(1L, 2L, 3L)));
    }

    @Test
    public void testIsEmptyOnEmptyDoubleArray() {
        assertTrue(isEmpty(EMPTY_DOUBLE_ARRAY));
    }

    @Test
    public void testIsEmptyOnNullDoubleArray() {
        assertTrue(isEmpty((double[]) null));
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
        assertTrue(isEmpty((Object[]) null));
    }

    // Test isNotEmpty(...) methods

    @Test
    public void testIsNotEmptyOnBooleanArray() {
        assertTrue(isNotEmpty(ofBooleans(true)));
        assertTrue(isNotEmpty(ofBooleans(true, false)));
        assertTrue(isNotEmpty(ofBooleans(true, false, true)));
    }

    @Test
    public void testIsNotEmptyOnEmptyBooleanArray() {
        assertFalse(isNotEmpty(EMPTY_BOOLEAN_ARRAY));
    }

    @Test
    public void testIsNotEmptyOnNullBooleanArray() {
        assertFalse(isNotEmpty((boolean[]) null));
    }

    @Test
    public void testIsNotEmptyOnByteArray() {
        assertTrue(isNotEmpty(ofBytes((byte) 1)));
        assertTrue(isNotEmpty(ofBytes((byte) 1, (byte) 2)));
        assertTrue(isNotEmpty(ofBytes((byte) 1, (byte) 2, (byte) 3)));
    }

    @Test
    public void testIsNotEmptyOnEmptyByteArray() {
        assertFalse(isNotEmpty(EMPTY_BYTE_ARRAY));
    }

    @Test
    public void testIsNotEmptyOnNullByteArray() {
        assertFalse(isNotEmpty((byte[]) null));
    }

    @Test
    public void testIsNotEmptyOnCharArray() {
        assertTrue(isNotEmpty(ofChars((char) 1)));
        assertTrue(isNotEmpty(ofChars((char) 1, (char) 2)));
        assertTrue(isNotEmpty(ofChars((char) 1, (char) 2, (char) 3)));
    }

    @Test
    public void testIsNotEmptyOnEmptyCharArray() {
        assertFalse(isNotEmpty(EMPTY_CHAR_ARRAY));
    }

    @Test
    public void testIsNotEmptyOnNullCharArray() {
        assertFalse(isNotEmpty((char[]) null));
    }

    @Test
    public void testIsNotEmptyOnShortArray() {
        assertTrue(isNotEmpty(ofShorts((short) 1)));
        assertTrue(isNotEmpty(ofShorts((short) 1, (short) 2)));
        assertTrue(isNotEmpty(ofShorts((short) 1, (short) 2, (short) 3)));
    }

    @Test
    public void testIsNotEmptyOnEmptyShortArray() {
        assertFalse(isNotEmpty(EMPTY_SHORT_ARRAY));
    }

    @Test
    public void testIsNotEmptyOnNullShortArray() {
        assertFalse(isNotEmpty((short[]) null));
    }

    @Test
    public void testIsNotEmptyOnIntArray() {
        assertTrue(isNotEmpty(ofInts(1)));
        assertTrue(isNotEmpty(ofInts(1, 2)));
        assertTrue(isNotEmpty(ofInts(1, 2, 3)));
    }

    @Test
    public void testIsNotEmptyOnEmptyIntArray() {
        assertFalse(isNotEmpty(EMPTY_INT_ARRAY));
    }

    @Test
    public void testIsNotEmptyOnNullIntArray() {
        assertFalse(isNotEmpty((int[]) null));
    }

    @Test
    public void testIsNotEmptyOnLongArray() {
        assertTrue(isNotEmpty(ofLongs(1L)));
        assertTrue(isNotEmpty(ofLongs(1L, 2L)));
        assertTrue(isNotEmpty(ofLongs(1L, 2L, 3L)));
    }

    @Test
    public void testIsNotEmptyOnEmptyLongArray() {
        assertFalse(isNotEmpty(EMPTY_LONG_ARRAY));
    }

    @Test
    public void testIsNotEmptyOnNullLongArray() {
        assertFalse(isNotEmpty((long[]) null));
    }

    @Test
    public void testIsNotEmptyOnFloatArray() {
        assertTrue(isNotEmpty(ofFloats(1L)));
        assertTrue(isNotEmpty(ofFloats(1L, 2L)));
        assertTrue(isNotEmpty(ofFloats(1L, 2L, 3L)));
    }

    @Test
    public void testIsNotEmptyOnEmptyFloatArray() {
        assertFalse(isNotEmpty(EMPTY_FLOAT_ARRAY));
    }

    @Test
    public void testIsNotEmptyOnNullFloatArray() {
        assertFalse(isNotEmpty((float[]) null));
    }

    @Test
    public void testIsNotEmptyOnDoubleArray() {
        assertTrue(isNotEmpty(ofDoubles(1L)));
        assertTrue(isNotEmpty(ofDoubles(1L, 2L)));
        assertTrue(isNotEmpty(ofDoubles(1L, 2L, 3L)));
    }

    @Test
    public void testIsNotEmptyOnEmptyDoubleArray() {
        assertFalse(isNotEmpty(EMPTY_DOUBLE_ARRAY));
    }

    @Test
    public void testIsNotEmptyOnNullDoubleArray() {
        assertFalse(isNotEmpty((double[]) null));
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
        assertFalse(isNotEmpty((Object[]) null));
    }

    // Test arrayEquals(...) methods;

    @Test
    public void testArrayEqualsOnBooleanArray() {
        assertTrue(arrayEquals(ofBooleans(true), ofBooleans(true)));
        assertTrue(arrayEquals(ofBooleans(true), new boolean[]{true}));

        assertTrue(arrayEquals(ofBooleans(true, false), ofBooleans(true, false)));
        assertTrue(arrayEquals(ofBooleans(true, false), new boolean[]{true, false}));
    }

    @Test
    public void testArrayEqualsOnEmptyBooleanArray() {
        assertTrue(arrayEquals(EMPTY_BOOLEAN_ARRAY, EMPTY_BOOLEAN_ARRAY));
        assertTrue(arrayEquals(EMPTY_BOOLEAN_ARRAY, new boolean[0]));
    }

    @Test
    public void testArrayEqualsOnNullBooleanArray() {
        assertTrue(arrayEquals((boolean[]) null, (boolean[]) null));
    }

    @Test
    public void testArrayEqualsOnByteArray() {
        assertTrue(arrayEquals(ofBytes((byte) 1), ofBytes((byte) 1)));
        assertTrue(arrayEquals(ofBytes((byte) 1), new byte[]{1}));

        assertTrue(arrayEquals(ofBytes((byte) 1, (byte) 2), ofBytes((byte) 1, (byte) 2)));
        assertTrue(arrayEquals(ofBytes((byte) 1, (byte) 2), new byte[]{1, 2}));
    }

    @Test
    public void testArrayEqualsOnEmptyByteArray() {
        assertTrue(arrayEquals(EMPTY_BYTE_ARRAY, EMPTY_BYTE_ARRAY));
        assertTrue(arrayEquals(EMPTY_BYTE_ARRAY, new byte[0]));
    }

    @Test
    public void testArrayEqualsOnNullByteArray() {
        assertTrue(arrayEquals((byte[]) null, (byte[]) null));
    }

    @Test
    public void testArrayEqualsOnCharArray() {
        assertTrue(arrayEquals(ofChars((char) 1), ofChars((char) 1)));
        assertTrue(arrayEquals(ofChars((char) 1), new char[]{1}));

        assertTrue(arrayEquals(ofChars((char) 1, (char) 2), ofChars((char) 1, (char) 2)));
        assertTrue(arrayEquals(ofChars((char) 1, (char) 2), new char[]{1, 2}));
    }

    @Test
    public void testArrayEqualsOnEmptyCharArray() {
        assertTrue(arrayEquals(EMPTY_CHAR_ARRAY, EMPTY_CHAR_ARRAY));
        assertTrue(arrayEquals(EMPTY_CHAR_ARRAY, new char[0]));
    }

    @Test
    public void testArrayEqualsOnNullCharArray() {
        assertTrue(arrayEquals((char[]) null, (char[]) null));
    }

    @Test
    public void testArrayEqualsOnShortArray() {
        assertTrue(arrayEquals(ofShorts((short) 1), ofShorts((short) 1)));
        assertTrue(arrayEquals(ofShorts((short) 1), new short[]{1}));

        assertTrue(arrayEquals(ofShorts((short) 1, (short) 2), ofShorts((short) 1, (short) 2)));
        assertTrue(arrayEquals(ofShorts((short) 1, (short) 2), new short[]{1, 2}));
    }

    @Test
    public void testArrayEqualsOnEmptyShortArray() {
        assertTrue(arrayEquals(EMPTY_SHORT_ARRAY, EMPTY_SHORT_ARRAY));
        assertTrue(arrayEquals(EMPTY_SHORT_ARRAY, new short[0]));
    }

    @Test
    public void testArrayEqualsOnNullShortArray() {
        assertTrue(arrayEquals((short[]) null, (short[]) null));
    }

    @Test
    public void testArrayEqualsOnIntArray() {
        assertTrue(arrayEquals(ofInts(1), ofInts(1)));
        assertTrue(arrayEquals(ofInts(1), new int[]{1}));

        assertTrue(arrayEquals(ofInts(1, 2), ofInts(1, 2)));
        assertTrue(arrayEquals(ofInts(1, 2), new int[]{1, 2}));
    }

    @Test
    public void testArrayEqualsOnEmptyIntArray() {
        assertTrue(arrayEquals(EMPTY_INT_ARRAY, EMPTY_INT_ARRAY));
        assertTrue(arrayEquals(EMPTY_INT_ARRAY, new int[0]));
    }

    @Test
    public void testArrayEqualsOnNullIntArray() {
        assertTrue(arrayEquals((int[]) null, (int[]) null));
    }

    @Test
    public void testArrayEqualsOnLongArray() {
        assertTrue(arrayEquals(ofLongs(1L), ofLongs(1L)));
        assertTrue(arrayEquals(ofLongs(1L), new long[]{1L}));

        assertTrue(arrayEquals(ofLongs(1L, 2L), ofLongs(1L, 2L)));
        assertTrue(arrayEquals(ofLongs(1L, 2L), new long[]{1L, 2L}));
    }

    @Test
    public void testArrayEqualsOnEmptyLongArray() {
        assertTrue(arrayEquals(EMPTY_LONG_ARRAY, EMPTY_LONG_ARRAY));
        assertTrue(arrayEquals(EMPTY_LONG_ARRAY, new long[0]));
    }

    @Test
    public void testArrayEqualsOnNullLongArray() {
        assertTrue(arrayEquals((long[]) null, (long[]) null));
    }

    @Test
    public void testArrayEqualsOnFloatArray() {
        assertTrue(arrayEquals(ofFloats(1F), ofFloats(1F)));
        assertTrue(arrayEquals(ofFloats(1F), new float[]{1F}));

        assertTrue(arrayEquals(ofFloats(1F, 2F), ofFloats(1F, 2F)));
        assertTrue(arrayEquals(ofFloats(1F, 2F), new float[]{1F, 2F}));
    }

    @Test
    public void testArrayEqualsOnEmptyFloatArray() {
        assertTrue(arrayEquals(EMPTY_FLOAT_ARRAY, EMPTY_FLOAT_ARRAY));
        assertTrue(arrayEquals(EMPTY_FLOAT_ARRAY, new float[0]));
    }

    @Test
    public void testArrayEqualsOnNullFloatArray() {
        assertTrue(arrayEquals((float[]) null, (float[]) null));
    }

    @Test
    public void testArrayEqualsOnDoubleArray() {
        assertTrue(arrayEquals(ofDoubles(1D), ofDoubles(1D)));
        assertTrue(arrayEquals(ofDoubles(1D), new double[]{1D}));

        assertTrue(arrayEquals(ofDoubles(1D, 2D), ofDoubles(1D, 2D)));
        assertTrue(arrayEquals(ofDoubles(1D, 2D), new double[]{1D, 2D}));
    }

    @Test
    public void testArrayEqualsOnEmptyDoubleArray() {
        assertTrue(arrayEquals(EMPTY_DOUBLE_ARRAY, EMPTY_DOUBLE_ARRAY));
        assertTrue(arrayEquals(EMPTY_DOUBLE_ARRAY, new double[0]));
    }

    @Test
    public void testArrayEqualsOnNullDoubleArray() {
        assertTrue(arrayEquals((double[]) null, (double[]) null));
    }

    @Test
    public void testArrayEqualsOnObjectArray() {
        assertTrue(arrayEquals(of("A", "B"), of("A", "B")));
        assertTrue(arrayEquals(of("A", "B"), new String[]{"A", "B"}));

        assertTrue(arrayEquals(of("A", "B"), combine("A", of("B"))));
        assertTrue(arrayEquals(of("A", "B", "C"), combine("A", of("B", "C"))));
    }

    @Test
    public void testArrayEqualsOnEmptyObjectArray() {
        assertTrue(arrayEquals(EMPTY_OBJECT_ARRAY, EMPTY_OBJECT_ARRAY));
        assertTrue(arrayEquals(EMPTY_OBJECT_ARRAY, new Object[0]));
    }

    @Test
    public void testArrayEqualsOnNullObjectArray() {
        assertTrue(arrayEquals((Object[]) null, (Object[]) null));
    }

    // Test asArray methods

    @Test
    public void testAsArrayOnEnumeration() {
        Enumeration<String> enums = ofEnums("A", "B");
        assertArrayEquals(ofArray("A", "B"), asArray(enums, String.class));
    }

    @Test
    public void testAsArrayOnIterable() {
        Iterable<String> iterable = ofList("A", "B");
        assertArrayEquals(ofArray("A", "B"), asArray(iterable, String.class));
    }

    @Test
    public void testAsArrayOnCollection() {
        Collection<String> collection = ofList("A", "B");
        assertArrayEquals(ofArray("A", "B"), asArray(collection, String.class));
    }

    // Test newArray
    @Test
    public void testNewArray() {
        Integer[] values = newArray(Integer.class, 3);
        assertEquals(3, values.length);
    }

    // Test combine

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

    // Test forEach(...) methods

    @Test
    public void testForEachWithConsumerOnBooleanArray() {
        boolean[] values = ofBooleans(true);
        forEach(values, (value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(value : {})", value);
            }
        });
    }

    @Test
    public void testForEachWithConsumerOnByteArray() {
        byte[] values = ofBytes((byte) 1);
        forEach(values, (value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(value : {})", value);
            }
        });
    }

    @Test
    public void testForEachWithConsumerOnCharArray() {
        char[] values = ofChars('A');
        forEach(values, (value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(value : {})", value);
            }
        });
    }

    @Test
    public void testForEachWithConsumerOnShortArray() {
        short[] values = ofShorts((short) 1);
        forEach(values, (value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(value : {})", value);
            }
        });
    }

    @Test
    public void testForEachWithConsumerOnIntArray() {
        int[] values = ofInts(1);
        forEach(values, (value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(value : {})", value);
            }
        });
    }

    @Test
    public void testForEachWithConsumerOnLongArray() {
        long[] values = ofLongs(1L);
        forEach(values, (value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(value : {})", value);
            }
        });
    }

    @Test
    public void testForEachWithConsumerOnFloatArray() {
        float[] values = ofFloats(1F);
        forEach(values, (value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(value : {})", value);
            }
        });
    }

    @Test
    public void testForEachWithConsumerOnDoubleArray() {
        double[] values = ofDoubles(1D);
        forEach(values, (value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(value : {})", value);
            }
        });
    }

    @Test
    public void testForEachWithConsumerOnObjectArray() {
        Object[] values = of("A");
        forEach(values, (value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(value : {})", value);
            }
        });
    }

    @Test
    public void testForEachWithBiConsumerOnBooleanArray() {
        boolean[] values = ofBooleans(true);
        forEach(values, (index, value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(index : {} , value : {})", index, value);
            }
        });
    }

    @Test
    public void testForEachWithBiConsumerOnByteArray() {
        byte[] values = ofBytes((byte) 1);
        forEach(values, (index, value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(index : {} , value : {})", index, value);
            }
        });
    }

    @Test
    public void testForEachWithBiConsumerOnCharArray() {
        char[] values = ofChars('A');
        forEach(values, (index, value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(index : {} , value : {})", index, value);
            }
        });
    }

    @Test
    public void testForEachWithBiConsumerOnShortArray() {
        short[] values = ofShorts((short) 1);
        forEach(values, (index, value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(index : {} , value : {})", index, value);
            }
        });
    }

    @Test
    public void testForEachWithBiConsumerOnIntArray() {
        int[] values = ofInts(1);
        forEach(values, (index, value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(index : {} , value : {})", index, value);
            }
        });
    }

    @Test
    public void testForEachWithBiConsumerOnLongArray() {
        long[] values = ofLongs(1L);
        forEach(values, (index, value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(index : {} , value : {})", index, value);
            }
        });
    }

    @Test
    public void testForEachWithBiConsumerOnFloatArray() {
        float[] values = ofFloats(1F);
        forEach(values, (index, value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(index : {} , value : {})", index, value);
            }
        });
    }

    @Test
    public void testForEachWithBiConsumerOnDoubleArray() {
        double[] values = ofDoubles(1D);
        forEach(values, (index, value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(index : {} , value : {})", index, value);
            }
        });
    }

    @Test
    public void testForEachWithBiConsumerOnObjectArray() {
        Object[] values = of("A");
        forEach(values, (index, value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(index : {} , value : {})", index, value);
            }
        });
    }

    @Test
    public void testContainsOnBooleanArray() {
        boolean[] values = ofBooleans(true);
        assertTrue(contains(values, true));
        assertFalse(contains(values, false));
    }

    @Test
    public void testContainsOnByteArray() {
        byte[] values = ofBytes((byte) 1);
        assertTrue(contains(values, (byte) 1));
        assertFalse(contains(values, (byte) 2));
    }

    @Test
    public void testContainsOnCharArray() {
        char[] values = ofChars('A');
        assertTrue(contains(values, 'A'));
        assertFalse(contains(values, 'B'));
    }

    @Test
    public void testContainsOnShortArray() {
        short[] values = ofShorts((short) 1);
        assertTrue(contains(values, (short) 1));
        assertFalse(contains(values, (short) 2));
    }

    @Test
    public void testContainsOnIntArray() {
        int[] values = ofInts(1);
        assertTrue(contains(values, 1));
        assertFalse(contains(values, 2));
    }

    @Test
    public void testContainsOnLongArray() {
        long[] values = ofLongs(1L);
        assertTrue(contains(values, 1L));
        assertFalse(contains(values, 2L));
    }

    @Test
    public void testContainsOnFloatArray() {
        float[] values = ofFloats(1F);
        assertTrue(contains(values, 1F));
        assertFalse(contains(values, 2F));
    }

    @Test
    public void testContainsOnDoubleArray() {
        double[] values = ofDoubles(1D);
        assertTrue(contains(values, 1D));
        assertFalse(contains(values, 2D));
    }

    @Test
    public void testContainsOnComparableArray() {
        Object[] values = of("A");
        assertTrue(contains(values, "A"));
        assertFalse(contains(values, "B"));
    }

    @Test
    public void testContainsOnObjectArray() {
        Object[] values = of(ofList("A"));
        assertTrue(contains(values, ofList("A")));
        assertFalse(contains(values, ofList("B")));
    }

    private static <T> T[] array(T... values) {
        return values;
    }

    private void assertEmptyArray(Object array, Class<?> expectedComponentType) {
        assertEquals(0, getLength(array));
        assertEquals(expectedComponentType, getTopComponentType(array));
    }

    private <E> void assertEmptyArray(E[] array, Class<E> expectedComponentType) {
        assertEquals(0, array.length);
        assertEquals(expectedComponentType, getTopComponentType(array));
    }
}
