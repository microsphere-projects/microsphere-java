package io.microsphere.collection;

import io.microsphere.Loggable;
import io.microsphere.lang.MutableInteger;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.microsphere.AbstractTestCase.TEST_NULL_ENUMERATION;
import static io.microsphere.AbstractTestCase.TEST_NULL_ITERABLE;
import static io.microsphere.AbstractTestCase.TEST_NULL_ITERATOR;
import static io.microsphere.AbstractTestCase.TEST_NULL_LIST;
import static io.microsphere.collection.ListUtils.addIfAbsent;
import static io.microsphere.collection.ListUtils.first;
import static io.microsphere.collection.ListUtils.forEach;
import static io.microsphere.collection.ListUtils.isList;
import static io.microsphere.collection.ListUtils.last;
import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.ListUtils.newCopyOnWriteArrayList;
import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.ListUtils.of;
import static io.microsphere.collection.ListUtils.ofArrayList;
import static io.microsphere.collection.ListUtils.ofLinkedList;
import static io.microsphere.collection.ListUtils.ofList;
import static io.microsphere.lang.MutableInteger.of;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyEnumeration;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptyList;
import static java.util.Collections.enumeration;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ListUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ListUtils
 * @since 1.0.0
 */
class ListUtilsTest implements Loggable {

    private static final List<String> TEST_LIST = asList("A", "B", "C");

    @Test
    void testIsListWithObject() {
        assertTrue(isList(new ArrayList()));
        assertTrue(isList(emptyList()));
        assertFalse(isList(emptyEnumeration()));
        assertFalse(isList("A"));
        assertFalse(isList((Object) null));
    }

    @Test
    void testIsListWithClass() {
        assertTrue(isList(List.class));
        assertTrue(isList(ArrayList.class));
        assertTrue(isList(LinkedList.class));
        assertFalse(isList(String.class));
        assertFalse(isList(null));
    }

    @Test
    void testFirst() {
        assertEquals("A", first(TEST_LIST));
        assertNull(first(emptyList()));
        assertNull(first(of()));
    }

    @Test
    void testLast() {
        assertEquals("C", last(TEST_LIST));
        assertNull(last(emptyList()));
        assertNull(last(of()));
    }

    @Test
    void testOf() {
        List<String> list = of();
        assertTrue(list.isEmpty());

        List<String> rawList = TEST_LIST;
        list = of("A", "B", "C");
        assertEquals(rawList, list);
    }

    @Test
    void testOfList() {
        List<String> rawList = TEST_LIST;
        List<String> list = ofList(rawList);
        assertEquals(rawList, list);

        list = ofList("A", "B", "C");
        assertEquals(rawList, list);

        Set<String> rawSet = singleton("A");
        list = ofList(rawSet);
        assertEquals(newArrayList(rawSet), list);

        list = ofList(enumeration(rawList));
        assertEquals(rawList, list);

        list = ofList(TEST_NULL_LIST);
        assertSame(emptyList(), list);
        assertEquals(emptyList(), list);

        list = ofList(TEST_NULL_ITERABLE);
        assertSame(emptyList(), list);
        assertEquals(emptyList(), list);

        list = ofList(TEST_NULL_ITERATOR);
        assertSame(emptyList(), list);
        assertEquals(emptyList(), list);

        list = ofList(TEST_NULL_ENUMERATION);
        assertEquals(emptyList(), list);
    }

    @Test
    void testNewArrayList() {
        assertEquals(newArrayList(), newArrayList(1));
        assertEquals(newArrayList(), newArrayList(emptyEnumeration()));
        assertEquals(newArrayList(newArrayList()), newArrayList(emptyIterator()));
    }

    @Test
    void testNewLinkedList() {
        assertEquals(emptyList(), newLinkedList());
        assertEquals(newLinkedList(), newLinkedList(emptyEnumeration()));
        assertEquals(newLinkedList(newArrayList()), newLinkedList(emptyIterator()));
    }

    @Test
    void testNewArrayListWithCollection() {
        Collection<String> source = asList("A", "B", "C");
        ArrayList<String> list = newArrayList(source);
        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
    }

    @Test
    void testNewArrayListWithSet() {
        HashSet<String> source = new HashSet<>(asList("A", "B", "C"));
        ArrayList<String> list = newArrayList(source);
        assertEquals(3, list.size());
        assertTrue(list.contains("A"));
        assertTrue(list.contains("B"));
        assertTrue(list.contains("C"));
    }

    @Test
    void testNewLinkedListWithCollection() {
        Collection<String> source = asList("A", "B", "C");
        LinkedList<String> list = newLinkedList(source);
        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
    }

    @Test
    void testNewLinkedListWithSet() {
        HashSet<String> source = new HashSet<>(asList("A", "B", "C"));
        LinkedList<String> list = newLinkedList(source);
        assertEquals(3, list.size());
        assertTrue(list.contains("A"));
        assertTrue(list.contains("B"));
        assertTrue(list.contains("C"));
    }

    @Test
    void testOfArrayList() {
        List<String> list = ofArrayList("A", "B", "C");
        assertEquals(list, ofList("A", "B", "C"));
        assertTrue(list.add("D"));
        assertTrue(list.addAll(ofList("A", "B", "C", "D")));
        assertTrue(list.removeAll(ofList("A", "B", "C")));
        assertTrue(list.containsAll(ofList("D")));
    }

    @Test
    void testOfArrayListOnEmptyArray() {
        assertThrows(IllegalArgumentException.class, () -> ofArrayList(null));
        assertThrows(IllegalArgumentException.class, () -> ofArrayList());
    }

    @Test
    void testOfLinkedList() {
        List<String> list = ofLinkedList("A", "B", "C");
        assertEquals(list, ofList("A", "B", "C"));
        assertTrue(list.add("D"));
        assertTrue(list.addAll(ofList("A", "B", "C", "D")));
        assertTrue(list.removeAll(ofList("A", "B", "C")));
        assertTrue(list.containsAll(ofList("D")));
    }

    @Test
    void testOfLinkedListOnEmptyArray() {
        assertThrows(IllegalArgumentException.class, () -> ofLinkedList(null));
        assertThrows(IllegalArgumentException.class, () -> ofLinkedList());
    }

    @Test
    void testForEach() {
        List<String> list = TEST_LIST;
        MutableInteger mutableInteger = of(0);
        Iterator<String> iterator = list.iterator();
        forEach(list, (index, value) -> {
            log("forEach(index = {} , value = '{}')", index, value);
            assertEquals(index, mutableInteger.getAndIncrement());
            assertTrue(iterator.hasNext());
            assertEquals(iterator.next(), value);
        });

        Iterator<String> iterator2 = list.iterator();
        forEach(list, (value) -> {
            log("forEach(value = '{}')", value);
            assertTrue(iterator2.hasNext());
            assertEquals(iterator2.next(), value);
        });
    }

    @Test
    void testAddIfAbsent() {
        ArrayList<String> values = newArrayList();
        assertTrue(addIfAbsent(values, "A"));
        assertFalse(addIfAbsent(values, "A"));
        assertTrue(addIfAbsent(values, "B"));
    }

    @Test
    void testNewCopyOnWriteArrayListEmpty() {
        CopyOnWriteArrayList<String> list = newCopyOnWriteArrayList();
        assertNotNull(list);
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    @Test
    void testNewCopyOnWriteArrayListWithCollection() {
        Collection<String> source = asList("a", "b", "c");
        CopyOnWriteArrayList<String> list = newCopyOnWriteArrayList(source);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
        assertTrue(list.contains("a"));
        assertTrue(list.contains("b"));
        assertTrue(list.contains("c"));
    }
}
