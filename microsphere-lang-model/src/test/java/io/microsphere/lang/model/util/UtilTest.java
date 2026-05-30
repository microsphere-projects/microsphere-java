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

package io.microsphere.lang.model.util;

import io.microsphere.test.annotation.processing.AbstractAnnotationProcessingTest;
import io.microsphere.test.service.TestServiceImpl;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import static io.microsphere.lang.model.util.ConstructorUtils.findConstructor;
import static io.microsphere.lang.model.util.FieldUtils.findField;
import static io.microsphere.lang.model.util.MethodUtils.findMethod;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * The utilies class for testing
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractAnnotationProcessingTest
 * @since 1.0.0
 */
public abstract class UtilTest extends AbstractAnnotationProcessingTest {

    @Override
    protected void beforeTest(ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) {
        initTestClass(TestServiceImpl.class);
    }

    protected List<TypeMirror> getTypeMirrors(Type... types) {
        return TypeUtils.getTypeMirrors(processingEnv, types);
    }

    protected TypeMirror getTypeMirror(Type type) {
        return TypeUtils.getTypeMirror(processingEnv, type);
    }

    protected List<TypeElement> getTypeElements(Type... types) {
        return TypeUtils.getTypeElements(processingEnv, types);
    }

    protected TypeElement getTypeElement(Type type) {
        return TypeUtils.getTypeElement(processingEnv, type);
    }

    protected VariableElement getField(Type type, String fieldName) {
        TypeElement typeElement = getTypeElement(type);
        return findField(typeElement, fieldName);
    }

    protected ExecutableElement getMethod(Type type, String methodName, Type... parameterTypes) {
        TypeElement typeElement = getTypeElement(type);
        return findMethod(typeElement, methodName, parameterTypes);
    }

    protected ExecutableElement getConstructor(Type type, Type... parameterTypes) {
        TypeElement typeElement = getTypeElement(type);
        return findConstructor(typeElement, parameterTypes);
    }

    protected Element[] getElements(Type... types) {
        return getTypeMirrors(types).stream().map(TypeUtils::ofTypeElement).toArray(Element[]::new);
    }

    protected DeclaredType getDeclaredType(Type type) {
        return TypeUtils.getDeclaredType(processingEnv, type);
    }

    protected void assertEmptyList(List<?> list) {
        assertSame(emptyList(), list);
    }
}
