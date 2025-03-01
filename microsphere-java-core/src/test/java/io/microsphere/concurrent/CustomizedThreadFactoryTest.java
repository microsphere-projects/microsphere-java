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

import org.junit.jupiter.api.Test;

import java.lang.management.ThreadMXBean;
import java.util.concurrent.ThreadFactory;

import static io.microsphere.concurrent.CustomizedThreadFactory.DEFAULT_DAEMON;
import static io.microsphere.concurrent.CustomizedThreadFactory.DEFAULT_PRIORITY;
import static io.microsphere.concurrent.CustomizedThreadFactory.newThreadFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link CustomizedThreadFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see CustomizedThreadFactory
 * @since 1.0.0
 */
public class CustomizedThreadFactoryTest {

    private final String namePrefix = "test-thread-";

    @Test
    public void test() throws InterruptedException {
        ThreadFactory threadFactory = newThreadFactory(namePrefix);
        assertThreadFactory(threadFactory, DEFAULT_DAEMON, DEFAULT_PRIORITY);

        threadFactory = newThreadFactory(namePrefix, false, Thread.MAX_PRIORITY);
        assertThreadFactory(threadFactory, false, Thread.MAX_PRIORITY);

        threadFactory = newThreadFactory(namePrefix, false, Thread.MIN_PRIORITY, 1024 * 1024);
        assertThreadFactory(threadFactory, false, Thread.MIN_PRIORITY);
    }

    private void assertThreadFactory(ThreadFactory threadFactory, boolean daemon, int priority) throws InterruptedException {
        Thread thread = threadFactory.newThread(() -> {
            Thread t = Thread.currentThread();
            assertTrue(t.getName().startsWith(namePrefix));
            assertEquals(daemon, t.isDaemon());
            assertEquals(priority, t.getPriority());
        });
        thread.start();
        thread.join();
    }
}
