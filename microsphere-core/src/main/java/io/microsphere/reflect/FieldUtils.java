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

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Predicate;

import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.reflect.ReflectionUtils.execute;

/**
 * The Java Reflection {@link Field} Utility class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class FieldUtils {

    private FieldUtils() {
    }

    /**
     * Find the declared {@link Field} by its' name
     *
     * @param klass     {@link Class class} to find
     * @param fieldName the name of {@link Field field}
     * @return null if not found
     */
    public static Field findField(Class<?> klass, String fieldName) {
        if (Object.class.equals(klass)) {
            return null;
        }
        Field field = null;
        try {
            field = klass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // ignore, try the super class
            field = findField(klass.getSuperclass(), fieldName);
        }
        return field;
    }

    /**
     * Find the declared {@link Field} by its' name
     *
     * @param klass      {@link Class class} to find
     * @param fieldName  the name of {@link Field field}
     * @param predicates zero or more {@link Predicate}
     * @return null if not found
     */
    public static Field findField(Class<?> klass, String fieldName, Predicate<Field>... predicates) {
        Field field = findField(klass, fieldName);
        return and(predicates).test(field) ? field : null;
    }

    /**
     * Get {@link Field} Value
     *
     * @param object    the object whose field should be modified
     * @param fieldName field name
     * @param <T>       field type
     * @return {@link Field} Value
     */
    public static <T> T getFieldValue(Object object, String fieldName) {
        return (T) getFieldValue(object, fieldName, null);
    }

    /**
     * Get {@link Field} Value
     *
     * @param object       the object whose field should be modified
     * @param fieldName    field name
     * @param <T>          field type
     * @param defaultValue default value
     * @return {@link Field} Value
     */
    public static <T> T getFieldValue(Object object, String fieldName, T defaultValue) {
        T value = getFieldValue(object, fieldName);
        return value != null ? value : defaultValue;
    }

    /**
     * Get {@link Field} Value
     *
     * @param object    the object whose field should be modified
     * @param fieldName field name
     * @param fieldType field type
     * @param <T>       field type
     * @return {@link Field} Value
     */
    public static <T> T getFieldValue(Object object, String fieldName, Class<T> fieldType) {
        Field field = findField(object.getClass(), fieldName, f -> Objects.equals(fieldType, f.getType()));
        return getFieldValue(object, field);
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
        return getFieldValue((Object) null, field);
    }

    /**
     * Get the value of {@link Field}
     *
     * @param object the object whose field should be modified
     * @param field  {@link Field}
     * @param <T>    the field type
     * @return <code>null</code> if <code>field</code> is <code>null</code> or get failed
     */
    public static <T> T getFieldValue(Object object, Field field) {
        T fieldValue = null;
        if (field != null) {
            fieldValue = execute(field, f -> (T) field.get(object));
        }
        return fieldValue;
    }

    /**
     * Set the value to static {@link Field}
     *
     * @param klass      the class declared the field
     * @param fieldName  the name of {@link Field}
     * @param fieldValue the value of {@link Field}
     */
    public static void setStaticFieldValue(Class<?> klass, String fieldName, Object fieldValue) {
        Field field = findField(klass, fieldName);
        setFieldValue(null, field, fieldValue);
    }

    /**
     * Set the value to {@link Field}
     *
     * @param object     the object whose field should be modified
     * @param fieldName  the name of {@link Field}
     * @param fieldValue the value of {@link Field}
     */
    public static void setFieldValue(Object object, String fieldName, Object fieldValue) {
        Field field = findField(object.getClass(), fieldName);
        setFieldValue(object, field, fieldValue);
    }

    /**
     * Set the value to {@link Field}
     *
     * @param object     the object whose field should be modified
     * @param field      the {@link Field} object
     * @param fieldValue the value of {@link Field}
     */
    public static void setFieldValue(Object object, Field field, Object fieldValue) {
        execute(field, f -> {
            f.set(object, fieldValue);
        });
    }
}
