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
package io.microsphere.invoke;

import io.microsphere.junit.jupiter.api.extension.annotation.UtilsTestExtension;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;

import static io.microsphere.invoke.MethodHandlesLookupUtils.findPublic;
import static io.microsphere.invoke.MethodHandlesLookupUtils.findPublicStatic;
import static io.microsphere.invoke.MethodHandlesLookupUtils.findPublicVirtual;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link MethodHandlesLookupUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@UtilsTestExtension
public class MethodHandlesLookupUtilsTest {

    @Test
    public void testFindPublicVirtual() throws Throwable {
        MethodHandle methodHandle = findPublicVirtual(String.class, "length");
        assertEquals(1, (int) methodHandle.invokeExact("A"));
    }

    @Test
    public void testFindPublicVirtualOnNonPublicMethod() throws Throwable {
        MethodHandle methodHandle = findPublicVirtual(Object.class, "clone");
        assertNull(methodHandle);
    }

    @Test
    public void testFindPublicOnAbsentMethod() {
        assertNull(findPublic(Object.class, "x", EMPTY_CLASS_ARRAY, (lookup, methodType) -> null));
    }

    @Test
    public void testFindPublicStatic() throws Throwable {
        MethodHandle methodHandle = findPublicStatic(String.class, "valueOf", int.class);
        assertEquals("1", (String) methodHandle.invokeExact(1));
    }
}
