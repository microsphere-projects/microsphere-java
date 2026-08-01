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
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.function.Function;

import static io.microsphere.lang.invoke.LambdaUtils.resolveParameterTypes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link LambdaUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LambdaUtils
 * @since 1.0.0
 */
public class LambdaUtilsTest {

    @Test
    void testDeduceGenericTypes() throws Throwable {

        String p0 = "0";
        Integer p1 = 1;

        EventListener<EchoEvent> e = event -> {
        };

        EventListener<FileChangedEvent> fce = event -> {
            assertNotNull(p0);
            assertNotNull(p1);
        };

        List<Class<?>> parameterTypes = resolveParameterTypes(e.getClass(), EventListener.class);
        assertEquals(1, parameterTypes.size());
        assertEquals(EchoEvent.class, parameterTypes.get(0));

        parameterTypes = resolveParameterTypes(fce.getClass(), EventListener.class);
        assertEquals(1, parameterTypes.size());
        assertEquals(FileChangedEvent.class, parameterTypes.get(0));

//        Function<String, String> upperLambda = toUpperLambda();
//        parameterTypes = resolveParameterTypes(upperLambda.getClass(), Function.class);
//        assertEquals(1, parameterTypes.size());
//        assertEquals(String.class, parameterTypes.get(0));
    }

    Function<String, String> toUpperLambda() throws Throwable {
        // 1. Set up the target method lookup context
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // 2. Locate the real method we want the lambda to execute
        // We are targeting: String.toUpperCase()
        MethodType targetMethodType = MethodType.methodType(String.class);
        MethodHandle implementationHandle = lookup.findVirtual(
                String.class,
                "toUpperCase",
                targetMethodType
        );

        // 3. Define the signatures required by LambdaMetafactory

        // The type signature of the functional interface's abstract method (erased type)
        // Function.apply(Object) -> Object
        MethodType samMethodType = MethodType.methodType(Object.class, Object.class);

        // The type signature of the functional interface's abstract method (instantiated type)
        // Function.apply(String) -> String
        MethodType instantiatedMethodType = MethodType.methodType(String.class, String.class);

        // 4. Invoke LambdaMetafactory to spin the runtime lambda class
        CallSite callSite = LambdaMetafactory.metafactory(
                lookup,                                     // Caller lookup context
                "apply",                                    // Name of the interface method to implement
                MethodType.methodType(Function.class),      // Factory signature (returns the functional interface)
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
}