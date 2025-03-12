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

import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static io.microsphere.management.JmxUtils.getRuntimeMXBean;
import static io.microsphere.reflect.MethodUtils.FINAL_METHOD_PREDICATE;
import static io.microsphere.reflect.MethodUtils.NON_PRIVATE_METHOD_PREDICATE;
import static io.microsphere.reflect.MethodUtils.NON_STATIC_METHOD_PREDICATE;
import static io.microsphere.reflect.MethodUtils.STATIC_METHOD_PREDICATE;
import static io.microsphere.reflect.MethodUtils.OBJECT_DECLARED_METHODS;
import static io.microsphere.reflect.MethodUtils.OBJECT_PUBLIC_METHODS;
import static io.microsphere.reflect.MethodUtils.PUBLIC_METHOD_PREDICATE;
import static io.microsphere.reflect.MethodUtils.excludedDeclaredClass;
import static io.microsphere.reflect.MethodUtils.filterMethods;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.MethodUtils.getAllDeclaredMethods;
import static io.microsphere.reflect.MethodUtils.getDeclaredMethods;
import static io.microsphere.reflect.MethodUtils.getSignature;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.reflect.MethodUtils.invokeStaticMethod;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
import static io.microsphere.util.ClassUtils.PRIMITIVE_TYPES;
import static java.lang.Integer.valueOf;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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


    @Test
    public void testSTATIC_METHOD_PREDICATE() {
        assertTrue(STATIC_METHOD_PREDICATE.test(findMethod(ReflectionTest.class, "staticMethod")));
    }

    @Test
    public void testNON_STATIC_METHOD_PREDICATE() {
        assertTrue(NON_STATIC_METHOD_PREDICATE.test(findMethod(ReflectionTest.class, "privateMethod")));
    }

    @Test
    public void testFINAL_METHOD_PREDICATE() {
        assertTrue(FINAL_METHOD_PREDICATE.test(findMethod(ReflectionTest.class, "errorMethod")));
    }

    @Test
    public void testPUBLIC_METHOD_PREDICATE() {
        assertTrue(MemberUtils.PUBLIC_MEMBER_PREDICATE.test(findMethod(ReflectionTest.class, "publicMethod", int.class)));
    }

    @Test
    public void testNON_PRIVATE_METHOD_PREDICATE() {
        assertTrue(NON_PRIVATE_METHOD_PREDICATE.test(findMethod(ReflectionTest.class, "publicMethod", int.class)));
        assertTrue(NON_PRIVATE_METHOD_PREDICATE.test(findMethod(ReflectionTest.class, "protectedMethod", Object[].class)));
        assertTrue(NON_PRIVATE_METHOD_PREDICATE.test(findMethod(ReflectionTest.class, "packagePrivateMethod", String.class)));
    }

    @Test
    public void testExcludedDeclaredClass() {
        Predicate<? super Method> predicate = excludedDeclaredClass(Object.class);
        for (Method method : ReflectionTest.class.getDeclaredMethods()) {
            assertTrue(predicate.test(method));
        }
    }

    @Test
    void testFilterMethodsOnNull() {
        testFilterMethodsOnEmptyList(null);

//        methods = getMethods(null);
//        assertSame(emptyList(), methods);
    }

    @Test
    public void testFilterMethodsOnPrimitive() {

        PRIMITIVE_TYPES.forEach(primitiveType -> {
            testFilterMethodsOnEmptyList(primitiveType);
        });

//        methods = getMethods(primitiveType);
//        assertEquals(emptyList(), methods);
    }

    @Test
    public void testFilterMethodsOnArray() {
        Class<?> arrayClass = EMPTY_CLASS_ARRAY.getClass();
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
    void testFilterMethodsOnClass() {
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

        methods = getDeclaredMethods(Object.class, PUBLIC_METHOD_PREDICATE);
        assertEquals(OBJECT_PUBLIC_METHODS, methods);

        methods = getDeclaredMethods(TestClass.class);
        assertEquals(7 + JACOCO_ADDED_METHOD_COUNT, methods.size());

        methods = getDeclaredMethods(TestClass.class, PUBLIC_METHOD_PREDICATE);
        assertEquals(1, methods.size());
    }

    @Test
    public void testGetAllDeclaredMethods() {
        List<Method> methods = getAllDeclaredMethods(Object.class);
        assertEquals(OBJECT_DECLARED_METHODS, methods);

        methods = getAllDeclaredMethods(Object.class, PUBLIC_METHOD_PREDICATE);
        assertEquals(OBJECT_PUBLIC_METHODS, methods);

        methods = getAllDeclaredMethods(TestClass.class);
        assertEquals(OBJECT_DECLARED_METHODS.size() + 7 + JACOCO_ADDED_METHOD_COUNT, methods.size());

        methods = getAllDeclaredMethods(TestClass.class, PUBLIC_METHOD_PREDICATE);
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

    private void testFilterMethodsOnEmptyList(Class<?> type) {
        List<Method> methods = filterMethods(type, true, true);
        assertSame(emptyList(), methods);

        methods = filterMethods(type, true, false);
        assertSame(emptyList(), methods);

        methods = filterMethods(type, false, true);
        assertSame(emptyList(), methods);

        methods = filterMethods(type, false, false);
        assertSame(emptyList(), methods);
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
