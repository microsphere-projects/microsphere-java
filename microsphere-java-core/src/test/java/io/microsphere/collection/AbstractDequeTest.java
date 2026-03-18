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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AbstractDeque} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractDeque
 * @since 1.0.0
 */
class AbstractDequeTest {

    private static final String TEST_VALUE = "1";

    private AbstractDeque<String> deque;

    @BeforeEach
    void setUp() {
        deque = new TestDeque<>(1);
    }

    @Test
    void testAddFirst() {
        deque.addFirst(TEST_VALUE);
        assertThrows(IllegalStateException.class, () -> deque.addFirst(TEST_VALUE));
    }

    @Test
    void testOfferFirst() {
        assertTrue(deque.offerFirst(TEST_VALUE));
        assertFalse(deque.offerFirst(TEST_VALUE));
    }

    @Test
    void testAddLast() {
        deque.addLast(TEST_VALUE);
        assertThrows(IllegalStateException.class, () -> deque.addLast(TEST_VALUE));
    }

    @Test
    void testOfferLast() {
        assertTrue(deque.offerLast(TEST_VALUE));
        assertFalse(deque.offerLast(TEST_VALUE));
    }

    @Test
    void testRemoveFirst() {
        deque.add(TEST_VALUE);
        assertSame(TEST_VALUE, deque.removeFirst());
        assertThrows(NoSuchElementException.class, () -> deque.removeFirst());
    }

    @Test
    void testRemoveLast() {
        deque.add(TEST_VALUE);
        assertSame(TEST_VALUE, deque.removeLast());
        assertThrows(NoSuchElementException.class, () -> deque.removeLast());
    }

    @Test
    void testPeekFirst() {
        assertNull(deque.peekFirst());
        deque.add(TEST_VALUE);
        assertSame(TEST_VALUE, deque.peekFirst());
        assertSame(TEST_VALUE, deque.peekFirst());
        assertSame(TEST_VALUE, deque.peekFirst());
    }

    @Test
    void testPeekLast() {
        assertNull(deque.peekLast());
        deque.add(TEST_VALUE);
        assertSame(TEST_VALUE, deque.peekLast());
        assertSame(TEST_VALUE, deque.peekLast());
        assertSame(TEST_VALUE, deque.peekLast());
    }

    @Test
    void testGetFirst() {
        deque.add(TEST_VALUE);
        assertSame(TEST_VALUE, deque.getFirst());
        deque.clear();
        assertThrows(NoSuchElementException.class, () -> deque.getFirst());
    }

    @Test
    void testGetLast() {
        deque.add(TEST_VALUE);
        assertSame(TEST_VALUE, deque.getLast());
        deque.clear();
        assertThrows(NoSuchElementException.class, () -> deque.getLast());
    }

    @Test
    void testRemoveFirstOccurrence() {
        assertFalse(deque.removeFirstOccurrence(null));
        deque.add(TEST_VALUE);
        assertFalse(deque.removeFirstOccurrence(""));
        assertTrue(deque.removeFirstOccurrence(TEST_VALUE));
        assertFalse(deque.removeFirstOccurrence(TEST_VALUE));
    }

    @Test
    void testRemoveLastOccurrence() {
        TestDeque<String> multiDeque = new TestDeque<>();
        multiDeque.add("A");
        multiDeque.add("B");
        multiDeque.add("A");
        assertTrue(multiDeque.removeLastOccurrence("A"));
        assertEquals(2, multiDeque.size());
        assertFalse(multiDeque.removeLastOccurrence("C"));
    }

    @Test
    void testPush() {
        deque.push(TEST_VALUE);
        assertThrows(IllegalStateException.class, () -> deque.push(TEST_VALUE));
    }

    @Test
    void testPop() {
        deque.add(TEST_VALUE);
        assertSame(TEST_VALUE, deque.pop());
        assertThrows(NoSuchElementException.class, () -> deque.pop());
    }

    @Test
    void testOffer() {
        assertTrue(deque.offer(TEST_VALUE));
        assertFalse(deque.offer(TEST_VALUE));
    }

    @Test
    void testPoll() {
        assertTrue(deque.offer(TEST_VALUE));
        assertSame(TEST_VALUE, deque.poll());
        assertNull(deque.poll());
    }

    @Test
    void testPeek() {
        assertNull(deque.peek());
        assertTrue(deque.offer(TEST_VALUE));
        assertSame(TEST_VALUE, deque.peek());
        assertSame(TEST_VALUE, deque.peek());
    }

    @Test
    void testDescendingIterator() {
        TestDeque<String> multiDeque = new TestDeque<>();
        multiDeque.add("A");
        multiDeque.add("B");
        multiDeque.add("C");
        Iterator<String> iterator = multiDeque.descendingIterator();
        assertTrue(iterator.hasNext());
        assertEquals("C", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("B", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("A", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void testSize() {
        assertEquals(0, deque.size());
        deque.add(TEST_VALUE);
        assertEquals(1, deque.size());
        deque.poll();
        assertEquals(0, deque.size());
    }
}