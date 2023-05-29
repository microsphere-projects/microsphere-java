package io.github.microsphere.i18n;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;

/**
 * Service internationalization message source
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface ServiceMessageSource {

    /**
     * Common internationalizing message sources
     */
    String COMMON_SOURCE = "common";

    /**
     * Initialize the life cycle
     */
    void init();

    /**
     * Destruction life cycle
     */
    void destroy();

    /**
     * Getting international Messages
     *
     * @param code   message Code
     * @param locale {@link Locale}
     * @param args   the argument of message pattern
     * @return 如果获取到，返回器内容，获取不到，返回 <code>null</code>
     */
    @Nullable
    String getMessage(String code, Locale locale, Object... args);

    default String getMessage(String code, Object... args) {
        return getMessage(code, getLocale(), args);
    }

    /**
     * Get the runtime {@link Locale}
     *
     * @return {@link Locale}
     */
    @NonNull
    default Locale getLocale() {
        Locale locale = LocaleContextHolder.getLocale();
        return locale == null ? getDefaultLocale() : locale;
    }

    /**
     * Get the default {@link Locale}
     *
     * @return {@link Locale#SIMPLIFIED_CHINESE} as default
     */
    @NonNull
    default Locale getDefaultLocale() {
        return Locale.SIMPLIFIED_CHINESE;
    }

    /**
     * Gets a list of supported {@link Locale}
     *
     * @return Non-null {@link List}, simplified Chinese and English by default
     */
    @NonNull
    default List<Locale> getSupportedLocales() {
        return asList(getDefaultLocale(), Locale.ENGLISH);
    }

    /**
     * Message service source
     *
     * @return The application name or {@link #COMMON_SOURCE}
     */
    default String getSource() {
        return COMMON_SOURCE;
    }
}
