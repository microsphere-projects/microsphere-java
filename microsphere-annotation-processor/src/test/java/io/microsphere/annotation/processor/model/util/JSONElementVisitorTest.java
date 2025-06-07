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

package io.microsphere.annotation.processor.model.util;


import io.microsphere.annotation.processor.AbstractAnnotationProcessingTest;
import io.microsphere.annotation.processor.TestAnnotation;
import io.microsphere.annotation.processor.model.Color;
import io.microsphere.annotation.processor.model.StringArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.io.Serializable;
import java.util.List;

import static io.microsphere.annotation.processor.util.TypeUtils.ofTypeElement;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link JSONElementVisitor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see JSONElementVisitor
 * @since 1.0.0
 */
class JSONElementVisitorTest extends AbstractAnnotationProcessingTest {

    private StringBuilder jsonBuilder;

    private JSONElementVisitor visitor;

    @BeforeEach
    void setUp() {
        this.jsonBuilder = new StringBuilder();
        this.visitor = new JSONElementVisitor() {

            @Override
            protected boolean doVisitPackage(PackageElement e, StringBuilder jsonBuilder) {
                jsonBuilder.append("visitPackage");
                return TRUE;
            }

            @Override
            protected boolean doVisitTypeParameter(TypeParameterElement e, StringBuilder jsonBuilder) {
                jsonBuilder.append("visitTypeParameter");
                return TRUE;
            }

            @Override
            public Boolean visitVariableAsEnumConstant(VariableElement e, StringBuilder stringBuilder) {
                jsonBuilder.append("visitVariableAsEnumConstant");
                return TRUE;
            }

            @Override
            public Boolean visitVariableAsField(VariableElement e, StringBuilder stringBuilder) {
                jsonBuilder.append("visitVariableAsField");
                return TRUE;
            }

            @Override
            public Boolean visitVariableAsParameter(VariableElement e, StringBuilder stringBuilder) {
                jsonBuilder.append("visitVariableAsParameter");
                return TRUE;
            }

            @Override
            public Boolean visitExecutableAsConstructor(ExecutableElement e, StringBuilder stringBuilder) {
                jsonBuilder.append("visitExecutableAsConstructor");
                return TRUE;
            }

            @Override
            public Boolean visitExecutableAsMethod(ExecutableElement e, StringBuilder stringBuilder) {
                jsonBuilder.append("visitExecutableAsMethod");
                return TRUE;
            }

            @Override
            public Boolean visitTypeAsInterface(TypeElement e, StringBuilder stringBuilder) {
                jsonBuilder.append("visitTypeAsInterface");
                return TRUE;
            }

            @Override
            public Boolean visitTypeAsEnum(TypeElement e, StringBuilder stringBuilder) {
                jsonBuilder.append("visitTypeAsEnum");
                return TRUE;
            }

            @Override
            public Boolean visitTypeAsClass(TypeElement e, StringBuilder stringBuilder) {
                jsonBuilder.append("visitTypeAsClass");
                return TRUE;
            }

            @Override
            public Boolean visitTypeAsAnnotationType(TypeElement e, StringBuilder stringBuilder) {
                jsonBuilder.append("visitTypeAsAnnotationType");
                return TRUE;
            }
        };
    }

    @Test
    void testVisitPackage() {
        assertTrue(visitor.visitPackage(this.elements.getPackageElement("io.microsphere.annotation.processor.model.util"), jsonBuilder));
        assertJson("visitPackage");
    }

    @Test
    void testVisitVariableAsEnumConstant() {
        VariableElement element = getField(Color.class, "RED");
        assertTrue(visitor.visitVariable(element, jsonBuilder));
        assertJson("visitVariableAsEnumConstant");
    }

    @Test
    void testVisitExecutableAsField() {
        VariableElement element = getField(testClass, "context");
        assertTrue(visitor.visitVariable(element, jsonBuilder));
        assertJson("visitVariableAsField");
    }

    @Test
    void testVisitVariableAsParameter() {
        ExecutableElement method = getMethod(testClass, "echo", String.class);
        for (VariableElement parameter : method.getParameters()) {
            assertTrue(visitor.visitVariable(parameter, jsonBuilder));
            assertJson("visitVariableAsParameter");
        }
    }

    @Test
    void testVisitExecutableAsConstructor() {
        ExecutableElement constructor = getConstructor(testClass);
        assertTrue(visitor.visitExecutable(constructor, jsonBuilder));
        assertJson("visitExecutableAsConstructor");
    }

    @Test
    void testVisitExecutableAsMethod() {
        ExecutableElement method = getMethod(testClass, "echo", String.class);
        assertTrue(visitor.visitExecutable(method, jsonBuilder));
        assertJson("visitExecutableAsMethod");
    }

    @Test
    void testVisitTypeAsInterface() {
        TypeElement typeElement = getTypeElement(Serializable.class);
        assertTrue(visitor.visitType(typeElement, jsonBuilder));
        assertJson("visitTypeAsInterface");
    }

    @Test
    void testVisitTypeAsEnum() {
        TypeElement typeElement = getTypeElement(Color.class);
        assertTrue(visitor.visitType(typeElement, jsonBuilder));
        assertTrue(jsonBuilder.toString().startsWith("visitTypeAsEnum"));
    }

    @Test
    void testVisitTypeAsClass() {
        TypeElement typeElement = getTypeElement(testClass);
        assertTrue(visitor.visitType(typeElement, jsonBuilder));
        assertTrue(jsonBuilder.toString().startsWith("visitTypeAsClass"));
    }

    @Test
    void testVisitTypeAsAnnotationType() {
        TypeElement typeElement = getTypeElement(TestAnnotation.class);
        assertTrue(visitor.visitType(typeElement, jsonBuilder));
        assertTrue(jsonBuilder.toString().startsWith("visitTypeAsAnnotationType"));
    }

    @Test
    void testVisitTypeParameter() {
        TypeElement typeElement = getTypeElement(StringArrayList.class);
        TypeMirror superclass = typeElement.getSuperclass();
        TypeElement superTypeElement = ofTypeElement(superclass);
        List<? extends TypeParameterElement> typeParameters = superTypeElement.getTypeParameters();
        for (TypeParameterElement typeParameter : typeParameters) {
            assertTrue(visitor.visitTypeParameter(typeParameter, jsonBuilder));
            assertJson("visitTypeParameter");
        }
    }

    void assertJson(String expected) {
        assertEquals(expected, jsonBuilder.toString());
    }
}