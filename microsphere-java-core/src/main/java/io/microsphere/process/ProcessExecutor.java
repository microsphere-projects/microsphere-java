package io.microsphere.process;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;

import static io.microsphere.constants.SymbolConstants.SPACE_CHAR;
import static io.microsphere.process.ProcessManager.INSTANCE;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static java.lang.Long.MAX_VALUE;
import static java.lang.Long.getLong;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.currentTimeMillis;

/**
 * {@link Process} Executor
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ProcessExecutor
 * @since 1.0.0
 */
public class ProcessExecutor {

    private static final long waitForTimeInSecond = getLong("process.executor.wait.for", 1);

    private final ProcessManager processManager = INSTANCE;

    private final Runtime runtime = getRuntime();

    private final String commandLine;

    private final String options;

    private boolean finished;

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
        this.commandLine = command + this.options;
    }

    /**
     * Execute current process.
     * <p/>
     * //     * @param inputStream  input stream keeps output stream from process
     *
     * @param outputStream output stream for process normal or error input stream.
     * @throws IOException if process execution is failed.
     */
    public void execute(OutputStream outputStream) throws IOException {
        try {
            this.execute(outputStream, MAX_VALUE);
        } catch (TimeoutException e) {
        }
    }

    /**
     * Execute current process.
     * <p/>
     * //     * @param inputStream  input stream keeps output stream from process
     *
     * @param outputStream          output stream for process normal or error input stream.
     * @param timeoutInMilliseconds milliseconds timeout
     * @throws IOException      if process execution is failed.
     * @throws TimeoutException if the execution is timeout over specified <code>timeoutInMilliseconds</code>
     */
    public void execute(OutputStream outputStream, long timeoutInMilliseconds) throws IOException, TimeoutException {
        Process process = runtime.exec(commandLine);
        long startTime = currentTimeMillis();
        long endTime = -1L;
        InputStream processInputStream = process.getInputStream();
        InputStream processErrorInputStream = process.getErrorStream();
//        OutputStream processOutputStream = process.getOutputStream();
        int exitValue = -1;
        while (!finished) {
            long costTime = endTime - startTime;
            if (costTime > timeoutInMilliseconds) {
                finished = true;
                processManager.destroy(process);
                String message = format("Execution is timeout[{} ms]!", timeoutInMilliseconds);
                throw new TimeoutException(message);
            }
            try {
                processManager.addUnfinishedProcess(process, options);
                while (processInputStream.available() > 0) {
                    outputStream.write(processInputStream.read());
                }
                while (processErrorInputStream.available() > 0) {
                    outputStream.write(processErrorInputStream.read());
                }
                exitValue = process.exitValue();
                if (exitValue != 0) {
                    throw new IOException();
                }
                finished = true;
            } catch (IllegalThreadStateException e) {
                // Process is not finished yet;
                // Sleep a little to save on CPU cycles
                waitFor(waitForTimeInSecond);
                endTime = currentTimeMillis();
            } finally {
                processManager.removeUnfinishedProcess(process, options);
            }
        }
    }

    /**
     * Wait for specified seconds
     *
     * @param seconds specified seconds
     */
    private void waitFor(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

    /**
     * Check current process finish or not.
     *
     * @return <code>true</code> if current process finished
     */
    public boolean isFinished() {
        return finished;
    }
}
