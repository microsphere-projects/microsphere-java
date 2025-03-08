package io.microsphere.concurrent;

import io.microsphere.logging.Logger;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.logging.LoggerFactory.getLogger;
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
public class DelegatingScheduledExecutorServiceTest {

    private static final Logger logger = getLogger(DelegatingScheduledExecutorServiceTest.class);

    private ScheduledExecutorService delegate = newSingleThreadScheduledExecutor();

    private DelegatingScheduledExecutorService instance = new DelegatingScheduledExecutorService(delegate);

    @Test
    public void test() throws Throwable {
        // test getDelegate
        assertSame(delegate, instance.getDelegate());

        // test setDelegate
        instance.setDelegate(delegate);
        assertSame(delegate, instance.getDelegate());

        // test schedule(Runnable...)
        Future future = instance.schedule(() -> {
            logger.debug("schedule");
        }, 1, MILLISECONDS);

        assertNull(future.get());

        // test schedule(Callable...)
        future = instance.schedule(() -> "Hello,World", 1, MILLISECONDS);
        assertEquals("Hello,World", future.get());

        // test scheduleAtFixedRate
        future = instance.scheduleAtFixedRate(() -> {
            logger.debug("scheduleAtFixedRate");
        }, 1, 1, MILLISECONDS);

        future.cancel(true);

        // test scheduleWithFixedDelay
        future = instance.scheduleWithFixedDelay(() -> {
            logger.debug("scheduleWithFixedDelay");
        }, 1, 1, MILLISECONDS);


        // test submit
        future = instance.submit(() -> {
            logger.debug("submit");
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

        futures = instance.invokeAll(ofList(() -> "Hello,World"), 1, MILLISECONDS);
        assertEquals(1, futures.size());
        assertEquals("Hello,World", futures.get(0).get());

        // test invokeAny
        assertEquals("Hello,World", instance.invokeAny(ofList(() -> "Hello,World")));
        assertEquals("Hello,World", instance.invokeAny(ofList(() -> "Hello,World"), 1, MILLISECONDS));

        // test execute
        instance.execute(() -> {
            logger.debug("execute");
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