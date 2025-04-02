package io.microsphere.nio.charset;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static io.microsphere.nio.charset.CharsetUtils.DEFAULT_CHARSET;
import static io.microsphere.util.ClassUtils.isAbstractClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link CharsetUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see CharsetUtils
 * @since 1.0.0
 */
public class CharsetUtilsTest {

    @Test
    public void testConstructor() {
        assertThrows(IllegalStateException.class, () -> new CharsetUtils(){});
    }

    @Test
    public void testClass() {
        assertTrue(isAbstractClass(CharsetUtils.class));
    }

    @Test
    public void testConstants() {
        assertEquals(StandardCharsets.UTF_8, DEFAULT_CHARSET);
    }

}