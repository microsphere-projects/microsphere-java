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

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.microsphere.collection.CollectionUtils.unmodifiableIterator;

/**
 * An unmodifiable view of a {@link Queue}. This implementation decorates a given queue and prevents any
 * modification operations from succeeding. All mutation methods, such as {@link #add}, {@link #remove},
 * {@link #offer}, and others, throw an {@link UnsupportedOperationException}.
 *
 * <p>
 * The behavior of this class is undefined if the underlying queue is modified directly while this view exists.
 * It is intended to be used for creating immutable snapshots of queues where modification attempts are not allowed.
 * </p>
 *
 * <p>
 * <h3>Example Usage</h3>
 * </p>
 *
 * <pre>{@code
 * Queue<String> mutableQueue = new LinkedList<>();
 * mutableQueue.add("Hello");
 * Queue<String> unmodifiableQueue = new UnmodifiableQueue<>(mutableQueue);
 * unmodifiableQueue.add("World"); // throws UnsupportedOperationException
 *  }</pre>
 *
 * <p>
 * This class is serializable if the underlying queue is serializable.
 * </p>
 *
 * @param <E> the type of elements held in this queue
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Queue
 * @see CollectionUtils#unmodifiableIterator(Iterator)
 * @since 1.0.0
 */
public class UnmodifiableQueue<E> implements Queue<E>, Serializable {

    private static final long serialVersionUID = -1L;

    private final Queue<E> delegate;

    UnmodifiableQueue(Queue<E> queue) {
        this.delegate = queue;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return unmodifiableIterator(delegate.iterator());
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean offer(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E poll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E element() {
        return delegate.element();
    }

    @Override
    public E peek() {
        return delegate.peek();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Spliterator<E> spliterator() {
        return delegate.spliterator();
    }

    @Override
    public Stream<E> stream() {
        return delegate.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return delegate.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        delegate.forEach(action);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
