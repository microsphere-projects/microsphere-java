package io.microsphere.concurrent;

import io.microsphere.util.ArrayUtils;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Stream;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.ListUtils.ofList;
import static io.microsphere.collection.QueueUtils.emptyQueue;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DelegatingBlockingQueue} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see DelegatingBlockingQueue
 * @since 1.0.0
 */
public class DelegatingBlockingQueueTest {

    private BlockingQueue<Integer> delegate = new LinkedBlockingDeque<>();

    private DelegatingBlockingQueue<Integer> queue = new DelegatingBlockingQueue<>(delegate);

    @Test
    public void test() throws Throwable {

        // test add
        assertTrue(queue.add(1));

        // test size
        assertEquals(1, queue.size());

        // test isEmpty
        assertFalse(queue.isEmpty());

        // test toArray()
        Object[] array = queue.toArray();
        assertArrayEquals(ArrayUtils.of(1), array);

        // test toArray(Object[])
        array = queue.toArray(new Object[1]);
        assertArrayEquals(ArrayUtils.of(1), array);

        // test contains
        assertTrue(queue.contains(1));

        // test addAll
        assertTrue(queue.addAll(ofList(2, 3)));

        // test removeAll
        assertTrue(queue.removeAll(ofList(2)));

        // test removeIf
        assertTrue(queue.removeIf(i -> i == 3));

        // test retainAll
        assertFalse(queue.retainAll(ofList(1)));

        // test clear
        queue.clear();

        // test equals
        assertEquals(queue, queue);
        assertEquals(queue, delegate);
        assertEquals(queue, new DelegatingBlockingQueue<>(delegate));
        assertNotEquals(queue, emptyQueue());

        // test hashCode
        assertEquals(System.identityHashCode(delegate), queue.hashCode());


        // test offer
        assertTrue(queue.offer(1));
        assertTrue(queue.offer(2, 1, SECONDS));

        // test put
        queue.put(3);

        // test take
        assertEquals(1, queue.take());

        // test peek
        assertEquals(2, queue.peek());

        // test element
        assertEquals(2, queue.element());

        // test poll
        assertEquals(2, queue.poll());
        assertEquals(3, queue.poll(1, SECONDS));

        // test remove
        assertThrows(NoSuchElementException.class, queue::remove);
        assertFalse(queue.remove(1));
        assertFalse(queue.remove(2));
        assertFalse(queue.remove(3));

        // test remainingCapacity
        assertEquals(Integer.MAX_VALUE, queue.remainingCapacity());

        // test drainTo
        queue.offer(1);
        queue.offer(2);
        queue.offer(3);
        List<Integer> values = newLinkedList();
        assertEquals(3, queue.drainTo(values));
        assertTrue(queue.isEmpty());
        assertEquals(ofList(1, 2, 3), values);
        values.clear();

        queue.offer(1);
        queue.offer(2);
        queue.offer(3);
        assertEquals(1, queue.drainTo(values, 1));
        assertEquals(ofList(1), values);

        // test spliterator
        Spliterator<Integer> spliterator = queue.spliterator();
        spliterator.tryAdvance(i -> assertEquals(2, i));
        spliterator.forEachRemaining(i -> assertEquals(3, i));
        queue.clear();

        // test stream()
        queue.offer(1);
        Stream<Integer> stream = queue.stream();
        stream.forEach(i -> assertEquals(1, i));

        // test parallelStream
        Stream<Integer> parallelStream = queue.parallelStream();
        parallelStream.forEach(i -> assertEquals(1, i));

        // test forEach
        queue.forEach(i -> assertEquals(1, i));

        // test iterator
        Iterator<Integer> iterator = queue.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(1, iterator.next());

        // test toString
        assertEquals(queue.toString(), delegate.toString());
    }
}