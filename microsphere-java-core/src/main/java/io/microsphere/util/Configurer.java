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

import io.microsphere.logging.Logger;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.text.FormatUtils.format;

/**
 * A fluent configuration utility for applying conditional operations on a value of type {@code T}.
 * <p>
 * The {@code Configurer} class provides a builder-style API to configure and manipulate values,
 * with support for comparison, predicate checks, type conversion, and application of consumer actions.
 * It is designed to be non-thread-safe and intended for single-thread usage.
 * </p>
 *
 * <h3>Key Features</h3>
 * <ul>
 *     <li>Value initialization with static factory methods.</li>
 *     <li>Conditional comparison to detect changes in value.</li>
 *     <li>Predicate-based filtering to control flow based on the current value.</li>
 *     <li>Type conversion using functional interfaces.</li>
 *     <li>Consumer application to perform side effects on the final value.</li>
 *     <li>Detailed logging of each operation for traceability.</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 *
 * <h4>Basic Configuration Flow</h4>
 * <pre>{@code
 * Configurer.configure("timeout", 30)
 *           .compare(25)
 *           .on(value -> value > 0)
 *           .as(value -> value * 1000)
 *           .apply(value -> System.setProperty("timeout.ms", String.valueOf(value)));
 * }</pre>
 *
 * <h4>Chaining Multiple Operations</h4>
 * <pre>{@code
 * Configurer.configure(() -> System.getenv("DEBUG_MODE"))
 *           .on(mode -> "true".equalsIgnoreCase(mode))
 *           .as(Boolean::valueOf)
 *           .apply(enabled -> logger.info("Debug mode is enabled: {}", enabled));
 * }</pre>
 *
 * <h4>Discarding or Filtering Values</h4>
 * <pre>{@code
 * Configurer.configure("username", () -> System.getProperty("user.name"))
 *           .on(name -> name.length() > 3)
 *           .apply(name -> System.out.println("Welcome, " + name));
 * }</pre>
 *
 * <p>
 * In the above example, if the username length is less than or equal to 3, the value will be discarded,
 * and no action will be taken.
 * </p>
 *
 * @param <T> the type of the value being configured
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class Configurer<T> {

    private static final Logger logger = getLogger(Configurer.class);

    private static final String UNNAMED = "UNNAMED";

    private final String name;

    private T value;

    private StringBuilder logBuilder = new StringBuilder();

    protected Configurer(String name) {
        this.name = name;
    }

    protected Configurer(Supplier<T> valueSupplier) {
        this(valueSupplier.get());
    }

    protected Configurer(T value) {
        this(UNNAMED, value);
    }

    protected Configurer(String name, Supplier<T> valueSupplier) {
        this(name, valueSupplier.get());
    }

    protected Configurer(String name, T value) {
        this.name = name;
        this.value = value;
        this.logBuilder = new StringBuilder();
        logBuilder.append(format("'{}' the config value is initialized ：'{}'", name, value));
    }

    public <T> Configurer<T> value(T value) {
        return new Configurer<>(name, value);
    }

    public <T> Configurer<T> value(Supplier<T> valueSupplier) {
        return new Configurer<>(name, valueSupplier);
    }

    public Configurer<T> compare(Supplier<T> comparedValueSupplier) {
        return compare(comparedValueSupplier.get());
    }

    public Configurer<T> compare(T comparedValue) {
        if (value != null) {
            if (Objects.equals(value, comparedValue)) {
                logBuilder.append(format(", the config value is not changed[current：'{}'，compared：'{}']", value, comparedValue));
                value = null;
            }
        }
        return this;
    }


    public Configurer<T> on(Predicate<? super T> predicate) {
        if (value != null) {
            if (!predicate.test(value)) {
                logBuilder.append(format(", the config value[current：'{}'] does not match", value));
                value = null;
            }
        }
        return this;
    }

    public <R> Configurer<R> as(Function<T, R> function) {
        final R result;
        if (value != null) {
            result = function.apply(value);
            logBuilder.append(format(", the config value is converted[current：'{}'，target：'{}']", value, result));
        } else {
            result = null;
        }

        if (result == null) {
            value = null;
            return (Configurer<R>) this;
        }

        Configurer configurer = new Configurer(name, result);
        configurer.logBuilder = logBuilder;
        return configurer;
    }

    public void apply(Consumer<T> valueConsumer) {
        if (value != null) {
            valueConsumer.accept(value);
            logBuilder.append(", the config value is applied");
        } else {
            logBuilder.append(", the config value is discarded");
        }
        logger.info(logBuilder.toString());
    }

    public static <T> Configurer<T> configure(String name) {
        return new Configurer<>(name);
    }

    public static <T> Configurer<T> configure(String name, T value) {
        return new Configurer<>(name, value);
    }

    public static <T> Configurer<T> configure(String name, Supplier<T> valueSupplier) {
        return new Configurer<>(name, valueSupplier);
    }

    public static <T> Configurer<T> configure(T value) {
        return new Configurer<>(value);
    }

    public static <T> Configurer<T> configure(Supplier<T> valueSupplier) {
        return new Configurer<>(valueSupplier);
    }
}
