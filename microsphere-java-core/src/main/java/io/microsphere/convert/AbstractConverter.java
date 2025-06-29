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

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.lang.Prioritized;
import io.microsphere.logging.Logger;
import io.microsphere.logging.LoggerFactory;

import java.util.List;
import java.util.Objects;

import static io.microsphere.util.ClassUtils.getAllClasses;
import static io.microsphere.util.ClassUtils.getTypeName;
import static io.microsphere.util.ExceptionUtils.wrap;
import static java.util.Objects.hash;

/**
 * An abstract base class for implementing the {@link Converter} interface.
 *
 * <p>This class provides a default implementation for priority resolution based on the inheritance hierarchy depth
 * of source and target types, ensuring more specific converters (those handling more derived types) receive higher priority.
 * It also includes common functionality such as logging support, null safety in conversion, and proper equals and hashcode behavior.</p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class StringToIntegerConverter extends AbstractConverter<String, Integer> {
 *     public boolean accept(Class<?> sourceType, Class<?> targetType) {
 *         return sourceType.isAssignableFrom(String.class) &&
 *                targetType.isAssignableFrom(Integer.class);
 *     }
 *
 *     public Integer doConvert(String source) {
 *         return Integer.parseInt(source);
 *     }
 * }
 * }</pre>
 *
 * <h3>Prioritized Behavior Example</h3>
 * <pre>{@code
 * public class HighPriorityStringToIntegerConverter extends AbstractConverter<String, Integer> {
 *     public boolean accept(Class<?> sourceType, Class<?> targetType) {
 *         return sourceType.isAssignableFrom(String.class) &&
 *                targetType.isAssignableFrom(Integer.class);
 *     }
 *
 *     public Integer doConvert(String source) {
 *         return Integer.parseInt(source);
 *     }
 *
 *     @Override
 *     public int getPriority() {
 *         return Prioritized.MAX_PRIORITY; // Highest priority
 *     }
 * }
 * }</pre>
 *
 * <p>The priority is calculated by combining the number of superclasses and interfaces from both the source and target types.
 * Negative values indicate higher priority. Subclasses can override the priority using the constructor or by overriding the
 * {@link #getPriority()} method.</p>
 *
 * <p>Subclasses must implement the {@link #doConvert(Object)} method to provide the actual conversion logic,
 * while null handling and exception wrapping are already taken care of by this base class.</p>
 *
 * @param <S> The source type
 * @param <T> The target type
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Converter
 * @see Prioritized
 * @since 1.0.0
 */
public abstract class AbstractConverter<S, T> implements Converter<S, T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Nullable
    private Integer priority;

    public AbstractConverter() {
        this.priority = resolvePriority();
    }

    @Override
    @Nullable
    public final T convert(@Nullable S source) {
        if (source == null) {
            return null;
        }
        T target = null;
        try {
            target = doConvert(source);
        } catch (Throwable e) {
            if (logger.isTraceEnabled()) {
                logger.trace("The source[value : {}] can't be converted by the Converter[class : '{}']", source, getTypeName(getClass()));
            }
            throw wrap(e, RuntimeException.class);
        }
        return target;
    }

    /**
     * Converts the non-null source-typed value to the target-typed value.
     * <p>
     * This method is called by {@link #convert(Object)} after checking if the source is null.
     * Subclasses must implement this method to provide the actual conversion logic.
     * </p>
     *
     * @param source the non-null source-typed value
     * @return the converted target-typed value, or {@code null} if conversion is not possible
     * @throws Throwable if an error occurs during conversion
     */
    @Nullable
    protected abstract T doConvert(@Nonnull S source) throws Throwable;

    /**
     * Resolve the priority based on the inheritance hierarchy depth of source and target types.
     * <p>
     * The priority is calculated by combining the number of superclasses and interfaces
     * from both the source and target types. This ensures more specific converters (those
     * handling more derived types) receive higher priority.
     * </p>
     *
     * @return the resolved priority as an {@link Integer}, negative value indicates higher priority
     */
    protected Integer resolvePriority() {
        Class<S> sourceType = getSourceType();
        Class<T> targetType = getTargetType();
        List<?> allTypesFromSource = getAllClasses(sourceType);
        List<?> allTypesFromTarget = getAllClasses(targetType);
        int high = allTypesFromSource.size() << 16;
        int low = allTypesFromTarget.size();
        return -(high | low);
    }

    @Override
    public int getPriority() {
        return priority == null ? Converter.super.getPriority() : priority.intValue();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbstractConverter)) return false;

        AbstractConverter<?, ?> that = (AbstractConverter<?, ?>) o;
        return Objects.equals(getSourceType(), that.getSourceType())
                && Objects.equals(getTargetType(), that.getTargetType())
                && Objects.equals(getPriority(), that.getPriority());
    }

    @Override
    public int hashCode() {
        return hash(getSourceType(), getTargetType(), getPriority());
    }
}
