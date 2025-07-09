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

import java.io.Serializable;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static io.microsphere.collection.CollectionUtils.emptyIterator;

/**
 * An empty and immutable {@link Deque} implementation that throws {@link UnsupportedOperationException}
 * for methods that attempt to modify the deque, and returns appropriate default values for read-only operations.
 *
 * <p>This class is a singleton implementation, and it's serializable. It's useful as a placeholder or
 * default value where an empty deque is needed without creating a new instance every time.</p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * Deque<String> deque = EmptyDeque.INSTANCE;
 *
 * // Reading from the deque
 * System.out.println(deque.isEmpty()); // true
 * System.out.println(deque.size());    // 0
 *
 * // Iterating over elements (no output)
 * for (String element : deque) {
 *     System.out.println(element);
 * }
 *
 * // Attempting to modify will throw an exception
 * try {
 *     deque.add("test");
 * } catch (UnsupportedOperationException e) {
 *     System.out.println("Modification not allowed: " + e.getMessage());
 * }
 * }</pre>
 *
 * @param <E> The type of elements held in this deque
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractDeque
 * @since 1.0.0
 */
public class EmptyDeque<E> extends AbstractDeque<E> implements Serializable {

    private static final long serialVersionUID = -1L;

    public static final EmptyDeque<?> INSTANCE = new EmptyDeque<>();

    @Override
    public Iterator<E> iterator() {
        return emptyIterator();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return emptyIterator();
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
        return null;
    }

    @Override
    public E pollLast() {
        return null;
    }

    @Override
    public E getFirst() {
        throw new NoSuchElementException();
    }

    @Override
    public E getLast() {
        throw new NoSuchElementException();
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
    public boolean removeLastOccurrence(Object o) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }
}
