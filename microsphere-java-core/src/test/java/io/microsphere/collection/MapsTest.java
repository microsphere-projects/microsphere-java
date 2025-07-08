package io.microsphere.collection;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.microsphere.collection.MapUtils.ofEntry;
import static io.microsphere.collection.MapUtilsTest.assertOfMap;
import static io.microsphere.collection.Maps.ofMap;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

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
}