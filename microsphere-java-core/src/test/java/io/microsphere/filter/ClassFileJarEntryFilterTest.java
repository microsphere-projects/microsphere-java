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
package io.microsphere.filter;

import org.junit.jupiter.api.Test;

import java.util.jar.JarEntry;

import static io.microsphere.filter.ClassFileJarEntryFilter.INSTANCE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ClassFileJarEntryFilter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ClassFileJarEntryFilter
 * @since 1.0.0
 */
class ClassFileJarEntryFilterTest {

    @Test
    void testAcceptClassFile() {
        JarEntry classEntry = new JarEntry("io/microsphere/Example.class");
        assertTrue(INSTANCE.accept(classEntry));
    }

    @Test
    void testRejectDirectory() {
        JarEntry dirEntry = new JarEntry("io/microsphere/");
        assertFalse(INSTANCE.accept(dirEntry));
    }

    @Test
    void testRejectNonClassFile() {
        JarEntry resourceEntry = new JarEntry("META-INF/MANIFEST.MF");
        assertFalse(INSTANCE.accept(resourceEntry));
    }

    @Test
    void testRejectPropertiesFile() {
        JarEntry propertiesEntry = new JarEntry("META-INF/services/io.microsphere.convert.Converter");
        assertFalse(INSTANCE.accept(propertiesEntry));
    }
}
