package io.microsphere.logging;

/**
 * {@link ACLLoggerFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ACLLoggerFactory
 * @since 1.0.0
 */
public class ACLLoggerFactoryTest extends AbstractLoggerTest {

    @Override
    protected Logger createLogger() {
        ACLLoggerFactory factory = new ACLLoggerFactory();
        return factory.createLogger(ACLLoggerFactory.class.getName());
    }
}