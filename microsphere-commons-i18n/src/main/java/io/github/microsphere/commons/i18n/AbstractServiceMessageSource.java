package io.github.microsphere.commons.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.Assert;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * 抽象 {@link ServiceMessageSource} 实现
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class AbstractServiceMessageSource implements ServiceMessageSource {

    /*
     * Message Source 分隔符
     */
    protected static final String SOURCE_SEPARATOR = ".";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final String source;

    protected final String codePrefix;

    private List<Locale> supportedLocales;

    private Locale defaultLocale;

    public AbstractServiceMessageSource(String source) {
        Assert.notNull(source, "'source' 参数不能为空");
        this.source = source;
        this.codePrefix = source + SOURCE_SEPARATOR;
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
    }

    @Override
    public final String getMessage(String code, Object... args) {
        return ServiceMessageSource.super.getMessage(code, args);
    }

    @Override
    public final String getMessage(String code, Locale locale, Object... args) {
        String message = null;
        if (code != null) {
            String resolvedCode = resolveMessageCode(code);
            if (resolvedCode != null) {
                Locale resolvedLocale = resolveLocale(locale);
                message = getInternalMessage(code, resolvedCode, locale, resolvedLocale, args);
            }
        }
        return message;
    }

    @Override
    public final Locale getLocale() {
        return ServiceMessageSource.super.getLocale();
    }

    @Override
    public final Locale getDefaultLocale() {
        if (defaultLocale != null) {
            return defaultLocale;
        }
        return ServiceMessageSource.super.getDefaultLocale();
    }

    @Override
    public final List<Locale> getSupportedLocales() {
        if (supportedLocales != null) {
            return supportedLocales;
        }
        return ServiceMessageSource.super.getSupportedLocales();
    }

    @Override
    public final String getSource() {
        return source;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
        LocaleContextHolder.setDefaultLocale(defaultLocale);
        logger.debug("Source '{}' 设置默认 Locale : '{}'", source, defaultLocale);
    }

    public void setSupportedLocales(List<Locale> supportedLocales) {
        this.supportedLocales = resolveLocales(supportedLocales);
        logger.debug("Source '{}' 设置支持的 Locale 列表 : {}", source, supportedLocales);
    }

    protected String resolveMessageCode(String code) {
        return code;
    }

    protected Locale resolveLocale(Locale locale) {
        return locale;
    }

    protected abstract String getInternalMessage(String code, String resolvedCode, Locale locale, Locale resolvedLocale, Object... args);

    protected boolean supports(Locale locale) {
        return getSupportedLocales().contains(locale);
    }

    protected static List<Locale> resolveLocales(List<Locale> supportedLocales) {
        List<Locale> resolvedLocales = new LinkedList<>();
        for (Locale supportedLocale : supportedLocales) {
            addLocale(resolvedLocales, supportedLocale);
            for (Locale derivedLocale : resolveDerivedLocales(supportedLocale)) {
                addLocale(resolvedLocales, derivedLocale);
            }
        }
        return unmodifiableList(resolvedLocales);
    }

    protected static void addLocale(List<Locale> locales, Locale locale) {
        if (!locales.contains(locale)) {
            locales.add(locale);
        }
    }

    protected static List<Locale> resolveDerivedLocales(Locale locale) {
        String language = locale.getLanguage();
        String region = locale.getCountry();
        String variant = locale.getVariant();

        boolean hasRegion = !isEmpty(region);
        boolean hasVariant = !isEmpty(variant);

        if (!hasRegion && !hasVariant) {
            return emptyList();
        }

        List<Locale> derivedLocales = new LinkedList<>();

        if (hasVariant) {
            derivedLocales.add(new Locale(language, region));
        }

        if (hasRegion) {
            derivedLocales.add(new Locale(language));
        }

        return derivedLocales;
    }


    protected String formatMessage(String message, Object... args) {
        // 复用 slf4j format，未来子类可能重新实现格式化
        return slf4jFormat(message, args);
    }

    /**
     * 复用 slf4j  API 格式化内容
     *
     * @param messagePattern 消息模板
     * @param args           模板参数
     * @return 格式化后的内容
     * @see MessageFormatter
     */
    protected static final String slf4jFormat(String messagePattern, Object... args) {
        String formattedMessage = MessageFormatter.arrayFormat(messagePattern, args).getMessage();
        return formattedMessage;
    }

}
