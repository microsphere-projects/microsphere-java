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

import io.microsphere.lang.Prioritized;
import io.microsphere.util.ClassLoaderUtils;

import java.util.Objects;
import java.util.ServiceLoader;

import static io.microsphere.reflect.TypeUtils.resolveActualTypeArgumentClass;
import static io.microsphere.util.ClassUtils.isAssignableFrom;
import static io.microsphere.util.ServiceLoaderUtils.loadServicesList;

/**
 * A class to convert the source-typed value to the target-typed value
 *
 * @param <S> The source type
 * @param <T> The target type
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
        return isAssignableFrom(sourceType, getSourceType()) && isAssignableFrom(targetType, getTargetType());
    }

    /**
     * Convert the source-typed value to the target-typed value
     *
     * @param source the source-typed value
     * @return the target-typed value
     */
    T convert(S source);

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
     * Get the Converter instance from {@link ServiceLoader} with the specified source and target type
     *
     * @param sourceType the source type
     * @param targetType the target type
     * @return
     */
    static <S, T> Converter<S, T> getConverter(Class<S> sourceType, Class<T> targetType) {
        ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
        return loadServicesList(Converter.class, classLoader).stream()
                .sorted()
                .filter(converter -> converter.accept(sourceType, targetType)).findFirst().orElse(null);
    }

    /**
     * Convert the value of source to target-type value if possible
     *
     * @param source     the value of source
     * @param targetType the target type
     * @param <T>        the target type
     * @return <code>null</code> if can't be converted
     */
    static <T> T convertIfPossible(Object source, Class<T> targetType) {
        Converter converter = getConverter(source.getClass(), targetType);
        if (converter != null) {
            return (T) converter.convert(source);
        }
        return null;
    }
}
