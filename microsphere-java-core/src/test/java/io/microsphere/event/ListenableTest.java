package io.microsphere.event;

import org.junit.jupiter.api.Test;

import static io.microsphere.event.Listenable.assertListener;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link Listenable} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Listenable
 * @since 1.0.0
 */
class ListenableTest {

    @Test
    void testAssertListenerNoNull() {
        assertThrows(IllegalArgumentException.class, () -> assertListener(null));
    }

}