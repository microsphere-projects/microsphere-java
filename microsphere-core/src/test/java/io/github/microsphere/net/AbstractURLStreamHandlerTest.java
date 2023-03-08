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
package io.github.microsphere.net;

import io.github.microsphere.net.handler.console.Handler;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

/**
 * {@link AbstractURLStreamHandler} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class AbstractURLStreamHandlerTest {

    @Test
    public void test() throws Throwable {
        Handler handler = new Handler();
        assertEquals("io.github.microsphere.net.handler", AbstractURLStreamHandler.getHandlePackages());

        String spec = "console:text://localhost:12345/abc;ref=top?n=1#hash";
        URL url = new URL(spec);
        assertSame(url.openStream(), handler.openConnection(url).getInputStream());
        assertEquals(spec, url.toString());

        spec = "console:text://localhost:12345/abc;ref=top?n=1";
        url = new URL(spec);
        assertSame(url.openStream(), handler.openConnection(url).getInputStream());
        assertEquals(spec, url.toString());

        spec = "console:text://localhost:12345/abc?n=1";
        url = new URL(spec);
        assertSame(url.openStream(), handler.openConnection(url).getInputStream());
        assertEquals(spec, url.toString());

        spec = "console:text://localhost:12345/abc";
        url = new URL(spec);
        assertSame(url.openStream(), handler.openConnection(url).getInputStream());
        assertEquals(spec, url.toString());

        spec = "console://localhost:12345/abc";
        url = new URL(spec);
        assertSame(url.openStream(), handler.openConnection(url).getInputStream());
        assertEquals(spec, url.toString());
    }

    @Test
    public void testClassPathProtocol() throws Throwable {
        new io.github.microsphere.net.classpath.Handler();
        URL url = new URL("classpath://META-INF/test.properties");
        Properties properties = new Properties();
        properties.load(new InputStreamReader(url.openStream(), "UTF-8"));
        assertEquals("测试名称", properties.get("name"));

        url = new URL("classpath:////META-INF/services/java.lang.CharSequence");
        assertNotNull(url.openStream());

        url = new URL("classpath:///META-INF/services/java.lang.CharSequence");
        assertNotNull(url.openStream());

        url = new URL("classpath://META-INF/services/java.lang.CharSequence");
        assertNotNull(url.openStream());

        url = new URL("classpath:/META-INF/services/java.lang.CharSequence");
        assertNotNull(url.openConnection());

        url = new URL("classpath:META-INF/services/java.lang.CharSequence");
        assertNotNull(url.openConnection(Proxy.NO_PROXY));
    }

    @Test(expected = IOException.class)
    public void testClassPathProtocolOnResourceNotFound() throws Throwable {
        new io.github.microsphere.net.classpath.Handler();
        URL url = new URL("classpath://META-INF/not-found.res");
        url.openStream();
    }
}
