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

import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.util.Utils;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;

import static io.microsphere.collection.ReversedDeque.of;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.ClassUtils.isAssignableFrom;

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
    public static boolean isQueue(@Nullable Object values) {
        return values instanceof Queue;
    }

    /**
     * Checks whether the specified {@link Class type} is an instance of {@link Queue}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean result = isQueue(LinkedList.class); // returns true
     *
     * result = isQueue(ArrayList.class); // returns false
     * }</pre>
     *
     * @param type the {@link Class type} to check
     * @return {@code true} if the given {@link Class type} is a {@link Queue},
     * {@code false} otherwise
     */
    public static boolean isQueue(@Nullable Class<?> type) {
        return isAssignableFrom(Queue.class, type);
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
    @Immutable
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
    @Nonnull
    @Immutable
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
    @Nonnull
    @Immutable
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
    @Nonnull
    @Immutable
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
    @Nonnull
    @Immutable
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

    /**
     * Creates an immutable queue containing the specified elements.
     *
     * <p>
     * This method first creates a new {@link ArrayDeque} with the given elements and then wraps it
     * in an unmodifiable view using {@link #unmodifiableQueue(Queue)}. The resulting queue is immutable,
     * so any attempt to modify it will result in an {@link UnsupportedOperationException}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Queue<String> queue = ofQueue("Hello", "World");
     * System.out.println(queue); // prints [Hello, World]
     *
     * Queue<Integer> emptyQueue = ofQueue();
     * System.out.println(emptyQueue); // prints []
     *
     * queue.add("Java"); // throws UnsupportedOperationException
     * }</pre>
     *
     * @param <E>      the type of elements held in the queue
     * @param elements the elements to be added to the queue, can be null or empty
     * @return an immutable queue containing the specified elements
     */
    @Nonnull
    @Immutable
    public static <E> Queue<E> ofQueue(E... elements) {
        return unmodifiableDeque(newArrayDeque(elements));
    }

    /**
     * Creates a new empty {@link ArrayDeque} instance.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ArrayDeque<String> deque = newArrayDeque();
     * deque.add("Hello");
     * deque.add("World");
     * System.out.println(deque); // prints [Hello, World]
     * }</pre>
     *
     * @param <E> the type of elements held in the deque
     * @return a new empty {@link ArrayDeque} instance
     */
    @Nonnull
    public static <E> ArrayDeque<E> newArrayDeque() {
        return new ArrayDeque<>();
    }

    /**
     * Creates a new {@link ArrayDeque} instance with the specified initial capacity.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ArrayDeque<String> deque = newArrayDeque(16);
     * deque.add("Hello");
     * deque.add("World");
     * System.out.println(deque); // prints [Hello, World]
     * }</pre>
     *
     * @param <E>             the type of elements held in the deque
     * @param initialCapacity the initial capacity of the deque
     * @return a new {@link ArrayDeque} instance with the specified initial capacity
     * @throws IllegalArgumentException if the specified initial capacity is negative
     */
    @Nonnull
    public static <E> ArrayDeque<E> newArrayDeque(int initialCapacity) {
        return new ArrayDeque<>(initialCapacity);
    }

    /**
     * Creates a new {@link ArrayDeque} instance containing the specified elements.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ArrayDeque<String> deque = newArrayDeque("Hello", "World");
     * System.out.println(deque); // prints [Hello, World]
     *
     * ArrayDeque<Integer> emptyDeque = newArrayDeque();
     * System.out.println(emptyDeque); // prints []
     * }</pre>
     *
     * @param <E>      the type of elements held in the deque
     * @param elements the elements to be added to the deque, can be null or empty
     * @return a new {@link ArrayDeque} instance containing the specified elements
     */
    @Nonnull
    public static <E> ArrayDeque<E> newArrayDeque(E... elements) {
        int length = length(elements);
        ArrayDeque<E> arrayDeque = newArrayDeque(length);
        for (int i = 0; i < length; i++) {
            arrayDeque.add(elements[i]);
        }
        return arrayDeque;
    }

    /**
     * Creates a new {@link ArrayDeque} instance containing all elements from the specified {@link Collection}.
     *
     * <p>This method delegates to the {@link ArrayDeque} constructor that accepts a collection,
     * ensuring all elements from the input collection are included in the resulting deque.
     * The returned deque is modifiable and allows null elements.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> list = Arrays.asList("Hello", "World");
     *     ArrayDeque<String> deque = QueueUtils.newArrayDeque(list);
     *     System.out.println(deque); // prints [Hello, World]
     *
     *     Set<Integer> set = new HashSet<>(Arrays.asList(1, 2, 3));
     *     ArrayDeque<Integer> intDeque = QueueUtils.newArrayDeque(set);
     *     System.out.println(intDeque.size()); // prints 3
     *
     *     ArrayDeque<String> emptyDeque = QueueUtils.newArrayDeque(Collections.emptyList());
     *     System.out.println(emptyDeque.isEmpty()); // prints true
     * }</pre>
     *
     * @param <E>      the type of elements held in the deque
     * @param elements the collection of elements to add to the deque, may be null or empty
     * @return a new {@link ArrayDeque} instance containing all elements from the provided collection
     */
    @Nonnull
    public static <E> ArrayDeque<E> newArrayDeque(Collection<? extends E> elements) {
        return new ArrayDeque<>(elements);
    }

    /**
     * Creates a new empty {@link PriorityQueue} instance.
     *
     * <p>This method provides a convenient way to create an empty priority queue.
     * The returned queue orders elements according to their natural ordering.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Queue<Integer> queue = QueueUtils.newPriorityQueue();
     *     System.out.println(queue.isEmpty()); // Output: true
     *
     *     queue.add(3);
     *     queue.add(1);
     *     queue.add(2);
     *     System.out.println(queue.poll()); // Output: 1
     * }</pre>
     *
     * @param <E> the type of elements in the queue
     * @return a new, empty {@link PriorityQueue}
     */
    @Nonnull
    public static <E> PriorityQueue<E> newPriorityQueue() {
        return new PriorityQueue<>();
    }

    /**
     * Creates a new {@link PriorityQueue} instance with the specified initial capacity.
     *
     * <p>This method provides a convenient way to create a priority queue with a predefined initial size.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Queue<Integer> queue = QueueUtils.newPriorityQueue(10);
     *     System.out.println(queue.isEmpty()); // Output: true
     * }</pre>
     *
     * @param initialCapacity the initial capacity of the priority queue
     * @param <E>             the type of elements in the queue
     * @return a new, empty {@link PriorityQueue} with the specified initial capacity
     */
    @Nonnull
    public static <E> PriorityQueue<E> newPriorityQueue(int initialCapacity) {
        return new PriorityQueue<>(initialCapacity);
    }

    /**
     * Creates a new {@link PriorityQueue} instance with the specified comparator.
     *
     * <p>This method provides a convenient way to create a priority queue with a custom comparator.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Queue<Integer> queue = QueueUtils.newPriorityQueue(Comparator.reverseOrder());
     *     queue.add(1);
     *     queue.add(3);
     *     queue.add(2);
     *     System.out.println(queue.poll()); // Output: 3
     * }</pre>
     *
     * @param comparator the comparator to use for ordering elements
     * @param <E>        the type of elements in the queue
     * @return a new, empty {@link PriorityQueue} with the specified comparator
     */
    @Nonnull
    public static <E> PriorityQueue<E> newPriorityQueue(Comparator<? super E> comparator) {
        return new PriorityQueue<>(comparator);
    }

    /**
     * Creates a new {@link PriorityQueue} instance from the specified {@link Collection}.
     *
     * <p>This method converts the given {@link Collection} into a {@link PriorityQueue}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Collection<Integer> original = Arrays.asList(3, 1, 2);
     *     Queue<Integer> queue = QueueUtils.newPriorityQueue(original);
     *     System.out.println(queue.poll()); // Output: 1
     * }</pre>
     *
     * @param elements the collection of elements to add to the queue, may be null or empty
     * @param <E>      the type of elements in the collection
     * @return a new {@link PriorityQueue} containing all elements from the collection
     */
    @Nonnull
    public static <E> PriorityQueue<E> newPriorityQueue(Collection<? extends E> elements) {
        return new PriorityQueue<>(elements);
    }

    /**
     * Creates a new empty {@link ConcurrentLinkedQueue} instance.
     *
     * <p>This method provides a convenient way to create an empty concurrent linked queue.
     * The returned queue is thread-safe and allows for safe concurrent operations.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Queue<String> queue = QueueUtils.newConcurrentLinkedQueue();
     *     System.out.println(queue.isEmpty()); // Output: true
     *
     *     queue.add("Hello");
     *     System.out.println(queue.poll()); // Output: Hello
     * }</pre>
     *
     * @param <E> the type of elements in the queue
     * @return a new, empty {@link ConcurrentLinkedQueue}
     */
    @Nonnull
    public static <E> ConcurrentLinkedQueue<E> newConcurrentLinkedQueue() {
        return new ConcurrentLinkedQueue<>();
    }

    /**
     * Creates a new {@link ConcurrentLinkedQueue} instance from the specified {@link Collection}.
     *
     * <p>This method converts the given {@link Collection} into a {@link ConcurrentLinkedQueue}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Collection<String> original = Arrays.asList("a", "b", "c");
     *     Queue<String> queue = QueueUtils.newConcurrentLinkedQueue(original);
     *     System.out.println(queue); // Output: [a, b, c]
     * }</pre>
     *
     * @param elements the collection of elements to add to the queue, may be null or empty
     * @param <E>      the type of elements in the collection
     * @return a new {@link ConcurrentLinkedQueue} containing all elements from the collection
     */
    @Nonnull
    public static <E> ConcurrentLinkedQueue<E> newConcurrentLinkedQueue(Collection<? extends E> elements) {
        return new ConcurrentLinkedQueue<>(elements);
    }

    /**
     * Creates a new empty {@link LinkedBlockingQueue} instance.
     *
     * <p>This method provides a convenient way to create an empty linked blocking queue.
     * The returned queue is thread-safe and allows for blocking operations.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Queue<String> queue = QueueUtils.newLinkedBlockingQueue();
     *     System.out.println(queue.isEmpty()); // Output: true
     *
     *     queue.add("Hello");
     *     System.out.println(queue.poll()); // Output: Hello
     * }</pre>
     *
     * @param <E> the type of elements in the queue
     * @return a new, empty {@link LinkedBlockingQueue}
     */
    @Nonnull
    public static <E> LinkedBlockingQueue<E> newLinkedBlockingQueue() {
        return new LinkedBlockingQueue<>();
    }

    /**
     * Creates a new {@link LinkedBlockingQueue} instance with the specified capacity.
     *
     * <p>This method provides a convenient way to create a linked blocking queue with a predefined capacity.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Queue<String> queue = QueueUtils.newLinkedBlockingQueue(10);
     *     System.out.println(queue.isEmpty()); // Output: true
     * }</pre>
     *
     * @param capacity the capacity of the linked blocking queue
     * @param <E>      the type of elements in the queue
     * @return a new, empty {@link LinkedBlockingQueue} with the specified capacity
     */
    @Nonnull
    public static <E> LinkedBlockingQueue<E> newLinkedBlockingQueue(int capacity) {
        return new LinkedBlockingQueue<>(capacity);
    }

    /**
     * Creates a new {@link LinkedBlockingQueue} instance from the specified {@link Collection}.
     *
     * <p>This method converts the given {@link Collection} into a {@link LinkedBlockingQueue}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Collection<String> original = Arrays.asList("a", "b", "c");
     *     Queue<String> queue = QueueUtils.newLinkedBlockingQueue(original);
     *     System.out.println(queue); // Output: [a, b, c]
     * }</pre>
     *
     * @param elements the collection of elements to add to the queue, may be null or empty
     * @param <E>      the type of elements in the collection
     * @return a new {@link LinkedBlockingQueue} containing all elements from the collection
     */
    @Nonnull
    public static <E> LinkedBlockingQueue<E> newLinkedBlockingQueue(Collection<? extends E> elements) {
        return new LinkedBlockingQueue<>(elements);
    }

    /**
     * Creates a new empty {@link ArrayBlockingQueue} instance with the specified capacity.
     *
     * <p>This method provides a convenient way to create an empty array blocking queue with a predefined capacity.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Queue<String> queue = QueueUtils.newArrayBlockingQueue(10);
     *     System.out.println(queue.isEmpty()); // Output: true
     *
     *     queue.add("Hello");
     *     System.out.println(queue.poll()); // Output: Hello
     * }</pre>
     *
     * @param capacity the capacity of the array blocking queue
     * @param <E>      the type of elements in the queue
     * @return a new, empty {@link ArrayBlockingQueue} with the specified capacity
     */
    @Nonnull
    public static <E> ArrayBlockingQueue<E> newArrayBlockingQueue(int capacity) {
        return new ArrayBlockingQueue<>(capacity);
    }

    /**
     * Creates a new {@link ArrayBlockingQueue} instance from the specified {@link Collection}.
     *
     * <p>This method converts the given {@link Collection} into an {@link ArrayBlockingQueue}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Collection<String> original = Arrays.asList("a", "b", "c");
     *     Queue<String> queue = QueueUtils.newArrayBlockingQueue(10, original);
     *     System.out.println(queue); // Output: [a, b, c]
     * }</pre>
     *
     * @param capacity the capacity of the array blocking queue
     * @param elements the collection of elements to add to the queue, may be null or empty
     * @param <E>      the type of elements in the collection
     * @return a new {@link ArrayBlockingQueue} containing all elements from the collection
     */
    @Nonnull
    public static <E> ArrayBlockingQueue<E> newArrayBlockingQueue(int capacity, Collection<? extends E> elements) {
        return new ArrayBlockingQueue<>(capacity, false, elements);
    }

    /**
     * Creates a new empty {@link PriorityBlockingQueue} instance.
     *
     * <p>This method provides a convenient way to create an empty priority blocking queue.
     * The returned queue is thread-safe and orders elements according to their natural ordering.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Queue<Integer> queue = QueueUtils.newPriorityBlockingQueue();
     *     System.out.println(queue.isEmpty()); // Output: true
     *
     *     queue.add(3);
     *     queue.add(1);
     *     System.out.println(queue.poll()); // Output: 1
     * }</pre>
     *
     * @param <E> the type of elements in the queue
     * @return a new, empty {@link PriorityBlockingQueue}
     */
    @Nonnull
    public static <E> PriorityBlockingQueue<E> newPriorityBlockingQueue() {
        return new PriorityBlockingQueue<>();
    }

    /**
     * Creates a new {{@link PriorityBlockingQueue} instance from the specified {@link Collection}.
     *
     * <p>This method converts the given {@link Collection} into a {@link PriorityBlockingQueue}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Collection<Integer> original = Arrays.asList(3, 1, 2);
     *     Queue<Integer> queue = QueueUtils.newPriorityBlockingQueue(original);
     *     System.out.println(queue.poll()); // Output: 1
     * }</pre>
     *
     * @param elements the collection of elements to add to the queue, may be null or empty
     * @param <E>      the type of elements in the collection
     * @return a new {@link PriorityBlockingQueue} containing all elements from the collection
     */
    @Nonnull
    public static <E> PriorityBlockingQueue<E> newPriorityBlockingQueue(Collection<? extends E> elements) {
        return new PriorityBlockingQueue<>(elements);
    }

    /**
     * Creates a new empty {@link DelayQueue} instance.
     *
     * <p>This method provides a convenient way to create an empty delay queue.
     * The returned queue is an unbounded queue where elements can only be retrieved after their delay expires.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Queue<DelayedElement> queue = QueueUtils.newDelayQueue();
     *     System.out.println(queue.isEmpty()); // Output: true
     * }</pre>
     *
     * @param <E> the type of elements in the queue, must be of type Delayed
     * @return a new, empty {@link DelayQueue}
     */
    @Nonnull
    public static <E extends Delayed> DelayQueue<E> newDelayQueue() {
        return new DelayQueue<>();
    }

    /**
     * Creates a new empty {@link SynchronousQueue} instance.
     *
     * <p>This method provides a convenient way to create an empty synchronous queue.
     * The returned queue is a blocking queue with zero capacity where each put operation must wait for a get operation.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Queue<String> queue = QueueUtils.newSynchronousQueue();
     *     System.out.println(queue.isEmpty()); // Output: true
     * }</pre>
     *
     * @param <E> the type of elements in the queue
     * @return a new, empty {@link SynchronousQueue}
     */
    @Nonnull
    public static <E> SynchronousQueue<E> newSynchronousQueue() {
        return new SynchronousQueue<>();
    }

    /**
     * Creates a new empty {@link LinkedTransferQueue} instance.
     *
     * <p>This method provides a convenient way to create an empty linked transfer queue.
     * The returned queue is an unbounded queue for use with producers and consumers that may arrive and depart dynamically.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Queue<String> queue = QueueUtils.newLinkedTransferQueue();
     *     System.out.println(queue.isEmpty()); // Output: true
     *
     *     queue.add("Hello");
     *     System.out.println(queue.poll()); // Output: Hello
     * }</pre>
     *
     * @param <E> the type of elements in the queue
     * @return a new, empty {@link LinkedTransferQueue}
     */
    @Nonnull
    public static <E> LinkedTransferQueue<E> newLinkedTransferQueue() {
        return new LinkedTransferQueue<>();
    }

    /**
     * Creates a new {@link LinkedTransferQueue} instance from the specified {@link Collection}.
     *
     * <p>This method converts the given {@link Collection} into a {@link LinkedTransferQueue}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Collection<String> original = Arrays.asList("a", "b", "c");
     *     Queue<String> queue = QueueUtils.newLinkedTransferQueue(original);
     *     System.out.println(queue); // Output: [a, b, c]
     * }</pre>
     *
     * @param elements the collection of elements to add to the queue, may be null or empty
     * @param <E>      the type of elements in the collection
     * @return a new {@link LinkedTransferQueue} containing all elements from the collection
     */
    @Nonnull
    public static <E> LinkedTransferQueue<E> newLinkedTransferQueue(Collection<? extends E> elements) {
        return new LinkedTransferQueue<>(elements);
    }

    private QueueUtils() {
    }
}
