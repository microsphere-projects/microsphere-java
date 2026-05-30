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

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * An {@link Iterator} that is unmodifiable, meaning the elements cannot be removed.
 * This class extends the {@link ReadOnlyIterator}, which throws an exception when the
 * remove operation is attempted.
 *
 * <h3>Example Usage</h3>
 * <pre>
 *   List<String> list = Arrays.asList("one", "two", "three");
 *   Iterator<String> unmodifiableIterator = new UnmodifiableIterator<>(list.iterator());
 *
 *   while (unmodifiableIterator.hasNext()) {
 *       System.out.println(unmodifiableIterator.next());
 *   }
 *
 *   // Attempting to remove will throw an IllegalStateException
 *   try {
 *       unmodifiableIterator.remove();
 *   } catch (IllegalStateException e) {
 *       System.out.println("Cannot remove elements from an unmodifiable iterator.");
 *   }
 * </pre>
 *
 * @param <E> the type of elements returned by this iterator
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@Immutable
public class UnmodifiableIterator<E> extends ReadOnlyIterator<E> {

    private final Iterator<E> delegate;

    public UnmodifiableIterator(Iterator<E> delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public E next() {
        return delegate.next();
    }

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
        delegate.forEachRemaining(action);
    }
}
