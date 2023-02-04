package io.github.microsphere.i18n.feign;

import feign.RequestTemplate;
import io.github.microsphere.commons.i18n.feign.AcceptLanguageHeaderRequestInterceptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

import static io.github.microsphere.commons.i18n.feign.AcceptLanguageHeaderRequestInterceptor.HEADER_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link AcceptLanguageHeaderRequestInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class AcceptLanguageHeaderRequestInterceptorTest {

    private AcceptLanguageHeaderRequestInterceptor requestInterceptor;

    private RequestTemplate requestTemplate;

    @Before
    public void before() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HEADER_NAME, "en");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        this.requestInterceptor = new AcceptLanguageHeaderRequestInterceptor();
        this.requestTemplate = new RequestTemplate();
    }

    @After
    public void after(){
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void testApply() {
        assertTrue(requestTemplate.headers().isEmpty());
        requestInterceptor.apply(requestTemplate);
        assertEquals(Arrays.asList("en"), requestTemplate.headers().get("Accept-Language"));
    }

    @Test
    public void testApplyNoWebMvc() {
        RequestContextHolder.resetRequestAttributes();
        assertTrue(requestTemplate.headers().isEmpty());
        requestInterceptor.apply(new RequestTemplate());
        assertTrue(requestTemplate.headers().isEmpty());
    }
}
