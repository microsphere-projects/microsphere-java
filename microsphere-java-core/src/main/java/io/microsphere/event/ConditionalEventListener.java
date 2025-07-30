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

/**
 * A conditional extension of the {@link EventListener} interface that allows filtering events before handling.
 *
 * <p>{@link ConditionalEventListener} provides an additional method, {@link #accept(Event)}, which is invoked
 * before the actual event handling ({@link EventListener#onEvent(Event)}) takes place. If the method returns
 * <code>true</code>, the event will be processed by this listener; otherwise, it will be skipped.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * ConditionalEventListener<MyEvent> listener = new ConditionalEventListener<>() {
 *     public boolean accept(MyEvent event) {
 *         // Only accept events with a specific flag
 *         return event.isImportant();
 *     }
 *
 *     public void onEvent(MyEvent event) {
 *         System.out.println("Handling important event: " + event);
 *     }
 * };
 * }</pre>
 *
 * <p>In this example, only events marked as "important" are handled by the listener.
 *
 * @param <E> the concrete class of {@link Event}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EventListener
 * @since 1.0.0
 */
public interface ConditionalEventListener<E extends Event> extends EventListener<E> {

    /**
     * Accept the event is handled or not by current listener
     *
     * @param event {@link Event event}
     * @return if handled, return <code>true</code>, or <code>false</code>
     */
    boolean accept(E event);
}
