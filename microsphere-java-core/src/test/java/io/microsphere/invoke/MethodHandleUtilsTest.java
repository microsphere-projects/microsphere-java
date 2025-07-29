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
import static io.microsphere.invoke.MethodHandleUtils.handleInvokeExactFailure;
import static io.microsphere.invoke.MethodHandleUtils.lookup;
import static io.microsphere.invoke.MethodHandlesLookupUtils.NOT_FOUND_METHOD_HANDLE;
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
    void testLookup() {
        MethodHandles.Lookup lookup = lookup(String.class);
        MethodHandles.Lookup lookup2 = lookup(String.class);
        assertSame(lookup, lookup2);
    }

    @Test
    void testFindVirtualOnPublicMethod() throws Throwable {
        testFindVirtual("publicMethod");
    }

    @Test
    void testFindVirtualOnProtectedMethod() throws Throwable {
        testFindVirtual("protectedMethod");
    }

    @Test
    void testFindVirtualOnDefaultMethod() throws Throwable {
        testFindVirtual("defaultMethod");
    }

    @Test
    void testFindVirtualOnPrivateMethod() throws Throwable {
        testFindVirtual("privateMethod");
    }

    @Test
    void testFindVirtualOnNotFoundMethod() throws Throwable {
        assertSame(NOT_FOUND_METHOD_HANDLE, findVirtual(MethodHandleUtilsTest.class, "notFound"));
    }

    @Test
    void testFindStaticOnPublicStaticMethod() throws Throwable {
        testFindStatic("publicStaticMethod");
    }

    @Test
    void testFindStaticOnProtectedStaticMethod() throws Throwable {
        testFindStatic("protectedStaticMethod");
    }

    @Test
    void testFindStaticOnDefaultStaticMethod() throws Throwable {
        testFindStatic("defaultStaticMethod");
    }


    @Test
    void testFindStaticOnPrivateStaticMethod() throws Throwable {
        testFindStatic("privateStaticMethod");
    }

    @Test
    void testHandleInvokeExactFailure() {
        MethodHandle methodHandle = findVirtual(MethodHandleUtilsTest.class, "privateMethod");
        handleInvokeExactFailure(new Throwable("testing"), methodHandle);
    }

    private void testFindVirtual(String methodName) throws Throwable {
        MethodHandle methodHandle = findVirtual(MethodHandleUtilsTest.class, methodName);
        assertEquals(methodName, (String) methodHandle.invokeExact(this));
    }

    private void testFindStatic(String methodName) throws Throwable {
        MethodHandle methodHandle = findStatic(MethodHandleUtilsTest.class, methodName);
        assertEquals(methodName, (String) methodHandle.invokeExact());
    }

    public String publicMethod() {
        return "publicMethod";
    }

    protected String protectedMethod() {
        return "protectedMethod";
    }

    String defaultMethod() {
        return "defaultMethod";
    }

    private String privateMethod() {
        return "privateMethod";
    }

    public static String publicStaticMethod() {
        return "publicStaticMethod";
    }

    private static String protectedStaticMethod() {
        return "protectedStaticMethod";
    }

    static String defaultStaticMethod() {
        return "defaultStaticMethod";
    }

    private static String privateStaticMethod() {
        return "privateStaticMethod";
    }
}
