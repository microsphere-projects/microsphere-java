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

import io.microsphere.annotation.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.Boolean.TRUE;
import static java.util.Optional.ofNullable;

/**
 * A utility class to conditionally execute logic based on version comparison.
 *
 * <p>{@link Compatible} allows defining conditional logic that is executed only if the current version meets a specified
 * condition relative to another version. It supports common version comparison operations such as equal to, greater than,
 * less than, and their inclusive counterparts.</p>
 *
 * <h3>Example Usage</h3>
 *
 * <h4>Example 1: Basic Usage</h4>
 * <pre>{@code
 * Version currentVersion = Version.of("1.2.3");
 * Compatible<Void, String> compatible = new Compatible<>(currentVersion, v -> "Matched Condition");
 *
 * // Execute logic only if current version is greater than "1.2.0"
 * Optional<String> result = compatible.on(">", "1.2.0", v -> "Executed").call();
 * System.out.println(result.orElse("Not Matched")); // Output: Executed
 * }</pre>
 *
 * <h4>Example 2: Chaining Conditions</h4>
 * <pre>{@code
 * Version currentVersion = Version.of("2.0.0");
 * Compatible<Void, String> base = new Compatible<>(currentVersion, null);
 *
 * // Apply multiple conditions in sequence
 * Optional<String> result = base
 *     .on("<", "1.9.0", v -> "Older")
 *     .on("==", "2.0.0", v -> "Exact Match")
 *     .on(">=", "2.0.0", v -> "At least 2.0.0")
 *     .call();
 *
 * System.out.println(result.orElse("No condition matched")); // Output: Exact Match
 * }</pre>
 *
 * <h4>Example 3: Using with Class-based Version</h4>
 * <pre>{@code
 * // Automatically fetches version from the manifest of the JAR containing MyComponent.class
 * Compatible<MyComponent, Boolean> compatibilityCheck = Compatible.of(MyComponent.class);
 *
 * boolean isSupported = compatibilityCheck
 *     .on(">=", "1.5.0", v -> true)
 *     .on("<", "1.5.0", v -> false)
 *     .get() != null;
 *
 * System.out.println("Feature supported: " + isSupported);
 * }</pre>
 *
 * @param <T> the type of context object associated with this compatibility check (not used directly in logic)
 * @param <R> the return type of the conditional function
 * @see Version
 * @see Version.Operator
 */
public class Compatible<T, R> {

    private final Version version;

    private final Function<Version, R> conditionalFunction;

    public Compatible(Version version, Function<Version, R> conditionalFunction) {
        this.version = version;
        this.conditionalFunction = conditionalFunction;
    }

    public static <T> Compatible<T, ?> of(Class<T> targetClass) {
        return new Compatible<>(Version.getVersion(targetClass), null);
    }

    public <R> Compatible<T, R> on(String operator, String comparedVersion,
                                   Function<Version, R> conditionalFunction) {
        return on(Version.Operator.of(operator), Version.of(comparedVersion), conditionalFunction);
    }

    public <R> Compatible<T, R> on(Version.Operator operator, Version comparedVersion,
                                   Function<Version, R> conditionalFunction) {
        if (TRUE.equals(operator.test(this.version, comparedVersion))) {
            return new Compatible<>(version, conditionalFunction);
        }
        return (Compatible<T, R>) this;
    }

    /**
     * @return
     */
    public Optional<R> call() {
        R result = null;
        if (conditionalFunction != null) {
            result = conditionalFunction.apply(version);
        }
        return ofNullable(result);
    }

    public void accept(Consumer<R> resultConsumer) {
        call().ifPresent(resultConsumer);
    }

    /**
     * @return
     */
    @Nullable
    public R get() {
        return call().orElse(null);
    }
}
