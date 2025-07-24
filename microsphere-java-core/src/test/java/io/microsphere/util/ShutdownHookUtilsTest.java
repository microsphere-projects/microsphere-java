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

import io.microsphere.lang.Prioritized;
import io.microsphere.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Queue;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ShutdownHookUtils.SHUTDOWN_HOOK_CALLBACKS_THREAD_FILTER;
import static io.microsphere.util.ShutdownHookUtils.addShutdownHookCallback;
import static io.microsphere.util.ShutdownHookUtils.clearShutdownHookCallbacks;
import static io.microsphere.util.ShutdownHookUtils.filterShutdownHookThreads;
import static io.microsphere.util.ShutdownHookUtils.getShutdownHookCallbacks;
import static io.microsphere.util.ShutdownHookUtils.getShutdownHookThreads;
import static io.microsphere.util.ShutdownHookUtils.registerShutdownHook;
import static io.microsphere.util.ShutdownHookUtils.removeShutdownHookCallback;
import static io.microsphere.util.ShutdownHookUtils.shutdownHookCallbacks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ShutdownHookUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class ShutdownHookUtilsTest {

    private static final Logger logger = getLogger(ShutdownHookUtilsTest.class);

    @BeforeEach
    public void setUp() {
        registerShutdownHook();
    }

    @AfterEach
    public void destroy() {
        clearShutdownHookCallbacks();
    }

    @Test
    public void testRegisterShutdownHook() {
        testFilterShutdownHookThreadsWithRemoved();
        registerShutdownHook();
        assertFalse(filterShutdownHookThreads(SHUTDOWN_HOOK_CALLBACKS_THREAD_FILTER).isEmpty());
    }

    @Test
    public void testGetShutdownHookThreads() {
        assertFalse(getShutdownHookThreads().isEmpty());
    }

    @Test
    public void testFilterShutdownHookThreads() {
        assertTrue(filterShutdownHookThreads(t -> false).isEmpty());
    }

    @Test
    public void testFilterShutdownHookThreadsWithRemoved() {
        assertFalse(filterShutdownHookThreads(SHUTDOWN_HOOK_CALLBACKS_THREAD_FILTER, true).isEmpty());
    }

    @Test
    public void testAddShutdownHookCallback() {
        int times = 3;
        for (int i = 0; i < times; i++) {
            addShutdownHookCallback(new ShutdownHookCallback(i));
        }

        for (int i = 0; i < times; i++) {
            ShutdownHookCallback shutdownHookCallback = (ShutdownHookCallback) shutdownHookCallbacks.poll();
            assertEquals(i, shutdownHookCallback.getPriority());
        }
    }

    @Test
    public void testRemoveShutdownHookCallback() {
        int times = 3;
        for (int i = 0; i < times; i++) {
            addShutdownHookCallback(new ShutdownHookCallback(i));
        }

        Queue<Runnable> shutdownHookCallbacks = getShutdownHookCallbacks();

        for (int i = 0; i < times; i++) {
            ShutdownHookCallback shutdownHookCallback = (ShutdownHookCallback) shutdownHookCallbacks.peek();
            assertTrue(removeShutdownHookCallback(shutdownHookCallback));
        }

        assertTrue(shutdownHookCallbacks.isEmpty());

    }

    @Test
    public void testRemoveShutdownHookCallbackOnNull() {
        assertFalse(removeShutdownHookCallback(null));
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

    static class ShutdownHookCallback implements Runnable, Prioritized {

        private final int priority;

        ShutdownHookCallback(int priority) {
            this.priority = priority;
        }

        @Override
        public void run() {
            logger.trace("Run an instance of ShutdownHookCallback[priority : {}] : {}", priority, this);
        }

        @Override
        public int getPriority() {
            return priority;
        }

    }
}
