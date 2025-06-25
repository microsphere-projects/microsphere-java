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
import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;

import static io.microsphere.collection.CollectionUtils.singletonIterator;
import static io.microsphere.collection.CollectionUtils.unmodifiableIterator;

/**
 * The utilities class for Java {@link Queue}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Queue
 * @since 1.0.0
 */
public abstract class QueueUtils implements Utils {

    private static final Deque EMPTY_DEQUE = new EmptyDeque();


    /**
     * Checks whether the specified {@link Iterable} is an instance of {@link Queue}.
     *
     * @param values the {@link Iterable} to check
     * @return {@code true} if the given {@link Iterable} is a {@link Queue}, 
     *         {@code false} otherwise
     */
    public static boolean isQueue(Iterable<?> values) {
        return values instanceof Queue;
    }

    /**
     * Checks whether the specified {@link Iterable} is an instance of {@link Deque}.
     *
     * @param values the {@link Iterable} to check
     * @return {@code true} if the given {@link Iterable} is a {@link Deque}, 
     *         {@code false} otherwise
     */
    public static boolean isDeque(Iterable<?> values) {
        return values instanceof Deque;
    }

    /**
     * Returns an empty queue.
     *
     * @param <E> the type of elements held in the queue
     * @return an empty instance of Queue
     */
    public static <E> Queue<E> emptyQueue() {
        return (Queue<E>) EMPTY_DEQUE;
    }

    /**
     * Returns an empty deque.
     *
     * @param <E> the type of elements held in the deque
     * @return an empty instance of Deque
     */
    public static <E> Deque<E> emptyDeque() {
        return (Deque<E>) EMPTY_DEQUE;
    }

    /**
     * Returns an unmodifiable view of the given queue.
     *
     * @param <E>   the type of elements held in the queue
     * @param queue the queue to be made unmodifiable, must not be null
     * @return an unmodifiable view of the specified queue
     * @throws NullPointerException if the provided queue is null
     */
    public static <E> Queue<E> unmodifiableQueue(Queue<E> queue) {
        return new UnmodifiableQueue(queue);
    }

    /**
     * Returns an unmodifiable view of the given deque.
     *
     * @param <E>   the type of elements held in the deque
     * @param deque the deque to be made unmodifiable, must not be null
     * @return an unmodifiable view of the specified deque
     * @throws NullPointerException if the provided deque is null
     */
    public static <E> Deque<E> unmodifiableDeque(Deque<E> deque) {
        return new UnmodifiableDeque(deque);
    }

    /**
     * Returns an immutable queue containing only the specified element.
     *
     * @param <E>     the type of the queue's elements
     * @param element the sole element to be stored in the returned queue
     * @return a singleton immutable queue containing the specified element
     */
    public static <E> Queue<E> singletonQueue(E element) {
        return new SingletonDeque<>(element);
    }

    /**
     * Returns an immutable deque containing only the specified element.
     *
     * @param <E>     the type of the deque's elements
     * @param element the sole element to be stored in the returned deque
     * @return a singleton immutable deque containing the specified element
     */
    public static <E> Deque<E> singletonDeque(E element) {
        return new SingletonDeque<>(element);
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
