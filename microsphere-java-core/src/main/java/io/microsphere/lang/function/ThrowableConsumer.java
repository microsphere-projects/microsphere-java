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
import java.util.function.Consumer;
import java.util.function.Function;

import static io.microsphere.util.Assert.assertNotNull;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result, which may throw a {@link Throwable}.
 *
 * <p>This is the two-arity specialization of {@link Function} and the
 * throwable-aware functional interface whose functional method is
 * {@link #accept(Object)}.
 *
 * <h2>Examples</h2>
 *
 * <pre>{@code
 * // Basic usage:
 * ThrowableConsumer<String> printer = System.out::println;
 *
 * printer.accept("Hello World"); // Outputs: Hello World
 *
 * // Throwing an exception:
 * ThrowableConsumer<Integer> riskyConsumer = i -> {
 *     if (i < 0) {
 *         throw new IllegalArgumentException("Negative value not allowed");
 *     }
 * };
 *
 * try {
 *     riskyConsumer.accept(-1);
 * } catch (Throwable t) {
 *     System.err.println("Caught exception: " + t.getMessage());
 * }
 * }</pre>
 *
 * @param <T> the type of the input to the operation
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Consumer
 * @see Throwable
 */
@FunctionalInterface
public interface ThrowableConsumer<T> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @throws Throwable if met with any error
     */
    void accept(T t) throws Throwable;

    /**
     * Executes {@link #accept(T)} with {@link #handleException(Object, Throwable) the default exception handling}
     *
     * @param t the function argument
     * @see #accept(T)
     */
    default void execute(T t) {
        execute(t, this::handleException);
    }

    /**
     * Executes {@link #accept(T)} with the customized exception handling
     *
     * @param t                the function argument
     * @param exceptionHandler the handler to handle the function argument and
     *                         the exception that the {@link #accept(T)} method throws
     * @throws NullPointerException if <code>exceptionHandler</code> is <code>null</code>
     */
    default void execute(T t, BiConsumer<T, Throwable> exceptionHandler) throws NullPointerException {
        assertNotNull(exceptionHandler, () -> "The 'exceptionHandler' must not be null");
        try {
            accept(t);
        } catch (Throwable e) {
            exceptionHandler.accept(t, e);
        }
    }

    /**
     * Handle any exception that the {@link #accept(T)} method throws
     *
     * @param t       the value to be consumed
     * @param failure the instance of {@link Throwable}
     */
    default void handleException(T t, Throwable failure) {
        throw new RuntimeException(failure);
    }

    /**
     * Executes {@link ThrowableConsumer} with {@link #handleException(Object, Throwable) the default exception handling}
     *
     * @param t        the function argument
     * @param consumer {@link ThrowableConsumer}
     * @param <T>      the source type
     * @return the result after execution
     * @throws NullPointerException if <code>consumer</code> is <code>null</code>
     */
    /**
     * Executes the given {@link ThrowableConsumer} with the default exception handling.
     *
     * <p>If an exception is thrown during execution, it will be handled by the
     * {@link #handleException(Object, Throwable)} method of the consumer.
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Example 1: Basic usage without any exception
     * ThrowableConsumer<String> printConsumer = System.out::println;
     *
     * ThrowableConsumer.execute("Hello World", printConsumer);
     * // Output: Hello World
     *
     * // Example 2: Consumer that throws an exception
     * ThrowableConsumer<Integer> riskyConsumer = i -> {
     *     if (i < 0) {
     *         throw new IllegalArgumentException("Negative value not allowed");
     *     }
     * };
     *
     * try {
     *     ThrowableConsumer.execute(-1, riskyConsumer);
     * } catch (RuntimeException e) {
     *     System.err.println("Caught exception: " + e.getCause().getMessage());
     *     // Output: Caught exception: Negative value not allowed
     * }
     * }</pre>
     *
     * @param t        the input argument to be consumed
     * @param consumer the instance of {@link ThrowableConsumer} to execute
     * @param <T>      the type of the input argument
     * @throws NullPointerException if the given consumer is null
     */
    static <T> void execute(T t, ThrowableConsumer<T> consumer) throws NullPointerException {
        consumer.execute(t, consumer::handleException);
    }

    /**
     * Executes the given {@link ThrowableConsumer} with a custom exception handler.
     *
     * <p>If an exception is thrown during execution, it will be passed to the provided
     * {@code exceptionHandler} along with the input value that caused the exception.
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Example 1: Basic usage without any exception
     * ThrowableConsumer<String> printConsumer = System.out::println;
     *
     * ThrowableConsumer.execute("Hello World", printConsumer, (t, e) -> {});
     * // Output: Hello World
     *
     * // Example 2: Custom exception handling
     * ThrowableConsumer<Integer> riskyConsumer = i -> {
     *     if (i < 0) {
     *         throw new IllegalArgumentException("Negative value not allowed");
     *     }
     * };
     *
     * BiConsumer<Integer, Throwable> handler = (value, ex) -> {
     *     System.err.println("Error at value " + value + ": " + ex.getMessage());
     * };
     *
     * ThrowableConsumer.execute(-1, riskyConsumer, handler);
     * // Output: Error at value -1: Negative value not allowed
     * }</pre>
     *
     * @param t                the input argument to be consumed
     * @param consumer         the instance of {@link ThrowableConsumer} to execute
     * @param exceptionHandler the handler to manage exceptions thrown during execution
     * @param <T>              the type of the input argument
     * @throws NullPointerException if the given {@code consumer} or {@code exceptionHandler} is null
     */
    static <T> void execute(T t, ThrowableConsumer<T> consumer, BiConsumer<T, Throwable> exceptionHandler) throws NullPointerException {
        assertNotNull(consumer, "The 'consumer' must not be null");
        assertNotNull(exceptionHandler, () -> "The 'exceptionHandler' must not be null");
        try {
            consumer.accept(t);
        } catch (Throwable e) {
            exceptionHandler.accept(t, e);
        }
    }
}