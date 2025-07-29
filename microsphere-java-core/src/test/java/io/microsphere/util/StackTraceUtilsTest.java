package io.microsphere.util;

import org.junit.jupiter.api.Test;

import static io.microsphere.util.StackTraceUtils.getCallerClassName;
import static io.microsphere.util.StackTraceUtils.getCallerClassNameInGeneralJVM;
import static io.microsphere.util.StackTraceUtils.getCallerClassNames;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_9;
import static io.microsphere.util.VersionUtils.testCurrentJavaVersion;
import static java.lang.Integer.MAX_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link StackTraceUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see StackTraceUtils
 * @since 1.0.0
 */
class StackTraceUtilsTest {

    private static final String CALLER_CLASS_NAME = StackTraceUtilsTest.class.getName();

    @Test
    void testGetCallerClassName() {
        assertEquals(CALLER_CLASS_NAME, getCallerClassName());
    }

    @Test
    void testGetCallerClassNameOnStackWalkerSupportedForTesting() {
        StackTraceUtils.stackWalkerSupportedForTesting = false;
        assertEquals(getCallerClassNameInGeneralJVM(), getCallerClassName());
        assertEquals(CALLER_CLASS_NAME, getCallerClassName());
        StackTraceUtils.stackWalkerSupportedForTesting = true;
    }

    @Test
    void testGetCallerClassNames() {
        if (testCurrentJavaVersion("<", JAVA_VERSION_9)) {
            assertThrows(NullPointerException.class, () -> getCallerClassNames());
        } else {
            assertTrue(getCallerClassNames().contains(CALLER_CLASS_NAME));
        }
    }

    @Test
    void testGetCallerClassNameInGeneralJVM() {
        String callerClassName = getCallerClassNameInGeneralJVM();
        assertEquals(CALLER_CLASS_NAME, callerClassName);
    }

    @Test
    void testGetCallerClassNameInGeneralJVMOnOverStack() {
        assertNull(getCallerClassNameInGeneralJVM(MAX_VALUE));
    }

}