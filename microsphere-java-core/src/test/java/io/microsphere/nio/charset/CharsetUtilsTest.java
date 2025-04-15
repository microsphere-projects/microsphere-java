package io.microsphere.nio.charset;

import io.microsphere.junit.jupiter.api.extension.annotation.UtilsTestExtension;
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
@UtilsTestExtension
public class CharsetUtilsTest  {

    @Test
    public void testConstants() {
        assertNotNull(DEFAULT_CHARSET);
    }

}