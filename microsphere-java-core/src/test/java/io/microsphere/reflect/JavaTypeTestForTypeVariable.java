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

import java.lang.reflect.TypeVariable;

import static io.microsphere.reflect.JavaType.EMPTY_JAVA_TYPE_ARRAY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link BaseJavaTypeTest} for {@link TypeVariable}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see JavaType
 * @see JavaType.Kind#TYPE_VARIABLE
 * @see TypeVariable
 * @since 1.0.0
 */
public class JavaTypeTestForTypeVariable<T> extends BaseJavaTypeTest<T> {

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
        testAs(A.class);
        testAs(B.class);
        testAs(C.class);
        testAs(D.class);
        testAs(String.class);
    }

    private void testAs(Class<?> type) {
        JavaType aType = javaType.as(type);
        assertNull(aType);
    }

}
