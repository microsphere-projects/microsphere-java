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

/**
 * A concrete implementation of {@link AbstractEventDispatcher} that uses a direct (synchronous) execution model.
 *
 * <p>This class dispatches events directly on the calling thread, ensuring sequential execution of listeners.
 * It is suitable for simple use cases where asynchronous or parallel processing is not required.</p>
 *
 * <h3>Key Features</h3>
 * <ul>
 *     <li><strong>Synchronous Dispatching:</strong> Events are processed immediately on the thread that calls {@link #dispatch(Event)}.</li>
 *     <li><strong>Ordered Listener Execution:</strong> Listeners are executed in the order determined by their priority (via the {@link Prioritized} interface).</li>
 *     <li><strong>Thread-Safe Registration:</strong> Listener registration and removal operations are thread-safe.</li>
 * </ul>
 *
 * <h3>Usage Example</h3>
 *
 * <pre>{@code
 * // Create an instance of DirectEventDispatcher
 * DirectEventDispatcher dispatcher = new DirectEventDispatcher();
 *
 * // Register a listener
 * dispatcher.addEventListener((EventListener<MyEvent>) event -> System.out.println("Event handled!"));
 *
 * // Dispatch an event
 * dispatcher.dispatch(new MyEvent());
 * }</pre>
 *
 * <h3>Execution Model</h3>
 * <p>All event listeners are executed sequentially on the same thread that invokes the {@link #dispatch(Event)} method. This ensures predictable execution order but may impact performance if any listener performs long-running operations.</p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractEventDispatcher
 * @see EventDispatcher
 * @see EventListener
 * @see Prioritized
 * @since 1.0.0
 */
public final class DirectEventDispatcher extends AbstractEventDispatcher {

    public DirectEventDispatcher() {
        super(DIRECT_EXECUTOR);
    }
}
