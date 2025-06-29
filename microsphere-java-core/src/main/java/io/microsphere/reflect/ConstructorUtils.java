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

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.lang.function.ThrowableSupplier;
import io.microsphere.logging.Logger;
import io.microsphere.util.Utils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.util.List;
import java.util.function.Predicate;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.lang.function.Streams.filterAll;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.AccessibleObjectUtils.trySetAccessible;
import static io.microsphere.reflect.MemberUtils.isPrivate;
import static io.microsphere.util.ArrayUtils.arrayToString;

/**
 * The utilities class of {@link Constructor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class ConstructorUtils implements Utils {

    private static final Logger logger = getLogger(ConstructorUtils.class);

    public static final Constructor NOT_FOUND_CONSTRUCTOR = null;

    /**
     * Checks whether the given constructor is a non-private constructor without parameters.
     *
     * <p>
     * This method verifies that the constructor:
     * <ul>
     *   <li>Is not null</li>
     *   <li>Is not private ({@link MemberUtils#isPrivate(Member)})</li>
     *   <li>Has no parameters (i.e., parameter count is less than 1)</li>
     * </ul>
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Constructor<?> constructor = MyClass.class.getConstructor();
     * boolean isValid = isNonPrivateConstructorWithoutParameters(constructor);
     * }</pre>
     *
     * @param constructor the constructor to check
     * @return {@code true} if the constructor is non-private and has no parameters; {@code false} otherwise
     */
    public static boolean isNonPrivateConstructorWithoutParameters(Constructor<?> constructor) {
        return constructor != null && !isPrivate(constructor) && constructor.getParameterCount() < 1;
    }

    /**
     * Checks whether the specified class has at least one non-private constructor without parameters.
     *
     * <p>
     * This method examines all declared constructors of the given class and returns {@code true}
     * if any of them is non-private and has no parameters.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean result = hasNonPrivateConstructorWithoutParameters(MyClass.class);
     * }</pre>
     *
     * @param type the class to check for a non-private parameterless constructor
     * @return {@code true} if such a constructor exists; otherwise, {@code false}
     */
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

    /**
     * Find public constructors from the specified class that match the given filter conditions.
     *
     * <p>
     * This method retrieves all public constructors of the provided class and filters them based on the
     * specified predicates. It is useful for selecting constructors with specific characteristics,
     * such as visibility, parameter types, or other custom criteria.
     * </p>
     *
     * <h3>Example Usage</h3>
     *
     * <h4>Basic Filtering</h4>
     * <pre>{@code
     * List<Constructor<?>> constructors = findConstructors(MyClass.class);
     * }</pre>
     *
     * <h4>Filter by Non-Private Constructors</h4>
     * <pre>{@code
     * List<Constructor<?>> nonPrivateConstructors = findConstructors(MyClass.class,
     *     constructor -> !MemberUtils.isPrivate(constructor));
     * }</pre>
     *
     * <h4>Filter by Constructor Parameter Count</h4>
     * <pre>{@code
     * List<Constructor<?>> noArgConstructors = findConstructors(MyClass.class,
     *     constructor -> constructor.getParameterCount() == 0);
     * }</pre>
     *
     * @param type               the class to find constructors from
     * @param constructorFilters one or more predicates used to filter constructors
     * @return a list of constructors that match the filter conditions
     */
    @Nonnull
    public static List<Constructor<?>> findConstructors(Class<?> type,
                                                        Predicate<? super Constructor<?>>... constructorFilters) {
        List<Constructor<?>> constructors = ofList(type.getConstructors());
        return filterAll(constructors, constructorFilters);
    }

    /**
     * Find declared constructors from the specified class that match the given filter conditions.
     *
     * <p>
     * This method retrieves all declared constructors (including private, protected, and package-private)
     * of the provided class and filters them based on the specified predicates. It is useful for selecting
     * constructors with specific characteristics such as visibility, parameter types, or other custom criteria.
     * </p>
     *
     * <h3>Example Usage</h3>
     *
     * <h4>Basic Filtering</h4>
     * <pre>{@code
     * List<Constructor<?>> constructors = findDeclaredConstructors(MyClass.class);
     * }</pre>
     *
     * <h4>Filter by Non-Private Constructors</h4>
     * <pre>{@code
     * List<Constructor<?>> nonPrivateConstructors = findDeclaredConstructors(MyClass.class,
     *     constructor -> !MemberUtils.isPrivate(constructor));
     * }</pre>
     *
     * <h4>Filter by Constructor Parameter Count</h4>
     * <pre>{@code
     * List<Constructor<?>> noArgConstructors = findDeclaredConstructors(MyClass.class,
     *     constructor -> constructor.getParameterCount() == 0);
     * }</pre>
     *
     * @param type               the class to find declared constructors from
     * @param constructorFilters one or more predicates used to filter constructors
     * @return a list of declared constructors that match the filter conditions
     */
    @Nonnull
    public static List<Constructor<?>> findDeclaredConstructors(Class<?> type,
                                                                Predicate<? super Constructor<?>>... constructorFilters) {
        List<Constructor<?>> constructors = ofList(type.getDeclaredConstructors());
        return filterAll(constructors, constructorFilters);
    }

    /**
     * Retrieves the public constructor of the specified class that matches the given parameter types.
     *
     * <p>
     * This method attempts to find a public constructor in the provided class that accepts the specified parameter types.
     * It wraps the underlying reflective operation and handles exceptions via the {@link ThrowableSupplier} execution.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Constructor<MyClass> constructor = getConstructor(MyClass.class, String.class, int.class);
     * }</pre>
     *
     * @param type           the class to retrieve the constructor from
     * @param parameterTypes the types of parameters expected by the constructor
     * @param <T>            the type of the object constructed by the retrieved constructor
     * @return the matching public constructor, or throws an exception if not found
     */
    @Nullable
    public static <T> Constructor<T> getConstructor(Class<T> type, Class<?>... parameterTypes) {
        return execute(() -> type.getConstructor(parameterTypes));
    }

    /**
     * Retrieves the declared constructor (including private, protected, and package-private)
     * of the specified class that matches the given parameter types.
     *
     * <p>
     * This method attempts to find a constructor with the specified parameter types in the provided class.
     * It wraps the underlying reflective operation and handles exceptions via the {@link ThrowableSupplier} execution.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Constructor<MyClass> constructor = getDeclaredConstructor(MyClass.class, String.class, int.class);
     * }</pre>
     *
     * @param type           the class to retrieve the declared constructor from
     * @param parameterTypes the types of parameters expected by the constructor
     * @param <T>            the type of the object constructed by the retrieved constructor
     * @return the matching declared constructor, or throws an exception if not found
     */
    @Nullable
    public static <T> Constructor<T> getDeclaredConstructor(Class<T> type, Class<?>... parameterTypes) {
        return execute(() -> type.getDeclaredConstructor(parameterTypes));
    }

    /**
     * Finds a constructor in the specified class that matches the given parameter types.
     *
     * <p>
     * This method attempts to locate a constructor within the provided class that accepts the specified parameter types.
     * If no such constructor is found, it returns {@code null} and logs a trace message indicating the failure.
     * </p>
     *
     * <h3>Example Usage</h3>
     *
     * <h4>Finding a Constructor</h4>
     * <pre>{@code
     * Constructor<MyClass> constructor = findConstructor(MyClass.class, String.class, int.class);
     * if (constructor != null) {
     *     MyClass instance = newInstance(constructor, "example", 42);
     * }
     * }</pre>
     *
     * <h4>Handling Missing Constructors</h4>
     * <pre>{@code
     * Constructor<MyClass> constructor = findConstructor(MyClass.class, double.class, boolean.class);
     * if (constructor == null) {
     *     System.out.println("No matching constructor found.");
     * }
     * }</pre>
     *
     * @param type           the class to search for the constructor
     * @param parameterTypes the types of parameters expected by the constructor
     * @param <T>            the type of the object constructed by the retrieved constructor
     * @return the matching constructor, or {@code null} if none is found
     */
    @Nullable
    public static <T> Constructor<T> findConstructor(Class<T> type, Class<?>... parameterTypes) {
        return execute(() -> type.getDeclaredConstructor(parameterTypes), e -> {
            if (logger.isTraceEnabled()) {
                logger.trace("The declared constructor of '{}' can't be found by parameter types : {}", type, arrayToString(parameterTypes));
            }
            return NOT_FOUND_CONSTRUCTOR;
        });
    }

    /**
     * Creates a new instance by invoking the specified {@link Constructor} with the provided arguments.
     *
     * <p>
     * This method makes the constructor accessible (if it is not already) using
     * {@link AccessibleObjectUtils#trySetAccessible(AccessibleObject)} and then invokes it using
     * the utility method from {@link ExecutableUtils#execute(Executable, ThrowableSupplier)} to handle exceptions uniformly.
     * </p>
     *
     * <h3>Example Usage</h3>
     *
     * <h4>Basic Instantiation</h4>
     * <pre>{@code
     * Constructor<MyClass> constructor = MyClass.class.getConstructor(String.class, int.class);
     * MyClass instance = newInstance(constructor, "hello", 42);
     * }</pre>
     *
     * <h4>Using a Private Constructor</h4>
     * <pre>{@code
     * Constructor<SingletonClass> privateConstructor = SingletonClass.class.getDeclaredConstructor();
     * SingletonClass instance = newInstance(privateConstructor); // Accesses private constructor
     * }</pre>
     *
     * @param constructor the constructor to invoke
     * @param args        the arguments to pass to the constructor
     * @param <T>         the type of the object created by the constructor
     * @return a new instance of the object constructed by the given constructor
     * @throws NullPointerException     if the constructor is {@code null}
     * @throws IllegalStateException    if the constructor cannot be accessed or invoked
     * @throws IllegalArgumentException if the arguments do not match the expected parameter types
     * @throws RuntimeException         if the constructor throws an exception during invocation
     */
    @Nonnull
    public static <T> T newInstance(Constructor<T> constructor, Object... args) {
        trySetAccessible(constructor);
        return ExecutableUtils.execute(constructor, () -> constructor.newInstance(args));
    }

    private ConstructorUtils() {
    }
}
