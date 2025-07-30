package io.microsphere.management;

import io.microsphere.AbstractTestCase;
import io.microsphere.process.ProcessIdResolver;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.microsphere.management.ManagementUtils.getCurrentProcessId;
import static io.microsphere.process.ProcessIdResolver.UNKNOWN_PROCESS_ID;
import static io.microsphere.util.ServiceLoaderUtils.loadServicesList;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ManagementUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ManagementUtils
 * @since 1.0.0
 */
class ManagementUtilsTest extends AbstractTestCase {

    @Test
    void testGetCurrentProcessId() {
        long currentProcessId = getCurrentProcessId();
        assertTrue(currentProcessId > 0);
    }

    @Test
    void testLog() {
        List<ProcessIdResolver> resolvers = loadServicesList(ProcessIdResolver.class);
        for (ProcessIdResolver resolver : resolvers) {
            ManagementUtils.log(resolver, UNKNOWN_PROCESS_ID);
        }
    }

}
