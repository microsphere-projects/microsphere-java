package io.microsphere.logging;

/**
 * {@link NoOpLogger} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractLogger
 * @since 1.0.0
 */
class NoOpLoggerTest extends AbstractLoggerTest {

    @Override
    protected Logger createLogger() {
        return new NoOpLogger("No-OP");
    }
}