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

import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract Test for Mutable {@link Deque}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Deque
 * @since 1.0.0
 */
public abstract class MutableDequeTest<D extends Deque<Object>> extends MutableQueueTest<D> {

    @Test
    void testAddFirst() {
        this.instance.addFirst("@");
        assertEquals(4, this.instance.size());
        assertArray("@", "A", "B", "C");
    }

    @Test
    void testAddLast() {
        this.instance.addLast("D");
        assertEquals(4, this.instance.size());
        assertArray("A", "B", "C", "D");
    }

    @Test
    void testOfferFirst() {
        assertTrue(this.instance.offerFirst("@"));
        assertEquals(4, this.instance.size());
        assertArray("@", "A", "B", "C");
    }

    @Test
    void testOfferLast() {
        assertTrue(this.instance.offerLast("D"));
        assertEquals(4, this.instance.size());
        assertArray("A", "B", "C", "D");
    }

    @Test
    void testRemoveFirst() {
        assertEquals("A", this.instance.removeFirst());
        assertEquals("B", this.instance.removeFirst());
        assertEquals("C", this.instance.removeFirst());
        assertTrue(this.instance.isEmpty());
    }

    @Test
    void testRemoveLast() {
        assertEquals("C", this.instance.removeLast());
        assertEquals("B", this.instance.removeLast());
        assertEquals("A", this.instance.removeLast());
        assertThrows(NoSuchElementException.class, this.instance::removeLast);
    }

    @Test
    void testPollFirst() {
        assertEquals("A", this.instance.pollFirst());
        assertEquals("B", this.instance.pollFirst());
        assertEquals("C", this.instance.pollFirst());
        assertNull(this.instance.pollFirst());
    }

    @Test
    void testPollLast() {
        assertEquals("C", this.instance.pollLast());
        assertEquals("B", this.instance.pollLast());
        assertEquals("A", this.instance.pollLast());
        assertNull(this.instance.pollLast());
    }

    @Test
    void testPeekFirst() {
        assertEquals("A", this.instance.peekFirst());
        assertEquals("A", this.instance.peekFirst());
        this.instance.clear();
        assertNull(this.instance.peekFirst());
    }

    @Test
    void testPeekLast() {
        assertEquals("C", this.instance.peekLast());
        assertEquals("C", this.instance.peekLast());
        this.instance.clear();
        assertNull(this.instance.peekLast());
    }

    @Test
    void testGetFirst() {
        assertEquals("A", this.instance.getFirst());
        assertEquals("A", this.instance.getFirst());
        this.instance.clear();
        assertThrows(NoSuchElementException.class, this.instance::getFirst);
    }

    @Test
    void testGetLast() {
        assertEquals("C", this.instance.getLast());
        assertEquals("C", this.instance.getLast());
        this.instance.clear();
        assertThrows(NoSuchElementException.class, this.instance::getLast);
    }

    @Test
    void testRemoveFirstOccurrence() {
        assertTrue(this.instance.removeFirstOccurrence("A"));
        assertTrue(this.instance.removeFirstOccurrence("B"));
        assertTrue(this.instance.removeFirstOccurrence("C"));
        assertFalse(this.instance.removeFirstOccurrence("A"));
        assertFalse(this.instance.removeFirstOccurrence("B"));
        assertFalse(this.instance.removeFirstOccurrence("C"));
    }

    @Test
    void testRemoveLastOccurrence() {
        assertTrue(this.instance.removeLastOccurrence("A"));
        assertTrue(this.instance.removeLastOccurrence("B"));
        assertTrue(this.instance.removeLastOccurrence("C"));
        assertFalse(this.instance.removeLastOccurrence("A"));
        assertFalse(this.instance.removeLastOccurrence("B"));
        assertFalse(this.instance.removeLastOccurrence("C"));
    }

    @Test
    void testDescendingIterator() {
        Iterator<Object> iterator = this.instance.descendingIterator();
        assertTrue(iterator.hasNext());
        assertEquals("C", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("B", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("A", iterator.next());
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void testPush() {
        this.instance.push("@");
        assertArray("@", "A", "B", "C");
    }

    @Test
    void testPop() {
        assertEquals("A", this.instance.pop());
        assertEquals("B", this.instance.pop());
        assertEquals("C", this.instance.pop());
        assertThrows(NoSuchElementException.class, this.instance::pop);
    }
}
