package io.microsphere.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link BaseUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see BaseUtils
 * @since 1.0.0
 */
class BaseUtilsTest {

    @Test
    public void testConstructor() {
        assertThrows(IllegalStateException.class, () -> new BaseUtils(){});
    }

}