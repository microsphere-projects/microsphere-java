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

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

import static io.microsphere.util.ArrayUtils.reverse;
import static io.microsphere.util.ArrayUtils.toArrayReversed;

/**
 * Reverse ordered {@link Deque} based on JDK 21 {@link java.util.ReverseOrderDequeView}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Deque
 * @see java.util.ReverseOrderDequeView
 * @since 1.0.0
 */
public class ReversedDeque<E> extends DelegatingDeque<E> {

    public ReversedDeque(Deque<E> delegate) {
        super(delegate);
    }

    public static <T> Deque<T> of(Deque<T> deque) {
        if (deque instanceof ReversedDeque) {
            return deque;
        } else {
            return new ReversedDeque<>(deque);
        }
    }

    // ========== Iterable ==========

    public void forEach(Consumer<? super E> action) {
        for (E e : this)
            action.accept(e);
    }

    public Iterator<E> iterator() {
        return getDelegate().descendingIterator();
    }

    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, Spliterator.ORDERED);
    }

    // ========== Collection ==========

    public boolean add(E e) {
        getDelegate().addFirst(e);
        return true;
    }

    public boolean addAll(Collection<? extends E> c) {
        boolean modified = false;
        for (E e : c) {
            getDelegate().addFirst(e);
            modified = true;
        }
        return modified;
    }

    // copied from AbstractCollection
    public boolean remove(Object o) {
        Iterator<E> it = iterator();
        if (o == null) {
            while (it.hasNext()) {
                if (it.next() == null) {
                    it.remove();
                    return true;
                }
            }
        } else {
            while (it.hasNext()) {
                if (o.equals(it.next())) {
                    it.remove();
                    return true;
                }
            }
        }
        return false;
    }

    // copied from AbstractCollection
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<?> it = iterator();
        while (it.hasNext()) {
            if (c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    // copied from AbstractCollection
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    public Object[] toArray() {
        return reverse(super.toArray());
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        return toArrayReversed(getDelegate(), a);
    }

    // copied from AbstractCollection
    public String toString() {
        Iterator<E> it = iterator();
        if (!it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (; ; ) {
            E e = it.next();
            sb.append(e == this ? "(this Collection)" : e);
            if (!it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Collection)) {
            return false;
        }

        Collection<E> that = (Collection<E>) o;
        if (that.size() != size()) {
            return false;
        }

        Iterator<E> iterator = iterator();
        Iterator<E> otherIterator = ((Iterable<E>) o).iterator();
        while (iterator.hasNext() && otherIterator.hasNext()) {
            if (!Objects.equals(iterator.next(), otherIterator.next())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        Iterator<E> iterator = iterator();
        while (iterator.hasNext()) {
            E e = iterator.next();
            hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
        }
        return hashCode;
    }

    // ========== Deque and Queue ==========

    public void addFirst(E e) {
        getDelegate().addLast(e);
    }

    public void addLast(E e) {
        getDelegate().addFirst(e);
    }

    public Iterator<E> descendingIterator() {
        return getDelegate().iterator();
    }

    public E element() {
        return getDelegate().getLast();
    }

    public E getFirst() {
        return getDelegate().getLast();
    }

    public E getLast() {
        return getDelegate().getFirst();
    }

    public boolean offer(E e) {
        return getDelegate().offerFirst(e);
    }

    public boolean offerFirst(E e) {
        return getDelegate().offerLast(e);
    }

    public boolean offerLast(E e) {
        return getDelegate().offerFirst(e);
    }

    public E peek() {
        return getDelegate().peekLast();
    }

    public E peekFirst() {
        return getDelegate().peekLast();
    }

    public E peekLast() {
        return getDelegate().peekFirst();
    }

    public E poll() {
        return getDelegate().pollLast();
    }

    public E pollFirst() {
        return getDelegate().pollLast();
    }

    public E pollLast() {
        return getDelegate().pollFirst();
    }

    public E pop() {
        return getDelegate().removeLast();
    }

    public void push(E e) {
        getDelegate().addLast(e);
    }

    public E remove() {
        return getDelegate().removeLast();
    }

    public E removeFirst() {
        return getDelegate().removeLast();
    }

    public E removeLast() {
        return getDelegate().removeFirst();
    }

    public boolean removeFirstOccurrence(Object o) {
        return getDelegate().removeLastOccurrence(o);
    }

    public boolean removeLastOccurrence(Object o) {
        return getDelegate().removeFirstOccurrence(o);
    }
}
