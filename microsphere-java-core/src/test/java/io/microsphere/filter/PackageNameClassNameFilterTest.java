package io.microsphere.filter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link PackageNameClassNameFilter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see PackageNameClassNameFilter
 * @since 1.0.0
 */
class PackageNameClassNameFilterTest {

    @Test
    void testAccept() {
        PackageNameClassNameFilter filter = new PackageNameClassNameFilter("io.microsphere", true);
        assertTrue(filter.accept("io.microsphere.filter.PackageNameClassNameFilterTest"));
        assertFalse(filter.accept("java.lang.String"));
        assertFalse(filter.accept(null));
    }
}