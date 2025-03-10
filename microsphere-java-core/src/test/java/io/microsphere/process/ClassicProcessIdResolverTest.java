package io.microsphere.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link ClassicProcessIdResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ClassicProcessIdResolver
 * @since 1.0.0
 */
public class ClassicProcessIdResolverTest {

    private ClassicProcessIdResolver resolver;

    @BeforeEach
    public void init() {
        resolver = new ClassicProcessIdResolver();
    }

    @Test
    public void testCurrent() {
        assertNotNull(resolver.current());
    }

    @Test
    public void testGetPriority() {
        assertEquals(9, resolver.getPriority());
    }
}