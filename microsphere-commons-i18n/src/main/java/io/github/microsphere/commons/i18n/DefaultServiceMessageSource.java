package io.github.microsphere.commons.i18n;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * 默认 {@link ServiceMessageSource} 实现
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class DefaultServiceMessageSource extends PropertiesResourceServiceMessageSource {

    /**
     * 资源路径模式
     */
    protected static final String RESOURCE_LOCATION_PATTERN = "META-INF/i18n/{}/{}";

    public DefaultServiceMessageSource(String source) {
        super(source);
    }

    protected String getResource(String resourceName) {
        return slf4jFormat(RESOURCE_LOCATION_PATTERN, getSource(), resourceName);
    }

    @Override
    protected List<Reader> loadAllPropertiesResources(Locale locale, String resource) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        Enumeration<URL> resources = classLoader.getResources(resource);
        List<Reader> propertiesResources = new LinkedList<>();
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            propertiesResources.add(new InputStreamReader(url.openStream(), getEncoding()));
        }
        return propertiesResources;
    }
}
