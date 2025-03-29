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
import javax.lang.model.element.TypeElement;
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

import static io.microsphere.annotation.processor.util.TypeUtils.getAllTypeElements;
import static io.microsphere.annotation.processor.util.TypeUtils.getTypeElement;
import static io.microsphere.annotation.processor.util.TypeUtils.isSameType;
import static io.microsphere.annotation.processor.util.TypeUtils.ofTypeElement;
import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.collection.CollectionUtils.size;
import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static io.microsphere.lang.function.Streams.filterAll;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static java.lang.Enum.valueOf;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * The utilities class for annotation in the package "javax.lang.model.*"
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface AnnotationUtils {

    static AnnotationMirror getAnnotation(AnnotatedConstruct annotatedConstruct, Class<? extends Annotation> annotationClass) {
        if (annotatedConstruct == null || annotationClass == null) {
            return null;
        }
        return getAnnotation(annotatedConstruct, annotationClass.getName());
    }

    static AnnotationMirror getAnnotation(AnnotatedConstruct annotatedConstruct, CharSequence annotationClassName) {
        if (annotatedConstruct == null || annotationClassName == null) {
            return null;
        }
        List<AnnotationMirror> annotations = getAnnotations(annotatedConstruct, annotationClassName);
        return annotations.isEmpty() ? null : annotations.get(0);
    }

    static List<AnnotationMirror> getAnnotations(AnnotatedConstruct annotatedConstruct) {
        if (annotatedConstruct == null) {
            return emptyList();
        }
        return findAnnotations(annotatedConstruct, EMPTY_PREDICATE_ARRAY);
    }

    static List<AnnotationMirror> getAnnotations(AnnotatedConstruct annotatedConstruct, Class<? extends Annotation> annotationClass) {
        if (annotatedConstruct == null || annotationClass == null) {
            return emptyList();
        }
        return getAnnotations(annotatedConstruct, annotationClass.getTypeName());
    }

    static List<AnnotationMirror> getAnnotations(AnnotatedConstruct annotatedConstruct, CharSequence annotationClassName) {
        if (annotatedConstruct == null || annotationClassName == null) {
            return emptyList();
        }
        return findAnnotations(annotatedConstruct, annotation -> matchesAnnotationClassName(annotation, annotationClassName));
    }

    static List<AnnotationMirror> getAllAnnotations(TypeMirror type) {
        if (type == null) {
            return emptyList();
        }
        return getAllAnnotations(ofTypeElement(type));
    }

    static List<AnnotationMirror> getAllAnnotations(Element element) {
        if (element == null) {
            return emptyList();
        }
        return findAllAnnotations(element, EMPTY_PREDICATE_ARRAY);
    }

    static List<AnnotationMirror> getAllAnnotations(TypeMirror type, Class<? extends Annotation> annotationClass) {
        if (type == null || annotationClass == null) {
            return emptyList();
        }
        return getAllAnnotations(ofTypeElement(type), annotationClass);
    }

    static List<AnnotationMirror> getAllAnnotations(Element element, Class<? extends Annotation> annotationClass) {
        if (element == null || annotationClass == null) {
            return emptyList();
        }
        return getAllAnnotations(element, annotationClass.getTypeName());
    }

    static List<AnnotationMirror> getAllAnnotations(TypeMirror type, CharSequence annotationClassName) {
        if (type == null || annotationClassName == null) {
            return emptyList();
        }
        return getAllAnnotations(ofTypeElement(type), annotationClassName);
    }

    static List<AnnotationMirror> getAllAnnotations(Element element, CharSequence annotationClassName) {
        if (element == null || annotationClassName == null) {
            return emptyList();
        }
        return findAllAnnotations(element, annotation -> matchesAnnotationClassName(annotation, annotationClassName));
    }

    static List<AnnotationMirror> getAllAnnotations(ProcessingEnvironment processingEnv, Type annotatedType) {
        if (processingEnv == null || annotatedType == null) {
            return emptyList();
        }
        return findAllAnnotations(processingEnv, annotatedType, EMPTY_PREDICATE_ARRAY);
    }

    static AnnotationMirror findAnnotation(TypeMirror type, Class<? extends Annotation> annotationClass) {
        if (type == null || annotationClass == null) {
            return null;
        }
        return findAnnotation(type, annotationClass.getTypeName());
    }

    static AnnotationMirror findAnnotation(TypeMirror type, CharSequence annotationClassName) {
        if (type == null || annotationClassName == null) {
            return null;
        }
        return findAnnotation(ofTypeElement(type), annotationClassName);
    }

    static AnnotationMirror findAnnotation(Element element, Class<? extends Annotation> annotationClass) {
        if (element == null || annotationClass == null) {
            return null;
        }
        return findAnnotation(element, annotationClass.getTypeName());
    }

    static AnnotationMirror findAnnotation(Element element, CharSequence annotationClassName) {
        if (element == null || annotationClassName == null) {
            return null;
        }
        List<AnnotationMirror> annotations = findAllAnnotations(element, annotation -> matchesAnnotationClassName(annotation, annotationClassName));
        return isEmpty(annotations) ? null : annotations.get(0);
    }

    static AnnotationMirror findMetaAnnotation(Element annotatedConstruct, Class<? extends Annotation> metaAnnotationClass) {
        if (annotatedConstruct == null || metaAnnotationClass == null) {
            return null;
        }
        return findMetaAnnotation(annotatedConstruct, metaAnnotationClass.getName());
    }

    static AnnotationMirror findMetaAnnotation(Element annotatedConstruct, CharSequence metaAnnotationClassName) {
        if (annotatedConstruct == null || metaAnnotationClassName == null) {
            return null;
        }

        AnnotationMirror metaAnnotation = null;

        List<AnnotationMirror> annotations = getAllAnnotations(annotatedConstruct);
        int size = size(annotations);

        for (int i = 0; i < size; i++) {
            AnnotationMirror annotation = annotations.get(i);
            if ((metaAnnotation = findAnnotation(annotation.getAnnotationType(), metaAnnotationClassName)) != null) {
                break;
            }
        }

        return metaAnnotation;
    }

    static boolean isAnnotationPresent(Element element, Class<? extends Annotation> annotationClass) {
        if (element == null || annotationClass == null) {
            return false;
        }
        return findAnnotation(element, annotationClass) != null || findMetaAnnotation(element, annotationClass) != null;
    }

    static boolean isAnnotationPresent(Element element, CharSequence annotationClassName) {
        if (element == null || annotationClassName == null) {
            return false;
        }
        return findAnnotation(element, annotationClassName) != null || findMetaAnnotation(element, annotationClassName) != null;
    }

    static List<AnnotationMirror> findAnnotations(AnnotatedConstruct annotatedConstruct, Predicate<? super AnnotationMirror>... annotationFilters) {
        if (annotatedConstruct == null) {
            return emptyList();
        }

        List<AnnotationMirror> annotations = (List) annotatedConstruct.getAnnotationMirrors();
        if (isEmpty(annotations)) {
            return emptyList();
        }

        if (isNotEmpty(annotationFilters)) {
            annotations = filterAll(annotations, annotationFilters);
        }

        return annotations.isEmpty() ? emptyList() : annotations;
    }

    static List<AnnotationMirror> findAllAnnotations(TypeMirror type, Predicate<? super AnnotationMirror>... annotationFilters) {
        if (type == null) {
            return emptyList();
        }
        return findAllAnnotations(ofTypeElement(type), annotationFilters);
    }

    static List<AnnotationMirror> findAllAnnotations(TypeElement element, Predicate<? super AnnotationMirror>... annotationFilters) {
        if (element == null) {
            return emptyList();
        }
        List<TypeElement> typeElements = getAllTypeElements(element);

        List<AnnotationMirror> annotations = typeElements.stream()
                .map(AnnotationUtils::getAnnotations)
                .flatMap(Collection::stream)
                .collect(toList());

        if (isNotEmpty(annotationFilters)) {
            annotations = filterAll(annotations, annotationFilters);
        }

        return isEmpty(annotations) ? emptyList() : annotations;
    }

    static List<AnnotationMirror> findAllAnnotations(Element element, Predicate<? super AnnotationMirror>... annotationFilters) {
        if (element == null) {
            return emptyList();
        }

        TypeElement typeElement = ofTypeElement(element);

        if (typeElement == null) {
            return findAnnotations(element, annotationFilters);
        }

        return findAllAnnotations(typeElement, annotationFilters);
    }

    static List<AnnotationMirror> findAllAnnotations(ProcessingEnvironment processingEnv, Type annotatedType, Predicate<? super AnnotationMirror>... annotationFilters) {
        if (processingEnv == null || annotatedType == null) {
            return emptyList();
        }
        return findAllAnnotations(processingEnv, annotatedType.getTypeName(), annotationFilters);
    }

    static List<AnnotationMirror> findAllAnnotations(ProcessingEnvironment processingEnv, CharSequence annotatedTypeName, Predicate<? super AnnotationMirror>... annotationFilters) {
        if (processingEnv == null || annotatedTypeName == null) {
            return emptyList();
        }
        return findAllAnnotations(getTypeElement(processingEnv, annotatedTypeName), annotationFilters);
    }

    static boolean matchesAnnotationClass(AnnotationMirror annotationMirror, Type annotationType) {
        if (annotationMirror == null || annotationType == null) {
            return false;
        }
        return matchesAnnotationClassName(annotationMirror, annotationType.getTypeName());
    }

    static boolean matchesAnnotationClassName(AnnotationMirror annotationMirror, CharSequence annotationClassName) {
        if (annotationMirror == null || annotationClassName == null) {
            return false;
        }
        return isSameType(annotationMirror.getAnnotationType(), annotationClassName);
    }

    static <T> T getAttribute(AnnotationMirror annotation, String attributeName) {
        return annotation == null ? null : getAttribute(annotation.getElementValues(), attributeName);
    }

    static <T> T getAttribute(Map<? extends ExecutableElement, ? extends AnnotationValue> attributesMap, String attributeName) {
        T annotationValue = null;
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : attributesMap.entrySet()) {
            ExecutableElement attributeMethod = entry.getKey();
            if (Objects.equals(attributeName, attributeMethod.getSimpleName().toString())) {
                TypeMirror attributeType = attributeMethod.getReturnType();
                AnnotationValue value = entry.getValue();
                if (attributeType instanceof ArrayType) { // array-typed attribute values
                    ArrayType arrayType = (ArrayType) attributeType;
                    String componentTypeName = arrayType.getComponentType().toString();
                    ClassLoader classLoader = AnnotationUtils.class.getClassLoader();
                    List<AnnotationValue> values = (List<AnnotationValue>) value.getValue();
                    int size = values.size();
                    Class componentClass = resolveClass(componentTypeName, classLoader);
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
                } else {
                    annotationValue = (T) value.getValue();
                }
                break;
            }
        }
        return annotationValue;
    }

    static <T> T getValue(AnnotationMirror annotation) {
        return getAttribute(annotation, "value");
    }
}
