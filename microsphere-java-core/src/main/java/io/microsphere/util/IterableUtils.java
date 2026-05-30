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

package io.microsphere.util;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;

import java.util.function.Consumer;

import static io.microsphere.util.ClassUtils.isAssignableFrom;

/**
 * The utility class for {@link Iterable}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Iterable
 * @since 1.0.0
 */
public abstract class IterableUtils implements Utils {

    /**
     * Checks if the given object is an instance of {@link Iterable}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<String> list = Arrays.asList("a", "b", "c");
     * boolean result1 = IterableUtils.isIterable(list);     // true
     * boolean result2 = IterableUtils.isIterable("string"); // false
     * boolean result3 = IterableUtils.isIterable(null);     // false
     * }</pre>
     *
     * @param object the object to check
     * @return {@code true} if the object is an instance of {@link Iterable}, {@code false} otherwise
     */
    public static boolean isIterable(@Nullable Object object) {
        return object instanceof Iterable;
    }

    /**
     * Checks if the given class is assignable from {@link Iterable}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean result1 = IterableUtils.isIterable(List.class);  // true
     * boolean result2 = IterableUtils.isIterable(String.class); // false
     * boolean result3 = IterableUtils.isIterable(null);        // false
     * }</pre>
     *
     * @param clazz the class to check
     * @return {@code true} if the class is assignable from {@link Iterable}, {@code false} otherwise
     * @see ClassUtils#isAssignableFrom(Class, Class)
     */
    public static boolean isIterable(@Nullable Class<?> clazz) {
        return isAssignableFrom(Iterable.class, clazz);
    }

    /**
     * Iterates over the given {@link Iterable} and applies the provided {@link Consumer} to each element.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<String> list = Arrays.asList("a", "b", "c");
     * IterableUtils.iterate(list, System.out::println);
     * // Output:
     * // a
     * // b
     * // c
     * }</pre>
     *
     * @param elements        the {@link Iterable} to iterate over
     * @param elementConsumer the {@link Consumer} to apply to each element
     * @param <E>             the type of elements in the {@link Iterable}
     */
    public static <E> void iterate(@Nullable Iterable<E> elements, @Nonnull Consumer<E> elementConsumer) {
        if (elements != null) {
            for (E element : elements) {
                elementConsumer.accept(element);
            }
        }
    }

    /**
     * Iterates over the given {@link Iterable} and applies the provided {@link Consumer} to each element.
     * <p>
     * This method is equivalent to {@link #iterate(Iterable, Consumer)}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<String> list = Arrays.asList("a", "b", "c");
     * IterableUtils.forEach(list, System.out::println);
     * // Output:
     * // a
     * // b
     * // c
     * }</pre>
     *
     * @param elements        the {@link Iterable} to iterate over
     * @param elementConsumer the {@link Consumer} to apply to each element
     * @param <E>             the type of elements in the {@link Iterable}
     * @see #iterate(Iterable, Consumer)
     */
    public static <E> void forEach(@Nullable Iterable<E> elements, @Nonnull Consumer<E> elementConsumer) {
        iterate(elements, elementConsumer);
    }

    private IterableUtils() {
    }
}
