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


import io.microsphere.event.EchoEvent;
import io.microsphere.event.EventListener;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Set;

import static io.microsphere.internal.reflect.ConstantPoolUtils.CONSTANT_POOL_CLASS;
import static io.microsphere.internal.reflect.ConstantPoolUtils.getClassAt;
import static io.microsphere.internal.reflect.ConstantPoolUtils.getClassAtIfLoaded;
import static io.microsphere.internal.reflect.ConstantPoolUtils.getClassRefIndexAt;
import static io.microsphere.internal.reflect.ConstantPoolUtils.getConstantPool;
import static io.microsphere.internal.reflect.ConstantPoolUtils.getDoubleAt;
import static io.microsphere.internal.reflect.ConstantPoolUtils.getFieldAt;
import static io.microsphere.internal.reflect.ConstantPoolUtils.getFieldAtIfLoaded;
import static io.microsphere.internal.reflect.ConstantPoolUtils.getFloatAt;
import static io.microsphere.internal.reflect.ConstantPoolUtils.getIntAt;
import static io.microsphere.internal.reflect.ConstantPoolUtils.getLongAt;
import static io.microsphere.internal.reflect.ConstantPoolUtils.getMemberRefInfoAt;
import static io.microsphere.internal.reflect.ConstantPoolUtils.getMethodAt;
import static io.microsphere.internal.reflect.ConstantPoolUtils.getMethodAtIfLoaded;
import static io.microsphere.internal.reflect.ConstantPoolUtils.getNameAndTypeRefIndexAt;
import static io.microsphere.internal.reflect.ConstantPoolUtils.getNameAndTypeRefInfoAt;
import static io.microsphere.internal.reflect.ConstantPoolUtils.getSize;
import static io.microsphere.internal.reflect.ConstantPoolUtils.getStringAt;
import static io.microsphere.internal.reflect.ConstantPoolUtils.getUTF8At;
import static io.microsphere.reflect.FieldUtils.findAllDeclaredFields;
import static io.microsphere.reflect.FieldUtils.getStaticFieldValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ConstantPoolUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConstantPoolUtils
 * @since 1.0.0
 */
class ConstantPoolUtilsTest {
    @Test
    void testConstants() {
        Set<Field> allDeclaredFields = findAllDeclaredFields(ConstantPoolUtils.class);
        assertTrue(allDeclaredFields.stream().anyMatch(field -> getStaticFieldValue(true, field) != null));
    }

    @Test
    void testGetConstantPool() {
        assertNotNull(getConstantPool(Object.class));
        assertNotNull(getConstantPool(String.class));
        assertInstanceOf(CONSTANT_POOL_CLASS, getConstantPool(Object.class));
    }

    @Test
    void testGetter() {
        Class<?> targetClass = ConstantPoolUtils.class;
        int size = getSize(targetClass);
        assertTrue(size > 0);

        for (int i = 0; i < size; i++) {
            int index = i;
            assertDoesNotThrow(() -> getClassAt(targetClass, index));
            assertDoesNotThrow(() -> getClassAtIfLoaded(targetClass, index));
            assertDoesNotThrow(() -> getClassRefIndexAt(targetClass, index));
            assertDoesNotThrow(() -> getMethodAt(targetClass, index));
            assertDoesNotThrow(() -> getMethodAtIfLoaded(targetClass, index));
            assertDoesNotThrow(() -> getFieldAt(targetClass, index));
            assertDoesNotThrow(() -> getFieldAtIfLoaded(targetClass, index));
            assertDoesNotThrow(() -> getMemberRefInfoAt(targetClass, index));
            assertDoesNotThrow(() -> getNameAndTypeRefIndexAt(targetClass, index));
            assertDoesNotThrow(() -> getNameAndTypeRefInfoAt(targetClass, index));
            assertDoesNotThrow(() -> getIntAt(targetClass, index));
            assertDoesNotThrow(() -> getLongAt(targetClass, index));
            assertDoesNotThrow(() -> getFloatAt(targetClass, index));
            assertDoesNotThrow(() -> getDoubleAt(targetClass, index));
            assertDoesNotThrow(() -> getStringAt(targetClass, index));
            assertDoesNotThrow(() -> getUTF8At(targetClass, index));
        }
    }

    @Test
    void testDeduceGenericTypeFromLambda() {
        EventListener<EchoEvent> e = event -> {
        };

        Class<? extends EventListener> targetClass = e.getClass();

        int count = 0;

        int size = getSize(targetClass);
        for (int i = 0; i < size; i++) {
            Member member = getMethodAt(targetClass, i);
            if (member instanceof Method) {
                count++;
            }
        }

        assertEquals(1, count);
    }

}