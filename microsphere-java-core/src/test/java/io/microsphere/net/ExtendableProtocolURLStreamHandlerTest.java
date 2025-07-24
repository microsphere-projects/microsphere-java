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

import io.microsphere.net.test.Handler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.SetUtils.ofSet;
import static io.microsphere.net.ExtendableProtocolURLStreamHandler.appendHandlePackage;
import static io.microsphere.net.ExtendableProtocolURLStreamHandler.assertClassName;
import static io.microsphere.net.ExtendableProtocolURLStreamHandler.assertClassTopLevel;
import static io.microsphere.net.ExtendableProtocolURLStreamHandler.assertPackage;
import static io.microsphere.net.ExtendableProtocolURLStreamHandler.getHandlePackages;
import static io.microsphere.net.ExtendableProtocolURLStreamHandler.getHandlePackagesPropertyValue;
import static io.microsphere.net.URLUtils.HANDLER_PACKAGES_PROPERTY_NAME;
import static io.microsphere.net.URLUtils.toExternalForm;
import static io.microsphere.net.console.HandlerTest.TEST_CONSOLE_URL_WITH_SP;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static java.net.Proxy.NO_PROXY;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ExtendableProtocolURLStreamHandler} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class ExtendableProtocolURLStreamHandlerTest {

    private static final String TEST_URL = "test://localhost:12345/abc";

    private static final String TEST_URL_WITH_SP = "test:text://localhost:12345/abc";

    private ExtendableProtocolURLStreamHandler handler;

    @BeforeEach
    void setUp() {
        // Handler for "test" protocol
        handler = new Handler();
    }

    @AfterEach
    void destroy() {
        System.getProperties().remove(HANDLER_PACKAGES_PROPERTY_NAME);
    }

    @Test
    void testAssertClassTopLevelOnLocalClass() {
        class LocalClass extends ExtendableProtocolURLStreamHandler {
        }
        assertThrows(IllegalArgumentException.class, () -> assertClassTopLevel(LocalClass.class));
    }

    @Test
    void testAssertClassTopLevelOnMemberClass() {
        assertThrows(IllegalArgumentException.class, () -> assertClassTopLevel(MemberClass.class));
    }

    @Test
    void testAssertClassTopLevelOnAnonymousClass() {
        assertThrows(IllegalArgumentException.class, () -> assertClassTopLevel(new ExtendableProtocolURLStreamHandler() {
        }.getClass()));
    }

    @Test
    void testAssertClassNameOnWrongClassName() {
        assertThrows(IllegalArgumentException.class, () -> assertClassName(MemberClass.class));
    }

    @Test
    void testAssertPackageOnSunHandlerClass() {
        Class<?> handler = resolveClass("sun.net.www.protocol.file.Handler");
        if (handler != null) {
            assertThrows(IllegalArgumentException.class, () -> assertPackage(handler));
        }
    }

    @Test
    void testConstructorWithProtocolArg() {
        assertTestHandler("test-1");
        assertTestHandler("test-2");
        assertTestHandler("test-3");
    }

    private void assertTestHandler(String protocol) {
        ExtendableProtocolURLStreamHandler handler = new Handler(protocol);
        assertEquals(protocol, handler.getProtocol());
    }

    @Test
    void testGetHandlePackages() {
        assertEquals(ofSet("io.microsphere.net"), getHandlePackages());
    }

    @Test
    void testGetHandlePackagesPropertyValue() {
        assertEquals("io.microsphere.net", getHandlePackagesPropertyValue());
    }

    @Test
    void testAppendHandlePackage() {
        appendHandlePackage("io.microsphere.lang");
        assertEquals(ofSet("io.microsphere.net", "io.microsphere"), getHandlePackages());
        assertEquals("io.microsphere.net|io.microsphere", getHandlePackagesPropertyValue());
    }

    @Test
    void testInit() {
        handler.init();
    }

    @Test
    void testInitSubProtocolURLConnectionFactories() {
        List<SubProtocolURLConnectionFactory> factories = emptyList();
        List<SubProtocolURLConnectionFactory> copy = newLinkedList(factories);
        handler.initSubProtocolURLConnectionFactories(copy);
        assertEquals(copy, factories);
    }

    @Test
    void testOpenConnection() throws IOException {
        URL url = new URL(TEST_URL);
        assertNull(url.openConnection());
        assertEquals(TEST_URL, url.toString());
    }

    @Test
    void testCustomizeSubProtocolURLConnectionFactories() {
        handler.customizeSubProtocolURLConnectionFactories(factories -> {
            factories.add(new CompositeSubProtocolURLConnectionFactory());
        });
    }

    @Test
    void testOpenConnectionWithProxy() throws IOException {
        handler.customizeSubProtocolURLConnectionFactories(factories -> {
            factories.add(new ConsoleSubProtocolURLConnectionFactory());
        });

        URL url = new URL(TEST_URL_WITH_SP);
        assertNull(url.openConnection(NO_PROXY));
        assertEquals(TEST_URL_WITH_SP, url.toString());

        url = new URL(TEST_CONSOLE_URL_WITH_SP);
        assertSame(url.openConnection(NO_PROXY).getInputStream(), handler.openConnection(url, NO_PROXY).getInputStream());
        assertEquals(TEST_CONSOLE_URL_WITH_SP, url.toString());
    }

    @Test
    void testOpenFallbackConnection() throws IOException {
        assertNull(handler.openFallbackConnection(null, null));
    }

    @Test
    void testEquals() throws IOException {
        assertTrue(handler.equals(new URL(TEST_URL), new URL(TEST_URL)));
        assertTrue(handler.equals(new URL(TEST_URL_WITH_SP), new URL(TEST_URL_WITH_SP)));
        assertFalse(handler.equals(new URL(TEST_URL), new URL(TEST_URL_WITH_SP)));
    }

    @Test
    void testHashCode() throws IOException {
        assertEquals(handler.hashCode(new URL(TEST_URL)), new URL(TEST_URL).hashCode());
        assertEquals(handler.hashCode(new URL(TEST_URL_WITH_SP)), new URL(TEST_URL_WITH_SP).hashCode());
        assertNotEquals(handler.hashCode(new URL(TEST_URL)), new URL(TEST_URL_WITH_SP).hashCode());
    }

    @Test
    void testHostsEqual() throws IOException {
        assertTrue(handler.hostsEqual(new URL(TEST_URL), new URL(TEST_URL)));
        assertTrue(handler.hostsEqual(new URL(TEST_URL), new URL(TEST_URL_WITH_SP)));
    }

    @Test
    void testToExternalForm() throws IOException {
        assertEquals(handler.toExternalForm(new URL(TEST_URL)), toExternalForm(new URL(TEST_URL)));
        assertEquals(handler.toExternalForm(new URL(TEST_URL_WITH_SP)), toExternalForm(new URL(TEST_URL_WITH_SP)));
    }

    @Test
    void testResolveSubProtocols() throws IOException {
        assertEquals(emptyList(), handler.resolveSubProtocols(new URL(TEST_URL)));
        assertEquals(ofList("text"), handler.resolveSubProtocols(new URL(TEST_URL_WITH_SP)));
    }

    @Test
    void testResolveAuthority() throws IOException {
        assertEquals("localhost:12345", handler.resolveAuthority(new URL(TEST_URL)));
        assertEquals("localhost:12345", handler.resolveAuthority(new URL(TEST_URL_WITH_SP)));
    }

    @Test
    public void testResolvePath() throws IOException {
        assertEquals("/abc", handler.resolvePath(new URL(TEST_URL)));
        assertEquals("/abc", handler.resolvePath(new URL(TEST_URL_WITH_SP)));
    }

    @Test
    public void testGetProtocol() {
        assertEquals("test", handler.getProtocol());
    }

    @Test
    public void testToString() {
        assertEquals("io.microsphere.net.test.Handler {defaultPort = -1 , protocol = 'test'}", handler.toString());
    }

    private static class MemberClass extends ExtendableProtocolURLStreamHandler {
    }
}
