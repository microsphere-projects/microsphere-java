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

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.Test;

import java.util.AbstractList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import static io.microsphere.reflect.FieldUtils.getStaticFieldValue;
import static io.microsphere.reflect.Modifier.isAbstract;
import static io.microsphere.reflect.Modifier.isAnnotation;
import static io.microsphere.reflect.Modifier.isBridge;
import static io.microsphere.reflect.Modifier.isEnum;
import static io.microsphere.reflect.Modifier.isFinal;
import static io.microsphere.reflect.Modifier.isInterface;
import static io.microsphere.reflect.Modifier.isMandated;
import static io.microsphere.reflect.Modifier.isNative;
import static io.microsphere.reflect.Modifier.isPrivate;
import static io.microsphere.reflect.Modifier.isProtected;
import static io.microsphere.reflect.Modifier.isPublic;
import static io.microsphere.reflect.Modifier.isStatic;
import static io.microsphere.reflect.Modifier.isStrict;
import static io.microsphere.reflect.Modifier.isSynchronized;
import static io.microsphere.reflect.Modifier.isSynthetic;
import static io.microsphere.reflect.Modifier.isTransient;
import static io.microsphere.reflect.Modifier.isVarArgs;
import static io.microsphere.reflect.Modifier.isVolatile;
import static io.microsphere.reflect.Modifier.values;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link Modifier} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class ModifierTest {

    class A {
        strictfp Number a() {
            return 0;
        }

    }

    class B extends A {

        @Override
        Integer a() {
            return 0;
        }
    }

    @Test
    void testGetValue() {
        for (Modifier modifier : values()) {
            assertEquals(modifier.getValue(), findModifierValue(modifier.name()));
        }
    }

    @Test
    void testMatches() {
        for (Modifier modifier : values()) {
            assertTrue(modifier.matches(modifier.getValue()));
        }
    }

    @Test
    void testIsPublic() {
        assertTrue(isPublic(String.class.getModifiers()));
        assertFalse(isPublic(ModifierTest.class.getModifiers()));
    }

    @Test
    void testIsPrivate() throws NoSuchFieldException {
        assertTrue(isPrivate(Integer.class.getDeclaredField("value").getModifiers()));
        assertFalse(isPrivate(String.class.getModifiers()));
    }

    @Test
    void testIsProtected() throws NoSuchFieldException {
        assertTrue(isProtected(AbstractTestCase.class.getDeclaredField("logger").getModifiers()));
        assertFalse(isProtected(String.class.getModifiers()));
    }

    @Test
    void testIsStatic() throws NoSuchFieldException {
        assertTrue(isStatic(Integer.class.getDeclaredField("TYPE").getModifiers()));
        assertFalse(isStatic(String.class.getModifiers()));
    }

    @Test
    void testIsFinal() {
        assertTrue(isFinal(String.class.getModifiers()));
        assertFalse(isFinal(Object.class.getModifiers()));
    }

    @Test
    void testIsSynchronized() throws NoSuchMethodException {
        assertTrue(isSynchronized(StringBuffer.class.getMethod("length").getModifiers()));
        assertFalse(isSynchronized(String.class.getModifiers()));
    }

    @Test
    void testIsVolatile() throws NoSuchFieldException {
        assertTrue(isVolatile(AbstractQueuedSynchronizer.class.getDeclaredField("state").getModifiers()));
        assertFalse(isVolatile(String.class.getModifiers()));
    }

    @Test
    void testIsTransient() throws NoSuchFieldException {
        assertTrue(isTransient(AbstractQueuedSynchronizer.class.getDeclaredField("head").getModifiers()));
        assertFalse(isTransient(String.class.getModifiers()));
    }

    @Test
    void testIsNative() throws NoSuchMethodException {
        assertTrue(isNative(Object.class.getDeclaredMethod("notify").getModifiers()));
        assertFalse(isNative(String.class.getModifiers()));
    }

    @Test
    void testIsInterface() {
        assertTrue(isInterface(CharSequence.class.getModifiers()));
        assertFalse(isInterface(String.class.getModifiers()));
    }

    @Test
    void testIsAbstract() {
        assertTrue(isAbstract(AbstractList.class.getModifiers()));
        assertFalse(isAbstract(String.class.getModifiers()));
    }

    @Test
    void testIsStrict() throws NoSuchMethodException {
        assertTrue(isStrict(A.class.getDeclaredMethod("a").getModifiers()));
        assertFalse(isStrict(Object.class.getModifiers()));
    }

    @Test
    void testIsBridge() {
        // TODO assertTrue case
        assertFalse(isBridge(Object.class.getModifiers()));
    }

    @Test
    void testIsVarArgs() throws NoSuchMethodException {
        // Method method = Objects.class.getMethod("hash", Object[].class);
        // Parameter parameter = method.getParameters()[0];
        // assertTrue(isVarArgs(parameter.getModifiers()));
        // TODO assertTrue case
        assertFalse(isVarArgs(Object.class.getModifiers()));
    }

    @Test
    void testIsSynthetic() {
        // TODO assertTrue case
        assertFalse(isSynthetic(Object.class.getModifiers()));
    }

    @Test
    void testIsAnnotation() {
        assertTrue(isAnnotation(Override.class.getModifiers()));
        assertFalse(isAnnotation(Object.class.getModifiers()));
    }

    @Test
    void testIsEnum() {
        assertTrue(isEnum(TimeUnit.class.getModifiers()));
        assertFalse(isEnum(Object.class.getModifiers()));
    }

    @Test
    void testIsMandated() {
        assertTrue(isMandated(Modifier.MANDATED.getValue()));
        assertFalse(isMandated(Object.class.getModifiers()));
    }

    private int findModifierValue(String name) {
        return getStaticFieldValue(java.lang.reflect.Modifier.class, name);
    }


}
