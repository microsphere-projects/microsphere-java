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
package io.microsphere.collection;

import org.junit.jupiter.api.Test;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import static io.microsphere.collection.EnumerationUtils.isEnumeration;
import static io.microsphere.collection.EnumerationUtils.of;
import static io.microsphere.collection.EnumerationUtils.ofEnumeration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link EnumerationUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EnumerationUtils
 * @since 1.0.0
 */
class EnumerationUtilsTest {

    @Test
    void testIsEnumerationWithObject() {
        assertTrue(isEnumeration(of("A", "B", "C")));

        assertFalse(isEnumeration("Hello"));
        assertFalse(isEnumeration((Object) null));
    }

    @Test
    void testIsEnumerationWithClass() {
        assertTrue(isEnumeration(Enumeration.class));

        assertFalse(isEnumeration(String.class));
        assertFalse(isEnumeration(null));
    }

    @Test
    void testOf() {
        assertEnumeration(of("A", "B", "C"));
    }

    @Test
    void testOfEnumeration() {
        assertEnumeration(ofEnumeration("A", "B", "C"));
    }

    private static <E> void assertEnumeration(Enumeration<String> e) {
        assertNotNull(e);

        assertTrue(e.hasMoreElements());
        assertEquals("A", e.nextElement());

        assertTrue(e.hasMoreElements());
        assertEquals("B", e.nextElement());

        assertTrue(e.hasMoreElements());
        assertEquals("C", e.nextElement());

        assertFalse(e.hasMoreElements());
        assertThrows(NoSuchElementException.class, e::nextElement);
    }

}
