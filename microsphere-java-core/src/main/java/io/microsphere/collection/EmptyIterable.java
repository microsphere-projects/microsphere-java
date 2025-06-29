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

/**
 * An empty {@link Iterable} implementation that always returns an empty iterator.
 * <p>
 * This class is a singleton-friendly extension of {@link IterableAdapter}, primarily useful for representing
 * an empty collection view. It guarantees that the method {@link #iterator()} will return an instance of
 * {@link EmptyIterator#INSTANCE}, ensuring consistent behavior across uses.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * Iterable<String> emptyIterable = EmptyIterable.INSTANCE;
 * for (String item : emptyIterable) {
 *     // This loop will not execute as the iterable is empty.
 * }
 * }</pre>
 *
 * @param <E> the type of elements returned by the iterator
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Iterable
 * @see EmptyIterator
 * @since 1.0.0
 */
public class EmptyIterable<E> extends IterableAdapter<E> {

    /**
     * Singleton instance of {@link EmptyIterator}
     */
    public static final EmptyIterable INSTANCE = new EmptyIterable();

    public EmptyIterable() {
        super(null);
    }
}
