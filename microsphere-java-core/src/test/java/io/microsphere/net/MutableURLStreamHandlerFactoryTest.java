package io.microsphere.net;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link MutableURLStreamHandlerFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see MutableURLStreamHandlerFactory
 * @since 1.0.0
 */
class MutableURLStreamHandlerFactoryTest {

    MutableURLStreamHandlerFactory factory;

    private static final int OP_ADD = 1;

    private static final int OP_GET = OP_ADD << 1;

    private static final int OP_CREATE = OP_GET << 1;

    private static final int OP_REMOVE = OP_CREATE << 1;

    private static final int OP_CLEAR = OP_REMOVE << 1;

    @BeforeEach
    void init() {
        factory = new MutableURLStreamHandlerFactory();
    }

    @AfterEach
    void destroy() {
        factory.clearHandlers();
    }

    @Test
    void testAddURLStreamHandler() {
        assertURLStreamHandler(new io.microsphere.net.classpath.Handler(), OP_ADD);
        assertURLStreamHandler(new io.microsphere.net.console.Handler(), OP_ADD);
        assertURLStreamHandler(new io.microsphere.net.test.Handler(), OP_ADD);
    }

    @Test
    void testRemoveURLStreamHandler() {
        assertURLStreamHandler(new io.microsphere.net.classpath.Handler(), OP_ADD | OP_REMOVE);
        assertURLStreamHandler(new io.microsphere.net.console.Handler(), OP_ADD | OP_REMOVE);
        assertURLStreamHandler(new io.microsphere.net.test.Handler(), OP_ADD | OP_REMOVE);
    }

    @Test
    void testGetURLStreamHandler() {
        assertURLStreamHandler(new io.microsphere.net.classpath.Handler(), OP_ADD | OP_GET);
        assertURLStreamHandler(new io.microsphere.net.console.Handler(), OP_ADD | OP_GET);
        assertURLStreamHandler(new io.microsphere.net.test.Handler(), OP_ADD | OP_GET);
    }

    @Test
    void testGetHandlers() {
        assertURLStreamHandler(new io.microsphere.net.classpath.Handler(), OP_ADD);
        assertEquals(1, factory.getHandlers().size());

        assertURLStreamHandler(new io.microsphere.net.console.Handler(), OP_ADD);
        assertEquals(2, factory.getHandlers().size());

        assertURLStreamHandler(new io.microsphere.net.test.Handler(), OP_ADD);
        assertEquals(3, factory.getHandlers().size());
    }

    @Test
    void testCreateURLStreamHandler() {
        assertURLStreamHandler(new io.microsphere.net.classpath.Handler(), OP_ADD | OP_CREATE);
        assertURLStreamHandler(new io.microsphere.net.console.Handler(), OP_ADD | OP_CREATE);
        assertURLStreamHandler(new io.microsphere.net.test.Handler(), OP_ADD | OP_CREATE);
    }

    @Test
    void testClearHandlers() {
        assertURLStreamHandler(new io.microsphere.net.classpath.Handler(), OP_ADD | OP_CLEAR);
        assertURLStreamHandler(new io.microsphere.net.console.Handler(), OP_ADD | OP_CLEAR);
        assertURLStreamHandler(new io.microsphere.net.test.Handler(), OP_ADD | OP_CLEAR);
    }

    protected void assertURLStreamHandler(ExtendableProtocolURLStreamHandler handler, int operator) {
        if ((operator & OP_ADD) != 0) {
            assertSame(factory, factory.addURLStreamHandler(handler.getProtocol(), handler));
        }

        if ((operator & OP_GET) != 0) {
            assertSame(handler, factory.getURLStreamHandler(handler.getProtocol()));
        }

        if ((operator & OP_CREATE) != 0) {
            assertSame(handler, factory.createURLStreamHandler(handler.getProtocol()));
        }

        if ((operator & OP_REMOVE) != 0) {
            assertSame(handler, factory.removeURLStreamHandler(handler.getProtocol()));
        }

        if ((operator & OP_CLEAR) != 0) {
            assertSame(factory, factory.clearHandlers());
        }
    }
}