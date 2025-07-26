package io.microsphere.collection;

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static io.microsphere.collection.CollectionUtils.toIterable;
import static io.microsphere.collection.EnumerationUtils.ofEnumeration;
import static io.microsphere.collection.MapUtils.FIXED_LOAD_FACTOR;
import static io.microsphere.collection.SetUtils.isSet;
import static io.microsphere.collection.SetUtils.newHashSet;
import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.collection.SetUtils.of;
import static io.microsphere.collection.SetUtils.ofSet;
import static java.util.Collections.emptyEnumeration;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SetUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see SetUtils
 * @since 1.0.0
 */
class SetUtilsTest extends AbstractTestCase {

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
        Set<String> expectedSet = new LinkedHashSet<>();
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
        Collection<String> elements = newHashSet(iterable);
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

    private void assertSet(Set<String> set) {
        assertEquals(3, set.size());
        assertTrue(set.contains("a"));
        assertTrue(set.contains("b"));
        assertTrue(set.contains("c"));
        assertFalse(set.contains("d"));
    }

}