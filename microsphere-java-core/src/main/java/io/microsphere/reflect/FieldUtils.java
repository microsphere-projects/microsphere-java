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
package io.microsphere.reflect;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.util.Utils;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static io.microsphere.collection.CollectionUtils.addAll;
import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.lang.function.Streams.filter;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.AccessibleObjectUtils.trySetAccessible;
import static io.microsphere.reflect.TypeUtils.isObjectClass;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ClassUtils.getAllInheritedTypes;
import static io.microsphere.util.ClassUtils.getTypeName;

/**
 * The Java Reflection {@link Field} Utility class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class FieldUtils implements Utils {

    private static final Logger logger = getLogger(FieldUtils.class);

    /**
     * Find the specified object's declared {@link Field} by its name.
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * class Example {
     *     private String testField;
     * }
     *
     * Example instance = new Example();
     * Field field = FieldUtils.findField(instance, "testField");
     * if (field != null) {
     *     System.out.println("Field found: " + field.getName());
     * } else {
     *     System.out.println("Field not found.");
     * }
     * }</pre>
     *
     * @param object    the object whose class is to be examined
     * @param fieldName the name of the field to find
     * @return the {@link Field} object if found; otherwise, {@code null}
     */
    @Nullable
    public static Field findField(Object object, String fieldName) {
        return findField(object.getClass(), fieldName);
    }

    /**
     * Finds a {@link Field} in the specified class by its name.
     *
     * <p>This method recursively searches for the field in the given class and its superclasses until it finds the field
     * or reaches the top of the class hierarchy (i.e., the {@code Object} class).</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * class Example {
     *     private String testField;
     * }
     *
     * Field field = FieldUtils.findField(Example.class, "testField");
     * if (field != null) {
     *     System.out.println("Field found: " + field.getName());
     * } else {
     *     System.out.println("Field not found.");
     * }
     * }</pre>
     *
     * @param klass     The class to examine for the field
     * @param fieldName The name of the field to find
     * @return The found {@link Field}, or {@code null} if no such field exists
     * @throws RuntimeException if an exception occurs during field lookup,
     *                          typically wrapped as an unchecked exception
     */
    @Nullable
    public static Field findField(Class<?> klass, String fieldName) {
        if (klass == null || isObjectClass(klass)) {
            return null;
        }
        Field field = null;
        try {
            field = klass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // ignore, try the super class
            field = findField(klass.getSuperclass(), fieldName);
        }
        if (field == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("The field[name :'{}'] not found in class : '{}'", fieldName, klass);
            }
        }
        return field;
    }

    /**
     * Find the declared {@link Field} by its name and type.
     *
     * <p>This method searches for a field with the specified name and type in the given class. If the field is not found,
     * it recursively checks the superclasses until it finds a matching field or reaches the top of the class hierarchy.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * class Example {
     *     private String testField;
     * }
     *
     * Field field = FieldUtils.findField(Example.class, "testField", String.class);
     * if (field != null) {
     *     System.out.println("Field found: " + field.getName());
     * } else {
     *     System.out.println("Field not found.");
     * }
     * }</pre>
     *
     * @param klass     The class to examine for the field
     * @param fieldName The name of the field to find
     * @param fieldType The expected type of the field
     * @return The found {@link Field}, or {@code null} if no such field exists
     */
    @Nullable
    public static Field findField(Class<?> klass, String fieldName, Class<?> fieldType) {
        return findField(klass, fieldName, field -> Objects.equals(fieldType, field.getType()));
    }

    /**
     * Find the declared {@link Field} by its name and apply additional filtering conditions.
     *
     * <p>This method searches for a field with the specified name in the given class. If the field is found, it applies
     * the provided predicates to further filter the field. If any predicate evaluates to false, this method returns
     * {@code null}.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * class Example {
     *     private String testField;
     * }
     *
     * // Find field and ensure it's private
     * Field field = FieldUtils.findField(Example.class, "testField", f -> Modifier.isPrivate(f.getModifiers()));
     * if (field != null) {
     *     System.out.println("Field found and passed all filters.");
     * } else {
     *     System.out.println("Field not found or did not pass filters.");
     * }
     * }</pre>
     *
     * @param klass      The class to examine for the field
     * @param fieldName  The name of the field to find
     * @param predicates One or more predicates used to filter the found field
     * @return The found field that satisfies all the provided predicates, or {@code null} if no such field exists
     */
    @Nullable
    public static Field findField(Class<?> klass, String fieldName, Predicate<? super Field>... predicates) {
        Field field = findField(klass, fieldName);
        return and(predicates).test(field) ? field : null;
    }

    /**
     * Retrieves the value of a static field from the specified class.
     *
     * <p>This method attempts to find the declared field by name in the given class and then retrieves its value.
     * If the field is not found or is not accessible, an appropriate exception may be thrown.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * class Example {
     *     private static String testField = "Hello, World!";
     * }
     *
     * String value = FieldUtils.getStaticFieldValue(Example.class, "testField");
     * System.out.println("Field value: " + value);  // Output: Field value: Hello, World!
     * }</pre>
     *
     * @param klass     The class containing the static field
     * @param fieldName The name of the static field to retrieve
     * @param <T>       The type of the field value
     * @return The value of the static field, or {@code null} if the field could not be accessed or was not found
     */
    @Nullable
    public static <T> T getStaticFieldValue(Class<?> klass, String fieldName) {
        Field field = findField(klass, fieldName);
        return getStaticFieldValue(field);
    }

    /**
     * Retrieves the value of a static field.
     *
     * <p>This method gets the value of the specified static field. If the field is not accessible,
     * an attempt will be made to make it accessible.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * class Example {
     *     private static String testField = "Static Value";
     * }
     *
     * Field field = FieldUtils.findField(Example.class, "testField");
     * String value = FieldUtils.getStaticFieldValue(field);
     * System.out.println("Field value: " + value);  // Output: Field value: Static Value
     * }</pre>
     *
     * @param field The {@link Field} object representing the static field
     * @param <T>   The type of the field value
     * @return The value of the static field, or {@code null} if the field could not be accessed or was not found
     */
    @Nullable
    public static <T> T getStaticFieldValue(Field field) {
        return getFieldValue(null, field);
    }

    /**
     * Sets the value of a static field in the specified class.
     *
     * <p>This method finds the declared field by name in the given class and sets its value to the provided value.
     * If the field is not found or cannot be accessed, an appropriate exception may be thrown.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * class Example {
     *     private static String testField = "Original Value";
     * }
     *
     * // Set the static field's value
     * FieldUtils.setStaticFieldValue(Example.class, "testField", "New Value");
     *
     * // Verify the change
     * String value = FieldUtils.getStaticFieldValue(Example.class, "testField");
     * System.out.println("Updated field value: " + value);  // Output: Updated field value: New Value
     * }</pre>
     *
     * @param klass      The class containing the static field
     * @param fieldName  The name of the static field to set
     * @param fieldValue The value to assign to the static field
     * @param <V>        The type of the field value
     * @return The previous value of the static field, or {@code null} if the field could not be accessed or was not found
     */
    @Nullable
    public static <V> V setStaticFieldValue(Class<?> klass, String fieldName, V fieldValue) {
        Field field = findField(klass, fieldName);
        return setFieldValue(null, field, fieldValue);
    }

    /**
     * Find and return all accessible fields in the class hierarchy, optionally filtered by one or more predicates.
     *
     * <p>This method collects all public fields from the specified class and its superclasses (including interfaces),
     * and applies optional filtering conditions to select only the desired fields.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * class Example {
     *     public int publicField;
     * }
     *
     * Set<Field> fields = FieldUtils.findAllFields(Example.class);
     * for (Field field : fields) {
     *     System.out.println("Found field: " + field.getName());
     * }
     * }</pre>
     *
     * <h4>Filtering Fields</h4>
     *
     * <pre>{@code
     * Set<Field> stringFields = FieldUtils.findAllFields(Example.class,
     *     field -> field.getType().equals(String.class));
     * }</pre>
     *
     * @param declaredClass The class whose fields are to be searched
     * @param fieldFilters  Optional predicates used to filter the found fields
     * @return A set of fields that match the given criteria, preserving insertion order
     */
    @Nonnull
    public static Set<Field> findAllFields(Class<?> declaredClass, Predicate<? super Field>... fieldFilters) {
        Set<Field> allFields = new LinkedHashSet<>();
        addAll(allFields, declaredClass.getFields());
        for (Class superType : getAllInheritedTypes(declaredClass)) {
            addAll(allFields, superType.getFields());
        }
        return filter(allFields, and(fieldFilters));
    }

    /**
     * Find and return all declared fields in the class hierarchy, optionally filtered by one or more predicates.
     *
     * <p>This method collects all declared fields from the specified class and its superclasses (including interfaces),
     * and applies optional filtering conditions to select only the desired fields.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * class Example {
     *     private String privateField;
     *     public int publicField;
     * }
     *
     * Set<Field> fields = FieldUtils.findAllDeclaredFields(Example.class);
     * for (Field field : fields) {
     *     System.out.println("Found field: " + field.getName());
     * }
     * }</pre>
     *
     * <h4>Filtering Fields</h4>
     *
     * <pre>{@code
     * Set<Field> privateFields = FieldUtils.findAllDeclaredFields(Example.class,
     *     field -> Modifier.isPrivate(field.getModifiers()));
     * }</pre>
     *
     * @param declaredClass The class whose declared fields are to be searched
     * @param fieldFilters  Optional predicates used to filter the found fields
     * @return A set of declared fields that match the given criteria, preserving insertion order
     */
    @Nonnull
    public static Set<Field> findAllDeclaredFields(Class<?> declaredClass, Predicate<? super Field>... fieldFilters) {
        Set<Field> allDeclaredFields = new LinkedHashSet<>();
        addAll(allDeclaredFields, declaredClass.getDeclaredFields());
        for (Class superType : getAllInheritedTypes(declaredClass)) {
            addAll(allDeclaredFields, superType.getDeclaredFields());
        }
        return filter(allDeclaredFields, and(fieldFilters));
    }

    /**
     * Retrieves the declared field with the specified name from the given class.
     *
     * <p>This method uses reflection to find the field and wraps any exceptions
     * thrown during execution in an unchecked exception.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * class Example {
     *     private String exampleField;
     * }
     *
     * Field field = FieldUtils.getDeclaredField(Example.class, "exampleField");
     * if (field != null) {
     *     System.out.println("Field found: " + field.getName());
     * } else {
     *     System.out.println("Field not found.");
     * }
     * }</pre>
     *
     * @param declaredClass The class containing the declared field
     * @param fieldName     The name of the field to retrieve
     * @return The {@link Field} object representing the declared field
     * @throws RuntimeException if an error occurs while retrieving the field,
     *                          typically wrapped as an unchecked exception
     */
    @Nullable
    public static Field getDeclaredField(Class<?> declaredClass, String fieldName) {
        return execute(() -> declaredClass.getDeclaredField(fieldName));
    }

    /**
     * Retrieves the value of a field with the specified name from the given object instance.
     *
     * <p>This method finds the declared field by its name in the class of the provided instance and retrieves its value.
     * If the field is not found or cannot be accessed, an appropriate exception will be thrown.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * class Example {
     *     private String exampleField = "Hello, Reflection!";
     * }
     *
     * Example instance = new Example();
     * String value = FieldUtils.getFieldValue(instance, "exampleField");
     * System.out.println("Field value: " + value);  // Output: Field value: Hello, Reflection!
     * }</pre>
     *
     * @param <V>       The type of the field value
     * @param instance  The object instance from which to retrieve the field value
     * @param fieldName The name of the field whose value is to be retrieved
     * @return The value of the field, or {@code null} if the field could not be accessed or was not found
     * @throws IllegalStateException    if this Field object is enforcing Java language access control and the underlying
     *                                  field is inaccessible
     * @throws IllegalArgumentException if the specified object is not an instance of the class or interface declaring
     *                                  the underlying field (or a subclass or implementor thereof)
     */
    @Nullable
    public static <V> V getFieldValue(Object instance, String fieldName) throws IllegalStateException, IllegalArgumentException {
        return getFieldValue(instance, findField(instance, fieldName));
    }

    /**
     * Retrieves the value of a field with the specified name from the given object instance,
     * returning the provided default value if the field is not found or its value is {@code null}.
     *
     * <p>This method finds the declared field by its name in the class of the provided instance and retrieves its value.
     * If the field is not found or cannot be accessed, an appropriate exception will be thrown.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * class Example {
     *     private String exampleField;
     * }
     *
     * Example instance = new Example();
     * String value = FieldUtils.getFieldValue(instance, "exampleField", "default");
     * System.out.println("Field value: " + value);  // Output: Field value: default
     * }</pre>
     *
     * @param <V>          The type of the field value
     * @param instance     The object instance from which to retrieve the field value
     * @param fieldName    The name of the field whose value is to be retrieved
     * @param defaultValue The default value to return if the field's value is {@code null}
     * @return The value of the field if found and not {@code null}; otherwise, the {@code defaultValue}
     * @throws IllegalStateException    If this {@code Field} object is enforcing Java language access control
     *                                  and the underlying field is inaccessible
     * @throws IllegalArgumentException If the specified object is not an instance of the class or interface
     *                                  declaring the underlying field (or a subclass or implementor thereof)
     */
    @Nullable
    public static <V> V getFieldValue(Object instance, String fieldName, V defaultValue) throws IllegalStateException, IllegalArgumentException {
        V value = getFieldValue(instance, fieldName);
        return value != null ? value : defaultValue;
    }

    /**
     * Retrieves the value of a field with the specified name and type from the given object instance.
     *
     * <p>This method finds the declared field by its name and ensures it matches the provided field type.
     * If the field is not found or its type does not match, {@code null} is returned.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * class Example {
     *     private String exampleField = "Typed Value";
     * }
     *
     * Example instance = new Example();
     * String value = FieldUtils.getFieldValue(instance, "exampleField", String.class);
     * System.out.println("Field value: " + value);  // Output: Field value: Typed Value
     * }</pre>
     *
     * @param <V>       The expected type of the field value
     * @param instance  The object instance from which to retrieve the field value
     * @param fieldName The name of the field whose value is to be retrieved
     * @param fieldType The expected type of the field
     * @return The value of the field if found and of the correct type; otherwise, {@code null}
     * @throws IllegalStateException    if this Field object is enforcing Java language access control and the underlying
     *                                  field is inaccessible
     * @throws IllegalArgumentException if the specified object is not an instance of the class or interface declaring
     *                                  the underlying field (or a subclass or implementor thereof)
     */
    @Nullable
    public static <V> V getFieldValue(Object instance, String fieldName, Class<V> fieldType) throws IllegalStateException, IllegalArgumentException {
        Field field = findField(instance.getClass(), fieldName, fieldType);
        return getFieldValue(instance, field);
    }

    /**
     * Retrieves the value of the specified {@link Field} from the given object instance.
     *
     * <p>This method accesses the field's value using reflection. If the field is not accessible,
     * an attempt will be made to make it accessible. If access fails, an exception will be thrown.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * class Example {
     *     private String exampleField = "Reflection Value";
     * }
     *
     * Example instance = new Example();
     * Field field = FieldUtils.findField(instance, "exampleField");
     * String value = FieldUtils.getFieldValue(instance, field);
     * System.out.println("Field value: " + value);  // Output: Field value: Reflection Value
     * }</pre>
     *
     * <h4>Handling Null Fields</h4>
     *
     * <pre>{@code
     * Field nullField = null;
     * String value = FieldUtils.getFieldValue(instance, nullField);
     * System.out.println("Field value: " + value);  // Output: Field value: null
     * }</pre>
     *
     * @param <V>      The type of the field value
     * @param instance The object instance from which to retrieve the field value
     * @param field    The {@link Field} object representing the field to retrieve
     * @return The value of the field if found and accessible; otherwise, {@code null}
     * @throws IllegalStateException    if this Field object is enforcing Java language access control and the underlying
     *                                  field is inaccessible
     * @throws IllegalArgumentException if the specified object is not an instance of the class or interface declaring
     *                                  the underlying field (or a subclass or implementor thereof)
     */
    @Nullable
    public static <V> V getFieldValue(Object instance, Field field) throws IllegalStateException, IllegalArgumentException {
        if (field == null) {
            return null;
        }

        V fieldValue = null;
        boolean accessible = false;
        try {
            accessible = trySetAccessible(field);
            fieldValue = (V) field.get(instance);
        } catch (IllegalAccessException e) {
            handleIllegalAccessException(e, instance, field, accessible);
        } catch (IllegalArgumentException e) {
            handleIllegalArgumentException(e, instance, field);
        }

        return fieldValue;
    }

    /**
     * Sets the value of a field with the specified name in the given object instance.
     *
     * <p>This method finds the declared field by its name in the class of the provided instance and sets its value
     * to the provided value. If the field is not found or cannot be accessed, an appropriate exception will be thrown.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * class Example {
     *     private String exampleField = "Original Value";
     * }
     *
     * Example instance = new Example();
     * // Get the original value
     * String originalValue = FieldUtils.getFieldValue(instance, "exampleField");
     * System.out.println("Original value: " + originalValue);  // Output: Original value: Original Value
     *
     * // Set a new value using setFieldValue
     * String previousValue = FieldUtils.setFieldValue(instance, "exampleField", "New Value");
     * System.out.println("Previous value: " + previousValue);  // Output: Previous value: Original Value
     * System.out.println("Updated value: " + FieldUtils.getFieldValue(instance, "exampleField"));
     * // Output: Updated value: New Value
     * }</pre>
     *
     * @param <V>       The type of the field value
     * @param instance  The object instance whose field value is to be set
     * @param fieldName The name of the field whose value is to be set
     * @param value     The new value to assign to the field
     * @return The previous value of the field before it was updated, or {@code null} if the field could not be accessed
     * or was not found
     * @throws IllegalStateException    if this Field object is enforcing Java language access control and the underlying
     *                                  field is inaccessible
     * @throws IllegalArgumentException if the specified object is not an instance of the class or interface declaring
     *                                  the underlying field (or a subclass or implementor thereof)
     */
    @Nullable
    public static <V> V setFieldValue(Object instance, String fieldName, V value) throws IllegalStateException, IllegalArgumentException {
        return setFieldValue(instance, findField(instance, fieldName), value);
    }

    /**
     * Sets the value of the specified {@link Field} in the given object instance.
     *
     * <p>This method accesses and modifies the field's value using reflection. If the field is not accessible,
     * an attempt will be made to make it accessible. If access fails, an exception will be thrown.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * class Example {
     *     private String exampleField = "Initial Value";
     * }
     *
     * Example instance = new Example();
     * Field field = FieldUtils.findField(instance, "exampleField");
     *
     * // Get the original value
     * String originalValue = FieldUtils.getFieldValue(instance, field);
     * System.out.println("Original value: " + originalValue);  // Output: Original value: Initial Value
     *
     * // Set a new value using setFieldValue
     * String previousValue = FieldUtils.setFieldValue(instance, field, "Updated Value");
     * System.out.println("Previous value: " + previousValue);  // Output: Previous value: Initial Value
     * System.out.println("New value: " + FieldUtils.getFieldValue(instance, field));  // Output: New value: Updated Value
     * }</pre>
     *
     * <h4>Handling Null Fields</h4>
     *
     * <pre>{@code
     * Field nullField = null;
     * String previousValue = FieldUtils.setFieldValue(instance, nullField, "New Value");
     * System.out.println("Previous value: " + previousValue);  // Output: Previous value: null
     * }</pre>
     *
     * @param <V>      The type of the field value
     * @param instance The object instance whose field value is to be modified
     * @param field    The {@link Field} object representing the field to modify
     * @param value    The new value to assign to the field
     * @return The previous value of the field before modification, or {@code null} if the field was not found or inaccessible
     * @throws IllegalStateException    If this Field object is enforcing Java language access control and the underlying field is inaccessible
     * @throws IllegalArgumentException If the specified object is not an instance of the class or interface declaring the underlying field
     */
    @Nullable
    public static <V> V setFieldValue(Object instance, Field field, V value) throws IllegalStateException, IllegalArgumentException {
        if (field == null) {
            return null;
        }

        V previousValue = null;
        boolean accessible = false;
        try {
            accessible = trySetAccessible(field);
            previousValue = (V) field.get(instance);
            if (!Objects.equals(previousValue, value)) {
                field.set(instance, value);
            }
        } catch (IllegalAccessException e) {
            handleIllegalAccessException(e, instance, field, accessible);
        } catch (IllegalArgumentException e) {
            handleIllegalArgumentException(e, instance, field);
        }

        return previousValue;
    }

    /**
     * Asserts that the type of the specified field in the given object or class matches or is a subtype of the expected type.
     *
     * <p>This method is useful when validating field types at runtime, especially when working with reflection-based operations,
     * such as injection frameworks, dynamic proxies, or generic utilities.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * class Example {
     *     private String testField;
     * }
     *
     * Example instance = new Example();
     * FieldUtils.assertFieldMatchType(instance, "testField", CharSequence.class);
     * // This will pass because String is a subclass of CharSequence
     * }</pre>
     *
     * <h4>Failure Case</h4>
     *
     * <pre>{@code
     * try {
     *     FieldUtils.assertFieldMatchType(instance, "testField", Integer.class);
     * } catch (IllegalArgumentException e) {
     *     System.out.println("Caught exception: " + e.getMessage());
     *     // Output: Caught exception: The type['java.lang.String'] of field['testField'] in Class['Example'] can't match expected type['java.lang.Integer']
     * }
     * }</pre>
     *
     * @param instance     the object whose class is to be examined, or directly a {@link Class} object
     * @param fieldName    the name of the field to check
     * @param expectedType the expected type the field should be or extend/implement
     * @throws IllegalArgumentException if the field's actual type does not match or is not assignable to the expected type
     */
    public static void assertFieldMatchType(Object instance, String fieldName, Class<?> expectedType) throws IllegalArgumentException {
        Class<?> type = instance instanceof Class ? (Class<?>) instance : instance.getClass();
        Field field = findField(type, fieldName);
        Class<?> fieldType = field.getType();
        if (!expectedType.isAssignableFrom(fieldType)) {
            String message = format("The type['{}'] of field['{}'] in Class['{}'] can't match expected type['{}']", fieldType.getName(), fieldName, type.getName(), expectedType.getName());
            throw new IllegalArgumentException(message);
        }
    }

    static void handleIllegalAccessException(IllegalAccessException e, Object instance, Field field, boolean accessible) {
        String errorMessage = format("The instance [object : {} , class : {} ] can't access the field[name : '{}' , type : {} , accessible : {}]",
                instance, getTypeName(instance.getClass()), field.getName(), getTypeName(field.getType()), accessible);
        if (logger.isTraceEnabled()) {
            logger.trace(errorMessage);
        }
        throw new IllegalStateException(errorMessage, e);
    }

    static void handleIllegalArgumentException(IllegalArgumentException e, Object instance, Field field) {
        String errorMessage = format("The instance[object : {} , class : {}] can't match the field[name : '{}' , type : {}]",
                instance, getTypeName(instance.getClass()), field.getName(), getTypeName(field.getType()));
        if (logger.isTraceEnabled()) {
            logger.trace(errorMessage);
        }
        throw new IllegalArgumentException(errorMessage, e);
    }

    private FieldUtils() {
    }
}
