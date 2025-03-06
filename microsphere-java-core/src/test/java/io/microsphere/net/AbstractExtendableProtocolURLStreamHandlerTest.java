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
package io.microsphere.net;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.List;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.SetUtils.ofSet;
import static io.microsphere.io.IOUtils.toByteArray;
import static io.microsphere.net.ExtendableProtocolURLStreamHandler.getHandlePackages;
import static io.microsphere.net.ExtendableProtocolURLStreamHandler.getHandlePackagesPropertyValue;
import static io.microsphere.net.URLUtils.HANDLER_PACKAGES_PROPERTY_NAME;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.StringUtils.split;
import static java.net.Proxy.NO_PROXY;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Abstract {@link ExtendableProtocolURLStreamHandler} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class AbstractExtendableProtocolURLStreamHandlerTest {

    protected ExtendableProtocolURLStreamHandler handler;

    protected URL testURL;

    protected URL testURLWithSP;

    @BeforeEach
    public final void init() throws IOException {
        handler = createHandler();
        this.testURL = new URL(getTestURL());
        this.testURLWithSP = new URL(getTestURLWithSP());
    }

    @AfterEach
    public void destroy() {
        System.getProperties().remove(HANDLER_PACKAGES_PROPERTY_NAME);
    }

    @Test
    public final void testGetHandlePackages() {
        assertEquals(ofSet(resolveHandlePackage(this.handler)), getHandlePackages());
    }

    @Test
    public final void testGetHandlePackagesPropertyValue() {
        assertEquals(resolveHandlePackage(this.handler), getHandlePackagesPropertyValue());
    }

    @Test
    public final void testInit() {
        handler.init();
    }

    @Test
    public final void testInitSubProtocolURLConnectionFactories() {
        List<SubProtocolURLConnectionFactory> factories = emptyList();
        List<SubProtocolURLConnectionFactory> copy = newLinkedList(factories);
        handler.initSubProtocolURLConnectionFactories(copy);
        assertEquals(copy, factories);
    }

    @Test
    public final void testCustomizeSubProtocolURLConnectionFactories() {
        handler.customizeSubProtocolURLConnectionFactories(factories -> {
            factories.add(new CompositeSubProtocolURLConnectionFactory());
        });
    }

    @Test
    public void testOpenConnection() throws IOException {
        testOpenConnection(testURL);
        testOpenConnection(testURLWithSP);
    }

    @Test
    public void testOpenConnectionWithProxy() throws IOException {
        testOpenConnection(testURL, NO_PROXY);
        testOpenConnection(testURLWithSP, NO_PROXY);
    }

    @Test
    public void testOpenFallbackConnection() throws IOException {
        assertNull(handler.openFallbackConnection(null, null));
    }

    @Test
    public final void testGetProtocol() {
        assertEquals(resolveProtocol(this.handler), handler.getProtocol());
    }

    @Test
    public final void testToString() {
        assertEquals(format("{} {defaultPort = -1 , protocol = '{}'}", handler.getClass().getName(), handler.getProtocol()), handler.toString());
    }

    protected abstract ExtendableProtocolURLStreamHandler createHandler();

    protected abstract String getTestURL();

    protected abstract String getTestURLWithSP();

    protected String resolveHandlePackage(ExtendableProtocolURLStreamHandler handler) {
        String packageName = handler.getClass().getPackage().getName();
        int lastIndex = packageName.lastIndexOf(".");
        return packageName.substring(0, lastIndex);
    }

    protected String resolveProtocol(ExtendableProtocolURLStreamHandler handler) {
        String packageName = handler.getClass().getPackage().getName();
        String[] parts = split(packageName, ".");
        return parts[parts.length - 1];
    }

    protected void testOpenConnection(URL url) throws IOException {
        assertNotNull(url.openConnection());
        assertArrayEquals(toByteArray(url.openStream()), toByteArray(handler.openConnection(url).getInputStream()));
    }

    protected void testOpenConnection(URL url, Proxy proxy) throws IOException {
        assertNotNull(url.openConnection(proxy));
        assertArrayEquals(toByteArray(url.openConnection(proxy).getInputStream()), toByteArray(handler.openConnection(url, proxy).getInputStream()));
    }
}
