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

import io.microsphere.AbstractTestCase;
import io.microsphere.logging.Logger;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import static io.microsphere.collection.EnumerationUtils.ofEnumeration;
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
import static io.microsphere.util.ArrayUtils.EMPTY_FILE_ARRAY;
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
import static io.microsphere.util.ArrayUtils.EMPTY_URL_ARRAY;
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
import static io.microsphere.util.ArrayUtils.reverse;
import static io.microsphere.util.ArrayUtils.size;
import static io.microsphere.util.ArrayUtils.toArrayReversed;
import static io.microsphere.util.ClassUtils.getTopComponentType;
import static java.lang.reflect.Array.getLength;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ArrayUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class ArrayUtilsTest extends AbstractTestCase {

    private static final Logger logger = getLogger(ArrayUtilsTest.class);

    @Test
    void testConstants() {

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
        assertEmptyArray(EMPTY_FILE_ARRAY, File.class);
        assertEmptyArray(EMPTY_URL_ARRAY, URL.class);
        assertEmptyArray(EMPTY_PARAMETER_ARRAY, Parameter.class);
        assertEmptyArray(EMPTY_ANNOTATION_ARRAY, Annotation.class);
    }

    @Test
    void tesOf() {
        assertArrayEquals(of("A"), ofArray("A"));
        assertArrayEquals(of("A", "B"), ofArray("A", "B"));
        assertArrayEquals(of("A", "B", "C"), ofArray("A", "B", "C"));
    }

    @Test
    void testOfBooleans() {
        assertArrayEquals(new boolean[]{true}, ofBooleans(true));
        assertArrayEquals(new boolean[]{true, false}, ofBooleans(true, false));
        assertArrayEquals(new boolean[]{true, false, true}, ofBooleans(true, false, true));
    }

    @Test
    void testOfBytes() {
        assertArrayEquals(new byte[]{1}, ofBytes((byte) 1));
        assertArrayEquals(new byte[]{1, 2}, ofBytes((byte) 1, (byte) 2));
        assertArrayEquals(new byte[]{1, 2, 3}, ofBytes((byte) 1, (byte) 2, (byte) 3));
    }

    @Test
    void testOfChars() {
        assertArrayEquals(new char[]{1}, ofChars((char) 1));
        assertArrayEquals(new char[]{1, 2}, ofChars((char) 1, (char) 2));
        assertArrayEquals(new char[]{1, 2, 3}, ofChars((char) 1, (char) 2, (char) 3));
    }

    @Test
    void testOfShorts() {
        assertArrayEquals(new short[]{1}, ofShorts((short) 1));
        assertArrayEquals(new short[]{1, 2}, ofShorts((short) 1, (short) 2));
        assertArrayEquals(new short[]{1, 2, 3}, ofShorts((short) 1, (short) 2, (short) 3));
    }

    @Test
    void testOfInts() {
        assertArrayEquals(new int[]{1}, ofInts(1));
        assertArrayEquals(new int[]{1, 2}, ofInts(1, 2));
        assertArrayEquals(new int[]{1, 2, 3}, ofInts(1, 2, 3));
    }

    @Test
    void testOfLongs() {
        assertArrayEquals(new long[]{1}, ofLongs(1L));
        assertArrayEquals(new long[]{1, 2}, ofLongs(1L, 2L));
        assertArrayEquals(new long[]{1, 2, 3}, ofLongs(1L, 2L, 3L));
    }

    @Test
    void testOfFloats() {
        assertArrayEquals(new float[]{1f}, ofFloats(1f));
        assertArrayEquals(new float[]{1f, 2f}, ofFloats(1f, 2f));
        assertArrayEquals(new float[]{1f, 2f, 3f}, ofFloats(1f, 2f, 3f));
    }

    @Test
    void testOfDoubles() {
        assertArrayEquals(new double[]{1d}, ofDoubles(1d));
        assertArrayEquals(new double[]{1d, 2d}, ofDoubles(1d, 2d));
        assertArrayEquals(new double[]{1d, 2d, 3d}, ofDoubles(1d, 2d, 3d));
    }

    @Test
    void testOfArray() {
        assertArrayEquals(of("A"), ofArray("A"));
        assertArrayEquals(of("A", "B"), ofArray("A", "B"));
        assertArrayEquals(of("A", "B", "C"), ofArray("A", "B", "C"));
    }

    // Test size(...) methods

    @Test
    void testSize() {
        assertEquals(1, size(ofArray("A")));
        assertEquals(2, size(ofArray("A", "B")));
    }

    @Test
    void testSizeOnNull() {
        assertEquals(0, size(ofArray(TEST_NULL_OBJECT_ARRAY)));
    }

    @Test
    void testSizeOnEmptyArray() {
        assertEquals(0, size(ofArray(EMPTY_OBJECT_ARRAY)));
    }

    // Test length(...) methods

    @Test
    void testLengthOnBooleanArray() {
        assertEquals(1, length(ofBooleans(true)));
        assertEquals(2, length(ofBooleans(true, false)));
        assertEquals(3, length(ofBooleans(true, false, true)));
    }

    @Test
    void testLengthOnEmptyBooleanArray() {
        assertEquals(0, length(EMPTY_BOOLEAN_ARRAY));
    }

    @Test
    void testLengthOnNullBooleanArray() {
        assertEquals(0, length((boolean[]) null));
    }

    @Test
    void testLengthOnByteArray() {
        assertEquals(1, length(ofBytes((byte) 1)));
        assertEquals(2, length(ofBytes((byte) 1, (byte) 2)));
        assertEquals(3, length(ofBytes((byte) 1, (byte) 2, (byte) 3)));
    }

    @Test
    void testLengthOnEmptyByteArray() {
        assertEquals(0, length(EMPTY_BYTE_ARRAY));
    }

    @Test
    void testLengthOnNullByteArray() {
        assertEquals(0, length((byte[]) null));
    }

    @Test
    void testLengthOnCharArray() {
        assertEquals(1, length(ofChars((char) 1)));
        assertEquals(2, length(ofChars((char) 1, (char) 2)));
        assertEquals(3, length(ofChars((char) 1, (char) 2, (char) 3)));
    }

    @Test
    void testLengthOnEmptyCharArray() {
        assertEquals(0, length(EMPTY_CHAR_ARRAY));
    }

    @Test
    void testLengthOnNullCharArray() {
        assertEquals(0, length((char[]) null));
    }

    @Test
    void testLengthOnShortArray() {
        assertEquals(1, length(ofShorts((short) 1)));
        assertEquals(2, length(ofShorts((short) 1, (short) 2)));
        assertEquals(3, length(ofShorts((short) 1, (short) 2, (short) 3)));
    }

    @Test
    void testLengthOnEmptyShortArray() {
        assertEquals(0, length(EMPTY_SHORT_ARRAY));
    }

    @Test
    void testLengthOnNullShortArray() {
        assertEquals(0, length((short[]) null));
    }

    @Test
    void testLengthOnIntArray() {
        assertEquals(1, length(ofInts(1)));
        assertEquals(2, length(ofInts(1, 2)));
        assertEquals(3, length(ofInts(1, 2, 3)));
    }

    @Test
    void testLengthOnEmptyIntArray() {
        assertEquals(0, length(EMPTY_INT_ARRAY));
    }

    @Test
    void testLengthOnNullIntArray() {
        assertEquals(0, length((int[]) null));
    }

    @Test
    void testLengthOnLongArray() {
        assertEquals(1, length(ofLongs(1L)));
        assertEquals(2, length(ofLongs(1L, 2L)));
        assertEquals(3, length(ofLongs(1L, 2L, 3L)));
    }

    @Test
    void testLengthOnEmptyLongArray() {
        assertEquals(0, length(EMPTY_LONG_ARRAY));
    }

    @Test
    void testLengthOnNullLongArray() {
        assertEquals(0, length((long[]) null));
    }

    @Test
    void testLengthOnFloatArray() {
        assertEquals(1, length(ofFloats(1L)));
        assertEquals(2, length(ofFloats(1L, 2L)));
        assertEquals(3, length(ofFloats(1L, 2L, 3L)));
    }

    @Test
    void testLengthOnEmptyFloatArray() {
        assertEquals(0, length(EMPTY_FLOAT_ARRAY));
    }

    @Test
    void testLengthOnNullFloatArray() {
        assertEquals(0, length((float[]) null));
    }

    @Test
    void testLengthOnDoubleArray() {
        assertEquals(1, length(ofDoubles(1L)));
        assertEquals(2, length(ofDoubles(1L, 2L)));
        assertEquals(3, length(ofDoubles(1L, 2L, 3L)));
    }

    @Test
    void testLengthOnEmptyDoubleArray() {
        assertEquals(0, length(EMPTY_DOUBLE_ARRAY));
    }

    @Test
    void testLengthOnNullDoubleArray() {
        assertEquals(0, length((double[]) null));
    }

    @Test
    void testLength() {
        assertEquals(1, length(ofArray("A")));
        assertEquals(2, length(ofArray("A", "B")));
    }

    @Test
    void testLengthOnNull() {
        assertEquals(0, length(ofArray(TEST_NULL_OBJECT_ARRAY)));
    }

    @Test
    void testLengthOnEmptyArray() {
        assertEquals(0, length(ofArray(EMPTY_OBJECT_ARRAY)));
    }

    // Test isEmpty(...) methods

    @Test
    void testIsEmptyOnBooleanArray() {
        assertFalse(isEmpty(ofBooleans(true)));
        assertFalse(isEmpty(ofBooleans(true, false)));
        assertFalse(isEmpty(ofBooleans(true, false, true)));
    }

    @Test
    void testIsEmptyOnEmptyBooleanArray() {
        assertTrue(isEmpty(EMPTY_BOOLEAN_ARRAY));
    }

    @Test
    void testIsEmptyOnNullBooleanArray() {
        assertTrue(isEmpty((boolean[]) null));
    }

    @Test
    void testIsEmptyOnByteArray() {
        assertFalse(isEmpty(ofBytes((byte) 1)));
        assertFalse(isEmpty(ofBytes((byte) 1, (byte) 2)));
        assertFalse(isEmpty(ofBytes((byte) 1, (byte) 2, (byte) 3)));
    }

    @Test
    void testIsEmptyOnEmptyByteArray() {
        assertTrue(isEmpty(EMPTY_BYTE_ARRAY));
    }

    @Test
    void testIsEmptyOnNullByteArray() {
        assertTrue(isEmpty((byte[]) null));
    }

    @Test
    void testIsEmptyOnCharArray() {
        assertFalse(isEmpty(ofChars((char) 1)));
        assertFalse(isEmpty(ofChars((char) 1, (char) 2)));
        assertFalse(isEmpty(ofChars((char) 1, (char) 2, (char) 3)));
    }

    @Test
    void testIsEmptyOnEmptyCharArray() {
        assertTrue(isEmpty(EMPTY_CHAR_ARRAY));
    }

    @Test
    void testIsEmptyOnNullCharArray() {
        assertTrue(isEmpty((char[]) null));
    }

    @Test
    void testIsEmptyOnShortArray() {
        assertFalse(isEmpty(ofShorts((short) 1)));
        assertFalse(isEmpty(ofShorts((short) 1, (short) 2)));
        assertFalse(isEmpty(ofShorts((short) 1, (short) 2, (short) 3)));
    }

    @Test
    void testIsEmptyOnEmptyShortArray() {
        assertTrue(isEmpty(EMPTY_SHORT_ARRAY));
    }

    @Test
    void testIsEmptyOnNullShortArray() {
        assertTrue(isEmpty((short[]) null));
    }

    @Test
    void testIsEmptyOnIntArray() {
        assertFalse(isEmpty(ofInts(1)));
        assertFalse(isEmpty(ofInts(1, 2)));
        assertFalse(isEmpty(ofInts(1, 2, 3)));
    }

    @Test
    void testIsEmptyOnEmptyIntArray() {
        assertTrue(isEmpty(EMPTY_INT_ARRAY));
    }

    @Test
    void testIsEmptyOnNullIntArray() {
        assertTrue(isEmpty((int[]) null));
    }

    @Test
    void testIsEmptyOnLongArray() {
        assertFalse(isEmpty(ofLongs(1L)));
        assertFalse(isEmpty(ofLongs(1L, 2L)));
        assertFalse(isEmpty(ofLongs(1L, 2L, 3L)));
    }

    @Test
    void testIsEmptyOnEmptyLongArray() {
        assertTrue(isEmpty(EMPTY_LONG_ARRAY));
    }

    @Test
    void testIsEmptyOnNullLongArray() {
        assertTrue(isEmpty((long[]) null));
    }

    @Test
    void testIsEmptyOnFloatArray() {
        assertFalse(isEmpty(ofFloats(1L)));
        assertFalse(isEmpty(ofFloats(1L, 2L)));
        assertFalse(isEmpty(ofFloats(1L, 2L, 3L)));
    }

    @Test
    void testIsEmptyOnEmptyFloatArray() {
        assertTrue(isEmpty(EMPTY_FLOAT_ARRAY));
    }

    @Test
    void testIsEmptyOnNullFloatArray() {
        assertTrue(isEmpty((float[]) null));
    }

    @Test
    void testIsEmptyOnDoubleArray() {
        assertFalse(isEmpty(ofDoubles(1L)));
        assertFalse(isEmpty(ofDoubles(1L, 2L)));
        assertFalse(isEmpty(ofDoubles(1L, 2L, 3L)));
    }

    @Test
    void testIsEmptyOnEmptyDoubleArray() {
        assertTrue(isEmpty(EMPTY_DOUBLE_ARRAY));
    }

    @Test
    void testIsEmptyOnNullDoubleArray() {
        assertTrue(isEmpty((double[]) null));
    }

    @Test
    void testIsEmpty() {
        assertFalse(isEmpty(ofArray("A")));
    }

    @Test
    void testIsEmptyOnEmptyArray() {
        assertTrue(isEmpty(EMPTY_OBJECT_ARRAY));
    }

    @Test
    void testIsEmptyOnNull() {
        assertTrue(isEmpty(TEST_NULL_OBJECT_ARRAY));
    }

    // Test isNotEmpty(...) methods

    @Test
    void testIsNotEmptyOnBooleanArray() {
        assertTrue(isNotEmpty(ofBooleans(true)));
        assertTrue(isNotEmpty(ofBooleans(true, false)));
        assertTrue(isNotEmpty(ofBooleans(true, false, true)));
    }

    @Test
    void testIsNotEmptyOnEmptyBooleanArray() {
        assertFalse(isNotEmpty(EMPTY_BOOLEAN_ARRAY));
    }

    @Test
    void testIsNotEmptyOnNullBooleanArray() {
        assertFalse(isNotEmpty((boolean[]) null));
    }

    @Test
    void testIsNotEmptyOnByteArray() {
        assertTrue(isNotEmpty(ofBytes((byte) 1)));
        assertTrue(isNotEmpty(ofBytes((byte) 1, (byte) 2)));
        assertTrue(isNotEmpty(ofBytes((byte) 1, (byte) 2, (byte) 3)));
    }

    @Test
    void testIsNotEmptyOnEmptyByteArray() {
        assertFalse(isNotEmpty(EMPTY_BYTE_ARRAY));
    }

    @Test
    void testIsNotEmptyOnNullByteArray() {
        assertFalse(isNotEmpty((byte[]) null));
    }

    @Test
    void testIsNotEmptyOnCharArray() {
        assertTrue(isNotEmpty(ofChars((char) 1)));
        assertTrue(isNotEmpty(ofChars((char) 1, (char) 2)));
        assertTrue(isNotEmpty(ofChars((char) 1, (char) 2, (char) 3)));
    }

    @Test
    void testIsNotEmptyOnEmptyCharArray() {
        assertFalse(isNotEmpty(EMPTY_CHAR_ARRAY));
    }

    @Test
    void testIsNotEmptyOnNullCharArray() {
        assertFalse(isNotEmpty((char[]) null));
    }

    @Test
    void testIsNotEmptyOnShortArray() {
        assertTrue(isNotEmpty(ofShorts((short) 1)));
        assertTrue(isNotEmpty(ofShorts((short) 1, (short) 2)));
        assertTrue(isNotEmpty(ofShorts((short) 1, (short) 2, (short) 3)));
    }

    @Test
    void testIsNotEmptyOnEmptyShortArray() {
        assertFalse(isNotEmpty(EMPTY_SHORT_ARRAY));
    }

    @Test
    void testIsNotEmptyOnNullShortArray() {
        assertFalse(isNotEmpty((short[]) null));
    }

    @Test
    void testIsNotEmptyOnIntArray() {
        assertTrue(isNotEmpty(ofInts(1)));
        assertTrue(isNotEmpty(ofInts(1, 2)));
        assertTrue(isNotEmpty(ofInts(1, 2, 3)));
    }

    @Test
    void testIsNotEmptyOnEmptyIntArray() {
        assertFalse(isNotEmpty(EMPTY_INT_ARRAY));
    }

    @Test
    void testIsNotEmptyOnNullIntArray() {
        assertFalse(isNotEmpty((int[]) null));
    }

    @Test
    void testIsNotEmptyOnLongArray() {
        assertTrue(isNotEmpty(ofLongs(1L)));
        assertTrue(isNotEmpty(ofLongs(1L, 2L)));
        assertTrue(isNotEmpty(ofLongs(1L, 2L, 3L)));
    }

    @Test
    void testIsNotEmptyOnEmptyLongArray() {
        assertFalse(isNotEmpty(EMPTY_LONG_ARRAY));
    }

    @Test
    void testIsNotEmptyOnNullLongArray() {
        assertFalse(isNotEmpty((long[]) null));
    }

    @Test
    void testIsNotEmptyOnFloatArray() {
        assertTrue(isNotEmpty(ofFloats(1L)));
        assertTrue(isNotEmpty(ofFloats(1L, 2L)));
        assertTrue(isNotEmpty(ofFloats(1L, 2L, 3L)));
    }

    @Test
    void testIsNotEmptyOnEmptyFloatArray() {
        assertFalse(isNotEmpty(EMPTY_FLOAT_ARRAY));
    }

    @Test
    void testIsNotEmptyOnNullFloatArray() {
        assertFalse(isNotEmpty((float[]) null));
    }

    @Test
    void testIsNotEmptyOnDoubleArray() {
        assertTrue(isNotEmpty(ofDoubles(1L)));
        assertTrue(isNotEmpty(ofDoubles(1L, 2L)));
        assertTrue(isNotEmpty(ofDoubles(1L, 2L, 3L)));
    }

    @Test
    void testIsNotEmptyOnEmptyDoubleArray() {
        assertFalse(isNotEmpty(EMPTY_DOUBLE_ARRAY));
    }

    @Test
    void testIsNotEmptyOnNullDoubleArray() {
        assertFalse(isNotEmpty((double[]) null));
    }

    @Test
    void testIsNotEmpty() {
        assertTrue(isNotEmpty(ofArray("A")));
    }

    @Test
    void testIsNotEmptyOnEmptyArray() {
        assertFalse(isNotEmpty(EMPTY_OBJECT_ARRAY));
    }

    @Test
    void testIsNotEmptyOnNull() {
        assertFalse(isNotEmpty(TEST_NULL_OBJECT_ARRAY));
    }

    // Test arrayEquals(...) methods;

    @Test
    void testArrayEqualsOnBooleanArray() {
        assertTrue(arrayEquals(ofBooleans(true), ofBooleans(true)));
        assertTrue(arrayEquals(ofBooleans(true), new boolean[]{true}));

        assertTrue(arrayEquals(ofBooleans(true, false), ofBooleans(true, false)));
        assertTrue(arrayEquals(ofBooleans(true, false), new boolean[]{true, false}));
    }

    @Test
    void testArrayEqualsOnEmptyBooleanArray() {
        assertTrue(arrayEquals(EMPTY_BOOLEAN_ARRAY, EMPTY_BOOLEAN_ARRAY));
        assertTrue(arrayEquals(EMPTY_BOOLEAN_ARRAY, new boolean[0]));
    }

    @Test
    void testArrayEqualsOnNullBooleanArray() {
        assertTrue(arrayEquals((boolean[]) null, (boolean[]) null));
    }

    @Test
    void testArrayEqualsOnByteArray() {
        assertTrue(arrayEquals(ofBytes((byte) 1), ofBytes((byte) 1)));
        assertTrue(arrayEquals(ofBytes((byte) 1), new byte[]{1}));

        assertTrue(arrayEquals(ofBytes((byte) 1, (byte) 2), ofBytes((byte) 1, (byte) 2)));
        assertTrue(arrayEquals(ofBytes((byte) 1, (byte) 2), new byte[]{1, 2}));
    }

    @Test
    void testArrayEqualsOnEmptyByteArray() {
        assertTrue(arrayEquals(EMPTY_BYTE_ARRAY, EMPTY_BYTE_ARRAY));
        assertTrue(arrayEquals(EMPTY_BYTE_ARRAY, new byte[0]));
    }

    @Test
    void testArrayEqualsOnNullByteArray() {
        assertTrue(arrayEquals((byte[]) null, (byte[]) null));
    }

    @Test
    void testArrayEqualsOnCharArray() {
        assertTrue(arrayEquals(ofChars((char) 1), ofChars((char) 1)));
        assertTrue(arrayEquals(ofChars((char) 1), new char[]{1}));

        assertTrue(arrayEquals(ofChars((char) 1, (char) 2), ofChars((char) 1, (char) 2)));
        assertTrue(arrayEquals(ofChars((char) 1, (char) 2), new char[]{1, 2}));
    }

    @Test
    void testArrayEqualsOnEmptyCharArray() {
        assertTrue(arrayEquals(EMPTY_CHAR_ARRAY, EMPTY_CHAR_ARRAY));
        assertTrue(arrayEquals(EMPTY_CHAR_ARRAY, new char[0]));
    }

    @Test
    void testArrayEqualsOnNullCharArray() {
        assertTrue(arrayEquals((char[]) null, (char[]) null));
    }

    @Test
    void testArrayEqualsOnShortArray() {
        assertTrue(arrayEquals(ofShorts((short) 1), ofShorts((short) 1)));
        assertTrue(arrayEquals(ofShorts((short) 1), new short[]{1}));

        assertTrue(arrayEquals(ofShorts((short) 1, (short) 2), ofShorts((short) 1, (short) 2)));
        assertTrue(arrayEquals(ofShorts((short) 1, (short) 2), new short[]{1, 2}));
    }

    @Test
    void testArrayEqualsOnEmptyShortArray() {
        assertTrue(arrayEquals(EMPTY_SHORT_ARRAY, EMPTY_SHORT_ARRAY));
        assertTrue(arrayEquals(EMPTY_SHORT_ARRAY, new short[0]));
    }

    @Test
    void testArrayEqualsOnNullShortArray() {
        assertTrue(arrayEquals((short[]) null, (short[]) null));
    }

    @Test
    void testArrayEqualsOnIntArray() {
        assertTrue(arrayEquals(ofInts(1), ofInts(1)));
        assertTrue(arrayEquals(ofInts(1), new int[]{1}));

        assertTrue(arrayEquals(ofInts(1, 2), ofInts(1, 2)));
        assertTrue(arrayEquals(ofInts(1, 2), new int[]{1, 2}));
    }

    @Test
    void testArrayEqualsOnEmptyIntArray() {
        assertTrue(arrayEquals(EMPTY_INT_ARRAY, EMPTY_INT_ARRAY));
        assertTrue(arrayEquals(EMPTY_INT_ARRAY, new int[0]));
    }

    @Test
    void testArrayEqualsOnNullIntArray() {
        assertTrue(arrayEquals((int[]) null, (int[]) null));
    }

    @Test
    void testArrayEqualsOnLongArray() {
        assertTrue(arrayEquals(ofLongs(1L), ofLongs(1L)));
        assertTrue(arrayEquals(ofLongs(1L), new long[]{1L}));

        assertTrue(arrayEquals(ofLongs(1L, 2L), ofLongs(1L, 2L)));
        assertTrue(arrayEquals(ofLongs(1L, 2L), new long[]{1L, 2L}));
    }

    @Test
    void testArrayEqualsOnEmptyLongArray() {
        assertTrue(arrayEquals(EMPTY_LONG_ARRAY, EMPTY_LONG_ARRAY));
        assertTrue(arrayEquals(EMPTY_LONG_ARRAY, new long[0]));
    }

    @Test
    void testArrayEqualsOnNullLongArray() {
        assertTrue(arrayEquals((long[]) null, (long[]) null));
    }

    @Test
    void testArrayEqualsOnFloatArray() {
        assertTrue(arrayEquals(ofFloats(1F), ofFloats(1F)));
        assertTrue(arrayEquals(ofFloats(1F), new float[]{1F}));

        assertTrue(arrayEquals(ofFloats(1F, 2F), ofFloats(1F, 2F)));
        assertTrue(arrayEquals(ofFloats(1F, 2F), new float[]{1F, 2F}));
    }

    @Test
    void testArrayEqualsOnEmptyFloatArray() {
        assertTrue(arrayEquals(EMPTY_FLOAT_ARRAY, EMPTY_FLOAT_ARRAY));
        assertTrue(arrayEquals(EMPTY_FLOAT_ARRAY, new float[0]));
    }

    @Test
    void testArrayEqualsOnNullFloatArray() {
        assertTrue(arrayEquals((float[]) null, (float[]) null));
    }

    @Test
    void testArrayEqualsOnDoubleArray() {
        assertTrue(arrayEquals(ofDoubles(1D), ofDoubles(1D)));
        assertTrue(arrayEquals(ofDoubles(1D), new double[]{1D}));

        assertTrue(arrayEquals(ofDoubles(1D, 2D), ofDoubles(1D, 2D)));
        assertTrue(arrayEquals(ofDoubles(1D, 2D), new double[]{1D, 2D}));
    }

    @Test
    void testArrayEqualsOnEmptyDoubleArray() {
        assertTrue(arrayEquals(EMPTY_DOUBLE_ARRAY, EMPTY_DOUBLE_ARRAY));
        assertTrue(arrayEquals(EMPTY_DOUBLE_ARRAY, new double[0]));
    }

    @Test
    void testArrayEqualsOnNullDoubleArray() {
        assertTrue(arrayEquals((double[]) null, (double[]) null));
    }

    @Test
    void testArrayEqualsOnObjectArray() {
        assertTrue(arrayEquals(of("A", "B"), of("A", "B")));
        assertTrue(arrayEquals(of("A", "B"), new String[]{"A", "B"}));

        assertTrue(arrayEquals(of("A", "B"), combine("A", of("B"))));
        assertTrue(arrayEquals(of("A", "B", "C"), combine("A", of("B", "C"))));
    }

    @Test
    void testArrayEqualsOnEmptyObjectArray() {
        assertTrue(arrayEquals(EMPTY_OBJECT_ARRAY, EMPTY_OBJECT_ARRAY));
        assertTrue(arrayEquals(EMPTY_OBJECT_ARRAY, new Object[0]));
    }

    @Test
    void testArrayEqualsOnNullObjectArray() {
        assertTrue(arrayEquals(TEST_NULL_OBJECT_ARRAY, TEST_NULL_OBJECT_ARRAY));
    }

    // Test asArray methods

    @Test
    void testAsArrayOnEnumeration() {
        Enumeration<String> enums = ofEnumeration("A", "B");
        assertArrayEquals(ofArray("A", "B"), asArray(enums, String.class));
    }

    @Test
    void testAsArrayOnIterable() {
        Iterable<String> iterable = ofList("A", "B");
        assertArrayEquals(ofArray("A", "B"), asArray(iterable, String.class));
    }

    @Test
    void testAsArrayOnCollection() {
        Collection<String> collection = ofList("A", "B");
        assertArrayEquals(ofArray("A", "B"), asArray(collection, String.class));
    }

    // Test newArray
    @Test
    void testNewArray() {
        Integer[] values = newArray(Integer.class, 3);
        assertEquals(3, values.length);
    }

    // Test combine

    @Test
    void testCombine() {
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
    void testForEachWithConsumerOnBooleanArray() {
        boolean[] values = ofBooleans(true);
        forEach(values, (value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(value : {})", value);
            }
        });
    }

    @Test
    void testForEachWithConsumerOnByteArray() {
        byte[] values = ofBytes((byte) 1);
        forEach(values, (value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(value : {})", value);
            }
        });
    }

    @Test
    void testForEachWithConsumerOnCharArray() {
        char[] values = ofChars('A');
        forEach(values, (value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(value : {})", value);
            }
        });
    }

    @Test
    void testForEachWithConsumerOnShortArray() {
        short[] values = ofShorts((short) 1);
        forEach(values, (value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(value : {})", value);
            }
        });
    }

    @Test
    void testForEachWithConsumerOnIntArray() {
        int[] values = ofInts(1);
        forEach(values, (value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(value : {})", value);
            }
        });
    }

    @Test
    void testForEachWithConsumerOnLongArray() {
        long[] values = ofLongs(1L);
        forEach(values, (value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(value : {})", value);
            }
        });
    }

    @Test
    void testForEachWithConsumerOnFloatArray() {
        float[] values = ofFloats(1F);
        forEach(values, (value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(value : {})", value);
            }
        });
    }

    @Test
    void testForEachWithConsumerOnDoubleArray() {
        double[] values = ofDoubles(1D);
        forEach(values, (value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(value : {})", value);
            }
        });
    }

    @Test
    void testForEachWithConsumerOnObjectArray() {
        Object[] values = of("A");
        forEach(values, (value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(value : {})", value);
            }
        });
    }

    @Test
    void testForEachWithBiConsumerOnBooleanArray() {
        boolean[] values = ofBooleans(true);
        forEach(values, (index, value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(index : {} , value : {})", index, value);
            }
        });
    }

    @Test
    void testForEachWithBiConsumerOnByteArray() {
        byte[] values = ofBytes((byte) 1);
        forEach(values, (index, value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(index : {} , value : {})", index, value);
            }
        });
    }

    @Test
    void testForEachWithBiConsumerOnCharArray() {
        char[] values = ofChars('A');
        forEach(values, (index, value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(index : {} , value : {})", index, value);
            }
        });
    }

    @Test
    void testForEachWithBiConsumerOnShortArray() {
        short[] values = ofShorts((short) 1);
        forEach(values, (index, value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(index : {} , value : {})", index, value);
            }
        });
    }

    @Test
    void testForEachWithBiConsumerOnIntArray() {
        int[] values = ofInts(1);
        forEach(values, (index, value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(index : {} , value : {})", index, value);
            }
        });
    }

    @Test
    void testForEachWithBiConsumerOnLongArray() {
        long[] values = ofLongs(1L);
        forEach(values, (index, value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(index : {} , value : {})", index, value);
            }
        });
    }

    @Test
    void testForEachWithBiConsumerOnFloatArray() {
        float[] values = ofFloats(1F);
        forEach(values, (index, value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(index : {} , value : {})", index, value);
            }
        });
    }

    @Test
    void testForEachWithBiConsumerOnDoubleArray() {
        double[] values = ofDoubles(1D);
        forEach(values, (index, value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(index : {} , value : {})", index, value);
            }
        });
    }

    @Test
    void testForEachWithBiConsumerOnObjectArray() {
        Object[] values = of("A");
        forEach(values, (index, value) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("forEach(index : {} , value : {})", index, value);
            }
        });
    }

    @Test
    void testContainsOnBooleanArray() {
        boolean[] values = ofBooleans(true);
        assertTrue(contains(values, true));
        assertFalse(contains(values, false));
    }

    @Test
    void testContainsOnByteArray() {
        byte[] values = ofBytes((byte) 1);
        assertTrue(contains(values, (byte) 1));
        assertFalse(contains(values, (byte) 2));
    }

    @Test
    void testContainsOnCharArray() {
        char[] values = ofChars('A');
        assertTrue(contains(values, 'A'));
        assertFalse(contains(values, 'B'));
    }

    @Test
    void testContainsOnShortArray() {
        short[] values = ofShorts((short) 1);
        assertTrue(contains(values, (short) 1));
        assertFalse(contains(values, (short) 2));
    }

    @Test
    void testContainsOnIntArray() {
        int[] values = ofInts(1);
        assertTrue(contains(values, 1));
        assertFalse(contains(values, 2));
    }

    @Test
    void testContainsOnLongArray() {
        long[] values = ofLongs(1L);
        assertTrue(contains(values, 1L));
        assertFalse(contains(values, 2L));
    }

    @Test
    void testContainsOnFloatArray() {
        float[] values = ofFloats(1F);
        assertTrue(contains(values, 1F));
        assertFalse(contains(values, 2F));
    }

    @Test
    void testContainsOnDoubleArray() {
        double[] values = ofDoubles(1D);
        assertTrue(contains(values, 1D));
        assertFalse(contains(values, 2D));
    }

    @Test
    void testContainsOnComparableArray() {
        Object[] values = of("A");
        assertTrue(contains(values, "A"));
        assertFalse(contains(values, "B"));
    }

    @Test
    void testContainsOnObjectArray() {
        Object[] values = of(ofList("A"));
        assertTrue(contains(values, ofList("A")));
        assertFalse(contains(values, ofList("B")));
    }

    @Test
    void testReverse() {
        String[] array = ofArray("A", "B", "C");
        String[] reversed = reverse(array);
        assertSame(reversed, array);
        assertArrayEquals(reversed, reverse(array));
    }

    @Test
    void testToArrayReversed() {
        List<String> list = ofList("A", "B", "C");
        String[] strings = new String[2];
        String[] reversed = toArrayReversed(list, strings);
        assertArrayEquals(reversed, ofArray("C", "B", "A"));

        strings = new String[3];
        reversed = toArrayReversed(list, strings);
        assertArrayEquals(reversed, ofArray("C", "B", "A"));

        strings = new String[5];
        reversed = toArrayReversed(list, strings);
        assertArrayEquals(reversed, ofArray("C", "B", "A", null, null));
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
