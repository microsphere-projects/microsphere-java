package io.github.microsphere.commons.i18n.util;

import io.github.microsphere.commons.i18n.EmptyServiceMessageSource;
import io.github.microsphere.commons.i18n.ServiceMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 国际化工具类
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class I18nUtils {

    private static final Logger logger = LoggerFactory.getLogger(I18nUtils.class);

    private static volatile ServiceMessageSource serviceMessageSource;

    public static ServiceMessageSource serviceMessageSource() {
        if (serviceMessageSource == null) {
            logger.warn("serviceMessageSource 尚未初始化，将使用 EmptyServiceMessageSource 对象");
            return EmptyServiceMessageSource.INSTANCE;
        }
        return serviceMessageSource;
    }

    public static void setServiceMessageSource(ServiceMessageSource serviceMessageSource) {
        I18nUtils.serviceMessageSource = serviceMessageSource;
        logger.debug("serviceMessageSource 已初始化");
    }

    public static void destroyServiceMessageSource() {
        serviceMessageSource = null;
        logger.debug("serviceMessageSource 已销毁");
    }
}
