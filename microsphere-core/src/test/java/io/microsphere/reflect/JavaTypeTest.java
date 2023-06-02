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

import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * {@ink JavaType} Test
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
    public void testFromMethod() {
        Method method = MethodUtils.findMethod(getClass(), "fromValue", HashMap.class);
        JavaType methodReturnType = JavaType.fromMethodReturnType(method);
        assertJavaType(methodReturnType);

        JavaType methodParameterType = JavaType.fromMethodParameter(method, 0);
        assertJavaType(methodParameterType);
    }

    @Test
    public void testFromField() {
        Field field = FieldUtils.findField(getClass(), "mapField");
        JavaType javaType = JavaType.from(field);
        assertJavaType(javaType);

        Type[] types = javaType.getKind().getGenericTypes(javaType);
        assertTrue(types.length == 2);

        javaType = javaType.as(Map.class);
        types = javaType.getKind().getGenericTypes(javaType);
        assertTrue(types.length == 2);
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
