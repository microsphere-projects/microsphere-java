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
import java.util.Deque;
import java.util.Iterator;

import static io.microsphere.collection.CollectionUtils.unmodifiableIterator;

/**
 * An unmodifiable view of a {@link Deque}. This implementation decorates a given deque and prevents any
 * modification operations from succeeding. All mutation methods, such as {@link #addFirst}, {@link #addLast},
 * {@link #push}, and others, throw an {@link UnsupportedOperationException}.
 *
 * <p>
 * The behavior of this class is undefined if the underlying deque is modified directly while this view exists.
 * It is intended to be used for creating immutable snapshots of deques where modification attempts are not allowed.
 * </p>
 *
 * <p>
 * <h3>Example Usage</h3>
 * </p>
 *
 * <pre>{@code
 * Deque<String> mutableDeque = new LinkedList<>();
 * mutableDeque.addFirst("World");
 * Deque<String> unmodifiableDeque = new UnmodifiableDeque<>(mutableDeque);
 * unmodifiableDeque.addFirst("Hello"); // throws UnsupportedOperationException
 *  }</pre>
 *
 * <p>
 * This class is serializable if the underlying deque is serializable.
 * </p>
 *
 * @param <E> the type of elements held in this deque
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Deque
 * @see CollectionUtils#unmodifiableIterator(Iterator)
 * @since 1.0.0
 */
public class UnmodifiableDeque<E> extends UnmodifiableQueue<E> implements Deque<E>, Serializable {

    private final Deque<E> delegate;

    public UnmodifiableDeque(Deque<E> deque) {
        super(deque);
        this.delegate = deque;
    }

    @Override
    public void addFirst(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addLast(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean offerFirst(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean offerLast(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E removeFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E removeLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E getFirst() {
        return delegate.getFirst();
    }

    @Override
    public E getLast() {
        return delegate.getLast();
    }

    @Override
    public E peekFirst() {
        return delegate.peekFirst();
    }

    @Override
    public E peekLast() {
        return delegate.peekLast();
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void push(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E pop() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return unmodifiableIterator(delegate.descendingIterator());
    }
}
