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

import static io.microsphere.AbstractTestCase.TEST_ELEMENT;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link UnmodifiableDeque} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see UnmodifiableDeque
 * @since 1.0.0
 */
class UnmodifiableDequeTest {

    private UnmodifiableDeque deque;

    @BeforeEach
    public void setUp() {
        deque = new UnmodifiableDeque(new SingletonDeque(TEST_ELEMENT));
    }

    @Test
    void testAddFirst() {
        assertThrows(UnsupportedOperationException.class, () -> deque.addFirst(TEST_ELEMENT));
    }

    @Test
    void testAddLast() {
        assertThrows(UnsupportedOperationException.class, () -> deque.addLast(TEST_ELEMENT));
    }

    @Test
    void testOfferFirst() {
        assertThrows(UnsupportedOperationException.class, () -> deque.offerFirst(TEST_ELEMENT));
    }

    @Test
    void testOfferLast() {
        assertThrows(UnsupportedOperationException.class, () -> deque.offerLast(TEST_ELEMENT));
    }

    @Test
    void testRemoveFirst() {
        assertThrows(UnsupportedOperationException.class, deque::removeFirst);
    }

    @Test
    void testRemoveLast() {
        assertThrows(UnsupportedOperationException.class, deque::removeLast);
    }

    @Test
    void testPollFirst() {
        assertThrows(UnsupportedOperationException.class, deque::pollFirst);
    }

    @Test
    void testPollLast() {
        assertThrows(UnsupportedOperationException.class, deque::pollLast);
    }

    @Test
    void testGetFirst() {
        assertEquals(TEST_ELEMENT, deque.getFirst());
    }

    @Test
    void testGetLast() {
        assertEquals(TEST_ELEMENT, deque.getLast());
    }

    @Test
    void testPeekFirst() {
        assertEquals(TEST_ELEMENT, deque.peekFirst());
    }

    @Test
    void testPeekLast() {
        assertEquals(TEST_ELEMENT, deque.peekLast());
    }

    @Test
    void testRemoveFirstOccurrence() {
        assertThrows(UnsupportedOperationException.class, () -> deque.removeFirstOccurrence(TEST_ELEMENT));
    }

    @Test
    void testRemoveLastOccurrence() {
        assertThrows(UnsupportedOperationException.class, () -> deque.removeLastOccurrence(TEST_ELEMENT));
    }

    @Test
    void testPush() {
        assertThrows(UnsupportedOperationException.class, () -> deque.push(TEST_ELEMENT));
    }

    @Test
    void testPop() {
        assertThrows(UnsupportedOperationException.class, deque::pop);
    }

    @Test
    void testDescendingIterator() {
        assertTrue(deque.descendingIterator() instanceof UnmodifiableIterator);
    }
}