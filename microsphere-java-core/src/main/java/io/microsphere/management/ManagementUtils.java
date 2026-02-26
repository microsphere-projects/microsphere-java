package io.microsphere.management;


import io.microsphere.process.ProcessIdResolver;
import io.microsphere.util.ServiceLoaderUtils;
import io.microsphere.util.Utils;

import java.util.Objects;

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

    static final long currentProcessId = resolveCurrentProcessId();

    private static long resolveCurrentProcessId() {
        return loadServicesList(ProcessIdResolver.class)
                .stream()
                .filter(ProcessIdResolver::supports)
                .map(ProcessIdResolver::current)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(UNKNOWN_PROCESS_ID);
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