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

import java.util.Collections;
import java.util.Enumeration;

/**
 * The utilities class for Java {@link Enumeration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Enumeration
 * @see Collections#enumeration
 * @since 1.0.0
 */
public abstract class EnumerationUtils implements Utils {

    /**
     * Creates an {@link Enumeration} from the provided elements.
     *
     * <p>Internally, this method delegates to {@link #ofEnumeration(Object[])} to create a new
     * enumeration based on the given array of elements.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Enumeration<String> en = EnumerationUtils.of("one", "two", "three");
     * while (en.hasMoreElements()) {
     *     System.out.println(en.nextElement());
     * }
     * }</pre>
     *
     * @param elements the array of elements to include in the enumeration
     * @param <E>      the type of the elements
     * @return a non-null {@link Enumeration} instance containing the specified elements
     */
    @Nonnull
    public static <E> Enumeration<E> of(E... elements) {
        return ofEnumeration(elements);
    }

    /**
     * Creates an {@link Enumeration} from the provided elements array.
     *
     * <p>This method constructs a new {@link ArrayEnumeration} instance backed by the given array,
     * allowing sequential read-only access to the array elements.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Enumeration<String> en = EnumerationUtils.ofEnumeration("apple", "banana", "cherry");
     * while (en.hasMoreElements()) {
     *     System.out.println(en.nextElement());
     * }
     * }</pre>
     *
     * @param elements the array of elements to include in the enumeration
     * @param <E>      the type of the elements
     * @return a non-null {@link Enumeration} instance containing the specified elements
     */
    @Nonnull
    public static <E> Enumeration<E> ofEnumeration(E... elements) {
        return new ArrayEnumeration<>(elements);
    }

    private EnumerationUtils() {
    }
}
