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
     * Find the specified objects' declared {@link Field} by its' name
     *
     * @param object    the {@link Object object} to find
     * @param fieldName the name of {@link Field field}
     * @return null if not found
     */
    public static Field findField(Object object, String fieldName) {
        return findField(object.getClass(), fieldName);
    }

    /**
     * Find the declared {@link Field} by its' name
     *
     * @param klass     {@link Class class} to find
     * @param fieldName the name of {@link Field field}
     * @return null if not found
     */
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
     * Find the declared {@link Field} by its' name
     *
     * @param klass     {@link Class class} to find
     * @param fieldName the name of {@link Field field}
     * @param fieldType the {@link Class type} of {@link Field field}
     * @return null if not found
     */
    public static Field findField(Class<?> klass, String fieldName, Class<?> fieldType) {
        return findField(klass, fieldName, field -> Objects.equals(fieldType, field.getType()));
    }

    /**
     * Find the declared {@link Field} by its' name
     *
     * @param klass      {@link Class class} to find
     * @param fieldName  the name of {@link Field field}
     * @param predicates zero or more {@link Predicate}
     * @return null if not found
     */
    public static Field findField(Class<?> klass, String fieldName, Predicate<? super Field>... predicates) {
        Field field = findField(klass, fieldName);
        return and(predicates).test(field) ? field : null;
    }

    /**
     * Get the static {@link Field} Value
     *
     * @param klass     {@link Class class} to find
     * @param fieldName the name of {@link Field field}
     * @return <code>null</code> if <code>field</code> is <code>null</code> or get failed
     */
    public static <T> T getStaticFieldValue(Class<?> klass, String fieldName) {
        Field field = findField(klass, fieldName);
        return getStaticFieldValue(field);
    }

    /**
     * Get the static {@link Field} Value
     *
     * @param field {@link Field}
     * @return <code>null</code> if <code>field</code> is <code>null</code> or get failed
     */
    public static <T> T getStaticFieldValue(Field field) {
        return getFieldValue(null, field);
    }

    /**
     * Set the value to static {@link Field}
     *
     * @param klass      the class declared the field
     * @param fieldName  the name of {@link Field}
     * @param fieldValue the value of {@link Field}
     */
    public static <V> V setStaticFieldValue(Class<?> klass, String fieldName, V fieldValue) {
        Field field = findField(klass, fieldName);
        return setFieldValue(null, field, fieldValue);
    }

    public static Set<Field> findAllFields(Class<?> declaredClass, Predicate<? super Field>... fieldFilters) {
        Set<Field> allFields = new LinkedHashSet<>();
        addAll(allFields, declaredClass.getFields());
        for (Class superType : getAllInheritedTypes(declaredClass)) {
            addAll(allFields, superType.getFields());
        }
        return filter(allFields, and(fieldFilters));
    }

    public static Set<Field> findAllDeclaredFields(Class<?> declaredClass, Predicate<? super Field>... fieldFilters) {
        Set<Field> allDeclaredFields = new LinkedHashSet<>();
        addAll(allDeclaredFields, declaredClass.getDeclaredFields());
        for (Class superType : getAllInheritedTypes(declaredClass)) {
            addAll(allDeclaredFields, superType.getDeclaredFields());
        }
        return filter(allDeclaredFields, and(fieldFilters));
    }

    /**
     * Like the {@link Class#getDeclaredField(String)} method without throwing any {@link Exception}
     *
     * @param declaredClass the declared class
     * @param fieldName     the name of {@link Field}
     * @return if can't be found, return <code>null</code>
     * @throws RuntimeException thrown if you can't find the {@link Field}
     */
    public static Field getDeclaredField(Class<?> declaredClass, String fieldName) {
        return execute(() -> declaredClass.getDeclaredField(fieldName));
    }

    /**
     * Get the value of the specified {@link Field}
     *
     * @param instance  the instance whose field should be modified
     * @param fieldName the name of {@link Field}
     * @return the value of  the specified {@link Field}
     * @throws IllegalStateException    if this Field object is enforcing Java language access control and the underlying
     *                                  field is inaccessible.
     * @throws IllegalArgumentException if the specified object is not an instance of the class or interface declaring
     *                                  the underlying field (or a subclass or implementor thereof).
     */
    public static <V> V getFieldValue(Object instance, String fieldName) throws IllegalStateException, IllegalArgumentException {
        return getFieldValue(instance, findField(instance, fieldName));
    }


    /**
     * Get {@link Field} Value
     *
     * @param instance     the instance whose field should be modified
     * @param fieldName    field name
     * @param <V>          field type
     * @param defaultValue default value
     * @return {@link Field} Value
     * @throws IllegalStateException    if this Field object is enforcing Java language access control and the underlying
     *                                  field is inaccessible.
     * @throws IllegalArgumentException if the specified object is not an instance of the class or interface declaring
     *                                  the underlying field (or a subclass or implementor thereof).
     */
    public static <V> V getFieldValue(Object instance, String fieldName, V defaultValue) throws IllegalStateException, IllegalArgumentException {
        V value = getFieldValue(instance, fieldName);
        return value != null ? value : defaultValue;
    }

    /**
     * Get {@link Field} Value
     *
     * @param instance  the instance whose field should be modified
     * @param fieldName field name
     * @param fieldType field type
     * @param <V>       field type
     * @return {@link Field} Value
     * @throws IllegalStateException    if this Field object is enforcing Java language access control and the underlying
     *                                  field is inaccessible.
     * @throws IllegalArgumentException if the specified object is not an instance of the class or interface declaring
     *                                  the underlying field (or a subclass or implementor thereof).
     */
    public static <V> V getFieldValue(Object instance, String fieldName, Class<V> fieldType) throws IllegalStateException, IllegalArgumentException {
        Field field = findField(instance.getClass(), fieldName, fieldType);
        return getFieldValue(instance, field);
    }

    /**
     * Get the value of the specified {@link Field}
     *
     * @param instance the instance whose field should be modified
     * @param field    {@link Field}
     * @return the value of  the specified {@link Field}
     * @throws IllegalStateException    if this Field object is enforcing Java language access control and the underlying
     *                                  field is inaccessible.
     * @throws IllegalArgumentException if the specified object is not an instance of the class or interface declaring
     *                                  the underlying field (or a subclass or implementor thereof).
     */
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
     * Set the value for the specified {@link Field}
     *
     * @param instance  the instance whose field should be modified
     * @param fieldName the name of {@link Field}
     * @param value     the value of field to be set
     * @return the previous value of the specified {@link Field}
     * @throws IllegalStateException    if this Field object is enforcing Java language access control and the underlying
     *                                  field is inaccessible.
     * @throws IllegalArgumentException if the specified object is not an instance of the class or interface declaring
     *                                  the underlying field (or a subclass or implementor thereof).
     */
    public static <V> V setFieldValue(Object instance, String fieldName, V value) throws IllegalStateException, IllegalArgumentException {
        return setFieldValue(instance, findField(instance, fieldName), value);
    }

    /**
     * Set the value for the specified {@link Field}
     *
     * @param instance the instance whose field should be modified
     * @param field    {@link Field}
     * @param value    the value of field to be set
     * @return the previous value of the specified {@link Field}
     * @throws IllegalStateException    if this Field object is enforcing Java language access control and the underlying
     *                                  field is inaccessible.
     * @throws IllegalArgumentException if the specified object is not an instance of the class or interface declaring
     *                                  the underlying field (or a subclass or implementor thereof).
     */
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
     * Assert Field type match
     *
     * @param instance     Object or class
     * @param fieldName    field name
     * @param expectedType expected type
     * @throws IllegalArgumentException if type is not matched
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

}
