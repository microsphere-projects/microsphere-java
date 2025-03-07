package io.microsphere.net;

import io.microsphere.lang.Prioritized;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link CompositeURLStreamHandlerFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see CompositeURLStreamHandlerFactory
 * @since 1.0.0
 */
public class CompositeURLStreamHandlerFactoryTest {

    private StandardURLStreamHandlerFactory factory;

    private CompositeURLStreamHandlerFactory compositeFactory;

    @BeforeEach
    public void init() {
        factory = new StandardURLStreamHandlerFactory();
        compositeFactory = new CompositeURLStreamHandlerFactory();
        compositeFactory.addURLStreamHandlerFactory(factory);
    }

    @Test
    public void testCreateURLStreamHandler() {
        URLStreamHandler handler = compositeFactory.createURLStreamHandler("file");
        assertNotNull(handler);
    }

    @Test
    public void testAddURLStreamHandlerFactory() {
        compositeFactory.addURLStreamHandlerFactory(compositeFactory);
    }

    @Test
    public void testAddURLStreamHandlerFactoryOnNull() {
        compositeFactory.addURLStreamHandlerFactory(null);
    }

    @Test
    public void testAddURLStreamHandlerFactoryOnComposite() {
        CompositeURLStreamHandlerFactory newCompositeFactory = new CompositeURLStreamHandlerFactory();
        newCompositeFactory.addURLStreamHandlerFactory(factory);
        compositeFactory.addURLStreamHandlerFactory(newCompositeFactory);
    }

    @Test
    public void testGetFactories() {
        testAddURLStreamHandlerFactoryOnComposite();
        List<URLStreamHandlerFactory> factories = compositeFactory.getFactories();
        assertEquals(1, factories.size());
        assertTrue(factories.contains(factory));
    }

    @Test
    public void testGetComparator() {
        assertSame(Prioritized.COMPARATOR, compositeFactory.getComparator());
    }

    @Test
    public void testTestToString() {
        assertNotNull(compositeFactory.toString());
    }
}