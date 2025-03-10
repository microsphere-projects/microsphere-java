package io.microsphere.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.microsphere.util.VersionUtils.CURRENT_JAVA_VERSION;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_9;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link ModernProcessIdResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ModernProcessIdResolver
 * @since 1.0.0
 */
public class ModernProcessIdResolverTest {

    private ModernProcessIdResolver resolver;

    @BeforeEach
    public void init() {
        resolver = new ModernProcessIdResolver();
    }

    @Test
    public void testCurrent() {
        if (JAVA_VERSION_9.le(CURRENT_JAVA_VERSION)) {
            assertNotNull(resolver.current());
        } else {
            assertNull(resolver.current());
        }
    }

    @Test
    public void testGetPriority() {
        assertEquals(1, resolver.getPriority());
    }
}