package io.microsphere.io.filter;

import org.junit.jupiter.api.Test;

import static io.microsphere.io.filter.TrueFileFilter.INSTANCE;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TrueFileFilter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see TrueFileFilter
 * @since 1.0.0
 */
public class TrueFileFilterTest {

    @Test
    public void test() {
        assertTrue(INSTANCE.accept(null));
        assertTrue(INSTANCE.accept(null, null));
    }
}