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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.microsphere.lang.Prioritized.COMPARATOR;
import static io.microsphere.reflect.FieldUtils.findField;
import static io.microsphere.reflect.FieldUtils.getStaticFieldValue;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableCollection;

/**
 * The utilities class for ShutdownHook
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see java.lang.ApplicationShutdownHooks
 * @since 1.0.0
 */
public abstract class ShutdownHookUtils extends BaseUtils {

    /**
     * The System property name of the capacity of ShutdownHook callbacks
     */
    public static final String SHUTDOWN_HOOK_CALLBACKS_CAPACITY_PROPERTY_NAME = "microsphere.shutdown-hook.callbacks-capacity";

    /**
     * The System property value of the capacity of ShutdownHook callbacks, the default value is 512
     */
    public static final int SHUTDOWN_HOOK_CALLBACKS_CAPACITY = Integer.getInteger(SHUTDOWN_HOOK_CALLBACKS_CAPACITY_PROPERTY_NAME, 512);

    private static final PriorityBlockingQueue<Runnable> shutdownHookCallbacks = new PriorityBlockingQueue<>(SHUTDOWN_HOOK_CALLBACKS_CAPACITY, COMPARATOR);

    private static final String TARGET_CLASS_NAME = "java.lang.ApplicationShutdownHooks";


    private static final Class<?> TARGET_CLASS = resolveClass(TARGET_CLASS_NAME, getSystemClassLoader());

    private static final Field HOOKS_FIELD = findField(TARGET_CLASS, "hooks");

    private static final IdentityHashMap<Thread, Thread> hooksRef = findHooks();

    private static IdentityHashMap<Thread, Thread> findHooks() {
        return getStaticFieldValue(HOOKS_FIELD);
    }

    static {
        Runtime.getRuntime().addShutdownHook(new ShutdownHookCallbacksThread());
    }

    /**
     * Get the shutdown hooks' threads that was added
     *
     * @return non-null
     */
    public static Set<Thread> getShutdownHookThreads() {
        return filterShutdownHookThreads(t -> true);
    }

    public static Set<Thread> filterShutdownHookThreads(Predicate<Thread> hookThreadFilter) {
        return filterShutdownHookThreads(hookThreadFilter, false);
    }

    public static Set<Thread> filterShutdownHookThreads(Predicate<Thread> hookThreadFilter, boolean removed) {
        if (hooksRef == null || hooksRef.isEmpty()) {
            return emptySet();
        }

        Set<Thread> hookThreads = hooksRef.keySet().stream().filter(hookThreadFilter).collect(Collectors.toSet());
        if (removed) {
            hookThreads.forEach(hooksRef::remove);
        }
        return hookThreads;
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
    public static Collection<Runnable> getShutdownHookCallbacks() {
        return unmodifiableCollection(shutdownHookCallbacks);
    }

    private static class ShutdownHookCallbacksThread extends Thread {

        public ShutdownHookCallbacksThread() {
            setName("ShutdownHookCallbacksThread");
        }

        @Override
        public void run() {
            executeShutdownHookCallbacks();
            clearShutdownHookCallbacks();
        }

        private void executeShutdownHookCallbacks() {
            for (Runnable callback : shutdownHookCallbacks) {
                callback.run();
            }
        }

        private void clearShutdownHookCallbacks() {
            shutdownHookCallbacks.clear();
        }
    }

}
