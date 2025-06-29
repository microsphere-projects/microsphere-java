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

import static io.microsphere.AbstractTestCase.TEST_ELEMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link SingletonDeque} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SingletonDeque
 * @since 1.0.0
 */
class SingletonDequeTest {

    private SingletonDeque singletonDeque;

    @BeforeEach
    public void before() {
        singletonDeque = new SingletonDeque(TEST_ELEMENT);
    }

    @Test
    void testIterator() {
        testIterator(singletonDeque.iterator());
    }

    @Test
    void testDescendingIterator() {
        testIterator(singletonDeque.descendingIterator());
    }

    void testIterator(Iterator<?> iterator) {
        ReadOnlyIteratorTest test = new ReadOnlyIteratorTest() {

            @Override
            protected Iterator<?> createIterator() {
                return iterator;
            }
        };

        test.testHasNext();
        test.testNext();
        test.testRemove();
        test.testForEachRemaining();
    }

    @Test
    void testRemoveFirstOccurrence() {
        assertThrows(UnsupportedOperationException.class, () -> singletonDeque.removeFirstOccurrence(TEST_ELEMENT));
    }

    @Test
    void testOfferFirst() {
        assertThrows(UnsupportedOperationException.class, () -> singletonDeque.offerFirst(TEST_ELEMENT));
    }

    @Test
    void testOfferLast() {
        assertThrows(UnsupportedOperationException.class, () -> singletonDeque.offerLast(TEST_ELEMENT));
    }

    @Test
    void testPollFirst() {
        assertThrows(UnsupportedOperationException.class, singletonDeque::pollFirst);
    }

    @Test
    void testPollLast() {
        assertThrows(UnsupportedOperationException.class, singletonDeque::pollLast);
    }

    @Test
    void testGetFirst() {
        assertEquals(TEST_ELEMENT, singletonDeque.getFirst());
    }

    @Test
    void testGetLast() {
        assertEquals(TEST_ELEMENT, singletonDeque.getLast());
    }

    @Test
    void testRemoveLastOccurrence() {
        assertThrows(UnsupportedOperationException.class, () -> singletonDeque.removeLastOccurrence(TEST_ELEMENT));
    }

    @Test
    void testSize() {
        assertEquals(1, singletonDeque.size());
    }

    @Test
    public void testEquals() {
        assertEquals(singletonDeque, singletonDeque);
        assertNotEquals(singletonDeque, null);
    }

    @Test
    public void testHashCode() {
        assertEquals(singletonDeque.hashCode(), singletonDeque.hashCode());
        assertEquals(TEST_ELEMENT.hashCode(), singletonDeque.hashCode());
    }
}