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

import io.microsphere.lang.Prioritized;
import org.junit.jupiter.api.Test;

import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static io.microsphere.management.JmxUtils.getRuntimeMXBean;
import static io.microsphere.reflect.MethodUtils.FINAL_METHOD_PREDICATE;
import static io.microsphere.reflect.MethodUtils.NON_PRIVATE_METHOD_PREDICATE;
import static io.microsphere.reflect.MethodUtils.NON_STATIC_METHOD_PREDICATE;
import static io.microsphere.reflect.MethodUtils.OBJECT_DECLARED_METHODS;
import static io.microsphere.reflect.MethodUtils.OBJECT_PUBLIC_METHODS;
import static io.microsphere.reflect.MethodUtils.PUBLIC_METHOD_PREDICATE;
import static io.microsphere.reflect.MethodUtils.STATIC_METHOD_PREDICATE;
import static io.microsphere.reflect.MethodUtils.buildKey;
import static io.microsphere.reflect.MethodUtils.buildSignature;
import static io.microsphere.reflect.MethodUtils.excludedDeclaredClass;
import static io.microsphere.reflect.MethodUtils.findAllDeclaredMethods;
import static io.microsphere.reflect.MethodUtils.findAllMethods;
import static io.microsphere.reflect.MethodUtils.findDeclaredMethods;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.MethodUtils.findMethods;
import static io.microsphere.reflect.MethodUtils.findNearestOverriddenMethod;
import static io.microsphere.reflect.MethodUtils.findOverriddenMethod;
import static io.microsphere.reflect.MethodUtils.getAllDeclaredMethods;
import static io.microsphere.reflect.MethodUtils.getAllMethods;
import static io.microsphere.reflect.MethodUtils.getDeclaredMethods;
import static io.microsphere.reflect.MethodUtils.getMethods;
import static io.microsphere.reflect.MethodUtils.getSignature;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.reflect.MethodUtils.invokeStaticMethod;
import static io.microsphere.reflect.MethodUtils.isObjectMethod;
import static io.microsphere.reflect.MethodUtils.overrides;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
import static io.microsphere.util.ClassUtils.PRIMITIVE_TYPES;
import static java.lang.Integer.valueOf;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    public void testGetDeclaredMethods() {
        List<Method> methods = getDeclaredMethods(Object.class);
        assertEquals(OBJECT_DECLARED_METHODS, methods);

        methods = getDeclaredMethods(TestClass.class);
        assertEquals(7 + JACOCO_ADDED_METHOD_COUNT, methods.size());
    }

    @Test
    public void testGetMethods() {
        List<Method> methods = getMethods(Object.class);
        assertEquals(OBJECT_PUBLIC_METHODS, methods);

        methods = getMethods(Object.class);
        assertEquals(OBJECT_PUBLIC_METHODS, methods);

        methods = getMethods(TestClass.class);
        assertEquals(1, methods.size());

        methods = getMethods(TestClass.class);
        assertEquals(1, methods.size());
    }

    @Test
    public void testGetAllDeclaredMethods() {
        List<Method> methods = getAllDeclaredMethods(Object.class);
        assertEquals(OBJECT_DECLARED_METHODS, methods);

        methods = getAllDeclaredMethods(TestClass.class);
        assertEquals(OBJECT_DECLARED_METHODS.size() + 7 + JACOCO_ADDED_METHOD_COUNT, methods.size());
    }

    @Test
    public void testGetAllMethods() {
        List<Method> methods = getAllMethods(Object.class);
        assertEquals(OBJECT_PUBLIC_METHODS, methods);

        methods = getAllMethods(Object.class);
        assertEquals(OBJECT_PUBLIC_METHODS, methods);

        methods = getAllMethods(TestClass.class);
        assertEquals(OBJECT_PUBLIC_METHODS.size() + 1, methods.size());
    }

    @Test
    public void testFindDeclaredMethods() {
        List<Method> methods = findDeclaredMethods(Object.class, PUBLIC_METHOD_PREDICATE);
        assertEquals(OBJECT_PUBLIC_METHODS, methods);

        methods = findDeclaredMethods(TestClass.class);
        assertEquals(7 + JACOCO_ADDED_METHOD_COUNT, methods.size());

        methods = findDeclaredMethods(TestClass.class, PUBLIC_METHOD_PREDICATE);
        assertEquals(1 + JACOCO_ADDED_METHOD_COUNT, methods.size());
    }

    @Test
    public void testFindMethods() {
        List<Method> methods = findMethods(Object.class, PUBLIC_METHOD_PREDICATE);
        assertEquals(OBJECT_PUBLIC_METHODS, methods);

        methods = findMethods(Object.class, PUBLIC_METHOD_PREDICATE);
        assertEquals(OBJECT_PUBLIC_METHODS, methods);

        methods = findMethods(TestClass.class);
        assertEquals(1, methods.size());

        methods = findMethods(TestClass.class, PUBLIC_METHOD_PREDICATE);
        assertEquals(1, methods.size());
    }

    @Test
    public void testFindAllDeclaredMethods() {
        List<Method> methods = findAllDeclaredMethods(Object.class, PUBLIC_METHOD_PREDICATE);
        assertEquals(OBJECT_PUBLIC_METHODS, methods);

        methods = findAllDeclaredMethods(TestClass.class);
        assertEquals(OBJECT_DECLARED_METHODS.size() + 7 + JACOCO_ADDED_METHOD_COUNT, methods.size());

        methods = findAllDeclaredMethods(TestClass.class, PUBLIC_METHOD_PREDICATE);
        assertEquals(OBJECT_PUBLIC_METHODS.size() + 1, methods.size());
    }

    @Test
    public void testFindAllMethods() {
        List<Method> methods = findAllMethods(Object.class, PUBLIC_METHOD_PREDICATE);
        assertEquals(OBJECT_PUBLIC_METHODS, methods);

        methods = findAllMethods(Object.class, PUBLIC_METHOD_PREDICATE);
        assertEquals(OBJECT_PUBLIC_METHODS, methods);

        methods = getAllMethods(TestClass.class);
        assertEquals(OBJECT_PUBLIC_METHODS.size() + 1, methods.size());
    }

    @Test
    void testFindMethodsOnNull() {
        testFindMethodsOnEmptyList(null);
    }

    @Test
    public void testFindMethodsOnPrimitive() {
        PRIMITIVE_TYPES.forEach(primitiveType -> {
            testFindMethodsOnEmptyList(primitiveType);
        });
    }

    @Test
    public void testFindMethodsOnArray() {
        Class<?> arrayClass = EMPTY_CLASS_ARRAY.getClass();
        List<Method> methods = findMethods(arrayClass, true, true);
        assertEquals(OBJECT_PUBLIC_METHODS, methods);

        methods = findMethods(arrayClass, false, true);
        assertEquals(OBJECT_PUBLIC_METHODS, methods);

        methods = findMethods(arrayClass, true, false);
        assertEquals(OBJECT_PUBLIC_METHODS, methods);

        methods = findMethods(arrayClass, false, false);
        assertEquals(OBJECT_PUBLIC_METHODS, methods);
    }

    @Test
    public void testFindMethodsOnClass() {
        List<Method> objectMethods = findMethods(Object.class, true, true);

        List<Method> methods = findMethods(TestClass.class, true, true);
        assertEquals(1 + objectMethods.size(), methods.size());

        methods = findMethods(TestClass.class, false, true);
        assertEquals(1, methods.size());

        methods = findMethods(TestClass.class, false, false);
        assertEquals(7 + JACOCO_ADDED_METHOD_COUNT, methods.size());

        methods = findMethods(TestClass.class, true, false);
        objectMethods = findMethods(Object.class, true, false);

        assertEquals(7 + JACOCO_ADDED_METHOD_COUNT + objectMethods.size(), methods.size());
    }

    @Test
    public void testFindMethodsFromInterface() {
        List<Method> methods = findMethods(TestInterface.class, true, true);
        assertEquals(2, methods.size()); // method + useLambda

        methods = findMethods(TestInterface.class, true, false);
        assertEquals(3 + JACOCO_ADDED_METHOD_COUNT, methods.size()); // method + useLambda + generated lambda method by compiler

        List<Method> subMethods = findMethods(TestSubInterface.class, false, true);
        assertEquals(1, subMethods.size()); // subMethod

        subMethods = findMethods(TestSubInterface.class, true, true);
        assertEquals(3, subMethods.size()); // method + useLambda + subMethod
    }

    @Test
    public void testFindMethodWithoutParameterTypes() {
        assertNotNull(findMethod(Object.class, "getClass"));
        assertNotNull(findMethod(Object.class, "hashCode"));
        assertNotNull(findMethod(Object.class, "clone"));
        assertNotNull(findMethod(Object.class, "toString"));
        assertNotNull(findMethod(Object.class, "notify"));
        assertNotNull(findMethod(Object.class, "notifyAll"));
        assertNotNull(findMethod(Object.class, "wait"));
    }

    @Test
    public void testFindMethodWithoutParameterTypesOnNotFound() {
        assertNull(findMethod(Object.class, "equals"));
    }

    @Test
    public void testFindMethodWithParameterTypes() {
        assertNotNull(findMethod(Object.class, "equals", Object.class));
        assertNotNull(findMethod(Object.class, "wait", long.class));
        assertNotNull(findMethod(Object.class, "wait", long.class, int.class));
    }

    @Test
    public void testFindMethodWithParameterTypesOnNotFound() {
        assertNull(findMethod(Object.class, "equals", int.class));
        assertNull(findMethod(Object.class, "wait", Object.class));
        assertNull(findMethod(Object.class, "notFound"));
    }

    @Test
    public void testFindMethodOnSuperTypes() {
        assertNotNull(findMethod(TestClass.class, "equals", Object.class));
        assertNotNull(findMethod(TestClass.class, "wait", long.class));
        assertNotNull(findMethod(TestClass.class, "wait", long.class, int.class));

        assertNotNull(findMethod(TestInterface.class, "equals", Object.class));
        assertNotNull(findMethod(TestInterface.class, "wait", long.class));
        assertNotNull(findMethod(TestInterface.class, "wait", long.class, int.class));

        assertNotNull(findMethod(TestSubInterface.class, "equals", Object.class));
        assertNotNull(findMethod(TestSubInterface.class, "wait", long.class));
        assertNotNull(findMethod(TestSubInterface.class, "wait", long.class, int.class));
    }

    @Test
    public void testFindMethodOnSuperTypesOnNotFound() {
        assertNull(findMethod(TestClass.class, "equals", int.class));
        assertNull(findMethod(TestClass.class, "wait", Object.class));
        assertNull(findMethod(TestClass.class, "notFound"));

        assertNull(findMethod(TestInterface.class, "equals", int.class));
        assertNull(findMethod(TestInterface.class, "wait", Object.class));
        assertNull(findMethod(TestInterface.class, "notFound"));

        assertNull(findMethod(TestSubInterface.class, "equals", int.class));
        assertNull(findMethod(TestSubInterface.class, "wait", Object.class));
        assertNull(findMethod(TestSubInterface.class, "notFound"));
    }

    @Test
    public void testFindMethodOnSuperInterfaces() {
        assertNotNull(findMethod(TestSubInterface.class, "method"));
        assertNotNull(findMethod(TestSubInterface.class, "useLambda"));
    }

    @Test
    public void testFindMethodOnSuperInterfacesOnNotFound() {
        assertNull(findMethod(TestInterface.class, "equals", int.class));
        assertNull(findMethod(TestInterface.class, "wait", Object.class));
        assertNull(findMethod(TestInterface.class, "notFound"));

        assertNull(findMethod(TestSubInterface.class, "equals", int.class));
        assertNull(findMethod(TestSubInterface.class, "wait", Object.class));
        assertNull(findMethod(TestSubInterface.class, "notFound"));
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
    public void testInvokeMethodOnNullPointerException() {
        assertThrows(NullPointerException.class, () -> invokeMethod("test", (Method) null));
    }

    @Test
    public void testInvokeMethodOnIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> invokeMethod("test", "notFound"));
    }

    @Test
    public void testInvokeMethodOnIllegalArgumentException() {
        Method method = findMethod(String.class, "toString");
        assertThrows(IllegalArgumentException.class, () -> invokeMethod("test", method, "abc"));
    }

    @Test
    public void testInvokeMethodOnInvocationTargetException() {
        assertThrows(RuntimeException.class, () -> invokeMethod(new ReflectionTest(), "errorMethod"));
    }

    @Test
    public void testInvokeStaticMethod() {
        Method method = findMethod(Integer.class, "valueOf", int.class);
        assertEquals(valueOf(0), (Integer) invokeStaticMethod(method, 0));
        assertEquals(valueOf(0), (Integer) invokeStaticMethod(TestClass.class, "value", 0));
    }

    @Test
    public void testOverridesOnNull() {
        assertFalse(overrides(null, null));

        Method method = findMethod(Integer.class, "valueOf", int.class);

        assertFalse(overrides(method, null));
        assertFalse(overrides(null, method));
    }

    @Test
    public void testOverridesOnSame() {
        Method method = findMethod(String.class, "toString");
        assertFalse(overrides(method, method));
    }

    @Test
    public void testOverridesOnDifferentMethodName() {
        Method hashCodeMethod = findMethod(Object.class, "hashCode");
        Method toStringMethod = findMethod(String.class, "toString");
        assertFalse(overrides(hashCodeMethod, toStringMethod));
        assertFalse(overrides(toStringMethod, hashCodeMethod));
    }

    @Test
    public void testOverridesOnStaticMethod() {
        Method valueOfMethod1 = findMethod(Integer.class, "valueOf", int.class);
        Method valueOfMethod2 = findMethod(String.class, "valueOf", int.class);
        assertFalse(overrides(valueOfMethod1, valueOfMethod2));
        assertFalse(overrides(valueOfMethod2, valueOfMethod1));
    }

    @Test
    public void testOverridesOnPrivate() {
        Method privateMethod = findMethod(ReflectionTest.class, "method", int.class);
        Method publicMethod = findMethod(ReflectionTest.class, "method", String.class);
        assertFalse(overrides(privateMethod, publicMethod));
        assertFalse(overrides(publicMethod, privateMethod));
    }

    @Test
    public void testOverridesOnDefaultMethod() {
        Method compareToMethod1 = findMethod(Prioritized.class, "compareTo", Object.class);
        Method compareToMethod2 = findMethod(Comparable.class, "compareTo", Object.class);
        assertFalse(overrides(compareToMethod1, compareToMethod2));
    }


    @Test
    public void testOverridesOnSameDeclaringClass() {
        Method waitMethod1 = findMethod(Object.class, "wait");
        Method waitMethod2 = findMethod(Object.class, "wait", long.class);
        assertFalse(overrides(waitMethod1, waitMethod2));
    }

    @Test
    public void testOverridesOnDifferentDeclaringClass() {
        Method toStringMethod1 = findMethod(Integer.class, "toString");
        Method toStringMethod2 = findMethod(String.class, "toString");
        assertFalse(overrides(toStringMethod1, toStringMethod2));
    }

    @Test
    public void testOverridesOnDifferentParameterCount() {
        Method appendMethod1 = findMethod(StringBuilder.class, "append", CharSequence.class);
        Method appendMethod2 = findMethod(Appendable.class, "append", CharSequence.class, int.class, int.class);
        assertFalse(overrides(appendMethod1, appendMethod2));
    }

    @Test
    public void testOverridesOnDifferentParameterTypes() {
        Method appendMethod1 = findMethod(StringBuilder.class, "append", CharSequence.class);
        Method appendMethod2 = findMethod(Appendable.class, "append", char.class);
        assertFalse(overrides(appendMethod1, appendMethod2));
    }

    @Test
    public void testOverrides() {
        Method overrider = findMethod(List.class, "size");
        Method overridden = findMethod(Collection.class, "size");

        assertTrue(overrides(overrider, overridden));
        assertFalse(overrides(overridden, overrider));
    }

    @Test
    public void testFindOverriddenMethod() {
        Method overrider = findMethod(List.class, "size");
        Method overridden = findMethod(Collection.class, "size");
        assertEquals(overridden, findOverriddenMethod(overrider, Collection.class));
    }

    @Test
    public void testFindOverriddenMethodOnNotFound() {
        Method overrider = findMethod(List.class, "size");
        assertNull(findOverriddenMethod(overrider, Object.class));
    }

    @Test
    public void testFindNearestOverriddenMethod() {
        Method overrider = findMethod(List.class, "size");
        Method overridden = findMethod(Collection.class, "size");
        assertEquals(overridden, findNearestOverriddenMethod(overrider));

        overrider = findMethod(ArrayList.class, "size");
        overridden = findMethod(AbstractList.class, "size");
        assertEquals(overridden, findNearestOverriddenMethod(overrider));
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

    @Test
    public void testIsObjectMethod() {
        assertIsObjectMethod(true, Object.class, "toString");
        assertIsObjectMethod(false, String.class, "toString");
        assertIsObjectMethod(false, String.class, "notFound");
    }

    private void assertIsObjectMethod(boolean expected, Class<?> declaredClass, String methodName, Class<?>... parameterTypes) {
        Method method = findMethod(declaredClass, methodName, parameterTypes);
        assertEquals(expected, isObjectMethod(method));
    }

    @Test
    public void testBuildKey() {
        assertMethodKey(String.class, "toString");
        assertMethodKey(ReflectionTest.class, "publicMethod", int.class);
        assertMethodKey(Appendable.class, "append", CharSequence.class, int.class, int.class);
    }

    private void assertMethodKey(Class<?> declaredClass, String methodName, Class<?>... parameterTypes) {
        MethodUtils.MethodKey methodKey1 = buildKey(declaredClass, methodName, parameterTypes);
        MethodUtils.MethodKey methodKey2 = buildKey(declaredClass, methodName, parameterTypes.length == 0 ? null : parameterTypes);

        assertEquals(methodKey1, methodKey2);
        assertEquals(methodKey1.hashCode(), methodKey2.hashCode());
        assertEquals(methodKey1.toString(), methodKey2.toString());
        assertEquals(methodKey1.toString(), buildSignature(methodKey1.declaredClass, methodKey1.methodName, methodKey1.parameterTypes));
    }

    private void testFindMethodsOnEmptyList(Class<?> type) {
        List<Method> methods = findMethods(type, true, true);
        assertSame(emptyList(), methods);

        methods = findMethods(type, true, false);
        assertSame(emptyList(), methods);

        methods = findMethods(type, false, true);
        assertSame(emptyList(), methods);

        methods = findMethods(type, false, false);
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
