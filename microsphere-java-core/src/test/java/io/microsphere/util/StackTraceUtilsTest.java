package io.microsphere.util;

import org.junit.jupiter.api.Test;

import static io.microsphere.util.StackTraceUtils.getCallerClassName;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link StackTraceUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see StackTraceUtils
 * @since 1.0.0
 */
public class StackTraceUtilsTest {

    @Test
    public void testGetCallerClassName() {
        assertEquals(StackTraceUtilsTest.class.getName(), getCallerClassName());
    }

}