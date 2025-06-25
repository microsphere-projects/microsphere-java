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
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.QueueUtils.singletonQueue;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link UnmodifiableQueue} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see UnmodifiableQueue
 * @since 1.0.0
 */
class UnmodifiableQueueTest {

    private UnmodifiableQueue<String> queue;

    @BeforeEach
    void setUp() {
        this.queue = new UnmodifiableQueue<>(singletonQueue(TEST_ELEMENT));
    }

    @Test
    void testSize() {
        assertEquals(1, queue.size());
    }

    @Test
    void testIsEmpty() {
        assertFalse(queue.isEmpty());
    }

    @Test
    void testContains() {
        assertTrue(queue.contains(TEST_ELEMENT));
    }

    @Test
    void testIterator() {
    }

    @Test
    void testToArray() {
        assertArrayEquals(ofArray(TEST_ELEMENT), queue.toArray());
        assertArrayEquals(ofArray(TEST_ELEMENT), queue.toArray(new String[0]));
    }

    @Test
    void testAdd() {
        assertThrows(UnsupportedOperationException.class, () -> queue.add(TEST_ELEMENT));
    }

    @Test
    void testRemove() {
        assertThrows(UnsupportedOperationException.class, queue::remove);
        assertThrows(UnsupportedOperationException.class, () -> queue.remove(TEST_ELEMENT));
    }

    @Test
    void testOffer() {
        assertThrows(UnsupportedOperationException.class, () -> queue.offer(TEST_ELEMENT));
    }

    @Test
    void testPoll() {
        assertThrows(UnsupportedOperationException.class, queue::poll);
    }

    @Test
    void testElement() {
        assertEquals(TEST_ELEMENT, queue.element());
    }

    @Test
    void testPeek() {
        assertEquals(TEST_ELEMENT, queue.peek());
    }

    @Test
    void testContainsAll() {
        assertTrue(queue.containsAll(ofList(TEST_ELEMENT)));
    }

    @Test
    void testAddAll() {
        assertThrows(UnsupportedOperationException.class, () -> queue.addAll(ofList(TEST_ELEMENT)));
    }

    @Test
    void testRemoveAll() {
        assertThrows(UnsupportedOperationException.class, () -> queue.removeAll(ofList(TEST_ELEMENT)));
    }

    @Test
    void testRemoveIf() {
        assertThrows(UnsupportedOperationException.class, () -> queue.removeIf(element -> element.equals(TEST_ELEMENT)));
    }

    @Test
    void testRetainAll() {
        assertThrows(UnsupportedOperationException.class, () -> queue.retainAll(ofList(TEST_ELEMENT)));
    }

    @Test
    void testClear() {
        assertThrows(UnsupportedOperationException.class, queue::clear);
    }

    @Test
    void testSpliterator() {
        assertNotNull(queue.spliterator());
    }

    @Test
    void testStream() {
        assertNotNull(queue.stream());
    }

    @Test
    void testParallelStream() {
        assertNotNull(queue.parallelStream());
    }

    @Test
    void testForEach() {
        queue.forEach(element -> assertEquals(TEST_ELEMENT, element));
    }

    @Test
    void testEquals() {
        assertEquals(queue, singletonQueue(TEST_ELEMENT));
    }

    @Test
    void testHashCode() {
        assertEquals(queue.hashCode(), singletonQueue(TEST_ELEMENT).hashCode());
    }

    @Test
    void testToString() {
        assertEquals(queue.toString(), singletonQueue(TEST_ELEMENT).toString());
    }
}