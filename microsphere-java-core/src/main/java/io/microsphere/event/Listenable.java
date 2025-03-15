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

import io.microsphere.util.ClassUtils;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static io.microsphere.collection.CollectionUtils.addAll;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.ClassUtils.getTypeName;
import static io.microsphere.util.ClassUtils.isConcreteClass;
import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isInterface;
import static java.util.stream.StreamSupport.stream;

/**
 * Event Listenable
 *
 * @see EventDispatcher
 * @since 1.0.0
 */
public interface Listenable<E extends EventListener<?>> {

    /**
     * Assets the listener is valid or not, rules:
     * <ul>
     *     <li>the <code>listener</code> must not be null</li>
     *     <li>the class of <code>listener</code> must not be final</li>
     * </ul>
     *
     * @param listener the instance of {@link EventListener}
     * @throws IllegalArgumentException
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
