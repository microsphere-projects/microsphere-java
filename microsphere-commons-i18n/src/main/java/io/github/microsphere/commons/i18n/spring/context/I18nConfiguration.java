package io.github.microsphere.commons.i18n.spring.context;

import io.github.microsphere.commons.i18n.CompositeServiceMessageSource;
import io.github.microsphere.commons.i18n.ServiceMessageSource;
import io.github.microsphere.commons.i18n.constants.I18nConstants;
import io.github.microsphere.commons.i18n.spring.beans.factory.ServiceMessageSourceFactoryBean;
import io.github.microsphere.commons.i18n.util.I18nUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Locale;

import static io.github.microsphere.commons.i18n.constants.I18nConstants.*;

/**
 * Internationalization Configuration class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class I18nConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(I18nConfiguration.class);

    @Autowired
    @Qualifier(I18nConstants.SERVICE_MESSAGE_SOURCE_BEAN_NAME)
    public void init(ServiceMessageSource serviceMessageSource) {
        I18nUtils.setServiceMessageSource(serviceMessageSource);
    }

    @PreDestroy
    public static void destroy() {
        I18nUtils.destroyServiceMessageSource();
    }

    @Bean(name = COMMON_SERVICE_MESSAGE_SOURCE_BEAN_NAME)
    public ServiceMessageSourceFactoryBean commonServiceMessageSource() {
        ServiceMessageSourceFactoryBean factoryBean = new ServiceMessageSourceFactoryBean(ServiceMessageSource.COMMON_SOURCE);
        factoryBean.setOrder(COMMON_SERVICE_MESSAGE_SOURCE_ORDER);
        return factoryBean;
    }

    @Bean(name = SERVICE_MESSAGE_SOURCE_BEAN_NAME)
    @Primary
    public CompositeServiceMessageSource serviceMessageSource(ObjectProvider<ServiceMessageSource> serviceMessageSources) {
        return new CompositeServiceMessageSource(serviceMessageSources);
    }

    @Bean
    @Qualifier(SERVICE_MESSAGE_SOURCE_BEAN_NAME)
    @Primary
    public MessageSourceAdapter messageSourceAdapter(ServiceMessageSource serviceMessageSource,
                                                     ObjectProvider<MessageSource> messageSourceProvider) {
        return new MessageSourceAdapter(serviceMessageSource, messageSourceProvider);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshedEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        processAcceptHeaderLocaleContextResolver(context);
    }

    private void processAcceptHeaderLocaleContextResolver(ApplicationContext context) {
        ObjectProvider<AcceptHeaderLocaleResolver> localeContextResolverProvider = context.getBeanProvider(AcceptHeaderLocaleResolver.class);
        localeContextResolverProvider.ifAvailable(localeContextResolver -> {
            ObjectProvider<ServiceMessageSource> serviceMessageSourceProvider = context.getBeanProvider(ServiceMessageSource.class);
            serviceMessageSourceProvider.ifAvailable(serviceMessageSource -> {
                Locale defaultLocale = serviceMessageSource.getDefaultLocale();
                List<Locale> supportedLocales = serviceMessageSource.getSupportedLocales();
                localeContextResolver.setDefaultLocale(defaultLocale);
                localeContextResolver.setSupportedLocales(supportedLocales);
                logger.debug("AcceptHeaderLocaleResolver Bean associated with default Locale : '{}' , list of supported Locales : {}", defaultLocale, supportedLocales);
            });
        });
    }

    public static boolean isEnabled(ApplicationContext context) {
        Environment environment = context.getEnvironment();
        String propertyName = ENABLED_PROPERTY_NAME;
        boolean enabled = environment.getProperty(propertyName, boolean.class, DEFAULT_ENABLED);
        logger.debug("Application context [id: '{}'] {} i18n, configure Spring properties ['{}' = {}]", context.getId(),
                enabled ? "Enabled" : "Disabled",
                propertyName,
                enabled);
        return enabled;
    }
}
