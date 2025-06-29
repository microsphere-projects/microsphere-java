package io.microsphere.management;


import io.microsphere.logging.Logger;
import io.microsphere.process.ProcessIdResolver;
import io.microsphere.util.ServiceLoaderUtils;
import io.microsphere.util.Utils;

import java.util.List;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.process.ProcessIdResolver.UNKNOWN_PROCESS_ID;
import static io.microsphere.util.ServiceLoaderUtils.loadServicesList;

/**
 * Utility class for management-related operations
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ProcessIdResolver
 * @see ServiceLoaderUtils
 * @since 1.0.0
 */
public abstract class ManagementUtils implements Utils {

    private static final Logger logger = getLogger(ManagementUtils.class);

    static final long currentProcessId = resolveCurrentProcessId();

    private static long resolveCurrentProcessId() {
        List<ProcessIdResolver> resolvers = loadServicesList(ProcessIdResolver.class);
        Long processId = null;
        for (ProcessIdResolver resolver : resolvers) {
            if (resolver.supports()) {
                if ((processId = resolver.current()) != null) {
                    log(resolver, processId);
                    break;
                }
            }
        }
        return processId == null ? UNKNOWN_PROCESS_ID : processId;
    }

    static void log(ProcessIdResolver resolver, Long processId) {
        if (logger.isTraceEnabled()) {
            logger.trace("The process id was resolved by ProcessIdResolver[class : '{}' , priority : {}] successfully : {}",
                    resolver.getClass().getName(), resolver.getPriority(), processId);
        }
    }

    /**
     * Get the process ID of current JVM
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Get the current process ID
     * long pid = ManagementUtils.getCurrentProcessId();
     * if (pid != -1) {
     *     System.out.println("Current Process ID: " + pid);
     * } else {
     *     System.out.println("Failed to resolve process ID.");
     * }
     * }</pre>
     *
     * <p>
     * This class uses the ServiceLoader pattern to discover and load all available implementations of
     * {@link ProcessIdResolver}, and selects the most appropriate one based on support and priority.
     * If no resolver can determine the process ID, it defaults to {@link io.microsphere.process.ProcessIdResolver#UNKNOWN_PROCESS_ID}.
     * </p>
     *
     * @return If can't get the process ID , return <code>-1</code>
     */
    public static long getCurrentProcessId() {
        return currentProcessId;
    }

    private ManagementUtils() {
    }
}
