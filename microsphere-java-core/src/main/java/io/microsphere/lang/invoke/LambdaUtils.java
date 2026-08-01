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

import io.microsphere.util.Utils;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.internal.reflect.ConstantPoolUtils.getMethodAt;
import static io.microsphere.internal.reflect.ConstantPoolUtils.getSize;
import static io.microsphere.reflect.MethodUtils.findFunctionalInterfaceMethod;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
import static io.microsphere.util.ClassUtils.isLambdaClass;
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

    public static List<Class<?>> resolveParameterTypes(Class<?> type, Class<?> functionalInterface) {
        if (!isLambdaClass(type)) {
            return emptyList();
        }

        Method functionalInterfaceMethod = findFunctionalInterfaceMethod(functionalInterface);
        if (functionalInterfaceMethod == null) {
            return emptyList();
        }

        if (!functionalInterface.isAssignableFrom(type)) {
            return emptyList();
        }

        Class<?>[] declaredParameterTypes = functionalInterfaceMethod.getParameterTypes();
        List<Class<?>> actualParameterTypes = emptyList();


        int size = getSize(type);
        for (int i = size - 1; i >= 0; i--) {
            Member member = getMethodAt(type, i);
            if (member instanceof Method) {
                Method method = (Method) member;
                Class<?>[] resolvedParameterTypes = resolveParameterTypes(method, declaredParameterTypes);
                if (resolvedParameterTypes != EMPTY_CLASS_ARRAY) {
                    actualParameterTypes = ofList(resolvedParameterTypes);
                    break;
                }
            }
        }

        return actualParameterTypes;
    }

    private static Class<?>[] resolveParameterTypes(Method method, Class<?>[] declaredParameterTypes) {
        Class<?>[] actualParameterTypes = method.getParameterTypes();
        int actualLength = actualParameterTypes.length;
        int length = declaredParameterTypes.length;
        if (actualLength < length) {
            return EMPTY_CLASS_ARRAY;
        }

        Class<?>[] matchedParameterTypes = new Class<?>[length];

        for (int i = 0; i < length; i++) {
            Class<?> declaredParameterType = declaredParameterTypes[length - 1 + i];
            Class<?> actualParameterType = actualParameterTypes[actualLength - 1 + i];
            if (!declaredParameterType.isAssignableFrom(actualParameterType)) {
                return EMPTY_CLASS_ARRAY;
            }
            matchedParameterTypes[i] = actualParameterType;
        }

        return matchedParameterTypes;
    }

    private LambdaUtils() {
    }
}