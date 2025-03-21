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
package io.microsphere.annotation.processor.util;

import io.microsphere.annotation.processor.AbstractAnnotationProcessingTest;
import io.microsphere.annotation.processor.TestServiceImpl;
import io.microsphere.annotation.processor.model.Color;
import io.microsphere.annotation.processor.model.Model;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.microsphere.annotation.processor.util.FieldUtils.findAllDeclaredFields;
import static io.microsphere.annotation.processor.util.FieldUtils.findDeclaredFields;
import static io.microsphere.annotation.processor.util.FieldUtils.findField;
import static io.microsphere.annotation.processor.util.FieldUtils.getAllDeclaredFields;
import static io.microsphere.annotation.processor.util.FieldUtils.getAllNonStaticFields;
import static io.microsphere.annotation.processor.util.FieldUtils.getDeclaredField;
import static io.microsphere.annotation.processor.util.FieldUtils.getDeclaredFields;
import static io.microsphere.annotation.processor.util.FieldUtils.getNonStaticFields;
import static io.microsphere.annotation.processor.util.FieldUtils.isEnumMemberField;
import static io.microsphere.annotation.processor.util.FieldUtils.isField;
import static io.microsphere.annotation.processor.util.FieldUtils.isNonStaticField;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link FieldUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class FieldUtilsTest extends AbstractAnnotationProcessingTest {

    private TypeElement testType;

    @Override
    protected void beforeTest() {
        testType = getTypeElement(TestServiceImpl.class);
    }

    @Test
    public void testGetDeclaredFields() {
        TypeElement type = getTypeElement(Model.class);
        List<VariableElement> fields = getDeclaredFields(type);
        assertModelFields(fields);

        fields = getDeclaredFields(type.asType());
        assertModelFields(fields);

        assertTrue(getDeclaredFields((Element) null).isEmpty());
        assertTrue(getDeclaredFields((TypeMirror) null).isEmpty());

        fields = findDeclaredFields(type, f -> "f".equals(f.getSimpleName().toString()));
        assertEquals(1, fields.size());
        assertEquals("f", fields.get(0).getSimpleName().toString());
    }

    @Test
    public void testGetAllDeclaredFields() {
        TypeElement type = getTypeElement(Model.class);

        List<VariableElement> fields = getAllDeclaredFields(type);

        assertModelAllFields(fields);

        assertTrue(getAllDeclaredFields((Element) null).isEmpty());
        assertTrue(getAllDeclaredFields((TypeMirror) null).isEmpty());

        fields = findAllDeclaredFields(type, f -> "f".equals(f.getSimpleName().toString()));
        assertEquals(1, fields.size());
        assertEquals("f", fields.get(0).getSimpleName().toString());
    }

    @Test
    public void testGetDeclaredField() {
        TypeElement type = getTypeElement(Model.class);
        testGetDeclaredField(type, "f", float.class);
        testGetDeclaredField(type, "d", double.class);
        testGetDeclaredField(type, "tu", TimeUnit.class);
        testGetDeclaredField(type, "str", String.class);
        testGetDeclaredField(type, "bi", BigInteger.class);
        testGetDeclaredField(type, "bd", BigDecimal.class);

        assertNull(getDeclaredField(type, "b"));
        assertNull(getDeclaredField(type, "s"));
        assertNull(getDeclaredField(type, "i"));
        assertNull(getDeclaredField(type, "l"));
        assertNull(getDeclaredField(type, "z"));

        assertNull(getDeclaredField((Element) null, "z"));
        assertNull(getDeclaredField((TypeMirror) null, "z"));
    }

    @Test
    public void testFindField() {
        TypeElement type = getTypeElement(Model.class);
        testFindField(type, "f", float.class);
        testFindField(type, "d", double.class);
        testFindField(type, "tu", TimeUnit.class);
        testFindField(type, "str", String.class);
        testFindField(type, "bi", BigInteger.class);
        testFindField(type, "bd", BigDecimal.class);
        testFindField(type, "b", byte.class);
        testFindField(type, "s", short.class);
        testFindField(type, "i", int.class);
        testFindField(type, "l", long.class);
        testFindField(type, "z", boolean.class);

        assertNull(findField((Element) null, "f"));
        assertNull(findField((Element) null, null));

        assertNull(findField((TypeMirror) null, "f"));
        assertNull(findField((TypeMirror) null, null));

        assertNull(findField(type, null));
        assertNull(findField(type.asType(), null));
    }

    @Test
    public void testIsEnumField() {
        TypeElement type = getTypeElement(Color.class);
        VariableElement field = findField(type, "RED");
        assertTrue(isEnumMemberField(field));

        field = findField(type, "YELLOW");
        assertTrue(isEnumMemberField(field));

        field = findField(type, "BLUE");
        assertTrue(isEnumMemberField(field));

        type = getTypeElement(Model.class);
        field = findField(type, "f");
        assertFalse(isEnumMemberField(field));

        assertFalse(isEnumMemberField(null));
    }

    @Test
    public void testIsNonStaticField() {
        TypeElement type = getTypeElement(Model.class);
        assertTrue(isNonStaticField(findField(type, "f")));

        type = getTypeElement(Color.class);
        assertFalse(isNonStaticField(findField(type, "BLUE")));
    }

    @Test
    public void testIsField() {
        TypeElement type = getTypeElement(Model.class);
        assertTrue(isField(findField(type, "f")));
        assertTrue(isField(findField(type, "f"), PRIVATE));

        type = getTypeElement(Color.class);
        assertTrue(isField(findField(type, "BLUE"), PUBLIC, STATIC, FINAL));


        assertFalse(isField(null));
        assertFalse(isField(null, PUBLIC, STATIC, FINAL));
    }

    @Test
    public void testGetNonStaticFields() {
        TypeElement type = getTypeElement(Model.class);

        List<VariableElement> fields = getNonStaticFields(type);
        assertModelFields(fields);

        fields = getNonStaticFields(type.asType());
        assertModelFields(fields);

        assertTrue(getAllNonStaticFields((Element) null).isEmpty());
        assertTrue(getAllNonStaticFields((TypeMirror) null).isEmpty());
    }

    @Test
    public void testGetAllNonStaticFields() {
        TypeElement type = getTypeElement(Model.class);

        List<VariableElement> fields = getAllNonStaticFields(type);
        assertModelAllFields(fields);

        fields = getAllNonStaticFields(type.asType());
        assertModelAllFields(fields);

        assertTrue(getAllNonStaticFields((Element) null).isEmpty());
        assertTrue(getAllNonStaticFields((TypeMirror) null).isEmpty());
    }

    private void assertModelFields(List<VariableElement> fields) {
        assertEquals(6, fields.size());
        assertEquals("d", fields.get(1).getSimpleName().toString());
        assertEquals("tu", fields.get(2).getSimpleName().toString());
        assertEquals("str", fields.get(3).getSimpleName().toString());
        assertEquals("bi", fields.get(4).getSimpleName().toString());
        assertEquals("bd", fields.get(5).getSimpleName().toString());
    }

    private void assertModelAllFields(List<VariableElement> fields) {
        assertEquals(11, fields.size());
        assertEquals("f", fields.get(0).getSimpleName().toString());
        assertEquals("d", fields.get(1).getSimpleName().toString());
        assertEquals("tu", fields.get(2).getSimpleName().toString());
        assertEquals("str", fields.get(3).getSimpleName().toString());
        assertEquals("bi", fields.get(4).getSimpleName().toString());
        assertEquals("bd", fields.get(5).getSimpleName().toString());
        assertEquals("b", fields.get(6).getSimpleName().toString());
        assertEquals("s", fields.get(7).getSimpleName().toString());
        assertEquals("i", fields.get(8).getSimpleName().toString());
        assertEquals("l", fields.get(9).getSimpleName().toString());
        assertEquals("z", fields.get(10).getSimpleName().toString());
    }

    private void testGetDeclaredField(TypeElement type, String fieldName, Type fieldType) {
        VariableElement field = getDeclaredField(type, fieldName);
        assertField(field, fieldName, fieldType);
    }

    private void testFindField(TypeElement type, String fieldName, Type fieldType) {
        VariableElement field = findField(type, fieldName);
        assertField(field, fieldName, fieldType);
    }

    private void assertField(VariableElement field, String fieldName, Type fieldType) {
        assertEquals(fieldName, field.getSimpleName().toString());
        assertEquals(fieldType.getTypeName(), field.asType().toString());
    }
}
