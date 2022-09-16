package io.github.microsphere.commons.i18n;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static java.util.Collections.*;
import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;

/**
 * 抽象 {@link ServiceMessageSource} 资源实现
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class AbstractResourceServiceMessageSource extends AbstractServiceMessageSource implements ResourceServiceMessageSource {

    /**
     * Message 资源名称前缀
     */
    protected static final String RESOURCE_NAME_PREFIX = "i18n_messages_";

    /**
     * Message 资源名称后缀
     */
    protected static final String RESOURCE_NAME_SUFFIX = ".properties";

    private volatile Map<Locale, Map<String, String>> localizedMessages = emptyMap();

    private volatile Set<String> resources = emptySet();

    public AbstractResourceServiceMessageSource(String source) {
        super(source);
    }

    @Override
    public void init() {
        Assert.notNull(this.source, "'source' 属性必须在初始化前赋值！");
        initialize();
    }

    @Override
    public void destroy() {
        clearAllMessages();
    }

    protected String resolveMessageCode(String code) {
        if (code.startsWith(codePrefix)) { // 完整的 Message code
            return code;
        }
        return codePrefix + code;
    }

    @Override
    protected String getInternalMessage(String code, String resolvedCode, Locale locale, Locale resolvedLocale, Object[] args) {
        String message = null;
        Map<String, String> messages = getMessages(resolvedLocale);
        String messagePattern = messages.get(resolvedCode);
        if (messagePattern != null) {
            message = formatMessage(messagePattern, args);
            logMessage(code, resolvedCode, locale, resolvedLocale, args, messagePattern, message);
        }
        return message;
    }

    protected void logMessage(String code, String resolvedCode, Locale locale, Locale resolvedLocale, Object[] args,
                              String messagePattern, String message) {
        if (logger.isDebugEnabled()) {
            logger.debug("Source '{}' 获取 Message[code : '{}' , resolvedCode : '{}' , locale : '{}' , resolvedLocale : '{}', args : '{}' , pattern : '{}'] : '{}'",
                    source, code, resolvedCode, locale, resolvedLocale, arrayToCommaDelimitedString(args), messagePattern, message);
        }
    }


    @Override
    public Set<String> getResources() {
        return unmodifiableSet(resources);
    }


    /**
     * 初始化
     */
    protected final void initialize() {
        List<Locale> supportedLocales = getSupportedLocales();
        assertSupportedLocales(supportedLocales);
        Map<Locale, Map<String, String>> localizedMessages = new HashMap<>(supportedLocales.size());
        Set<String> resources = new LinkedHashSet<>();
        for (Locale resolveLocale : supportedLocales) {
            localizedMessages.computeIfAbsent(resolveLocale, locale -> {
                String resource = getResource(locale);
                Map<String, String> messages = loadMessages(locale, resource);
                validateMessages(messages, resource);
                resources.add(resource);
                if (!CollectionUtils.isEmpty(messages)) {
                    logger.debug("Source '{}' 加载 Locale '{}' 资源['{}']中的 messages : {}", source, locale, resource, messages);
                    localizedMessages.put(locale, messages);
                } else {
                    logger.debug("Source '{}' 未找到 Locale '{}' 资源['{}']中的 messages", source, locale, resource);
                }
                return messages;
            });
        }
        // 更新字段
        synchronized (this) {
            this.localizedMessages = localizedMessages;
            this.resources = resources;
        }
        logger.debug("Source '{}' 初始化完成 , resources : {} , localizedMessages : {}", source, resources, localizedMessages);
    }

    private void assertSupportedLocales(List<Locale> supportedLocales) {
        if (CollectionUtils.isEmpty(supportedLocales)) {
            throw new IllegalStateException(slf4jFormat("{}.getSupportedLocales() 方法不能返回 Locale 空列表!", this.getClass()));
        }
    }

    protected final void clearAllMessages() {
        this.localizedMessages.clear();
        this.resources.clear();
        this.localizedMessages = null;
        this.resources = null;
    }

    private void validateMessages(Map<String, String> messages, String resourceName) {
        messages.forEach((code, message) -> validateMessageCode(code, resourceName));
    }

    protected void validateMessageCode(String code, String resourceName) {
        validateMessageCodePrefix(code, resourceName);
    }

    private void validateMessageCodePrefix(String code, String resourceName) {
        if (!code.startsWith(codePrefix)) {
            throw new IllegalStateException(slf4jFormat("Source '{}' Message资源[name : '{}'] code '{}' 必须以 '{}' 为前缀",
                    source, resourceName, code, codePrefix));
        }
    }

    private String getResource(Locale locale) {
        String resourceName = getResourceName(locale);
        return getResource(resourceName);
    }

    private String getResourceName(Locale locale) {
        return RESOURCE_NAME_PREFIX + locale + RESOURCE_NAME_SUFFIX;
    }

    protected abstract String getResource(String resourceName);

    protected abstract Map<String, String> loadMessages(Locale locale, String resource);

    protected final Map<String, String> getMessages(Locale locale) {
        return localizedMessages.getOrDefault(locale, emptyMap());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getClass().getSimpleName())
                .append("{source='").append(source).append('\'')
                .append(", resources=").append(resources)
                .append(", defaultLocale=").append(getDefaultLocale())
                .append(", supportedLocales=").append(getSupportedLocales())
                .append(", localizedMessages=").append(localizedMessages)
                .append('}');
        return sb.toString();
    }
}
