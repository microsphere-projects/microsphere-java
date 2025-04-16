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

import io.microsphere.util.Utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.microsphere.collection.CollectionUtils.singletonIterator;
import static io.microsphere.collection.CollectionUtils.unmodifiableIterator;
import static java.util.Collections.emptyIterator;

/**
 * The utilities class for Java {@link Queue}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Queue
 * @since 1.0.0
 */
public abstract class QueueUtils implements Utils {

    private static final Deque EMPTY_DEQUE = new EmptyDeque();

    public static boolean isQueue(Iterable<?> values) {
        return values instanceof Queue;
    }

    public static boolean isDeque(Iterable<?> values) {
        return values instanceof Deque;
    }

    public static <E> Queue<E> emptyQueue() {
        return (Queue<E>) EMPTY_DEQUE;
    }

    public static <E> Deque<E> emptyDeque() {
        return (Deque<E>) EMPTY_DEQUE;
    }

    public static <E> Queue<E> unmodifiableQueue(Queue<E> queue) {
        return new UnmodifiableQueue(queue);
    }

    public static <E> Deque<E> unmodifiableDeque(Deque<E> deque) {
        return new UnmodifiableDeque(deque);
    }

    public static <E> Queue<E> singletonQueue(E element) {
        return new SingletonDeque<>(element);
    }

    public static <E> Deque<E> singletonDeque(E element) {
        return new SingletonDeque<>(element);
    }

    static class EmptyDeque<E> extends AbstractDeque<E> implements Serializable {

        private static final long serialVersionUID = -1L;

        @Override
        public Iterator<E> iterator() {
            return emptyIterator();
        }

        @Override
        public Iterator<E> descendingIterator() {
            return emptyIterator();
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
        public E pollFirst() {
            throw new UnsupportedOperationException();
        }

        @Override
        public E pollLast() {
            throw new UnsupportedOperationException();
        }

        @Override
        public E getFirst() {
            throw new UnsupportedOperationException();
        }

        @Override
        public E getLast() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeLastOccurrence(Object o) {
            return false;
        }

        @Override
        public int size() {
            return 0;
        }
    }

    static class UnmodifiableQueue<E> implements Queue<E>, Serializable {

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
    }

    static class UnmodifiableDeque<E> extends UnmodifiableQueue<E> implements Deque<E>, Serializable {

        private final Deque<E> delegate;

        UnmodifiableDeque(Deque<E> deque) {
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
            return getFirst();
        }

        @Override
        public E peekLast() {
            return getLast();
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

    static class SingletonDeque<E> extends AbstractDeque<E> implements Serializable {

        private static final long serialVersionUID = -1L;

        private final E element;


        SingletonDeque(E element) {
            this.element = element;
        }

        @Override
        public Iterator<E> iterator() {
            return singletonIterator(element);
        }

        @Override
        public Iterator<E> descendingIterator() {
            return singletonIterator(element);
        }

        @Override
        public boolean removeFirstOccurrence(Object o) {
            return super.removeFirstOccurrence(o);
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
        public E pollFirst() {
            throw new UnsupportedOperationException();
        }

        @Override
        public E pollLast() {
            throw new UnsupportedOperationException();
        }

        @Override
        public E getFirst() {
            return element;
        }

        @Override
        public E getLast() {
            return element;
        }

        @Override
        public boolean removeLastOccurrence(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return 1;
        }
    }

    private QueueUtils() {
    }
}
