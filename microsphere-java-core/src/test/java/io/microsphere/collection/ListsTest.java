package io.microsphere.collection;

import org.junit.jupiter.api.Test;

import static io.microsphere.collection.ListUtils.of;
import static io.microsphere.collection.Lists.ofList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link Lists} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Lists
 * @since 1.0.0
 */
public class ListsTest {

    @Test
    public void testOfList0(){
        assertEquals(emptyList(), ofList());
    }

    @Test
    public void testOfList1() {
        assertEquals(of(1), ofList(1));
    }

    @Test
    public void testOfList2() {
        assertEquals(of(1, 2), ofList(1, 2));
    }

    @Test
    public void testOfList3() {
        assertEquals(of(1, 2, 3), ofList(1, 2, 3));
    }

    @Test
    public void testOfList4() {
        assertEquals(of(1, 2, 3, 4), ofList(1, 2, 3, 4));
    }

    @Test
    public void testOfList5() {
        assertEquals(of(1, 2, 3, 4, 5), ofList(1, 2, 3, 4, 5));
    }

    @Test
    public void testOfList6() {
        assertEquals(of(1, 2, 3, 4, 5, 6), ofList(1, 2, 3, 4, 5, 6));
    }

    @Test
    public void testOfList7() {
        assertEquals(of(1, 2, 3, 4, 5, 6, 7), ofList(1, 2, 3, 4, 5, 6, 7));
    }

    @Test
    public void testOfList8() {
        assertEquals(of(1, 2, 3, 4, 5, 6, 7, 8), ofList(1, 2, 3, 4, 5, 6, 7, 8));
    }

    @Test
    public void testOfList9() {
        assertEquals(of(1, 2, 3, 4, 5, 6, 7, 8, 9), ofList(1, 2, 3, 4, 5, 6, 7, 8, 9));
    }

    @Test
    public void testOfList10() {
        assertEquals(of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), ofList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    public void testOfList() {
        assertEquals(emptyList(), ofList(null));
        assertEquals(of(1, 2, 3), ofList(1, 2, 3));
    }
}