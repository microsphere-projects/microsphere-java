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
package io.microsphere.concurrent;

import io.microsphere.util.ShutdownHookUtils;
import io.microsphere.util.Utils;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.microsphere.util.ArrayUtils.forEach;
import static io.microsphere.util.ShutdownHookUtils.addShutdownHookCallback;

/**
 * {@link Executor} Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Executor
 * @see ExecutorService
 * @see Executors
 * @since 1.0.0
 */
public abstract class ExecutorUtils implements Utils {

    /**
     * Registers a shutdown hook to gracefully shut down the given {@link Executor} instances when the JVM exits.
     * 
     * <p>
     * This method adds a JVM shutdown hook using {@link ShutdownHookUtils#addShutdownHookCallback(Runnable)},
     * ensuring that all provided executors are shut down properly upon application exit.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ExecutorService executor1 = Executors.newFixedThreadPool(2);
     * ExecutorService executor2 = Executors.newSingleThreadExecutor();
     *
     * ExecutorUtils.shutdownOnExit(executor1, executor2);
     * }</pre>
     *
     * @param one    the first {@link Executor} to shut down on JVM exit; must not be {@code null}
     * @param others additional {@link Executor} instances to shut down; may be empty or {@code null}
     */
    public static void shutdownOnExit(Executor one, Executor... others) {
        addShutdownHookCallback(() -> {
            shutdown(one);
            forEach(others, other -> shutdown(other));
        });
    }

    /**
     * Attempts to shut down the given {@link Executor} if it is an instance of {@link ExecutorService}.
     *
     * <p>
     * If the provided {@link Executor} is an instance of {@link ExecutorService}, this method will
     * delegate the shutdown process to the {@link #shutdown(ExecutorService)} method.
     * Otherwise, no action is taken and the method returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ExecutorService executor = Executors.newFixedThreadPool(2);
     * boolean isShutdown = ExecutorUtils.shutdown(executor);
     * System.out.println("Executor shutdown: " + isShutdown); // Output: true
     * }</pre>
     *
     * <pre>{@code
     * Executor nonServiceExecutor = (runnable) -> new Thread(runnable).start();
     * boolean isShutdown = ExecutorUtils.shutdown(nonServiceExecutor);
     * System.out.println("Executor shutdown: " + isShutdown); // Output: false
     * }</pre>
     *
     * @param executor the {@link Executor} instance to check and potentially shut down; may be {@code null}
     * @return <code>true</code> if the executor was an {@link ExecutorService} and has been successfully shut down;
     *         <code>false</code> otherwise
     */
    public static boolean shutdown(Executor executor) {
        if (executor instanceof ExecutorService) {
            return shutdown((ExecutorService) executor);
        }
        return false;
    }

    /**
     * Attempts to shut down the given {@link ExecutorService} gracefully.
     *
     * <p>
     * This method checks if the provided {@link ExecutorService} is not already shutdown.
     * If it is still active, this method initiates an orderly shutdown by calling
     * {@link ExecutorService#shutdown()}. If the executor is already shutdown or
     * null, no action is taken and the method returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ExecutorService executor = Executors.newFixedThreadPool(2);
     * boolean isShutdown = ExecutorUtils.shutdown(executor);
     * System.out.println("Executor shutdown: " + isShutdown); // Output: true
     * }</pre>
     *
     * <pre>{@code
     * ExecutorService alreadyShutdownExecutor = Executors.newSingleThreadExecutor();
     * alreadyShutdownExecutor.shutdown(); // manually shutting down
     * boolean result = ExecutorUtils.shutdown(alreadyShutdownExecutor);
     * System.out.println("Executor shutdown: " + result); // Output: false
     * }</pre>
     *
     * @param executorService the {@link ExecutorService} instance to shut down; may be {@code null}
     * @return <code>true</code> if the executor was actively running and has been successfully shut down;
     *         <code>false</code> if it was already shutdown or null
     */
    public static boolean shutdown(ExecutorService executorService) {
        if (executorService == null) {
            return false;
        }
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
        return true;
    }

    private ExecutorUtils() {
    }
}
