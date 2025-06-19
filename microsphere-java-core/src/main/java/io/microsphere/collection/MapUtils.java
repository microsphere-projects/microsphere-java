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

import static io.microsphere.collection.CollectionUtils.size;
import static io.microsphere.util.ArrayUtils.length;
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
    public static final float MIN_LOAD_FACTOR = Float.MIN_VALUE;

    /**
     * The fixed load factor for {@link HashMap} or {@link Hashtable} = 1.00
     */
    protected static final float FIXED_LOAD_FACTOR = 1.00f;

    /**
     * Checks if the provided map is either {@code null} or empty.
     *
     * @param map the map to check
     * @return {@code true} if the map is null or has no entries, otherwise {@code false}
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Checks if the provided map is not null and contains at least one entry.
     *
     * @param map the map to check
     * @return {@code true} if the map is not null and has at least one entry, otherwise {@code false}
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * Creates an immutable map containing a single key-value pair.
     *
     * @param key   the key to be placed in the map
     * @param value the value to be associated with the key
     * @param <K>   the type of the key
     * @param <V>   the type of the value
     * @return a new immutable map containing the specified key-value pair
     */
    public static <K, V> Map<K, V> of(K key, V value) {
        return ofMap(key, value);
    }

    /**
     * Creates an immutable map containing two key-value pairs.
     *
     * @param key1   the first key to be placed in the map
     * @param value1 the value to be associated with the first key
     * @param key2   the second key to be placed in the map
     * @param value2 the value to be associated with the second key
     * @param <K>    the type of the keys
     * @param <V>    the type of the values
     * @return a new immutable map containing the specified key-value pairs
     */
    public static <K, V> Map<K, V> of(K key1, V value1, K key2, V value2) {
        return ofMap(key1, value1, key2, value2);
    }

    /**
     * Creates an immutable map containing three key-value pairs.
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
    public static <K, V> Map<K, V> of(K key1, V value1, K key2, V value2, K key3, V value3) {
        return ofMap(key1, value1, key2, value2, key3, value3);
    }

    /**
     * Creates an immutable map containing four key-value pairs.
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
    public static <K, V> Map<K, V> of(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4) {
        return ofMap(key1, value1, key2, value2, key3, value3, key4, value4);
    }

    /**
     * Creates an immutable map containing five key-value pairs.
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
    public static <K, V> Map<K, V> of(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4, K key5, V value5) {
        return ofMap(key1, value1, key2, value2, key3, value3, key4, value4, key5, value5);
    }

    /**
     * Creates an immutable map from a sequence of key-value pairs provided as varargs.
     * <p>
     * The arguments must be provided in consecutive key-value pairs. For example:
     * {@code of("key1", "value1", "key2", "value2")}.
     * </p>
     *
     * @param values A varargs array of objects representing key-value pairs.
     * @return A new immutable Map constructed from the provided key-value pairs.
     * @throws IllegalArgumentException if the number of arguments is not even (indicating incomplete key-value pairs)
     * @see #ofMap(Object...)
     */
    public static Map of(Object... values) {
        return ofMap(values);
    }

    /**
     * Creates an immutable map from the provided {@link Map.Entry} elements.
     *
     * @param entries a varargs array of Map.Entry elements representing key-value pairs
     * @param <K>     the type of the keys
     * @param <V>     the type of the values
     * @return a new immutable map containing the specified entries
     * @throws IllegalArgumentException if the entries array is null or has zero length
     * @see #ofMap(Map.Entry...)
     */
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
     * @param key   the key to be placed in the map
     * @param value the value to be associated with the key
     * @param <K>   the type of the key
     * @param <V>   the type of the value
     * @return a new immutable map containing the specified key-value pair
     */
    public static <K, V> Map<K, V> ofMap(K key, V value) {
        return singletonMap(key, value);
    }

    /**
     * Creates an immutable map from a sequence of key-value pairs provided as varargs.
     * <p>
     * The arguments must be provided in consecutive key-value pairs. For example:
     * {@code ofMap("key1", "value1", "key2", "value2")}.
     * </p>
     *
     * @param keyValuePairs A varargs array of objects representing key-value pairs.
     * @return A new immutable Map constructed from the provided key-value pairs.
     * @throws IllegalArgumentException if the number of arguments is not even (indicating incomplete key-value pairs)
     */
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
     * @param <K> the type of keys maintained by the returned map
     * @param <V> the type of mapped values
     * @return a new empty HashMap instance
     * @see HashMap#HashMap()
     */
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>();
    }

    /**
     * Creates a new {@link HashMap} with the specified initial capacity and
     * the default load factor (0.75).
     *
     * @param initialCapacity The initial capacity of the returned HashMap.
     * @param <K>             The type of keys maintained by the returned map.
     * @param <V>             The type of mapped values.
     * @return A new HashMap instance with the specified initial capacity.
     * @see HashMap#HashMap(int)
     */
    public static <K, V> HashMap<K, V> newHashMap(int initialCapacity) {
        return new HashMap<>(initialCapacity);
    }

    /**
     * Creates a new {@link HashMap} with the specified initial capacity and load factor.
     *
     * @param initialCapacity The initial capacity of the returned HashMap.
     * @param loadFactor      The load factor of the returned HashMap.
     * @param <K>             The type of keys maintained by the returned map.
     * @param <V>             The type of mapped values.
     * @return A new HashMap instance with the specified initial capacity and load factor.
     * @see HashMap#HashMap(int, float)
     */
    public static <K, V> HashMap<K, V> newHashMap(int initialCapacity, float loadFactor) {
        return new HashMap<>(initialCapacity, loadFactor);
    }

    /**
     * Creates a new {@link HashMap} with the same mappings as the specified map.
     *
     * @param map The initial map whose entries are to be copied into the new HashMap.
     * @param <K> The type of keys maintained by the returned map.
     * @param <V> The type of mapped values.
     * @return A new HashMap instance initialized with the entries from the provided map.
     * @see HashMap#HashMap(Map)
     */
    public static <K, V> HashMap<K, V> newHashMap(Map<? extends K, ? extends V> map) {
        return new HashMap<>(map);
    }

    /**
     * Creates a new empty {@link LinkedHashMap} with the default initial capacity (16) and
     * the default load factor (0.75).
     *
     * @param <K> the type of keys maintained by the returned map
     * @param <V> the type of mapped values
     * @return a new empty LinkedHashMap instance
     * @see LinkedHashMap#LinkedHashMap()
     */
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
        return new LinkedHashMap<>();
    }

    /**
     * Creates a new {@link LinkedHashMap} with the specified initial capacity and
     * the default load factor (0.75).
     *
     * @param initialCapacity The initial capacity of the returned LinkedHashMap.
     * @param <K>             The type of keys maintained by the returned map.
     * @param <V>             The type of mapped values.
     * @return A new LinkedHashMap instance with the specified initial capacity.
     * @see LinkedHashMap#LinkedHashMap(int)
     */
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(int initialCapacity) {
        return new LinkedHashMap<>(initialCapacity);
    }

    /**
     * Creates a new {@link LinkedHashMap} with the specified initial capacity and load factor.
     *
     * @param initialCapacity The initial capacity of the returned LinkedHashMap.
     * @param loadFactor      The load factor of the returned LinkedHashMap.
     * @param <K>             The type of keys maintained by the returned map.
     * @param <V>             The type of mapped values.
     * @return A new LinkedHashMap instance with the specified initial capacity and load factor.
     * @see LinkedHashMap#LinkedHashMap(int, float)
     */
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(int initialCapacity,
                                                              float loadFactor) {
        return new LinkedHashMap<>(initialCapacity, loadFactor);
    }

    /**
     * Creates a new {@link LinkedHashMap} with the specified initial capacity, load factor,
     * and access order mode.
     *
     * <p>If accessOrder is true, the map will be sorted based on the access order (least recently
     * accessed first). If false, it will be sorted based on insertion order.</p>
     *
     * @param initialCapacity The initial capacity
     *                        ``` of the returned LinkedHashMap.
     * @param loadFactor      The load factor of the returned LinkedHashMap.
     * @param accessOrder     Specifies the ordering mode - true for access-order, false for insertion-order.
     * @param <K>             The type of keys maintained by the returned map.
     * @param <V>             The type of mapped values.
     * @return A new LinkedHashMap instance with the specified initial capacity, load factor, and access order.
     * @see LinkedHashMap#LinkedHashMap(int, float, boolean)
     */
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(int initialCapacity,
                                                              float loadFactor,
                                                              boolean accessOrder) {
        return new LinkedHashMap<>(initialCapacity, loadFactor, accessOrder);
    }

    /**
     * Creates a new {@link LinkedHashMap} with the same mappings as the specified map.
     *
     * @param map The initial map whose entries are to be copied into the new LinkedHashMap.
     * @param <K> The type of keys maintained by the returned map.
     * @param <V> The type of mapped values.
     * @return A new LinkedHashMap instance initialized with the entries from the provided map.
     * @see LinkedHashMap#LinkedHashMap(Map)
     */
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(Map<? extends K, ? extends V> map) {
        return new LinkedHashMap(map);
    }

    /**
     * Creates a new empty {@link ConcurrentHashMap} with the default initial capacity (16) and
     * the default load factor (0.75), using the default concurrency level.
     *
     * @param <K> the type of keys maintained by the returned map
     * @param <V> the type of mapped values
     * @return a new empty ConcurrentHashMap instance
     * @see ConcurrentHashMap#ConcurrentHashMap()
     */
    public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Creates a new {@link ConcurrentHashMap} with the specified initial capacity and
     * the default load factor (0.75), using the default concurrency level.
     *
     * @param initialCapacity The initial capacity of the returned ConcurrentHashMap.
     * @param <K>             The type of keys maintained by the returned map.
     * @param <V>             The type of mapped values.
     * @return A new ConcurrentHashMap instance with the specified initial capacity.
     * @see ConcurrentHashMap#ConcurrentHashMap(int)
     */
    public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap(int initialCapacity) {
        return new ConcurrentHashMap<>(initialCapacity);
    }

    /**
     * Creates a new {@link ConcurrentHashMap} with the specified initial capacity and load factor,
     * using the default concurrency level.
     *
     * @param initialCapacity The initial capacity of the returned ConcurrentHashMap.
     * @param loadFactor      The load factor of the returned ConcurrentHashMap.
     * @param <K>             The type of keys maintained by the returned map.
     * @param <V>             The type of mapped values.
     * @return A new ConcurrentHashMap instance with the specified initial capacity and load factor.
     * @see ConcurrentHashMap#ConcurrentHashMap(int, float)
     */
    public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap(int initialCapacity,
                                                                      float loadFactor) {
        return new ConcurrentHashMap<>(initialCapacity, loadFactor);
    }

    /**
     * Creates a new {@link ConcurrentHashMap} with the same mappings as the specified map.
     *
     * @param map The initial map whose entries are to be copied into the new ConcurrentHashMap.
     * @param <K> The type of keys maintained by the returned map.
     * @param <V> The type of mapped values.
     * @return A new ConcurrentHashMap instance initialized with the entries from the provided map.
     * @see ConcurrentHashMap#ConcurrentHashMap(Map)
     */
    public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap(Map<? extends K, ? extends V> map) {
        return new ConcurrentHashMap<>(map);
    }

    /**
     * Creates a new empty {@link TreeMap}
     *
     * @param <K> the type of keys maintained by the returned map
     * @param <V> the type of mapped values
     * @return a new empty TreeMap instance
     * @see TreeMap#TreeMap()
     */
    public static <K, V> TreeMap<K, V> newTreeMap() {
        return new TreeMap<>();
    }

    /**
     * Creates a new empty {@link TreeMap} with the specified comparator to order the keys.
     *
     * @param comparator The comparator that will be used to order the keys in the returned TreeMap.
     *                   If null, the natural ordering of the keys will be used.
     * @param <K>        The type of keys maintained by the returned map.
     * @param <V>        The type of mapped values.
     * @return A new empty TreeMap instance with the specified comparator.
     * @see TreeMap#TreeMap(Comparator)
     */
    public static <K, V> TreeMap<K, V> newTreeMap(Comparator<? super K> comparator) {
        return new TreeMap<>(comparator);
    }

    /**
     * Creates a new {@link TreeMap} with the same mappings as the specified map.
     *
     * @param map The initial map whose entries are to be copied into the new TreeMap.
     * @param <K> The type of keys maintained by the returned map.
     * @param <V> The type of mapped values.
     * @return A new TreeMap instance initialized with the entries from the provided map.
     * @see TreeMap#TreeMap(Map)
     */
    public static <K, V> TreeMap<K, V> newTreeMap(Map<? extends K, ? extends V> map) {
        return new TreeMap<>(map);
    }

    /**
     * Creates a new {@link TreeMap} with the same mappings as the specified SortedMap.
     *
     * @param map The initial SortedMap whose entries are to be copied into the new TreeMap.
     * @param <K> The type of keys maintained by the returned map.
     * @param <V> The type of mapped values.
     * @return A new TreeMap instance initialized with the entries from the provided SortedMap.
     * @see TreeMap#TreeMap(SortedMap)
     */
    public static <K, V> TreeMap<K, V> newTreeMap(SortedMap<K, ? extends V> map) {
        return new TreeMap<>(map);
    }

    /**
     * Shallow Clone {@link Map}
     *
     * @param source the source of {@link Map}
     * @param <K>    the {@link Class type} of key
     * @param <V>    the {@link Class type} of value
     * @return non-null
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
     * Creates a new {@link HashMap} with the specified initial capacity and fixed load factor.
     *
     * <p>The fixed load factor is defined by {@link #FIXED_LOAD_FACTOR} which ensures predictable resizing behavior.</p>
     *
     * @param size The initial capacity of the returned HashMap.
     * @param <K>  The type of keys maintained by the returned map.
     * @param <V>  The type of mapped values.
     * @return A new HashMap instance with the specified initial capacity and fixed load factor.
     * @see HashMap#HashMap(int, float)
     */
    public static <K, V> Map<K, V> newFixedHashMap(int size) {
        return newHashMap(size, FIXED_LOAD_FACTOR);
    }

    public static <K, V> Map<K, V> newFixedLinkedHashMap(int size) {
        return newLinkedHashMap(size, FIXED_LOAD_FACTOR);
    }

    /**
     * Converts a collection of elements into an immutable map using the provided mapper function to extract map entries.
     * <p>
     * The resulting map is fixed-size and read-only, created with a predictable capacity based on the input collection size.
     * If the input collection is empty, an empty map is returned.
     * </p>
     *
     * @param values      the collection of elements to be converted into a map
     * @param entryMapper the function that maps each element to a {@link Map.Entry}
     * @param <K>         the type of the keys in the resulting map
     * @param <V>         the type of the values in the resulting map
     * @param <E>         the type of the elements in the input collection
     * @return an immutable map containing the mapped key-value pairs
     * @see #newFixedLinkedHashMap(int)
     * @see java.util.Collections#unmodifiableMap(Map)
     */
    public static <K, V, E> Map<K, V> toFixedMap(Collection<E> values,
                                                 Function<E, Map.Entry<K, V>> entryMapper) {
        int size = size(values);
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
     * Creates a mutable {@link Map.Entry} with the specified key and value.
     *
     * <p>This method returns a modifiable entry implementation that allows updating the value
     * while keeping the same key. It is suitable for use in custom map implementations or other
     * scenarios where a modifiable entry needs to be maintained.</p>
     *
     * @param key   the key to be stored in the entry
     * @param value the value to be associated with the key
     * @param <K>   the type of the key
     * @param <V>   the type of the value
     * @return a new mutable map entry containing the specified key and value
     * @see DefaultEntry
     * @see #immutableEntry(Object, Object)
     */
    public static <K, V> Map.Entry<K, V> ofEntry(K key, V value) {
        return DefaultEntry.of(key, value);
    }

    /**
     * Creates an immutable {@link Map.Entry} with the specified key and value.
     *
     * <p>This method returns a fixed entry implementation that does not allow modification of its contents.
     * The returned entry is suitable for use in read-only contexts, such as populating immutable maps.</p>
     *
     * @param key   the key to be stored in the entry
     * @param value the value to be associated with the key
     * @param <K>   the type of the key
     * @param <V>   the type of the value
     * @return a new immutable map entry containing the specified key and value
     * @see ImmutableEntry
     * @see #ofEntry(Object, Object)
     */
    public static <K, V> Map.Entry<K, V> immutableEntry(K key, V value) {
        return ImmutableEntry.of(key, value);
    }

    private MapUtils() {
    }
}
