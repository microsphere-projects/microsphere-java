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
 * HTTP Header "Accept-Language" {@link RequestInterceptor}
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
            logger.debug("Feign calls in non-Spring WebMVC scenarios, ignoring setting request headers: '{}'", HEADER_NAME);
            return;
        }

        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;

        HttpServletRequest request = servletRequestAttributes.getRequest();

        String acceptLanguage = request.getHeader(HEADER_NAME);

        if (StringUtils.hasText(acceptLanguage)) {
            template.header(HEADER_NAME, acceptLanguage);
            logger.debug("Feign has set HTTP request header [name : '{}' , value : '{}']", HEADER_NAME, acceptLanguage);
        } else {
            logger.debug("Feign could not set HTTP request header [name : '{}'] because the requester did not pass: '{}'", HEADER_NAME, acceptLanguage);
        }

    }
}
