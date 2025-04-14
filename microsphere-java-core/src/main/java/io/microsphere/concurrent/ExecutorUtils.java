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
public abstract class ExecutorUtils {

    /**
     * Shutdown one or more {@link Executor executors} on JVM exit
     *
     * @param one    One {@link Executor}
     * @param others the other {@link Executor executors}
     */
    public static void shutdownOnExit(Executor one, Executor... others) {
        addShutdownHookCallback(() -> {
            shutdown(one);
            forEach(others, other -> shutdown(other));
        });
    }

    /**
     * Shutdown {@link Executor} if it's not shutdown
     *
     * @param executor {@link ExecutorService}
     */
    public static void shutdown(Executor executor) {
        if (executor instanceof ExecutorService) {
            shutdown((ExecutorService) executor);
        }
    }

    /**
     * Shutdown {@link ExecutorService} if it's not shutdown
     *
     * @param executorService {@link ExecutorService}
     */
    public static void shutdown(ExecutorService executorService) {
        if (executorService == null) {
            return;
        }
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

}
