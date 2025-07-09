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

import io.microsphere.util.Utils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import static io.microsphere.reflect.ConstructorUtils.hasNonPrivateConstructorWithoutParameters;
import static io.microsphere.reflect.MethodUtils.FINAL_METHOD_PREDICATE;
import static io.microsphere.reflect.MethodUtils.NON_PRIVATE_METHOD_PREDICATE;
import static io.microsphere.reflect.MethodUtils.NON_STATIC_METHOD_PREDICATE;
import static io.microsphere.reflect.MethodUtils.OBJECT_METHOD_PREDICATE;
import static io.microsphere.reflect.MethodUtils.findAllDeclaredMethods;
import static io.microsphere.util.ClassUtils.isArray;
import static io.microsphere.util.ClassUtils.isPrimitive;
import static java.lang.reflect.Modifier.isFinal;


/**
 * The utilities class for {@link Proxy Proxy}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class ProxyUtils implements Utils {

    /**
     * Determines whether the specified class is proxyable.
     *
     * <p>A class is considered proxyable if:
     * <ul>
     *     <li>It has a non-private constructor with no parameters</li>
     *     <li>It is not declared final</li>
     *     <li>It does not have non-static, final methods with public, protected, or default visibility</li>
     *     <li>It is not a primitive type</li>
     *     <li>It is not an array type</li>
     * </ul>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class ExampleClass {
     *     public void exampleMethod() {}
     * }
     *
     * boolean result = ProxyUtils.isProxyable(ExampleClass.class);
     * // result will be true as ExampleClass meets all proxyable conditions.
     *
     * public final class FinalClass {
     *     public void method() {}
     * }
     *
     * result = ProxyUtils.isProxyable(FinalClass.class);
     * // result will be false as FinalClass is declared final.
     * }</pre>
     *
     * @param type the class to check for proxyability
     * @return {@code true} if the class is proxyable, otherwise {@code false}
     */
    public static boolean isProxyable(Class<?> type) {
        if (isArray(type)) {
            return false;
        }

        if (isPrimitive(type)) {
            return false;
        }

        if (isFinal(type.getModifiers())) {
            return false;
        }

        if (!hasNonPrivateConstructorWithoutParameters(type)) {
            return false;
        }

        List<Method> methods = findAllDeclaredMethods(type,
                NON_STATIC_METHOD_PREDICATE,
                FINAL_METHOD_PREDICATE,
                NON_PRIVATE_METHOD_PREDICATE,
                OBJECT_METHOD_PREDICATE.negate());

        return methods.isEmpty();
    }

    private ProxyUtils() {
    }
}
