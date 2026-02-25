package io.microsphere.concurrent;

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static io.microsphere.collection.Lists.ofList;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DelegatingScheduledExecutorService} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see DelegatingScheduledExecutorService
 * @since 1.0.0
 */
class DelegatingScheduledExecutorServiceTest extends AbstractTestCase {

    private ScheduledExecutorService delegate = newSingleThreadScheduledExecutor();

    private DelegatingScheduledExecutorService instance = new DelegatingScheduledExecutorService(delegate);

    @Test
    void test() throws Throwable {
        // test getDelegate
        assertSame(delegate, instance.getDelegate());

        // test setDelegate
        instance.setDelegate(delegate);
        assertSame(delegate, instance.getDelegate());

        // test schedule(Runnable...)
        Future future = instance.schedule(() -> {
            log("schedule");
        }, 1, MILLISECONDS);

        assertNull(future.get());

        // test schedule(Callable...)
        future = instance.schedule(() -> "Hello,World", 1, MILLISECONDS);
        assertEquals("Hello,World", future.get());

        // test scheduleAtFixedRate
        future = instance.scheduleAtFixedRate(() -> {
            log("scheduleAtFixedRate");
        }, 1, 1, MILLISECONDS);

        future.cancel(true);

        // test scheduleWithFixedDelay
        future = instance.scheduleWithFixedDelay(() -> {
            log("scheduleWithFixedDelay");
        }, 1, 1, MILLISECONDS);


        // test submit
        future = instance.submit(() -> {
            log("submit");
        });
        future.get();

        future = instance.submit(() -> {
        }, "Hello,World");
        assertEquals("Hello,World", future.get());

        future = instance.submit(() -> "Hello,World");
        assertEquals("Hello,World", future.get());

        // test invokeAll
        List<Future<String>> futures = instance.invokeAll(ofList(() -> "Hello,World"));
        assertEquals(1, futures.size());
        assertEquals("Hello,World", futures.get(0).get());

        futures = instance.invokeAll(ofList(() -> "Hello,World"), 100, MILLISECONDS);
        assertEquals(1, futures.size());
        assertEquals("Hello,World", futures.get(0).get());

        // test invokeAny
        assertEquals("Hello,World", instance.invokeAny(ofList(() -> "Hello,World")));
        assertEquals("Hello,World", instance.invokeAny(ofList(() -> "Hello,World"), 5, MILLISECONDS));

        // test execute
        instance.execute(() -> {
            log("execute");
        });

        // test shutdownNow
        assertFalse(instance.shutdownNow().isEmpty());

        // test shutdown
        instance.shutdown();

        // test awaitTermination
        assertTrue(instance.awaitTermination(10, MILLISECONDS));

        // test isShutdown
        assertTrue(instance.isShutdown());

        // test isTerminated
        assertTrue(instance.isTerminated());

    }
}