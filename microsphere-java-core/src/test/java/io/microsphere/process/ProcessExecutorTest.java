package io.microsphere.process;

import io.microsphere.Loggable;
import io.microsphere.LoggingTest;
import io.microsphere.io.FastByteArrayOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ProcessExecutor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ProcessExecutorTest
 * @since 1.0.0
 */
class ProcessExecutorTest extends LoggingTest implements Loggable {

    private FastByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        this.outputStream = new FastByteArrayOutputStream(8 * 1024);
    }

    @AfterEach
    void tearDown() {
        this.outputStream.close();
    }

    @Test
    void testExecute() throws Exception {
        ProcessExecutor processExecutor = new ProcessExecutor("java", "-version");
        processExecutor.execute(this.outputStream);
        assertTrue(this.outputStream.size() > 0);
        String response = this.outputStream.toString();
        log(response);
    }

    /**
     * Arguments containing whitespace must be passed to the process as a single token.
     * <p>
     * {@code "-Dfoo=a b"} is one JVM option. With the previous join-then-split
     * implementation, the command line became {@code "java -Dfoo=a b -version"} and was
     * re-split into {@code ["java", "-Dfoo=a", "b", "-version"]}, making {@code java}
     * treat {@code "b"} as the main class and fail with a non-zero exit value.
     */
    @Test
    void testExecuteWithArgumentContainingWhitespace() throws Exception {
        ProcessExecutor processExecutor = new ProcessExecutor("java", "-Dfoo=a b", "-version");
        processExecutor.execute(this.outputStream);
        assertTrue(this.outputStream.size() > 0);
        log(this.outputStream.toString());
    }

    @Test
    void testNullCommand() {
        assertThrows(IllegalArgumentException.class, () -> new ProcessExecutor(null));
    }

    @Test
    void testNullOption() {
        assertThrows(IllegalArgumentException.class, () -> new ProcessExecutor("java", (String) null));
    }

    @Test
    void testExecuteWithTimeout() {
        ProcessExecutor processExecutor = new ProcessExecutor("javac", "--help");
        assertThrows(TimeoutException.class, () -> processExecutor.execute(this.outputStream, 1));
        assertEquals(0, this.outputStream.size());
    }

    @Test
    void testExecuteOnFailed() {
        ProcessExecutor processExecutor = new ProcessExecutor("javac", "-a");
        assertThrows(IOException.class, () -> processExecutor.execute(this.outputStream));
    }

    @Test
    void testExecuteOnNotFoundCommand() {
        ProcessExecutor processExecutor = new ProcessExecutor("not-found-command");
        assertThrows(IOException.class, () -> processExecutor.execute(this.outputStream));
    }
}
