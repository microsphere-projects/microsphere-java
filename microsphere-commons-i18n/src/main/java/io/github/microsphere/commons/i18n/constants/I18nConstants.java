package io.github.microsphere.commons.i18n.constants;

import io.github.microsphere.commons.i18n.ServiceMessageSource;

import java.util.Locale;

/**
 * 国际化属性常量
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface I18nConstants {

    String PROPERTY_NAME_PREFIX = "microsphere.i18n.";

    /**
     * 激活 配置名称
     */
    String ENABLED_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "enabled";

    /**
     * 默认激活
     */
    boolean DEFAULT_ENABLED = true;

    /**
     * 默认 {@link Locale} 属性名称
     */
    String DEFAULT_LOCALE_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "default-locale";

    /**
     * 支持的 {@link Locale} 列表属性名称
     */
    String SUPPORTED_LOCALES_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "supported-locales";

    /**
     * Message Code 模式前缀
     */
    String MESSAGE_PATTERN_PREFIX = "{";

    /**
     * Message Code 模式后缀
     */
    String MESSAGE_PATTERN_SUFFIX = "}";

    /**
     * 通用 {@link ServiceMessageSource} Bean 名称
     */
    String COMMON_SERVICE_MESSAGE_SOURCE_BEAN_NAME = "commonServiceMessageSource";

    /**
     * 通用 {@link ServiceMessageSource} Bean 优先级
     */
    int COMMON_SERVICE_MESSAGE_SOURCE_ORDER = 500;

    /**
     * 主 {@link ServiceMessageSource} Bean
     */
    String SERVICE_MESSAGE_SOURCE_BEAN_NAME = "serviceMessageSource";
}
