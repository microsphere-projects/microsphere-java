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
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.ws.rs.Path;
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
import static io.microsphere.annotation.processor.util.MethodUtils.findMethod;
import static io.microsphere.annotation.processor.util.MethodUtils.getAllDeclaredMethods;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        assertNull(getAnnotation(testTypeElement, (Class) null));
        assertNull(getAnnotation(testTypeElement.asType(), (Class) null));
        assertNull(getAnnotation(null, (Class) null));
        assertNull(getAnnotation(null, (Class) null));
    }

    @Test
    public void testGetAnnotationWithClassNameOnNull() {
        assertNull(getAnnotation(testTypeElement, (String) null));
        assertNull(getAnnotation(testTypeElement.asType(), (String) null));
        assertNull(getAnnotation(null, (String) null));
        assertNull(getAnnotation(null, (String) null));
    }

    @Test
    public void testGetAnnotations() {
        List<AnnotationMirror> annotations = getAnnotations(testTypeElement);
        assertEquals(2, annotations.size());
        assertEquals(Service.class.getName(), annotations.get(0).getAnnotationType().toString());
        assertEquals(ServiceMode.class.getName(), annotations.get(1).getAnnotationType().toString());
    }

    @Test
    public void testGetAnnotationsWithAnnotationType() {
        assertGetAnnotations(Service.class);
        assertGetAnnotations(ServiceMode.class);
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
    public void testGetAnnotationsOnNull() {
        assertTrue(getAnnotations(null, (Class) null).isEmpty());
        assertTrue(getAnnotations(testTypeElement, (Class) null).isEmpty());
        assertTrue(getAnnotations(null, Service.class).isEmpty());
    }

    @Test
    public void testGetAnnotationsWithAnnotationClassNameOnNull() {
        assertTrue(getAnnotations(null, (String) null).isEmpty());
        assertTrue(getAnnotations(testTypeElement, (String) null).isEmpty());
        assertTrue(getAnnotations(null, Service.class.getName()).isEmpty());
    }

    @Test
    public void testGetAllAnnotations() {

        List<AnnotationMirror> annotations = getAllAnnotations(testTypeElement);
        assertEquals(3, annotations.size());

        annotations = findAllAnnotations(testTypeElement.asType(), annotation -> true);
        assertEquals(3, annotations.size());

        annotations = getAllAnnotations(processingEnv, TestServiceImpl.class);
        assertEquals(3, annotations.size());

        annotations = getAllAnnotations(testTypeElement.asType(), Service.class);
        assertEquals(1, annotations.size());

        annotations = getAllAnnotations(testTypeElement, Override.class);
        assertEquals(0, annotations.size());

        assertTrue(getAllAnnotations((Element) null, (Class) null).isEmpty());
        assertTrue(getAllAnnotations((TypeMirror) null, (String) null).isEmpty());
        assertTrue(getAllAnnotations((ProcessingEnvironment) null, (Class) null).isEmpty());
        assertTrue(findAllAnnotations((ProcessingEnvironment) null, (String) null).isEmpty());

        assertTrue(getAllAnnotations((Element) null).isEmpty());
        assertTrue(getAllAnnotations((TypeMirror) null).isEmpty());
        assertTrue(getAllAnnotations(processingEnv, (Class) null).isEmpty());
        assertTrue(findAllAnnotations(processingEnv, (String) null).isEmpty());


        assertTrue(getAllAnnotations(testTypeElement, (Class) null).isEmpty());
        assertTrue(getAllAnnotations(testTypeElement.asType(), (Class) null).isEmpty());

        assertTrue(getAllAnnotations(testTypeElement, (String) null).isEmpty());
        assertTrue(getAllAnnotations(testTypeElement.asType(), (String) null).isEmpty());

        assertTrue(getAllAnnotations((Element) null, Service.class).isEmpty());
        assertTrue(getAllAnnotations((TypeMirror) null, Service.class.getTypeName()).isEmpty());
    }


    @Test
    public void testFindAnnotation() {

        assertEquals("org.springframework.stereotype.Service", findAnnotation(testTypeElement, Service.class).getAnnotationType().toString());
        assertEquals("javax.ws.rs.Path", findAnnotation(testTypeElement, Path.class).getAnnotationType().toString());
        assertEquals("javax.ws.rs.Path", findAnnotation(testTypeElement.asType(), Path.class).getAnnotationType().toString());
        assertEquals("javax.ws.rs.Path", findAnnotation(testTypeElement.asType(), Path.class.getTypeName()).getAnnotationType().toString());
        assertNull(findAnnotation(testTypeElement, Override.class));

        assertNull(findAnnotation((Element) null, (Class) null));
        assertNull(findAnnotation((Element) null, (String) null));
        assertNull(findAnnotation((TypeMirror) null, (Class) null));
        assertNull(findAnnotation((TypeMirror) null, (String) null));

        assertNull(findAnnotation(testTypeElement, (Class) null));
        assertNull(findAnnotation(testTypeElement, (String) null));
        assertNull(findAnnotation(testTypeElement.asType(), (Class) null));
        assertNull(findAnnotation(testTypeElement.asType(), (String) null));
    }

    @Test
    public void testFindMetaAnnotation() {
        getAllDeclaredMethods(getTypeElement(TestService.class)).forEach(method -> {
            assertEquals("javax.ws.rs.HttpMethod", findMetaAnnotation(method, "javax.ws.rs.HttpMethod").getAnnotationType().toString());
        });
    }

    @Test
    public void testFindMetaAnnotationOnNull() {
        assertNull(findMetaAnnotation(null, null));
        assertNull(findMetaAnnotation(null, "test"));
    }

    @Test
    public void testGetAttribute() {
        assertEquals("testService", getAttribute(findAnnotation(testTypeElement, Service.class), "value"));
        assertEquals("testService", getAttribute(findAnnotation(testTypeElement, Service.class).getElementValues(), "value"));
        assertEquals("/echo", getAttribute(findAnnotation(testTypeElement, Path.class), "value"));

        assertNull(getAttribute(findAnnotation(testTypeElement, Path.class), null));
        assertNull(getAttribute(findAnnotation(testTypeElement, (Class) null), null));

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
        assertEquals(annotationType.getName(), annotations.get(0).getAnnotationType().toString());
    }

    private void assertGetAnnotations(String annotationClassName) {
        List<AnnotationMirror> annotations = getAnnotations(testTypeElement, annotationClassName);
        assertEquals(1, annotations.size());
        assertEquals(annotationClassName, annotations.get(0).getAnnotationType().toString());
    }
}
