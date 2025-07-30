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
package io.microsphere.collection;

import io.microsphere.annotation.Immutable;

import java.util.Collections;
import java.util.Iterator;

import static java.util.Collections.emptyIterator;

/**
 * An empty and immutable implementation of the {@link Iterator} interface.
 * <p>
 * This class provides a singleton instance through the public static field {@link #INSTANCE},
 * which can be used directly without creating additional instances. It is safe for use in
 * multi-threaded environments since it does not maintain any mutable state.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Using the singleton instance
 * Iterator<String> iterator = EmptyIterator.INSTANCE;
 *
 * // Iterating over the empty iterator
 * while (iterator.hasNext()) {
 *     String next = iterator.next(); // Will never enter the loop
 * }
 *
 * // Attempting to remove from the iterator will throw an exception
 * try {
 *     iterator.remove();
 * } catch (UnsupportedOperationException e) {
 *     System.out.println("Cannot modify an empty, immutable iterator.");
 * }
 * }</pre>
 *
 * @param <E> the type of elements returned by this iterator
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Iterator
 * @see Collections#emptyIterator()
 */
@Immutable
public class EmptyIterator<E> implements Iterator<E> {

    /**
     * The singleton of {@link EmptyIterator}
     */
    public static final EmptyIterator INSTANCE = new EmptyIterator();

    private final Iterator<E> delegate;

    public EmptyIterator() {
        this.delegate = emptyIterator();
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public E next() {
        return delegate.next();
    }

    @Override
    public void remove() {
        delegate.remove();
    }
}
