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
import static io.microsphere.util.ClassUtils.isAssignableFrom;
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
     * Checks whether the specified {@link Iterable} is an instance of {@link Set}.
     *
     * <p>This method returns {@code true} if the provided iterable is a {@link Set}, ensuring that
     * operations like duplicate elimination and order independence are already handled by the implementation.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Set<String> set = new HashSet<>();
     * set.add("apple");
     * set.add("banana");
     *
     * boolean result1 = SetUtils.isSet(set); // returns true
     *
     * List<String> list = Arrays.asList("apple", "banana");
     * boolean result2 = SetUtils.isSet(list); // returns false
     * }</pre>
     *
     * @param values the values to check, may be null
     * @return {@code true} if the given iterable is a {@link Set}; otherwise, {@code false}
     */
    public static boolean isSet(@Nullable Object values) {
        return values instanceof Set;
    }

    /**
     * Checks whether the specified {@link Class type} is assignable from {@link Set} interface.
     *
     * <p>This method returns {@code true} if the provided type is a {@link Set} interface or its sub-interface,
     * or the implementation class of {@link Set} interface.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean result1 = SetUtils.isSet(Set.class);        // returns true
     * boolean result2 = SetUtils.isSet(HashSet.class);    // returns true
     * boolean result3 = SetUtils.isSet(List.class);       // returns false
     * boolean result4 = SetUtils.isSet(String.class);     // returns false
     * }</pre>
     *
     * @param type the {@link Class type} to check, may be null
     * @return {@code true} if the given type is a {@link Set} interface or its sub-interface,
     * or the implementation class of {@link Set} interface.
     */
    public static boolean isSet(@Nullable Class<?> type) {
        return isAssignableFrom(Set.class, type);
    }

    /**
     * Creates an unmodifiable {@link Set} from the given varargs array of elements.
     *
     * <p>This method converts the provided array into a set to eliminate duplicates,
     * and returns it as an unmodifiable view. If the input array is null or empty,
     * an empty set is returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Set<String> set1 = SetUtils.of("apple", "banana", "apple");
     * // returns an unmodifiable set containing ["apple", "banana"]
     *
     * String[] fruits = {"apple", "banana"};
     * Set<String> set2 = SetUtils.of(fruits);
     * // returns an unmodifiable set containing ["apple", "banana"]
     *
     * Set<String> emptySet = SetUtils.of();
     * // returns an empty unmodifiable set
     * }</pre>
     *
     * @param elements the array of elements to add to the set, may be null or empty
     * @param <E>      the type of elements in the array
     * @return an unmodifiable {@link Set} containing all unique elements from the provided array
     */
    @Nonnull
    public static <E> Set<E> of(E... elements) {
        return ofSet(elements);
    }

    /**
     * Creates an unmodifiable {@link Set} from the given varargs array of elements.
     *
     * <p>This method converts the provided array into a set to eliminate duplicates,
     * and returns it as an unmodifiable view. If the input array is null or empty,
     * an empty set is returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Set<String> set1 = SetUtils.ofSet("apple", "banana", "apple");
     * // returns an unmodifiable set containing ["apple", "banana"]
     *
     * String[] fruits = {"apple", "banana"};
     * Set<String> set2 = SetUtils.ofSet(fruits);
     * // returns an unmodifiable set containing ["apple", "banana"]
     *
     * Set<String> emptySet = SetUtils.ofSet();
     * // returns an empty unmodifiable set
     * }</pre>
     *
     * @param elements the array of elements to add to the set, may be null or empty
     * @param <E>      the type of elements in the array
     * @return an unmodifiable {@link Set} containing all unique elements from the provided array
     */
    @Nonnull
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
     * Creates an unmodifiable {@link Set} from the given {@link Enumeration}.
     *
     * <p>This method iterates through the provided enumeration and adds each element to a new set,
     * ensuring uniqueness, and returns it as an unmodifiable view. If the enumeration is null or has no elements,
     * an empty set is returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Vector<String> vector = new Vector<>();
     * vector.add("apple");
     * vector.add("banana");
     * Enumeration<String> enumeration = vector.elements();
     *
     * Set<String> set = SetUtils.ofSet(enumeration);
     * // returns an unmodifiable set containing ["apple", "banana"]
     *
     * Set<String> emptySet = SetUtils.ofSet(null);
     * // returns an empty unmodifiable set
     * }</pre>
     *
     * @param elements the enumeration of elements to add to the set, may be null or empty
     * @param <E>      the type of elements in the enumeration
     * @return an unmodifiable {@link Set} containing all unique elements from the provided enumeration
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
     * Creates an unmodifiable {@link Set} from the given {@link Iterable}.
     *
     * <p>This method iterates through the provided iterable and adds each element to a new set,
     * ensuring uniqueness, and returns it as an unmodifiable view. If the iterable is null or empty,
     * an empty set is returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<String> list = Arrays.asList("apple", "banana", "apple");
     * Set<String> set1 = SetUtils.ofSet(list);
     * // returns an unmodifiable set containing ["apple", "banana"]
     *
     * Set<String> emptySet = SetUtils.ofSet(null);
     * // returns an empty unmodifiable set
     * }</pre>
     *
     * @param elements the iterable of elements to add to the set, may be null or empty
     * @param <E>      the type of elements in the iterable
     * @return an unmodifiable {@link Set} containing all unique elements from the provided iterable
     */
    @Nonnull
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
     * Creates an unmodifiable {@link Set} from the given {@link Collection}.
     *
     * <p>This method adds all elements from the provided collection to a new set, ensuring uniqueness,
     * and returns it as an unmodifiable view. If the collection is null or empty, an empty set is returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<String> list = Arrays.asList("apple", "banana", "apple");
     * Set<String> set1 = SetUtils.ofSet(list);
     * // returns an unmodifiable set containing ["apple", "banana"]
     *
     * Set<String> emptySet = SetUtils.ofSet(null);
     * // returns an empty unmodifiable set
     * }</pre>
     *
     * @param elements the collection of elements to add to the set, may be null or empty
     * @param <T>      the type of elements in the collection
     * @return an unmodifiable {@link Set} containing all unique elements from the provided collection
     */
    @Nonnull
    public static <T> Set<T> ofSet(Collection<T> elements) {
        return ofSet(elements, (T[]) null);
    }

    /**
     * Creates an unmodifiable {@link Set} from the given {@link Collection} and additional varargs elements.
     *
     * <p>This method combines all elements from the provided collection and the varargs array into a new set,
     * ensuring uniqueness, and returns it as an unmodifiable view. If both the collection and varargs array are null or empty,
     * an empty set is returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<String> list = Arrays.asList("apple", "banana");
     * Set<String> set1 = SetUtils.ofSet(list, "orange", "grape");
     * // returns an unmodifiable set containing ["apple", "banana", "orange", "grape"]
     *
     * Set<String> set2 = SetUtils.ofSet(null, "apple", "banana");
     * // returns an unmodifiable set containing ["apple", "banana"]
     *
     * Set<String> emptySet = SetUtils.ofSet(Collections.emptyList(), (String[]) null);
     * // returns an empty unmodifiable set
     * }</pre>
     *
     * @param elements the collection of elements to add to the set, may be null or empty
     * @param others   the additional elements to include in the set, may be null or empty
     * @param <T>      the type of elements in the collection and varargs
     * @return an unmodifiable {@link Set} containing all unique elements from the provided collection and varargs
     */
    @Nonnull
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
     *
     * <p>This method iterates through the given iterable and adds each element to the newly created set,
     * ensuring uniqueness. If the input iterable is null or empty, a new empty set will still be returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<String> list = Arrays.asList("apple", "banana", "apple");
     * Set<String> set = SetUtils.newHashSet(list);
     * // returns a hash set containing ["apple", "banana"]
     *
     * Set<String> emptySet = SetUtils.newHashSet(null);
     * // returns a new empty hash set
     * }</pre>
     *
     * @param elements the iterable of elements to add to the set, may be null or empty
     * @param <E>      the type of elements in the iterable
     * @return a new {@link HashSet} containing all unique elements from the provided iterable
     */
    @Nonnull
    public static <E> Set<E> newHashSet(Iterable<E> elements) {
        Set<E> set = newHashSet();
        for (E value : elements) {
            set.add(value);
        }
        return set;
    }

    /**
     * Creates a new {@link HashSet} containing all elements from the provided {@link Collection}.
     *
     * <p>This method delegates to the {@link HashSet} constructor that accepts a collection,
     * ensuring all elements from the input collection are included in the resulting set.
     * The returned set is not thread-safe and allows null elements.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<String> list = Arrays.asList("apple", "banana", "apple");
     * Set<String> set = SetUtils.newHashSet(list);
     * // returns a hash set containing ["apple", "banana"]
     *
     * Set<String> emptySet = SetUtils.newHashSet(Collections.emptyList());
     * // returns a new empty hash set
     * }</pre>
     *
     * @param elements the collection of elements to add to the set, may be null or empty
     * @param <E>      the type of elements in the collection
     * @return a new {@link HashSet} containing all elements from the provided collection
     */
    @Nonnull
    public static <E> Set<E> newHashSet(Collection<E> elements) {
        return new HashSet(elements);
    }

    /**
     * Creates a new {@link HashSet} containing all elements from the provided varargs array.
     *
     * <p>This method adds each element from the input array to the newly created set,
     * ensuring uniqueness. The insertion order is not preserved as it uses {@link HashSet}.
     * If the input array is null or empty, a new empty set will still be returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Set<String> set1 = SetUtils.newHashSet("apple", "banana", "apple");
     * // returns a hash set containing ["apple", "banana"]
     *
     * String[] fruits = {"apple", "banana"};
     * Set<String> set2 = SetUtils.newHashSet(fruits);
     * // returns a hash set containing ["apple", "banana"]
     *
     * Set<String> emptySet = SetUtils.newHashSet();
     * // returns a new empty hash set
     * }</pre>
     *
     * @param elements the array of elements to add to the set, may be null or empty
     * @param <E>      the type of elements in the array
     * @return a new {@link HashSet} containing all unique elements from the provided array
     */
    @Nonnull
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
     *
     * <p>This method provides a convenient way to instantiate an empty {@link HashSet} instance.
     * The returned set is not thread-safe and allows null elements.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Set<String> set = SetUtils.newHashSet();
     * // returns a new empty hash set with default initial capacity (16) and load factor (0.75)
     * }</pre>
     *
     * @param <E> the type of elements in the set
     * @return a new empty {@link HashSet}
     */
    @Nonnull
    public static <E> Set<E> newHashSet() {
        return new HashSet<>();
    }

    /**
     * Creates a new, empty {@link HashSet} with the specified initial capacity and default load factor.
     *
     * <p>This method provides a convenient way to instantiate an empty {@link HashSet} instance
     * with the given initial capacity. The returned set is not thread-safe and allows null elements.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Set<String> set = SetUtils.newHashSet(32);
     * // returns a new empty hash set with initial capacity of 32 and default load factor (0.75)
     * }</pre>
     *
     * @param initialCapacity the initial capacity of the returned set
     * @param <E>             the type of elements in the set
     * @return a new empty {@link HashSet} with the specified initial capacity
     */
    @Nonnull
    public static <E> Set<E> newHashSet(int initialCapacity) {
        return new HashSet<>(initialCapacity);
    }

    /**
     * Creates a new, empty {@link HashSet} with the specified initial capacity and load factor.
     *
     * <p>This method provides a convenient way to instantiate an empty {@link HashSet} instance
     * with the given initial capacity and load factor. The returned set is not thread-safe and allows null elements.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Set<String> set = SetUtils.newHashSet(32, 0.5f);
     * // returns a new empty hash set with initial capacity of 32 and load factor of 0.5
     * }</pre>
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
     *
     * <p>This method iterates through the given iterable and adds each element to the newly created set,
     * preserving insertion order. If the input iterable is null or empty, an empty set will still be returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<String> list = Arrays.asList("apple", "banana", "apple");
     * Set<String> set = SetUtils.newLinkedHashSet(list);
     * // returns a linked hash set containing ["apple", "banana"]
     *
     * Set<String> emptySet = SetUtils.newLinkedHashSet(null);
     * // returns a new empty linked hash set
     * }</pre>
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
    @Nonnull
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
     *
     * <p>This method delegates to the {@link LinkedHashSet} constructor that accepts a collection,
     * ensuring all elements from the input collection are included in the resulting set while preserving insertion order.
     * The returned set is not thread-safe and allows null elements.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<String> list = Arrays.asList("apple", "banana", "apple");
     * Set<String> set = SetUtils.newLinkedHashSet(list);
     * // returns a linked hash set containing ["apple", "banana"]
     *
     * Set<String> emptySet = SetUtils.newLinkedHashSet(Collections.emptyList());
     * // returns a new empty linked hash set
     * }</pre>
     *
     * @param elements the collection of elements to add to the set, may be null or empty
     * @param <E>      the type of elements in the collection
     * @return a new {@link LinkedHashSet} containing all elements from the provided collection
     */
    @Nonnull
    public static <E> Set<E> newLinkedHashSet(Collection<E> elements) {
        return new LinkedHashSet(elements);
    }

    /**
     * Creates a new {@link LinkedHashSet} containing all elements from the provided varargs array.
     *
     * <p>This method adds each element from the input array to the newly created set,
     * ensuring uniqueness while preserving the insertion order. If the input array is null or empty,
     * a new empty set will still be returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Set<String> set1 = SetUtils.newLinkedHashSet("apple", "banana", "apple");
     * // returns a linked hash set containing ["apple", "banana"]
     *
     * String[] fruits = {"apple", "banana"};
     * Set<String> set2 = SetUtils.newLinkedHashSet(fruits);
     * // returns a linked hash set containing ["apple", "banana"]
     *
     * Set<String> emptySet = SetUtils.newLinkedHashSet();
     * // returns a new empty linked hash set
     * }</pre>
     *
     * @param elements the array of elements to add to the set, may be null or empty
     * @param <E>      the type of elements in the array
     * @return a new {@link LinkedHashSet} containing all unique elements from the provided array
     */
    @Nonnull
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
     *
     * <p>This method provides a convenient way to instantiate an empty {@link LinkedHashSet} instance.
     * The returned set is not thread-safe and allows null elements.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Set<String> set = SetUtils.newLinkedHashSet();
     * // returns a new empty linked hash set with default initial capacity (16) and load factor (0.75)
     * }</pre>
     *
     * @param <E> the type of elements in the set
     * @return a new empty {@link LinkedHashSet}
     */
    @Nonnull
    public static <E> Set<E> newLinkedHashSet() {
        return new LinkedHashSet<>();
    }

    /**
     * Creates a new, empty {@link LinkedHashSet} with the specified initial capacity and default load factor.
     *
     * <p>This method provides a convenient way to instantiate an empty {@link LinkedHashSet} instance
     * with the given initial capacity. The returned set is not thread-safe and allows null elements.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Set<String> set = SetUtils.newLinkedHashSet(32);
     * // returns a new empty linked hash set with initial capacity of 32 and default load factor (0.75)
     * }</pre>
     *
     * @param initialCapacity the initial capacity of the returned set
     * @param <E>             the type of elements in the set
     * @return a new empty {@link LinkedHashSet} with the specified initial capacity
     */
    @Nonnull
    public static <E> Set<E> newLinkedHashSet(int initialCapacity) {
        return new LinkedHashSet<>(initialCapacity);
    }

    /**
     * Creates a new, empty {@link LinkedHashSet} with the specified initial capacity and load factor.
     *
     * <p>This method provides a convenient way to instantiate an empty {@link LinkedHashSet} instance
     * with the given initial capacity and load factor. The returned set is not thread-safe and allows null elements.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Set<String> set = SetUtils.newLinkedHashSet(32, 0.5f);
     * // returns a new empty linked hash set with initial capacity of 32 and load factor of 0.5
     * }</pre>
     *
     * @param initialCapacity the initial capacity of the returned set
     * @param loadFactor      the load factor of the returned set
     * @param <E>             the type of elements in the set
     * @return a new empty {@link LinkedHashSet} with the specified initial capacity and load factor
     */
    @Nonnull
    public static <E> Set<E> newLinkedHashSet(int initialCapacity, float loadFactor) {
        return new LinkedHashSet<>(initialCapacity, loadFactor);
    }

    private SetUtils() {
    }
}
