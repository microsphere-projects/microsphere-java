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

import static io.microsphere.util.ValueHolder.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link ValueHolder} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ValueHolder
 * @since 1.0.0
 */
public class ValueHolderTest {

    @Test
    public void testValueHolder() {
        // Test empty constructor
        ValueHolder valueHolder = new ValueHolder();
        // Assert that the value is null
        assertNull(valueHolder.getValue());

        // Test constructor with value
        String initialValue = "initial value";
        valueHolder = new ValueHolder(initialValue);
        // Assert that the value is equal to the initial value
        assertEquals(initialValue, valueHolder.getValue());
    }

    @Test
    public void testSetValue() {
        ValueHolder valueHolder = new ValueHolder();
        String newValue = "new value";
        valueHolder.setValue(newValue);
        // Assert that the value is equal to the new value
        assertEquals(newValue, valueHolder.getValue());
    }

    @Test
    public void testGetValue() {
        ValueHolder valueHolder = of("initial value");
        // Assert that the value is equal to the initial value
        assertEquals("initial value", valueHolder.getValue());
    }

    @Test
    public void testReset() {
        ValueHolder valueHolder = of(1);
        valueHolder.reset();
        assertNull(valueHolder.getValue());
    }


    @Test
    public void testToString() {
        ValueHolder valueHolder = of(1);
        assertEquals("ValueHolder{value=1}", valueHolder.toString());
    }

    @Test
    public void testEquals() {
        ValueHolder valueHolder = of(1);
        assertEquals(valueHolder, valueHolder);
        assertNotEquals(1, valueHolder);
        assertEquals(of(1), valueHolder);
        assertEquals(of("A"), of("A"));
        assertEquals(of(null), of(null));
    }

    @Test
    public void testHashCode() {
        assertEquals(1, of(1).hashCode());
        assertEquals("A".hashCode(), of("A").hashCode());
        assertEquals(0, of(null).hashCode());
    }
}
