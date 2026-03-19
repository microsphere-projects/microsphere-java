package io.microsphere.collection;

import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.util.Map;

import static io.microsphere.collection.MapUtils.ofEntry;
import static io.microsphere.collection.MapUtilsTest.assertOfMap;
import static io.microsphere.collection.Maps.ofMap;
import static io.microsphere.collection.Maps.ofMap0;
import static io.microsphere.collection.Maps.ofMap1;
import static io.microsphere.collection.Maps.ofMap2;
import static io.microsphere.collection.Maps.ofMap3;
import static io.microsphere.collection.Maps.ofMap4;
import static io.microsphere.collection.Maps.ofMap5;
import static io.microsphere.collection.Maps.ofMap6;
import static io.microsphere.collection.Maps.ofMap7;
import static io.microsphere.collection.Maps.ofMap8;
import static io.microsphere.collection.Maps.ofMap9;
import static io.microsphere.collection.Maps.ofMap10;
import static io.microsphere.collection.Maps.ofMapEntries;
import static io.microsphere.invoke.MethodHandlesLookupUtils.findPublicStatic;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link Maps} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Maps
 * @since 1.0.0
 */
class MapsTest {

    @Test
    void testOfMap0() {
        assertEquals(emptyMap(), ofMap());
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
    void testOfMap2() {
        Map<String, Integer> map = ofMap("A", 1, "B", 2);
        assertEquals(2, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
        assertNull(map.get("C"));
        assertOfMap(map);
    }

    @Test
    void testOfMap3() {
        Map<String, Integer> map = ofMap("A", 1, "B", 2, "C", 3);
        assertEquals(3, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
        assertEquals(3, map.get("C"));
        assertNull(map.get("D"));
        assertOfMap(map);
    }

    @Test
    void testOfMap4() {
        Map<String, Integer> map = ofMap("A", 1, "B", 2, "C", 3, "D", 4);
        assertEquals(4, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
        assertEquals(3, map.get("C"));
        assertEquals(4, map.get("D"));
        assertNull(map.get("E"));
        assertOfMap(map);
    }

    @Test
    void testOfMap5() {
        Map<String, Integer> map = ofMap("A", 1, "B", 2, "C", 3, "D", 4, "E", 5);
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
    void testOfMap6() {
        Map<String, Integer> map = ofMap("A", 1, "B", 2, "C", 3, "D", 4, "E", 5, "F", 6);
        assertEquals(6, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
        assertEquals(3, map.get("C"));
        assertEquals(4, map.get("D"));
        assertEquals(5, map.get("E"));
        assertEquals(6, map.get("F"));
        assertNull(map.get("G"));
        assertOfMap(map);
    }

    @Test
    void testOfMap7() {
        Map<String, Integer> map = ofMap("A", 1, "B", 2, "C", 3, "D", 4, "E", 5, "F", 6, "G", 7);
        assertEquals(7, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
        assertEquals(3, map.get("C"));
        assertEquals(4, map.get("D"));
        assertEquals(5, map.get("E"));
        assertEquals(6, map.get("F"));
        assertEquals(7, map.get("G"));
        assertNull(map.get("H"));
        assertOfMap(map);
    }

    @Test
    void testOfMap8() {
        Map<String, Integer> map = ofMap("A", 1, "B", 2, "C", 3, "D", 4, "E", 5, "F", 6, "G", 7, "H", 8);
        assertEquals(8, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
        assertEquals(3, map.get("C"));
        assertEquals(4, map.get("D"));
        assertEquals(5, map.get("E"));
        assertEquals(6, map.get("F"));
        assertEquals(7, map.get("G"));
        assertEquals(8, map.get("H"));
        assertNull(map.get("I"));
        assertOfMap(map);
    }

    @Test
    void testOfMap9() {
        Map<String, Integer> map = ofMap("A", 1, "B", 2, "C", 3, "D", 4, "E", 5, "F", 6, "G", 7, "H", 8, "I", 9);
        assertEquals(9, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
        assertEquals(3, map.get("C"));
        assertEquals(4, map.get("D"));
        assertEquals(5, map.get("E"));
        assertEquals(6, map.get("F"));
        assertEquals(7, map.get("G"));
        assertEquals(8, map.get("H"));
        assertEquals(9, map.get("I"));
        assertNull(map.get("J"));
        assertOfMap(map);
    }

    @Test
    void testOfMap10() {
        Map<String, Integer> map = ofMap("A", 1, "B", 2, "C", 3, "D", 4, "E", 5, "F", 6, "G", 7, "H", 8, "I", 9, "J", 10);
        assertEquals(10, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
        assertEquals(3, map.get("C"));
        assertEquals(4, map.get("D"));
        assertEquals(5, map.get("E"));
        assertEquals(6, map.get("F"));
        assertEquals(7, map.get("G"));
        assertEquals(8, map.get("H"));
        assertEquals(9, map.get("I"));
        assertEquals(10, map.get("J"));
        assertNull(map.get("K"));
        assertOfMap(map);
    }

    @Test
    void testOfMapOnEntries() {
        Map.Entry<String, Integer> entryA = ofEntry("A", 1);
        Map.Entry<String, Integer> entryB = ofEntry("B", 2);
        Map.Entry<String, Integer> entryC = ofEntry("C", 3);

        Map<String, Integer> map = Maps.ofMap(entryA, entryB, entryC);
        assertEquals(3, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
        assertEquals(3, map.get("C"));
        assertOfMap(map);
    }

    @Test
    void testMapOfOnNullEntries() {
        Map map = Maps.ofMap((Map.Entry[]) null);
        assertSame(emptyMap(), map);
    }

    @Test
    void testMapOfOnEmptyEntries() {
        Map map = Maps.ofMap(new Map.Entry[0]);
        assertSame(emptyMap(), map);
    }

    @Test
    void testOfMapOnSingleEntry() {
        Map.Entry<String, Integer> entry = ofEntry("A", 1);
        Map<String, Integer> map = Maps.ofMap(entry);
        assertEquals(1, map.size());
        assertEquals(1, map.get("A"));
        assertNull(map.get("B"));
        assertOfMap(map);
    }

    @Test
    void testOfMapImmutability() {
        assertThrows(UnsupportedOperationException.class, () -> ofMap().put("k", "v"));
        assertThrows(UnsupportedOperationException.class, () -> ofMap("A", 1).put("B", 2));
        assertThrows(UnsupportedOperationException.class, () -> ofMap("A", 1, "B", 2).remove("A"));
        assertThrows(UnsupportedOperationException.class, () -> ofMap("A", 1, "B", 2, "C", 3).clear());
    }

    @Test
    void testOfMap0WithNull() {
        assertEquals(emptyMap(), ofMap0(null));
    }

    @Test
    void testOfMap0WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyMap");
        assertEquals(emptyMap(), ofMap0(wrongMethodHandle));
    }

    @Test
    void testOfMap1WithNull() {
        assertEquals(singletonMap("A", 1), ofMap1(null, "A", 1));
    }

    @Test
    void testOfMap1WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyMap");
        assertEquals(singletonMap("A", 1), ofMap1(wrongMethodHandle, "A", 1));
    }

    @Test
    void testOfMap2WithNull() {
        Map<String, Integer> map = ofMap2(null, "A", 1, "B", 2);
        assertEquals(2, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
    }

    @Test
    void testOfMap2WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyMap");
        Map<String, Integer> map = ofMap2(wrongMethodHandle, "A", 1, "B", 2);
        assertEquals(2, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
    }

    @Test
    void testOfMap3WithNull() {
        Map<String, Integer> map = ofMap3(null, "A", 1, "B", 2, "C", 3);
        assertEquals(3, map.size());
    }

    @Test
    void testOfMap3WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyMap");
        Map<String, Integer> map = ofMap3(wrongMethodHandle, "A", 1, "B", 2, "C", 3);
        assertEquals(3, map.size());
    }

    @Test
    void testOfMap4WithNull() {
        Map<String, Integer> map = ofMap4(null, "A", 1, "B", 2, "C", 3, "D", 4);
        assertEquals(4, map.size());
    }

    @Test
    void testOfMap4WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyMap");
        Map<String, Integer> map = ofMap4(wrongMethodHandle, "A", 1, "B", 2, "C", 3, "D", 4);
        assertEquals(4, map.size());
    }

    @Test
    void testOfMap5WithNull() {
        Map<String, Integer> map = ofMap5(null, "A", 1, "B", 2, "C", 3, "D", 4, "E", 5);
        assertEquals(5, map.size());
    }

    @Test
    void testOfMap5WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyMap");
        Map<String, Integer> map = ofMap5(wrongMethodHandle, "A", 1, "B", 2, "C", 3, "D", 4, "E", 5);
        assertEquals(5, map.size());
    }

    @Test
    void testOfMap6WithNull() {
        Map<String, Integer> map = ofMap6(null, "A", 1, "B", 2, "C", 3, "D", 4, "E", 5, "F", 6);
        assertEquals(6, map.size());
    }

    @Test
    void testOfMap6WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyMap");
        Map<String, Integer> map = ofMap6(wrongMethodHandle, "A", 1, "B", 2, "C", 3, "D", 4, "E", 5, "F", 6);
        assertEquals(6, map.size());
    }

    @Test
    void testOfMap7WithNull() {
        Map<String, Integer> map = ofMap7(null, "A", 1, "B", 2, "C", 3, "D", 4, "E", 5, "F", 6, "G", 7);
        assertEquals(7, map.size());
    }

    @Test
    void testOfMap7WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyMap");
        Map<String, Integer> map = ofMap7(wrongMethodHandle, "A", 1, "B", 2, "C", 3, "D", 4, "E", 5, "F", 6, "G", 7);
        assertEquals(7, map.size());
    }

    @Test
    void testOfMap8WithNull() {
        Map<String, Integer> map = ofMap8(null, "A", 1, "B", 2, "C", 3, "D", 4, "E", 5, "F", 6, "G", 7, "H", 8);
        assertEquals(8, map.size());
    }

    @Test
    void testOfMap8WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyMap");
        Map<String, Integer> map = ofMap8(wrongMethodHandle, "A", 1, "B", 2, "C", 3, "D", 4, "E", 5, "F", 6, "G", 7, "H", 8);
        assertEquals(8, map.size());
    }

    @Test
    void testOfMap9WithNull() {
        Map<String, Integer> map = ofMap9(null, "A", 1, "B", 2, "C", 3, "D", 4, "E", 5, "F", 6, "G", 7, "H", 8, "I", 9);
        assertEquals(9, map.size());
    }

    @Test
    void testOfMap9WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyMap");
        Map<String, Integer> map = ofMap9(wrongMethodHandle, "A", 1, "B", 2, "C", 3, "D", 4, "E", 5, "F", 6, "G", 7, "H", 8, "I", 9);
        assertEquals(9, map.size());
    }

    @Test
    void testOfMap10WithNull() {
        Map<String, Integer> map = ofMap10(null, "A", 1, "B", 2, "C", 3, "D", 4, "E", 5, "F", 6, "G", 7, "H", 8, "I", 9, "J", 10);
        assertEquals(10, map.size());
    }

    @Test
    void testOfMap10WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyMap");
        Map<String, Integer> map = ofMap10(wrongMethodHandle, "A", 1, "B", 2, "C", 3, "D", 4, "E", 5, "F", 6, "G", 7, "H", 8, "I", 9, "J", 10);
        assertEquals(10, map.size());
    }

    @Test
    void testOfMapEntriesWithNull() {
        Map.Entry<String, Integer> entryA = ofEntry("A", 1);
        Map.Entry<String, Integer> entryB = ofEntry("B", 2);
        Map<String, Integer> map = ofMapEntries(null, new Map.Entry[]{entryA, entryB});
        assertEquals(2, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
    }

    @Test
    void testOfMapEntriesWithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyMap");
        Map.Entry<String, Integer> entryA = ofEntry("A", 1);
        Map.Entry<String, Integer> entryB = ofEntry("B", 2);
        Map<String, Integer> map = ofMapEntries(wrongMethodHandle, new Map.Entry[]{entryA, entryB});
        assertEquals(2, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
    }
}