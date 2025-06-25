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

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * <p>{@code ArrayEnumeration} is an implementation of enumeration based on an array, 
 * used to sequentially access elements in the array.</p>
 *
 * <p>This class implements the {@link Enumeration} interface and is suitable for scenarios 
 * where read-only sequential access to array elements is required.</p>
 *
 * <h3>Example</h3>
 * <pre>
 * String[] data = {"one", "two", "three"};
 * ArrayEnumeration<String> enumeration = new ArrayEnumeration<>(data);
 * while (enumeration.hasMoreElements()) {
 *     System.out.println(enumeration.nextElement());
 * }
 * </pre>
 *
 * @param <E> the type of elements in the array
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ArrayEnumeration<E> implements Enumeration<E> {

    private final E[] elements;

    private final int size;

    private int position;

    public ArrayEnumeration(E[] elements) {
        this.elements = elements;
        this.size = elements.length;
        this.position = 0;
    }

    @Override
    public boolean hasMoreElements() {
        return position < size;
    }

    @Override
    public E nextElement() {
        if (!hasMoreElements()) {
            throw new NoSuchElementException("No more elements exist");
        }
        return elements[position++];
    }
}
