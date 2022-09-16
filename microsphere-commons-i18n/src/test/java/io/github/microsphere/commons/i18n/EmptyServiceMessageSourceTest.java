package io.github.microsphere.commons.i18n;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * {@link EmptyServiceMessageSource} 测试
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class EmptyServiceMessageSourceTest {

    private EmptyServiceMessageSource serviceMessageSource = EmptyServiceMessageSource.INSTANCE;

    @Before
    public void before() {
        serviceMessageSource.init();
    }

    @After
    public void after() {
        serviceMessageSource.destroy();
    }

    @Test
    public void testGetMessage() {
        assertNull(serviceMessageSource.getMessage("test"));
        assertNull(serviceMessageSource.getMessage("test", "a"));
        assertNull(serviceMessageSource.getMessage("test", Locale.ENGLISH, "a"));
    }

    @Test
    public void testGetSource() {
        assertEquals("Empty", serviceMessageSource.getSource());
    }

    @Test
    public void testGetDefaultLocale() {
        assertEquals(Locale.SIMPLIFIED_CHINESE, serviceMessageSource.getDefaultLocale());
    }

    @Test
    public void testGetLocale() {
        assertEquals(Locale.SIMPLIFIED_CHINESE, serviceMessageSource.getLocale());
    }

    @Test
    public void testGetSupportedLocales() {
        assertEquals(asList(Locale.SIMPLIFIED_CHINESE, Locale.ENGLISH), serviceMessageSource.getSupportedLocales());
    }
}
