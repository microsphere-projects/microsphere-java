package io.microsphere.i18n.util;

import io.microsphere.i18n.spring.beans.TestServiceMessageSourceConfiguration;
import io.microsphere.i18n.spring.context.I18nConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {I18nConfiguration.class, I18nUtils.class, TestServiceMessageSourceConfiguration.class})
public class MessageUtilsTest {

    @Before
    public void before() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    public void testGetLocalizedMessage() {
        // Testing Simplified Chinese
        // null
        Assert.assertEquals(null, MessageUtils.getLocalizedMessage(null));
        // If the message argument is "a", the pattern "{" "}" is not included, and the original content is returned
        assertEquals("a", MessageUtils.getLocalizedMessage("a"));
        // "{a}" is the Message Code template, where "a" is Message Code
        assertEquals("测试-a", MessageUtils.getLocalizedMessage("{a}"));

        // The same is true for overloaded methods with Message Pattern arguments
        assertEquals("hello", MessageUtils.getLocalizedMessage("hello", "World"));
        assertEquals("您好,World", MessageUtils.getLocalizedMessage("{hello}", "World"));

        // If message code does not exist, return the original content of message
        assertEquals("{code-not-found}", MessageUtils.getLocalizedMessage("{code-not-found}"));
        assertEquals("code-not-found", MessageUtils.getLocalizedMessage("{microsphere-test.code-not-found}"));
        assertEquals("code-not-found", MessageUtils.getLocalizedMessage("{common.code-not-found}"));

        // The test of English
        assertEquals("hello", MessageUtils.getLocalizedMessage("hello", Locale.ENGLISH, "World"));
        assertEquals("Hello,World", MessageUtils.getLocalizedMessage("{hello}", Locale.ENGLISH, "World"));
    }
}
