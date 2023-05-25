package io.github.microsphere.i18n.spring.beans.factory;

import io.github.microsphere.i18n.AbstractServiceMessageSource;
import io.github.microsphere.i18n.ReloadableResourceServiceMessageSource;
import io.github.microsphere.i18n.ServiceMessageSource;
import io.github.microsphere.i18n.constants.I18nConstants;
import io.github.microsphere.i18n.spring.context.ResourceServiceMessageSourceChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.springframework.beans.BeanUtils.instantiateClass;
import static org.springframework.util.ClassUtils.getConstructorIfAvailable;
import static org.springframework.util.ClassUtils.resolveClassName;
import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;
import static org.springframework.util.StringUtils.hasText;

/**
 * {@link ServiceMessageSource} {@link FactoryBean} Implementation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public final class ServiceMessageSourceFactoryBean extends AbstractServiceMessageSource implements FactoryBean<ServiceMessageSource>,
        ApplicationListener<ResourceServiceMessageSourceChangedEvent>, InitializingBean, EnvironmentAware, BeanClassLoaderAware, DisposableBean, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(ServiceMessageSourceFactoryBean.class);

    private ClassLoader classLoader;

    private ConfigurableEnvironment environment;

    private List<AbstractServiceMessageSource> serviceMessageSources;

    private int order;

    public ServiceMessageSourceFactoryBean(String source) {
        super(source);
    }

    @Override
    public ServiceMessageSource getObject() throws Exception {
        return this;
    }

    @Override
    public Class<?> getObjectType() {
        return ServiceMessageSource.class;
    }

    @Override
    public void init() {
        if (this.serviceMessageSources == null) {
            this.serviceMessageSources = loadServiceMessageSources();
        }
    }

    @Override
    public void destroy() {
        serviceMessageSources.forEach(ServiceMessageSource::destroy);
    }

    @Override
    protected String getInternalMessage(String code, String resolvedCode, Locale locale, Locale resolvedLocale, Object... args) {
        String message = null;
        for (AbstractServiceMessageSource serviceMessageSource : serviceMessageSources) {
            message = serviceMessageSource.getMessage(resolvedCode, resolvedLocale, args);
            if (message != null) {
                break;
            }
        }
        if (message == null && logger.isDebugEnabled()) {
            logger.debug("Source '{}' Message not found[code : '{}' , resolvedCode : '{}' , locale : '{}' , resolvedLocale : '{}', args : '{}']",
                    source, code, resolvedCode, locale, resolvedLocale, arrayToCommaDelimitedString(args));
        }
        return message;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setEnvironment(Environment environment) {
        Assert.isInstanceOf(ConfigurableEnvironment.class, environment, "The 'environment' parameter must be of type ConfigurableEnvironment");
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    private List<AbstractServiceMessageSource> loadServiceMessageSources() {
        List<String> factoryNames = SpringFactoriesLoader.loadFactoryNames(AbstractServiceMessageSource.class, classLoader);

        Locale defaultLocale = initDefaultLocale(environment);
        List<Locale> supportedLocales = initSupportedLocales(environment);

        setDefaultLocale(defaultLocale);
        setSupportedLocales(supportedLocales);

        List<AbstractServiceMessageSource> serviceMessageSources = new ArrayList<>(factoryNames.size());

        for (String factoryName : factoryNames) {
            Class<?> factoryClass = resolveClassName(factoryName, classLoader);
            Constructor constructor = getConstructorIfAvailable(factoryClass, String.class);
            AbstractServiceMessageSource serviceMessageSource = (AbstractServiceMessageSource) instantiateClass(constructor, source);
            serviceMessageSources.add(serviceMessageSource);

            if (serviceMessageSource instanceof EnvironmentAware) {
                ((EnvironmentAware) serviceMessageSource).setEnvironment(environment);
            }
            serviceMessageSource.setDefaultLocale(defaultLocale);
            serviceMessageSource.setSupportedLocales(supportedLocales);
            serviceMessageSource.init();
        }

        AnnotationAwareOrderComparator.sort(serviceMessageSources);

        return serviceMessageSources;
    }

    private Locale initDefaultLocale(ConfigurableEnvironment environment) {
        String propertyName = I18nConstants.DEFAULT_LOCALE_PROPERTY_NAME;
        String localeValue = environment.getProperty(propertyName);
        final Locale locale;
        if (!hasText(localeValue)) {
            locale = super.getDefaultLocale();
            logger.debug("Default Locale configuration property [name : '{}'] not found, use default value: '{}'", propertyName, locale);
        } else {
            locale = StringUtils.parseLocale(localeValue);
            logger.debug("Default Locale : '{}' parsed by configuration properties [name : '{}']", propertyName, locale);
        }
        return locale;
    }

    private List<Locale> initSupportedLocales(ConfigurableEnvironment environment) {
        final List<Locale> supportedLocales;
        String propertyName = I18nConstants.SUPPORTED_LOCALES_PROPERTY_NAME;
        List<String> locales = environment.getProperty(propertyName, List.class, emptyList());
        if (locales.isEmpty()) {
            supportedLocales = super.getSupportedLocales();
            logger.debug("Support Locale list configuration property [name : '{}'] not found, use default value: {}", propertyName, supportedLocales);
        } else {
            supportedLocales = locales.stream().map(StringUtils::parseLocale).collect(Collectors.toList());
            logger.debug("List of supported Locales parsed by configuration property [name : '{}']: {}", propertyName, supportedLocales);
        }
        return unmodifiableList(supportedLocales);
    }

    @Override
    public void onApplicationEvent(ResourceServiceMessageSourceChangedEvent event) {
        Iterable<String> changedResources = event.getChangedResources();
        logger.debug("Receive event change resource: {}", changedResources);
        for (AbstractServiceMessageSource serviceMessageSource : serviceMessageSources) {
            if (serviceMessageSource instanceof ReloadableResourceServiceMessageSource) {
                ReloadableResourceServiceMessageSource reloadableResourceServiceMessageSource = (ReloadableResourceServiceMessageSource) serviceMessageSource;
                if (reloadableResourceServiceMessageSource.canReload(changedResources)) {
                    reloadableResourceServiceMessageSource.reload();
                    logger.debug("change resource [{}] activate {} reloaded", changedResources, reloadableResourceServiceMessageSource);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "ServiceMessageSourceFactoryBean{" +
                "serviceMessageSources=" + serviceMessageSources +
                ", order=" + order +
                '}';
    }
}
