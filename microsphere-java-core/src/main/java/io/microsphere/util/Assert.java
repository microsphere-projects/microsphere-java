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

import io.microsphere.annotation.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.collection.MapUtils.isEmpty;
import static io.microsphere.reflect.FieldUtils.findField;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static io.microsphere.util.CharSequenceUtils.isEmpty;
import static io.microsphere.util.ClassUtils.getTypeName;
import static io.microsphere.util.ClassUtils.isArray;
import static io.microsphere.util.ClassUtils.isAssignableFrom;
import static io.microsphere.util.StringUtils.isBlank;

/**
 * The utility class for Assertion
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class Assert {

    /**
     * Assert a boolean expression, throwing an {@code IllegalArgumentException}
     * if the expression evaluates to {@code false}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     assertTrue(i > 0, "The value must be greater than zero");
     * }</pre>
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * assertTrue(i > 0, () -> "The value '" + i + "' must be greater than zero");
     * }</pre>
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     assertNull(value, "The value must be null");
     * }</pre>
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * assertNull(value, () -> "The value '" + value + "' must be null");
     * }</pre>
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     assertNotNull(clazz, "The class must not be null");
     * }</pre>
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * assertNotNull(entity.getId(), () -> "ID for entity " + entity.getName() + " must not be null");
     * }</pre>
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * assertNotEmpty(entity.getName(), "Name for entity " + entity.getName() + " must not be empty");
     * }</pre>
     *
     * @param text    the text to check
     * @param message the exception message to use if the
     *                assertion fails
     */
    public static void assertNotEmpty(@Nullable String text, String message) {
        if (isEmpty(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that a string is not empty ("").
     * <h3>Example Usage</h3>
     * <pre>{@code
     * assertNotEmpty(entity.getName(), () -> "Name for entity " + entity.getName() + " must not be empty");
     * }</pre>
     *
     * @param text            the text to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     */
    public static void assertNotEmpty(@Nullable String text, Supplier<String> messageSupplier) {
        if (isEmpty(text)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that a string is not blank.
     * <h3>Example Usage</h3>
     * <pre>{@code
     * assertNotBlank(entity.getName(), "Name for entity " + entity.getName() + " must not be blank");
     * }</pre>
     *
     * @param text    the text to check
     * @param message the exception message to use if the
     *                assertion fails
     */
    public static void assertNotBlank(@Nullable String text, String message) {
        if (isBlank(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that a string is not blank.
     * <h3>Example Usage</h3>
     * <pre>{@code
     * assertNotBlank(entity.getName(), () -> "Name for entity " + entity.getName() + " must not be blank");
     * }</pre>
     *
     * @param text            the text to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     */
    public static void assertNotBlank(@Nullable String text, Supplier<String> messageSupplier) {
        if (isBlank(text)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that an array contains elements; that is, it must not be
     * {@code null} and must contain at least one element.
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     assertNotEmpty(array, "The array must contain elements");
     * }</pre>
     *
     * @param array   the array to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object array is {@code null} or contains no elements
     */
    public static void assertNotEmpty(@Nullable Object[] array, String message) {
        if (isEmpty(array)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that an array contains elements; that is, it must not be
     * {@code null} and must contain at least one element.
     * <h3>Example Usage</h3>
     * <pre>{@code
     * assertNotEmpty(array, () -> "The " + arrayType + " array must contain elements");
     * }</pre>
     *
     * @param array           the array to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalArgumentException if the object array is {@code null} or contains no elements
     */
    public static void assertNotEmpty(@Nullable Object[] array, Supplier<String> messageSupplier) {
        if (isEmpty(array)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that a collection contains elements; that is, it must not be
     * {@code null} and must contain at least one element.
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     assertNotEmpty(collection, "Collection must contain elements");
     * }</pre>
     *
     * @param collection the collection to check
     * @param message    the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the collection is {@code null} or
     *                                  contains no elements
     */
    public static void assertNotEmpty(@Nullable Collection<?> collection, String message) {
        if (isEmpty(collection)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that a collection contains elements; that is, it must not be
     * {@code null} and must contain at least one element.
     * <h3>Example Usage</h3>
     * <pre>{@code
     * assertNotEmpty(collection, () -> "The " + collectionType + " collection must contain elements");
     * }</pre>
     *
     * @param collection      the collection to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalArgumentException if the collection is {@code null} or
     *                                  contains no elements
     */
    public static void assertNotEmpty(@Nullable Collection<?> collection, Supplier<String> messageSupplier) {
        if (isEmpty(collection)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that a Map contains entries; that is, it must not be {@code null}
     * and must contain at least one entry.
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     assertNotEmpty(map, "Map must contain entries");
     * }</pre>
     *
     * @param map     the map to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the map is {@code null} or contains no entries
     */
    public static void assertNotEmpty(@Nullable Map<?, ?> map, String message) {
        if (isEmpty(map)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that a Map contains entries; that is, it must not be {@code null}
     * and must contain at least one entry.
     * <h3>Example Usage</h3>
     * <pre>{@code
     * assertNotEmpty(map, () -> "The " + mapType + " map must contain entries");
     * }</pre>
     *
     * @param map             the map to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalArgumentException if the map is {@code null} or contains no entries
     */
    public static void assertNotEmpty(@Nullable Map<?, ?> map, Supplier<String> messageSupplier) {
        if (isEmpty(map)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that an array contains no {@code null} elements.
     * <p>Note: Does not complain if the array is empty!
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     assertNoNullElements(array, "The array must contain non-null elements");
     * }</pre>
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * assertNoNullElements(array, () -> "The " + arrayType + " array must contain non-null elements");
     * }</pre>
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     assertNoNullElements(elements, "Elements must contain non-null elements");
     * }</pre>
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * assertNoNullElements(elements, () -> "Collection " + collectionName + " must contain non-null elements");
     * }</pre>
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

    /**
     * Assert the index is valid for the given array.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * assertArrayIndex(array, index); // throws IllegalArgumentException if index is invalid
     * }</pre>
     *
     * @param array Object array to check
     * @param index Index value to validate
     * @throws IllegalArgumentException       if the object is not an array
     * @throws ArrayIndexOutOfBoundsException if the index is negative or exceeds the array length
     */
    public static void assertArrayIndex(Object array, int index) throws IllegalArgumentException {
        if (index < 0) {
            String message = format("The index argument must be positive , actual is {}", index);
            throw new ArrayIndexOutOfBoundsException(message);
        }
        assertArrayType(array);
        int length = Array.getLength(array);
        if (index > length - 1) {
            String message = format("The index must be less than {} , actual is {}", length, index);
            throw new ArrayIndexOutOfBoundsException(message);
        }
    }

    /**
     * Assert the provided object is an array.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * assertArrayType(new String[]{"one", "two"}); // passes
     * assertArrayType("notAnArray");              // throws IllegalArgumentException
     * }</pre>
     *
     * @param array the object to check
     * @throws IllegalArgumentException if the object is not an array
     */
    public static void assertArrayType(Object array) throws IllegalArgumentException {
        Class<?> type = array.getClass();
        if (!isArray(type)) {
            String message = format("The argument is not an array object, its type is {}", type.getName());
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that the field with the specified name in the given object's class matches the expected type.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class Example {
     *     private String name;
     * }
     *
     * Example example = new Example();
     * assertFieldMatchType(example, "name", String.class);  // passes
     * assertFieldMatchType(example, "name", Integer.class); // throws IllegalArgumentException
     * }</pre>
     *
     * @param object       the object whose class is to be checked
     * @param fieldName    the name of the field
     * @param expectedType the expected type of the field
     * @throws NullPointerException     if the field cannot be found in the object's class
     * @throws IllegalArgumentException if the field's type does not match the expected type
     */
    public static void assertFieldMatchType(Object object, String fieldName, Class<?> expectedType) throws NullPointerException, IllegalArgumentException {
        Class<?> type = object.getClass();
        Field field = findField(type, fieldName);
        Class<?> fieldType = field.getType();
        if (!isAssignableFrom(expectedType, fieldType)) {
            String message = format("The type['{}'] of field[name : '{}'] in Class['{}'] can't match expected type['{}']",
                    getTypeName(fieldType), fieldName, getTypeName(type), getTypeName(expectedType));
            throw new IllegalArgumentException(message);
        }
    }

    private static String nullSafeGet(Supplier<String> messageSupplier) {
        return messageSupplier == null ? null : messageSupplier.get();
    }
}
