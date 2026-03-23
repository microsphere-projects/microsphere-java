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

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.Objects;

import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.util.AnnotationUtils.findAnnotation;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.ClassUtils.getType;

/**
 * A {@link Comparator} implementation that sorts objects based on the value of the
 * {@link javax.annotation.Priority} annotation.
 *
 * <p>If an object does not have the {@link javax.annotation.Priority} annotation,
 * it is treated as having a default priority value of -1. If both objects lack the
 * annotation, they are considered equal in priority.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * List<Object> list = new ArrayList<>();
 * list.add(new HighPriorityService());
 * list.add(new LowPriorityService());
 * Collections.sort(list, PriorityComparator.INSTANCE);
 * }</pre>
 *
 * <p>Note: The comparison is consistent with equals only if the compared objects
 * are of the same class or both lack the Priority annotation.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see javax.annotation.Priority
 * @since 1.0.0
 */
public class PriorityComparator implements Comparator<Object> {

    private static final Class PRIORITY_CLASS = resolveClass("javax.annotation.Priority");

    private static final int UNDEFINED_VALUE = -1;

    /**
     * Singleton instance of {@link PriorityComparator}
     */
    public static final PriorityComparator INSTANCE = new PriorityComparator();

    /**
     * Compares two objects based on the value of their {@link javax.annotation.Priority} annotation.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   PriorityComparator comparator = PriorityComparator.INSTANCE;
     *   int result = comparator.compare(serviceA, serviceB);
     *   if (result < 0) {
     *       System.out.println("serviceA has higher priority");
     *   }
     * }</pre>
     *
     * @param o1 the first object to compare
     * @param o2 the second object to compare
     * @return a negative integer, zero, or a positive integer as the first object's priority
     *         is less than, equal to, or greater than the second object's priority
     * @since 1.0.0
     */
    @Override
    public int compare(Object o1, Object o2) {
        return compare(getType(o1), getType(o2));
    }

    /**
     * Compares two classes based on the value of the default {@link javax.annotation.Priority} annotation.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   int result = PriorityComparator.compare(HighPriorityService.class, LowPriorityService.class);
     *   System.out.println("Comparison result: " + result);
     * }</pre>
     *
     * @param type1 the first class to compare
     * @param type2 the second class to compare
     * @return a negative integer, zero, or a positive integer as the first class's priority
     *         is less than, equal to, or greater than the second class's priority
     * @since 1.0.0
     */
    static int compare(Class<?> type1, Class<?> type2) {
        return compare(type1, type2, PRIORITY_CLASS);
    }

    /**
     * Compares two classes based on the value of the specified priority annotation.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   int result = PriorityComparator.compare(
     *       HighPriorityService.class,
     *       LowPriorityService.class,
     *       javax.annotation.Priority.class
     *   );
     *   System.out.println("Comparison result: " + result);
     * }</pre>
     *
     * @param type1         the first class to compare
     * @param type2         the second class to compare
     * @param priorityClass the priority annotation class used to extract priority values
     * @return a negative integer, zero, or a positive integer as the first class's priority
     *         is less than, equal to, or greater than the second class's priority;
     *         returns {@code 0} if the classes are equal or the priority annotation class is {@code null}
     * @since 1.0.0
     */
    static int compare(Class<?> type1, Class<?> type2, Class<? extends Annotation> priorityClass) {
        if (Objects.equals(type1, type2) || priorityClass == null) {
            return 0;
        }

        Object priority1 = findAnnotation(type1, priorityClass);
        Object priority2 = findAnnotation(type2, priorityClass);

        int priorityValue1 = getValue(priority1);
        int priorityValue2 = getValue(priority2);

        return Integer.compare(priorityValue1, priorityValue2);
    }

    /**
     * Extracts the integer priority value from a priority annotation instance.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Annotation priority = HighPriorityService.class.getAnnotation(javax.annotation.Priority.class);
     *   int value = PriorityComparator.getValue(priority);
     *   System.out.println("Priority value: " + value);
     * }</pre>
     *
     * @param priority the priority annotation instance, or {@code null} if not annotated
     * @return the priority value, or {@code -1} if the annotation is {@code null} or the value is negative
     * @since 1.0.0
     */
    static int getValue(Object priority) {
        int value = priority == null ? UNDEFINED_VALUE : invokeMethod(priority, "value");
        return value < 0 ? UNDEFINED_VALUE : value;
    }
}