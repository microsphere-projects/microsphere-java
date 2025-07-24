package io.microsphere.logging;

import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.LogManager;

/**
 * {@link JDKLoggerFactory}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see JDKLoggerFactory
 * @since 1.0.0
 */
class JDKLoggerFactoryTest extends AbstractLoggerTest {

    @BeforeAll
    public static void beforeAll() throws IOException {
        URL resource = LoggerFactoryTest.class.getResource("/META-INF/logging.properties");
        try (InputStream inputStream = resource.openStream()) {
            LogManager.getLogManager().readConfiguration(inputStream);
        }
    }

    @Override
    protected Logger createLogger() {
        return new JDKLoggerFactory().createLogger(JDKLoggerFactoryTest.class.getName());
    }
}