package io.microsphere.util;

import io.microsphere.event.EchoEventListener;
import io.microsphere.event.EchoEventListener2;
import io.microsphere.event.EventListener;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static io.microsphere.util.ServiceLoaderUtils.loadFirstService;
import static io.microsphere.util.ServiceLoaderUtils.loadLastService;
import static io.microsphere.util.ServiceLoaderUtils.loadServicesList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link ServiceLoaderUtilsTest}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see ServiceLoaderUtilsTest
 * @since 1.0.0
 */
public class ServiceLoaderUtilsTest {

    @Test
    public void testLoadServicesList() throws Exception {

        ClassLoader classLoader = getClass().getClassLoader();
        List<EventListener> eventListeners = loadServicesList(EventListener.class, classLoader);
        assertEquals(2, eventListeners.size());

        EventListener eventListener = eventListeners.get(0);
        EventListener firstService = loadFirstService(EventListener.class, classLoader);
        EventListener lastService = loadLastService(EventListener.class, classLoader);

        assertNotNull(eventListener);
        assertEquals(eventListener, firstService);
        assertEquals(EchoEventListener2.class, firstService.getClass());
        assertEquals(EchoEventListener.class, lastService.getClass());

        IllegalArgumentException e = null;

        try {
            loadServicesList(Set.class, classLoader);
        } catch (IllegalArgumentException e_) {
            e = e_;
        }

        assertNotNull(e);

    }
}
