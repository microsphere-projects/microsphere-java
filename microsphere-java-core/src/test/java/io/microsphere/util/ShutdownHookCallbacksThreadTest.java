package io.microsphere.util;

import org.junit.jupiter.api.Test;

import static io.microsphere.util.ShutdownHookCallbacksThread.INSTANCE;
import static io.microsphere.util.ShutdownHookUtils.addShutdownHookCallback;

/**
 * {@link ShutdownHookCallbacksThread} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ShutdownHookCallbacksThread
 * @since 1.0.0
 */
public class ShutdownHookCallbacksThreadTest {

    @Test
    public void testRun() {
        ShutdownHookCallbacksThread thread = INSTANCE;

        int times = 3;
        for (int i = 0; i < times; i++) {
            addShutdownHookCallback(new ShutdownHookUtilsTest.ShutdownHookCallback(i));
        }

        thread.run();

    }
}