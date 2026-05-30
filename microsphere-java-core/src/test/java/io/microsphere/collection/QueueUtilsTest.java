package io.microsphere.collection;

import org.junit.jupiter.api.Test;

import java.util.ArrayBlockingQueue;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Comparator;
import java.util.DelayQueue;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedBlockingQueue;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;

import static io.microsphere.collection.EmptyDeque.INSTANCE;
import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.ListUtils.ofLinkedList;
import static io.microsphere.collection.ListUtils.ofList;
import static io.microsphere.collection.QueueUtils.EMPTY_DEQUE;
import static io.microsphere.collection.QueueUtils.emptyDeque;
import static io.microsphere.collection.QueueUtils.emptyQueue;
import static io.microsphere.collection.QueueUtils.isDeque;
import static io.microsphere.collection.QueueUtils.isQueue;
import static io.microsphere.collection.QueueUtils.newArrayBlockingQueue;
import static io.microsphere.collection.QueueUtils.newArrayDeque;
import static io.microsphere.collection.QueueUtils.newConcurrentLinkedQueue;
import static io.microsphere.collection.QueueUtils.newDelayQueue;
import static io.microsphere.collection.QueueUtils.newLinkedBlockingQueue;
import static io.microsphere.collection.QueueUtils.newLinkedTransferQueue;
import static io.microsphere.collection.QueueUtils.newPriorityBlockingQueue;
import static io.microsphere.collection.QueueUtils.newPriorityQueue;
import static io.microsphere.collection.QueueUtils.newSynchronousQueue;
import static io.microsphere.collection.QueueUtils.ofQueue;
import static io.microsphere.collection.QueueUtils.reversedDeque;
import static io.microsphere.collection.QueueUtils.singletonDeque;
import static io.microsphere.collection.QueueUtils.singletonQueue;
import static io.microsphere.collection.QueueUtils.unmodifiableDeque;
import static io.microsphere.collection.QueueUtils.unmodifiableQueue;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static java.util.Arrays.asList;
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
    void testOfQueue() {
        Queue<String> queue = ofQueue("a", "b", "c");
        assertNotNull(queue);
        assertEquals(3, queue.size());
        assertTrue(queue.containsAll(asList("a", "b", "c")));
    }

    @Test
    void testNewArrayDeque() {
        ArrayDeque<String> deque = newArrayDeque();
        assertNotNull(deque);
    }

    @Test
    void testNewArrayDequeWithCapacity() {
        ArrayDeque<String> deque = newArrayDeque(10);
        assertNotNull(deque);
    }

    @Test
    void testNewArrayDequeWithElements() {
        ArrayDeque<String> deque = newArrayDeque("a", "b", "c");
        assertNotNull(deque);
        assertEquals(3, deque.size());
        assertTrue(deque.containsAll(asList("a", "b", "c")));
    }

    @Test
    void testNewArrayDequeWithCollection() {
        Collection<String> source = asList("a", "b", "c");
        ArrayDeque<String> deque = newArrayDeque(source);
        assertNotNull(deque);
        assertEquals(3, deque.size());
        assertEquals("a", deque.getFirst());
        assertEquals("c", deque.getLast());
    }

    @Test
    void testNewArrayDequeWithSet() {
        HashSet<String> source = new HashSet<>(asList("a", "b", "c"));
        ArrayDeque<String> deque = newArrayDeque(source);
        assertNotNull(deque);
        assertEquals(3, deque.size());
        assertTrue(deque.contains("a"));
        assertTrue(deque.contains("b"));
        assertTrue(deque.contains("c"));
    }

    @Test
    void testNewPriorityQueueEmpty() {
        PriorityQueue<Integer> queue = newPriorityQueue();
        assertNotNull(queue);
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    void testNewPriorityQueueWithCapacity() {
        PriorityQueue<Integer> queue = newPriorityQueue(10);
        assertNotNull(queue);
        assertTrue(queue.isEmpty());
    }

    @Test
    void testNewPriorityQueueWithComparator() {
        Comparator<Integer> reverseComparator = (a, b) -> b.compareTo(a);
        PriorityQueue<Integer> queue = newPriorityQueue(reverseComparator);
        queue.add(3);
        queue.add(1);
        queue.add(2);
        assertEquals(3, queue.poll());  // Reverse order
        assertEquals(2, queue.poll());
        assertEquals(1, queue.poll());
    }

    @Test
    void testNewPriorityQueueWithCollection() {
        Collection<Integer> source = asList(3, 1, 2);
        PriorityQueue<Integer> queue = newPriorityQueue(source);
        assertNotNull(queue);
        assertEquals(3, queue.size());
        assertEquals(1, queue.poll());  // Natural order
        assertEquals(2, queue.poll());
        assertEquals(3, queue.poll());
    }

    @Test
    void testNewConcurrentLinkedQueueEmpty() {
        ConcurrentLinkedQueue<String> queue = newConcurrentLinkedQueue();
        assertNotNull(queue);
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    void testNewConcurrentLinkedQueueWithCollection() {
        Collection<String> source = asList("a", "b", "c");
        ConcurrentLinkedQueue<String> queue = newConcurrentLinkedQueue(source);
        assertNotNull(queue);
        assertEquals(3, queue.size());
        assertTrue(queue.contains("a"));
        assertTrue(queue.contains("b"));
        assertTrue(queue.contains("c"));
    }

    @Test
    void testNewLinkedBlockingQueueEmpty() {
        LinkedBlockingQueue<String> queue = newLinkedBlockingQueue();
        assertNotNull(queue);
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    void testNewLinkedBlockingQueueWithCapacity() {
        LinkedBlockingQueue<String> queue = newLinkedBlockingQueue(5);
        assertNotNull(queue);
        assertTrue(queue.isEmpty());
    }

    @Test
    void testNewLinkedBlockingQueueWithCollection() {
        Collection<String> source = asList("a", "b", "c");
        LinkedBlockingQueue<String> queue = newLinkedBlockingQueue(source);
        assertNotNull(queue);
        assertEquals(3, queue.size());
        assertEquals("a", queue.poll());
        assertEquals("b", queue.poll());
        assertEquals("c", queue.poll());
    }

    @Test
    void testNewArrayBlockingQueueWithCapacity() {
        ArrayBlockingQueue<String> queue = newArrayBlockingQueue(10);
        assertNotNull(queue);
        assertTrue(queue.isEmpty());
    }

    @Test
    void testNewArrayBlockingQueueWithCollection() {
        Collection<String> source = asList("a", "b", "c");
        ArrayBlockingQueue<String> queue = newArrayBlockingQueue(10, source);
        assertNotNull(queue);
        assertEquals(3, queue.size());
        assertTrue(queue.contains("a"));
        assertTrue(queue.contains("b"));
        assertTrue(queue.contains("c"));
    }

    @Test
    void testNewPriorityBlockingQueueEmpty() {
        PriorityBlockingQueue<Integer> queue = newPriorityBlockingQueue();
        assertNotNull(queue);
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    void testNewPriorityBlockingQueueWithCollection() {
        Collection<Integer> source = asList(3, 1, 2);
        PriorityBlockingQueue<Integer> queue = newPriorityBlockingQueue(source);
        assertNotNull(queue);
        assertEquals(3, queue.size());
        assertEquals(1, queue.poll());  // Natural order
        assertEquals(2, queue.poll());
        assertEquals(3, queue.poll());
    }

    @Test
    void testNewDelayQueueEmpty() {
        DelayQueue<TestDelayed> queue = newDelayQueue();
        assertNotNull(queue);
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    void testNewSynchronousQueueEmpty() {
        SynchronousQueue<String> queue = newSynchronousQueue();
        assertNotNull(queue);
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    void testNewLinkedTransferQueueEmpty() {
        LinkedTransferQueue<String> queue = newLinkedTransferQueue();
        assertNotNull(queue);
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    void testNewLinkedTransferQueueWithCollection() {
        Collection<String> source = asList("a", "b", "c");
        LinkedTransferQueue<String> queue = newLinkedTransferQueue(source);
        assertNotNull(queue);
        assertEquals(3, queue.size());
        assertTrue(queue.contains("a"));
        assertTrue(queue.contains("b"));
        assertTrue(queue.contains("c"));
    }

    // Test helper class for DelayQueue testing
    static class TestDelayed implements Delayed {
        @Override
        public long getDelay(java.util.concurrent.TimeUnit unit) {
            return 0;
        }

        @Override
        public int compareTo(Delayed o) {
            return 0;
        }
    }
}
