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
package io.microsphere.classloading;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.net.URLClassLoader;

import static io.microsphere.net.URLUtils.EMPTY_URL_ARRAY;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static io.microsphere.util.ClassLoaderUtils.newURLClassLoader;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ServiceLoadingURLClassPathHandle} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class ServiceLoadingURLClassPathHandleTest {

    private ServiceLoadingURLClassPathHandle handle;

    @BeforeEach
    void setUp() {
        this.handle = new ServiceLoadingURLClassPathHandle();
    }

    @Test
    void testSupports() {
        assertTrue(this.handle.supports());
    }

    @Test
    void testGetURLs() {
        ClassLoader classLoader = getDefaultClassLoader();
        URL[] urls = handle.getURLs(classLoader);
        assertNotNull(urls);

        assertArrayEquals(EMPTY_URL_ARRAY, handle.getURLs(null));
    }

    @Test
    void testRemoveURL() {
        ClassLoader classLoader = getDefaultClassLoader();
        URL[] urls = this.handle.getURLs(classLoader);
        URLClassLoader urlClassLoader = newURLClassLoader(urls);
        for (URL url : urls) {
            assertTrue(this.handle.removeURL(urlClassLoader, url));
        }
    }
}
