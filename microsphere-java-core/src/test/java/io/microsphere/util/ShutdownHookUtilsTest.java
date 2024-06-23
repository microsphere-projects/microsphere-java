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
import org.junit.jupiter.api.Test;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ShutdownHookUtils.addShutdownHookCallback;
import static io.microsphere.util.ShutdownHookUtils.getShutdownHookCallbacks;
import static io.microsphere.util.ShutdownHookUtils.getShutdownHookThreads;
import static io.microsphere.util.ShutdownHookUtils.removeShutdownHookCallback;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ShutdownHookUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ShutdownHookUtilsTest {

    private static final Logger logger = getLogger(ShutdownHookUtilsTest.class);

    @Test
    public void testGetShutdownHookThreads() {
        assertFalse(getShutdownHookThreads().isEmpty());
    }

    @Test
    public void testShutdownHookCallback() throws InterruptedException {

        final Runnable callback = new Runnable() {
            @Override
            public void run() {
                Thread currentThread = Thread.currentThread();
                synchronized (currentThread) {
                    try {
                        logger.info("Thread[name : '{}'] is about to be waited...", currentThread.getName());
                        currentThread.wait();
                        assertTrue(getShutdownHookCallbacks().isEmpty());
                        assertFalse(removeShutdownHookCallback(this));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        final Thread thread = new Thread(callback, "test-thread");

        addShutdownHookCallback(() -> {
            synchronized (thread) {
                logger.info("Thread[name : '{}'] is about to notify a waited Thread[name: '{}']...", Thread.currentThread().getName(), thread.getName());
                thread.notify();
            }
        });

        assertTrue(getShutdownHookCallbacks().size() > 0);

        thread.start();
    }
}
