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

    /**
     * Constructs a {@link Configurer} with the specified name and no initial value.
     * The value can be set later using {@link #value(Object)} or {@link #value(Supplier)}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Configurer<Integer> configurer = new Configurer<>("timeout");
     *   configurer.value(30).apply(val -> System.out.println("Timeout: " + val));
     * }</pre>
     *
     * @param name the descriptive name of this configuration entry
     * @since 1.0.0
     */
    protected Configurer(String name) {
        this.name = name;
    }

    /**
     * Constructs a {@link Configurer} with an unnamed configuration entry whose value
     * is eagerly resolved from the given {@link Supplier}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Configurer<String> configurer = new Configurer<>(() -> System.getenv("SERVICE_URL"));
     *   configurer.on(Objects::nonNull).apply(url -> System.out.println("URL: " + url));
     * }</pre>
     *
     * @param valueSupplier a supplier providing the initial configuration value
     * @since 1.0.0
     */
    protected Configurer(Supplier<T> valueSupplier) {
        this(valueSupplier.get());
    }

    /**
     * Constructs a {@link Configurer} with an unnamed configuration entry and the given value.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Configurer<Integer> configurer = new Configurer<>(5000);
     *   configurer.on(val -> val > 0).apply(val -> System.out.println("Timeout ms: " + val));
     * }</pre>
     *
     * @param value the initial configuration value
     * @since 1.0.0
     */
    protected Configurer(T value) {
        this(UNNAMED, value);
    }

    /**
     * Constructs a {@link Configurer} with the specified name and a value eagerly resolved
     * from the given {@link Supplier}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Configurer<String> configurer = new Configurer<>("db.host", () -> System.getProperty("db.host"));
     *   configurer.on(Objects::nonNull).apply(host -> connectTo(host));
     * }</pre>
     *
     * @param name          the descriptive name of this configuration entry
     * @param valueSupplier a supplier providing the initial configuration value
     * @since 1.0.0
     */
    protected Configurer(String name, Supplier<T> valueSupplier) {
        this(name, valueSupplier.get());
    }

    /**
     * Constructs a {@link Configurer} with the specified name and initial value.
     * This is the primary constructor that initializes the internal log builder.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Configurer<Integer> configurer = new Configurer<>("retryCount", 3);
     *   configurer.on(val -> val >= 0)
     *             .apply(val -> System.out.println("Retries set to: " + val));
     * }</pre>
     *
     * @param name  the descriptive name of this configuration entry
     * @param value the initial configuration value
     * @since 1.0.0
     */
    protected Configurer(String name, T value) {
        this.name = name;
        this.value = value;
        this.logBuilder = new StringBuilder();
        logBuilder.append(format("'{}' the config value is initialized ：'{}'", name, value));
    }

    /**
     * Creates a new {@link Configurer} with the same name and the specified value,
     * replacing the current configuration value.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Configurer.configure("timeout")
     *             .value(30)
     *             .apply(val -> System.out.println("Timeout: " + val));
     * }</pre>
     *
     * @param value the new configuration value
     * @param <T>   the type of the new value
     * @return a new {@link Configurer} instance with the given value
     * @since 1.0.0
     */
    public <T> Configurer<T> value(T value) {
        return new Configurer<>(name, value);
    }

    /**
     * Creates a new {@link Configurer} with the same name and a value eagerly resolved
     * from the given {@link Supplier}, replacing the current configuration value.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Configurer.configure("serviceUrl")
     *             .value(() -> System.getProperty("service.url", "http://localhost:8080"))
     *             .apply(url -> System.out.println("Service URL: " + url));
     * }</pre>
     *
     * @param valueSupplier a supplier providing the new configuration value
     * @param <T>           the type of the new value
     * @return a new {@link Configurer} instance with the supplied value
     * @since 1.0.0
     */
    public <T> Configurer<T> value(Supplier<T> valueSupplier) {
        return new Configurer<>(name, valueSupplier);
    }

    /**
     * Compares the current value with a value resolved from the given {@link Supplier}.
     * If the current value equals the compared value, the current value is discarded (set to {@code null})
     * to indicate that no change has occurred.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Configurer.configure("maxConnections", 100)
     *             .compare(() -> loadPreviousMaxConnections())
     *             .apply(val -> updateConnectionPool(val));
     * }</pre>
     *
     * @param comparedValueSupplier a supplier providing the value to compare against
     * @return this {@link Configurer} instance for method chaining
     * @since 1.0.0
     */
    public Configurer<T> compare(Supplier<T> comparedValueSupplier) {
        return compare(comparedValueSupplier.get());
    }

    /**
     * Compares the current value with the given value. If both values are equal, the current
     * value is discarded (set to {@code null}) to indicate that no configuration change occurred.
     * This is useful for avoiding unnecessary updates when the value has not changed.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Configurer.configure("retryCount", 3)
     *             .compare(3)
     *             .apply(val -> System.out.println("Retry count changed to: " + val));
     *   // Nothing is printed because the value has not changed
     * }</pre>
     *
     * @param comparedValue the value to compare against the current value
     * @return this {@link Configurer} instance for method chaining
     * @since 1.0.0
     */
    public Configurer<T> compare(T comparedValue) {
        if (value != null) {
            if (Objects.equals(value, comparedValue)) {
                logBuilder.append(format(", the config value is not changed[current：'{}'，compared：'{}']", value, comparedValue));
                value = null;
            }
        }
        return this;
    }


    /**
     * Filters the current value using the given {@link Predicate}. If the predicate evaluates
     * to {@code false}, the current value is discarded (set to {@code null}) and subsequent
     * operations in the chain will be skipped.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Configurer.configure("timeout", 30)
     *             .on(val -> val > 0 && val <= 300)
     *             .apply(val -> System.out.println("Valid timeout: " + val));
     * }</pre>
     *
     * @param predicate the predicate to test the current value against
     * @return this {@link Configurer} instance for method chaining
     * @since 1.0.0
     */
    public Configurer<T> on(Predicate<? super T> predicate) {
        if (value != null) {
            if (!predicate.test(value)) {
                logBuilder.append(format(", the config value[current：'{}'] does not match", value));
                value = null;
            }
        }
        return this;
    }

    /**
     * Converts the current value to a new type using the given {@link Function}.
     * If the current value is {@code null} or the function returns {@code null},
     * the resulting configurer will hold a {@code null} value.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Configurer.configure("timeout", "5000")
     *             .as(Integer::parseInt)
     *             .on(ms -> ms > 0)
     *             .apply(ms -> httpClient.setReadTimeout(ms));
     * }</pre>
     *
     * @param function the conversion function to apply to the current value
     * @param <R>      the target type after conversion
     * @return a {@link Configurer} holding the converted value
     * @since 1.0.0
     */
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

    /**
     * Terminal operation that applies the given {@link Consumer} to the current value if it is
     * non-{@code null}. If the value has been discarded by a prior {@link #compare} or {@link #on}
     * operation, the consumer is not invoked. This method always logs the outcome.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Configurer.configure("maxPoolSize", 20)
     *             .compare(10)
     *             .on(val -> val > 0)
     *             .apply(val -> dataSource.setMaxPoolSize(val));
     * }</pre>
     *
     * @param valueConsumer the consumer to accept the current value
     * @since 1.0.0
     */
    public void apply(Consumer<T> valueConsumer) {
        if (value != null) {
            valueConsumer.accept(value);
            logBuilder.append(", the config value is applied");
        } else {
            logBuilder.append(", the config value is discarded");
        }
        logger.info(logBuilder.toString());
    }

    /**
     * Creates a new {@link Configurer} with the specified name and no initial value.
     * The value can be set later using {@link #value(Object)} or {@link #value(Supplier)}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Configurer.<Integer>configure("connectionTimeout")
     *             .value(5000)
     *             .on(val -> val > 0)
     *             .apply(val -> client.setConnectionTimeout(val));
     * }</pre>
     *
     * @param name the descriptive name of the configuration entry
     * @param <T>  the type of the value to be configured
     * @return a new {@link Configurer} instance with the given name
     * @since 1.0.0
     */
    public static <T> Configurer<T> configure(String name) {
        return new Configurer<>(name);
    }

    /**
     * Creates a new {@link Configurer} with the specified name and initial value.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Configurer.configure("retryCount", 3)
     *             .compare(previousRetryCount)
     *             .on(val -> val >= 0 && val <= 10)
     *             .apply(val -> retryPolicy.setMaxRetries(val));
     * }</pre>
     *
     * @param name  the descriptive name of the configuration entry
     * @param value the initial configuration value
     * @param <T>   the type of the value to be configured
     * @return a new {@link Configurer} instance with the given name and value
     * @since 1.0.0
     */
    public static <T> Configurer<T> configure(String name, T value) {
        return new Configurer<>(name, value);
    }

    /**
     * Creates a new {@link Configurer} with the specified name and a value eagerly resolved
     * from the given {@link Supplier}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Configurer.configure("cacheSize", () -> Integer.getInteger("cache.size", 256))
     *             .on(size -> size > 0)
     *             .apply(size -> cacheManager.resize(size));
     * }</pre>
     *
     * @param name          the descriptive name of the configuration entry
     * @param valueSupplier a supplier providing the initial configuration value
     * @param <T>           the type of the value to be configured
     * @return a new {@link Configurer} instance with the given name and supplied value
     * @since 1.0.0
     */
    public static <T> Configurer<T> configure(String name, Supplier<T> valueSupplier) {
        return new Configurer<>(name, valueSupplier);
    }

    /**
     * Creates a new unnamed {@link Configurer} with the specified value.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Configurer.configure(Duration.ofSeconds(30))
     *             .as(Duration::toMillis)
     *             .apply(ms -> httpClient.setReadTimeout(ms));
     * }</pre>
     *
     * @param value the initial configuration value
     * @param <T>   the type of the value to be configured
     * @return a new unnamed {@link Configurer} instance with the given value
     * @since 1.0.0
     */
    public static <T> Configurer<T> configure(T value) {
        return new Configurer<>(value);
    }

    /**
     * Creates a new unnamed {@link Configurer} with a value eagerly resolved from the given
     * {@link Supplier}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Configurer.configure(() -> System.getenv("LOG_LEVEL"))
     *             .on(level -> level != null && !level.isEmpty())
     *             .apply(level -> loggerConfig.setLevel(level));
     * }</pre>
     *
     * @param valueSupplier a supplier providing the initial configuration value
     * @param <T>           the type of the value to be configured
     * @return a new unnamed {@link Configurer} instance with the supplied value
     * @since 1.0.0
     */
    public static <T> Configurer<T> configure(Supplier<T> valueSupplier) {
        return new Configurer<>(valueSupplier);
    }
}
