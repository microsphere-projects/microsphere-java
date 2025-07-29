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

import java.lang.reflect.Method;

import static io.microsphere.reflect.ExecutableUtils.execute;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link ExecutableUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ExecutableUtils
 * @see ReflectionTest
 * @since 1.0.0
 */
public class ExecutableUtilsTest {

    private static final Class<ReflectionTest> testClass = ReflectionTest.class;

    private static final ReflectionTest test = new ReflectionTest();

    @Test
    void testExecuteWithThrowableConsumerOnPublicMethod() {
        Method method = findMethod(ReflectionTest.class, "publicMethod", int.class);
        execute(method, m -> {
            assertEquals("1", method.invoke(test, 1));
        });
    }

    @Test
    void testExecuteWithThrowableConsumerOnProtectedMethod() {
        Method method = findMethod(ReflectionTest.class, "protectedMethod", Object[].class);
        execute(method, m -> {
            Object[] args = new Object[]{1};
            Object arg = args;
            assertArrayEquals(args, (Object[]) method.invoke(test, arg));
        });
    }

    @Test
    void testExecuteWithThrowableConsumerOnPackagePrivateMethod() {
        Method method = findMethod(ReflectionTest.class, "packagePrivateMethod", String.class);
        execute(method, m -> {
            assertEquals("1", method.invoke(test, "1"));
        });
    }

    @Test
    void testExecuteWithThrowableConsumerOnIllegalAccessException() {
        Method method = findMethod(ReflectionTest.class, "privateMethod");
        assertThrows(IllegalStateException.class, () -> execute(method, m -> {
            assertEquals("test", method.invoke(test));
        }));
    }

    @Test
    void testExecuteWithThrowableConsumerOnInvocationTargetException() {
        Method method = findMethod(ReflectionTest.class, "errorMethod");
        assertThrows(RuntimeException.class, () -> execute(method, m -> {
            method.invoke(test);
        }));
    }

    @Test
    void testExecuteWithThrowableConsumerOnIllegalArgumentException() {
        Method method = findMethod(ReflectionTest.class, "protectedMethod", Object[].class);
        assertThrows(IllegalArgumentException.class, () -> execute(method, m -> {
            method.invoke(test);
        }));
    }

    @Test
    void testExecuteWithThrowableConsumerOnClassCastException() {
        Method method = findMethod(ReflectionTest.class, "publicMethod", int.class);
        assertThrows(RuntimeException.class, () -> execute(method, m -> {
            assertEquals(1, (int) method.invoke(test, 1));
        }));
    }

}
