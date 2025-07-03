package io.microsphere.process;

import io.microsphere.annotation.Nonnull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Collections.unmodifiableMap;

/**
 * Manages and tracks processes, providing methods to handle running processes and clean up resources.
 * <p>
 * This class provides functionality to:
 * <ul>
 *     <li>Add and remove unfinished processes with their respective arguments</li>
 *     <li>Retrieve an unmodifiable map of all unfinished processes</li>
 *     <li>Safely destroy a process</li>
 * </ul>
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>
 * // Adding a process
 * Process process = Runtime.getRuntime().exec("some-command");
 * ProcessManager.INSTANCE.addUnfinishedProcess(process, "some-command");
 *
 * // Destroying a process
 * ProcessManager.INSTANCE.destroy(process);
 *
 * // Retrieving all unfinished processes
 * Map<Process, String> processes = ProcessManager.INSTANCE.unfinishedProcessesMap();
 * </pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Process
 * @since 1.0.0
 */
public class ProcessManager {

    /**
     * Singleton instance
     */
    public static final ProcessManager INSTANCE = new ProcessManager();

    private final ConcurrentMap<Process, String> unfinishedProcessesCache = new ConcurrentHashMap<>();

    protected ProcessManager addUnfinishedProcess(Process process, String arguments) {
        unfinishedProcessesCache.putIfAbsent(process, arguments);
        return this;
    }

    protected ProcessManager removeUnfinishedProcess(Process process, String arguments) {
        unfinishedProcessesCache.remove(process, arguments);
        return this;
    }

    public void destroy(Process process) {
        process.destroy();
    }

    /**
     * Unfinished Processes Map
     *
     * @return non-null
     */
    @Nonnull
    public Map<Process, String> unfinishedProcessesMap() {
        return unmodifiableMap(unfinishedProcessesCache);
    }
}
