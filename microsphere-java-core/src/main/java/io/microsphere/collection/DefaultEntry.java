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
import java.util.Objects;

/**
 * A default implementation of the {@link Map.Entry} interface, representing a key-value pair.
 * This class provides an immutable key and a mutable value, allowing for updates to the value
 * while keeping the same key. It is suitable for use in custom map implementations or other
 * scenarios where a modifiable entry needs to be maintained.
 *
 * @param <K> the type of the key
 * @param <V> the type of the value
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Map.Entry
 * @since 1.0.0
 */
public class DefaultEntry<K, V> implements Map.Entry<K, V> {

    private final K key;

    private V value;

    public DefaultEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Create a new instance of {@link DefaultEntry} with the specified key and value.
     *
     * @param key   the key
     * @param value the value
     * @param <K>   the type of the key
     * @param <V>   the type of the value
     * @return a new instance of {@link DefaultEntry}
     */
    public static <K, V> DefaultEntry<K, V> of(K key, V value) {
        return new DefaultEntry<>(key, value);
    }

    @Override
    public final K getKey() {
        return key;
    }

    @Override
    public final V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        V oldValue = this.value;
        this.value = value;
        return oldValue;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultEntry)) return false;

        DefaultEntry<?, ?> that = (DefaultEntry<?, ?>) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(key);
        result = 31 * result + Objects.hashCode(value);
        return result;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }
}
