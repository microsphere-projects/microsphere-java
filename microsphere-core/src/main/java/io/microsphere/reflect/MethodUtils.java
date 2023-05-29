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
package io.github.microsphere.reflect;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static io.github.microsphere.collection.SetUtils.of;
import static io.github.microsphere.constants.SymbolConstants.*;
import static io.github.microsphere.lang.function.Streams.filterAll;
import static io.github.microsphere.reflect.MemberUtils.isPrivate;
import static io.github.microsphere.reflect.MemberUtils.isStatic;
import static io.github.microsphere.util.ClassUtils.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_CLASS_ARRAY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * The Java Reflection {@link Method} Utility class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class MethodUtils {

    /**
     * The {@link Predicate} reference to {@link MethodUtils#isObjectMethod(Method)}
     */
    public final static Predicate<? super Method> OBJECT_METHOD_PREDICATE = MethodUtils::isObjectMethod;

    public final static Set<Method> OBJECT_METHODS = of(Object.class.getMethods());

    private MethodUtils() {
    }

    /**
     * Create an instance of {@link Predicate} for {@link Method} to exclude the specified declared class
     *
     * @param declaredClass the declared class to exclude
     * @return non-null
     */
    public static Predicate<Method> excludedDeclaredClass(Class<?> declaredClass) {
        return method -> !Objects.equals(declaredClass, method.getDeclaringClass());
    }

    /**
     * Get all {@link Method methods} of the declared class
     *
     * @param declaringClass        the declared class
     * @param includeInheritedTypes include the inherited types, e,g. super classes or interfaces
     * @param publicOnly            only public method
     * @param methodsToFilter       (optional) the methods to be filtered
     * @return non-null read-only {@link List}
     */
    public static List<Method> getMethods(Class<?> declaringClass, boolean includeInheritedTypes, boolean publicOnly,
                                          Predicate<? super Method>... methodsToFilter) {

        if (declaringClass == null || declaringClass.isPrimitive()) {
            return emptyList();
        }

        // All declared classes
        List<Class<?>> declaredClasses = new LinkedList<>();
        // Add the top declaring class
        declaredClasses.add(declaringClass);
        // If the super classes are resolved, all them into declaredClasses
        if (includeInheritedTypes) {
            declaredClasses.addAll(getAllInheritedTypes(declaringClass));
        }

        // All methods
        List<Method> allMethods = new LinkedList<>();

        for (Class<?> classToSearch : declaredClasses) {
            Method[] methods = publicOnly ? classToSearch.getMethods() : classToSearch.getDeclaredMethods();
            // Add the declared methods or public methods
            for (Method method : methods) {
                allMethods.add(method);
            }
        }

        return unmodifiableList(filterAll(allMethods, methodsToFilter));
    }

    /**
     * Get all declared {@link Method methods} of the declared class, excluding the inherited methods
     *
     * @param declaringClass  the declared class
     * @param methodsToFilter (optional) the methods to be filtered
     * @return non-null read-only {@link List}
     * @see #getMethods(Class, boolean, boolean, Predicate[])
     */
    public static List<Method> getDeclaredMethods(Class<?> declaringClass, Predicate<Method>... methodsToFilter) {
        return getMethods(declaringClass, false, false, methodsToFilter);
    }

    /**
     * Get all public {@link Method methods} of the declared class, including the inherited methods.
     *
     * @param declaringClass  the declared class
     * @param methodsToFilter (optional) the methods to be filtered
     * @return non-null read-only {@link List}
     * @see #getMethods(Class, boolean, boolean, Predicate[])
     */
    public static List<Method> getMethods(Class<?> declaringClass, Predicate<Method>... methodsToFilter) {
        return getMethods(declaringClass, false, true, methodsToFilter);
    }

    /**
     * Get all declared {@link Method methods} of the declared class, including the inherited methods.
     *
     * @param declaringClass  the declared class
     * @param methodsToFilter (optional) the methods to be filtered
     * @return non-null read-only {@link List}
     * @see #getMethods(Class, boolean, boolean, Predicate[])
     */
    public static List<Method> getAllDeclaredMethods(Class<?> declaringClass, Predicate<? super Method>... methodsToFilter) {
        return getMethods(declaringClass, true, false, methodsToFilter);
    }

    /**
     * Get all public {@link Method methods} of the declared class, including the inherited methods.
     *
     * @param declaringClass  the declared class
     * @param methodsToFilter (optional) the methods to be filtered
     * @return non-null read-only {@link List}
     * @see #getMethods(Class, boolean, boolean, Predicate[])
     */
    public static List<Method> getAllMethods(Class<?> declaringClass, Predicate<Method>... methodsToFilter) {
        return getMethods(declaringClass, true, true, methodsToFilter);
    }

    /**
     * Find the {@link Method} by the the specified type and method name without the parameter types
     *
     * @param type       the target type
     * @param methodName the specified method name
     * @return if not found, return <code>null</code>
     */
    public static Method findMethod(Class type, String methodName) {
        return findMethod(type, methodName, EMPTY_CLASS_ARRAY);
    }

    /**
     * Find the {@link Method} by the the specified type, method name and parameter types
     *
     * @param type           the target type
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @return if not found, return <code>null</code>
     */
    public static Method findMethod(Class type, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        try {
            if (type != null && isNotEmpty(methodName)) {
                method = type.getDeclaredMethod(methodName, parameterTypes);
            }
        } catch (NoSuchMethodException e) {
        }
        return method;
    }

    /**
     * Invoke the target objects' method
     *
     * @param object     the target object
     * @param methodName the method name
     * @param parameters the method parameters
     * @param <T>        the return type
     * @return the target method's execution result
     */
    public static <T> T invokeMethod(Object object, String methodName, Object... parameters) {
        Class type = object.getClass();
        return invokeMethod(object, type, methodName, parameters);
    }

    /**
     * Invoke the target classes' static method
     *
     * @param type       the target class
     * @param methodName the method name
     * @param parameters the method parameters
     * @param <T>        the return type
     * @return the target method's execution result
     */
    public static <T> T invokeStaticMethod(Class<?> type, String methodName, Object... parameters) {
        return invokeMethod(null, type, methodName, parameters);
    }

    public static <T> T invokeMethod(Object instance, Class<?> type, String methodName, Object... parameters) {
        Class[] parameterTypes = getTypes(parameters);
        Method method = findMethod(type, methodName, parameterTypes);
        T value = null;

        if (method == null) {
            throw new IllegalStateException(String.format("cannot find method %s,class: %s", methodName, type.getName()));
        }

        try {
            method.setAccessible(true);
            value = (T) method.invoke(instance, parameters);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        return value;
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

        if (overrider == null || overridden == null) {
            return false;
        }

        // equality comparison: If two methods are same
        if (Objects.equals(overrider, overridden)) {
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

        // Inheritance comparison: The declaring class of overrider must be inherit from the overridden's
        if (!overridden.getDeclaringClass().isAssignableFrom(overrider.getDeclaringClass())) {
            return false;
        }

        // Method comparison: must not be "default" method
        if (overrider.isDefault()) {
            return false;
        }

        // Method comparison: The method name must be equal
        if (!Objects.equals(overrider.getName(), overridden.getName())) {
            return false;
        }

        // Method comparison: The count of method parameters must be equal
        if (!Objects.equals(overrider.getParameterCount(), overridden.getParameterCount())) {
            return false;
        }

        // Method comparison: Any parameter type of overrider must equal the overridden's
        for (int i = 0; i < overrider.getParameterCount(); i++) {
            if (!Objects.equals(overridden.getParameterTypes()[i], overrider.getParameterTypes()[i])) {
                return false;
            }
        }

        // Method comparison: The return type of overrider must be inherit from the overridden's
        if (!overridden.getReturnType().isAssignableFrom(overrider.getReturnType())) {
            return false;
        }

        // Throwable comparison: "throws" Throwable list will be ignored, trust the compiler verify

        return true;
    }

    /**
     * Find the nearest overridden {@link Method method} from the inherited class
     *
     * @param overrider the overrider {@link Method method}
     * @return if found, the overrider <code>method</code>, or <code>null</code>
     */
    static Method findNearestOverriddenMethod(Method overrider) {
        Class<?> declaringClass = overrider.getDeclaringClass();
        Method overriddenMethod = null;
        for (Class<?> inheritedType : getAllInheritedTypes(declaringClass)) {
            overriddenMethod = findOverriddenMethod(overrider, inheritedType);
            if (overriddenMethod != null) {
                break;
            }
        }
        return overriddenMethod;
    }

    /**
     * Find the overridden {@link Method method} from the declaring class
     *
     * @param overrider      the overrider {@link Method method}
     * @param declaringClass the class that is declaring the overridden {@link Method method}
     * @return if found, the overrider <code>method</code>, or <code>null</code>
     */
    static Method findOverriddenMethod(Method overrider, Class<?> declaringClass) {
        List<Method> matchedMethods = getAllMethods(declaringClass, method -> overrides(overrider, method));
        return matchedMethods.isEmpty() ? null : matchedMethods.get(0);
    }

    /**
     * Get the signature of {@link Method the specified method}
     *
     * @param method {@link Method the specified method}
     * @return non-null
     */
    public static String getSignature(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        Class<?>[] parameterTypes = method.getParameterTypes();
        int parameterCount = parameterTypes.length;
        String[] parameterTypeNames = new String[parameterCount];
        String methodName = method.getName();
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
            return Objects.equals(Object.class, method.getDeclaringClass());
        }
        return false;
    }
}
