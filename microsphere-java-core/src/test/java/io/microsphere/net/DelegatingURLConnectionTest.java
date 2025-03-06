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
public class DelegatingURLConnectionTest {

    private static final String CONTENT_LENGTH_HEADER_NAME = "content-length";

    private static final String LAST_MODIFIED_HEADER_NAME = "last-modified";

    private static final String NOT_EXISTS_HEADER_NAME = "not-exists";

    private URL url;

    private URLConnection delegate;

    private DelegatingURLConnection urlConnection;

    @BeforeEach
    public void init() throws Exception {
        URL baseURL = getClass().getProtectionDomain().getCodeSource().getLocation();
        this.url = new URL(baseURL.toString() + "META-INF/test.properties");
        this.delegate = url.openConnection();
        this.urlConnection = new DelegatingURLConnection(this.delegate);
    }

    @AfterEach
    public void destroy() {
    }

    @Test
    public void testConnect() throws IOException {
        this.urlConnection.connect();
    }

    @Test
    public void testSetConnectTimeout() {
        this.urlConnection.setConnectTimeout(100);
    }

    @Test
    public void testGetConnectTimeout() {
        testSetConnectTimeout();
        assertEquals(100, this.urlConnection.getConnectTimeout());
    }

    @Test
    public void testSetReadTimeout() {
        this.urlConnection.setReadTimeout(200);
    }

    @Test
    public void testGetReadTimeout() {
        testSetReadTimeout();
        assertEquals(200, this.urlConnection.getReadTimeout());
    }

    @Test
    public void testGetURL() {
        assertEquals(url, urlConnection.getURL());
    }

    @Test
    public void testGetContentLength() {
        assertEquals(19, urlConnection.getContentLength());
    }

    @Test
    public void testGetContentLengthLong() {
        assertEquals(19, urlConnection.getContentLengthLong());
    }

    @Test
    public void testGetContentType() {
        assertEquals("content/unknown", urlConnection.getContentType());
    }

    @Test
    public void testGetContentEncoding() {
        assertNull(urlConnection.getContentEncoding());
    }

    @Test
    public void testGetExpiration() {
        assertEquals(0, urlConnection.getExpiration());
    }

    @Test
    public void testGetDate() {
        assertEquals(0, urlConnection.getDate());
    }

    @Test
    public void testGetLastModified() {
        assertFalse(urlConnection.getLastModified() > currentTimeMillis());
    }

    @Test
    public void testGetHeaderField() {
        assertNull(urlConnection.getHeaderField(NOT_EXISTS_HEADER_NAME));
        assertEquals("19", urlConnection.getHeaderField(CONTENT_LENGTH_HEADER_NAME));
        assertNotNull(urlConnection.getHeaderField(LAST_MODIFIED_HEADER_NAME));
    }

    @Test
    public void testGetHeaderFields() {
        assertTrue(urlConnection.getHeaderFields().isEmpty());
    }

    @Test
    public void testGetHeaderFieldInt() {
        assertEquals(19, urlConnection.getHeaderFieldInt(CONTENT_LENGTH_HEADER_NAME, 10));
        assertEquals(1, urlConnection.getHeaderFieldInt(NOT_EXISTS_HEADER_NAME, 1));
    }

    @Test
    public void testGetHeaderFieldLong() {
        assertEquals(19, urlConnection.getHeaderFieldLong(CONTENT_LENGTH_HEADER_NAME, 10));
        assertEquals(1, urlConnection.getHeaderFieldLong(NOT_EXISTS_HEADER_NAME, 1));
    }

    @Test
    public void testGetHeaderFieldDate() {
        long now = currentTimeMillis();
        assertTrue(now > urlConnection.getHeaderFieldDate(LAST_MODIFIED_HEADER_NAME, now));
        assertEquals(now, urlConnection.getHeaderFieldDate(NOT_EXISTS_HEADER_NAME, now));
    }

    @Test
    public void testGetHeaderFieldKey() {
        assertEquals(CONTENT_LENGTH_HEADER_NAME, urlConnection.getHeaderFieldKey(0));
        assertEquals(LAST_MODIFIED_HEADER_NAME, urlConnection.getHeaderFieldKey(1));
        assertNull(urlConnection.getHeaderFieldKey(2));
    }

    @Test
    public void testGetHeaderFieldWithInt() {
        assertEquals("19", urlConnection.getHeaderField(0));
        assertNotNull(urlConnection.getHeaderField(1));
        assertNull(urlConnection.getHeaderFieldKey(2));
    }

    @Test
    public void testGetContent() throws IOException {
        urlConnection.getContent();
    }

    @Test
    public void testGetContentWithClassArray() throws IOException {
        urlConnection.getContent(new Class[0]);
    }

    @Test
    public void testGetPermission() throws IOException {
        assertNotNull(urlConnection.getPermission());
    }

    @Test
    public void testGetInputStream() throws Exception {
        String encoding = "UTF-8";
        String data = "name = 测试名称";
        assertEquals(data, IOUtils.toString(urlConnection.getInputStream(), encoding));
    }

    @Test
    public void testGetOutputStream() throws Exception {
        assertThrows(Exception.class, urlConnection::getOutputStream);
    }

    @Test
    public void testToString() {
        assertNotNull(urlConnection.toString());
    }

    @Test
    public void testSetDoInput() {
        urlConnection.setDoInput(true);
    }

    @Test
    public void testGetDoInput() {
        testSetDoInput();
        assertTrue(urlConnection.getDoInput());
    }

    @Test
    public void testSetDoOutput() {
        urlConnection.setDoOutput(true);
    }

    @Test
    public void testGetDoOutput() {
        testSetDoOutput();
        assertTrue(urlConnection.getDoOutput());
    }

    @Test
    public void testSetAllowUserInteraction() {
        urlConnection.setAllowUserInteraction(true);
    }

    @Test
    public void testGetAllowUserInteraction() {
        testSetAllowUserInteraction();
        assertTrue(urlConnection.getAllowUserInteraction());
    }

    @Test
    public void testSetUseCaches() {
        urlConnection.setUseCaches(true);
    }

    @Test
    public void testGetUseCaches() {
        testSetUseCaches();
        assertTrue(urlConnection.getUseCaches());
    }

    @Test
    public void testSetIfModifiedSince() {
        long now = currentTimeMillis();
        urlConnection.setIfModifiedSince(now);
    }

    @Test
    public void testGetIfModifiedSince() {
        testSetIfModifiedSince();
        assertTrue(currentTimeMillis() >= urlConnection.getIfModifiedSince());
    }

    @Test
    public void testSetDefaultUseCaches() {
        urlConnection.setDefaultUseCaches(true);
    }

    @Test
    public void testGetDefaultUseCaches() {
        urlConnection.setDefaultUseCaches(true);
        assertTrue(urlConnection.getDefaultUseCaches());
    }

    @Test
    public void testSetRequestProperty() {
        urlConnection.setRequestProperty("key-1", "value-1");
    }

    @Test
    public void testAddRequestProperty() {
        urlConnection.addRequestProperty("key-1", "value-1-1");
        urlConnection.addRequestProperty("key-2", "value-2");
    }

    @Test
    public void testGetRequestProperty() {
        assertNull(urlConnection.getRequestProperty("key-1"));
    }

    @Test
    public void testGetRequestProperties() {
        Map<String, List<String>> requestProperties = urlConnection.getRequestProperties();
        assertNotNull(requestProperties);
    }
}
