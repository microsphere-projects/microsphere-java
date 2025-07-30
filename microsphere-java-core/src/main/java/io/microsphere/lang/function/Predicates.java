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
package io.microsphere.lang.function;

import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;

import java.util.function.Predicate;

import static io.microsphere.util.ArrayUtils.length;

/**
 * The utilities class for Java {@link Predicate}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface Predicates {

    /**
     * An empty array of {@link Predicate} instances.
     *
     * <p>This constant is useful when you need to return or pass a zero-length array of predicates,
     * ensuring type safety and avoiding unnecessary allocations.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Predicate<String>[] emptyPredicates = Predicates.EMPTY_PREDICATE_ARRAY;
     * }</pre>
     */
    @Immutable
    Predicate[] EMPTY_PREDICATE_ARRAY = new Predicate[0];

    /**
     * Returns an empty array of {@link Predicate} instances.
     * <p>
     * This method is useful when you need to return or pass a zero-length array of predicates,
     * ensuring type safety and avoiding unnecessary allocations.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Predicate<String>[] emptyPredicates = Predicates.emptyArray();
     * }</pre>
     *
     * @param <T> the type of the input object to be tested by the predicates
     * @return a shared, empty array of {@link Predicate} instances
     */
    @Nonnull
    @Immutable
    static <T> Predicate<T>[] emptyArray() {
        return (Predicate<T>[]) EMPTY_PREDICATE_ARRAY;
    }

    /**
     * Returns a {@link Predicate} that always evaluates to <code>true</code>.
     *
     * <p>
     * This method is useful when you need a no-op predicate that accepts all inputs.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Predicate<String> predicate = Predicates.alwaysTrue();
     * boolean result = predicate.test("anyValue"); // result will always be true
     * }</pre>
     *
     * @param <T> the type of the input object to be tested (ignored in the implementation)
     * @return a {@link Predicate} that always returns <code>true</code>
     */
    @Nonnull
    @Immutable
    static <T> Predicate<T> alwaysTrue() {
        return e -> true;
    }

    /**
     * Returns a {@link Predicate} that always evaluates to <code>false</code>.
     *
     * <p>
     * This method is useful when you need a predicate that rejects all inputs.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Predicate<String> predicate = Predicates.alwaysFalse();
     * boolean result = predicate.test("anyValue"); // result will always be false
     * }</pre>
     *
     * @param <T> the type of the input object to be tested (ignored in the implementation)
     * @return a {@link Predicate} that always returns <code>false</code>
     */
    @Nonnull
    @Immutable
    static <T> Predicate<T> alwaysFalse() {
        return e -> false;
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical AND of the given predicates.
     *
     * <p>
     * This method applies the logical AND operation to the provided array of predicates. The resulting predicate will test its input against all the given predicates,
     * returning <code>true</code> only if all predicates return <code>true</code>. It uses short-circuit evaluation, meaning it stops testing as soon as one predicate returns <code>false</code>.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Predicate<Integer> isPositive = i -> i > 0;
     * Predicate<Integer> isEven = i -> i % 2 == 0;
     *
     * Predicate<Integer> isPositiveAndEven = Predicates.and(isPositive, isEven);
     *
     * System.out.println(isPositiveAndEven.test(4));  // Output: true
     * System.out.println(isPositiveAndEven.test(-2)); // Output: false
     * System.out.println(isPositiveAndEven.test(3));  // Output: false
     * }</pre>
     *
     * @param predicates an array of predicates to be logically ANDed together
     * @param <T>        the type of the input object to be tested
     * @return a composed predicate that evaluates to <code>true</code> only if all given predicates evaluate to <code>true</code>
     */
    @Nonnull
    static <T> Predicate<? super T> and(Predicate<? super T>... predicates) {
        int length = length(predicates);
        if (length == 0) {
            return alwaysTrue();
        } else if (length == 1) {
            return predicates[0];
        } else {
            Predicate<T> andPredicate = alwaysTrue();
            for (Predicate<? super T> p : predicates) {
                andPredicate = andPredicate.and(p);
            }
            return andPredicate;
        }
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical OR of the given predicates.
     *
     * <p>
     * This method applies the logical OR operation to the provided array of predicates. The resulting predicate will test its input against each predicate in sequence,
     * returning <code>true</code> if any predicate returns <code>true</code>. It uses short-circuit evaluation, meaning it stops testing as soon as one predicate returns <code>true</code>.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Predicate<Integer> isPositive = i -> i > 0;
     * Predicate<Integer> isEven = i -> i % 2 == 0;
     *
     * Predicate<Integer> isPositiveOrEven = Predicates.or(isPositive, isEven);
     *
     * System.out.println(isPositiveOrEven.test(4));  // Output: true
     * System.out.println(isPositiveOrEven.test(-2)); // Output: true
     * System.out.println(isPositiveOrEven.test(3));  // Output: true
     * System.out.println(isPositiveOrEven.test(-3)); // Output: false
     * }</pre>
     *
     * @param predicates an array of predicates to be logically ORed together
     * @param <T>        the type of the input object to be tested
     * @return a composed predicate that evaluates to <code>true</code> if any of the given predicates evaluate to <code>true</code>
     */
    @Nonnull
    static <T> Predicate<? super T> or(Predicate<? super T>... predicates) {
        int length = length(predicates);
        if (length == 0) {
            return alwaysTrue();
        } else if (length == 1) {
            return predicates[0];
        } else {
            Predicate<T> orPredicate = alwaysFalse();
            for (Predicate<? super T> p : predicates) {
                orPredicate = orPredicate.or(p);
            }
            return orPredicate;
        }
    }

}
