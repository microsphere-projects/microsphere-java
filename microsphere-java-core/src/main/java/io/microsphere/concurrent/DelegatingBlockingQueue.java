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
package io.microsphere.concurrent;

import io.microsphere.lang.DelegatingWrapper;
import io.microsphere.lang.Wrapper;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A delegating implementation of {@link BlockingQueue} that wraps another
 * {@link BlockingQueue} instance and forwards all method calls to it.
 * <p>
 * This class can be used as a base class for decorators that add functionality
 * to a {@link BlockingQueue}, such as adding instrumentation, validation,
 * or other cross-cutting concerns.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * BlockingQueue<String> delegate = new LinkedBlockingQueue<>();
 * DelegatingBlockingQueue<String> queue = new DelegatingBlockingQueue<>(delegate);
 *
 * queue.put("item");              // Adds an element to the queue
 * String item = queue.take();     // Retrieves and removes an element
 * }</pre>
 *
 * @param <E> The type of elements held in this queue.
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class DelegatingBlockingQueue<E> implements BlockingQueue<E>, DelegatingWrapper {

    private final BlockingQueue<E> delegate;

    public DelegatingBlockingQueue(BlockingQueue<E> delegate) {
        BlockingQueue<E> unwrapper = Wrapper.tryUnwrap(delegate, BlockingQueue.class);
        this.delegate = unwrapper == null ? delegate : unwrapper;
    }

    @Override
    public Iterator<E> iterator() {
        return this.delegate.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.delegate.toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.delegate.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return this.delegate.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.delegate.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return this.delegate.removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.delegate.retainAll(c);
    }

    @Override
    public void clear() {
        this.delegate.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BlockingQueue)) {
            return false;
        }
        if (o instanceof DelegatingBlockingQueue) {
            DelegatingBlockingQueue that = (DelegatingBlockingQueue) o;
            return this.delegate.equals(that.delegate);
        }
        return this.delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return this.delegate.hashCode();
    }

    @Override
    public Spliterator<E> spliterator() {
        return this.delegate.spliterator();
    }

    @Override
    public Stream<E> stream() {
        return this.delegate.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return this.delegate.parallelStream();
    }

    @Override
    public boolean add(E e) {
        return this.delegate.add(e);
    }

    @Override
    public boolean offer(E e) {
        return this.delegate.offer(e);
    }

    @Override
    public void put(E e) throws InterruptedException {
        this.delegate.put(e);
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return this.delegate.offer(e, timeout, unit);
    }

    @Override
    public E take() throws InterruptedException {
        return this.delegate.take();
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return this.delegate.poll(timeout, unit);
    }

    @Override
    public int remainingCapacity() {
        return this.delegate.remainingCapacity();
    }

    @Override
    public boolean remove(Object o) {
        return this.delegate.remove(o);
    }

    @Override
    public boolean contains(Object o) {
        return this.delegate.contains(o);
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return this.delegate.drainTo(c);
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        return this.delegate.drainTo(c, maxElements);
    }

    @Override
    public E remove() {
        return this.delegate.remove();
    }

    @Override
    public E poll() {
        return this.delegate.poll();
    }

    @Override
    public E element() {
        return this.delegate.element();
    }

    @Override
    public E peek() {
        return this.delegate.peek();
    }

    @Override
    public int size() {
        return this.delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        this.delegate.forEach(action);
    }

    @Override
    public String toString() {
        return this.delegate.toString();
    }

    @Override
    public Object getDelegate() {
        return this.delegate;
    }
}
