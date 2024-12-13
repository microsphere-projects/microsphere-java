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

import io.microsphere.collection.CollectionUtils;
import io.microsphere.collection.MapUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The utility class for Assertion
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class Assert extends BaseUtils {

    /**
     * Assert a boolean expression, throwing an {@code IllegalArgumentException}
     * if the expression evaluates to {@code false}.
     * <pre>assertTrue(i &gt; 0, "The value must be greater than zero");</pre>
     *
     * @param expression a boolean expression
     * @param message    the exception message to use if the assertion fails
     * @throws IllegalArgumentException if {@code expression} is {@code false}
     */
    public static void assertTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert a boolean expression, throwing an {@code IllegalArgumentException}
     * if the expression evaluates to {@code false}.
     * <pre>
     * assertTrue(i &gt; 0, () -&gt; "The value '" + i + "' must be greater than zero");
     * </pre>
     *
     * @param expression      a boolean expression
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalArgumentException if {@code expression} is {@code false}
     */
    public static void assertTrue(boolean expression, Supplier<String> messageSupplier) {
        if (!expression) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that an object is {@code null}.
     * <pre>assertNull(value, "The value must be null");</pre>
     *
     * @param object  the object to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object is not {@code null}
     */
    public static void assertNull(@Nullable Object object, String message) {
        if (object != null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that an object is {@code null}.
     * <pre>
     * assertNull(value, () -&gt; "The value '" + value + "' must be null");
     * </pre>
     *
     * @param object          the object to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalArgumentException if the object is not {@code null}
     */
    public static void assertNull(@Nullable Object object, Supplier<String> messageSupplier) {
        if (object != null) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that an object is not {@code null}.
     * <pre>assertNotNull(clazz, "The class must not be null");</pre>
     *
     * @param object  the object to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object is {@code null}
     */
    public static void assertNotNull(@Nullable Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that an object is not {@code null}.
     * <pre>
     * assertNotNull(entity.getId(), () -&gt; "ID for entity " + entity.getName() + " must not be null");
     * </pre>
     *
     * @param object          the object to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalArgumentException if the object is {@code null}
     */
    public static void assertNotNull(@Nullable Object object, Supplier<String> messageSupplier) {
        if (object == null) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that a string is not empty ("").
     * <pre>
     * assertNotEmpty(entity.getName(), "Name for entity " + entity.getName() + " must not be empty");
     * </pre>
     *
     * @param text    the text to check
     * @param message the exception message to use if the
     *                assertion fails
     */
    public static void assertNotEmpty(@Nullable String text, String message) {
        if (StringUtils.isEmpty(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that a string is not empty ("").
     * <pre>
     * assertNotEmpty(entity.getName(), () -&gt; "Name for entity " + entity.getName() + " must not be empty");
     * </pre>
     *
     * @param text            the text to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     */
    public static void assertNotEmpty(@Nullable String text, Supplier<String> messageSupplier) {
        if (StringUtils.isEmpty(text)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that a string is not blank.
     * <pre>
     * assertNotBlank(entity.getName(), "Name for entity " + entity.getName() + " must not be blank");
     * </pre>
     *
     * @param text    the text to check
     * @param message the exception message to use if the
     *                assertion fails
     */
    public static void assertNotBlank(@Nullable String text, String message) {
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that a string is not blank.
     * <pre>
     * assertNotBlank(entity.getName(), () -&gt; "Name for entity " + entity.getName() + " must not be blank");
     * </pre>
     *
     * @param text            the text to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     */
    public static void assertNotBlank(@Nullable String text, Supplier<String> messageSupplier) {
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that an array contains elements; that is, it must not be
     * {@code null} and must contain at least one element.
     * <pre>assertNotEmpty(array, "The array must contain elements");</pre>
     *
     * @param array   the array to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object array is {@code null} or contains no elements
     */
    public static void assertNotEmpty(@Nullable Object[] array, String message) {
        if (ArrayUtils.isEmpty(array)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that an array contains elements; that is, it must not be
     * {@code null} and must contain at least one element.
     * <pre>
     * assertNotEmpty(array, () -&gt; "The " + arrayType + " array must contain elements");
     * </pre>
     *
     * @param array           the array to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalArgumentException if the object array is {@code null} or contains no elements
     */
    public static void assertNotEmpty(@Nullable Object[] array, Supplier<String> messageSupplier) {
        if (ArrayUtils.isEmpty(array)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that a collection contains elements; that is, it must not be
     * {@code null} and must contain at least one element.
     * <pre>assertNotEmpty(collection, "Collection must contain elements");</pre>
     *
     * @param collection the collection to check
     * @param message    the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the collection is {@code null} or
     *                                  contains no elements
     */
    public static void assertNotEmpty(@Nullable Collection<?> collection, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that a collection contains elements; that is, it must not be
     * {@code null} and must contain at least one element.
     * <pre>
     * assertNotEmpty(collection, () -&gt; "The " + collectionType + " collection must contain elements");
     * </pre>
     *
     * @param collection      the collection to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalArgumentException if the collection is {@code null} or
     *                                  contains no elements
     */
    public static void assertNotEmpty(@Nullable Collection<?> collection, Supplier<String> messageSupplier) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that a Map contains entries; that is, it must not be {@code null}
     * and must contain at least one entry.
     * <pre>assertNotEmpty(map, "Map must contain entries");</pre>
     *
     * @param map     the map to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the map is {@code null} or contains no entries
     */
    public static void assertNotEmpty(@Nullable Map<?, ?> map, String message) {
        if (MapUtils.isEmpty(map)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that a Map contains entries; that is, it must not be {@code null}
     * and must contain at least one entry.
     * <pre>
     * assertNotEmpty(map, () -&gt; "The " + mapType + " map must contain entries");
     * </pre>
     *
     * @param map             the map to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalArgumentException if the map is {@code null} or contains no entries
     */
    public static void assertNotEmpty(@Nullable Map<?, ?> map, Supplier<String> messageSupplier) {
        if (MapUtils.isEmpty(map)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that an array contains no {@code null} elements.
     * <p>Note: Does not complain if the array is empty!
     * <pre>assertNoNullElements(array, "The array must contain non-null elements");</pre>
     *
     * @param array   the array to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object array contains a {@code null} element
     */
    public static void assertNoNullElements(@Nullable Object[] array, String message) {
        if (array != null) {
            for (Object element : array) {
                if (element == null) {
                    throw new IllegalArgumentException(message);
                }
            }
        }
    }

    /**
     * Assert that an array contains no {@code null} elements.
     * <p>Note: Does not complain if the array is empty!
     * <pre>
     * assertNoNullElements(array, () -&gt; "The " + arrayType + " array must contain non-null elements");
     * </pre>
     *
     * @param array           the array to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalArgumentException if the object array contains a {@code null} element
     */
    public static void assertNoNullElements(@Nullable Object[] array, Supplier<String> messageSupplier) {
        if (array != null) {
            for (Object element : array) {
                if (element == null) {
                    throw new IllegalArgumentException(nullSafeGet(messageSupplier));
                }
            }
        }
    }

    /**
     * Assert that a elements contains no {@code null} elements.
     * <p>Note: Does not complain if the elements is empty!
     * <pre>assertNoNullElements(elements, "Elements must contain non-null elements");</pre>
     *
     * @param elements the elements to check
     * @param message  the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the elements contains a {@code null} element
     */
    public static void assertNoNullElements(@Nullable Iterable<?> elements, String message) {
        if (elements != null) {
            for (Object element : elements) {
                if (element == null) {
                    throw new IllegalArgumentException(message);
                }
            }
        }
    }

    /**
     * Assert that a elements contains no {@code null} elements.
     * <p>Note: Does not complain if the elements is empty!
     * <pre>
     * assertNoNullElements(elements, () -&gt; "Collection " + collectionName + " must contain non-null elements");
     * </pre>
     *
     * @param elements        the elements to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalArgumentException if the elements contains a {@code null} element
     */
    public static void assertNoNullElements(@Nullable Iterable<?> elements, Supplier<String> messageSupplier) {
        if (elements != null) {
            for (Object element : elements) {
                if (element == null) {
                    throw new IllegalArgumentException(nullSafeGet(messageSupplier));
                }
            }
        }
    }

    private static String nullSafeGet(Supplier<String> messageSupplier) {
        return messageSupplier == null ? null : messageSupplier.get();
    }
}
