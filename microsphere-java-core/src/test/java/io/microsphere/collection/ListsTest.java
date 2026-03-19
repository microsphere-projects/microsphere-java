package io.microsphere.collection;

import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;

import static io.microsphere.AbstractTestCase.TEST_NULL_OBJECT_ARRAY;
import static io.microsphere.collection.ListUtils.of;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.Lists.ofList0;
import static io.microsphere.collection.Lists.ofList1;
import static io.microsphere.collection.Lists.ofList2;
import static io.microsphere.collection.Lists.ofList3;
import static io.microsphere.collection.Lists.ofList4;
import static io.microsphere.collection.Lists.ofList5;
import static io.microsphere.collection.Lists.ofList6;
import static io.microsphere.collection.Lists.ofList7;
import static io.microsphere.collection.Lists.ofList8;
import static io.microsphere.collection.Lists.ofList9;
import static io.microsphere.collection.Lists.ofList10;
import static io.microsphere.collection.Lists.ofListElements;
import static io.microsphere.invoke.MethodHandlesLookupUtils.findPublicStatic;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link Lists} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Lists
 * @since 1.0.0
 */
class ListsTest {

    @Test
    void testOfList0() {
        assertEquals(emptyList(), ofList());
    }

    @Test
    void testOfList1() {
        assertEquals(of(1), ofList(1));
    }

    @Test
    void testOfList2() {
        assertEquals(of(1, 2), ofList(1, 2));
    }

    @Test
    void testOfList3() {
        assertEquals(of(1, 2, 3), ofList(1, 2, 3));
    }

    @Test
    void testOfList4() {
        assertEquals(of(1, 2, 3, 4), ofList(1, 2, 3, 4));
    }

    @Test
    void testOfList5() {
        assertEquals(of(1, 2, 3, 4, 5), ofList(1, 2, 3, 4, 5));
    }

    @Test
    void testOfList6() {
        assertEquals(of(1, 2, 3, 4, 5, 6), ofList(1, 2, 3, 4, 5, 6));
    }

    @Test
    void testOfList7() {
        assertEquals(of(1, 2, 3, 4, 5, 6, 7), ofList(1, 2, 3, 4, 5, 6, 7));
    }

    @Test
    void testOfList8() {
        assertEquals(of(1, 2, 3, 4, 5, 6, 7, 8), ofList(1, 2, 3, 4, 5, 6, 7, 8));
    }

    @Test
    void testOfList9() {
        assertEquals(of(1, 2, 3, 4, 5, 6, 7, 8, 9), ofList(1, 2, 3, 4, 5, 6, 7, 8, 9));
    }

    @Test
    void testOfList10() {
        assertEquals(of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), ofList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    void testOfList() {
        assertEquals(emptyList(), ofList(TEST_NULL_OBJECT_ARRAY));
        assertEquals(of(1, 2, 3), ofList(1, 2, 3));
    }

    @Test
    void testOfListImmutability() {
        assertThrows(UnsupportedOperationException.class, () -> ofList().add("x"));
        assertThrows(UnsupportedOperationException.class, () -> ofList(1).add(2));
        assertThrows(UnsupportedOperationException.class, () -> ofList(1, 2).remove(0));
        assertThrows(UnsupportedOperationException.class, () -> ofList(1, 2, 3).clear());
    }

    @Test
    void testOfList0WithNull() {
        assertEquals(emptyList(), ofList0(null));
    }

    @Test
    void testOfList0WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyList");
        assertEquals(emptyList(), ofList0(wrongMethodHandle));
    }

    @Test
    void testOfList1WithNull() {
        assertEquals(singletonList(1), ofList1(null, 1));
    }

    @Test
    void testOfList1WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyList");
        assertEquals(singletonList(1), ofList1(wrongMethodHandle, 1));
    }

    @Test
    void testOfList2WithNull() {
        assertEquals(of(1, 2), ofList2(null, 1, 2));
    }

    @Test
    void testOfList2WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyList");
        assertEquals(of(1, 2), ofList2(wrongMethodHandle, 1, 2));
    }

    @Test
    void testOfList3WithNull() {
        assertEquals(of(1, 2, 3), ofList3(null, 1, 2, 3));
    }

    @Test
    void testOfList3WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyList");
        assertEquals(of(1, 2, 3), ofList3(wrongMethodHandle, 1, 2, 3));
    }

    @Test
    void testOfList4WithNull() {
        assertEquals(of(1, 2, 3, 4), ofList4(null, 1, 2, 3, 4));
    }

    @Test
    void testOfList4WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyList");
        assertEquals(of(1, 2, 3, 4), ofList4(wrongMethodHandle, 1, 2, 3, 4));
    }

    @Test
    void testOfList5WithNull() {
        assertEquals(of(1, 2, 3, 4, 5), ofList5(null, 1, 2, 3, 4, 5));
    }

    @Test
    void testOfList5WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyList");
        assertEquals(of(1, 2, 3, 4, 5), ofList5(wrongMethodHandle, 1, 2, 3, 4, 5));
    }

    @Test
    void testOfList6WithNull() {
        assertEquals(of(1, 2, 3, 4, 5, 6), ofList6(null, 1, 2, 3, 4, 5, 6));
    }

    @Test
    void testOfList6WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyList");
        assertEquals(of(1, 2, 3, 4, 5, 6), ofList6(wrongMethodHandle, 1, 2, 3, 4, 5, 6));
    }

    @Test
    void testOfList7WithNull() {
        assertEquals(of(1, 2, 3, 4, 5, 6, 7), ofList7(null, 1, 2, 3, 4, 5, 6, 7));
    }

    @Test
    void testOfList7WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyList");
        assertEquals(of(1, 2, 3, 4, 5, 6, 7), ofList7(wrongMethodHandle, 1, 2, 3, 4, 5, 6, 7));
    }

    @Test
    void testOfList8WithNull() {
        assertEquals(of(1, 2, 3, 4, 5, 6, 7, 8), ofList8(null, 1, 2, 3, 4, 5, 6, 7, 8));
    }

    @Test
    void testOfList8WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyList");
        assertEquals(of(1, 2, 3, 4, 5, 6, 7, 8), ofList8(wrongMethodHandle, 1, 2, 3, 4, 5, 6, 7, 8));
    }

    @Test
    void testOfList9WithNull() {
        assertEquals(of(1, 2, 3, 4, 5, 6, 7, 8, 9), ofList9(null, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    }

    @Test
    void testOfList9WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyList");
        assertEquals(of(1, 2, 3, 4, 5, 6, 7, 8, 9), ofList9(wrongMethodHandle, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    }

    @Test
    void testOfList10WithNull() {
        assertEquals(of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), ofList10(null, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    void testOfList10WithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyList");
        assertEquals(of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), ofList10(wrongMethodHandle, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    void testOfListElementsWithNull() {
        Integer[] elements = {1, 2, 3};
        assertEquals(of(elements), ofListElements(null, elements));
    }

    @Test
    void testOfListElementsWithWrongMethodHandle() {
        MethodHandle wrongMethodHandle = findPublicStatic(java.util.Collections.class, "emptyList");
        Integer[] elements = {1, 2, 3};
        assertEquals(of(elements), ofListElements(wrongMethodHandle, elements));
    }
}