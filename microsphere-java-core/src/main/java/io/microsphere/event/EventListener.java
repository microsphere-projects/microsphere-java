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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

import static io.microsphere.reflect.TypeUtils.getAllParameterizedTypes;


/**
 * The {@link Event Event} Listener that is based on Java standard {@link java.util.EventListener} interface supports
 * the generic {@link Event}.
 * <p>
 * The {@link #onEvent(Event) handle method} will be notified when the matched-type {@link Event Event} is
 * published, whose priority could be changed by {@link #getPriority()} method.
 *
 * <h3>Example</h3>
 * Here is a simple example of how to implement this interface:
 * <pre>{@code
 * public class MyEventListener implements EventListener<MyEvent> {
 *
 *     private final int priority;
 *
 *     public MyEventListener(int priority) {
 *         this.priority = priority;
 *     }
 *
 *     @Override
 *     public void onEvent(MyEvent event) {
 *         // Handle the event
 *     }
 *
 *     @Override
 *     public int getPriority() {
 *         return priority;
 *     }
 * }
 * }</pre>
 *
 * <p>
 * For more complex usage, such as defining multiple listeners with different priorities and event types,
 * refer to the documentation or examples provided in the project.
 *
 * @param <E> the concrete class of {@link Event Event}
 * @see Event
 * @see java.util.EventListener
 * @since 1.0.0
 */
@FunctionalInterface
public interface EventListener<E extends Event> extends java.util.EventListener, Prioritized {

    /**
     * Find the {@link Class type} of the {@link Event} from the specified {@link EventListener} instance.
     *
     * <p>This method is useful when you need to determine the specific event type that a listener is registered for.
     * It examines the generic type information of the listener to find the event type it listens to.</p>
     *
     * <h3>Example</h3>
     * Suppose there's a custom event listener defined like this:
     * <pre>{@code
     * public class MyEventListener implements EventListener<MyEvent> {
     *     @Override
     *     public void onEvent(MyEvent event) {
     *         // Handle the event
     *     }
     * }
     * }</pre>
     *
     * You can use this method to find out what type of event it listens to:
     * <pre>{@code
     * EventListener<MyEvent> listener = new MyEventListener();
     * Class<? extends Event> eventType = EventListener.findEventType(listener);
     * System.out.println(eventType);  // Outputs: class MyEvent
     * }</pre>
     *
     * @param listener the {@link EventListener} instance to examine
     * @return the {@link Class} object representing the type of event this listener handles, or {@code null} if not found
     */
    static Class<? extends Event> findEventType(EventListener<?> listener) {
        return findEventType(listener.getClass());
    }

    /**
     * Find the {@link Class type} of the {@link Event} from the specified {@link EventListener} class.
     *
     * <p>This method is useful when you need to determine the specific event type that a listener is registered for.
     * It examines the generic type information of the listener to find the event type it listens to.</p>
     *
     * <h3>Example</h3>
     * Suppose there's a custom event listener defined like this:
     * <pre>{@code
     * public class MyEventListener implements EventListener<MyEvent> {
     *     @Override
     *     public void onEvent(MyEvent event) {
     *         // Handle the event
     *     }
     * }
     * }</pre>
     *
     * You can use this method to find out what type of event it listens to:
     * <pre>{@code
     * Class<? extends Event> eventType = EventListener.findEventType(MyEventListener.class);
     * System.out.println(eventType);  // Outputs: class MyEvent
     * }</pre>
     *
     * @param listenerClass the class of the {@link EventListener} instance to examine
     * @return the {@link Class} object representing the type of event this listener handles, or {@code null} if not found
     */
    static Class<? extends Event> findEventType(Class<?> listenerClass) {
        Class<? extends Event> eventType = null;

        if (listenerClass != null && EventListener.class.isAssignableFrom(listenerClass)) {
            eventType = getAllParameterizedTypes(listenerClass)
                    .stream()
                    .map(EventListener::findEventType)
                    .filter(Objects::nonNull)
                    .findAny()
                    .orElse((Class) findEventType(listenerClass.getSuperclass()));
        }

        return eventType;
    }

    /**
     * Extracts the concrete {@link Event} type from a {@link ParameterizedType} instance.
     *
     * <p>This method is typically used when inspecting generic interfaces or classes to determine
     * what specific type of event a listener is registered for. It checks whether the provided
     * parameterized type represents an implementation of {@link EventListener}, and if so, finds
     * the event type from its generic parameters.</p>
     *
     * <h3>Example</h3>
     * Suppose you have a parameterized type representing a class like this:
     * <pre>{@code
     * public class MyEventListener implements EventListener<MyEvent> {
     *     @Override
     *     public void onEvent(MyEvent event) {
     *         // Handle the event
     *     }
     * }
     * }</pre>
     *
     * You can use this method to find out what type of event it listens to:
     * <pre>{@code
     * ParameterizedType parameterizedType = (ParameterizedType) MyEventListener.class.getGenericInterfaces()[0];
     * Class<? extends Event> eventType = EventListener.findEventType(parameterizedType);
     * System.out.println(eventType);  // Outputs: class MyEvent
     * }</pre>
     *
     * @param parameterizedType the parameterized type to examine
     * @return the {@link Class} object representing the type of event this listener handles,
     *         or {@code null} if no suitable event type is found
     */
    static Class<? extends Event> findEventType(ParameterizedType parameterizedType) {
        Class<? extends Event> eventType = null;

        Type rawType = parameterizedType.getRawType();
        if ((rawType instanceof Class) && EventListener.class.isAssignableFrom((Class) rawType)) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            for (Type typeArgument : typeArguments) {
                if (typeArgument instanceof Class) {
                    Class argumentClass = (Class) typeArgument;
                    if (Event.class.isAssignableFrom(argumentClass)) {
                        eventType = argumentClass;
                        break;
                    }
                }
            }
        }

        return eventType;
    }

    /**
     * Handle a {@link Event Event} when it's be published
     *
     * @param event a {@link Event Event}
     */
    void onEvent(E event);

    /**
     * The priority of {@link EventListener current listener}.
     *
     * @return the value is more greater, the priority is more lower.
     * {@link Integer#MIN_VALUE} indicates the highest priority. The default value is {@link Integer#MAX_VALUE}.
     * The comparison rule , refer to {@link #compareTo}.
     */
    default int getPriority() {
        return NORMAL_PRIORITY;
    }
}