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

import io.microsphere.util.BaseUtils;

import java.lang.invoke.MethodHandle;
import java.util.List;

import static io.microsphere.collection.ListUtils.of;
import static io.microsphere.invoke.MethodHandlesLookupUtils.findPublicStatic;

/**
 * The utility class for {@link List}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see List
 * @since 1.0.0
 */
public abstract class Lists extends BaseUtils {

    /**
     * The {@link MethodHandle} of {@link List#of(Object...)} since JDK 9
     */
    private static final MethodHandle ofMethodHandle = findPublicStatic(List.class, "of", Object[].class);

    /**
     * Returns an unmodifiable list containing an arbitrary number of elements.
     *
     * @param elements the elements array
     * @param <E>      the element type
     * @return non-null
     * @see ListUtils#ofList(E...)
     */
    public static <E> List<E> ofList(E... elements) {
        if (ofMethodHandle == null) {
            return of(elements);
        }
        try {
            return (List<E>) ofMethodHandle.invokeExact(elements);
        } catch (Throwable e) {
            return of(elements);
        }
    }
}
