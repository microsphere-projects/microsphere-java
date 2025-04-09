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
import io.microsphere.test.B;
import io.microsphere.test.C;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.RandomAccess;

import static io.microsphere.reflect.JavaType.Kind.PARAMETERIZED_TYPE;
import static io.microsphere.reflect.JavaType.from;
import static io.microsphere.reflect.JavaTypeKindTest.C_STRING_PARAMETERIZED_TYPE;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link Kind} Test for {@link ParameterizedType}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see JavaType
 * @see Kind#PARAMETERIZED_TYPE
 * @see ParameterizedType
 * @since 1.0.0
 */
public class JavaTypeKindTestForParameterizedType {

    @Test
    public void testGetSuperType() {
        Type superType = PARAMETERIZED_TYPE.getSuperType(C_STRING_PARAMETERIZED_TYPE);
        assertSame(B.class, superType);

        assertThrows(ClassCastException.class, () -> PARAMETERIZED_TYPE.getSuperType(String.class));
    }

    @Test
    public void testGetRawType() {
        Type rawType = PARAMETERIZED_TYPE.getRawType(C_STRING_PARAMETERIZED_TYPE);
        assertSame(C.class, rawType);

        assertThrows(ClassCastException.class, () -> PARAMETERIZED_TYPE.getRawType(String.class));

    }

    @Test
    public void testGetInterfaces() {
        Type[] interfaces = PARAMETERIZED_TYPE.getInterfaces(C_STRING_PARAMETERIZED_TYPE);
        assertEquals(1, interfaces.length);
        assertEquals(RandomAccess.class, interfaces[0]);

        assertThrows(ClassCastException.class, () -> PARAMETERIZED_TYPE.getInterfaces(String.class));
    }

    @Test
    public void testGetGenericTypes() {
        Type[] genericTypes = PARAMETERIZED_TYPE.getGenericTypes(from(C_STRING_PARAMETERIZED_TYPE));
        assertEquals(1, genericTypes.length);
        assertEquals(String.class, genericTypes[0]);

        assertThrows(ClassCastException.class, () -> PARAMETERIZED_TYPE.getGenericTypes(from(String.class)));

        // Map#putAll(Map<? extends K, ? extends V> m)
        Method method = findMethod(Map.class, "putAll", Map.class);
        Type[] parameterTypes = method.getGenericParameterTypes();
        Type parameterizedType = parameterTypes[0];

        genericTypes = PARAMETERIZED_TYPE.getGenericTypes(from(parameterizedType));
        assertEquals(2, genericTypes.length);
        assertNull(genericTypes[0]);
        assertNull(genericTypes[1]);
    }
}
