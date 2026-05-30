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

import static io.microsphere.util.VersionUtils.CURRENT_JAVA_VERSION;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_9;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ModernURLClassPathHandle} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see 1.0.0
 * @since 1.0.0
 */
class ModernURLClassPathHandleTest extends AbstractURLClassPathHandleTest {

    @Override
    protected AbstractURLClassPathHandle createHandle() {
        return new ModernURLClassPathHandle();
    }

    @Override
    @Test
    void testSupports() {
        assertEquals(CURRENT_JAVA_VERSION.ge(JAVA_VERSION_9), handle.supports());
    }

    @Override
    @Test
    void testGetURLClassPathClassName() {
        assertEquals("jdk.internal.loader.URLClassPath", handle.getURLClassPathClassName());
    }

    @Override
    @Test
    void testGetUrlsFieldName() {
        assertEquals("unopenedUrls", handle.getUrlsFieldName());
    }
}
