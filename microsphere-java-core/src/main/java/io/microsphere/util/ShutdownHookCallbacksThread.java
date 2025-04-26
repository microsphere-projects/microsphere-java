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

/**
 * The Thread for executing the {@link Runnable} callbacks
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

    ShutdownHookCallbacksThread() {
        setName(getClass().getSimpleName());
    }

    @Override
    public void run() {
        executeShutdownHookCallbacks();
        ShutdownHookUtils.clearShutdownHookCallbacks();
    }

    private void executeShutdownHookCallbacks() {
        for (Runnable callback : ShutdownHookUtils.shutdownHookCallbacks) {
            if (logger.isTraceEnabled()) {
                logger.trace("The ShutdownHook Callback is about to run : {}", callback);
            }
            callback.run();
        }
    }

}
