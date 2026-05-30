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
 * A generic implementation of the {@link Event} class that allows for type-safe event handling.
 * <p>
 * This class extends the base {@link Event} class and provides a way to carry an arbitrary source object
 * associated with the event. The source can be accessed in a type-safe manner using the {@link #getSource()} method.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>
 * // Create a new GenericEvent with a String source
 * GenericEvent<String> event = new GenericEvent<>("Hello, World!");
 *
 * // Access the source with its original type
 * String source = event.getSource(); // No casting needed
 *
 * // Get the timestamp when the event was created
 * long timestamp = event.getTimestamp();
 * </pre>
 *
 * @param <S> the type of the event source
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class GenericEvent<S> extends Event {

    public GenericEvent(S source) {
        super(source);
    }

    public S getSource() {
        return (S) super.getSource();
    }
}
