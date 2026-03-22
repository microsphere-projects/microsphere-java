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
package io.microsphere.util;

import io.microsphere.logging.Logger;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ShutdownHookUtils.clearShutdownHookCallbacks;
import static io.microsphere.util.ShutdownHookUtils.shutdownHookCallbacks;

/**
 * A {@link Thread} that executes registered shutdown hook {@link Runnable} callbacks
 * when the JVM begins its shutdown sequence.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // This thread is used internally by ShutdownHookUtils:
 *   // ShutdownHookUtils.addShutdownHookCallback(() -> System.out.println("Shutting down..."));
 *   // The ShutdownHookCallbacksThread will execute all registered callbacks on JVM shutdown.
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ShutdownHookUtils#registerShutdownHook()
 * @since 1.0.0
 */
class ShutdownHookCallbacksThread extends Thread {

    private static final Logger logger = getLogger(ShutdownHookCallbacksThread.class);

    /**
     * The singleton instance of {@link ShutdownHookCallbacksThread}
     */
    static final ShutdownHookCallbacksThread INSTANCE = new ShutdownHookCallbacksThread();

    /**
     * Constructs a new {@link ShutdownHookCallbacksThread} with a default thread name
     * derived from the class simple name.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Internally constructed as a singleton:
     *   // ShutdownHookCallbacksThread thread = new ShutdownHookCallbacksThread();
     *   // thread.getName(); // "ShutdownHookCallbacksThread"
     * }</pre>
     *
     * @since 1.0.0
     */
    ShutdownHookCallbacksThread() {
        setName(getClass().getSimpleName());
    }

    /**
     * Executes all registered shutdown hook callbacks and then clears the callback registry.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // This method is invoked automatically by the JVM during shutdown:
     *   // Runtime.getRuntime().addShutdownHook(ShutdownHookCallbacksThread.INSTANCE);
     *   // All registered callbacks in ShutdownHookUtils.shutdownHookCallbacks will be executed.
     * }</pre>
     *
     * @since 1.0.0
     */
    @Override
    public void run() {
        executeShutdownHookCallbacks();
        clearShutdownHookCallbacks();
    }

    private void executeShutdownHookCallbacks() {
        for (Runnable callback : shutdownHookCallbacks) {
            if (logger.isTraceEnabled()) {
                logger.trace("The ShutdownHook Callback is about to run : {}", callback);
            }
            callback.run();
        }
    }

}
