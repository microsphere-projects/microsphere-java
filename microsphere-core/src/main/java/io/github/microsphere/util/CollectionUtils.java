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
package io.github.microsphere.util;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.Float.MIN_NORMAL;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

/**
 * The utilities class for Java Collection
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @version 1.0.0
 */
public abstract class CollectionUtils extends BaseUtils {

    private static final Deque EMPTY_DEQUE = new EmptyDeque();

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isSet(Iterable<?> values) {
        return values instanceof Set;
    }

    public static boolean isList(Iterable<?> values) {
        return values instanceof List;
    }

    public static boolean isQueue(Iterable<?> values) {
        return values instanceof Queue;
    }

    public static <E> Queue<E> emptyQueue() {
        return EMPTY_DEQUE;
    }

    public static <E> Iterator<E> toIterator(Enumeration<E> enumeration) {
        return new EnumerationIteratorAdapter(enumeration);
    }

    public static <E> Iterable<E> toIterable(Enumeration<E> enumeration) {
        return new EnumerationIterableAdapter(enumeration);
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

    public static <E> List<E> ofList(Iterable<E> iterable) {
        return ofList(iterable.iterator());
    }

    public static <E> List<E> ofList(Enumeration<E> enumeration) {
        return ofList(toIterator(enumeration));
    }

    public static <E> List<E> ofList(Iterator<E> iterator) {
        return unmodifiableList(toList(iterator));
    }

    /**
     * Convert to multiple values to be {@link LinkedHashSet}
     *
     * @param values one or more values
     * @param <T>    the type of <code>values</code>
     * @return read-only {@link Set}
     */
    public static <T> Set<T> ofSet(Iterable<T> values) {
        return unmodifiableSet(newLinkedHashSet(values));
    }

    /**
     * Convert to multiple values to be {@link LinkedHashSet}
     *
     * @param values one or more values
     * @param <T>    the type of <code>values</code>
     * @return read-only {@link Set}
     */
    public static <T> Set<T> ofSet(T... values) {
        int size = values == null ? 0 : values.length;
        if (size < 1) {
            return emptySet();
        }

        Set<T> elements = new LinkedHashSet<>(size, MIN_NORMAL);
        for (int i = 0; i < size; i++) {
            elements.add(values[i]);
        }
        return unmodifiableSet(elements);
    }

    public static <T> Set<T> asSet(T... values) {
        int size = values == null ? 0 : values.length;
        if (size < 1) {
            return emptySet();
        }

        float loadFactor = 1f / ((size + 1) * 1.0f);

        if (loadFactor > 0.75f) {
            loadFactor = 0.75f;
        }

        Set<T> elements = new LinkedHashSet<>(size, loadFactor);
        for (int i = 0; i < size; i++) {
            elements.add(values[i]);
        }
        return unmodifiableSet(elements);
    }

    public static <T> Set<T> newLinkedHashSet() {
        return new LinkedHashSet<>();
    }

    public static <T> Set<T> newLinkedHashSet(Iterable<T> values) {
        Set<T> set = newLinkedHashSet();
        values.forEach(set::add);
        return set;
    }

    public static <T> List<T> newArrayList(int size) {
        return new ArrayList<>(size);
    }

    public static <T> List<T> newArrayList(Iterable<T> values) {
        List<T> list = new ArrayList<>();
        values.forEach(list::add);
        return list;
    }

    public static <T> List<T> newLinkedList(Iterable<T> values) {
        List<T> list = newLinkedList();
        values.forEach(list::add);
        return list;
    }

    public static <T> List<T> newLinkedList() {
        return new LinkedList<>();
    }

    /**
     * Get the size of the specified {@link Collection}
     *
     * @param collection the specified {@link Collection}
     * @return must be positive number
     */
    public static int size(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    /**
     * Compares the specified collection with another, the main implementation references
     * {@link AbstractSet}
     *
     * @param one     {@link Collection}
     * @param another {@link Collection}
     * @return if equals, return <code>true</code>, or <code>false</code>
     */
    public static boolean equals(Collection<?> one, Collection<?> another) {

        if (one == another) {
            return true;
        }

        if (isEmpty(one) && isEmpty(another)) {
            return true;
        }

        if (size(one) != size(another)) {
            return false;
        }

        try {
            return one.containsAll(another);
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }
    }

    /**
     * Add the multiple values into {@link Collection the specified collection}
     *
     * @param collection {@link Collection the specified collection}
     * @param values     the multiple values
     * @param <T>        the type of values
     * @return the effected count after added
     */
    public static <T> int addAll(Collection<T> collection, T... values) {

        int size = values == null ? 0 : values.length;

        if (collection == null || size < 1) {
            return 0;
        }

        int effectedCount = 0;
        for (int i = 0; i < size; i++) {
            if (collection.add(values[i])) {
                effectedCount++;
            }
        }

        return effectedCount;
    }

    /**
     * Take the first element from the specified collection
     *
     * @param values the collection object
     * @param <T>    the type of element of collection
     * @return if found, return the first one, or <code>null</code>
     */
    public static <T> T first(Collection<T> values) {
        if (isEmpty(values)) {
            return null;
        }
        if (values instanceof List) {
            List<T> list = (List<T>) values;
            return list.get(0);
        } else {
            return values.iterator().next();
        }
    }

    /**
     * Shallow Clone {@link Map}
     *
     * @param source the source of {@link Map}
     * @param <K>    the {@link Class type} of key
     * @param <V>    the {@link Class type} of value
     * @return non-null
     */
    @Nonnull
    public static <K, V> Map<K, V> shallowCloneMap(@Nonnull Map<K, V> source) {
        if (source instanceof SortedMap) {
            return new TreeMap(source);
        } else if (source instanceof LinkedHashMap) {
            return new LinkedHashMap(source);
        } else if (source instanceof IdentityHashMap) {
            return new IdentityHashMap(source);
        } else if (source instanceof ConcurrentNavigableMap) {
            return new ConcurrentSkipListMap(source);
        } else if (source instanceof ConcurrentMap) {
            return new ConcurrentHashMap<>(source);
        } else {
            return new HashMap(source);
        }
    }

    public static <E> Queue<E> unmodifiableQueue(Queue<E> queue) {
        return new UnmodifiableQueue(queue);
    }

    static class EmptyDeque<E> extends AbstractQueue<E> implements Deque<E> {

        @Override
        public Iterator<E> iterator() {
            return null;
        }

        @Override
        public Iterator<E> descendingIterator() {
            return null;
        }

        @Override
        public void addFirst(E e) {

        }

        @Override
        public void addLast(E e) {

        }

        @Override
        public boolean offerFirst(E e) {
            return false;
        }

        @Override
        public boolean offerLast(E e) {
            return false;
        }

        @Override
        public E removeFirst() {
            return null;
        }

        @Override
        public E removeLast() {
            return null;
        }

        @Override
        public E pollFirst() {
            return null;
        }

        @Override
        public E pollLast() {
            return null;
        }

        @Override
        public E getFirst() {
            return null;
        }

        @Override
        public E getLast() {
            return null;
        }

        @Override
        public E peekFirst() {
            return null;
        }

        @Override
        public E peekLast() {
            return null;
        }

        @Override
        public boolean removeFirstOccurrence(Object o) {
            return false;
        }

        @Override
        public boolean removeLastOccurrence(Object o) {
            return false;
        }

        @Override
        public void push(E e) {

        }

        @Override
        public E pop() {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean offer(E e) {
            return false;
        }

        @Override
        public E poll() {
            return null;
        }

        @Override
        public E peek() {
            return null;
        }
    }


    static class UnmodifiableQueue<E> extends AbstractQueue<E> implements Queue<E>, Serializable {

        private static final long serialVersionUID = -1578116770333032259L;

        private final Collection<E> delegate;

        UnmodifiableQueue(Queue<E> queue) {
            this.delegate = unmodifiableCollection(queue);
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return delegate.contains(o);
        }

        @Override
        public Iterator<E> iterator() {
            return delegate.iterator();
        }

        @Override
        public Object[] toArray() {
            return delegate.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return delegate.toArray(a);
        }

        @Override
        public boolean offer(E e) {
            return delegate.add(e);
        }

        @Override
        public E poll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public E peek() {
            Iterator<E> iterator = iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            }
            return null;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return delegate.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            return delegate.addAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return delegate.removeAll(c);
        }

        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            return delegate.removeIf(filter);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return delegate.retainAll(c);
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @Override
        public boolean equals(Object o) {
            return delegate.equals(o);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public Spliterator<E> spliterator() {
            return delegate.spliterator();
        }

        @Override
        public Stream<E> stream() {
            return delegate.stream();
        }

        @Override
        public Stream<E> parallelStream() {
            return delegate.parallelStream();
        }

        @Override
        public void forEach(Consumer<? super E> action) {
            delegate.forEach(action);
        }
    }

    static class EnumerationIterableAdapter<E> implements Iterable<E> {

        private final Enumeration<E> enumeration;

        EnumerationIterableAdapter(Enumeration<E> enumeration) {
            this.enumeration = enumeration;
        }

        @Override
        public Iterator<E> iterator() {
            return new EnumerationIteratorAdapter(enumeration);
        }
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
