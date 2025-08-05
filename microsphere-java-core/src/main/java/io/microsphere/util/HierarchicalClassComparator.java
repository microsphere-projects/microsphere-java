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

import static io.microsphere.util.ClassUtils.isAssignableFrom;

/**
 * {@link Comparator} for hierarchical class.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Comparator
 * @since 1.0.0
 */
public class HierarchicalClassComparator implements Comparator<Class<?>> {

    /**
     * The singleton for ascending order, from the top of the hierarchy to the bottom
     */
    public static final Comparator<Class<?>> ASCENT = new HierarchicalClassComparator();

    /**
     * The singleton for descending order, from the bottom of the hierarchy to the top
     */
    public static final Comparator<Class<?>> DESCENT = ASCENT.reversed();

    protected HierarchicalClassComparator() {
    }

    /**
     * Compares two classes based on their hierarchical relationship.
     * <p>
     * The comparison follows these rules:
     * <ul>
     *   <li>If both classes are the same, returns 0.</li>
     *   <li>If {@code o1} is a superclass or interface of {@code o2}, returns -1 (in ascending order).</li>
     *   <li>If {@code o2} is a superclass or interface of {@code o1}, returns 1 (in ascending order).</li>
     * </ul>
     * </p>
     * <p>
     * Examples (using ASCENT comparator):
     * <pre>
     * compare(Object.class, String.class)  => -1  // Object is superclass of String
     * compare(String.class, Object.class)  =>  1  // String is subclass of Object
     * compare(String.class, Integer.class) =>  1  // No direct relation, but returns positive
     * compare(String.class, String.class)  =>  0  // Same class
     * </pre>
     * </p>
     *
     * @param o1 the first class to be compared
     * @param o2 the second class to be compared
     * @return a negative integer, zero, or a positive integer as the first argument is less than,
     * equal to, or greater than the second according to hierarchical ordering.
     */
    @Override
    public int compare(Class<?> o1, Class<?> o2) {
        if (o1 == o2) {
            return 0;
        }
        if (isAssignableFrom(o2, o1)) {
            return 1;
        }
        return -1;
    }
}
