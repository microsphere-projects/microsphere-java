package io.github.microsphere.commons.i18n.util;

import io.github.microsphere.commons.i18n.spring.beans.TestServiceMessageSourceConfiguration;
import io.github.microsphere.commons.i18n.spring.context.I18nConfiguration;
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
        // 测试简体中文
        // null
        assertEquals(null, MessageUtils.getLocalizedMessage(null));
        // message 参数为 "a" 的情况，不包含模式 "{" "}"，返回原始内容
        assertEquals("a", MessageUtils.getLocalizedMessage("a"));
        // "{a}" 为 Message Code 模板，其中 "a" 为 Message Code
        assertEquals("测试-a", MessageUtils.getLocalizedMessage("{a}"));

        // 同理如下，带 Message Pattern 参数的重载方法
        assertEquals("hello", MessageUtils.getLocalizedMessage("hello", "World"));
        assertEquals("您好,World", MessageUtils.getLocalizedMessage("{hello}", "World"));

        // 当 message code 不存在时，返回 message 原内容
        assertEquals("{code-not-found}", MessageUtils.getLocalizedMessage("{code-not-found}"));
        assertEquals("code-not-found", MessageUtils.getLocalizedMessage("{microsphere-test.code-not-found}"));
        assertEquals("code-not-found", MessageUtils.getLocalizedMessage("{common.code-not-found}"));

        // 测试英文
        assertEquals("hello", MessageUtils.getLocalizedMessage("hello", Locale.ENGLISH, "World"));
        assertEquals("Hello,World", MessageUtils.getLocalizedMessage("{hello}", Locale.ENGLISH, "World"));
    }
}
