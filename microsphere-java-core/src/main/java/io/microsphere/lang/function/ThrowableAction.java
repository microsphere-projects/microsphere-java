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
import java.util.function.Function;

import static io.microsphere.util.Assert.assertNotNull;
import static java.util.Objects.requireNonNull;

/**
 * A function interface for action with {@link Throwable}
 *
 * @see Function
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
     * Executes {@link ThrowableAction} with {@link #handleException(Throwable) the default exception handling}
     *
     * @param action {@link ThrowableAction}
     * @throws NullPointerException if <code>action</code> is <code>null</code>
     */
    static void execute(ThrowableAction action) {
        execute(action, action::handleException);
    }

    /**
     * Executes {@link ThrowableAction} with the customized {@link Throwable exception} handling
     *
     * @param action           {@link ThrowableAction}
     * @param exceptionHandler the handler to handle any {@link Throwable exception} that the {@link #execute()} method throws
     * @throws NullPointerException if <code>action</code> or <code>exceptionHandler</code> is <code>null</code>
     */
    static void execute(ThrowableAction action, Consumer<Throwable> exceptionHandler) throws NullPointerException {
        assertNotNull(action, () -> "The 'action' must not be null");
        action.execute(exceptionHandler);
    }
}
