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
import io.microsphere.test.A;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;

import static io.microsphere.reflect.JavaType.Kind.CLASS;
import static io.microsphere.reflect.JavaType.from;
import static io.microsphere.util.ArrayUtils.EMPTY_TYPE_ARRAY;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link Kind} Test for {@link Class}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see JavaType
 * @see Kind#CLASS
 * @see Class
 * @since 1.0.0
 */
class JavaTypeKindTestForClass extends AbstractJavaTypeKindTest {

    @Test
    @Override
    void testGetSuperType() {
        Type superType = CLASS.getSuperType(Object.class);
        assertNull(superType);

        superType = CLASS.getSuperType(String.class);
        assertSame(Object.class, superType);
    }

    @Test
    @Override
    void testGetRawType() {
        Type rawType = CLASS.getRawType(Object.class);
        assertSame(Object.class, rawType);

        rawType = CLASS.getRawType(String.class);
        assertSame(String.class, rawType);
    }

    @Test
    @Override
    void testGetInterfaces() {
        Type[] interfaces = CLASS.getInterfaces(Object.class);
        assertArrayEquals(EMPTY_TYPE_ARRAY, interfaces);

        interfaces = CLASS.getInterfaces(A.class);
        assertArrayEquals(A.class.getGenericInterfaces(), interfaces);
        assertArrayEquals(ofArray(Serializable.class), interfaces);
    }

    @Test
    @Override
    void testGetGenericTypes() {
        Type[] genericTypes = CLASS.getGenericTypes(from(Object.class));
        assertArrayEquals(EMPTY_TYPE_ARRAY, genericTypes);

        genericTypes = CLASS.getGenericTypes(from(Map.class));
        assertEquals(2, genericTypes.length);
        assertNull(genericTypes[0]);
        assertNull(genericTypes[1]);
    }
}
