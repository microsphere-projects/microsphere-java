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
import io.microsphere.reflect.JavaType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * The {@link Event Event} Listener that is based on Java standard {@link java.util.EventListener} interface supports
 * the generic {@link Event}.
 * <p>
 * The {@link #onEvent(Event) handle method} will be notified when the matched-type {@link Event Event} is
 * published, whose priority could be changed by {@link #getPriority()} method.
 *
 * @param <E> the concrete class of {@link Event Event}
 * @see Event
 * @see java.util.EventListener
 * @since 1.0.0
 */
@FunctionalInterface
public interface EventListener<E extends Event> extends java.util.EventListener, Prioritized {

    /**
     * Find the {@link Class type} {@link Event event} from the specified {@link EventListener event listener}
     *
     * @param listener the {@link Class class} of {@link EventListener event listener}
     * @return <code>null</code> if not found
     */
    static Class<? extends Event> findEventType(EventListener<?> listener) {
        return findEventType(listener.getClass());
    }

    /**
     * Find the {@link Class type} {@link Event event} from the specified {@link EventListener event listener}
     *
     * @param listenerClass the {@link Class class} of {@link EventListener event listener}
     * @return <code>null</code> if not found
     */
    static Class<? extends Event> findEventType(Class<? extends EventListener> listenerClass) {
        return JavaType.from(listenerClass)
                .as(EventListener.class)
                .getGenericType(0)
                .toClass();
    }

    /**
     * Find the type {@link Event event} from the specified {@link ParameterizedType} presents
     * a class of {@link EventListener event listener}
     *
     * @param parameterizedType the {@link ParameterizedType} presents a class of {@link EventListener event listener}
     * @return <code>null</code> if not found
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