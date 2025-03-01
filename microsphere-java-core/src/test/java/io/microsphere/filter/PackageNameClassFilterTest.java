package io.microsphere.filter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link PackageNameClassFilter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see PackageNameClassFilter
 * @since 1.0.0
 */
public class PackageNameClassFilterTest {

    @Test
    void testAccept() {
        PackageNameClassFilter filter = new PackageNameClassFilter("io.microsphere", true);
        assertTrue(filter.accept(PackageNameClassFilterTest.class));
        assertFalse(filter.accept(String.class));
        assertFalse(filter.accept(null));
    }
}