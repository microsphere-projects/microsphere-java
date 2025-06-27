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

import io.microsphere.logging.Logger;
import io.microsphere.util.Utils;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;

import static io.microsphere.constants.PathConstants.SLASH_CHAR;
import static io.microsphere.constants.SeparatorConstants.LINE_SEPARATOR;
import static io.microsphere.constants.SymbolConstants.DOUBLE_QUOTE;
import static io.microsphere.constants.SymbolConstants.SPACE;
import static io.microsphere.invoke.MethodHandleUtils.findVirtual;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.MemberUtils.asMember;
import static io.microsphere.reflect.MemberUtils.isPublic;
import static io.microsphere.reflect.ReflectionUtils.isInaccessibleObjectException;
import static io.microsphere.util.StringUtils.substringBetween;

/**
 * The utilities class of {@link AccessibleObject}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AccessibleObject
 * @since 1.0.0
 */
public abstract class AccessibleObjectUtils implements Utils {

    private static final Logger logger = getLogger(AccessibleObjectUtils.class);

    /**
     * The method name of {@link AccessibleObject#canAccess(Object)} since JDK 9
     */
    private static final String canAccessMethodName = "canAccess";

    /**
     * The method name of {@link AccessibleObject#trySetAccessible()} since JDK 9
     */
    private static final String trySetAccessibleMethodName = "trySetAccessible";

    /**
     * The {@link MethodHandle} of {@link AccessibleObject#canAccess(Object)} since JDK 9
     * if <code>canAccessMethodHandle == null</code>, it indicates the version of JDK is less than 9
     */
    private static final MethodHandle canAccessMethodHandle = findVirtual(AccessibleObject.class, canAccessMethodName, Object.class);

    /**
     * The {@link MethodHandle} of {@link AccessibleObject#trySetAccessible()} since JDK 9
     * if <code>canAccessMethodHandle == null</code>, it indicates the version of JDK is less than 9
     */
    private static final MethodHandle trySetAccessibleMethodHandle = findVirtual(AccessibleObject.class, trySetAccessibleMethodName);

    /**
     * Attempts to set the accessibility of the given {@link AccessibleObject}.
     *
     * <p>
     * This method intelligently selects the appropriate approach based on the JDK version:
     * </p>
     * <ul>
     *   <li>If running on JDK 9 or later, it uses the {@link AccessibleObject#trySetAccessible()} method via a {@link MethodHandle}.</li>
     *   <li>If running on JDK 8 or earlier, it falls back to calling the traditional {@link AccessibleObject#setAccessible(boolean)} method,
     *       but only if it is not already accessible.</li>
     * </ul>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Field field = MyClass.class.getDeclaredField("myField");
     * boolean success = AccessibleObjectUtils.trySetAccessible(field);
     * if (success) {
     *     // Access the field reflectively
     * }
     * }</pre>
     *
     * @param accessibleObject The {@link AccessibleObject} instance to make accessible.
     * @return {@code true} if the object was successfully made accessible; {@code false} otherwise.
     * @see AccessibleObject#trySetAccessible()
     * @see AccessibleObject#setAccessible(boolean)
     * @see AccessibleObject#isAccessible()
     */
    public static boolean trySetAccessible(AccessibleObject accessibleObject) {
        MethodHandle methodHandle = trySetAccessibleMethodHandle;
        if (methodHandle == null) { // JDK < 9 or not be initialized
            return setAccessible(accessibleObject);
        } else { // JDK 9+
            return trySetAccessible(methodHandle, accessibleObject);
        }
    }

    /**
     * Tests whether the caller can access the specified {@link AccessibleObject} with the given instance.
     *
     * <p>
     * If the reflected object corresponds to an instance method or field, the provided
     * {@code object} must be an instance of the declaring class. For static members and constructors,
     * the {@code object} must be {@code null}.
     * </p>
     *
     * <h3>Example Usage</h3>
     *
     * <h4>Testing Access to an Instance Field</h4>
     * <pre>{@code
     * MyClass instance = new MyClass();
     * Field field = MyClass.class.getDeclaredField("myField");
     * boolean accessible = AccessibleObjectUtils.canAccess(instance, field);
     * if (accessible) {
     *     System.out.println("Field is accessible.");
     * }
     * }</pre>
     *
     * <h4>Testing Access to a Static Method</h4>
     * <pre>{@code
     * Method method = MyClass.class.getDeclaredMethod("myStaticMethod");
     * boolean accessible = AccessibleObjectUtils.canAccess(null, method);
     * if (accessible) {
     *     System.out.println("Static method is accessible.");
     * }
     * }</pre>
     *
     * @param object           An instance of the declaring class for instance methods/fields; must be
     *                         {@code null} for static members or constructors.
     * @param accessibleObject The reflected object (field, method, constructor) to test access for.
     * @return {@code true} if the caller can access this reflected object;
     * {@code false} otherwise.
     * @see AccessibleObject#isAccessible()
     */
    public static boolean canAccess(Object object, AccessibleObject accessibleObject) {
        Member member = asMember(accessibleObject);
        if (isPublic(member)) {
            return true;
        }
        Boolean access = tryCanAccess(object, accessibleObject);
        return access == null ? accessibleObject.isAccessible() : access;
    }

    /**
     * Set the {@link AccessibleObject} accessible.
     *
     * @param accessibleObject the {@link AccessibleObject} instance
     * @return previous accessible status of the {@link AccessibleObject} instance
     */
    static boolean setAccessible(AccessibleObject accessibleObject) {
        boolean accessible = accessibleObject.isAccessible();
        if (!accessible) {
            try {
                accessibleObject.setAccessible(true);
                accessible = true;
            } catch (RuntimeException e) {
                handleInaccessibleObjectExceptionIfFound(e);
            }
        }
        return accessible;
    }

    private static boolean trySetAccessible(MethodHandle methodHandle, AccessibleObject accessibleObject) {
        boolean accessible = false;
        try {
            accessible = (boolean) methodHandle.invokeExact(accessibleObject);
        } catch (Throwable e) {
            logger.error("It's failed to invokeExact on {} with accessibleObject : {}", methodHandle, accessibleObject, e);
        }
        return accessible;
    }

    private static Boolean tryCanAccess(Object object, AccessibleObject accessibleObject) {
        Boolean access = null;
        if (canAccessMethodHandle != null) { // JDK 9+
            try {
                access = (boolean) canAccessMethodHandle.invokeExact(accessibleObject, object);
            } catch (Throwable e) {
                logger.error("It's failed to invokeExact on {} with object : {} , accessible object : {}", canAccessMethodHandle, object, accessibleObject, e);
            }
        }
        return access;
    }

    private static void handleInaccessibleObjectExceptionIfFound(Throwable e) {
        if (isInaccessibleObjectException(e)) {
            String rawErrorMessage = e.getMessage();
            String moduleName = substringBetween(rawErrorMessage, "module ", SPACE);
            String packageName = substringBetween(rawErrorMessage, "opens ", DOUBLE_QUOTE);
            // JDK 16+ : JEP 396: Strongly Encapsulate JDK Internals by Default - https://openjdk.org/jeps/396
            StringBuilder errorMessageBuilder = new StringBuilder("JEP 396: Strongly Encapsulate JDK Internals by Default since JDK 16 - https://openjdk.org/jeps/396.");
            errorMessageBuilder.append(LINE_SEPARATOR)
                    .append("It's require to add JVM Options '--add-opens=")
                    .append(moduleName)
                    .append(SLASH_CHAR)
                    .append(packageName)
                    .append("=ALL-UNNAMED' for running");
            logger.error(errorMessageBuilder.toString(), e);
        }
    }

    private AccessibleObjectUtils() {
    }
}
