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
package io.microsphere.net.test;

import io.microsphere.net.AbstractExtendableProtocolURLStreamHandlerTest;
import io.microsphere.net.ExtendableProtocolURLStreamHandler;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {link Handler} Test for "test" protocol
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractExtendableProtocolURLStreamHandlerTest
 * @since 1.0.0
 */
class HandlerTest extends AbstractExtendableProtocolURLStreamHandlerTest {

    private static final String TEST_URL = "test:////META-INF/test.properties";

    private static final String TEST_URL_WITH_SP = "test:text:////META-INF/test.properties";

    @Override
    protected ExtendableProtocolURLStreamHandler createHandler() {
        return new Handler();
    }

    @Override
    protected String getTestURL() {
        return TEST_URL;
    }

    @Override
    protected String getTestURLWithSP() {
        return TEST_URL_WITH_SP;
    }

    @Override
    protected void testOpenConnection(URL url) throws IOException {
        assertNull(url.openConnection());
    }

    @Override
    protected void testOpenConnection(URL url, Proxy proxy) throws IOException {
        assertNull(url.openConnection(proxy));
        assertNull(this.handler.openConnection(url, proxy));
    }
}
