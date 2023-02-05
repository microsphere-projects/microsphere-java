package io.github.microsphere.i18n.spring.context;

import io.github.microsphere.i18n.CompositeServiceMessageSource;
import io.github.microsphere.i18n.ServiceMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.Assert.assertEquals;

/**
 * {@link I18nInitializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = I18nInitializer.class, loader = AnnotationConfigContextLoader.class, classes = I18nInitializerTest.class)
@Configuration
public class I18nInitializerTest {

    @Before
    public void before() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Autowired
    private ServiceMessageSource serviceMessageSource;

    @Test
    public void test() {
        assertEquals(CompositeServiceMessageSource.class, serviceMessageSource.getClass());
    }
}
