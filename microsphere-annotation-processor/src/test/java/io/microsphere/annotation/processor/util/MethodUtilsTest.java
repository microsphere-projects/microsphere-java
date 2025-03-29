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

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import static io.microsphere.annotation.processor.util.MemberUtils.getDeclaredMembers;
import static io.microsphere.annotation.processor.util.MemberUtils.isPublicNonStatic;
import static io.microsphere.annotation.processor.util.MethodUtils.filterMethods;
import static io.microsphere.annotation.processor.util.MethodUtils.findAllDeclaredMethods;
import static io.microsphere.annotation.processor.util.MethodUtils.findDeclaredMethods;
import static io.microsphere.annotation.processor.util.MethodUtils.findMethod;
import static io.microsphere.annotation.processor.util.MethodUtils.findPublicNonStaticMethods;
import static io.microsphere.annotation.processor.util.MethodUtils.getAllDeclaredMethods;
import static io.microsphere.annotation.processor.util.MethodUtils.getDeclaredMethods;
import static io.microsphere.annotation.processor.util.MethodUtils.getEnclosingElement;
import static io.microsphere.annotation.processor.util.MethodUtils.getMethodName;
import static io.microsphere.annotation.processor.util.MethodUtils.getMethodParameterTypeMirrors;
import static io.microsphere.annotation.processor.util.MethodUtils.getMethodParameterTypeNames;
import static io.microsphere.annotation.processor.util.MethodUtils.getOverrideMethod;
import static io.microsphere.annotation.processor.util.MethodUtils.getReturnTypeName;
import static io.microsphere.annotation.processor.util.MethodUtils.isMethod;
import static io.microsphere.annotation.processor.util.MethodUtils.isPublicNonStaticMethod;
import static io.microsphere.annotation.processor.util.MethodUtils.matches;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.lang.function.Predicates.alwaysFalse;
import static io.microsphere.lang.function.Predicates.alwaysTrue;
import static io.microsphere.reflect.TypeUtils.getTypeNames;
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
        assertEmptyList(methods);

        methods = findDeclaredMethods(testTypeMirror, alwaysFalse());
        assertEmptyList(methods);
    }

    @Test
    public void testFindDeclaredMethodsOnNoMemberType() {
        TypeElement typeElement = getTypeElement(Serializable.class);
        List<ExecutableElement> methods = findDeclaredMethods(typeElement, alwaysTrue());
        assertEmptyList(methods);
    }

    @Test
    public void testFindDeclaredMethodsOnNoMethodType() {
        TypeElement typeElement = getTypeElement(PropertyConstants.class);
        List<ExecutableElement> methods = findDeclaredMethods(typeElement, alwaysTrue());
        assertEmptyList(methods);
    }

    @Test
    public void testFindAllDeclaredMethods() {
        List<ExecutableElement> methods = findAllDeclaredMethods(testTypeElement, alwaysTrue());
        assertEquals(objectMethodsSize + 14, methods.size());

        methods = findAllDeclaredMethods(testTypeMirror, alwaysTrue());
        assertEquals(objectMethodsSize + 14, methods.size());

        methods = findAllDeclaredMethods(testTypeElement, alwaysFalse());
        assertEmptyList(methods);

        methods = findAllDeclaredMethods(testTypeMirror, alwaysFalse());
        assertEmptyList(methods);
    }

    @Test
    public void testFindAllDeclaredMethodsOnNoMemberType() {
        TypeElement typeElement = getTypeElement(Serializable.class);
        List<ExecutableElement> methods = findAllDeclaredMethods(typeElement, alwaysTrue());
        assertEmptyList(methods);
    }

    @Test
    public void testFindAllDeclaredMethodsOnNoMethodType() {
        TypeElement typeElement = getTypeElement(Constants.class);
        List<ExecutableElement> methods = findAllDeclaredMethods(typeElement, alwaysTrue());
        assertEmptyList(methods);
    }

    @Test
    public void testFindAllDeclaredMethodsOnNull() {
        assertEmptyList(findAllDeclaredMethods(NULL_TYPE_ELEMENT, alwaysTrue()));
        assertEmptyList(findAllDeclaredMethods(NULL_TYPE_ELEMENT, alwaysFalse()));
        assertEmptyList(findAllDeclaredMethods(NULL_TYPE_MIRROR, alwaysTrue()));
        assertEmptyList(findAllDeclaredMethods(NULL_TYPE_MIRROR, alwaysFalse()));
    }

    @Test
    public void testFindAllDeclaredMethodsWithExcludedTypes() {
        List<? extends ExecutableElement> methods = findAllDeclaredMethodsWithoutObjectType();
        assertEquals(14, methods.size());
    }

    @Test
    public void testFindAllDeclaredMethodsWithExcludedTypesOnNull() {
        assertEmptyList(findAllDeclaredMethods(NULL_TYPE_ELEMENT, Object.class));
        assertEmptyList(findAllDeclaredMethods(NULL_TYPE_MIRROR, Object.class));
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
        assertEmptyList(findPublicNonStaticMethods(NULL_TYPE_ELEMENT, Object.class));
        assertEmptyList(findPublicNonStaticMethods(NULL_TYPE_MIRROR, Object.class));
    }

    @Test
    public void testIsMethod() {
        List<? extends ExecutableElement> methods = findPublicNonStaticMethods(testTypeElement, Object.class);
        assertEquals(14, methods.stream().map(MethodUtils::isMethod).count());
    }

    @Test
    public void testIsMethodOnNull() {
        assertFalse(isMethod(NULL_METHOD));
    }

    @Test
    public void testIsPublicNonStaticMethod() {
        List<? extends Element> members = getDeclaredMembers(testTypeElement);
        for (Element member : members) {
            if (member instanceof ExecutableElement) {
                ExecutableElement element = (ExecutableElement) member;
                switch (member.getKind()) {
                    case METHOD:
                        assertEquals(isPublicNonStaticMethod(element), isPublicNonStatic(element));
                        break;
                    case CONSTRUCTOR:
                        assertFalse(isPublicNonStaticMethod(element));
                        break;
                }
            }
        }

        // Integer#valueOf(String) is a public static method
        assertFalse(isPublicNonStaticMethod(findMethod(getTypeElement(Integer.class), "valueOf", String.class)));

    }

    @Test
    public void testIsPublicNonStaticMethodOnNull() {
        assertFalse(isPublicNonStaticMethod(NULL_METHOD));
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
    public void testFindMethodOnNotFound() {
        assertNull(findMethod(testTypeElement, "notFound"));
        assertNull(findMethod(testTypeElement, "notFound", String.class));
        assertNull(findMethod(testTypeElement, "notFound", "java.lang.String"));

        assertNull(findMethod(testTypeMirror, "notFound"));
        assertNull(findMethod(testTypeMirror, "notFound", String.class));
        assertNull(findMethod(testTypeMirror, "notFound", "java.lang.String"));
    }

    @Test
    public void testFindMethodOnNull() {
        assertNull(findMethod(NULL_TYPE_ELEMENT, "toString"));
        assertNull(findMethod(NULL_TYPE_ELEMENT, "toString", String.class));
        assertNull(findMethod(NULL_TYPE_ELEMENT, "toString", "java.lang.String"));
        assertNull(findMethod(NULL_TYPE_ELEMENT, "toString", NULL_TYPE_ARRAY));
        assertNull(findMethod(NULL_TYPE_ELEMENT, "toString", NULL_STRING_ARRAY));

        assertNull(findMethod(NULL_TYPE_MIRROR, "toString"));
        assertNull(findMethod(NULL_TYPE_MIRROR, "toString", String.class));
        assertNull(findMethod(NULL_TYPE_MIRROR, "toString", "java.lang.String"));
        assertNull(findMethod(NULL_TYPE_MIRROR, "toString", NULL_TYPE_ARRAY));
        assertNull(findMethod(NULL_TYPE_MIRROR, "toString", NULL_STRING_ARRAY));

        assertNull(findMethod(testTypeElement, NULL_STRING));
        assertNull(findMethod(testTypeElement, NULL_STRING, String.class));
        assertNull(findMethod(testTypeElement, NULL_STRING, "java.lang.String"));
        assertNull(findMethod(testTypeElement, NULL_STRING, NULL_TYPE_ARRAY));
        assertNull(findMethod(testTypeElement, NULL_STRING, NULL_STRING_ARRAY));

        assertNull(findMethod(testTypeMirror, NULL_STRING));
        assertNull(findMethod(testTypeMirror, NULL_STRING, String.class));
        assertNull(findMethod(testTypeMirror, NULL_STRING, "java.lang.String"));
        assertNull(findMethod(testTypeMirror, NULL_STRING, NULL_TYPE_ARRAY));
        assertNull(findMethod(testTypeMirror, NULL_STRING, NULL_STRING_ARRAY));


        assertNull(findMethod(testTypeElement, "toString", NULL_TYPE_ARRAY));
        assertNull(findMethod(testTypeElement, "toString", NULL_STRING_ARRAY));

        assertNull(findMethod(testTypeMirror, "toString", NULL_TYPE_ARRAY));
        assertNull(findMethod(testTypeMirror, "toString", NULL_STRING_ARRAY));
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
    public void testFilterMethods() {

    }

    @Test
    public void testFilterMethodsOnNull() {
        assertEmptyList(filterMethods(NULL_LIST, alwaysTrue()));
        assertEmptyList(filterMethods(NULL_LIST, NULL_PREDICATE_ARRAY));
    }

    @Test
    public void testFilterMethodsOnEmpty() {
        assertEmptyList(filterMethods(emptyList(), alwaysTrue()));
        assertEmptyList(filterMethods(emptyList(), NULL_PREDICATE_ARRAY));
    }

    @Test
    public void testFilterMethodsOnReturningEmptyList() {
        List<ExecutableElement> methods = getDeclaredMethods(testTypeElement);
        assertEmptyList(filterMethods(methods, alwaysFalse()));
        assertSame(methods, filterMethods(methods));
    }

    @Test
    public void testGetMethodName() {
        ExecutableElement method = findMethod(testTypeElement, "echo", "java.lang.String");
        assertEquals("echo", getMethodName(method));
        assertNull(getMethodName(NULL_METHOD));
    }

    @Test
    public void testGetMethodNameOnNull() {
        assertNull(getMethodName(NULL_METHOD));
    }

    @Test
    public void testReturnTypeName() {
        ExecutableElement method = findMethod(testTypeElement, "echo", "java.lang.String");
        assertEquals("java.lang.String", getReturnTypeName(method));
    }

    @Test
    public void testReturnTypeNameOnNull() {
        assertNull(getReturnTypeName(NULL_METHOD));
    }

    @Test
    public void testMatchParameterTypeNames() {
        String[] parameterTypeNames = ofArray("java.lang.String");
        ExecutableElement method = findMethod(testTypeElement, "echo", parameterTypeNames);
        assertArrayEquals(parameterTypeNames, getMethodParameterTypeNames(method));
    }

    @Test
    public void testMatchParameterTypeNamesOnNull() {
        assertSame(EMPTY_STRING_ARRAY, getMethodParameterTypeNames(NULL_METHOD));
    }

    @Test
    public void testMatchParameterTypes() {
        ExecutableElement method = findMethod(testTypeElement, "toString");
        assertEmptyList(getMethodParameterTypeMirrors(method));

        method = findMethod(testTypeElement, "equals", Object.class);
        List<TypeMirror> parameterTypes = getMethodParameterTypeMirrors(method);
        assertEquals(ofList(parameterTypes.toArray(EMPTY_TYPE_MIRROR_ARRAY)), parameterTypes);
    }

    @Test
    public void testMatchParameterTypesOnNull() {
        assertEmptyList(getMethodParameterTypeMirrors(NULL_METHOD));
    }

    @Test
    public void testMatches() {
        assertFindMethod(testClass, "echo", String.class);
    }

    @Test
    public void tstMatchesOnFalse() {
        ExecutableElement method = findMethod(testTypeElement, "echo", String.class);

        Type[] parameterTypes = ofArray(String.class, String.class);
        String[] parameterTypeNames = getTypeNames(parameterTypes);
        assertFalse(matches(method, "echo", parameterTypes));
        assertFalse(matches(method, "echo", parameterTypeNames));

        parameterTypes = ofArray(Object.class);
        parameterTypeNames = getTypeNames(parameterTypes);
        assertFalse(matches(method, "echo", parameterTypes));
        assertFalse(matches(method, "echo", parameterTypeNames));
    }

    @Test
    public void testMatchesOnNull() {
        String methodName = "echo";
        Type[] parameterTypes = ofArray(String.class);
        String[] parameterTypeNames = getTypeNames(parameterTypes);
        ExecutableElement method = findMethod(testTypeElement, methodName, parameterTypes);

        assertFalse(matches(NULL_METHOD, NULL_STRING, parameterTypes));
        assertFalse(matches(method, NULL_STRING, parameterTypes));
        assertFalse(matches(method, methodName, NULL_TYPE_ARRAY));

        assertFalse(matches(NULL_METHOD, NULL_STRING, parameterTypeNames));
        assertFalse(matches(method, NULL_STRING, parameterTypeNames));
        assertFalse(matches(method, methodName, NULL_STRING_ARRAY));
    }

    @Test
    public void testGetEnclosingElement() {
        String methodName = "echo";
        Type[] parameterTypes = ofArray(String.class);
        ExecutableElement method = findMethod(testTypeElement, methodName, parameterTypes);
        assertSame(testTypeElement, getEnclosingElement(method));
    }

    @Test
    public void testGetEnclosingElementOnNull() {
        assertNull(getEnclosingElement(NULL_METHOD));
    }

    private void assertFindMethod(Type type, String methodName, Type... parameterTypes) {
        TypeElement typeElement = getTypeElement(type);
        String[] parameterTypeNames = getTypeNames(parameterTypes);

        ExecutableElement method = findMethod(typeElement, methodName, parameterTypes);
        assertMatchesMethod(method, methodName, parameterTypes);

        method = findMethod(typeElement, methodName, parameterTypeNames);
        assertMatchesMethod(method, methodName, parameterTypes);

        method = findMethod(typeElement.asType(), methodName, parameterTypes);
        assertMatchesMethod(method, methodName, parameterTypes);

        method = findMethod(typeElement.asType(), methodName, parameterTypeNames);
        assertMatchesMethod(method, methodName, parameterTypeNames);
    }

    private void assertMatchesMethod(ExecutableElement method, String methodName, Type... parameterTypes) {
        assertTrue(matches(method, methodName, parameterTypes));
    }

    private void assertMatchesMethod(ExecutableElement method, String methodName, String... parameterTypeNames) {
        assertTrue(matches(method, methodName, parameterTypeNames));
    }

    private List<? extends ExecutableElement> findAllDeclaredMethodsWithoutObjectType() {
        return findAllDeclaredMethods(testTypeElement, Object.class);
    }
}
