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


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import static io.microsphere.collection.EnumerationUtils.ofEnumeration;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Collections.emptyEnumeration;
import static java.util.Collections.enumeration;
import static java.util.Objects.hash;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract {@link Enumeration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Enumeration
 * @since 1.0.0
 */
abstract class AbstractEnumerationTest {

    protected static final Object[] values = ofArray(1, 2, 3);

    protected Enumeration<Object> enumeration;

    @BeforeEach
    void setUp() {
        this.enumeration = createEnumeration();
    }

    protected abstract Enumeration<Object> createEnumeration();

    @Test
    void testHashCode() {
        assertEquals(hash(values), this.enumeration.hashCode());
    }

    @Test
    void testEquals() {
        assertNotEquals(this.enumeration, null);
        assertNotEquals(emptyEnumeration(), this.enumeration);
        assertNotEquals(enumeration(ofList(1)), this.enumeration);
        assertNotEquals(enumeration(ofList(1, 2)), this.enumeration);
        assertNotEquals(enumeration(ofList(1, 2, 3)), this.enumeration);
        assertEquals(ofEnumeration(1, 2, 3), this.enumeration);
    }

    @Test
    void testToString() {
        assertEquals("[1, 2, 3]", this.enumeration.toString());
    }

    @Test
    void testHasMoreElements() {
        assertTrue(this.enumeration.hasMoreElements());
    }

    @Test
    void testNextElement() {
        assertEquals(1, this.enumeration.nextElement());
        assertEquals(2, this.enumeration.nextElement());
        assertEquals(3, this.enumeration.nextElement());
        assertFalse(this.enumeration.hasMoreElements());
        assertThrows(NoSuchElementException.class, this.enumeration::nextElement);
    }
}