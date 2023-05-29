package io.microsphere.i18n;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * {@link DefaultServiceMessageSource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class DefaultServiceMessageSourceTest {

    @BeforeClass
    public static void beforeClass() {
        LocaleContextHolder.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
    }

    @AfterClass
    public static void afterClass() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    public void test() {
        DefaultServiceMessageSource serviceMessageSource = new DefaultServiceMessageSource("test");
        serviceMessageSource.init();

        assertEquals("测试-a", serviceMessageSource.getMessage("a"));
        assertEquals("您好,World", serviceMessageSource.getMessage("hello", "World"));

        serviceMessageSource.destroy();
    }

    @Test(expected = IllegalStateException.class)
    public void testValidateMessageCode() {
        DefaultServiceMessageSource serviceMessageSource = new DefaultServiceMessageSource("error");
        serviceMessageSource.init();
    }

}
