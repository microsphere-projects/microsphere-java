package io.github.microsphere.commons.i18n.spring.beans;

import io.github.microsphere.commons.i18n.spring.beans.factory.ServiceMessageSourceFactoryBean;
import org.springframework.context.annotation.Bean;

/**
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class TestServiceMessageSourceConfiguration {

    @Bean
    public static ServiceMessageSourceFactoryBean testServiceMessageSource() {
        return new ServiceMessageSourceFactoryBean("test");
    }
}
