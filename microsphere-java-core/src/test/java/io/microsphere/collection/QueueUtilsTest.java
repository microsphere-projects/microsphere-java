package io.microsphere.collection;


import org.junit.jupiter.api.Test;

import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.QueueUtils.emptyDeque;
import static io.microsphere.collection.QueueUtils.emptyQueue;
import static io.microsphere.collection.QueueUtils.isDeque;
import static io.microsphere.collection.QueueUtils.isQueue;
import static io.microsphere.collection.QueueUtils.singletonDeque;
import static io.microsphere.collection.QueueUtils.singletonQueue;
import static io.microsphere.collection.QueueUtils.unmodifiableDeque;
import static io.microsphere.collection.QueueUtils.unmodifiableQueue;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link QueueUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see QueueUtils
 * @since 1.0.0
 */
public class QueueUtilsTest {

    @Test
    public void testIsQueue() {
        assertTrue(isQueue(emptyQueue()));
        assertTrue(isQueue(emptyDeque()));
        assertFalse(isQueue(null));
        assertTrue(isQueue(unmodifiableQueue(emptyDeque())));
        assertFalse(isQueue(emptyList()));
    }

    @Test
    public void testIsDeque() {
        assertTrue(isDeque(emptyQueue()));
        assertTrue(isDeque(emptyDeque()));
        assertFalse(isDeque(null));
        assertTrue(isDeque(newLinkedList()));
        assertTrue(isDeque(singletonQueue("a")));
        assertTrue(isDeque(singletonDeque("a")));
    }

    @Test
    public void testEmptyDeque() {
        Deque<String> deque = emptyDeque();
        assertTrue(deque.isEmpty());
        assertSame(emptyIterator(), deque.iterator());
        assertSame(emptyIterator(), deque.descendingIterator());

        assertThrows(UnsupportedOperationException.class, () -> deque.offerFirst("a"));
        assertThrows(UnsupportedOperationException.class, () -> deque.offerLast("a"));
        assertThrows(UnsupportedOperationException.class, () -> deque.pollFirst());
        assertThrows(UnsupportedOperationException.class, () -> deque.pollLast());
        assertThrows(UnsupportedOperationException.class, () -> deque.getFirst());
        assertThrows(UnsupportedOperationException.class, () -> deque.getLast());

        assertFalse(deque.removeFirstOccurrence("a"));
        assertEquals(0, deque.size());
    }

    @Test
    public void testUnmodifiableQueue() {
        Queue<String> queue = unmodifiableQueue(emptyDeque());
        assertUnmodifiableQueue(queue);
    }

    @Test
    public void testUnmodifiableDeque() {
        Deque<String> deque = unmodifiableDeque(emptyDeque());
        assertUnmodifiableQueue(deque);
        assertUnmodifiableDeque(deque);
    }

    @Test
    public void testSingletonDeque() {
        Deque<String> deque = singletonDeque("a");
        assertSingletonIterator(deque.iterator());
        assertSingletonIterator(deque.descendingIterator());
        assertFalse(deque.offerFirst("b"));
        assertFalse(deque.offerLast("b"));

        assertThrows(UnsupportedOperationException.class, deque::pollFirst);
        assertThrows(UnsupportedOperationException.class, deque::pollLast);

        assertEquals("a", deque.getFirst());
        assertEquals("a", deque.getLast());

        assertThrows(UnsupportedOperationException.class, () -> deque.removeLastOccurrence("b"));

        assertEquals(1, deque.size());
    }

    private static void assertSingletonIterator(Iterator<String> it) {
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertThrows(NoSuchElementException.class, () -> it.next());
    }

    private static void assertUnmodifiableDeque(Deque<String> deque) {
        assertThrows(UnsupportedOperationException.class, () -> deque.addFirst("a"));
        assertThrows(UnsupportedOperationException.class, () -> deque.addLast("a"));
        assertThrows(UnsupportedOperationException.class, () -> deque.offerFirst("a"));
        assertThrows(UnsupportedOperationException.class, () -> deque.offerLast("a"));
        assertThrows(UnsupportedOperationException.class, deque::removeFirst);
        assertThrows(UnsupportedOperationException.class, deque::removeLast);
        assertThrows(UnsupportedOperationException.class, deque::pollFirst);
        assertThrows(UnsupportedOperationException.class, deque::pollLast);
        assertThrows(UnsupportedOperationException.class, deque::getFirst);
        assertThrows(UnsupportedOperationException.class, deque::getLast);
        assertThrows(UnsupportedOperationException.class, deque::peekFirst);
        assertThrows(UnsupportedOperationException.class, deque::peekLast);
        assertThrows(UnsupportedOperationException.class, () -> deque.removeFirstOccurrence(null));
        assertThrows(UnsupportedOperationException.class, () -> deque.removeLastOccurrence(null));
        assertThrows(UnsupportedOperationException.class, () -> deque.push("a"));
        assertThrows(UnsupportedOperationException.class, deque::pop);

        assertNotNull(deque.descendingIterator());
    }

    private static void assertUnmodifiableQueue(Queue<String> queue) {
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
        assertFalse(queue.contains("a"));
        Iterator<String> iterator = queue.iterator();
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, () -> iterator.next());
        assertThrows(UnsupportedOperationException.class, () -> iterator.remove());

        Object[] array = queue.toArray();
        assertEquals(0, array.length);

        String[] values = queue.toArray(new String[0]);
        assertEquals(0, values.length);

        assertThrows(UnsupportedOperationException.class, () -> queue.add("a"));
        assertThrows(UnsupportedOperationException.class, () -> queue.remove("a"));
        assertThrows(UnsupportedOperationException.class, () -> queue.offer("a"));
        assertThrows(UnsupportedOperationException.class, () -> queue.remove());
        assertThrows(UnsupportedOperationException.class, () -> queue.poll());
        assertThrows(UnsupportedOperationException.class, () -> queue.element());
        assertThrows(UnsupportedOperationException.class, () -> queue.peek());

        assertTrue(queue.containsAll(emptyList()));

        assertThrows(UnsupportedOperationException.class, () -> queue.addAll(emptyList()));
        assertThrows(UnsupportedOperationException.class, () -> queue.removeAll(emptyList()));
        assertThrows(UnsupportedOperationException.class, () -> queue.removeIf(a -> true));
        assertThrows(UnsupportedOperationException.class, () -> queue.retainAll(emptyList()));
        assertThrows(UnsupportedOperationException.class, () -> queue.clear());

        assertTrue(queue.equals(queue));
        assertTrue(queue.equals(emptyQueue()));
        assertTrue(queue.hashCode() > 0);

        assertNotNull(queue.spliterator());
        assertNotNull(queue.stream());
        assertNotNull(queue.parallelStream());
        queue.forEach(e -> {
        });
    }

}