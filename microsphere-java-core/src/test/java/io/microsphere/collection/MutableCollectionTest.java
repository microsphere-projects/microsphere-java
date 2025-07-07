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
    public void init() {
        this.instance = newInstance();
        init(this.instance);
    }

    protected abstract C newInstance();

    protected void init(C instance) {
        instance.add("A");
        instance.add("B");
        instance.add("C");
    }

    @Test
    void testSize() {
        assertEquals(3, instance.size());
    }

    @Test
    void testIsEmpty() {
        assertFalse(instance.isEmpty());
    }

    @Test
    void testContains() {
        assertTrue(instance.contains("A"));
        assertTrue(instance.contains("B"));
        assertTrue(instance.contains("C"));
        assertFalse(instance.contains("D"));
    }

    @Test
    void testIterator() {
        Iterator<Object> iterator = instance.iterator();
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
        Object[] array = instance.toArray();
        assertEquals(3, array.length);
        assertArray("A", "B", "C");
    }

    @Test
    void testToArrayWithArray() {
        Object[] array = instance.toArray(new Object[0]);
        assertEquals(3, array.length);
        assertArray("A", "B", "C");
    }

    void assertArray(Object... array) {
        assertArrayEquals(array, instance.toArray());
    }

    @Test
    void testAdd() {
        testSize();
        assertTrue(instance.add("D"));
        assertEquals(4, instance.size());
    }

    @Test
    void testRemove() {
        assertTrue(instance.remove("A"));
        assertFalse(instance.remove("A"));
        assertTrue(instance.remove("B"));
        assertTrue(instance.remove("C"));
        assertFalse(instance.remove("D"));
        instance.add(null);
        assertTrue(instance.remove(null));
        assertFalse(instance.remove(null));
    }

    @Test
    void testContainsAll() {
        assertTrue(instance.containsAll(asList("A")));
        assertTrue(instance.containsAll(asList("A", "B")));
        assertTrue(instance.containsAll(asList("A", "B", "C")));
        assertFalse(instance.containsAll(asList("A", "B", "C", "D")));
        assertTrue(instance.containsAll(emptyList()));
    }

    @Test
    void testAddAll() {
        testSize();
        assertTrue(instance.addAll(asList("A")));
        assertEquals(4, instance.size());
        assertTrue(instance.addAll(asList("B", "C")));
        assertEquals(6, instance.size());
    }

    @Test
    void testRemoveAll() {
        testSize();
        assertTrue(instance.removeAll(asList("A")));
        assertEquals(2, instance.size());
        assertTrue(instance.removeAll(asList("B", "C")));
        assertTrue(instance.isEmpty());
    }

    @Test
    void testRemoveIf() {
        testSize();
        assertTrue(instance.removeIf(element -> element.equals("A")));
        assertEquals(2, instance.size());
        assertTrue(instance.removeIf(element -> element.equals("B")));
        assertEquals(1, instance.size());
        assertTrue(instance.removeIf(element -> element.equals("C")));
        assertTrue(instance.isEmpty());
    }

    @Test
    void testRetainAll() {
        testSize();
        assertTrue(instance.retainAll(asList("A", "B")));
        assertEquals(2, instance.size());
        assertTrue(instance.retainAll(asList("A")));
        assertEquals(1, instance.size());
    }

    @Test
    void testClear() {
        testSize();
        instance.clear();
        assertTrue(instance.isEmpty());
    }

    @Test
    void testEquals() {
        assertFalse(instance.equals(new Object()));
        assertFalse(instance.equals(ofList("A", "B")));
        assertFalse(instance.equals(ofList("A", "B", "D")));
        assertTrue(instance.equals(new ArrayList<>(instance)));
    }

    @Test
    void testHashCode() {
        assertEquals(instance.hashCode(), new ArrayList<>(instance).hashCode());

        instance.add(null);
        assertEquals(instance.hashCode(), new ArrayList<>(instance).hashCode());
    }

    @Test
    void testForEach() {
        Iterator<Object> iterator = instance.iterator();
        instance.forEach(e -> {
            assertTrue(iterator.hasNext());
            assertEquals(iterator.next(), e);
        });
    }

    @Test
    void testToString() {
        assertEquals(instance.toString(), new ArrayList<>(instance).toString());
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
