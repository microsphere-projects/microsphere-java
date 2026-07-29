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

package io.microsphere.io.serializer;

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

    private static final Class<?>[] SUPPORTED_CLASSES = {
            Boolean.class,
            Character.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            String.class,
            Object.class
    };

    private Serializers serializers;

    @BeforeEach
    void setUp() {
        this.serializers = new Serializers();
    }

    @Test
    void testtGet() {
        for (Class<?> supportedClass : SUPPORTED_CLASSES) {
            assertEquals(emptyList(), this.serializers.get(supportedClass));
        }

        this.serializers.loadSPI();

        for (Class<?> supportedClass : SUPPORTED_CLASSES) {
            assertNotNull(this.serializers.get(supportedClass));
        }
    }

    @Test
    void testGetMostCompatible() {
        for (Class<?> supportedClass : SUPPORTED_CLASSES) {
            assertNull(this.serializers.getMostCompatible(supportedClass));
        }

        this.serializers.loadSPI();

        for (Class<?> supportedClass : SUPPORTED_CLASSES) {
            assertNotNull(this.serializers.getMostCompatible(supportedClass));
        }
    }

    @Test
    void testGetHighestPriority() {
        for (Class<?> supportedClass : SUPPORTED_CLASSES) {
            assertNull(this.serializers.getHighestPriority(supportedClass));
        }

        this.serializers.loadSPI();

        for (Class<?> supportedClass : SUPPORTED_CLASSES) {
            assertNotNull(this.serializers.getHighestPriority(supportedClass));
        }
    }

    @Test
    void testGetLowestPriority() {
        for (Class<?> supportedClass : SUPPORTED_CLASSES) {
            assertNull(this.serializers.getLowestPriority(supportedClass));
        }

        this.serializers.loadSPI();

        for (Class<?> supportedClass : SUPPORTED_CLASSES) {
            assertNotNull(this.serializers.getLowestPriority(supportedClass));
        }
    }
}
