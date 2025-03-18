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

import io.microsphere.annotation.Since;
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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.MapUtils.ofMap;
import static io.microsphere.reflect.MethodUtils.OBJECT_PUBLIC_METHODS;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.util.AnnotationUtils.ANNOTATION_INTERFACE_METHOD_PREDICATE;
import static io.microsphere.util.AnnotationUtils.CALLER_SENSITIVE_ANNOTATION_CLASS;
import static io.microsphere.util.AnnotationUtils.CALLER_SENSITIVE_ANNOTATION_CLASS_NAME;
import static io.microsphere.util.AnnotationUtils.EMPTY_ANNOTATION_ARRAY;
import static io.microsphere.util.AnnotationUtils.NATIVE_ANNOTATION_TYPES;
import static io.microsphere.util.AnnotationUtils.NON_ANNOTATION_INTERFACE_METHOD_PREDICATE;
import static io.microsphere.util.AnnotationUtils.NON_OBJECT_METHOD_PREDICATE;
import static io.microsphere.util.AnnotationUtils.exists;
import static io.microsphere.util.AnnotationUtils.filterAnnotations;
import static io.microsphere.util.AnnotationUtils.findAllDeclaredAnnotations;
import static io.microsphere.util.AnnotationUtils.findAnnotation;
import static io.microsphere.util.AnnotationUtils.findAttributeValue;
import static io.microsphere.util.AnnotationUtils.findAttributesMap;
import static io.microsphere.util.AnnotationUtils.findDeclaredAnnotations;
import static io.microsphere.util.AnnotationUtils.getAllDeclaredAnnotations;
import static io.microsphere.util.AnnotationUtils.getAttributeValue;
import static io.microsphere.util.AnnotationUtils.getAttributesMap;
import static io.microsphere.util.AnnotationUtils.getDeclaredAnnotations;
import static io.microsphere.util.AnnotationUtils.isAnnotationInterfaceMethod;
import static io.microsphere.util.AnnotationUtils.isAnnotationPresent;
import static io.microsphere.util.AnnotationUtils.isCallerSensitivePresent;
import static io.microsphere.util.AnnotationUtils.isMetaAnnotation;
import static io.microsphere.util.AnnotationUtils.isSameType;
import static io.microsphere.util.AnnotationUtils.isType;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.ClassLoaderUtils.isPresent;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AnnotationUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class AnnotationUtilsTest {

    private static final Method stringEqualsMethod = findMethod(String.class, "equals", Object.class);

    private static final Method annotationTypeMethod = findMethod(Annotation.class, "annotationType");

    private static final Method retentionValueMethod = findMethod(Retention.class, "value");

    private static final Method targetValueMethod = findMethod(Target.class, "value");

    private static final Annotation[] annotationsOfA = A.class.getAnnotations();

    private static final Annotation[] annotationsOfB = B.class.getAnnotations();

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
    public void testNON_OBJECT_METHOD_PREDICATE() {
        assertTrue(NON_OBJECT_METHOD_PREDICATE.test(findMethod(Target.class, "value")));
        assertTrue(NON_OBJECT_METHOD_PREDICATE.test(findMethod(Retention.class, "value")));
        assertTrue(NON_OBJECT_METHOD_PREDICATE.test(stringEqualsMethod));

        OBJECT_PUBLIC_METHODS.forEach(method -> assertFalse(NON_OBJECT_METHOD_PREDICATE.test(method)));
    }

    @Test
    public void testNON_OBJECT_METHOD_PREDICATE_OnNull() {
        assertFalse(NON_OBJECT_METHOD_PREDICATE.test(null));
    }

    @Test
    public void testANNOTATION_INTERFACE_METHOD_PREDICATE() {
        assertTrue(ANNOTATION_INTERFACE_METHOD_PREDICATE.test(annotationTypeMethod));
        assertFalse(ANNOTATION_INTERFACE_METHOD_PREDICATE.test(retentionValueMethod));
        assertFalse(ANNOTATION_INTERFACE_METHOD_PREDICATE.test(targetValueMethod));
    }

    @Test
    public void testANNOTATION_INTERFACE_METHOD_PREDICATE_OnNull() {
        assertFalse(ANNOTATION_INTERFACE_METHOD_PREDICATE.test(null));
    }

    @Test
    public void testNON_ANNOTATION_INTERFACE_METHOD_PREDICATE() {
        assertTrue(NON_ANNOTATION_INTERFACE_METHOD_PREDICATE.test(null));
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
        assertEquals(3, annotations.size());
        assertEquals(DataAccess.class, annotations.get(0).annotationType());
        assertSame(Since.class, annotations.get(1).annotationType());
        assertEquals(DataAccess.class, annotations.get(2).annotationType());
        assertSame(annotations.get(0), annotations.get(2));
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
        assertEquals(2, annotations.size());
        assertEquals(DataAccess.class, annotations.get(0).annotationType());
        assertEquals(Since.class, annotations.get(1).annotationType());
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
        assertEquals(3, annotations.size());
        assertEquals(DataAccess.class, annotations.get(0).annotationType());
        assertEquals(Since.class, annotations.get(1).annotationType());
        assertEquals(DataAccess.class, annotations.get(2).annotationType());
        assertSame(annotations.get(0), annotations.get(2));
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
    public void testFindAllDeclaredAnnotationsNoNull() {
        assertSame(emptyList(), findAllDeclaredAnnotations(null, annotation -> true));
    }

    @Test
    public void testFindDeclaredAnnotations() {
        List<Annotation> annotations = findDeclaredAnnotations(A.class, annotation -> true);
        assertEquals(1, annotations.size());
        assertEquals(DataAccess.class, annotations.get(0).annotationType());
    }

    @Test
    public void testFindDeclaredAnnotationsOnInheritedClass() {
        List<Annotation> annotations = findDeclaredAnnotations(B.class, annotation -> true);
        assertEquals(2, annotations.size());
        assertEquals(DataAccess.class, annotations.get(0).annotationType());
        assertEquals(Since.class, annotations.get(1).annotationType());
    }

    @Test
    public void testFindDeclaredAnnotationsOnAnnotation() {
        List<Annotation> annotations = findDeclaredAnnotations(DataAccess.class, annotation -> true);
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
    public void testFilterAnnotations() {
        assertFilterAnnotations(annotationsOfA);
        assertFilterAnnotations(annotationsOfB);
    }

    @Test
    public void testFilterAnnotationsOnEmpty() {
        assertSame(emptyList(), filterAnnotations(EMPTY_ANNOTATION_ARRAY, annotation -> true));
        assertSame(emptyList(), filterAnnotations(emptyList(), annotation -> true));
    }

    @Test
    public void testFilterAnnotationsOnNull() {
        assertSame(emptyList(), filterAnnotations((Annotation[]) null, annotation -> true));
        assertSame(emptyList(), filterAnnotations((List) null, annotation -> true));
    }

    @Test
    public void testFindAttributeValue() {
        assertNotNull(findAttributeValue(DataAccess.class.getAnnotations(), "value"));
    }

    @Test
    public void testFindAttributeValueOnAttributeNotFound() {
        assertNull(findAttributeValue(annotationsOfA, "value"));
    }

    @Test
    public void testGetAttributeValue() {
        ElementType[] elementTypes = getAttributeValue(DataAccess.class.getAnnotation(Target.class), "value");
        assertEquals(2, elementTypes.length);
        assertEquals(TYPE, elementTypes[0]);
        assertEquals(METHOD, elementTypes[1]);

        RetentionPolicy retentionPolicy = getAttributeValue(DataAccess.class.getAnnotation(Retention.class), "value");
        assertEquals(RUNTIME, retentionPolicy);
    }

    @Test
    public void testGetAttributeValueOnAttributeNotFound() {
        assertNull(getAttributeValue(DataAccess.class.getAnnotation(Target.class), "notFound"));
    }

    @Test
    public void testGetAttributesMap() {
        Map<String, Object> attributesMap = getAttributesMap(B.class.getAnnotation(Since.class));
        assertEquals(2, attributesMap.size());
        assertEquals(ofMap("module", "microsphere-java-core", "value", "1.0.0"), attributesMap);

        attributesMap = getAttributesMap(Since.class.getAnnotation(Target.class));
        assertEquals(1, attributesMap.size());
        assertArrayEquals(ofArray(TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, ANNOTATION_TYPE, PACKAGE, TYPE_PARAMETER, TYPE_USE), (ElementType[]) attributesMap.get("value"));
    }

    @Test
    public void testGetAttributesMapOnNull() {
        assertSame(emptyMap(), getAttributesMap(null));
    }

    @Test
    public void testGetAttributesMapOnNoNoAttribute() {
        assertSame(emptyMap(), getAttributesMap(Target.class.getAnnotation(Documented.class)));
    }

    @Test
    public void testFindAttributesMapWithAttributesNames() {
        Map<String, Object> attributesMap = findAttributesMap(B.class.getAnnotation(Since.class), "module", "value");
        assertEquals(2, attributesMap.size());
        assertEquals(ofMap("module", "microsphere-java-core", "value", "1.0.0"), attributesMap);

        attributesMap = findAttributesMap(B.class.getAnnotation(Since.class), "module");
        assertEquals(1, attributesMap.size());
        assertEquals(ofMap("module", "microsphere-java-core"), attributesMap);

        attributesMap = findAttributesMap(B.class.getAnnotation(Since.class), "notFound");
        assertSame(emptyMap(), attributesMap);
    }

    @Test
    public void testExistsOnArray() {
        assertTrue(exists(annotationsOfA, DataAccess.class));
        assertTrue(exists(annotationsOfB, DataAccess.class));

        assertFalse(exists(Object.class.getAnnotations(), DataAccess.class));
    }

    @Test
    public void testExistsOnCollection() {
        assertTrue(exists(ofList(annotationsOfA), DataAccess.class));
        assertTrue(exists(ofList(annotationsOfA), DataAccess.class));

        assertFalse(exists(ofList(Object.class.getAnnotations()), DataAccess.class));
    }

    @Test
    public void testExistsOnNull() {
        assertFalse(exists((Annotation[]) null, DataAccess.class));
        assertFalse(exists((Collection) null, DataAccess.class));
        assertFalse(exists((Iterable) null, DataAccess.class));
        assertFalse(exists(annotationsOfA, null));
        assertFalse(exists(ofList(annotationsOfA), null));
    }

    @Test
    public void testIsAnnotatedPresentWithArray() {
        assertTrue(isAnnotationPresent(ofArray(A.class), DataAccess.class));
        assertTrue(isAnnotationPresent(ofArray(B.class), DataAccess.class));
        assertFalse(isAnnotationPresent(ofArray(A.class, B.class), Monitored.class));
        assertFalse(isAnnotationPresent(ofArray(B.class, A.class), Monitored.class));
    }

    @Test
    public void testIsAnnotatedPresentWithArrayOnNull() {
        assertFalse(isAnnotationPresent((AnnotatedElement[]) null, DataAccess.class));
        assertFalse(isAnnotationPresent(ofArray(A.class), null));
    }

    @Test
    public void testIsAnnotatedPresentWithArrayOnEmptyArray() {
        assertFalse(isAnnotationPresent(ofArray(), DataAccess.class));
        assertFalse(isAnnotationPresent(ofArray(), Monitored.class));
    }

    @Test
    public void testIsAnnotatedPresentWithAnnotatedElement() {
        assertTrue(isAnnotationPresent(B.class, DataAccess.class));
        assertTrue(isAnnotationPresent(A.class, DataAccess.class));
        assertFalse(isAnnotationPresent(A.class, Monitored.class));
    }

    @Test
    public void testIsAnnotatedPresentWithAnnotatedElementOnNull() {
        assertFalse(isAnnotationPresent((AnnotatedElement) null, DataAccess.class));
        assertFalse(isAnnotationPresent(A.class, (Class) null));
    }

    @Test
    public void testIsAnnotatedPresentWithAnnotation() {
        assertFalse(isAnnotationPresent(B.class.getAnnotation(DataAccess.class), DataAccess.class));
        assertFalse(isAnnotationPresent(A.class.getAnnotation(DataAccess.class), DataAccess.class));
        assertTrue(isAnnotationPresent(A.class.getAnnotation(DataAccess.class), Monitored.class));
    }

    @Test
    public void testIsAnnotatedPresentWithAnnotationElementOnNull() {
        assertFalse(isAnnotationPresent((Annotation) null, DataAccess.class));
        assertFalse(isAnnotationPresent(A.class.getAnnotation(DataAccess.class), (Class) null));
    }

    @Test
    public void testIsAnnotatedPresentWithAnnotationAndTypes() {
        assertTrue(isAnnotationPresent(B.class.getAnnotation(DataAccess.class), ofList(Inherited.class, Monitored.class)));
    }

    @Test
    public void testIsAnnotatedPresentWithAnnotationAndTypesOnNull() {
        assertFalse(isAnnotationPresent((Annotation) null, ofList(DataAccess.class, Monitored.class)));
        assertFalse(isAnnotationPresent(B.class.getAnnotation(DataAccess.class), (Iterable) null));
    }


    @Test
    public void testIsAnnotationInterfaceMethod() {
        assertTrue(isAnnotationInterfaceMethod(annotationTypeMethod));
        assertFalse(isAnnotationInterfaceMethod(retentionValueMethod));
        assertFalse(isAnnotationInterfaceMethod(targetValueMethod));
    }


    @Test
    public void testIsCallerSensitivePresent() {
        assertEquals(isPresent(CALLER_SENSITIVE_ANNOTATION_CLASS_NAME), isCallerSensitivePresent());
        assertEquals(CALLER_SENSITIVE_ANNOTATION_CLASS != null, isCallerSensitivePresent());
    }

    private void assertFilterAnnotations(Annotation[] annotations) {
        assertEquals(ofList(annotations), filterAnnotations(annotations, annotation -> true));
        assertEquals(ofList(annotations), filterAnnotations(ofList(annotations), annotation -> true));

        assertSame(emptyList(), filterAnnotations(annotations, annotation -> false));
        assertSame(emptyList(), filterAnnotations(ofList(annotations), annotation -> false));
    }


    @DataAccess
    class A {
    }

    @Since(module = "microsphere-java-core", value = "1.0.0")
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


