package io.microsphere.logging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link NoOpLogger} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractLogger
 * @since 1.0.0
 */
public class NoOpLoggerTest extends AbstractLoggerTest {

    @Override
    protected AbstractLogger createLogger() {
        return new NoOpLogger("No-OP");
    }
}