package io.github.microsphere.i18n.spring;

import io.github.microsphere.i18n.PropertiesResourceServiceMessageSource;
import io.github.microsphere.i18n.ReloadableResourceServiceMessageSource;
import io.github.microsphere.i18n.ServiceMessageSource;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySources;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.springframework.util.StringUtils.hasText;

/**
 * Spring {@link PropertySources} {@link ServiceMessageSource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class PropertySourcesServiceMessageSource extends PropertiesResourceServiceMessageSource implements ReloadableResourceServiceMessageSource, EnvironmentAware {

    private Environment environment;

    public PropertySourcesServiceMessageSource(String source) {
        super(source);
    }

    @Override
    protected String getResource(String resourceName) {
        return getSource() + "." + resourceName;
    }

    @Override
    protected List<Reader> loadAllPropertiesResources(Locale locale, String resource) throws IOException {
        String propertyName = resource;
        String propertiesContent = environment.getProperty(propertyName);
        return hasText(propertiesContent) ? asList(new StringReader(propertiesContent)) : emptyList();
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
