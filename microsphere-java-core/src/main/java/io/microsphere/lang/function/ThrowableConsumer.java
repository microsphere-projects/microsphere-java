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

import static java.util.Objects.requireNonNull;

/**
 * {@link Consumer} with {@link Throwable}
 *
 * @param <T> the source type
 * @see Function
 * @see Throwable
 * @since 1.0.0
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
        requireNonNull(exceptionHandler, "The exceptionHandler must not be null");
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
    static <T> void execute(T t, ThrowableConsumer<T> consumer) throws NullPointerException {
        requireNonNull(consumer, "The consumer must not be null");
        consumer.execute(t);
    }

    /**
     * Executes {@link ThrowableConsumer} with the customized exception handling
     *
     * @param t                the function argument
     * @param consumer         {@link ThrowableConsumer}
     * @param exceptionHandler the handler to handle any {@link Throwable exception} that the {@link #accept(T)} ()} method throws
     * @param <T>              the source type
     * @return the result after execution
     * @throws NullPointerException if <code>consumer</code> and <code>exceptionHandler</code> is <code>null</code>
     */
    static <T> void execute(T t, ThrowableConsumer<T> consumer, BiConsumer<T, Throwable> exceptionHandler) throws NullPointerException {
        requireNonNull(consumer, "The consumer must not be null");
        consumer.execute(t, exceptionHandler);
    }
}
