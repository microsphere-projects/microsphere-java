package io.github.microsphere.commons.i18n.spring.beans.factory;

import io.github.microsphere.commons.i18n.ServiceMessageSource;
import io.github.microsphere.commons.i18n.spring.beans.TestServiceMessageSourceConfiguration;
import io.github.microsphere.commons.i18n.spring.context.ResourceServiceMessageSourceChangedEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Locale;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * {@link ServiceMessageSourceFactoryBean} 测试
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ServiceMessageSourceFactoryBeanTest.class, TestServiceMessageSourceConfiguration.class})
@TestPropertySource(properties = {
        "microsphere.i18n.default-locale=en",
        "microsphere.i18n.supported-locales=en",
})
public class ServiceMessageSourceFactoryBeanTest {

    @Autowired
    private ServiceMessageSource serviceMessageSource;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ConfigurableEnvironment environment;

    private MockPropertySource propertySource;

    @Before
    public void before() {
        LocaleContextHolder.resetLocaleContext();
        propertySource = new MockPropertySource("mock");
        environment.getPropertySources().addFirst(propertySource);
    }

    @Test
    public void testGetMessage() {
        assertEquals("test-a", serviceMessageSource.getMessage("a"));
        assertEquals("Hello,World", serviceMessageSource.getMessage("hello", "World"));

        // Test US
        assertNull(serviceMessageSource.getMessage("a", Locale.US));

        ResourceServiceMessageSourceChangedEvent event = new ResourceServiceMessageSourceChangedEvent(Arrays.asList("test.i18n_messages_en.properties"));
        propertySource.setProperty("test.i18n_messages_en.properties", "test.a=1");
        eventPublisher.publishEvent(event);
        assertEquals("1", serviceMessageSource.getMessage("a"));
    }

    @Test
    public void testGetLocale() {
        assertEquals(Locale.ENGLISH, serviceMessageSource.getLocale());

        // Test US
        LocaleContextHolder.setLocale(Locale.US);
        assertEquals(Locale.US, serviceMessageSource.getLocale());
    }

    @Test
    public void testGetDefaultLocale() {
        assertEquals(Locale.ENGLISH, serviceMessageSource.getDefaultLocale());
    }

    @Test
    public void testGetSupportedLocales() {
        assertEquals(asList(Locale.ENGLISH), serviceMessageSource.getSupportedLocales());
    }

    @Test
    public void testGetSource() {
        assertEquals("test", serviceMessageSource.getSource());
    }

}
