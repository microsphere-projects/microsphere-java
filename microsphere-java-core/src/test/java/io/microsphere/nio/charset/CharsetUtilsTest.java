package io.microsphere.nio.charset;

import org.junit.jupiter.api.Test;

import static io.microsphere.nio.charset.CharsetUtils.DEFAULT_CHARSET;
import static io.microsphere.util.ClassUtils.isAbstractClass;
import static io.microsphere.util.SystemUtils.FILE_ENCODING;
import static java.nio.charset.Charset.forName;
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
        assertThrows(IllegalStateException.class, () -> new CharsetUtils() {
        });
    }

    @Test
    public void testClass() {
        assertTrue(isAbstractClass(CharsetUtils.class));
    }

    @Test
    public void testConstants() {
        assertEquals(forName(FILE_ENCODING), DEFAULT_CHARSET);
    }

}