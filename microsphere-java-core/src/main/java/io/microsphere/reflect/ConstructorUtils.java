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
package io.microsphere.reflect;

import io.microsphere.logging.Logger;
import io.microsphere.util.BaseUtils;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.lang.function.Streams.filterAll;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.MemberUtils.isPrivate;

/**
 * The utilities class of {@link Constructor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class ConstructorUtils extends BaseUtils {

    private static final Logger logger = getLogger(ConstructorUtils.class);

    public static final Constructor NOT_FOUND_CONSTRUCTOR = null;

    /**
     * Is a non-private constructor without parameters
     *
     * @param constructor {@link Constructor}
     * @return <code>true</code> if the given {@link Constructor} is a public no-arg one,
     * otherwise <code>false</code>
     */
    public static boolean isNonPrivateConstructorWithoutParameters(Constructor<?> constructor) {
        return !isPrivate(constructor) && constructor.getParameterCount() < 1;
    }

    public static boolean hasNonPrivateConstructorWithoutParameters(Class<?> type) {
        Constructor<?>[] constructors = type.getDeclaredConstructors();
        boolean has = false;
        for (Constructor<?> constructor : constructors) {
            if (isNonPrivateConstructorWithoutParameters(constructor)) {
                has = true;
                break;
            }
        }
        return has;
    }

    public static List<Constructor<?>> findConstructors(Class<?> type,
                                                        Predicate<? super Constructor<?>>... constructorFilters) {
        List<Constructor<?>> constructors = ofList(type.getConstructors());
        return filterAll(constructors, constructorFilters);
    }

    public static List<Constructor<?>> findDeclaredConstructors(Class<?> type,
                                                                Predicate<? super Constructor<?>>... constructorFilters) {
        List<Constructor<?>> constructors = ofList(type.getDeclaredConstructors());
        return filterAll(constructors, constructorFilters);
    }

    public static <T> Constructor<T> getConstructor(Class<T> type, Class<?>... parameterTypes) {
        return execute(() -> type.getConstructor(parameterTypes));
    }

    public static <T> Constructor<T> getDeclaredConstructor(Class<T> type, Class<?>... parameterTypes) {
        return execute(() -> type.getDeclaredConstructor(parameterTypes));
    }

    public static <T> Constructor<T> findConstructor(Class<T> type, Class<?>... parameterTypes) {
        return execute(() -> type.getDeclaredConstructor(parameterTypes), e -> {
            if (logger.isTraceEnabled()) {
                logger.trace("The declared constructor of '{}' can't be found by parameter types : {}", type, Arrays.toString(parameterTypes));
            }
            return NOT_FOUND_CONSTRUCTOR;
        });
    }

    /**
     * Create an instance by the specified {@link Constructor} and arguments
     *
     * @param constructor {@link Constructor}
     * @param args        the {@link Constructor Constructors} arguments
     * @param <T>         the type of instance
     * @return non-null
     */
    public static <T> T newInstance(Constructor<T> constructor, Object... args) {
        return ExecutableUtils.execute(constructor, () -> constructor.newInstance(args));
    }
}
