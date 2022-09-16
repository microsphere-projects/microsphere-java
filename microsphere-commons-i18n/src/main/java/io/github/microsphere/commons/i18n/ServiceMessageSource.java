package io.github.microsphere.commons.i18n;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;

/**
 * 服务国际化消息源
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface ServiceMessageSource {

    /**
     * 通过国际化消息源
     */
    String COMMON_SOURCE = "common";

    /**
     * 初始化生命周期
     */
    void init();

    /**
     * 销毁生命周期
     */
    void destroy();

    /**
     * 获取国际化消息
     *
     * @param code   消息 Code
     * @param locale {@link Locale}
     * @param args   消息模板参数
     * @return 如果获取到，返回器内容，获取不到，返回 <code>null</code>
     */
    @Nullable
    String getMessage(String code, Locale locale, Object... args);

    default String getMessage(String code, Object... args) {
        return getMessage(code, getLocale(), args);
    }

    /**
     * 获取运行时 {@link Locale}
     *
     * @return {@link Locale}
     */
    @NonNull
    default Locale getLocale() {
        Locale locale = LocaleContextHolder.getLocale();
        return locale == null ? getDefaultLocale() : locale;
    }

    /**
     * 获取默认 {@link Locale}
     *
     * @return 默认简体中文
     */
    @NonNull
    default Locale getDefaultLocale() {
        return Locale.SIMPLIFIED_CHINESE;
    }

    /**
     * 获取支持的 {@link Locale} 列表
     *
     * @return 非 null {@link List}， 默认简体中文和英文
     */
    @NonNull
    default List<Locale> getSupportedLocales() {
        return asList(getDefaultLocale(), Locale.ENGLISH);
    }

    /**
     * 消息服务来源
     *
     * @return 应用名称 或 {@link #COMMON_SOURCE}
     */
    default String getSource() {
        return COMMON_SOURCE;
    }
}
