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

/**
 * A skeletal implementation of the {@link Iterator} interface, designed to support
 * read-only iteration. This class provides a default implementation for the 
 * {@link #remove()} method, which throws an {@link IllegalStateException}, 
 * indicating that removal is not supported.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class MyReadOnlyIterator extends ReadOnlyIterator<String> {
 *     private String[] data = {"apple", "banana", "cherry"};
 *     private int index = 0;
 *
 *     @Override
 *     public boolean hasNext() {
 *         return index < data.length;
 *     }
 *
 *     @Override
 *     public String next() {
 *         return data[index++];
 *     }
 * }
 * }</pre>
 *
 * @param <E> the type of elements returned by this iterator
 */
public abstract class ReadOnlyIterator<E> implements Iterator<E> {

    @Override
    public final void remove() {
        throw new IllegalStateException("Read-Only");
    }

}
