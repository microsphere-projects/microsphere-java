package io.microsphere.process;

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ProcessExecutor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ProcessExecutorTest
 * @since 1.0.0
 */
class ProcessExecutorTest extends AbstractTestCase {

    private ProcessExecutor executor;

    @BeforeEach
    void init() {
        this.executor = new ProcessExecutor("java", "-version");
    }

    @Test
    void testIsFinished() throws Exception {
        assertFalse(this.executor.isFinished());
    }

    @Test
    void testExecute() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8 * 1024);
        this.executor.execute(outputStream);
        assertTrue(outputStream.size() > 0);
        assertTrue(this.executor.isFinished());
        String response = new String(outputStream.toByteArray());
        log(response);
    }

    @Test
    void testExecuteWithTimeout() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8 * 1024);
        assertThrows(TimeoutException.class, () -> this.executor.execute(outputStream, 1));
        assertEquals(0, outputStream.size());
        assertTrue(this.executor.isFinished());
    }

    @Test
    void testExecuteOnWrongCommand() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8 * 1024);
        ProcessExecutor processExecutor = new ProcessExecutor("ttttt");
        assertThrows(IOException.class, () -> processExecutor.execute(outputStream));
        assertFalse(processExecutor.isFinished());
    }

}
