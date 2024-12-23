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

import io.microsphere.util.ArrayUtils;
import org.junit.jupiter.api.Test;

import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static io.microsphere.management.JmxUtils.getRuntimeMXBean;
import static io.microsphere.reflect.MethodUtils.OBJECT_DECLARED_METHODS;
import static io.microsphere.reflect.MethodUtils.OBJECT_PUBLIC_METHODS;
import static io.microsphere.reflect.MethodUtils.PULIC_METHOD_PREDICATE;
import static io.microsphere.reflect.MethodUtils.filterMethods;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.MethodUtils.getAllDeclaredMethods;
import static io.microsphere.reflect.MethodUtils.getDeclaredMethods;
import static io.microsphere.reflect.MethodUtils.getMethods;
import static io.microsphere.reflect.MethodUtils.getSignature;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.reflect.MethodUtils.invokeStaticMethod;
import static java.lang.Integer.valueOf;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link MethodUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class MethodUtilsTest {

    private static final int JACOCO_ADDED_METHOD_COUNT;

    static {
        RuntimeMXBean runtimeMXBean = getRuntimeMXBean();
        JACOCO_ADDED_METHOD_COUNT = runtimeMXBean.getInputArguments()
                .stream()
                .filter(arg -> arg.contains("org.jacoco.agent")).count() > 0 ? 1 : 0;
    }

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
    void testFilterMethodsFromNull() {
        List<Method> methods = filterMethods(null, true, true);
        assertEquals(emptyList(), methods);

        methods = filterMethods(null, true, false);
        assertEquals(emptyList(), methods);

        methods = filterMethods(null, false, true);
        assertEquals(emptyList(), methods);

        methods = filterMethods(null, false, false);
        assertEquals(emptyList(), methods);

        methods = getMethods(null);
        assertEquals(emptyList(), methods);
    }

    @Test
    public void testFilterMethodsFromPrimitive() {
        Class<?> primitiveType = int.class;
        List<Method> methods = filterMethods(primitiveType, true, true);
        assertEquals(emptyList(), methods);

        methods = filterMethods(primitiveType, true, false);
        assertEquals(emptyList(), methods);

        methods = filterMethods(primitiveType, false, true);
        assertEquals(emptyList(), methods);

        methods = filterMethods(primitiveType, false, false);
        assertEquals(emptyList(), methods);

        methods = getMethods(primitiveType);
        assertEquals(emptyList(), methods);
    }

    @Test
    public void testFilterMethodsFromArray() {
        Class<?> arrayClass = ArrayUtils.EMPTY_CLASS_ARRAY.getClass();
        List<Method> methods = filterMethods(arrayClass, true, true);
        assertEquals(OBJECT_PUBLIC_METHODS, methods);

        methods = filterMethods(arrayClass, false, true);
        assertEquals(OBJECT_PUBLIC_METHODS, methods);

        methods = filterMethods(arrayClass, true, false);
        assertEquals(OBJECT_PUBLIC_METHODS, methods);

        methods = filterMethods(arrayClass, false, false);
        assertEquals(OBJECT_PUBLIC_METHODS, methods);
    }

    @Test
    void testFilterMethodsFromClass() {
        List<Method> objectMethods = filterMethods(Object.class, true, true);

        List<Method> methods = filterMethods(TestClass.class, true, true);
        assertEquals(1 + objectMethods.size(), methods.size());

        methods = filterMethods(TestClass.class, false, true);
        assertEquals(1, methods.size());

        methods = filterMethods(TestClass.class, false, false);
        assertEquals(7 + JACOCO_ADDED_METHOD_COUNT, methods.size());

        methods = filterMethods(TestClass.class, true, false);
        objectMethods = filterMethods(Object.class, true, false);

        assertEquals(7 + JACOCO_ADDED_METHOD_COUNT + objectMethods.size(), methods.size());
    }

    @Test
    void testFilterMethodsFromInterface() {
        List<Method> methods = filterMethods(TestInterface.class, true, true);
        assertEquals(2, methods.size()); // method + useLambda

        methods = filterMethods(TestInterface.class, true, false);
        assertEquals(3 + JACOCO_ADDED_METHOD_COUNT, methods.size()); // method + useLambda + generated lambda method by compiler

        List<Method> subMethods = filterMethods(TestSubInterface.class, false, true);
        assertEquals(1, subMethods.size()); // subMethod

        subMethods = filterMethods(TestSubInterface.class, true, true);
        assertEquals(3, subMethods.size()); // method + useLambda + subMethod
    }

    @Test
    public void testGetDeclaredMethods() {
        List<Method> methods = getDeclaredMethods(Object.class);
        assertEquals(OBJECT_DECLARED_METHODS, methods);

        methods = getDeclaredMethods(Object.class, PULIC_METHOD_PREDICATE);
        assertEquals(OBJECT_PUBLIC_METHODS, methods);

        methods = getDeclaredMethods(TestClass.class);
        assertEquals(7 + JACOCO_ADDED_METHOD_COUNT, methods.size());

        methods = getDeclaredMethods(TestClass.class, PULIC_METHOD_PREDICATE);
        assertEquals(1, methods.size());
    }

    @Test
    public void testGetAllDeclaredMethods() {
        List<Method> methods = getAllDeclaredMethods(Object.class);
        assertEquals(OBJECT_DECLARED_METHODS, methods);

        methods = getAllDeclaredMethods(Object.class, PULIC_METHOD_PREDICATE);
        assertEquals(OBJECT_PUBLIC_METHODS, methods);

        methods = getAllDeclaredMethods(TestClass.class);
        assertEquals(OBJECT_DECLARED_METHODS.size() + 7 + JACOCO_ADDED_METHOD_COUNT, methods.size());

        methods = getAllDeclaredMethods(TestClass.class, PULIC_METHOD_PREDICATE);
        assertEquals(OBJECT_PUBLIC_METHODS.size() + 1, methods.size());
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

    @Test
    public void test() {
        Method[] methods = TestSubInterface.class.getDeclaredMethods();
        methods = TestInterface.class.getDeclaredMethods();
        System.out.println(methods);
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
