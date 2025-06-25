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


import org.junit.jupiter.api.Test;

import static io.microsphere.collection.CollectionUtils.emptyIterator;
import static io.microsphere.collection.EmptyDeque.INSTANCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link EmptyDeque} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EmptyDeque
 * @since 1.0.0
 */
class EmptyDequeTest {

    private EmptyDeque emptyDeque = INSTANCE;

    @Test
    void testIterator() {
        assertSame(emptyIterator(), emptyDeque.iterator());
    }

    @Test
    void testDescendingIterator() {
        assertSame(emptyIterator(), emptyDeque.descendingIterator());
    }

    @Test
    void testOfferFirst() {
        assertThrows(UnsupportedOperationException.class, () -> emptyDeque.offerFirst(null));
    }

    @Test
    void testOfferLast() {
        assertThrows(UnsupportedOperationException.class, () -> emptyDeque.offerLast(null));
    }

    @Test
    void testPollFirst() {
        assertThrows(UnsupportedOperationException.class, emptyDeque::pollFirst);
    }

    @Test
    void testPollLast() {
        assertThrows(UnsupportedOperationException.class, emptyDeque::pollLast);
    }

    @Test
    void testGetFirst() {
        assertThrows(UnsupportedOperationException.class, emptyDeque::getFirst);
    }

    @Test
    void testGetLast() {
        assertThrows(UnsupportedOperationException.class, emptyDeque::getLast);
    }

    @Test
    void testRemoveLastOccurrence() {
        assertFalse(emptyDeque.removeLastOccurrence(null));
    }

    @Test
    void testSize() {
        assertEquals(0, emptyDeque.size());
    }
}