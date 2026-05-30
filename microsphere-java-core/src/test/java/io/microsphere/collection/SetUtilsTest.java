package io.microsphere.collection;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;

import static io.microsphere.AbstractTestCase.TEST_NULL_STRING_ARRAY;
import static io.microsphere.collection.CollectionUtils.toIterable;
import static io.microsphere.collection.EnumerationUtils.ofEnumeration;
import static io.microsphere.collection.MapUtils.FIXED_LOAD_FACTOR;
import static io.microsphere.collection.SetUtils.isSet;
import static io.microsphere.collection.SetUtils.newConcurrentSkipListSet;
import static io.microsphere.collection.SetUtils.newCopyOnWriteArraySet;
import static io.microsphere.collection.SetUtils.newFixedHashSet;
import static io.microsphere.collection.SetUtils.newFixedLinkedHashSet;
import static io.microsphere.collection.SetUtils.newHashSet;
import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.collection.SetUtils.newTreeSet;
import static io.microsphere.collection.SetUtils.of;
import static io.microsphere.collection.SetUtils.ofSet;
import static java.util.Collections.emptyEnumeration;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SetUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see SetUtils
 * @since 1.0.0
 */
class SetUtilsTest {

    private static final String[] ELEMENTS = new String[]{"a", "b", "c"};

    @Test
    void testIsSetWithInstance() {
        assertTrue(isSet(emptySet()));
        assertFalse(isSet(emptyList()));
        assertFalse(isSet("Hello,World"));
        assertFalse(isSet((Object) null));
    }

    @Test
    void testIsSetWithType() {
        assertTrue(isSet(Set.class));
        assertFalse(isSet(Collection.class));
        assertFalse(isSet(Map.class));
        assertFalse(isSet(null));
    }

    @Test
    void testOfSet() {
        Set<String> set = ofSet();
        assertEquals(emptySet(), set);

        set = ofSet((TEST_NULL_STRING_ARRAY));
        assertEquals(emptySet(), set);

        set = ofSet("A", "B", "C");
        LinkedHashSet<String> expectedSet = newLinkedHashSet();
        expectedSet.add("A");
        expectedSet.add("B");
        expectedSet.add("C");
        assertEquals(expectedSet, set);
    }

    @Test
    void testOf() {
        Set<String> set = of(ELEMENTS);
        assertSet(set);

        set = of();
        assertSame(emptySet(), set);
    }

    @Test
    void testOfSetForArray() {
        Set<String> set = ofSet("a", "b", "c");
        assertSet(set);
    }

    @Test
    void testOfSetForEnumeration() {
        Enumeration<String> e = null;
        assertSame(emptySet(), ofSet(e));

        e = emptyEnumeration();
        assertSame(emptySet(), ofSet(e));

        e = ofEnumeration(ELEMENTS);
        Set<String> set = ofSet(e);
        assertSet(set);
    }

    @Test
    void testOfSetIterable() {
        Iterable<String> iterable = null;
        assertSame(emptySet(), ofSet(iterable));
        iterable = emptyList();
        assertSame(emptySet(), ofSet(iterable));

        assertSame(emptySet(), ofSet(emptySet()));

        iterable = toIterable(newLinkedHashSet(ELEMENTS).iterator());
        assertEquals(newHashSet(ELEMENTS), ofSet(iterable));

        assertEquals(ofSet("a"), ofSet(emptySet(), "a"));
        assertEquals(ofSet("a", "b"), ofSet(ofSet("a"), "b"));
        assertEquals(ofSet("a", "b"), ofSet(ofSet("a", "b")));
    }

    @Test
    void testNewHashSet() {
        Iterable<String> iterable = newHashSet(1, FIXED_LOAD_FACTOR);
        assertEquals(iterable, newHashSet(iterable));

        iterable = newHashSet(ELEMENTS);
        HashSet<String> elements = newHashSet(iterable);
        assertEquals(iterable, newHashSet(iterable));
        assertEquals(iterable, newHashSet(elements));
        assertSet((Set<String>) iterable);
    }

    @Test
    void testNewLinkedHashSet() {
        Iterable<String> iterable = newLinkedHashSet(1, FIXED_LOAD_FACTOR);
        Collection<String> elements = newLinkedHashSet(iterable);
        assertEquals(iterable, elements);

        iterable = newLinkedHashSet(ELEMENTS);
        elements = newLinkedHashSet(iterable);
        assertEquals(iterable, newLinkedHashSet(iterable));
        assertEquals(iterable, newLinkedHashSet(elements));
        assertSet((Set<String>) iterable);
    }

    @Test
    void testNewFixedHashSet() {
        HashSet<String> set = newFixedHashSet(3);
        set.add("a");
        set.add("b");
        set.add("c");
        assertSet(set);
    }

    @Test
    void testNewFixedLinkedHashSet() {
        LinkedHashSet<String> set = newFixedLinkedHashSet(3);
        set.add("a");
        set.add("b");
        set.add("c");
        assertSet(set);
    }

    @Test
    void testNewTreeSetEmpty() {
        TreeSet<String> set = newTreeSet();
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
    }

    @Test
    void testNewTreeSetWithComparator() {
        Comparator<String> reverseComparator = (s1, s2) -> s2.compareTo(s1);
        TreeSet<String> set = newTreeSet(reverseComparator);
        set.add("a");
        set.add("b");
        set.add("c");
        assertEquals(3, set.size());
        // TreeSet with reverse comparator should be in reverse order
        Object[] elements = set.toArray();
        assertEquals("c", elements[0]);
        assertEquals("b", elements[1]);
        assertEquals("a", elements[2]);
    }

    @Test
    void testNewTreeSetWithCollection() {
        HashSet<String> sourceSet = newHashSet(ELEMENTS);
        TreeSet<String> set = newTreeSet(sourceSet);
        assertEquals(3, set.size());
        assertTrue(set.contains("a"));
        assertTrue(set.contains("b"));
        assertTrue(set.contains("c"));
        // TreeSet should be sorted naturally
        Object[] elements = set.toArray();
        assertEquals("a", elements[0]);
        assertEquals("b", elements[1]);
        assertEquals("c", elements[2]);
    }

    @Test
    void testNewTreeSetWithSortedSet() {
        TreeSet<String> sourceSortedSet = new TreeSet<>(java.util.Arrays.asList("z", "y", "x"));
        TreeSet<String> set = newTreeSet(sourceSortedSet);
        assertEquals(3, set.size());
        assertTrue(set.contains("x"));
        assertTrue(set.contains("y"));
        assertTrue(set.contains("z"));
        // TreeSet should be sorted naturally
        Object[] elements = set.toArray();
        assertEquals("x", elements[0]);
        assertEquals("y", elements[1]);
        assertEquals("z", elements[2]);
    }

    @Test
    void testNewCopyOnWriteArraySetEmpty() {
        CopyOnWriteArraySet<String> set = newCopyOnWriteArraySet();
        assertNotNull(set);
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
    }

    @Test
    void testNewCopyOnWriteArraySetWithCollection() {
        Collection<String> source = asList("a", "b", "c");
        CopyOnWriteArraySet<String> set = newCopyOnWriteArraySet(source);
        assertNotNull(set);
        assertEquals(3, set.size());
        assertTrue(set.contains("a"));
        assertTrue(set.contains("b"));
        assertTrue(set.contains("c"));
    }

    @Test
    void testNewConcurrentSkipListSetEmpty() {
        ConcurrentSkipListSet<String> set = newConcurrentSkipListSet();
        assertNotNull(set);
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
    }

    @Test
    void testNewConcurrentSkipListSetWithCollection() {
        Collection<String> source = asList("c", "a", "b");
        ConcurrentSkipListSet<String> set = newConcurrentSkipListSet(source);
        assertNotNull(set);
        assertEquals(3, set.size());
        assertTrue(set.contains("a"));
        assertTrue(set.contains("b"));
        assertTrue(set.contains("c"));
        // ConcurrentSkipListSet maintains sorted order
        Object[] elements = set.toArray();
        assertEquals("a", elements[0]);
        assertEquals("b", elements[1]);
        assertEquals("c", elements[2]);
    }

    @Test
    void testNewConcurrentSkipListSetWithSortedSet() {
        SortedSet<String> sourceSortedSet = new TreeSet<>(asList("z", "y", "x"));
        ConcurrentSkipListSet<String> set = newConcurrentSkipListSet(sourceSortedSet);
        assertNotNull(set);
        assertEquals(3, set.size());
        assertTrue(set.contains("x"));
        assertTrue(set.contains("y"));
        assertTrue(set.contains("z"));
        // ConcurrentSkipListSet maintains sorted order
        Object[] elements = set.toArray();
        assertEquals("x", elements[0]);
        assertEquals("y", elements[1]);
        assertEquals("z", elements[2]);
    }

    private void assertSet(Set<String> set) {
        assertEquals(3, set.size());
        assertTrue(set.contains("a"));
        assertTrue(set.contains("b"));
        assertTrue(set.contains("c"));
        assertFalse(set.contains("d"));
    }
}
