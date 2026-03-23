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

    /**
     * Constructs a new {@link Functional} instance with the given name and value.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Subclass creating a named Functional wrapper
     *   Functional<String> func = new Functional<>("greeting", "Hello, World!");
     * }</pre>
     *
     * @param name  the descriptive name for this functional instance
     * @param value the value to be operated on
     * @since 1.0.0
     */
    protected Functional(String name, V value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Tests the current value against the given {@link Predicate}. If the predicate matches,
     * subsequent operations ({@link #as(Function)}, {@link #apply(Consumer)}) will be executed;
     * otherwise, they will be skipped. If the value is {@code null}, the predicate is not evaluated.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Functional.value("microsphere")
     *       .on(s -> s.startsWith("micro"))
     *       .apply(s -> System.out.println("Matched: " + s));
     * }</pre>
     *
     * @param predicate the condition to test the value against
     * @return this {@link Functional} instance for fluent chaining
     * @since 1.0.0
     */
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

    /**
     * Transforms the current value using the given {@link Function} and returns a new {@link Functional}
     * wrapping the result. If the previous {@link #on(Predicate)} did not match, the transformation
     * is skipped and this instance is returned as-is.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Functional<Integer> length = Functional.value("Hello")
     *       .on(s -> s.length() > 3)
     *       .as(String::length);
     * }</pre>
     *
     * @param <R>      the type of the transformed result
     * @param function the transformation function to apply to the value
     * @return a new {@link Functional} wrapping the transformed result, or this instance if skipped
     * @since 1.0.0
     */
    public <R> Functional<R> as(Function<V, R> function) {
        if (isSkip()) {
            return (Functional<R>) this;
        }
        final R result = function.apply(this.value);
        return new Functional(this.name, result);
    }

    /**
     * Applies the given {@link Consumer} to the current value as a terminal operation.
     * If the previous {@link #on(Predicate)} did not match, the consumer is not invoked.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Functional.value(99)
     *       .on(n -> n > 50)
     *       .apply(n -> System.out.println("Large number: " + n));
     * }</pre>
     *
     * @param valueConsumer the consumer to apply to the value
     * @since 1.0.0
     */
    public void apply(Consumer<V> valueConsumer) {
        if (isSkip()) {
            return;
        }
        valueConsumer.accept(value);
    }

    private boolean isSkip() {
        return FALSE.equals(matched);
    }

    /**
     * Returns a string representation of this {@link Functional} instance, including its name,
     * value, and current match state.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   String text = Functional.of("config", "debug").toString();
     *   // Output: Functional{name='config', value=debug, matched=null}
     * }</pre>
     *
     * @return a string describing this instance
     * @since 1.0.0
     */
    @Override
    public String toString() {
        return "Functional{" +
                "name='" + name + QUOTE_CHAR +
                ", value=" + value +
                ", matched=" + matched +
                '}';
    }

    /**
     * Creates an unnamed {@link Functional} instance whose value is obtained from the given
     * {@link Supplier}. The supplier is evaluated immediately.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Functional<Long> timestamp = Functional.value(System::currentTimeMillis);
     *   timestamp.on(t -> t > 0L)
     *       .apply(t -> System.out.println("Current time: " + t));
     * }</pre>
     *
     * @param <V>           the type of the value
     * @param valueSupplier the supplier providing the value
     * @return a new unnamed {@link Functional} instance wrapping the supplied value
     * @since 1.0.0
     */
    public static <V> Functional<V> value(Supplier<V> valueSupplier) {
        return value(valueSupplier.get());
    }

    /**
     * Creates an unnamed {@link Functional} instance wrapping the given value.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Functional.value("Hello, World!")
     *       .on(s -> !s.isEmpty())
     *       .as(String::toUpperCase)
     *       .apply(s -> System.out.println(s));
     * }</pre>
     *
     * @param <V>   the type of the value
     * @param value the value to wrap
     * @return a new unnamed {@link Functional} instance wrapping the value
     * @since 1.0.0
     */
    public static <V> Functional<V> value(V value) {
        return of(UNNAMED, value);
    }

    /**
     * Creates a named {@link Functional} instance whose value is obtained from the given
     * {@link Supplier}. The supplier is evaluated immediately. The name is useful for
     * debugging and identification in {@link #toString()}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Functional<List<String>> items = Functional.of("inventory", () -> fetchItems());
     *   items.on(list -> !list.isEmpty())
     *       .apply(list -> System.out.println("Items count: " + list.size()));
     * }</pre>
     *
     * @param <V>           the type of the value
     * @param name          the descriptive name for this functional instance
     * @param valueSupplier the supplier providing the value
     * @return a new named {@link Functional} instance wrapping the supplied value
     * @since 1.0.0
     */
    public static <V> Functional<V> of(String name, Supplier<V> valueSupplier) {
        return of(name, valueSupplier.get());
    }

    /**
     * Creates a named {@link Functional} instance wrapping the given value. The name is useful
     * for debugging and identification in {@link #toString()}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Functional.of("user-email", user.getEmail())
     *       .on(email -> email.contains("@"))
     *       .as(String::toLowerCase)
     *       .apply(email -> sendNotification(email));
     * }</pre>
     *
     * @param <V>   the type of the value
     * @param name  the descriptive name for this functional instance
     * @param value the value to wrap
     * @return a new named {@link Functional} instance wrapping the value
     * @since 1.0.0
     */
    public static <V> Functional<V> of(String name, V value) {
        return new Functional<>(name, value);
    }
}
