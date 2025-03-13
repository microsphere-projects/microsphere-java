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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Set;

import static io.microsphere.reflect.FieldUtils.assertFieldMatchType;
import static io.microsphere.reflect.FieldUtils.findAllDeclaredFields;
import static io.microsphere.reflect.FieldUtils.findAllFields;
import static io.microsphere.reflect.FieldUtils.findField;
import static io.microsphere.reflect.FieldUtils.getDeclaredField;
import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.reflect.FieldUtils.getStaticFieldValue;
import static io.microsphere.reflect.FieldUtils.handleIllegalAccessException;
import static io.microsphere.reflect.FieldUtils.setFieldValue;
import static io.microsphere.reflect.FieldUtils.setStaticFieldValue;
import static io.microsphere.util.VersionUtils.CURRENT_JAVA_VERSION;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link FieldUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class FieldUtilsTest {

    private static String value = "1";

    private final ReflectionTest test = new ReflectionTest();

    @AfterEach
    public void destroy() {
        ReflectionTest.staticField = "staticField";
    }

    @Test
    public void testFindFieldOnObject() {
        assertFindField(test, "privateField");
        assertFindField(test, "packagePrivateField");
        assertFindField(test, "protectedField");
        assertFindField(test, "publicField");
        assertFindField(test, "staticField");

        assertNull(findField(test, "unknownField"));
    }

    @Test
    public void testFindFieldOnClass() {
        assertFindField(ReflectionTest.class, "privateField");
        assertFindField(ReflectionTest.class, "packagePrivateField");
        assertFindField(ReflectionTest.class, "protectedField");
        assertFindField(ReflectionTest.class, "publicField");
        assertFindField(ReflectionTest.class, "staticField");

        assertNull(findField(ReflectionTest.class, "unknownField"));
    }

    @Test
    public void testFindFieldOnClassAndFieldType() {
        assertFindField(ReflectionTest.class, "privateField", String.class);
        assertFindField(ReflectionTest.class, "packagePrivateField", String.class);
        assertFindField(ReflectionTest.class, "protectedField", String.class);
        assertFindField(ReflectionTest.class, "publicField", String.class);
        assertFindField(ReflectionTest.class, "staticField", String.class);

        assertNull(findField(ReflectionTest.class, "staticField", Object.class));
    }

    @Test
    public void testFindFieldOnPredicate() {
        assertFindField(ReflectionTest.class, "privateField", true);
        assertFindField(ReflectionTest.class, "packagePrivateField", true);
        assertFindField(ReflectionTest.class, "protectedField", true);
        assertFindField(ReflectionTest.class, "publicField", true);
        assertFindField(ReflectionTest.class, "staticField", true);

        assertFindField(ReflectionTest.class, "privateField", false);
        assertFindField(ReflectionTest.class, "packagePrivateField", false);
        assertFindField(ReflectionTest.class, "protectedField", false);
        assertFindField(ReflectionTest.class, "publicField", false);
        assertFindField(ReflectionTest.class, "staticField", false);
    }


    @Test
    public void testFindAllFields() {
        Set<Field> fields = findAllFields(ReflectionTest.class, p -> false);
        assertEquals(0, fields.size());

        fields = findAllFields(ReflectionTestExt.class, p -> false);
        assertEquals(0, fields.size());
    }

    @Test
    public void testFindAllFieldsWithoutPredicate() {
        Set<Field> fields = findAllFields(ReflectionTest.class);
        assertEquals(1, fields.size());

        fields = findAllFields(ReflectionTestExt.class);
        assertEquals(1, fields.size());
    }

    @Test
    public void testFindAllDeclaredFieldsWithoutPredicate() {
        Set<Field> fields = findAllDeclaredFields(ReflectionTest.class);
        assertTrue(fields.size() >= 5);

        fields = findAllDeclaredFields(ReflectionTestExt.class);
        assertTrue(fields.size() >= 7);
    }

    @Test
    public void testGetDeclaredField() {
        assertGetDeclaredField(ReflectionTest.class, "privateField");
        assertGetDeclaredField(ReflectionTest.class, "packagePrivateField");
        assertGetDeclaredField(ReflectionTest.class, "protectedField");
        assertGetDeclaredField(ReflectionTest.class, "publicField");
        assertGetDeclaredField(ReflectionTest.class, "staticField");

        assertThrows(RuntimeException.class, () -> getDeclaredField(ReflectionTestExt.class, "unknownField"));

        assertGetDeclaredField(ReflectionTestExt.class, "integerField");
        assertGetDeclaredField(ReflectionTestExt.class, "stringField");

        assertThrows(RuntimeException.class, () -> getDeclaredField(ReflectionTestExt.class, "privateField"));
        assertThrows(RuntimeException.class, () -> getDeclaredField(ReflectionTestExt.class, "packagePrivateField"));
        assertThrows(RuntimeException.class, () -> getDeclaredField(ReflectionTestExt.class, "protectedField"));
        assertThrows(RuntimeException.class, () -> getDeclaredField(ReflectionTestExt.class, "publicField"));
        assertThrows(RuntimeException.class, () -> getDeclaredField(ReflectionTestExt.class, "staticField"));
        assertThrows(RuntimeException.class, () -> getDeclaredField(ReflectionTestExt.class, "unknownField"));
    }

    @Test
    public void testGetStaticFieldValue() {
        assertSame(System.in, getStaticFieldValue(System.class, "in"));
        assertSame(System.out, getStaticFieldValue(System.class, "out"));
        assertSame(ReflectionTest.staticField, getStaticFieldValue(ReflectionTest.class, "staticField"));

        assertGetStaticFieldValue(ReflectionTest.class, "staticField");
    }

    @Test
    public void testGetStaticFieldValueOnField() {
        Field field = findField(ReflectionTest.class, "staticField");
        assertSame("staticField", getStaticFieldValue(field));
        assertSame(ReflectionTest.staticField, getStaticFieldValue(field));
    }

    @Test
    public void testGetFieldValue() {
        String value = "Hello,World";
        if (CURRENT_JAVA_VERSION.le(JAVA_VERSION_8)) {
            assertArrayEquals(value.toCharArray(), getFieldValue(value, "value", char[].class));
        } else {
            assertArrayEquals(value.getBytes(), getFieldValue(value, "value", byte[].class));
        }

        assertGetFieldValue(test, "privateField");
        assertGetFieldValue(test, "packagePrivateField");
        assertGetFieldValue(test, "protectedField");
        assertGetFieldValue(test, "publicField");
    }

    @Test
    public void testGetFieldValueWithDefaultValue() {
        ReflectionTestExt testExt = new ReflectionTestExt();
        assertGetFieldValue(testExt, "integerField", 0);
        assertGetFieldValue(testExt, "stringField", "test");
    }

    @Test
    public void testGetFieldValueOnIllegalArgumentException() {
        Field field = findField(test, "privateField");
        assertThrows(IllegalArgumentException.class, () -> getFieldValue("test", field));
    }

    @Test
    public void testSetFieldValue() {
        Integer value = 999;
        setFieldValue(value, "value", 2);
        assertEquals(value.intValue(), 2);

        assertSetFieldValue(test, "privateField", "test");
        assertSetFieldValue(test, "packagePrivateField", "test");
        assertSetFieldValue(test, "protectedField", "test");
        assertSetFieldValue(test, "publicField", "test");
    }

    @Test
    public void testSetFieldValueOnFieldNotFound() {
        assertNull(setFieldValue(test, "notFoundField", null));
    }

    @Test
    public void testSetFieldValueOnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> setFieldValue(test, "privateField", 1));
    }

    @Test
    public void testSetStaticFieldValue() {
        setStaticFieldValue(getClass(), "value", "abc");
        assertEquals("abc", value);

        assertSetStaticFieldValue(ReflectionTest.class, "staticField", "test");
    }

    @Test
    public void testAssertFieldMatchType() {
        assertFieldMatchType(test, "privateField", String.class);
        assertFieldMatchType(test, "packagePrivateField", String.class);
        assertFieldMatchType(test, "protectedField", String.class);
        assertFieldMatchType(test, "publicField", String.class);
        assertFieldMatchType(test, "staticField", String.class);
        assertFieldMatchType(ReflectionTestExt.class, "integerField", Integer.class);
        assertFieldMatchType(ReflectionTestExt.class, "stringField", String.class);
    }

    @Test
    public void testAssertFieldMatchTypeOnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> assertFieldMatchType(test, "privateField", Integer.class));
    }

    @Test
    public void testHandleIllegalAccessException() {
        Field field = findField(ReflectionTest.class, "staticField");
        assertThrows(IllegalStateException.class, () -> handleIllegalAccessException(new IllegalAccessException(), test, field, field.isAccessible()));
    }


    static class ReflectionTestExt extends ReflectionTest {

        private Integer integerField;

        private String stringField;

    }

    private void assertFindField(Object object, String fieldName) {
        assertNotNull(findField(object, fieldName));
    }

    private void assertFindField(Class<?> klass, String fieldName) {
        assertNotNull(findField(klass, fieldName));
    }

    private void assertFindField(Class<?> klass, String fieldName, Class<?> fieldType) {
        assertNotNull(findField(klass, fieldName, fieldType));
    }

    private void assertFindField(Class<?> klass, String fieldName, boolean found) {
        assertEquals(found, findField(klass, fieldName, f -> found) != null);
    }

    private void assertGetStaticFieldValue(Class<?> klass, String fieldName) {
        assertEquals(fieldName, getStaticFieldValue(klass, fieldName));
    }

    private void assertGetFieldValue(ReflectionTest test, String fieldName) {
        assertEquals(fieldName, getFieldValue(test, fieldName));
    }

    private void assertGetFieldValue(ReflectionTest test, String fieldName, Object defaultValue) {
        assertEquals(defaultValue, getFieldValue(test, fieldName, defaultValue));
    }

    private void assertSetFieldValue(ReflectionTest test, String fieldName, String fieldValue) {
        assertEquals(fieldName, setFieldValue(test, fieldName, fieldValue));
    }

    private void assertSetStaticFieldValue(Class<?> klass, String fieldName, String fieldValue) {
        assertEquals(fieldName, setStaticFieldValue(klass, fieldName, fieldValue));
    }

    private void assertGetDeclaredField(Class<?> klass, String fieldName) {
        assertNotNull(getDeclaredField(klass, fieldName));
    }
}
