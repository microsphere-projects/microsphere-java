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
import org.springframework.stereotype.Service;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.ws.rs.Path;
import java.util.Iterator;
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
import static io.microsphere.annotation.processor.util.MethodUtils.getAllDeclaredMethods;
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

    private TypeElement testType;

    @Override
    protected void beforeTest() {
        testType = getType(TestServiceImpl.class);
    }

    @Test
    public void testGetAnnotation() {
        AnnotationMirror serviceAnnotation = getAnnotation(testType, Service.class);
        assertEquals("testService", getAttribute(serviceAnnotation, "value"));

        assertNull(getAnnotation(testType, (Class) null));
        assertNull(getAnnotation(testType, (String) null));

        assertNull(getAnnotation(testType.asType(), (Class) null));
        assertNull(getAnnotation(testType.asType(), (String) null));

        assertNull(getAnnotation((Element) null, (Class) null));
        assertNull(getAnnotation((Element) null, (String) null));

        assertNull(getAnnotation((TypeElement) null, (Class) null));
        assertNull(getAnnotation((TypeElement) null, (String) null));
    }

    @Test
    public void testGetAnnotations() {
        List<AnnotationMirror> annotations = getAnnotations(testType);
        Iterator<AnnotationMirror> iterator = annotations.iterator();

        assertEquals(2, annotations.size());
        assertEquals("org.springframework.stereotype.Service", iterator.next().getAnnotationType().toString());
        assertEquals("javax.xml.ws.ServiceMode", iterator.next().getAnnotationType().toString());

        annotations = getAnnotations(testType, Service.class);
        iterator = annotations.iterator();
        assertEquals(1, annotations.size());
        assertEquals("org.springframework.stereotype.Service", iterator.next().getAnnotationType().toString());

        annotations = getAnnotations(testType.asType(), Service.class);
        iterator = annotations.iterator();
        assertEquals(1, annotations.size());
        assertEquals("org.springframework.stereotype.Service", iterator.next().getAnnotationType().toString());

        annotations = getAnnotations(testType.asType(), Service.class.getTypeName());
        iterator = annotations.iterator();
        assertEquals(1, annotations.size());
        assertEquals("org.springframework.stereotype.Service", iterator.next().getAnnotationType().toString());

        annotations = getAnnotations(testType, Override.class);
        assertEquals(0, annotations.size());

        annotations = getAnnotations(testType, Service.class);
        assertEquals(1, annotations.size());

        assertTrue(getAnnotations(null, (Class) null).isEmpty());
        assertTrue(getAnnotations(null, (String) null).isEmpty());
        assertTrue(getAnnotations(testType, (Class) null).isEmpty());
        assertTrue(getAnnotations(testType, (String) null).isEmpty());

        assertTrue(getAnnotations(null, Service.class).isEmpty());
        assertTrue(getAnnotations(null, Service.class.getTypeName()).isEmpty());
    }

    @Test
    public void testFindAllAnnotations() {

        List<AnnotationMirror> annotations = getAllAnnotations(testType);
        assertEquals(3, annotations.size());

        annotations = findAllAnnotations(testType.asType(), annotation -> true);
        assertEquals(3, annotations.size());

        annotations = getAllAnnotations(processingEnv, TestServiceImpl.class);
        assertEquals(3, annotations.size());

        annotations = getAllAnnotations(testType.asType(), Service.class);
        assertEquals(1, annotations.size());

        annotations = getAllAnnotations(testType, Override.class);
        assertEquals(0, annotations.size());

        assertTrue(getAllAnnotations((Element) null, (Class) null).isEmpty());
        assertTrue(getAllAnnotations((TypeMirror) null, (String) null).isEmpty());
        assertTrue(getAllAnnotations((ProcessingEnvironment) null, (Class) null).isEmpty());
        assertTrue(findAllAnnotations((ProcessingEnvironment) null, (String) null).isEmpty());

        assertTrue(getAllAnnotations((Element) null).isEmpty());
        assertTrue(getAllAnnotations((TypeMirror) null).isEmpty());
        assertTrue(getAllAnnotations(processingEnv, (Class) null).isEmpty());
        assertTrue(findAllAnnotations(processingEnv, (String) null).isEmpty());


        assertTrue(getAllAnnotations(testType, (Class) null).isEmpty());
        assertTrue(getAllAnnotations(testType.asType(), (Class) null).isEmpty());

        assertTrue(getAllAnnotations(testType, (String) null).isEmpty());
        assertTrue(getAllAnnotations(testType.asType(), (String) null).isEmpty());

        assertTrue(getAllAnnotations((Element) null, Service.class).isEmpty());
        assertTrue(getAllAnnotations((TypeMirror) null, Service.class.getTypeName()).isEmpty());
    }


    @Test
    public void testFindAnnotation() {

        assertEquals("org.springframework.stereotype.Service", findAnnotation(testType, Service.class).getAnnotationType().toString());
        assertEquals("javax.ws.rs.Path", findAnnotation(testType, Path.class).getAnnotationType().toString());
        assertEquals("javax.ws.rs.Path", findAnnotation(testType.asType(), Path.class).getAnnotationType().toString());
        assertEquals("javax.ws.rs.Path", findAnnotation(testType.asType(), Path.class.getTypeName()).getAnnotationType().toString());
        assertNull(findAnnotation(testType, Override.class));

        assertNull(findAnnotation((Element) null, (Class) null));
        assertNull(findAnnotation((Element) null, (String) null));
        assertNull(findAnnotation((TypeMirror) null, (Class) null));
        assertNull(findAnnotation((TypeMirror) null, (String) null));

        assertNull(findAnnotation(testType, (Class) null));
        assertNull(findAnnotation(testType, (String) null));
        assertNull(findAnnotation(testType.asType(), (Class) null));
        assertNull(findAnnotation(testType.asType(), (String) null));
    }

    @Test
    public void testFindMetaAnnotation() {
        getAllDeclaredMethods(getType(TestService.class)).forEach(method -> {
            assertEquals("javax.ws.rs.HttpMethod", findMetaAnnotation(method, "javax.ws.rs.HttpMethod").getAnnotationType().toString());
        });
    }

    @Test
    public void testGetAttribute() {
        assertEquals("testService", getAttribute(findAnnotation(testType, Service.class), "value"));
        assertEquals("testService", getAttribute(findAnnotation(testType, Service.class).getElementValues(), "value"));
        assertEquals("/echo", getAttribute(findAnnotation(testType, Path.class), "value"));

        assertNull(getAttribute(findAnnotation(testType, Path.class), null));
        assertNull(getAttribute(findAnnotation(testType, (Class) null), null));

//        ExecutableElement method = findMethod(getType(SpringRestService.class), "param", String.class);
//
//        AnnotationMirror annotation = findAnnotation(method, GetMapping.class);
//
//        assertArrayEquals(new String[]{"/param"}, (String[]) getAttribute(annotation, "value"));
//        assertNull(getAttribute(annotation, "path"));
    }

    @Test
    public void testGetValue() {
        AnnotationMirror pathAnnotation = getAnnotation(getType(TestService.class), Path.class);
        assertEquals("/echo", getValue(pathAnnotation));
    }

    @Test
    public void testIsAnnotationPresent() {
        assertTrue(isAnnotationPresent(testType, "org.springframework.stereotype.Service"));
        assertTrue(isAnnotationPresent(testType, "javax.xml.ws.ServiceMode"));
        assertTrue(isAnnotationPresent(testType, "javax.ws.rs.Path"));
    }
}
