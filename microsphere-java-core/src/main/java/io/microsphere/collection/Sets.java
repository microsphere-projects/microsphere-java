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

import java.lang.invoke.MethodHandle;
import java.util.Set;

import static io.microsphere.invoke.MethodHandlesLookupUtils.findPublicStatic;
import static io.microsphere.util.ArrayUtils.length;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

/**
 * The utility class for {@link Set} for Modern JDK(9+), which supports the feedback if Java Runtime is below JDK 9.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see SetUtils
 * @since 1.0.0
 */
public abstract class Sets extends SetUtils {

    /**
     * The {@link MethodHandle} of {@link Set#of()} since JDK 9
     */
    private static final MethodHandle of0MethodHandle = findPublicStatic(Set.class, "of");

    /**
     * The {@link MethodHandle} of {@link Set#of(Object)} since JDK 9
     */
    private static final MethodHandle of1MethodHandle = findPublicStatic(Set.class, "of", Object.class);

    /**
     * The {@link MethodHandle} of {@link Set#of(Object, Object)} since JDK 9
     */
    private static final MethodHandle of2MethodHandle = findPublicStatic(Set.class, "of", Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link Set#of(Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of3MethodHandle = findPublicStatic(Set.class, "of", Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link Set#of(Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of4MethodHandle = findPublicStatic(Set.class, "of", Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link Set#of(Object, Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of5MethodHandle = findPublicStatic(Set.class, "of", Object.class, Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link Set#of(Object, Object, Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of6MethodHandle = findPublicStatic(Set.class, "of", Object.class, Object.class, Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link Set#of(Object, Object, Object, Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of7MethodHandle = findPublicStatic(Set.class, "of", Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link Set#of(Object, Object, Object, Object, Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of8MethodHandle = findPublicStatic(Set.class, "of", Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link Set#of(Object, Object, Object, Object, Object, Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of9MethodHandle = findPublicStatic(Set.class, "of", Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link Set#of(Object, Object, Object, Object, Object, Object, Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of10MethodHandle = findPublicStatic(Set.class, "of", Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link Set#of(Object...)} since JDK 9
     */
    private static final MethodHandle ofMethodHandle = findPublicStatic(Set.class, "of", Object[].class);

    /**
     * Returns an unmodifiable set containing zero elements.
     *
     * @param <E> the {@code Set}'s element type
     * @return an empty {@code Set}
     */
    static <E> Set<E> ofSet() {
        if (of0MethodHandle == null) {
            return emptySet();
        }
        try {
            return (Set<E>) of0MethodHandle.invokeExact();
        } catch (Throwable e) {
            return emptySet();
        }
    }

    /**
     * Returns an unmodifiable set containing one element.
     *
     * @param <E> the {@code Set}'s element type
     * @param e1  the single element
     * @return a {@code Set} containing the specified element
     * @throws NullPointerException if the element is {@code null}
     */
    static <E> Set<E> ofSet(E e1) {
        if (of1MethodHandle == null) {
            return singleton(e1);
        }
        try {
            return (Set<E>) of1MethodHandle.invokeExact(e1);
        } catch (Throwable e) {
            return singleton(e1);
        }
    }

    /**
     * Returns an unmodifiable set containing two elements.
     *
     * @param <E> the {@code Set}'s element type
     * @param e1  the first element
     * @param e2  the second element
     * @return a {@code Set} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     */
    static <E> Set<E> ofSet(E e1, E e2) {
        if (of2MethodHandle == null) {
            return of(e1, e2);
        }
        try {
            return (Set<E>) of2MethodHandle.invokeExact(e1, e2);
        } catch (Throwable e) {
            return of(e1, e2);
        }
    }

    /**
     * Returns an unmodifiable set containing three elements.
     *
     * @param <E> the {@code Set}'s element type
     * @param e1  the first element
     * @param e2  the second element
     * @param e3  the third element
     * @return a {@code Set} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     */
    static <E> Set<E> ofSet(E e1, E e2, E e3) {
        if (of3MethodHandle == null) {
            return of(e1, e2, e3);
        }
        try {
            return (Set<E>) of3MethodHandle.invokeExact(e1, e2, e3);
        } catch (Throwable e) {
            return of(e1, e2, e3);
        }
    }

    /**
     * Returns an unmodifiable set containing four elements.
     *
     * @param <E> the {@code Set}'s element type
     * @param e1  the first element
     * @param e2  the second element
     * @param e3  the third element
     * @param e4  the fourth element
     * @return a {@code Set} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     */
    static <E> Set<E> ofSet(E e1, E e2, E e3, E e4) {
        if (of4MethodHandle == null) {
            return of(e1, e2, e3, e4);
        }
        try {
            return (Set<E>) of4MethodHandle.invokeExact(e1, e2, e3, e4);
        } catch (Throwable e) {
            return of(e1, e2, e3, e4);
        }
    }

    /**
     * Returns an unmodifiable set containing five elements.
     *
     * @param <E> the {@code Set}'s element type
     * @param e1  the first element
     * @param e2  the second element
     * @param e3  the third element
     * @param e4  the fourth element
     * @param e5  the fifth element
     * @return a {@code Set} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     */
    static <E> Set<E> ofSet(E e1, E e2, E e3, E e4, E e5) {
        if (of5MethodHandle == null) {
            return of(e1, e2, e3, e4, e5);
        }
        try {
            return (Set<E>) of5MethodHandle.invokeExact(e1, e2, e3, e4, e5);
        } catch (Throwable e) {
            return of(e1, e2, e3, e4, e5);
        }
    }

    /**
     * Returns an unmodifiable set containing six elements.
     *
     * @param <E> the {@code Set}'s element type
     * @param e1  the first element
     * @param e2  the second element
     * @param e3  the third element
     * @param e4  the fourth element
     * @param e5  the fifth element
     * @param e6  the sixth element
     * @return a {@code Set} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     */
    static <E> Set<E> ofSet(E e1, E e2, E e3, E e4, E e5, E e6) {
        if (of6MethodHandle == null) {
            return of(e1, e2, e3, e4, e5, e6);
        }
        try {
            return (Set<E>) of6MethodHandle.invokeExact(e1, e2, e3, e4, e5, e6);
        } catch (Throwable e) {
            return of(e1, e2, e3, e4, e5, e6);
        }
    }

    /**
     * Returns an unmodifiable set containing seven elements.
     *
     * @param <E> the {@code Set}'s element type
     * @param e1  the first element
     * @param e2  the second element
     * @param e3  the third element
     * @param e4  the fourth element
     * @param e5  the fifth element
     * @param e6  the sixth element
     * @param e7  the seventh element
     * @return a {@code Set} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     */
    static <E> Set<E> ofSet(E e1, E e2, E e3, E e4, E e5, E e6, E e7) {
        if (of7MethodHandle == null) {
            return of(e1, e2, e3, e4, e5, e6, e7);
        }
        try {
            return (Set<E>) of7MethodHandle.invokeExact(e1, e2, e3, e4, e5, e6, e7);
        } catch (Throwable e) {
            return of(e1, e2, e3, e4, e5, e6, e7);
        }
    }

    /**
     * Returns an unmodifiable set containing eight elements.
     *
     * @param <E> the {@code Set}'s element type
     * @param e1  the first element
     * @param e2  the second element
     * @param e3  the third element
     * @param e4  the fourth element
     * @param e5  the fifth element
     * @param e6  the sixth element
     * @param e7  the seventh element
     * @param e8  the eighth element
     * @return a {@code Set} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     */
    static <E> Set<E> ofSet(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8) {
        if (of8MethodHandle == null) {
            return of(e1, e2, e3, e4, e5, e6, e7, e8);
        }
        try {
            return (Set<E>) of8MethodHandle.invokeExact(e1, e2, e3, e4, e5, e6, e7, e8);
        } catch (Throwable e) {
            return of(e1, e2, e3, e4, e5, e6, e7, e8);
        }
    }

    /**
     * Returns an unmodifiable set containing nine elements.
     *
     * @param <E> the {@code Set}'s element type
     * @param e1  the first element
     * @param e2  the second element
     * @param e3  the third element
     * @param e4  the fourth element
     * @param e5  the fifth element
     * @param e6  the sixth element
     * @param e7  the seventh element
     * @param e8  the eighth element
     * @param e9  the ninth element
     * @return a {@code Set} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     */
    static <E> Set<E> ofSet(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9) {
        if (of9MethodHandle == null) {
            return of(e1, e2, e3, e4, e5, e6, e7, e8, e9);
        }
        try {
            return (Set<E>) of9MethodHandle.invokeExact(e1, e2, e3, e4, e5, e6, e7, e8, e9);
        } catch (Throwable e) {
            return of(e1, e2, e3, e4, e5, e6, e7, e8, e9);
        }
    }

    /**
     * Returns an unmodifiable set containing ten elements.
     *
     * @param <E> the {@code Set}'s element type
     * @param e1  the first element
     * @param e2  the second element
     * @param e3  the third element
     * @param e4  the fourth element
     * @param e5  the fifth element
     * @param e6  the sixth element
     * @param e7  the seventh element
     * @param e8  the eighth element
     * @param e9  the ninth element
     * @param e10 the tenth element
     * @return a {@code Set} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     */
    static <E> Set<E> ofSet(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10) {
        if (of10MethodHandle == null) {
            return of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
        }
        try {
            return (Set<E>) of10MethodHandle.invokeExact(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
        } catch (Throwable e) {
            return of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
        }
    }

    /**
     * Returns an unmodifiable set containing an arbitrary number of elements.
     *
     * @param elements the elements array
     * @param <E>      the element type
     * @return non-null
     * @see SetUtils#ofSet(E...)
     */
    public static <E> Set<E> ofSet(E... elements) {
        if (length(elements) < 1) {
            return ofSet();
        }
        if (ofMethodHandle == null) {
            return of(elements);
        }
        try {
            return (Set<E>) ofMethodHandle.invokeExact(elements);
        } catch (Throwable e) {
            return of(elements);
        }
    }

    private Sets() {
    }
}
