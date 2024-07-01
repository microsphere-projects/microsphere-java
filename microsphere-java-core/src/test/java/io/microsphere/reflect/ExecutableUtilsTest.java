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

import io.microsphere.lang.function.ThrowableFunction;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static io.microsphere.reflect.ExecutableUtils.execute;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ExecutableUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ExecutableUtils
 * @see ReflectionTest
 * @since 1.0.0
 */
public class ExecutableUtilsTest {

    private static final Class<ReflectionTest> testClass = ReflectionTest.class;

    private static final ReflectionTest test = new ReflectionTest();

    @Test
    public void testExecute() {
        Method method = getMethod("privateMethod");
        assertEquals("test", execute(method, (ThrowableFunction<Method, Object>) m -> method.invoke(test)));
    }

    private Method getMethod(String methodName) {
        return findMethod(testClass, methodName);
    }
}
