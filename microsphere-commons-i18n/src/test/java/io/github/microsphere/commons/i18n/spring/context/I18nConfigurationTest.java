package io.github.microsphere.commons.i18n.spring.context;

import io.github.microsphere.commons.i18n.ServiceMessageSource;
import io.github.microsphere.commons.i18n.spring.beans.TestServiceMessageSourceConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Locale;

import static io.github.microsphere.commons.i18n.util.I18nUtils.serviceMessageSource;
import static org.junit.Assert.*;

/**
 * {@link I18nConfiguration} 测试类
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
        // 测试简体中文
        // Message Code 为 "a" 的情况
        assertEquals("测试-a", serviceMessageSource.getMessage("a"));

        // 同理如下，带 Message Pattern 参数的重载方法
        assertEquals("您好,World", serviceMessageSource.getMessage("hello", "World"));

        // 当 code 不存在时，返回 null
        assertNull(serviceMessageSource.getMessage("code-not-found"));

        // 测试英文，因为英文 Message 资源不存在
        assertEquals("Hello,World", serviceMessageSource.getMessage("hello", Locale.ENGLISH, "World"));

        // 当 code 不存在时，返回 null
        assertNull(serviceMessageSource.getMessage("code-not-found", Locale.US));
    }

    @Test
    public void testCommonServiceMessageSource() {
        assertSame(serviceMessageSource(), serviceMessageSource);
    }
}
