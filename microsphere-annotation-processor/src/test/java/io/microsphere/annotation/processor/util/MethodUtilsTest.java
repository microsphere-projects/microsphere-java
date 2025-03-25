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
import io.microsphere.annotation.processor.TestService;
import io.microsphere.annotation.processor.model.Model;
import io.microsphere.constants.Constants;
import io.microsphere.constants.PropertyConstants;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import static io.microsphere.annotation.processor.util.MethodUtils.findAllDeclaredMethods;
import static io.microsphere.annotation.processor.util.MethodUtils.findDeclaredMethods;
import static io.microsphere.annotation.processor.util.MethodUtils.findMethod;
import static io.microsphere.annotation.processor.util.MethodUtils.findPublicNonStaticMethods;
import static io.microsphere.annotation.processor.util.MethodUtils.getAllDeclaredMethods;
import static io.microsphere.annotation.processor.util.MethodUtils.getDeclaredMethods;
import static io.microsphere.annotation.processor.util.MethodUtils.getMethodName;
import static io.microsphere.annotation.processor.util.MethodUtils.getMethodParameterTypeNames;
import static io.microsphere.annotation.processor.util.MethodUtils.getOverrideMethod;
import static io.microsphere.annotation.processor.util.MethodUtils.getReturnTypeName;
import static io.microsphere.annotation.processor.util.MethodUtils.isMethod;
import static io.microsphere.annotation.processor.util.MethodUtils.isPublicNonStaticMethod;
import static io.microsphere.annotation.processor.util.MethodUtils.matches;
import static io.microsphere.lang.function.Predicates.alwaysFalse;
import static io.microsphere.lang.function.Predicates.alwaysTrue;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MethodUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class MethodUtilsTest extends AbstractAnnotationProcessingTest {

    private List<ExecutableElement> objectMethods;

    private int objectMethodsSize;

    @Override
    protected void addCompiledClasses(Set<Class<?>> compiledClasses) {
        compiledClasses.add(PropertyConstants.class);
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        TypeElement type = getTypeElement(Object.class);
        List<ExecutableElement> methods = getDeclaredMethods(type);
        this.objectMethods = methods;
        this.objectMethodsSize = methods.size();
    }

    @Test
    public void testDeclaredMethods() {
        TypeElement type = getTypeElement(Model.class);
        List<ExecutableElement> methods = getDeclaredMethods(type);
        assertEquals(12, methods.size());

        methods = getDeclaredMethods(type.asType());
        assertEquals(12, methods.size());
    }

    @Test
    public void testDeclaredMethodsOnNull() {
        assertTrue(getDeclaredMethods(NULL_TYPE_ELEMENT).isEmpty());
        assertTrue(getDeclaredMethods(NULL_TYPE_MIRROR).isEmpty());
    }

    @Test
    public void testGetAllDeclaredMethods() {
        TypeElement type = getTypeElement(Model.class);
        List<ExecutableElement> methods = getAllDeclaredMethods(type);
        assertEquals(objectMethodsSize + 22, methods.size());

        methods = getAllDeclaredMethods(type.asType());
        assertEquals(objectMethodsSize + 22, methods.size());
    }

    @Test
    public void testGetAllDeclaredMethodsOnNull() {
        assertTrue(getAllDeclaredMethods(NULL_TYPE_ELEMENT).isEmpty());
        assertTrue(getAllDeclaredMethods(NULL_TYPE_MIRROR).isEmpty());
    }

    @Test
    public void testFindDeclaredMethods() {
        List<ExecutableElement> methods = findDeclaredMethods(testTypeElement, alwaysTrue());
        assertEquals(2, methods.size());

        methods = findDeclaredMethods(testTypeMirror, alwaysTrue());
        assertEquals(2, methods.size());

        methods = findDeclaredMethods(testTypeElement, alwaysFalse());
        assertSame(emptyList(), methods);

        methods = findDeclaredMethods(testTypeMirror, alwaysFalse());
        assertSame(emptyList(), methods);
    }

    @Test
    public void testFindDeclaredMethodsOnNoMemberType() {
        TypeElement typeElement = getTypeElement(Serializable.class);
        List<ExecutableElement> methods = findDeclaredMethods(typeElement, alwaysTrue());
        assertSame(emptyList(), methods);
    }

    @Test
    public void testFindDeclaredMethodsOnNoMethodType() {
        TypeElement typeElement = getTypeElement(PropertyConstants.class);
        List<ExecutableElement> methods = findDeclaredMethods(typeElement, alwaysTrue());
        assertSame(emptyList(), methods);
    }

    @Test
    public void testFindAllDeclaredMethods() {
        List<ExecutableElement> methods = findAllDeclaredMethods(testTypeElement, alwaysTrue());
        assertEquals(objectMethodsSize + 14, methods.size());

        methods = findAllDeclaredMethods(testTypeMirror, alwaysTrue());
        assertEquals(objectMethodsSize + 14, methods.size());

        methods = findAllDeclaredMethods(testTypeElement, alwaysFalse());
        assertSame(emptyList(), methods);

        methods = findAllDeclaredMethods(testTypeMirror, alwaysFalse());
        assertSame(emptyList(), methods);
    }

    @Test
    public void testFindAllDeclaredMethodsOnNoMemberType() {
        TypeElement typeElement = getTypeElement(Serializable.class);
        List<ExecutableElement> methods = findAllDeclaredMethods(typeElement, alwaysTrue());
        assertSame(emptyList(), methods);
    }

    @Test
    public void testFindAllDeclaredMethodsOnNoMethodType() {
        TypeElement typeElement = getTypeElement(Constants.class);
        List<ExecutableElement> methods = findAllDeclaredMethods(typeElement, alwaysTrue());
        assertSame(emptyList(), methods);
    }

    @Test
    public void testFindAllDeclaredMethodsOnNull() {
        assertSame(emptyList(), findAllDeclaredMethods(NULL_TYPE_ELEMENT, alwaysTrue()));
        assertSame(emptyList(), findAllDeclaredMethods(NULL_TYPE_ELEMENT, alwaysFalse()));
        assertSame(emptyList(), findAllDeclaredMethods(NULL_TYPE_MIRROR, alwaysTrue()));
        assertSame(emptyList(), findAllDeclaredMethods(NULL_TYPE_MIRROR, alwaysFalse()));
    }

    @Test
    public void testFindAllDeclaredMethodsWithExcludedTypes() {
        List<? extends ExecutableElement> methods = findAllDeclaredMethodsWithoutObjectType();
        assertEquals(14, methods.size());
    }

    @Test
    public void testFindAllDeclaredMethodsWithExcludedTypesOnNull() {
        assertSame(emptyList(), findAllDeclaredMethods(NULL_TYPE_ELEMENT, Object.class));
        assertSame(emptyList(), findAllDeclaredMethods(NULL_TYPE_MIRROR, Object.class));
    }

    @Test
    public void testFindPublicNonStaticMethods() {
        List<? extends ExecutableElement> methods = findPublicNonStaticMethods(testTypeElement, Object.class);
        assertEquals(14, methods.size());

        methods = findPublicNonStaticMethods(testTypeElement.asType(), Object.class);
        assertEquals(14, methods.size());
    }

    @Test
    public void testFindPublicNonStaticMethodsOnNull() {
        assertSame(emptyList(), findPublicNonStaticMethods(NULL_TYPE_ELEMENT, Object.class));
        assertSame(emptyList(), findPublicNonStaticMethods(NULL_TYPE_MIRROR, Object.class));
    }

    @Test
    public void testIsMethod() {
        List<? extends ExecutableElement> methods = findPublicNonStaticMethods(testTypeElement, Object.class);
        assertEquals(14, methods.stream().map(MethodUtils::isMethod).count());
    }

    @Test
    public void testIsMethodOnNull() {
        assertFalse(isMethod(null));
    }

    @Test
    public void testIsPublicNonStaticMethod() {
        List<? extends ExecutableElement> methods = findPublicNonStaticMethods(testTypeElement, Object.class);
        assertEquals(14, methods.stream().map(MethodUtils::isPublicNonStaticMethod).count());
    }

    @Test
    public void testIsPublicNonStaticMethodOnNull() {
        assertFalse(isPublicNonStaticMethod(null));
    }

    @Test
    public void testFindMethod() {
        // Test methods from java.lang.Object
        // Object#toString()
        Type type = Model.class;
        assertFindMethod(type, "toString");

        // Object#hashCode()
        assertFindMethod(type, "hashCode");

        // Object#getClass()
        assertFindMethod(type, "getClass");

        // Object#finalize()
        assertFindMethod(type, "finalize");

        // Object#clone()
        assertFindMethod(type, "clone");

        // Object#notify()
        assertFindMethod(type, "notify");

        // Object#notifyAll()
        assertFindMethod(type, "notifyAll");

        // Object#wait(long)
        assertFindMethod(type, "wait", long.class);

        // Object#wait(long,int)
        assertFindMethod(type, "wait", long.class, int.class);

        // Object#equals(Object)
        assertFindMethod(type, "equals", Object.class);
    }

    @Test
    public void testFindMethodOnNull() {
        assertNull(findMethod(NULL_TYPE_ELEMENT, "toString"));
        assertNull(findMethod(NULL_TYPE_MIRROR, "toString"));
    }

    @Test
    public void testGetOverrideMethod() {
        List<? extends ExecutableElement> methods = findAllDeclaredMethodsWithoutObjectType();

        ExecutableElement overrideMethod = getOverrideMethod(processingEnv, testTypeElement, methods.get(0));
        assertNull(overrideMethod);

        ExecutableElement declaringMethod = findMethod(getTypeElement(TestService.class), "echo", "java.lang.String");

        overrideMethod = getOverrideMethod(processingEnv, testTypeElement, declaringMethod);
        assertEquals(methods.get(0), overrideMethod);
    }

    @Test
    public void testGetMethodName() {
        ExecutableElement method = findMethod(testTypeElement, "echo", "java.lang.String");
        assertEquals("echo", getMethodName(method));
        assertNull(getMethodName(null));
    }

    @Test
    public void testGetMethodNameOnNull() {
        assertNull(getMethodName(null));
    }

    @Test
    public void testReturnTypeName() {
        ExecutableElement method = findMethod(testTypeElement, "echo", "java.lang.String");
        assertEquals("java.lang.String", getReturnTypeName(method));
    }

    @Test
    public void testReturnTypeNameOnNull() {
        assertNull(getReturnTypeName(null));
    }

    @Test
    public void testMatchParameterTypeNames() {
        String[] parameterTypeNames = ofArray("java.lang.String");
        ExecutableElement method = findMethod(testTypeElement, "echo", parameterTypeNames);
        assertArrayEquals(parameterTypeNames, getMethodParameterTypeNames(method));
    }

    @Test
    public void testMatchParameterTypeNamesOnNull() {
        assertSame(EMPTY_STRING_ARRAY, getMethodParameterTypeNames(null));
    }

    private void assertFindMethod(Type type, String methodName, Type... parameterTypes) {
        TypeElement typeElement = getTypeElement(type);
        ExecutableElement method = findMethod(typeElement, methodName, parameterTypes);
        assertMethod(method, methodName, parameterTypes);

        method = findMethod(typeElement.asType(), methodName, parameterTypes);
        assertMethod(method, methodName, parameterTypes);
    }

    private void assertMethod(ExecutableElement method, String methodName, Type... parameterTypes) {
        assertTrue(matches(method, methodName, parameterTypes));
    }

    private List<? extends ExecutableElement> findAllDeclaredMethodsWithoutObjectType() {
        return findAllDeclaredMethods(testTypeElement, Object.class);
    }
}
