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
package io.github.microsphere.collection;

import io.github.microsphere.util.BaseUtils;

import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Collections.emptyIterator;
import static java.util.Collections.unmodifiableCollection;

/**
 * The utilities class for Java {@link Queue}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Queue
 * @since 1.0.0
 */
public abstract class QueueUtils extends BaseUtils {

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

    static class EmptyDeque<E> extends AbstractQueue<E> implements Deque<E> {

        @Override
        public Iterator<E> iterator() {
            return emptyIterator();
        }

        @Override
        public Iterator<E> descendingIterator() {
            return iterator();
        }

        @Override
        public void addFirst(E e) {
            offer(e);
        }

        @Override
        public void addLast(E e) {
            offer(e);
        }

        @Override
        public boolean offerFirst(E e) {
            offer(e);
            return false;
        }

        @Override
        public boolean offerLast(E e) {
            offer(e);
            return false;
        }

        @Override
        public E removeFirst() {
            return peek();
        }

        @Override
        public E removeLast() {
            return peek();
        }

        @Override
        public E pollFirst() {
            return peek();
        }

        @Override
        public E pollLast() {
            return peek();
        }

        @Override
        public E getFirst() {
            return peek();
        }

        @Override
        public E getLast() {
            return peek();
        }

        @Override
        public E peekFirst() {
            return peek();
        }

        @Override
        public E peekLast() {
            return peek();
        }

        @Override
        public boolean removeFirstOccurrence(Object o) {
            return false;
        }

        @Override
        public boolean removeLastOccurrence(Object o) {
            return false;
        }

        @Override
        public void push(E e) {
            offer(e);
        }

        @Override
        public E pop() {
            return peek();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean offer(E e) {
            throw new IllegalStateException("Empty Queue can't add");
        }

        @Override
        public E poll() {
            return peek();
        }

        @Override
        public E peek() {
            throw new NoSuchElementException();
        }
    }


    static class UnmodifiableQueue<E> extends AbstractQueue<E> implements Queue<E>, Serializable {

        private static final long serialVersionUID = -1578116770333032259L;

        private final Collection<E> delegate;

        UnmodifiableQueue(Queue<E> queue) {
            this.delegate = unmodifiableCollection(queue);
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
            return delegate.iterator();
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
        public boolean offer(E e) {
            return delegate.add(e);
        }

        @Override
        public E poll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public E peek() {
            Iterator<E> iterator = iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            }
            return null;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return delegate.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            return delegate.addAll(c);
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
        public boolean equals(Object o) {
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
}
