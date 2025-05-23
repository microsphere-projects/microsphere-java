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

import io.microsphere.annotation.processor.model.util.ResolvableAnnotationValueVisitor;
import io.microsphere.util.Utils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;

import static io.microsphere.annotation.processor.util.MethodUtils.findDeclaredMethods;
import static io.microsphere.annotation.processor.util.MethodUtils.getDeclaredMethods;
import static io.microsphere.annotation.processor.util.MethodUtils.getMethodName;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllTypeElements;
import static io.microsphere.annotation.processor.util.TypeUtils.getTypeElement;
import static io.microsphere.annotation.processor.util.TypeUtils.isSameType;
import static io.microsphere.annotation.processor.util.TypeUtils.ofTypeElement;
import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.collection.CollectionUtils.size;
import static io.microsphere.collection.MapUtils.isEmpty;
import static io.microsphere.collection.MapUtils.newFixedLinkedHashMap;
import static io.microsphere.collection.MapUtils.ofEntry;
import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static io.microsphere.lang.function.Streams.filterAll;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static io.microsphere.util.StringUtils.isBlank;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

/**
 * The utilities class for annotation in the package "javax.lang.model.*"
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface AnnotationUtils extends Utils {

    /**
     * The default {@link AnnotationValueVisitor}
     */
    AnnotationValueVisitor<Object, ExecutableElement> DEFAULT_ANNOTATION_VALUE_VISITOR = new ResolvableAnnotationValueVisitor();

    boolean WITH_DEFAULT = true;

    /**
     * Retrieves the first {@link AnnotationMirror} of the specified annotation class from the given
     * {@link AnnotatedConstruct}. If either the construct or the annotation class is {@code null}, 
     * this method returns {@code null}.
     *
     * @param annotatedConstruct the annotated construct to search for annotations, may be {@code null}
     * @param annotationClass    the annotation class to look for, may be {@code null}
     * @return the first matching {@link AnnotationMirror}, or {@code null} if none found
     */
    static AnnotationMirror getAnnotation(AnnotatedConstruct annotatedConstruct, Class<? extends Annotation> annotationClass) {
        if (annotatedConstruct == null || annotationClass == null) {
            return null;
        }
        return getAnnotation(annotatedConstruct, annotationClass.getName());
    }

    /**
     * Retrieves the first {@link AnnotationMirror} of the specified annotation class name from the given
     * {@link AnnotatedConstruct}. If either the construct or the annotation class name is {@code null}, 
     * this method returns {@code null}.
     *
     * @param annotatedConstruct the annotated construct to search for annotations, may be {@code null}
     * @param annotationClassName the fully qualified class name of the annotation to look for, may be {@code null}
     * @return the first matching {@link AnnotationMirror}, or {@code null} if none found
     */
    static AnnotationMirror getAnnotation(AnnotatedConstruct annotatedConstruct, CharSequence annotationClassName) {
        if (annotatedConstruct == null || annotationClassName == null) {
            return null;
        }
        List<AnnotationMirror> annotations = getAnnotations(annotatedConstruct, annotationClassName);
        return annotations.isEmpty() ? null : annotations.get(0);
    }

    /**
     * Retrieves all {@link AnnotationMirror} instances from the given {@link AnnotatedConstruct}.
     * If the annotated construct is {@code null}, this method returns an empty list.
     *
     * @param annotatedConstruct the annotated construct to search for annotations, may be {@code null}
     * @return a non-null immutable list of {@link AnnotationMirror} instances; never {@code null}
     */
    static List<AnnotationMirror> getAnnotations(AnnotatedConstruct annotatedConstruct) {
        if (annotatedConstruct == null) {
            return emptyList();
        }
        return findAnnotations(annotatedConstruct, EMPTY_PREDICATE_ARRAY);
    }


    /**
     * Retrieves all {@link AnnotationMirror} instances of the specified annotation class from the given
     * {@link AnnotatedConstruct}. If either the construct or the annotation class is {@code null}, 
     * this method returns an empty list.
     *
     * @param annotatedConstruct the annotated construct to search for annotations, may be {@code null}
     * @param annotationClass    the annotation class to look for, may be {@code null}
     * @return a non-null immutable list of {@link AnnotationMirror} instances; never {@code null}
     */
    static List<AnnotationMirror> getAnnotations(AnnotatedConstruct annotatedConstruct, Class<? extends Annotation> annotationClass) {
        if (annotatedConstruct == null || annotationClass == null) {
            return emptyList();
        }
        return getAnnotations(annotatedConstruct, annotationClass.getTypeName());
    }

    /**
     * Retrieves all {@link AnnotationMirror} instances of the specified annotation class name from the given
     * {@link AnnotatedConstruct}. If either the construct or the annotation class name is {@code null}, 
     * this method returns an empty list.
     *
     * @param annotatedConstruct the annotated construct to search for annotations, may be {@code null}
     * @param annotationClassName the fully qualified class name of the annotation to look for, may be {@code null}
     * @return a non-null immutable list of {@link AnnotationMirror} instances; never {@code null}
     */
    static List<AnnotationMirror> getAnnotations(AnnotatedConstruct annotatedConstruct, CharSequence annotationClassName) {
        if (annotatedConstruct == null || annotationClassName == null) {
            return emptyList();
        }
        return findAnnotations(annotatedConstruct, annotation -> matchesAnnotationTypeName(annotation, annotationClassName));
    }

    /**
     * Retrieves all {@link AnnotationMirror} instances from the given {@link TypeMirror}.
     * If the type mirror is {@code null}, this method returns an empty list.
     *
     * @param type the type mirror to search for annotations, may be {@code null}
     * @return a non-null immutable list of {@link AnnotationMirror} instances; never {@code null}
     */
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
        return findAllAnnotations(element, annotation -> matchesAnnotationTypeName(annotation, annotationClassName));
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
        List<AnnotationMirror> annotations = findAllAnnotations(element, annotation -> matchesAnnotationTypeName(annotation, annotationClassName));
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

    static boolean matchesAnnotationType(AnnotationMirror annotationMirror, Type annotationType) {
        if (annotationMirror == null || annotationType == null) {
            return false;
        }
        return matchesAnnotationTypeName(annotationMirror, annotationType.getTypeName());
    }

    static boolean matchesAnnotationTypeName(AnnotationMirror annotationMirror, CharSequence annotationTypeName) {
        if (annotationMirror == null || annotationTypeName == null) {
            return false;
        }
        return isSameType(annotationMirror.getAnnotationType(), annotationTypeName);
    }

    static String getAttributeName(ExecutableElement attributeMethod) {
        return getMethodName(attributeMethod);
    }

    static boolean matchesAttributeMethod(ExecutableElement attributeMethod, String attributeName) {
        return attributeMethod != null && Objects.equals(getAttributeName(attributeMethod), attributeName);
    }

    static boolean matchesAttributeValue(AnnotationValue annotationValue, Object attributeValue) {
        return annotationValue != null && Objects.equals(annotationValue.getValue(), attributeValue);
    }

    static Map<String, Object> getAttributesMap(AnnotatedConstruct annotatedConstruct, Class<? extends Annotation> annotationClass) {
        return getAttributesMap(annotatedConstruct, annotationClass, WITH_DEFAULT);
    }

    /**
     * Get the attributes map from the specified annotation
     *
     * @param annotatedConstruct the annotated construct
     * @param annotationClass    the {@link Class class} of {@link Annotation annotation}
     * @param withDefault        with default attribute value
     * @return non-null read-only {@link Map}
     */
    static Map<String, Object> getAttributesMap(AnnotatedConstruct annotatedConstruct, Class<? extends Annotation> annotationClass, boolean withDefault) {
        return getAttributesMap(getAnnotation(annotatedConstruct, annotationClass), withDefault);
    }

    /**
     * Get the attributes map from the specified annotation
     *
     * @param annotation the specified annotation
     * @return non-null read-only {@link Map}
     */
    static Map<String, Object> getAttributesMap(AnnotationMirror annotation) {
        return getAttributesMap(annotation, WITH_DEFAULT);
    }

    /**
     * Get the attributes map from the specified annotation
     *
     * @param annotation  the specified annotation
     * @param withDefault with default attribute value
     * @return non-null read-only {@link Map}
     */
    static Map<String, Object> getAttributesMap(AnnotationMirror annotation, boolean withDefault) {
        Map<ExecutableElement, AnnotationValue> attributes = getElementValues(annotation, withDefault);
        int size = attributes.size();
        if (size < 1) {
            return emptyMap();
        }
        Map<String, Object> attributesMap = newFixedLinkedHashMap(size);
        for (Entry<ExecutableElement, AnnotationValue> entry : attributes.entrySet()) {
            ExecutableElement attributeMethod = entry.getKey();
            String attributeName = getAttributeName(attributeMethod);
            Object attributeValue = getAttribute(entry);
            attributesMap.put(attributeName, attributeValue);
        }
        return attributesMap;
    }

    static Map<ExecutableElement, AnnotationValue> getElementValues(AnnotatedConstruct annotatedConstruct, Class<? extends Annotation> annotationClass) {
        return getElementValues(annotatedConstruct, annotationClass, WITH_DEFAULT);
    }

    static Map<ExecutableElement, AnnotationValue> getElementValues(AnnotatedConstruct annotatedConstruct, Class<? extends Annotation> annotationClass, boolean withDefault) {
        return getElementValues(getAnnotation(annotatedConstruct, annotationClass), withDefault);
    }

    static Map<ExecutableElement, AnnotationValue> getElementValues(AnnotationMirror annotation) {
        return getElementValues(annotation, WITH_DEFAULT);
    }

    static Map<ExecutableElement, AnnotationValue> getElementValues(AnnotationMirror annotation, boolean withDefault) {
        if (annotation == null) {
            return emptyMap();
        }
        DeclaredType annotationType = annotation.getAnnotationType();
        List<ExecutableElement> attributeMethods = getDeclaredMethods(annotationType);
        int size = attributeMethods.size();
        Map<ExecutableElement, AnnotationValue> attributes = newFixedLinkedHashMap(size);
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotation.getElementValues();
        for (int i = 0; i < size; i++) {
            ExecutableElement attributeMethod = attributeMethods.get(i);
            AnnotationValue annotationValue = elementValues.get(attributeMethod);
            if (withDefault && annotationValue == null) {
                annotationValue = attributeMethod.getDefaultValue();
            }
            if (annotationValue != null) {
                attributes.put(attributeMethod, annotationValue);
            }
        }
        return attributes;
    }

    static Entry<ExecutableElement, AnnotationValue> getElementValue(AnnotationMirror annotation, String attributeName, boolean withDefault) {
        if (annotation == null || isBlank(attributeName)) {
            return null;
        }

        ExecutableElement attributeMethod = null;
        AnnotationValue annotationValue = null;
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotation.getElementValues();
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
            attributeMethod = entry.getKey();
            if (matchesAttributeMethod(attributeMethod, attributeName)) {
                annotationValue = entry.getValue();
                break;
            }
        }

        if (withDefault && annotationValue == null) { // not found if the default value is required
            DeclaredType annotationType = annotation.getAnnotationType();
            List<ExecutableElement> attributeMethods = findDeclaredMethods(annotationType, method -> !elementValues.containsKey(method));
            int size = attributeMethods.size();
            for (int i = 0; i < size; i++) {
                attributeMethod = attributeMethods.get(i);
                if (matchesAttributeMethod(attributeMethod, attributeName)) {
                    annotationValue = attributeMethod.getDefaultValue();
                    break;
                }
            }
        }

        return ofEntry(attributeMethod, annotationValue);
    }

    static Entry<ExecutableElement, AnnotationValue> getElementValue(Map<ExecutableElement, AnnotationValue> elementValues, String attributeName) {
        if (isEmpty(elementValues)) {
            return null;
        }
        for (Entry<ExecutableElement, AnnotationValue> entry : elementValues.entrySet()) {
            if (matchesAttributeMethod(entry.getKey(), attributeName)) {
                return entry;
            }
        }
        return null;
    }

    static <T> T getAttribute(AnnotationMirror annotation, String attributeName) {
        return getAttribute(annotation, attributeName, WITH_DEFAULT);
    }

    static <T> T getAttribute(AnnotationMirror annotation, String attributeName, boolean withDefault) {
        Entry<ExecutableElement, AnnotationValue> attributeEntry = getElementValue(annotation, attributeName, withDefault);
        return getAttribute(attributeEntry);
    }

    static <T> T getAttribute(Entry<ExecutableElement, AnnotationValue> elementValue) {
        if (elementValue == null) {
            return null;
        }

        ExecutableElement attributeMethod = elementValue.getKey();
        AnnotationValue annotationValue = elementValue.getValue();

        return (T) annotationValue.accept(DEFAULT_ANNOTATION_VALUE_VISITOR, attributeMethod);
    }

    static <T> T getValue(AnnotationMirror annotation) {
        return getAttribute(annotation, "value");
    }
}
