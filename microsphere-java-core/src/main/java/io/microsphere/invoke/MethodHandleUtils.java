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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.microsphere.invoke.MethodHandleUtils.LookupKey.buildKey;
import static io.microsphere.invoke.MethodHandleUtils.LookupMode.getModes;
import static io.microsphere.invoke.MethodHandlesLookupUtils.NOT_FOUND_METHOD_HANDLE;
import static io.microsphere.invoke.MethodHandlesLookupUtils.findPublic;
import static io.microsphere.reflect.ConstructorUtils.findConstructor;
import static io.microsphere.reflect.ConstructorUtils.getDeclaredConstructor;
import static io.microsphere.reflect.ConstructorUtils.newInstance;
import static io.microsphere.reflect.MemberUtils.isPublic;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.MethodUtils.isCallerSensitiveMethod;
import static java.lang.invoke.MethodHandles.Lookup.PACKAGE;
import static java.lang.invoke.MethodHandles.Lookup.PRIVATE;
import static java.lang.invoke.MethodHandles.Lookup.PROTECTED;
import static java.lang.invoke.MethodHandles.Lookup.PUBLIC;
import static java.lang.invoke.MethodHandles.publicLookup;

/**
 * The utilities class for {@link MethodHandle}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class MethodHandleUtils {

    /**
     * A single-bit mask representing {@code module} access,
     * which may contribute to the result of {@link MethodHandles.Lookup#lookupModes lookupModes}.
     * The value is {@code 0x10}, which does not correspond meaningfully to
     * any particular {@linkplain Modifier modifier bit}.
     * In conjunction with the {@code PUBLIC} modifier bit, a {@code Lookup}
     * with this lookup mode can access all public types in the module of the
     * lookup class and public types in packages exported by other modules
     * to the module of the class to be looked up.
     * <p>
     * If this lookup mode is set, the {@linkplain MethodHandles.Lookup#previousrequestedClass()
     * previous lookup class} is always {@code null}.
     *
     * @see MethodHandles.Lookup#MODULE
     * @since 9
     */
    public static final int MODULE = PACKAGE << 1;

    /**
     * A single-bit mask representing {@code unconditional} access
     * which may contribute to the result of {@link MethodHandles.Lookup#lookupModes lookupModes}.
     * The value is {@code 0x20}, which does not correspond meaningfully to
     * any particular {@linkplain Modifier modifier bit}.
     * A {@code Lookup} with this lookup mode assumes {@linkplain
     * java.lang.Module#canRead(java.lang.Module) readability}.
     * This lookup mode can access all public members of public types
     * of all modules when the type is in a package that is {@link
     * java.lang.Module#isExported(String) exported unconditionally}.
     *
     * <p>
     * If this lookup mode is set, the {@linkplain MethodHandles.Lookup#previousrequestedClass()
     * previous lookup class} is always {@code null}.
     *
     * @see MethodHandles.Lookup#publicLookup()
     * @see MethodHandles.Lookup#UNCONDITIONAL
     * @since 9
     */
    public static final int UNCONDITIONAL = PACKAGE << 2;

    /**
     * A single-bit mask representing {@code original} access
     * which may contribute to the result of {@link MethodHandles.Lookup#lookupModes lookupModes}.
     * The value is {@code 0x40}, which does not correspond meaningfully to
     * any particular {@linkplain Modifier modifier bit}.
     *
     * <p>
     * If this lookup mode is set, the {@code Lookup} object must be
     * created by the original lookup class by calling
     * {@link MethodHandles#lookup()} method or by a bootstrap method
     * invoked by the VM.  The {@code Lookup} object with this lookup
     * mode has {@linkplain MethodHandles.Lookup#hasFullPrivilegeAccess() full privilege access}.
     *
     * @see MethodHandles.Lookup#ORIGINAL
     * @since 16
     */
    public static final int ORIGINAL = PACKAGE << 3;

    /**
     * A single-bit mask representing all accesses (public, private, protected and package)
     * The value, 0x0f, happens to be the same as the value of the modifier bit.
     */
    public static final int ALL_MODES = (PUBLIC | PRIVATE | PROTECTED | PACKAGE | MODULE | UNCONDITIONAL | ORIGINAL);

    /**
     * The {@link Constructor} for {@link MethodHandles.Lookup#Lookup(Class)} since JDK 7
     */
    private static final Constructor<MethodHandles.Lookup> lookupConstructor1 = getDeclaredConstructor(MethodHandles.Lookup.class, Class.class);

    /**
     * The {@link Constructor} for {@link MethodHandles.Lookup#Lookup(Class, int)} since JDK 7
     */
    private static final Constructor<MethodHandles.Lookup> lookupConstructor2 = findConstructor(MethodHandles.Lookup.class, Class.class, int.class);

    /**
     * The {@link Constructor} for {@link MethodHandles.Lookup#Lookup(Class, Class, int)} since JDK 14
     */
    private static final Constructor<MethodHandles.Lookup> lookupConstructor3 = findConstructor(MethodHandles.Lookup.class, Class.class, Class.class, int.class);

    private static final ConcurrentMap<LookupKey, MethodHandles.Lookup> lookupCache = new ConcurrentHashMap<>();

    /**
     * The {@link MethodHandles.Lookup} for {@link MethodHandles#publicLookup()}
     */
    public static final MethodHandles.Lookup PUBLIC_LOOKUP = publicLookup();

    /**
     * The allowed {@link MethodHandles.Lookup} modes enumeration
     *
     * @see MethodHandles.Lookup#PUBLIC
     * @see MethodHandles.Lookup#PRIVATE
     * @see MethodHandles.Lookup#PROTECTED
     * @see MethodHandles.Lookup#PACKAGE
     * @see MethodHandles.Lookup#MODULE
     * @see MethodHandles.Lookup#UNCONDITIONAL
     * @see MethodHandles.Lookup#ORIGINAL
     * @see MethodHandles.Lookup#TRUSTED
     * @see MethodHandles.Lookup#ALL_MODES
     */
    public enum LookupMode {

        /**
         * @see MethodHandles.Lookup#PUBLIC
         */
        PUBLIC(MethodHandles.Lookup.PUBLIC),

        /**
         * @see MethodHandles.Lookup#PRIVATE
         */
        PRIVATE(MethodHandles.Lookup.PRIVATE),

        /**
         * @see MethodHandles.Lookup#PROTECTED
         */
        PROTECTED(MethodHandles.Lookup.PROTECTED),

        /**
         * @see MethodHandles.Lookup#PACKAGE
         */
        PACKAGE(MethodHandles.Lookup.PACKAGE),

        /**
         * @see MethodHandleUtils#MODULE
         */
        MODULE(MethodHandleUtils.MODULE),

        /**
         * @see MethodHandleUtils#UNCONDITIONAL
         */
        UNCONDITIONAL(MethodHandleUtils.UNCONDITIONAL),

        /**
         * @see MethodHandleUtils#ORIGINAL
         */
        ORIGINAL(MethodHandleUtils.ORIGINAL),

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
        final Class<?> requestedClass;

        final int allowedModes;

        LookupKey(Class<?> requestedClass, int allowedModes) {
            this.requestedClass = requestedClass;
            this.allowedModes = allowedModes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LookupKey lookupKey = (LookupKey) o;
            return allowedModes == lookupKey.allowedModes && Objects.equals(requestedClass, lookupKey.requestedClass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(requestedClass, allowedModes);
        }

        static LookupKey buildKey(Class<?> requestedClass, int allowedModes) {
            return new LookupKey(requestedClass, allowedModes);
        }
    }

    /**
     * Create an instance of {@link MethodHandles.Lookup} by the specified lookup class
     * with {@link #ALL_MODES all accesses (public, private, protected and package)}
     *
     * @param requestedClass the class to be looked up
     * @return non-null
     */
    public static MethodHandles.Lookup lookup(Class<?> requestedClass) {
        return lookup(requestedClass, LookupMode.ALL);
    }

    /**
     * Create an instance of {@link MethodHandles.Lookup} by the specified lookup class
     * with {@link #ALL_MODES all access (public, private, protected and package)}
     *
     * @param requestedClass the class to be looked up
     * @return non-null
     */
    public static MethodHandles.Lookup lookup(Class<?> requestedClass, LookupMode... lookupModes) {
        int allowedModes = getModes(lookupModes);
        LookupKey key = buildKey(requestedClass, allowedModes);
        return lookupCache.computeIfAbsent(key, MethodHandleUtils::newLookup);
    }

    /**
     * The convenient method to find {@link MethodHandles.Lookup#findVirtual(Class, String, MethodType)}
     *
     * @param requestedClass the class to be looked up
     * @param methodName     the target method name
     * @param parameterTypes the types of target method parameters
     * @return {@link MethodHandle}
     */
    public static MethodHandle findVirtual(Class<?> requestedClass, String methodName, Class... parameterTypes) {
        return find(requestedClass, methodName, parameterTypes, (lookup, methodType) -> lookup.findVirtual(requestedClass, methodName, methodType));
    }

    /**
     * The convenient method to find {@link MethodHandles.Lookup#findStatic(Class, String, MethodType)}
     *
     * @param requestedClass the class to be looked up
     * @param methodName     the target method name
     * @param parameterTypes the types of target method parameters
     * @return {@link MethodHandle}
     */
    public static MethodHandle findStatic(Class<?> requestedClass, String methodName, Class... parameterTypes) {
        return find(requestedClass, methodName, parameterTypes, (lookup, methodType) -> lookup.findStatic(requestedClass, methodName, methodType));
    }

    protected static MethodHandle find(Class<?> requestedClass, String methodName, Class[] parameterTypes,
                                       ThrowableBiFunction<MethodHandles.Lookup, MethodType, MethodHandle> function) {
        Method method = findMethod(requestedClass, methodName, parameterTypes);
        if (method == null) {
            return NOT_FOUND_METHOD_HANDLE;
        }
        if (isiCandidateMethod(method)) {
            return findPublic(method, function);
        }
        MethodHandles.Lookup lookup = lookup(requestedClass);
        return MethodHandlesLookupUtils.find(lookup, requestedClass, methodName, parameterTypes, function);
    }

    private static MethodHandles.Lookup newLookup(LookupKey key) {
        if (lookupConstructor3 != null) {
            return newInstance(lookupConstructor3, key.requestedClass, key.requestedClass, key.allowedModes);
        }
        return newInstance(lookupConstructor2, key.requestedClass, key.allowedModes);
    }

    private static boolean isiCandidateMethod(Method method) {
        return isPublic(method) && !isCallerSensitiveMethod(method);
    }
}
