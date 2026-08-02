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

package io.microsphere.internal.reflect;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.util.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.util.ClassLoaderUtils.loadClass;
import static io.microsphere.util.ObjectUtils.defaultIfNull;
import static java.lang.ClassLoader.getSystemClassLoader;

/**
 * The utility class for {@link sun.reflect.ConstantPool} or {@link jdk.internal.reflect.ConstantPool}(JDK 9+
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see sun.reflect.ConstantPool
 * @see jdk.internal.reflect.ConstantPool
 * @since 1.0.0
 */
public abstract class ConstantPoolUtils implements Utils {

    private static final Logger logger = getLogger(ConstantPoolUtils.class);

    /**
     * The class name of {@link sun.reflect.ConstantPool}
     */
    public static final String LEGACY_CONSTANT_POOL_CLASS_NAME = "sun.reflect.ConstantPool";

    /**
     * The class name of {@link jdk.internal.reflect.ConstantPool}
     */
    public static final String CONSTANT_POOL_CLASS_NAME = "jdk.internal.reflect.ConstantPool";

    /**
     * The {@link Class} of {@link sun.reflect.ConstantPool} or {@link jdk.internal.reflect.ConstantPool}
     */
    @Nonnull
    public static final Class<?> CONSTANT_POOL_CLASS = loadConstantPoolClass();

    /**
     * The {@link Method} of {@link sun.reflect.ConstantPool#getSize()} or {@link jdk.internal.reflect.ConstantPool#getSize()}
     */
    static final Method getSizeMethod = findMethod(CONSTANT_POOL_CLASS, "getSize");

    /**
     * The {@link Method} of {@link sun.reflect.ConstantPool#getClassAt(int)} or {@link jdk.internal.reflect.ConstantPool#getClassAt(int)}
     */
    static final Method getClassAtMethod = findMethod(CONSTANT_POOL_CLASS, "getClassAt", int.class);

    /**
     * The {@link Method} of {@link sun.reflect.ConstantPool#getClassAtIfLoaded(int)} or {@link jdk.internal.reflect.ConstantPool#getClassAtIfLoaded(int)}
     */
    static final Method getClassAtIfLoadedMethod = findMethod(CONSTANT_POOL_CLASS, "getClassAtIfLoaded", int.class);

    /**
     * The {@link Method} of {@link sun.reflect.ConstantPool#getClassRefIndexAt(int)} or {@link jdk.internal.reflect.ConstantPool#getClassRefIndexAt(int)}
     */
    static final Method getClassRefIndexAtMethod = findMethod(CONSTANT_POOL_CLASS, "getClassRefIndexAt", int.class);

    /**
     * The {@link Method} of {@link sun.reflect.ConstantPool#getMethodAt(int)} or {@link jdk.internal.reflect.ConstantPool#getMethodAt(int)}
     */
    static final Method getMethodAtMethod = findMethod(CONSTANT_POOL_CLASS, "getMethodAt", int.class);

    /**
     * The {@link Method} of {@link sun.reflect.ConstantPool#getMethodAtIfLoaded(int)} or {@link jdk.internal.reflect.ConstantPool#getMethodAtIfLoaded(int)}
     */
    static final Method getMethodAtIfLoadedMethod = findMethod(CONSTANT_POOL_CLASS, "getMethodAtIfLoaded", int.class);

    /**
     * The {@link Method} of {@link sun.reflect.ConstantPool#getFieldAt(int)} or {@link jdk.internal.reflect.ConstantPool#getFieldAt(int)}
     */
    static final Method getFieldAtMethod = findMethod(CONSTANT_POOL_CLASS, "getFieldAt", int.class);

    /**
     * The {@link Method} of {@link sun.reflect.ConstantPool#getFieldAtIfLoaded(int)} or {@link jdk.internal.reflect.ConstantPool#getFieldAtIfLoaded(int)}
     */
    static final Method getFieldAtIfLoadedMethod = findMethod(CONSTANT_POOL_CLASS, "getFieldAtIfLoaded", int.class);

    /**
     * The {@link Method} of {@link sun.reflect.ConstantPool#getMemberRefInfoAt(int)} or {@link jdk.internal.reflect.ConstantPool#getMemberRefInfoAt(int)}
     */
    static final Method getMemberRefInfoAtMethod = findMethod(CONSTANT_POOL_CLASS, "getMemberRefInfoAt", int.class);

    /**
     * The {@link Method} of {@link sun.reflect.ConstantPool#getNameAndTypeRefIndexAt(int)} or {@link jdk.internal.reflect.ConstantPool#getNameAndTypeRefIndexAt(int)}
     */
    static final Method getNameAndTypeRefIndexAtMethod = findMethod(CONSTANT_POOL_CLASS, "getNameAndTypeRefIndexAt", int.class);

    /**
     * The {@link Method} of {@link sun.reflect.ConstantPool#getNameAndTypeRefInfoAt(int)} or {@link jdk.internal.reflect.ConstantPool#getNameAndTypeRefInfoAt(int)}
     */
    static final Method getNameAndTypeRefInfoAtMethod = findMethod(CONSTANT_POOL_CLASS, "getNameAndTypeRefInfoAt", int.class);

    /**
     * The {@link Method} of {@link sun.reflect.ConstantPool#getIntAt(int)} or {@link jdk.internal.reflect.ConstantPool#getIntAt(int)}
     */
    static final Method getIntAtMethod = findMethod(CONSTANT_POOL_CLASS, "getIntAt", int.class);

    /**
     * The {@link Method} of {@link sun.reflect.ConstantPool#getLongAt(int)} or {@link jdk.internal.reflect.ConstantPool#getLongAt(int)}
     */
    static final Method getLongAtMethod = findMethod(CONSTANT_POOL_CLASS, "getLongAt", int.class);

    /**
     * The {@link Method} of {@link sun.reflect.ConstantPool#getFloatAt(int)} or {@link jdk.internal.reflect.ConstantPool#getFloatAt(int)}
     */
    static final Method getFloatAtMethod = findMethod(CONSTANT_POOL_CLASS, "getFloatAt", int.class);

    /**
     * The {@link Method} of {@link sun.reflect.ConstantPool#getDoubleAt(int)} or {@link jdk.internal.reflect.ConstantPool#getDoubleAt(int)}
     */
    static final Method getDoubleAtMethod = findMethod(CONSTANT_POOL_CLASS, "getDoubleAt", int.class);

    /**
     * The {@link Method} of {@link sun.reflect.ConstantPool#getStringAt(int)} or {@link jdk.internal.reflect.ConstantPool#getStringAt(int)}
     */
    static final Method getStringAtMethod = findMethod(CONSTANT_POOL_CLASS, "getStringAt", int.class);

    /**
     * The {@link Method} of {@link sun.reflect.ConstantPool#getUTF8At(int)} or {@link jdk.internal.reflect.ConstantPool#getUTF8At(int)}
     */
    static final Method getUTF8AtMethod = findMethod(CONSTANT_POOL_CLASS, "getUTF8At", int.class);

    public static int getSize(Class<?> targetClass) {
        Integer size = invoke(targetClass, getSizeMethod);
        return size != null ? size : 0;
    }

    @Nullable
    public static Class<?> getClassAt(Class<?> targetClass, int index) {
        return invoke(targetClass, getClassAtMethod, index);
    }

    @Nullable
    public static Class<?> getClassAtIfLoaded(Class<?> targetClass, int index) {
        return invoke(targetClass, getClassAtIfLoadedMethod, index);
    }

    /**
     * Get a class reference index for a method or a field.
     *
     * @param targetClass the class to inspect
     * @param index the index of the class reference
     * @return the class reference index
     */
    @Nullable
    public static Integer getClassRefIndexAt(Class<?> targetClass, int index) {
        return invoke(targetClass, getClassRefIndexAtMethod, index);
    }

    @Nullable
    public static Member getMethodAt(Class<?> targetClass, int index) {
        return invoke(targetClass, getMethodAtMethod, index);
    }

    @Nullable
    public static Member getMethodAtIfLoaded(Class<?> targetClass, int index) {
        return invoke(targetClass, getMethodAtIfLoadedMethod, index);
    }

    @Nullable
    public static Field getFieldAt(Class<?> targetClass, int index) {
        return invoke(targetClass, getFieldAtMethod, index);
    }

    @Nullable
    public static Field getFieldAtIfLoaded(Class<?> targetClass, int index) {
        return invoke(targetClass, getFieldAtIfLoadedMethod, index);
    }

    @Nonnull
    public static String[] getMemberRefInfoAt(Class<?> targetClass, int index) {
        return invoke(targetClass, getMemberRefInfoAtMethod, index);
    }

    @Nullable
    public static Integer getNameAndTypeRefIndexAt(Class<?> targetClass, int index) {
        return invoke(targetClass, getNameAndTypeRefIndexAtMethod, index);
    }

    @Nonnull
    public static String[] getNameAndTypeRefInfoAt(Class<?> targetClass, int index) {
        return invoke(targetClass, getNameAndTypeRefInfoAtMethod, index);
    }

    @Nullable
    public static Integer getIntAt(Class<?> targetClass, int index) {
        return invoke(targetClass, getIntAtMethod, index);
    }

    @Nullable
    public static Long getLongAt(Class<?> targetClass, int index) {
        return invoke(targetClass, getLongAtMethod, index);
    }

    @Nullable
    public static Float getFloatAt(Class<?> targetClass, int index) {
        return invoke(targetClass, getFloatAtMethod, index);
    }

    public static Double getDoubleAt(Class<?> targetClass, int index) {
        return invoke(targetClass, getDoubleAtMethod, index);
    }

    @Nullable
    public static String getStringAt(Class<?> targetClass, int index) {
        return invoke(targetClass, getStringAtMethod, index);
    }

    @Nullable
    public static String getUTF8At(Class<?> targetClass, int index) {
        return invoke(targetClass, getUTF8AtMethod, index);
    }

    @Nullable
    static <T> T invoke(Class<?> targetClass, Method method, Object... args) {
        T returnValue = null;
        try {
            Object constantPool = getConstantPool(targetClass);
            returnValue = invokeMethod(true, constantPool, method, args);
        } catch (Throwable e) {
            logger.trace(e.getMessage());
        }
        return returnValue;
    }

    static Object getConstantPool(Class<?> targetClass) {
        return invokeMethod(true, (Object) targetClass, "getConstantPool");
    }

    static Class<?> loadConstantPoolClass() {
        ClassLoader classLoader = getSystemClassLoader();
        Class<?> javaLangAccessClass = loadClass(classLoader, LEGACY_CONSTANT_POOL_CLASS_NAME);
        return defaultIfNull(javaLangAccessClass, () -> loadClass(classLoader, CONSTANT_POOL_CLASS_NAME));
    }

    private ConstantPoolUtils() {
    }
}