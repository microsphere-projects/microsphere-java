package io.microsphere.util;

import io.microsphere.AbstractTestCase;
import io.microsphere.event.EchoEventListener;
import io.microsphere.event.EchoEventListener2;
import io.microsphere.event.EventListener;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.util.ServiceLoaderUtils.loadFirstService;
import static io.microsphere.util.ServiceLoaderUtils.loadLastService;
import static io.microsphere.util.ServiceLoaderUtils.loadServices;
import static io.microsphere.util.ServiceLoaderUtils.loadServicesList;
import static io.microsphere.util.ServiceLoaderUtils.serviceLoaderCached;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link ServiceLoaderUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ServiceLoaderUtilsTest
 * @since 1.0.0
 */
public class ServiceLoaderUtilsTest extends AbstractTestCase {

    private static final Class<EventListener> TEST_CLASS = EventListener.class;

    private static final boolean TEST_CACHED = serviceLoaderCached;

    @Test
    public void testLoadServicesListWithServiceType() {
        List<EventListener> eventListeners = loadServicesList(TEST_CLASS);
        assertEventListeners(eventListeners);
    }

    @Test
    public void testLoadServicesListWithServiceTypeAndClassLoader() {
        List<EventListener> eventListeners = loadServicesList(TEST_CLASS, classLoader);
        assertEventListeners(eventListeners);
    }

    @Test
    public void testLoadServicesListWithServiceTypeAndCached() {
        List<EventListener> eventListeners = loadServicesList(TEST_CLASS, TEST_CACHED);
        assertEventListeners(eventListeners);
    }

    @Test
    public void testLoadServicesListWithServiceTypeAndClassLoaderAndCached() {
        List<EventListener> eventListeners = loadServicesList(TEST_CLASS, classLoader, TEST_CACHED);
        assertEventListeners(eventListeners);

        eventListeners = loadServicesList(TEST_CLASS, null, TEST_CACHED);
        assertEventListeners(eventListeners);
    }

    @Test
    public void testLoadServicesListOnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> loadServicesList(Set.class));
        assertThrows(IllegalArgumentException.class, () -> loadServicesList(Set.class, classLoader));
        assertThrows(IllegalArgumentException.class, () -> loadServicesList(Set.class, classLoader, true));
    }

    @Test
    public void testLoadServicesWithServiceType() {
        EventListener[] eventListeners = loadServices(TEST_CLASS);
        assertEventListeners(eventListeners);
    }

    @Test
    public void testLoadServicesWithServiceTypeAndClassLoader() {
        EventListener[] eventListeners = loadServices(TEST_CLASS, classLoader);
        assertEventListeners(eventListeners);
    }

    @Test
    public void testLoadServicesWithServiceTypeAndCached() {
        EventListener[] eventListeners = loadServices(TEST_CLASS, TEST_CACHED);
        assertEventListeners(eventListeners);
    }

    @Test
    public void testLoadServicesWithServiceTypeAndClassLoaderAndCached() {
        EventListener[] eventListeners = loadServices(TEST_CLASS, classLoader, TEST_CACHED);
        assertEventListeners(eventListeners);
    }

    @Test
    public void testLoadServicesOnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> loadServices(Set.class));
        assertThrows(IllegalArgumentException.class, () -> loadServices(Set.class, classLoader));
        assertThrows(IllegalArgumentException.class, () -> loadServices(Set.class, classLoader, true));
    }

    @Test
    public void testLoadFirstServiceWithServiceType() {
        EventListener eventListener = loadFirstService(TEST_CLASS);
        assertFirstEventListener(eventListener);
    }

    @Test
    public void testLoadFirstServiceWithServiceTypeAndClassLoader() {
        EventListener eventListener = loadFirstService(TEST_CLASS, classLoader);
        assertFirstEventListener(eventListener);
    }

    @Test
    public void testLoadFirstServiceWithServiceTypeAndCached() {
        EventListener eventListener = loadFirstService(TEST_CLASS, TEST_CACHED);
        assertFirstEventListener(eventListener);
    }

    @Test
    public void testLoadFirstServiceWithServiceTypeAndClassLoaderAndCached() {
        EventListener eventListener = loadFirstService(TEST_CLASS, classLoader, TEST_CACHED);
        assertFirstEventListener(eventListener);
    }

    @Test
    public void testLoadFirstServiceOnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> loadFirstService(Set.class));
        assertThrows(IllegalArgumentException.class, () -> loadFirstService(Set.class, classLoader));
        assertThrows(IllegalArgumentException.class, () -> loadFirstService(Set.class, classLoader, true));
    }

    @Test
    public void testLoadLastServiceWithServiceType() {
        EventListener eventListener = loadLastService(TEST_CLASS);
        assertLastEventListener(eventListener);
    }

    @Test
    public void testLoadLastServiceWithServiceTypeAndClassLoader() {
        EventListener eventListener = loadLastService(TEST_CLASS, classLoader);
        assertLastEventListener(eventListener);
    }

    @Test
    public void testLoadLastServiceWithServiceTypeAndCached() {
        EventListener eventListener = loadLastService(TEST_CLASS, TEST_CACHED);
        assertLastEventListener(eventListener);
    }

    @Test
    public void testLoadLastServiceWithServiceTypeAndClassLoaderAndCached() {
        EventListener eventListener = loadLastService(TEST_CLASS, classLoader, TEST_CACHED);
        assertLastEventListener(eventListener);
    }

    @Test
    public void testLoadLastServiceOnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> loadLastService(Set.class));
        assertThrows(IllegalArgumentException.class, () -> loadLastService(Set.class, classLoader));
        assertThrows(IllegalArgumentException.class, () -> loadLastService(Set.class, classLoader, true));
    }

    private void assertEventListeners(EventListener[] eventListeners) {
        assertEventListeners(ofList(eventListeners));
    }

    private void assertEventListeners(List<EventListener> eventListeners) {
        assertEquals(2, eventListeners.size());

        EventListener eventListener = eventListeners.get(0);
        EventListener firstService = loadFirstService(TEST_CLASS, classLoader);
        EventListener lastService = loadLastService(TEST_CLASS, classLoader);

        assertNotNull(eventListener);
        assertEquals(eventListener, firstService);
        assertEquals(EchoEventListener2.class, firstService.getClass());
        assertEquals(EchoEventListener.class, lastService.getClass());
    }

    private void assertFirstEventListener(EventListener eventListener) {
        assertNotNull(eventListener);
        assertEquals(EchoEventListener2.class, eventListener.getClass());
    }

    private void assertLastEventListener(EventListener eventListener) {
        assertNotNull(eventListener);
        assertEquals(EchoEventListener.class, eventListener.getClass());
    }
}
