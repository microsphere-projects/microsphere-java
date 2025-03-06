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

import io.microsphere.net.console.Handler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.ListUtils.ofList;
import static io.microsphere.collection.SetUtils.ofSet;
import static io.microsphere.net.ExtendableProtocolURLStreamHandler.appendHandlePackage;
import static io.microsphere.net.ExtendableProtocolURLStreamHandler.assertClassName;
import static io.microsphere.net.ExtendableProtocolURLStreamHandler.assertClassTopLevel;
import static io.microsphere.net.ExtendableProtocolURLStreamHandler.assertPackage;
import static io.microsphere.net.ExtendableProtocolURLStreamHandler.getHandlePackages;
import static io.microsphere.net.ExtendableProtocolURLStreamHandler.getHandlePackagesPropertyValue;
import static io.microsphere.net.URLUtils.toExternalForm;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static java.net.Proxy.NO_PROXY;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
public class ExtendableProtocolURLStreamHandlerTest {

    private static final String TEST_URL = "console://localhost:12345/abc";

    private static final String TEST_URL_WITH_SP = "console:text://localhost:12345/abc";

    private static final String TEST_URL_WITH_SP_PARAMS = TEST_URL_WITH_SP + ";ref=top?n=1";

    private static final String TEST_URL_WITH_SP_PARAMS_HASH = TEST_URL_WITH_SP_PARAMS + "#hash";

    private ExtendableProtocolURLStreamHandler handler;

    @BeforeEach
    public void init() {
        // Handler for "console" protocol
        handler = new Handler();
    }

    @AfterEach
    public void destroy() {
    }

    @Test
    public void testAssertClassTopLevelOnLocalClass() {
        class LocalClass extends ExtendableProtocolURLStreamHandler {

        }
        assertThrows(IllegalArgumentException.class, () -> assertClassTopLevel(LocalClass.class));
    }

    @Test
    public void testAssertClassTopLevelOnMemberClass() {
        assertThrows(IllegalArgumentException.class, () -> assertClassTopLevel(MemberClass.class));
    }

    @Test
    public void testAssertClassTopLevelOnAnonymousClass() {
        assertThrows(IllegalArgumentException.class, () ->
                assertClassTopLevel(new ExtendableProtocolURLStreamHandler() {
                }.getClass()));
    }

    @Test
    public void testAssertClassNameOnWrongClassName() {
        assertThrows(IllegalArgumentException.class, () -> assertClassName(MemberClass.class));
    }

    @Test
    public void testAssertPackageOnSunHandlerClass() {
        Class<?> handler = resolveClass("sun.net.www.protocol.file.Handler");
        if (handler != null) {
            assertThrows(IllegalArgumentException.class, () -> assertPackage(handler));
        }
    }

    @Test
    public void testConstructorWithProtocolArg() {
        assertTestHandler("test-1");
        assertTestHandler("test-2");
        assertTestHandler("test-3");
    }

    private void assertTestHandler(String protocol) {
        ExtendableProtocolURLStreamHandler handler = new TestHandler(protocol);
        assertEquals(protocol, handler.getProtocol());
    }

    @Test
    public void testClassPathProtocol() throws Throwable {
        io.microsphere.net.classpath.Handler handler = new io.microsphere.net.classpath.Handler();
        URL url = new URL("classpath://META-INF/test.properties");
        Properties properties = new Properties();
        properties.load(new InputStreamReader(url.openStream(), "UTF-8"));
        assertEquals("测试名称", properties.get("name"));

        url = new URL("classpath:////META-INF/services/io.microsphere.event.EventListener");
        assertNotNull(url.openStream());

        url = new URL("classpath:///META-INF/services/io.microsphere.event.EventListener");
        assertNotNull(url.openStream());

        url = new URL("classpath://META-INF/services/io.microsphere.event.EventListener");
        assertNotNull(url.openStream());

        url = new URL("classpath:/META-INF/services/io.microsphere.event.EventListener");
        assertNotNull(url.openConnection());

        url = new URL("classpath:META-INF/services/io.microsphere.event.EventListener");
        assertNotNull(url.openConnection(Proxy.NO_PROXY));

    }

    @Test
    public void testClassPathProtocolOnResourceNotFound() {
        assertThrows(IOException.class, () -> {
            new io.microsphere.net.classpath.Handler();
            URL url = new URL("classpath://META-INF/not-found.res");
            url.openStream();
        });
    }


    @Test
    public void testGetHandlePackages() {
        assertEquals(ofSet("io.microsphere.net"), getHandlePackages());
    }

    @Test
    public void testGetHandlePackagesPropertyValue() {
        assertEquals("io.microsphere.net", getHandlePackagesPropertyValue());
    }

    @Test
    public void testAppendHandlePackage() {
        appendHandlePackage("io.microsphere.lang");
        assertEquals(ofSet("io.microsphere.net", "io.microsphere"), getHandlePackages());
        assertEquals("io.microsphere.net|io.microsphere", getHandlePackagesPropertyValue());
    }

    @Test
    public void testInit() {
        handler.init();
    }

    @Test
    public void testInitSubProtocolURLConnectionFactories() {
        List<SubProtocolURLConnectionFactory> factories = emptyList();
        List<SubProtocolURLConnectionFactory> copy = newLinkedList(factories);
        handler.initSubProtocolURLConnectionFactories(copy);
        assertEquals(copy, factories);
    }

    @Test
    public void testOpenConnection() throws IOException {

        URL url = new URL(TEST_URL);
        assertSame(url.openStream(), handler.openConnection(url, NO_PROXY).getInputStream());
        assertEquals(TEST_URL, url.toString());

        url = new URL(TEST_URL_WITH_SP);
        assertSame(url.openStream(), handler.openConnection(url, NO_PROXY).getInputStream());
        assertEquals(TEST_URL_WITH_SP, url.toString());
    }

    @Test
    public void testTestOpenConnection() throws IOException {
        String spec = TEST_URL_WITH_SP_PARAMS;
        URL url = new URL(spec);
        assertSame(url.openStream(), handler.openConnection(url).getInputStream());
        assertEquals(spec, url.toString());

        spec = TEST_URL_WITH_SP_PARAMS_HASH;
        url = new URL(spec);
        assertSame(url.openStream(), handler.openConnection(url).getInputStream());
        assertEquals(spec, url.toString());
    }

    @Test
    public void testOpenFallbackConnection() throws IOException {
        assertNull(handler.openFallbackConnection(null, null));
    }

    @Test
    public void testEquals() throws IOException {
        assertTrue(handler.equals(new URL(TEST_URL), new URL(TEST_URL)));
        assertTrue(handler.equals(new URL(TEST_URL_WITH_SP), new URL(TEST_URL_WITH_SP)));
        assertTrue(handler.equals(new URL(TEST_URL_WITH_SP_PARAMS), new URL(TEST_URL_WITH_SP_PARAMS)));
        assertTrue(handler.equals(new URL(TEST_URL_WITH_SP_PARAMS_HASH), new URL(TEST_URL_WITH_SP_PARAMS_HASH)));
        assertFalse(handler.equals(new URL(TEST_URL), new URL(TEST_URL_WITH_SP)));
        assertFalse(handler.equals(new URL(TEST_URL), new URL(TEST_URL_WITH_SP_PARAMS)));
        assertFalse(handler.equals(new URL(TEST_URL), new URL(TEST_URL_WITH_SP_PARAMS_HASH)));
    }

    @Test
    public void testHashCode() throws IOException {
        assertEquals(handler.hashCode(new URL(TEST_URL)), new URL(TEST_URL).hashCode());
        assertEquals(handler.hashCode(new URL(TEST_URL_WITH_SP)), new URL(TEST_URL_WITH_SP).hashCode());
        assertEquals(handler.hashCode(new URL(TEST_URL_WITH_SP_PARAMS)), new URL(TEST_URL_WITH_SP_PARAMS).hashCode());
        assertEquals(handler.hashCode(new URL(TEST_URL_WITH_SP_PARAMS_HASH)), new URL(TEST_URL_WITH_SP_PARAMS_HASH).hashCode());
        assertNotEquals(handler.hashCode(new URL(TEST_URL)), new URL(TEST_URL_WITH_SP).hashCode());
        assertNotEquals(handler.hashCode(new URL(TEST_URL)), new URL(TEST_URL_WITH_SP_PARAMS).hashCode());
        assertNotEquals(handler.hashCode(new URL(TEST_URL)), new URL(TEST_URL_WITH_SP_PARAMS_HASH).hashCode());
    }

    @Test
    public void testHostsEqual() throws IOException {
        assertTrue(handler.hostsEqual(new URL(TEST_URL), new URL(TEST_URL)));
        assertTrue(handler.hostsEqual(new URL(TEST_URL), new URL(TEST_URL_WITH_SP)));
        assertTrue(handler.hostsEqual(new URL(TEST_URL), new URL(TEST_URL_WITH_SP_PARAMS)));
        assertTrue(handler.hostsEqual(new URL(TEST_URL), new URL(TEST_URL_WITH_SP_PARAMS_HASH)));
    }

    @Test
    public void testToExternalForm() throws IOException {
        assertEquals(handler.toExternalForm(new URL(TEST_URL)), toExternalForm(new URL(TEST_URL)));
        assertEquals(handler.toExternalForm(new URL(TEST_URL_WITH_SP)), toExternalForm(new URL(TEST_URL_WITH_SP)));
        assertEquals(handler.toExternalForm(new URL(TEST_URL_WITH_SP_PARAMS)), toExternalForm(new URL(TEST_URL_WITH_SP_PARAMS)));
        assertEquals(handler.toExternalForm(new URL(TEST_URL_WITH_SP_PARAMS_HASH)), toExternalForm(new URL(TEST_URL_WITH_SP_PARAMS_HASH)));
    }

    @Test
    public void testResolveSubProtocols() throws IOException {
        assertEquals(emptyList(), handler.resolveSubProtocols(new URL(TEST_URL)));
        assertEquals(ofList("text"), handler.resolveSubProtocols(new URL(TEST_URL_WITH_SP)));
        assertEquals(ofList("text"), handler.resolveSubProtocols(new URL(TEST_URL_WITH_SP_PARAMS)));
        assertEquals(ofList("text"), handler.resolveSubProtocols(new URL(TEST_URL_WITH_SP_PARAMS_HASH)));
    }

    @Test
    public void testResolveAuthority() throws IOException {
        assertEquals("localhost:12345", handler.resolveAuthority(new URL(TEST_URL)));
        assertEquals("localhost:12345", handler.resolveAuthority(new URL(TEST_URL_WITH_SP)));
        assertEquals("localhost:12345", handler.resolveAuthority(new URL(TEST_URL_WITH_SP_PARAMS)));
        assertEquals("localhost:12345", handler.resolveAuthority(new URL(TEST_URL_WITH_SP_PARAMS_HASH)));
    }

    @Test
    public void testResolvePath() throws IOException {
        assertEquals("/abc", handler.resolvePath(new URL(TEST_URL)));
        assertEquals("/abc", handler.resolvePath(new URL(TEST_URL_WITH_SP)));
        assertEquals("/abc", handler.resolvePath(new URL(TEST_URL_WITH_SP_PARAMS)));
        assertEquals("/abc", handler.resolvePath(new URL(TEST_URL_WITH_SP_PARAMS_HASH)));
    }

    @Test
    public void testGetProtocol() {
        assertEquals("console", handler.getProtocol());
    }

    @Test
    public void testToString() {
        assertEquals("io.microsphere.net.console.Handler {defaultPort = -1 , protocol = 'console'}", handler.toString());
    }

    private static class MemberClass extends ExtendableProtocolURLStreamHandler {
    }
}
