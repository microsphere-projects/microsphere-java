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
package io.github.microsphere.util;

import org.junit.Test;

import static io.github.microsphere.util.ShutdownHookUtils.*;
import static io.github.microsphere.util.ShutdownHookUtils.getShutdownHookCallbacks;
import static org.junit.Assert.*;

/**
 * {@link ShutdownHookUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ShutdownHookUtilsTest {

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
                        System.out.printf("Thread[name : %s] is about to be waited...\n", currentThread.getName());
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
                System.out.printf("Thread[name : %s] is about to notify a waited Thread[name: %s]...\n", Thread.currentThread().getName(), thread.getName());
                thread.notify();
            }
        });

        assertEquals(1, getShutdownHookCallbacks().size());

        thread.start();
    }
}
