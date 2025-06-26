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
package io.microsphere.lang.function;

import io.microsphere.collection.SetUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.microsphere.collection.SetUtils.isSet;
import static io.microsphere.collection.SetUtils.ofSet;
import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.lang.function.Predicates.or;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * The utilities class for {@link Stream}
 *
 * @since 1.0.0
 */
public interface Streams {

    /**
     * Creates a sequential {@link Stream} from the given array of values.
     *
     * <p> This method is useful for converting an array into a {@link Stream} to enable
     * functional-style operations such as filtering, mapping, and collecting.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] names = {"Alice", "Bob", "Charlie"};
     * Stream<String> stream = Streams.stream(names);
     * stream.filter(name -> name.startsWith("A"))
     *       .forEach(System.out::println);  // Prints: Alice
     * }</pre>
     *
     * @param <T>    the type of the stream elements
     * @param values the array of elements to form the stream
     * @return a sequential {@link Stream} backed by the given array
     */
    static <T> Stream<T> stream(T... values) {
        return Stream.of(values);
    }

    /**
     * Creates a sequential {@link Stream} from the given {@link Iterable}.
     *
     * <p> This method is useful for converting an {@link Iterable} (like a {@link List}
     * or a {@link Set}) into a {@link Stream}, enabling functional-style operations such as filtering,
     * mapping, and collecting.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
     * Stream<String> stream = Streams.stream(names);
     * stream.filter(name -> name.startsWith("B"))
     *       .forEach(System.out::println);  // Prints: Bob
     * }</pre>
     *
     * @param <T>      the type of the stream elements
     * @param iterable the {@link Iterable} to convert into a stream
     * @return a sequential {@link Stream} backed by the given iterable
     */
    static <T> Stream<T> stream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    /**
     * Filters the elements from the provided array using the given predicate and returns a {@link Stream}.
     *
     * <p> This method creates a sequential stream from the input array, then applies the given predicate
     * to retain only the elements that match the condition.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] names = {"Alice", "Bob", "Charlie"};
     * Stream<String> filteredStream = Streams.filterStream(names, name -> name.startsWith("C"));
     * filteredStream.forEach(System.out::println);  // Prints: Charlie
     * }</pre>
     *
     * @param <T>       the type of the stream elements
     * @param values    the array of elements to be filtered
     * @param predicate the condition to apply to each element
     * @return a filtered stream containing only the elements that satisfy the predicate
     */
    static <T> Stream<T> filterStream(T[] values, Predicate<? super T> predicate) {
        Stream<T> stream = stream(values);
        return stream.filter(predicate);
    }

    /**
     * Filters the elements from the provided {@link Iterable} using the given predicate and returns a {@link Stream}.
     *
     * <p> This method creates a sequential stream from the input {@link Iterable}, then applies the given predicate
     * to retain only the elements that match the condition.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
     * Stream<String> filteredStream = Streams.filterStream(names, name -> name.startsWith("B"));
     * filteredStream.forEach(System.out::println);  // Prints: Bob
     * }</pre>
     *
     * @param <T>       the type of the stream elements
     * @param values    the {@link Iterable} to convert into a stream and apply filtering
     * @param predicate the condition to apply to each element
     * @return a filtered stream containing only the elements that satisfy the predicate
     */
    static <T, S extends Iterable<T>> Stream<T> filterStream(S values, Predicate<? super T> predicate) {
        return stream(values).filter(predicate);
    }

    /**
     * Filters the elements from the given array using the provided predicate and returns a {@link List}.
     *
     * <p> This method converts the input array into a {@link List}, then applies the given predicate
     * to retain only the elements that match the condition.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] names = {"Alice", "Bob", "Charlie"};
     * List<String> filteredList = Streams.filterList(names, name -> name.startsWith("A"));
     * System.out.println(filteredList);  // Output: [Alice]
     * }</pre>
     *
     * @param <T>       the type of the elements in the array and list
     * @param values    the array of elements to be filtered
     * @param predicate the condition to apply to each element
     * @return a list containing only the elements that satisfy the predicate
     */
    static <T> List<T> filterList(T[] values, Predicate<? super T> predicate) {
        return filterList(asList(values), predicate);
    }

    /**
     * Filters the elements from the given {@link Iterable} using the provided predicate and returns a {@link List}.
     *
     * <p> This method converts the input {@link Iterable} into a {@link Stream}, applies the given predicate
     * to retain only the elements that match the condition, and then collects the result into a new {@link List}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
     * List<String> filteredList = Streams.filterList(names, name -> name.startsWith("B"));
     * System.out.println(filteredList);  // Output: [Bob]
     * }</pre>
     *
     * @param <T>       the type of the elements in the iterable and list
     * @param values    the {@link Iterable} to convert into a stream and apply filtering
     * @param predicate the condition to apply to each element
     * @return a list containing only the elements that satisfy the predicate
     */
    static <T, S extends Iterable<T>> List<T> filterList(S values, Predicate<? super T> predicate) {
        return filterStream(values, predicate).collect(toList());
    }

    /**
     * Filters the elements from the given array using the provided predicate and returns a {@link Set}.
     *
     * <p> This method converts the input array into a new {@link Set} using {@link SetUtils#ofSet(Object[])},
     * then applies the given predicate to retain only the elements that match the condition.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] names = {"Alice", "Bob", "Charlie"};
     * Set<String> filteredSet = Streams.filterSet(names, name -> name.startsWith("A"));
     * System.out.println(filteredSet);  // Output: [Alice]
     * }</pre>
     *
     * @param <T>       the type of the elements in the array and set
     * @param values    the array of elements to be filtered
     * @param predicate the condition to apply to each element
     * @return a set containing only the elements that satisfy the predicate
     */
    static <T> Set<T> filterSet(T[] values, Predicate<? super T> predicate) {
        return filterSet(ofSet(values), predicate);
    }

    /**
     * Filters elements from the given {@link Iterable} using the provided {@link Predicate}
     * and collects the result into a new {@link Set} that maintains insertion order.
     *
     * <p>This method is particularly useful when you need to:
     * <ul>
     *     <li>Filter elements based on a condition</li>
     *     <li>Maintain the insertion order of filtered results</li>
     *     <li>Avoid duplicate elements during filtering</li>
     * </ul>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "Anna");
     * Set<String> filteredSet = Streams.filterSet(names, name -> name.startsWith("A"));
     * System.out.println(filteredSet);  // Output: [Alice, Anna]
     * }</pre>
     *
     * @param <T>       the type of the stream elements
     * @param values    the iterable containing elements to be filtered
     * @param predicate the condition used to include elements in the resulting set
     * @return a new {@link Set} containing only the elements that satisfy the predicate,
     * maintaining insertion order using a {@link LinkedHashSet}
     */
    static <T, S extends Iterable<T>> Set<T> filterSet(S values, Predicate<? super T> predicate) {
        return filterStream(values, predicate).collect(LinkedHashSet::new, Set::add, Set::addAll);
    }

    /**
     * Filters elements from the given {@link Iterable} using the provided {@link Predicate}
     * and returns a collection of the same type as the input, either a {@link List} or a {@link Set}.
     *
     * <p>This method intelligently handles both {@link List} and {@link Set} types by:
     * <ul>
     *     <li>Detecting if the input is a {@link Set} using {@link SetUtils#isSet(Iterable)}</li>
     *     <li>Using {@link #filterSet(Iterable, Predicate)} for sets to maintain uniqueness and order</li>
     *     <li>Using {@link #filterList(Iterable, Predicate)} for lists to preserve element order and duplicates</li>
     * </ul>
     *
     * <h3>Example Usage with List</h3>
     * <pre>{@code
     * List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
     * List<String> filteredList = Streams.filter(names, name -> name.startsWith("B"));
     * System.out.println(filteredList);  // Output: [Bob]
     * }</pre>
     *
     * <h3>Example Usage with Set</h3>
     * <pre>{@code
     * Set<String> names = new LinkedHashSet<>(Arrays.asList("Alice", "Bob", "Charlie", "Anna"));
     * Set<String> filteredSet = Streams.filter(names, name -> name.startsWith("A"));
     * System.out.println(filteredSet);  // Output: [Alice, Anna]
     * }</pre>
     *
     * @param <T>       the type of the stream elements
     * @param <S>       the type of the iterable, either a list or set
     * @param values    the iterable containing elements to be filtered
     * @param predicate the condition used to include elements in the resulting collection
     * @return a new collection of the same type as the input, containing only the elements that satisfy the predicate
     */
    static <T, S extends Iterable<T>> S filter(S values, Predicate<? super T> predicate) {
        final boolean isSet = isSet(values);
        return (S) (isSet ? filterSet(values, predicate) : filterList(values, predicate));
    }

    /**
     * Filters elements from the given {@link Iterable} using the combined result of multiple predicates in a logical AND fashion.
     *
     * <p> This method applies all provided predicates as a single condition where an element must satisfy
     * <em>all</em> predicates to be included in the resulting collection. The return type matches the input's
     * collection type (either a {@link List} or a {@link Set}) based on the same logic used by {@link #filter(Iterable, Predicate)}.
     *
     * <h3>Example Usage with List</h3>
     * <pre>{@code
     * List<String> names = Arrays.asList("Alice", "Anna", "Bob", "Andrew");
     * List<String> filtered = Streams.filterAll(names,
     *     name -> name.startsWith("A"),
     *     name -> name.length() > 4
     * );
     * System.out.println(filtered);  // Output: [Alice, Andrew]
     * }</pre>
     *
     * <h3>Example Usage with Set</h3>
     * <pre>{@code
     * Set<String> names = new LinkedHashSet<>(Arrays.asList("Alice", "Anna", "Bob", "Andrew"));
     * Set<String> filtered = Streams.filterAll(names,
     *     name -> name.startsWith("A"),
     *     name -> name.length() > 4
     * );
     * System.out.println(filtered);  // Output: [Alice, Andrew]
     * }</pre>
     *
     * @param <T>       the type of the stream elements
     * @param <S>       the type of the iterable, either a list or set
     * @param values    the iterable containing elements to be filtered
     * @param predicates the array of predicates to combine and apply to each element
     * @return a new collection of the same type as the input, containing only the elements that satisfy all predicates
     */
    static <T, S extends Iterable<T>> S filterAll(S values, Predicate<? super T>... predicates) {
        return filter(values, and(predicates));
    }

    /**
     * Filters elements from the given array using the combined result of multiple predicates in a logical AND fashion,
     * and returns a {@link List} containing the matching elements.
     *
     * <p> This method applies all provided predicates as a single condition where an element must satisfy
     * <em>all</em> predicates to be included in the resulting list.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] names = {"Alice", "Anna", "Bob", "Andrew"};
     * List<String> filtered = Streams.filterAllList(names,
     *     name -> name.startsWith("A"),
     *     name -> name.length() > 4
     * );
     * System.out.println(filtered);  // Output: [Alice, Andrew]
     * }</pre>
     *
     * @param <T>        the type of the stream elements
     * @param values     the array containing elements to be filtered
     * @param predicates the array of predicates to combine and apply to each element
     * @return a new {@link List} containing only the elements that satisfy all predicates
     */
    static <T> List<T> filterAllList(T[] values, Predicate<? super T>... predicates) {
        return filterAll(asList(values), and(predicates));
    }

    /**
     * Filters elements from the given array using the combined result of multiple predicates in a logical AND fashion,
     * and returns a {@link Set} containing the matching elements.
     *
     * <p> This method applies all provided predicates as a single condition where an element must satisfy
     * <em>all</em> predicates to be included in the resulting set. The returned {@link Set} maintains insertion order
     * using a {@link LinkedHashSet}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] names = {"Alice", "Anna", "Bob", "Andrew"};
     * Set<String> filtered = Streams.filterAllSet(names,
     *     name -> name.startsWith("A"),
     *     name -> name.length() > 4
     * );
     * System.out.println(filtered);  // Output: [Alice, Andrew]
     * }</pre>
     *
     * @param <T>        the type of the stream elements
     * @param values     the array containing elements to be filtered
     * @param predicates the array of predicates to combine and apply to each element
     * @return a new {@link Set} containing only the elements that satisfy all predicates,
     * maintaining insertion order using a {@link LinkedHashSet}
     */
    static <T> Set<T> filterAllSet(T[] values, Predicate<? super T>... predicates) {
        return filterAll(ofSet(values), and(predicates));
    }

    /**
     * Filters elements from the given {@link Iterable} using the combined result of multiple predicates in a logical OR fashion.
     *
     * <p> This method applies all provided predicates as a single condition where an element must satisfy
     * <em>at least one</em> predicate to be included in the resulting collection. The return type matches the input's
     * collection type (either a {@link List} or a {@link Set}) based on the same logic used by {@link #filter(Iterable, Predicate)}.
     *
     * <h3>Example Usage with List</h3>
     * <pre>{@code
     * List<String> names = Arrays.asList("Alice", "Anna", "Bob", "Andrew");
     * List<String> filtered = Streams.filterAny(names,
     *     name -> name.startsWith("B"),
     *     name -> name.length() <= 3
     * );
     * System.out.println(filtered);  // Output: [Anna, Bob]
     * }</pre>
     *
     * <h3>Example Usage with Set</h3>
     * <pre>{@code
     * Set<String> names = new LinkedHashSet<>(Arrays.asList("Alice", "Anna", "Bob", "Andrew"));
     * Set<String> filtered = Streams.filterAny(names,
     *     name -> name.startsWith("B"),
     *     name -> name.length() <= 3
     * );
     * System.out.println(filtered);  // Output: [Anna, Bob]
     * }</pre>
     *
     * @param <T>       the type of the stream elements
     * @param <S>       the type of the iterable, either a list or set
     * @param values    the iterable containing elements to be filtered
     * @param predicates the array of predicates to combine and apply to each element
     * @return a new collection of the same type as the input, containing only the elements that satisfy at least one predicate
     */
    static <T, S extends Iterable<T>> S filterAny(S values, Predicate<? super T>... predicates) {
        return filter(values, or(predicates));
    }

    /**
     * Filters elements from the given array using the combined result of multiple predicates in a logical OR fashion,
     * and returns a {@link List} containing the matching elements.
     *
     * <p> This method applies all provided predicates as a single condition where an element must satisfy
     * <em>at least one</em> predicate to be included in the resulting list. The order of elements is preserved,
     * and duplicates are allowed if they exist in the input array.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] names = {"Alice", "Anna", "Bob", "Andrew"};
     * List<String> filtered = Streams.filterAnyList(names,
     *     name -> name.startsWith("B"),
     *     name -> name.length() <= 3
     * );
     * System.out.println(filtered);  // Output: [Anna, Bob]
     * }</pre>
     *
     * @param <T>        the type of the stream elements
     * @param values     the array containing elements to be filtered
     * @param predicates the array of predicates to combine and apply to each element
     * @return a new {@link List} containing only the elements that satisfy at least one predicate
     */
    static <T> List<T> filterAnyList(T[] values, Predicate<? super T>... predicates) {
        return filterAny(asList(values), or(predicates));
    }

    /**
     * Filters elements from the given array using the combined result of multiple predicates in a logical OR fashion,
     * and returns a {@link Set} containing the matching elements.
     *
     * <p> This method applies all provided predicates as a single condition where an element must satisfy
     * <em>at least one</em> predicate to be included in the resulting set. The returned {@link Set} maintains insertion order
     * using a {@link LinkedHashSet}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] names = {"Alice", "Anna", "Bob", "Andrew"};
     * Set<String> filtered = Streams.filterAnySet(names,
     *     name -> name.startsWith("B"),
     *     name -> name.length() <= 3
     * );
     * System.out.println(filtered);  // Output: [Anna, Bob]
     * }</pre>
     *
     * @param <T>        the type of the stream elements
     * @param values     the array containing elements to be filtered
     * @param predicates the array of predicates to combine and apply to each element
     * @return a new {@link Set} containing only the elements that satisfy at least one predicate,
     * maintaining insertion order using a {@link LinkedHashSet}
     */
    static <T> Set<T> filterAnySet(T[] values, Predicate<? super T>... predicates) {
        return filterAny(ofSet(values), or(predicates));
    }

    /**
     * Filters elements from the given {@link Iterable} using the combined result of multiple predicates in a logical AND fashion,
     * and returns the first matching element.
     *
     * <p> This method applies all provided predicates as a single condition where an element must satisfy
     * <em>all</em> predicates to be considered a match. It returns the first such element found, or {@code null}
     * if no element matches.
     *
     * <p> If the input is a {@link List}, the method respects the list's iteration order. For a {@link Set},
     * especially a {@link LinkedHashSet}, it maintains insertion order during filtering.
     *
     * <h3>Example Usage with List</h3>
     * <pre>{@code
     * List<String> names = Arrays.asList("Alice", "Anna", "Bob", "Andrew");
     * String result = Streams.filterFirst(names,
     *     name -> name.startsWith("A"),
     *     name -> name.length() > 4
     * );
     * System.out.println(result);  // Output: Alice
     * }</pre>
     *
     * <h3>Example Usage with Set</h3>
     * <pre>{@code
     * Set<String> names = new LinkedHashSet<>(Arrays.asList("Alice", "Anna", "Bob", "Andrew"));
     * String result = Streams.filterFirst(names,
     *     name -> name.startsWith("A"),
     *     name -> name.length() > 4
     * );
     * System.out.println(result);  // Output: Alice
     * }</pre>
     *
     * @param <T>        the type of the stream elements
     * @param values     the iterable containing elements to be filtered
     * @param predicates the array of predicates to combine and apply to each element
     * @return the first element that satisfies all predicates, or {@code null} if none match
     */
    static <T> T filterFirst(Iterable<T> values, Predicate<? super T>... predicates) {
        return StreamSupport.stream(values.spliterator(), false)
                .filter(and(predicates))
                .findFirst()
                .orElse(null);
    }
}


