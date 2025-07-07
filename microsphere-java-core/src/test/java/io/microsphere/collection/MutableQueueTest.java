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

import java.util.NoSuchElementException;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract Test for Mutable {@link Queue}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Queue
 * @since 1.0.0
 */
public abstract class MutableQueueTest<Q extends Queue<Object>> extends MutableCollectionTest<Q> {

    @Test
    void testOffer() {
        assertTrue(this.instance.offer("D"));
        assertTrue(this.instance.offer("E"));
    }

    @Test
    void testPoll() {
        assertEquals("A", this.instance.poll());
        assertEquals("B", this.instance.poll());
        assertEquals("C", this.instance.poll());
        assertNull(instance.poll());
    }

    @Test
    void testElement() {
        assertEquals("A", this.instance.element());
        assertEquals("A", this.instance.remove());
        assertEquals("B", this.instance.remove());
        assertEquals("C", this.instance.remove());
        assertThrows(NoSuchElementException.class, instance::element);
    }

    @Test
    void testPeek() {
        assertEquals("A", this.instance.peek());
        assertEquals("A", this.instance.peek());
        assertEquals("A", this.instance.peek());
    }
}
