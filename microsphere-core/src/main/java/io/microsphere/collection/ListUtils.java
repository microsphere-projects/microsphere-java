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

import io.microsphere.util.BaseUtils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static io.microsphere.collection.CollectionUtils.toIterator;
import static java.util.Collections.unmodifiableList;

/**
 * The utilities class for Java {@link List}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see List
 * @since 1.0.0
 */
public abstract class ListUtils extends BaseUtils {

    public static boolean isList(Iterable<?> values) {
        return values instanceof List;
    }

    public static <E> List<E> toList(Iterable<E> iterable) {
        return toList(iterable.iterator());
    }

    public static <E> List<E> toList(Enumeration<E> enumeration) {
        return toList(toIterator(enumeration));
    }

    public static <E> List<E> toList(Iterator<E> iterator) {
        List<E> list = newLinkedList();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    public static <E> List<E> asList(Iterable<E> iterable) {
        return asList(iterable.iterator());
    }

    public static <E> List<E> asList(Enumeration<E> enumeration) {
        return asList(toIterator(enumeration));
    }

    public static <E> List<E> asList(Iterator<E> iterator) {
        return unmodifiableList(toList(iterator));
    }

    public static <E> List<E> newArrayList(int size) {
        return new ArrayList<>(size);
    }

    public static <E> List<E> newArrayList(Iterable<E> values) {
        List<E> list = newArrayList();
        for (E value : values) {
            list.add(value);
        }
        return list;
    }

    public static <E> List<E> newArrayList() {
        return new ArrayList<>();
    }

    public static <E> List<E> newLinkedList(Iterable<E> values) {
        List<E> list = newLinkedList();
        for (E value : values) {
            list.add(value);
        }
        return list;
    }

    public static <E> List<E> newLinkedList() {
        return new LinkedList<>();
    }

}
