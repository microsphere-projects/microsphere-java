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
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import static io.microsphere.collection.CollectionUtils.addAll;
import static io.microsphere.collection.CollectionUtils.first;
import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.collection.CollectionUtils.isNotEmpty;
import static io.microsphere.collection.CollectionUtils.singletonIterable;
import static io.microsphere.collection.CollectionUtils.size;
import static io.microsphere.collection.CollectionUtils.toIterable;
import static java.util.Collections.emptyEnumeration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@lin CollectionUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class CollectionUtilsTest extends AbstractTestCase {

    @Test
    public void testIsEmpty() {
        assertTrue(isEmpty(NULL_COLLECTION));
        assertTrue(isEmpty(NULL_LIST));
        assertTrue(isEmpty(NULL_SET));
        assertTrue(isEmpty(NULL_QUEUE));
        assertTrue(isEmpty(NULL_DEQUE));

        assertTrue(isEmpty(EMPTY_COLLECTION));
        assertTrue(isEmpty(EMPTY_LIST));
        assertTrue(isEmpty(EMPTY_SET));
        assertTrue(isEmpty(EMPTY_QUEUE));
        assertTrue(isEmpty(EMPTY_DEQUE));

        assertFalse(isEmpty(SINGLETON_LIST));
        assertFalse(isEmpty(SINGLETON_SET));
        assertFalse(isEmpty(SINGLETON_QUEUE));
        assertFalse(isEmpty(SINGLETON_DEQUE));
    }

    @Test
    public void testIsNotEmpty() {

        assertFalse(isNotEmpty(NULL_COLLECTION));
        assertFalse(isNotEmpty(NULL_LIST));
        assertFalse(isNotEmpty(NULL_SET));
        assertFalse(isNotEmpty(NULL_QUEUE));
        assertFalse(isNotEmpty(NULL_DEQUE));

        assertFalse(isNotEmpty(EMPTY_COLLECTION));
        assertFalse(isNotEmpty(EMPTY_LIST));
        assertFalse(isNotEmpty(EMPTY_SET));
        assertFalse(isNotEmpty(EMPTY_QUEUE));
        assertFalse(isNotEmpty(EMPTY_DEQUE));

        assertTrue(isNotEmpty(SINGLETON_LIST));
        assertTrue(isNotEmpty(SINGLETON_SET));
        assertTrue(isNotEmpty(SINGLETON_QUEUE));
        assertTrue(isNotEmpty(SINGLETON_DEQUE));
    }

    @Test
    public void testToIterable() {
        Iterable iterable = toIterable(emptyEnumeration());
        assertEmptyIterable(iterable);

        iterable = toIterable((Enumeration) null);
        assertEmptyIterable(iterable);
    }

    private void assertEmptyIterable(Iterable iterable) {
        assertEmptyIterator(iterable.iterator());
    }

    private void assertEmptyIterator(Iterator iterator) {
        assertFalse(iterator.hasNext());
        assertThrowable(iterator::next, NoSuchElementException.class);
        assertThrowable(iterator::remove, UnsupportedOperationException.class);
    }

    @Test
    public void testSize() {
        assertEquals(0, size(NULL_COLLECTION));
        assertEquals(0, size(NULL_LIST));
        assertEquals(0, size(NULL_SET));
        assertEquals(0, size(NULL_QUEUE));
        assertEquals(0, size(NULL_DEQUE));

        assertEquals(0, size(EMPTY_COLLECTION));
        assertEquals(0, size(EMPTY_LIST));
        assertEquals(0, size(EMPTY_SET));
        assertEquals(0, size(EMPTY_QUEUE));
        assertEquals(0, size(EMPTY_DEQUE));

        assertEquals(1, size(SINGLETON_LIST));
        assertEquals(1, size(SINGLETON_SET));
        assertEquals(1, size(SINGLETON_QUEUE));
        assertEquals(1, size(SINGLETON_DEQUE));

        assertEquals(1, size((Iterable) SINGLETON_LIST));
        assertEquals(1, size(singletonIterable(TEST_ELEMENT)));
    }

    @Test
    public void testEquals() {
        assertFalse(CollectionUtils.equals(null, SINGLETON_LIST));
        assertFalse(CollectionUtils.equals(SINGLETON_LIST, null));

        assertTrue(CollectionUtils.equals(null, null));
        assertTrue(CollectionUtils.equals(EMPTY_LIST, EMPTY_SET));
        assertTrue(CollectionUtils.equals(EMPTY_LIST, EMPTY_QUEUE));
        assertTrue(CollectionUtils.equals(EMPTY_LIST, EMPTY_DEQUE));
        assertTrue(CollectionUtils.equals(SINGLETON_LIST, SINGLETON_LIST));
        assertTrue(CollectionUtils.equals(SINGLETON_LIST, SINGLETON_SET));
        assertTrue(CollectionUtils.equals(SINGLETON_LIST, SINGLETON_QUEUE));
        assertTrue(CollectionUtils.equals(SINGLETON_LIST, SINGLETON_DEQUE));
    }

    @Test
    public void testAddAll() {
        List<String> values = new LinkedList<>();
        assertEquals(0, addAll(EMPTY_LIST));
        assertEquals(0, addAll(null, "A"));
        assertEquals(0, addAll(values));
        assertEquals(2, addAll(values, "A", "B"));
        assertEquals(Arrays.asList("A", "B"), values);
    }

    @Test
    public void testFirst() {
        assertNull(first((Collection) null));
        assertNull(first((Iterator) null));
        assertNull(first((Iterable) null));
        assertNull(first(EMPTY_LIST));
        assertNull(first(EMPTY_SET));
        assertNull(first(EMPTY_QUEUE));
        assertNull(first(EMPTY_DEQUE));
        assertEquals(TEST_ELEMENT, first(SINGLETON_LIST));
        assertEquals(TEST_ELEMENT, first(SINGLETON_SET));
        assertEquals(TEST_ELEMENT, first(toIterable(SINGLETON_LIST)));
    }

}
