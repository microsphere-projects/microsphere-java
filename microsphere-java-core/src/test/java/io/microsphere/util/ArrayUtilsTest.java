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

import static io.microsphere.util.ArrayUtils.EMPTY_BOOLEAN_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_BYTE_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_CHARACTER_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_FLOAT_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_INTEGER_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_LONG_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_SHORT_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.combine;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.ArrayUtils.of;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.ArrayUtils.size;
import static io.microsphere.util.ClassUtils.getTopComponentType;
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
        assertEmptyArray(EMPTY_OBJECT_ARRAY, Object.class);
        assertEmptyArray(EMPTY_CLASS_ARRAY, Class.class);
        assertEmptyArray(EMPTY_STRING_ARRAY, String.class);
        assertEmptyArray(EMPTY_LONG_OBJECT_ARRAY, Long.class);
        assertEmptyArray(EMPTY_INTEGER_OBJECT_ARRAY, Integer.class);
        assertEmptyArray(EMPTY_SHORT_OBJECT_ARRAY, Short.class);
        assertEmptyArray(EMPTY_BYTE_OBJECT_ARRAY, Byte.class);
        assertEmptyArray(EMPTY_DOUBLE_OBJECT_ARRAY, Double.class);
        assertEmptyArray(EMPTY_FLOAT_OBJECT_ARRAY, Float.class);
        assertEmptyArray(EMPTY_BOOLEAN_OBJECT_ARRAY, Boolean.class);
        assertEmptyArray(EMPTY_CHARACTER_OBJECT_ARRAY, Character.class);
    }

    @Test
    public void tesOf() {
        assertArrayEquals(of("A"), array("A"));
        assertArrayEquals(of("A", "B"), array("A", "B"));
        assertArrayEquals(of("A", "B", "C"), array("A", "B", "C"));
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
        assertEquals(0, length(ofArray(null)));
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
        assertEquals(0, size(ofArray(null)));
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

    private <E> void assertEmptyArray(E[] array, Class<E> expectedComponentType) {
        assertEquals(0, length(array));
        assertEquals(expectedComponentType, getTopComponentType(array));
    }
}
