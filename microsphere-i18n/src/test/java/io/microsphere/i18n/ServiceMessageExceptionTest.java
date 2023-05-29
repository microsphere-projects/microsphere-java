/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.i18n;

import io.microsphere.i18n.util.I18nUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Arrays;
import java.util.Locale;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link ServiceMessageException} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ServiceMessageExceptionTest {

    @BeforeClass
    public static void init() {
        LocaleContextHolder.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        DefaultServiceMessageSource serviceMessageSource = new DefaultServiceMessageSource("test");
        serviceMessageSource.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        serviceMessageSource.setSupportedLocales(asList(Locale.SIMPLIFIED_CHINESE));
        serviceMessageSource.init();
        I18nUtils.setServiceMessageSource(serviceMessageSource);
    }

    @AfterClass
    public static void afterClass() {
        I18nUtils.setServiceMessageSource(null);
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    public void test() {
        assertServiceMessageException("测试-a", "{a}");
        assertServiceMessageException("您好,World", "{hello}", "World");
    }

    private void assertServiceMessageException(String localizedMessage, String message, Object... args) {
        ServiceMessageException exception = new ServiceMessageException(message, args);
        assertTrue(exception instanceof RuntimeException);
        assertEquals(message, exception.getMessage());
        assertEquals(localizedMessage, exception.getLocalizedMessage());
        assertEquals(format("ServiceMessageException[message='%s', args=%s, localized message='%s']", message, Arrays.toString(args), localizedMessage), exception.toString());
    }

}
