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
package io.microsphere.collection;

import io.microsphere.AbstractTestCase;
import io.microsphere.logging.Logger;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.microsphere.collection.ListUtils.first;
import static io.microsphere.collection.ListUtils.forEach;
import static io.microsphere.collection.ListUtils.isList;
import static io.microsphere.collection.ListUtils.last;
import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.ListUtils.of;
import static io.microsphere.collection.ListUtils.ofArrayList;
import static io.microsphere.collection.ListUtils.ofLinkedList;
import static io.microsphere.collection.ListUtils.ofList;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyEnumeration;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptyList;
import static java.util.Collections.enumeration;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ListUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ListUtils
 * @since 1.0.0
 */
class ListUtilsTest extends AbstractTestCase {

    private static final Logger logger = getLogger(ListUtilsTest.class);

    private static final List<String> TEST_LIST = asList("A", "B", "C");

    @Test
    void testIsList() {
        assertTrue(isList(new ArrayList()));
        assertTrue(isList(emptyList()));
        assertFalse(isList(emptyEnumeration()));
        assertFalse(isList("A"));
        assertFalse(isList(null));
    }

    @Test
    void testFirst() {
        assertEquals("A", first(TEST_LIST));
        assertNull(first(emptyList()));
        assertNull(first(of()));
    }

    @Test
    void testLast() {
        assertEquals("C", last(TEST_LIST));
        assertNull(last(emptyList()));
        assertNull(last(of()));
    }

    @Test
    void testOf() {
        List<String> list = of();
        assertTrue(list.isEmpty());

        List<String> rawList = TEST_LIST;
        list = of("A", "B", "C");
        assertEquals(rawList, list);
    }

    @Test
    void testOfList() {
        List<String> rawList = TEST_LIST;
        List<String> list = ofList(rawList);
        assertEquals(rawList, list);

        list = ofList("A", "B", "C");
        assertEquals(rawList, list);

        Set<String> rawSet = singleton("A");
        list = ofList(rawSet);
        assertEquals(newArrayList(rawSet), list);

        list = ofList(enumeration(rawList));
        assertEquals(rawList, list);

        list = ofList(TEST_NULL_LIST);
        assertSame(emptyList(), list);
        assertEquals(emptyList(), list);

        list = ofList(TEST_NULL_ITERABLE);
        assertSame(emptyList(), list);
        assertEquals(emptyList(), list);

        list = ofList(TEST_NULL_ITERATOR);
        assertSame(emptyList(), list);
        assertEquals(emptyList(), list);

        list = ofList(TEST_NULL_ENUMERATION);
        assertEquals(emptyList(), list);
    }

    @Test
    void testNewArrayList() {
        assertEquals(newArrayList(), newArrayList(1));
        assertEquals(newArrayList(), newArrayList(emptyEnumeration()));
        assertEquals(newArrayList(newArrayList()), newArrayList(emptyIterator()));
    }

    @Test
    void testNewLinkedList() {
        assertEquals(emptyList(), newLinkedList());
        assertEquals(newLinkedList(), newLinkedList(emptyEnumeration()));
        assertEquals(newLinkedList(newArrayList()), newLinkedList(emptyIterator()));
    }

    @Test
    void testOfArrayList() {
        List<String> list = ofArrayList("A", "B", "C");
        assertEquals(list, ofList("A", "B", "C"));
        assertTrue(list.add("D"));
        assertTrue(list.addAll(ofList("A", "B", "C", "D")));
        assertTrue(list.removeAll(ofList("A", "B", "C")));
        assertTrue(list.containsAll(ofList("D")));
    }

    @Test
    void testOfArrayListOnEmptyArray() {
        assertThrows(IllegalArgumentException.class, () -> ofArrayList(null));
        assertThrows(IllegalArgumentException.class, () -> ofArrayList());
    }

    @Test
    void testOfLinkedList() {
        List<String> list = ofLinkedList("A", "B", "C");
        assertEquals(list, ofList("A", "B", "C"));
        assertTrue(list.add("D"));
        assertTrue(list.addAll(ofList("A", "B", "C", "D")));
        assertTrue(list.removeAll(ofList("A", "B", "C")));
        assertTrue(list.containsAll(ofList("D")));
    }

    @Test
    void testOfLinkedListOnEmptyArray() {
        assertThrows(IllegalArgumentException.class, () -> ofLinkedList(null));
        assertThrows(IllegalArgumentException.class, () -> ofLinkedList());
    }

    @Test
    void testForEach() {
        List<String> list = TEST_LIST;
        forEach(list, (index, value) -> {
            logger.trace("forEach(index = {} , value = '{}')", index, value);
        });
        forEach(list, (value) -> {
            logger.trace("forEach(value = '{}')", value);
        });
    }
}
