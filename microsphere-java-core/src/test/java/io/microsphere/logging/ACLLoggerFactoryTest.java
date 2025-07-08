package io.microsphere.logging;

/**
 * {@link ACLLoggerFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ACLLoggerFactory
 * @since 1.0.0
 */
class ACLLoggerFactoryTest extends AbstractLoggerTest {

    @Override
    protected Logger createLogger() {
        return new ACLLoggerFactory().createLogger(ACLLoggerFactory.class.getName());
    }
}