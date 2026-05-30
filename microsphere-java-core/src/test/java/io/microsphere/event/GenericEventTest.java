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
package io.microsphere.event;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link GenericEvent} Test
 *
 * @since 1.0.0
 */
class GenericEventTest {

    @Test
    void test() {

        long timestamp = System.currentTimeMillis();
        GenericEvent<String> event = new GenericEvent("Hello,World");

        assertEquals("Hello,World", event.getSource());
        assertTrue(event.getTimestamp() >= timestamp);
    }

    @Test
    void testNullSourceThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new GenericEvent<>(null));
    }

    @Test
    void testTimestampBounds() {
        long before = System.currentTimeMillis();
        GenericEvent<String> event = new GenericEvent<>("Test");
        long after = System.currentTimeMillis();
        assertTrue(event.getTimestamp() >= before);
        assertTrue(event.getTimestamp() <= after);
    }

    @Test
    void testTimestampOrdering() throws InterruptedException {
        GenericEvent<String> event1 = new GenericEvent<>("First");
        Thread.sleep(1);
        GenericEvent<String> event2 = new GenericEvent<>("Second");
        assertTrue(event2.getTimestamp() >= event1.getTimestamp());
    }

    @Test
    void testWithIntegerSource() {
        GenericEvent<Integer> event = new GenericEvent<>(42);
        assertEquals(42, event.getSource());
    }

    @Test
    void testWithListSource() {
        List<String> source = Arrays.asList("a", "b", "c");
        GenericEvent<List<String>> event = new GenericEvent<>(source);
        assertEquals(source, event.getSource());
    }

}
