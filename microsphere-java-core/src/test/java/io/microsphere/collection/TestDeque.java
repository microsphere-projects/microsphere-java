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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.Integer.MAX_VALUE;

/**
 * {@link AbstractDeque} class for testing
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractDeque
 * @since 1.0.0
 */
public class TestDeque<E> extends AbstractDeque<E> {

    private final int maxCapacity;

    private final LinkedList<E> delegate = new LinkedList<>();

    public TestDeque() {
        this(MAX_VALUE);
    }

    public TestDeque(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    @Override
    public Iterator<E> iterator() {
        return delegate.iterator();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        delegate.forEach(action);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return delegate.removeIf(filter);
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
    public Iterator<E> descendingIterator() {
        return delegate.descendingIterator();
    }

    @Override
    public boolean offerFirst(E e) {
        if (isFull()) {
            return false;
        }
        return delegate.offerFirst(e);
    }

    @Override
    public boolean offerLast(E e) {
        if (isFull()) {
            return false;
        }
        return delegate.offerLast(e);
    }

    @Override
    public E pollFirst() {
        return delegate.pollFirst();
    }

    @Override
    public E pollLast() {
        return delegate.pollLast();
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
    public boolean removeLastOccurrence(Object o) {
        return delegate.removeLastOccurrence(o);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    public boolean isFull() {
        return this.size() >= this.maxCapacity;
    }
}