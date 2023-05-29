package io.github.microsphere.i18n.util;

import io.github.microsphere.i18n.DefaultServiceMessageSource;
import io.github.microsphere.i18n.EmptyServiceMessageSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertSame;

/**
 * {@link I18nUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class I18nUtilsTest {

    private I18nUtils i18nUtils;

    @Before
    public void before() {
        I18nUtils.destroyServiceMessageSource();
    }

    @After
    public void after() {
        I18nUtils.destroyServiceMessageSource();
    }

    @Test
    public void test() {
        assertSame(EmptyServiceMessageSource.INSTANCE, I18nUtils.serviceMessageSource());

        DefaultServiceMessageSource defaultServiceMessageSource = new DefaultServiceMessageSource("test");
        I18nUtils.setServiceMessageSource(defaultServiceMessageSource);

        assertSame(defaultServiceMessageSource, I18nUtils.serviceMessageSource());
    }


}
