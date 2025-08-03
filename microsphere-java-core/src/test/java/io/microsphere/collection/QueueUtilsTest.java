package io.microsphere.collection;


import org.junit.jupiter.api.Test;

import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import static io.microsphere.collection.EmptyDeque.INSTANCE;
import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.ListUtils.ofLinkedList;
import static io.microsphere.collection.ListUtils.ofList;
import static io.microsphere.collection.QueueUtils.EMPTY_DEQUE;
import static io.microsphere.collection.QueueUtils.emptyDeque;
import static io.microsphere.collection.QueueUtils.emptyQueue;
import static io.microsphere.collection.QueueUtils.isDeque;
import static io.microsphere.collection.QueueUtils.isQueue;
import static io.microsphere.collection.QueueUtils.newArrayDeque;
import static io.microsphere.collection.QueueUtils.reversedDeque;
import static io.microsphere.collection.QueueUtils.singletonDeque;
import static io.microsphere.collection.QueueUtils.singletonQueue;
import static io.microsphere.collection.QueueUtils.unmodifiableDeque;
import static io.microsphere.collection.QueueUtils.unmodifiableQueue;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
class QueueUtilsTest {

    @Test
    void testConstants() {
        assertSame(EMPTY_DEQUE, INSTANCE);
    }

    @Test
    void testIsQueueWithObject() {
        assertTrue(isQueue(emptyQueue()));
        assertTrue(isQueue(emptyDeque()));
        assertFalse(isQueue((Object) null));
        assertTrue(isQueue(unmodifiableQueue(emptyDeque())));
        assertFalse(isQueue(emptyList()));
    }

    @Test
    void testIsQueueWithType() {
        assertTrue(isQueue(Queue.class));
        assertTrue(isQueue(Deque.class));

        assertFalse(isQueue(Object.class));
        assertFalse(isQueue((null)));
    }

    @Test
    void testIsDeque() {
        assertTrue(isDeque(emptyQueue()));
        assertTrue(isDeque(emptyDeque()));
        assertFalse(isDeque(null));
        assertTrue(isDeque(newLinkedList()));
        assertTrue(isDeque(singletonQueue("a")));
        assertTrue(isDeque(singletonDeque("a")));
    }

    @Test
    void testEmptyDeque() {
        Deque<String> deque = emptyDeque();
        assertTrue(deque.isEmpty());
        assertSame(emptyIterator(), deque.iterator());
        assertSame(emptyIterator(), deque.descendingIterator());

        assertThrows(UnsupportedOperationException.class, () -> deque.offerFirst("a"));
        assertThrows(UnsupportedOperationException.class, () -> deque.offerLast("a"));
        assertNull(deque.pollFirst());
        assertNull(deque.pollLast());
        assertThrows(NoSuchElementException.class, deque::getFirst);
        assertThrows(NoSuchElementException.class, deque::getLast);

        assertFalse(deque.removeFirstOccurrence("a"));
        assertEquals(0, deque.size());
    }

    @Test
    void testUnmodifiableQueue() {
        Queue<String> queue = unmodifiableQueue(emptyDeque());
        assertUnmodifiableQueue(queue);
    }

    @Test
    void testUnmodifiableDeque() {
        Deque<String> deque = unmodifiableDeque(emptyDeque());
        assertUnmodifiableDeque(deque);
    }

    @Test
    void testSingletonDeque() {
        SingletonDeque<String> singletonDeque = (SingletonDeque<String>) singletonDeque("a");
        assertSingletonIterator(singletonDeque.iterator());
        assertSingletonIterator(singletonDeque.descendingIterator());

        assertThrows(UnsupportedOperationException.class, () -> singletonDeque.addFirst("a"));
        assertThrows(UnsupportedOperationException.class, () -> singletonDeque.addLast("a"));
        assertThrows(UnsupportedOperationException.class, singletonDeque::removeFirst);
        assertThrows(UnsupportedOperationException.class, singletonDeque::removeLast);
        assertEquals("a", singletonDeque.peekFirst());
        assertEquals("a", singletonDeque.peekLast());
        assertThrows(UnsupportedOperationException.class, () -> singletonDeque.removeFirstOccurrence("a"));
        assertThrows(UnsupportedOperationException.class, () -> singletonDeque.push("a"));
        assertThrows(UnsupportedOperationException.class, singletonDeque::pop);
        assertThrows(UnsupportedOperationException.class, singletonDeque::pop);
        assertThrows(UnsupportedOperationException.class, () -> singletonDeque.offer("a"));
        assertThrows(UnsupportedOperationException.class, () -> singletonDeque.offerFirst("a"));
        assertThrows(UnsupportedOperationException.class, () -> singletonDeque.offerLast("a"));
        assertThrows(UnsupportedOperationException.class, singletonDeque::poll);
        assertEquals("a", singletonDeque.peek());

        assertThrows(UnsupportedOperationException.class, singletonDeque::pollFirst);
        assertThrows(UnsupportedOperationException.class, singletonDeque::pollLast);

        assertEquals("a", singletonDeque.getFirst());
        assertEquals("a", singletonDeque.getLast());


        assertEquals(1, singletonDeque.size());
    }

    @Test
    void testReversedQueue() {
        Deque<String> deque = ofLinkedList("A", "B", "C");
        Deque<String> reversedDeque = reversedDeque(deque);
        assertTrue(reversedDeque.equals(ofList("C", "B", "A")));
    }

    private static void assertSingletonIterator(Iterator<String> it) {
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertThrows(NoSuchElementException.class, () -> it.next());
    }

    private static void assertUnmodifiableDeque(Deque<String> deque) {
        assertUnmodifiableQueue(deque);
        assertThrows(UnsupportedOperationException.class, () -> deque.addFirst("a"));
        assertThrows(UnsupportedOperationException.class, () -> deque.addLast("a"));
        assertThrows(UnsupportedOperationException.class, () -> deque.offerFirst("a"));
        assertThrows(UnsupportedOperationException.class, () -> deque.offerLast("a"));
        assertThrows(UnsupportedOperationException.class, deque::removeFirst);
        assertThrows(UnsupportedOperationException.class, deque::removeLast);
        assertThrows(UnsupportedOperationException.class, deque::pollFirst);
        assertThrows(UnsupportedOperationException.class, deque::pollLast);
        assertThrows(NoSuchElementException.class, deque::getFirst);
        assertThrows(NoSuchElementException.class, deque::getLast);
        assertNull(deque.peekFirst());
        assertNull(deque.peekLast());
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
        assertThrows(NoSuchElementException.class, iterator::next);
        assertThrows(IllegalStateException.class, iterator::remove);

        Object[] array = queue.toArray();
        assertEquals(0, array.length);

        String[] values = queue.toArray(EMPTY_STRING_ARRAY);
        assertEquals(0, values.length);

        assertThrows(UnsupportedOperationException.class, () -> queue.add("a"));
        assertThrows(UnsupportedOperationException.class, () -> queue.remove("a"));
        assertThrows(UnsupportedOperationException.class, () -> queue.offer("a"));
        assertThrows(UnsupportedOperationException.class, () -> queue.remove());
        assertThrows(UnsupportedOperationException.class, () -> queue.poll());
        assertThrows(NoSuchElementException.class, queue::element);
        assertNull(queue.peek());

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
  
    @Test
    void testNewArrayDeque() {
        Deque<String> deque = newArrayDeque();
        assertNotNull(deque);
    }

    @Test
    void testNewArrayDequeWithCapacity() {
        Deque<String> deque = newArrayDeque(10);
        assertNotNull(deque);
    }

}