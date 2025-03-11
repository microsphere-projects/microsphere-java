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
import io.microsphere.util.BaseUtils;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;

import static io.microsphere.constants.PathConstants.SLASH_CHAR;
import static io.microsphere.constants.SeparatorConstants.LINE_SEPARATOR;
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
public abstract class AccessibleObjectUtils extends BaseUtils {

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
     * Try to set the {@link AccessibleObject} accessible.
     * <p>
     * If JDK >=9 , {@link AccessibleObject#trySetAccessible()} method will be invoked,
     * or {@link AccessibleObject#setAccessible(boolean)} method will be invoked if
     * {@link AccessibleObject#isAccessible()} is <code>false</code>.
     *
     * @param accessibleObject the {@link AccessibleObject} instance
     * @return
     * @see AccessibleObject#trySetAccessible()
     * @see AccessibleObject#setAccessible(boolean)
     * @see AccessibleObject#isAccessible()
     */
    public static boolean trySetAccessible(AccessibleObject accessibleObject) {
        MethodHandle methodHandle = trySetAccessibleMethodHandle;
        if (methodHandle == null) { // JDK < 9 or not be initialized
            setAccessible(accessibleObject);
            return true;
        } else { // JDK 9+
            return trySetAccessible(methodHandle, accessibleObject);
        }
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
            } catch (RuntimeException e) {
                if (isInaccessibleObjectException(e)) {
                    String rawErrorMessage = e.getMessage();
                    String moduleName = substringBetween(rawErrorMessage, "module ", " ");
                    String packageName = substringBetween(rawErrorMessage, "opens ", "\"");
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
                throw e;
            }
        }
        return accessible;
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
        Member member = asMember(accessibleObject);
        if (isPublic(member)) {
            return true;
        }
        Boolean access = tryCanAccess(object, accessibleObject);
        return access == null ? accessibleObject.isAccessible() : access;
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
}
