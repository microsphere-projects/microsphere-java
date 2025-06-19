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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.microsphere.collection.CollectionUtils.size;
import static io.microsphere.collection.CollectionUtils.toIterable;
import static io.microsphere.collection.CollectionUtils.toIterator;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

/**
 * The utilities class for Java {@link List}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Lists
 * @see List
 * @since 1.0.0
 */
public abstract class ListUtils implements Utils {

    public static boolean isList(Object values) {
        return values instanceof List;
    }

    /**
     * Get the first element of the specified {@link List}
     *
     * @param list the specified {@link List}
     * @param <E>  the type of element
     * @return the first one if found, or <code>null</code>
     */
    public static <E> E first(List<E> list) {
        return size(list) < 1 ? null : list.get(0);
    }

    /**
     * Get the last element of the specified {@link List}
     *
     * @param list the specified {@link List}
     * @param <E>  the type of element
     * @return the last one if found, or <code>null</code>
     */
    public static <E> E last(List<E> list) {
        int size = size(list);
        return size < 1 ? null : list.get(size - 1);
    }

    /**
     * Create an immutable {@link List} from the specified elements.
     *
     * <p>This method is equivalent to calling {@link #ofList(Object...)}.</p>
     *
     * @param elements the elements to be added to the list
     * @param <E>      the type of elements in the list
     * @return an immutable list containing the specified elements
     * @see #ofList(Object[])
     */
    public static <E> List<E> of(E... elements) {
        return ofList(elements);
    }

    /**
     * Create a {@link List} from the specified array
     *
     * @param elements
     * @param <E>
     * @return
     * @see {@link Lists#ofList} as recommended
     */
    public static <E> List<E> ofList(E... elements) {
        if (isEmpty(elements)) {
            return emptyList();
        }
        return unmodifiableList(asList(elements));
    }

    /**
     * Create an immutable {@link List} from the specified {@link Iterable}.
     *
     * <p>If the given {@link Iterable} is already a non-null {@link List},
     * it will be returned as unmodifiable. Otherwise, the elements will be copied
     * from the {@link Iterator} obtained via the iterable.</p>
     *
     * @param iterable The {@link Iterable} to convert.
     * @param <E>      The type of elements in the iterable.
     * @return An immutable list containing all elements from the iterable.
     * @see #ofList(Iterator)
     */
    public static <E> List<E> ofList(Iterable<E> iterable) {
        if (iterable == null) {
            return emptyList();
        } else if (isList(iterable)) {
            return unmodifiableList((List) iterable);
        } else {
            return ofList(iterable.iterator());
        }
    }

    /**
     * Create an immutable {@link List} from the specified {@link Enumeration}.
     *
     * <p>If the given {@link Enumeration} is {@code null}, an empty list will be returned.
     * Otherwise, the elements will be copied into a new list.</p>
     *
     * @param enumeration The {@link Enumeration} to convert.
     * @param <E>         The type of elements in the enumeration.
     * @return An immutable list containing all elements from the enumeration.
     * @see #ofList(Iterator)
     */
    public static <E> List<E> ofList(Enumeration<E> enumeration) {
        return ofList(toIterator(enumeration));
    }

    /**
     * Create an immutable {@link List} from the specified {@link Iterator}.
     *
     * <p>If the given {@link Iterator} is {@code null}, an empty list will be returned.
     * Otherwise, the elements will be copied into a new linked list.</p>
     *
     * @param iterator The {@link Iterator} to convert.
     * @param <E>      The type of elements in the iterator.
     * @return An immutable list containing all elements from the iterator.
     * @see #ofList(Iterable)
     * @see #newLinkedList(Iterator)
     */
    public static <E> List<E> ofList(Iterator<E> iterator) {
        if (iterator == null) {
            return emptyList();
        }
        List<E> list = newLinkedList();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return unmodifiableList(list);
    }

    /**
     * Creates a new empty {@link ArrayList} instance.
     *
     * @param <E> the type of elements in the list
     * @return a new, empty {@link ArrayList}
     */
    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<>();
    }

    /**
     * Creates a new empty {@link ArrayList} instance with the specified initial capacity.
     *
     * @param size the initial capacity of the returned array list
     * @param <E>  the type of elements in the list
     * @return a new, empty {@link ArrayList} with the specified initial capacity
     * @throws IllegalArgumentException if the specified size is negative
     */
    public static <E> ArrayList<E> newArrayList(int size) {
        return new ArrayList<>(size);
    }

    /**
     * Creates a new {@link LinkedList} instance from the specified {@link Enumeration}.
     *
     * <p>This method converts the given {@link Enumeration} into an {@link Iterable}
     * using {@link CollectionUtils#toIterable(Enumeration)} and then delegates to
     * {@link #newLinkedList(Iterable)} to construct the list.</p>
     *
     * @param values The enumeration to convert.
     * @param <E>    The type of elements in the enumeration.
     * @return A new linked list containing all elements from the enumeration.
     * @see #newLinkedList(Iterable)
     * @see CollectionUtils#toIterable(Enumeration)
     */
    public static <E> LinkedList<E> newArrayList(Enumeration<E> values) {
        return newLinkedList(toIterable(values));
    }

    /**
     * Creates a new {@link ArrayList} instance containing all elements from the specified {@link Iterable}.
     *
     * <p>If the given {@link Iterable} is {@code null}, an empty list will be returned.
     * Otherwise, the elements will be iterated and added to a new array list.</p>
     *
     * @param values The iterable to convert.
     * @param <E>    The type of elements in the iterable.
     * @return A new array list containing all elements from the iterable.
     * @see #newArrayList(Iterator)
     */
    public static <E> ArrayList<E> newArrayList(Iterable<E> values) {
        return newArrayList(values.iterator());
    }

    /**
     * Creates a new {@link ArrayList} instance containing all elements from the specified {@link Iterator}.
     *
     * <p>If the given {@link Iterator} is {@code null}, an empty list will be returned.
     * Otherwise, the elements will be iterated and added to a new array list.</p>
     *
     * @param iterator The iterator to convert.
     * @param <E>      The type of elements in the iterator.
     * @return A new array list containing all elements from the iterator.
     * @see #newArrayList(Iterable)
     */
    public static <E> ArrayList<E> newArrayList(Iterator<E> iterator) {
        ArrayList<E> list = newArrayList();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    /**
     * Creates a new empty {@link LinkedList} instance.
     *
     * @param <E> the type of elements in the list
     * @return a new, empty {@link LinkedList}
     */
    public static <E> LinkedList<E> newLinkedList() {
        return new LinkedList<>();
    }

    /**
     * Creates a new {@link LinkedList} instance from the specified {@link Enumeration}.
     *
     * <p>This method converts the given {@link Enumeration} into an {@link Iterable}
     * using {@link CollectionUtils#toIterable(Enumeration)} and then delegates to
     * {@link #newLinkedList(Iterable)} to construct the list.</p>
     *
     * @param values The enumeration to convert.
     * @param <E>    The type of elements in the enumeration.
     * @return A new linked list containing all elements from the enumeration.
     * @see #newLinkedList(Iterable)
     * @see CollectionUtils#toIterable(Enumeration)
     */
    public static <E> LinkedList<E> newLinkedList(Enumeration<E> values) {
        return newLinkedList(toIterable(values));
    }

    /**
     * Creates a new {@link LinkedList} instance containing all elements from the specified {@link Iterable}.
     *
     * <p>If the given {@link Iterable} is {@code null}, an empty linked list will be returned.
     * Otherwise, the elements will be iterated and added to a new linked list.</p>
     *
     * @param values The iterable to convert.
     * @param <E>    The type of elements in the iterable.
     * @return A new linked list containing all elements from the iterable.
     * @see #newLinkedList(Iterator)
     */
    public static <E> LinkedList<E> newLinkedList(Iterable<E> values) {
        return newLinkedList(values.iterator());
    }

    /**
     * Creates a new {@link LinkedList} instance containing all elements from the specified {@link Iterator}.
     *
     * <p>If the given {@link Iterator} is {@code null}, an empty linked list will be returned.
     * Otherwise, the elements will be iterated and added to a new linked list.</p>
     *
     * @param iterator The iterator to convert.
     * @param <E>      The type of elements in the iterator.
     * @return A new linked list containing all elements from the iterator.
     */
    public static <E> LinkedList<E> newLinkedList(Iterator<E> iterator) {
        LinkedList<E> list = newLinkedList();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    /**
     * Performs the given action for each element of the specified list, 
     * providing both the index and the element.
     *
     * @param values the list to iterate over
     * @param indexedElementConsumer the action to perform on each element, 
     *                               taking the index and element as arguments
     * @param <T> the type of elements in the list
     */
    public static <T> void forEach(List<T> values, BiConsumer<Integer, T> indexedElementConsumer) {
        int length = size(values);
        for (int i = 0; i < length; i++) {
            T value = values.get(i);
            indexedElementConsumer.accept(i, value);
        }
    }

    /**
     * Performs the given action for each element of the specified list.
     *
     * <p>This method wraps the provided {@link Consumer} and delegates to
     * {@link #forEach(List, BiConsumer)} by ignoring the index parameter.</p>
     *
     * @param values   the list to iterate over
     * @param consumer the action to perform on each element
     * @param <T>      the type of elements in the list
     */
    public static <T> void forEach(List<T> values, Consumer<T> consumer) {
        forEach(values, (i, e) -> consumer.accept(e));
    }

    private ListUtils() {
    }
}
