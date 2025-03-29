package io.microsphere.management;


import io.microsphere.logging.Logger;
import io.microsphere.process.ProcessIdResolver;
import io.microsphere.util.BaseUtils;

import java.util.List;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ServiceLoaderUtils.loadServicesList;

/**
 * Management Utility class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ManagementUtils
 * @since 1.0.0
 */
public abstract class ManagementUtils {

    private static final Logger logger = getLogger(ManagementUtils.class);

    static final int UNKNOWN_PROCESS_ID = -1;

    static final long currentProcessId = resolveCurrentProcessId();

    private static long resolveCurrentProcessId() {
        List<ProcessIdResolver> resolvers = loadServicesList(ProcessIdResolver.class);
        Long processId = null;
        for (ProcessIdResolver resolver : resolvers) {
            if ((processId = resolver.current()) != null) {
                break;
            }
        }
        return processId == null ? UNKNOWN_PROCESS_ID : processId;
    }

    /**
     * Get the process ID of current JVM
     *
     * @return If can't get the process ID , return <code>-1</code>
     */
    public static long getCurrentProcessId() {
        return currentProcessId;
    }

}
