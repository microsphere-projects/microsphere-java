package io.microsphere.concurrent;

import io.microsphere.util.ArrayUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static io.microsphere.collection.ListUtils.ofList;
import static io.microsphere.collection.QueueUtils.emptyQueue;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

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
        assertNotEquals(emptyQueue(), queue);

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
        assertEquals(0, queue.drainTo(Arrays.asList(1)));
        assertEquals(0, queue.drainTo(Arrays.asList(1, 2, 3, 4, 5), 3));

        // test spliterator
        queue.spliterator();
    }

    @Test
    void stream() {
    }

    @Test
    void parallelStream() {
    }


    @Test
    void drainTo() {
    }

    @Test
    void testDrainTo() {
    }

    @Test
    void forEach() {
    }
}