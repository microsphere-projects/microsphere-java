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

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Predicate;

import static io.microsphere.collection.QueueUtils.unmodifiableQueue;
import static io.microsphere.lang.Prioritized.COMPARATOR;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.FieldUtils.getStaticFieldValue;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.ShutdownHookCallbacksThread.INSTANCE;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.Runtime.getRuntime;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;

/**
 * The utilities class for ShutdownHook
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see java.lang.ApplicationShutdownHooks
 * @since 1.0.0
 */
public abstract class ShutdownHookUtils extends BaseUtils {

    private static final Logger logger = getLogger(ShutdownHookUtils.class);

    /**
     * The System property name of the capacity of ShutdownHook callbacks
     */
    public static final String SHUTDOWN_HOOK_CALLBACKS_CAPACITY_PROPERTY_NAME = "microsphere.shutdown-hook.callbacks-capacity";

    /**
     * The System property value of the capacity of ShutdownHook callbacks, the default value is 512
     */
    public static final int SHUTDOWN_HOOK_CALLBACKS_CAPACITY = Integer.getInteger(SHUTDOWN_HOOK_CALLBACKS_CAPACITY_PROPERTY_NAME, 512);

    /**
     * The {@link Predicate} to filter the type that is {@link ShutdownHookCallbacksThread}
     */
    public static final Predicate<? super Thread> SHUTDOWN_HOOK_CALLBACKS_THREAD_FILTER = t -> ShutdownHookCallbacksThread.class == t.getClass();

    static final PriorityBlockingQueue<Runnable> shutdownHookCallbacks = new PriorityBlockingQueue<>(SHUTDOWN_HOOK_CALLBACKS_CAPACITY, COMPARATOR);

    private static final String TARGET_CLASS_NAME = "java.lang.ApplicationShutdownHooks";

    private static final String HOOKS_FIELD_NAME = "hooks";

    static {
        registerShutdownHook();
    }

    /**
     * Register the {@link ShutdownHookCallbacksThread} as the shutdown hook
     *
     * @see ShutdownHookCallbacksThread
     */
    public static void registerShutdownHook() {

        Set<Thread> shutdownHookThreads = filterShutdownHookThreads(SHUTDOWN_HOOK_CALLBACKS_THREAD_FILTER);

        if (shutdownHookThreads.isEmpty()) {
            getRuntime().addShutdownHook(INSTANCE);
        }
    }

    /**
     * Get the shutdown hooks' threads that was added
     *
     * @return non-null
     */
    public static Set<Thread> getShutdownHookThreads() {
        return filterShutdownHookThreads(t -> true);
    }

    public static Set<Thread> filterShutdownHookThreads(Predicate<? super Thread> hookThreadFilter) {
        return filterShutdownHookThreads(hookThreadFilter, false);
    }

    public static Set<Thread> filterShutdownHookThreads(Predicate<? super Thread> hookThreadFilter, boolean removed) {
        Map<Thread, Thread> shutdownHookThreadsMap = shutdownHookThreadsMap();

        Set<Thread> shutdownHookThreads = shutdownHookThreadsMap.keySet().stream()
                .filter(hookThreadFilter)
                .collect(toSet());

        if (removed) {
            shutdownHookThreads.forEach(shutdownHookThreadsMap::remove);
        }

        return unmodifiableSet(shutdownHookThreads);
    }

    /**
     * Add the Shutdown Hook Callback
     *
     * @param callback the {@link Runnable} callback
     * @return <code>true</code> if the specified Shutdown Hook Callback added, otherwise <code>false</code>
     */
    public static boolean addShutdownHookCallback(Runnable callback) {
        boolean added = false;
        if (callback != null) {
            added = shutdownHookCallbacks.add(callback);
        }
        return added;
    }

    /**
     * Remove the Shutdown Hook Callback
     *
     * @param callback the {@link Runnable} callback
     * @return <code>true</code> if the specified Shutdown Hook Callback removed, otherwise <code>false</code>
     */
    public static boolean removeShutdownHookCallback(Runnable callback) {
        boolean removed = false;
        if (callback != null) {
            removed = shutdownHookCallbacks.remove(callback);
        }
        return removed;
    }

    /**
     * Get all Shutdown Hook Callbacks
     *
     * @return non-null
     */
    public static Queue<Runnable> getShutdownHookCallbacks() {
        return unmodifiableQueue(shutdownHookCallbacks);
    }

    static void clearShutdownHookCallbacks() {
        shutdownHookCallbacks.clear();
    }

    private static Map<Thread, Thread> shutdownHookThreadsMap() {
        Class<?> applicationShutdownHooksClass = resolveClass(TARGET_CLASS_NAME, getSystemClassLoader());
        return applicationShutdownHooksClass == null ? emptyMap() : getStaticFieldValue(applicationShutdownHooksClass, HOOKS_FIELD_NAME);
    }

}
