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
import org.junit.jupiter.api.Test;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.lang.annotation.ElementType;
import java.util.List;

import static io.microsphere.annotation.processor.util.ElementUtils.filterElements;
import static io.microsphere.annotation.processor.util.ElementUtils.hasModifiers;
import static io.microsphere.annotation.processor.util.ElementUtils.isClass;
import static io.microsphere.annotation.processor.util.ElementUtils.isDeclaredType;
import static io.microsphere.annotation.processor.util.ElementUtils.isExecutable;
import static io.microsphere.annotation.processor.util.ElementUtils.isField;
import static io.microsphere.annotation.processor.util.ElementUtils.isInitializer;
import static io.microsphere.annotation.processor.util.ElementUtils.isInterface;
import static io.microsphere.annotation.processor.util.ElementUtils.isMember;
import static io.microsphere.annotation.processor.util.ElementUtils.isPublicNonStatic;
import static io.microsphere.annotation.processor.util.ElementUtils.isVariable;
import static io.microsphere.annotation.processor.util.ElementUtils.matchParameterTypeNames;
import static io.microsphere.annotation.processor.util.ElementUtils.matchParameterTypes;
import static io.microsphere.annotation.processor.util.ElementUtils.matchesElementKind;
import static io.microsphere.annotation.processor.util.ElementUtils.matchesElementType;
import static io.microsphere.annotation.processor.util.ElementUtils.toElementKind;
import static io.microsphere.annotation.processor.util.MemberUtils.getAllDeclaredMembers;
import static io.microsphere.annotation.processor.util.MemberUtils.getDeclaredMembers;
import static io.microsphere.annotation.processor.util.MethodUtils.findMethod;
import static io.microsphere.collection.ListUtils.ofList;
import static io.microsphere.lang.function.Predicates.alwaysFalse;
import static io.microsphere.lang.function.Predicates.alwaysTrue;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.ElementType.values;
import static java.util.Collections.emptyList;
import static javax.lang.model.element.ElementKind.ANNOTATION_TYPE;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.CONSTRUCTOR;
import static javax.lang.model.element.ElementKind.ENUM;
import static javax.lang.model.element.ElementKind.ENUM_CONSTANT;
import static javax.lang.model.element.ElementKind.EXCEPTION_PARAMETER;
import static javax.lang.model.element.ElementKind.FIELD;
import static javax.lang.model.element.ElementKind.INSTANCE_INIT;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.element.ElementKind.LOCAL_VARIABLE;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.ElementKind.OTHER;
import static javax.lang.model.element.ElementKind.PARAMETER;
import static javax.lang.model.element.ElementKind.RESOURCE_VARIABLE;
import static javax.lang.model.element.ElementKind.STATIC_INIT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.util.ElementFilter.fieldsIn;
import static javax.lang.model.util.ElementFilter.methodsIn;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ElementUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ElementUtils
 * @since 1.0.0
 */
class ElementUtilsTest extends AbstractAnnotationProcessingTest {

    private ExecutableElement echoMethod;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        this.echoMethod = findMethod(testTypeElement, "echo", "java.lang.String");
    }

    @Test
    void testMatchesElementTypeElementKind() {
        assertTrue(matchesElementKind(echoMethod, METHOD));
        assertFalse(matchesElementKind(echoMethod, FIELD));
    }

    @Test
    void testMatchesElementTypeElementKindOnNull() {
        assertFalse(matchesElementKind(NULL_ELEMENT, FIELD));
        assertFalse(matchesElementKind(echoMethod, NULL_ELEMENT_KIND));
    }

    @Test
    void testIsPublicNonStatic() {
        methodsIn(getDeclaredMembers(testTypeElement)).forEach(method -> assertTrue(isPublicNonStatic(method)));

        // Integer#valueOf(String) is a public static method
        assertFalse(isPublicNonStatic(findMethod(getTypeElement(Integer.class), "valueOf", String.class)));
    }

    @Test
    void testIsPublicNonStaticOnNull() {
        assertFalse(isPublicNonStatic(NULL_ELEMENT));
    }

    @Test
    void testHasModifiers() {
        List<? extends Element> members = getAllDeclaredMembers(testTypeElement.asType());
        List<VariableElement> fields = fieldsIn(members);
        assertTrue(hasModifiers(fields.get(0), PRIVATE));
    }

    @Test
    void testHasModifiersOnNull() {
        assertFalse(hasModifiers(NULL_ELEMENT));
        assertFalse(hasModifiers(testTypeElement, null));
    }

    @Test
    void testIsClass() {
        assertTrue(isClass(CLASS));
        assertTrue(isClass(ENUM));
        assertFalse(isClass(INTERFACE));
    }

    @Test
    void testIsClassOnNull() {
        assertFalse(isClass(null));
    }

    @Test
    void testIsInterface() {
        assertTrue(isInterface(INTERFACE));
        assertTrue(isInterface(ANNOTATION_TYPE));
        assertFalse(isInterface(CLASS));
    }

    @Test
    void testIsInterfaceOnNull() {
        assertFalse(isInterface(null));
    }

    @Test
    void testIsDeclaredType() {
        assertTrue(isDeclaredType(CLASS));
        assertTrue(isDeclaredType(ENUM));
        assertTrue(isDeclaredType(INTERFACE));
        assertTrue(isDeclaredType(ANNOTATION_TYPE));
        assertFalse(isDeclaredType(LOCAL_VARIABLE));
    }

    @Test
    void testIsDeclaredTypeOnNull() {
        assertFalse(isDeclaredType(null));
    }

    @Test
    void testIsField() {
        assertTrue(isField(FIELD));
        assertTrue(isField(ENUM_CONSTANT));
        assertFalse(isField(LOCAL_VARIABLE));
    }

    @Test
    void testIsFieldOnNull() {
        assertFalse(isField(null));
    }

    @Test
    void testIsExecutable() {
        assertTrue(isExecutable(METHOD));
        assertTrue(isExecutable(CONSTRUCTOR));
        assertTrue(isExecutable(STATIC_INIT));
        assertTrue(isExecutable(INSTANCE_INIT));
        assertFalse(isExecutable(CLASS));
    }

    @Test
    void testIsExecutableOnNull() {
        assertFalse(isExecutable(null));
    }

    @Test
    void testIsMember() {
        assertTrue(isMember(METHOD));
        assertTrue(isMember(CONSTRUCTOR));
        assertTrue(isMember(STATIC_INIT));
        assertTrue(isMember(INSTANCE_INIT));
        assertTrue(isMember(FIELD));
        assertTrue(isMember(ENUM_CONSTANT));
        assertFalse(isMember(CLASS));
    }

    @Test
    void testIsMemberOnNull() {
        assertFalse(isMember(null));
    }

    @Test
    void testIsInitializer() {
        assertTrue(isInitializer(STATIC_INIT));
        assertTrue(isInitializer(INSTANCE_INIT));
        assertFalse(isInitializer(METHOD));
        assertFalse(isInitializer(CONSTRUCTOR));
        assertFalse(isInitializer(CLASS));
    }

    @Test
    void testIsInitializerOnNull() {
        assertFalse(isInitializer(null));
    }

    @Test
    void testIsVariable() {
        assertTrue(isVariable(ENUM_CONSTANT));
        assertTrue(isVariable(FIELD));
        assertTrue(isVariable(PARAMETER));
        assertTrue(isVariable(LOCAL_VARIABLE));
        assertTrue(isVariable(EXCEPTION_PARAMETER));
        assertTrue(isVariable(RESOURCE_VARIABLE));
        assertFalse(isVariable(CLASS));
    }

    @Test
    void testIsVariableOnNull() {
        assertFalse(isVariable(null));
    }

    @Test
    void testToElementKind() {
        for (ElementType elementType : values()) {
            assertElementKind(elementType);
        }
    }

    @Test
    void testToElementKindOnNull() {
        assertSame(OTHER, toElementKind(null));
    }

    @Test
    void testMatchesElementType() {
        for (ElementType elementType : values()) {
            assertMatchesElementType(elementType);
        }
    }

    @Test
    void testMatchesElementTypeOnNull() {
        assertFalse(matchesElementType(null, (ElementType) null));
        assertFalse(matchesElementType(null, TYPE_USE));
        assertFalse(matchesElementType(toElementKind(TYPE_USE), (ElementType) null));
    }

    @Test
    void testMatchesElementTypeWithArray() {
        for (ElementType elementType : values()) {
            assertTrue(matchesElementType(toElementKind(elementType), values()));
        }
    }

    @Test
    void testMatchesElementTypeWithElement() {
        matchesElementType(this.testTypeElement, TYPE);
    }

    @Test
    void testMatchesElementTypeWithArrayOnNull() {
        assertFalse(matchesElementType(NULL_ELEMENT_KIND));
        assertFalse(matchesElementType(NULL_ELEMENT));

        assertFalse(matchesElementType(NULL_ELEMENT_KIND, (ElementType[]) null));
        assertFalse(matchesElementType(NULL_ELEMENT, (ElementType[]) null));

        assertFalse(matchesElementType(NULL_ELEMENT_KIND, TYPE_USE, PACKAGE));
        assertFalse(matchesElementType(NULL_ELEMENT, TYPE_USE, PACKAGE));

        assertFalse(matchesElementType(toElementKind(TYPE_USE), (ElementType[]) null));
    }

    @Test
    void testFilterElements() {
        assertEmptyList(filterElements(ofList(testTypeElement), alwaysFalse()));
    }

    @Test
    void testFilterElementsOnNull() {
        assertEmptyList(filterElements(NULL_LIST, alwaysTrue()));
        List<ExecutableElement> methods = ofList(echoMethod);
        assertSame(emptyList(), filterElements(methods, NULL_PREDICATE_ARRAY));
    }

    @Test
    void testFilterElementsOnEmpty() {
        assertEmptyList(filterElements(emptyList(), alwaysTrue()));
        List<ExecutableElement> methods = ofList(echoMethod);
        assertEquals(methods, filterElements(methods));
    }

    @Test
    void testMatchParameterTypes() {
        assertTrue(matchParameterTypes(echoMethod.getParameters(), String.class));
        assertFalse(matchParameterTypes(echoMethod.getParameters(), Object.class));
    }

    @Test
    void testMatchParameterTypesOnNull() {
        assertFalse(matchParameterTypes(NULL_LIST, String.class));
        assertFalse(matchParameterTypes(emptyList(), NULL_CLASS_ARRAY));
    }

    @Test
    void testMatchParameterTypeNames() {
        assertTrue(matchParameterTypeNames(echoMethod.getParameters(), "java.lang.String"));
        assertFalse(matchParameterTypeNames(echoMethod.getParameters(), "java.lang.Object"));
    }

    @Test
    void testMatchParameterTypeNamesOnNull() {
        assertFalse(matchParameterTypeNames(NULL_LIST, "java.lang.String"));
        assertFalse(matchParameterTypeNames(emptyList(), NULL_STRING_ARRAY));
    }

    void assertElementKind(ElementType elementType) {
        ElementKind elementKind = toElementKind(elementType);
        assertNotNull(elementKind);
    }

    void assertMatchesElementType(ElementType elementType) {
        assertTrue(matchesElementType(toElementKind(elementType), elementType));
    }
}