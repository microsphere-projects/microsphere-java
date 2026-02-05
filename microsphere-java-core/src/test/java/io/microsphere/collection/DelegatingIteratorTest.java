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


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static io.microsphere.collection.ListUtils.ofArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DelegatingIterator} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DelegatingIterator
 * @since 1.0.0
 */
class DelegatingIteratorTest {

    private Iterator<String> iterator;

    private DelegatingIterator<String> delegatingIterator;

    @BeforeEach
    void setUp() {
        List<String> list = ofArrayList("1", "2", "3");
        this.iterator = list.iterator();
        this.delegatingIterator = new DelegatingIterator<>(this.iterator);
    }

    @Test
    void testHasNext() {
        assertTrue(delegatingIterator.hasNext());
    }

    @Test
    void testNext() {
        assertEquals("1", this.delegatingIterator.next());
        assertEquals("2", this.delegatingIterator.next());
        assertEquals("3", this.delegatingIterator.next());
        assertThrows(NoSuchElementException.class, this.delegatingIterator::next);
    }

    @Test
    void testRemove() {
        while (this.delegatingIterator.hasNext()) {
            this.delegatingIterator.next();
            this.delegatingIterator.remove();
        }
    }

    @Test
    void testForEachRemaining() {
        this.delegatingIterator.forEachRemaining(Assertions::assertNotNull);
    }

    @Test
    void testGetDelegate() {
        assertSame(this.delegatingIterator.getDelegate(), this.iterator);
    }

    @Test
    void testHashCode() {
        assertEquals(this.delegatingIterator.hashCode(), this.iterator.hashCode());
    }

    @Test
    void testEquals() {
        assertEquals(this.delegatingIterator, this.iterator);
    }

    @Test
    void testToString() {
        assertEquals(this.delegatingIterator.toString(), this.iterator.toString());
    }
}