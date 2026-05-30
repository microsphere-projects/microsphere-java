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

import io.microsphere.test.C;

import java.lang.reflect.GenericArrayType;

import static io.microsphere.reflect.JavaType.EMPTY_JAVA_TYPE_ARRAY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link AbstractJavaTypeTest} for {@link GenericArrayType}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see JavaType
 * @see JavaType.Kind#GENERIC_ARRAY_TYPE
 * @see GenericArrayType
 * @since 1.0.0
 */
class JavaTypeTestForGenericArrayType extends AbstractJavaTypeTest<C<String>[]> {

    @Override
    protected void testGetSuperType(JavaType superType) {
        assertNull(superType);
    }

    @Override
    protected void testGetInterfaces(JavaType[] interfaces) {
        assertArrayEquals(EMPTY_JAVA_TYPE_ARRAY, interfaces);
    }

    @Override
    protected void testGetGenericTypes(JavaType[] genericTypes) {
        assertArrayEquals(EMPTY_JAVA_TYPE_ARRAY, genericTypes);
    }

    @Override
    protected void testGetGenericType(JavaType genericType, int i) {
    }

    @Override
    protected void testAs() {
        JavaType type = javaType.as(null);
        assertNull(type);
    }

}
