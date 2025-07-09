package io.microsphere.process;

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static io.microsphere.process.ProcessManager.INSTANCE;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link ProcessManager} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ProcessManager
 * @since 1.0.0
 */
class ProcessManagerTest extends AbstractTestCase {

    @Test
    public void test() throws Throwable {
        ProcessManager processManager = INSTANCE;
        ProcessExecutor processExecutor = new ProcessExecutor("java", "-version");
        ExecutorService executorService = newFixedThreadPool(1);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8 * 1024);
        TimeUnit timeUnit = SECONDS;
        long timeout = timeUnit.toMillis(2);
        Future<Boolean> future = executorService.submit(() -> {
            processExecutor.execute(outputStream, timeout);
            return processExecutor.isFinished();
        });

        while (!future.isDone()) {
            Map<Process, String> unfinishedProcessesMap = processManager.unfinishedProcessesMap();
            for (Map.Entry<Process, String> entry : unfinishedProcessesMap.entrySet()) {
                Process process = entry.getKey();
                String arguments = entry.getValue();
                assertNotNull(process);
                assertEquals(" -version", arguments);
            }
        }

        executorService.shutdown();

        String response = new String(outputStream.toByteArray());
        log(response);
    }

}