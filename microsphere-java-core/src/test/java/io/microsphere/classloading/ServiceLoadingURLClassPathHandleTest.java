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

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static io.microsphere.util.ClassLoaderUtils.newURLClassLoader;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ServiceLoadingURLClassPathHandle} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ServiceLoadingURLClassPathHandleTest {

    private ServiceLoadingURLClassPathHandle handle;

    @BeforeEach
    public void init() {
        this.handle = new ServiceLoadingURLClassPathHandle();
    }

    @Test
    public void test() throws IOException {
        ServiceLoadingURLClassPathHandle handle = new ServiceLoadingURLClassPathHandle();
        assertTrue(handle.supports());
        ClassLoader classLoader = getDefaultClassLoader();
        URL[] urls = handle.getURLs(classLoader);
        assertNotNull(urls);

        URLClassLoader urlClassLoader = newURLClassLoader(urls);
        for (URL url : urls) {
            assertTrue(handle.removeURL(urlClassLoader, url));
        }
    }
}
