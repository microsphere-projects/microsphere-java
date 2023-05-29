package io.microsphere.i18n;

import java.util.Locale;

/**
 * Empty {@link ServiceMessageSource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class EmptyServiceMessageSource implements ServiceMessageSource {

    public static final EmptyServiceMessageSource INSTANCE = new EmptyServiceMessageSource();

    private EmptyServiceMessageSource() {
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
    }

    @Override
    public String getMessage(String code, Locale locale, Object... args) {
        return null;
    }

    @Override
    public String getSource() {
        return "Empty";
    }
}
