package io.microsphere.management;

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.Test;

import static io.microsphere.management.JmxUtils.getRuntimeMXBean;
import static io.microsphere.management.ManagementUtils.GET_PROCESS_ID_METHOD_NAME;
import static io.microsphere.management.ManagementUtils.JVM_FIELD_NAME;
import static io.microsphere.management.ManagementUtils.UNKNOWN_PROCESS_ID;
import static io.microsphere.management.ManagementUtils.getCurrentProcessId;
import static io.microsphere.management.ManagementUtils.getNativeCurrentPID;
import static io.microsphere.management.ManagementUtils.jvm;
import static io.microsphere.management.ManagementUtils.resolveCurrentPID;
import static io.microsphere.management.ManagementUtils.runtimeMXBean;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ManagementUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see ManagementUtils
 * @since 1.0.0
 */
public class ManagementUtilsTest extends AbstractTestCase {

    @Test
    public void testStaticFields() {
        assertEquals(-1, UNKNOWN_PROCESS_ID);
        assertEquals("jvm", JVM_FIELD_NAME);
        assertEquals("getProcessId", GET_PROCESS_ID_METHOD_NAME);
        assertSame(getRuntimeMXBean(), runtimeMXBean);
        assertNotNull(jvm);
    }

    @Test
    public void testGetNativeCurrentPID() {
        assertNotNull(getNativeCurrentPID());
    }

    @Test
    public void testResolveCurrentPID() {
        assertNotEquals(-1, resolveCurrentPID());
    }

    @Test
    public void testGetCurrentProcessId() {
        int currentProcessId = getCurrentProcessId();
        assertTrue(currentProcessId > 0);
    }

}
