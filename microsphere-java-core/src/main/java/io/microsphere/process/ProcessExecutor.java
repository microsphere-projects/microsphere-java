package io.microsphere.process;

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.io.FastByteArrayInputStream;
import io.microsphere.io.FastByteArrayOutputStream;
import io.microsphere.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.microsphere.annotation.ConfigurationProperty.SYSTEM_PROPERTIES_SOURCE;
import static io.microsphere.concurrent.CustomizedThreadFactory.newThreadFactory;
import static io.microsphere.concurrent.ExecutorUtils.shutdownOnExit;
import static io.microsphere.constants.SymbolConstants.SPACE_CHAR;
import static io.microsphere.io.IOUtils.copy;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.process.ProcessManager.INSTANCE;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static io.microsphere.util.ExceptionUtils.wrap;
import static java.lang.Long.getLong;
import static java.lang.Long.parseLong;
import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * {@link Process} Executor
 *
 * <p>Executes operating system commands and manages the execution lifecycle, including handling input/output streams,
 * monitoring process status, and enforcing timeouts. This class wraps command execution in a structured way to provide
 * enhanced control and integration with the framework.
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

    private final Runtime runtime = getRuntime();

    private final String options;

    private final String commandLine;

    private final ExecutorService executor;

    /**
     * Constructor
     *
     * @param command command
     * @param options command options
     */
    public ProcessExecutor(String command, String... options) {
        StringBuilder optionsBuilder = new StringBuilder();
        if (isNotEmpty(options)) {
            for (String argument : options) {
                optionsBuilder.append(SPACE_CHAR).append(argument);
            }
        }
        this.options = optionsBuilder.toString();
        this.executor = newSingleThreadExecutor(newThreadFactory("process-exec", true));
        this.commandLine = command + this.options;
        shutdownOnExit(this.executor);
    }

    /**
     * Execute current process.
     *
     * @param outputStream output stream for process normal or error input stream.
     * @throws IOException      if process execution is failed.
     * @throws TimeoutException if the execution is timeout over specified {@link #DEFAULT_TIMEOUT}
     */
    public void execute(OutputStream outputStream) throws IOException, TimeoutException {
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
    public void execute(OutputStream outputStream, long timeoutInMilliseconds) throws IOException, TimeoutException {
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
    public void execute(OutputStream outputStream, long timeout, TimeUnit timeUnit) throws IOException, TimeoutException {

        Future<byte[]> future = executor.submit(() -> {
            Process process = runtime.exec(commandLine);
            InputStream processInputStream = process.getInputStream();
            InputStream processErrorInputStream = process.getErrorStream();
            FastByteArrayOutputStream targetOutputStream = new FastByteArrayOutputStream();
            int exitValue = -1;
            try {
                processManager.addUnfinishedProcess(process, options);
                // Copy the standard input stream
                copy(processInputStream, targetOutputStream);
                // Copy the error input stream
                copy(processErrorInputStream, targetOutputStream);
                process.waitFor(timeout, timeUnit);

                exitValue = process.exitValue();
                if (exitValue != 0) {
                    throw new IOException();
                }
            } finally {
                processManager.removeUnfinishedProcess(process, options);
                logger.trace("The command['{}'] is executed with exit value : {}", commandLine, exitValue);
            }
            return targetOutputStream.toByteArray();
        });

        try {
            byte[] bytes = future.get(timeout, timeUnit);
            copy(new FastByteArrayInputStream(bytes), outputStream);
        } catch (InterruptedException | ExecutionException e) {
            throw wrap(e, IOException.class);
        }
    }
}