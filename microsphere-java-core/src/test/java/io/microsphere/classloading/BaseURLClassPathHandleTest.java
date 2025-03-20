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

import static io.microsphere.AbstractTestCase.TEST_CLASS_LOADER;
import static io.microsphere.net.URLUtils.EMPTY_URL_ARRAY;
import static io.microsphere.net.URLUtils.ofURL;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
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

    public abstract void testGetPriority();

    @Test
    public void test() {
        ClassLoader classLoader = TEST_CLASS_LOADER;
        if (handle.supports()) {
            URL[] urls = handle.getURLs(classLoader);
            for (URL url : urls) {
                String path = url.getPath();
                if (path.contains("jmh-generator-annprocess")) {
                    assertTrue(handle.removeURL(classLoader, url));
                }
            }
        }
    }

    @Test
    public void testGetURLsOnNullClassLoader() {
        assertSame(EMPTY_URL_ARRAY, handle.getURLs(null));
    }

    @Test
    public void testRemoveURLOnNullClassLoader() {
        assertFalse(handle.removeURL(null, ofURL("file://a.jar")));
    }

    @Test
    public void testRemoveURLOnNullURL() {
        assertFalse(handle.removeURL(TEST_CLASS_LOADER, null));
    }
}
