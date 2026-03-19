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
package io.microsphere.convert.multiple;

import io.microsphere.convert.StringConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.Integer.MAX_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link StringToIterableConverter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see StringToIterableConverter
 * @since 1.0.0
 */
class StringToIterableConverterTest {

    private StringToCollectionConverter converter;

    @BeforeEach
    void setUp() {
        converter = new StringToCollectionConverter();
    }

    @Test
    void testAccept() {
        assertTrue(converter.accept(String.class, Collection.class));
        assertTrue(converter.accept(String.class, List.class));
        assertTrue(converter.accept(String.class, Set.class));
        assertFalse(converter.accept(String.class, String.class));
        assertFalse(converter.accept(null, null));
    }

    @Test
    void testConvert() {
        Collection<Integer> result = (Collection<Integer>) converter.convert("1,2,3", Collection.class, Integer.class);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(1));
        assertTrue(result.contains(2));
        assertTrue(result.contains(3));

        Collection<String> strResult = (Collection<String>) converter.convert("a,b", Collection.class, String.class);
        assertNotNull(strResult);
        assertEquals(2, strResult.size());
        assertTrue(strResult.contains("a"));
        assertTrue(strResult.contains("b"));
    }

    @Test
    void testConvertOnNoStringConverter() {
        // No StringConverter registered for Object.class → returns null
        assertNull(converter.convert(new String[]{"a"}, 1, Collection.class, Object.class));
    }

    @Test
    void testConvertOnNonCollectionIterable() {
        // When createMultiValue returns a non-Collection Iterable, the elements are not added;
        // the iterable is returned as-is once a StringConverter is found for the element type.
        StringToIterableConverter<Iterable> iterableConverter = new StringToIterableConverter<Iterable>() {
            @Override
            protected Iterable createMultiValue(int size, Class<?> multiValueType) {
                return Collections::emptyIterator;
            }
        };

        Object result = iterableConverter.convert(new String[]{"1", "2"}, 2, Iterable.class, Integer.class);
        assertNotNull(result);
    }

    @Test
    void testGetStringConverter() {
        // Integer has a registered StringConverter
        Optional<StringConverter> intConverter = converter.getStringConverter(Integer.class);
        assertTrue(intConverter.isPresent());

        // Object.class has no registered StringConverter
        Optional<StringConverter> objectConverter = converter.getStringConverter(Object.class);
        assertFalse(objectConverter.isPresent());
    }

    @Test
    void testGetSupportedType() {
        assertEquals(Collection.class, converter.getSupportedType());
    }

    @Test
    void testGetPriority() {
        // Collection has 1 interface level above Iterable, so level=1 → priority = MAX_VALUE - 1
        assertEquals(MAX_VALUE - 1, converter.getPriority());
    }

    @Test
    void testGetSourceType() {
        assertEquals(String.class, converter.getSourceType());
    }
}
