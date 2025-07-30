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
import io.microsphere.util.Utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

import static io.microsphere.collection.PropertiesUtils.flatProperties;
import static io.microsphere.constants.SymbolConstants.DOT_CHAR;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.ClassUtils.isAssignableFrom;
import static java.lang.Float.MIN_VALUE;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableMap;

/**
 * The utilities class for Java {@link Map}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Map
 * @since 1.0.0
 */
public abstract class MapUtils implements Utils {

    /**
     * The min load factor for {@link HashMap} or {@link Hashtable}
     */
    public static final float MIN_LOAD_FACTOR = MIN_VALUE;

    /**
     * The fixed load factor for {@link HashMap} or {@link Hashtable} = 1.00
     */
    public static final float FIXED_LOAD_FACTOR = 1.00f;

    /**
     * Checks if the specified object is an instance of {@link Map}.
     *
     * <p>This method provides a convenient way to determine whether a given object
     * is a map. It returns {@code true} if the object is an instance of {@link Map},
     * otherwise {@code false}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * System.out.println(MapUtils.isMap(new HashMap<>())); // true
     * System.out.println(MapUtils.isMap(new ArrayList<>())); // false
     * System.out.println(MapUtils.isMap(null)); // false
     * }</pre>
     *
     * @param instance the object to check, may be {@code null}
     * @return {@code true} if the object is an instance of {@link Map}, otherwise {@code false}
     */
    public static boolean isMap(Object instance) {
        return instance instanceof Map;
    }

    /**
     * Checks if the specified class type is an implementation of {@link Map}.
     *
     * <p>This method provides a convenient way to determine whether a given class
     * implements the {@link Map} interface. It returns {@code true} if the class
     * is assignable from {@link Map}, indicating that it is a map type.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * System.out.println(MapUtils.isMap(HashMap.class)); // true
     * System.out.println(MapUtils.isMap(List.class));    // false
     * System.out.println(MapUtils.isMap(null));         // false
     * }</pre>
     *
     * @param type the class type to check, may be {@code null}
     * @return {@code true} if the type is a {@link Map} implementation, otherwise {@code false}
     */
    public static boolean isMap(Class<?> type) {
        return isAssignableFrom(Map.class, type);
    }

    /**
     * Checks if the specified map is either {@code null} or empty.
     *
     * <p>A map is considered empty if it contains no key-value mappings.
     * This method provides a null-safe way to check for emptiness.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<String, Integer> map1 = new HashMap<>();
     * System.out.println(isEmpty(map1)); // true
     *
     * map1.put("one", 1);
     * System.out.println(isEmpty(map1)); // false
     *
     * Map<String, Integer> map2 = null;
     * System.out.println(isEmpty(map2)); // true
     * }</pre>
     *
     * @param map the map to check, may be {@code null}
     * @return {@code true} if the map is {@code null} or empty, otherwise {@code false}
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Checks if the specified map is not empty and not null.
     *
     * <p>This method provides a null-safe way to determine if a map contains one or more key-value mappings.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<String, Integer> map1 = new HashMap<>();
     * System.out.println(isNotEmpty(map1)); // false
     *
     * map1.put("one", 1);
     * System.out.println(isNotEmpty(map1)); // true
     *
     * Map<String, Integer> map2 = null;
     * System.out.println(isNotEmpty(map2)); // false
     * }</pre>
     *
     * @param map the map to check, may be {@code null}
     * @return {@code true} if the map is neither null nor empty, otherwise {@code false}
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }


    /**
     * Returns the size of the specified map, or {@code 0} if the map is {@code null}.
     *
     * <p>This method provides a null-safe way to obtain the size of a map. It avoids
     * {@link NullPointerException} by returning zero when the input map is {@code null}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<String, Integer> map1 = new HashMap<>();
     * map1.put("one", 1);
     * map1.put("two", 2);
     * System.out.println(size(map1)); // Output: 2
     *
     * Map<String, Integer> map2 = null;
     * System.out.println(size(map2)); // Output: 0
     *
     * Map<String, Integer> map3 = new HashMap<>();
     * System.out.println(size(map3)); // Output: 0
     * }</pre>
     *
     * @param map the map whose size is to be returned, may be {@code null}
     * @return the size of the map, or {@code 0} if the map is {@code null}
     */
    public static int size(Map<?, ?> map) {
        return map == null ? 0 : map.size();
    }

    /**
     * Creates an immutable map containing a single key-value pair.
     *
     * <p>This method provides a convenient way to create a small, read-only map
     * with one entry. The resulting map is thread-safe and cannot be modified
     * after creation.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<String, Integer> map = MapUtils.of("one", 1);
     * System.out.println(map.get("one")); // Output: 1
     * }</pre>
     *
     * @param key   the key to be placed in the map
     * @param value the value to be associated with the key
     * @param <K>   the type of the key
     * @param <V>   the type of the value
     * @return a new immutable map containing the specified key-value pair
     */
    @Nonnull
    @Immutable
    public static <K, V> Map<K, V> of(K key, V value) {
        return ofMap(key, value);
    }

    /**
     * Creates an immutable map containing two key-value pairs.
     *
     * <p>This method provides a convenient way to create a small, read-only map with two entries.
     * The resulting map is thread-safe and cannot be modified after creation.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<String, Integer> map = MapUtils.of("one", 1, "two", 2);
     * System.out.println(map.get("one")); // Output: 1
     * System.out.println(map.get("two")); // Output: 2
     * }</pre>
     *
     * @param key1   the first key to be placed in the map
     * @param value1 the value to be associated with the first key
     * @param key2   the second key to be placed in the map
     * @param value2 the value to be associated with the second key
     * @param <K>    the type of the keys
     * @param <V>    the type of the values
     * @return a new immutable map containing the specified key-value pairs
     */
    @Nonnull
    @Immutable
    public static <K, V> Map<K, V> of(K key1, V value1, K key2, V value2) {
        return ofMap(key1, value1, key2, value2);
    }

    /**
     * Creates an immutable map containing three key-value pairs.
     *
     * <p>This method provides a convenient way to create a small, read-only map with three entries.
     * The resulting map is thread-safe and cannot be modified after creation.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<String, Integer> map = MapUtils.of("one", 1, "two", 2, "three", 3);
     * System.out.println(map.get("one"));   // Output: 1
     * System.out.println(map.get("two"));   // Output: 2
     * System.out.println(map.get("three")); // Output: 3
     * }</pre>
     *
     * @param key1   the first key to be placed in the map
     * @param value1 the value to be associated with the first key
     * @param key2   the second key to be placed in the map
     * @param value2 the value to be associated with the second key
     * @param key3   the third key to be placed in the map
     * @param value3 the value to be associated with the third key
     * @param <K>    the type of the keys
     * @param <V>    the type of the values
     * @return a new immutable map containing the specified key-value pairs
     */
    @Nonnull
    @Immutable
    public static <K, V> Map<K, V> of(K key1, V value1, K key2, V value2, K key3, V value3) {
        return ofMap(key1, value1, key2, value2, key3, value3);
    }

    /**
     * Creates an immutable map containing four key-value pairs.
     *
     * <p>This method provides a convenient way to create a small, read-only map with four entries.
     * The resulting map is thread-safe and cannot be modified after creation.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<String, Integer> map = MapUtils.of("one", 1, "two", 2, "three", 3, "four", 4);
     * System.out.println(map.get("one"));   // Output: 1
     * System.out.println(map.get("two"));   // Output: 2
     * System.out.println(map.get("three")); // Output: 3
     * System.out.println(map.get("four"));  // Output: 4
     * }</pre>
     *
     * @param key1   the first key to be placed in the map
     * @param value1 the value to be associated with the first key
     * @param key2   the second key to be placed in the map
     * @param value2 the value to be associated with the second key
     * @param key3   the third key to be placed in the map
     * @param value3 the value to be associated with the third key
     * @param key4   the fourth key to be placed in the map
     * @param value4 the value to be associated with the fourth key
     * @param <K>    the type of the keys
     * @param <V>    the type of the values
     * @return a new immutable map containing the specified key-value pairs
     */
    @Nonnull
    @Immutable
    public static <K, V> Map<K, V> of(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4) {
        return ofMap(key1, value1, key2, value2, key3, value3, key4, value4);
    }

    /**
     * Creates an immutable map containing five key-value pairs.
     *
     * <p>This method provides a convenient way to create a small, read-only map with five entries.
     * The resulting map is thread-safe and cannot be modified after creation.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<String, Integer> map = MapUtils.of("one", 1, "two", 2, "three", 3, "four", 4, "five", 5);
     * System.out.println(map.get("one"));   // Output: 1
     * System.out.println(map.get("two"));   // Output: 2
     * System.out.println(map.get("three")); // Output: 3
     * System.out.println(map.get("four"));  // Output: 4
     * System.out.println(map.get("five"));  // Output: 5
     * }</pre>
     *
     * @param key1   the first key to be placed in the map
     * @param value1 the value to be associated with the first key
     * @param key2   the second key to be placed in the map
     * @param value2 the value to be associated with the second key
     * @param key3   the third key to be placed in the map
     * @param value3 the value to be associated with the third key
     * @param key4   the fourth key to be placed in the map
     * @param value4 the value to be associated with the fourth key
     * @param key5   the fifth key to be placed in the map
     * @param value5 the value to be associated with the fifth key
     * @param <K>    the type of the keys
     * @param <V>    the type of the values
     * @return a new immutable map containing the specified key-value pairs
     */
    @Nonnull
    @Immutable
    public static <K, V> Map<K, V> of(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4, K key5, V value5) {
        return ofMap(key1, value1, key2, value2, key3, value3, key4, value4, key5, value5);
    }

    /**
     * Creates an immutable map from a varargs array of key-value pairs.
     *
     * <p>This method provides a convenient way to create a small, read-only map with multiple entries.
     * The arguments must be provided in consecutive key-value pairs. For example:
     * {@code MapUtils.of("key1", "value1", "key2", "value2")}.
     * The resulting map is thread-safe and cannot be modified after creation.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<String, Integer> map = MapUtils.of("one", 1, "two", 2);
     * System.out.println(map.get("one")); // Output: 1
     * System.out.println(map.get("two")); // Output: 2
     *
     * Map<String, String> emptyMap = MapUtils.of(); // returns an empty map
     * }</pre>
     *
     * @param values a varargs array of objects representing key-value pairs
     * @return a new immutable map containing the specified key-value pairs
     * @throws IllegalArgumentException if the number of arguments is not even (indicating incomplete key-value pairs)
     */
    @Nonnull
    @Immutable
    public static Map of(Object... values) {
        return ofMap(values);
    }

    /**
     * Creates an immutable map from the provided array of {@link Map.Entry} objects.
     *
     * <p>This method offers a convenient way to construct a small, read-only map using pre-defined entries.
     * The resulting map is thread-safe and cannot be modified after creation.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<String, Integer> entry1 = new AbstractMap.SimpleEntry<>("one", 1);
     * Map<String, Integer> entry2 = new AbstractMap.SimpleEntry<>("two", 2);
     * Map<String, Integer> map = MapUtils.of(entry1, entry2);
     *
     * System.out.println(map.get("one")); // Output: 1
     * System.out.println(map.get("two")); // Output: 2
     * }</pre>
     *
     * @param entries a varargs array of Map.Entry objects representing key-value pairs
     * @param <K>     the type of the keys
     * @param <V>     the type of the values
     * @return a new immutable map containing the specified entries
     * @throws NullPointerException if the entries array is null
     */
    @Nonnull
    @Immutable
    public static <K, V> Map<K, V> of(Map.Entry<? extends K, ? extends V>... entries) {
        int length = length(entries);
        if (length < 1) {
            return emptyMap();
        }
        Map<K, V> map = newFixedLinkedHashMap(length);
        for (int i = 0; i < length; i++) {
            Map.Entry<? extends K, ? extends V> entry = entries[i];
            map.put(entry.getKey(), entry.getValue());
        }
        return unmodifiableMap(map);
    }

    /**
     * Creates an immutable map containing a single key-value pair.
     *
     * <p>This method provides a convenient way to create a small, read-only map with one entry.
     * The resulting map is thread-safe and cannot be modified after creation.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<String, Integer> map = MapUtils.ofMap("one", 1);
     * System.out.println(map.get("one")); // Output: 1
     * }</pre>
     *
     * @param key   the key to be placed in the map
     * @param value the value to be associated with the key
     * @param <K>   the type of the key
     * @param <V>   the type of the value
     * @return a new immutable map containing the specified key-value pair
     */
    @Nonnull
    @Immutable
    public static <K, V> Map<K, V> ofMap(K key, V value) {
        return singletonMap(key, value);
    }

    /**
     * Creates an immutable map from a varargs array of key-value pairs.
     *
     * <p>This method provides a convenient way to create a small, read-only map with multiple entries.
     * The arguments must be provided in consecutive key-value pairs. For example:
     * {@code MapUtils.ofMap("key1", "value1", "key2", "value2")}.
     * The resulting map is thread-safe and cannot be modified after creation.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<String, Integer> map = MapUtils.ofMap("one", 1, "two", 2);
     * System.out.println(map.get("one")); // Output: 1
     * System.out.println(map.get("two")); // Output: 2
     *
     * Map<String, String> emptyMap = MapUtils.ofMap(); // returns an empty map
     * }</pre>
     *
     * @param keyValuePairs a varargs array of objects representing key-value pairs
     * @return a new immutable map containing the specified key-value pairs
     * @throws IllegalArgumentException if the number of arguments is not even (indicating incomplete key-value pairs)
     */
    @Nonnull
    @Immutable
    public static Map ofMap(Object... keyValuePairs) {
        int length = length(keyValuePairs);
        if (length < 1) {
            return emptyMap();
        }
        int size = length / 2;
        Map map = newFixedLinkedHashMap(size);
        for (int i = 0; i < length; ) {
            map.put(keyValuePairs[i++], keyValuePairs[i++]);
        }
        return unmodifiableMap(map);
    }

    /**
     * Creates a new empty {@link HashMap} with the default initial capacity (16) and
     * the default load factor (0.75).
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *  HashMap<String, Integer> map = MapUtils.newHashMap();
     *  map.put("one", 1);
     *  System.out.println(map.get("one")); // Output: 1
     *  }</pre>
     *
     * @param <K> the type of keys maintained by the returned map
     * @param <V> the type of mapped values
     * @return a new empty HashMap instance
     * @see HashMap#HashMap()
     */
    @Nonnull
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>();
    }

    /**
     * Creates a new {@link HashMap} with the specified initial capacity and
     * the default load factor (0.75).
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * HashMap<String, Integer> map = MapUtils.newHashMap(10);
     * map.put("one", 1);
     * System.out.println(map.get("one")); // Output: 1
     * }</pre>
     *
     * @param initialCapacity The initial capacity of the returned HashMap.
     * @param <K>             The type of keys maintained by the returned map.
     * @param <V>             The type of mapped values.
     * @return A new HashMap instance with the specified initial capacity.
     * @see HashMap#HashMap(int)
     */
    @Nonnull
    public static <K, V> HashMap<K, V> newHashMap(int initialCapacity) {
        return new HashMap<>(initialCapacity);
    }

    /**
     * Creates a new {@link HashMap} with the specified initial capacity and load factor.
     *
     * <p>The initial capacity refers to the number of buckets in the hash table, while the load factor
     * determines how full the hash table can get before its capacity is automatically increased.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * HashMap<String, Integer> map = MapUtils.newHashMap(10, 0.75f);
     * map.put("one", 1);
     * System.out.println(map.get("one")); // Output: 1
     * }</pre>
     *
     * @param initialCapacity The initial capacity of the returned HashMap.
     * @param loadFactor      The load factor of the returned HashMap.
     * @param <K>             The type of keys maintained by the returned map.
     * @param <V>             The type of mapped values.
     * @return A new HashMap instance with the specified initial capacity and load factor.
     * @see HashMap#HashMap(int, float)
     */
    @Nonnull
    public static <K, V> HashMap<K, V> newHashMap(int initialCapacity, float loadFactor) {
        return new HashMap<>(initialCapacity, loadFactor);
    }

    /**
     * Creates a new {@link HashMap} with the same mappings as the specified map.
     *
     * <p>This method provides a convenient way to create a new HashMap instance that contains all the key-value pairs
     * from an existing map. The order of the entries in the resulting HashMap is not guaranteed, as HashMap does not
     * maintain any specific order.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<String, Integer> originalMap = new HashMap<>();
     * originalMap.put("one", 1);
     * originalMap.put("two", 2);
     *
     * HashMap<String, Integer> copiedMap = MapUtils.newHashMap(originalMap);
     * System.out.println(copiedMap.get("one")); // Output: 1
     * System.out.println(copiedMap.get("two")); // Output: 2
     * }</pre>
     *
     * @param map The initial map whose entries are to be copied into the new HashMap.
     * @param <K> The type of keys maintained by the returned map.
     * @param <V> The type of mapped values.
     * @return A new HashMap instance initialized with the entries from the provided map.
     * @see HashMap#HashMap(Map)
     */
    @Nonnull
    public static <K, V> HashMap<K, V> newHashMap(Map<? extends K, ? extends V> map) {
        return new HashMap<>(map);
    }

    /**
     * Creates a new empty {@link LinkedHashMap} with the default initial capacity (16) and
     * the default load factor (0.75).
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * LinkedHashMap<String, Integer> map = MapUtils.newLinkedHashMap();
     * map.put("one", 1);
     * System.out.println(map.get("one")); // Output: 1
     * }</pre>
     *
     * @param <K> the type of keys maintained by the returned map
     * @param <V> the type of mapped values
     * @return a new empty LinkedHashMap instance
     * @see LinkedHashMap#LinkedHashMap()
     */
    @Nonnull
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
        return new LinkedHashMap<>();
    }

    /**
     * Creates a new {@link LinkedHashMap} with the specified initial capacity and
     * the default load factor (0.75).
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * LinkedHashMap<String, Integer> map = MapUtils.newLinkedHashMap(10);
     * map.put("one", 1);
     * System.out.println(map.get("one")); // Output: 1
     * }</pre>
     *
     * @param initialCapacity The initial capacity of the returned LinkedHashMap.
     * @param <K>             The type of keys maintained by the returned map.
     * @param <V>             The type of mapped values.
     * @return A new LinkedHashMap instance with the specified initial capacity.
     * @see LinkedHashMap#LinkedHashMap(int)
     */
    @Nonnull
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(int initialCapacity) {
        return new LinkedHashMap<>(initialCapacity);
    }

    /**
     * Creates a new {@link LinkedHashMap} with the specified initial capacity and load factor.
     *
     * <p>The initial capacity refers to the number of buckets in the hash table, while the load factor
     * determines how full the hash table can get before its capacity is automatically increased.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * LinkedHashMap<String, Integer> map = MapUtils.newLinkedHashMap(10, 0.75f);
     * map.put("one", 1);
     * System.out.println(map.get("one")); // Output: 1
     * }</pre>
     *
     * @param initialCapacity The initial capacity of the returned LinkedHashMap.
     * @param loadFactor      The load factor of the returned LinkedHashMap.
     * @param <K>             The type of keys maintained by the returned map.
     * @param <V>             The type of mapped values.
     * @return A new LinkedHashMap instance with the specified initial capacity and load factor.
     * @see LinkedHashMap#LinkedHashMap(int, float)
     */
    @Nonnull
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(int initialCapacity, float loadFactor) {
        return new LinkedHashMap<>(initialCapacity, loadFactor);
    }

    /**
     * Creates a new {@link LinkedHashMap} with the specified initial capacity, load factor, and ordering mode.
     *
     * <p>The initial capacity refers to the number of buckets in the hash table, while the load factor
     * determines how full the hash table can get before its capacity is automatically increased.
     * The {@code accessOrder} parameter determines the iteration ordering of the map:
     * <ul>
     *   <li>If set to {@code true}, the map will be ordered by the access order (least recently accessed
     *       elements come first).</li>
     *   <li>If set to {@code false}, the map will be ordered by insertion order.</li>
     * </ul>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * LinkedHashMap<String, Integer> map = MapUtils.newLinkedHashMap(10, 0.75f, true);
     * map.put("one", 1);
     * map.put("two", 2);
     * map.get("one"); // Accessing "one" may reorder the map depending on accessOrder
     * }</pre>
     *
     * @param initialCapacity The initial capacity of the returned LinkedHashMap.
     * @param loadFactor      The load factor of the returned LinkedHashMap.
     * @param accessOrder     If true, the map will use access order for iteration; if false, insertion order.
     * @param <K>             The type of keys maintained by the returned map.
     * @param <V>             The type of mapped values.
     * @return A new LinkedHashMap instance with the specified initial capacity, load factor, and ordering mode.
     * @see LinkedHashMap#LinkedHashMap(int, float, boolean)
     */
    @Nonnull
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
        return new LinkedHashMap<>(initialCapacity, loadFactor, accessOrder);
    }

    /**
     * Creates a new {@link LinkedHashMap} with the same mappings as the specified map.
     *
     * <p>This method provides a convenient way to create a new LinkedHashMap instance that contains all the key-value pairs
     * from an existing map. The iteration order of the resulting map is based on insertion order unless the original map
     * was an instance of {@link LinkedHashMap} with access order enabled.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<String, Integer> originalMap = new HashMap<>();
     * originalMap.put("one", 1);
     * originalMap.put("two", 2);
     *
     * LinkedHashMap<String, Integer> linkedMap = MapUtils.newLinkedHashMap(originalMap);
     * System.out.println(linkedMap.get("one")); // Output: 1
     * System.out.println(linkedMap.get("two")); // Output: 2
     * }</pre>
     *
     * @param map The initial map whose entries are to be copied into the new LinkedHashMap.
     * @param <K> The type of keys maintained by the returned map.
     * @param <V> The type of mapped values.
     * @return A new LinkedHashMap instance initialized with the entries from the provided map.
     * @see LinkedHashMap#LinkedHashMap(Map)
     */
    @Nonnull
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(Map<? extends K, ? extends V> map) {
        return new LinkedHashMap(map);
    }

    /**
     * Creates a new empty {@link ConcurrentHashMap} with the default initial capacity (16) and
     * the default load factor (0.75), using the default concurrency level.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *  ConcurrentHashMap<String, Integer> map = MapUtils.newConcurrentHashMap();
     *  map.put("one", 1);
     *  System.out.println(map.get("one")); // Output: 1
     *  }</pre>
     *
     * @param <K> the type of keys maintained by the returned map
     * @param <V> the type of mapped values
     * @return a new empty ConcurrentHashMap instance
     * @see ConcurrentHashMap#ConcurrentHashMap()
     */
    @Nonnull
    public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Creates a new {@link ConcurrentHashMap} with the specified initial capacity and
     * the default load factor (0.75), using the default concurrency level.
     *
     * <p>This method provides a convenient way to initialize a ConcurrentHashMap with a known
     * initial size, which can help reduce the frequency of rehash operations as elements are added.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *  ConcurrentHashMap<String, Integer> map = MapUtils.newConcurrentHashMap(10);
     *  map.put("one", 1);
     *  System.out.println(map.get("one")); // Output: 1
     * }</pre>
     *
     * @param initialCapacity The initial capacity of the returned ConcurrentHashMap.
     * @param <K>             The type of keys maintained by the returned map.
     * @param <V>             The type of mapped values.
     * @return A new ConcurrentHashMap instance with the specified initial capacity.
     * @see ConcurrentHashMap#ConcurrentHashMap(int)
     */
    @Nonnull
    public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap(int initialCapacity) {
        return new ConcurrentHashMap<>(initialCapacity);
    }

    /**
     * Creates a new {@link ConcurrentHashMap} with the specified initial capacity and load factor.
     *
     * <p>The initial capacity refers to the number of buckets in the hash table, while the load factor
     * determines how full the hash table can get before its capacity is automatically increased.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *  ConcurrentHashMap<String, Integer> map = MapUtils.newConcurrentHashMap(10, 0.75f);
     *  map.put("one", 1);
     *  System.out.println(map.get("one")); // Output: 1
     * }</pre>
     *
     * @param initialCapacity The initial capacity of the returned ConcurrentHashMap.
     * @param loadFactor      The load factor of the returned ConcurrentHashMap.
     * @param <K>             The type of keys maintained by the returned map.
     * @param <V>             The type of mapped values.
     * @return A new ConcurrentHashMap instance with the specified initial capacity and load factor.
     * @see ConcurrentHashMap#ConcurrentHashMap(int, float)
     */
    @Nonnull
    public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap(int initialCapacity,
                                                                      float loadFactor) {
        return new ConcurrentHashMap<>(initialCapacity, loadFactor);
    }

    /**
     * Creates a new {@link ConcurrentHashMap} with the same mappings as the specified map.
     *
     * <p>This method provides a convenient way to create a new ConcurrentHashMap instance that contains all the key-value pairs
     * from an existing map. The returned map is thread-safe and suitable for concurrent access scenarios.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<String, Integer> originalMap = new HashMap<>();
     * originalMap.put("one", 1);
     * originalMap.put("two", 2);
     *
     * ConcurrentHashMap<String, Integer> concurrentMap = MapUtils.newConcurrentHashMap(originalMap);
     * System.out.println(concurrentMap.get("one")); // Output: 1
     * System.out.println(concurrentMap.get("two")); // Output: 2
     * }</pre>
     *
     * @param map The initial map whose entries are to be copied into the new ConcurrentHashMap.
     * @param <K> The type of keys maintained by the returned map.
     * @param <V> The type of mapped values.
     * @return A new ConcurrentHashMap instance initialized with the entries from the provided map.
     * @see ConcurrentHashMap#ConcurrentHashMap(Map)
     */
    @Nonnull
    public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap(Map<? extends K, ? extends V> map) {
        return new ConcurrentHashMap<>(map);
    }

    /**
     * Creates a new empty {@link TreeMap} using natural ordering for the keys.
     *
     * <p>This method provides a convenient way to create a TreeMap with default settings,
     * ensuring that the keys will be sorted according to their natural ordering.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TreeMap<String, Integer> treeMap = MapUtils.newTreeMap();
     * treeMap.put("banana", 2);
     * treeMap.put("apple", 1);
     *
     * // Output will be ordered by keys: apple=1, banana=2
     * for (Map.Entry<String, Integer> entry : treeMap.entrySet()) {
     *     System.out.println(entry.getKey() + "=" + entry.getValue());
     * }
     * }</pre>
     *
     * @param <K> the type of keys maintained by the returned map
     * @param <V> the type of mapped values
     * @return a new empty TreeMap instance with natural key ordering
     * @see TreeMap#TreeMap()
     */
    @Nonnull
    public static <K, V> TreeMap<K, V> newTreeMap() {
        return new TreeMap<>();
    }

    /**
     * Creates a new empty {@link TreeMap} with the specified comparator to order the keys.
     *
     * <p>This method provides a convenient way to create a TreeMap with a custom ordering strategy,
     * allowing for flexible sorting based on the provided comparator.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Comparator<String> descendingOrder = Collections.reverseOrder();
     * TreeMap<String, Integer> treeMap = MapUtils.newTreeMap(descendingOrder);
     * treeMap.put("banana", 2);
     * treeMap.put("apple", 1);
     *
     * // Output will be ordered by keys in descending order: banana=2, apple=1
     * for (Map.Entry<String, Integer> entry : treeMap.entrySet()) {
     *     System.out.println(entry.getKey() + "=" + entry.getValue());
     * }
     * }</pre>
     *
     * @param comparator The comparator to use for ordering the keys in this map.
     * @param <K>        The type of keys maintained by the returned map.
     * @param <V>        The type of mapped values.
     * @return A new empty TreeMap instance with the specified comparator.
     * @see TreeMap#TreeMap(Comparator)
     */
    @Nonnull
    public static <K, V> TreeMap<K, V> newTreeMap(Comparator<? super K> comparator) {
        return new TreeMap<>(comparator);
    }

    /**
     * Creates a new {@link TreeMap} with the same mappings as the specified map.
     *
     * <p>This method provides a convenient way to create a new TreeMap instance that contains all the key-value pairs
     * from an existing map. The keys in the resulting TreeMap will be sorted according to their natural ordering,
     * or by the comparator used by the provided map if it is a sorted map.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<String, Integer> originalMap = new HashMap<>();
     * originalMap.put("banana", 2);
     * originalMap.put("apple", 1);
     *
     * TreeMap<String, Integer> treeMap = MapUtils.newTreeMap(originalMap);
     *
     * // Output will be ordered by keys: apple=1, banana=2
     * for (Map.Entry<String, Integer> entry : treeMap.entrySet()) {
     *     System.out.println(entry.getKey() + "=" + entry.getValue());
     * }
     * }</pre>
     *
     * @param map The initial map whose entries are to be copied into the new TreeMap.
     * @param <K> The type of keys maintained by the returned map.
     * @param <V> The type of mapped values.
     * @return A new TreeMap instance initialized with the entries from the provided map.
     * @see TreeMap#TreeMap(Map)
     */
    @Nonnull
    public static <K, V> TreeMap<K, V> newTreeMap(Map<? extends K, ? extends V> map) {
        return new TreeMap<>(map);
    }

    /**
     * Creates a new {@link TreeMap} with the same mappings as the specified sorted map.
     *
     * <p>This method provides a convenient way to create a new TreeMap instance that contains all the key-value pairs
     * from an existing SortedMap. The keys in the resulting TreeMap will be sorted according to the comparator used by
     * the provided map, or by natural ordering if the map uses natural ordering.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * SortedMap<String, Integer> originalSortedMap = new TreeMap<>();
     * originalSortedMap.put("banana", 2);
     * originalSortedMap.put("apple", 1);
     *
     * TreeMap<String, Integer> treeMap = MapUtils.newTreeMap(originalSortedMap);
     *
     * // Output will be ordered by keys: apple=1, banana=2
     * for (Map.Entry<String, Integer> entry : treeMap.entrySet()) {
     *     System.out.println(entry.getKey() + "=" + entry.getValue());
     * }
     * }</pre>
     *
     * @param map The initial sorted map whose entries are to be copied into the new TreeMap.
     * @param <K> The type of keys maintained by the returned map.
     * @param <V> The type of mapped values.
     * @return A new TreeMap instance initialized with the entries from the provided sorted map.
     * @see TreeMap#TreeMap(SortedMap)
     */
    @Nonnull
    public static <K, V> TreeMap<K, V> newTreeMap(SortedMap<K, ? extends V> map) {
        return new TreeMap<>(map);
    }

    /**
     * Performs a shallow clone of the provided map, creating a new map instance with the same key-value mappings.
     *
     * <p>This method returns a new map of the most appropriate type based on the source map's implementation:
     * <ul>
     *   <li>{@link LinkedHashMap} for insertion-ordered maps</li>
     *   <li>{@link ConcurrentSkipListMap} for concurrent navigable maps</li>
     *   <li>{@link TreeMap} for sorted maps</li>
     *   <li>{@link ConcurrentHashMap} for concurrent maps</li>
     *   <li>{@link IdentityHashMap} for identity-based maps</li>
     *   <li>{@link HashMap} as the default fallback</li>
     * </ul>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<String, Integer> original = new HashMap<>();
     * original.put("one", 1);
     * original.put("two", 2);
     *
     * Map<String, Integer> cloned = MapUtils.shallowCloneMap(original);
     * System.out.println(cloned.get("one")); // Output: 1
     * System.out.println(cloned.get("two")); // Output: 2
     *
     * // The cloned map is independent from the original
     * original.put("three", 3);
     * System.out.println(cloned.containsKey("three")); // false
     * }</pre>
     *
     * @param source the map to be shallow cloned, must not be {@code null}
     * @param <K>    the type of keys maintained by the map
     * @param <V>    the type of mapped values
     * @return a new map containing the same key-value pairs as the source map
     * @throws NullPointerException if the source map is null
     */
    @Nonnull
    public static <K, V> Map<K, V> shallowCloneMap(@Nonnull Map<K, V> source) {
        if (source instanceof LinkedHashMap) {
            return new LinkedHashMap(source);
        } else if (source instanceof ConcurrentNavigableMap) {
            return new ConcurrentSkipListMap(source);
        } else if (source instanceof SortedMap) {
            return new TreeMap(source);
        } else if (source instanceof ConcurrentMap) {
            return new ConcurrentHashMap<>(source);
        } else if (source instanceof IdentityHashMap) {
            return new IdentityHashMap(source);
        } else {
            return new HashMap(source);
        }
    }

    /**
     * Creates a new empty {@link HashMap} with the specified initial capacity and a fixed load factor.
     *
     * <p>This method is useful when you want to create a HashMap with a known initial size
     * and use the predefined fixed load factor ({@value #FIXED_LOAD_FACTOR}), which helps in minimizing resizing operations.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<String, Integer> map = MapUtils.newFixedHashMap(10);
     * map.put("one", 1);
     * System.out.println(map.get("one")); // Output: 1
     * }</pre>
     *
     * @param size The initial capacity of the returned HashMap.
     * @param <K>  The type of keys maintained by the returned map.
     * @param <V>  The type of mapped values.
     * @return A new HashMap instance with the specified initial capacity and fixed load factor.
     * @see HashMap#HashMap(int, float)
     */
    @Nonnull
    public static <K, V> Map<K, V> newFixedHashMap(int size) {
        return newHashMap(size, FIXED_LOAD_FACTOR);
    }

    /**
     * Creates a new empty {@link LinkedHashMap} with the specified initial capacity and a fixed load factor.
     *
     * <p>This method is useful when you want to create a LinkedHashMap with a known initial size
     * and use the predefined fixed load factor ({@value #FIXED_LOAD_FACTOR}), which helps in minimizing resizing operations.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<String, Integer> map = MapUtils.newFixedLinkedHashMap(10);
     * map.put("one", 1);
     * System.out.println(map.get("one")); // Output: 1
     * }</pre>
     *
     * @param size The initial capacity of the returned LinkedHashMap.
     * @param <K>  The type of keys maintained by the returned map.
     * @param <V>  The type of mapped values.
     * @return A new LinkedHashMap instance with the specified initial capacity and fixed load factor.
     * @see LinkedHashMap#LinkedHashMap(int, float)
     */
    @Nonnull
    public static <K, V> Map<K, V> newFixedLinkedHashMap(int size) {
        return newLinkedHashMap(size, FIXED_LOAD_FACTOR);
    }

    /**
     * Converts a collection of elements into an immutable map using the provided function to extract key-value pairs.
     *
     * <p>This method creates a fixed-size, immutable map from the given collection by applying the entryMapper
     * function to each element to obtain key-value mappings. The resulting map is thread-safe and cannot be modified.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<User> users = Arrays.asList(new User("1", "Alice"), new User("2", "Bob"));
     *
     * Map<String, String> userMap = MapUtils.toFixedMap(users, user ->
     *     MapUtils.ofEntry(user.getId(), user.getName()));
     *
     * System.out.println(userMap.get("1")); // Output: Alice
     * System.out.println(userMap.get("2")); // Output: Bob
     * }</pre>
     *
     * @param values      the collection of elements to convert into a map, must not be null
     * @param entryMapper the function that maps each element to a key-value pair (Map.Entry), must not be null
     * @param <K>         the type of keys maintained by the returned map
     * @param <V>         the type of mapped values
     * @param <E>         the type of elements in the input collection
     * @return a new immutable map containing the key-value pairs derived from the input collection
     * @throws NullPointerException if values or entryMapper is null
     */
    @Nonnull
    @Immutable
    public static <K, V, E> Map<K, V> toFixedMap(Collection<E> values,
                                                 Function<E, Map.Entry<K, V>> entryMapper) {
        int size = CollectionUtils.size(values);
        if (size < 1) {
            return emptyMap();
        }

        Map<K, V> fixedMap = newFixedLinkedHashMap(size);

        for (E value : values) {
            Map.Entry<K, V> entry = entryMapper.apply(value);
            fixedMap.put(entry.getKey(), entry.getValue());
        }
        return unmodifiableMap(fixedMap);
    }

    /**
     * Creates an immutable {@link Map.Entry} with the specified key and value.
     *
     * <p>This method returns a fixed entry implementation that does not allow modification of its contents.
     * The returned entry is suitable for use in read-only contexts, such as populating immutable maps.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map.Entry<String, Integer> entry = MapUtils.ofEntry("one", 1);
     * System.out.println(entry.getKey());   // Output: one
     * System.out.println(entry.getValue()); // Output: 1
     *
     * // Using in combination with other utilities to build a map:
     * Map<String, Integer> map = MapUtils.of(
     *     MapUtils.ofEntry("apple", 3),
     *     MapUtils.ofEntry("banana", 5)
     * );
     * System.out.println(map.get("apple"));  // Output: 3
     * System.out.println(map.get("banana")); // Output: 5
     * }</pre>
     *
     * @param key   the key to be stored in the entry
     * @param value the value to be associated with the key
     * @param <K>   the type of the key
     * @param <V>   the type of the value
     * @return a new immutable map entry containing the specified key and value
     * @see DefaultEntry
     */
    @Nonnull
    public static <K, V> Map.Entry<K, V> ofEntry(K key, V value) {
        return DefaultEntry.of(key, value);
    }

    /**
     * Creates an immutable {@link Map.Entry} with the specified key and value.
     *
     * <p>This method returns a read-only entry implementation that does not allow modification of its contents.
     * It is suitable for use in read-only contexts, such as populating immutable maps.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map.Entry<String, Integer> entry = MapUtils.immutableEntry("one", 1);
     * System.out.println(entry.getKey());   // Output: one
     * System.out.println(entry.getValue()); // Output: 1
     *
     * // Attempting to modify the entry will throw an exception:
     * // entry.setValue(2); // throws UnsupportedOperationException
     * }</pre>
     *
     * @param key   the key to be stored in the entry
     * @param value the value to be associated with the key
     * @param <K>   the type of the key
     * @param <V>   the type of the value
     * @return a new immutable map entry containing the specified key and value
     * @see ImmutableEntry
     */
    @Nonnull
    @Immutable
    public static <K, V> Map.Entry<K, V> immutableEntry(K key, V value) {
        return ImmutableEntry.of(key, value);
    }

    /**
     * Flattens a nested map of properties into a single-level map.
     *
     * <p>If the input map is empty or null, the same map instance is returned.</p>
     *
     * <p>For example, given the following input:
     * <pre>{@code
     * {
     *   "a": "1",
     *   "b": {
     *     "c": "2",
     *     "d": {
     *       "e": "3"
     *     }
     *   }
     * }
     * }</pre>
     * The resulting flattened map would be:
     * <pre>{@code
     * {
     *   "a": "1",
     *   "b.c": "2",
     *   "b.d.e": "3"
     * }
     * }</pre>
     *
     * @param map The map containing potentially nested properties to be flattened.
     * @return A new unmodifiable map with all properties flattened to a single level.
     */
    public static Map<String, Object> flattenMap(Map<String, Object> map) {
        return flatProperties(map);
    }

    /**
     * Converts a flat map with dot-separated keys into a nested map structure.
     *
     * <p>This method takes a map where keys represent hierarchical paths (e.g., "a.b.c") and
     * transforms it into a nested map structure where each level of the hierarchy becomes
     * a separate map. This is particularly useful for configuration properties or data
     * that needs to be organized in a tree-like structure.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<String, Object> flatMap = new LinkedHashMap<>();
     * flatMap.put("a.b.1", "value1");
     * flatMap.put("a.b.2", "value2");
     * flatMap.put("a.c", "value3");
     * flatMap.put("d", "value4");
     *
     * Map<String, Object> nested = MapUtils.nestedMap(flatMap);
     * // Result:
     * // {
     * //   "a": {
     * //     "b": {
     * //       "1": "value1",
     * //       "2": "value2"
     * //     },
     * //     "c": "value3"
     * //   },
     * //   "d": "value4"
     * // }
     * }</pre>
     *
     * @param map the flat map to be converted into a nested structure, may be {@code null}
     * @return a new map with nested structure, or an empty map if input is {@code null} or empty
     */
    public static Map<String, Object> nestedMap(Map<String, Object> map) {
        Map<String, Object> nestedMap = newLinkedHashMap();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String propertyName = entry.getKey();
            String propertyValue = String.valueOf(entry.getValue());
            int index = propertyName.indexOf(DOT_CHAR);
            if (index > 0) {
                String actualPropertyName = propertyName.substring(0, index);
                String subPropertyName = propertyName.substring(index + 1, propertyName.length());
                Object actualPropertyValue = nestedMap.get(actualPropertyName);
                if (actualPropertyValue == null) {
                    actualPropertyValue = newLinkedHashMap();
                    nestedMap.put(actualPropertyName, actualPropertyValue);
                }

                if (actualPropertyValue instanceof Map) {
                    Map<String, Object> nestedProperties = (Map<String, Object>) actualPropertyValue;
                    Map<String, Object> subProperties = extraProperties(nestedProperties);
                    subProperties.put(subPropertyName, propertyValue);
                    Map<String, Object> subNestedMap = nestedMap(subProperties);
                    nestedProperties.putAll(subNestedMap);
                }
            } else {
                nestedMap.put(propertyName, propertyValue);
            }
        }
        return nestedMap;
    }

    /**
     * Extracts and flattens properties from a nested map structure into a single-level map.
     *
     * <p>This method recursively processes the input map, converting any nested maps into
     * dot-separated key paths in the resulting map. Non-map values are converted to strings.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<String, Object> nested = new LinkedHashMap<>();
     * Map<String, Object> inner = new LinkedHashMap<>();
     * inner.put("x", "10");
     * inner.put("y", "20");
     * nested.put("a", inner);
     * nested.put("b", "value");
     *
     * Map<String, Object> result = MapUtils.extraProperties(nested);
     * // Result: {"a.x"="10", "a.y"="20", "b"="value"}
     * }</pre>
     *
     * @param map the map from which to extract properties, may be {@code null}
     * @return a new map containing flattened properties, or an empty map if input is {@code null} or empty
     */
    static Map<String, Object> extraProperties(Map<String, Object> map) {
        int size = size(map);
        Map<String, Object> properties = newLinkedHashMap(size);
        if (size > 0) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Map) {
                    Map<String, String> subProperties = extraProperties((Map) value);
                    for (Map.Entry<String, String> e : subProperties.entrySet()) {
                        String subKey = e.getKey();
                        String subValue = e.getValue();
                        properties.put(key + DOT_CHAR + subKey, subValue);
                    }
                } else if (value instanceof String) {
                    properties.put(key, value.toString());
                }
            }
        }
        return properties;
    }

    private MapUtils() {
    }
}
