package io.microsphere.concurrent;

import io.microsphere.AbstractTestCase;
import io.microsphere.junit.jupiter.api.extension.annotation.UtilsTestExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import static io.microsphere.concurrent.CustomizedThreadFactory.newThreadFactory;
import static io.microsphere.concurrent.ExecutorUtils.shutdown;
import static io.microsphere.concurrent.ExecutorUtils.shutdownOnExit;
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
@UtilsTestExtension
public class ExecutorUtilsTest extends AbstractTestCase {

    private ExecutorService executorService;

    @BeforeEach
    public void init() {
        executorService = newSingleThreadExecutor(newThreadFactory("ExecutorUtilsTest-", true));
        executorService.execute(() -> log("Running..."));
    }

    @Test
    public void testShutdownOnExit() {
        shutdownOnExit(executorService, executorService, executorService);
    }

    @Test
    public void testShutdownForExecutor() {
        assertTrue(shutdown((Executor) executorService));
        assertTrue(shutdown((Executor) executorService));
    }

    @Test
    public void testShutdownForExecutorOnNull() {
        assertFalse(shutdown((Executor) null));
    }

    @Test
    public void testShutdownForExecutorService() {
        assertTrue(shutdown(executorService));
        assertTrue(shutdown(executorService));
    }

    @Test
    public void testShutdownForExecutorServiceOnNull() {
        assertFalse(shutdown(null));
    }
}