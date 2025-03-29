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

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import static io.microsphere.collection.CollectionUtils.size;
import static io.microsphere.collection.MapUtils.FIXED_LOAD_FACTOR;
import static io.microsphere.util.ArrayUtils.length;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableSet;

/**
 * The utilities class for Java {@link Set}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Set
 * @since 1.0.0
 */
public abstract class SetUtils {

    public static boolean isSet(@Nullable Iterable<?> elements) {
        return elements instanceof Set;
    }

    /**
     * Convert to multiple elements to be {@link LinkedHashSet}
     *
     * @param elements one or more elements
     * @param <E>      the type of <code>elements</code>
     * @return read-only {@link Set}
     */
    public static <E> Set<E> of(E... elements) {
        return ofSet(elements);
    }

    /**
     * Convert to multiple elements to be {@link LinkedHashSet}
     *
     * @param elements one or more elements
     * @param <E>      the type of <code>elements</code>
     * @return read-only {@link Set}
     */
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
     * Build a read-only {@link Set} from the given {@lin Enumeration} elements
     *
     * @param elements one or more elements
     * @param <E>      the type of <code>elements</code>
     * @return non-null read-only {@link Set}
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
     * Convert to multiple elements to be {@link LinkedHashSet}
     *
     * @param elements one or more elements
     * @param <E>      the type of <code>elements</code>
     * @return read-only {@link Set}
     */
    public static <E> Set<E> ofSet(Iterable<E> elements) {
        if (elements == null) {
            return emptySet();
        }
        if (elements instanceof Collection) {
            return ofSet((Collection) elements);
        }
        return unmodifiableSet(newLinkedHashSet(elements));
    }

    public static <T> Set<T> ofSet(Collection<T> elements) {
        return ofSet(elements, (T[]) null);
    }

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

    public static <E> Set<E> newHashSet(Iterable<E> elements) {
        Set<E> set = newHashSet();
        for (E value : elements) {
            set.add(value);
        }
        return set;
    }

    public static <E> Set<E> newHashSet(Collection<E> elements) {
        return new HashSet(elements);
    }

    public static <E> Set<E> newHashSet(E... elements) {
        int length = length(elements);
        Set<E> set = newHashSet(length);
        for (int i = 0; i < length; i++) {
            set.add(elements[i]);
        }
        return set;
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

    public static <E> Set<E> newLinkedHashSet(Iterable<E> elements) {
        return newLinkedHashSet(elements.iterator());
    }

    public static <E> Set<E> newLinkedHashSet(Iterator<E> elements) {
        Set<E> set = newLinkedHashSet();
        while (elements.hasNext()) {
            E value = elements.next();
            set.add(value);
        }
        return set;
    }

    public static <E> Set<E> newLinkedHashSet(Collection<E> elements) {
        return new LinkedHashSet(elements);
    }

    public static <E> Set<E> newLinkedHashSet(E... elements) {
        int length = length(elements);
        Set<E> set = newLinkedHashSet(length);
        for (int i = 0; i < length; i++) {
            set.add(elements[i]);
        }
        return set;
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
