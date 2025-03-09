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
 * {@link Function} with {@link Throwable}
 *
 * @param <T> the source type
 * @param <R> the return type
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
     * Executes {@link ThrowableFunction} with {@link #handleException(T, Throwable) the default exception handling}
     *
     * @param t        the throwable-function argument
     * @param function {@link ThrowableFunction}
     * @param <T>      the source type
     * @param <R>      the return type
     * @return the result after execution
     * @throws NullPointerException if <code>function</code> is <code>null</code>
     */
    static <T, R> R execute(T t, ThrowableFunction<T, R> function) throws NullPointerException {
        assertNotNull(function, () -> "The 'function' must not be null");
        return function.execute(t);
    }

    /**
     * Executes {@link ThrowableFunction} with the customized exception handling
     *
     * @param t                the function argument
     * @param exceptionHandler the handler to handle the function argument and
     *                         the exception that the {@link #apply(T)} method throws
     * @param function         {@link ThrowableFunction}
     * @param <T>              the source type
     * @param <R>              the return type
     * @return the result after execution
     * @throws NullPointerException if <code>function</code> and <code>exceptionHandler</code> is <code>null</code>
     */
    static <T, R> R execute(T t, ThrowableFunction<T, R> function, BiFunction<T, Throwable, R> exceptionHandler)
            throws NullPointerException {
        assertNotNull(function, () -> "The 'function' must not be null");
        assertNotNull(exceptionHandler, "The 'exceptionHandler' must not be null");
        return function.execute(t, exceptionHandler);
    }
}
