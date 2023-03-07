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

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * {@link Handler} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class AbstractURLStreamHandlerTest {

    @Test
    public void test() throws Throwable {
        Handler handler = new Handler();
        assertEquals("io.github.microsphere.net.handler", AbstractURLStreamHandler.getHandlePackages());
        URL url = new URL("console:text://localhost:12345/abc?n=1;ref=top#hash");
        assertSame(url.openStream(), handler.openConnection(url).getInputStream());
        assertEquals("console://localhost:12345/abc?n=1;type=text;ref=top#hash", url.toString());

        url = new URL("console:text://localhost:12345/abc?n=1");
        assertSame(url.openStream(), handler.openConnection(url).getInputStream());
        assertEquals("console://localhost:12345/abc?n=1;type=text", url.toString());

        url = new URL("console://localhost:12345/abc?n=1;ref=top#hash");
        assertSame(url.openStream(), handler.openConnection(url).getInputStream());
        assertEquals("console://localhost:12345/abc?n=1;ref=top#hash", url.toString());
    }
}
