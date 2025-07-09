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

import java.util.Comparator;
import java.util.Objects;

import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.util.AnnotationUtils.findAnnotation;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;

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

    @Override
    public int compare(Object o1, Object o2) {
        return compare(asClass(o1), asClass(o2));
    }

    public static int compare(Class<?> type1, Class<?> type2) {
        if (Objects.equals(type1, type2) || PRIORITY_CLASS == null) {
            return 0;
        }

        Object priority1 = findAnnotation(type1, PRIORITY_CLASS);
        Object priority2 = findAnnotation(type2, PRIORITY_CLASS);

        int priorityValue1 = getValue(priority1);
        int priorityValue2 = getValue(priority2);

        return Integer.compare(priorityValue1, priorityValue2);
    }

    private static Class<?> asClass(Object object) {
        return object instanceof Class ? (Class) object : object.getClass();
    }

    private static int getValue(Object priority) {
        int value = priority == null ? UNDEFINED_VALUE : invokeMethod(priority, "value");
        return value < 0 ? UNDEFINED_VALUE : value;
    }

}
