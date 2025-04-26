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

import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.util.Utils;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
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
     * Create an instance of {@link Predicate} for {@link Method} to exclude the specified declared class
     *
     * @param declaredClass the target class to exclude
     * @return non-null
     */
    public static Predicate<? super Method> excludedDeclaredClass(Class<?> declaredClass) {
        return method -> !Objects.equals(declaredClass, method.getDeclaringClass());
    }

    /**
     * Get all declared {@link Method methods} of the target class, excluding the inherited methods
     *
     * @param targetClass the target class
     * @return non-null read-only {@link List}
     * @see #findDeclaredMethods(Class, Predicate...)
     */
    public static List<Method> getDeclaredMethods(Class<?> targetClass) {
        return findDeclaredMethods(targetClass, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all public {@link Method methods} of the target class, excluding the inherited methods.
     *
     * @param targetClass the target class
     * @return non-null read-only {@link List}
     * @see #findMethods(Class, Predicate...)
     */
    public static List<Method> getMethods(Class<?> targetClass) {
        return findMethods(targetClass, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all declared {@link Method methods} of the target class, including the inherited methods.
     *
     * @param targetClass the target class
     * @return non-null read-only {@link List}
     * @see #findAllDeclaredMethods(Class, Predicate...)
     */
    public static List<Method> getAllDeclaredMethods(Class<?> targetClass) {
        return findAllDeclaredMethods(targetClass, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all public {@link Method methods} of the target class, including the inherited methods.
     *
     * @param targetClass the target class
     * @return non-null read-only {@link List}
     * @see #findAllMethods(Class, Predicate...)
     */
    public static List<Method> getAllMethods(Class<?> targetClass) {
        return findAllMethods(targetClass, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Find all declared {@link Method methods} of the target class, excluding the inherited methods
     *
     * @param targetClass     the target class
     * @param methodsToFilter (optional) the methods to be filtered
     * @return non-null read-only {@link List}
     * @see #findMethods(Class, boolean, boolean, Predicate[])
     */
    public static List<Method> findDeclaredMethods(Class<?> targetClass, Predicate<? super Method>... methodsToFilter) {
        return findMethods(targetClass, false, false, methodsToFilter);
    }

    /**
     * Find all public {@link Method methods} of the target class, excluding the inherited methods.
     *
     * @param targetClass     the target class
     * @param methodsToFilter (optional) the methods to be filtered
     * @return non-null read-only {@link List}
     * @see #findMethods(Class, boolean, boolean, Predicate[])
     */
    public static List<Method> findMethods(Class<?> targetClass, Predicate<? super Method>... methodsToFilter) {
        return findMethods(targetClass, false, true, methodsToFilter);
    }

    /**
     * Get all declared {@link Method methods} of the target class, including the inherited methods.
     *
     * @param targetClass     the target class
     * @param methodsToFilter (optional) the methods to be filtered
     * @return non-null read-only {@link List}
     * @see #findMethods(Class, boolean, boolean, Predicate[])
     */
    public static List<Method> findAllDeclaredMethods(Class<?> targetClass, Predicate<? super Method>... methodsToFilter) {
        return findMethods(targetClass, true, false, methodsToFilter);
    }

    /**
     * Get all public {@link Method methods} of the target class, including the inherited methods.
     *
     * @param targetClass     the target class
     * @param methodsToFilter (optional) the methods to be filtered
     * @return non-null read-only {@link List}
     * @see #findMethods(Class, boolean, boolean, Predicate[])
     */
    public static List<Method> findAllMethods(Class<?> targetClass, Predicate<? super Method>... methodsToFilter) {
        return findMethods(targetClass, true, true, methodsToFilter);
    }

    /**
     * Find all {@link Method methods} of the target class by the specified {@link Predicate}
     *
     * @param targetClass           the target class
     * @param includeInheritedTypes include the inherited types, e,g. super classes or interfaces
     * @param publicOnly            only public method
     * @param methodsToFilter       (optional) the methods to be filtered
     * @return non-null read-only {@link List}
     */
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
     * Find the {@link Method} by the specified type(including inherited types) and method name without the
     * parameter type.
     *
     * @param targetClass the target type
     * @param methodName  the specified method name
     * @return if not found, return <code>null</code>
     */
    public static Method findMethod(Class targetClass, String methodName) {
        return findMethod(targetClass, methodName, EMPTY_CLASS_ARRAY);
    }

    /**
     * Find the {@link Method} by the specified type (including inherited types) and method name and parameter types
     * with cache
     *
     * @param targetClass    the target type
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @return if not found, return <code>null</code>
     */
    public static Method findMethod(Class targetClass, String methodName, Class<?>... parameterTypes) {
        MethodKey key = buildKey(targetClass, methodName, parameterTypes);
        return methodsCache.computeIfAbsent(key, MethodUtils::doFindMethod);
    }

    /**
     * Find the declared {@link Method} by the specified type (including inherited types) and method name and parameter types
     *
     * @param targetClass    the target type
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @return
     */
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
     * Invoke the target objects' method
     *
     * @param object     the target object
     * @param methodName the method name
     * @param arguments  the method arguments
     * @param <R>        the return type
     * @return the target method's execution result
     */
    public static <R> R invokeMethod(Object object, String methodName, Object... arguments) {
        Class type = object.getClass();
        return invokeMethod(object, type, methodName, arguments);
    }

    /**
     * Invoke the target classes' static method
     *
     * @param targetClass the target class
     * @param methodName  the method name
     * @param arguments   the method arguments
     * @param <R>         the return type
     * @return the target method's execution result
     */
    public static <R> R invokeStaticMethod(Class<?> targetClass, String methodName, Object... arguments) {
        return invokeMethod(null, targetClass, methodName, arguments);
    }

    /**
     * Invoke the target classes' static method
     *
     * @param method    the method
     * @param arguments the method arguments
     * @param <R>       the return type
     * @return the target method's execution result
     */
    public static <R> R invokeStaticMethod(Method method, Object... arguments) {
        return invokeMethod(null, method, arguments);
    }

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
     * @param overrider  the first method, possible overrider
     * @param overridden the second method, possibly being overridden
     * @return {@code true} if and only if the first method overrides
     * the second
     * @jls 8.4.8 Inheritance, Overriding, and Hiding
     * @jls 9.4.1 Inheritance and Overriding
     * @see Elements#overrides(ExecutableElement, ExecutableElement, TypeElement)
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
     * Find the nearest overridden {@link Method method} from the inherited class
     *
     * @param overrider the overrider {@link Method method}
     * @return if found, the overrider <code>method</code>, or <code>null</code>
     */
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
     * Find the overridden {@link Method method} from the target class
     *
     * @param overrider   the overrider {@link Method method}
     * @param targetClass the class that is declaring the overridden {@link Method method}
     * @return if found, the overrider <code>method</code>, or <code>null</code>
     */
    public static Method findOverriddenMethod(Method overrider, Class<?> targetClass) {
        List<Method> matchedMethods = findDeclaredMethods(targetClass, method -> overrides(overrider, method));
        return matchedMethods.isEmpty() ? null : matchedMethods.get(0);
    }

    /**
     * Get the signature of {@link Method the specified method}
     *
     * @param method {@link Method the specified method}
     * @return non-null
     */
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

    public static boolean isObjectMethod(Method method) {
        if (method != null) {
            return isObjectClass(method.getDeclaringClass());
        }
        return false;
    }

    /**
     * Test whether the specified {@link Method method} annotates {@linkplain jdk.internal.reflect.CallerSensitive} or not
     *
     * @param method {@link Method}
     * @return <code>true</code> if the specified {@link Method method} annotates {@linkplain jdk.internal.reflect.CallerSensitive}
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
