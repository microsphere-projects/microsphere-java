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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Set;

import static io.microsphere.util.ClassLoaderUtils.findAllClassPathURLs;
import static java.lang.Thread.currentThread;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract {@link URLClassPathHandle} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see URLClassPathHandle
 * @since 1.0.0
 */
@Disabled
public abstract class BaseURLClassPathHandleTest<H extends URLClassPathHandle> {

    protected H handle;

    @BeforeEach
    public void init() {
        this.handle = createHandle();
    }

    protected abstract H createHandle();

    public abstract void testSupports();

    public abstract void testGetURLs();

    public abstract void testGetPriority();

    @Test
    public void testRemoveURL() {
        ClassLoader classLoader = currentThread().getContextClassLoader();
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
