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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static io.microsphere.lang.function.Streams.filterAll;
import static io.microsphere.lang.function.Streams.filterFirst;
import static java.lang.Enum.valueOf;
import static java.util.Collections.emptyList;

/**
 * The utilities class for annotation in the package "javax.lang.model.*"
 *
 * @since 1.0.0
 */
public abstract class AnnotationUtils {

    public static AnnotationMirror getAnnotation(AnnotatedConstruct annotatedConstruct, Class<? extends Annotation> annotationClass) {
        return annotationClass == null ? null : getAnnotation(annotatedConstruct, annotationClass.getTypeName());
    }

    public static AnnotationMirror getAnnotation(AnnotatedConstruct annotatedConstruct, CharSequence annotationClassName) {
        List<AnnotationMirror> annotations = getAnnotations(annotatedConstruct, annotationClassName);
        return annotations.isEmpty() ? null : annotations.get(0);
    }

    public static List<AnnotationMirror> getAnnotations(AnnotatedConstruct annotatedConstruct, Class<? extends Annotation> annotationClass) {
        return annotationClass == null ? emptyList() : getAnnotations(annotatedConstruct, annotationClass.getTypeName());
    }

    public static List<AnnotationMirror> getAnnotations(AnnotatedConstruct annotatedConstruct, CharSequence annotationClassName) {
        return getAnnotations(annotatedConstruct, annotation -> TypeUtils.isSameType(annotation.getAnnotationType(), annotationClassName));
    }

    public static List<AnnotationMirror> getAnnotations(AnnotatedConstruct annotatedConstruct) {
        return getAnnotations(annotatedConstruct, EMPTY_PREDICATE_ARRAY);
    }

    public static List<AnnotationMirror> getAnnotations(AnnotatedConstruct annotatedConstruct, Predicate<AnnotationMirror>... annotationFilters) {

        AnnotatedConstruct actualAnnotatedConstruct = annotatedConstruct;

        if (annotatedConstruct instanceof TypeMirror) {
            actualAnnotatedConstruct = TypeUtils.ofTypeElement((TypeMirror) actualAnnotatedConstruct);
        }

        return actualAnnotatedConstruct == null ? emptyList() : filterAll((List<AnnotationMirror>) actualAnnotatedConstruct.getAnnotationMirrors(), annotationFilters);
    }

    public static List<AnnotationMirror> getAllAnnotations(TypeMirror type) {
        return getAllAnnotations(TypeUtils.ofTypeElement(type));
    }

    public static List<AnnotationMirror> getAllAnnotations(Element element) {
        return getAllAnnotations(element, EMPTY_PREDICATE_ARRAY);
    }

    public static List<AnnotationMirror> getAllAnnotations(TypeMirror type, Class<? extends Annotation> annotationClass) {
        return getAllAnnotations(TypeUtils.ofTypeElement(type), annotationClass);
    }

    public static List<AnnotationMirror> getAllAnnotations(Element element, Class<? extends Annotation> annotationClass) {
        return element == null || annotationClass == null ? emptyList() : getAllAnnotations(element, annotationClass.getTypeName());
    }

    public static List<AnnotationMirror> getAllAnnotations(TypeMirror type, CharSequence annotationClassName) {
        return getAllAnnotations(TypeUtils.ofTypeElement(type), annotationClassName);
    }

    public static List<AnnotationMirror> getAllAnnotations(Element element, CharSequence annotationClassName) {
        return getAllAnnotations(element, annotation -> TypeUtils.isSameType(annotation.getAnnotationType(), annotationClassName));
    }

    public static List<AnnotationMirror> getAllAnnotations(TypeMirror type, Predicate<AnnotationMirror>... annotationFilters) {
        return getAllAnnotations(TypeUtils.ofTypeElement(type), annotationFilters);
    }

    public static List<AnnotationMirror> getAllAnnotations(Element element, Predicate<AnnotationMirror>... annotationFilters) {

        List<AnnotationMirror> allAnnotations = TypeUtils.isTypeElement(element) ? TypeUtils.getHierarchicalTypes(TypeUtils.ofTypeElement(element)).stream().map(AnnotationUtils::getAnnotations).flatMap(Collection::stream).collect(Collectors.toList()) : element == null ? emptyList() : (List<AnnotationMirror>) element.getAnnotationMirrors();

        return filterAll(allAnnotations, annotationFilters);
    }

    public static List<AnnotationMirror> getAllAnnotations(ProcessingEnvironment processingEnv, Type annotatedType) {
        return getAllAnnotations(processingEnv, annotatedType, EMPTY_PREDICATE_ARRAY);
    }

    public static List<AnnotationMirror> getAllAnnotations(ProcessingEnvironment processingEnv, Type annotatedType, Predicate<AnnotationMirror>... annotationFilters) {
        return annotatedType == null ? emptyList() : getAllAnnotations(processingEnv, annotatedType.getTypeName(), annotationFilters);
    }

    public static List<AnnotationMirror> getAllAnnotations(ProcessingEnvironment processingEnv, CharSequence annotatedTypeName, Predicate<AnnotationMirror>... annotationFilters) {
        return getAllAnnotations(TypeUtils.getType(processingEnv, annotatedTypeName), annotationFilters);
    }

    public static AnnotationMirror findAnnotation(TypeMirror type, Class<? extends Annotation> annotationClass) {
        return annotationClass == null ? null : findAnnotation(type, annotationClass.getTypeName());
    }

    public static AnnotationMirror findAnnotation(TypeMirror type, CharSequence annotationClassName) {
        return findAnnotation(TypeUtils.ofTypeElement(type), annotationClassName);
    }

    public static AnnotationMirror findAnnotation(Element element, Class<? extends Annotation> annotationClass) {
        return annotationClass == null ? null : findAnnotation(element, annotationClass.getTypeName());
    }

    public static AnnotationMirror findAnnotation(Element element, CharSequence annotationClassName) {
        return filterFirst(getAllAnnotations(element, annotation -> TypeUtils.isSameType(annotation.getAnnotationType(), annotationClassName)));
    }

    public static AnnotationMirror findMetaAnnotation(Element annotatedConstruct, CharSequence metaAnnotationClassName) {
        return annotatedConstruct == null ? null : getAnnotations(annotatedConstruct).stream().map(annotation -> findAnnotation(annotation.getAnnotationType(), metaAnnotationClassName)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public static boolean isAnnotationPresent(Element element, CharSequence annotationClassName) {
        return findAnnotation(element, annotationClassName) != null || findMetaAnnotation(element, annotationClassName) != null;
    }

    public static <T> T getAttribute(AnnotationMirror annotation, String attributeName) {
        return annotation == null ? null : getAttribute(annotation.getElementValues(), attributeName);
    }

    public static <T> T getAttribute(Map<? extends ExecutableElement, ? extends AnnotationValue> attributesMap, String attributeName) {
        T annotationValue = null;
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : attributesMap.entrySet()) {
            ExecutableElement attributeMethod = entry.getKey();
            if (Objects.equals(attributeName, attributeMethod.getSimpleName().toString())) {
                TypeMirror attributeType = attributeMethod.getReturnType();
                AnnotationValue value = entry.getValue();
                if (attributeType instanceof ArrayType) { // array-typed attribute values
                    ArrayType arrayType = (ArrayType) attributeType;
                    String componentType = arrayType.getComponentType().toString();
                    ClassLoader classLoader = AnnotationUtils.class.getClassLoader();
                    List<AnnotationValue> values = (List<AnnotationValue>) value.getValue();
                    int size = values.size();
                    try {
                        Class componentClass = classLoader.loadClass(componentType);
                        boolean isEnum = componentClass.isEnum();
                        Object array = Array.newInstance(componentClass, values.size());
                        for (int i = 0; i < size; i++) {
                            Object element = values.get(i).getValue();
                            if (isEnum) {
                                element = valueOf(componentClass, element.toString());
                            }
                            Array.set(array, i, element);
                        }
                        annotationValue = (T) array;
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    annotationValue = (T) value.getValue();
                }
                break;
            }
        }
        return annotationValue;
    }

    public static <T> T getValue(AnnotationMirror annotation) {
        return (T) getAttribute(annotation, "value");
    }
}
