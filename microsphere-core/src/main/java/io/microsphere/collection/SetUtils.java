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
package io.github.microsphere.collection;

import io.github.microsphere.util.BaseUtils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import static io.github.microsphere.collection.CollectionUtils.toIterable;
import static io.github.microsphere.collection.MapUtils.MIN_LOAD_FACTOR;
import static io.github.microsphere.util.ArrayUtils.length;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

/**
 * The utilities class for Java {@link Set}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Set
 * @since 1.0.0
 */
public abstract class SetUtils extends BaseUtils {

    public static boolean isSet(Iterable<?> values) {
        return values instanceof Set;
    }

    /**
     * Build a read-only {@link Set} from the given {@lin Enumeration} values
     *
     * @param values one or more values
     * @param <E>    the type of <code>values</code>
     * @return non-null read-only {@link Set}
     */
    @Nonnull
    public static <E> Set<E> of(Enumeration<E> values) {
        return of(toIterable(values));
    }

    /**
     * Convert to multiple values to be {@link LinkedHashSet}
     *
     * @param values one or more values
     * @param <E>    the type of <code>values</code>
     * @return read-only {@link Set}
     */
    public static <E> Set<E> of(Iterable<E> values) {
        return unmodifiableSet(newLinkedHashSet(values));
    }


    /**
     * Convert to multiple values to be {@link LinkedHashSet}
     *
     * @param values one or more values
     * @param <E>    the type of <code>values</code>
     * @return read-only {@link Set}
     */
    public static <E> Set<E> of(E... values) {
        return asSet(values);
    }

    /**
     * Convert to multiple values to be {@link LinkedHashSet}
     *
     * @param values one or more values
     * @param <E>    the type of <code>values</code>
     * @return read-only {@link Set}
     */
    public static <E> Set<E> asSet(Iterable<E> values) {
        return unmodifiableSet(newLinkedHashSet(values));
    }

    /**
     * Convert to multiple values to be {@link LinkedHashSet}
     *
     * @param values one or more values
     * @param <E>    the type of <code>values</code>
     * @return read-only {@link Set}
     */
    public static <E> Set<E> asSet(E... values) {
        int size = length(values);
        if (size < 1) {
            return emptySet();
        }

        Set<E> elements = new LinkedHashSet<>(size, MIN_LOAD_FACTOR);

        for (int i = 0; i < size; i++) {
            elements.add(values[i]);
        }
        return unmodifiableSet(elements);
    }

    public static <E> Set<E> newHashSet(Iterable<E> values) {
        Set<E> set = newHashSet();
        for (E value : values) {
            set.add(value);
        }
        return set;
    }

    public static <E> Set<E> newHashSet(Collection<? extends E> values) {
        return new HashSet(values);
    }

    public static <E> Set<E> newHashSet() {
        return new HashSet<>();
    }

    public static <E> Set<E> newHashSet(int initialCapacity) {
        return new HashSet<>(initialCapacity);
    }

    public static <E> Set<E> newHashSet(int initialCapacity, float loadFactor) {
        return new HashSet<>(initialCapacity, loadFactor);
    }

    public static <E> Set<E> newLinkedHashSet(Iterable<E> values) {
        return newLinkedHashSet(values.iterator());
    }

    public static <E> Set<E> newLinkedHashSet(Iterator<E> values) {
        Set<E> set = newLinkedHashSet();
        while (values.hasNext()) {
            E value = values.next();
            set.add(value);
        }
        return set;
    }

    public static <E> Set<E> newLinkedHashSet(Collection<? extends E> values) {
        return new LinkedHashSet(values);
    }

    public static <E> Set<E> newLinkedHashSet() {
        return new LinkedHashSet<>();
    }

    public static <E> Set<E> newLinkedHashSet(int initialCapacity) {
        return new LinkedHashSet<>(initialCapacity);
    }

    public static <E> Set<E> newLinkedHashSet(int initialCapacity, float loadFactor) {
        return new LinkedHashSet<>(initialCapacity, loadFactor);
    }
}
