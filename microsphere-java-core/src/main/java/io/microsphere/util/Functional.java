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

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.microsphere.constants.SymbolConstants.QUOTE_CHAR;
import static java.lang.Boolean.FALSE;

/**
 * A fluent API utility class for performing chained operations on a value of type {@code V}.
 * <p>
 * This class allows for conditional processing ({@link #on(Predicate)}), value transformation ({@link #as(Function)}),
 * and side-effect execution ({@link #apply(Consumer)}) in a fluent and readable manner.
 * </p>
 * <p>
 * The state of the object is not thread-safe due to the mutable internal state tracking (e.g., matched).
 * It's designed for single-threaded use or must be externally synchronized when used concurrently.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Example 1: Conditional mapping
 * String result = Functional.value("Hello")
 *     .on(s -> s.length() > 3)
 *     .as(String::toUpperCase)
 *     .value();
 *
 * // Example 2: Conditional side effect
 * Functional.value(42)
 *     .on(n -> n > 40)
 *     .apply(n -> System.out.println("Value is: " + n));
 *
 * // Example 3: Named functional operation
 * Functional.of("user", user)
 *     .on(u -> u.isActive())
 *     .as(User::getName)
 *     .apply(name -> System.out.println("Active user: " + name));
 * }</pre>
 *
 * @param <V> The type of the value being operated on
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class Functional<V> {

    private static final String UNNAMED = "UNNAMED";

    private final String name;

    private final V value;

    private Boolean matched = null;

    protected Functional(String name, V value) {
        this.name = name;
        this.value = value;
    }

    public Functional<V> on(Predicate<? super V> predicate) {
        if (isSkip()) {
            return this;
        }
        V value = this.value;
        if (value != null) {
            matched = predicate.test(value);
        }
        return this;
    }

    public <R> Functional<R> as(Function<V, R> function) {
        if (isSkip()) {
            return (Functional<R>) this;
        }
        final R result = function.apply(this.value);
        return new Functional(this.name, result);
    }

    public void apply(Consumer<V> valueConsumer) {
        if (isSkip()) {
            return;
        }
        valueConsumer.accept(value);
    }

    private boolean isSkip() {
        return FALSE.equals(matched);
    }

    @Override
    public String toString() {
        return "Functional{" +
                "name='" + name + QUOTE_CHAR +
                ", value=" + value +
                ", matched=" + matched +
                '}';
    }

    public static <V> Functional<V> value(Supplier<V> valueSupplier) {
        return value(valueSupplier.get());
    }

    public static <V> Functional<V> value(V value) {
        return of(UNNAMED, value);
    }

    public static <V> Functional<V> of(String name, Supplier<V> valueSupplier) {
        return of(name, valueSupplier.get());
    }

    public static <V> Functional<V> of(String name, V value) {
        return new Functional<>(name, value);
    }
}
