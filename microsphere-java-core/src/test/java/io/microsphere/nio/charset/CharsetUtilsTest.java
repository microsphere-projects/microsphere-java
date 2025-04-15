package io.microsphere.nio.charset;

import io.microsphere.AbstractUtilsTest;
import org.junit.jupiter.api.Test;

import static io.microsphere.nio.charset.CharsetUtils.DEFAULT_CHARSET;
import static io.microsphere.util.ClassUtils.isAbstractClass;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link CharsetUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see CharsetUtils
 * @since 1.0.0
 */
public class CharsetUtilsTest extends AbstractUtilsTest<CharsetUtils> {

    @Test
    public void testConstants() {
        assertNotNull(DEFAULT_CHARSET);
    }

}