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
package io.microsphere.util;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Native;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;

import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.util.AnnotationUtils.ANNOTATION_METHOD_PREDICATE;
import static io.microsphere.util.AnnotationUtils.CALLER_SENSITIVE_ANNOTATION_CLASS;
import static io.microsphere.util.AnnotationUtils.CALLER_SENSITIVE_ANNOTATION_CLASS_NAME;
import static io.microsphere.util.AnnotationUtils.INHERITED_OBJECT_METHOD_PREDICATE;
import static io.microsphere.util.AnnotationUtils.NATIVE_ANNOTATION_TYPES;
import static io.microsphere.util.AnnotationUtils.NON_ANNOTATION_METHOD_PREDICATE;
import static io.microsphere.util.AnnotationUtils.NON_INHERITED_OBJECT_METHOD_PREDICATE;
import static io.microsphere.util.AnnotationUtils.findAllDeclaredAnnotations;
import static io.microsphere.util.AnnotationUtils.findAnnotation;
import static io.microsphere.util.AnnotationUtils.findDeclaredAnnotations;
import static io.microsphere.util.AnnotationUtils.getAllDeclaredAnnotations;
import static io.microsphere.util.AnnotationUtils.getDeclaredAnnotations;
import static io.microsphere.util.AnnotationUtils.isAnnotationMethod;
import static io.microsphere.util.AnnotationUtils.isAnnotationPresent;
import static io.microsphere.util.AnnotationUtils.isCallerSensitivePresent;
import static io.microsphere.util.AnnotationUtils.isMetaAnnotation;
import static io.microsphere.util.AnnotationUtils.isSameType;
import static io.microsphere.util.AnnotationUtils.isType;
import static io.microsphere.util.ClassLoaderUtils.isPresent;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AnnotationUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class AnnotationUtilsTest {

    private static final Method equalsMethod = findMethod(String.class, "equals", Object.class);

    private static final Method annotationTypeMethod = findMethod(Annotation.class, "annotationType");

    private static final Method retentionValueMethod = findMethod(Retention.class, "value");

    private static final Method targetValueMethod = findMethod(Target.class, "value");

    @Test
    public void testNATIVE_ANNOTATION_TYPES() {
        assertEquals(6, NATIVE_ANNOTATION_TYPES.size());
        assertTrue(NATIVE_ANNOTATION_TYPES.contains(Target.class));
        assertTrue(NATIVE_ANNOTATION_TYPES.contains(Retention.class));
        assertTrue(NATIVE_ANNOTATION_TYPES.contains(Documented.class));
        assertTrue(NATIVE_ANNOTATION_TYPES.contains(Inherited.class));
        assertTrue(NATIVE_ANNOTATION_TYPES.contains(Native.class));
        assertTrue(NATIVE_ANNOTATION_TYPES.contains(Repeatable.class));
    }

    @Test
    public void testINHERITED_OBJECT_METHOD_PREDICATE() {
        assertTrue(INHERITED_OBJECT_METHOD_PREDICATE.test(equalsMethod));
    }

    @Test
    public void testINHERITED_OBJECT_METHOD_PREDICATE_OnNull() {
        assertFalse(INHERITED_OBJECT_METHOD_PREDICATE.test(null));
    }

    @Test
    public void testNON_INHERITED_OBJECT_METHOD_PREDICATE() {
        assertFalse(NON_INHERITED_OBJECT_METHOD_PREDICATE.test(equalsMethod));
    }

    @Test
    public void testANNOTATION_METHOD_PREDICATE() {
        assertTrue(ANNOTATION_METHOD_PREDICATE.test(annotationTypeMethod));
        assertTrue(ANNOTATION_METHOD_PREDICATE.test(retentionValueMethod));
        assertTrue(ANNOTATION_METHOD_PREDICATE.test(targetValueMethod));
    }

    @Test
    public void testANNOTATION_METHOD_PREDICATE_OnNull() {
        assertFalse(ANNOTATION_METHOD_PREDICATE.test(null));
    }

    @Test
    public void testNON_ANNOTATION_METHOD_PREDICATE() {
        assertTrue(NON_ANNOTATION_METHOD_PREDICATE.test(null));
    }

    @Test
    public void testIsType() {
        assertTrue(isType(A.class));
    }

    @Test
    public void testIsType_OnNull() {
        assertFalse(isType(null));
    }

    @Test
    public void testIsSameType() {
        DataAccess dataAccess = A.class.getAnnotation(DataAccess.class);
        assertTrue(isSameType(dataAccess, DataAccess.class));

        dataAccess = B.class.getAnnotation(DataAccess.class);
        assertTrue(isSameType(dataAccess, DataAccess.class));
    }

    @Test
    public void testIsSameTypeOnNull() {
        assertFalse(isSameType(null, DataAccess.class));
        assertFalse(isSameType(A.class.getAnnotation(DataAccess.class), null));
    }

    @Test
    public void testFindAnnotation() {
        DataAccess dataAccess = findAnnotation(A.class, DataAccess.class);
        assertNotNull(dataAccess);
        assertSame(dataAccess, findAnnotation(B.class, DataAccess.class));
        assertSame(dataAccess, B.class.getAnnotation(DataAccess.class));
    }

    @Test
    public void testIsMetaAnnotationOnAnnotationObject() {
        DataAccess dataAccess = findAnnotation(A.class, DataAccess.class);
        assertTrue(isMetaAnnotation(dataAccess, Monitored.class));
        assertTrue(isMetaAnnotation(dataAccess, ServiceMode.class));
    }

    @Test
    public void testIsMetaAnnotation() {
        assertTrue(isMetaAnnotation(Monitored.class, ServiceMode.class));
        assertTrue(isMetaAnnotation(DataAccess.class, ServiceMode.class));
    }

    @Test
    public void testGetAllDeclaredAnnotations() {
        List<Annotation> annotations = getAllDeclaredAnnotations(A.class);
        assertEquals(1, annotations.size());
        assertEquals(DataAccess.class, annotations.get(0).annotationType());
    }

    @Test
    public void testGetAllDeclaredAnnotationsOnInheritedClass() {
        List<Annotation> annotations = getAllDeclaredAnnotations(B.class);
        assertEquals(2, annotations.size());
        assertSame(annotations.get(0), annotations.get(1));
        assertEquals(DataAccess.class, annotations.get(0).annotationType());
        assertEquals(DataAccess.class, annotations.get(1).annotationType());
    }

    @Test
    public void testGetAllDeclaredAnnotationsOnAnnotation() {
        List<Annotation> annotations = getAllDeclaredAnnotations(DataAccess.class);
        assertEquals(4, annotations.size());
        assertEquals(Inherited.class, annotations.get(0).annotationType());
        assertEquals(Target.class, annotations.get(1).annotationType());
        assertEquals(Retention.class, annotations.get(2).annotationType());
        assertEquals(Monitored.class, annotations.get(3).annotationType());
    }

    @Test
    public void testGetAllDeclaredAnnotationsNoNull() {
        assertSame(emptyList(), getAllDeclaredAnnotations(null));
    }

    @Test
    public void testGetDeclaredAnnotations() {
        List<Annotation> annotations = getAllDeclaredAnnotations(A.class);
        assertEquals(1, annotations.size());
        assertEquals(DataAccess.class, annotations.get(0).annotationType());
    }

    @Test
    public void testGetDeclaredAnnotationsOnInheritedClass() {
        List<Annotation> annotations = findDeclaredAnnotations(B.class);
        assertEquals(1, annotations.size());
        assertEquals(DataAccess.class, annotations.get(0).annotationType());
    }

    @Test
    public void testGetDeclaredAnnotationsOnAnnotation() {
        List<Annotation> annotations = getAllDeclaredAnnotations(DataAccess.class);
        assertEquals(4, annotations.size());
        assertEquals(Inherited.class, annotations.get(0).annotationType());
        assertEquals(Target.class, annotations.get(1).annotationType());
        assertEquals(Retention.class, annotations.get(2).annotationType());
        assertEquals(Monitored.class, annotations.get(3).annotationType());
    }

    @Test
    public void testGetDeclaredAnnotationsNoNull() {
        assertSame(emptyList(), getDeclaredAnnotations(null));
    }

    @Test
    public void testFindAllDeclaredAnnotations() {
        List<Annotation> annotations = findAllDeclaredAnnotations(A.class, annotation -> true);
        assertEquals(1, annotations.size());
        assertEquals(DataAccess.class, annotations.get(0).annotationType());
    }

    @Test
    public void testFindAllDeclaredAnnotationsOnInheritedClass() {
        List<Annotation> annotations = findAllDeclaredAnnotations(B.class, annotation -> true);
        assertEquals(2, annotations.size());
        assertSame(annotations.get(0), annotations.get(1));
        assertEquals(DataAccess.class, annotations.get(0).annotationType());
        assertEquals(DataAccess.class, annotations.get(1).annotationType());
    }

    @Test
    public void testFindAllDeclaredAnnotationsOnAnnotation() {
        List<Annotation> annotations = findAllDeclaredAnnotations(DataAccess.class, annotation -> true);
        assertEquals(4, annotations.size());
        assertEquals(Inherited.class, annotations.get(0).annotationType());
        assertEquals(Target.class, annotations.get(1).annotationType());
        assertEquals(Retention.class, annotations.get(2).annotationType());
        assertEquals(Monitored.class, annotations.get(3).annotationType());
    }

    @Test
    public void testFindDeclaredAnnotationsNoNull() {
        assertSame(emptyList(), findDeclaredAnnotations(null, annotation -> true));
    }

    @Test
    public void testFilterAnnotation() {

    }

    @Test
    public void testFilterAnnotations() {

    }

    @Test
    public void testGetAttributeValue() {

    }

    @Test
    public void testContains() {

    }

    @Test
    public void testExists() {

    }

    @Test
    public void testExistsAnnotated() {

    }

    @Test
    public void testIsAnnotatedPresent() {
        assertTrue(isAnnotationPresent(B.class, DataAccess.class));
        assertTrue(isAnnotationPresent(A.class, DataAccess.class));
        assertFalse(isAnnotationPresent(A.class, Monitored.class));
    }

    @Test
    public void testGetAttributeValues() {

    }

    @Test
    public void testGetAttributesMap() {

    }

    @Test
    public void testIsAnnotationMethod() {
        isAnnotationMethod(annotationTypeMethod);
        isAnnotationMethod(retentionValueMethod);
        isAnnotationMethod(targetValueMethod);
    }


    @Test
    public void testIsCallerSensitivePresent() {
        assertEquals(isPresent(CALLER_SENSITIVE_ANNOTATION_CLASS_NAME), isCallerSensitivePresent());
        assertEquals(CALLER_SENSITIVE_ANNOTATION_CLASS != null, isCallerSensitivePresent());
    }


    @DataAccess
    class A {
    }


    class B extends A {

    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    public @interface ServiceMode {
    }


    @Inherited
    @Target({TYPE, METHOD})
    @Retention(RUNTIME)
    @ServiceMode
    @interface Monitored {

    }

    @Inherited
    @Target({TYPE, METHOD})
    @Retention(RUNTIME)
    @Monitored
    @interface DataAccess {
    }

}


