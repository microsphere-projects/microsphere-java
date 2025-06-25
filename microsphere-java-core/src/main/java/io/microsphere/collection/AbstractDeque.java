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

import java.util.AbstractQueue;
import java.util.Deque;
import java.util.NoSuchElementException;

/**
 * Abstract {@link Deque} implementation that provides default implementations for some of the
 * operations in the {@link Deque} interface.
 *
 * <p>This class extends {@link AbstractQueue}, which itself extends {@link java.util.AbstractCollection},
 * and hence inherits behaviors such as size, isEmpty, etc. Subclasses need to implement the abstract
 * methods defined by the deque contract, such as insertion, removal, and access methods.</p>
 *
 * <p>The default implementations in this class make assumptions about the behavior of other methods,
 * so subclasses should ensure consistent behavior when overriding any of these methods.</p>
 *
 * @param <E> The type of elements held in this deque
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class AbstractDeque<E> extends AbstractQueue<E> implements Deque<E> {

    @Override
    public void addFirst(E e) {
        if (!offerFirst(e))
            throw new IllegalStateException("Queue full");
    }

    @Override
    public void addLast(E e) {
        if (!offerLast(e))
            throw new IllegalStateException("Queue full");
    }

    @Override
    public E removeFirst() {
        E x = pollFirst();
        if (x != null)
            return x;
        else
            throw new NoSuchElementException();
    }

    @Override
    public E removeLast() {
        E x = pollLast();
        if (x != null)
            return x;
        else
            throw new NoSuchElementException();
    }

    @Override
    public E peekFirst() {
        return getFirst();
    }

    @Override
    public E peekLast() {
        return getLast();
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return remove(o);
    }

    @Override
    public void push(E e) {
        addFirst(e);
    }

    @Override
    public E pop() {
        return removeFirst();
    }

    @Override
    public boolean offer(E e) {
        return offerLast(e);
    }

    @Override
    public E poll() {
        return pollFirst();
    }

    @Override
    public E peek() {
        return peekFirst();
    }
}
