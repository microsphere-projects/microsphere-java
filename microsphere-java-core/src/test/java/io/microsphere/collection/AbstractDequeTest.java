package io.microsphere.collection;

import org.junit.jupiter.api.Test;

import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.Lists.ofList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AbstractDeque} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractDeque
 * @since 1.0.0
 */
public class AbstractDequeTest {

    private final AbstractDeque<String> deque = new AbstractDeque<String>() {

        private final Deque<String> values = newLinkedList(ofList("a"));

        @Override
        public Iterator<String> iterator() {
            return values.iterator();
        }

        @Override
        public Iterator<String> descendingIterator() {
            return values.descendingIterator();
        }

        @Override
        public boolean offerFirst(String s) {
            return false;
        }

        @Override
        public boolean offerLast(String s) {
            return false;
        }

        @Override
        public String pollFirst() {
            return values.pollFirst();
        }

        @Override
        public String pollLast() {
            return values.pollLast();
        }

        @Override
        public String getFirst() {
            return values.getFirst();
        }

        @Override
        public String getLast() {
            return values.getLast();
        }

        @Override
        public boolean removeLastOccurrence(Object o) {
            return values.removeLastOccurrence(o);
        }

        @Override
        public int size() {
            return 0;
        }
    };

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