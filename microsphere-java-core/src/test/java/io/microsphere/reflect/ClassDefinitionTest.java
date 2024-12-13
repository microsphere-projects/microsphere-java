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

import io.microsphere.lang.Deprecation;
import io.microsphere.util.Version;
import org.junit.jupiter.api.Test;

import static io.microsphere.lang.DeprecationTest.DEPRECATION;
import static io.microsphere.lang.DeprecationTest.SINCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ClassDefinition} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ClassDefinition
 * @since 1.0.0
 */
public class ClassDefinitionTest {

    private static final String CLASS_NAME = "io.microsphere.reflect.ClassDefinitionTest";

    @Test
    public void test() {
        assertClassDefinition(SINCE, DEPRECATION, CLASS_NAME);
        assertClassDefinition(SINCE, CLASS_NAME);
    }

    private void assertClassDefinition(String since, String className) {
        assertClassDefinition(since, null, className);
    }

    private void assertClassDefinition(String since, Deprecation deprecation, String className) {
        ClassDefinition cd = new ClassDefinition(since, deprecation, className);
        ClassDefinition cd2 = new ClassDefinition(since, deprecation, className);

        assertEquals(cd, cd2);
        assertEquals(cd.hashCode(), cd2.hashCode());
        assertEquals(cd.toString(), cd2.toString());

        assertEquals(Version.of(since), cd.getSince());
        assertEquals(deprecation, cd.getDeprecation());
        assertEquals(className, cd.getClassName());
        assertEquals(getClass(), cd.getResolvedClass());
        assertTrue(cd.isPresent());
        assertEquals(deprecation != null, cd.isDeprecated());

        assertEquals(Version.of(since), cd.getSince());
        assertEquals(deprecation, cd.getDeprecation());
        assertEquals(className, cd.getClassName());
    }
}
