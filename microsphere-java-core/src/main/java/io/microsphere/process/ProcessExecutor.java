package io.microsphere.process;

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.io.FastByteArrayInputStream;
import io.microsphere.io.FastByteArrayOutputStream;
import io.microsphere.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.microsphere.annotation.ConfigurationProperty.SYSTEM_PROPERTIES_SOURCE;
import static io.microsphere.concurrent.CustomizedThreadFactory.newThreadFactory;
import static io.microsphere.concurrent.ExecutorUtils.shutdownOnExit;
import static io.microsphere.io.IOUtils.copy;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.process.ProcessManager.INSTANCE;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static io.microsphere.util.ArrayUtils.combine;
import static io.microsphere.util.Assert.assertNoNullElements;
import static io.microsphere.util.Assert.assertNotBlank;
import static io.microsphere.util.ExceptionUtils.wrap;
import static java.lang.Long.getLong;
import static java.lang.Long.parseLong;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * {@link Process} Executor
 *
 * <p>Executes operating system commands and manages the execution lifecycle, including handling input/output streams,
 * monitoring process status, and enforcing timeouts. This class wraps command execution in a structured way to provide
 * enhanced control and integration with the framework.
 *
 * <p>The command and its options are passed to {@link ProcessBuilder} as an argument array, preserved as-is:
 * whitespace inside an argument (e.g. {@code "-Dfoo=a b"}) is passed to the process as a single token, and no
 * shell is involved at all.
 *
 * <h3>Example Usage</h3>
 * <pre>
 *     // Execute a simple command with options and capture output
 *     ProcessExecutor executor = new ProcessExecutor("ls", "-l", "/home");
 *     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 *     executor.execute(outputStream);
 *     System.out.println("Command Output: " + outputStream.toString());
 *
 *     // Execute a command with timeout protection
 *     try {
 *         executor.execute(outputStream, 5000); // 5 seconds timeout
 *     } catch (TimeoutException e) {
 *         System.err.println("Command timed out!");
 *     }
 * </pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ProcessManager
 * @since 1.0.0
 */
public class ProcessExecutor {

    private static final Logger logger = getLogger(ProcessExecutor.class);

    /**
     * The default proeprty value for the timeout of process execution : 30 seconds
     */
    public static final String DEFAULT_PROCESS_EXECUTION_TIMEOUT_PROPERTY_VAUE = "30000";

    /**
     * The default value for the timeout of process execution : 30 seconds
     */
    public static final long DEFAULT_PROCESS_EXECUTION_TIMEOUT = parseLong(DEFAULT_PROCESS_EXECUTION_TIMEOUT_PROPERTY_VAUE);

    /**
     * The property name for the timeout of process execution : "process.execution.timeout"
     *
     * @see #DEFAULT_PROCESS_EXECUTION_TIMEOUT_PROPERTY_VAUE
     * @see #DEFAULT_PROCESS_EXECUTION_TIMEOUT
     */
    @ConfigurationProperty(
            type = long.class,
            defaultValue = DEFAULT_PROCESS_EXECUTION_TIMEOUT_PROPERTY_VAUE,
            source = SYSTEM_PROPERTIES_SOURCE
    )
    public static final String PROCESS_EXECUTION_TIMEOUT_PROPERTY_NAME = "process.execution.timeout";

    /**
     * the timeout of process execution
     */
    public static final long DEFAULT_TIMEOUT = getLong(PROCESS_EXECUTION_TIMEOUT_PROPERTY_NAME, DEFAULT_PROCESS_EXECUTION_TIMEOUT);

    private final ProcessManager processManager = INSTANCE;

    /**
     * The command and its arguments, preserved as-is. Arguments are never joined
     * and re-split, so whitespace inside an argument (e.g. {@code -Dfoo=a b}) is
     * passed to the process as a single token.
     */
    private final String[] command;

    private final ExecutorService executor;

    /**
     * Constructor
     *
     * @param command command, must not be {@code null}
     * @param options command options, each must not be {@code null}
     * @throws IllegalArgumentException if {@code command} or any element of {@code options} is {@code null}
     */
    public ProcessExecutor(@Nonnull String command, @Nullable String... options) {
        assertNotBlank(command, () -> "The command must not be blank");
        assertNoNullElements(options, () -> "The command options must not contain null elements");
        String[] commandArray = combine(command, options);
        this.command = commandArray;
        this.executor = newSingleThreadExecutor(newThreadFactory("process-exec", true));
        shutdownOnExit(this.executor);
    }

    /**
     * Execute current process.
     *
     * @param outputStream output stream for process normal or error input stream.
     * @throws IOException      if process execution is failed.
     * @throws TimeoutException if the execution is timeout over specified {@link #DEFAULT_TIMEOUT}
     */
    public void execute(@Nonnull OutputStream outputStream) throws IOException, TimeoutException {
        this.execute(outputStream, DEFAULT_TIMEOUT);
    }

    /**
     * Execute current process.
     *
     * @param outputStream          output stream for process normal or error input stream.
     * @param timeoutInMilliseconds milliseconds timeout
     * @throws IOException      if process execution is failed.
     * @throws TimeoutException if the execution is timeout over specified <code>timeoutInMilliseconds</code>
     */
    public void execute(@Nonnull OutputStream outputStream, long timeoutInMilliseconds) throws IOException, TimeoutException {
        execute(outputStream, timeoutInMilliseconds, MILLISECONDS);
    }

    /**
     * Execute current process.
     *
     * @param outputStream output stream for process normal or error input stream.
     * @param timeout      the timeout value
     * @param timeUnit     {@link TimeUnit}
     * @throws IOException      if process execution is failed.
     * @throws TimeoutException if the execution is timeout over specified <code>timeout</code> and <code>timeUnit</code>
     */
    public void execute(@Nonnull OutputStream outputStream, long timeout, @Nonnull TimeUnit timeUnit) throws IOException, TimeoutException {

        Future<byte[]> future = executor.submit(() -> {
            // Use ProcessBuilder with merged stderr so we only need to drain one stream,
            // avoiding the classic stdout/stderr deadlock caused by sequential draining.
            // The command array is passed as-is: arguments containing whitespace are NOT
            // joined and re-split, so each element stays a single argument.
            Process process = new ProcessBuilder(this.command)
                    .redirectErrorStream(true)
                    .start();
            InputStream processInputStream = process.getInputStream();
            FastByteArrayOutputStream targetOutputStream = new FastByteArrayOutputStream();
            int exitValue = -1;
            try {
                processManager.addUnfinishedProcess(process);
                // Copy the merged standard + error output stream
                copy(processInputStream, targetOutputStream);

                // wait for the process being executed
                process.waitFor(timeout, timeUnit);

                // try to exit with value
                exitValue = process.exitValue();
                if (exitValue != 0) {
                    String message = format("The command['{}'] execution is exited with invalid value : {}", arrayToString(this.command), exitValue);
                    throw new IOException(message);
                }
            } finally {
                processManager.removeUnfinishedProcess(process);
                if (logger.isTraceEnabled()) {
                    logger.trace("The command['{}'] is executed with exit value : {}", arrayToString(this.command), exitValue);
                }
            }
            return targetOutputStream.toByteArray();
        });

        try {
            byte[] bytes = future.get(timeout, timeUnit);
            copy(new FastByteArrayInputStream(bytes), outputStream);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        } catch (Exception e) {
            throw wrap(e, IOException.class);
        }
    }
}
