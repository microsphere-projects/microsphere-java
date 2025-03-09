package io.microsphere.nio.charset;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static io.microsphere.nio.charset.CharsetUtils.DEFAULT_CHARSET;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link CharsetUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see CharsetUtils
 * @since 1.0.0
 */
public class CharsetUtilsTest {
    @Test
    public void testConstants() {
        assertEquals(StandardCharsets.UTF_8, DEFAULT_CHARSET);
    }

}