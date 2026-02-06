package io.microsphere.concurrent;

import io.microsphere.AbstractTestCase;
import io.microsphere.util.ShutdownHookUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;

import static io.microsphere.concurrent.CustomizedThreadFactory.newThreadFactory;
import static io.microsphere.concurrent.ExecutorUtils.shutdown;
import static io.microsphere.concurrent.ExecutorUtils.shutdownOnExit;
import static io.microsphere.reflect.FieldUtils.getStaticFieldValue;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ExecutorUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ExecutorUtils
 * @since 1.0.0
 */
class ExecutorUtilsTest extends AbstractTestCase {

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        this.executorService = newSingleThreadExecutor(newThreadFactory("ExecutorUtilsTest-", true));
        this.executorService.execute(() -> log("Running..."));
    }

    @Test
    void testShutdownOnExit() {
        shutdownOnExit(this.executorService, this.executorService, this.executorService);
        PriorityBlockingQueue<Runnable> shutdownHookCallbacks = getStaticFieldValue(ShutdownHookUtils.class, "shutdownHookCallbacks");
        Runnable callback = shutdownHookCallbacks.poll();
        callback.run();
        assertTrue(this.executorService.isShutdown());
    }

    @Test
    void testShutdownForExecutor() {
        assertTrue(shutdown((Executor) this.executorService));
        assertTrue(shutdown((Executor) this.executorService));
    }

    @Test
    void testShutdownForExecutorOnNull() {
        assertFalse(shutdown((Executor) null));
    }

    @Test
    void testShutdownForExecutorService() {
        assertTrue(shutdown(this.executorService));
        assertTrue(shutdown(this.executorService));
    }

    @Test
    void testShutdownForExecutorServiceOnNull() {
        assertFalse(shutdown(null));
    }
}