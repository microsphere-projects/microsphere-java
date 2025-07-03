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

import java.util.function.Consumer;

import static io.microsphere.util.Assert.assertNotNull;

/**
 * A functional interface for actions that may throw a {@link Throwable}.
 * <p>
 * This interface is similar to {@link Runnable}, but allows the action to throw any exception.
 * It can be used as a base for more specific interfaces that require exception handling.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Simple usage with a lambda expression
 * ThrowableAction action = () -> {
 *     // some code that may throw an exception
 * };
 *
 * // Execute the action with default exception handling
 * action.execute();
 *
 * // Execute the action with custom exception handling
 * action.execute(ex -> System.err.println("An error occurred: " + ex.getMessage()));
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Runnable
 * @see Throwable
 * @since 1.0.0
 */
@FunctionalInterface
public interface ThrowableAction {

    /**
     * Executes the action
     *
     * @throws Throwable if met with error
     */
    void execute() throws Throwable;

    /**
     * Executes {@link #execute()} with the customized {@link Throwable exception} handling
     *
     * @param exceptionHandler the handler to handle any {@link Throwable exception} that the {@link #execute()} method throws
     * @throws NullPointerException if <code>exceptionHandler</code> is <code>null</code>
     */
    default void execute(Consumer<Throwable> exceptionHandler) {
        assertNotNull(exceptionHandler, () -> "The 'exceptionHandler' must not be null");
        try {
            execute();
        } catch (Throwable e) {
            exceptionHandler.accept(e);
        }
    }

    /**
     * Handle any exception that the {@link #execute()} method throws
     *
     * @param failure the instance of {@link Throwable}
     */
    default void handleException(Throwable failure) {
        throw new RuntimeException(failure);
    }

    /**
     * Executes the given {@link ThrowableAction} with default exception handling.
     * <p>
     * If the action throws an exception during execution, it will be passed to the
     * {@link #handleException(Throwable)} method for handling.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Execute an action with default exception handling
     * ThrowableAction action = () -> {
     *     if (someErrorCondition) {
     *         throw new IOException("Something went wrong");
     *     }
     * };
     *
     * ThrowableAction.execute(action); // Uses default exception handling
     * }</pre>
     *
     * @param action the {@link ThrowableAction} to execute
     * @throws NullPointerException if the provided action is {@code null}
     */
    static void execute(ThrowableAction action) {
        execute(action, action::handleException);
    }

    /**
     * Executes the given {@link ThrowableAction} with a custom exception handler.
     * <p>
     * If the action throws an exception during execution, it will be passed to the
     * provided {@link Consumer} exception handler for customized handling.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Execute an action with a custom exception handler
     * ThrowableAction action = () -> {
     *     if (someErrorCondition) {
     *         throw new IOException("Something went wrong");
     *     }
     * };
     *
     * ThrowableAction.execute(action, ex -> System.err.println("Error: " + ex.getMessage()));
     * }</pre>
     *
     * @param action the {@link ThrowableAction} to execute
     * @param exceptionHandler the handler to manage any exceptions thrown by the action
     * @throws NullPointerException if the provided action is {@code null}
     */
    static void execute(ThrowableAction action, Consumer<Throwable> exceptionHandler) throws NullPointerException {
        assertNotNull(action, () -> "The 'action' must not be null");
        action.execute(exceptionHandler);
    }
}
