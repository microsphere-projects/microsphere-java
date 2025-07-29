package io.microsphere.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.microsphere.util.Version.Operator.GE;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_9;
import static io.microsphere.util.VersionUtils.testCurrentJavaVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link ModernProcessIdResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ModernProcessIdResolver
 * @since 1.0.0
 */
class ModernProcessIdResolverTest {

    private ModernProcessIdResolver resolver;

    private static final boolean isGEJava9 = testCurrentJavaVersion(GE, JAVA_VERSION_9);

    @BeforeEach
    void setUp() {
        resolver = new ModernProcessIdResolver();
    }

    @Test
    void testSupports() {
        assertEquals(isGEJava9, resolver.supports());
    }

    @Test
    void testCurrent() {
        if (isGEJava9) {
            assertNotNull(resolver.current());
        } else {
            assertThrows(NullPointerException.class, resolver::current);
        }
    }

    @Test
    void testGetPriority() {
        assertEquals(1, resolver.getPriority());
    }
}