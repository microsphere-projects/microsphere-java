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
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.microsphere.invoke.MethodHandleUtils.findVirtual;
import static io.microsphere.reflect.MethodUtils.findFunctionalInterfaceMethod;
import static io.microsphere.util.ArrayUtils.combine;
import static java.lang.invoke.LambdaMetafactory.metafactory;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

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
     * Creates a {@link Function} lambda instance for the specified target class, and method.
     *
     * @param targetClass the target class containing the method to be invoked
     * @param methodName  the name of the method to be invoked
     * @param <T>         the type of the function input
     * @param <R>         the return type of the function
     * @return a {@link Function} lambda instance
     * @throws Throwable if an error occurs during lambda creation
     */
    public static <T, R> Function<T, R> function(Class<T> functionInputType, Class<T> targetClass, String methodName) throws Throwable {
        return lambda(Function.class, functionInputType, targetClass, methodName);
    }

    /**
     * Creates a {@link Consumer} lambda instance for the specified target class, and method.
     *
     * @param targetClass the target class containing the method to be invoked
     * @param methodName  the name of the method to be invoked
     * @param <T>         the type of the consumer input
     * @return a {@link Consumer} lambda instance
     * @throws Throwable if an error occurs during lambda creation
     */
    public static <T> Consumer<T> consumer(Class<T> functionInputType, Class<T> targetClass, String methodName) throws Throwable {
        return lambda(Consumer.class, functionInputType, targetClass, methodName);
    }

    /**
     * Creates a lambda instance for the specified functional interface, target class, and method.
     *
     * @param functionalInterface the functional interface class
     * @param targetClass         the target class containing the method to be invoked
     * @param methodName          the name of the method to be invoked
     * @param parameterTypes      the types of the method parameters
     * @param <F>                 the type of the functional interface
     * @return a lambda instance implementing the specified functional interface
     * @throws Throwable if an error occurs during lambda creation
     */
    public static <T, F> F lambda(Class<F> functionalInterface, Class<T> functionInputType,
                                  Class<T> targetClass, String methodName, Class<?>... parameterTypes) throws Throwable {
        // 1. Set up the target method lookup context
        Lookup lookup = lookup();

        // 2. Locate the real method we want the lambda to execute
        MethodHandle implementationHandle = findVirtual(targetClass, methodName, parameterTypes);

        Method functionalInterfaceMethod = findFunctionalInterfaceMethod(functionalInterface);
        Class<?> functionalInterfaceReturnType = functionalInterfaceMethod.getReturnType();
        Class<?>[] functionalInterfaceMethodParameterTypes = functionalInterfaceMethod.getParameterTypes();

        // 3. Define the signatures required by LambdaMetafactory
        // The type signature of the functional interface's abstract method (erased type)
        MethodType samMethodType = methodType(functionalInterfaceReturnType, functionalInterfaceMethodParameterTypes);

        // The type signature of the functional interface's abstract method (instantiated type)
        Class<?>[] instantiatedMethodParameterTypes = combine(functionInputType, parameterTypes);
        MethodType instantiatedMethodType = methodType(functionalInterfaceReturnType, instantiatedMethodParameterTypes);

        // 4. Invoke LambdaMetafactory to spin the runtime lambda class
        CallSite callSite = metafactory(
                lookup,                                     // Caller lookup context
                functionalInterfaceMethod.getName(),        // Name of the interface method to implement
                methodType(functionalInterface),            // Factory signature (returns the functional interface)
                samMethodType,                              // Type signature of the SAM (Single Abstract Method)
                implementationHandle,                       // Direct method handle to the actual implementation
                instantiatedMethodType                      // Specific enforced runtime signature
        );

        // 5. Extract the factory and generate the lambda instance
        MethodHandle factory = callSite.getTarget();
        return (F) factory.invoke();
    }

    private LambdaUtils() {
    }
}