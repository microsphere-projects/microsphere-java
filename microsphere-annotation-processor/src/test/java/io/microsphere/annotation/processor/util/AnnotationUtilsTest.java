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
import io.microsphere.annotation.processor.TestServiceImpl;
import io.microsphere.annotation.processor.model.Model;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.xml.ws.ServiceMode;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;

import static io.microsphere.annotation.processor.util.AnnotationUtils.findAllAnnotations;
import static io.microsphere.annotation.processor.util.AnnotationUtils.findAnnotation;
import static io.microsphere.annotation.processor.util.AnnotationUtils.findMetaAnnotation;
import static io.microsphere.annotation.processor.util.AnnotationUtils.getAllAnnotations;
import static io.microsphere.annotation.processor.util.AnnotationUtils.getAnnotation;
import static io.microsphere.annotation.processor.util.AnnotationUtils.getAnnotations;
import static io.microsphere.annotation.processor.util.AnnotationUtils.getAttribute;
import static io.microsphere.annotation.processor.util.AnnotationUtils.getValue;
import static io.microsphere.annotation.processor.util.AnnotationUtils.isAnnotationPresent;
import static io.microsphere.annotation.processor.util.AnnotationUtils.matchesAnnotationType;
import static io.microsphere.annotation.processor.util.FieldUtils.findField;
import static io.microsphere.annotation.processor.util.MethodUtils.findMethod;
import static io.microsphere.annotation.processor.util.MethodUtils.getAllDeclaredMethods;
import static io.microsphere.lang.function.Predicates.alwaysFalse;
import static io.microsphere.lang.function.Predicates.alwaysTrue;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The {@link AnnotationUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class AnnotationUtilsTest extends AbstractAnnotationProcessingTest {

    @Test
    public void testGetAnnotation() {
        asserGetAnnotation(Service.class);
    }

    @Test
    public void testGetAnnotationWithClassName() {
        asserGetAnnotation(Service.class.getName());
    }

    @Test
    public void testGetAnnotationOnNull() {
        assertNull(getAnnotation(testTypeElement, NULL_CLASS));
        assertNull(getAnnotation(testTypeElement.asType(), NULL_CLASS));
        assertNull(getAnnotation(NULL_ANNOTATED_CONSTRUCT, NULL_CLASS));
    }

    @Test
    public void testGetAnnotationWithClassNameOnNull() {
        assertNull(getAnnotation(testTypeElement, NULL_STRING));
        assertNull(getAnnotation(testTypeElement.asType(), NULL_STRING));
        assertNull(getAnnotation(NULL_ANNOTATED_CONSTRUCT, NULL_STRING));
    }

    @Test
    public void testGetAnnotations() {
        List<AnnotationMirror> annotations = getAnnotations(testTypeElement);
        assertEquals(2, annotations.size());
        assertEquals(Service.class.getName(), annotations.get(0).getAnnotationType().toString());
        assertEquals(ServiceMode.class.getName(), annotations.get(1).getAnnotationType().toString());
    }

    @Test
    public void testGetAnnotationsOnNull() {
        List<AnnotationMirror> annotations = getAnnotations(NULL_ANNOTATED_CONSTRUCT);
        assertSame(emptyList(), annotations);
    }

    @Test
    public void testGetAnnotationsWithAnnotationType() {
        assertGetAnnotations(Service.class);
        assertGetAnnotations(ServiceMode.class);
    }

    @Test
    public void testGetAnnotationsWithAnnotationTypeOnNull() {
        assertTrue(getAnnotations(NULL_ANNOTATED_CONSTRUCT, NULL_CLASS).isEmpty());
        assertTrue(getAnnotations(testTypeElement, NULL_CLASS).isEmpty());
        assertTrue(getAnnotations(NULL_ANNOTATED_CONSTRUCT, Service.class).isEmpty());
    }

    @Test
    public void testGetAnnotationsWithAnnotationTypeOnNotFound() {
        List<AnnotationMirror> annotations = getAnnotations(testTypeElement, Override.class);
        assertEquals(0, annotations.size());
    }

    @Test
    public void testGetAnnotationsWithAnnotationClassName() {
        assertGetAnnotations(Service.class.getName());
        assertGetAnnotations(ServiceMode.class.getName());
    }

    @Test
    public void testGetAnnotationsWithAnnotationClassNameOnNull() {
        assertTrue(getAnnotations(NULL_ANNOTATED_CONSTRUCT, NULL_STRING).isEmpty());
        assertTrue(getAnnotations(testTypeElement, NULL_STRING).isEmpty());
        assertTrue(getAnnotations(NULL_ANNOTATED_CONSTRUCT, Service.class.getName()).isEmpty());
    }

    @Test
    public void testGetAllAnnotations() {
        List<AnnotationMirror> annotations = getAllAnnotations(testTypeElement);
        assertEquals(3, annotations.size());

        annotations = getAllAnnotations(testTypeMirror);
        assertEquals(3, annotations.size());
    }

    @Test
    public void testGetAllAnnotationsOnNull() {
        assertSame(emptyList(), getAllAnnotations(NULL_ELEMENT));
        assertSame(emptyList(), getAllAnnotations(NULL_TYPE_MIRROR));
    }

    @Test
    public void testGetAllAnnotationsWithAnnotationType() {
        List<AnnotationMirror> annotations = getAllAnnotations(testTypeElement, Override.class);
        assertEquals(0, annotations.size());

        annotations = getAllAnnotations(testTypeMirror, Override.class);
        assertEquals(0, annotations.size());

        annotations = getAllAnnotations(testTypeElement, Service.class);
        assertEquals(1, annotations.size());

        annotations = getAllAnnotations(testTypeMirror, Service.class);
        assertEquals(1, annotations.size());

        annotations = getAllAnnotations(processingEnv, TestServiceImpl.class);
        assertEquals(3, annotations.size());
    }

    @Test
    public void testGetAllAnnotationsWithAnnotationTypeOnNull() {
        assertSame(emptyList(), getAllAnnotations(NULL_ELEMENT, NULL_CLASS));
        assertSame(emptyList(), getAllAnnotations(NULL_TYPE_MIRROR, NULL_CLASS));
        assertSame(emptyList(), getAllAnnotations(NULL_PROCESSING_ENVIRONMENT, NULL_CLASS));

        assertSame(emptyList(), getAllAnnotations(NULL_ELEMENT, Service.class));
        assertSame(emptyList(), getAllAnnotations(NULL_TYPE_MIRROR, Service.class));
        assertSame(emptyList(), getAllAnnotations(NULL_PROCESSING_ENVIRONMENT, Service.class));

        assertSame(emptyList(), getAllAnnotations(testTypeElement, NULL_CLASS));
        assertSame(emptyList(), getAllAnnotations(testTypeMirror, NULL_CLASS));
    }

    @Test
    public void testGetAllAnnotationsWithAnnotationClassName() {
        List<AnnotationMirror> annotations = getAllAnnotations(testTypeElement, Override.class.getName());
        assertEquals(0, annotations.size());

        annotations = getAllAnnotations(testTypeMirror, Service.class.getName());
        assertEquals(1, annotations.size());
    }

    @Test
    public void testGetAllAnnotationsWithAnnotationClassNameOnNull() {
        assertSame(emptyList(), getAllAnnotations(NULL_ELEMENT, NULL_STRING));
        assertSame(emptyList(), getAllAnnotations(NULL_TYPE_MIRROR, NULL_STRING));

        assertTrue(getAllAnnotations(NULL_ELEMENT, Service.class.getName()).isEmpty());
        assertTrue(getAllAnnotations(NULL_TYPE_MIRROR, Service.class.getName()).isEmpty());

        assertSame(emptyList(), getAllAnnotations(testTypeElement, NULL_STRING));
        assertSame(emptyList(), getAllAnnotations(testTypeMirror, NULL_STRING));
    }

    @Test
    public void testFindAnnotation() {
        assertFindAnnotation(Service.class);
        assertFindAnnotation(Path.class);
    }

    @Test
    public void testFindAnnotationOnNotFound() {
        assertNull(findAnnotation(testTypeMirror, Target.class));
        assertNull(findAnnotation(testTypeElement, Target.class));
        assertNull(findAnnotation(testTypeMirror, Override.class));
        assertNull(findAnnotation(testTypeElement, Override.class));
    }

    @Test
    public void testFindAnnotationOnNull() {
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
    public void testFindMetaAnnotation() {
        getAllDeclaredMethods(getTypeElement(TestService.class)).forEach(method -> {
            assertEquals("javax.ws.rs.HttpMethod", findMetaAnnotation(method, "javax.ws.rs.HttpMethod").getAnnotationType().toString());
        });
    }

    @Test
    public void testFindMetaAnnotationOnNull() {
        assertNull(findMetaAnnotation(NULL_ELEMENT, NULL_STRING));
        assertNull(findMetaAnnotation(NULL_ELEMENT, "test"));
    }

    @Test
    public void testFindAllAnnotationsWithTypeMirror() {
        List<AnnotationMirror> annotations = findAllAnnotations(testTypeMirror, alwaysTrue());
        assertEquals(3, annotations.size());

        annotations = findAllAnnotations(testTypeMirror, alwaysFalse());
        assertSame(emptyList(), annotations);
    }

    @Test
    public void testFindAllAnnotationsWithTypeElement() {
        List<AnnotationMirror> annotations = findAllAnnotations(testTypeElement, alwaysTrue());
        assertEquals(3, annotations.size());

        annotations = findAllAnnotations(testTypeElement, alwaysFalse());
        assertSame(emptyList(), annotations);
    }

    @Test
    public void testFindAllAnnotationsWithMethod() {
        ExecutableElement method = findMethod(testTypeElement, "echo", String.class);

        List<AnnotationMirror> annotations = findAllAnnotations(method, alwaysTrue());
        assertEquals(1, annotations.size());
        assertTrue(matchesAnnotationType(annotations.get(0), Cacheable.class));

        method = findMethod(getTypeElement(TestService.class), "echo", String.class);

        annotations = findAllAnnotations(method);
        assertEquals(1, annotations.size());
        assertTrue(matchesAnnotationType(annotations.get(0), GET.class));
    }

    @Test
    public void testFindAllAnnotationsWithMethodParameters() {
        ExecutableElement method = findMethod(getTypeElement(TestService.class), "echo", String.class);
        List<? extends VariableElement> parameters = method.getParameters();
        assertEquals(1, parameters.size());

        List<AnnotationMirror> annotations = findAllAnnotations(parameters.get(0), alwaysTrue());
        assertEquals(2, annotations.size());
        assertTrue(matchesAnnotationType(annotations.get(0), PathParam.class));
        assertTrue(matchesAnnotationType(annotations.get(1), DefaultValue.class));

        method = findMethod(getTypeElement(TestService.class), "model", Model.class);
        parameters = method.getParameters();
        assertEquals(1, parameters.size());

        annotations = findAllAnnotations(parameters.get(0));
        assertEquals(1, annotations.size());
        assertTrue(matchesAnnotationType(annotations.get(0), PathParam.class));
    }

    @Test
    public void testFindAllAnnotationsWithField() {
        VariableElement field = findField(testTypeElement, "context");

        List<AnnotationMirror> annotations = findAllAnnotations(field, alwaysTrue());
        assertEquals(1, annotations.size());
        assertTrue(matchesAnnotationType(annotations.get(0), Autowired.class));

        field = findField(testTypeElement, "environment");
        annotations = findAllAnnotations(field, alwaysTrue());
        assertSame(emptyList(), annotations);
    }

    @Test
    public void testFindAllAnnotationsWithElementOnNull() {
        assertSame(emptyList(), findAllAnnotations(NULL_ELEMENT, alwaysTrue()));
    }

    @Test
    public void testGetAttribute() {
        assertEquals("testService", getAttribute(findAnnotation(testTypeElement, Service.class), "value"));
        assertEquals("testService", getAttribute(findAnnotation(testTypeElement, Service.class).getElementValues(), "value"));
        assertEquals("/echo", getAttribute(findAnnotation(testTypeElement, Path.class), "value"));

        assertNull(getAttribute(findAnnotation(testTypeElement, Path.class), NULL_STRING));
        assertNull(getAttribute(findAnnotation(testTypeElement, NULL_CLASS), NULL_STRING));

        ExecutableElement echoMethod = findMethod(testTypeElement, "echo", String.class);
        AnnotationMirror cacheableAnnotation = findAnnotation(echoMethod, Cacheable.class);
        String[] cacheNames = getAttribute(cacheableAnnotation, "cacheNames");
        assertArrayEquals(ofArray("cache-1", "cache-2"), cacheNames);

        DeclaredType cacheableAnnotationType = cacheableAnnotation.getAnnotationType();
        AnnotationMirror targetAnnotation = findAnnotation(cacheableAnnotationType, Target.class);
        ElementType[] elementTypes = getAttribute(targetAnnotation, "value");
        assertArrayEquals(ofArray(TYPE, METHOD), elementTypes);

    }

    @Test
    public void testGetValue() {
        AnnotationMirror pathAnnotation = getAnnotation(getTypeElement(TestService.class), Path.class);
        assertEquals("/echo", getValue(pathAnnotation));
    }

    @Test
    public void testIsAnnotationPresent() {
        assertTrue(isAnnotationPresent(testTypeElement, "org.springframework.stereotype.Service"));
        assertTrue(isAnnotationPresent(testTypeElement, "javax.xml.ws.ServiceMode"));
        assertTrue(isAnnotationPresent(testTypeElement, "javax.ws.rs.Path"));
    }

    private void assertFindAnnotation(Class<? extends Annotation> annotationType) {
        assertTrue(matchesAnnotationType(findAnnotation(testTypeMirror, annotationType), annotationType));
        assertTrue(matchesAnnotationType(findAnnotation(testTypeElement, annotationType), annotationType));
        assertTrue(matchesAnnotationType(findAnnotation(testTypeMirror, annotationType.getName()), annotationType));
        assertTrue(matchesAnnotationType(findAnnotation(testTypeElement, annotationType.getName()), annotationType));
    }


    private void asserGetAnnotation(Class<? extends Annotation> annotationClass) {
        AnnotationMirror serviceAnnotation = getAnnotation(testTypeElement, annotationClass);
        assertEquals(annotationClass.getName(), serviceAnnotation.getAnnotationType().toString());
    }

    private void asserGetAnnotation(String annotationClassName) {
        AnnotationMirror serviceAnnotation = getAnnotation(testTypeElement, annotationClassName);
        assertEquals(annotationClassName, serviceAnnotation.getAnnotationType().toString());
    }

    private void assertGetAnnotations(Class<? extends Annotation> annotationType) {
        List<AnnotationMirror> annotations = getAnnotations(testTypeElement, annotationType);
        assertEquals(1, annotations.size());
        assertTrue(matchesAnnotationType(annotations.get(0), annotationType));
        assertEquals(annotationType.getName(), annotations.get(0).getAnnotationType().toString());
    }

    private void assertGetAnnotations(String annotationClassName) {
        List<AnnotationMirror> annotations = getAnnotations(testTypeElement, annotationClassName);
        assertEquals(1, annotations.size());
        assertEquals(annotationClassName, annotations.get(0).getAnnotationType().toString());
    }
}
