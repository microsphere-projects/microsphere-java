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

import java.util.Map;

/**
 * An immutable implementation of {@link Map.Entry} that stores a fixed key-value pair.
 *
 * <p>This class provides a read-only entry suitable for use in immutable maps. Once created,
 * the key and value cannot be changed, and calling {@link #setValue(Object)} will throw an
 * {@link UnsupportedOperationException}.</p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * ImmutableEntry<String, Integer> entry = ImmutableEntry.of("age", 30);
 * System.out.println(entry.getKey());   // Output: age
 * System.out.println(entry.getValue()); // Output: 30
 * 
 * // The following line would throw an exception:
 * // entry.setValue(25); // throws UnsupportedOperationException
 * }</pre>
 *
 * @param <K> the type of the key
 * @param <V> the type of the value
 * @see MapUtils#ofEntry(Object, Object)
 * @see MapUtils#immutableEntry(Object, Object)
 */
public class ImmutableEntry<K, V> extends DefaultEntry<K, V> {

    public ImmutableEntry(K key, V value) {
        super(key, value);
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException("ReadOnly Entry can't be modified");
    }

    /**
     * Create an immutable entry with the specified key and value.
     *
     * @param key   the key
     * @param value the value
     * @param <K>   the type of the key
     * @param <V>   the type of the value
     * @return an immutable entry with the specified key and value
     */
    public static <K, V> ImmutableEntry<K, V> of(K key, V value) {
        return new ImmutableEntry<>(key, value);
    }
}
