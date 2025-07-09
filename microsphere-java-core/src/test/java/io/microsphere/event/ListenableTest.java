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
    public void testAssertListenerNoNull() {
        assertThrows(IllegalArgumentException.class, () -> assertListener(null));
    }

    @Test
    public void testAssertListenerOnInterface() {
        assertThrows(IllegalArgumentException.class, () -> assertListener(new FinalEventListener()));
    }


    static final class FinalEventListener implements EventListener<Event> {

        @Override
        public void onEvent(Event event) {

        }
    }


}