package io.github.microsphere.i18n.spring.context;

import io.github.microsphere.i18n.ServiceMessageSource;
import io.github.microsphere.i18n.spring.beans.TestServiceMessageSourceConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Locale;

import static io.github.microsphere.i18n.util.I18nUtils.serviceMessageSource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * {@link I18nConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {I18nConfiguration.class, TestServiceMessageSourceConfiguration.class})
public class I18nConfigurationTest {

    @Before
    public void before() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Autowired
    private ServiceMessageSource serviceMessageSource;

    @Test
    public void testGetMessage() {
        // Testing Simplified Chinese
        // If the Message Code is "a"
        assertEquals("测试-a", serviceMessageSource.getMessage("a"));

        // The same is true for overloaded methods with Message Pattern arguments
        assertEquals("您好,World", serviceMessageSource.getMessage("hello", "World"));

        // Returns null if code does not exist
        assertNull(serviceMessageSource.getMessage("code-not-found"));

        // Test English, because the English Message resource does not exist
        assertEquals("Hello,World", serviceMessageSource.getMessage("hello", Locale.ENGLISH, "World"));

        // Returns null if code does not exist
        assertNull(serviceMessageSource.getMessage("code-not-found", Locale.US));
    }

    @Test
    public void testCommonServiceMessageSource() {
        assertSame(serviceMessageSource(), serviceMessageSource);
    }
}
