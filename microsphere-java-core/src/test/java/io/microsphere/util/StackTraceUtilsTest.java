package io.microsphere.util;

import org.junit.jupiter.api.Test;

import static io.microsphere.util.StackTraceUtils.getStackTrace;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link StackTraceUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see StackTraceUtils
 * @since 1.0.0
 */
class StackTraceUtilsTest {

    @Test
    void testGetStackTrace() {
        assertNotNull(getStackTrace());
    }
}