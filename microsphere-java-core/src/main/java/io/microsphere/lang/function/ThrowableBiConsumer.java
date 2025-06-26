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

import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;


/**
 * Represents an operation that accepts two input arguments and returns no result,
 * potentially throwing a {@link Throwable}. This is the two-arity specialization of
 * {@link ThrowableConsumer}.
 *
 * <p>Additionally, this interface provides a default method ({@link #andThen}) to support
 * chaining operations, similar to functional constructs in the JDK.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * ThrowableBiConsumer<Integer, Integer> addAndPrint = (a, b) -> {
 *     int sum = a + b;
 *     System.out.println("Sum: " + sum);
 * };
 *
 * addAndPrint.accept(3, 5); // Output: Sum: 8
 * }</pre>
 *
 * <h2>Error Handling Example:</h2>
 * <pre>{@code
 * ThrowableBiConsumer<String, Integer> parseAndPrint = (str, radix) -> {
 *     int value = Integer.parseInt(str, radix);
 *     System.out.println("Parsed value: " + value);
 * };
 *
 * try {
 *     parseAndPrint.accept("123", 10); // Output: Parsed value: 123
 *     parseAndPrint.accept("abc", 16); // May throw NumberFormatException
 * } catch (Throwable t) {
 *     System.err.println("Error occurred: " + t.getMessage());
 * }
 * }</pre>
 *
 * @param <T> the type of the first argument to the operation
 * @param <U> the type of the second argument to the operation
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BiConsumer
 * @see Throwable
 * @since 1.0.0
 */
@FunctionalInterface
public interface ThrowableBiConsumer<T, U> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @throws Throwable if met with error
     */
    void accept(T t, U u) throws Throwable;

    /**
     * Returns a composed {@code ThrowableBiConsumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code ThrowableBiConsumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default ThrowableBiConsumer<T, U> andThen(ThrowableBiConsumer<? super T, ? super U> after) throws Throwable {
        requireNonNull(after);
        return (l, r) -> {
            accept(l, r);
            after.accept(l, r);
        };
    }
}
