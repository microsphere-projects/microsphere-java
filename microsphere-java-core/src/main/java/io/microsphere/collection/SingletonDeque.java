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

import io.microsphere.annotation.Immutable;

import java.io.Serializable;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;

import static io.microsphere.collection.CollectionUtils.singletonIterator;

/**
 * A {@link Deque} implementation that holds a single, immutable element.
 * <p>
 * This class provides a lightweight, read-only deque structure containing exactly one element.
 * All operations that attempt to modify the deque (e.g., add, remove, poll) will throw an
 * {@link UnsupportedOperationException} because the deque is designed to be immutable.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * SingletonDeque<String> deque = new SingletonDeque<>("Hello");
 *
 * System.out.println(deque.getFirst()); // Output: Hello
 * System.out.println(deque.getLast());  // Output: Hello
 * System.out.println(deque.size());     // Output: 1
 *
 * try {
 *     deque.addFirst("World");
 * } catch (UnsupportedOperationException e) {
 *     System.out.println("Modification not allowed");
 * }
 *
 * Iterator<String> it = deque.iterator();
 * while (it.hasNext()) {
 *     System.out.println(it.next()); // Output: Hello
 * }
 * }</pre>
 *
 * @param <E> The type of the singleton element held in this deque
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractDeque
 * @since 1.0.0
 */
@Immutable
public class SingletonDeque<E> extends AbstractDeque<E> implements Serializable {

    private static final long serialVersionUID = -1L;

    private final E element;

    public SingletonDeque(E element) {
        this.element = element;
    }

    @Override
    public Iterator<E> iterator() {
        return singletonIterator(element);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return singletonIterator(element);
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean offerFirst(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean offerLast(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E getFirst() {
        return element;
    }

    @Override
    public E getLast() {
        return element;
    }

    @Override
    public E peekFirst() {
        return element;
    }

    @Override
    public E peekLast() {
        return element;
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof SingletonDeque)) return false;

        SingletonDeque<?> that = (SingletonDeque<?>) o;
        return Objects.equals(element, that.element);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(element);
    }
}
