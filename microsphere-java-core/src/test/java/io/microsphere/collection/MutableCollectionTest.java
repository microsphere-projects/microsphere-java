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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.util.ArrayUtils.EMPTY_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.StreamSupport.stream;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract Test for Mutable {@link Collection}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Collection
 * @since 1.0.0
 */
public abstract class MutableCollectionTest<C extends Collection<Object>> {

    protected C instance;

    @BeforeEach
    void setUp() {
        this.instance = newInstance();
        init(this.instance);
    }

    protected abstract C newInstance();

    protected void init(C instance) {
        this.instance.add("A");
        this.instance.add("B");
        this.instance.add("C");
    }

    @Test
    void testSize() {
        assertEquals(3, this.instance.size());
    }

    @Test
    void testIsEmpty() {
        assertFalse(this.instance.isEmpty());
    }

    @Test
    void testContains() {
        assertTrue(this.instance.contains("A"));
        assertTrue(this.instance.contains("B"));
        assertTrue(this.instance.contains("C"));
        assertFalse(this.instance.contains("D"));
    }

    @Test
    void testIterator() {
        Iterator<Object> iterator = this.instance.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("A", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("B", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("C", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void testToArray() {
        Object[] array = this.instance.toArray();
        assertEquals(3, array.length);
        assertArray("A", "B", "C");
    }

    @Test
    void testToArrayWithArray() {
        Object[] array = this.instance.toArray(EMPTY_OBJECT_ARRAY);
        assertEquals(3, array.length);
        assertArray("A", "B", "C");
    }

    void assertArray(Object... array) {
        assertArrayEquals(array, this.instance.toArray());
    }

    @Test
    void testAdd() {
        testSize();
        assertTrue(this.instance.add("D"));
        assertEquals(4, this.instance.size());
    }

    @Test
    void testRemove() {
        assertTrue(this.instance.remove("A"));
        assertFalse(this.instance.remove("A"));
        assertTrue(this.instance.remove("B"));
        assertTrue(this.instance.remove("C"));
        assertFalse(this.instance.remove("D"));
        this.instance.add(null);
        assertTrue(this.instance.remove(null));
        assertFalse(this.instance.remove(null));
    }

    @Test
    void testContainsAll() {
        assertTrue(this.instance.containsAll(asList("A")));
        assertTrue(this.instance.containsAll(asList("A", "B")));
        assertTrue(this.instance.containsAll(asList("A", "B", "C")));
        assertFalse(this.instance.containsAll(asList("A", "B", "C", "D")));
        assertTrue(this.instance.containsAll(emptyList()));
    }

    @Test
    void testAddAll() {
        testSize();
        assertTrue(this.instance.addAll(asList("A")));
        assertEquals(4, this.instance.size());
        assertTrue(this.instance.addAll(asList("B", "C")));
        assertEquals(6, this.instance.size());
    }

    @Test
    void testRemoveAll() {
        testSize();
        assertTrue(this.instance.removeAll(asList("A")));
        assertEquals(2, this.instance.size());
        assertTrue(this.instance.removeAll(asList("B", "C")));
        assertTrue(this.instance.isEmpty());
    }

    @Test
    void testRemoveIf() {
        testSize();
        assertTrue(this.instance.removeIf(element -> element.equals("A")));
        assertEquals(2, this.instance.size());
        assertTrue(this.instance.removeIf(element -> element.equals("B")));
        assertEquals(1, this.instance.size());
        assertTrue(this.instance.removeIf(element -> element.equals("C")));
        assertTrue(this.instance.isEmpty());
    }

    @Test
    void testRetainAll() {
        testSize();
        assertTrue(this.instance.retainAll(asList("A", "B")));
        assertEquals(2, this.instance.size());
        assertTrue(this.instance.retainAll(asList("A")));
        assertEquals(1, this.instance.size());
    }

    @Test
    void testClear() {
        testSize();
        this.instance.clear();
        assertTrue(this.instance.isEmpty());
    }

    @Test
    void testEquals() {
        assertFalse(this.instance.equals(new Object()));
        assertFalse(this.instance.equals(ofList("A", "B")));
        assertFalse(this.instance.equals(ofList("A", "B", "D")));
        assertTrue(this.instance.equals(new ArrayList<>(instance)));
    }

    @Test
    void testHashCode() {
        assertEquals(this.instance.hashCode(), new ArrayList<>(instance).hashCode());

        this.instance.add(null);
        assertEquals(this.instance.hashCode(), new ArrayList<>(instance).hashCode());
    }

    @Test
    void testForEach() {
        Iterator<Object> iterator = this.instance.iterator();
        this.instance.forEach(e -> {
            assertTrue(iterator.hasNext());
            assertEquals(iterator.next(), e);
        });
    }

    @Test
    void testToString() {
        assertEquals(this.instance.toString(), new ArrayList<>(instance).toString());
    }

    @Test
    void testSpliterator() {
        assertArrayEquals(ofArray("A", "B", "C"), stream(this.instance.spliterator(), false).toArray());
    }

    @Test
    void testStream() {
        assertArrayEquals(ofArray("A", "B", "C"), this.instance.stream().toArray());
    }

    @Test
    void testParallelStream() {
        assertArrayEquals(ofArray("A", "B", "C"), this.instance.parallelStream().toArray());
    }
}
