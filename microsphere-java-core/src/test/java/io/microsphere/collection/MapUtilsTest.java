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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static io.microsphere.collection.MapUtils.immutableEntry;
import static io.microsphere.collection.MapUtils.isEmpty;
import static io.microsphere.collection.MapUtils.isNotEmpty;
import static io.microsphere.collection.MapUtils.newConcurrentHashMap;
import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.collection.MapUtils.newLinkedHashMap;
import static io.microsphere.collection.MapUtils.newTreeMap;
import static io.microsphere.collection.MapUtils.of;
import static io.microsphere.collection.MapUtils.ofEntry;
import static io.microsphere.collection.MapUtils.toFixedMap;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MapUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see MapUtils
 * @since 1.0.0
 */
public class MapUtilsTest {

    @Test
    public void testIsEmpty() {
        assertTrue(isEmpty(null));
        assertTrue(isEmpty(emptyMap()));
        assertTrue(isEmpty(newHashMap()));
        assertTrue(isEmpty(newHashMap(2)));
        assertTrue(isEmpty(newHashMap(2, 0.01f)));
        assertFalse(isEmpty(of("A", 1)));
    }

    @Test
    public void testIsNotEmpty() {
        assertFalse(isNotEmpty(null));
        assertFalse(isNotEmpty(emptyMap()));
        assertFalse(isNotEmpty(newLinkedHashMap()));
        assertFalse(isNotEmpty(newLinkedHashMap(2)));
        assertFalse(isNotEmpty(newLinkedHashMap(2, 0.01f)));
        assertTrue(isNotEmpty(of("A", 1)));
    }

    @Test
    public void testOf() {
        Map<String, Integer> map = of("A", 1);
        assertEquals(1, map.get("A"));
        assertNull(map.get("B"));

        map = of("B", 2, "C", 3);
        assertEquals(3, map.get("C"));
        assertEquals(2, map.get("B"));
        assertNull(map.get("A"));


        map = of("B", 2, "C", 3, "D", 4);
        assertEquals(4, map.get("D"));
        assertEquals(3, map.get("C"));
        assertEquals(2, map.get("B"));
        assertNull(map.get("A"));

        map = of("B", 2, "C", 3, "D", 4, "E", 5);
        assertEquals(5, map.get("E"));
        assertEquals(4, map.get("D"));
        assertEquals(3, map.get("C"));
        assertEquals(2, map.get("B"));
        assertNull(map.get("A"));

        map = of("B", 2, "C", 3, "D", 4, "E", 5, "F", 6);
        assertEquals(6, map.get("F"));
        assertEquals(5, map.get("E"));
        assertEquals(4, map.get("D"));
        assertEquals(3, map.get("C"));
        assertEquals(2, map.get("B"));
        assertNull(map.get("A"));

        map = of("B", 2, "C", 3, "D", 4, "E", 5, "F", 6, "G", 7);
        assertEquals(7, map.get("G"));
        assertEquals(6, map.get("F"));
        assertEquals(5, map.get("E"));
        assertEquals(4, map.get("D"));
        assertEquals(3, map.get("C"));
        assertEquals(2, map.get("B"));
        assertNull(map.get("A"));
    }

    @Test
    public void testOfEntry() {
        Map.Entry<String, Integer> entry = ofEntry("A", 1);
        assertEquals(MapUtils.ImmutableEntry.class, entry.getClass());
        assertEquals("A", entry.getKey());
        assertEquals(1, entry.getValue());
        assertThrows(UnsupportedOperationException.class, () -> entry.setValue(2));
    }

    @Test
    public void testImmutableEntry() {
        Map.Entry<String, Integer> entry = immutableEntry("A", 1);
        assertEquals(MapUtils.ImmutableEntry.class, entry.getClass());
        assertEquals("A", entry.getKey());
        assertEquals(1, entry.getValue());
        assertThrows(UnsupportedOperationException.class, () -> entry.setValue(2));
    }

    @Test
    public void testNew() {
        assertNewMap(HashMap.class, newHashMap());
        assertNewMap(HashMap.class, newHashMap(2));
        assertNewMap(HashMap.class, newHashMap(2, 0.75f));
        assertNewMap(HashMap.class, newHashMap(emptyMap()));

        assertNewMap(LinkedHashMap.class, newLinkedHashMap());
        assertNewMap(LinkedHashMap.class, newLinkedHashMap(2));
        assertNewMap(LinkedHashMap.class, newLinkedHashMap(2, 0.75f));
        assertNewMap(LinkedHashMap.class, newLinkedHashMap(emptyMap()));

        assertNewMap(TreeMap.class, newTreeMap());
        assertNewMap(TreeMap.class, newTreeMap(Integer::compare));
        assertNewMap(TreeMap.class, newTreeMap(emptyMap()));
        assertNewMap(TreeMap.class, newTreeMap(new TreeMap<>()));

        assertNewMap(ConcurrentHashMap.class, newConcurrentHashMap());
        assertNewMap(ConcurrentHashMap.class, newConcurrentHashMap(2));
        assertNewMap(ConcurrentHashMap.class, newConcurrentHashMap(2, 0.75f));
        assertNewMap(ConcurrentHashMap.class, newConcurrentHashMap(emptyMap()));
    }

    @Test
    public void testToFixedMap() {
        Map<String, Integer> map = toFixedMap(Arrays.asList("A", "B"), a -> ofEntry(a, a.hashCode()));
        assertEquals(2, map.size());
        assertEquals("A".hashCode(), map.get("A"));
        assertEquals("B".hashCode(), map.get("B"));
    }

    private void assertNewMap(Class<? extends Map> mapClass, Map<?, ?> map) {
        assertEquals(mapClass, map.getClass());
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }
}
