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
 * {@link Serializers} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Serializers
 * @since 1.0.0
 */
class SerializersTest {

    private Serializers serializers;

    @BeforeEach
    void setUp() {
        this.serializers = new Serializers();
    }

    @Test
    void testLoadSPI() {
        assertEquals(emptyList(), this.serializers.get(String.class));
        assertEquals(emptyList(), this.serializers.get(Object.class));

        this.serializers.loadSPI();

        assertNotNull(this.serializers.get(String.class));
        assertNotNull(this.serializers.get(Object.class));
    }

    @Test
    void testGetMostCompatible() {
        assertNull(this.serializers.getMostCompatible(String.class));
        assertNull(this.serializers.getMostCompatible(Object.class));

        this.serializers.loadSPI();

        assertNotNull(this.serializers.getMostCompatible(String.class));
        assertNotNull(this.serializers.getMostCompatible(Object.class));
    }

    @Test
    void testGetHighestPriority() {
        assertNull(this.serializers.getHighestPriority(String.class));
        assertNull(this.serializers.getHighestPriority(Object.class));

        this.serializers.loadSPI();

        assertNotNull(this.serializers.getHighestPriority(String.class));
        assertNotNull(this.serializers.getHighestPriority(Object.class));
    }

    @Test
    void testGetLowestPriority() {
        assertNull(this.serializers.getLowestPriority(String.class));
        assertNull(this.serializers.getLowestPriority(Object.class));

        this.serializers.loadSPI();

        assertNotNull(this.serializers.getLowestPriority(String.class));
        assertNotNull(this.serializers.getLowestPriority(Object.class));
    }

    @Test
    void testGet() {
        assertEquals(emptyList(), this.serializers.get(String.class));
        assertEquals(emptyList(), this.serializers.get(Object.class));

        this.serializers.loadSPI();

        assertNotNull(this.serializers.getMostCompatible(String.class));
        assertNotNull(this.serializers.getMostCompatible(Object.class));
    }
}