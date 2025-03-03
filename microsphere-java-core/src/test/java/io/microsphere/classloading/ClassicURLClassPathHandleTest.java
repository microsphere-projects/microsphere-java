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

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Set;

import static io.microsphere.util.ClassLoaderUtils.findAllClassPathURLs;
import static io.microsphere.util.VersionUtils.CURRENT_JAVA_VERSION;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ClassicURLClassPathHandle} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see 1.0.0
 * @since 1.0.0
 */
public class ClassicURLClassPathHandleTest {

    private final static ClassicURLClassPathHandle handle = new ClassicURLClassPathHandle();

    @Test
    public void testSupports() {
        assertEquals(CURRENT_JAVA_VERSION.le(JAVA_VERSION_8), handle.supports());
    }

    @Test
    public void testGetURLClassPathClassName() {
        assertEquals("sun.misc.URLClassPath", handle.getURLClassPathClassName());
    }

    @Test
    public void testGetUrlsFieldName() {
        assertEquals("urls", handle.getUrlsFieldName());
    }

    @Test
    public void testRemoveURL() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (handle.supports()) {
            Set<URL> urls = findAllClassPathURLs(classLoader);
            for (URL url : urls) {
                String path = url.getPath();
                if (path.contains("jmh-generator-annprocess")) {
                    assertTrue(handle.removeURL(classLoader, url));
                }
            }
        }
    }
}
