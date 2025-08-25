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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static io.microsphere.collection.CollectionUtils.addAll;
import static io.microsphere.collection.CollectionUtils.emptyDeque;
import static io.microsphere.collection.CollectionUtils.emptyIterable;
import static io.microsphere.collection.CollectionUtils.emptyIterator;
import static io.microsphere.collection.CollectionUtils.emptyQueue;
import static io.microsphere.collection.CollectionUtils.first;
import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.collection.CollectionUtils.isNotEmpty;
import static io.microsphere.collection.CollectionUtils.singletonIterable;
import static io.microsphere.collection.CollectionUtils.size;
import static io.microsphere.collection.CollectionUtils.toIterable;
import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.QueueUtils.EMPTY_DEQUE;
import static io.microsphere.collection.SetUtils.newHashSet;
import static java.util.Collections.emptyEnumeration;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@lin CollectionUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class CollectionUtilsTest extends AbstractTestCase {

    @Test
    void testIsEmpty() {
        assertTrue(isEmpty(TEST_NULL_COLLECTION));
        assertTrue(isEmpty(TEST_NULL_LIST));
        assertTrue(isEmpty(TEST_NULL_SET));
        assertTrue(isEmpty(TEST_NULL_QUEUE));
        assertTrue(isEmpty(TEST_NULL_DEQUE));

        assertTrue(isEmpty(TEST_EMPTY_COLLECTION));
        assertTrue(isEmpty(TEST_EMPTY_LIST));
        assertTrue(isEmpty(TEST_EMPTY_SET));
        assertTrue(isEmpty(TEST_EMPTY_QUEUE));
        assertTrue(isEmpty(TEST_EMPTY_DEQUE));

        assertFalse(isEmpty(TEST_SINGLETON_LIST));
        assertFalse(isEmpty(TEST_SINGLETON_SET));
        assertFalse(isEmpty(TEST_SINGLETON_QUEUE));
        assertFalse(isEmpty(TEST_SINGLETON_DEQUE));
    }

    @Test
    void testIsNotEmpty() {

        assertFalse(isNotEmpty(TEST_NULL_COLLECTION));
        assertFalse(isNotEmpty(TEST_NULL_LIST));
        assertFalse(isNotEmpty(TEST_NULL_SET));
        assertFalse(isNotEmpty(TEST_NULL_QUEUE));
        assertFalse(isNotEmpty(TEST_NULL_DEQUE));

        assertFalse(isNotEmpty(TEST_EMPTY_COLLECTION));
        assertFalse(isNotEmpty(TEST_EMPTY_LIST));
        assertFalse(isNotEmpty(TEST_EMPTY_SET));
        assertFalse(isNotEmpty(TEST_EMPTY_QUEUE));
        assertFalse(isNotEmpty(TEST_EMPTY_DEQUE));

        assertTrue(isNotEmpty(TEST_SINGLETON_LIST));
        assertTrue(isNotEmpty(TEST_SINGLETON_SET));
        assertTrue(isNotEmpty(TEST_SINGLETON_QUEUE));
        assertTrue(isNotEmpty(TEST_SINGLETON_DEQUE));
    }

    @Test
    void testToIterable() {
        Iterable iterable = toIterable(emptyList());
        assertEmptyIterable(iterable);

        iterable = toIterable(emptyIterator());
        assertEmptyIterable(iterable);

        iterable = toIterable(emptyEnumeration());
        assertEmptyIterable(iterable);

        iterable = toIterable(emptyList());
        assertEmptyIterable(iterable);

        iterable = toIterable(TEST_NULL_ITERATOR);
        assertEmptyIterable(iterable);

        iterable = toIterable(TEST_NULL_COLLECTION);
        assertEmptyIterable(iterable);

        iterable = toIterable(TEST_NULL_ENUMERATION);
        assertEmptyIterable(iterable);

    }

    private void assertEmptyIterable(Iterable iterable) {
        assertEmptyIterator(iterable.iterator());
    }

    private void assertEmptyIterator(Iterator iterator) {
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
        assertThrows(IllegalStateException.class, iterator::remove);
    }

    @Test
    void testEmptyIterator() {
        assertSame(Collections.emptyIterator(), emptyIterator());
    }

    @Test
    void testEmptyIterable() {
        assertSame(EmptyIterable.INSTANCE, emptyIterable());
    }

    @Test
    void testSize() {
        assertEquals(0, size(TEST_NULL_ITERABLE));
        assertEquals(0, size(TEST_NULL_COLLECTION));
        assertEquals(0, size(TEST_NULL_LIST));
        assertEquals(0, size(TEST_NULL_SET));
        assertEquals(0, size(TEST_NULL_QUEUE));
        assertEquals(0, size(TEST_NULL_DEQUE));

        assertEquals(0, size(TEST_EMPTY_COLLECTION));
        assertEquals(0, size(TEST_EMPTY_LIST));
        assertEquals(0, size(TEST_EMPTY_SET));
        assertEquals(0, size(TEST_EMPTY_QUEUE));
        assertEquals(0, size(TEST_EMPTY_DEQUE));

        assertEquals(1, size(TEST_SINGLETON_LIST));
        assertEquals(1, size(TEST_SINGLETON_SET));
        assertEquals(1, size(TEST_SINGLETON_QUEUE));
        assertEquals(1, size(TEST_SINGLETON_DEQUE));

        assertEquals(1, size((Iterable) TEST_SINGLETON_LIST));
        assertEquals(1, size(singletonIterable(TEST_ELEMENT)));
    }

    @Test
    void testEquals() {
        assertFalse(CollectionUtils.equals(null, TEST_SINGLETON_LIST));
        assertFalse(CollectionUtils.equals(TEST_SINGLETON_LIST, null));

        assertTrue(CollectionUtils.equals(null, null));
        assertTrue(CollectionUtils.equals(TEST_EMPTY_LIST, TEST_EMPTY_SET));
        assertTrue(CollectionUtils.equals(TEST_EMPTY_LIST, TEST_EMPTY_QUEUE));
        assertTrue(CollectionUtils.equals(TEST_EMPTY_LIST, TEST_EMPTY_DEQUE));
        assertTrue(CollectionUtils.equals(TEST_SINGLETON_LIST, TEST_SINGLETON_LIST));
        assertTrue(CollectionUtils.equals(TEST_SINGLETON_LIST, TEST_SINGLETON_SET));
        assertTrue(CollectionUtils.equals(TEST_SINGLETON_LIST, TEST_SINGLETON_QUEUE));
        assertTrue(CollectionUtils.equals(TEST_SINGLETON_LIST, TEST_SINGLETON_DEQUE));

        assertFalse(CollectionUtils.equals(TEST_SINGLETON_LIST, singletonList(1)));
        List list = newLinkedList();
        list.add(null);
        assertFalse(CollectionUtils.equals(TEST_SINGLETON_LIST, list));
    }

    @Test
    void testAddAllWithList() {
        List<String> values = newLinkedList();
        assertEquals(2, addAll(values, "A", "B"));
        assertEquals(ofList("A", "B"), values);
    }

    @Test
    void testAddAllWithSet() {
        Set<String> set = newHashSet(TEST_ELEMENT);
        assertEquals(0, addAll(set, TEST_ELEMENT));
    }

    @Test
    void testAddAllWithoutValues() {
        assertEquals(0, addAll(TEST_EMPTY_LIST));
        assertEquals(0, addAll(newLinkedList()));
    }

    @Test
    void testAddAllWithNullCollection() {
        assertEquals(0, addAll(TEST_NULL_COLLECTION, "A"));
    }

    @Test
    public void testAddAllWithListAndIterable() {
        List<String> values = newLinkedList();
        assertEquals(2, addAll(values, ofList("A", "B")));
    }

    @Test
    public void testAddAllWithSetAndIterable() {
        Set<String> set = newHashSet(TEST_ELEMENT);
        assertEquals(0, addAll(set, set));
    }

    @Test
    public void testAddAllWithNullList() {
        assertEquals(0, addAll(TEST_NULL_COLLECTION, ofList("A")));
    }

    @Test
    public void testAddAllWithNullIterable() {
        assertEquals(0, addAll(newLinkedList(), (List) null));
    }

    @Test
    void testFirst() {
        assertNull(first(TEST_NULL_ITERATOR));
        assertNull(first(TEST_EMPTY_QUEUE.iterator()));
        assertNull(first(TEST_NULL_ITERABLE));
        assertNull(first(TEST_NULL_COLLECTION));
        assertNull(first(TEST_EMPTY_LIST));
        assertNull(first(TEST_EMPTY_SET));
        assertNull(first(TEST_EMPTY_QUEUE));
        assertNull(first(TEST_EMPTY_DEQUE));
        assertEquals(TEST_ELEMENT, first(TEST_SINGLETON_LIST));
        assertEquals(TEST_ELEMENT, first(TEST_SINGLETON_SET));
        assertEquals(TEST_ELEMENT, first(toIterable(TEST_SINGLETON_LIST)));
    }

    @Test
    void testEmptyQueue() {
        assertSame(QueueUtils.emptyQueue(), emptyQueue());
        assertSame(EMPTY_DEQUE, emptyQueue());
    }

    @Test
    void testEmptyDeque() {
        assertSame(QueueUtils.emptyDeque(), emptyDeque());
        assertSame(EMPTY_DEQUE, emptyDeque());
    }

}
