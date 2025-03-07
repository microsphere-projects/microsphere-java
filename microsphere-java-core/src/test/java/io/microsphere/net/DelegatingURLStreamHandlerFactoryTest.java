package io.microsphere.net;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link DelegatingURLStreamHandlerFactory Test}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see DelegatingURLStreamHandlerFactory
 * @since 1.0.0
 */
public class DelegatingURLStreamHandlerFactoryTest {

    private StandardURLStreamHandlerFactory delegate;

    private DelegatingURLStreamHandlerFactory factory;

    @BeforeEach
    public void init() {
        this.delegate = new StandardURLStreamHandlerFactory();
        this.factory = new DelegatingURLStreamHandlerFactory(this.delegate);
    }

    @Test
    public void testConstructorOnNull() {
        assertThrows(IllegalArgumentException.class, () -> new DelegatingURLStreamHandlerFactory(null));
    }

    @Test
    public void testCreateURLStreamHandler() {
        assertNotNull(this.factory.createURLStreamHandler("file"));
    }

    @Test
    public void testGetDelegate() {
        assertSame(this.delegate, this.factory.getDelegate());
    }
}