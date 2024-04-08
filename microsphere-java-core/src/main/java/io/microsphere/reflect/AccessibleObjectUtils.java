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

import io.microsphere.lang.function.ThrowableConsumer;
import io.microsphere.lang.function.ThrowableFunction;
import io.microsphere.lang.function.ThrowableSupplier;
import io.microsphere.util.BaseUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * The utilities class of {@link AccessibleObject}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AccessibleObject
 * @since 1.0.0
 */
public abstract class AccessibleObjectUtils extends BaseUtils {

    /**
     * Execute an {@link AccessibleObject} instance
     *
     * @param object   {@link AccessibleObject} instance, {@link Field}, {@link Method} or {@link Constructor}
     * @param callback the call back to execute {@link AccessibleObject} object
     * @param <A>      The type or subtype of {@link AccessibleObject}
     */
    public static <A extends AccessibleObject> void execute(A object, ThrowableConsumer<A> callback) {
        execute(object, a -> {
            callback.accept(a);
            return null;
        });
    }

    /**
     * Executes the {@link AccessibleObject}
     *
     * @param accessibleObject {@link AccessibleObject}
     * @param supplier         {@link ThrowableConsumer}
     * @throws RuntimeException if execution failed
     */
    public static <A extends AccessibleObject, R> R execute(A accessibleObject, ThrowableSupplier<R> supplier) {
        return execute(accessibleObject, (ThrowableFunction<A, R>) a -> supplier.execute());
    }

    /**
     * Execute an {@link AccessibleObject} instance
     *
     * @param accessibleObject {@link AccessibleObject} instance, {@link Field}, {@link Method} or {@link Constructor}
     * @param callback         the call back to execute {@link AccessibleObject} accessibleObject
     * @param <A>              The type or subtype of {@link AccessibleObject}
     * @param <R>              The type of execution result
     * @return The execution result
     * @throws NullPointerException If <code>accessibleObject</code> is <code>null</code>
     */
    public static <A extends AccessibleObject, R> R execute(A accessibleObject, ThrowableFunction<A, R> callback) throws NullPointerException {
        boolean accessible = accessibleObject.isAccessible();
        final R result;
        try {
            if (!accessible) {
                accessibleObject.setAccessible(true);
            }
            result = callback.execute(accessibleObject);
        } finally {
            if (!accessible) {
                accessibleObject.setAccessible(accessible);
            }
        }
        return result;
    }
}
