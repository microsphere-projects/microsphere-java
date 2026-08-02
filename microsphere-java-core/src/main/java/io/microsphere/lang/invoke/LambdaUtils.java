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

package io.microsphere.lang.invoke;

import io.microsphere.annotation.Nullable;
import io.microsphere.util.ClassUtils;
import io.microsphere.util.Utils;

import java.io.Serializable;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.internal.reflect.ConstantPoolUtils.getMethodAt;
import static io.microsphere.internal.reflect.ConstantPoolUtils.getSize;
import static io.microsphere.reflect.MemberUtils.isStatic;
import static io.microsphere.reflect.MethodUtils.findFunctionalInterfaceMethod;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
import static io.microsphere.util.ClassUtils.findAllInterfaces;
import static io.microsphere.util.ClassUtils.getType;
import static io.microsphere.util.ClassUtils.isLambdaClass;
import static io.microsphere.util.ClassUtils.resolvePrimitiveType;
import static io.microsphere.util.ClassUtils.tryResolveWrapperType;
import static java.util.Collections.emptyList;

/**
 * The utility class for Java Lambda
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LambdaMetafactory
 * @see CallSite
 * @see MethodHandles
 * @since 1.0.0
 */
public abstract class LambdaUtils implements Utils {

    /**
     * Resolve the parameter types of the lambda method
     *
     * @param object the lambda object
     * @return the parameter types of the lambda method
     */
    public static List<Class<?>> resolveLambdaMethodParameterTypes(@Nullable Object object) {
        return resolveLambdaMethodParameterTypes(getType(object));
    }

    /**
     * Resolve the parameter types of the lambda method
     *
     * @param type the lambda class type
     * @return the parameter types of the lambda method
     */
    public static List<Class<?>> resolveLambdaMethodParameterTypes(@Nullable Class<?> type) {
        List<Class<?>> allInterfaces = findAllInterfaces(type, ClassUtils::isFunctionalInterface);
        if (allInterfaces.size() == 1) {
            Class<?> functionalInterface = allInterfaces.get(0);
            return resolveLambdaMethodParameterTypes(type, functionalInterface);
        }
        return emptyList();
    }

    /**
     * Resolve the parameter types of the lambda method
     *
     * @param object              the lambda object
     * @param functionalInterface the functional interface type
     * @return the parameter types of the lambda method
     */
    public static List<Class<?>> resolveLambdaMethodParameterTypes(@Nullable Object object, @Nullable Class<?> functionalInterface) {
        List<Class<?>> parameterTypes = resolveLambdaMethodParameterTypes(getType(object), functionalInterface);
        if (parameterTypes.isEmpty() && object instanceof Serializable && isLambdaClass(getType(object))) {
            parameterTypes = resolveSerializableLambdaParameterTypes((Serializable) object, functionalInterface);
        }
        return parameterTypes;
    }

    /**
     * Resolve the parameter types of the lambda method
     *
     * @param type                the lambda class type
     * @param functionalInterface the functional interface type
     * @return the parameter types of the lambda method
     */
    public static List<Class<?>> resolveLambdaMethodParameterTypes(@Nullable Class<?> type, @Nullable Class<?> functionalInterface) {
        if (!isLambdaClass(type)) {
            return emptyList();
        }

        Method declaredMethod = findFunctionalInterfaceMethod(functionalInterface);
        if (declaredMethod == null) {
            return emptyList();
        }

        if (!functionalInterface.isAssignableFrom(type)) {
            return emptyList();
        }

        Class<?>[] declaredParameterTypes = declaredMethod.getParameterTypes();
        List<Class<?>> actualParameterTypes = emptyList();

        int size = getSize(type);
        for (int i = size - 1; i > -1; i--) {
            Member member = getMethodAt(type, i);
            if (member instanceof Method) {
                Method actualMethod = (Method) member;
                Class<?>[] resolvedParameterTypes = resolveParameterTypes(actualMethod, declaredParameterTypes);
                if (resolvedParameterTypes != EMPTY_CLASS_ARRAY) {
                    actualParameterTypes = ofList(resolvedParameterTypes);
                    break;
                }
            }
        }

        return actualParameterTypes;
    }

    static Class<?>[] resolveParameterTypes(Method actualMethod, Class<?>... declaredParameterTypes) {
        Class<?>[] actualParameterTypes = actualMethod.getParameterTypes();
        int actualLength = actualParameterTypes.length;
        int length = declaredParameterTypes.length;
        Class<?>[] matchedParameterTypes = new Class<?>[length];
        if (actualLength < length) {
            if (isStatic(actualMethod)) {
                // static method must have the same length of parameters with the functional interface method
                return EMPTY_CLASS_ARRAY;
            }

            matchedParameterTypes[0] = actualMethod.getDeclaringClass();
            for (int i = 1; i < length; i++) {
                matchedParameterTypes[i] = tryResolveWrapperType(actualParameterTypes[i - 1]);
            }

        } else {
            for (int i = 0; i < length; i++) {
                int index = length - 1 - i;
                Class<?> declaredParameterType = declaredParameterTypes[index];
                Class<?> actualParameterType = actualParameterTypes[actualLength - 1 - i];
                if (declaredParameterType.isPrimitive()) {
                    actualParameterType = resolvePrimitiveType(actualParameterType);
                    if (declaredParameterType != actualParameterType) {
                        return EMPTY_CLASS_ARRAY;
                    }
                } else {
                    actualParameterType = tryResolveWrapperType(actualParameterType);
                    if (!declaredParameterType.isAssignableFrom(actualParameterType)) {
                        return EMPTY_CLASS_ARRAY;
                    }
                }

                matchedParameterTypes[index] = actualParameterType;
            }
        }
        return matchedParameterTypes;
    }

    /**
     * Resolve the parameter types of a serializable lambda using {@link SerializedLambda}.
     * <p>
     * This method provides a reliable alternative to constant-pool inspection for lambda types
     * that implement {@link Serializable}. It invokes the lambda's {@code writeReplace()} method
     * to obtain a {@link SerializedLambda} descriptor containing the instantiated method type,
     * from which the exact parameter types (including erased generic types) can be recovered.
     * </p>
     *
     * @param lambda              the serializable lambda object
     * @param functionalInterface the functional interface type
     * @return the parameter types of the lambda method, or an empty list if they cannot be resolved
     */
    private static List<Class<?>> resolveSerializableLambdaParameterTypes(Serializable lambda, Class<?> functionalInterface) {
        Method functionalInterfaceMethod = findFunctionalInterfaceMethod(functionalInterface);
        if (functionalInterfaceMethod == null) {
            return emptyList();
        }
        try {
            Method writeReplace = lambda.getClass().getDeclaredMethod("writeReplace");
            SerializedLambda sl = (SerializedLambda) writeReplace.invoke(lambda);
            String instantiatedMethodType = sl.getInstantiatedMethodType();
            ClassLoader classLoader = lambda.getClass().getClassLoader();
            MethodType methodType = MethodType.fromMethodDescriptorString(instantiatedMethodType, classLoader);
            List<Class<?>> parameterTypes = methodType.parameterList();
            if (parameterTypes.size() == functionalInterfaceMethod.getParameterCount()) {
                return parameterTypes;
            }
        } catch (Exception e) {
            // silently ignore if SerializedLambda cannot be obtained
        }
        return emptyList();
    }

    private LambdaUtils() {
    }
}