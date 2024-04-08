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

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sun.net.www.protocol.file.Handler;

import java.net.URL;
import java.net.URLConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DelegatingURLConnection} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class DelegatingURLConnectionTest {

    private URL url;

    private URLConnection delegate;

    private DelegatingURLConnection urlConnection;

    @BeforeEach
    public void before() throws Exception {
        URL baseURL = getClass().getProtectionDomain().getCodeSource().getLocation();
        this.url = new URL(baseURL.toString() + "META-INF/test.properties");
        Handler handler = new Handler();
        this.delegate = handler.openConnection(url);
        this.urlConnection = new DelegatingURLConnection(this.delegate);
    }

    @Test
    public void testStatus() throws Exception {
        assertEquals(url, urlConnection.getURL());

        urlConnection.setConnectTimeout(1);
        assertEquals(1, urlConnection.getConnectTimeout());

        urlConnection.setAllowUserInteraction(true);
        assertTrue(urlConnection.getAllowUserInteraction());

        urlConnection.setDefaultUseCaches(true);
        assertTrue(urlConnection.getDefaultUseCaches());

        urlConnection.setDoInput(true);
        assertTrue(urlConnection.getDoInput());

        urlConnection.setDoOutput(true);
        assertTrue(urlConnection.getDoOutput());

        long now = System.currentTimeMillis();
        urlConnection.setIfModifiedSince(now);
        assertEquals(now, urlConnection.getIfModifiedSince());

        urlConnection.setReadTimeout(2);
        assertEquals(2, urlConnection.getReadTimeout());

        urlConnection.setUseCaches(true);
        assertTrue(urlConnection.getUseCaches());
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

}
