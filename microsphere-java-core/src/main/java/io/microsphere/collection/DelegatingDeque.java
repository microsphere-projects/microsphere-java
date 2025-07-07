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

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.microsphere.invoke.MethodHandlesLookupUtils.findPublicVirtual;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.util.Assert.assertNotNull;

/**
 * Delegating {@link Deque}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractDeque
 * @since 1.0.0
 */
public class DelegatingDeque<E> implements Deque<E>, DelegatingWrapper {

    /**
     * The MethodHandle for {@link Deque#reversed()} since Java 21
     */
    private static final MethodHandle reversedMethodHandle = findPublicVirtual(Deque.class, "reversed");

    protected final Deque<E> delegate;

    public DelegatingDeque(Deque<E> delegate) {
        assertNotNull(delegate, () -> "The 'delegate' argument must not be null!");
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
    public boolean add(E e) {
        return delegate.add(e);
    }

    @Override
    public boolean offer(E e) {
        return delegate.offer(e);
    }

    @Override
    public E remove() {
        return delegate.remove();
    }

    @Override
    public E poll() {
        return delegate.poll();
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
    public boolean addAll(Collection<? extends E> c) {
        return delegate.addAll(c);
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
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public Iterator<E> iterator() {
        return delegate.iterator();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return delegate.descendingIterator();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
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
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return delegate.removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public void clear() {
        delegate.clear();
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

    @Override
    public Object getDelegate() {
        return this.delegate;
    }

    /**
     * @since Java 21
     */
    public Deque<E> reversed() {
        if (reversedMethodHandle == null) {
            return ReversedDeque.of(delegate);
        }
        return execute(() -> (Deque<E>) reversedMethodHandle.invokeExact(delegate));
    }
}
