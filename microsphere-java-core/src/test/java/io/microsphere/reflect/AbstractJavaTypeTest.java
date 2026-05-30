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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractList;
import java.util.Collection;

import static io.microsphere.reflect.JavaType.EMPTY_JAVA_TYPE_ARRAY;
import static io.microsphere.reflect.JavaType.Kind;
import static io.microsphere.reflect.JavaType.Kind.CLASS;
import static io.microsphere.reflect.JavaType.Kind.GENERIC_ARRAY_TYPE;
import static io.microsphere.reflect.JavaType.Kind.PARAMETERIZED_TYPE;
import static io.microsphere.reflect.JavaType.Kind.TYPE_VARIABLE;
import static io.microsphere.reflect.JavaType.Kind.UNKNOWN;
import static io.microsphere.reflect.JavaType.Kind.WILDCARD_TYPE;
import static io.microsphere.reflect.JavaType.Kind.valueOf;
import static io.microsphere.reflect.JavaType.NULL_JAVA_TYPE;
import static io.microsphere.reflect.JavaType.OBJECT_JAVA_TYPE;
import static io.microsphere.reflect.JavaType.from;
import static io.microsphere.reflect.JavaType.searchInterfaceType;
import static java.util.Objects.hash;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Base {@link JavaType} for {@link Class}
 *
 * @param <T> the type to be tested
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class AbstractJavaTypeTest<T> {

    protected JavaType javaType;

    private Type type;

    @BeforeAll
    static void beforeAll() {
        testEmptyJavaTypeArray();
        testObjectJavaType();
        testNullJavaType();
        testSearchInterfaceType();
    }

    private static void testEmptyJavaTypeArray() {
        assertArrayEquals(new JavaType[0], EMPTY_JAVA_TYPE_ARRAY);
    }

    private static void testObjectJavaType() {
        assertEquals(from(Object.class, CLASS), OBJECT_JAVA_TYPE);
        assertEquals(Object.class, OBJECT_JAVA_TYPE.getType());
        assertEquals(CLASS, OBJECT_JAVA_TYPE.getKind());
        assertNull(OBJECT_JAVA_TYPE.getSource());
    }

    private static void testNullJavaType() {
        assertEquals(from(null, UNKNOWN), NULL_JAVA_TYPE);
        assertNull(NULL_JAVA_TYPE.getType());
        assertEquals(UNKNOWN, NULL_JAVA_TYPE.getKind());
        assertNull(NULL_JAVA_TYPE.getSource());
    }

    private static void testSearchInterfaceType() {
        assertNotNull(searchInterfaceType(Collection.class, AbstractList.class));
        assertNull(searchInterfaceType(Serializable.class, AbstractList.class));
    }

    public AbstractJavaTypeTest() {
        this.type = resolveType();
    }

    protected Type resolveType() {
        ParameterizedType superType = (ParameterizedType) this.getClass().getGenericSuperclass();
        Type superRawType;
        while ((superRawType = superType.getRawType()) != AbstractJavaTypeTest.class) {
            superType = (ParameterizedType) ((Class) superRawType).getGenericSuperclass();
        }
        return superType.getActualTypeArguments()[0];
    }

    @BeforeEach
    void setUp() {
        javaType = createJavaType();
    }

    protected JavaType createJavaType() {
        return from(type(), kind(), source());
    }

    protected Type type() {
        return type;
    }

    protected Kind kind() {
        return valueOf(type());
    }

    protected JavaType source() {
        return from(this.getClass());
    }

    @Test
    void test() {
        testType();
        testKind();
        testSource();
        testGetRootSource(javaType.getRootSource());
        testEquals();
        testHashCode();
        testToString();
        testGetSuperType(javaType.getSuperType());
        testGetInterfaces(javaType.getInterfaces());
        JavaType[] genericTypes = javaType.getGenericTypes();
        testGetGenericTypes(genericTypes);
        for (int i = 0; i < genericTypes.length; i++) {
            testGetGenericType(genericTypes[i], i);
        }
        testAs();
    }

    protected void testKind() {
        Kind kind = javaType.getKind();
        assertEquals(kind(), kind);
        assertEquals(CLASS == kind, javaType.isClass());
        assertEquals(PARAMETERIZED_TYPE == kind, javaType.isParameterizedType());
        assertEquals(TYPE_VARIABLE == kind, javaType.isTypeVariable());
        assertEquals(WILDCARD_TYPE == kind, javaType.isWildCardType());
        assertEquals(GENERIC_ARRAY_TYPE == kind, javaType.isGenericArrayType());
        assertEquals(UNKNOWN == kind, javaType.isUnknownType());
    }

    protected void testType() {
        Type type = type();
        assertEquals(type, javaType.getType());
        Kind kind = javaType.getKind();
        switch (kind) {
            case CLASS:
                assertEquals(type, javaType.getRawType());
                assertEquals(type, javaType.toClass());
                assertNull(javaType.toParameterizedType());
                assertNull(javaType.toTypeVariable());
                assertNull(javaType.toWildcardType());
                assertNull(javaType.toGenericArrayType());
                break;
            case PARAMETERIZED_TYPE:
                assertEquals(((ParameterizedType) type).getRawType(), javaType.getRawType());
                assertNotNull(javaType.toClass());
                assertEquals(type, javaType.toParameterizedType());
                assertNull(javaType.toTypeVariable());
                assertNull(javaType.toWildcardType());
                assertNull(javaType.toGenericArrayType());
                break;
            case TYPE_VARIABLE:
                assertNull(javaType.getRawType());
                assertNull(javaType.toClass());
                assertNull(javaType.toParameterizedType());
                assertEquals(type, javaType.toTypeVariable());
                assertNull(javaType.toWildcardType());
                assertNull(javaType.toGenericArrayType());
                break;
            case WILDCARD_TYPE:
                assertNull(javaType.getRawType());
                assertNull(javaType.toClass());
                assertNull(javaType.toParameterizedType());
                assertNull(javaType.toTypeVariable());
                assertEquals(type, javaType.toWildcardType());
                assertNull(javaType.toGenericArrayType());
                break;
            case GENERIC_ARRAY_TYPE:
                assertNull(javaType.getRawType());
                assertNull(javaType.toClass());
                assertNull(javaType.toParameterizedType());
                assertNull(javaType.toTypeVariable());
                assertNull(javaType.toWildcardType());
                assertEquals(type, javaType.toGenericArrayType());
                break;
        }
    }

    protected void testSource() {
        JavaType source = source();
        assertEquals(source, javaType.getSource());
        assertEquals(source == null, javaType.isRootSource());
        assertEquals(source == null, javaType.isSource());
        assertTrue(source.isSource());
    }

    protected void testGetRootSource(JavaType rootSource) {
        assertEquals(source(), rootSource);
    }

    protected void testEquals() {
        Type type = type();
        Kind kind = kind();
        JavaType source = source();

        assertEquals(from(type, kind, source), javaType);
        assertEquals(source == null, from(type, kind).equals(javaType));
        assertNotEquals(from(type, kind, javaType), javaType);
        assertNotEquals(from(type, kind), javaType);
        assertNotEquals(from(type), javaType);

        assertNotEquals(from(type, UNKNOWN), javaType);

        assertNotEquals(type, javaType);
        assertNotEquals(javaType, type);
        assertNotEquals(null, javaType);
        assertNotEquals(javaType, null);
    }

    protected void testHashCode() {
        Type type = type();
        Kind kind = kind();
        JavaType source = source();
        assertEquals(hash(type, kind, source), javaType.hashCode());
    }

    protected void testToString() {
        Type type = type();
        Kind kind = kind();
        JavaType source = source();
        StringBuilder sb = new StringBuilder("JavaType{");
        sb.append("type=").append(type);
        sb.append(", kind=").append(kind);
        sb.append(", source=").append(source);
        sb.append('}');
        assertEquals(sb.toString(), javaType.toString());
    }

    protected abstract void testGetSuperType(JavaType superType);

    protected abstract void testGetInterfaces(JavaType[] interfaces);

    protected abstract void testGetGenericTypes(JavaType[] genericTypes);

    protected abstract void testGetGenericType(JavaType genericType, int i);

    protected abstract void testAs();

}
