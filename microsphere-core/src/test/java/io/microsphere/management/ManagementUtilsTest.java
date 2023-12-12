package io.microsphere.management;

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        assertNotNull(ManagementUtils.jvm);
        assertNotNull(ManagementUtils.getProcessIdMethod);
    }

    @Test
    public void testGetCurrentProcessId() {
        int currentProcessId = ManagementUtils.getCurrentProcessId();
        assertTrue(currentProcessId > 0);
    }
}
