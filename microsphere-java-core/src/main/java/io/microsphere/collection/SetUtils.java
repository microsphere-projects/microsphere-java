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
import io.microsphere.annotation.Nullable;
import io.microsphere.util.Utils;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import static io.microsphere.collection.CollectionUtils.size;
import static io.microsphere.collection.MapUtils.FIXED_LOAD_FACTOR;
import static io.microsphere.util.ArrayUtils.length;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableSet;

/**
 * The utilities class for Java {@link Set}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Sets
 * @since 1.0.0
 */
public abstract class SetUtils implements Utils {

    /**
     * Determine whether the specified {@link Iterable} is an instance of {@link Set}.
     *
     * @param elements the elements to check, may be null or empty
     * @return true if the given elements are an instance of {@link Set}, false otherwise
     */
    public static boolean isSet(@Nullable Iterable<?> elements) {
        return elements instanceof Set;
    }

    /**
     * Convert to multiple elements to be {@link LinkedHashSet}
     *
     * @param elements one or more elements
     * @param <E>      the type of <code>elements</code>
     * @return read-only {@link Set}
     */
    public static <E> Set<E> of(E... elements) {
        return ofSet(elements);
    }

    /**
     * Convert to multiple elements to be {@link LinkedHashSet}
     *
     * @param elements one or more elements
     * @param <E>      the type of <code>elements</code>
     * @return read-only {@link Set}
     */
    public static <E> Set<E> ofSet(E... elements) {
        int size = length(elements);
        if (size < 1) {
            return emptySet();
        } else if (size == 1) {
            return singleton(elements[0]);
        }

        Set<E> set = new LinkedHashSet<>(size, FIXED_LOAD_FACTOR);

        for (int i = 0; i < size; i++) {
            set.add(elements[i]);
        }
        return unmodifiableSet(set);
    }

    /**
     * Build a read-only {@link Set} from the given {@lin Enumeration} elements
     *
     * @param elements one or more elements
     * @param <E>      the type of <code>elements</code>
     * @return non-null read-only {@link Set}
     */
    @Nonnull
    public static <E> Set<E> ofSet(Enumeration<E> elements) {
        if (elements == null || !elements.hasMoreElements()) {
            return emptySet();
        }

        Set<E> set = newLinkedHashSet();
        while (elements.hasMoreElements()) {
            set.add(elements.nextElement());
        }

        return unmodifiableSet(set);
    }

    /**
     * Convert to multiple elements to be {@link LinkedHashSet}
     *
     * @param elements one or more elements
     * @param <E>      the type of <code>elements</code>
     * @return read-only {@link Set}
     */
    public static <E> Set<E> ofSet(Iterable<E> elements) {
        if (elements == null) {
            return emptySet();
        }
        if (elements instanceof Collection) {
            return ofSet((Collection) elements);
        }
        return unmodifiableSet(newLinkedHashSet(elements));
    }

    /**
     * Convert the provided {@link Collection} to an unmodifiable {@link Set}.
     * <p>
     * This method essentially converts the given collection into a set and returns it as an unmodifiable view.
     * If the input collection is null or empty, an empty set is returned.
     * </p>
     *
     * @param elements the collection to convert
     * @param <T>      the type of elements in the collection
     * @return an unmodifiable {@link Set} containing all elements from the provided collection
     */
    public static <T> Set<T> ofSet(Collection<T> elements) {
        return ofSet(elements, (T[]) null);
    }

    /**
     * Converts the provided {@link Collection} and additional elements into an unmodifiable {@link Set}.
     * <p>
     * This method combines the given collection and varargs elements into a single set,
     * ensuring uniqueness, and returns it as an unmodifiable view. If both the collection and varargs are empty,
     * an empty set is returned.
     * </p>
     *
     * @param elements the primary collection to convert, may be null or empty
     * @param others   additional elements to include in the set
     * @param <T>      the type of elements in the collection and varargs
     * @return an unmodifiable {@link Set} containing all unique elements from the collection and varargs
     */
    public static <T> Set<T> ofSet(Collection<T> elements, T... others) {
        int valuesSize = size(elements);

        if (valuesSize < 1) {
            return of(others);
        }

        int othersSize = length(others);

        int size = valuesSize + othersSize;

        Set<T> set = newLinkedHashSet(size, FIXED_LOAD_FACTOR);
        // add elements
        set.addAll(elements);

        // add others
        for (int i = 0; i < othersSize; i++) {
            set.add(others[i]);
        }

        return unmodifiableSet(set);
    }

    /**
     * Creates a new {@link HashSet} containing all elements from the provided {@link Iterable}.
     * <p>
     * This method iterates through the given iterable and adds each element to the newly created set.
     * If the input is null or empty, a new empty set will still be returned.
     * </p>
     *
     * @param elements the iterable of elements to add to the set, may be null or empty
     * @param <E>      the type of elements in the iterable
     * @return a new {@link HashSet} containing all unique elements from the provided iterable
     */
    public static <E> Set<E> newHashSet(Iterable<E> elements) {
        Set<E> set = newHashSet();
        for (E value : elements) {
            set.add(value);
        }
        return set;
    }

    /**
     * Creates a new {@link HashSet} containing all elements from the provided {@link Collection}.
     * <p>
     * This method delegates to the {@link HashSet} constructor that accepts a collection,
     * ensuring all elements from the input collection are included in the resulting set.
     * </p>
     *
     * @param elements the collection of elements to add to the set, may be null or empty
     * @param <E>      the type of elements in the collection
     * @return a new {@link HashSet} containing all unique elements from the provided collection
     */
    public static <E> Set<E> newHashSet(Collection<E> elements) {
        return new HashSet(elements);
    }

    /**
     * Creates a new {@link HashSet} containing all elements from the provided varargs array.
     * <p>
     * This method adds each element from the input array to the newly created set.
     * If the input array is null or empty, a new empty set will still be returned.
     * </p>
     *
     * @param elements the array of elements to add to the set, may be null or empty
     * @param <E>      the type of elements in the array
     * @return a new {@link HashSet} containing all unique elements from the provided array
     */
    public static <E> Set<E> newHashSet(E... elements) {
        int length = length(elements);
        Set<E> set = newHashSet(length);
        for (int i = 0; i < length; i++) {
            set.add(elements[i]);
        }
        return set;
    }

    /**
     * Creates a new, empty {@link HashSet} with the default initial capacity and load factor.
     * <p>
     * This method provides a convenient way to instantiate an empty HashSet instance.
     * The returned set is not thread-safe and allows null elements.
     * </p>
     *
     * @param <E> the type of elements in the set
     * @return a new empty {@link HashSet}
     */
    public static <E> Set<E> newHashSet() {
        return new HashSet<>();
    }

    /**
     * Creates a new, empty {@link HashSet} with the specified initial capacity and default load factor.
     * <p>
     * This method provides a convenient way to instantiate an empty HashSet instance with the given initial capacity.
     * The returned set is not thread-safe and allows null elements.
     * </p>
     *
     * @param initialCapacity the initial capacity of the returned set
     * @param <E>             the type of elements in the set
     * @return a new empty {@link HashSet} with the specified initial capacity
     */
    public static <E> Set<E> newHashSet(int initialCapacity) {
        return new HashSet<>(initialCapacity);
    }

    /**
     * Creates a new, empty {@link HashSet} with the specified initial capacity and load factor.
     * <p>
     * This method provides a convenient way to instantiate an empty HashSet instance with the given initial capacity
     * and load factor. The returned set is not thread-safe and allows null elements.
     * </p>
     *
     * @param initialCapacity the initial capacity of the returned set
     * @param loadFactor      the load factor of the returned set
     * @param <E>             the type of elements in the set
     * @return a new empty {@link HashSet} with the specified initial capacity and load factor
     */
    public static <E> Set<E> newHashSet(int initialCapacity, float loadFactor) {
        return new HashSet<>(initialCapacity, loadFactor);
    }

    /**
     * Creates a new {@link LinkedHashSet} containing all elements from the provided {@link Iterable}.
     * <p>
     * This method iterates through the given iterable and adds each element to the newly created set,
     * preserving the insertion order. If the input is null or empty, a new empty set will still be returned.
     * </p>
     *
     * @param elements the iterable of elements to add to the set, may be null or empty
     * @param <E>      the type of elements in the iterable
     * @return a new {@link LinkedHashSet} containing all unique elements from the provided iterable
     */
    public static <E> Set<E> newLinkedHashSet(Iterable<E> elements) {
        return newLinkedHashSet(elements.iterator());
    }

    /**
     * Creates a new {@link LinkedHashSet} containing all elements from the provided {@link Iterator}.
     * <p>
     * This method iterates through the given iterator and adds each element to the newly created set,
     * preserving the insertion order. If the input iterator is null or has no elements, an empty set
     * will still be returned.
     * </p>
     *
     * @param elements the iterator of elements to add to the set, may be null or empty
     * @param <E>      the type of elements in the iterator
     * @return a new {@link LinkedHashSet} containing all unique elements from the provided iterator
     */
    public static <E> Set<E> newLinkedHashSet(Iterator<E> elements) {
        Set<E> set = newLinkedHashSet();
        while (elements.hasNext()) {
            E value = elements.next();
            set.add(value);
        }
        return set;
    }

    /**
     * Creates a new {@link LinkedHashSet} containing all elements from the provided {@link Collection}.
     * <p>
     * This method delegates to the {@link LinkedHashSet} constructor that accepts a collection,
     * ensuring all elements from the input collection are included in the resulting set.
     * </p>
     *
     * @param elements the collection of elements to add to the set, may be null or empty
     * @param <E>      the type of elements in the collection
     * @return a new {@link LinkedHashSet} containing all unique elements from the provided collection
     */
    public static <E> Set<E> newLinkedHashSet(Collection<E> elements) {
        return new LinkedHashSet(elements);
    }

    /**
     * Creates a new {@link LinkedHashSet} containing all elements from the provided varargs array.
     * <p>
     * This method adds each element from the input array to the newly created set, preserving insertion order.
     * If the input array is null or empty, a new empty set will still be returned.
     * </p>
     *
     * @param elements the array of elements to add to the set, may be null or empty
     * @param <E>      the type of elements in the array
     * @return a new {@link LinkedHashSet} containing all unique elements from the provided array
     */
    public static <E> Set<E> newLinkedHashSet(E... elements) {
        int length = length(elements);
        Set<E> set = newLinkedHashSet(length);
        for (int i = 0; i < length; i++) {
            set.add(elements[i]);
        }
        return set;
    }

    /**
     * Creates a new, empty {@link LinkedHashSet} with the default initial capacity and load factor.
     * <p>
     * This method provides a convenient way to instantiate an empty LinkedHashSet instance.
     * The returned set is not thread-safe and allows null elements.
     * </p>
     *
     * @param <E> the type of elements in the set
     * @return a new empty {@link LinkedHashSet}
     */
    public static <E> Set<E> newLinkedHashSet() {
        return new LinkedHashSet<>();
    }

    /**
     * Creates a new, empty {@link LinkedHashSet} with the specified initial capacity and default load factor.
     * <p>
     * This method provides a convenient way to instantiate an empty LinkedHashSet instance with the given initial capacity.
     * The returned set is not thread-safe and allows null elements.
     * </p>
     *
     * @param initialCapacity the initial capacity of the returned set
     * @param <E>             the type of elements in the set
     * @return a new empty {@link LinkedHashSet} with the specified initial capacity
     */
    public static <E> Set<E> newLinkedHashSet(int initialCapacity) {
        return new LinkedHashSet<>(initialCapacity);
    }

    /**
     * Creates a new, empty {@link LinkedHashSet} with the specified initial capacity and load factor.
     * <p>
     * This method provides a convenient way to instantiate an empty LinkedHashSet instance with the given initial capacity
     * and load factor. The returned set is not thread-safe and allows null elements.
     * </p>
     *
     * @param initialCapacity the initial capacity of the returned set
     * @param loadFactor      the load factor of the returned set
     * @param <E>             the type of elements in the set
     * @return a new empty {@link LinkedHashSet} with the specified initial capacity and load factor
     */
    public static <E> Set<E> newLinkedHashSet(int initialCapacity, float loadFactor) {
        return new LinkedHashSet<>(initialCapacity, loadFactor);
    }

    private SetUtils() {
    }
}
