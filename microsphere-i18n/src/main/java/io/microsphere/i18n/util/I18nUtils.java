package io.microsphere.i18n.util;

import io.microsphere.i18n.EmptyServiceMessageSource;
import io.microsphere.i18n.ServiceMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internationalization Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class I18nUtils {

    private static final Logger logger = LoggerFactory.getLogger(I18nUtils.class);

    private static volatile ServiceMessageSource serviceMessageSource;

    public static ServiceMessageSource serviceMessageSource() {
        if (serviceMessageSource == null) {
            logger.warn("serviceMessageSource is not initialized, EmptyServiceMessageSource will be used");
            return EmptyServiceMessageSource.INSTANCE;
        }
        return serviceMessageSource;
    }

    public static void setServiceMessageSource(ServiceMessageSource serviceMessageSource) {
        I18nUtils.serviceMessageSource = serviceMessageSource;
        logger.debug("serviceMessageSource is initialized");
    }

    public static void destroyServiceMessageSource() {
        serviceMessageSource = null;
        logger.debug("serviceMessageSource is destroyed");
    }
}
