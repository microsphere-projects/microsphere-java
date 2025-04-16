package io.microsphere.collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.Lists.ofList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DelegatingDeque} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractDeque
 * @since 1.0.0
 */
public class DelegatingDequeTest {

    private AbstractDeque<String> deque;

    @BeforeEach
    public void init() {
        this.deque = new DelegatingDeque(newLinkedList(ofList("a")));
    }

    @Test
    public void testAddFirst() {
        assertThrows(IllegalStateException.class, () -> deque.addFirst("test"));
    }

    @Test
    public void testAddLast() {
        assertThrows(IllegalStateException.class, () -> deque.addLast("test"));
    }

    @Test
    public void testRemoveFirst() {
        assertEquals("a", deque.removeFirst());
        assertThrows(NoSuchElementException.class, deque::removeFirst);
    }

    @Test
    public void testRemoveLast() {
        assertEquals("a", deque.removeLast());
        assertThrows(NoSuchElementException.class, deque::removeLast);
    }

    @Test
    public void testPeekFirst() {
        assertEquals("a", deque.peekFirst());
    }

    @Test
    public void testPeekLast() {
        assertEquals("a", deque.peekLast());
    }

    @Test
    public void testRemoveFirstOccurrence() {
        assertTrue(deque.removeFirstOccurrence("a"));
    }

    @Test
    public void testPush() {
        assertThrows(IllegalStateException.class, () -> deque.push("b"));
    }

    @Test
    public void testPop() {
        assertEquals("a", deque.pop());
        assertThrows(NoSuchElementException.class, deque::pop);
    }

    @Test
    public void testOffer() {
        assertFalse(deque.offer("b"));
    }

    @Test
    public void testPoll() {
        assertEquals("a", deque.poll());
    }

    @Test
    public void testPeek() {
        assertEquals("a", deque.peek());
    }
}