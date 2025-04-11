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
import org.junit.jupiter.api.Test;

import static io.microsphere.reflect.JavaType.Kind.UNKNOWN;
import static io.microsphere.reflect.JavaType.from;
import static io.microsphere.util.ArrayUtils.EMPTY_TYPE_ARRAY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link Kind} Test for Unknown
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see JavaType
 * @see Kind#UNKNOWN
 * @since 1.0.0
 */
public class JavaTypeKindTestForUnknown {

    @Test
    public void testGetSuperType() {
        assertNull(UNKNOWN.getSuperType(Object.class));
    }

    @Test
    public void testGetRawType() {
        assertNull(UNKNOWN.getRawType(Object.class));
    }

    @Test
    public void testGetInterfaces() {
        assertArrayEquals(EMPTY_TYPE_ARRAY, UNKNOWN.getInterfaces(Object.class));
    }

    @Test
    public void testGetGenericTypes() {
        assertArrayEquals(EMPTY_TYPE_ARRAY, UNKNOWN.getGenericTypes(from(Object.class)));
    }
}
