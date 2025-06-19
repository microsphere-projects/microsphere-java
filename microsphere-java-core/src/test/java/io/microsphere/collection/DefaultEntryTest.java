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

import static io.microsphere.collection.DefaultEntry.of;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link DefaultEntry} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DefaultEntry
 * @since 1.0.0
 */
class DefaultEntryTest {

    private DefaultEntry<String, String> entry;

    @BeforeEach
    void setUp() {
        this.entry = of("key", "value");
    }

    @Test
    void testGetKey() {
        assertEquals("key", entry.getKey());
    }

    @Test
    void testGetValue() {
        assertEquals("value", entry.getValue());
    }

    @Test
    void testSetValue() {
        assertEquals("value", entry.setValue("newValue"));
        assertEquals("newValue", entry.getValue());
    }

    @Test
    void testEquals() {
        assertEquals(this.entry, of("key", "value"));
        assertNotEquals(this.entry, of("key", "value-1"));
        assertNotEquals(this.entry, of("key-1", "value"));
        assertNotEquals(this.entry, new Object());
    }

    @Test
    void testHashCode() {
        assertEquals(this.entry.hashCode(), of("key", "value").hashCode());
    }

    @Test
    void testToString() {
        assertEquals("key=value", entry.toString());
    }
}