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
package io.microsphere.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.microsphere.collection.MapUtils.ofMap;
import static io.microsphere.constants.SymbolConstants.SPACE;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.ClassLoaderUtils.ResourceType.CLASS;
import static io.microsphere.util.ClassLoaderUtils.ResourceType.DEFAULT;
import static io.microsphere.util.ClassLoaderUtils.ResourceType.PACKAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ClassLoaderUtils.ResourceType} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ClassLoaderUtils.ResourceType
 * @since 1.0.0
 */
class ResourceTypeTest {

    private static final Map<ClassLoaderUtils.ResourceType, String[]> resourceNamesMap = ofMap(
            DEFAULT, ofArray("///////META-INF//abc\\/def", "META-INF/abc/def"),
            CLASS, ofArray("java.lang.String.class", "java/lang/String.class"),
            PACKAGE, ofArray("java.lang", "java/lang/")
    );


    @Test
    void testSupports() {
        assertTrue(DEFAULT.supports(null));
        assertTrue(DEFAULT.supports(""));
        assertTrue(DEFAULT.supports("a"));

        assertFalse(CLASS.supports(null));
        assertFalse(CLASS.supports("java.lang.String"));
        assertTrue(CLASS.supports("java.lang.String.class"));

        assertFalse(PACKAGE.supports(null));
        assertFalse(PACKAGE.supports("java.lang.String.class"));
        assertFalse(PACKAGE.supports("java/lang/String"));
        assertFalse(PACKAGE.supports("java\\lang\\String"));
        assertTrue(PACKAGE.supports("java.lang"));
    }

    @Test
    void testNormalize() {
        assertNull(DEFAULT.normalize(null));
        assertEquals("test", DEFAULT.normalize("test"));

        assertNull(CLASS.normalize(null));
        assertEquals("java/lang/String.class", CLASS.normalize("java/lang/String.class"));
        assertEquals("java/lang/String.class", CLASS.normalize("java/lang/String"));
        assertEquals("java/lang/String.class", CLASS.normalize("java.lang.String.class"));
        assertEquals("java/lang/String.class", CLASS.normalize("java/lang/String"));

        assertNull(PACKAGE.normalize(null));
        assertEquals("java/lang/", PACKAGE.normalize("java/lang/"));
        assertEquals("java/lang/", PACKAGE.normalize("java/lang"));
        assertEquals("java/lang/", PACKAGE.normalize("java.lang."));
        assertEquals("java/lang/", PACKAGE.normalize("java.lang"));

    }

    @Test
    void testResolve() {
        for (Map.Entry<ClassLoaderUtils.ResourceType, String[]> entry : resourceNamesMap.entrySet()) {
            ClassLoaderUtils.ResourceType resourceType = entry.getKey();
            String[] resourceNames = entry.getValue();
            assertNull(resourceType.resolve(null));
            assertNull(resourceType.resolve(""));
            assertNull(resourceType.resolve(SPACE));
            assertEquals(resourceNames[1], resourceType.resolve(resourceNames[0]));
        }

        assertNull(DEFAULT.resolve(null));
        assertNull(CLASS.resolve("java.lang.String"));
        assertNull(PACKAGE.resolve("java.lang.String.class"));
    }
}
