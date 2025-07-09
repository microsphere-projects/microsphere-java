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

import java.util.ArrayList;
import java.util.List;

import static io.microsphere.collection.CollectionUtils.addAll;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.ClassUtils.getTypeName;
import static java.lang.reflect.Modifier.isFinal;
import static java.util.stream.StreamSupport.stream;

/**
 * A component that allows registration and management of {@link EventListener} instances.
 * Implementations of this interface provide methods to add, remove, and retrieve event listeners,
 * and may support additional features such as ordering based on listener priority.
 *
 * <p><strong>Example:</strong>
 * <pre>
 * public class MyListenable implements Listenable<MyEventListener> {
 *     // Implementation details...
 * }
 *
 * Listenable<MyEventListener> listenable = new MyListenable();
 * listenable.addEventListener(new MyEventListener());
 * </pre>
 *
 * @param <E> the type of the {@link EventListener} supported by this component
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EventDispatcher
 * @since 1.0.0
 */
public interface Listenable<E extends EventListener<?>> {

    /**
     * Validates the given {@link EventListener} instance to ensure it meets certain criteria.
     *
     * <p>This method checks that:
     * <ul>
     *   <li>The listener is not null.</li>
     *   <li>The class of the listener is not a final class.</li>
     * </ul>
     *
     * <h3>Example Usage</h3>
     * Here's how you might use this method in practice:
     * <pre>{@code
     * EventListener<MyEvent> listener = new MyEventListener();
     * Listenable.assertListener(listener);
     * }</pre>
     *
     * <p>If the listener class is final, an exception will be thrown:
     * <pre>{@code
     * final class FinalEventListener implements EventListener<MyEvent> {
     *     public void onEvent(MyEvent event) {}
     * }
     *
     * EventListener<MyEvent> listener = new FinalEventListener();
     * Listenable.assertListener(listener); // throws IllegalArgumentException
     * }</pre>
     *
     * @param listener the instance of {@link EventListener} to validate
     * @throws IllegalArgumentException if the listener is null or its class is final
     */
    static void assertListener(EventListener<?> listener) throws IllegalArgumentException {
        assertNotNull(listener, () -> "The 'listener' must not be null.");

        Class<?> listenerClass = listener.getClass();

        int modifiers = listenerClass.getModifiers();

        if (isFinal(modifiers)) {
            throw new IllegalArgumentException(format("The listener[class : '{}'] must be non-final class", getTypeName(listenerClass)));
        }
    }

    /**
     * Add a {@link EventListener event listener}
     *
     * @param listener a {@link EventListener event listener}
     *                 If current {@link EventListener} is existed, return <code>false</code>
     * @throws NullPointerException     if <code>listener</code> argument is <code>null</code>
     * @throws IllegalArgumentException if <code>listener</code> argument is not concrete instance
     */
    void addEventListener(E listener) throws NullPointerException, IllegalArgumentException;

    /**
     * Add one or more {@link EventListener event listeners}
     *
     * @param listener a {@link EventListener event listener}
     * @param others   an optional {@link EventListener event listeners}
     * @throws NullPointerException     if one of arguments is <code>null</code>
     * @throws IllegalArgumentException if one of arguments argument is not concrete instance
     */
    default void addEventListeners(E listener, E... others) throws NullPointerException,
            IllegalArgumentException {
        List<E> listeners = new ArrayList<>(1 + others.length);
        listeners.add(listener);
        addAll(listeners, others);
        addEventListeners(listeners);
    }

    /**
     * Add multiple {@link EventListener event listeners}
     *
     * @param listeners the {@link EventListener event listeners}
     * @throws NullPointerException     if <code>listeners</code> argument is <code>null</code>
     * @throws IllegalArgumentException if any element of <code>listeners</code> is not concrete instance
     */
    default void addEventListeners(Iterable<E> listeners) throws NullPointerException, IllegalArgumentException {
        stream(listeners.spliterator(), false).forEach(this::addEventListener);
    }

    /**
     * Remove a {@link EventListener event listener}
     *
     * @param listener a {@link EventListener event listener}
     * @return If remove successfully, return <code>true</code>.
     * If current {@link EventListener} is existed, return <code>false</code>
     * @throws NullPointerException if <code>listener</code> argument is <code>null</code>
     */
    void removeEventListener(E listener) throws NullPointerException, IllegalArgumentException;

    /**
     * Remove a {@link EventListener event listener}
     *
     * @param listeners the {@link EventListener event listeners}
     * @return If remove successfully, return <code>true</code>.
     * If current {@link EventListener} is existed, return <code>false</code>
     * @throws NullPointerException     if <code>listener</code> argument is <code>null</code>
     * @throws IllegalArgumentException if any element of <code>listeners</code> is not concrete instance
     */
    default void removeEventListeners(Iterable<E> listeners) throws NullPointerException, IllegalArgumentException {
        stream(listeners.spliterator(), false).forEach(this::removeEventListener);
    }

    /**
     * Remove all {@link EventListener event listeners}
     *
     * @return a amount of removed listeners
     */
    default void removeAllEventListeners() {
        removeEventListeners(getAllEventListeners());
    }

    /**
     * Get all registered {@link EventListener event listeners}
     *
     * @return non-null read-only ordered {@link EventListener event listeners}
     * @see EventListener#getPriority()
     */
    List<E> getAllEventListeners();
}
