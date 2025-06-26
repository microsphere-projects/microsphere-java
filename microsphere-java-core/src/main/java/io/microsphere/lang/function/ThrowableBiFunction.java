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

import java.util.function.BiFunction;

import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.Assert.assertNotNull;

/**
 * Represents a function that accepts two arguments and produces a result, which may throw a {@link Throwable}.
 *
 * <p>This is a functional interface whose functional method is {@link #apply(Object, Object)}.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * ThrowableBiFunction<Integer, Integer, Integer> divide = (a, b) -> {
 *     if (b == 0) throw new ArithmeticException("Division by zero");
 *     return a / b;
 * };
 *
 * // Using execute with default exception handling
 * Integer result = ThrowableBiFunction.execute(10, 0, divide);
 * // This will internally handle the exception using DEFAULT_EXCEPTION_HANDLER and throw a RuntimeException
 *
 * // Custom Exception Handling Example:
 * Integer resultWithCustomHandler = ThrowableBiFunction.execute(10, 0, divide, (first, second, error) -> {
 *     System.out.println("Error occurred: " + error.getMessage());
 *     return 0; // Default value on failure
 * });
 * }</pre>
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <R> the type of the result of the function
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BiFunction
 * @see Throwable
 */
@FunctionalInterface
public interface ThrowableBiFunction<T, U, R> {

    ExceptionHandler DEFAULT_EXCEPTION_HANDLER = (first, second, failure) -> {
        String errorMessage = format("It's failed to execute the function with arguments[{}, {}] is failed", first, second);
        throw new RuntimeException(errorMessage, failure);
    };

    /**
     * Applies this function to the given argument.
     *
     * @param first  the first argument to be applied for the function
     * @param second the second argument to be applied for the function
     * @return the function result
     * @throws Throwable if met with any error
     */
    R apply(T first, U second) throws Throwable;

    /**
     * Executes {@link ThrowableBiFunction} with {@link #DEFAULT_EXCEPTION_HANDLER the default exception handling}
     *
     * @param first    the first argument to be applied for the function
     * @param second   the second argument to be applied for the function
     * @param function {@link ThrowableBiFunction}
     * @param <T>      the first argument type
     * @param <U>      the second argument type
     * @param <R>      the return type
     * @return the result after execution
     * @throws NullPointerException if <code>function</code> is <code>null</code>
     */
    static <T, U, R> R execute(T first, U second, ThrowableBiFunction<T, U, R> function) throws NullPointerException {
        return execute(first, second, function, (ExceptionHandler<T, U, R>) DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Executes {@link ThrowableBiFunction} with the customized exception handling
     *
     * @param first            the first argument to be applied for the function
     * @param second           the second argument to be applied for the function
     * @param exceptionHandler the handler to handle the function argument and
     *                         the exception that the {@link #apply(T, U)} method throws
     * @param function         {@link ThrowableBiFunction}
     * @param <T>              the first argument type
     * @param <U>              the second argument type
     * @param <R>              the return type
     * @return the result after execution
     * @throws NullPointerException if <code>function</code> and <code>exceptionHandler</code> is <code>null</code>
     */
    static <T, U, R> R execute(T first, U second, ThrowableBiFunction<T, U, R> function, ExceptionHandler<T, U, R> exceptionHandler) throws NullPointerException {
        assertNotNull(function, () -> "The 'function' must not be null");
        assertNotNull(exceptionHandler, () -> "The 'exceptionHandler' must not be null");
        R result;
        try {
            result = function.apply(first, second);
        } catch (Throwable failure) {
            result = exceptionHandler.handle(first, second, failure);
        }
        return result;
    }

    /**
     * The handler interface for {@link Throwable exception}
     *
     * @param <T> the first argument type
     * @param <U> the second argument type
     * @param <R> the return type
     */
    interface ExceptionHandler<T, U, R> {

        /**
         * Handle the exception with the function arguments
         *
         * @param first   the first argument to be applied for the function
         * @param second  the second argument to be applied for the function
         * @param failure
         * @return
         */
        R handle(T first, U second, Throwable failure);

    }
}
