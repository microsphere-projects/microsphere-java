package io.microsphere.io.event;

import org.junit.jupiter.api.Test;

import java.io.File;

import static io.microsphere.io.event.FileChangedEvent.Kind.CREATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link FileChangedEvent} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see FileChangedEvent
 * @since 1.0.0
 */
public class FileChangedEventTest {

    @Test
    public void test() {
        File file = new File("new");
        FileChangedEvent event = new FileChangedEvent(file, CREATED);
        assertSame(file, event.getFile());
        assertSame(file, event.getSource());
        assertEquals(CREATED, event.getKind());
        assertNotNull(event.toString());
    }

    @Test
    public void testOnFailed() {
        assertThrows(IllegalArgumentException.class, () -> new FileChangedEvent(null, CREATED));
        assertThrows(IllegalArgumentException.class, () -> new FileChangedEvent(new File(""), null));
    }

}