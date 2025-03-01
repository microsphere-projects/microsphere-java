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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Customized {@link ThreadFactory}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class CustomizedThreadFactory implements ThreadFactory {

    /**
     * The default value of {@link #daemon}
     */
    public static final boolean DEFAULT_DAEMON = true;

    /**
     * The default value of {@link #priority}
     */
    public static final int DEFAULT_PRIORITY = Thread.NORM_PRIORITY;

    /**
     * The default value of {@link #stackSize}
     */
    public static final long DEFAULT_STACK_SIZE = 0;

    private final ThreadGroup group;

    private final AtomicInteger threadNumber;

    private final String namePrefix;

    private final boolean daemon;

    private final int priority;

    private final long stackSize;

    protected CustomizedThreadFactory(String namePrefix, boolean daemon, int priority, long stackSize) {
        SecurityManager s = System.getSecurityManager();
        this.group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        this.threadNumber = new AtomicInteger(1);
        this.namePrefix = namePrefix + "-thread-";
        this.daemon = daemon;
        this.priority = priority;
        this.stackSize = stackSize;
    }

    public static ThreadFactory newThreadFactory(String namePrefix) {
        return newThreadFactory(namePrefix, DEFAULT_DAEMON);
    }

    public static ThreadFactory newThreadFactory(String namePrefix, boolean daemon) {
        return newThreadFactory(namePrefix, daemon, DEFAULT_PRIORITY);
    }

    public static ThreadFactory newThreadFactory(String namePrefix, boolean daemon, int priority) {
        return newThreadFactory(namePrefix, daemon, priority, DEFAULT_STACK_SIZE);
    }

    public static ThreadFactory newThreadFactory(String namePrefix, boolean daemon, int priority, long stackSize) {
        return new CustomizedThreadFactory(namePrefix, daemon, priority, stackSize);
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), stackSize);
        t.setDaemon(daemon);
        t.setPriority(priority);
        return t;
    }
}
