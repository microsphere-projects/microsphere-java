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

import io.microsphere.AbstractTestCase;
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
class AnnotationUtilsTest extends AbstractTestCase {

    private static final Method stringEqualsMethod = findMethod(String.class, "equals", Object.class);

    private static final Method annotationTypeMethod = findMethod(Annotation.class, "annotationType");

    private static final Method retentionValueMethod = findMethod(Retention.class, "value");

    private static final Method targetValueMethod = findMethod(Target.class, "value");

    private static final Annotation[] annotationsOfA = A.class.getAnnotations();

    private static final Annotation[] annotationsOfB = B.class.getAnnotations();

    private static final DataAccess dataAccessOfA = A.class.getAnnotation(DataAccess.class);

    private static final DataAccess dataAccessOfB = B.class.getAnnotation(DataAccess.class);


    @Test
    void testNATIVE_ANNOTATION_TYPES() {
        assertEquals(6, NATIVE_ANNOTATION_TYPES.size());
        assertTrue(NATIVE_ANNOTATION_TYPES.contains(Target.class));
        assertTrue(NATIVE_ANNOTATION_TYPES.contains(Retention.class));
        assertTrue(NATIVE_ANNOTATION_TYPES.contains(Documented.class));
        assertTrue(NATIVE_ANNOTATION_TYPES.contains(Inherited.class));
        assertTrue(NATIVE_ANNOTATION_TYPES.contains(Native.class));
        assertTrue(NATIVE_ANNOTATION_TYPES.contains(Repeatable.class));
    }

    @Test
    void testNON_OBJECT_METHOD_PREDICATE() {
        assertTrue(NON_OBJECT_METHOD_PREDICATE.test(findMethod(Target.class, "value")));
        assertTrue(NON_OBJECT_METHOD_PREDICATE.test(findMethod(Retention.class, "value")));
        assertTrue(NON_OBJECT_METHOD_PREDICATE.test(stringEqualsMethod));

        OBJECT_PUBLIC_METHODS.forEach(method -> assertFalse(NON_OBJECT_METHOD_PREDICATE.test(method)));
    }

    @Test
    void testNON_OBJECT_METHOD_PREDICATE_OnNull() {
        assertFalse(NON_OBJECT_METHOD_PREDICATE.test(null));
    }

    @Test
    void testANNOTATION_INTERFACE_METHOD_PREDICATE() {
        assertTrue(ANNOTATION_INTERFACE_METHOD_PREDICATE.test(annotationTypeMethod));
        assertFalse(ANNOTATION_INTERFACE_METHOD_PREDICATE.test(retentionValueMethod));
        assertFalse(ANNOTATION_INTERFACE_METHOD_PREDICATE.test(targetValueMethod));
    }

    @Test
    void testANNOTATION_INTERFACE_METHOD_PREDICATE_OnNull() {
        assertFalse(ANNOTATION_INTERFACE_METHOD_PREDICATE.test(null));
    }

    @Test
    void testNON_ANNOTATION_INTERFACE_METHOD_PREDICATE() {
        assertTrue(NON_ANNOTATION_INTERFACE_METHOD_PREDICATE.test(null));
    }

    @Test
    void testIsType() {
        assertTrue(isType(A.class));
    }

    @Test
    void testIsType_OnNull() {
        assertFalse(isType(null));
    }

    @Test
    void testIsSameType() {
        assertTrue(isSameType(dataAccessOfA, DataAccess.class));
        assertTrue(isSameType(dataAccessOfB, DataAccess.class));
    }

    @Test
    void testIsSameTypeOnNull() {
        assertFalse(isSameType(null, DataAccess.class));
        assertFalse(isSameType(dataAccessOfA, null));
    }

    @Test
    void testFindAnnotation() {
        assertSame(dataAccessOfA, findAnnotation(A.class, DataAccess.class));
        assertSame(dataAccessOfB, findAnnotation(B.class, DataAccess.class));
    }

    @Test
    void testIsMetaAnnotationWithAnnotationObjectAndMetadataAnnotationType() {
        assertTrue(isMetaAnnotation(dataAccessOfA, Monitored.class));
        assertTrue(isMetaAnnotation(dataAccessOfA, ServiceMode.class));
    }

    @Test
    void testIsMetaAnnotationWithAnnotationObjectAndMetadataAnnotationTypeOnNull() {
        assertFalse(isMetaAnnotation(TEST_NULL_ANNOTATION, ServiceMode.class));
        assertFalse(isMetaAnnotation(dataAccessOfA, TEST_NULL_ANNOTATION_CLASS));
    }

    @Test
    void testIsMetaAnnotationWithAnnotationObjectAndMetadataAnnotationTypes() {
        assertTrue(isMetaAnnotation(dataAccessOfA, Monitored.class, ServiceMode.class));
        assertTrue(isMetaAnnotation(dataAccessOfA, ServiceMode.class, Target.class));
    }

    @Test
    void testIsMetaAnnotationWithAnnotationObjectAndMetadataAnnotationTypesOnNull() {
        assertFalse(isMetaAnnotation(TEST_NULL_ANNOTATION, Monitored.class, ServiceMode.class));
        assertFalse(isMetaAnnotation(dataAccessOfA, TEST_NULL_ANNOTATION_CLASSES));
    }

    @Test
    void testIsMetaAnnotationWithAnnotationObjectAndMetadataAnnotationTypesOnEmpty() {
        assertFalse(isMetaAnnotation(dataAccessOfA, TEST_EMPTY_ANNOTATION_CLASSES));
    }

    @Test
    void testIsMetaAnnotationWithAnnotationObjectAndIterableOfMetadataAnnotationTypes() {
        assertTrue(isMetaAnnotation(dataAccessOfA, ofList(Monitored.class, ServiceMode.class)));
        assertTrue(isMetaAnnotation(dataAccessOfA, ofList(ServiceMode.class, Target.class)));
    }

    @Test
    void testIsMetaAnnotationWithAnnotationObjectAndIterableOfMetadataAnnotationTypesOnNull() {
        assertFalse(isMetaAnnotation(TEST_NULL_ANNOTATION, ofList(Monitored.class, ServiceMode.class)));
        assertFalse(isMetaAnnotation(dataAccessOfA, TEST_NULL_LIST));
    }

    @Test
    void testIsMetaAnnotationWithAnnotationObjectAndIterableOfMetadataAnnotationTypesOnEmpty() {
        assertFalse(isMetaAnnotation(dataAccessOfA, TEST_EMPTY_LIST));
    }

    @Test
    void testIsMetaAnnotationWithAnnotationTypeAndMetadataAnnotationType() {
        assertTrue(isMetaAnnotation(Monitored.class, ServiceMode.class));
        assertTrue(isMetaAnnotation(DataAccess.class, ServiceMode.class));
        assertTrue(isMetaAnnotation(DataAccess.class, Monitored.class));
        assertFalse(isMetaAnnotation(DataAccess.class, Repeatable.class));
    }

    @Test
    void testIsMetaAnnotationWithAnnotationTypeAndMetadataAnnotationTypeOnNull() {
        assertFalse(isMetaAnnotation(TEST_NULL_ANNOTATION_CLASS, ServiceMode.class));
        assertFalse(isMetaAnnotation(DataAccess.class, TEST_NULL_ANNOTATION_CLASS));
    }

    @Test
    void testIsMetaAnnotationWithAnnotationTypeAndMetadataAnnotationTypes() {
        assertTrue(isMetaAnnotation(DataAccess.class, ServiceMode.class, Monitored.class));
        assertTrue(isMetaAnnotation(DataAccess.class, Monitored.class, ServiceMode.class));
        assertTrue(isMetaAnnotation(DataAccess.class, ServiceMode.class, Target.class));
        assertTrue(isMetaAnnotation(DataAccess.class, Target.class, ServiceMode.class));
        assertTrue(isMetaAnnotation(DataAccess.class, ofArray(ServiceMode.class)));
    }

    @Test
    void testIsMetaAnnotationWithAnnotationTypeAndMetadataAnnotationTypesOnNull() {
        assertFalse(isMetaAnnotation(DataAccess.class, TEST_NULL_ANNOTATION_CLASSES));
        assertFalse(isMetaAnnotation(TEST_NULL_ANNOTATION_CLASS, ServiceMode.class, Target.class));
    }

    @Test
    void testIsMetaAnnotationWithAnnotationTypeAndIterableOfMetadataAnnotationTypes() {
        assertTrue(isMetaAnnotation(DataAccess.class, ofList(Monitored.class, ServiceMode.class)));
        assertTrue(isMetaAnnotation(DataAccess.class, ofList(ServiceMode.class, Target.class)));
    }

    @Test
    void testIsMetaAnnotationWithAnnotationTypeAndIterableOfMetadataAnnotationTypesOnNull() {
        assertFalse(isMetaAnnotation(TEST_NULL_ANNOTATION_CLASS, ofList(Monitored.class, ServiceMode.class)));
        assertFalse(isMetaAnnotation(DataAccess.class, TEST_NULL_LIST));
    }

    @Test
    void testIsMetaAnnotationWithAnnotationTypeAndIterableOfMetadataAnnotationTypesOnEmpty() {
        assertFalse(isMetaAnnotation(DataAccess.class, TEST_EMPTY_LIST));
    }

    @Test
    void testGetAllDeclaredAnnotations() {
        List<Annotation> annotations = getAllDeclaredAnnotations(A.class);
        assertEquals(1, annotations.size());
        assertEquals(DataAccess.class, annotations.get(0).annotationType());
    }

    @Test
    void testGetAllDeclaredAnnotationsOnInheritedClass() {
        List<Annotation> annotations = getAllDeclaredAnnotations(B.class);
        assertEquals(3, annotations.size());
        assertEquals(DataAccess.class, annotations.get(0).annotationType());
        assertSame(Since.class, annotations.get(1).annotationType());
        assertEquals(DataAccess.class, annotations.get(2).annotationType());
        assertSame(annotations.get(0), annotations.get(2));
    }

    @Test
    void testGetAllDeclaredAnnotationsOnAnnotation() {
        List<Annotation> annotations = getAllDeclaredAnnotations(DataAccess.class);
        assertEquals(4, annotations.size());
        assertEquals(Inherited.class, annotations.get(0).annotationType());
        assertEquals(Target.class, annotations.get(1).annotationType());
        assertEquals(Retention.class, annotations.get(2).annotationType());
        assertEquals(Monitored.class, annotations.get(3).annotationType());
    }

    @Test
    void testGetAllDeclaredAnnotationsNoNull() {
        assertSame(emptyList(), getAllDeclaredAnnotations(null));
    }

    @Test
    void testGetDeclaredAnnotations() {
        List<Annotation> annotations = getDeclaredAnnotations(A.class);
        assertEquals(1, annotations.size());
        assertEquals(DataAccess.class, annotations.get(0).annotationType());
    }

    @Test
    void testGetDeclaredAnnotationsOnInheritedClass() {
        List<Annotation> annotations = getDeclaredAnnotations(B.class);
        assertEquals(2, annotations.size());
        assertEquals(DataAccess.class, annotations.get(0).annotationType());
        assertEquals(Since.class, annotations.get(1).annotationType());
    }

    @Test
    void testGetDeclaredAnnotationsOnAnnotation() {
        List<Annotation> annotations = getDeclaredAnnotations(DataAccess.class);
        assertEquals(4, annotations.size());
        assertEquals(Inherited.class, annotations.get(0).annotationType());
        assertEquals(Target.class, annotations.get(1).annotationType());
        assertEquals(Retention.class, annotations.get(2).annotationType());
        assertEquals(Monitored.class, annotations.get(3).annotationType());
    }

    @Test
    void testGetDeclaredAnnotationsNoNull() {
        assertSame(emptyList(), getDeclaredAnnotations(null));
    }

    @Test
    void testFindAllDeclaredAnnotations() {
        List<Annotation> annotations = findAllDeclaredAnnotations(A.class, annotation -> true);
        assertEquals(1, annotations.size());
        assertEquals(DataAccess.class, annotations.get(0).annotationType());
    }

    @Test
    void testFindAllDeclaredAnnotationsOnInheritedClass() {
        List<Annotation> annotations = findAllDeclaredAnnotations(B.class, annotation -> true);
        assertEquals(3, annotations.size());
        assertEquals(DataAccess.class, annotations.get(0).annotationType());
        assertEquals(Since.class, annotations.get(1).annotationType());
        assertEquals(DataAccess.class, annotations.get(2).annotationType());
        assertSame(annotations.get(0), annotations.get(2));
    }

    @Test
    void testFindAllDeclaredAnnotationsOnAnnotation() {
        List<Annotation> annotations = findAllDeclaredAnnotations(DataAccess.class, annotation -> true);
        assertEquals(4, annotations.size());
        assertEquals(Inherited.class, annotations.get(0).annotationType());
        assertEquals(Target.class, annotations.get(1).annotationType());
        assertEquals(Retention.class, annotations.get(2).annotationType());
        assertEquals(Monitored.class, annotations.get(3).annotationType());
    }

    @Test
    void testFindAllDeclaredAnnotationsNoNull() {
        assertSame(emptyList(), findAllDeclaredAnnotations(null, annotation -> true));
    }

    @Test
    void testFindDeclaredAnnotations() {
        List<Annotation> annotations = findDeclaredAnnotations(A.class, annotation -> true);
        assertEquals(1, annotations.size());
        assertEquals(DataAccess.class, annotations.get(0).annotationType());
    }

    @Test
    void testFindDeclaredAnnotationsOnInheritedClass() {
        List<Annotation> annotations = findDeclaredAnnotations(B.class, annotation -> true);
        assertEquals(2, annotations.size());
        assertEquals(DataAccess.class, annotations.get(0).annotationType());
        assertEquals(Since.class, annotations.get(1).annotationType());
    }

    @Test
    void testFindDeclaredAnnotationsOnAnnotation() {
        List<Annotation> annotations = findDeclaredAnnotations(DataAccess.class, annotation -> true);
        assertEquals(4, annotations.size());
        assertEquals(Inherited.class, annotations.get(0).annotationType());
        assertEquals(Target.class, annotations.get(1).annotationType());
        assertEquals(Retention.class, annotations.get(2).annotationType());
        assertEquals(Monitored.class, annotations.get(3).annotationType());
    }

    @Test
    void testFindDeclaredAnnotationsNoNull() {
        assertSame(emptyList(), findDeclaredAnnotations(null, annotation -> true));
    }

    @Test
    void testFilterAnnotations() {
        assertFilterAnnotations(annotationsOfA);
        assertFilterAnnotations(annotationsOfB);
    }

    @Test
    void testFilterAnnotationsOnEmpty() {
        assertSame(emptyList(), filterAnnotations(EMPTY_ANNOTATION_ARRAY, annotation -> true));
        assertSame(emptyList(), filterAnnotations(emptyList(), annotation -> true));
    }

    @Test
    void testFilterAnnotationsOnNull() {
        assertSame(emptyList(), filterAnnotations((Annotation[]) null, annotation -> true));
        assertSame(emptyList(), filterAnnotations(TEST_NULL_LIST, annotation -> true));
    }

    @Test
    void testFindAttributeValue() {
        assertNotNull(findAttributeValue(DataAccess.class.getAnnotations(), "value"));
    }

    @Test
    void testFindAttributeValueOnAttributeNotFound() {
        assertNull(findAttributeValue(annotationsOfA, "value"));
    }

    @Test
    void testGetAttributeValue() {
        ElementType[] elementTypes = getAttributeValue(DataAccess.class.getAnnotation(Target.class), "value");
        assertEquals(1, elementTypes.length);
        assertEquals(TYPE, elementTypes[0]);

        RetentionPolicy retentionPolicy = getAttributeValue(DataAccess.class.getAnnotation(Retention.class), "value");
        assertEquals(RUNTIME, retentionPolicy);
    }

    @Test
    void testGetAttributeValueOnAttributeNotFound() {
        assertNull(getAttributeValue(DataAccess.class.getAnnotation(Target.class), "notFound"));
    }

    @Test
    void testGetAttributesMap() {
        Map<String, Object> attributesMap = getAttributesMap(B.class.getAnnotation(Since.class));
        assertEquals(2, attributesMap.size());
        assertEquals(ofMap("module", "microsphere-java-core", "value", "1.0.0"), attributesMap);

        attributesMap = getAttributesMap(Since.class.getAnnotation(Target.class));
        assertEquals(1, attributesMap.size());
        assertArrayEquals(ofArray(TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, ANNOTATION_TYPE, PACKAGE, TYPE_PARAMETER, TYPE_USE), (ElementType[]) attributesMap.get("value"));
    }

    @Test
    void testGetAttributesMapOnNull() {
        assertSame(emptyMap(), getAttributesMap(null));
    }

    @Test
    void testGetAttributesMapOnNoNoAttribute() {
        assertSame(emptyMap(), getAttributesMap(Target.class.getAnnotation(Documented.class)));
    }

    @Test
    void testFindAttributesMapWithAttributesNames() {
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
    void testExistsOnArray() {
        assertTrue(exists(annotationsOfA, DataAccess.class));
        assertTrue(exists(annotationsOfB, DataAccess.class));

        assertFalse(exists(Object.class.getAnnotations(), DataAccess.class));
    }

    @Test
    void testExistsWithCollection() {
        assertTrue(exists(ofList(annotationsOfA), DataAccess.class));
        assertTrue(exists(ofList(annotationsOfA), DataAccess.class));

        assertFalse(exists(ofList(Object.class.getAnnotations()), DataAccess.class));
    }

    @Test
    void testExistsOnNull() {
        assertFalse(exists((Annotation[]) null, DataAccess.class));
        assertFalse(exists(TEST_NULL_COLLECTION, DataAccess.class));
        assertFalse(exists(TEST_NULL_ITERABLE, DataAccess.class));
        assertFalse(exists(annotationsOfA, null));
        assertFalse(exists(ofList(annotationsOfA), null));
    }

    @Test
    void testIsAnnotatedPresentWithArray() {
        assertTrue(isAnnotationPresent(ofArray(A.class), DataAccess.class));
        assertTrue(isAnnotationPresent(ofArray(B.class), DataAccess.class));
        assertFalse(isAnnotationPresent(ofArray(A.class, B.class), Monitored.class));
        assertFalse(isAnnotationPresent(ofArray(B.class, A.class), Monitored.class));
    }

    @Test
    void testIsAnnotatedPresentWithArrayOnNull() {
        assertFalse(isAnnotationPresent((AnnotatedElement[]) null, DataAccess.class));
        assertFalse(isAnnotationPresent(ofArray(A.class), null));
    }

    @Test
    void testIsAnnotatedPresentWithArrayOnEmptyArray() {
        assertFalse(isAnnotationPresent(ofArray(), DataAccess.class));
        assertFalse(isAnnotationPresent(ofArray(), Monitored.class));
    }

    @Test
    void testIsAnnotatedPresentWithAnnotatedElement() {
        assertTrue(isAnnotationPresent(B.class, DataAccess.class));
        assertTrue(isAnnotationPresent(A.class, DataAccess.class));
        assertFalse(isAnnotationPresent(A.class, Monitored.class));
    }

    @Test
    void testIsAnnotatedPresentWithAnnotatedElementOnNull() {
        assertFalse(isAnnotationPresent((AnnotatedElement) null, DataAccess.class));
        assertFalse(isAnnotationPresent(A.class, TEST_NULL_ANNOTATION_CLASS));
    }

    @Test
    void testIsAnnotatedPresentWithAnnotation() {
        assertFalse(isAnnotationPresent(dataAccessOfB, DataAccess.class));
        assertFalse(isAnnotationPresent(dataAccessOfA, DataAccess.class));
        assertTrue(isAnnotationPresent(dataAccessOfA, Monitored.class));
    }

    @Test
    void testIsAnnotatedPresentWithAnnotationElementOnNull() {
        assertFalse(isAnnotationPresent(TEST_NULL_ANNOTATION, DataAccess.class));
        assertFalse(isAnnotationPresent(dataAccessOfA, TEST_NULL_ANNOTATION_CLASS));
    }

    @Test
    void testIsAnnotatedPresentWithAnnotationAndAnnotationTypes() {
        assertTrue(isAnnotationPresent(dataAccessOfB, ofList(Inherited.class, Monitored.class)));
    }

    @Test
    void testIsAnnotatedPresentWithAnnotationAndAnnotationTypesOnNull() {
        assertFalse(isAnnotationPresent(TEST_NULL_ANNOTATION, ofList(DataAccess.class, Monitored.class)));
        assertFalse(isAnnotationPresent(dataAccessOfB, TEST_NULL_ITERABLE));
    }

    @Test
    void testIsAnnotatedPresentWithAnnotatedElementAndAnnotationTypes() {
        assertTrue(isAnnotationPresent(B.class, ofList(DataAccess.class, Since.class)));
        assertFalse(isAnnotationPresent(B.class, ofList()));
    }

    @Test
    void testIsAnnotatedPresentWithAnnotatedElementAndAnnotationTypesOnNull() {
        assertFalse(isAnnotationPresent((AnnotatedElement) null, ofList(DataAccess.class, Since.class)));
        assertFalse(isAnnotationPresent(B.class, TEST_NULL_ITERABLE));
    }

    @Test
    void testIsAnnotationInterfaceMethod() {
        assertTrue(isAnnotationInterfaceMethod(annotationTypeMethod));
        assertFalse(isAnnotationInterfaceMethod(retentionValueMethod));
        assertFalse(isAnnotationInterfaceMethod(targetValueMethod));
    }


    @Test
    void testIsCallerSensitivePresent() {
        assertEquals(isPresent(CALLER_SENSITIVE_ANNOTATION_CLASS_NAME), isCallerSensitivePresent());
        assertEquals(CALLER_SENSITIVE_ANNOTATION_CLASS != null, isCallerSensitivePresent());
    }

    private void assertFilterAnnotations(Annotation[] annotations) {
        assertEquals(ofList(annotations), filterAnnotations(annotations, annotation -> true));
        assertEquals(ofList(annotations), filterAnnotations(ofList(annotations), annotation -> true));

        assertSame(emptyList(), filterAnnotations(annotations, annotation -> false));
        assertSame(emptyList(), filterAnnotations(ofList(annotations), annotation -> false));
    }

    @Target(TYPE)
    @Retention(RUNTIME)
    @Inherited
    @Documented
    public @interface ServiceMode {
    }

    @Inherited
    @Target(TYPE)
    @Retention(RUNTIME)
    @ServiceMode
    @interface Monitored {
    }

    @Inherited
    @Target(TYPE)
    @Retention(RUNTIME)
    @Monitored
    @interface DataAccess {
    }

    @DataAccess
    class A {
    }

    @Since(module = "microsphere-java-core", value = "1.0.0")
    class B extends A {
    }

}


