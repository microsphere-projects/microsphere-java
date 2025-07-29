package io.microsphere.logging;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.microsphere.constants.SymbolConstants.SPACE;
import static io.microsphere.logging.LoggerFactory.loadAvailableFactories;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link NoOpLoggerFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see NoOpLoggerFactory
 * @since 1.0.0
 */
class NoOpLoggerFactoryTest {

    private static NoOpLoggerFactory factory;

    @BeforeAll
    static void beforeAll() {
        List<LoggerFactory> factories = loadAvailableFactories();
        for (LoggerFactory f : factories) {
            if (f instanceof NoOpLoggerFactory) {
                factory = (NoOpLoggerFactory) f;
            }
        }
    }

    @Test
    void testGetDelegateLoggerClassName() {
        assertThrows(UnsupportedOperationException.class, factory::getDelegateLoggerClassName);
    }

    @Test
    void testIsAvailable() {
        assertTrue(factory.isAvailable());
    }

    @Test
    void testCreateLogger() {
        Logger logger = factory.createLogger("test");
        assertEquals("test", logger.getName());

        logger = factory.createLogger("");
        assertEquals("", logger.getName());

        logger = factory.createLogger(SPACE);
        assertEquals("", logger.getName());

        logger = factory.createLogger(null);
        assertEquals("", logger.getName());
    }
}