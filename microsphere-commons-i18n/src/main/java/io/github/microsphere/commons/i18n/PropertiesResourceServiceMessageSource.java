package io.github.microsphere.commons.i18n;

import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

import static java.util.Collections.emptyMap;

/**
 * {@link Properties} 资源 {@link ServiceMessageSource} 实现
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class PropertiesResourceServiceMessageSource extends AbstractResourceServiceMessageSource {

    public PropertiesResourceServiceMessageSource(String source) {
        super(source);
    }

    @Override
    protected final Map<String, String> loadMessages(Locale locale, String resource) {
        Map<String, String> messages = emptyMap();
        try {
            Properties properties = loadAllProperties(locale, resource);
            if (!CollectionUtils.isEmpty(properties)) {
                messages = new HashMap<>(properties.size());
                messages.putAll((Map) properties);
            }
        } catch (IOException e) {
            throw new RuntimeException(slf4jFormat("Source '{}' Messages Properties 资源[locale : {}, name : {}] 读取失败", source, locale, resource), e);
        }
        return Collections.unmodifiableMap(messages);
    }

    private Properties loadAllProperties(Locale locale, String resource) throws IOException {
        List<Reader> propertiesResources = loadAllPropertiesResources(locale, resource);
        logger.debug("Source '{}' 加载 {} 个 Properties 资源['{}']", source, propertiesResources.size(), resource);
        if (CollectionUtils.isEmpty(propertiesResources)) {
            return null;
        }
        Properties properties = new Properties();
        for (Reader propertiesResource : propertiesResources) {
            try (Reader reader = propertiesResource) {
                properties.load(reader);
            }
        }
        logger.debug("Source '{}' 加载所有 Properties 资源[name :{}] : {}", source, resource, properties);
        return properties;
    }

    protected abstract String getResource(String resourceName);

    protected abstract List<Reader> loadAllPropertiesResources(Locale locale, String resource) throws IOException;
}
