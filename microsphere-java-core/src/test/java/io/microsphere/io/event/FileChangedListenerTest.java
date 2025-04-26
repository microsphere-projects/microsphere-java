package io.microsphere.io.event;

import io.microsphere.AbstractTestCase;
import io.microsphere.io.event.FileChangedEvent.Kind;
import io.microsphere.util.ValueHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static io.microsphere.io.FileUtils.forceDelete;
import static io.microsphere.io.event.FileChangedEvent.Kind.CREATED;
import static io.microsphere.io.event.FileChangedEvent.Kind.DELETED;
import static io.microsphere.io.event.FileChangedEvent.Kind.MODIFIED;
import static io.microsphere.io.event.FileChangedEvent.Kind.values;
import static io.microsphere.lang.Prioritized.NORMAL_PRIORITY;
import static io.microsphere.util.ValueHolder.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link FileChangedListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see FileChangedListener
 * @since
 */
public class FileChangedListenerTest extends AbstractTestCase {

    private FileChangedListener listener;

    @BeforeEach
    public void init() {
        listener = new DefaultFileChangedListener();
    }

    @AfterEach
    public void destroy() {
        listener = null;
    }

    @Test
    public void testGetPriority() {
        assertEquals(NORMAL_PRIORITY, listener.getPriority());
    }

    @Test
    public void testOnEvent() throws IOException {
        ValueHolder<FileChangedEvent> eventHolder = of(null);

        FileChangedListener listener = new FileChangedListener() {
            @Override
            public void onFileCreated(FileChangedEvent event) {
                eventHolder.setValue(event);
            }

            @Override
            public void onFileModified(FileChangedEvent event) {
                eventHolder.setValue(event);
            }

            @Override
            public void onFileDeleted(FileChangedEvent event) {
                eventHolder.setValue(event);
            }
        };

        for (Kind kind : values()) {
            testEvent(listener, kind);
            assertEquals(eventHolder.getValue().getKind(), kind);
            eventHolder.setValue(null);
        }
    }

    @Test
    public void testOnEventOnNull() {
        assertThrows(NullPointerException.class, () -> listener.onEvent(null));
    }

    @Test
    public void testOnFileCreated() throws IOException {
        testEvent(listener, CREATED);
    }

    @Test
    public void testOnFileModified() throws IOException {
        testEvent(listener, MODIFIED);
    }

    @Test
    public void testOnFileDeleted() throws IOException {
        testEvent(listener, DELETED);
    }

    private void testEvent(FileChangedListener listener, Kind kind) throws IOException {
        File file = createRandomTempFile();
        FileChangedEvent event = new FileChangedEvent(file, kind);
        listener.onEvent(event);
        forceDelete(file);
    }
}