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
package io.microsphere.net.classpath;

import io.microsphere.net.AbstractExtendableProtocolURLStreamHandlerTest;
import io.microsphere.net.ExtendableProtocolURLStreamHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;

import static io.microsphere.io.IOUtils.toByteArray;
import static java.net.Proxy.NO_PROXY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link Handler} Test for "classpath" protocol
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Handler
 * @since 1.0.0
 */
public class HandlerTest extends AbstractExtendableProtocolURLStreamHandlerTest {

    public static final String TEST_PROPERTIES_CLASSPATH_URL = "classpath://META-INF/test.properties";

    public static final String EVENT_LISTENER_URL = "classpath:////META-INF/services/io.microsphere.event.EventListener";

    private static final String NOT_FOUND_URL = "classpath://META-INF/not-found.res";

    @Override
    protected void testOpenConnection(URL url) throws IOException {
        assertArrayEquals(toByteArray(url.openConnection().getInputStream()), toByteArray(handler.openConnection(url, NO_PROXY).getInputStream()));
    }

    @Override
    protected void testOpenConnection(URL url, Proxy proxy) throws IOException {
        assertArrayEquals(toByteArray(url.openConnection(NO_PROXY).getInputStream()), toByteArray(handler.openConnection(url, NO_PROXY).getInputStream()));
    }

    @Test
    public void testOpenConnectionOnResourceNotFound() {
        assertThrows(IOException.class, () -> {
            URL url = new URL(NOT_FOUND_URL);
            url.openStream();
        });
    }

    @Override
    protected ExtendableProtocolURLStreamHandler createHandler() {
        return new Handler();
    }

    @Override
    protected String getTestURL() {
        return TEST_PROPERTIES_CLASSPATH_URL;
    }

    @Override
    protected String getTestURLWithSP() {
        return EVENT_LISTENER_URL;
    }
}
