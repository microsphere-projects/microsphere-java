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

    /**
     * Constructs an empty {@link ValueHolder} with a {@code null} initial value.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ValueHolder<String> holder = new ValueHolder<>();
     *   System.out.println(holder.getValue()); // Output: null
     * }</pre>
     *
     * @since 1.0.0
     */
    public ValueHolder() {
    }

    /**
     * Constructs a {@link ValueHolder} with the specified initial value.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ValueHolder<Integer> holder = new ValueHolder<>(42);
     *   System.out.println(holder.getValue()); // Output: 42
     * }</pre>
     *
     * @param value the initial value to hold, may be {@code null}
     * @since 1.0.0
     */
    public ValueHolder(V value) {
        this.value = value;
    }

    /**
     * Returns the currently held value.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ValueHolder<String> holder = ValueHolder.of("Hello");
     *   String value = holder.getValue(); // "Hello"
     * }</pre>
     *
     * @return the held value, or {@code null} if no value has been set
     * @since 1.0.0
     */
    public V getValue() {
        return value;
    }

    /**
     * Sets the held value to the specified value.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ValueHolder<Double> holder = new ValueHolder<>();
     *   holder.setValue(3.14);
     *   System.out.println(holder.getValue()); // Output: 3.14
     * }</pre>
     *
     * @param value the value to hold, may be {@code null}
     * @since 1.0.0
     */
    public void setValue(V value) {
        this.value = value;
    }

    /**
     * Resets the held value to {@code null}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ValueHolder<String> holder = ValueHolder.of("data");
     *   holder.reset();
     *   System.out.println(holder.getValue()); // Output: null
     * }</pre>
     *
     * @since 1.0.0
     */
    public void reset() {
        setValue(null);
    }

    /**
     * Checks whether this {@link ValueHolder} is equal to another object. Two {@link ValueHolder}
     * instances are considered equal if they hold equal values as determined by {@link Objects#equals(Object, Object)}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ValueHolder<Integer> holder1 = ValueHolder.of(100);
     *   ValueHolder<Integer> holder2 = ValueHolder.of(100);
     *   System.out.println(holder1.equals(holder2)); // Output: true
     * }</pre>
     *
     * @param o the object to compare with
     * @return {@code true} if the other object is a {@link ValueHolder} holding an equal value, {@code false} otherwise
     * @since 1.0.0
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ValueHolder)) {
            return false;
        }
        ValueHolder<?> that = (ValueHolder<?>) o;
        return Objects.equals(value, that.value);
    }

    /**
     * Returns the hash code of the held value using {@link Objects#hashCode(Object)}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ValueHolder<String> holder = ValueHolder.of("key");
     *   int hash = holder.hashCode(); // hash code of "key"
     * }</pre>
     *
     * @return the hash code of the held value, or {@code 0} if the value is {@code null}
     * @since 1.0.0
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    /**
     * Returns a string representation of this {@link ValueHolder} in the format
     * {@code ValueHolder{value=...}}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ValueHolder<String> holder = ValueHolder.of("test");
     *   System.out.println(holder.toString()); // Output: ValueHolder{value=test}
     * }</pre>
     *
     * @return a string representation of this holder and its value
     * @since 1.0.0
     */
    @Override
    public String toString() {
        return "ValueHolder{" +
                "value=" + value +
                '}';
    }

    /**
     * Creates a new {@link ValueHolder} containing the specified value.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ValueHolder<String> holder = ValueHolder.of("Hello World");
     *   System.out.println(holder.getValue()); // Output: Hello World
     * }</pre>
     *
     * @param value the value to hold, may be {@code null}
     * @param <V>   the type of the value
     * @return a new {@link ValueHolder} containing the given value
     * @since 1.0.0
     */
    public static <V> ValueHolder<V> of(V value) {
        return new ValueHolder<>(value);
    }

}