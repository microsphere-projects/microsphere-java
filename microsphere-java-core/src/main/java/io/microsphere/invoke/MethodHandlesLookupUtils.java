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
package io.microsphere.invoke;

import io.microsphere.lang.function.ThrowableBiFunction;
import io.microsphere.logging.Logger;
import io.microsphere.util.BaseUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import static io.microsphere.lang.function.ThrowableBiFunction.execute;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * The utilities class for {@link MethodHandles.Lookup}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class MethodHandlesLookupUtils extends BaseUtils {

    /**
     * {@link MethodHandle} for Not-Found
     */
    public static final MethodHandle NOT_FOUND_METHOD_HANDLE = null;

    /**
     * The {@link MethodHandles.Lookup} for {@link MethodHandles#publicLookup()}
     */
    public static final MethodHandles.Lookup PUBLIC_LOOKUP = publicLookup();

    /**
     * The convenient method to find {@link MethodHandles.Lookup#findVirtual(Class, String, MethodType)} for public method
     *
     * @param requestedClass the class to be looked up
     * @param methodName     the target method name
     * @param parameterTypes the types of target method parameters
     * @return {@link MethodHandle}
     */
    public static MethodHandle findPublicVirtual(Class<?> requestedClass, String methodName, Class... parameterTypes) {
        return findPublic(requestedClass, methodName, parameterTypes, (lookup, methodType) -> lookup.findVirtual(requestedClass, methodName, methodType));
    }

    /**
     * The convenient method to find {@link MethodHandles.Lookup#findStatic(Class, String, MethodType)} for public static method
     *
     * @param requestedClass the class to be looked up
     * @param methodName     the target method name
     * @param parameterTypes the types of target method parameters
     * @return {@link MethodHandle}
     */
    public static MethodHandle findPublicStatic(Class<?> requestedClass, String methodName, Class... parameterTypes) {
        return findPublic(requestedClass, methodName, parameterTypes, (lookup, methodType) -> lookup.findStatic(requestedClass, methodName, methodType));
    }

    protected static MethodHandle findPublic(Class<?> requestedClass, String methodName, Class[] parameterTypes,
                                             ThrowableBiFunction<MethodHandles.Lookup, MethodType, MethodHandle> function) {
        return find(PUBLIC_LOOKUP, requestedClass, methodName, parameterTypes, function);
    }

    protected static MethodHandle find(MethodHandles.Lookup lookup, Class<?> requestedClass, String methodName, Class[] parameterTypes,
                                       ThrowableBiFunction<MethodHandles.Lookup, MethodType, MethodHandle> function) {
        Method method = findMethod(requestedClass, methodName, parameterTypes);
        return find(lookup, method, function);
    }

    protected static MethodHandle findPublic(Method method, ThrowableBiFunction<MethodHandles.Lookup, MethodType, MethodHandle> function) {
        return find(PUBLIC_LOOKUP, method, function);
    }

    protected static MethodHandle find(MethodHandles.Lookup lookup, Method method,
                                       ThrowableBiFunction<MethodHandles.Lookup, MethodType, MethodHandle> function) {
        if (method == null) {
            return NOT_FOUND_METHOD_HANDLE;
        }
        Class[] parameterTypes = method.getParameterTypes();
        Class<?> returnType = method.getReturnType();
        MethodType methodType = isEmpty(parameterTypes) ? methodType(returnType) : methodType(returnType, parameterTypes);
        return execute(lookup, methodType, function, (l, mt, e) -> {
            Logger logger = getLogger(MethodHandlesLookupUtils.class);
            if (logger.isWarnEnabled()) {
                logger.warn("The MethodHandle can't be found by Lookup[{}] on the method : {}", l, method, e);
            }
            return null;
        });
    }

    private MethodHandlesLookupUtils() {
    }
}
