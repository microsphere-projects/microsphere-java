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
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.Assert.assertTrue;
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
     * Retrieves the first element from the specified list.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<Integer> numbers = Arrays.asList(1, 2, 3);
     *     Integer firstNumber = first(numbers); // returns 1
     *
     *     List<String> emptyList = Collections.emptyList();
     *     String firstString = first(emptyList); // returns null
     * }</pre>
     *
     * @param list the list from which to retrieve the first element
     * @param <E>  the type of element in the list
     * @return the first element if the list is not empty, or {@code null} otherwise
     */
    @Nullable
    public static <E> E first(List<E> list) {
        return size(list) < 1 ? null : list.get(0);
    }

    /**
     * Retrieves the last element from the specified list.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<Integer> numbers = Arrays.asList(1, 2, 3);
     *     Integer lastNumber = last(numbers); // returns 3
     *
     *     List<String> emptyList = Collections.emptyList();
     *     String lastString = last(emptyList); // returns null
     * }</pre>
     *
     * @param list the list from which to retrieve the last element
     * @param <E>  the type of element in the list
     * @return the last element if the list is not empty, or {@code null} otherwise
     */
    @Nullable
    public static <E> E last(List<E> list) {
        int size = size(list);
        return size < 1 ? null : list.get(size - 1);
    }

    /**
     * Creates an immutable list from the given elements.
     *
     * <p>This method is a convenient way to create a list with a small number of elements.
     * The returned list is unmodifiable, meaning that any attempt to change its contents
     * will result in an {@link UnsupportedOperationException}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> fruits = ListUtils.of("apple", "banana", "cherry");
     *     System.out.println(fruits); // Output: [apple, banana, cherry]
     *
     *     List<Integer> numbers = ListUtils.of(1, 2, 3, 4, 5);
     *     System.out.println(numbers); // Output: [1, 2, 3, 4, 5]
     *
     *     List<String> emptyList = ListUtils.of();
     *     System.out.println(emptyList); // Output: []
     * }</pre>
     *
     * @param elements the elements to include in the list
     * @param <E>      the type of elements in the list
     * @return an immutable list containing the specified elements
     * @see #ofList(Object[]) for more details on behavior and immutability
     */
    @Nonnull
    public static <E> List<E> of(E... elements) {
        return ofList(elements);
    }

    /**
     * Creates an immutable list from the given array of elements.
     *
     * <p>This method is typically used to create a list from an array or varargs input.
     * If the provided array is empty, it returns an empty list. The returned list is
     * unmodifiable, meaning any attempt to modify it will throw an
     * {@link UnsupportedOperationException}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> fruits = ListUtils.ofList("apple", "banana", "cherry");
     *     System.out.println(fruits); // Output: [apple, banana, cherry]
     *
     *     String[] names = {};
     *     List<String> emptyList = ListUtils.ofList(names);
     *     System.out.println(emptyList); // Output: []
     * }</pre>
     *
     * @param elements the array of elements to include in the list
     * @param <E>      the type of elements in the array
     * @return an immutable list containing the specified elements
     */
    @Nonnull
    public static <E> List<E> ofList(E... elements) {
        if (isEmpty(elements)) {
            return emptyList();
        }
        return unmodifiableList(asList(elements));
    }

    /**
     * Creates an immutable list from the specified {@link Iterable}.
     *
     * <p>If the given iterable is {@code null}, an empty list will be returned.
     * If the iterable is already a list, it will be wrapped in an unmodifiable list.
     * Otherwise, the elements will be copied into a new list.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Set<String> fruits = new HashSet<>(Arrays.asList("apple", "banana", "cherry"));
     *     List<String> fruitsList = ListUtils.ofList(fruits);
     *     System.out.println(fruitsList); // Output: [apple, banana, cherry]
     *
     *     List<Integer> numbers = Arrays.asList(1, 2, 3);
     *     List<Integer> unmodifiableNumbers = ListUtils.ofList(numbers);
     *     System.out.println(unmodifiableNumbers); // Output: [1, 2, 3] - already a list, wrapped as unmodifiable
     *
     *     List<String> emptyList = ListUtils.ofList((Iterable<String>) null);
     *     System.out.println(emptyList); // Output: []
     * }</pre>
     *
     * @param iterable The iterable to convert.
     * @param <E>      The type of elements in the iterable.
     * @return An immutable list containing all elements from the iterable.
     */
    @Nonnull
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
     * Creates an immutable list from the specified {@link Enumeration}.
     *
     * <p>If the given enumeration is {@code null}, an empty list will be returned.
     * Otherwise, the elements will be copied into a new list using the underlying iterator,
     * and the result will be wrapped in an unmodifiable list.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Vector<String> vector = new Vector<>(Arrays.asList("one", "two", "three"));
     *     List<String> list = ListUtils.ofList(vector.elements());
     *     System.out.println(list); // Output: [one, two, three]
     *
     *     List<Integer> emptyList = ListUtils.ofList((Enumeration<Integer>) null);
     *     System.out.println(emptyList); // Output: []
     * }</pre>
     *
     * @param enumeration The enumeration to convert.
     * @param <E>         The type of elements in the enumeration.
     * @return An immutable list containing all elements from the enumeration.
     */
    @Nonnull
    public static <E> List<E> ofList(Enumeration<E> enumeration) {
        return ofList(toIterator(enumeration));
    }

    /**
     * Creates an immutable list from the specified {@link Iterator}.
     *
     * <p>If the given iterator is {@code null}, an empty list will be returned.
     * Otherwise, the elements will be copied into a new list using the underlying iteration,
     * and the result will be wrapped in an unmodifiable list.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> fruits = ListUtils.ofList(Arrays.asList("apple", "banana", "cherry").iterator());
     *     System.out.println(fruits); // Output: [apple, banana, cherry]
     *
     *     List<Integer> numbers = ListUtils.ofList((Iterator<Integer>) null);
     *     System.out.println(numbers); // Output: []
     * }</pre>
     *
     * @param iterator The iterator to convert.
     * @param <E>      The type of elements in the iterator.
     * @return An immutable list containing all elements from the iterator.
     */
    @Nonnull
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
     * <p>This method provides a convenient way to create an empty array list with default initial capacity.
     * The returned list is modifiable and will grow dynamically as elements are added.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> list = ListUtils.newArrayList();
     *     System.out.println(list.isEmpty()); // Output: true
     *
     *     list.add("Hello");
     *     System.out.println(list); // Output: [Hello]
     * }</pre>
     *
     * @param <E> the type of elements in the list
     * @return a new, empty {@link ArrayList} with default initial capacity
     */
    @Nonnull
    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<>();
    }

    /**
     * Creates a new {@link ArrayList} instance with the specified initial capacity.
     *
     * <p>This method provides a convenient way to create an array list with a predefined initial size,
     * which can help optimize performance when the number of elements to be added is known in advance.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> list = ListUtils.newArrayList(10);
     *     System.out.println(list.isEmpty()); // Output: true
     *
     *     list.add("Hello");
     *     System.out.println(list); // Output: [Hello]
     * }</pre>
     *
     * @param size The initial capacity of the array list.
     * @param <E>  The type of elements in the list.
     * @return A new, empty {@link ArrayList} with the specified initial capacity.
     */
    @Nonnull
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Vector<String> vector = new Vector<>(Arrays.asList("apple", "banana", "cherry"));
     *     List<String> list = ListUtils.newArrayList(vector.elements());
     *     System.out.println(list); // Output: [apple, banana, cherry]
     *
     *     List<Integer> emptyList = ListUtils.newArrayList((Enumeration<Integer>) null);
     *     System.out.println(emptyList); // Output: []
     * }</pre>
     *
     * @param values The enumeration to convert.
     * @param <E>    The type of elements in the enumeration.
     * @return A new linked list containing all elements from the enumeration.
     * @see #newLinkedList(Iterable)
     * @see CollectionUtils#toIterable(Enumeration)
     */
    @Nonnull
    public static <E> ArrayList<E> newArrayList(Enumeration<E> values) {
        return newArrayList(toIterable(values));
    }

    /**
     * Creates a new {@link ArrayList} instance containing all elements from the specified {@link Iterable}.
     *
     * <p>If the given {@link Iterable} is {@code null}, an empty array list will be returned.
     * Otherwise, the elements will be iterated and added to a new array list.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> fruits = Arrays.asList("apple", "banana", "cherry");
     *     List<String> listCopy = ListUtils.newArrayList(fruits);
     *     System.out.println(listCopy); // Output: [apple, banana, cherry]
     *
     *     Set<Integer> numbersSet = new HashSet<>(Arrays.asList(1, 2, 3));
     *     List<Integer> numbersList = ListUtils.newArrayList(numbersSet);
     *     System.out.println(numbersList); // Output: [1, 2, 3] - order may vary depending on Set implementation
     *
     *     List<String> emptyList = ListUtils.newArrayList((Iterable<String>) null);
     *     System.out.println(emptyList); // Output: []
     * }</pre>
     *
     * @param values The iterable to convert.
     * @param <E>    The type of elements in the iterable.
     * @return A new array list containing all elements from the iterable.
     * @see #newArrayList(Iterator)
     */
    @Nonnull
    public static <E> ArrayList<E> newArrayList(Iterable<E> values) {
        return newArrayList(values.iterator());
    }

    /**
     * Creates a new {@link ArrayList} instance containing all elements from the specified {@link Iterator}.
     *
     * <p>If the given {@link Iterator} is {@code null}, an empty array list will be returned.
     * Otherwise, the elements will be iterated and added to a new array list.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> fruits = Arrays.asList("apple", "banana", "cherry");
     *     List<String> listCopy = ListUtils.newArrayList(fruits.iterator());
     *     System.out.println(listCopy); // Output: [apple, banana, cherry]
     *
     *     List<Integer> numbers = ListUtils.newArrayList((Iterator<Integer>) null);
     *     System.out.println(numbers); // Output: []
     * }</pre>
     *
     * @param iterator The iterator to convert.
     * @param <E>      The type of elements in the iterator.
     * @return A new array list containing all elements from the iterator.
     */
    @Nonnull
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
     * <p>This method provides a convenient way to create an empty linked list.
     * The returned list is modifiable and allows for efficient insertions and deletions.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> list = ListUtils.newLinkedList();
     *     System.out.println(list.isEmpty()); // Output: true
     *
     *     list.add("Hello");
     *     System.out.println(list); // Output: [Hello]
     * }</pre>
     *
     * @param <E> the type of elements in the list
     * @return a new, empty {@link LinkedList}
     */
    @Nonnull
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Vector<String> vector = new Vector<>(Arrays.asList("apple", "banana", "cherry"));
     *     List<String> list = ListUtils.newLinkedList(vector.elements());
     *     System.out.println(list); // Output: [apple, banana, cherry]
     *
     *     List<Integer> emptyList = ListUtils.newLinkedList((Enumeration<Integer>) null);
     *     System.out.println(emptyList); // Output: []
     * }</pre>
     *
     * @param values The enumeration to convert.
     * @param <E>    The type of elements in the enumeration.
     * @return A new linked list containing all elements from the enumeration.
     * @see #newLinkedList(Iterable)
     * @see CollectionUtils#toIterable(Enumeration)
     */
    @Nonnull
    public static <E> LinkedList<E> newLinkedList(Enumeration<E> values) {
        return newLinkedList(toIterable(values));
    }

    /**
     * Creates a new {@link LinkedList} instance containing all elements from the specified {@link Iterable}.
     *
     * <p>If the given {@link Iterable} is {@code null}, an empty linked list will be returned.
     * Otherwise, the elements will be iterated and added to a new linked list.</p>
     *
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> fruits = Arrays.asList("apple", "banana", "cherry");
     *     List<String> listCopy = ListUtils.newLinkedList(fruits);
     *     System.out.println(listCopy); // Output: [apple, banana, cherry]
     *
     *     Set<Integer> numbersSet = new HashSet<>(Arrays.asList(1, 2, 3));
     *     List<Integer> numbersList = ListUtils.newLinkedList(numbersSet);
     *     System.out.println(numbersList); // Output: [1, 2, 3] - order may vary depending on Set implementation
     *
     *     List<String> emptyList = ListUtils.newLinkedList((Iterable<String>) null);
     *     System.out.println(emptyList); // Output: []
     * }</pre>
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> fruits = Arrays.asList("apple", "banana", "cherry");
     *     List<String> listCopy = ListUtils.newLinkedList(fruits.iterator());
     *     System.out.println(listCopy); // Output: [apple, banana, cherry]
     *
     *     List<Integer> numbers = ListUtils.newLinkedList((Iterator<Integer>) null);
     *     System.out.println(numbers); // Output: []
     * }</pre>
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
     * Creates a new {@link ArrayList} containing all elements from the specified array.
     * The resulting list is modifiable, allowing for further additions or modifications after creation.
     *
     * <p>If the given array is empty, the {@link IllegalArgumentException} will be thrown</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     String[] fruits = {"apple", "banana", "cherry"};
     *     List<String> fruitList = ListUtils.ofArrayList(fruits);
     *     System.out.println(fruitList); // Output: [apple, banana, cherry]
     *     fruitList.add("orange");       // return true
     *     System.out.println(fruitList); // Output: [apple, banana, cherry, orange]
     *
     *     Integer[] numbers = {};
     *     List<Integer> numberList = ListUtils.ofArrayList(numbers); // throws IllegalArgumentException
     *
     *     List<Integer> emptyList = ListUtils.ofLinkedList((Integer[]) null); // throws IllegalArgumentException
     *
     * }</pre>
     *
     * @param array the array to convert
     * @param <E>   the type of elements in the array
     * @return a new {@link ArrayList} containing all elements from the array
     * @throws IllegalArgumentException if the array is null or empty
     * @see #ofList(Object[]) for creating an immutable version of the list
     */
    public static <E> ArrayList<E> ofArrayList(E... array) throws IllegalArgumentException {
        int length = length(array);
        assertTrue(length > 0, () -> "The array length must be greater than 0");
        ArrayList<E> list = newArrayList(length);
        for (int i = 0; i < length; i++) {
            list.add(array[i]);
        }
        return list;
    }

    /**
     * Creates a new {@link LinkedList} containing all elements from the specified array.
     *
     * <p>This method copies the elements from the provided array into a newly created linked list,
     * allowing for efficient insertions and deletions. If the array is empty or {@code null},
     * an {@link IllegalArgumentException} will be thrown.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     String[] fruits = {"apple", "banana", "cherry"};
     *     List<String> fruitList = ListUtils.ofLinkedList(fruits);
     *     System.out.println(fruitList); // Output: [apple, banana, cherry]
     *     fruitList.add("orange");       // return true
     *     System.out.println(fruitList); // Output: [apple, banana, cherry, orange]
     *
     *     Integer[] numbers = {};
     *     List<Integer> numberList = ListUtils.ofLinkedList(numbers); // throws IllegalArgumentException
     *
     *     List<Integer> emptyList = ListUtils.ofLinkedList((Integer[]) null); // throws IllegalArgumentException
     * }</pre>
     *
     * @param array the array to convert
     * @param <E>   the type of elements in the array
     * @return a new {@link LinkedList} containing all elements from the array
     * @throws IllegalArgumentException if the array is null or empty
     * @see #ofList(Object[]) for creating an immutable version of the list
     */
    public static <E> LinkedList<E> ofLinkedList(E... array) throws IllegalArgumentException {
        int length = length(array);
        assertTrue(length > 0, () -> "The array length must be greater than 0");
        LinkedList<E> list = newLinkedList();
        for (int i = 0; i < length; i++) {
            list.add(array[i]);
        }
        return list;
    }

    /**
     * Performs the given action for each element of the specified list, providing both the index and the element.
     *
     * <p>This method iterates over the elements of the list and applies the provided bi-consumer function
     * to each element along with its index. It is useful when operations need to take into account the position
     * of the element in the list.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> fruits = Arrays.asList("apple", "banana", "cherry");
     *     ListUtils.forEach(fruits, (index, fruit) -> System.out.println("Index: " + index + ", Fruit: " + fruit));
     *     // Output:
     *     // Index: 0, Fruit: apple
     *     // Index: 1, Fruit: banana
     *     // Index: 2, Fruit: cherry
     *
     *     List<Integer> numbers = Collections.emptyList();
     *     ListUtils.forEach(numbers, (index, number) -> System.out.println("Index: " + index + ", Number: " + number));
     *     // No output, as the list is empty
     * }</pre>
     *
     * @param values                 the list to iterate over
     * @param indexedElementConsumer the action to perform on each element, taking the index and the element as arguments
     * @param <T>                    the type of elements in the list
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
     * <p>This method ignores the index of elements and directly applies the provided consumer
     * to each element in the list. It is useful when the operation does not require the element's index.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> fruits = Arrays.asList("apple", "banana", "cherry");
     *     ListUtils.forEach(fruits, fruit -> System.out.println("Fruit: " + fruit));
     *     // Output:
     *     // Fruit: apple
     *     // Fruit: banana
     *     // Fruit: cherry
     *
     *     List<Integer> numbers = Collections.emptyList();
     *     ListUtils.forEach(numbers, number -> System.out.println("Number: " + number));
     *     // No output, as the list is empty
     * }</pre>
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
