package io.microsphere.io;

import io.microsphere.AbstractTestCase;
import io.microsphere.io.event.FileChangedEvent;
import io.microsphere.io.event.LoggingFileChangedListener;
import org.junit.jupiter.api.Test;

import java.io.File;

import static io.microsphere.collection.Lists.ofList;

/**
 * {@link FileWatchService} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see FileWatchService
 * @since 1.0.0
 */
public class FileWatchServiceTest extends AbstractTestCase {

    private FileWatchService service = (file, listener, kinds) -> {
        log("Watching : {} , listener : {} , kinds : {}", file, listener, kinds);
    };

    @Test
    public void testWatch() {
        File file = newRandomTempFile();
        service.watch(file, new LoggingFileChangedListener(), FileChangedEvent.Kind.values());
        service.watch(file, ofList(new LoggingFileChangedListener()), FileChangedEvent.Kind.values());
    }
}