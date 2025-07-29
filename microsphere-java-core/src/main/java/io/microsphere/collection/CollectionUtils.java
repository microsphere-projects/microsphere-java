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

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import static io.microsphere.collection.ListUtils.isList;
import static io.microsphere.util.ArrayUtils.length;

/**
 * The utilities class for Java Collection
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Collections
 */
public abstract class CollectionUtils implements Utils {

    /**
     * Checks if the provided collection is null or empty.
     *
     * @param collection the collection to check
     * @return true if the collection is null or empty, false otherwise
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * CollectionUtils.isEmpty(null)             // returns true
     * CollectionUtils.isEmpty(Collections.emptyList())  // returns true
     * CollectionUtils.isEmpty(Arrays.asList(1, 2, 3))     // returns false
     * }</pre>
     */
    public static boolean isEmpty(@Nullable Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks if the provided collection is not null and not empty.
     *
     * @param collection the collection to check
     * @return true if the collection is neither null nor empty, false otherwise
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * CollectionUtils.isNotEmpty(null)             // returns false
     * CollectionUtils.isNotEmpty(Collections.emptyList())  // returns false
     * CollectionUtils.isNotEmpty(Arrays.asList(1, 2, 3))     // returns true
     * }</pre>
     */
    public static boolean isNotEmpty(@Nullable Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * Converts a nullable {@link Collection} into an {@link Iterable}.
     * If the provided collection is null, it returns an empty iterable.
     *
     * @param collection the collection to convert, may be null
     * @param <E>        the type of elements in the collection
     * @return an {@link Iterable} backed by the given collection; never null
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Convert a non-null collection to iterable
     * Collection<String> list = Arrays.asList("a", "b", "c");
     * Iterable<String> iterable = CollectionUtils.toIterable(list);
     *
     * // Convert a null collection to iterable
     * Iterable<String> emptyIterable = CollectionUtils.toIterable(null); // returns empty iterable
     * }</pre>
     */
    @Nonnull
    public static <E> Iterable<E> toIterable(@Nullable Collection<E> collection) {
        return collection == null ? EmptyIterable.INSTANCE : collection;
    }

    /**
     * Converts a nullable {@link Iterator} into an {@link Iterable}.
     * If the provided iterator is null, it returns an empty iterable.
     *
     * @param iterator the iterator to convert, may be null
     * @param <E>      the type of elements in the iterator
     * @return a non-null {@link Iterable} backed by the given iterator
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Convert a non-null iterator to iterable
     * Iterator<String> iterator = Arrays.asList("a", "b", "c").iterator();
     * Iterable<String> iterable = CollectionUtils.toIterable(iterator);
     *
     * // Convert a null iterator to iterable
     * Iterable<String> emptyIterable = CollectionUtils.toIterable(null); // returns empty iterable
     * }</pre>
     */
    @Nonnull
    public static <E> Iterable<E> toIterable(@Nullable Iterator<E> iterator) {
        return new IterableAdapter(iterator);
    }

    /**
     * Converts a nullable {@link Enumeration} into an {@link Iterator}.
     * If the provided enumeration is null, it returns an empty iterator.
     *
     * @param enumeration the enumeration to convert, may be null
     * @param <E>         the type of elements in the enumeration
     * @return a non-null {@link Iterator} backed by the given enumeration
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Convert a non-null enumeration to iterator
     * Enumeration<String> enumeration = Collections.enumeration(Arrays.asList("a", "b", "c"));
     * Iterator<String> iterator = CollectionUtils.toIterator(enumeration);
     *
     * // Convert a null enumeration to iterator
     * Iterator<String> emptyIterator = CollectionUtils.toIterator(null); // returns empty iterator
     * }</pre>
     */
    @Nonnull
    public static <E> Iterator<E> toIterator(@Nullable Enumeration<E> enumeration) {
        return new EnumerationIteratorAdapter(enumeration);
    }
    
    /**
     * Converts a nullable {@link Enumeration} into an {@link Iterable}.
     * If the provided enumeration is null, it returns an empty iterable.
     *
     * @param enumeration the enumeration to convert, may be null
     * @param <E>         the type of elements in the enumeration
     * @return a non-null {@link Iterable} backed by the given enumeration
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Convert a non-null enumeration to iterable
     * Enumeration<String> enumeration = Collections.enumeration(Arrays.asList("a", "b", "c"));
     * Iterable<String> iterable = CollectionUtils.toIterable(enumeration);
     *
     * // Convert a null enumeration to iterable
     * Iterable<String> emptyIterable = CollectionUtils.toIterable(null); // returns empty iterable
     * }</pre>
     */
    @Nonnull
    public static <E> Iterable<E> toIterable(@Nullable Enumeration<E> enumeration) {
        return toIterable(toIterator(enumeration));
    }

    /**
     * Creates a singleton {@link Iterable} that contains only the specified element.
     * If the provided element is null, returns an empty iterable.
     *
     * @param element the single element to be contained in the iterable, may be null
     * @param <E>     the type of the element
     * @return a non-null {@link Iterable} containing the single element or an empty iterable if element is null
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Create an iterable with a non-null element
     * Iterable<String> iterable1 = CollectionUtils.singletonIterable("hello");
     *
     * // Create an empty iterable from a null element
     * Iterable<String> iterable2 = CollectionUtils.singletonIterable(null);
     * }</pre>
     */
    @Nonnull
    @Immutable
    public static <E> Iterable<E> singletonIterable(@Nullable E element) {
        return toIterable(singletonIterator(element));
    }

    /**
     * Creates a singleton read-only {@link Iterator} that contains only the specified element.
     * If the provided element is null, returns an iterator with no elements.
     *
     * <p>
     * The returned iterator is read-only and cannot be used to remove elements.
     * </p>
     *
     * @param element the single element to be contained in the iterator, may be null
     * @param <E>     the type of the element
     * @return a non-null read-only {@link Iterator} containing the single element or an empty iterator if element is null
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Create an iterator with a non-null element
     * Iterator<String> iterator1 = CollectionUtils.singletonIterator("hello");
     *
     * // Create an empty iterator from a null element
     * Iterator<String> iterator2 = CollectionUtils.singletonIterator(null);
     * }</pre>
     */
    @Nonnull
    @Immutable
    public static <E> Iterator<E> singletonIterator(@Nullable E element) {
        return new SingletonIterator<>(element);
    }

    /**
     * Creates a singleton read-only {@link Enumeration} that contains only the specified element.
     * If the provided element is null, returns an enumeration with no elements.
     *
     * <p>
     * The returned enumeration is read-only and cannot be used to remove elements.
     * </p>
     *
     * @param element the single element to be contained in the enumeration, may be null
     * @param <E>     the type of the element
     * @return a non-null read-only {@link Enumeration} containing the single element or an empty enumeration if element is null
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Create an enumeration with a non-null element
     * Enumeration<String> enumeration1 = CollectionUtils.singletonEnumeration("hello");
     *
     * // Create an empty enumeration from a null element
     * Enumeration<String> enumeration2 = CollectionUtils.singletonEnumeration(null);
     * }</pre>
     */
    @Nonnull
    @Immutable
    public static <E> Enumeration<E> singletonEnumeration(@Nullable E element) {
        return new SingletonEnumeration<>(element);
    }

    /**
     * Returns an unmodifiable (read-only) view of the given iterator.
     * Attempts to modify the underlying data structure via the returned iterator will result in
     * an {@link UnsupportedOperationException}.
     *
     * <p>
     * If the provided iterator is null, this method returns an empty unmodifiable iterator.
     * </p>
     *
     * @param iterator the iterator to wrap as unmodifiable, may be null
     * @param <E>      the type of elements returned by the iterator
     * @return a non-null read-only {@link Iterator} backed by the given iterator
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Wrap a valid iterator into an unmodifiable one
     * Iterator<String> modifiable = Arrays.asList("a", "b", "c").iterator();
     * Iterator<String> unmodifiable = CollectionUtils.unmodifiableIterator(modifiable);
     *
     * // Attempting to remove will throw an exception
     * if (unmodifiable.hasNext()) {
     *     unmodifiable.next();
     *     unmodifiable.remove();  // throws UnsupportedOperationException
     * }
     *
     * // Passing null returns an empty unmodifiable iterator
     * Iterator<String> empty = CollectionUtils.unmodifiableIterator(null);
     * assert !empty.hasNext();
     * }</pre>
     */
    @Nonnull
    @Immutable
    public static <E> Iterator<E> unmodifiableIterator(@Nullable Iterator<E> iterator) {
        return new UnmodifiableIterator(iterator);
    }

    /**
     * Returns an empty iterator that contains no elements.
     *
     * <p>
     * Any call to {@link Iterator#hasNext()} will return false, and any call to
     * {@link Iterator#next()} will throw a {@link java.util.NoSuchElementException}.
     * </p>
     *
     * @param <E> the type of elements returned by this iterator (though none will be returned)
     * @return a non-null, read-only empty iterator
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Iterator<String> emptyIterator = CollectionUtils.emptyIterator();
     * System.out.println(emptyIterator.hasNext()); // prints: false
     * }</pre>
     */
    @Nonnull
    @Immutable
    public static <E> Iterator<E> emptyIterator() {
        return Collections.emptyIterator();
    }

    /**
     * Returns an empty, immutable {@link Iterable} that contains no elements.
     *
     * <p>
     * Any attempt to iterate over the returned iterable will result in:
     * </p>
     * <ul>
     *   <li>{@link Iterator#hasNext()} always returns {@code false}</li>
     *   <li>{@link Iterator#next()} throws a {@link java.util.NoSuchElementException}</li>
     * </ul>
     *
     * @param <E> the type of elements (though none will be present)
     * @return a non-null, read-only empty iterable
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Iterable<String> empty = CollectionUtils.emptyIterable();
     * System.out.println(empty.iterator().hasNext()); // prints: false
     * }</pre>
     */
    @Nonnull
    @Immutable
    public static <E> Iterable<E> emptyIterable() {
        return EmptyIterable.INSTANCE;
    }

    /**
     * Returns the size of the specified {@link Collection}.
     *
     * <p>If the provided collection is null, this method returns 0.</p>
     *
     * @param collection the specified {@link Collection}, may be null
     * @return the size of the collection, or 0 if it is null
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * CollectionUtils.size(null)             // returns 0
     * CollectionUtils.size(Collections.emptyList())  // returns 0
     * CollectionUtils.size(Arrays.asList(1, 2, 3))     // returns 3
     * }</pre>
     */
    public static int size(@Nullable Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    /**
     * Returns the size of the specified {@link Iterable}.
     *
     * <p>If the provided iterable is null, this method returns 0.</p>
     *
     * @param iterable the specified {@link Iterable}, may be null
     * @return the number of elements in the iterable, or 0 if it is null
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * CollectionUtils.size((Iterable<?>) null)        // returns 0
     *
     * Iterable<String> emptyList = Collections.emptyList();
     * CollectionUtils.size(emptyList)                // returns 0
     *
     * List<Integer> numbers = Arrays.asList(1, 2, 3);
     * CollectionUtils.size(numbers)                  // returns 3
     *
     * Iterable<String> singleton = CollectionUtils.singletonIterable("hello");
     * CollectionUtils.size(singleton)                // returns 1
     * }</pre>
     */
    public static int size(@Nullable Iterable<?> iterable) {
        if (iterable == null) {
            return 0;
        }
        if (iterable instanceof Collection) {
            return size((Collection) iterable);
        }
        Iterator<?> iterator = iterable.iterator();
        int size = 0;
        while (iterator.hasNext()) {
            iterator.next();
            size++;
        }
        return size;
    }


    /**
     * Compares two collections for equality, considering {@code null} and empty collections as equal.
     *
     * <p>
     * Two collections are considered equal if they contain the same elements in any order,
     * and their sizes are equal. If both collections are {@code null} or both are empty,
     * they are also considered equal.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * CollectionUtils.equals(null, null)             // returns true
     * CollectionUtils.equals(Collections.emptyList(), null)  // returns true
     * CollectionUtils.equals(Arrays.asList(1, 2, 3), Arrays.asList(3, 2, 1)) // returns true
     * CollectionUtils.equals(Arrays.asList(1, 2), Arrays.asList(1, 2, 3))     // returns false
     * }</pre>
     *
     * @param one     the first collection to compare, may be {@code null}
     * @param another the second collection to compare, may be {@code null}
     * @return {@code true} if the collections are considered equal, otherwise {@code false}
     */
    public static boolean equals(@Nullable Collection<?> one, @Nullable Collection<?> another) {

        if (one == another) {
            return true;
        }

        if (isEmpty(one) && isEmpty(another)) {
            return true;
        }

        if (size(one) != size(another)) {
            return false;
        }

        return one.containsAll(another);
    }

    /**
     * Adds all the elements in the specified array to the given collection.
     *
     * <p>
     * If the provided collection is null or empty, no elements are added, and 0 is returned.
     * If the array is null or has no elements, 0 is also returned.
     * </p>
     *
     * @param collection the collection to which elements are to be added, may be null or empty
     * @param values     the array of elements to add, may be null
     * @param <T>        the type of elements in the collection and array
     * @return the number of elements successfully added to the collection
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Collection<String> collection = new ArrayList<>();
     * int count1 = CollectionUtils.addAll(collection, "a", "b", "c"); // returns 3
     *
     * int count2 = CollectionUtils.addAll(null, "a", "b"); // returns 0
     *
     * int count3 = CollectionUtils.addAll(collection, (String[]) null); // returns 0
     *
     * Collection<String> emptyCollection = Collections.emptyList();
     * int count4 = CollectionUtils.addAll(emptyCollection, "x"); // returns 0
     * }</pre>
     */
    public static <T> int addAll(@Nullable Collection<T> collection, T... values) {

        if (collection == null) {
            return 0;
        }

        int size = length(values);

        if (size < 1) {
            return 0;
        }

        int effectedCount = 0;
        for (int i = 0; i < size; i++) {
            if (collection.add(values[i])) {
                effectedCount++;
            }
        }

        return effectedCount;
    }

    /**
     * Retrieves the first element from the given collection.
     *
     * <p>
     * If the collection is null or empty, this method returns {@code null}.
     * If the collection is an instance of a list, it delegates to {@link ListUtils#first(List)}.
     * Otherwise, it retrieves the first element using the iterator obtained from the collection.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Collection<String> list = Arrays.asList("a", "b", "c");
     * String result1 = CollectionUtils.first(list); // returns "a"
     *
     * Collection<String> empty = Collections.emptyList();
     * String result2 = CollectionUtils.first(empty); // returns null
     *
     * Collection<String> nullCollection = null;
     * String result3 = CollectionUtils.first(nullCollection); // returns null
     * }</pre>
     *
     * @param values the collection to retrieve the first element from, may be null
     * @param <T>    the type of elements in the collection
     * @return the first element if available; otherwise, null
     */
    @Nullable
    public static <T> T first(@Nullable Collection<T> values) {
        if (isEmpty(values)) {
            return null;
        }
        if (isList(values)) {
            return ListUtils.first((List<T>) values);
        } else {
            return first((Iterable<T>) values);
        }
    }

    /**
     * Retrieves the first element from the given {@link Iterable}.
     *
     * <p>
     * If the iterable is null or has no elements, this method returns {@code null}.
     * </p>
     *
     * @param values the iterable to retrieve the first element from, may be null
     * @param <T>    the type of elements in the iterable
     * @return the first element if available; otherwise, null
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Get the first element from a non-empty iterable
     * Iterable<String> iterable1 = Arrays.asList("apple", "banana");
     * String result1 = CollectionUtils.first(iterable1); // returns "apple"
     *
     * // Get the first element from an empty iterable
     * Iterable<String> iterable2 = Collections.emptyList();
     * String result2 = CollectionUtils.first(iterable2); // returns null
     *
     * // Get the first element from a null iterable
     * String result3 = CollectionUtils.first(null); // returns null
     * }</pre>
     */
    @Nullable
    public static <T> T first(@Nullable Iterable<T> values) {
        return values == null ? null : first(values.iterator());
    }

    /**
     * Retrieves the first element from the given {@link Iterator}.
     *
     * <p>
     * If the iterator is null or has no elements, this method returns {@code null}.
     * </p>
     *
     * @param values the iterator to retrieve the first element from, may be null
     * @param <T>    the type of elements in the iterator
     * @return the first element if available; otherwise, null
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Get the first element from a non-empty iterator
     * Iterator<String> iterator1 = Arrays.asList("apple", "banana").iterator();
     * String result1 = CollectionUtils.first(iterator1); // returns "apple"
     *
     * // Get the first element from an empty iterator
     * Iterator<String> iterator2 = Collections.emptyIterator();
     * String result2 = CollectionUtils.first(iterator2); // returns null
     *
     * // Get the first element from a null iterator
     * String result3 = CollectionUtils.first(null); // returns null
     * }</pre>
     */
    @Nullable
    public static <T> T first(@Nullable Iterator<T> values) {
        if (values == null || !values.hasNext()) {
            return null;
        }
        return values.next();
    }

    /**
     * Returns an empty, immutable {@link Queue} that throws {@link UnsupportedOperationException}
     * for all modification operations.
     *
     * <p>
     * This method returns a singleton instance of an empty queue. Any attempt to modify the returned queue
     * will result in an {@link UnsupportedOperationException}. It is useful as a placeholder or default value
     * where an empty queue is needed without creating a new instance every time.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Queue<String> emptyQueue = CollectionUtils.emptyQueue();
     *
     * System.out.println(emptyQueue.isEmpty()); // true
     * System.out.println(emptyQueue.size());    // 0
     *
     * try {
     *     emptyQueue.add("test");
     * } catch (UnsupportedOperationException e) {
     *     System.out.println("Modification not allowed"); // This block will execute
     * }
     * }</pre>
     *
     * @param <T> the type of elements held in the returned queue
     * @return an empty and immutable queue instance
     * @see EmptyDeque
     */
    @Nonnull
    @Immutable
    public static <T> Queue<T> emptyQueue() {
        return QueueUtils.emptyQueue();
    }

    /**
     * Returns an empty, immutable {@link Deque} instance that throws {@link UnsupportedOperationException}
     * for all modification operations.
     *
     * <p>
     * This method returns a singleton instance of an empty deque. Any attempt to modify the returned deque
     * will result in an {@link UnsupportedOperationException}. It is useful as a placeholder or default value
     * where an empty deque is needed without creating a new instance every time.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Deque<String> emptyDeque = CollectionUtils.emptyDeque();
     *
     * System.out.println(emptyDeque.isEmpty()); // true
     * System.out.println(emptyDeque.size());    // 0
     *
     * try {
     *     emptyDeque.add("test");
     * } catch (UnsupportedOperationException e) {
     *     System.out.println("Modification not allowed"); // This block will execute
     * }
     * }</pre>
     *
     * @param <T> the type of elements held in this deque
     * @return an empty and immutable deque instance
     * @see EmptyDeque
     */
    @Nonnull
    @Immutable
    public static <T> Deque<T> emptyDeque() {
        return QueueUtils.emptyDeque();
    }

    private CollectionUtils() {
    }
}
