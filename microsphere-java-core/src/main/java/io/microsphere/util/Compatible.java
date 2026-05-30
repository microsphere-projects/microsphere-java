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
import io.microsphere.util.Version.Operator;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.microsphere.util.Version.getVersion;
import static io.microsphere.util.Version.ofVersion;
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
 * @see Operator
 */
public class Compatible<T, R> {

    private final Version version;

    private final Function<Version, R> conditionalFunction;

    /**
     * Constructs a {@link Compatible} instance with the specified version and conditional function.
     *
     * <p>This constructor initializes a {@link Compatible} object with a given version and a function
     * that defines the logic to execute when a version condition is satisfied. The conditional function
     * is applied only if the version meets the criteria defined in subsequent {@link #on(String, String, Function)}
     * or {@link #on(Operator, Version, Function)} calls.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Version currentVersion = Version.of("1.2.3");
     * Compatible<Void, String> compatible = new Compatible<>(currentVersion, v -> "Default Result");
     *
     * // Execute logic only if current version is greater than "1.2.0"
     * Optional<String> result = compatible.on(">", "1.2.0", v -> "Condition Matched").call();
     * System.out.println(result.orElse("No Match")); // Output: Condition Matched
     * }</pre>
     *
     * @param version             the version to be used for comparison
     * @param conditionalFunction the function to execute if a condition is satisfied, or {@code null} if no default action is needed
     */
    public Compatible(Version version, Function<Version, R> conditionalFunction) {
        this.version = version;
        this.conditionalFunction = conditionalFunction;
    }

    /**
     * Creates a {@link Compatible} instance for the specified class, automatically detecting its version.
     *
     * <p>This factory method retrieves the version associated with the given class (typically from its JAR manifest)
     * and initializes a {@link Compatible} instance with no initial conditional function. This is useful for
     * performing version-based compatibility checks on classes without manually specifying their versions.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Automatically fetches version from the manifest of the JAR containing MyComponent.class
     * Compatible<MyComponent, Boolean> compatibilityCheck = Compatible.of(MyComponent.class);
     *
     * boolean isSupported = compatibilityCheck
     *     .on(">=", "1.5.0", v -> true)
     *     .on("<", "1.5.0", v -> false)
     *     .get() != null;
     *
     * System.out.println("Feature supported: " + isSupported); // Output: Feature supported: true (if version >= 1.5.0)
     * }</pre>
     *
     * @param targetClass the class for which to create a {@link Compatible} instance
     * @param <T>         the type of the target class
     * @return a new {@link Compatible} instance initialized with the detected version of the target class
     * @see Version#getVersion(Class)
     */
    public static <T> Compatible<T, ?> of(Class<T> targetClass) {
        return new Compatible<>(getVersion(targetClass), null);
    }

    /**
     * Adds a conditional function to be executed if the current version satisfies the specified operator and compared version.
     *
     * <p>This method allows chaining multiple conditions. If the current version meets the condition defined by the
     * operator and compared version, the provided function will be executed. Otherwise, the chain continues to the next condition.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Version currentVersion = Version.of("1.2.3");
     * Compatible<Void, String> compatible = new Compatible<>(currentVersion, v -> "Default Result");
     *
     * // Execute logic only if current version is greater than "1.2.0"
     * Optional<String> result = compatible.on(">", "1.2.0", v -> "Condition Matched").call();
     * System.out.println(result.orElse("No Match")); // Output: Condition Matched
     * }</pre>
     *
     * @param operator         the operator to compare versions (e.g., ">", "<", "==", ">=", "<=")
     * @param comparedVersion  the version to compare against, as a string
     * @param conditionalFunction the function to execute if the condition is met
     * @param <R>              the return type of the conditional function
     * @return a new {@link Compatible} instance with the added condition, or the same instance if the condition is not met
     */
    public <R> Compatible<T, R> on(String operator, String comparedVersion,
                                   Function<Version, R> conditionalFunction) {
        return on(Operator.of(operator), ofVersion(comparedVersion), conditionalFunction);
    }

    /**
     * Adds a conditional function to be executed if the current version satisfies the specified operator and compared version.
     *
     * <p>This method allows chaining multiple conditions. If the current version meets the condition defined by the
     * operator and compared version, the provided function will be executed. Otherwise, the chain continues to the next condition.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Version currentVersion = Version.of("1.2.3");
     * Compatible<Void, String> compatible = new Compatible<>(currentVersion, v -> "Default Result");
     *
     * // Execute logic only if current version is greater than "1.2.0"
     * Optional<String> result = compatible.on(Operator.GT, Version.of("1.2.0"), v -> "Condition Matched").call();
     * System.out.println(result.orElse("No Match")); // Output: Condition Matched
     * }</pre>
     *
     * @param operator         the operator to compare versions (e.g., Operator.GT, Operator.LT, Operator.EQ, Operator.GE, Operator.LE)
     * @param comparedVersion  the version to compare against
     * @param conditionalFunction the function to execute if the condition is met
     * @param <R>              the return type of the conditional function
     * @return a new {@link Compatible} instance with the added condition, or the same instance if the condition is not met
     */
    public <R> Compatible<T, R> on(Operator operator, Version comparedVersion,
                                   Function<Version, R> conditionalFunction) {
        if (TRUE.equals(operator.test(this.version, comparedVersion))) {
            return new Compatible<>(version, conditionalFunction);
        }
        return (Compatible<T, R>) this;
    }

    /**
     * Executes the conditional function if a matching condition was found and returns the result wrapped in an {@link Optional}.
     * If no condition matches or no function is defined, returns an empty {@link Optional}.
     *
     * <p>This method is typically called after chaining one or more {@link #on(String, String, Function)} conditions.
     * It evaluates the provided function (if any) against the current version and returns the computed result.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Version currentVersion = Version.of("1.2.3");
     * Compatible<Void, String> compatible = new Compatible<>(currentVersion, v -> "Default Result");
     *
     * // Execute logic only if current version is greater than "1.2.0"
     * Optional<String> result = compatible.on(">", "1.2.0", v -> "Condition Matched").call();
     * System.out.println(result.orElse("No Match")); // Output: Condition Matched
     * }</pre>
     *
     * @return an {@link Optional} containing the result of the conditional function, or empty if no condition matched
     */
    public Optional<R> call() {
        R result = null;
        if (conditionalFunction != null) {
            result = conditionalFunction.apply(version);
        }
        return ofNullable(result);
    }

    /**
     * Accepts a consumer to process the result of the conditional function if a matching condition was found.
     * This method is useful for side-effect operations such as logging or updating state based on the result.
     *
     * <p>If a condition matches and the conditional function produces a result, the provided {@link Consumer}
     * will be invoked with that result. If no condition matches or no function is defined, the consumer is not called.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Version currentVersion = Version.of("1.2.3");
     * Compatible<Void, String> compatible = new Compatible<>(currentVersion, v -> "Default Result");
     *
     * // Process the result if the condition matches
     * compatible.on(">", "1.2.0", v -> "Condition Matched")
     *           .accept(result -> System.out.println("Result: " + result)); // Output: Result: Condition Matched
     * }</pre>
     *
     * @param resultConsumer the consumer to accept the result of the conditional function
     */
    public void accept(Consumer<R> resultConsumer) {
        call().ifPresent(resultConsumer);
    }

    /**
     * Retrieves the result of the conditional function if a matching condition was found.
     * If no condition matches or no function is defined, returns {@code null}.
     *
     * <p>This method is a convenience wrapper around {@link #call()} that directly returns
     * the result instead of wrapping it in an {@link Optional}. It is useful when you are
     * certain that a condition will match and want to avoid handling {@link Optional}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Version currentVersion = Version.of("1.2.3");
     * Compatible<Void, String> compatible = new Compatible<>(currentVersion, v -> "Default Result");
     *
     * // Get the result directly if the condition matches
     * String result = compatible.on(">", "1.2.0", v -> "Condition Matched").get();
     * System.out.println(result); // Output: Condition Matched
     * }</pre>
     *
     * @return the result of the conditional function, or {@code null} if no condition matched
     */
    @Nullable
    public R get() {
        return call().orElse(null);
    }
}