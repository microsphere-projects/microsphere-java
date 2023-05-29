package io.github.microsphere.i18n;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

/**
 * Resource {@link ServiceMessageSource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface ResourceServiceMessageSource extends ServiceMessageSource {

    /**
     * Gets a list of all {@link Locale} resources
     *
     * @return
     */
    Set<String> getResources();

    /**
     * Gets the resource content character encoding
     *
     * @return The default is utf-8
     */
    default Charset getEncoding() {
        return StandardCharsets.UTF_8;
    }
}
