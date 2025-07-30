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
import io.microsphere.annotation.processor.TestAnnotation;
import io.microsphere.annotation.processor.TestService;
import io.microsphere.annotation.processor.TestServiceImpl;
import io.microsphere.annotation.processor.model.Model;
import io.microsphere.annotation.processor.model.element.StringAnnotationValue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.xml.ws.ServiceMode;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static io.microsphere.annotation.processor.util.AnnotationUtils.EMPTY_ELEMENT_TYPE_ARRAY;
import static io.microsphere.annotation.processor.util.AnnotationUtils.findAllAnnotations;
import static io.microsphere.annotation.processor.util.AnnotationUtils.findAnnotation;
import static io.microsphere.annotation.processor.util.AnnotationUtils.findAnnotations;
import static io.microsphere.annotation.processor.util.AnnotationUtils.findMetaAnnotation;
import static io.microsphere.annotation.processor.util.AnnotationUtils.getAllAnnotations;
import static io.microsphere.annotation.processor.util.AnnotationUtils.getAnnotation;
import static io.microsphere.annotation.processor.util.AnnotationUtils.getAnnotations;
import static io.microsphere.annotation.processor.util.AnnotationUtils.getAttribute;
import static io.microsphere.annotation.processor.util.AnnotationUtils.getAttributeName;
import static io.microsphere.annotation.processor.util.AnnotationUtils.getAttributesMap;
import static io.microsphere.annotation.processor.util.AnnotationUtils.getElementTypes;
import static io.microsphere.annotation.processor.util.AnnotationUtils.getElementValue;
import static io.microsphere.annotation.processor.util.AnnotationUtils.getElementValues;
import static io.microsphere.annotation.processor.util.AnnotationUtils.getValue;
import static io.microsphere.annotation.processor.util.AnnotationUtils.isAnnotationPresent;
import static io.microsphere.annotation.processor.util.AnnotationUtils.matchesAnnotationTypeName;
import static io.microsphere.annotation.processor.util.AnnotationUtils.matchesAttributeMethod;
import static io.microsphere.annotation.processor.util.AnnotationUtils.matchesAttributeValue;
import static io.microsphere.annotation.processor.util.AnnotationUtils.matchesDefaultAttributeValue;
import static io.microsphere.annotation.processor.util.FieldUtils.findField;
import static io.microsphere.annotation.processor.util.MethodUtils.findMethod;
import static io.microsphere.annotation.processor.util.MethodUtils.getAllDeclaredMethods;
import static io.microsphere.lang.function.Predicates.alwaysFalse;
import static io.microsphere.lang.function.Predicates.alwaysTrue;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.StringUtils.EMPTY_STRING;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The {@link AnnotationUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class AnnotationUtilsTest extends AbstractAnnotationProcessingTest {

    @Test
    void testGetAnnotation() {
        asserGetAnnotation(Service.class);
    }

    @Test
    void testGetAnnotationWithClassName() {
        asserGetAnnotation("org.springframework.stereotype.Service");
    }

    @Test
    void testGetAnnotationOnNull() {
        assertNull(getAnnotation(testTypeElement, NULL_CLASS));
        assertNull(getAnnotation(testTypeElement.asType(), NULL_CLASS));
        assertNull(getAnnotation(NULL_ANNOTATED_CONSTRUCT, NULL_CLASS));
    }

    @Test
    void testGetAnnotationWithClassNameOnNull() {
        assertNull(getAnnotation(testTypeElement, NULL_STRING));
        assertNull(getAnnotation(testTypeElement.asType(), NULL_STRING));
        assertNull(getAnnotation(NULL_ANNOTATED_CONSTRUCT, NULL_STRING));
    }

    @Test
    void testGetAnnotations() {
        List<AnnotationMirror> annotations = getAnnotations(testTypeElement);
        assertEquals(4, annotations.size());
        assertAnnotation(annotations.get(0), Service.class);
        assertAnnotation(annotations.get(1), ServiceMode.class);
        assertAnnotation(annotations.get(2), ComponentScans.class);
        assertAnnotation(annotations.get(3), TestAnnotation.class);
    }

    @Test
    void testGetAnnotationsOnNull() {
        List<AnnotationMirror> annotations = getAnnotations(NULL_ANNOTATED_CONSTRUCT);
        assertEmptyList(annotations);
    }

    @Test
    void testGetAnnotationsWithAnnotationClass() {
        assertGetAnnotations(Service.class);
        assertGetAnnotations(ServiceMode.class);
    }

    @Test
    void testGetAnnotationsWithAnnotationClassOnNull() {
        assertTrue(getAnnotations(NULL_ANNOTATED_CONSTRUCT, NULL_CLASS).isEmpty());
        assertTrue(getAnnotations(testTypeElement, NULL_CLASS).isEmpty());
        assertTrue(getAnnotations(NULL_ANNOTATED_CONSTRUCT, Service.class).isEmpty());
    }

    @Test
    void testGetAnnotationsWithAnnotationClassOnNotFound() {
        List<AnnotationMirror> annotations = getAnnotations(testTypeElement, Override.class);
        assertEquals(0, annotations.size());
    }

    @Test
    void testGetAnnotationsWithAnnotationClassName() {
        assertGetAnnotations("org.springframework.stereotype.Service");
        assertGetAnnotations("javax.xml.ws.ServiceMode");
    }

    @Test
    void testGetAnnotationsWithAnnotationClassNameOnNull() {
        assertTrue(getAnnotations(NULL_ANNOTATED_CONSTRUCT, NULL_STRING).isEmpty());
        assertTrue(getAnnotations(testTypeElement, NULL_STRING).isEmpty());
        assertTrue(getAnnotations(NULL_ANNOTATED_CONSTRUCT, "org.springframework.stereotype.Service").isEmpty());
    }

    @Test
    void testGetAllAnnotations() {
        List<AnnotationMirror> annotations = getAllAnnotations(testTypeElement);
        assertEquals(5, annotations.size());

        annotations = getAllAnnotations(testTypeMirror);
        assertEquals(5, annotations.size());
    }

    @Test
    void testGetAllAnnotationsOnNull() {
        assertEmptyList(getAllAnnotations(NULL_ELEMENT));
        assertEmptyList(getAllAnnotations(NULL_TYPE_MIRROR));
    }

    @Test
    void testGetAllAnnotationsWithAnnotationClass() {
        List<AnnotationMirror> annotations = getAllAnnotations(testTypeElement, Override.class);
        assertEquals(0, annotations.size());

        annotations = getAllAnnotations(testTypeMirror, Override.class);
        assertEquals(0, annotations.size());

        annotations = getAllAnnotations(testTypeElement, Service.class);
        assertEquals(1, annotations.size());

        annotations = getAllAnnotations(testTypeMirror, Service.class);
        assertEquals(1, annotations.size());

        annotations = getAllAnnotations(processingEnv, TestServiceImpl.class);
        assertEquals(5, annotations.size());
    }

    @Test
    void testGetAllAnnotationsWithAnnotationClassOnNull() {
        assertEmptyList(getAllAnnotations(NULL_ELEMENT, NULL_CLASS));
        assertEmptyList(getAllAnnotations(NULL_TYPE_MIRROR, NULL_CLASS));
        assertEmptyList(getAllAnnotations(NULL_PROCESSING_ENVIRONMENT, NULL_CLASS));

        assertEmptyList(getAllAnnotations(NULL_ELEMENT, Service.class));
        assertEmptyList(getAllAnnotations(NULL_TYPE_MIRROR, Service.class));
        assertEmptyList(getAllAnnotations(NULL_PROCESSING_ENVIRONMENT, Service.class));

        assertEmptyList(getAllAnnotations(testTypeElement, NULL_CLASS));
        assertEmptyList(getAllAnnotations(testTypeMirror, NULL_CLASS));
        assertEmptyList(getAllAnnotations(processingEnv, NULL_CLASS));
    }

    @Test
    void testGetAllAnnotationsWithAnnotationClassName() {
        List<AnnotationMirror> annotations = getAllAnnotations(testTypeElement, "java.lang.Override");
        assertEquals(0, annotations.size());

        annotations = getAllAnnotations(testTypeMirror, "org.springframework.stereotype.Service");
        assertEquals(1, annotations.size());
    }

    @Test
    void testGetAllAnnotationsWithAnnotationClassNameOnNull() {
        assertEmptyList(getAllAnnotations(NULL_ELEMENT, NULL_STRING));
        assertEmptyList(getAllAnnotations(NULL_TYPE_MIRROR, NULL_STRING));

        assertTrue(getAllAnnotations(NULL_ELEMENT, "org.springframework.stereotype.Service").isEmpty());
        assertTrue(getAllAnnotations(NULL_TYPE_MIRROR, "org.springframework.stereotype.Service").isEmpty());

        assertEmptyList(getAllAnnotations(testTypeElement, NULL_STRING));
        assertEmptyList(getAllAnnotations(testTypeMirror, NULL_STRING));
    }

    @Test
    void testFindAnnotation() {
        assertFindAnnotation(Service.class);
        assertFindAnnotation(Path.class);
    }

    @Test
    void testFindAnnotationOnNotFound() {
        assertNull(findAnnotation(testTypeMirror, Target.class));
        assertNull(findAnnotation(testTypeElement, Target.class));
        assertNull(findAnnotation(testTypeMirror, Override.class));
        assertNull(findAnnotation(testTypeElement, Override.class));
    }

    @Test
    void testFindAnnotationOnNull() {
        assertNull(findAnnotation(NULL_ELEMENT, NULL_CLASS));
        assertNull(findAnnotation(NULL_TYPE_MIRROR, NULL_CLASS));
        assertNull(findAnnotation(testTypeMirror, NULL_CLASS));
        assertNull(findAnnotation(testTypeElement, NULL_CLASS));

        assertNull(findAnnotation(NULL_ELEMENT, NULL_STRING));
        assertNull(findAnnotation(NULL_TYPE_MIRROR, NULL_STRING));
        assertNull(findAnnotation(testTypeMirror, NULL_STRING));
        assertNull(findAnnotation(testTypeElement, NULL_STRING));
    }

    @Test
    void testFindMetaAnnotationWithAnnotationClass() {
        getAllDeclaredMethods(getTypeElement(TestService.class)).forEach(method -> {
            assertFindMetaAnnotation(method, HttpMethod.class);
        });
    }

    @Test
    void testFindMetaAnnotationWithAnnotationClassOnNotFound() {
        assertNull(findMetaAnnotation(testTypeElement, Service.class));
    }

    @Test
    void testFindMetaAnnotationWithAnnotationClassNameOnNotFound() {
        assertNull(findMetaAnnotation(testTypeElement, "org.springframework.stereotype.Service"));
    }

    @Test
    void testFindMetaAnnotationWithAnnotationClassOnNull() {
        assertNull(findMetaAnnotation(NULL_ELEMENT, NULL_CLASS));
        assertNull(findMetaAnnotation(NULL_ELEMENT, Service.class));
        assertNull(findMetaAnnotation(testTypeElement, NULL_CLASS));
    }

    @Test
    void testFindMetaAnnotationWithAnnotationClassName() {
        getAllDeclaredMethods(getTypeElement(TestService.class)).forEach(method -> {
            assertFindMetaAnnotation(method, "javax.ws.rs.HttpMethod");
        });
    }

    @Test
    void testFindMetaAnnotationWithAnnotationClassNameOnNull() {
        assertNull(findMetaAnnotation(NULL_ELEMENT, NULL_STRING));
        assertNull(findMetaAnnotation(NULL_ELEMENT, "test"));
        assertNull(findMetaAnnotation(testTypeElement, NULL_STRING));
    }

    @Test
    void testFindAllAnnotationsWithTypeMirror() {
        List<AnnotationMirror> annotations = findAllAnnotations(testTypeMirror, alwaysTrue());
        assertEquals(5, annotations.size());

        annotations = findAllAnnotations(testTypeMirror, alwaysFalse());
        assertEmptyList(annotations);
    }

    @Test
    void testFindAllAnnotationsWithTypeElement() {
        List<AnnotationMirror> annotations = findAllAnnotations(testTypeElement, alwaysTrue());
        assertEquals(5, annotations.size());

        annotations = findAllAnnotations(testTypeElement, alwaysFalse());
        assertEmptyList(annotations);
    }

    @Test
    void testFindAllAnnotationsWithMethod() {
        ExecutableElement method = findMethod(testTypeElement, "echo", String.class);

        List<AnnotationMirror> annotations = findAllAnnotations(method, alwaysTrue());
        assertEquals(1, annotations.size());
        assertAnnotation(annotations.get(0), Cacheable.class);

        method = findMethod(getTypeElement(TestService.class), "echo", String.class);

        annotations = findAllAnnotations(method);
        assertEquals(1, annotations.size());
        assertAnnotation(annotations.get(0), GET.class);
    }

    @Test
    void testFindAllAnnotationsWithMethodParameters() {
        ExecutableElement method = findMethod(getTypeElement(TestService.class), "echo", String.class);
        List<? extends VariableElement> parameters = method.getParameters();
        assertEquals(1, parameters.size());

        List<AnnotationMirror> annotations = findAllAnnotations(parameters.get(0), alwaysTrue());
        assertEquals(2, annotations.size());
        assertAnnotation(annotations.get(0), PathParam.class);
        assertAnnotation(annotations.get(1), DefaultValue.class);

        method = findMethod(getTypeElement(TestService.class), "model", Model.class);
        parameters = method.getParameters();
        assertEquals(1, parameters.size());

        annotations = findAllAnnotations(parameters.get(0));
        assertEquals(1, annotations.size());
        assertAnnotation(annotations.get(0), PathParam.class);
    }

    @Test
    void testFindAllAnnotationsWithField() {
        VariableElement field = findField(testTypeElement, "context");

        List<AnnotationMirror> annotations = findAllAnnotations(field, alwaysTrue());
        assertEquals(1, annotations.size());
        assertAnnotation(annotations.get(0), Autowired.class);

        field = findField(testTypeElement, "environment");
        annotations = findAllAnnotations(field, alwaysTrue());
        assertEmptyList(annotations);
    }

    @Test
    void testFindAllAnnotationsWithTypeMirrorOnNull() {
        assertEmptyList(findAllAnnotations(NULL_TYPE_MIRROR, alwaysTrue()));
        assertEmptyList(findAllAnnotations(NULL_TYPE_MIRROR, alwaysFalse()));
    }

    @Test
    void testFindAllAnnotationsWithTypeElementOnNull() {
        assertEmptyList(findAllAnnotations(NULL_TYPE_ELEMENT, alwaysTrue()));
        assertEmptyList(findAllAnnotations(NULL_TYPE_ELEMENT, alwaysFalse()));
    }

    @Test
    void testFindAllAnnotationsWithElementOnNull() {
        assertEmptyList(findAllAnnotations(NULL_ELEMENT, alwaysTrue()));
        assertEmptyList(findAllAnnotations(NULL_ELEMENT, alwaysFalse()));
    }

    @Test
    void testFindAllAnnotationsOnNull() {
        assertEmptyList(findAllAnnotations(NULL_PROCESSING_ENVIRONMENT, Service.class, alwaysTrue()));
        assertEmptyList(findAllAnnotations(NULL_PROCESSING_ENVIRONMENT, Service.class, alwaysTrue()));
        assertEmptyList(findAllAnnotations(NULL_PROCESSING_ENVIRONMENT, "org.springframework.stereotype.Service", alwaysFalse()));
        assertEmptyList(findAllAnnotations(NULL_PROCESSING_ENVIRONMENT, "org.springframework.stereotype.Service", alwaysFalse()));
        assertEmptyList(findAllAnnotations(processingEnv, NULL_TYPE, alwaysTrue()));
        assertEmptyList(findAllAnnotations(processingEnv, NULL_TYPE, alwaysFalse()));
        assertEmptyList(findAllAnnotations(processingEnv, NULL_STRING, alwaysTrue()));
        assertEmptyList(findAllAnnotations(processingEnv, NULL_STRING, alwaysFalse()));
    }

    @Test
    void testMatchesAnnotationClass() {
        AnnotationMirror annotation = findAnnotation(testTypeElement, Service.class);
        assertTrue(AnnotationUtils.matchesAnnotationType(annotation, Service.class));
    }

    @Test
    void testMatchesAnnotationClassOnNull() {
        assertFalse(AnnotationUtils.matchesAnnotationType(NULL_ANNOTATION_MIRROR, Service.class));
        assertFalse(AnnotationUtils.matchesAnnotationType(findAnnotation(testTypeElement, Service.class), NULL_CLASS));
    }

    @Test
    void testMatchesAnnotationTypeName() {
        AnnotationMirror annotation = findAnnotation(testTypeElement, "org.springframework.stereotype.Service");
        assertTrue(matchesAnnotationTypeName(annotation, "org.springframework.stereotype.Service"));
    }

    @Test
    void testMatchesAnnotationTypeNameOnNull() {
        assertFalse(matchesAnnotationTypeName(NULL_ANNOTATION_MIRROR, "org.springframework.stereotype.Service"));
        assertFalse(matchesAnnotationTypeName(findAnnotation(testTypeElement, "org.springframework.stereotype.Service"), NULL_STRING));
    }

    @Test
    void testGetAttribute() {
        assertEquals("testService", getAttribute(findAnnotation(testTypeElement, Service.class), "value"));
        assertEquals("testService", getAttribute(findAnnotation(testTypeElement, Service.class), "value", false));
        assertEquals("/echo", getAttribute(findAnnotation(testTypeElement, Path.class), "value"));

        assertNull(getAttribute(findAnnotation(testTypeElement, Path.class), NULL_STRING));
        assertNull(getAttribute(findAnnotation(testTypeElement, NULL_CLASS), NULL_STRING));

        ExecutableElement echoMethod = findMethod(testTypeElement, "echo", String.class);
        AnnotationMirror cacheableAnnotation = findAnnotation(echoMethod, Cacheable.class);
        String[] cacheNames = getAttribute(cacheableAnnotation, "cacheNames");
        assertArrayEquals(ofArray("cache-1", "cache-2"), cacheNames);

        String key = getAttribute(cacheableAnnotation, "key");
        assertEquals(EMPTY_STRING, key);

        DeclaredType cacheableAnnotationType = cacheableAnnotation.getAnnotationType();
        AnnotationMirror targetAnnotation = findAnnotation(cacheableAnnotationType, Target.class);
        ElementType[] elementTypes = getAttribute(targetAnnotation, "value");
        assertArrayEquals(ofArray(TYPE, METHOD), elementTypes);

    }

    @Test
    void testGetValue() {
        AnnotationMirror pathAnnotation = getAnnotation(getTypeElement(TestService.class), Path.class);
        assertEquals("/echo", getValue(pathAnnotation));
    }

    @Test
    void testIsAnnotationPresentOnAnnotationClass() {
        assertTrue(isAnnotationPresent(testTypeElement, Service.class));
        assertTrue(isAnnotationPresent(testTypeElement, Component.class));
        assertTrue(isAnnotationPresent(testTypeElement, ServiceMode.class));
        assertTrue(isAnnotationPresent(testTypeElement, Inherited.class));
        assertTrue(isAnnotationPresent(testTypeElement, Documented.class));
    }

    @Test
    void testIsAnnotationPresentOnAnnotationClassOnNull() {
        assertFalse(isAnnotationPresent(NULL_ELEMENT, Service.class));
        assertFalse(isAnnotationPresent(testTypeElement, NULL_CLASS));
        assertFalse(isAnnotationPresent(testTypeElement, Override.class));
    }

    @Test
    void testIsAnnotationPresentOnAnnotationClassName() {
        assertTrue(isAnnotationPresent(testTypeElement, "org.springframework.stereotype.Service"));
        assertTrue(isAnnotationPresent(testTypeElement, "org.springframework.stereotype.Component"));
        assertTrue(isAnnotationPresent(testTypeElement, "javax.xml.ws.ServiceMode"));
        assertTrue(isAnnotationPresent(testTypeElement, "java.lang.annotation.Inherited"));
        assertTrue(isAnnotationPresent(testTypeElement, "java.lang.annotation.Documented"));
    }

    @Test
    void testIsAnnotationPresentOnAnnotationClassNameOnNull() {
        assertFalse(isAnnotationPresent(NULL_ELEMENT, "org.springframework.stereotype.Service"));
        assertFalse(isAnnotationPresent(testTypeElement, NULL_STRING));
        assertFalse(isAnnotationPresent(testTypeElement, "java.lang.Override"));

    }

    @Test
    void testFindAnnotations() {
        List<AnnotationMirror> annotations = findAnnotations(testTypeElement);
        assertEquals(4, annotations.size());
        assertAnnotation(annotations.get(0), Service.class);
        assertAnnotation(annotations.get(1), ServiceMode.class);
        assertAnnotation(annotations.get(2), ComponentScans.class);
        assertAnnotation(annotations.get(3), TestAnnotation.class);

        annotations = findAnnotations(testTypeElement, alwaysTrue());
        assertEquals(4, annotations.size());
        assertAnnotation(annotations.get(0), Service.class);
        assertAnnotation(annotations.get(1), ServiceMode.class);
        assertAnnotation(annotations.get(2), ComponentScans.class);
        assertAnnotation(annotations.get(3), TestAnnotation.class);

        annotations = findAnnotations(testTypeElement, alwaysFalse());
        assertEmptyList(annotations);
    }

    @Test
    void testFindAnnotationsOnNotFound() {
        assertEmptyList(findAnnotations(getTypeElement(Serializable.class)));
    }

    @Test
    void testFindAnnotationsOnNull() {
        assertEmptyList(findAnnotations(NULL_ELEMENT));
    }

    @Test
    void testGetAttributeName() {
        Map<ExecutableElement, AnnotationValue> elementValues = getElementValues(testTypeElement, TestAnnotation.class);
        for (Entry<ExecutableElement, AnnotationValue> entry : elementValues.entrySet()) {
            ExecutableElement attributeMethod = entry.getKey();
            assertEquals(attributeMethod.getSimpleName().toString(), getAttributeName(attributeMethod));
        }
    }

    @Test
    void testMatchesAttributeMethod() {
        Map<ExecutableElement, AnnotationValue> elementValues = getElementValues(testTypeElement, TestAnnotation.class);
        for (Entry<ExecutableElement, AnnotationValue> entry : elementValues.entrySet()) {
            ExecutableElement attributeMethod = entry.getKey();
            assertTrue(matchesAttributeMethod(attributeMethod, getAttributeName(attributeMethod)));
        }
    }

    @Test
    void testMatchesAttributeMethodOnNull() {
        assertFalse(matchesAttributeMethod(null, null));

        Map<ExecutableElement, AnnotationValue> elementValues = getElementValues(testTypeElement, TestAnnotation.class);
        for (Entry<ExecutableElement, AnnotationValue> entry : elementValues.entrySet()) {
            ExecutableElement attributeMethod = entry.getKey();
            assertFalse(matchesAttributeMethod(attributeMethod, null));
        }
    }

    @Test
    void testMatchesAttributeValue() {
        Map<ExecutableElement, AnnotationValue> elementValues = getElementValues(testTypeElement, TestAnnotation.class);
        for (Entry<ExecutableElement, AnnotationValue> entry : elementValues.entrySet()) {
            AnnotationValue annotationValue = entry.getValue();
            assertTrue(matchesAttributeValue(annotationValue, annotationValue));
            assertTrue(matchesAttributeValue(annotationValue, annotationValue.getValue()));
        }

        assertTrue(matchesAttributeValue(new StringAnnotationValue(""), new StringAnnotationValue("")));
    }

    @Test
    void testMatchesAttributeValueOnNull() {
        assertTrue(matchesAttributeValue(null, null));
        assertFalse(matchesAttributeValue(null, (Object) null));

        Map<ExecutableElement, AnnotationValue> elementValues = getElementValues(testTypeElement, TestAnnotation.class);
        for (Entry<ExecutableElement, AnnotationValue> entry : elementValues.entrySet()) {
            AnnotationValue annotationValue = entry.getValue();
            assertFalse(matchesAttributeValue(annotationValue, null));
            assertFalse(matchesAttributeValue(annotationValue, (Object) null));
        }
    }

    @Test
    void testMatchesDefaultAttributeValue() {
        Map<ExecutableElement, AnnotationValue> elementValues = getElementValues(testTypeElement, ServiceMode.class);
        for (Entry<ExecutableElement, AnnotationValue> entry : elementValues.entrySet()) {
            ExecutableElement attributeMethod = entry.getKey();
            assertTrue(matchesDefaultAttributeValue(attributeMethod, attributeMethod.getDefaultValue()));
        }
    }

    @Test
    void testGetElementValue() {
        Map<ExecutableElement, AnnotationValue> elementValues = getElementValues(testTypeElement, TestAnnotation.class);
        for (Entry<ExecutableElement, AnnotationValue> entry : elementValues.entrySet()) {
            ExecutableElement attributeMethod = entry.getKey();
            String attributeName = getAttributeName(attributeMethod);
            assertEquals(entry, getElementValue(elementValues, attributeName));
        }

        assertNull(getElementValue(elementValues, "unknown"));
    }

    @Test
    void testGetElementValueOnEmptyElementValues() {
        AnnotationMirror annotation = findAnnotation(testTypeElement, ServiceMode.class);
        Map elementValues = annotation.getElementValues();
        assertNull(getElementValue(elementValues, "value"));
    }

    @Test
    void testGetElementValueOnNull() {
        assertNull(getElementValue(null, "value"));
    }

    @Test
    void testGetElementValuesMapOnAnnotatedClass() {
        Map<String, Object> attributesMap = getAttributesMap(testTypeElement, Service.class);
        assertEquals(1, attributesMap.size());
        assertEquals("testService", attributesMap.get("value"));
    }

    @Test
    void testGetElementValuesMapOnAnnotatedMethod() {
        ExecutableElement method = findMethod(testTypeElement, "echo", String.class);
        Map<String, Object> attributesMap = getAttributesMap(method, Cacheable.class);
        assertEquals(9, attributesMap.size());
        assertArrayEquals(ofArray("cache-1", "cache-2"), (String[]) attributesMap.get("cacheNames"));
    }

    @Test
    void testGetElementValuesMapOnRepeatableAnnotation() {
        Map<String, Object> attributesMap = getAttributesMap(testTypeElement, ComponentScans.class);
        assertEquals(1, attributesMap.size());

        ComponentScans componentScans = testClass.getAnnotation(ComponentScans.class);
        ComponentScan[] componentScanArray = (ComponentScan[]) attributesMap.get("value");
        assertEquals(2, componentScanArray.length);
        assertArrayEquals(componentScanArray, componentScans.value());
    }

    @Test
    void testGetElementValuesMapOnNull() {
        Map<String, Object> attributesMap = getAttributesMap(null, null);
        assertSame(emptyMap(), attributesMap);

        attributesMap = getAttributesMap(testTypeElement, null);
        assertSame(emptyMap(), attributesMap);

        attributesMap = getAttributesMap(null);
        assertSame(emptyMap(), attributesMap);
    }

    @Test
    void testGetElementValuesOnAnnotatedClass() {
        Map<ExecutableElement, AnnotationValue> elementValues = getElementValues(testTypeElement, Service.class);
        assertServiceAttributes(elementValues);

        elementValues = getElementValues(testTypeElement, Service.class, false);
        assertServiceAttributes(elementValues);
    }

    @Test
    void testGetElementValuesOnAnnotatedMethod() {
        ExecutableElement method = findMethod(testTypeElement, "echo", String.class);
        Map<ExecutableElement, AnnotationValue> elementValues = getElementValues(method, Cacheable.class, false);
        assertEquals(1, elementValues.size());
        assertAttributeEntry(elementValues, "cacheNames", ofArray("cache-1", "cache-2"));


        elementValues = getElementValues(method, Cacheable.class, true);
        assertEquals(9, elementValues.size());
        assertAttributeEntry(elementValues, "value", EMPTY_STRING_ARRAY);
        assertAttributeEntry(elementValues, "cacheNames", ofArray("cache-1", "cache-2"));
        assertAttributeEntry(elementValues, "key", EMPTY_STRING);
        assertAttributeEntry(elementValues, "keyGenerator", EMPTY_STRING);
        assertAttributeEntry(elementValues, "cacheManager", EMPTY_STRING);
        assertAttributeEntry(elementValues, "cacheResolver", EMPTY_STRING);
        assertAttributeEntry(elementValues, "condition", EMPTY_STRING);
        assertAttributeEntry(elementValues, "unless", EMPTY_STRING);
        assertAttributeEntry(elementValues, "sync", false);
    }

    @Test
    void testGetElementValuesOnNull() {
        Map<ExecutableElement, AnnotationValue> elementValues = getElementValues(null);
        assertSame(emptyMap(), elementValues);
    }

    @Test
    void testGetElementTypes() {
        assertElementTypes(Service.class, TYPE);
        assertElementTypes(ServiceMode.class, TYPE);
        assertElementTypes(ComponentScans.class, TYPE);
        assertElementTypes(TestAnnotation.class, TYPE);
    }

    void assertElementTypes(Class<? extends Annotation> annotationClass, ElementType... expectedElementTypes) {
        AnnotationMirror annotationMirror = findAnnotation(this.testTypeElement, annotationClass);
        assertArrayEquals(expectedElementTypes, getElementTypes(annotationMirror));
    }

    @Test
    void testGetElementTypesOnNull() {
        assertSame(EMPTY_ELEMENT_TYPE_ARRAY, getElementTypes((AnnotationMirror) null));
        assertSame(EMPTY_ELEMENT_TYPE_ARRAY, getElementTypes((DeclaredType) null));
    }

    void assertServiceAttributes(Map<ExecutableElement, AnnotationValue> attributes) {
        assertEquals(1, attributes.size());
        assertAttributeEntry(attributes, "value", "testService");
    }

    void assertAttributeEntry(Map<ExecutableElement, AnnotationValue> attributes, String attributeName, Object attributeValue) {
        for (Entry<ExecutableElement, AnnotationValue> entry : attributes.entrySet()) {
            ExecutableElement attributeMethod = entry.getKey();
            if (matchesAttributeMethod(attributeMethod, attributeName)) {
                assertAttributeEntry(entry, attributeName, attributeValue);
                break;
            }
        }
    }

    void assertAttributeEntry(Entry<ExecutableElement, AnnotationValue> attributeEntry, String attributeName, Object attributeValue) {
        ExecutableElement attributeMethod = attributeEntry.getKey();
        AnnotationValue annotationValue = attributeEntry.getValue();
        assertEquals(attributeName, getAttributeName(attributeMethod));
        Object value = getAttribute(attributeEntry);
        Class<?> attributeValueClass = value.getClass();
        if (attributeValueClass.isArray()) {
            Class<?> componentType = attributeValueClass.getComponentType();
            if (String.class.equals(componentType)) {
                assertArrayEquals((String[]) attributeValue, (String[]) value);
            }
        } else {
            assertEquals(attributeValue, value);
        }
    }

    private void assertFindMetaAnnotation(Element element, Class<? extends Annotation> annotationClass) {
        assertAnnotation(findMetaAnnotation(element, annotationClass), annotationClass);
    }

    private void assertFindMetaAnnotation(Element element, String annotationClassName) {
        assertAnnotation(findMetaAnnotation(element, annotationClassName), annotationClassName);
    }

    private void assertFindAnnotation(Class<? extends Annotation> annotationClass) {
        assertAnnotation(findAnnotation(testTypeMirror, annotationClass), annotationClass);
        assertAnnotation(findAnnotation(testTypeElement, annotationClass), annotationClass);
        assertAnnotation(findAnnotation(testTypeMirror, annotationClass.getName()), annotationClass);
        assertAnnotation(findAnnotation(testTypeElement, annotationClass.getName()), annotationClass);
    }

    private void asserGetAnnotation(Class<? extends Annotation> annotationClass) {
        AnnotationMirror annotation = getAnnotation(testTypeElement, annotationClass);
        assertAnnotation(annotation, annotationClass);
    }

    private void asserGetAnnotation(String annotationClassName) {
        AnnotationMirror annotation = getAnnotation(testTypeElement, annotationClassName);
        assertAnnotation(annotation, annotationClassName);
    }

    private void assertGetAnnotations(Class<? extends Annotation> annotationClass) {
        List<AnnotationMirror> annotations = getAnnotations(testTypeElement, annotationClass);
        assertEquals(1, annotations.size());
        assertAnnotation(annotations.get(0), annotationClass);
    }

    private void assertGetAnnotations(String annotationClassName) {
        List<AnnotationMirror> annotations = getAnnotations(testTypeElement, annotationClassName);
        assertEquals(1, annotations.size());
        assertAnnotation(annotations.get(0), annotationClassName);
    }

    private void assertAnnotation(AnnotationMirror annotation, Class<? extends Annotation> annotationClass) {
        assertTrue(AnnotationUtils.matchesAnnotationType(annotation, annotationClass));
        assertAnnotation(annotation, annotationClass.getName());
    }

    private void assertAnnotation(AnnotationMirror annotation, String annotationClassName) {
        assertEquals(annotation.getAnnotationType().toString(), annotationClassName);
    }
}
