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
package io.microsphere.event;

import java.util.concurrent.Executor;

/**
 * An interface that defines the contract for dispatching events to registered listeners.
 * <p>
 * The EventDispatcher is responsible for managing and notifying event listeners when an event occurs.
 * It provides methods for adding/removing listeners and dispatching events in a specific execution context.
 * </p>
 *
 * <h3>Usage Examples</h3>
 *
 * <h4>Example 1: Using Default Dispatcher (Sequential Execution)</h4>
 * <pre>{@code
 * EventDispatcher dispatcher = EventDispatcher.newDefault();
 * dispatcher.addEventListener(myListener);
 * dispatcher.dispatch(new MyEvent());
 * }</pre>
 *
 * <h4>Example 2: Using Parallel Dispatcher with Custom Executor</h4>
 * <pre>{@code
 * Executor executor = Executors.newFixedThreadPool(4);
 * EventDispatcher dispatcher = EventDispatcher.parallel(executor);
 * dispatcher.addEventListener(myListener);
 * dispatcher.dispatch(new MyEvent());
 * }</pre>
 *
 * <h4>Example 3: Adding Multiple Listeners</h4>
 * <pre>{@code
 * dispatcher.addEventListeners(listener1, listener2, listener3);
 * }</pre>
 *
 * @see Event
 * @see EventListener
 * @see DirectEventDispatcher
 * @since 1.0.0
 */
public interface EventDispatcher extends Listenable<EventListener<?>> {

    /**
     * Direct {@link Executor} uses sequential execution model
     */
    Executor DIRECT_EXECUTOR = Runnable::run;

    /**
     * The default implementation of {@link EventDispatcher}
     *
     * @return the default implementation of {@link EventDispatcher}
     */
    static EventDispatcher newDefault() {
        return new DirectEventDispatcher();
    }

    /**
     * The parallel implementation of {@link EventDispatcher} with the specified {@link Executor}
     *
     * @param executor {@link Executor}
     * @return the default implementation of {@link EventDispatcher}
     */
    static EventDispatcher parallel(Executor executor) {
        return new ParallelEventDispatcher(executor);
    }

    /**
     * Dispatch a event to the registered {@link EventListener event listeners}
     *
     * @param event a {@link Event event}
     */
    void dispatch(Event event);

    /**
     * The {@link Executor} to dispatch a {@link Event event}
     *
     * @return default implementation directly invoke {@link Runnable#run()} method, rather than multiple-threaded
     * {@link Executor}. If the return value is <code>null</code>, the behavior is same as default.
     * @see #DIRECT_EXECUTOR
     */
    default Executor getExecutor() {
        return DIRECT_EXECUTOR;
    }
}
