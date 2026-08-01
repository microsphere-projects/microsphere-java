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


import io.microsphere.event.EchoEvent;
import io.microsphere.event.EventListener;
import io.microsphere.io.event.FileChangedEvent;
import org.junit.jupiter.api.Test;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.ToLongBiFunction;

import static io.microsphere.lang.invoke.LambdaUtils.resolveLambdaMethodParameterTypes;
import static io.microsphere.lang.invoke.LambdaUtils.resolveParameterTypes;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
import static java.lang.Double.valueOf;
import static java.lang.invoke.LambdaMetafactory.metafactory;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link LambdaUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LambdaUtils
 * @since 1.0.0
 */
public class LambdaUtilsTest {

    @Test
    void testResolveLambdaMethodParameterTypesOnCompiledLambda() {

        String p0 = "0";
        Integer p1 = 1;

        EventListener<EchoEvent> e = event -> {
        };

        List<Class<?>> parameterTypes = resolveLambdaMethodParameterTypes(e, EventListener.class);
        assertEquals(1, parameterTypes.size());
        assertEquals(EchoEvent.class, parameterTypes.get(0));

        // e instance implements two interfaces of @FunctionalInterface :  EventListener and Prioritized
        parameterTypes = resolveLambdaMethodParameterTypes(e);
        assertTrue(parameterTypes.isEmpty());

        EventListener<FileChangedEvent> fce = event -> {
            assertNotNull(p0);
            assertNotNull(p1);
        };

        parameterTypes = resolveLambdaMethodParameterTypes(fce, EventListener.class);
        assertEquals(1, parameterTypes.size());
        assertEquals(FileChangedEvent.class, parameterTypes.get(0));

        LongFunction<Long> lf = v -> v;
        parameterTypes = resolveLambdaMethodParameterTypes(lf, LongFunction.class);
        assertEquals(1, parameterTypes.size());
        assertEquals(long.class, parameterTypes.get(0));

        parameterTypes = resolveLambdaMethodParameterTypes(lf);
        assertEquals(1, parameterTypes.size());
        assertEquals(long.class, parameterTypes.get(0));

        DoubleFunction<Long> df = v -> valueOf(v).longValue();
        parameterTypes = resolveLambdaMethodParameterTypes(df, DoubleFunction.class);
        assertEquals(1, parameterTypes.size());
        assertEquals(double.class, parameterTypes.get(0));

        ToLongBiFunction<String, Long> tl = (s, l) -> p1.longValue();
        parameterTypes = resolveLambdaMethodParameterTypes(tl, ToLongBiFunction.class);
        assertEquals(2, parameterTypes.size());
        assertEquals(String.class, parameterTypes.get(0));
        assertEquals(Long.class, parameterTypes.get(1));
    }

    @Test
    void testResolveLambdaMethodParameterTypesOnLambdaFactory() throws Throwable {
        Function<String, String> upperLambda = toUpperLambda();
        List<Class<?>> parameterTypes = resolveLambdaMethodParameterTypes(upperLambda, Function.class);
        assertEquals(1, parameterTypes.size());
        assertEquals(String.class, parameterTypes.get(0));

        BiFunction<Integer, Integer, Integer> sumLambda = sumLambda();
        parameterTypes = resolveLambdaMethodParameterTypes(sumLambda, BiFunction.class);
        assertEquals(2, parameterTypes.size());
        assertEquals(Integer.class, parameterTypes.get(0));
        assertEquals(Integer.class, parameterTypes.get(1));

        TriFunction<Integer, Integer, Integer, Integer> volumeLambda = volumeLambda();
        parameterTypes = resolveLambdaMethodParameterTypes(volumeLambda, TriFunction.class);
        assertEquals(3, parameterTypes.size());
        assertEquals(Integer.class, parameterTypes.get(0));
        assertEquals(Integer.class, parameterTypes.get(1));
        assertEquals(Integer.class, parameterTypes.get(2));

        TriFunction<String, CharSequence, CharSequence, String> replaceLambda = replaceLambda();
        parameterTypes = resolveLambdaMethodParameterTypes(replaceLambda, TriFunction.class);
        assertEquals(3, parameterTypes.size());
        assertEquals(String.class, parameterTypes.get(0));
        assertEquals(CharSequence.class, parameterTypes.get(1));
        assertEquals(CharSequence.class, parameterTypes.get(2));

        parameterTypes = resolveLambdaMethodParameterTypes(volumeLambda, Function.class);
        assertTrue(parameterTypes.isEmpty());

        parameterTypes = resolveLambdaMethodParameterTypes(volumeLambda, BiFunction.class);
        assertTrue(parameterTypes.isEmpty());

        parameterTypes = resolveLambdaMethodParameterTypes(volumeLambda, String.class);
        assertTrue(parameterTypes.isEmpty());

        parameterTypes = resolveLambdaMethodParameterTypes(BiFunction.class, String.class);
        assertTrue(parameterTypes.isEmpty());
    }

    @Test
    void testResolveParameterTypesOnMismatch() {
        Method method = findMethod(String.class, "valueOf", int.class);
        assertSame(EMPTY_CLASS_ARRAY, resolveParameterTypes(method, Long.class));

        method = findMethod(String.class, "valueOf", Object.class);
        assertSame(EMPTY_CLASS_ARRAY, resolveParameterTypes(method, int.class));
    }

    Function<String, String> toUpperLambda() throws Throwable {
        // 1. Set up the target method lookup context
        MethodHandles.Lookup lookup = lookup();

        // 2. Locate the real method we want the lambda to execute
        // We are targeting: String.toUpperCase()
        MethodType targetMethodType = methodType(String.class);
        MethodHandle implementationHandle = lookup.findVirtual(
                String.class,
                "toUpperCase",
                targetMethodType
        );

        // 3. Define the signatures required by LambdaMetafactory

        // The type signature of the functional interface's abstract method (erased type)
        // Function.apply(Object) -> Object
        MethodType samMethodType = methodType(Object.class, Object.class);

        // The type signature of the functional interface's abstract method (instantiated type)
        // Function.apply(String) -> String
        MethodType instantiatedMethodType = methodType(String.class, String.class);

        // 4. Invoke LambdaMetafactory to spin the runtime lambda class
        CallSite callSite = metafactory(
                lookup,                                     // Caller lookup context
                "apply",                                    // Name of the interface method to implement
                methodType(Function.class),                 // Factory signature (returns the functional interface)
                samMethodType,                              // Type signature of the SAM (Single Abstract Method)
                implementationHandle,                       // Direct method handle to the actual implementation
                instantiatedMethodType                      // Specific enforced runtime signature
        );

        // 5. Extract the factory and generate the lambda instance
        MethodHandle factory = callSite.getTarget();
        @SuppressWarnings("unchecked")
        Function<String, String> toUpperLambda = (Function<String, String>) factory.invokeExact();
        return toUpperLambda;
    }

    BiFunction<Integer, Integer, Integer> sumLambda() throws Throwable {
        // 1. Set up the target method lookup context
        MethodHandles.Lookup lookup = lookup();

        // 2. Locate the real method we want the lambda to execute
        // We are targeting the static method: Integer.sum(int, int)
        MethodType targetMethodType = methodType(int.class, int.class, int.class);
        MethodHandle implementationHandle = lookup.findStatic(
                Integer.class,
                "sum",
                targetMethodType
        );

        // 3. Define the signatures required by LambdaMetafactory

        // The type signature of the functional interface's abstract method (erased type)
        // BiFunction.apply(Object, Object) -> Object
        MethodType samMethodType = methodType(Object.class, Object.class, Object.class);

        // The type signature of the functional interface's abstract method (instantiated type)
        // BiFunction.apply(Integer, Integer) -> Integer
        MethodType instantiatedMethodType = methodType(Integer.class, Integer.class, Integer.class);

        // 4. Invoke LambdaMetafactory to spin the runtime lambda class
        CallSite callSite = metafactory(
                lookup,                                         // Caller lookup context
                "apply",                                        // Name of the interface method to implement
                methodType(BiFunction.class),                   // Factory signature (returns the functional interface)
                samMethodType,                                  // Type signature of the SAM (Single Abstract Method)
                implementationHandle,                           // Direct method handle to the actual implementation
                instantiatedMethodType                          // Specific enforced runtime signature
        );

        // 5. Extract the factory and generate the BiFunction lambda instance
        MethodHandle factory = callSite.getTarget();
        @SuppressWarnings("unchecked")
        BiFunction<Integer, Integer, Integer> sumLambda = (BiFunction<Integer, Integer, Integer>) factory.invokeExact();
        return sumLambda;
    }

    // Define our custom functional interface
    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }

    // The target method we want the lambda to execute
    public static int calculateVolume(int length, int width, int height) {
        return length * width * height;
    }

    TriFunction<Integer, Integer, Integer, Integer> volumeLambda() throws Throwable {
        // 1. Set up the target method lookup context
        MethodHandles.Lookup lookup = lookup();

        // 2. Locate the real static method with 3 parameters
        MethodType targetMethodType = methodType(int.class, int.class, int.class, int.class);
        MethodHandle implementationHandle = lookup.findStatic(
                LambdaUtilsTest.class,
                "calculateVolume",
                targetMethodType
        );

        // 3. Define the signatures required by LambdaMetafactory

        // The type signature of the SAM (Single Abstract Method) after type erasure.
        // TriFunction.apply(Object, Object, Object) -> Object
        MethodType samMethodType = methodType(
                Object.class,  // Return type
                Object.class, Object.class, Object.class // 3 Input types
        );

        // The exact generic type signature enforced at runtime.
        // TriFunction.apply(Integer, Integer, Integer) -> Integer
        MethodType instantiatedMethodType = methodType(
                Integer.class, // Return type
                Integer.class, Integer.class, Integer.class // 3 Input types
        );

        // 4. Invoke LambdaMetafactory to spin the runtime lambda class
        CallSite callSite = metafactory(
                lookup,                                         // Caller lookup context
                "apply",                                        // Name of the interface method to implement
                methodType(TriFunction.class),                  // Factory signature (returns our TriFunction)
                samMethodType,                                  // Type signature of the SAM
                implementationHandle,                           // Direct method handle to the actual implementation
                instantiatedMethodType                          // Enforced runtime signature
        );

        // 5. Extract the factory and generate the TriFunction lambda instance
        MethodHandle factory = callSite.getTarget();
        @SuppressWarnings("unchecked")
        TriFunction<Integer, Integer, Integer, Integer> volumeLambda =
                (TriFunction<Integer, Integer, Integer, Integer>) factory.invokeExact();
        return volumeLambda;
    }

    TriFunction<String, CharSequence, CharSequence, String> replaceLambda() throws Throwable {
        // 2. Set up the target method lookup context
        MethodHandles.Lookup lookup = lookup();

        // 3. Locate the non-static instance method using findVirtual
        // String.replace(CharSequence, CharSequence) returns String
        MethodType targetMethodType = methodType(String.class, CharSequence.class, CharSequence.class);
        MethodHandle implementationHandle = lookup.findVirtual(
                String.class,
                "replace",
                targetMethodType
        );

        // 4. Define the signatures required by LambdaMetafactory

        // After type erasure, all generics become Object.
        // TriFunction.apply(Object, Object, Object) -> Object
        MethodType samMethodType = methodType(
                Object.class,
                Object.class, Object.class, Object.class
        );

        // Enforced runtime types.
        // TriFunction.apply(String, CharSequence, CharSequence) -> String
        // Note: The first parameter is the instance type (String)!
        MethodType instantiatedMethodType = methodType(
                String.class,
                String.class, CharSequence.class, CharSequence.class
        );

        // 5. Invoke LambdaMetafactory to spin the runtime lambda class
        CallSite callSite = metafactory(
                lookup,                                         // Caller lookup context
                "apply",                                        // Name of the interface method to implement
                methodType(TriFunction.class),                  // Factory signature (returns our TriFunction)
                samMethodType,                                  // Type signature of the SAM
                implementationHandle,                           // Direct method handle to the instance method
                instantiatedMethodType                          // Enforced runtime signature
        );

        // 6. Extract the factory and generate the TriFunction lambda instance
        MethodHandle factory = callSite.getTarget();
        @SuppressWarnings("unchecked")
        TriFunction<String, CharSequence, CharSequence, String> replaceLambda =
                (TriFunction<String, CharSequence, CharSequence, String>) factory.invokeExact();
        return replaceLambda;
    }
}