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

import io.microsphere.annotation.Nonnull;
import io.microsphere.util.Utils;

import java.util.Deque;
import java.util.Queue;

import static io.microsphere.collection.ReversedDeque.of;

/**
 * The utilities class for Java {@link Queue}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Queue
 * @since 1.0.0
 */
public abstract class QueueUtils implements Utils {

    public static final Deque<?> EMPTY_DEQUE = EmptyDeque.INSTANCE;

    /**
     * Checks whether the specified {@link Iterable} is an instance of {@link Queue}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Queue<String> queue = new LinkedList<>();
     * boolean result = isQueue(queue); // returns true
     *
     * List<String> list = new ArrayList<>();
     * result = isQueue(list); // returns false
     * }</pre>
     *
     * @param values the {@link Iterable} to check
     * @return {@code true} if the given {@link Iterable} is a {@link Queue},
     * {@code false} otherwise
     */
    public static boolean isQueue(Iterable<?> values) {
        return values instanceof Queue;
    }

    /**
     * Checks whether the specified {@link Iterable} is an instance of {@link Deque}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Deque<String> deque = new LinkedList<>();
     * boolean result = isDeque(deque); // returns true
     *
     * List<String> list = new ArrayList<>();
     * result = isDeque(list); // returns false
     * }</pre>
     *
     * @param values the {@link Iterable} to check
     * @return {@code true} if the given {@link Iterable} is a {@link Deque},
     * {@code false} otherwise
     */
    public static boolean isDeque(Iterable<?> values) {
        return values instanceof Deque;
    }

    /**
     * Returns an empty immutable queue instance.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Queue<String> empty = emptyQueue();
     * boolean isEmpty = empty.isEmpty(); // returns true
     * int size = empty.size(); // returns 0
     * }</pre>
     *
     * @param <E> the type of elements held in the queue
     * @return an empty immutable queue instance
     */
    @Nonnull
    public static <E> Queue<E> emptyQueue() {
        return (Queue<E>) EMPTY_DEQUE;
    }

    /**
     * Returns an empty immutable deque instance.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Deque<String> empty = emptyDeque();
     * boolean isEmpty = empty.isEmpty(); // returns true
     * int size = empty.size(); // returns 0
     * }</pre>
     *
     * @param <E> the type of elements held in the deque
     * @return an empty immutable deque instance
     */
    public static <E> Deque<E> emptyDeque() {
        return (Deque<E>) EMPTY_DEQUE;
    }

    /**
     * Returns an unmodifiable view of the given queue.
     *
     * <p>
     * This method wraps the provided queue in an {@link UnmodifiableQueue}, which prevents any modifications to the queue.
     * Any attempt to modify the returned queue will result in an {@link UnsupportedOperationException}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Queue<String> mutableQueue = new LinkedList<>();
     * mutableQueue.add("Hello");
     * Queue<String> unmodifiable = unmodifiableQueue(mutableQueue);
     *
     * unmodifiable.add("World"); // throws UnsupportedOperationException
     * }</pre>
     *
     * @param <E>   the type of elements held in the queue
     * @param queue the queue to be made unmodifiable, must not be null
     * @return an unmodifiable view of the specified queue
     * @throws NullPointerException if the provided queue is null
     */
    @Nonnull
    public static <E> Queue<E> unmodifiableQueue(Queue<E> queue) {
        return new UnmodifiableQueue(queue);
    }

    /**
     * Returns an unmodifiable view of the given deque.
     *
     * <p>
     * This method wraps the provided deque in an {@link UnmodifiableDeque}, which prevents any modifications to the deque.
     * Any attempt to modify the returned deque will result in an {@link UnsupportedOperationException}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Deque<String> mutableDeque = new LinkedList<>();
     * mutableDeque.add("Hello");
     * Deque<String> unmodifiable = unmodifiableDeque(mutableDeque);
     *
     * unmodifiable.addFirst("World"); // throws UnsupportedOperationException
     * }</pre>
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
     * <p>
     * The returned queue is a singleton instance that holds exactly one element. It is immutable,
     * so any attempt to modify the queue will result in an {@link UnsupportedOperationException}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Queue<String> singleton = singletonQueue("Hello");
     * boolean isEmpty = singleton.isEmpty(); // returns false
     * int size = singleton.size(); // returns 1
     * String value = singleton.poll(); // returns "Hello"
     * }</pre>
     *
     * @param <E>     the type of the queue's element
     * @param element the sole element to be stored in the returned queue
     * @return a singleton immutable queue containing the specified element
     */
    public static <E> Queue<E> singletonQueue(E element) {
        return new SingletonDeque<>(element);
    }

    /**
     * Returns an immutable deque containing only the specified element.
     *
     * <p>
     * The returned deque is a singleton instance that holds exactly one element. It is immutable,
     * so any attempt to modify the deque will result in an {@link UnsupportedOperationException}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Deque<String> singleton = singletonDeque("Hello");
     * boolean isEmpty = singleton.isEmpty(); // returns false
     * int size = singleton.size(); // returns 1
     * String value = singleton.pollFirst(); // returns "Hello"
     * }</pre>
     *
     * @param <E>     the type of the deque's element
     * @param element the sole element to be stored in the returned deque
     * @return a singleton immutable deque containing the specified element
     */
    public static <E> Deque<E> singletonDeque(E element) {
        return new SingletonDeque<>(element);
    }

    /**
     * Returns a reversed view of the specified deque.
     *
     * <p>
     * This method wraps the provided deque in a {@link ReversedDeque}, which presents the elements in reverse order.
     * Modifications to the original deque are reflected in the reversed view, and vice versa.
     * However, attempts to modify the reversed view may result in an {@link UnsupportedOperationException}
     * depending on the underlying implementation.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Deque<String> deque = new LinkedList<>();
     * deque.addLast("A");
     * deque.addLast("B");
     * deque.addLast("C");
     *
     * Deque<String> reversed = reversedDeque(deque);
     * String first = reversed.peekFirst(); // returns "C"
     * String last = reversed.peekLast();   // returns "A"
     *
     * // Iterating through the reversed view
     * for (String value : reversed) {
     *     System.out.println(value); // prints "C", "B", "A"
     * }
     * }</pre>
     *
     * @param <E>   the type of elements held in the deque
     * @param deque the deque to be viewed in reverse order, must not be null
     * @return a reversed view of the specified deque
     * @throws NullPointerException if the provided deque is null
     */
    public static <E> Deque<E> reversedDeque(Deque<E> deque) {
        return of(deque);
    }

    private QueueUtils() {
    }
}
