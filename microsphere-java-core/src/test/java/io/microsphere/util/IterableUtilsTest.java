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

package io.microsphere.util;


import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import static io.microsphere.collection.QueueUtils.emptyDeque;
import static io.microsphere.collection.QueueUtils.emptyQueue;
import static io.microsphere.util.IterableUtils.isIterable;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link IterableUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see IterableUtils
 * @since 1.0.0
 */
class IterableUtilsTest {

    @Test
    void testIsIterableWithObject() {
        assertTrue(isIterable(emptyList()));
        assertTrue(isIterable(emptySet()));
        assertTrue(isIterable(emptyQueue()));
        assertTrue(isIterable(emptyDeque()));

        assertFalse(isIterable((Object) null));
        assertFalse(isIterable(""));
    }

    @Test
    void testTestIsIterableWithClass() {
        assertTrue(isIterable(Iterable.class));
        assertTrue(isIterable(Collection.class));
        assertTrue(isIterable(List.class));
        assertTrue(isIterable(Set.class));
        assertTrue(isIterable(Queue.class));
        assertTrue(isIterable(Deque.class));

        assertFalse(isIterable(Object.class));
        assertFalse(isIterable(null));
    }
}