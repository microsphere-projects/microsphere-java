package io.microsphere.logging;

/**
 * {@link Sfl4jLoggerFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Sfl4jLoggerFactory
 * @since 1.0.0
 */
class Sfl4jLoggerFactoryTest extends AbstractLoggerTest {

    @Override
    protected Logger createLogger() {
        return new Sfl4jLoggerFactory().createLogger(Sfl4jLoggerFactoryTest.class.getName());
    }
}