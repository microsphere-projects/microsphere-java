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

import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static io.microsphere.invoke.MethodHandleUtils.findStatic;
import static io.microsphere.invoke.MethodHandleUtils.findVirtual;
import static io.microsphere.invoke.MethodHandleUtils.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link MethodHandleUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class MethodHandleUtilsTest {

    @Test
    public void testLookup() {
        MethodHandles.Lookup lookup = lookup(String.class);
        MethodHandles.Lookup lookup2 = lookup(String.class);
        assertSame(lookup, lookup2);
    }

    //
    @Test
    public void testFindVirtual() throws Throwable {
        MethodHandle methodHandle = findVirtual(String.class, "length");
        assertEquals(1, (int) methodHandle.invokeExact("A"));
    }

    @Test
    public void testFindStatic() throws Throwable {
        MethodHandle methodHandle = findStatic(String.class, "valueOf", int.class);
        assertEquals("1", (String) methodHandle.invokeExact(1));
    }
}
