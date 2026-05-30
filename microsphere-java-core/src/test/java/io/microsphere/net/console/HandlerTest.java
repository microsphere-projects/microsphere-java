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
package io.microsphere.net.console;

import io.microsphere.net.AbstractExtendableProtocolURLStreamHandlerTest;
import io.microsphere.net.ExtendableProtocolURLStreamHandler;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;

import static java.net.Proxy.NO_PROXY;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link Handler} Test for "console" protocol
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Handler
 * @since 1.0.0
 */
public class HandlerTest extends AbstractExtendableProtocolURLStreamHandlerTest {

    public static final String TEST_CONSOLE_URL = "console://localhost:12345/abc";

    public static final String TEST_CONSOLE_URL_WITH_SP = "console:text://localhost:12345/abc";

    public static final String TEST_CONSOLE_URL_WITH_SP_PARAMS = TEST_CONSOLE_URL_WITH_SP + ";ref=top?n=1";

    public static final String TEST_CONSOLE_URL_WITH_SP_PARAMS_HASH = TEST_CONSOLE_URL_WITH_SP_PARAMS + "#hash";

    @Override
    protected ExtendableProtocolURLStreamHandler createHandler() {
        return new Handler();
    }

    @Override
    protected String getTestURL() {
        return TEST_CONSOLE_URL;
    }

    @Override
    protected String getTestURLWithSP() {
        return TEST_CONSOLE_URL_WITH_SP_PARAMS_HASH;
    }

    @Override
    protected void testOpenConnection(URL url) throws IOException {
        assertSame(url.openStream(), this.handler.openConnection(url, NO_PROXY).getInputStream());
    }

    @Override
    protected void testOpenConnection(URL url, Proxy proxy) throws IOException {
        assertSame(url.openConnection(proxy).getInputStream(), this.handler.openConnection(url, proxy).getInputStream());
    }
}
