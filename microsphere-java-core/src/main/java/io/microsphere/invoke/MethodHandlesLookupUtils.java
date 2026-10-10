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

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.lang.function.ThrowableBiFunction;
import io.microsphere.logging.Logger;
import io.microsphere.util.Utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import static io.microsphere.lang.function.ThrowableBiFunction.execute;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * Utilities class providing convenient methods for working with {@link Lookup}.
 *
 * <p>This class offers various static methods to simplify the process of obtaining and using
 * method handles, particularly for public virtual and static methods. It serves as a central
 * utility to reduce boilerplate code when dealing with reflection and method handle lookup.
 *
 * <h3>Example Usage</h3>
 * <ul>
 *     <li>Finding a public virtual method:
 *         <pre>{@code
 * MethodHandle mh = MethodHandlesLookupUtils.findPublicVirtual(String.class, "toString");
 * }</pre>
 *     </li>
 *     <li>Finding a public static method:
 *         <pre>{@code
 * MethodHandle mh = MethodHandlesLookupUtils.findPublicStatic(Math.class, "abs", int.class);
 * }</pre>
 *     </li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MethodHandles
 * @see MethodHandle
 * @since 1.0.0
 */
public abstract class MethodHandlesLookupUtils implements Utils {

    /**
     * {@link MethodHandle} for Not-Found
     */
    public static final MethodHandle NOT_FOUND_METHOD_HANDLE = null;

    /**
     * The {@link Lookup} for {@link MethodHandles#publicLookup()}
     */
    public static final Lookup PUBLIC_LOOKUP = publicLookup();

    /**
     * The convenient method to find {@link Lookup#findVirtual(Class, String, MethodType)} for public method
     *
     * @param requestedClass the class to be looked up
     * @param methodName     the target method name
     * @param parameterTypes the types of target method parameters
     * @return {@link MethodHandle}
     */
    @Nullable
    public static MethodHandle findPublicVirtual(@Nullable Class<?> requestedClass, @Nullable String methodName, Class... parameterTypes) {
        return findPublic(requestedClass, methodName, parameterTypes, (lookup, methodType) -> lookup.findVirtual(requestedClass, methodName, methodType));
    }

    /**
     * The convenient method to find {@link Lookup#findStatic(Class, String, MethodType)} for public static method
     *
     * @param requestedClass the class to be looked up
     * @param methodName     the target method name
     * @param parameterTypes the types of target method parameters
     * @return {@link MethodHandle}
     */
    @Nullable
    public static MethodHandle findPublicStatic(@Nullable Class<?> requestedClass, @Nullable String methodName, Class... parameterTypes) {
        return findPublic(requestedClass, methodName, parameterTypes, (lookup, methodType) -> lookup.findStatic(requestedClass, methodName, methodType));
    }

    @Nullable
    protected static MethodHandle findPublic(@Nullable Class<?> requestedClass, @Nullable String methodName, @Nullable Class[] parameterTypes,
                                             @Nonnull ThrowableBiFunction<Lookup, MethodType, MethodHandle> function) {
        return find(PUBLIC_LOOKUP, requestedClass, methodName, parameterTypes, function);
    }

    @Nullable
    protected static MethodHandle find(@Nullable Lookup lookup, @Nullable Class<?> requestedClass, @Nullable String methodName, @Nullable Class[] parameterTypes,
                                       @Nonnull ThrowableBiFunction<Lookup, MethodType, MethodHandle> function) {
        Method method = findMethod(requestedClass, methodName, parameterTypes);
        return find(lookup, method, function);
    }

    @Nullable
    protected static MethodHandle findPublic(@Nullable Method method, @Nonnull ThrowableBiFunction<Lookup, MethodType, MethodHandle> function) {
        return find(PUBLIC_LOOKUP, method, function);
    }

    @Nullable
    protected static MethodHandle find(@Nullable Lookup lookup, @Nullable Method method,
                                       @Nonnull ThrowableBiFunction<Lookup, MethodType, MethodHandle> function) {
        if (method == null) {
            return NOT_FOUND_METHOD_HANDLE;
        }
        Class[] parameterTypes = method.getParameterTypes();
        Class<?> returnType = method.getReturnType();
        MethodType methodType = isEmpty(parameterTypes) ? methodType(returnType) : methodType(returnType, parameterTypes);
        return execute(lookup, methodType, function, (l, mt, e) -> {
            Logger logger = getLogger(MethodHandlesLookupUtils.class);
            if (logger.isTraceEnabled()) {
                logger.trace("The MethodHandle can't be found by Lookup[{}] on the method : {}", l, method, e);
            }
            return null;
        });
    }

    private MethodHandlesLookupUtils() {
    }
}
