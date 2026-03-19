package io.microsphere.collection;

import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;

import static io.microsphere.AbstractTestCase.TEST_NULL_OBJECT_ARRAY;
import static io.microsphere.collection.SetUtils.of;
import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.collection.Sets.ofSet0;
import static io.microsphere.collection.Sets.ofSet1;
import static io.microsphere.collection.Sets.ofSet2;
import static io.microsphere.collection.Sets.ofSet3;
import static io.microsphere.collection.Sets.ofSet4;
import static io.microsphere.collection.Sets.ofSet5;
import static io.microsphere.collection.Sets.ofSet6;
import static io.microsphere.collection.Sets.ofSet7;
import static io.microsphere.collection.Sets.ofSet8;
import static io.microsphere.collection.Sets.ofSet9;
import static io.microsphere.collection.Sets.ofSet10;
import static io.microsphere.collection.Sets.ofSetElements;
import static io.microsphere.invoke.MethodHandlesLookupUtils.findPublicStatic;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link Sets} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Sets
 * @since 1.0.0
 */
class SetsTest {

    @Test
    void testOfSet0() {
        assertEquals(emptySet(), ofSet());
    }

    @Test
    void testOfSet1() {
        assertEquals(of(1), ofSet(1));
    }

    @Test
    void testOfSet2() {
        assertEquals(of(1, 2), ofSet(1, 2));
    }

    @Test
    void testOfSet3() {
        assertEquals(of(1, 2, 3), ofSet(1, 2, 3));
    }

    @Test
    void testOfSet4() {
        assertEquals(of(1, 2, 3, 4), ofSet(1, 2, 3, 4));
    }

    @Test
    void testOfSet5() {
        assertEquals(of(1, 2, 3, 4, 5), ofSet(1, 2, 3, 4, 5));
    }

    @Test
    void testOfSet6() {
        assertEquals(of(1, 2, 3, 4, 5, 6), ofSet(1, 2, 3, 4, 5, 6));
    }

    @Test
    void testOfSet7() {
        assertEquals(of(1, 2, 3, 4, 5, 6, 7), ofSet(1, 2, 3, 4, 5, 6, 7));
    }

    @Test
    void testOfSet8() {
        assertEquals(of(1, 2, 3, 4, 5, 6, 7, 8), ofSet(1, 2, 3, 4, 5, 6, 7, 8));
    }

    @Test
    void testOfSet9() {
        assertEquals(of(1, 2, 3, 4, 5, 6, 7, 8, 9), ofSet(1, 2, 3, 4, 5, 6, 7, 8, 9));
    }

    @Test
    void testOfSet10() {
        assertEquals(of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), ofSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    void testOfSet() {
        assertEquals(emptySet(), ofSet(TEST_NULL_OBJECT_ARRAY));
        assertEquals(of(1, 2, 3), ofSet(1, 2, 3));
    }

    @Test
    void testOfSetImmutability() {
        assertThrows(UnsupportedOperationException.class, () -> ofSet().add("x"));
        assertThrows(UnsupportedOperationException.class, () -> ofSet(1).add(2));
        assertThrows(UnsupportedOperationException.class, () -> ofSet(1, 2).remove(1));
        assertThrows(UnsupportedOperationException.class, () -> ofSet(1, 2, 3).clear());
    }

    @Test
    void testOfSet0WithNull() {
        assertEquals(emptySet(), ofSet0(null));
    }

    @Test
    void testOfSet0WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptySet");
        assertEquals(emptySet(), ofSet0(wrongMethodHandle));
    }

    @Test
    void testOfSet1WithNull() {
        assertEquals(singleton(1), ofSet1(null, 1));
    }

    @Test
    void testOfSet1WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptySet");
        assertEquals(singleton(1), ofSet1(wrongMethodHandle, 1));
    }

    @Test
    void testOfSet2WithNull() {
        assertEquals(of(1, 2), ofSet2(null, 1, 2));
    }

    @Test
    void testOfSet2WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptySet");
        assertEquals(of(1, 2), ofSet2(wrongMethodHandle, 1, 2));
    }

    @Test
    void testOfSet3WithNull() {
        assertEquals(of(1, 2, 3), ofSet3(null, 1, 2, 3));
    }

    @Test
    void testOfSet3WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptySet");
        assertEquals(of(1, 2, 3), ofSet3(wrongMethodHandle, 1, 2, 3));
    }

    @Test
    void testOfSet4WithNull() {
        assertEquals(of(1, 2, 3, 4), ofSet4(null, 1, 2, 3, 4));
    }

    @Test
    void testOfSet4WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptySet");
        assertEquals(of(1, 2, 3, 4), ofSet4(wrongMethodHandle, 1, 2, 3, 4));
    }

    @Test
    void testOfSet5WithNull() {
        assertEquals(of(1, 2, 3, 4, 5), ofSet5(null, 1, 2, 3, 4, 5));
    }

    @Test
    void testOfSet5WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptySet");
        assertEquals(of(1, 2, 3, 4, 5), ofSet5(wrongMethodHandle, 1, 2, 3, 4, 5));
    }

    @Test
    void testOfSet6WithNull() {
        assertEquals(of(1, 2, 3, 4, 5, 6), ofSet6(null, 1, 2, 3, 4, 5, 6));
    }

    @Test
    void testOfSet6WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptySet");
        assertEquals(of(1, 2, 3, 4, 5, 6), ofSet6(wrongMethodHandle, 1, 2, 3, 4, 5, 6));
    }

    @Test
    void testOfSet7WithNull() {
        assertEquals(of(1, 2, 3, 4, 5, 6, 7), ofSet7(null, 1, 2, 3, 4, 5, 6, 7));
    }

    @Test
    void testOfSet7WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptySet");
        assertEquals(of(1, 2, 3, 4, 5, 6, 7), ofSet7(wrongMethodHandle, 1, 2, 3, 4, 5, 6, 7));
    }

    @Test
    void testOfSet8WithNull() {
        assertEquals(of(1, 2, 3, 4, 5, 6, 7, 8), ofSet8(null, 1, 2, 3, 4, 5, 6, 7, 8));
    }

    @Test
    void testOfSet8WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptySet");
        assertEquals(of(1, 2, 3, 4, 5, 6, 7, 8), ofSet8(wrongMethodHandle, 1, 2, 3, 4, 5, 6, 7, 8));
    }

    @Test
    void testOfSet9WithNull() {
        assertEquals(of(1, 2, 3, 4, 5, 6, 7, 8, 9), ofSet9(null, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    }

    @Test
    void testOfSet9WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptySet");
        assertEquals(of(1, 2, 3, 4, 5, 6, 7, 8, 9), ofSet9(wrongMethodHandle, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    }

    @Test
    void testOfSet10WithNull() {
        assertEquals(of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), ofSet10(null, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    void testOfSet10WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptySet");
        assertEquals(of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), ofSet10(wrongMethodHandle, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    void testOfSetElementsWithNull() {
        Integer[] elements = {1, 2, 3};
        assertEquals(of(elements), ofSetElements(null, elements));
    }

    @Test
    void testOfSetElementsWithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptySet");
        Integer[] elements = {1, 2, 3};
        assertEquals(of(elements), ofSetElements(wrongMethodHandle, elements));
    }
}