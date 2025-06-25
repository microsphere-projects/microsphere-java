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

import java.util.Iterator;

import static io.microsphere.collection.EmptyIterator.INSTANCE;

/**
 * An adapter that wraps an {@link Iterator} and provides an {@link Iterable} interface.
 * <p>
 * This allows the iteration over a sequence of elements using the enhanced for loop (for-each loop)
 * or other constructs that expect an {@link Iterable}.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 *     Iterator<String> iterator = Arrays.asList("one", "two", "three").iterator();
 *     Iterable<String> iterable = new IterableAdapter<>(iterator);
 *
 *     for (String value : iterable) {
 *         System.out.println(value);
 *     }
 * </pre>
 * </p>
 *
 * @param <T> the type of elements returned by the iterator.
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see CollectionUtils#toIterable(Iterator)
 * @since 1.0.0
 */
public class IterableAdapter<T> implements Iterable<T> {

    private final Iterator<T> iterator;

    public IterableAdapter(Iterator<T> iterator) {
        this.iterator = iterator == null ? INSTANCE : iterator;
    }

    @Override
    public Iterator<T> iterator() {
        return iterator;
    }
}
