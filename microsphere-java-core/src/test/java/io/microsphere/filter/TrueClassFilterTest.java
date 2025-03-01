package io.microsphere.filter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link TrueClassFilter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see TrueClassFilter
 * @since 1.0.0
 */
class TrueClassFilterTest {

    @Test
    void testAccept() {
        assertTrue(TrueClassFilter.INSTANCE.accept(null));
        assertTrue(TrueClassFilter.INSTANCE.accept(getClass()));
    }
}