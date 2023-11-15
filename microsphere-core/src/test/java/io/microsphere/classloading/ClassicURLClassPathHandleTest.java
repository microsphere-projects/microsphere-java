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

import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

import static io.microsphere.util.ClassLoaderUtils.findAllClassPathURLs;
import static io.microsphere.util.ClassLoaderUtils.removeClassPathURL;
import static org.junit.Assert.assertTrue;

/**
 * {@link ClassicURLClassPathHandle} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see 1.0.0
 * @since 1.0.0
 */
public class ClassicURLClassPathHandleTest {

    @Test
    public void test() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ClassicURLClassPathHandle handle = new ClassicURLClassPathHandle();
        if (handle.supports()) {
            Set<URL> urls = findAllClassPathURLs(classLoader);
            for (URL url : urls) {
                String path = url.getPath();
                if (path.contains("jmh-generator-annprocess")) {
                    assertTrue(handle.removeURL((URLClassLoader) classLoader, url));
                }
            }
        }
    }
}
