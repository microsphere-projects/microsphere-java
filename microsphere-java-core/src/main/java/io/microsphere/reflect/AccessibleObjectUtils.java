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
import io.microsphere.logging.Logger;
import io.microsphere.util.BaseUtils;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import static io.microsphere.invoke.MethodHandleUtils.findVirtual;
import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * The utilities class of {@link AccessibleObject}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AccessibleObject
 * @since 1.0.0
 */
public abstract class AccessibleObjectUtils extends BaseUtils {

    private static final Logger logger = getLogger(AccessibleObjectUtils.class);

    /**
     * The method name of {@link AccessibleObject#canAccess(Object)} since Java 9
     */
    private static final String canAccessMethodName = "canAccess";

    /**
     * The {@link MethodHandle} of {@link AccessibleObject#canAccess(Object)} since Java 9
     * if <code>canAccessMethodHandle == null</code>, it indicates the version of Java is less than 9
     */
    private static final MethodHandle canAccessMethodHandle = findVirtual(AccessibleObject.class, canAccessMethodName, Object.class);

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

    /**
     * Test if the caller can access this reflected object. If this reflected
     * object corresponds to an instance method or field then this method tests
     * if the caller can access the given {@code obj} with the reflected object.
     * For instance methods or fields then the {@code obj} argument must be an
     * instance of the {@link Member#getDeclaringClass() declaring class}. For
     * static members and constructors then {@code obj} must be {@code null}.
     *
     * @param object an instance object of the declaring class of this reflected object if it is an instance method or field
     * @return {@code true} if the caller can access this reflected object.
     */
    public static boolean canAccess(Object object, AccessibleObject accessibleObject) {

        Boolean access = tryCanAccess(object, accessibleObject);

        return access == null ? accessibleObject.isAccessible() : access;
    }

    private static Boolean tryCanAccess(Object object, AccessibleObject accessibleObject) {
        Boolean access = null;
        if (canAccessMethodHandle != null) { // Java 9+
            try {
                access = (boolean) canAccessMethodHandle.invokeExact(accessibleObject, object);
            } catch (Throwable e) {
                logger.error("java.lang.reflect.AccessibleObject#canAccess(Object) can't be invoked, object : {} , accessible object : {}",
                        object, accessibleObject, e);
            }
        }
        return access;
    }
}
