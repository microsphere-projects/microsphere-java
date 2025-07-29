package io.microsphere.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link VirtualMachineProcessIdResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see VirtualMachineProcessIdResolver
 * @since 1.0.0
 */
class VirtualMachineProcessIdResolverTest {

    private VirtualMachineProcessIdResolver resolver;

    @BeforeEach
    void setUp() {
        this.resolver = new VirtualMachineProcessIdResolver();
    }

    @Test
    public void testSupports() {
        assertTrue(resolver.supports());
    }

    @Test
    public void testCurrent() {
        assertNotNull(resolver.current());
    }

    @Test
    public void testGetPriority() {
        assertEquals(5, resolver.getPriority());
    }
}