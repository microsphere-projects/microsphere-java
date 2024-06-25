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
import io.microsphere.util.ArrayUtils;
import io.microsphere.util.BaseUtils;

import javax.annotation.Nullable;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import static io.microsphere.collection.SetUtils.of;
import static io.microsphere.constants.SymbolConstants.COMMA_CHAR;
import static io.microsphere.constants.SymbolConstants.LEFT_PARENTHESIS_CHAR;
import static io.microsphere.constants.SymbolConstants.RIGHT_PARENTHESIS_CHAR;
import static io.microsphere.constants.SymbolConstants.SHARP_CHAR;
import static io.microsphere.lang.function.Streams.filterAll;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.AccessibleObjectUtils.trySetAccessible;
import static io.microsphere.reflect.MemberUtils.isPrivate;
import static io.microsphere.reflect.MemberUtils.isPublic;
import static io.microsphere.reflect.MemberUtils.isStatic;
import static io.microsphere.reflect.MethodUtils.MethodKey.buildKey;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
import static io.microsphere.util.ClassUtils.getAllInheritedTypes;
import static io.microsphere.util.ClassUtils.getTypeName;
import static io.microsphere.util.ClassUtils.getTypes;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

/**
 * The Java Reflection {@link Method} Utility class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class MethodUtils extends BaseUtils {

    private static final Logger logger = getLogger(MethodUtils.class);

    /**
     * The {@link Predicate} reference to {@link MethodUtils#isObjectMethod(Method)}
     */
    public final static Predicate<? super Method> OBJECT_METHOD_PREDICATE = MethodUtils::isObjectMethod;

    public final static Set<Method> OBJECT_METHODS = of(Object.class.getMethods());

    private final static ConcurrentMap<MethodKey, Method> methodsCache = new ConcurrentHashMap<>();

    static class MethodKey {

        private final Class<?> declaredClass;

        private final String methodName;

        private final Class<?>[] parameterTypes;

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
            return Arrays.equals(parameterTypes, methodKey.parameterTypes);
        }

        @Override
        public int hashCode() {
            int result = declaredClass != null ? declaredClass.hashCode() : 0;
            result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
            result = 31 * result + Arrays.hashCode(parameterTypes);
            return result;
        }

        @Override
        public String toString() {
            StringJoiner stringJoiner = new StringJoiner(",", "(", ") ");
            ArrayUtils.forEach(parameterTypes, parameterType -> stringJoiner.add(getTypeName(parameterType)));
            return getTypeName(declaredClass) + "#" + methodName + stringJoiner;
        }

        static MethodKey buildKey(Class<?> declaredClass, String methodName, Class<?>[] parameterTypes) {
            return new MethodKey(declaredClass, methodName, parameterTypes);
        }
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
    public static List<Method> getMethods(Class<?> declaringClass, boolean includeInheritedTypes, boolean publicOnly, Predicate<? super Method>... methodsToFilter) {

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
            // Method[] methods = publicOnly ? classToSearch.getMethods() : classToSearch.getDeclaredMethods();
            Method[] methods = classToSearch.getDeclaredMethods();
            // Add the declared methods or public methods
            for (Method method : methods) {
                if (publicOnly) {
                    if (Modifier.isPublic(method.getModifiers())) {
                        allMethods.add(method);
                    }
                } else {
                    allMethods.add(method);
                }
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
     * Find the {@link Method} by the the specified type(including inherited types) and method name without the
     * parameter type.
     *
     * @param type       the target type
     * @param methodName the specified method name
     * @return if not found, return <code>null</code>
     */
    public static Method findMethod(Class type, String methodName) {
        return findMethod(type, methodName, EMPTY_CLASS_ARRAY);
    }

    /**
     * Find the {@link Method} by the the specified type(including inherited types) and method name and parameter types
     *
     * @param type           the target type
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @return if not found, return <code>null</code>
     */
    public static Method findMethod(Class type, String methodName, Class<?>... parameterTypes) {
        MethodKey key = buildKey(type, methodName, parameterTypes);
        return methodsCache.computeIfAbsent(key, MethodUtils::findMethod);
    }

    static Method findMethod(MethodKey key) {
        Class<?> declaredClass = key.declaredClass;
        String methodName = key.methodName;
        Class<?>[] parameterTypes = key.parameterTypes;
        return findDeclaredMethod(declaredClass, methodName, parameterTypes);
    }

    public static Method findDeclaredMethod(Class<?> declaredClass, String methodName, Class<?>... parameterTypes) {
        Method method = getDeclaredMethod(declaredClass, methodName, parameterTypes);
        if (method == null) {
            Set<Class<?>> inheritedTypes = getAllInheritedTypes(declaredClass);
            for (Class<?> inheritedType : inheritedTypes) {
                method = getDeclaredMethod(inheritedType, methodName, parameterTypes);
                if (method != null) {
                    break;
                }
            }
        }
        if (method == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("The declared method was not found in the declared class[name : '{}'] by name['{}'] and parameter types['']",
                        declaredClass, methodName, asList(parameterTypes));
            }
        }

        return method;
    }

    public static Method getDeclaredMethod(Class<?> declaredClass, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        try {
            method = declaredClass.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
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
     * @param type       the target class
     * @param methodName the method name
     * @param arguments  the method arguments
     * @param <R>        the return type
     * @return the target method's execution result
     */
    public static <R> R invokeStaticMethod(Class<?> type, String methodName, Object... arguments) {
        return invokeMethod(null, type, methodName, arguments);
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
        R result = null;
        boolean accessible = false;
        RuntimeException failure = null;
        try {
            trySetAccessible(method);
            result = (R) method.invoke(instance, arguments);
        } catch (IllegalAccessException e) {
            String errorMessage = format("The method[signature : '{}' , instance : {}] can't be accessed[accessible : {}]", getSignature(method), instance, accessible);
            failure = new IllegalStateException(errorMessage, e);
        } catch (IllegalArgumentException e) {
            String errorMessage = format("The arguments can't match the method[signature : '{}' , instance : {}] : {}", getSignature(method), instance, asList(arguments));
            failure = new IllegalArgumentException(errorMessage, e);
        } catch (InvocationTargetException e) {
            String errorMessage = format("It's failed to invoke the method[signature : '{}' , instance : {} , arguments : {}]", getSignature(method), instance, asList(arguments));
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
    public static Method findOverriddenMethod(Method overrider, Class<?> declaringClass) {
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
