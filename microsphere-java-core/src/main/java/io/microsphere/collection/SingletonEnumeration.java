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
 * A simple implementation of the {@link Enumeration} interface that contains a single element.
 * <p>
 * This enumeration can only return the singleton element once, after which it will
 * return false for subsequent calls to {@link #hasMoreElements()} and throw a
 * {@link NoSuchElementException} on subsequent calls to {@link #nextElement()}.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * String item = "Hello";
 * SingletonEnumeration<String> enumeration = new SingletonEnumeration<>(item);
 *
 * while (enumeration.hasMoreElements()) {
 *     System.out.println(enumeration.nextElement());
 * }
 * }</pre>
 *
 * <p>The output of the example would be:</p>
 * <pre>
 * Hello
 * </pre>
 *
 * @param <E> The type of the element in this enumeration.
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class SingletonEnumeration<E> implements Enumeration<E> {

    private final E element;

    private boolean hasNext = true;

    public SingletonEnumeration(E element) {
        this.element = element;
    }

    @Override
    public boolean hasMoreElements() {
        return hasNext;
    }

    @Override
    public E nextElement() {
        if (hasNext) {
            hasNext = false;
            return element;
        }
        throw new NoSuchElementException();
    }
}
