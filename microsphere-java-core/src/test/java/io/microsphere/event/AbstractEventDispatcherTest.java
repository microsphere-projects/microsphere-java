package io.microsphere.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link AbstractEventDispatcher} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractEventDispatcher
 * @since 1.0.0
 */
class AbstractEventDispatcherTest {

    @Test
    public void testConstructorOnFailed() {
        assertThrows(NullPointerException.class, () -> new AbstractEventDispatcher(null) {
        });
    }

}