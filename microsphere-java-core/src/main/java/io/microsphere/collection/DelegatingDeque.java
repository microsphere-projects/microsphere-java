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

import java.lang.invoke.MethodHandle;
import java.util.Deque;
import java.util.Iterator;

import static io.microsphere.collection.QueueUtils.reversedDeque;
import static io.microsphere.invoke.MethodHandlesLookupUtils.findPublicVirtual;
import static io.microsphere.lang.function.ThrowableSupplier.execute;

/**
 * Delegating {@link Deque}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractDeque
 * @since 1.0.0
 */
public class DelegatingDeque<E> extends DelegatingQueue<E> implements Deque<E> {

    /**
     * The MethodHandle for {@link Deque#reversed()} since Java 21
     */
    private static final MethodHandle reversedMethodHandle = findPublicVirtual(Deque.class, "reversed");

    protected final Deque<E> delegate;

    public DelegatingDeque(Deque<E> delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public void addFirst(E e) {
        delegate.addFirst(e);
    }

    @Override
    public void addLast(E e) {
        delegate.addLast(e);
    }

    @Override
    public boolean offerFirst(E e) {
        return delegate.offerFirst(e);
    }

    @Override
    public boolean offerLast(E e) {
        return delegate.offerLast(e);
    }

    @Override
    public E removeFirst() {
        return delegate.removeFirst();
    }

    @Override
    public E removeLast() {
        return delegate.removeLast();
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
    public boolean removeFirstOccurrence(Object o) {
        return delegate.removeFirstOccurrence(o);
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        return delegate.removeLastOccurrence(o);
    }

    @Override
    public void push(E e) {
        delegate.push(e);
    }

    @Override
    public E pop() {
        return delegate.pop();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return delegate.descendingIterator();
    }

    /**
     * @since Java 21
     */
    public Deque<E> reversed() {
        if (reversedMethodHandle == null) {
            return reversedDeque(delegate);
        }
        return execute(() -> (Deque<E>) reversedMethodHandle.invokeExact(delegate));
    }
}
