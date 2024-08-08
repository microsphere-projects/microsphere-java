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
 * {@link FunctionalInterface} Configurer (No Thread-Safe)
 *
 * @param <T> The type to be configured
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


    public Configurer<T> on(Predicate<T> predicate) {
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
