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
import java.util.ArrayList;
import java.util.List;

import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.MethodUtils.getMethods;
import static io.microsphere.reflect.MethodUtils.getSignature;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.reflect.MethodUtils.invokeStaticMethod;
import static java.lang.Integer.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link MethodUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class MethodUtilsTest {

    /**
     * Test {@link MethodUtils#getSignature(Method)}
     */
    @Test
    public void testGetSignature() {
        Method method = null;

        // Test non-argument Method
        method = findMethod(this.getClass(), "testGetSignature");
        assertEquals("io.microsphere.reflect.MethodUtilsTest#testGetSignature()", getSignature(method));

        // Test one-argument Method
        method = findMethod(Object.class, "equals", Object.class);
        assertEquals("java.lang.Object#equals(java.lang.Object)", getSignature(method));

        // Test two-argument Method
        method = findMethod(MethodUtils.class, "findMethod", Class.class, String.class, Class[].class);
        assertEquals("io.microsphere.reflect.MethodUtils#findMethod(java.lang.Class,java.lang.String,java.lang.Class[])", getSignature(method));

    }

    @Test
    void testGetMethodsFromClass() {
        List<Method> objectMethods = getMethods(Object.class, true, true);

        List<Method> methods = getMethods(TestClass.class, true, true);
        assertEquals(1 + objectMethods.size(), methods.size());

        methods = getMethods(TestClass.class, false, true);
        assertEquals(1, methods.size());

        methods = getMethods(TestClass.class, false, false);
        assertEquals(7, methods.size());

        methods = getMethods(TestClass.class, true, false);
        objectMethods = getMethods(Object.class, true, false);

        assertEquals(7 + objectMethods.size(), methods.size());
    }

    @Test
    void testGetMethodsFromInterface() {
        List<Method> methods = getMethods(TestInterface.class, true, true);
        assertEquals(2, methods.size()); // method + useLambda

        methods = getMethods(TestInterface.class, true, false);
        assertEquals(3, methods.size()); // method + useLambda + 合成的lambda方法Object[]::new
        // NOTE:需不需要把合成的方法计算在内？ 应该是要算的

        List<Method> subMethods = getMethods(TestSubInterface.class, false, true);
        assertEquals(1, subMethods.size()); // subMethod

        subMethods = getMethods(TestSubInterface.class, true, true);
        assertEquals(3, subMethods.size()); // method + useLambda + subMethod
    }

    @Test
    public void testInvokeMethod() {
        String test = "test";
        assertEquals(test, invokeMethod(test, "toString"));

        TestClass testClass = new TestClass();
        assertEquals(valueOf(0), invokeMethod(testClass, "intMethod"));
        assertEquals(testClass, invokeMethod(testClass, "objectMethod"));
    }

    @Test
    public void testInvokeStaticMethod() {
        Method method = findMethod(Integer.class, "valueOf", int.class);
        assertEquals(valueOf(0), (Integer) invokeStaticMethod(method, 0));
        assertEquals(valueOf(0), (Integer) invokeStaticMethod(TestClass.class, "value", 0));
    }

    static class TestClass {
        public void method1() {
        }

        void method2() {
        }

        protected void method3() {
        }

        private void method4() {
        }

        private int intMethod() {
            return 0;
        }

        private Object objectMethod() {
            return this;
        }

        private static int value(Integer value) {
            return value;
        }
    }

    interface TestInterface {
        void method();

        default void useLambda() {
            new ArrayList<>().stream().toArray(Object[]::new);
        }
    }

    interface TestSubInterface extends TestInterface {
        void subMethod();
    }
}
