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
package io.microsphere.convert;

import io.microsphere.annotation.Nullable;
import io.microsphere.lang.Prioritized;

import static io.microsphere.convert.Converters.findConverter;
import static io.microsphere.reflect.TypeUtils.resolveActualTypeArgumentClass;
import static io.microsphere.util.ClassUtils.isAssignableFrom;


/**
 * A functional interface that defines a strategy for converting values from one type ({@code S}) to another type ({@code T}).
 * <p>
 * Implementations of this interface can be used to encapsulate conversion logic between types,
 * and they may optionally implement the {@link Prioritized} interface to control ordering when multiple
 * converters are available.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class StringToIntegerConverter implements Converter<String, Integer> {
 *     public boolean accept(Class<?> sourceType, Class<?> targetType) {
 *         return sourceType.isAssignableFrom(String.class) &&
 *                targetType.isAssignableFrom(Integer.class);
 *     }
 *
 *     public Integer convert(String source) {
 *         return Integer.parseInt(source);
 *     }
 * }
 * }</pre>
 *
 * <h3>Prioritized Behavior Example</h3>
 * <pre>{@code
 * public class HighPriorityStringToIntegerConverter implements Converter<String, Integer>, Prioritized {
 *     public int getPriority() {
 *         return Prioritized.MAX_PRIORITY; // Highest priority
 *     }
 *
 *     public boolean accept(Class<?> sourceType, Class<?> targetType) {
 *         return sourceType.isAssignableFrom(String.class) &&
 *                targetType.isAssignableFrom(Integer.class);
 *     }
 *
 *     public Integer convert(String source) {
 *         return Integer.parseInt(source);
 *     }
 * }
 * }</pre>
 *
 * @param <S> The source type
 * @param <T> The target type
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@FunctionalInterface
public interface Converter<S, T> extends Prioritized {

    /**
     * Accept the source type and target type or not
     *
     * @param sourceType the source type
     * @param targetType the target type
     * @return if accepted, return <code>true</code>, or <code>false</code>
     */
    default boolean accept(Class<?> sourceType, Class<?> targetType) {
        return isAssignableFrom(getSourceType(), sourceType) && isAssignableFrom(getTargetType(), targetType);
    }

    /**
     * Convert the source-typed value to the target-typed value
     *
     * @param source the source-typed value
     * @return the target-typed value
     */
    @Nullable
    T convert(@Nullable S source);

    /**
     * Get the source type
     *
     * @return non-null
     */
    default Class<S> getSourceType() {
        return resolveActualTypeArgumentClass(getClass(), Converter.class, 0);
    }

    /**
     * Get the target type
     *
     * @return non-null
     */
    default Class<T> getTargetType() {
        return resolveActualTypeArgumentClass(getClass(), Converter.class, 1);
    }

    /**
     * Retrieves a converter instance that can convert from the specified source type to the target type.
     * <p>
     * This method uses the service loader mechanism to find all available converters and sorts them based on their priority.
     * The first converter that accepts the given source and target types will be returned.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Class<String> sourceType = String.class;
     * Class<Integer> targetType = Integer.class;
     * Converter<String, Integer> converter = getConverter(sourceType, targetType);
     * if (converter != null) {
     *     Integer result = converter.convert("123");
     * }
     * }</pre>
     *
     * @param sourceType the class of the source type
     * @param targetType the class of the target type
     * @param <S>        the source type
     * @param <T>        the target type
     * @return a converter instance that can handle the specified types, or {@code null} if no suitable converter is found
     */
    static <S, T> Converter<S, T> getConverter(Class<S> sourceType, Class<T> targetType) {
        return findConverter(sourceType, targetType);
    }

    /**
     * Converts the given {@code source} object to the specified {@code targetType} if a suitable
     * converter is available.
     *
     * <p>This method attempts to find a converter that can convert from the type of the source
     * object to the target type using the service loader mechanism. If such a converter exists,
     * it will be used to perform the conversion. If no suitable converter is found, this method
     * returns {@code null}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String source = "123";
     * Integer result = convertIfPossible(source, Integer.class);
     * }</pre>
     *
     * @param source     the source object to be converted
     * @param targetType the target type to convert to
     * @param <T>        the type of the target
     * @return the converted object of type {@code T}, or {@code null} if no suitable converter is found
     */
    static <T> T convertIfPossible(Object source, Class<T> targetType) {
        Converter<Object, T> converter = (Converter<Object, T>) getConverter(source.getClass(), targetType);
        if (converter != null) {
            return converter.convert(source);
        }
        return null;
    }
}
