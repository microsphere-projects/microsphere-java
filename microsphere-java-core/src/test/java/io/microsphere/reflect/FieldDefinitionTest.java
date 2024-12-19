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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link FieldDefinition} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see FieldDefinition
 * @since 1.0.0
 */
public class FieldDefinitionTest {

    private String name;

    @Test
    public void test() throws Throwable {
        assertFieldDefinition(SINCE, getClass().getName(), "name", "test");
        assertFieldDefinition(SINCE, DEPRECATION, getClass().getName(), "name", "test");
    }

    private void assertFieldDefinition(String since, String className, String fieldName, Object fieldValue) throws Throwable {
        assertFieldDefinition(since, null, className, fieldName, fieldValue);
    }

    private void assertFieldDefinition(String since, Deprecation deprecation, String className, String fieldName, Object fieldValue) throws Throwable {
        FieldDefinition fd = new FieldDefinition(since, deprecation, className, fieldName);
        assertEquals(Version.of(since), fd.getSince());
        assertEquals(deprecation, fd.getDeprecation());
        assertEquals(className, fd.getClassName());
        assertEquals(fieldName, fd.getFieldName());
        assertEquals(this.getClass(), fd.getResolvedClass());
        assertNotNull(fd.getResolvedField());
        Object instance = fd.getDeclaredClass().newInstance();
        assertNull(fd.get(instance));
        assertNull(fd.set(instance, fieldValue));
        assertEquals(fieldValue, fd.get(instance));
    }
}
