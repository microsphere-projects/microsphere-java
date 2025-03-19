package io.microsphere.management;

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.Test;

import static io.microsphere.management.ManagementUtils.getCurrentProcessId;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ManagementUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ManagementUtils
 * @since 1.0.0
 */
public class ManagementUtilsTest extends AbstractTestCase {

    @Test
    public void testGetCurrentProcessId() {
        long currentProcessId = getCurrentProcessId();
        assertTrue(currentProcessId > 0);
    }

}
