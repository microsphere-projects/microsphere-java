package io.microsphere.collection;

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.Test;

import static io.microsphere.collection.SetUtils.of;
import static io.microsphere.collection.Sets.ofSet;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link Sets} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Sets
 * @since 1.0.0
 */
class SetsTest extends AbstractTestCase {

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
}