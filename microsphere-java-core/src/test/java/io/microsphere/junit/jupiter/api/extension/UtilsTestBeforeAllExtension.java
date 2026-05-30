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
package io.microsphere.junit.jupiter.api.extension;

import io.microsphere.util.Utils;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Constructor;

import static io.microsphere.reflect.MemberUtils.isPrivate;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.ClassUtils.isAbstractClass;
import static io.microsphere.util.ClassUtils.isAssignableFrom;
import static io.microsphere.util.StringUtils.substringBefore;
import static java.lang.reflect.Modifier.isPublic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link BeforeAllCallback} for the utilities class test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BeforeAllCallback
 * @since 1.0.0
 */
public class UtilsTestBeforeAllExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Class<?> testClass = context.getTestClass().orElseGet(null);
        if (testClass != null) {
            String testClassName = testClass.getName();
            ClassLoader classLoader = testClass.getClassLoader();
            String utilsClassName = substringBefore(testClassName, "Test");
            Class<?> utilsClass = resolveClass(utilsClassName, classLoader);
            if (utilsClass != null) { // the utilities class exists
                if (isAssignableFrom(Utils.class, utilsClass)) {
                    assertUtilitiesClass(utilsClass);
                }
            }
        }
    }

    void assertUtilitiesClass(Class<?> utilsClass) throws Exception {
        assertTrue(isPublic(utilsClass.getModifiers()), "The utilities class must be public!");
        assertTrue(isAbstractClass(utilsClass), "The utilities class must be abstract!");
        assertUtilitiesClassConstructor(utilsClass);
    }

    void assertUtilitiesClassConstructor(Class<?> utilsClass) {
        Constructor<?>[] constructors = utilsClass.getDeclaredConstructors();
        assertEquals(1, constructors.length,
                "The utilities class must contain only one the constructor!");

        Constructor constructor = constructors[0];
        assertTrue(isPrivate(constructor), "The constructor of the utilities class must be private!");
        assertEquals(0, constructor.getParameterCount(),
                "The constructor of the utilities class must not contain any parameter!");
    }
}
