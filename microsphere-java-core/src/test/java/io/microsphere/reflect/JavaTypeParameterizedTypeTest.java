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

import io.microsphere.test.A;
import io.microsphere.test.B;
import io.microsphere.test.C;
import io.microsphere.test.D;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.RandomAccess;

import static io.microsphere.reflect.JavaType.EMPTY_JAVA_TYPE_ARRAY;
import static io.microsphere.reflect.JavaType.Kind.CLASS;
import static io.microsphere.reflect.JavaType.Kind.PARAMETERIZED_TYPE;
import static io.microsphere.reflect.JavaType.Kind.UNKNOWN;
import static io.microsphere.reflect.JavaType.from;
import static io.microsphere.reflect.generics.ParameterizedTypeImpl.of;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link BaseJavaTypeTest} for {@link ParameterizedType}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see D
 * @since 1.0.0
 */
public class JavaTypeParameterizedTypeTest extends BaseJavaTypeTest<C<String>> {

    @Override
    protected void testGetSuperType(JavaType superType) {
        assertEquals(from(B.class, CLASS, superType.getSource()), superType);
    }

    @Override
    protected void testGetInterfaces(JavaType[] interfaces) {
        assertArrayEquals(ofArray(from(RandomAccess.class, CLASS, javaType)), interfaces);
    }

    @Override
    protected void testGetGenericTypes(JavaType[] genericTypes) {
        assertEquals(1, genericTypes.length);
    }

    @Override
    protected void testGetGenericType(JavaType genericType, int i) {
        assertEquals(from(String.class, CLASS, javaType), genericType);
        assertEquals(0, i);
    }

    @Override
    protected void testGetRootSource(JavaType rootSource) {
        assertEquals(source(), rootSource);
    }

    @Override
    protected void testAs() {
        testB();
        testA();
        testObject();
    }

    private void testB() {
        JavaType bType = javaType.as(B.class);

        // test source
        assertEquals(javaType, bType.getSource());
        assertEquals(source(), bType.getRootSource());

        // test super type
        assertEquals(from(A.class, CLASS, bType), bType.getSuperType());
        assertEquals(from(A.class, CLASS, bType), bType.getSuperType());

        // test interfaces
        assertArrayEquals(ofArray(from(of(Comparable.class, B.class), PARAMETERIZED_TYPE, bType)), bType.getInterfaces());
        assertArrayEquals(ofArray(from(of(Comparable.class, B.class), PARAMETERIZED_TYPE, bType)), bType.getInterfaces());

        // test generic types
        assertArrayEquals(EMPTY_JAVA_TYPE_ARRAY, bType.getGenericTypes());
        assertArrayEquals(EMPTY_JAVA_TYPE_ARRAY, bType.getGenericTypes());
    }


    private void testA() {
        JavaType aType = javaType.as(A.class);

        // test source
        assertEquals(javaType, aType.getSource());
        assertEquals(source(), aType.getRootSource());

        // test super type
        assertEquals(from(Object.class, CLASS, aType), aType.getSuperType());
        assertEquals(from(Object.class, CLASS, aType), aType.getSuperType());

        // test interfaces
        assertArrayEquals(ofArray(from(Serializable.class, CLASS, aType)), aType.getInterfaces());
        assertArrayEquals(ofArray(from(Serializable.class, CLASS, aType)), aType.getInterfaces());

        // test generic types
        assertArrayEquals(EMPTY_JAVA_TYPE_ARRAY, aType.getGenericTypes());
        assertArrayEquals(EMPTY_JAVA_TYPE_ARRAY, aType.getGenericTypes());
    }

    private void testObject() {
        JavaType objectType = javaType.as(Object.class);

        // test source
        assertEquals(javaType, objectType.getSource());
        assertEquals(source(), objectType.getRootSource());

        // test super type
        assertEquals(from(null, UNKNOWN, objectType), objectType.getSuperType());
        assertEquals(from(null, UNKNOWN, objectType), objectType.getSuperType());

        // test interfaces
        assertArrayEquals(EMPTY_JAVA_TYPE_ARRAY, objectType.getInterfaces());
        assertArrayEquals(EMPTY_JAVA_TYPE_ARRAY, objectType.getInterfaces());

        // test generic types
        assertArrayEquals(EMPTY_JAVA_TYPE_ARRAY, objectType.getGenericTypes());
        assertArrayEquals(EMPTY_JAVA_TYPE_ARRAY, objectType.getGenericTypes());
    }

}
