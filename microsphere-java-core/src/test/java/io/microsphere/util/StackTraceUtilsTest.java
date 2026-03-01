package io.microsphere.util;

import org.junit.jupiter.api.Test;

import static io.microsphere.util.StackTraceUtils.getCallerClassInStatckTrace;
import static io.microsphere.util.StackTraceUtils.getCallerClassNameInStackTrace;
import static io.microsphere.util.StackTraceUtils.getStackTrace;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link StackTraceUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see StackTraceUtils
 * @since 1.0.0
 */
class StackTraceUtilsTest {

    private static final Class<?> CALLER_CLASS = StackTraceUtilsTest.class;

    private static final String CALLER_CLASS_NAME = CALLER_CLASS.getName();

    @Test
    void testGetCallerClassInStatckTrace() {
        Class<?> callerClassInStatckTrace = getCallerClassInStatckTrace();
        assertEquals(CALLER_CLASS, callerClassInStatckTrace);
    }

    @Test
    void testGetCallerClassNameInStackTrace() {
        String callerClassName = getCallerClassNameInStackTrace();
        assertEquals(CALLER_CLASS_NAME, callerClassName);
    }

    @Test
    void testGetCallerClassInStatckTraceWithFrame() {
        assertNull(getCallerClassInStatckTrace(99999));
    }

    @Test
    void testGetStackTrace() {
        assertNotNull(getStackTrace());
    }
}