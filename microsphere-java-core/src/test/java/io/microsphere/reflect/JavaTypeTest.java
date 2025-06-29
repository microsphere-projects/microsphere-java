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

import io.microsphere.test.StringIntegerBooleanHashMap;
import io.microsphere.test.StringIntegerHashMap;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.microsphere.reflect.JavaType.EMPTY_JAVA_TYPE_ARRAY;
import static io.microsphere.reflect.JavaType.NULL_JAVA_TYPE;
import static io.microsphere.reflect.JavaType.from;
import static io.microsphere.reflect.JavaType.fromField;
import static io.microsphere.reflect.JavaType.fromMethodParameter;
import static io.microsphere.reflect.JavaType.fromMethodParameters;
import static io.microsphere.reflect.JavaType.fromMethodReturnType;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.core.ResolvableType.forType;

/**
 * {@link JavaType} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class JavaTypeTest {

    /**
     * Type -> ParameterizedType
     * Raw Type = HashMap
     * Generic Types = [ Integer.class ,  ParameterizedType(List<String>>) ]
     * Super Type = ParameterizedType(AbstractMap)
     */
    private static HashMap<Integer, List<String>> mapField;

    private static HashMap<Integer, List<String>> fromValue(HashMap<Integer, List<String>> value) {
        return value;
    }

    @Test
    public void testFromType() {
        JavaType javaType = from(StringIntegerBooleanHashMap.class);
        assertGenericTypes(javaType);

        javaType = javaType.getSuperType();
        assertGenericTypes(javaType, String.class, Integer.class, Boolean.class);

        javaType = from(StringIntegerHashMap.class);
        assertGenericTypes(javaType);

        javaType = javaType.getSuperType();
        assertGenericTypes(javaType, String.class, Integer.class);

        javaType = from(HashMap.class);
        assertGenericTypes(javaType, null, null);

        javaType = javaType.getSuperType();
        assertGenericTypes(javaType, null, null);
    }

    @Test
    public void testFromMethodReturnType() {
        JavaType methodReturnType = fromMethodReturnType(getClass(), "fromValue", HashMap.class);
        assertJavaType(methodReturnType);

        Method method = findMethod(getClass(), "fromValue", HashMap.class);
        methodReturnType = fromMethodReturnType(method);
        assertJavaType(methodReturnType);
    }

    @Test
    public void testFromMethodReturnTypeOnMethodNotFound() {
        JavaType methodReturnType = fromMethodReturnType(getClass(), "fromValue", String.class);
        assertSame(NULL_JAVA_TYPE, methodReturnType);

        methodReturnType = fromMethodReturnType(null);
        assertSame(NULL_JAVA_TYPE, methodReturnType);
    }

    @Test
    public void testFromMethodParameters() {
        JavaType[] javaTypes = fromMethodParameters(getClass(), "fromValue", HashMap.class);
        assertEquals(1, javaTypes.length);
        assertJavaType(javaTypes[0]);

        Method method = findMethod(getClass(), "fromValue", HashMap.class);
        javaTypes = fromMethodParameters(method);
        assertEquals(1, javaTypes.length);
        assertJavaType(javaTypes[0]);
    }

    @Test
    public void testFromMethodParametersOnMethodNotFound() {
        JavaType[] javaTypes = fromMethodParameters(getClass(), "fromValue", String.class);
        assertEquals(0, javaTypes.length);
        assertSame(EMPTY_JAVA_TYPE_ARRAY, javaTypes);
    }

    @Test
    public void testFromMethodParameter() {
        Method method = findMethod(getClass(), "fromValue", HashMap.class);
        JavaType methodParameterType = fromMethodParameter(method, 0);
        assertJavaType(methodParameterType);

        JavaType[] methodParameters = fromMethodParameters(method);
        assertEquals(1, methodParameters.length);
        assertJavaType(methodParameters[0]);
    }

    @Test
    public void testFromField() {
        JavaType javaType = fromField(getClass(), "mapField");
        assertJavaType(javaType);
    }

    @Test
    public void testFromFieldOnNull() {
        JavaType javaType = fromField(getClass(), "notFoundField");
        assertEquals(NULL_JAVA_TYPE, javaType);

        javaType = fromField(null);
        assertEquals(NULL_JAVA_TYPE, javaType);
    }

    @Test
    public void testForClass() throws Throwable {
        testAbstractJavaTypeTest(JavaTypeTestForClass.class);
    }

    @Test
    public void testForGenericArrayType() throws Throwable {
        testAbstractJavaTypeTest(JavaTypeTestForGenericArrayType.class);
    }

    @Test
    public void testForObjectClass() throws Throwable {
        testAbstractJavaTypeTest(JavaTypeTestForObjectClass.class);
    }

    @Test
    public void testForParameterizedType() throws Throwable {
        testAbstractJavaTypeTest(JavaTypeTestForParameterizedType.class);

    }

    @Test
    public void testForTypeVariable() throws Throwable {
        testAbstractJavaTypeTest(JavaTypeTestForTypeVariable.class);
    }

    @Test
    public void testForUnknown() throws Throwable {
        testAbstractJavaTypeTest(JavaTypeTestForUnknown.class);
    }

    @Test
    public void testForWildcardType() throws Throwable {
        testAbstractJavaTypeTest(JavaTypeTestForWildcardType.class);
    }

    @Test
    public void testKindForClass() throws Throwable {
        testAbstractJavaTypeKindTest(JavaTypeKindTestForClass.class);
    }

    @Test
    public void testKindForGenericArrayType() throws Throwable {
        testAbstractJavaTypeKindTest(JavaTypeKindTestForGenericArrayType.class);
    }

    @Test
    public void testKindForParameterizedType() throws Throwable {
        testAbstractJavaTypeKindTest(JavaTypeKindTestForParameterizedType.class);
    }

    @Test
    public void testKindForTypeVariable() throws Throwable {
        testAbstractJavaTypeKindTest(JavaTypeKindTestForTypeVariable.class);
    }

    @Test
    public void testKindForUnknown() throws Throwable {
        testAbstractJavaTypeKindTest(JavaTypeKindTestForUnknown.class);
    }

    @Test
    public void testKindForWildcardType() throws Throwable {
        testAbstractJavaTypeKindTest(JavaTypeKindTestForWildcardType.class);
    }

    private void testAbstractJavaTypeKindTest(Class<? extends AbstractJavaTypeKindTest> testClass) throws Throwable {
        AbstractJavaTypeKindTest test = testClass.getConstructor().newInstance();
        test.testGetSuperType();
        test.testGetRawType();
        test.testGetInterfaces();
        test.testGetGenericTypes();
    }

    private void testAbstractJavaTypeTest(Class<? extends AbstractJavaTypeTest> testClass) throws Throwable {
        AbstractJavaTypeTest.beforeAll();
        AbstractJavaTypeTest test = testClass.getConstructor().newInstance();
        test.init();
        test.test();
    }

    private static void assertGenericTypes(JavaType javaType, Class<?>... expectedClasses) {
        int length = expectedClasses.length;
        // Compare with Spring ResolvableType
        ResolvableType resolvableType = forType(javaType.getType());
        JavaType[] genericTypes = javaType.getGenericTypes();
        Class[] genericsClasses = resolvableType.resolveGenerics();
        assertEquals(length, genericTypes.length);
        assertEquals(genericsClasses.length, genericTypes.length);
        for (int i = 0; i < length; i++) {
            assertEquals(expectedClasses[i], genericsClasses[i]);
            assertEquals(expectedClasses[i], genericTypes[i].getType());
        }
    }

    private static void assertJavaType(JavaType javaType) {
        // assert JavaType.Kind ==  PARAMETERIZED_TYPE
        assertEquals(JavaType.Kind.PARAMETERIZED_TYPE, javaType.getKind());
        // assert Type == ParameterizedType
        Type fieldType = javaType.getType();
        assertTrue(ParameterizedType.class.isInstance(fieldType));
        // assert source
        assertNull(javaType.getSource());
        assertNull(javaType.getRootSource());
        assertTrue(javaType.isSource());
        assertTrue(javaType.isRootSource());

        // toClass -> HashMap.class
        assertEquals(HashMap.class, javaType.toClass());
        // HashMap implements Map<Integer, List<String>>, Cloneable, Serializable
        JavaType[] interfaces = javaType.getInterfaces();
        assertEquals(3, interfaces.length);
        assertEquals(Map.class, javaType.getInterface(0).toClass());
        assertEquals(Cloneable.class, javaType.getInterface(1).toClass());
        assertEquals(Serializable.class, javaType.getInterface(2).toClass());

        // as Cloneable -> JavaType Cloneable : Interface type - Cloneable.class
        JavaType targetType = javaType.as(Cloneable.class);
        assertEquals(JavaType.Kind.CLASS, targetType.getKind());
        assertEquals(Cloneable.class, targetType.getRawType());
        assertEquals(Cloneable.class, targetType.toClass());
        // assert source
        assertEquals(javaType, targetType.getSource());
        assertEquals(javaType, targetType.getRootSource());
        // assert generic types
        assertEquals(0, targetType.getGenericTypes().length);

        // as Map -> JavaType Map<Integer,List<String>> : Super type - AbstractMap -> Map
        targetType = javaType.as(Map.class);
        assertTrue(ParameterizedType.class.isInstance(targetType.getType()));
        assertEquals(Map.class, targetType.getRawType());
        assertEquals(Map.class, targetType.toClass());
        // assert source
        assertEquals(javaType, targetType.getSource());
        assertEquals(javaType, targetType.getRootSource());
        // assert generic types
        assertEquals(2, targetType.getGenericTypes().length);
        assertEquals(Integer.class, targetType.getGenericType(0).toClass());
        assertEquals(List.class, targetType.getGenericType(1).toClass());

        ParameterizedType parameterizedType = (ParameterizedType) javaType.getType();
        // Raw Type = HashMap
        assertEquals(HashMap.class, parameterizedType.getRawType());
        // Generic Types = [ Integer.class ,  ParameterizedType(List<String>>) ]

        JavaType[] genericJavaTypes = javaType.getGenericTypes();
        assertEquals(2, genericJavaTypes.length);
        // First - Integer.class
        JavaType genericJavaType0 = genericJavaTypes[0];
        assertEquals(JavaType.Kind.CLASS, genericJavaType0.getKind());
        assertEquals(Integer.class, genericJavaType0.getType());
        // Second - ParameterizedType(List<String>>)
        //          Raw Type == java.util.List
        JavaType genericJavaType1 = genericJavaTypes[1];
        assertEquals(JavaType.Kind.PARAMETERIZED_TYPE, genericJavaType1.getKind());
        Type genericRawType1 = genericJavaType1.getRawType();
        assertEquals(List.class, genericRawType1);
        // genericType1 == ParameterizedType(List<String>>)
        Type genericType1 = genericJavaType1.getType();
        JavaType[] secondGenericJavaTypes = genericJavaType1.getGenericTypes();
        assertEquals(1, secondGenericJavaTypes.length);
        JavaType secondGenericJavaType0 = secondGenericJavaTypes[0];
        assertEquals(String.class, secondGenericJavaType0.getRawType());

        // Super JavaType = ParameterizedType(AbstractMap<Integer, List<String>>)
        JavaType superJavaType = javaType.getSuperType();
        Type superType = superJavaType.getType();

        assertTrue(ParameterizedType.class.isInstance(superType));
    }
}
