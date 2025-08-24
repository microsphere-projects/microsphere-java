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

import java.util.EmptyStackException;

import static io.microsphere.collection.ListUtils.ofArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ArrayStack} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ArrayStack
 * @since 1.0.0
 */
class ArrayStackTest {

    private ArrayStack<String> stack;

    @BeforeEach
    void setUp() {
        this.stack = new ArrayStack<>();
    }

    @Test
    void testPush() {
        this.stack.push("A");
        assertEquals(ofArrayList("A"), this.stack);
    }

    @Test
    void testPop() {
        this.stack.push("A");
        assertEquals("A", this.stack.pop());
        assertEquals(0, this.stack.size());
    }

    @Test
    void testPopWithoutElement() {
        assertThrows(EmptyStackException.class, this.stack::pop);
    }

    @Test
    void testPeek() {
        this.stack.push("A");
        assertEquals("A", this.stack.peek());
        assertEquals(1, this.stack.size());
    }

    @Test
    void testPeekWithoutElement() {
        assertThrows(EmptyStackException.class, this.stack::peek);
    }

    @Test
    void testEmpty() {
        assertTrue(this.stack.empty());

        this.stack.push("A");
        assertFalse(this.stack.empty());
    }

    @Test
    void testSearch() {
        this.stack.push("A");

        assertEquals(1, this.stack.search("A"));
        assertEquals(-1, this.stack.search("B"));
    }
}