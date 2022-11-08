package io.github.microsphere.commons.i18n.constants;

import io.github.microsphere.commons.i18n.ServiceMessageSource;

import java.util.Locale;

/**
 * Internationalization property constants
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface I18nConstants {

    String PROPERTY_NAME_PREFIX = "microsphere.i18n.";

    /**
     * Enabled Configuration Name
     */
    String ENABLED_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "enabled";

    /**
     * Enabled By Default
     */
    boolean DEFAULT_ENABLED = true;

    /**
     * Default {@link Locale} property name
     */
    String DEFAULT_LOCALE_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "default-locale";

    /**
     * Supported {@link Locale} list property names
     */
    String SUPPORTED_LOCALES_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "supported-locales";

    /**
     * Message Code pattern prefix
     */
    String MESSAGE_PATTERN_PREFIX = "{";

    /**
     * Message Code pattern suffix
     */
    String MESSAGE_PATTERN_SUFFIX = "}";

    /**
     * Generic {@link ServiceMessageSource} bean name
     */
    String COMMON_SERVICE_MESSAGE_SOURCE_BEAN_NAME = "commonServiceMessageSource";

    /**
     * Generic {@link ServiceMessageSource} Bean Priority
     */
    int COMMON_SERVICE_MESSAGE_SOURCE_ORDER = 500;

    /**
     * Primary {@link ServiceMessageSource} Bean
     */
    String SERVICE_MESSAGE_SOURCE_BEAN_NAME = "serviceMessageSource";
}
