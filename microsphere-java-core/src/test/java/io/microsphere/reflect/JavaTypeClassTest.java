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

import io.microsphere.test.D;

import java.lang.reflect.Type;

import static io.microsphere.reflect.JavaType.Kind.CLASS;
import static io.microsphere.reflect.JavaType.Kind.PARAMETERIZED_TYPE;
import static io.microsphere.reflect.JavaType.from;
import static io.microsphere.reflect.JavaTypeKindTest.C_STRING_PARAMETERIZED_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link BaseJavaTypeTest} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class JavaTypeClassTest extends BaseJavaTypeTest {

    @Override
    protected Type type() {
        return D.class;
    }

    @Override
    protected JavaType.Kind kind() {
        return CLASS;
    }

    @Override
    protected JavaType source() {
        return from(getClass());
    }

    @Override
    protected void testGetSuperType(JavaType superType) {
        assertEquals(from(C_STRING_PARAMETERIZED_TYPE, PARAMETERIZED_TYPE, superType.getSource()), superType);
    }

    @Override
    protected void testGetInterfaces(JavaType[] interfaces) {

    }

    @Override
    protected void testGetGenericTypes(JavaType[] genericTypes) {

    }

    @Override
    protected void testGetGenericType(JavaType genericType, int i) {

    }

    @Override
    protected void testAs() {

    }

    @Override
    protected void testGetRootSource(JavaType rootSource) {

    }
}
