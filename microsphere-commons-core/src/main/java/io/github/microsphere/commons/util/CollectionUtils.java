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
package io.github.microsphere.commons.util;

import java.util.*;

/**
 * The utilities class for Java Collection
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @version 1.0.0
 */
public abstract class CollectionUtils {

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static <E> List<E> toList(Iterable<E> iterable) {
        return toList(iterable.iterator());
    }

    public static <E> List<E> toList(Iterator<E> iterator) {
        List<E> list = new LinkedList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    public static <E> List<E> toList(Enumeration<E> enumeration) {
        return toList(new EnumerationIteratorAdapter(enumeration));
    }

    static class EnumerationIteratorAdapter<E> implements Iterator<E> {

        private final Enumeration<E> enumeration;

        EnumerationIteratorAdapter(Enumeration<E> enumeration) {
            this.enumeration = enumeration;
        }

        @Override
        public boolean hasNext() {
            return enumeration.hasMoreElements();
        }

        @Override
        public E next() {
            return enumeration.nextElement();
        }
    }

}
