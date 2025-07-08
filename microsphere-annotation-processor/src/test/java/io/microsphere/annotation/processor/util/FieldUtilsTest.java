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
import io.microsphere.annotation.processor.model.Color;
import io.microsphere.annotation.processor.model.Model;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.microsphere.annotation.processor.util.FieldUtils.equalsFieldName;
import static io.microsphere.annotation.processor.util.FieldUtils.filterDeclaredFields;
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
import static io.microsphere.annotation.processor.util.MethodUtils.findMethod;
import static io.microsphere.lang.function.Predicates.alwaysFalse;
import static io.microsphere.lang.function.Predicates.alwaysTrue;
import static io.microsphere.util.StringUtils.EMPTY_STRING;
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
class FieldUtilsTest extends AbstractAnnotationProcessingTest {

    @Test
    void testGetDeclaredField() {
        TypeElement type = getTypeElement(Model.class);
        testGetDeclaredField(type, "f", float.class);
        testGetDeclaredField(type, "d", double.class);
        testGetDeclaredField(type, "tu", TimeUnit.class);
        testGetDeclaredField(type, "str", String.class);
        testGetDeclaredField(type, "bi", BigInteger.class);
        testGetDeclaredField(type, "bd", BigDecimal.class);
    }

    @Test
    void testGetDeclaredFieldOnNotFound() {
        TypeElement type = getTypeElement(Model.class);
        assertNull(getDeclaredField(type, "b"));
        assertNull(getDeclaredField(type, "s"));
        assertNull(getDeclaredField(type, "i"));
        assertNull(getDeclaredField(type, "l"));
        assertNull(getDeclaredField(type, "z"));
    }

    @Test
    void testGetDeclaredFieldOnNull() {
        assertNull(getDeclaredField(NULL_ELEMENT, "z"));
        assertNull(getDeclaredField(NULL_TYPE_MIRROR, "z"));
    }

    @Test
    void testGetDeclaredFields() {
        TypeElement type = getTypeElement(Model.class);
        List<VariableElement> fields = getDeclaredFields(type);
        assertModelFields(fields);

        fields = getDeclaredFields(type.asType());
        assertModelFields(fields);
    }

    @Test
    public void testGetDeclaredFieldsOnNull() {
        assertTrue(getDeclaredFields(NULL_ELEMENT).isEmpty());
        assertTrue(getDeclaredFields(NULL_TYPE_MIRROR).isEmpty());
    }

    @Test
    public void testGetAllDeclaredFields() {
        TypeElement type = getTypeElement(Model.class);
        List<VariableElement> fields = getAllDeclaredFields(type);
        assertModelAllFields(fields);
    }

    @Test
    public void testGetAllDeclaredFieldsOnNull() {
        assertTrue(getAllDeclaredFields(NULL_ELEMENT).isEmpty());
        assertTrue(getAllDeclaredFields(NULL_TYPE_MIRROR).isEmpty());
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
    }

    @Test
    public void testFindFieldOnNull() {
        TypeElement type = getTypeElement(Model.class);
        assertNull(findField(NULL_ELEMENT, "f"));
        assertNull(findField(NULL_ELEMENT, NULL_STRING));

        assertNull(findField(NULL_TYPE_MIRROR, "f"));
        assertNull(findField(NULL_TYPE_MIRROR, NULL_STRING));

        assertNull(findField(type, NULL_STRING));
        assertNull(findField(type.asType(), NULL_STRING));
    }

    @Test
    public void testFindDeclaredFields() {
        TypeElement type = getTypeElement(Model.class);

        List<VariableElement> fields = findAllDeclaredFields(type, alwaysTrue());
        assertModelAllFields(fields);

        fields = findAllDeclaredFields(type, alwaysFalse());
        assertEmptyList(fields);

        fields = findDeclaredFields(type, f -> "f".equals(f.getSimpleName().toString()));
        assertEquals(1, fields.size());
        assertEquals("f", fields.get(0).getSimpleName().toString());
    }

    @Test
    public void testFindDeclaredFieldsOnNull() {
        assertEmptyList(findDeclaredFields(NULL_ELEMENT, alwaysTrue()));
        assertEmptyList(findDeclaredFields(NULL_TYPE_MIRROR, alwaysTrue()));
    }

    @Test
    public void testFindAllDeclaredFields() {
        TypeElement type = getTypeElement(Model.class);

        List<VariableElement> fields = findAllDeclaredFields(type, alwaysTrue());
        assertModelAllFields(fields);

        fields = findAllDeclaredFields(type, alwaysFalse());
        assertEmptyList(fields);

        fields = findAllDeclaredFields(type, f -> "f".equals(f.getSimpleName().toString()));
        assertEquals(1, fields.size());
        assertEquals("f", fields.get(0).getSimpleName().toString());
    }

    @Test
    public void testFindAllDeclaredFieldsOnNull() {
        assertEmptyList(findAllDeclaredFields(NULL_ELEMENT, alwaysTrue()));
        assertEmptyList(findAllDeclaredFields(NULL_TYPE_MIRROR, alwaysTrue()));
    }

    @Test
    public void testFilterDeclaredFieldsOnNull() {
        assertFilterDeclaredFieldsReturningEmptyList(NULL_TYPE_MIRROR);
    }

    @Test
    public void testFilterDeclaredFields() {
        TypeMirror type = getTypeMirror(Model.class);
        List<VariableElement> fields = filterDeclaredFields(type, true, alwaysTrue());
        assertModelAllFields(fields);

        fields = filterDeclaredFields(type, true, alwaysFalse());
        assertEmptyList(fields);

        fields = filterDeclaredFields(type, false, alwaysTrue());
        assertModelFields(fields);

        fields = filterDeclaredFields(type, false, alwaysFalse());
        assertEmptyList(fields);
    }

    @Test
    public void testFilterDeclaredFieldsOnNoDeclaredMembers() {
        TypeMirror type = getTypeMirror(Serializable.class);
        assertFilterDeclaredFieldsReturningEmptyList(type);
    }

    @Test
    public void testFilterDeclaredFieldsOnNoDeclaredFields() {
        TypeMirror type = getTypeMirror(Object.class);
        assertFilterDeclaredFieldsReturningEmptyList(type);
    }

    private void assertFilterDeclaredFieldsReturningEmptyList(TypeMirror type) {
        assertEmptyList(filterDeclaredFields(type, true, alwaysTrue()));
        assertEmptyList(filterDeclaredFields(type, false, alwaysTrue()));
        assertEmptyList(filterDeclaredFields(type, true, alwaysFalse()));
        assertEmptyList(filterDeclaredFields(type, false, alwaysFalse()));
        assertEmptyList(filterDeclaredFields(type, true, NULL_PREDICATE_ARRAY));
        assertEmptyList(filterDeclaredFields(type, false, NULL_PREDICATE_ARRAY));
        assertEmptyList(filterDeclaredFields(type, true));
        assertEmptyList(filterDeclaredFields(type, false));
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

        assertFalse(isEnumMemberField(NULL_FIELD));
    }

    @Test
    public void testIsNonStaticField() {
        TypeElement type = getTypeElement(Model.class);
        assertTrue(isNonStaticField(findField(type, "f")));
    }

    @Test
    public void testIsNonStaticFieldOnStaticField() {
        TypeElement type = getTypeElement(Color.class);
        for (Color color : Color.values()) {
            assertFalse(isNonStaticField(findField(type, color.name())));
        }
    }

    @Test
    public void testIsNonStaticFieldOnMethod() {
        TypeElement type = getTypeElement(Model.class);
        ExecutableElement method = findMethod(type, "setF", float.class);
        for (VariableElement parameter : method.getParameters()) {
            assertFalse(isNonStaticField(parameter));
        }
    }

    @Test
    public void testIsField() {
        TypeElement type = getTypeElement(Model.class);
        assertTrue(isField(findField(type, "f")));
        assertTrue(isField(findField(type, "f"), PRIVATE));

        type = getTypeElement(Color.class);
        assertTrue(isField(findField(type, "BLUE"), PUBLIC, STATIC, FINAL));
    }

    @Test
    public void testIsFieldOnMethod() {
        TypeElement type = getTypeElement(Model.class);
        ExecutableElement method = findMethod(type, "getF");
        for (VariableElement parameter : method.getParameters()) {
            assertFalse(isField(parameter));
        }
    }

    @Test
    public void testIsFieldOnNull() {
        assertFalse(isField(NULL_FIELD));
        assertFalse(isField(NULL_FIELD, PUBLIC, STATIC, FINAL));

        TypeElement type = getTypeElement(Model.class);
        assertFalse(isField(findField(type, "f"), NULL_MODIFIER_ARRAY));
    }

    @Test
    public void testGetNonStaticFields() {
        TypeElement type = getTypeElement(Model.class);

        List<VariableElement> fields = getNonStaticFields(type);
        assertModelFields(fields);

        fields = getNonStaticFields(type.asType());
        assertModelFields(fields);

        assertTrue(getAllNonStaticFields(NULL_ELEMENT).isEmpty());
        assertTrue(getAllNonStaticFields(NULL_TYPE_MIRROR).isEmpty());
    }

    @Test
    public void testGetNonStaticFieldsOnNull() {
        assertTrue(getNonStaticFields(NULL_TYPE_MIRROR).isEmpty());
        assertTrue(getNonStaticFields(NULL_ELEMENT).isEmpty());
    }

    @Test
    public void testGetNonStaticFieldsOnEnum() {
        TypeElement type = getTypeElement(ElementType.class);
        List<VariableElement> fields = getNonStaticFields(type);
        assertEmptyList(fields);
    }

    @Test
    public void testGetAllNonStaticFields() {
        TypeElement type = getTypeElement(Model.class);

        List<VariableElement> fields = getAllNonStaticFields(type);
        assertModelAllFields(fields);

        fields = getAllNonStaticFields(type.asType());
        assertModelAllFields(fields);

        assertTrue(getAllNonStaticFields(NULL_ELEMENT).isEmpty());
        assertTrue(getAllNonStaticFields(NULL_TYPE_MIRROR).isEmpty());
    }

    @Test
    public void testEqualsFieldName() {
        TypeElement type = getTypeElement(Model.class);
        String fieldName = "f";
        VariableElement field = findField(type, fieldName);
        assertTrue(equalsFieldName(field, fieldName));
        assertFalse(equalsFieldName(field, "d"));
    }

    @Test
    public void testEqualsFieldNameOnNull() {
        TypeElement type = getTypeElement(Model.class);
        String fieldName = "f";
        VariableElement field = findField(type, fieldName);

        assertFalse(equalsFieldName(NULL_FIELD, EMPTY_STRING));
        assertFalse(equalsFieldName(field, NULL_STRING));
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

        field = getDeclaredField(type.asType(), fieldName);
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
