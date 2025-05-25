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

package io.microsphere.annotation.processor.util;


import org.junit.jupiter.api.Test;

import javax.lang.model.element.ElementKind;
import java.lang.annotation.ElementType;

import static io.microsphere.annotation.processor.util.EnumUtils.matches;
import static io.microsphere.annotation.processor.util.EnumUtils.toElementKind;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.values;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link EnumUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnumUtils
 * @since 1.0.0
 */
class EnumUtilsTest {

    @Test
    void testToElementKind() {
        for (ElementType elementType : values()) {
            assertElementKind(elementType);
        }
    }

    @Test
    public void testMatches() {
        for (ElementType elementType : values()) {
            assertMatches(elementType);
        }
    }

    @Test
    public void testMatchesOnNull() {
        assertFalse(matches(null, (ElementType) null));
        assertFalse(matches(null, TYPE));
        assertFalse(matches(toElementKind(TYPE), (ElementType) null));
    }

    @Test
    public void testMatchesWithArray() {
        for (ElementType elementType : values()) {
            assertTrue(matches(toElementKind(elementType), values()));
        }
    }

    @Test
    public void testMatchesWithArrayOnNull() {
        assertFalse(matches(null));
        assertFalse(matches(null, (ElementType[]) null));
        assertFalse(matches(null, TYPE, FIELD));
        assertFalse(matches(toElementKind(TYPE), (ElementType[]) null));
    }

    void assertElementKind(ElementType elementType) {
        ElementKind elementKind = toElementKind(elementType);
        assertNotNull(elementKind);
    }

    void assertMatches(ElementType elementType) {
        assertTrue(matches(toElementKind(elementType), elementType));
    }
}