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

import io.microsphere.reflect.JavaType.Kind;
import io.microsphere.test.C;
import io.microsphere.test.D;
import org.junit.jupiter.api.Test;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import static io.microsphere.reflect.JavaType.Kind.CLASS;
import static io.microsphere.reflect.JavaType.Kind.GENERIC_ARRAY_TYPE;
import static io.microsphere.reflect.JavaType.Kind.PARAMETERIZED_TYPE;
import static io.microsphere.reflect.JavaType.Kind.TYPE_VARIABLE;
import static io.microsphere.reflect.JavaType.Kind.UNKNOWN;
import static io.microsphere.reflect.JavaType.Kind.WILDCARD_TYPE;
import static io.microsphere.reflect.JavaType.Kind.valueOf;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link Kind} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class JavaTypeKindTest {

    /**
     * ParameterizedType : {@link C<String>}
     *
     * @see C
     */
    static final Type C_STRING_PARAMETERIZED_TYPE = D.class.getGenericSuperclass();

    static final ParameterizedType TEST_PARAMETERIZED_TYPE = (ParameterizedType) findMethod(Object.class, "getClass").getGenericReturnType();

    static final WildcardType TEST_WILDCARD_TYPE = (WildcardType) TEST_PARAMETERIZED_TYPE.getActualTypeArguments()[0];

    static final GenericArrayType TEST_GENERIC_ARRAY_TYPE = (GenericArrayType) findMethod(Class.class, "getClasses").getGenericReturnType();

    @Test
    void testValueOf() {
        assertSame(CLASS, valueOf(String.class));
        assertSame(PARAMETERIZED_TYPE, valueOf(C_STRING_PARAMETERIZED_TYPE));
        assertSame(TYPE_VARIABLE, valueOf(C.class.getTypeParameters()[0]));
        assertSame(WILDCARD_TYPE, valueOf(TEST_WILDCARD_TYPE));
        assertSame(GENERIC_ARRAY_TYPE, valueOf(TEST_GENERIC_ARRAY_TYPE));
        assertSame(UNKNOWN, valueOf((Type) null));
    }
}
