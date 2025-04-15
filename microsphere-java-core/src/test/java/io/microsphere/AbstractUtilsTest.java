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
package io.microsphere;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static io.microsphere.reflect.JavaType.from;
import static io.microsphere.reflect.MemberUtils.isPrivate;
import static io.microsphere.util.ClassUtils.isAbstractClass;
import static java.lang.reflect.Modifier.isPublic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The abstract class for utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractTestCase
 * @since 1.0.0
 */
@Disabled
public abstract class AbstractUtilsTest<U> extends AbstractTestCase {

    protected Class<U> utilitiesClass;

    @BeforeEach
    public void initUtilitiesClass() throws Throwable {
        utilitiesClass = from(getClass())
                .as(AbstractUtilsTest.class)
                .getGenericType(0)
                .toClass();
        assertNotNull(utilitiesClass);
    }

    @Test
    public void testUtilitiesClass() throws Throwable {
        assertTrue(isPublic(utilitiesClass.getModifiers()));
        assertTrue(isAbstractClass(utilitiesClass));
    }

    @Test
    public void testUtilitiesClassConstructor() throws Throwable {
        Constructor<?>[] constructors = utilitiesClass.getDeclaredConstructors();
        assertEquals(1, constructors.length);

        Constructor constructor = constructors[0];
        assertTrue(isPrivate(constructor));
        assertEquals(0, constructor.getParameterCount());
    }
}
