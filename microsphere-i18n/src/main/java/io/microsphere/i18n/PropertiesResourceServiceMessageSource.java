package io.github.microsphere.i18n;

import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static java.util.Collections.emptyMap;

/**
 * {@link Properties} Resource {@link ServiceMessageSource} Class
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
            throw new RuntimeException(slf4jFormat("Source '{}' Messages Properties Resource[locale : {}, name : {}] loading is failed", source, locale, resource), e);
        }
        return Collections.unmodifiableMap(messages);
    }

    private Properties loadAllProperties(Locale locale, String resource) throws IOException {
        List<Reader> propertiesResources = loadAllPropertiesResources(locale, resource);
        logger.debug("Source '{}' loads {} Properties Resources['{}']", source, propertiesResources.size(), resource);
        if (CollectionUtils.isEmpty(propertiesResources)) {
            return null;
        }
        Properties properties = new Properties();
        for (Reader propertiesResource : propertiesResources) {
            try (Reader reader = propertiesResource) {
                properties.load(reader);
            }
        }
        logger.debug("Source '{}' loads all Properties Resources[name :{}] : {}", source, resource, properties);
        return properties;
    }

    protected abstract String getResource(String resourceName);

    protected abstract List<Reader> loadAllPropertiesResources(Locale locale, String resource) throws IOException;
}
