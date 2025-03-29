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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.microsphere.collection.CollectionUtils.size;
import static io.microsphere.collection.CollectionUtils.toIterable;
import static io.microsphere.collection.CollectionUtils.toIterator;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

/**
 * The utilities class for Java {@link List}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see List
 * @since 1.0.0
 */
public abstract class ListUtils {

    public static boolean isList(Object values) {
        return values instanceof List;
    }

    public static <E> List<E> of(E... elements) {
        return ofList(elements);
    }

    /**
     * Create a {@link List} from the specified array
     *
     * @param elements
     * @param <E>
     * @return
     * @see {@link Lists#ofList} as recommended
     */
    public static <E> List<E> ofList(E... elements) {
        if (isEmpty(elements)) {
            return emptyList();
        }
        return unmodifiableList(asList(elements));
    }

    public static <E> List<E> ofList(Iterable<E> iterable) {
        if (iterable == null) {
            return emptyList();
        } else if (isList(iterable)) {
            return unmodifiableList((List) iterable);
        } else {
            return ofList(iterable.iterator());
        }
    }

    public static <E> List<E> ofList(Enumeration<E> enumeration) {
        return ofList(toIterator(enumeration));
    }

    public static <E> List<E> ofList(Iterator<E> iterator) {
        if (iterator == null) {
            return emptyList();
        }
        List<E> list = newLinkedList();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return unmodifiableList(list);
    }

    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<>();
    }

    public static <E> ArrayList<E> newArrayList(int size) {
        return new ArrayList<>(size);
    }

    public static <E> LinkedList<E> newArrayList(Enumeration<E> values) {
        return newLinkedList(toIterable(values));
    }

    public static <E> ArrayList<E> newArrayList(Iterable<E> values) {
        return newArrayList(values.iterator());
    }

    public static <E> ArrayList<E> newArrayList(Iterator<E> iterator) {
        ArrayList<E> list = newArrayList();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    public static <E> LinkedList<E> newLinkedList() {
        return new LinkedList<>();
    }

    public static <E> LinkedList<E> newLinkedList(Enumeration<E> values) {
        return newLinkedList(toIterable(values));
    }

    public static <E> LinkedList<E> newLinkedList(Iterable<E> values) {
        return newLinkedList(values.iterator());
    }

    public static <E> LinkedList<E> newLinkedList(Iterator<E> iterator) {
        LinkedList<E> list = newLinkedList();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    public static <T> void forEach(List<T> values, BiConsumer<Integer, T> indexedElementConsumer) {
        int length = size(values);
        for (int i = 0; i < length; i++) {
            T value = values.get(i);
            indexedElementConsumer.accept(i, value);
        }
    }

    public static <T> void forEach(List<T> values, Consumer<T> consumer) {
        forEach(values, (i, e) -> consumer.accept(e));
    }
}
