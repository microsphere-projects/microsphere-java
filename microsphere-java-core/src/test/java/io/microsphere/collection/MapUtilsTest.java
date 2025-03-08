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

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.MapUtils.FIXED_LOAD_FACTOR;
import static io.microsphere.collection.MapUtils.MIN_LOAD_FACTOR;
import static io.microsphere.collection.MapUtils.immutableEntry;
import static io.microsphere.collection.MapUtils.isEmpty;
import static io.microsphere.collection.MapUtils.isNotEmpty;
import static io.microsphere.collection.MapUtils.newConcurrentHashMap;
import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.collection.MapUtils.newLinkedHashMap;
import static io.microsphere.collection.MapUtils.newTreeMap;
import static io.microsphere.collection.MapUtils.of;
import static io.microsphere.collection.MapUtils.ofEntry;
import static io.microsphere.collection.MapUtils.ofMap;
import static io.microsphere.collection.MapUtils.shallowCloneMap;
import static io.microsphere.collection.MapUtils.toFixedMap;
import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static java.lang.Integer.valueOf;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
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
    public void testConstants() {
        assertEquals(Float.MIN_VALUE, MIN_LOAD_FACTOR);
        assertEquals(1.f, FIXED_LOAD_FACTOR);
    }

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
    public void testOf1() {
        Map<String, Integer> map = of("A", 1);
        assertEquals(1, map.size());
        assertEquals(1, map.get("A"));
        assertNull(map.get("B"));
    }

    @Test
    public void testOf2() {
        Map<String, Integer> map = of("A", 1, "B", 2);
        assertEquals(2, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
        assertNull(map.get("C"));
    }

    @Test
    public void testOf3() {
        Map<String, Integer> map = of("A", 1, "B", 2, "C", 3);
        assertEquals(3, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
        assertEquals(3, map.get("C"));
        assertNull(map.get("D"));
    }

    @Test
    public void testOf4() {
        Map<String, Integer> map = of("A", 1, "B", 2, "C", 3, "D", 4);
        assertEquals(4, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
        assertEquals(3, map.get("C"));
        assertEquals(4, map.get("D"));
        assertNull(map.get("E"));
    }

    @Test
    public void testOf5() {
        Map<String, Integer> map = of("A", 1, "B", 2, "C", 3, "D", 4, "E", 5);
        assertEquals(5, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
        assertEquals(3, map.get("C"));
        assertEquals(4, map.get("D"));
        assertEquals(5, map.get("E"));
        assertNull(map.get("F"));
    }

    @Test
    public void testOf() {
        Map<String, Integer> map = of("B", 2, "C", 3, "D", 4, "E", 5, "F", 6);
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
    public void testOfMap() {
        Map<String, Integer> map = ofMap("A", 1);
        assertEquals(1, map.size());
        assertEquals(1, map.get("A"));
        assertNull(map.get("B"));
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
    public void testNewHashMap() {
        assertNewMap(HashMap.class, newHashMap());
        assertNewMap(HashMap.class, newHashMap(2));
        assertNewMap(HashMap.class, newHashMap(2, 0.75f));
        assertNewMap(HashMap.class, newHashMap(emptyMap()));
    }

    @Test
    public void testNewLinkedHashMap() {
        assertNewMap(LinkedHashMap.class, newLinkedHashMap());
        assertNewMap(LinkedHashMap.class, newLinkedHashMap(2));
        assertNewMap(LinkedHashMap.class, newLinkedHashMap(2, 0.75f));
        assertNewMap(LinkedHashMap.class, newLinkedHashMap(2, 0.75f, true));
        assertNewMap(LinkedHashMap.class, newLinkedHashMap(emptyMap()));
    }

    @Test
    public void testNewTreeMap() {
        assertNewMap(TreeMap.class, newTreeMap());
        assertNewMap(TreeMap.class, newTreeMap(Integer::compare));
        assertNewMap(TreeMap.class, newTreeMap(emptyMap()));
        assertNewMap(TreeMap.class, newTreeMap(new TreeMap<>()));
    }

    @Test
    public void testNewConcurrentHashMap() {
        assertNewMap(TreeMap.class, newTreeMap());
        assertNewMap(TreeMap.class, newTreeMap(Integer::compare));
        assertNewMap(TreeMap.class, newTreeMap(emptyMap()));
        assertNewMap(TreeMap.class, newTreeMap(new TreeMap<>()));
    }

    @Test
    public void testNewFixedHashMap() {
        assertNewFixedMap(8, MapUtils::newFixedHashMap);
    }

    @Test
    public void testNewFixedLinkedHashMap() {
        assertNewFixedMap(8, MapUtils::newFixedLinkedHashMap);
    }

    @Test
    public void testToFixedMap() {
        Map<String, Integer> map = toFixedMap(Collections.<String>emptyList(), a -> ofEntry(a, a.hashCode()));
        assertTrue(map.isEmpty());

        map = toFixedMap(ofList("A", "B"), a -> ofEntry(a, a.hashCode()));
        assertEquals(2, map.size());
        assertEquals("A".hashCode(), map.get("A"));
        assertEquals("B".hashCode(), map.get("B"));
    }

    @Test
    public void testShallowCloneMap() {
        Map<String, Integer> map = of("A", 1);
        Map<String, Integer> cloneMap = shallowCloneMap(map);
        assertEquals(map, cloneMap);

        map = newTreeMap(map);
        cloneMap = shallowCloneMap(map);
        assertEquals(map, cloneMap);

        map = newLinkedHashMap(map);
        cloneMap = shallowCloneMap(map);
        assertEquals(map, cloneMap);

        map = new IdentityHashMap(map);
        cloneMap = shallowCloneMap(map);
        assertEquals(map, cloneMap);

        map = new ConcurrentSkipListMap<>(map);
        cloneMap = shallowCloneMap(map);
        assertEquals(map, cloneMap);

        map = newConcurrentHashMap(map);
        cloneMap = shallowCloneMap(map);
        assertEquals(map, cloneMap);

        map = unmodifiableMap(map);
        cloneMap = shallowCloneMap(map);
        assertEquals(map, cloneMap);
    }

    private void assertNewFixedMap(int size, Function<Integer, Map<String, Integer>> fixedMapCreator) {
        Map<String, Integer> map = fixedMapCreator.apply(size);
        map.put("A", 1);
        map.put("B", 2);
        map.put("C", 3);
        map.put("D", 4);
        map.put("E", 5);
        map.put("F", 6);
        map.put("G", 7);
        map.put("H", 8);
        assertEquals(size, map.size());
        assertEquals(valueOf(size), getFieldValue(map, "threshold"));
    }

    private void assertNewMap(Class<? extends Map> mapClass, Map<?, ?> map) {
        assertEquals(mapClass, map.getClass());
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }
}
