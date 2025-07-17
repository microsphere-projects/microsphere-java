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

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Predicate;

import static io.microsphere.annotation.ConfigurationProperty.Sources.SYSTEM_PROPERTIES;
import static io.microsphere.collection.QueueUtils.unmodifiableQueue;
import static io.microsphere.constants.PropertyConstants.MICROSPHERE_PROPERTY_NAME_PREFIX;
import static io.microsphere.lang.Prioritized.COMPARATOR;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.FieldUtils.getStaticFieldValue;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.ShutdownHookCallbacksThread.INSTANCE;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.Integer.getInteger;
import static java.lang.Integer.parseInt;
import static java.lang.Runtime.getRuntime;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;

/**
 * Utilities for managing shutdown hooks in a JVM application.
 *
 * <p>This class provides methods to register and manage shutdown hook callbacks, which are executed when the JVM begins its shutdown sequence.
 * These callbacks can be used to perform cleanup operations, such as closing resources or saving state before the application exits.</p>
 *
 * <h3>Example Usage</h3>
 *
 * <h4>Registering a Shutdown Hook</h4>
 * <pre>{@code
 * ShutdownHookUtils.addShutdownHookCallback(() -> {
 *     System.out.println("Performing cleanup before shutdown...");
 * });
 * }</pre>
 *
 * <h4>Filtering Existing Shutdown Hooks</h4>
 * <pre>{@code
 * Set<Thread> shutdownHooks = ShutdownHookUtils.getShutdownHookThreads();
 * shutdownHooks.forEach(System.out::println);
 * }</pre>
 *
 * <h4>Clearing All Registered Callbacks</h4>
 * <pre>{@code
 * ShutdownHookUtils.clearShutdownHookCallbacks();
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see java.lang.Runtime#addShutdownHook(Thread)
 * @see java.lang.ApplicationShutdownHooks
 * @see io.microsphere.util.ShutdownHookCallbacksThread
 * @since 1.0.0
 */
public abstract class ShutdownHookUtils implements Utils {

    private static final Logger logger = getLogger(ShutdownHookUtils.class);

    /**
     * The System property name of the capacity of ShutdownHook callbacks : {@code "microsphere.shutdown-hook.callbacks-capacity"}
     */
    public static final String SHUTDOWN_HOOK_CALLBACKS_CAPACITY_PROPERTY_NAME = MICROSPHERE_PROPERTY_NAME_PREFIX + "shutdown-hook.callbacks-capacity";

    /**
     * The default property value of the capacity of ShutdownHook callbacks: {@code "512"}
     */
    public static final String DEFAULT_SHUTDOWN_HOOK_CALLBACKS_CAPACITY_PROPERTY_VALUE = "512";

    /**
     * The default value of the capacity of ShutdownHook callbacks: {@code 512}
     */
    public static final int DEFAULT_SHUTDOWN_HOOK_CALLBACKS_CAPACITY = parseInt(DEFAULT_SHUTDOWN_HOOK_CALLBACKS_CAPACITY_PROPERTY_VALUE);

    /**
     * The capacity of ShutdownHook callbacks, the default value is 512
     */
    @ConfigurationProperty(
            name = SHUTDOWN_HOOK_CALLBACKS_CAPACITY_PROPERTY_NAME,
            defaultValue = DEFAULT_SHUTDOWN_HOOK_CALLBACKS_CAPACITY_PROPERTY_VALUE,
            description = "The capacity of ShutdownHook callbacks",
            source = SYSTEM_PROPERTIES
    )
    public static final int SHUTDOWN_HOOK_CALLBACKS_CAPACITY = getInteger(SHUTDOWN_HOOK_CALLBACKS_CAPACITY_PROPERTY_NAME, DEFAULT_SHUTDOWN_HOOK_CALLBACKS_CAPACITY);

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
     * Registers the {@link ShutdownHookCallbacksThread} as a JVM shutdown hook to execute registered callbacks
     * during application shutdown.
     *
     * <p>If no such hook has been previously registered, this method adds the hook using
     * {@link Runtime#addShutdownHook(Thread)}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Ensure the shutdown hook is registered
     * ShutdownHookUtils.registerShutdownHook();
     * }</pre>
     */
    public static void registerShutdownHook() {

        Set<Thread> shutdownHookThreads = filterShutdownHookThreads(SHUTDOWN_HOOK_CALLBACKS_THREAD_FILTER);

        if (shutdownHookThreads.isEmpty()) {
            getRuntime().addShutdownHook(INSTANCE);
        }
    }

    /**
     * Retrieves an unmodifiable set of all registered JVM shutdown hook threads.
     *
     * <p>This method provides access to the current collection of threads that have been registered
     * as JVM shutdown hooks. These hooks are typically added using {@link Runtime#addShutdownHook(Thread)}.
     * The returned set reflects the current state and includes all known shutdown hook threads.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <h4>Listing All Shutdown Hook Threads</h4>
     * <pre>{@code
     * Set<Thread> shutdownHooks = ShutdownHookUtils.getShutdownHookThreads();
     * System.out.println("Registered shutdown hook threads:");
     * shutdownHooks.forEach(System.out::println);
     * }</pre>
     *
     * <h4>Checking for Specific Shutdown Hooks</h4>
     * <pre>{@code
     * Set<Thread> shutdownHooks = ShutdownHookUtils.getShutdownHookThreads();
     * boolean hasMyHook = shutdownHooks.contains(myCustomShutdownHookThread);
     * if (hasMyHook) {
     *     System.out.println("My custom shutdown hook is already registered.");
     * }
     * }</pre>
     *
     * @return A non-null, unmodifiable set containing all currently registered shutdown hook threads.
     */
    @Nonnull
    public static Set<Thread> getShutdownHookThreads() {
        return filterShutdownHookThreads(t -> true);
    }

    /**
     * Filters and returns a set of registered JVM shutdown hook threads based on the provided predicate.
     *
     * <p>This method allows filtering of shutdown hook threads by applying a given condition (predicate).
     * The returned set contains only those threads that match the filter criteria. It provides a way to
     * selectively retrieve specific shutdown hooks, such as identifying custom shutdown hook threads.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <h4>Filtering MicroSphere Shutdown Hook Threads</h4>
     * <pre>{@code
     * Set<Thread> microsphereShutdownHooks = ShutdownHookUtils.filterShutdownHookThreads(SHUTDOWN_HOOK_CALLBACKS_THREAD_FILTER);
     * System.out.println("MicroSphere shutdown hooks:");
     * microsphereShutdownHooks.forEach(System.out::println);
     * }</pre>
     *
     * <h4>Filtering Custom Shutdown Hooks</h4>
     * <pre>{@code
     * Predicate<Thread> myCustomHookPredicate = t -> t.getName().contains("MyCustomHook");
     * Set<Thread> customShutdownHooks = ShutdownHookUtils.filterShutdownHookThreads(myCustomHookPredicate);
     * System.out.println("Custom shutdown hooks:");
     * customShutdownHooks.forEach(System.out::println);
     * }</pre>
     *
     * @param hookThreadFilter A {@link Predicate} used to filter which shutdown hook threads should be included in the result.
     *                         Only threads for which the predicate returns {@code true} will be included.
     * @return A non-null, unmodifiable set containing the filtered shutdown hook threads.
     */
    public static Set<Thread> filterShutdownHookThreads(Predicate<? super Thread> hookThreadFilter) {
        return filterShutdownHookThreads(hookThreadFilter, false);
    }

    /**
     * Filters and returns a set of registered JVM shutdown hook threads based on the provided predicate.
     *
     * <p>This method allows filtering of shutdown hook threads by applying a given condition (predicate).
     * The returned set contains only those threads that match the filter criteria. If the {@code removed}
     * flag is set to {@code true}, matching threads will be removed from the internal registry after being collected.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <h4>Filtering and Retaining MicroSphere Shutdown Hook Threads</h4>
     * <pre>{@code
     * Set<Thread> microsphereShutdownHooks = ShutdownHookUtils.filterShutdownHookThreads(
     *     ShutdownHookCallbacksThread.class::isInstance, false);
     * System.out.println("MicroSphere shutdown hooks:");
     * microsphereShutdownHooks.forEach(System.out::println);
     * }</pre>
     *
     * <h4>Filtering and Removing Custom Shutdown Hooks</h4>
     * <pre>{@code
     * Predicate<Thread> myCustomHookPredicate = t -> t.getName().contains("MyCustomHook");
     * Set<Thread> customShutdownHooks = ShutdownHookUtils.filterShutdownHookThreads(myCustomHookPredicate, true);
     * System.out.println("Removed custom shutdown hooks:");
     * customShutdownHooks.forEach(System.out::println);
     * }</pre>
     *
     * @param hookThreadFilter A {@link Predicate} used to filter which shutdown hook threads should be included in the result.
     *                         Only threads for which the predicate returns {@code true} will be included.
     * @param removed          If {@code true}, the filtered threads will be removed from the internal registry.
     * @return A non-null, unmodifiable set containing the filtered shutdown hook threads.
     */
    @Nonnull
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
     * Adds a shutdown hook callback to be executed during JVM shutdown.
     *
     * <p>This method registers a {@link Runnable} callback that will be invoked when the JVM begins its shutdown sequence.
     * The callback is added to an internal queue and will be executed in the order determined by its priority.
     * If the callback is already registered, it will not be added again.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <h4>Registering a Simple Shutdown Hook</h4>
     * <pre>{@code
     * boolean registered = ShutdownHookUtils.addShutdownHookCallback(() -> {
     *     System.out.println("Performing cleanup before application exit...");
     * });
     * if (registered) {
     *     System.out.println("Shutdown hook successfully registered.");
     * }
     * }</pre>
     *
     * <h4>Registering a Prioritized Shutdown Hook</h4>
     * <pre>{@code
     * boolean registered = ShutdownHookUtils.addShutdownHookCallback(new PrioritizedRunnable(() -> {
     *     System.out.println("High-priority cleanup task.");
     * }, 100));
     * }</pre>
     *
     * @param callback the {@link Runnable} callback to be executed during JVM shutdown; may be {@code null}, in which case no action is taken
     * @return {@code true} if the callback was successfully added; {@code false} otherwise
     */
    public static boolean addShutdownHookCallback(@Nullable Runnable callback) {
        boolean added = false;
        if (callback != null) {
            added = shutdownHookCallbacks.add(callback);
        }
        return added;
    }

    /**
     * Removes a previously registered shutdown hook callback from the queue.
     *
     * <p>This method attempts to remove the specified {@link Runnable} callback from the internal queue
     * of shutdown hook callbacks. If the callback is not present in the queue, this method returns
     * {@code false} and no action is taken.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <h4>Removing a Simple Shutdown Hook Callback</h4>
     * <pre>{@code
     * Runnable cleanupTask = () -> System.out.println("Cleaning up resources...");
     * ShutdownHookUtils.addShutdownHookCallback(cleanupTask);
     *
     * boolean removed = ShutdownHookUtils.removeShutdownHookCallback(cleanupTask);
     * if (removed) {
     *     System.out.println("Cleanup task removed successfully.");
     * } else {
     *     System.out.println("Cleanup task was not found.");
     * }
     * }</pre>
     *
     * <h4>Removing a Prioritized Shutdown Hook Callback</h4>
     * <pre>{@code
     * PrioritizedRunnable highPriorityTask = new PrioritizedRunnable(() -> {
     *     System.out.println("High-priority cleanup task.");
     * }, 100);
     *
     * ShutdownHookUtils.addShutdownHookCallback(highPriorityTask);
     * boolean removed = ShutdownHookUtils.removeShutdownHookCallback(highPriorityTask);
     *
     * if (removed) {
     *     System.out.println("High-priority task removed.");
     * }
     * }</pre>
     *
     * @param callback the {@link Runnable} callback to be removed; may be {@code null}, in which case no action is taken
     * @return {@code true} if the callback was successfully removed; {@code false} if it was not found or if it was {@code null}
     */
    public static boolean removeShutdownHookCallback(@Nullable Runnable callback) {
        boolean removed = false;
        if (callback != null) {
            removed = shutdownHookCallbacks.remove(callback);
        }
        return removed;
    }

    /**
     * Retrieves an unmodifiable {@link Queue} containing all registered shutdown hook callbacks.
     *
     * <p>These callbacks are executed in the order determined by their priority when the JVM begins its shutdown sequence.
     * The returned queue reflects the current state and includes all known shutdown hook callbacks.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <h4>Accessing All Registered Shutdown Hook Callbacks</h4>
     * <pre>{@code
     * Queue<Runnable> callbacks = ShutdownHookUtils.getShutdownHookCallbacks();
     * System.out.println("Registered shutdown hook callbacks:");
     * callbacks.forEach(callback -> System.out.println(callback));
     * }</pre>
     *
     * <h4>Checking for Specific Callbacks</h4>
     * <pre>{@code
     * Queue<Runnable> callbacks = ShutdownHookUtils.getShutdownHookCallbacks();
     * boolean hasMyCallback = callbacks.contains(myCustomRunnable);
     * if (hasMyCallback) {
     *     System.out.println("My custom callback is registered.");
     * }
     * }</pre>
     *
     * @return A non-null, unmodifiable queue containing all currently registered shutdown hook callbacks.
     */
    @Nonnull
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

    private ShutdownHookUtils() {
    }

}