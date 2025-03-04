package io.microsphere.process;

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ProcessExecutor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see ProcessExecutorTest
 * @since 1.0.0
 */
public class ProcessExecutorTest extends AbstractTestCase {

    private ProcessExecutor executor;

    @BeforeEach
    public void init() {
        this.executor = new ProcessExecutor("java", "-version");
    }

    @Test
    public void testIsFinished() throws Exception {
        assertFalse(this.executor.isFinished());
    }

    @Test
    public void testExecute() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8 * 1024);
        this.executor.execute(outputStream, 2000);
        assertTrue(outputStream.size() > 0);
        assertTrue(this.executor.isFinished());
        String response = new String(outputStream.toByteArray());
        log(response);
    }

}
