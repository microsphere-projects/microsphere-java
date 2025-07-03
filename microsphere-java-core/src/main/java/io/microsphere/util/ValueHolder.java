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

import java.util.Objects;

/**
 * A container class to hold a mutable value of any type. This class is not thread-safe.
 *
 * <p>It provides methods to set, get, and reset the value, as well as utility methods like
 * {@link #equals(Object)}, {@link #hashCode()}, and {@link #toString()} for general usage.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Create an empty ValueHolder for String type
 * ValueHolder<String> stringHolder = new ValueHolder<>();
 * stringHolder.setValue("Hello");
 * System.out.println(stringHolder.getValue());  // Output: Hello
 *
 * // Create a ValueHolder with an initial value using factory method
 * ValueHolder<Integer> intHolder = ValueHolder.of(123);
 * System.out.println(intHolder.getValue());     // Output: 123
 *
 * // Reset the value back to null
 * intHolder.reset();
 * System.out.println(intHolder.getValue());     // Output: null
 * }</pre>
 *
 * @param <V> the type of the value being held
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ValueHolder<V> {

    private V value;

    public ValueHolder() {
    }

    public ValueHolder(V value) {
        this.value = value;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public void reset() {
        setValue(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueHolder<?> that = (ValueHolder<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "ValueHolder{" +
                "value=" + value +
                '}';
    }

    public static <V> ValueHolder<V> of(V value) {
        return new ValueHolder<>(value);
    }

}
