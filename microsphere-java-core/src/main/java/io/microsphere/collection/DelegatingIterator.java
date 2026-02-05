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

import io.microsphere.lang.DelegatingWrapper;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * A delegating implementation of the {@link Iterator} interface that forwards all method calls to a delegate iterator.
 * This class is useful when you want to wrap an existing iterator and potentially override some of its behavior.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * List<String> list = Arrays.asList("a", "b", "c");
 * Iterator<String> iterator = new DelegatingIterator<>(list.iterator());
 * while (iterator.hasNext()) {
 *     System.out.println(iterator.next());
 * }
 * }</pre>
 *
 * @param <E> the type of elements returned by this iterator
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Iterator
 */
public class DelegatingIterator<E> implements Iterator<E>, DelegatingWrapper {

    private final Iterator<E> delegate;

    public DelegatingIterator(Iterator<E> delegate) {
        this.delegate = delegate;
    }

    @Override
    public final boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public final E next() {
        return delegate.next();
    }

    @Override
    public final void remove() {
        delegate.remove();
    }

    @Override
    public final void forEachRemaining(Consumer<? super E> action) {
        this.delegate.forEachRemaining(action);
    }

    @Override
    public final Object getDelegate() {
        return this.delegate;
    }

    @Override
    public final int hashCode() {
        return this.delegate.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        return this.delegate.equals(obj);
    }

    @Override
    public final String toString() {
        return this.delegate.toString();
    }
}