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
 * The Composite class of {@link ServiceMessageSource}
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
        logger.debug("Source '{}' sets ServiceMessageSource list： {}", serviceMessageSources);
    }

    protected Locale resolveLocale(Locale locale) {

        Locale defaultLocale = getDefaultLocale();

        if (locale == null || Objects.equals(defaultLocale, locale)) { // If it's the default Locale
            return defaultLocale;
        }

        if (supports(locale)) { // If it matches the supported Locale list
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
            if (!Objects.equals(defaultLocale, resolvedLocale)) { // Use the default Locale as the bottom pocket
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
        logger.debug("Initializes the ServiceMessageSource Bean list : {}", serviceMessageSources);
        return unmodifiableList(serviceMessageSources);
    }

    @Override
    public String toString() {
        return "CompositeServiceMessageSource{" +
                "serviceMessageSources=" + serviceMessageSources +
                '}';
    }
}
