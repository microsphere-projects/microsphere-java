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
import java.util.Iterator;

import static java.util.Collections.emptyEnumeration;

/**
 * {@link Iterator} Adapter based on {@link Enumeration}
 *
 * @param <E> The elements' type
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class EnumerationIteratorAdapter<E> extends ReadOnlyIterator<E> {

    private final Enumeration<E> enumeration;

    EnumerationIteratorAdapter(Enumeration<E> enumeration) {
        this.enumeration = enumeration == null ? emptyEnumeration() : enumeration;
    }

    @Override
    public boolean hasNext() {
        return enumeration.hasMoreElements();
    }

    @Override
    public E next() {
        return enumeration.nextElement();
    }
}
