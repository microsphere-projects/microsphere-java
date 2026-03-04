package io.microsphere.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.concurrent.Executor;

import static java.lang.Thread.currentThread;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link AbstractEventDispatcher} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractEventDispatcher
 * @since 1.0.0
 */
class AbstractEventDispatcherTest {

    private static final Executor executor = Runnable::run;

    private AbstractEventDispatcher eventDispatcher;

    @BeforeEach
    void setUp() {
        this.eventDispatcher = new AbstractEventDispatcher(executor) {
        };
    }

    @Test
    void testConstructorOnFailed() {
        assertThrows(IllegalArgumentException.class, () -> new AbstractEventDispatcher(null) {
        });
    }

    @Test
    void testLoadEventListenerInstances() {
        this.eventDispatcher.loadEventListenerInstances();
        List<EventListener<?>> eventListeners = this.eventDispatcher.getAllEventListeners();
        assertFalse(eventListeners.isEmpty());
    }

    @Test
    void testLoadEventListenerInstancesOnFailed() {
        ClassLoader classLoader = currentThread().getContextClassLoader();
        try {
            URLClassLoader newClassLoader = new URLClassLoader(new URL[0], null);
            currentThread().setContextClassLoader(newClassLoader);
            assertDoesNotThrow(this.eventDispatcher::loadEventListenerInstances);
        } finally {
            currentThread().setContextClassLoader(classLoader);
        }
    }

    @Test
    void testGetExecutor() {
        assertSame(executor, this.eventDispatcher.getExecutor());
    }

    @Test
    void testDoInListener() {
        assertDoesNotThrow(() -> this.eventDispatcher.doInListener((Class) null, eventListeners -> {
        }));
    }
}