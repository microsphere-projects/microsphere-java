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
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import static io.microsphere.collection.MapUtils.ofMap;
import static io.microsphere.util.Assert.assertArrayIndex;
import static io.microsphere.util.Assert.assertArrayType;
import static io.microsphere.util.Assert.assertFieldMatchType;
import static io.microsphere.util.Assert.assertNoNullElements;
import static io.microsphere.util.Assert.assertNotBlank;
import static io.microsphere.util.Assert.assertNotEmpty;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.Assert.assertNull;
import static io.microsphere.util.Assert.assertTrue;
import static java.lang.reflect.Array.newInstance;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * {@link Assert} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Assert
 * @since 1.0.0
 */
public class AssertTest extends AbstractTestCase {

    @Test
    public void testAssertTrue() {
        assertTrue(true, "True");
        assertTrue(true, () -> "True");
        assertTrue(true, (Supplier<String>) null);
        assertThrows(IllegalArgumentException.class, () -> assertTrue(false, "False"));
        assertThrows(IllegalArgumentException.class, () -> assertTrue(false, () -> "False"));
        assertThrows(IllegalArgumentException.class, () -> assertTrue(false, (Supplier<String>) null));
    }

    @Test
    public void testAssertNull() {
        assertNull(null, "null");
        assertNull(null, () -> "null");
        assertNull(null, (Supplier<String>) null);
        assertThrows(IllegalArgumentException.class, () -> assertNull(false, "False"));
        assertThrows(IllegalArgumentException.class, () -> assertNull(false, () -> "False"));
        assertThrows(IllegalArgumentException.class, () -> assertNull(false, (Supplier<String>) null));
    }

    @Test
    public void testAssertNotNull() {
        assertNotNull(false, "false");
        assertNotNull(false, () -> "false");
        assertNotNull(false, (Supplier<String>) null);
        assertThrows(IllegalArgumentException.class, () -> assertNotNull(null, "null"));
        assertThrows(IllegalArgumentException.class, () -> assertNotNull(null, () -> "null"));
        assertThrows(IllegalArgumentException.class, () -> assertNotNull(null, (Supplier<String>) null));
    }

    @Test
    public void testAssertNotEmpty() {
        assertNotEmpty("abc", "abc");
        assertNotEmpty("abc", () -> "abc");
        assertNotEmpty("abc", TEST_NULL_STRING);
        assertNotEmpty("abc", (Supplier<String>) null);

        assertThrows(IllegalArgumentException.class, () -> assertNotEmpty(TEST_NULL_STRING, "null"));
        assertThrows(IllegalArgumentException.class, () -> assertNotEmpty("", () -> "empty"));
    }

    @Test
    public void testAssertNotBlank() {
        assertNotBlank("abc", "abc");
        assertNotBlank("abc", () -> "abc");
        assertNotBlank("abc", TEST_NULL_STRING);
        assertNotBlank("abc", (Supplier<String>) null);

        assertThrows(IllegalArgumentException.class, () -> assertNotBlank(TEST_NULL_STRING, "null"));
        assertThrows(IllegalArgumentException.class, () -> assertNotBlank("", () -> "empty"));
        assertThrows(IllegalArgumentException.class, () -> assertNotBlank(" ", () -> "blank"));
    }

    @Test
    public void testAssertArrayNotEmpty() {
        Object[] array = {"a", "b", "c"};
        assertNotEmpty(array, "array");
        assertNotEmpty(array, () -> "array");
        assertNotEmpty(array, (Supplier<String>) null);

        assertThrows(IllegalArgumentException.class, () -> assertNotEmpty(TEST_NULL_OBJECT_ARRAY, "null"));
        assertThrows(IllegalArgumentException.class, () -> assertNotEmpty(TEST_NULL_OBJECT_ARRAY, () -> "null"));
        assertThrows(IllegalArgumentException.class, () -> assertNotEmpty(TEST_NULL_OBJECT_ARRAY, (Supplier<String>) null));

        Object[] emptyArray = {};

        assertThrows(IllegalArgumentException.class, () -> assertNotEmpty(emptyArray, "null"));
        assertThrows(IllegalArgumentException.class, () -> assertNotEmpty(emptyArray, () -> "null"));
        assertThrows(IllegalArgumentException.class, () -> assertNotEmpty(emptyArray, (Supplier<String>) null));
    }

    @Test
    public void testAssertCollectionNotEmpty() {
        Collection<String> collection = asList("a", "b", "c");
        assertNotEmpty(collection, "collection");
        assertNotEmpty(collection, () -> "collection");
        assertNotEmpty(collection, (Supplier<String>) null);

        assertThrows(IllegalArgumentException.class, () -> assertNotEmpty((Collection<String>) null, "null"));
        assertThrows(IllegalArgumentException.class, () -> assertNotEmpty((Collection<String>) null, () -> "null"));
        assertThrows(IllegalArgumentException.class, () -> assertNotEmpty((Collection<String>) null, (Supplier<String>) null));

        Collection<String> emptyArray = emptyList();

        assertThrows(IllegalArgumentException.class, () -> assertNotEmpty(emptyArray, "emptyArray"));
        assertThrows(IllegalArgumentException.class, () -> assertNotEmpty(emptyArray, () -> "emptyArray"));
        assertThrows(IllegalArgumentException.class, () -> assertNotEmpty(emptyArray, (Supplier<String>) null));
    }

    @Test
    public void testAssertMapNotEmpty() {
        Map<String, String> map = ofMap("A", "1");
        assertNotEmpty(map, "map");
        assertNotEmpty(map, () -> "map");
        assertNotEmpty(map, (Supplier<String>) null);

        assertThrows(IllegalArgumentException.class, () -> assertNotEmpty((Map<String, String>) null, "null"));
        assertThrows(IllegalArgumentException.class, () -> assertNotEmpty((Map<String, String>) null, () -> "null"));
        assertThrows(IllegalArgumentException.class, () -> assertNotEmpty((Map<String, String>) null, (Supplier<String>) null));

        Map<String, String> emptyMap = emptyMap();

        assertThrows(IllegalArgumentException.class, () -> assertNotEmpty(emptyMap, "emptyMap"));
        assertThrows(IllegalArgumentException.class, () -> assertNotEmpty(emptyMap, () -> "emptyMap"));
        assertThrows(IllegalArgumentException.class, () -> assertNotEmpty(emptyMap, (Supplier<String>) null));
    }

    @Test
    public void testAssertArrayNoNullElements() {
        Object[] array = {"a", "b", "c"};
        assertNoNullElements(array, "array");
        assertNoNullElements(array, () -> "array");
        assertNoNullElements(array, (Supplier<String>) null);


        assertNoNullElements(TEST_NULL_OBJECT_ARRAY, "null");
        assertNoNullElements(TEST_NULL_OBJECT_ARRAY, () -> "null");
        assertNoNullElements(TEST_NULL_OBJECT_ARRAY, (Supplier<String>) null);

        Object[] emptyArray = {};

        assertNoNullElements(emptyArray, "null");
        assertNoNullElements(emptyArray, () -> "null");
        assertNoNullElements(emptyArray, (Supplier<String>) null);

        Object[] arrayWithNull = {"a", null, "c"};

        assertThrows(IllegalArgumentException.class, () -> assertNoNullElements(arrayWithNull, "arrayWithNull"));
        assertThrows(IllegalArgumentException.class, () -> assertNoNullElements(arrayWithNull, () -> "arrayWithNull"));
        assertThrows(IllegalArgumentException.class, () -> assertNoNullElements(arrayWithNull, (Supplier<String>) null));
    }

    @Test
    public void testAssertIterableNoNullElements() {
        Collection<String> collection = asList("a", "b", "c");
        assertNoNullElements(collection, "collection");
        assertNoNullElements(collection, () -> "collection");
        assertNoNullElements(collection, (Supplier<String>) null);


        assertNoNullElements((Collection<String>) null, "null");
        assertNoNullElements((Collection<String>) null, () -> "null");
        assertNoNullElements((Collection<String>) null, (Supplier<String>) null);

        Collection<String> emptyCollection = emptyList();

        assertNoNullElements(emptyCollection, "null");
        assertNoNullElements(emptyCollection, () -> "null");
        assertNoNullElements(emptyCollection, (Supplier<String>) null);

        Collection<String> collectionWithNull = asList("a", null, "c");

        assertThrows(IllegalArgumentException.class, () -> assertNoNullElements(collectionWithNull, "collectionWithNull"));
        assertThrows(IllegalArgumentException.class, () -> assertNoNullElements(collectionWithNull, () -> "collectionWithNull"));
        assertThrows(IllegalArgumentException.class, () -> assertNoNullElements(collectionWithNull, (Supplier<String>) null));
    }


    @Test
    public void testAssertArrayIndex() {
        int size = 10;
        Object array = newInstance(int.class, size);
        for (int i = 0; i < size; i++) {
            assertArrayIndex(array, i);
        }

        for (int i = size; i < size * 2; i++) {
            final int index = i;
            assertThrows(ArrayIndexOutOfBoundsException.class, () -> assertArrayIndex(array, index));
        }
    }

    @Test
    public void testAssertArrayIndexOnArrayIndexOutOfBoundsException() {
        int size = 10;
        Object array = newInstance(int.class, size);

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> assertArrayIndex(array, -1));

        for (int i = size; i < size * 2; i++) {
            final int index = i;
            assertThrows(ArrayIndexOutOfBoundsException.class, () -> assertArrayIndex(array, index));
        }
    }


    @Test
    public void testAssertArrayType() {
        testAssertArrayType(long.class);
        testAssertArrayType(int.class);
        testAssertArrayType(short.class);
        testAssertArrayType(byte.class);
        testAssertArrayType(boolean.class);
        testAssertArrayType(double.class);
        testAssertArrayType(float.class);
        testAssertArrayType(char.class);
        testAssertArrayType(String.class);
        testAssertArrayType(Object.class);
    }

    @Test
    public void testAssertArrayTypeOnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> assertArrayType(new Object()));
    }

    @Test
    public void testAssertArrayTypeOnNullPointerException() {
        assertThrows(NullPointerException.class, () -> assertArrayType(null));
    }

    @Test
    public void testAssertFieldMatchType() {
        assertFieldMatchType("test", "hash", int.class);
    }

    @Test
    public void testAssertFieldMatchTypeOnFieldNotFound() {
        assertThrows(NullPointerException.class, () -> assertFieldMatchType("test", "hashCode", int.class));
    }

    @Test
    public void testAssertFieldMatchTypeOnFieldTypeNotMatch() {
        assertThrows(IllegalArgumentException.class, () -> assertFieldMatchType("test", "hash", Integer.class));
    }

    private void testAssertArrayType(Class<?> type) {
        Object array = newInstance(type, 0);
        assertArrayType(array);
    }
}
