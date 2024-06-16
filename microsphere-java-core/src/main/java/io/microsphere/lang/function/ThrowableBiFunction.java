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
import java.util.function.Function;

import static io.microsphere.text.FormatUtils.format;
import static java.util.Objects.requireNonNull;

/**
 * {@link BiFunction} with {@link Throwable}
 *
 * @param <T> the type of the first argument to be applied for the function
 * @param <U> the type of the second argument to be applied for the function
 * @param <R> the type of the result of the function
 * @see Function
 * @see Throwable
 * @since 1.0.0
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
        requireNonNull(function, "The function must not be null");
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
        requireNonNull(function, "The function must not be null");
        requireNonNull(exceptionHandler, "The exceptionHandler must not be null");
        R result = null;
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
