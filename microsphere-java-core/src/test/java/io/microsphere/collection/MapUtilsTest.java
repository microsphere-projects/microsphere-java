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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.MapUtils.FIXED_LOAD_FACTOR;
import static io.microsphere.collection.MapUtils.MIN_LOAD_FACTOR;
import static io.microsphere.collection.MapUtils.extraProperties;
import static io.microsphere.collection.MapUtils.flattenMap;
import static io.microsphere.collection.MapUtils.immutableEntry;
import static io.microsphere.collection.MapUtils.isEmpty;
import static io.microsphere.collection.MapUtils.isMap;
import static io.microsphere.collection.MapUtils.isNotEmpty;
import static io.microsphere.collection.MapUtils.nestedMap;
import static io.microsphere.collection.MapUtils.newConcurrentHashMap;
import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.collection.MapUtils.newLinkedHashMap;
import static io.microsphere.collection.MapUtils.newTreeMap;
import static io.microsphere.collection.MapUtils.of;
import static io.microsphere.collection.MapUtils.ofEntry;
import static io.microsphere.collection.MapUtils.ofMap;
import static io.microsphere.collection.MapUtils.shallowCloneMap;
import static io.microsphere.collection.MapUtils.size;
import static io.microsphere.collection.MapUtils.toFixedMap;
import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.util.ArrayUtils.EMPTY_OBJECT_ARRAY;
import static java.lang.Float.MIN_VALUE;
import static java.lang.Integer.valueOf;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MapUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see MapUtils
 * @since 1.0.0
 */
class MapUtilsTest {

    @Test
    void testConstants() {
        assertEquals(MIN_VALUE, MIN_LOAD_FACTOR);
        assertEquals(1.f, FIXED_LOAD_FACTOR);
    }

    @Test
    public void testIsMapWithInstance() {
        assertTrue(isMap(new HashMap()));
        assertTrue(isMap(emptyMap()));
        assertFalse(isMap("Hello,World"));
        assertFalse(isMap((Object) null));
    }

    @Test
    public void testIsMapWithClass() {
        assertTrue(isMap(Map.class));
        assertTrue(isMap(HashMap.class));
        assertTrue(isMap(LinkedHashMap.class));
        assertTrue(isMap(TreeMap.class));
        assertTrue(isMap(ConcurrentHashMap.class));
        assertTrue(isMap(ConcurrentSkipListMap.class));
        assertTrue(isMap(IdentityHashMap.class));
        assertFalse(isMap(Map.Entry.class));
        assertFalse(isMap(Object.class));
        assertFalse(isMap(null));
    }

    @Test
    void testIsEmpty() {
        assertTrue(isEmpty(null));
        assertTrue(isEmpty(emptyMap()));
        assertTrue(isEmpty(newHashMap()));
        assertTrue(isEmpty(newHashMap(2)));
        assertTrue(isEmpty(newHashMap(2, 0.01f)));
        assertFalse(isEmpty(of("A", 1)));
    }

    @Test
    void testIsNotEmpty() {
        assertFalse(isNotEmpty(null));
        assertFalse(isNotEmpty(emptyMap()));
        assertFalse(isNotEmpty(newLinkedHashMap()));
        assertFalse(isNotEmpty(newLinkedHashMap(2)));
        assertFalse(isNotEmpty(newLinkedHashMap(2, 0.01f)));
        assertTrue(isNotEmpty(of("A", 1)));
    }

    @Test
    void testSize() {
        assertEquals(0, size(null));
        assertEquals(0, size(emptyMap()));
        assertEquals(0, size(newHashMap()));
        assertEquals(1, size(of("A", 1)));
    }

    @Test
    void testOf1() {
        Map<String, Integer> map = of("A", 1);
        assertEquals(1, map.size());
        assertEquals(1, map.get("A"));
        assertNull(map.get("B"));
        assertOfMap(map);
    }

    @Test
    void testOf2() {
        Map<String, Integer> map = of("A", 1, "B", 2);
        assertEquals(2, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
        assertNull(map.get("C"));
        assertOfMap(map);
    }

    @Test
    void testOf3() {
        Map<String, Integer> map = of("A", 1, "B", 2, "C", 3);
        assertEquals(3, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
        assertEquals(3, map.get("C"));
        assertNull(map.get("D"));
        assertOfMap(map);
    }

    @Test
    void testOf4() {
        Map<String, Integer> map = of("A", 1, "B", 2, "C", 3, "D", 4);
        assertEquals(4, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
        assertEquals(3, map.get("C"));
        assertEquals(4, map.get("D"));
        assertNull(map.get("E"));
        assertOfMap(map);
    }

    @Test
    void testOf5() {
        Map<String, Integer> map = of("A", 1, "B", 2, "C", 3, "D", 4, "E", 5);
        assertEquals(5, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
        assertEquals(3, map.get("C"));
        assertEquals(4, map.get("D"));
        assertEquals(5, map.get("E"));
        assertNull(map.get("F"));
        assertOfMap(map);
    }

    @Test
    void testOf() {
        Map<String, Integer> map = of("B", 2, "C", 3, "D", 4, "E", 5, "F", 6);
        assertEquals(6, map.get("F"));
        assertEquals(5, map.get("E"));
        assertEquals(4, map.get("D"));
        assertEquals(3, map.get("C"));
        assertEquals(2, map.get("B"));
        assertNull(map.get("A"));
        assertOfMap(map);

        map = of("B", 2, "C", 3, "D", 4, "E", 5, "F", 6, "G", 7);
        assertEquals(7, map.get("G"));
        assertEquals(6, map.get("F"));
        assertEquals(5, map.get("E"));
        assertEquals(4, map.get("D"));
        assertEquals(3, map.get("C"));
        assertEquals(2, map.get("B"));
        assertNull(map.get("A"));
        assertOfMap(map);
    }

    @Test
    void testOfOnEntries() {
        Map.Entry<String, Integer> entryA = ofEntry("A", 1);
        Map.Entry<String, Integer> entryB = ofEntry("B", 2);
        Map.Entry<String, Integer> entryC = ofEntry("C", 3);

        Map<String, Integer> map = of(entryA, entryB, entryC);
        assertEquals(3, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
        assertEquals(3, map.get("C"));
        assertOfMap(map);
    }

    @Test
    void testOfOnNullEntries() {
        Map map = of((Map.Entry[]) null);
        assertSame(emptyMap(), map);
    }

    @Test
    void testOfOnEmptyEntries() {
        Map map = of(new Map.Entry[0]);
        assertSame(emptyMap(), map);
    }

    @Test
    void testOfMap1() {
        Map<String, Integer> map = ofMap("A", 1);
        assertEquals(1, map.size());
        assertEquals(1, map.get("A"));
        assertNull(map.get("B"));
        assertOfMap(map);
    }

    @Test
    void testOfMap() {
        Map<String, Integer> map = ofMap("A", 1, "C", 3);
        assertEquals(2, map.size());
        assertEquals(1, map.get("A"));
        assertNull(map.get("B"));
        assertEquals(3, map.get("C"));
        assertOfMap(map);
    }

    @Test
    void testOfMapOnEmptyArray() {
        Map map = ofMap(EMPTY_OBJECT_ARRAY);
        assertTrue(map.isEmpty());
        assertSame(emptyMap(), map);
    }

    @Test
    void testOfMapOnNull() {
        Map map = ofMap(null);
        assertTrue(map.isEmpty());
        assertSame(emptyMap(), map);
    }

    @Test
    void testOfEntry() {
        Map.Entry<String, Integer> entry = ofEntry("A", 1);
        assertEquals(DefaultEntry.class, entry.getClass());
        assertEquals("A", entry.getKey());
        assertEquals(1, entry.getValue());
        assertEquals(1, entry.setValue(2));
        assertEquals(2, entry.getValue());
    }

    @Test
    void testImmutableEntry() {
        Map.Entry<String, Integer> entry = immutableEntry("A", 1);
        assertEquals(ImmutableEntry.class, entry.getClass());
        assertEquals("A", entry.getKey());
        assertEquals(1, entry.getValue());
        assertThrows(UnsupportedOperationException.class, () -> entry.setValue(2));
    }

    @Test
    void testNewHashMap() {
        assertNewMap(HashMap.class, newHashMap());
        assertNewMap(HashMap.class, newHashMap(2));
        assertNewMap(HashMap.class, newHashMap(2, 0.75f));
        assertNewMap(HashMap.class, newHashMap(emptyMap()));
    }

    @Test
    void testNewLinkedHashMap() {
        assertNewMap(LinkedHashMap.class, newLinkedHashMap());
        assertNewMap(LinkedHashMap.class, newLinkedHashMap(2));
        assertNewMap(LinkedHashMap.class, newLinkedHashMap(2, 0.75f));
        assertNewMap(LinkedHashMap.class, newLinkedHashMap(2, 0.75f, true));
        assertNewMap(LinkedHashMap.class, newLinkedHashMap(emptyMap()));
    }

    @Test
    void testNewTreeMap() {
        assertNewMap(TreeMap.class, newTreeMap());
        assertNewMap(TreeMap.class, newTreeMap(Integer::compare));
        assertNewMap(TreeMap.class, newTreeMap(emptyMap()));
        assertNewMap(TreeMap.class, newTreeMap(new TreeMap<>()));
    }

    @Test
    void testNewConcurrentHashMap() {
        assertNewMap(ConcurrentHashMap.class, newConcurrentHashMap());
        assertNewMap(ConcurrentHashMap.class, newConcurrentHashMap(emptyMap()));
        assertNewMap(ConcurrentHashMap.class, newConcurrentHashMap(newHashMap()));
        assertNewMap(ConcurrentHashMap.class, newConcurrentHashMap(newTreeMap()));
        assertNewMap(ConcurrentHashMap.class, newConcurrentHashMap(1));
        assertNewMap(ConcurrentHashMap.class, newConcurrentHashMap(1, 0.75f));
    }

    @Test
    void testNewFixedHashMap() {
        assertNewFixedMap(8, MapUtils::newFixedHashMap);
    }

    @Test
    void testNewFixedLinkedHashMap() {
        assertNewFixedMap(8, MapUtils::newFixedLinkedHashMap);
    }

    @Test
    void testToFixedMap() {
        Map<String, Integer> map = toFixedMap(Collections.<String>emptyList(), a -> ofEntry(a, a.hashCode()));
        assertTrue(map.isEmpty());

        map = toFixedMap(ofList("A", "B"), a -> ofEntry(a, a.hashCode()));
        assertEquals(2, map.size());
        assertEquals("A".hashCode(), map.get("A"));
        assertEquals("B".hashCode(), map.get("B"));
    }

    @Test
    void testShallowCloneMap() {
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

    @Test
    void testFlattenMap() {
        Map<String, Object> level3Properties = ofMap("f", "F");
        Map<String, Object> level2Properties = ofMap("c", "C", "d", level3Properties);
        Map<String, Object> properties = ofMap("a", "A", "b", level2Properties, "z", emptyList());
        Map<String, Object> flattenMap = flattenMap(properties);
        assertEquals("A", flattenMap.get("a"));
        assertEquals("C", flattenMap.get("b.c"));
        assertEquals("F", flattenMap.get("b.d.f"));
    }

    @Test
    void testNestedMap() {
        Map<String, Object> map = newLinkedHashMap();
        map.put("a.b.1", "1");
        map.put("a.b.2", "2");
        map.put("d.e.f.1", "1");
        map.put("d.e.f.2", "2");
        map.put("d.e.f.3", "3");

        // {a={b={1=1, 2=2}}, d={e={f={1=1, 2=2, 3=3}}}}
        Map<String, Object> nestedMap = nestedMap(map);
        assertEquals(2, nestedMap.size());
        Map<String, Object> aMap = (Map<String, Object>) nestedMap.get("a");
        assertEquals(1, aMap.size());

        Map<String, Object> bMap = (Map<String, Object>) aMap.get("b");
        assertEquals(2, bMap.size());
        assertEquals("1", bMap.get("1"));
        assertEquals("2", bMap.get("2"));

        Map<String, Object> dMap = (Map<String, Object>) nestedMap.get("d");
        assertEquals(1, dMap.size());

        Map<String, Object> eMap = (Map<String, Object>) dMap.get("e");
        assertEquals(1, eMap.size());

        Map<String, Object> fMap = (Map<String, Object>) eMap.get("f");
        assertEquals(3, fMap.size());
        assertEquals("1", fMap.get("1"));
        assertEquals("2", fMap.get("2"));
        assertEquals("3", fMap.get("3"));

    }

    @Test
    public void testExtraProperties() {
        assertEquals(emptyMap(), extraProperties(null));
        assertEquals(emptyMap(), extraProperties(emptyMap()));
        assertEquals(emptyMap(), extraProperties(newHashMap()));
    }

    static void assertOfMap(Map map) {
        assertThrows(UnsupportedOperationException.class, () -> map.remove("A"));
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
