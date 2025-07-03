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

import io.microsphere.lang.Prioritized;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.microsphere.event.EventListener.findEventType;
import static io.microsphere.util.ServiceLoaderUtils.loadServicesList;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;

/**
 * Abstract implementation of {@link EventDispatcher} that provides common functionality for dispatching events to
 * registered listeners.
 *
 * <p>This class manages the registration and removal of event listeners, maintains a cache of listeners organized by event
 * type, and supports dispatching events using a specified execution strategy (e.g., sequential or parallel). It also
 * supports sorting of listeners based on their priority via the {@link Prioritized} interface.</p>
 *
 * <h3>Key Features</h3>
 * <ul>
 *     <li><strong>Listener Registration:</strong> Supports adding and removing event listeners with type safety.</li>
 *     <li><strong>Event Dispatching:</strong> Dispatches events to all relevant listeners using an execution strategy defined by an {@link Executor}.</li>
 *     <li><strong>Listener Priority:</strong> Listeners are sorted by priority before being notified of an event. Higher priority listeners (lower integer value) are executed first.</li>
 *     <li><strong>Caching:</strong> Maintains a thread-safe mapping from event types to listeners for efficient dispatching.</li>
 *     <li><strong>Service Loading:</strong> Optionally loads listeners via Java's SPI mechanism ({@link ServiceLoader}).</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 *
 * <pre>{@code
 * public class MyEvent extends Event {}
 *
 * public interface MyEventListener extends EventListener<MyEvent> {
 *     void onEvent(MyEvent event);
 * }
 *
 * // Create a dispatcher with direct (sequential) execution
 * AbstractEventDispatcher dispatcher = new DirectEventDispatcher();
 *
 * // Register a listener
 * dispatcher.addEventListener((MyEventListener) event -> System.out.println("Event received!"));
 *
 * // Dispatch an event
 * dispatcher.dispatch(new MyEvent());
 * }</pre>
 *
 * <h3>Extending This Class</h3>
 * <p>Subclasses must provide a concrete implementation of the {@link #getExecutor()} method that defines how events should be dispatched.</p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EventDispatcher
 * @see EventListener
 * @see Event
 * @see Listenable
 * @see Prioritized
 * @see ServiceLoader
 * @since 1.0.0
 */
public abstract class AbstractEventDispatcher implements EventDispatcher {

    private final Object mutex = new Object();

    private final ConcurrentMap<Class<? extends Event>, List<EventListener>> listenersCache = new ConcurrentHashMap<>();

    private final Executor executor;

    /**
     * Constructor with an instance of {@link Executor}
     *
     * @param executor {@link Executor}
     * @throws NullPointerException <code>executor</code> is <code>null</code>
     */
    protected AbstractEventDispatcher(Executor executor) {
        if (executor == null) {
            throw new NullPointerException("executor must not be null");
        }
        this.executor = executor;
        this.loadEventListenerInstances();
    }

    @Override
    public void addEventListener(EventListener<?> listener) throws NullPointerException, IllegalArgumentException {
        Listenable.assertListener(listener);
        doInListener(listener, listeners -> {
            addIfAbsent(listeners, listener);
        });
    }

    @Override
    public void removeEventListener(EventListener<?> listener) throws NullPointerException, IllegalArgumentException {
        Listenable.assertListener(listener);
        doInListener(listener, listeners -> listeners.remove(listener));
    }

    @Override
    public List<EventListener<?>> getAllEventListeners() {
        List<EventListener<?>> listeners = new LinkedList<>();

        sortedListeners().forEach(listener -> {
            addIfAbsent(listeners, listener);
        });

        return unmodifiableList(listeners);
    }

    protected Stream<EventListener> sortedListeners() {
        return sortedListeners(e -> true);
    }

    protected Stream<EventListener> sortedListeners(Predicate<? super Map.Entry<Class<? extends Event>, List<EventListener>>> predicate) {
        return listenersCache
                .entrySet()
                .stream()
                .filter(predicate)
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .sorted();
    }

    private <E> void addIfAbsent(Collection<E> collection, E element) {
        if (!collection.contains(element)) {
            collection.add(element);
        }
    }

    @Override
    public void dispatch(Event event) {

        Executor executor = getExecutor();

        // execute in sequential or parallel execution model
        executor.execute(() -> {
            sortedListeners(entry -> entry.getKey().isAssignableFrom(event.getClass()))
                    .forEach(listener -> {
                        if (listener instanceof ConditionalEventListener) {
                            ConditionalEventListener predicateEventListener = (ConditionalEventListener) listener;
                            if (!predicateEventListener.accept(event)) { // No accept
                                return;
                            }
                        }
                        // Handle the event
                        listener.onEvent(event);
                    });
        });
    }

    /**
     * @return the non-null {@link Executor}
     */
    @Override
    public final Executor getExecutor() {
        return executor;
    }

    protected void doInListener(EventListener<?> listener, Consumer<Collection<EventListener>> consumer) {
        Class<? extends Event> eventType = findEventType(listener);
        if (eventType != null) {
            synchronized (mutex) {
                List<EventListener> listeners = listenersCache.computeIfAbsent(eventType, e -> new LinkedList<>());
                // consume
                consumer.accept(listeners);
                // sort
                sort(listeners);
            }
        }
    }

    /**
     * Default, load the instances of {@link EventListener event listeners} by {@link ServiceLoader}
     * <p>
     * It could be override by the sub-class
     *
     * @see EventListener
     * @see ServiceLoader#load(Class)
     */
    protected void loadEventListenerInstances() {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            loadServicesList(EventListener.class, classLoader)
                    .stream()
                    .sorted()
                    .forEach(this::addEventListener);
        } catch (Throwable ignored) {
        }
    }
}
