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

import io.microsphere.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DelegatingURLConnection} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class DelegatingURLConnectionTest {

    private static final String CONTENT_LENGTH_HEADER_NAME = "content-length";

    private static final String LAST_MODIFIED_HEADER_NAME = "last-modified";

    private static final String NOT_EXISTS_HEADER_NAME = "not-exists";

    private URL url;

    private URLConnection delegate;

    private DelegatingURLConnection urlConnection;

    @BeforeEach
    void init() throws Exception {
        URL baseURL = getClass().getProtectionDomain().getCodeSource().getLocation();
        this.url = new URL(baseURL.toString() + "META-INF/test.properties");
        this.delegate = url.openConnection();
        this.urlConnection = new DelegatingURLConnection(this.delegate);
    }

    @AfterEach
    void destroy() {
    }

    @Test
    void testConnect() throws IOException {
        this.urlConnection.connect();
    }

    @Test
    void testSetConnectTimeout() {
        this.urlConnection.setConnectTimeout(100);
    }

    @Test
    void testGetConnectTimeout() {
        testSetConnectTimeout();
        assertEquals(100, this.urlConnection.getConnectTimeout());
    }

    @Test
    void testSetReadTimeout() {
        this.urlConnection.setReadTimeout(200);
    }

    @Test
    void testGetReadTimeout() {
        testSetReadTimeout();
        assertEquals(200, this.urlConnection.getReadTimeout());
    }

    @Test
    void testGetURL() {
        assertEquals(url, urlConnection.getURL());
    }

    @Test
    void testGetContentLength() {
        assertEquals(19, urlConnection.getContentLength());
    }

    @Test
    void testGetContentLengthLong() {
        assertEquals(19, urlConnection.getContentLengthLong());
    }

    @Test
    void testGetContentType() {
        assertEquals("content/unknown", urlConnection.getContentType());
    }

    @Test
    void testGetContentEncoding() {
        assertNull(urlConnection.getContentEncoding());
    }

    @Test
    void testGetExpiration() {
        assertEquals(0, urlConnection.getExpiration());
    }

    @Test
    void testGetDate() {
        assertEquals(0, urlConnection.getDate());
    }

    @Test
    void testGetLastModified() {
        assertFalse(urlConnection.getLastModified() > currentTimeMillis());
    }

    @Test
    void testGetHeaderField() {
        assertNull(urlConnection.getHeaderField(NOT_EXISTS_HEADER_NAME));
        assertEquals("19", urlConnection.getHeaderField(CONTENT_LENGTH_HEADER_NAME));
        assertNotNull(urlConnection.getHeaderField(LAST_MODIFIED_HEADER_NAME));
    }

    @Test
    void testGetHeaderFields() {
        assertNotNull(urlConnection.getHeaderFields());
    }

    @Test
    void testGetHeaderFieldInt() {
        assertEquals(19, urlConnection.getHeaderFieldInt(CONTENT_LENGTH_HEADER_NAME, 10));
        assertEquals(1, urlConnection.getHeaderFieldInt(NOT_EXISTS_HEADER_NAME, 1));
    }

    @Test
    void testGetHeaderFieldLong() {
        assertEquals(19, urlConnection.getHeaderFieldLong(CONTENT_LENGTH_HEADER_NAME, 10));
        assertEquals(1, urlConnection.getHeaderFieldLong(NOT_EXISTS_HEADER_NAME, 1));
    }

    @Test
    void testGetHeaderFieldDate() {
        long now = currentTimeMillis();
        assertTrue(now > urlConnection.getHeaderFieldDate(LAST_MODIFIED_HEADER_NAME, now));
        assertEquals(now, urlConnection.getHeaderFieldDate(NOT_EXISTS_HEADER_NAME, now));
    }

    @Test
    void testGetHeaderFieldKey() {
        assertEquals(CONTENT_LENGTH_HEADER_NAME, urlConnection.getHeaderFieldKey(0));
        assertEquals(LAST_MODIFIED_HEADER_NAME, urlConnection.getHeaderFieldKey(1));
        assertNull(urlConnection.getHeaderFieldKey(2));
    }

    @Test
    void testGetHeaderFieldWithInt() {
        assertEquals("19", urlConnection.getHeaderField(0));
        assertNotNull(urlConnection.getHeaderField(1));
        assertNull(urlConnection.getHeaderFieldKey(2));
    }

    @Test
    void testGetContent() throws IOException {
        urlConnection.getContent();
    }

    @Test
    void testGetContentWithClassArray() throws IOException {
        urlConnection.getContent(new Class[0]);
    }

    @Test
    void testGetPermission() throws IOException {
        assertNotNull(urlConnection.getPermission());
    }

    @Test
    void testGetInputStream() throws Exception {
        String encoding = "UTF-8";
        String data = "name = 测试名称";
        assertEquals(data, IOUtils.toString(urlConnection.getInputStream(), encoding));
    }

    @Test
    void testGetOutputStream() throws Exception {
        assertThrows(Exception.class, urlConnection::getOutputStream);
    }

    @Test
    void testToString() {
        assertNotNull(urlConnection.toString());
    }

    @Test
    void testSetDoInput() {
        urlConnection.setDoInput(true);
    }

    @Test
    void testGetDoInput() {
        testSetDoInput();
        assertTrue(urlConnection.getDoInput());
    }

    @Test
    void testSetDoOutput() {
        urlConnection.setDoOutput(true);
    }

    @Test
    void testGetDoOutput() {
        testSetDoOutput();
        assertTrue(urlConnection.getDoOutput());
    }

    @Test
    void testSetAllowUserInteraction() {
        urlConnection.setAllowUserInteraction(true);
    }

    @Test
    void testGetAllowUserInteraction() {
        testSetAllowUserInteraction();
        assertTrue(urlConnection.getAllowUserInteraction());
    }

    @Test
    void testSetUseCaches() {
        urlConnection.setUseCaches(true);
    }

    @Test
    void testGetUseCaches() {
        testSetUseCaches();
        assertTrue(urlConnection.getUseCaches());
    }

    @Test
    void testSetIfModifiedSince() {
        long now = currentTimeMillis();
        urlConnection.setIfModifiedSince(now);
    }

    @Test
    void testGetIfModifiedSince() {
        testSetIfModifiedSince();
        assertTrue(currentTimeMillis() >= urlConnection.getIfModifiedSince());
    }

    @Test
    void testSetDefaultUseCaches() {
        urlConnection.setDefaultUseCaches(true);
    }

    @Test
    void testGetDefaultUseCaches() {
        urlConnection.setDefaultUseCaches(true);
        assertTrue(urlConnection.getDefaultUseCaches());
    }

    @Test
    void testSetRequestProperty() {
        urlConnection.setRequestProperty("key-1", "value-1");
    }

    @Test
    void testAddRequestProperty() {
        urlConnection.addRequestProperty("key-1", "value-1-1");
        urlConnection.addRequestProperty("key-2", "value-2");
    }

    @Test
    void testGetRequestProperty() {
        assertNull(urlConnection.getRequestProperty("key-1"));
    }

    @Test
    public void testGetRequestProperties() {
        Map<String, List<String>> requestProperties = urlConnection.getRequestProperties();
        assertNotNull(requestProperties);
    }
}
