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
import io.microsphere.logging.Logger;
import io.microsphere.util.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import static io.microsphere.collection.ListUtils.of;
import static io.microsphere.constants.SymbolConstants.COMMA_CHAR;
import static io.microsphere.constants.SymbolConstants.LEFT_PARENTHESIS_CHAR;
import static io.microsphere.constants.SymbolConstants.RIGHT_PARENTHESIS_CHAR;
import static io.microsphere.constants.SymbolConstants.SHARP_CHAR;
import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.lang.function.Streams.filterAll;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.AccessibleObjectUtils.trySetAccessible;
import static io.microsphere.reflect.MemberUtils.isPrivate;
import static io.microsphere.reflect.MemberUtils.isStatic;
import static io.microsphere.reflect.TypeUtils.isObjectClass;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.AnnotationUtils.CALLER_SENSITIVE_ANNOTATION_CLASS;
import static io.microsphere.util.AnnotationUtils.isAnnotationPresent;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
import static io.microsphere.util.ArrayUtils.arrayEquals;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static io.microsphere.util.ClassUtils.getAllInheritedTypes;
import static io.microsphere.util.ClassUtils.getTypeName;
import static io.microsphere.util.ClassUtils.getTypes;
import static io.microsphere.util.ClassUtils.isArray;
import static io.microsphere.util.ClassUtils.isPrimitive;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.hash;

/**
 * The Java Reflection {@link Method} Utility class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class MethodUtils implements Utils {

    private static final Logger logger = getLogger(MethodUtils.class);

    /**
     * The public methods of {@link Object}
     */
    public final static List<Method> OBJECT_PUBLIC_METHODS = of(Object.class.getMethods());

    /**
     * The declared methods of {@link Object}
     */
    public final static List<Method> OBJECT_DECLARED_METHODS = of(Object.class.getDeclaredMethods());

    /**
     * The {@link Predicate} reference to {@link MethodUtils#isObjectMethod(Method)}
     */
    public final static Predicate<? super Method> OBJECT_METHOD_PREDICATE = MethodUtils::isObjectMethod;

    /**
     * The {@link Predicate} reference to {@link MemberUtils#isPublic(Member)}
     */
    public final static Predicate<? super Method> PUBLIC_METHOD_PREDICATE = MemberUtils::isPublic;

    /**
     * The {@link Predicate} reference to {@link MemberUtils#isStatic(Member)}
     */
    public final static Predicate<? super Method> STATIC_METHOD_PREDICATE = MemberUtils::isStatic;

    /**
     * The {@link Predicate} reference to {@link MemberUtils#isNonStatic(Member)}
     */
    public final static Predicate<? super Method> NON_STATIC_METHOD_PREDICATE = MemberUtils::isNonStatic;

    /**
     * The {@link Predicate} reference to {@link MemberUtils#isFinal(Member)}
     */
    public final static Predicate<? super Method> FINAL_METHOD_PREDICATE = MemberUtils::isFinal;

    /**
     * The {@link Predicate} reference to {@link MemberUtils#isNonPrivate(Member)}
     */
    public final static Predicate<? super Method> NON_PRIVATE_METHOD_PREDICATE = MemberUtils::isNonPrivate;

    private final static ConcurrentMap<MethodKey, Method> methodsCache = new ConcurrentHashMap<>(256);

    private static final ConcurrentMap<Class<?>, Method[]> declaredMethodsCache = new ConcurrentHashMap<>(256);

    /**
     * Creates a {@link Predicate} that excludes methods declared by the specified class.
     *
     * <p>This method is useful when filtering methods to exclude those that are declared by a specific class,
     * for example, when searching for overridden methods in subclasses.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Find all methods in MyClass excluding those declared by MySuperClass
     * List<Method> filteredMethods = MethodUtils.findMethods(MyClass.class,
     *     MethodUtils.excludedDeclaredClass(MySuperClass.class));
     * }</pre>
     *
     * @param declaredClass the class whose declared methods should be excluded
     * @return a non-null {@link Predicate} that evaluates to {@code true} for methods not declared by the given class
     */
    @Nonnull
    public static Predicate<? super Method> excludedDeclaredClass(Class<?> declaredClass) {
        return method -> !Objects.equals(declaredClass, method.getDeclaringClass());
    }

    /**
     * Get all declared {@link Method methods} of the target class, excluding the inherited methods.
     *
     * <p>This method retrieves only the methods that are directly declared in the specified class,
     * without including any methods from its superclasses or interfaces.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Get all declared methods in MyClass
     * List<Method> declaredMethods = MethodUtils.getDeclaredMethods(MyClass.class);
     * }</pre>
     *
     * @param targetClass the target class
     * @return non-null read-only {@link List} of declared methods
     * @see #findDeclaredMethods(Class, Predicate...)
     */
    @Nonnull
    public static List<Method> getDeclaredMethods(Class<?> targetClass) {
        return findDeclaredMethods(targetClass, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all public {@link Method methods} of the target class, excluding the inherited methods.
     *
     * <p>This method retrieves only the public methods that are directly declared in the specified class,
     * without including any methods from its superclasses or interfaces.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Get all public methods declared in MyClass
     * List<Method> publicMethods = MethodUtils.getMethods(MyClass.class);
     * }</pre>
     *
     * @param targetClass the target class
     * @return non-null read-only {@link List}
     * @see #findMethods(Class, Predicate...)
     */
    @Nonnull
    public static List<Method> getMethods(Class<?> targetClass) {
        return findMethods(targetClass, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all declared {@link Method methods} of the target class, including the inherited methods.
     *
     * <p>This method retrieves all methods that are declared in the specified class and its superclasses,
     * including those from interfaces implemented by the class and its ancestors.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Get all declared methods in MyClass, including inherited ones
     * List<Method> allDeclaredMethods = MethodUtils.getAllDeclaredMethods(MyClass.class);
     * }</pre>
     *
     * @param targetClass the target class
     * @return non-null read-only {@link List}
     * @see #findAllDeclaredMethods(Class, Predicate...)
     */
    @Nonnull
    public static List<Method> getAllDeclaredMethods(Class<?> targetClass) {
        return findAllDeclaredMethods(targetClass, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all public {@link Method methods} of the target class, including the inherited methods.
     *
     * <p>This method retrieves all public methods that are declared in the specified class and its superclasses,
     * including those from interfaces implemented by the class and its ancestors.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Get all public methods of MyClass, including inherited ones
     * List<Method> allPublicMethods = MethodUtils.getAllMethods(MyClass.class);
     * }</pre>
     *
     * <p><b>Note:</b> If you need only the methods declared directly in the class (excluding inherited ones),
     * consider using {@link #getMethods(Class)} instead.</p>
     *
     * @param targetClass the target class
     * @return non-null read-only {@link List}
     * @see #findAllMethods(Class, Predicate...)
     */
    @Nonnull
    public static List<Method> getAllMethods(Class<?> targetClass) {
        return findAllMethods(targetClass, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Find all declared {@link Method methods} of the target class, excluding the inherited methods.
     *
     * <p>This method retrieves only the methods that are directly declared in the specified class,
     * without including any methods from its superclasses or interfaces.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Get all declared methods in MyClass
     * List<Method> declaredMethods = MethodUtils.findDeclaredMethods(MyClass.class);
     * }</pre>
     *
     * <h4>Filtering Example</h4>
     * <pre>{@code
     * // Get all non-private declared methods in MyClass
     * List<Method> nonPrivateMethods = MethodUtils.findDeclaredMethods(MyClass.class,
     *     MethodUtils::isNonPrivate);
     * }</pre>
     *
     * @param targetClass     the target class
     * @param methodsToFilter (optional) the methods to be filtered
     * @return non-null read-only {@link List}
     * @see #findMethods(Class, boolean, boolean, Predicate[])
     */
    @Nonnull
    public static List<Method> findDeclaredMethods(Class<?> targetClass, Predicate<? super Method>... methodsToFilter) {
        return findMethods(targetClass, false, false, methodsToFilter);
    }

    /**
     * Find all public methods directly declared in the specified class, without including inherited methods.
     *
     * <p>This method retrieves only the public methods that are explicitly declared in the given class,
     * excluding any methods from its superclasses or interfaces.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Get all public methods declared in MyClass
     * List<Method> publicMethods = MethodUtils.findMethods(MyClass.class);
     * }</pre>
     *
     * <h4>Filtering Example</h4>
     *
     * <pre>{@code
     * // Get all non-static public methods declared in MyClass
     * List<Method> nonStaticPublicMethods = MethodUtils.findMethods(MyClass.class,
     *     method -> !MemberUtils.isStatic(method));
     * }</pre>
     *
     * @param targetClass     the target class to inspect
     * @param methodsToFilter optional predicates used to filter the methods further
     * @return a non-null read-only list of public methods declared in the specified class
     */
    @Nonnull
    public static List<Method> findMethods(Class<?> targetClass, Predicate<? super Method>... methodsToFilter) {
        return findMethods(targetClass, false, true, methodsToFilter);
    }

    /**
     * Retrieves all declared methods directly defined in the specified class, excluding inherited methods.
     *
     * <p>This method returns only the methods that are explicitly declared in the given class,
     * and does not include any methods from superclasses or interfaces.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Get all declared methods in MyClass
     * List<Method> declaredMethods = MethodUtils.findAllDeclaredMethods(MyClass.class);
     * }</pre>
     *
     * <h4>Filtering Example</h4>
     *
     * <pre>{@code
     * // Get all non-private declared methods in MyClass
     * List<Method> nonPrivateMethods = MethodUtils.findAllDeclaredMethods(MyClass.class,
     *     method -> !MemberUtils.isPrivate(method));
     * }</pre>
     *
     * @param targetClass     the class to retrieve declared methods from
     * @param methodsToFilter optional predicates to filter the methods
     * @return a non-null read-only list of declared methods in the specified class
     */
    @Nonnull
    public static List<Method> findAllDeclaredMethods(Class<?> targetClass, Predicate<? super Method>... methodsToFilter) {
        return findMethods(targetClass, true, false, methodsToFilter);
    }

    /**
     * Get all public {@link Method methods} of the target class, including the inherited methods.
     *
     * <p>This method retrieves all public methods that are declared in the specified class and its superclasses,
     * including those from interfaces implemented by the class and its ancestors.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Get all public methods of MyClass, including inherited ones
     * List<Method> allPublicMethods = MethodUtils.findAllMethods(MyClass.class);
     * }</pre>
     *
     * <h4>Filtering Example</h4>
     *
     * <pre>{@code
     * // Get all non-static public methods of MyClass, including inherited ones
     * List<Method> nonStaticPublicMethods = MethodUtils.findAllMethods(MyClass.class,
     *     method -> !MemberUtils.isStatic(method));
     * }</pre>
     *
     * @param targetClass     the target class
     * @param methodsToFilter (optional) the methods to be filtered
     * @return non-null read-only {@link List}
     */
    @Nonnull
    public static List<Method> findAllMethods(Class<?> targetClass, Predicate<? super Method>... methodsToFilter) {
        return findMethods(targetClass, true, true, methodsToFilter);
    }

    /**
     * Find all {@link Method methods} of the target class by the specified criteria.
     *
     * <p>This method provides a flexible way to retrieve methods from a class based on whether
     * inherited methods should be included, whether only public methods should be considered,
     * and optional filtering predicates.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <h4>Basic Usage</h4>
     * <pre>{@code
     * // Get all public methods of MyClass including inherited ones
     * List<Method> methods = MethodUtils.findMethods(MyClass.class, true, true);
     * }</pre>
     *
     * <h4>Filtering Example</h4>
     * <pre>{@code
     * // Get all non-static public methods of MyClass including inherited ones
     * List<Method> nonStaticPublicMethods = MethodUtils.findMethods(MyClass.class, true, true,
     *     method -> !MemberUtils.isStatic(method));
     * }</pre>
     *
     * <h4>Advanced Filtering Example</h4>
     * <pre>{@code
     * // Get all non-private, non-static methods of MyClass including inherited ones
     * List<Method> filteredMethods = MethodUtils.findMethods(MyClass.class, true, false,
     *     MethodUtils::isNonPrivate, MemberUtils::isNonStatic);
     * }</pre>
     *
     * @param targetClass           the target class
     * @param includeInheritedTypes if set to true, includes methods from superclasses and interfaces
     * @param publicOnly            if set to true, only public methods are returned
     * @param methodsToFilter       (optional) one or more predicates to further filter the methods
     * @return a non-null read-only list of methods matching the criteria
     */
    @Nonnull
    public static List<Method> findMethods(Class<?> targetClass, boolean includeInheritedTypes, boolean publicOnly,
                                           Predicate<? super Method>... methodsToFilter) {

        if (targetClass == null || isPrimitive(targetClass)) {
            return emptyList();
        }

        if (isArray(targetClass)) {
            return doFilterMethods(OBJECT_PUBLIC_METHODS, methodsToFilter);
        }

        if (isObjectClass(targetClass)) {
            return publicOnly ? doFilterMethods(OBJECT_PUBLIC_METHODS, methodsToFilter) : doFilterMethods(OBJECT_DECLARED_METHODS, methodsToFilter);
        }

        Predicate predicate = and(methodsToFilter);
        if (publicOnly) {
            predicate = PUBLIC_METHOD_PREDICATE.and(predicate);
        }

        // All methods
        List<Method> allMethods = new LinkedList<>();

        if (includeInheritedTypes) {
            while (targetClass != null) {
                filterDeclaredMethodsHierarchically(targetClass, predicate, allMethods);
                targetClass = targetClass.getSuperclass();
            }
        } else {
            filterDeclaredMethods(targetClass, predicate, allMethods);
        }

        return unmodifiableList(allMethods);
    }

    /**
     * Find the {@link Method} by the specified type (including inherited types) and method name without the
     * parameter type.
     *
     * <p>This method searches for a method with the given name in the specified class and its superclasses,
     * returning the first match found. If no method is found, this method returns {@code null}.
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Find a method named "toString" in the MyClass class
     * Method method = MethodUtils.findMethod(MyClass.class, "toString");
     * if (method != null) {
     *     System.out.println("Method found: " + method);
     * } else {
     *     System.out.println("Method not found.");
     * }
     * }</pre>
     *
     * @param targetClass the target type to search for the method
     * @param methodName  the name of the method to find
     * @return the found method, or {@code null} if no matching method is found
     */
    @Nullable
    public static Method findMethod(Class targetClass, String methodName) {
        return findMethod(targetClass, methodName, EMPTY_CLASS_ARRAY);
    }

    /**
     * Find the {@link Method} by the specified type (including inherited types), method name, and parameter types.
     *
     * <p>This method searches for a method with the given name and parameter types in the specified class and its superclasses,
     * returning the first match found. The search is cached to improve performance on repeated calls.
     * If no matching method is found, this method returns {@code null}.
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Find a method named "toString" with no parameters in MyClass
     * Method method = MethodUtils.findMethod(MyClass.class, "toString");
     * if (method != null) {
     *     System.out.println("Method found: " + method);
     * } else {
     *     System.out.println("Method not found.");
     * }
     * }</pre>
     *
     * <pre>{@code
     * // Find a method named "setValue" that takes a String parameter
     * Method method = MethodUtils.findMethod(MyClass.class, "setValue", String.class);
     * if (method != null) {
     *     System.out.println("Method found: " + method);
     * }
     * }</pre>
     *
     * @param targetClass    the target class to search for the method
     * @param methodName     the name of the method to find
     * @param parameterTypes the parameter types of the method (optional, defaults to empty array)
     * @return the found method, or {@code null} if no matching method is found
     */
    @Nullable
    public static Method findMethod(Class targetClass, String methodName, Class<?>... parameterTypes) {
        MethodKey key = buildKey(targetClass, methodName, parameterTypes);
        return methodsCache.computeIfAbsent(key, MethodUtils::doFindMethod);
    }

    /**
     * Finds a declared method in the specified class, including its superclasses and interfaces.
     *
     * <p>This method searches for a method with the given name and parameter types in the specified class,
     * its superclasses (if the class is not an interface), and its implemented interfaces. It returns the
     * first matching method found.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Find a method named "exampleMethod" with no parameters
     * Method method1 = MethodUtils.findDeclaredMethod(MyClass.class, "exampleMethod");
     * }</pre>
     *
     * <pre>{@code
     * // Find a method named "exampleMethod" that takes a String and an int
     * Method method2 = MethodUtils.findDeclaredMethod(MyClass.class, "exampleMethod", String.class, int.class);
     * }</pre>
     *
     * @param targetClass    the class to search for the declared method
     * @param methodName     the name of the method to find
     * @param parameterTypes the parameter types of the method (optional, defaults to empty array)
     * @return the found method, or {@code null} if no matching method is found
     */
    @Nullable
    public static Method findDeclaredMethod(Class<?> targetClass, String methodName, Class<?>... parameterTypes) {

        if (targetClass == null) {
            return null;
        }

        // First, try to find the declared method in directly target class
        Method method = doFindDeclaredMethod(targetClass, methodName, parameterTypes);

        if (method == null) {  // Second, to find the declared method in the super class
            Class<?> superClass = targetClass.isInterface() ? Object.class : targetClass.getSuperclass();
            method = findDeclaredMethod(superClass, methodName, parameterTypes);
        }

        if (method == null) { // Third, to find the declared method in the interfaces
            for (Class<?> interfaceClass : targetClass.getInterfaces()) {
                method = findDeclaredMethod(interfaceClass, methodName, parameterTypes);
                if (method != null) {
                    break;
                }
            }
        }

        if (method == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("The declared method was not found in the target class[name : '{}'] by name['{}'] and parameter types['{}']",
                        targetClass, methodName, arrayToString(parameterTypes));
            }
        }

        return method;
    }

    /**
     * Invokes a method with the specified name on the given object, using the provided arguments.
     *
     * <p>This method dynamically retrieves the class of the target object and searches for the appropriate method
     * to invoke based on the method name and argument types. It supports both instance and static methods.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Example class with an instance method
     * public class MyClass {
     *     public String greet(String name) {
     *         return "Hello, " + name;
     *     }
     * }
     *
     * // Create an instance of MyClass
     * MyClass myInstance = new MyClass();
     *
     * // Call the 'greet' method using invokeMethod
     * String result = MethodUtils.invokeMethod(myInstance, "greet", "World");
     * System.out.println(result);  // Output: Hello, World
     * }</pre>
     *
     * <p><b>Note:</b> This method internally uses reflection to find and invoke the matching method,
     * which may throw exceptions if the method cannot be found or invoked properly.</p>
     *
     * @param object     The object on which the method will be invoked. Must not be null.
     * @param methodName The name of the method to invoke. Must not be null or empty.
     * @param arguments  The arguments to pass to the method. Can be null or empty if the method requires no parameters.
     * @param <R>        The expected return type of the method.
     * @return The result of invoking the method, wrapped in the appropriate type.
     * @throws NullPointerException     If the provided object is null.
     * @throws IllegalStateException    If the method cannot be found or accessed.
     * @throws IllegalArgumentException If the arguments do not match the method's parameter types.
     * @throws RuntimeException         If the underlying method throws an exception during invocation.
     */
    @Nullable
    public static <R> R invokeMethod(Object object, String methodName, Object... arguments) {
        Class type = object.getClass();
        return invokeMethod(object, type, methodName, arguments);
    }

    /**
     * Invokes a static method of the specified target class with the given method name and arguments.
     *
     * <p>This utility method simplifies the process of invoking a static method using reflection by internally
     * calling {@link #invokeMethod(Object, Class, String, Object...)} with a null instance to indicate that
     * the method is static.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * public class ExampleClass {
     *     public static int add(int a, int b) {
     *         return a + b;
     *     }
     * }
     *
     * // Invoke the static method "add"
     * Integer result = MethodUtils.invokeStaticMethod(ExampleClass.class, "add", 2, 3);
     * System.out.println(result);  // Output: 5
     * }</pre>
     *
     * @param targetClass the class containing the static method
     * @param methodName  the name of the static method to invoke
     * @param arguments   the arguments to pass to the method (can be null or empty)
     * @param <R>         the expected return type of the method
     * @return the result of the method invocation, wrapped in the appropriate type
     * @throws NullPointerException     if the provided target class or method name is null
     * @throws IllegalStateException    if the method cannot be found or accessed
     * @throws IllegalArgumentException if the arguments do not match the method's parameter types
     * @throws RuntimeException         if the underlying method throws an exception during invocation
     */
    @Nullable
    public static <R> R invokeStaticMethod(Class<?> targetClass, String methodName, Object... arguments) {
        return invokeMethod(null, targetClass, methodName, arguments);
    }

    /**
     * Invokes the specified static method represented by the given {@link Method} object.
     *
     * <p>This method is specifically designed to invoke static methods. If the provided method is not static,
     * it may result in an exception during invocation.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * public class ExampleClass {
     *     public static int multiply(int a, int b) {
     *         return a * b;
     *     }
     * }
     *
     * // Retrieve the method using reflection
     * Method method = ExampleClass.class.getMethod("multiply", int.class, int.class);
     *
     * // Invoke the static method
     * Integer result = MethodUtils.invokeStaticMethod(method, 5, 3);
     * System.out.println(result);  // Output: 15
     * }</pre>
     *
     * @param method    the {@link Method} object representing the static method to be invoked
     * @param arguments the arguments to pass to the method (can be null or empty)
     * @param <R>       the expected return type of the method
     * @return the result of the method invocation, wrapped in the appropriate type
     * @throws NullPointerException     if the provided method is null
     * @throws IllegalStateException    if the method cannot be accessed or throws an exception during invocation
     * @throws IllegalArgumentException if the arguments do not match the method's parameter types
     */
    @Nullable
    public static <R> R invokeStaticMethod(Method method, Object... arguments) {
        return invokeMethod(null, method, arguments);
    }

    /**
     * Invokes a method with the specified name on the given class type, using the provided arguments.
     *
     * <p>This method dynamically searches for a method in the specified class that matches the method name
     * and argument types, and then invokes it. It supports both instance and static methods.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * public class ExampleClass {
     *     public String greet(String name) {
     *         return "Hello, " + name;
     *     }
     * }
     *
     * // Create an instance of ExampleClass
     * ExampleClass exampleInstance = new ExampleClass();
     *
     * // Call the 'greet' method using invokeMethod
     * String result = MethodUtils.invokeMethod(exampleInstance, ExampleClass.class, "greet", "World");
     * System.out.println(result);  // Output: Hello, World
     * }</pre>
     *
     * <p><b>Note:</b> This method internally uses reflection to find and invoke the matching method,
     * which may throw exceptions if the method cannot be found or invoked properly.</p>
     *
     * @param instance   The object on which the method will be invoked. Can be null for static methods.
     * @param type       The class type to search for the method. Must not be null.
     * @param methodName The name of the method to invoke. Must not be null or empty.
     * @param arguments  The arguments to pass to the method. Can be null or empty if the method requires no parameters.
     * @param <R>        The expected return type of the method.
     * @return The result of invoking the method, wrapped in the appropriate type.
     * @throws NullPointerException     If the provided type or method name is null.
     * @throws IllegalStateException    If the method cannot be found or accessed.
     * @throws IllegalArgumentException If the arguments do not match the method's parameter types.
     * @throws RuntimeException         If the underlying method throws an exception during invocation.
     */
    @Nullable
    public static <R> R invokeMethod(Object instance, Class<?> type, String methodName, Object... arguments) {
        Class[] parameterTypes = getTypes(arguments);
        Method method = findMethod(type, methodName, parameterTypes);

        if (method == null) {
            throw new IllegalStateException(format("cannot find method[name : '{}'], class: '{}'", methodName, type.getName()));
        }

        return invokeMethod(instance, method, arguments);
    }

    /**
     * Invokes the underlying method represented by this {@code Method}
     * object, on the specified object with the specified parameters.
     * Individual parameters are automatically unwrapped to match
     * primitive formal parameters, and both primitive and reference
     * parameters are subject to method invocation conversions as
     * necessary.
     *
     * <p>If the underlying method is static, then the specified {@code instance}
     * argument is ignored. It may be null.
     *
     * <p>If the number of formal parameters required by the underlying method is
     * 0, the supplied {@code args} array may be of length 0 or null.
     *
     * <p>If the underlying method is an instance method, it is invoked
     * using dynamic method lookup as documented in The Java Language
     * Specification, section {@jls 15.12.4.4}; in particular,
     * overriding based on the runtime type of the target object may occur.
     *
     * <p>If the underlying method is static, the class that declared
     * the method is initialized if it has not already been initialized.
     *
     * <p>If the method completes normally, the value it returns is
     * returned to the caller of invoke; if the value has a primitive
     * type, it is first appropriately wrapped in an object. However,
     * if the value has the type of an array of a primitive type, the
     * elements of the array are <i>not</i> wrapped in objects; in
     * other words, an array of primitive type is returned.  If the
     * underlying method return type is void, the invocation returns
     * null.
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * public class ExampleClass {
     *     public String greet(String name) {
     *         return "Hello, " + name;
     *     }
     *
     *     public static int add(int a, int b) {
     *         return a + b;
     *     }
     * }
     *
     * // Instance method example
     * ExampleClass instance = new ExampleClass();
     * String result = MethodUtils.invokeMethod(instance, ExampleClass.class.getMethod("greet", String.class), "World");
     * System.out.println(result); // Output: Hello, World
     *
     * // Static method example
     * Integer sum = MethodUtils.invokeMethod(null, ExampleClass.class.getMethod("add", int.class, int.class), 2, 3);
     * System.out.println(sum); // Output: 5
     * }</pre>
     *
     * @param instance  the object the underlying method is invoked from
     * @param method    the underlying method
     * @param arguments the arguments used for the method call
     * @param <R>
     * @return the result of dispatching the method represented by
     * this object on {@code instance} with parameters
     * {@code arguments}
     * @throws NullPointerException     if this {@link Method} object is <code>null</code>
     * @throws IllegalStateException    if this {@code Method} object
     *                                  is enforcing Java language access control and the underlying
     *                                  method is inaccessible.
     * @throws IllegalArgumentException if the method is an
     *                                  instance method and the specified object argument
     *                                  is not an instance of the class or interface
     *                                  declaring the underlying method (or of a subclass
     *                                  or implementor thereof); if the number of actual
     *                                  and formal parameters differ; if an unwrapping
     *                                  conversion for primitive arguments fails; or if,
     *                                  after possible unwrapping, a parameter value
     *                                  cannot be converted to the corresponding formal
     *                                  parameter type by a method invocation conversion.
     * @throws RuntimeException         if the underlying method
     *                                  throws an exception.
     */
    @Nullable
    public static <R> R invokeMethod(@Nullable Object instance, Method method, Object... arguments) {
        if (method == null) {
            throw new NullPointerException("The 'method' must not be null");
        }
        R result = null;
        boolean accessible = false;
        RuntimeException failure = null;
        try {
            accessible = trySetAccessible(method);
            result = (R) method.invoke(instance, arguments);
        } catch (IllegalAccessException e) {
            String errorMessage = format("The method[signature : '{}' , instance : {}] can't be accessed[accessible : {}]", getSignature(method), instance, accessible);
            failure = new IllegalStateException(errorMessage, e);
        } catch (IllegalArgumentException e) {
            String errorMessage = format("The arguments can't match the method[signature : '{}' , instance : {}] : {}", getSignature(method), instance, arrayToString(arguments));
            failure = new IllegalArgumentException(errorMessage, e);
        } catch (InvocationTargetException e) {
            String errorMessage = format("It's failed to invoke the method[signature : '{}' , instance : {} , arguments : {}]", getSignature(method), instance, arrayToString(arguments));
            failure = new RuntimeException(errorMessage, e.getTargetException());
        }

        if (failure != null) {
            logger.error(failure.getMessage(), failure.getCause());
            throw failure;
        }

        return result;
    }

    /**
     * Tests whether one method, as a member of a given type,
     * overrides another method.
     *
     * <p>This method checks if the first method ({@code overrider}) overrides the second method ({@code overridden}).
     * For a method to override another method, it must meet several conditions based on the Java Language Specification,
     * such as being declared in a subclass of the declaring class of the overridden method, having the same name and signature,
     * not being private or static, and having a return type that is a subtype of the overridden method's return type.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * class Parent {
     *     public void sayHello() {
     *         System.out.println("Hello from Parent");
     *     }
     * }
     *
     * class Child extends Parent {
     *     @Override
     *     public void sayHello() {
     *         System.out.println("Hello from Child");
     *     }
     * }
     *
     * Method parentMethod = Parent.class.getMethod("sayHello");
     * Method childMethod = Child.class.getMethod("sayHello");
     *
     * boolean result = MethodUtils.overrides(childMethod, parentMethod);
     * System.out.println(result);  // Output: true
     * }</pre>
     *
     * <p><b>Note:</b> This utility method is useful when implementing frameworks or tools that need to determine
     * inheritance relationships between methods at runtime using reflection.</p>
     *
     * @param overrider  the method that may override the other method
     * @param overridden the method that may be overridden
     * @return {@code true} if and only if the first method overrides the second
     * @jls 8.4.8 Inheritance, Overriding, and Hiding
     * @jls 9.4.1 Inheritance and Overriding
     */
    public static boolean overrides(Method overrider, Method overridden) {

        if (overrider == null || overridden == null || overrider == overridden) {
            return false;
        }

        // Method comparison: The method name must be equal
        if (!Objects.equals(overrider.getName(), overridden.getName())) {
            return false;
        }

        // Modifiers comparison: Any method must be non-static method
        if (isStatic(overrider) || isStatic(overridden)) { //
            return false;
        }

        // Modifiers comparison: the accessibility of any method must not be private
        if (isPrivate(overrider) || isPrivate(overridden)) {
            return false;
        }

        // Method comparison: must not be "default" method
        if (overrider.isDefault()) {
            return false;
        }

        Class<?> overriderDeclaringClass = overrider.getDeclaringClass();
        Class<?> overriddenDeclaringClass = overridden.getDeclaringClass();

        // Method comparison: The declaring class of overrider must not equal the overridden's
        if (overriderDeclaringClass == overriddenDeclaringClass) {
            return false;
        }

        // Inheritance comparison: the target class of overrider must be inherit from the overridden's
        if (!overriddenDeclaringClass.isAssignableFrom(overriderDeclaringClass)) {
            return false;
        }

        // Method comparison: The count of method parameters must be equal
        int parameterCount = overrider.getParameterCount();
        if (parameterCount != overridden.getParameterCount()) {
            return false;
        }

        Class<?>[] overriderParameterTypes = overrider.getParameterTypes();
        Class<?>[] overriddenParameterTypes = overridden.getParameterTypes();

        // Method comparison: Any parameter type of overrider must equal the overridden's
        if (!matchesParameterTypes(overriderParameterTypes, overriddenParameterTypes, parameterCount)) {
            return false;
        }

        // Method comparison: The return type of overrider must be inherit from the overridden's.
        // Actually, the different return types of overrider and overridden are not allowed by compiler after above tests.
        return overridden.getReturnType().isAssignableFrom(overrider.getReturnType());

        // Throwable comparison: "throws" Throwable list will be ignored, trust the compiler verify
    }

    /**
     * Finds the nearest overridden method in the class hierarchy for the given overriding method.
     *
     * <p>This method searches through the inheritance chain of the class that declares the
     * provided overriding method to locate the first method it overrides. The search includes
     * both superclasses and interfaces.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * public class Parent {
     *     public void exampleMethod() {}
     * }
     *
     * public class Child extends Parent {
     *     @Override
     *     public void exampleMethod() {}
     * }
     *
     * Method overrider = Child.class.getMethod("exampleMethod");
     * Method overridden = MethodUtils.findNearestOverriddenMethod(overrider);
     *
     * if (overridden != null) {
     *     System.out.println("Found overridden method: " + overridden.getDeclaringClass().getName());
     * } else {
     *     System.out.println("No overridden method found.");
     * }
     * }</pre>
     *
     * @param overrider the method that potentially overrides another method
     * @return the overridden method if found; otherwise, {@code null}
     */
    @Nullable
    public static Method findNearestOverriddenMethod(Method overrider) {
        Class<?> targetClass = overrider.getDeclaringClass();
        Method overriddenMethod = null;
        for (Class<?> inheritedType : getAllInheritedTypes(targetClass)) {
            overriddenMethod = findOverriddenMethod(overrider, inheritedType);
            if (overriddenMethod != null) {
                break;
            }
        }
        return overriddenMethod;
    }

    /**
     * Finds the method in the specified target class that is overridden by the given overriding method.
     *
     * <p>This method searches for a method in the provided {@code targetClass} with the same name and signature
     * as the {@code overrider} method, and checks whether it is actually overridden by the provided method.
     * If a matching overridden method is found, it is returned; otherwise, this method returns {@code null}.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * public class Parent {
     *     public void exampleMethod(String arg) {
     *         System.out.println("Parent method");
     *     }
     * }
     *
     * public class Child extends Parent {
     *     @Override
     *     public void exampleMethod(String arg) {
     *         System.out.println("Child method");
     *     }
     * }
     *
     * Method overrider = Child.class.getMethod("exampleMethod", String.class);
     * Method overridden = MethodUtils.findOverriddenMethod(overrider, Parent.class);
     *
     * if (overridden != null) {
     *     System.out.println("Found overridden method: " + overridden.getName());
     * } else {
     *     System.out.println("No overridden method found.");
     * }
     * }</pre>
     *
     * <p><b>Note:</b> This utility method is useful when working with reflection to identify the original method
     * being overridden in a superclass or interface.</p>
     *
     * @param overrider   the method that potentially overrides another method
     * @param targetClass the class where the overridden method might be declared
     * @return the overridden method declared in the target class, or {@code null} if none is found
     */
    @Nullable
    public static Method findOverriddenMethod(Method overrider, Class<?> targetClass) {
        List<Method> matchedMethods = findDeclaredMethods(targetClass, method -> overrides(overrider, method));
        return matchedMethods.isEmpty() ? null : matchedMethods.get(0);
    }

    /**
     * Generates a string representation of the method signature.
     *
     * <p>The signature includes the fully qualified name of the declaring class,
     * the method name, and the parameter types in parentheses.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * Method method = String.class.getMethod("substring", int.class, int.class);
     * String signature = MethodUtils.getSignature(method);
     * System.out.println(signature);  // Output: java.lang.String#substring(int,int)
     * }</pre>
     *
     * @param method The method for which to generate the signature.
     * @return A non-null string representing the method signature.
     */
    @Nonnull
    public static String getSignature(Method method) {
        return buildSignature(method.getDeclaringClass(), method.getName(), method.getParameterTypes());
    }

    static String buildSignature(Class<?> declaringClass, String methodName, Class<?>[] parameterTypes) {
        int parameterCount = parameterTypes.length;
        String[] parameterTypeNames = new String[parameterCount];
        String declaringClassName = getTypeName(declaringClass);
        int size = declaringClassName.length() + 1 // '#'
                + methodName.length() + 1  // '('
                + (parameterCount == 0 ? 0 : parameterCount - 1) // (parameterCount - 1) * ','
                + 1  // ')'
                ;

        for (int i = 0; i < parameterCount; i++) {
            Class<?> parameterType = parameterTypes[i];
            String parameterTypeName = getTypeName(parameterType);
            parameterTypeNames[i] = parameterTypeName;
            size += parameterTypeName.length();
        }

        StringBuilder signatureBuilder = new StringBuilder(size);

        signatureBuilder.append(declaringClassName).append(SHARP_CHAR).append(methodName).append(LEFT_PARENTHESIS_CHAR);

        for (int i = 0; i < parameterCount; i++) {
            String parameterTypeName = parameterTypeNames[i];
            signatureBuilder.append(parameterTypeName);
            if (i < parameterCount - 1) {
                signatureBuilder.append(COMMA_CHAR);
            }
            parameterTypeNames[i] = null;
        }

        signatureBuilder.append(RIGHT_PARENTHESIS_CHAR);

        return signatureBuilder.toString();
    }

    /**
     * Checks whether the given method is declared by the {@link Object} class.
     *
     * <p>This utility method helps determine if a method belongs directly to the root class {@code Object},
     * which is useful when filtering out methods that are common to all Java objects and not specific to a subclass.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * Method toStringMethod = String.class.getMethod("toString");
     * boolean isObjectMethod = MethodUtils.isObjectMethod(toStringMethod);
     * System.out.println(isObjectMethod); // Output: true
     * }</pre>
     *
     * <pre>{@code
     * Method customMethod = MyClass.class.getMethod("customMethod");
     * boolean isObjectMethod = MethodUtils.isObjectMethod(customMethod);
     * System.out.println(isObjectMethod); // Output: false (assuming customMethod is defined in MyClass)
     * }</pre>
     *
     * @param method the method to check, may be null
     * @return true if the method is declared by the {@link Object} class; false otherwise or if the method is null
     */
    public static boolean isObjectMethod(Method method) {
        if (method != null) {
            return isObjectClass(method.getDeclaringClass());
        }
        return false;
    }

    /**
     * Checks if the specified method is annotated with {@link jdk.internal.reflect.CallerSensitive}.
     *
     * <p>The {@code CallerSensitive} annotation indicates that the method's behavior may be influenced by the caller's context.
     * This is typically used in internal Java APIs to restrict or alter behavior based on the calling class.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * Method defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
     * boolean isCallerSensitive = MethodUtils.isCallerSensitiveMethod(defineClassMethod);
     * System.out.println("Is defineClass method caller-sensitive? " + isCallerSensitive);  // Likely output: true
     * }</pre>
     *
     * @param method the method to check, may be null
     * @return <code>true</code> if the method is non-null and annotated with {@link jdk.internal.reflect.CallerSensitive}; false otherwise
     * @see jdk.internal.reflect.CallerSensitive
     */
    public static boolean isCallerSensitiveMethod(Method method) {
        return isAnnotationPresent(method, CALLER_SENSITIVE_ANNOTATION_CLASS);
    }

    static void filterDeclaredMethodsHierarchically(Class<?> targetClass, Predicate<? super Method> methodToFilter, List<Method> methodsToCollect) {
        filterDeclaredMethods(targetClass, methodToFilter, methodsToCollect);
        for (Class<?> interfaceClass : targetClass.getInterfaces()) {
            filterDeclaredMethodsHierarchically(interfaceClass, methodToFilter, methodsToCollect);
        }
    }

    static void filterDeclaredMethods(Class<?> targetClass, Predicate<? super Method> methodToFilter, List<Method> methodsToCollect) {
        for (Method method : doGetDeclaredMethods(targetClass)) {
            if (methodToFilter.test(method)) {
                methodsToCollect.add(method);
            }
        }
    }

    static Method doFindDeclaredMethod(Class<?> klass, String methodName, Class<?>[] parameterTypes) {
        Method[] declaredMethods = doGetDeclaredMethods(klass);
        return doFindMethod(declaredMethods, methodName, parameterTypes);
    }

    static Method doFindMethod(Method[] methods, String methodName, Class<?>[] parameterTypes) {
        Method targetMethod = null;
        for (Method method : methods) {
            if (matches(method, methodName, parameterTypes)) {
                targetMethod = method;
                break;
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("The target method[name : '{}' , parameter types : {}] {} found in the methods : {}",
                    methodName, arrayToString(parameterTypes), targetMethod == null ? "can't be" : "is",
                    arrayToString(methods));
        }
        return targetMethod;
    }

    static boolean matches(Method method, String methodName, Class<?>[] parameterTypes) {
        int parameterCount = parameterTypes.length;
        return parameterCount == method.getParameterCount()
                && Objects.equals(method.getName(), methodName)
                && matchesParameterTypes(method.getParameterTypes(), parameterTypes, parameterCount);
    }

    static boolean matchesParameterTypes(Class<?>[] oneParameterTypes, Class<?>[] anotherParameterTypes, int parameterCount) {
        for (int i = 0; i < parameterCount; i++) {
            if (oneParameterTypes[i] != anotherParameterTypes[i]) {
                return false;
            }
        }
        return true;
    }

    static Method[] doGetDeclaredMethods(Class<?> klass) {
        return declaredMethodsCache.computeIfAbsent(klass, c -> c.getDeclaredMethods());
    }

    static List<Method> doFilterMethods(List<Method> methods, Predicate<? super Method>... methodsToFilter) {
        return unmodifiableList(filterAll(methods, methodsToFilter));
    }

    static MethodKey buildKey(Class<?> declaredClass, String methodName, Class<?>... parameterTypes) {
        return new MethodKey(declaredClass, methodName, parameterTypes);
    }

    static Method doFindMethod(MethodKey key) {
        Class<?> declaredClass = key.declaredClass;
        String methodName = key.methodName;
        Class<?>[] parameterTypes = key.parameterTypes;
        return findDeclaredMethod(declaredClass, methodName, parameterTypes);
    }

    static class MethodKey {

        final Class<?> declaredClass;

        final String methodName;

        final Class<?>[] parameterTypes;

        MethodKey(Class<?> declaredClass, String methodName, Class<?>[] parameterTypes) {
            this.declaredClass = declaredClass;
            this.methodName = methodName;
            this.parameterTypes = parameterTypes == null ? EMPTY_CLASS_ARRAY : parameterTypes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MethodKey methodKey = (MethodKey) o;

            if (!Objects.equals(declaredClass, methodKey.declaredClass)) return false;
            if (!Objects.equals(methodName, methodKey.methodName)) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return arrayEquals(parameterTypes, methodKey.parameterTypes);
        }

        @Override
        public int hashCode() {
            int result = declaredClass != null ? declaredClass.hashCode() : 0;
            result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
            result = 31 * result + hash(parameterTypes);
            return result;
        }

        @Override
        public String toString() {
            return buildSignature(declaredClass, methodName, parameterTypes);
        }
    }

    private MethodUtils() {
    }
}
