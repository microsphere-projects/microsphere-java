package io.microsphere.collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AbstractDeque} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractDeque
 * @since 1.0.0
 */
class AbstractDequeTest {

    private static final String TEST_VALUE = "1";

    private AbstractDeque<String> deque;

    @BeforeEach
    void setUp() {
        deque = new TestDeque<>(1);
    }

    @Test
    void testAddFirst() {
        deque.addFirst(TEST_VALUE);
        assertThrows(IllegalStateException.class, () -> deque.addFirst(TEST_VALUE));
    }

    @Test
    void testOfferFirst() {
        assertTrue(deque.offerFirst(TEST_VALUE));
        assertFalse(deque.offerFirst(TEST_VALUE));
    }

    @Test
    void testAddLast() {
        deque.addLast(TEST_VALUE);
        assertThrows(IllegalStateException.class, () -> deque.addLast(TEST_VALUE));
    }

    @Test
    void testOfferLast() {
        assertTrue(deque.offerLast(TEST_VALUE));
        assertFalse(deque.offerLast(TEST_VALUE));
    }

    @Test
    void testRemoveFirst() {
        deque.add(TEST_VALUE);
        assertSame(TEST_VALUE, deque.removeFirst());
        assertThrows(NoSuchElementException.class, () -> deque.removeFirst());
    }

    @Test
    void testRemoveLast() {
        deque.add(TEST_VALUE);
        assertSame(TEST_VALUE, deque.removeLast());
        assertThrows(NoSuchElementException.class, () -> deque.removeLast());
    }

    @Test
    void testPeekFirst() {
        assertNull(deque.peekFirst());
        deque.add(TEST_VALUE);
        assertSame(TEST_VALUE, deque.peekFirst());
        assertSame(TEST_VALUE, deque.peekFirst());
        assertSame(TEST_VALUE, deque.peekFirst());
    }

    @Test
    void testPeekLast() {
        assertNull(deque.peekLast());
        deque.add(TEST_VALUE);
        assertSame(TEST_VALUE, deque.peekLast());
        assertSame(TEST_VALUE, deque.peekLast());
        assertSame(TEST_VALUE, deque.peekLast());
    }

    @Test
    void testRemoveFirstOccurrence() {
        assertFalse(deque.removeFirstOccurrence(null));
        deque.add(TEST_VALUE);
        assertFalse(deque.removeFirstOccurrence(""));
        assertTrue(deque.removeFirstOccurrence(TEST_VALUE));
        assertFalse(deque.removeFirstOccurrence(TEST_VALUE));
    }

    @Test
    void testPush() {
        deque.push(TEST_VALUE);
        assertThrows(IllegalStateException.class, () -> deque.push(TEST_VALUE));
    }

    @Test
    void testPop() {
        deque.add(TEST_VALUE);
        assertSame(TEST_VALUE, deque.pop());
        assertThrows(NoSuchElementException.class, () -> deque.pop());
    }

    @Test
    void testOffer() {
        assertTrue(deque.offer(TEST_VALUE));
        assertFalse(deque.offer(TEST_VALUE));
    }

    @Test
    void testPoll() {
        assertTrue(deque.offer(TEST_VALUE));
        assertSame(TEST_VALUE, deque.poll());
        assertNull(deque.poll());
    }

    @Test
    void testPeek() {
        assertTrue(deque.offer(TEST_VALUE));
        assertSame(TEST_VALUE, deque.peek());
        assertSame(TEST_VALUE, deque.peek());
    }
}