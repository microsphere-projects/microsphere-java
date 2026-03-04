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

package io.microsphere.io;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link Deserializers} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Deserializers
 * @since 1.0.0
 */
class DeserializersTest {

    private Deserializers deserializers;

    @BeforeEach
    void setUp() {
        this.deserializers = new Deserializers();
    }

    @Test
    void testLoadSPI() {
        assertEquals(emptyList(), this.deserializers.get(String.class));
        assertEquals(emptyList(), this.deserializers.get(Object.class));

        this.deserializers.loadSPI();

        assertNotNull(this.deserializers.get(String.class));
        assertNotNull(this.deserializers.get(Object.class));
    }

    @Test
    void testGetMostCompatible() {
        assertNull(this.deserializers.getMostCompatible(String.class));
        assertNull(this.deserializers.getMostCompatible(Object.class));

        this.deserializers.loadSPI();

        assertNotNull(this.deserializers.getMostCompatible(String.class));
        assertNotNull(this.deserializers.getMostCompatible(Object.class));
    }

    @Test
    void testGetHighestPriority() {
        assertNull(this.deserializers.getHighestPriority(String.class));
        assertNull(this.deserializers.getHighestPriority(Object.class));

        this.deserializers.loadSPI();

        assertNotNull(this.deserializers.getHighestPriority(String.class));
        assertNotNull(this.deserializers.getHighestPriority(Object.class));
    }

    @Test
    void testGetLowestPriority() {
        assertNull(this.deserializers.getLowestPriority(String.class));
        assertNull(this.deserializers.getLowestPriority(Object.class));

        this.deserializers.loadSPI();

        assertNotNull(this.deserializers.getLowestPriority(String.class));
        assertNotNull(this.deserializers.getLowestPriority(Object.class));
    }

    @Test
    void testGet() {
        assertEquals(emptyList(), this.deserializers.get(String.class));
        assertEquals(emptyList(), this.deserializers.get(Object.class));

        this.deserializers.loadSPI();

        assertNotNull(this.deserializers.getMostCompatible(String.class));
        assertNotNull(this.deserializers.getMostCompatible(Object.class));
    }
}