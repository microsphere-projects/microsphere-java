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
import io.microsphere.util.BaseUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.microsphere.invoke.MethodHandleUtils.LookupKey.buildKey;
import static io.microsphere.invoke.MethodHandleUtils.LookupMode.getModes;
import static io.microsphere.lang.function.ThrowableBiFunction.execute;
import static io.microsphere.reflect.ConstructorUtils.getDeclaredConstructor;
import static io.microsphere.reflect.ConstructorUtils.newInstance;
import static io.microsphere.reflect.MemberUtils.isPublic;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static java.lang.invoke.MethodHandles.Lookup.PACKAGE;
import static java.lang.invoke.MethodHandles.Lookup.PRIVATE;
import static java.lang.invoke.MethodHandles.Lookup.PROTECTED;
import static java.lang.invoke.MethodHandles.Lookup.PUBLIC;
import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * The utilities class for {@link MethodHandle}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class MethodHandleUtils extends BaseUtils {

    /**
     * A single-bit mask representing all accesses (public, private, protected and package)
     * The value, 0x0f, happens to be the same as the value of the modifier bit.
     */
    public static final int ALL_MODES = (PUBLIC | PRIVATE | PROTECTED | PACKAGE);

    private static final Constructor<MethodHandles.Lookup> lookupConstructor1 = getDeclaredConstructor(MethodHandles.Lookup.class, Class.class);

    private static final Constructor<MethodHandles.Lookup> lookupConstructor2 = getDeclaredConstructor(MethodHandles.Lookup.class, Class.class, int.class);

    private static final ConcurrentMap<LookupKey, MethodHandles.Lookup> lookupCache = new ConcurrentHashMap<>();

    /**
     * The allowed {@link MethodHandles.Lookup} modes enumeration
     *
     * @see MethodHandles.Lookup#PUBLIC
     * @see MethodHandles.Lookup#PRIVATE
     * @see MethodHandles.Lookup#PROTECTED
     * @see MethodHandles.Lookup#PACKAGE
     * @see MethodHandles.Lookup#TRUSTED
     * @see MethodHandles.Lookup#ALL_MODES
     */
    public static enum LookupMode {


        /**
         * A single-bit mask representing {@code public} access,
         * The value is {@code 0x01}, happens to be the same as the value of the
         * {@code public} {@linkplain java.lang.reflect.Modifier#PUBLIC modifier bit}.
         */
        PUBLIC(MethodHandles.Lookup.PUBLIC),

        /**
         * A single-bit mask representing {@code private} access,
         * The value is {@code 0x02}, happens to be the same as the value of the
         * {@code public} {@linkplain java.lang.reflect.Modifier#PRIVATE modifier bit}.
         */
        PRIVATE(MethodHandles.Lookup.PRIVATE),

        /**
         * A single-bit mask representing {@code protected} access,
         * The value is {@code 0x04}, happens to be the same as the value of the
         * {@code public} {@linkplain java.lang.reflect.Modifier#PROTECTED modifier bit}.
         */
        PROTECTED(MethodHandles.Lookup.PROTECTED),

        /**
         * A single-bit mask representing {@code package} access (default access),
         * The value is {@code 0x08}, which does not correspond meaningfully to
         * any particular {@linkplain java.lang.reflect.Modifier modifier bit}.
         */
        PACKAGE(MethodHandles.Lookup.PACKAGE),

        /**
         * A single-bit mask representing all accesses (public, private, protected and package)
         * The value, 0x0f
         */
        ALL(ALL_MODES),

        /**
         * -1
         */
        TRUSTED(-1);

        private final int value;

        LookupMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static int getModes(LookupMode... lookupModes) {
            int modes = 0;
            for (int i = 0; i < lookupModes.length; i++) {
                LookupMode lookupMode = lookupModes[i];
                modes |= lookupMode.value;
            }
            return modes;
        }
    }

    static class LookupKey {
        final Class<?> lookupClass;

        final int allowedModes;

        LookupKey(Class<?> lookupClass, int allowedModes) {
            this.lookupClass = lookupClass;
            this.allowedModes = allowedModes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LookupKey lookupKey = (LookupKey) o;
            return allowedModes == lookupKey.allowedModes && Objects.equals(lookupClass, lookupKey.lookupClass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lookupClass, allowedModes);
        }

        static LookupKey buildKey(Class<?> lookupClass, int allowedModes) {
            return new LookupKey(lookupClass, allowedModes);
        }
    }

    /**
     * Create an instance of {@link MethodHandles.Lookup} by the specified lookup class
     * with {@link #ALL_MODES all accesses (public, private, protected and package)}
     *
     * @param lookupClass the lookup class
     * @return non-null
     */
    public static MethodHandles.Lookup lookup(Class<?> lookupClass) {
        return lookup(lookupClass, LookupMode.ALL);
    }

    /**
     * Create an instance of {@link MethodHandles.Lookup} by the specified lookup class
     * with {@link #ALL_MODES all access (public, private, protected and package)}
     *
     * @param lookupClass the lookup class
     * @return non-null
     */
    public static MethodHandles.Lookup lookup(Class<?> lookupClass, LookupMode... lookupModes) {
        int allowedModes = getModes(lookupModes);
        LookupKey key = buildKey(lookupClass, allowedModes);
        return lookupCache.computeIfAbsent(key, MethodHandleUtils::newLookup);
    }

    /**
     * The convenient method to find {@link MethodHandles.Lookup#findVirtual(Class, String, MethodType)}
     *
     * @param lookupClass    the class to be looked up
     * @param methodName     the target method name
     * @param parameterTypes the types of target method parameters
     * @return {@link MethodHandle}
     */
    public static MethodHandle findVirtual(Class<?> lookupClass, String methodName, Class... parameterTypes) {
        return find(lookupClass, methodName, parameterTypes,
                (lookup, methodType) -> lookup.findVirtual(lookupClass, methodName, methodType));
    }

    /**
     * The convenient method to find {@link MethodHandles.Lookup#findStatic(Class, String, MethodType)}
     *
     * @param lookupClass    the class to be looked up
     * @param methodName     the target method name
     * @param parameterTypes the types of target method parameters
     * @return {@link MethodHandle}
     */
    public static MethodHandle findStatic(Class<?> lookupClass, String methodName, Class... parameterTypes) {
        return find(lookupClass, methodName, parameterTypes,
                (lookup, methodType) -> lookup.findStatic(lookupClass, methodName, methodType));
    }

    protected static MethodHandle find(Class<?> lookupClass, String methodName, Class[] parameterTypes,
                                       ThrowableBiFunction<MethodHandles.Lookup, MethodType, MethodHandle> function) {
        Method method = findMethod(lookupClass, methodName, parameterTypes);
        Class<?> returnType = method.getReturnType();
        MethodType methodType = isEmpty(parameterTypes) ? methodType(returnType) : methodType(returnType, parameterTypes);
        MethodHandles.Lookup lookup = isPublic(method) ? publicLookup() : lookup(lookupClass);
        return execute(lookup, methodType, function);
    }

    private static MethodHandles.Lookup newLookup(LookupKey key) {
        return newInstance(lookupConstructor2, key.lookupClass, key.allowedModes);
    }
}
