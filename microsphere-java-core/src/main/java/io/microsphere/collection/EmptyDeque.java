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

import static java.util.Collections.emptyIterator;

/**
 * An empty and immutable implementation of the Deque interface.
 * <p>
 * This class implements a {@link Deque} that is always empty. All operations that attempt to modify the deque will result in an
 * {@link UnsupportedOperationException}. It is serializable and guarantees consistent behavior across different environments.
 * </p>
 *
 * @param <E> The type of elements held in this deque.
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractDeque
 * @since 1.0.0
 */
public class EmptyDeque<E> extends AbstractDeque<E> implements Serializable {

    private static final long serialVersionUID = -1L;

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
        throw new UnsupportedOperationException();
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E getFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E getLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }
}
