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

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.RandomAccess;

import static io.microsphere.collection.ListUtils.of;
import static io.microsphere.invoke.MethodHandlesLookupUtils.findPublicStatic;
import static io.microsphere.util.ArrayUtils.length;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * The utility class for {@link List} for Modern JDK(9+), which supports the feedback if Java Runtime is below JDK 9.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ListUtils
 * @see List
 * @since 1.0.0
 */
public abstract class Lists implements Utils {

    /**
     * The {@link MethodHandle} of {@link List#of()} since JDK 9
     */
    private static final MethodHandle of0MethodHandle = findPublicStatic(List.class, "of");

    /**
     * The {@link MethodHandle} of {@link List#of(Object)} since JDK 9
     */
    private static final MethodHandle of1MethodHandle = findPublicStatic(List.class, "of", Object.class);

    /**
     * The {@link MethodHandle} of {@link List#of(Object, Object)} since JDK 9
     */
    private static final MethodHandle of2MethodHandle = findPublicStatic(List.class, "of", Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link List#of(Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of3MethodHandle = findPublicStatic(List.class, "of", Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link List#of(Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of4MethodHandle = findPublicStatic(List.class, "of", Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link List#of(Object, Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of5MethodHandle = findPublicStatic(List.class, "of", Object.class, Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link List#of(Object, Object, Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of6MethodHandle = findPublicStatic(List.class, "of", Object.class, Object.class, Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link List#of(Object, Object, Object, Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of7MethodHandle = findPublicStatic(List.class, "of", Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link List#of(Object, Object, Object, Object, Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of8MethodHandle = findPublicStatic(List.class, "of", Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link List#of(Object, Object, Object, Object, Object, Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of9MethodHandle = findPublicStatic(List.class, "of", Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link List#of(Object, Object, Object, Object, Object, Object, Object, Object, Object, Object)} since JDK 9
     */
    private static final MethodHandle of10MethodHandle = findPublicStatic(List.class, "of", Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class);

    /**
     * The {@link MethodHandle} of {@link List#of(Object...)} since JDK 9
     */
    private static final MethodHandle ofMethodHandle = findPublicStatic(List.class, "of", Object[].class);

    /**
     * Returns an empty unmodifiable list.
     *
     * <p>The returned list is serializable and implements the {@link RandomAccess}
     * interface if the implementation class allows.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> emptyList = Lists.ofList();
     * }</pre>
     *
     * @param <E> the element type of the list
     * @return an empty unmodifiable list
     */
    @Nonnull
    @Immutable
    public static <E> List<E> ofList() {
        return ofList0(of0MethodHandle);
    }

    static <E> List<E> ofList0(MethodHandle methodHandle) {
        if (methodHandle == null) {
            return emptyList();
        }
        try {
            return (List<E>) methodHandle.invokeExact();
        } catch (Throwable e) {
            return emptyList();
        }
    }

    /**
     * Returns an unmodifiable list containing one element.
     *
     * <p>The returned list is serializable and implements the {@link RandomAccess}
     * interface if the implementation class allows.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> singleElementList = Lists.ofList("Hello");
     * }</pre>
     *
     * @param <E> the {@code List}'s element type
     * @param e1  the single element
     * @return a {@code List} containing the specified element
     * @throws NullPointerException if the element is {@code null}
     */
    @Nonnull
    @Immutable
    public static <E> List<E> ofList(E e1) {
        return ofList1(of1MethodHandle, e1);
    }

    static <E> List<E> ofList1(MethodHandle methodHandle, E e1) {
        if (methodHandle == null) {
            return singletonList(e1);
        }
        try {
            return (List<E>) methodHandle.invokeExact(e1);
        } catch (Throwable e) {
            return singletonList(e1);
        }
    }

    /**
     * Returns an unmodifiable list containing two elements.
     *
     * <p>The returned list is serializable and implements the {@link RandomAccess}
     * interface if the implementation class allows.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> twoElementList = Lists.ofList("Hello", "World");
     * }</pre>
     *
     * @param <E> the {@code List}'s element type
     * @param e1  the first element
     * @param e2  the second element
     * @return a {@code List} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     */
    @Nonnull
    @Immutable
    public static <E> List<E> ofList(E e1, E e2) {
        return ofList2(of2MethodHandle, e1, e2);
    }

    static <E> List<E> ofList2(MethodHandle methodHandle, E e1, E e2) {
        if (methodHandle == null) {
            return of(e1, e2);
        }
        try {
            return (List<E>) methodHandle.invokeExact(e1, e2);
        } catch (Throwable e) {
            return of(e1, e2);
        }
    }

    /**
     * Returns an unmodifiable list containing three elements.
     *
     * <p>The returned list is serializable and implements the {@link RandomAccess}
     * interface if the implementation class allows.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> threeElementList = Lists.ofList("Hello", "World", "Java");
     * }</pre>
     *
     * @param <E> the {@code List}'s element type
     * @param e1  the first element
     * @param e2  the second element
     * @param e3  the third element
     * @return a {@code List} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     */
    @Nonnull
    @Immutable
    public static <E> List<E> ofList(E e1, E e2, E e3) {
        return ofList3(of3MethodHandle, e1, e2, e3);
    }

    static <E> List<E> ofList3(MethodHandle methodHandle, E e1, E e2, E e3) {
        if (methodHandle == null) {
            return of(e1, e2, e3);
        }
        try {
            return (List<E>) methodHandle.invokeExact(e1, e2, e3);
        } catch (Throwable e) {
            return of(e1, e2, e3);
        }
    }

    /**
     * Returns an unmodifiable list containing four elements.
     *
     * <p>The returned list is serializable and implements the {@link RandomAccess}
     * interface if the implementation class allows.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> fourElementList = Lists.ofList("Hello", "World", "Java", "Lists");
     * }</pre>
     *
     * @param <E> the {@code List}'s element type
     * @param e1  the first element
     * @param e2  the second element
     * @param e3  the third element
     * @param e4  the fourth element
     * @return a {@code List} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     */
    @Nonnull
    @Immutable
    public static <E> List<E> ofList(E e1, E e2, E e3, E e4) {
        return ofList4(of4MethodHandle, e1, e2, e3, e4);
    }

    static <E> List<E> ofList4(MethodHandle methodHandle, E e1, E e2, E e3, E e4) {
        if (methodHandle == null) {
            return of(e1, e2, e3, e4);
        }
        try {
            return (List<E>) methodHandle.invokeExact(e1, e2, e3, e4);
        } catch (Throwable e) {
            return of(e1, e2, e3, e4);
        }
    }

    /**
     * Returns an unmodifiable list containing five elements.
     *
     * <p>The returned list is serializable and implements the {@link RandomAccess}
     * interface if the implementation class allows.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> fiveElementList = Lists.ofList("Hello", "World", "Java", "Lists", "Example");
     * }</pre>
     *
     * @param <E> the {@code List}'s element type
     * @param e1  the first element
     * @param e2  the second element
     * @param e3  the third element
     * @param e4  the fourth element
     * @param e5  the fifth element
     * @return a {@code List} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     */
    @Nonnull
    @Immutable
    public static <E> List<E> ofList(E e1, E e2, E e3, E e4, E e5) {
        return ofList5(of5MethodHandle, e1, e2, e3, e4, e5);
    }

    static <E> List<E> ofList5(MethodHandle methodHandle, E e1, E e2, E e3, E e4, E e5) {
        if (methodHandle == null) {
            return of(e1, e2, e3, e4, e5);
        }
        try {
            return (List<E>) methodHandle.invokeExact(e1, e2, e3, e4, e5);
        } catch (Throwable e) {
            return of(e1, e2, e3, e4, e5);
        }
    }

    /**
     * Returns an unmodifiable list containing six elements.
     *
     * @param <E> the {@code List}'s element type
     * @param e1  the first element
     * @param e2  the second element
     * @param e3  the third element
     * @param e4  the fourth element
     * @param e5  the fifth element
     * @param e6  the sixth element
     * @return a {@code List} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     */
    @Nonnull
    @Immutable
    static <E> List<E> ofList(E e1, E e2, E e3, E e4, E e5, E e6) {
        return ofList6(of6MethodHandle, e1, e2, e3, e4, e5, e6);
    }

    static <E> List<E> ofList6(MethodHandle methodHandle, E e1, E e2, E e3, E e4, E e5, E e6) {
        if (methodHandle == null) {
            return of(e1, e2, e3, e4, e5, e6);
        }
        try {
            return (List<E>) methodHandle.invokeExact(e1, e2, e3, e4, e5, e6);
        } catch (Throwable e) {
            return of(e1, e2, e3, e4, e5, e6);
        }
    }

    /**
     * Returns an unmodifiable list containing seven elements.
     *
     * <p>The returned list is serializable and implements the {@link RandomAccess}
     * interface if the implementation class allows.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> sevenElementList = Lists.ofList("Hello", "World", "Java", "Lists", "Example", "Seven", "Elements");
     * }</pre>
     *
     * @param <E> the {@code List}'s element type
     * @param e1  the first element
     * @param e2  the second element
     * @param e3  the third element
     * @param e4  the fourth element
     * @param e5  the fifth element
     * @param e6  the sixth element
     * @param e7  the seventh element
     * @return a {@code List} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     */
    @Nonnull
    @Immutable
    public static <E> List<E> ofList(E e1, E e2, E e3, E e4, E e5, E e6, E e7) {
        return ofList7(of7MethodHandle, e1, e2, e3, e4, e5, e6, e7);
    }

    static <E> List<E> ofList7(MethodHandle methodHandle, E e1, E e2, E e3, E e4, E e5, E e6, E e7) {
        if (methodHandle == null) {
            return of(e1, e2, e3, e4, e5, e6, e7);
        }
        try {
            return (List<E>) methodHandle.invokeExact(e1, e2, e3, e4, e5, e6, e7);
        } catch (Throwable e) {
            return of(e1, e2, e3, e4, e5, e6, e7);
        }
    }

    /**
     * Returns an unmodifiable list containing eight elements.
     *
     * <p>The returned list is serializable and implements the {@link RandomAccess}
     * interface if the implementation class allows.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> eightElementList = Lists.ofList("Hello", "World", "Java", "Lists", "Example", "Eight", "Elements", "Demo");
     * }</pre>
     *
     * @param <E> the {@code List}'s element type
     * @param e1  the first element
     * @param e2  the second element
     * @param e3  the third element
     * @param e4  the fourth element
     * @param e5  the fifth element
     * @param e6  the sixth element
     * @param e7  the seventh element
     * @param e8  the eighth element
     * @return a {@code List} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     */
    @Nonnull
    @Immutable
    public static <E> List<E> ofList(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8) {
        return ofList8(of8MethodHandle, e1, e2, e3, e4, e5, e6, e7, e8);
    }

    static <E> List<E> ofList8(MethodHandle methodHandle, E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8) {
        if (methodHandle == null) {
            return of(e1, e2, e3, e4, e5, e6, e7, e8);
        }
        try {
            return (List<E>) methodHandle.invokeExact(e1, e2, e3, e4, e5, e6, e7, e8);
        } catch (Throwable e) {
            return of(e1, e2, e3, e4, e5, e6, e7, e8);
        }
    }

    /**
     * Returns an unmodifiable list containing nine elements.
     *
     * <p>The returned list is serializable and implements the {@link RandomAccess}
     * interface if the implementation class allows.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> nineElementList = Lists.ofList("Hello", "World", "Java", "Lists", "Example", "Nine", "Elements", "Demo", "Test");
     * }</pre>
     *
     * @param <E> the {@code List}'s element type
     * @param e1  the first element
     * @param e2  the second element
     * @param e3  the third element
     * @param e4  the fourth element
     * @param e5  the fifth element
     * @param e6  the sixth element
     * @param e7  the seventh element
     * @param e8  the eighth element
     * @param e9  the ninth element
     * @return a {@code List} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     */
    @Nonnull
    @Immutable
    public static <E> List<E> ofList(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9) {
        return ofList9(of9MethodHandle, e1, e2, e3, e4, e5, e6, e7, e8, e9);
    }

    static <E> List<E> ofList9(MethodHandle methodHandle, E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9) {
        if (methodHandle == null) {
            return of(e1, e2, e3, e4, e5, e6, e7, e8, e9);
        }
        try {
            return (List<E>) methodHandle.invokeExact(e1, e2, e3, e4, e5, e6, e7, e8, e9);
        } catch (Throwable e) {
            return of(e1, e2, e3, e4, e5, e6, e7, e8, e9);
        }
    }

    /**
     * Returns an unmodifiable list containing ten elements.
     *
     * @param <E> the {@code List}'s element type
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
     * @return a {@code List} containing the specified elements
     * @throws NullPointerException if an element is {@code null}
     */
    @Nonnull
    @Immutable
    static <E> List<E> ofList(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10) {
        return ofList10(of10MethodHandle, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
    }

    static <E> List<E> ofList10(MethodHandle methodHandle, E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10) {
        if (methodHandle == null) {
            return of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
        }
        try {
            return (List<E>) methodHandle.invokeExact(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
        } catch (Throwable e) {
            return of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
        }
    }

    /**
     * Returns an unmodifiable list containing the specified elements.
     *
     * <p>The returned list is serializable and implements the {@link RandomAccess}
     * interface if the implementation class allows.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> emptyList = Lists.ofList(); // returns an empty list
     *     List<String> singleElementList = Lists.ofList("Hello"); // returns a list with one element
     *     List<String> multiElementList = Lists.ofList("Hello", "World", "Java"); // returns a list with multiple elements
     * }</pre>
     *
     * @param <E>      the {@code List}'s element type
     * @param elements the elements to be included in the list
     * @return a {@code List} containing the specified elements
     * @throws NullPointerException if any element is {@code null}
     */
    @Nonnull
    @Immutable
    public static <E> List<E> ofList(E... elements) {
        if (length(elements) < 1) {
            return ofList();
        }
        return ofListElements(ofMethodHandle, elements);
    }

    static <E> List<E> ofListElements(MethodHandle methodHandle, E[] elements) {
        if (methodHandle == null) {
            return of(elements);
        }
        try {
            return (List<E>) methodHandle.invokeExact(elements);
        } catch (Throwable e) {
            return of(elements);
        }
    }

    private Lists() {
    }
}
