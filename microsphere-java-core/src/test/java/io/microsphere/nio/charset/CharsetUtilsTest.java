package io.microsphere.nio.charset;

import org.junit.jupiter.api.Test;

import static io.microsphere.nio.charset.CharsetUtils.DEFAULT_CHARSET;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link CharsetUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see CharsetUtils
 * @since 1.0.0
 */
class CharsetUtilsTest {

    @Test
    void testConstants() {
        assertNotNull(DEFAULT_CHARSET);
    }

}