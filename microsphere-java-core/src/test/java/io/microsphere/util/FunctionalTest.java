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

import static io.microsphere.util.Functional.of;
import static io.microsphere.util.Functional.value;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link Functional} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Functional
 * @since 1.0.0
 */
public class FunctionalTest {

    private ValueHolder<Object> valueHolder = new ValueHolder();

    @Test
    public void testValueAndOn() {

        assertValue(value(1).on(v -> v > 0), 1);

        assertValue(value(1).on(v -> v < 0), null);

        assertValue(value(1).on(v -> v < 0).on(v -> v > 0), null);

    }

    @Test
    public void testValueAndOnAndAs() {

        assertValue(value(1).on(v -> v < 0).as(String::valueOf), null);

        assertValue(value(1).on(v -> v < 0).as(String::valueOf).as(Integer::new), null);
    }

    @Test
    public void testValueSupplier() {
        assertValue(value(() -> 1).on(v -> v > 0), 1);
    }

    @Test
    public void testOfWithNameAndValue() {
        assertValue(of("test", 1).on(v -> v > 0).as(String::valueOf), "1");
    }

    @Test
    public void testOfWithNameAndValueSupplier() {
        assertValue(of("test", () -> 1).on(v -> v > 0).as(String::valueOf), "1");
    }

    private <T> void assertValue(Functional<T> functional, T expected) {
        functional.apply(valueHolder::setValue);
        assertEquals(expected, valueHolder.getValue());
        assertNotNull(functional.toString());
        valueHolder.reset();
    }

}
