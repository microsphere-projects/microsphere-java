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

import static io.microsphere.util.Assert.assertNotNull;

/**
 * Represents a function that accepts one argument and produces a result, which may throw a checked exception.
 * <p>
 * This interface is similar to {@link Function}, but allows the functional method to throw any {@link Throwable}.
 * It also provides default methods for composing functions and handling exceptions gracefully.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // <h3>Example Usage</h3>
 * ThrowableFunction<String, Integer> parser = Integer::valueOf;
 *
 * // Using execute() with default exception handling (throws RuntimeException)
 * Integer result1 = parser.execute("123"); // returns 123
 *
 * // Using execute() with custom exception handling
 * Integer result2 = parser.execute("invalid", (input, ex) -> {
 *     System.out.println("Parsing failed for: " + input);
 *     return -1; // fallback value
 * });
 * }</pre>
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Function
 * @see Throwable
 * @since 1.0.0
 */
@FunctionalInterface
public interface ThrowableFunction<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @throws Throwable if met with any error
     */
    R apply(T t) throws Throwable;

    /**
     * Executes {@link #apply(T)} with {@link #handleException(T, Throwable) the default exception handling}
     *
     * @param t the function argument
     * @return the function result
     */
    default R execute(T t) throws RuntimeException {
        return execute(t, this::handleException);
    }

    /**
     * Executes {@link #apply(T)} with the customized exception handling
     *
     * @param t                the function argument
     * @param exceptionHandler the handler to handle the function argument and
     *                         the exception that the {@link #apply(T)} method throws
     * @return the function result
     */
    default R execute(T t, BiFunction<T, Throwable, R> exceptionHandler) throws RuntimeException {
        R result;
        try {
            result = apply(t);
        } catch (Throwable e) {
            result = exceptionHandler.apply(t, e);
        }
        return result;
    }

    /**
     * Handle any exception that the {@link #apply(T)} method throws
     *
     * @param t       the value to be consumed
     * @param failure the instance of {@link Throwable}
     */
    default R handleException(T t, Throwable failure) {
        throw new RuntimeException(failure);
    }

    /**
     * Returns a composed throwable-function that first applies the {@code before}
     * throwable-function to its input, and then applies this throwable-function to the result.
     * If evaluation of either throwable-function throws an exception, it is relayed to
     * the caller of the composed throwable-function.
     *
     * @param <V>    the type of input to the {@code before} throwable-function, and to the
     *               composed throwable-function
     * @param before the throwable-function to apply before this throwable-function is applied
     * @return a composed throwable-function that first applies the {@code before}
     * throwable-function and then applies this throwable-function
     * @throws NullPointerException if before is null
     * @see #andThen(ThrowableFunction)
     */
    default <V> ThrowableFunction<V, R> compose(ThrowableFunction<? super V, ? extends T> before) {
        assertNotNull(before, () -> "The 'before' must not be null");
        return (V v) -> apply(before.apply(v));
    }

    /**
     * Returns a composed throwable-function that first applies this throwable-function to
     * its input, and then applies the {@code after} throwable-function to the result.
     * If evaluation of either throwable-function throws an exception, it is relayed to
     * the caller of the composed throwable-function.
     *
     * @param <V>   the type of output of the {@code after} throwable-function, and of the
     *              composed throwable-function
     * @param after the throwable-function to apply after this throwable-function is applied
     * @return a composed throwable-function that first applies this throwable-function and then
     * applies the {@code after} throwable-function
     * @throws NullPointerException if after is null
     * @see #compose(ThrowableFunction)
     */
    default <V> ThrowableFunction<T, V> andThen(ThrowableFunction<? super R, ? extends V> after) {
        assertNotNull(after, () -> "The 'after' must not be null");
        return (T t) -> after.apply(apply(t));
    }

    /**
     * Executes the given {@link ThrowableFunction} with the provided argument using the function's default exception handling.
     * <p>
     * If the execution of the function throws an exception, it will be handled by the function's own
     * {@link #handleException(Object, Throwable)} method.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ThrowableFunction<String, Integer> parser = Integer::valueOf;
     *
     * // Successful execution
     * Integer result1 = execute("123", parser); // returns 123
     *
     * // Execution that fails and uses the default exception handler (throws RuntimeException)
     * Integer result2 = execute("invalid", parser); // throws RuntimeException wrapping NumberFormatException
     * }</pre>
     *
     * @param t        the input argument to the function
     * @param function the {@link ThrowableFunction} to execute
     * @param <T>      the type of the input to the function
     * @param <R>      the type of the result of the function
     * @return the result of the function execution
     * @throws IllegalArgumentException if the provided function is null
     */
    static <T, R> R execute(T t, ThrowableFunction<T, R> function) throws IllegalArgumentException {
        return execute(t, function, function::handleException);
    }

    /**
     * Executes the given {@link ThrowableFunction} with the provided argument using a custom exception handler.
     * <p>
     * This method applies the provided function to the input argument. If the function throws an exception,
     * it is passed to the provided {@code exceptionHandler} for handling, allowing custom fallback behavior.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ThrowableFunction<String, Integer> parser = Integer::valueOf;
     *
     * // Successful execution
     * Integer result1 = execute("123", parser, (input, ex) -> -1); // returns 123
     *
     * // Execution that fails, using the custom exception handler
     * Integer result2 = execute("invalid", parser, (input, ex) -> -1); // returns -1
     * }</pre>
     *
     * @param t                the input argument to the function
     * @param function         the {@link ThrowableFunction} to execute
     * @param exceptionHandler the handler to use if the function throws an exception;
     *                         takes the input and the exception, and returns a fallback value
     * @param <T>              the type of the input to the function
     * @param <R>              the type of the result of the function
     * @return the result of the function execution
     * @throws IllegalArgumentException if the provided function or exception handler is null
     */
    static <T, R> R execute(T t, ThrowableFunction<T, R> function, BiFunction<T, Throwable, R> exceptionHandler)
            throws IllegalArgumentException {
        assertNotNull(function, () -> "The 'function' must not be null");
        assertNotNull(exceptionHandler, "The 'exceptionHandler' must not be null");
        return function.execute(t, exceptionHandler);
    }
}
