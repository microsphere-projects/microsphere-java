package io.github.microsphere.commons.i18n.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.servlet.http.HttpServletRequest;

/**
 * HTTP 请求头 "Accept-Language"
 * {@link RequestInterceptor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AcceptHeaderLocaleResolver
 * @since 1.0.0
 */
public class AcceptLanguageHeaderRequestInterceptor implements RequestInterceptor {

    public static final String HEADER_NAME = "Accept-Language";
    private static final Logger logger = LoggerFactory.getLogger(AcceptLanguageHeaderRequestInterceptor.class);

    @Override
    public void apply(RequestTemplate template) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            logger.debug("非 Spring WebMVC 场景 Feign 调用，忽略设置请求头: '{}'", HEADER_NAME);
            return;
        }

        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;

        HttpServletRequest request = servletRequestAttributes.getRequest();

        String acceptLanguage = request.getHeader(HEADER_NAME);

        if (StringUtils.hasText(acceptLanguage)) {
            template.header(HEADER_NAME, acceptLanguage);
            logger.debug("Feign 已设置 HTTP 请求头[name : '{}' , value : '{}']", HEADER_NAME, acceptLanguage);
        } else {
            logger.debug("Feign 无法设置 HTTP 请求头[name : '{}']，因为请求方未传递： '{}'", HEADER_NAME, acceptLanguage);
        }

    }
}
