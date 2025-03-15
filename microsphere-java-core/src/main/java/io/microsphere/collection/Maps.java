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

import io.microsphere.util.BaseUtils;

import java.lang.invoke.MethodHandle;
import java.util.Map;

import static io.microsphere.collection.MapUtils.of;
import static io.microsphere.invoke.MethodHandlesLookupUtils.findPublicStatic;
import static io.microsphere.util.ArrayUtils.length;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

/**
 * The utility class for {@link Map} for Modern JDK(9+), which supports the feedback if Java Runtime is below JDK 9.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see MapUtils
 * @since 1.0.0
 */
public abstract class Maps extends BaseUtils {

    /**
     * The {@link MethodHandle} of {@link Map#of()} since JDK 9
     */
    private static final MethodHandle of0MethodHandle = findPublicStatic(Map.class, "of");

    /**
     * The {@link MethodHandle} of {@link Map#of(Object, Object)} since JDK 9
     */
    private static final MethodHandle of1MethodHandle = findPublicStatic(Map.class, "of", Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link Map#of(Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of2MethodHandle = findPublicStatic(Map.class, "of", Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link Map#of(Object, Object, Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of3MethodHandle = findPublicStatic(Map.class, "of", Object.class, Object.class, Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link Map#of(Object, Object, Object, Object, Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of4MethodHandle = findPublicStatic(Map.class, "of", Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link Map#of(Object, Object, Object, Object, Object, Object, Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of5MethodHandle = findPublicStatic(Map.class, "of", Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link Map#of(Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of6MethodHandle = findPublicStatic(Map.class, "of", Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link Map#of(Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of7MethodHandle = findPublicStatic(Map.class, "of", Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link Map#of(Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of8MethodHandle = findPublicStatic(Map.class, "of", Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link Map#of(Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of9MethodHandle = findPublicStatic(Map.class, "of", Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link Map#of(Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of10MethodHandle = findPublicStatic(Map.class, "of", Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link Map#ofEntries(Map.Entry...)} since JDK 9
     */
    private static final MethodHandle ofEntriesMethodHandle = findPublicStatic(Map.class, "ofEntries", Map.Entry[].class);

    /**
     * Returns an unmodifiable map containing zero mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @return an empty {@code Map}
     */
    @SuppressWarnings("unchecked")
    static <K, V> Map<K, V> ofMap() {
        if (of0MethodHandle == null) {
            return emptyMap();
        }
        try {
            return (Map<K, V>) of0MethodHandle.invokeExact();
        } catch (Throwable e) {
            return emptyMap();
        }
    }

    /**
     * Returns an unmodifiable map containing a single mapping.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param k1  the mapping's key
     * @param v1  the mapping's value
     * @return a {@code Map} containing the specified mapping
     * @throws NullPointerException if the key or the value is {@code null}
     */
    static <K, V> Map<K, V> ofMap(K k1, V v1) {
        if (of1MethodHandle == null) {
            return singletonMap(k1, v1);
        }
        try {
            return (Map<K, V>) of1MethodHandle.invokeExact(k1, v1);
        } catch (Throwable e) {
            return singletonMap(k1, v1);
        }
    }

    /**
     * Returns an unmodifiable map containing two mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param k1  the first mapping's key
     * @param v1  the first mapping's value
     * @param k2  the second mapping's key
     * @param v2  the second mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if the keys are duplicates
     * @throws NullPointerException     if any key or value is {@code null}
     */
    static <K, V> Map<K, V> ofMap(K k1, V v1, K k2, V v2) {
        if (of2MethodHandle == null) {
            return of(k1, v1, k2, v2);
        }
        try {
            return (Map<K, V>) of2MethodHandle.invokeExact(k1, v1, k2, v2);
        } catch (Throwable e) {
            return of(k1, v1, k2, v2);
        }
    }

    /**
     * Returns an unmodifiable map containing three mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param k1  the first mapping's key
     * @param v1  the first mapping's value
     * @param k2  the second mapping's key
     * @param v2  the second mapping's value
     * @param k3  the third mapping's key
     * @param v3  the third mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException     if any key or value is {@code null}
     */
    static <K, V> Map<K, V> ofMap(K k1, V v1, K k2, V v2, K k3, V v3) {
        if (of3MethodHandle == null) {
            return of(k1, v1, k2, v2, k3, v3);
        }
        try {
            return (Map<K, V>) of3MethodHandle.invokeExact(k1, v1, k2, v2, k3, v3);
        } catch (Throwable e) {
            return of(k1, v1, k2, v2, k3, v3);
        }
    }

    /**
     * Returns an unmodifiable map containing four mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param k1  the first mapping's key
     * @param v1  the first mapping's value
     * @param k2  the second mapping's key
     * @param v2  the second mapping's value
     * @param k3  the third mapping's key
     * @param v3  the third mapping's value
     * @param k4  the fourth mapping's key
     * @param v4  the fourth mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException     if any key or value is {@code null}
     */
    static <K, V> Map<K, V> ofMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        if (of4MethodHandle == null) {
            return of(k1, v1, k2, v2, k3, v3, k4, v4);
        }
        try {
            return (Map<K, V>) of4MethodHandle.invokeExact(k1, v1, k2, v2, k3, v3, k4, v4);
        } catch (Throwable e) {
            return of(k1, v1, k2, v2, k3, v3, k4, v4);
        }
    }

    /**
     * Returns an unmodifiable map containing five mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param k1  the first mapping's key
     * @param v1  the first mapping's value
     * @param k2  the second mapping's key
     * @param v2  the second mapping's value
     * @param k3  the third mapping's key
     * @param v3  the third mapping's value
     * @param k4  the fourth mapping's key
     * @param v4  the fourth mapping's value
     * @param k5  the fifth mapping's key
     * @param v5  the fifth mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException     if any key or value is {@code null}
     */
    static <K, V> Map<K, V> ofMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        if (of5MethodHandle == null) {
            return of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
        }
        try {
            return (Map<K, V>) of5MethodHandle.invokeExact(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
        } catch (Throwable e) {
            return of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
        }
    }

    /**
     * Returns an unmodifiable map containing six mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param k1  the first mapping's key
     * @param v1  the first mapping's value
     * @param k2  the second mapping's key
     * @param v2  the second mapping's value
     * @param k3  the third mapping's key
     * @param v3  the third mapping's value
     * @param k4  the fourth mapping's key
     * @param v4  the fourth mapping's value
     * @param k5  the fifth mapping's key
     * @param v5  the fifth mapping's value
     * @param k6  the sixth mapping's key
     * @param v6  the sixth mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException     if any key or value is {@code null}
     */
    static <K, V> Map<K, V> ofMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        if (of6MethodHandle == null) {
            return of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
        }
        try {
            return (Map<K, V>) of6MethodHandle.invokeExact(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
        } catch (Throwable e) {
            return of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
        }
    }

    /**
     * Returns an unmodifiable map containing seven mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param k1  the first mapping's key
     * @param v1  the first mapping's value
     * @param k2  the second mapping's key
     * @param v2  the second mapping's value
     * @param k3  the third mapping's key
     * @param v3  the third mapping's value
     * @param k4  the fourth mapping's key
     * @param v4  the fourth mapping's value
     * @param k5  the fifth mapping's key
     * @param v5  the fifth mapping's value
     * @param k6  the sixth mapping's key
     * @param v6  the sixth mapping's value
     * @param k7  the seventh mapping's key
     * @param v7  the seventh mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException     if any key or value is {@code null}
     */
    static <K, V> Map<K, V> ofMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        if (of7MethodHandle == null) {
            return of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
        }
        try {
            return (Map<K, V>) of7MethodHandle.invokeExact(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
        } catch (Throwable e) {
            return of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
        }
    }

    /**
     * Returns an unmodifiable map containing eight mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param k1  the first mapping's key
     * @param v1  the first mapping's value
     * @param k2  the second mapping's key
     * @param v2  the second mapping's value
     * @param k3  the third mapping's key
     * @param v3  the third mapping's value
     * @param k4  the fourth mapping's key
     * @param v4  the fourth mapping's value
     * @param k5  the fifth mapping's key
     * @param v5  the fifth mapping's value
     * @param k6  the sixth mapping's key
     * @param v6  the sixth mapping's value
     * @param k7  the seventh mapping's key
     * @param v7  the seventh mapping's value
     * @param k8  the eighth mapping's key
     * @param v8  the eighth mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException     if any key or value is {@code null}
     */
    static <K, V> Map<K, V> ofMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7,
                                  K k8, V v8) {
        if (of8MethodHandle == null) {
            return of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
        }
        try {
            return (Map<K, V>) of8MethodHandle.invokeExact(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
        } catch (Throwable e) {
            return of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
        }
    }

    /**
     * Returns an unmodifiable map containing nine mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param k1  the first mapping's key
     * @param v1  the first mapping's value
     * @param k2  the second mapping's key
     * @param v2  the second mapping's value
     * @param k3  the third mapping's key
     * @param v3  the third mapping's value
     * @param k4  the fourth mapping's key
     * @param v4  the fourth mapping's value
     * @param k5  the fifth mapping's key
     * @param v5  the fifth mapping's value
     * @param k6  the sixth mapping's key
     * @param v6  the sixth mapping's value
     * @param k7  the seventh mapping's key
     * @param v7  the seventh mapping's value
     * @param k8  the eighth mapping's key
     * @param v8  the eighth mapping's value
     * @param k9  the ninth mapping's key
     * @param v9  the ninth mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException     if any key or value is {@code null}
     */
    static <K, V> Map<K, V> ofMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7,
                                  K k8, V v8, K k9, V v9) {
        if (of9MethodHandle == null) {
            return of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
        }
        try {
            return (Map<K, V>) of9MethodHandle.invokeExact(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
        } catch (Throwable e) {
            return of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
        }
    }

    /**
     * Returns an unmodifiable map containing ten mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param k1  the first mapping's key
     * @param v1  the first mapping's value
     * @param k2  the second mapping's key
     * @param v2  the second mapping's value
     * @param k3  the third mapping's key
     * @param v3  the third mapping's value
     * @param k4  the fourth mapping's key
     * @param v4  the fourth mapping's value
     * @param k5  the fifth mapping's key
     * @param v5  the fifth mapping's value
     * @param k6  the sixth mapping's key
     * @param v6  the sixth mapping's value
     * @param k7  the seventh mapping's key
     * @param v7  the seventh mapping's value
     * @param k8  the eighth mapping's key
     * @param v8  the eighth mapping's value
     * @param k9  the ninth mapping's key
     * @param v9  the ninth mapping's value
     * @param k10 the tenth mapping's key
     * @param v10 the tenth mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException     if any key or value is {@code null}
     */
    static <K, V> Map<K, V> ofMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7,
                                  K k8, V v8, K k9, V v9, K k10, V v10) {
        if (of10MethodHandle == null) {
            return of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10);
        }
        try {
            return (Map<K, V>) of10MethodHandle.invokeExact(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10);
        } catch (Throwable e) {
            return of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10);
        }
    }

    /**
     * Returns an unmodifiable map containing keys and values extracted from the given entries.
     * The entries themselves are not stored in the map.
     *
     * @param <K>     the {@code Map}'s key type
     * @param <V>     the {@code Map}'s value type
     * @param entries {@code Map.Entry}s containing the keys and values from which the map is populated
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException     if any entry, key, or value is {@code null}, or if
     *                                  the {@code entries} array is {@code null}
     * @apiNote It is convenient to create the map entries using the {@link Map#entry Map.entry()} method.
     * For example,
     *
     * <pre>{@code
     *     import static java.util.Map.entry;
     *
     *     Map<Integer,String> map = Map.ofEntries(
     *         entry(1, "a"),
     *         entry(2, "b"),
     *         entry(3, "c"),
     *         ...
     *         entry(26, "z"));
     * }</pre>
     * @see Map#entry Map.entry()
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    static <K, V> Map<K, V> ofMap(Map.Entry<? extends K, ? extends V>... entries) {
        if (length(entries) < 1) {
            return emptyMap();
        }
        if (ofEntriesMethodHandle == null) {
            return of(entries);
        }
        try {
            return (Map<K, V>) ofEntriesMethodHandle.invokeExact(entries);
        } catch (Throwable e) {
            return of(entries);
        }
    }
}
