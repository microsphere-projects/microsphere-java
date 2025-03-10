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

import org.junit.jupiter.api.Test;

import javax.annotation.processing.AbstractProcessor;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static io.microsphere.reflect.AccessibleObjectUtils.canAccess;
import static io.microsphere.reflect.AccessibleObjectUtils.setAccessible;
import static io.microsphere.reflect.AccessibleObjectUtils.trySetAccessible;
import static io.microsphere.reflect.ConstructorUtils.findConstructor;
import static io.microsphere.reflect.MemberUtils.isStatic;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.ReflectionUtils.INACCESSIBLE_OBJECT_EXCEPTION_CLASS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AccessibleObjectUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AccessibleObjectUtils
 * @since 1.0.0
 */
public class AccessibleObjectUtilsTest {

    private static final String test = "test";

    private static final Method[] methods = String.class.getMethods();

    private static final Class<?> targetClass = AccessibleObjectUtils.class;

    /**
     * private method in the internal module
     */
    private static final Method tryCanAccessMethod = findMethod(targetClass, "tryCanAccess", Object.class, AccessibleObject.class);

    /**
     * protected constructor in the module "java.compiler" and package "javax.annotation.processing"
     */
    private static final Constructor abstractProcessorConstructor = findConstructor(AbstractProcessor.class);

    @Test
    public void testSetAccessible() {
        for (Method method : methods) {
            assertEquals(method.isAccessible(), setAccessible(method));
        }
    }

    @Test
    public void testSetAccessibleOnNonPublicMembers() {
        assertEquals(tryCanAccessMethod.isAccessible(), setAccessible(tryCanAccessMethod));

        if (INACCESSIBLE_OBJECT_EXCEPTION_CLASS != null) {
            assertThrows(INACCESSIBLE_OBJECT_EXCEPTION_CLASS, () -> setAccessible(abstractProcessorConstructor));
        } else {
            assertEquals(abstractProcessorConstructor.isAccessible(), setAccessible(abstractProcessorConstructor));
        }
    }

    @Test
    public void testCanAccess() {
        for (Method method : methods) {
            if (!isStatic(method)) {
                assertTrue(canAccess(null, method));
            }
        }
    }

    @Test
    public void testCanAccessOnNonPublicMembers() {
        assertFalse(canAccess(null, tryCanAccessMethod));
    }

    @Test
    public void testTrySetAccessible() {
        for (Method method : methods) {
            assertTrue(trySetAccessible(method));
        }
    }

    @Test
    public void testTrySetAccessibleOnNonPublicMembers() {
        assertTrue(trySetAccessible(tryCanAccessMethod));
        assertTrue(trySetAccessible(abstractProcessorConstructor));
    }
}
