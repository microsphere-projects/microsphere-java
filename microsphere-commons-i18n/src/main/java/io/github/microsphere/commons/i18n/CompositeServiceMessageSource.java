package io.github.microsphere.commons.i18n;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static java.util.Collections.unmodifiableList;

/**
 * {@link ServiceMessageSource} 组合实现
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class CompositeServiceMessageSource extends AbstractServiceMessageSource implements SmartInitializingSingleton {

    private final ObjectProvider<ServiceMessageSource> serviceMessageSourcesProvider;

    private List<ServiceMessageSource> serviceMessageSources;

    public CompositeServiceMessageSource(ObjectProvider<ServiceMessageSource> serviceMessageSourcesProvider) {
        super("Composite");
        this.serviceMessageSourcesProvider = serviceMessageSourcesProvider;
    }

    @Override
    public void afterSingletonsInstantiated() {
        List<ServiceMessageSource> serviceMessageSources = initServiceMessageSources();
        setServiceMessageSources(serviceMessageSources);

        Locale defaultLocale = initDefaultLocale(serviceMessageSources);
        setDefaultLocale(defaultLocale);

        List<Locale> supportedLocales = initSupportedLocales(serviceMessageSources);
        setSupportedLocales(supportedLocales);
    }

    public void setServiceMessageSources(List<ServiceMessageSource> serviceMessageSources) {
        this.serviceMessageSources = serviceMessageSources;
        logger.debug("Source '{}' 设置 ServiceMessageSource 列表： {}", serviceMessageSources);
    }

    protected Locale resolveLocale(Locale locale) {

        Locale defaultLocale = getDefaultLocale();

        if (locale == null || Objects.equals(defaultLocale, locale)) { // 如果为默认 Locale的话
            return defaultLocale;
        }

        if (supports(locale)) { // 如果匹配支持 Locale 列表
            return locale;
        }

        Locale resolvedLocale = null;

        List<Locale> derivedLocales = resolveDerivedLocales(locale);
        for (Locale derivedLocale : derivedLocales) {
            if (supports(derivedLocale)) {
                resolvedLocale = derivedLocale;
                break;
            }
        }

        return resolvedLocale == null ? defaultLocale : resolvedLocale;
    }

    @Override
    protected String getInternalMessage(String code, String resolvedCode, Locale locale, Locale resolvedLocale, Object... args) {
        String message = null;
        for (ServiceMessageSource serviceMessageSource : serviceMessageSources) {
            message = serviceMessageSource.getMessage(resolvedCode, resolvedLocale, args);
            if (message != null) {
                break;
            }
        }

        if (message == null) {
            Locale defaultLocale = getDefaultLocale();
            if (!Objects.equals(defaultLocale, resolvedLocale)) { // 使用默认 Locale 作为兜底
                message = getInternalMessage(resolvedCode, resolvedCode, defaultLocale, defaultLocale, args);
            }
        }

        return message;
    }

    private Locale initDefaultLocale(List<ServiceMessageSource> serviceMessageSources) {
        return serviceMessageSources.isEmpty() ? super.getDefaultLocale() : serviceMessageSources.get(0).getDefaultLocale();
    }

    private List<Locale> initSupportedLocales(List<ServiceMessageSource> serviceMessageSources) {
        List<Locale> allSupportedLocales = new LinkedList<>();
        for (ServiceMessageSource serviceMessageSource : serviceMessageSources) {
            for (Locale supportedLocale : serviceMessageSource.getSupportedLocales()) {
                allSupportedLocales.add(supportedLocale);
            }
        }
        return unmodifiableList(allSupportedLocales);
    }

    private List<ServiceMessageSource> initServiceMessageSources() {
        List<ServiceMessageSource> serviceMessageSources = new LinkedList<>();
        for (ServiceMessageSource serviceMessageSource : serviceMessageSourcesProvider) {
            if (serviceMessageSource != this) {
                serviceMessageSources.add(serviceMessageSource);
            }
        }
        AnnotationAwareOrderComparator.sort(serviceMessageSources);
        logger.debug("初始化 ServiceMessageSource Bean 列表 : {}", serviceMessageSources);
        return unmodifiableList(serviceMessageSources);
    }

    @Override
    public String toString() {
        return "CompositeServiceMessageSource{" +
                "serviceMessageSources=" + serviceMessageSources +
                '}';
    }
}
