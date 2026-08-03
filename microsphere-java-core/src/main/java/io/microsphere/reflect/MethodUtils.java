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

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.annotation.Immutable;
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
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import static io.microsphere.annotation.ConfigurationProperty.SYSTEM_PROPERTIES_SOURCE;
import static io.microsphere.collection.ListUtils.first;
import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.ListUtils.of;
import static io.microsphere.collection.MapUtils.newConcurrentHashMap;
import static io.microsphere.constants.PropertyConstants.MICROSPHERE_PROPERTY_NAME_PREFIX;
import static io.microsphere.constants.SymbolConstants.COMMA_CHAR;
import static io.microsphere.constants.SymbolConstants.LEFT_PARENTHESIS;
import static io.microsphere.constants.SymbolConstants.LEFT_PARENTHESIS_CHAR;
import static io.microsphere.constants.SymbolConstants.RIGHT_PARENTHESIS;
import static io.microsphere.constants.SymbolConstants.RIGHT_PARENTHESIS_CHAR;
import static io.microsphere.constants.SymbolConstants.SHARP;
import static io.microsphere.constants.SymbolConstants.SHARP_CHAR;
import static io.microsphere.constants.SymbolConstants.VERTICAL_BAR_CHAR;
import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.lang.function.Streams.filterAll;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.AccessibleObjectUtils.trySetAccessible;
import static io.microsphere.reflect.ExecutableUtils.matchParameterTypes;
import static io.microsphere.reflect.MemberUtils.isInvalidDeclaringClass;
import static io.microsphere.reflect.MemberUtils.isPrivate;
import static io.microsphere.reflect.MemberUtils.isStatic;
import static io.microsphere.reflect.Modifier.BRIDGE;
import static io.microsphere.reflect.Modifier.STATIC;
import static io.microsphere.reflect.Modifier.SYNTHETIC;
import static io.microsphere.reflect.Modifier.matchesAny;
import static io.microsphere.reflect.TypeUtils.isObjectClass;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.AnnotationUtils.CALLER_SENSITIVE_ANNOTATION_CLASS;
import static io.microsphere.util.AnnotationUtils.isAnnotationPresent;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
import static io.microsphere.util.ArrayUtils.arrayEquals;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.ClassUtils.getAllInheritedTypes;
import static io.microsphere.util.ClassUtils.getTypeName;
import static io.microsphere.util.ClassUtils.isArray;
import static io.microsphere.util.ClassUtils.isInterface;
import static io.microsphere.util.ExceptionUtils.wrap;
import static io.microsphere.util.StringUtils.split;
import static io.microsphere.util.StringUtils.startsWith;
import static io.microsphere.util.StringUtils.substringBefore;
import static io.microsphere.util.StringUtils.substringBetween;
import static io.microsphere.util.SystemUtils.getSystemProperty;
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
     * The prefix of the "GETTER" method name : "get"
     */
    public static final String GET_METHOD_NAME_PREFIX = "get";

    /**
     * The prefix of the "SETTER" method name : "set"
     */
    public static final String SET_METHOD_NAME_PREFIX = "set";

    /**
     * The prefix of the "IS" method name : "is"
     */
    public static final String IS_METHOD_NAME_PREFIX = "is";

    /**
     * The property name of banned methods : "microsphere.reflect.banned-methods"
     * <h3>Example Usage</h3>
     * <pre>{@code
     * System.setProperty(BANNED_METHODS_PROPERTY_NAME, "java.lang.String#substring() | java.lang.String#substring(int,int)")
     * Method method = MethodUtils.findMethod(String.class, "substring", int.class, int.class); // returns null
     * method = MethodUtils.findMethod(String.class, "substring"); // returns null
     * method = MethodUtils.findMethod(String.class, "substring", int.class); // returns non-null
     * }</pre>
     */
    @ConfigurationProperty(
            type = String[].class,
            source = SYSTEM_PROPERTIES_SOURCE
    )
    public static final String BANNED_METHODS_PROPERTY_NAME = MICROSPHERE_PROPERTY_NAME_PREFIX + "reflect.banned-methods";

    /**
     * The public methods of {@link Object}
     */
    @Nonnull
    @Immutable
    public final static List<Method> OBJECT_PUBLIC_METHODS = of(Object.class.getMethods());

    /**
     * The declared methods of {@link Object}
     */
    @Nonnull
    @Immutable
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

    private final static ConcurrentMap<MethodKey, Method> methodsCache = newConcurrentHashMap(256);

    /**
     * The cache to store the methods to be banned by the {@link #buildSignature(Class, String, Class[]) signatures}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * System.setProperty(BANNED_METHODS_PROPERTY_NAME, "java.lang.String#substring() | java.lang.String#substring(int,int)")
     * Method method = MethodUtils.findMethod(String.class, "substring", int.class, int.class); // returns null
     * method = MethodUtils.findMethod(String.class, "substring"); // returns null
     * method = MethodUtils.findMethod(String.class, "substring", int.class); // returns non-null
     * }</pre>
     */
    private final static ConcurrentMap<MethodKey, Method> bannedMethodsCache = newConcurrentHashMap(16);

    private static final ConcurrentMap<Class<?>, Method[]> declaredMethodsCache = newConcurrentHashMap(256);

    static {
        initBannedMethods();
    }

    /**
     * Initializes the banned methods cache based on the system property {@value #BANNED_METHODS_PROPERTY_NAME}.
     * <p>
     * This method reads the comma-separated list of method signatures from the system property
     * {@value #BANNED_METHODS_PROPERTY_NAME}, parses each signature, resolves the corresponding {@link Method},
     * and stores it in the {@link #bannedMethodsCache}. The format for each method signature is:
     * {@code fully.qualified.ClassName#methodName(parameter.Type1,parameter.Type2)}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Set the system property to ban specific methods
     * System.setProperty(MethodUtils.BANNED_METHODS_PROPERTY_NAME,
     *     "java.lang.String#substring(int,int)|java.lang.Object#toString()");
     *
     * // Initialize the banned methods cache
     * MethodUtils.initBannedMethods();
     *
     * // After this call, findMethod will return null for banned methods
     * Method substringMethod = MethodUtils.findMethod(String.class, "substring", int.class, int.class);
     * // substringMethod will be null
     * }</pre>
     */
    public static void initBannedMethods() {
        String bannedMethodsPropertyValue = getSystemProperty(BANNED_METHODS_PROPERTY_NAME);
        String[] bannedMethodsSignatures = split(bannedMethodsPropertyValue, VERTICAL_BAR_CHAR);
        int length = length(bannedMethodsSignatures);
        if (length > 0) {
            ClassLoader classLoader = MethodUtils.class.getClassLoader();
            for (String bannedMethodsSignature : bannedMethodsSignatures) {
                String signature = bannedMethodsSignature.trim();
                String declaredClassName = substringBefore(signature, SHARP);
                String methodName = substringBetween(signature, SHARP, LEFT_PARENTHESIS);
                String parameterClasses = substringBetween(signature, LEFT_PARENTHESIS, RIGHT_PARENTHESIS);
                String[] parameterClassNames = split(parameterClasses, COMMA_CHAR);
                Class<?> declaredClass = resolveClass(declaredClassName, classLoader);
                Class<?>[] parameterTypes = new Class[parameterClassNames.length];
                for (int i = 0; i < parameterClassNames.length; i++) {
                    parameterTypes[i] = resolveClass(parameterClassNames[i], classLoader);
                }
                banMethod(declaredClass, methodName, parameterTypes);
            }
        }
    }

    /**
     * Bans method to the cache based on the provided class, method name, and parameter types.
     * <p>
     * This method creates a {@link MethodKey} using the specified parameters, finds the corresponding {@link Method}
     * using {@link #doFindMethod(MethodKey)}, and stores it in the {@link #bannedMethodsCache}. If the method is already
     * present in the cache, it will not be overwritten.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Ban the String.substring(int, int) method
     * MethodUtils.banMethod(String.class, "substring", int.class, int.class);
     *
     * // After this call, findMethod will return null for the banned method
     * Method method = MethodUtils.findMethod(String.class, "substring", int.class, int.class);
     * // method will be null
     * }</pre>
     *
     * @param declaredClass  the class that declares the method to be banned
     * @param methodName     the name of the method to be banned
     * @param parameterTypes the parameter types of the method to be banned (can be empty if the method has no parameters)
     * @return the banned method, or {@code null} if the method could not be found
     * @throws NullPointerException If the target method can't be found
     * @see #initBannedMethods()
     * @see #bannedMethodsCache
     */
    @Nonnull
    public static Method banMethod(@Nonnull Class<?> declaredClass, @Nonnull String methodName, @Nonnull Class<?>... parameterTypes) {
        MethodKey key = buildKey(declaredClass, methodName, parameterTypes);
        Method method = methodsCache.computeIfAbsent(key, MethodUtils::doFindMethod);
        bannedMethodsCache.put(key, method);
        return method;
    }

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
    public static Predicate<? super Method> excludedDeclaredClass(@Nullable Class<?> declaredClass) {
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
    @Immutable
    public static List<Method> getDeclaredMethods(@Nullable Class<?> targetClass) {
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
    @Immutable
    public static List<Method> getMethods(@Nullable Class<?> targetClass) {
        return findMethods(targetClass, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all declared {@link Method methods} of the target class, including the inherited methods.
     *
     * <p>This method retrieves all methods that are declared in the specified class and its superclasses,
     * including those from interfaces implemented by the class and its ancestors.</p>
     *
     * <h3>Example Usage</h3>
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
    @Immutable
    public static List<Method> getAllDeclaredMethods(@Nullable Class<?> targetClass) {
        return findAllDeclaredMethods(targetClass, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all public {@link Method methods} of the target class, including the inherited methods.
     *
     * <p>This method retrieves all public methods that are declared in the specified class and its superclasses,
     * including those from interfaces implemented by the class and its ancestors.</p>
     *
     * <h3>Example Usage</h3>
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
    @Immutable
    public static List<Method> getAllMethods(@Nullable Class<?> targetClass) {
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
    @Immutable
    public static List<Method> findDeclaredMethods(@Nullable Class<?> targetClass, @Nullable Predicate<? super Method>... methodsToFilter) {
        return findMethods(targetClass, false, false, methodsToFilter);
    }

    /**
     * Find all public methods directly declared in the specified class, without including inherited methods.
     *
     * <p>This method retrieves only the public methods that are explicitly declared in the given class,
     * excluding any methods from its superclasses or interfaces.</p>
     *
     * <h3>Example Usage</h3>
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
    @Immutable
    public static List<Method> findMethods(@Nullable Class<?> targetClass, @Nullable Predicate<? super Method>... methodsToFilter) {
        return findMethods(targetClass, false, true, methodsToFilter);
    }

    /**
     * Retrieves all declared methods directly defined in the specified class, excluding inherited methods.
     *
     * <p>This method returns only the methods that are explicitly declared in the given class,
     * and does not include any methods from superclasses or interfaces.</p>
     *
     * <h3>Example Usage</h3>
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
    @Immutable
    public static List<Method> findAllDeclaredMethods(@Nullable Class<?> targetClass, @Nullable Predicate<? super Method>... methodsToFilter) {
        return findMethods(targetClass, true, false, methodsToFilter);
    }

    /**
     * Get all public {@link Method methods} of the target class, including the inherited methods.
     *
     * <p>This method retrieves all public methods that are declared in the specified class and its superclasses,
     * including those from interfaces implemented by the class and its ancestors.</p>
     *
     * <h3>Example Usage</h3>
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
    @Immutable
    public static List<Method> findAllMethods(@Nullable Class<?> targetClass, @Nullable Predicate<? super Method>... methodsToFilter) {
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
    @Immutable
    public static List<Method> findMethods(@Nullable Class<?> targetClass, boolean includeInheritedTypes, boolean publicOnly,
                                           @Nullable Predicate<? super Method>... methodsToFilter) {

        if (isInvalidDeclaringClass(targetClass)) {
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
        LinkedList<Method> allMethods = newLinkedList();

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
    public static Method findMethod(@Nullable Class targetClass, @Nullable String methodName) {
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
    public static Method findMethod(@Nullable Class targetClass, @Nullable String methodName, @Nullable Class<?>... parameterTypes) {
        MethodKey key = buildKey(targetClass, methodName, parameterTypes);
        return bannedMethodsCache.containsKey(key) ? null : methodsCache.computeIfAbsent(key, MethodUtils::doFindMethod);
    }

    /**
     * Finds a declared method in the specified class, including its superclasses and interfaces.
     *
     * <p>This method searches for a method with the given name and parameter types in the specified class,
     * its superclasses (if the class is not an interface), and its implemented interfaces. It returns the
     * first matching method found.</p>
     *
     * <h3>Example Usage</h3>
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
    public static Method findDeclaredMethod(@Nullable Class<?> targetClass, @Nullable String methodName, @Nullable Class<?>... parameterTypes) {
        if (isInvalidDeclaringClass(targetClass)) {
            return null;
        }
        // First, try to find the declared method in directly target class
        Method method = doFindDeclaredMethod(targetClass, methodName, parameterTypes);
        // Second, to find the declared method in the super class
        if (method == null) {
            Class<?> superClass = targetClass.isInterface() ? Object.class : targetClass.getSuperclass();
            method = findDeclaredMethod(superClass, methodName, parameterTypes);
        }
        // Third, to find the declared method in the interfaces
        if (method == null) {
            for (Class<?> interfaceClass : targetClass.getInterfaces()) {
                method = findDeclaredMethod(interfaceClass, methodName, parameterTypes);
                if (method != null) {
                    break;
                }
            }
        }
        // Finally, log if the method was not found
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
     * @throws IllegalArgumentException If the method cannot be found, or the arguments do not match the method's
     *                                  parameter types or accessed.
     * @throws RuntimeException         If the underlying method throws an exception during invocation.
     */
    @Nullable
    public static <R> R invokeMethod(@Nonnull Object object, @Nonnull String methodName, @Nonnull Object... arguments) {
        return invokeMethod(object, object.getClass(), methodName, arguments);
    }

    /**
     * Invokes a method with the specified name on the given object, optionally forcing accessibility.
     *
     * <p>When {@code forceAccess} is {@code true}, this method allows invocation of non-public methods by
     * setting accessible flag before invoking.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class User {
     *     private String secret() {
     *         return "hidden";
     *     }
     * }
     *
     * User user = new User();
     * String value = MethodUtils.invokeMethod(true, user, "secret");
     * System.out.println(value); // Output: hidden
     * }</pre>
     *
     * @param <R>         the expected return type
     * @param forceAccess whether to force method accessibility
     * @param object      the target object
     * @param methodName  the method name
     * @param arguments   method arguments
     * @return the invocation result, or {@code null} if method return type is void
     */
    @Nullable
    public static <R> R invokeMethod(boolean forceAccess, @Nonnull Object object, @Nonnull String methodName, @Nonnull Object... arguments) {
        return invokeMethod(forceAccess, object, object.getClass(), methodName, arguments);
    }

    /**
     * Invokes a method with the specified name on the given class type, using the provided arguments.
     *
     * <p>This method dynamically searches for a method in the specified class that matches the method name
     * and argument types, and then invokes it. It supports both instance and static methods.</p>
     *
     * <h3>Example Usage</h3>
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
     * @param object     The object on which the method will be invoked. Can be null for static methods.
     * @param type       The class type to search for the method. Must not be null.
     * @param methodName The name of the method to invoke. Must not be null or empty.
     * @param arguments  The arguments to pass to the method. Can be null or empty if the method requires no parameters.
     * @param <R>        The expected return type of the method.
     * @return The result of invoking the method, wrapped in the appropriate type.
     * @throws IllegalArgumentException If the provided object is null or the arguments do not match the method's parameter types or accessed.
     * @throws RuntimeException         If the underlying method throws an exception during invocation.
     */
    @Nullable
    public static <R> R invokeMethod(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String methodName, @Nonnull Object... arguments) {
        return invokeMethod(false, object, type, methodName, arguments);
    }

    /**
     * Invokes a method by name on the specified type, optionally forcing accessibility.
     *
     * <p>This overload is useful when the invocation target is represented by a super type or interface,
     * or when private/protected methods need to be accessed via reflection.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class BaseService {
     *     protected String ping(String name) {
     *         return "pong " + name;
     *     }
     * }
     *
     * BaseService service = new BaseService();
     * String result = MethodUtils.invokeMethod(true, service, BaseService.class, "ping", "Microsphere");
     * System.out.println(result); // Output: pong Microsphere
     * }</pre>
     *
     * @param <R>         the expected return type
     * @param forceAccess whether to force method accessibility
     * @param object      the target object (can be {@code null} for static methods)
     * @param type        the type used to resolve the method
     * @param methodName  the method name
     * @param arguments   method arguments
     * @return the invocation result, or {@code null} if method return type is void
     * @throws IllegalArgumentException if the method cannot be accessed with provided arguments
     * @throws RuntimeException         if the underlying method throws an exception
     */
    @Nullable
    public static <R> R invokeMethod(boolean forceAccess, @Nonnull Object object, @Nonnull Class<?> type, @Nonnull String methodName, @Nonnull Object... arguments) {
        List<Method> allDeclaredMethods = findAllDeclaredMethods(type, method -> {
            if (Objects.equals(methodName, method.getName()) && matchParameterTypes(method, arguments)) {
                return true;
            }
            return false;
        });
        if (allDeclaredMethods.isEmpty()) {
            String message = format("No method was not matched by the declared type : {} , name : '{}' , arguments : {}",
                    type, methodName, arrayToString(arguments));
            throw new IllegalArgumentException(message);
        }
        Method method = allDeclaredMethods.get(0);
        return invokeMethod(forceAccess, object, method, arguments);
    }

    /**
     * Invokes a static method of the specified target class with the given method name and arguments.
     *
     * <p>This utility method simplifies the process of invoking a static method using reflection by internally
     * calling {@link #invokeMethod(Object, Class, String, Object...)} with a null instance to indicate that
     * the method is static.</p>
     *
     * <h3>Example Usage</h3>
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
     * @throws NullPointerException     if the provided target class or method name is null or the method cannot be found.
     * @throws IllegalArgumentException if the arguments do not match the method's parameter types or accessed
     * @throws RuntimeException         if the underlying method throws an exception during invocation
     */
    @Nullable
    public static <R> R invokeStaticMethod(@Nonnull Class<?> targetClass, @Nonnull String methodName, @Nonnull Object... arguments) {
        return invokeStaticMethod(false, targetClass, methodName, arguments);
    }

    /**
     * Invokes a static method of the specified class by name, with optional forced accessibility.
     *
     * <p>When {@code forceAccess} is {@code true}, this method attempts to make the target method
     * accessible before invocation, allowing reflective access to non-public static methods.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class SecretMath {
     *     private static int sum(int a, int b) {
     *         return a + b;
     *     }
     * }
     *
     * Integer value = MethodUtils.invokeStaticMethod(true, SecretMath.class, "sum", 1, 2);
     * System.out.println(value); // Output: 3
     * }</pre>
     *
     * @param <R>         the expected return type
     * @param forceAccess whether to force accessibility for non-public methods
     * @param targetClass the class declaring the static method
     * @param methodName  the name of the static method to invoke
     * @param arguments   the arguments passed to the method
     * @return the invocation result, or {@code null} if the method return type is void
     * @throws NullPointerException     if {@code targetClass} or {@code methodName} is null, or if method lookup fails
     * @throws IllegalArgumentException if the arguments do not match the method signature
     * @throws RuntimeException         if the underlying method throws an exception
     */
    @Nullable
    public static <R> R invokeStaticMethod(boolean forceAccess, @Nonnull Class<?> targetClass, @Nonnull String methodName, @Nonnull Object... arguments) {
        return invokeMethod(forceAccess, null, targetClass, methodName, arguments);
    }

    /**
     * Invokes the specified static method represented by the given {@link Method} object.
     *
     * <p>This method is specifically designed to invoke static methods. If the provided method is not static,
     * it may result in an exception during invocation.</p>
     *
     * <h3>Example Usage</h3>
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
     * @throws IllegalArgumentException if the provided method is null or the arguments do not match the method's parameter types or accessed.
     */
    @Nullable
    public static <R> R invokeStaticMethod(@Nonnull Method method, @Nonnull Object... arguments) {
        return invokeStaticMethod(false, method, arguments);
    }

    /**
     * Invokes a static method represented by a {@link Method} with optional forced accessibility.
     *
     * <p>If {@code forceAccess} is {@code true}, this method tries to enable access to non-public
     * static methods before invocation.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class Internal {
     *     private static String echo(String value) {
     *         return value;
     *     }
     * }
     *
     * Method method = Internal.class.getDeclaredMethod("echo", String.class);
     * String result = MethodUtils.invokeStaticMethod(method, true, "Microsphere");
     * System.out.println(result); // Output: Microsphere
     * }</pre>
     *
     * @param <R>         the expected return type
     * @param forceAccess whether to force accessibility for non-public methods
     * @param method      the static method to invoke
     * @param arguments   the arguments passed to the method
     * @return the invocation result, or {@code null} if the method return type is void
     * @throws IllegalArgumentException if the method is null, non-static, or arguments do not match
     * @throws RuntimeException         if the underlying method throws an exception
     */
    @Nullable
    public static <R> R invokeStaticMethod(boolean forceAccess, @Nonnull Method method, @Nonnull Object... arguments) {
        return invokeMethod(forceAccess, null, method, arguments);
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
     * @throws IllegalArgumentException if the method is <code>null</code> or is an
     *                                  instance method and the specified object argument
     *                                  is not an instance of the class or interface
     *                                  declaring the underlying method (or of a subclass
     *                                  or implementor thereof); if the number of actual
     *                                  and formal parameters differ; if an unwrapping
     *                                  conversion for primitive arguments fails; or if,
     *                                  after possible unwrapping, a parameter value
     *                                  cannot be converted to the corresponding formal
     *                                  parameter type by a method invocation conversion.
     * @throws RuntimeException         if the underlying method throws an exception.
     */
    @Nullable
    public static <R> R invokeMethod(@Nullable Object instance, @Nonnull Method method, @Nonnull Object... arguments) {
        return invokeMethod(false, instance, method, arguments);
    }

    /**
     * Invokes the given {@link Method} on the specified instance, optionally forcing reflective access.
     *
     * <p>This overload is the core reflective invocation path used by other utility methods in this class.
     * If {@code forceAccess} is {@code true}, it attempts to make the method accessible before invoking,
     * which allows calling non-public methods when the runtime permits it.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class Demo {
     *     private String hello(String name) {
     *         return "Hello, " + name;
     *     }
     * }
     *
     * Demo demo = new Demo();
     * Method hello = Demo.class.getDeclaredMethod("hello", String.class);
     * String value = MethodUtils.invokeMethod(true, demo, hello, "Microsphere");
     * System.out.println(value); // Output: Hello, Microsphere
     * }</pre>
     *
     * <pre>{@code
     * class MathUtil {
     *     static int sum(int a, int b) {
     *         return a + b;
     *     }
     * }
     *
     * Method sum = MathUtil.class.getDeclaredMethod("sum", int.class, int.class);
     * Integer result = MethodUtils.invokeMethod(true, null, sum, 1, 2);
     * System.out.println(result); // Output: 3
     * }</pre>
     *
     * @param <R>         the expected return type
     * @param forceAccess whether to force accessibility before invocation
     * @param instance    the target instance for instance methods; may be {@code null} for static methods
     * @param method      the method to invoke, must not be {@code null}
     * @param arguments   the invocation arguments (can be empty)
     * @return the invocation result, or {@code null} when the method return type is {@code void}
     * @throws IllegalArgumentException if the method cannot be accessed with provided arguments
     * @throws RuntimeException         if the underlying method throws an exception
     */
    @Nullable
    public static <R> R invokeMethod(boolean forceAccess, @Nullable Object instance, @Nonnull Method method, @Nonnull Object... arguments) {
        assertNotNull(method, () -> "The 'method' must not be null");
        R result = null;
        RuntimeException failure = null;
        boolean trySetAccessible = false;
        try {
            if (forceAccess) {
                trySetAccessible = trySetAccessible(method);
            }
            result = (R) method.invoke(instance, arguments);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            failure = wrap(e, IllegalArgumentException.class);
        } catch (InvocationTargetException e) {
            failure = wrap(e.getTargetException(), RuntimeException.class);
        } finally {
            if (logger.isTraceEnabled()) {
                logger.trace("Invoked the method[signature : '{}' , forceAccess : {} ,  trySetAccessible : {} , instance : {} , arguments : {}] : {}",
                        getSignature(method), forceAccess, trySetAccessible, instance, arrayToString(arguments), result, failure);
            }
        }

        if (failure != null) {
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
    public static boolean overrides(@Nullable Method overrider, @Nullable Method overridden) {

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
     * @throws NullPointerException if the provided method is null
     */
    @Nullable
    public static Method findNearestOverriddenMethod(@Nonnull Method overrider) {
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
    public static Method findOverriddenMethod(@Nullable Method overrider, @Nullable Class<?> targetClass) {
        List<Method> matchedMethods = findDeclaredMethods(targetClass, method -> overrides(overrider, method));
        return first(matchedMethods);
    }

    /**
     * Finds the single abstract method (SAM) of a functional interface.
     *
     * <p>This method checks if the provided {@code type} is an interface and not annotated with
     * {@link FunctionalInterface}. It then searches for a single abstract method that is not static,
     * default, synthetic, or a bridge method. If such a method is found, it is returned; otherwise,
     * this method returns {@code null}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @FunctionalInterface
     * interface MyFunctionalInterface {
     *     void execute();
     * }
     *
     * Method sam = MethodUtils.findFunctionalInterfaceMethod(MyFunctionalInterface.class);
     * if (sam != null) {
     *     System.out.println("Found SAM: " + sam.getName());
     * } else {
     *     System.out.println("No SAM found.");
     * }
     * }</pre>
     *
     * @param type the class to inspect for a functional interface method
     * @return the single abstract method if found; otherwise, {@code null}
     */
    @Nullable
    public static Method findFunctionalInterfaceMethod(@Nonnull Class<?> type) {
        if (!isInterface(type)) {
            return null;
        }

        Method[] methods = type.getMethods();
        int length = methods.length;
        if (length == 0) {
            return null;
        }

        int count = 0;
        int index = -1;

        for (int i = 0; i < length; i++) {
            Method method = methods[i];
            if (isFunctionalInterfaceMethod(method)) {
                if (count > 0) { // More than one functional interface method found, return null immediately
                    return null;
                }
                count++;
                index = i;
            }
        }

        return index == -1 ? null : methods[index];
    }

    static boolean isFunctionalInterfaceMethod(Method method) {
        int modifiers = method.getModifiers();
        if (matchesAny(modifiers, STATIC, BRIDGE, SYNTHETIC) || method.isDefault() || isOverridenObjectMethod(method)) {
            return false;
        }
        return true;
    }

    /**
     * Generates a string representation of the method signature.
     *
     * <p>The signature includes the fully qualified name of the declaring class,
     * the method name, and the parameter types in parentheses.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Method method = String.class.getMethod("substring", int.class, int.class);
     * String signature = MethodUtils.getSignature(method);
     * System.out.println(signature);  // Output: java.lang.String#substring(int,int)
     * }</pre>
     *
     * @param method The method for which to generate the signature.
     * @return A non-null string representing the method signature.
     * @throws NullPointerException if the provided method is null
     */
    @Nonnull
    public static String getSignature(@Nonnull Method method) {
        return buildSignature(method.getDeclaringClass(), method.getName(), method.getParameterTypes());
    }

    /**
     * Builds a method signature string based on the provided declaring class, method name, and parameter types.
     *
     * <p>The generated signature follows the format: {@code declaringClassName#methodName(paramType1,paramType2,...)}.
     * This utility method is primarily used internally to create unique identifiers for methods.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String signature = MethodUtils.buildSignature(String.class, "substring", new Class[]{int.class, int.class});
     * System.out.println(signature);  // Output: java.lang.String#substring(int,int)
     * }</pre>
     *
     * <pre>{@code
     * String signature = MethodUtils.buildSignature(List.class, "add", new Class[]{Object.class});
     * System.out.println(signature);  // Output: java.util.List#add(java.lang.Object)
     * }</pre>
     *
     * @param declaringClass the class that declares the method
     * @param methodName     the name of the method
     * @param parameterTypes the parameter types of the method
     * @return a non-null string representing the method signature
     * @throws NullPointerException if any of the provided arguments are null
     */
    public static String buildSignature(@Nonnull Class<?> declaringClass, @Nonnull String methodName, @Nonnull Class<?>... parameterTypes) {
        int parameterCount = length(parameterTypes);
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
    public static boolean isObjectMethod(@Nullable Method method) {
        if (method != null) {
            return isObjectClass(method.getDeclaringClass());
        }
        return false;
    }

    /**
     * Checks whether the given method overrides a method declared in the {@link Object} class.
     *
     * <p>This utility method is useful for identifying methods that provide custom implementations
     * of standard {@code Object} methods (like {@code toString}, {@code equals}, etc.) in subclasses.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Method toStringMethod = MyClass.class.getMethod("toString");
     * boolean isOverridden = MethodUtils.isOverridenObjectMethod(toStringMethod);
     * System.out.println(isOverridden); // Output: true if MyClass overrides toString, false otherwise
     * }</pre>
     *
     * @param method the method to check, may be null
     * @return true if the method overrides a method from the {@link Object} class; false otherwise or if the method is null
     */
    public static boolean isOverridenObjectMethod(@Nullable Method method) {
        if (method != null && method.getDeclaringClass() != Object.class && !isPrivate(method)) {
            String methodName = method.getName();
            Class<?>[] parameterTypes = method.getParameterTypes();
            return findMethod(Object.class, methodName, parameterTypes) != null;
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
    public static boolean isCallerSensitiveMethod(@Nullable Method method) {
        return isAnnotationPresent(method, CALLER_SENSITIVE_ANNOTATION_CLASS);
    }

    /**
     * Checks if the specified method is a JavaBean "is" getter method.
     *
     * <p>A method is considered an "is" getter if it starts with "is", has no parameters, and returns a boolean type.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     private boolean active;
     *
     *     public boolean isActive() {
     *         return active;
     *     }
     * }
     *
     * Method isActiveMethod = Example.class.getMethod("isActive");
     * boolean isIsMethod = MethodUtils.isIsMethod(isActiveMethod);
     * System.out.println(isIsMethod); // Output: true
     * }</pre>
     *
     * @param method the method to check, may be null
     * @return true if the method is an "is" getter; false otherwise or if the method is null
     */
    public static boolean isIsMethod(@Nullable Method method) {
        if (startsWith(getMethodName(method), IS_METHOD_NAME_PREFIX)) {
            if (isNoArgMethod(method)) {
                return matchesReturnType(method, boolean.class);
            }
        }
        return false;
    }

    /**
     * Checks if the specified method is a JavaBean getter method.
     *
     * <p>A method is considered a getter if it starts with "get", has no parameters, and does not return void.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     private String name;
     *
     *     public String getName() {
     *         return name;
     *     }
     * }
     *
     * Method getNameMethod = Example.class.getMethod("getName");
     * boolean isGetter = MethodUtils.isGetterMethod(getNameMethod);
     * System.out.println(isGetter); // Output: true
     * }</pre>
     *
     * @param method the method to check, may be null
     * @return true if the method is a getter; false otherwise or if the method is null
     */
    public static boolean isGetterMethod(@Nullable Method method) {
        if (startsWith(getMethodName(method), GET_METHOD_NAME_PREFIX)) {
            if (isNoArgMethod(method)) {
                return !matchesReturnType(method, void.class);
            }
        }
        return false;
    }

    /**
     * Checks if the specified method is a JavaBean setter method.
     *
     * <p>A method is considered a setter if it starts with "set", has exactly one parameter, and returns void.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     private String name;
     *
     *     public void setName(String name) {
     *         this.name = name;
     *     }
     * }
     *
     * Method setNameMethod = Example.class.getMethod("setName", String.class);
     * boolean isSetter = MethodUtils.isSetterMethod(setNameMethod);
     * System.out.println(isSetter); // Output: true
     * }</pre>
     *
     * @param method the method to check, may be null
     * @return true if the method is a setter; false otherwise or if the method is null
     */
    public static boolean isSetterMethod(@Nullable Method method) {
        if (startsWith(getMethodName(method), SET_METHOD_NAME_PREFIX)) {
            if (matchesParameterCount(method, 1)) {
                return matchesReturnType(method, void.class);
            }
        }
        return false;
    }

    /**
     * Retrieves the name of the specified method.
     *
     * <p>This utility method safely retrieves the name of a method, returning {@code null} if the method is {@code null}.
     * It is useful for logging or debugging purposes where method names are needed without risking a {@code NullPointerException}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Method method = String.class.getMethod("substring", int.class, int.class);
     * String methodName = MethodUtils.getMethodName(method);
     * System.out.println(methodName); // Output: substring
     *
     * Method nullMethod = null;
     * String nullMethodName = MethodUtils.getMethodName(nullMethod);
     * System.out.println(nullMethodName); // Output: null
     * }</pre>
     *
     * @param method the method from which to retrieve the name, may be null
     * @return the name of the method, or {@code null} if the method is {@code null}
     */
    @Nullable
    public static String getMethodName(@Nullable Method method) {
        return method == null ? null : method.getName();
    }

    /**
     * Checks if the specified method has the given number of parameters.
     *
     * <p>This utility method is useful for validating method signatures, especially when working with reflection
     * to ensure that a method matches expected parameter counts.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Method method = String.class.getMethod("substring", int.class, int.class);
     * boolean hasTwoParameters = MethodUtils.matchesParameterCount(method, 2);
     * System.out.println(hasTwoParameters); // Output: true
     *
     * Method noArgMethod = String.class.getMethod("isEmpty");
     * boolean hasNoParameters = MethodUtils.matchesParameterCount(noArgMethod, 0);
     * System.out.println(hasNoParameters); // Output: true
     *
     * Method nullMethod = null;
     * boolean resultForNull = MethodUtils.matchesParameterCount(nullMethod, 1);
     * System.out.println(resultForNull); // Output: false
     * }</pre>
     *
     * @param method         the method to check, may be null
     * @param parameterCount the expected number of parameters
     * @return true if the method has the specified number of parameters; false if the method is null or does not match
     */
    public static boolean matchesParameterCount(@Nullable Method method, int parameterCount) {
        return method == null ? false : method.getParameterCount() == parameterCount;
    }

    /**
     * Checks if the specified method has the given return type.
     *
     * <p>This utility method is useful for validating method signatures, especially when working with reflection
     * to ensure that a method matches expected return types.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Method method = String.class.getMethod("substring", int.class, int.class);
     * boolean returnsString = MethodUtils.matchesReturnType(method, String.class);
     * System.out.println(returnsString); // Output: true
     *
     * Method voidMethod = System.class.getMethod("gc");
     * boolean returnsVoid = MethodUtils.matchesReturnType(voidMethod, void.class);
     * System.out.println(returnsVoid); // Output: true
     *
     * Method nullMethod = null;
     * boolean resultForNull = MethodUtils.matchesReturnType(nullMethod, Object.class);
     * System.out.println(resultForNull); // Output: false
     * }</pre>
     *
     * @param method     the method to check, may be null
     * @param returnType the expected return type, may be null
     * @return true if the method has the specified return type; false if the method is null or does not match
     */
    public static boolean matchesReturnType(@Nullable Method method, @Nullable Class<?> returnType) {
        return method == null ? false : Objects.equals(returnType, method.getReturnType());
    }

    /**
     * Clears the cache of discovered methods.
     */
    public static void clearMethodsCache() {
        methodsCache.clear();
    }

    /**
     * Clears the cache of banned methods.
     *
     * <p>This method removes all entries from the {@link #bannedMethodsCache}, effectively
     * allowing all previously banned methods to be discoverable again by {@link #findMethod(Class, String, Class[])}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Clear all banned methods
     * MethodUtils.clearBannedMethodsCache();
     *
     * // After this call, findMethod will return previously banned methods
     * Method method = MethodUtils.findMethod(String.class, "substring", int.class, int.class);
     * // method may now be non-null if it was previously banned
     * }</pre>
     *
     * @see #initBannedMethods()
     * @see #bannedMethodsCache
     */
    public static void clearBannedMethodsCache() {
        bannedMethodsCache.clear();
    }

    /**
     * Clears the cache of declared methods.
     */
    public static void clearDeclaredMethodsCache() {
        declaredMethodsCache.clear();
    }

    static boolean isNoArgMethod(Method method) {
        return matchesParameterCount(method, 0);
    }

    static void filterDeclaredMethodsHierarchically(Class<?> targetClass, Predicate<? super Method> methodToFilter, List<Method> methodsToCollect) {
        filterDeclaredMethods(targetClass, methodToFilter, methodsToCollect);
        for (Class<?> interfaceClass : targetClass.getInterfaces()) {
            filterDeclaredMethodsHierarchically(interfaceClass, methodToFilter, methodsToCollect);
        }
    }

    static void filterDeclaredMethods(@Nullable Class<?> targetClass, Predicate<? super Method> methodToFilter, List<Method> methodsToCollect) {
        for (Method method : doGetDeclaredMethods(targetClass)) {
            if (methodToFilter.test(method)) {
                methodsToCollect.add(method);
            }
        }
    }

    static Method doFindDeclaredMethod(@Nonnull Class<?> klass, @Nullable String methodName, @Nullable Class<?>[] parameterTypes) {
        Method[] declaredMethods = doGetDeclaredMethods(klass);
        return doFindMethod(declaredMethods, methodName, parameterTypes);
    }

    static Method doFindMethod(@Nonnull Method[] methods, String methodName, Class<?>[] parameterTypes) {
        Method targetMethod = null;
        for (Method method : methods) {
            if (matches(method, methodName, parameterTypes)) {
                targetMethod = method;
                break;
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("To find the target method[name : '{}' , parameter types : {} , methods : {}] : {}",
                    methodName, arrayToString(parameterTypes), arrayToString(methods), targetMethod);
        }
        return targetMethod;
    }

    static boolean matches(@Nonnull Method method, @Nullable String methodName, @Nullable Class<?>[] parameterTypes) {
        if (!Objects.equals(method.getName(), methodName)) {
            return false;
        }
        int parameterCount = length(parameterTypes);
        if (!(method.getParameterCount() == parameterCount)) {
            return false;
        }
        return matchesParameterTypes(method.getParameterTypes(), parameterTypes, parameterCount);
    }

    static boolean matchesParameterTypes(Class<?>[] oneParameterTypes, Class<?>[] anotherParameterTypes, int parameterCount) {
        for (int i = 0; i < parameterCount; i++) {
            if (!Objects.equals(oneParameterTypes[i], anotherParameterTypes[i])) {
                return false;
            }
        }
        return true;
    }

    static Method[] doGetDeclaredMethods(@Nonnull Class<?> klass) {
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

        MethodKey(Class<?> declaredClass, String methodName, Class<?>... parameterTypes) {
            this.declaredClass = declaredClass;
            this.methodName = methodName;
            this.parameterTypes = parameterTypes == null ? EMPTY_CLASS_ARRAY : parameterTypes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof MethodKey)) {
                return false;
            }

            MethodKey methodKey = (MethodKey) o;

            return Objects.equals(this.declaredClass, methodKey.declaredClass)
                    && Objects.equals(this.methodName, methodKey.methodName)
                    && arrayEquals(this.parameterTypes, methodKey.parameterTypes);
        }

        @Override
        public int hashCode() {
            int result = hash(this.declaredClass);
            result = 31 * result + hash(this.methodName);
            result = 31 * result + hash(this.parameterTypes);
            return result;
        }

        @Override
        public String toString() {
            return buildSignature(this.declaredClass, this.methodName, this.parameterTypes);
        }
    }

    private MethodUtils() {
    }
}

