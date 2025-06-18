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
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
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
     * The name of the attribute method : value()
     */
    String VALUE_ATTRIBUTE_NAME = "value";

    /**
     * The default {@link AnnotationValueVisitor}
     */
    AnnotationValueVisitor<Object, ExecutableElement> DEFAULT_ANNOTATION_VALUE_VISITOR = new ResolvableAnnotationValueVisitor();

    /**
     * The empty {@link ElementType} array
     */
    ElementType[] EMPTY_ELEMENT_TYPE_ARRAY = new ElementType[0];

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
     * @param annotatedConstruct  the annotated construct to search for annotations, may be {@code null}
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
     * @param annotatedConstruct  the annotated construct to search for annotations, may be {@code null}
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

    /**
     * Retrieves all {@link AnnotationMirror} instances from the given {@link Element}.
     * If the element is {@code null}, this method returns an empty list.
     *
     * <p>This method is designed to provide a consistent way of retrieving annotations
     * across different constructs in the annotation processing framework.</p>
     *
     * @param element the annotated element to search for annotations, may be {@code null}
     * @return a non-null immutable list of {@link AnnotationMirror} instances; never {@code null}
     */
    static List<AnnotationMirror> getAllAnnotations(Element element) {
        if (element == null) {
            return emptyList();
        }
        return findAllAnnotations(element, EMPTY_PREDICATE_ARRAY);
    }


    /**
     * Retrieves all {@link AnnotationMirror} instances of the specified annotation class from the given
     * {@link TypeMirror}. If either the type or the annotation class is {@code null},
     * this method returns an empty list.
     *
     * @param type            the type mirror to search for annotations, may be {@code null}
     * @param annotationClass the annotation class to look for, may be {@code null}
     * @return a non-null immutable list of {@link AnnotationMirror} instances; never {@code null}
     */
    static List<AnnotationMirror> getAllAnnotations(TypeMirror type, Class<? extends Annotation> annotationClass) {
        if (type == null || annotationClass == null) {
            return emptyList();
        }
        return getAllAnnotations(ofTypeElement(type), annotationClass);
    }

    /**
     * Retrieves all {@link AnnotationMirror} instances of the specified annotation class from the given
     * {@link Element}. If either the element or the annotation class is {@code null},
     * this method returns an empty list.
     *
     * @param element         the annotated element to search for annotations, may be {@code null}
     * @param annotationClass the annotation class to look for, may be {@code null}
     * @return a non-null immutable list of {@link AnnotationMirror} instances; never {@code null}
     */
    static List<AnnotationMirror> getAllAnnotations(Element element, Class<? extends Annotation> annotationClass) {
        if (element == null || annotationClass == null) {
            return emptyList();
        }
        return getAllAnnotations(element, annotationClass.getTypeName());
    }

    /**
     * Retrieves all {@link AnnotationMirror} instances of the specified annotation class name from the given
     * {@link TypeMirror}. If either the type or the annotation class name is {@code null},
     * this method returns an empty list.
     *
     * @param type                the type mirror to search for annotations, may be {@code null}
     * @param annotationClassName the fully qualified class name of the annotation to look for, may be {@code null}
     * @return a non-null immutable list of {@link AnnotationMirror} instances; never {@code null}
     */
    static List<AnnotationMirror> getAllAnnotations(TypeMirror type, CharSequence annotationClassName) {
        if (type == null || annotationClassName == null) {
            return emptyList();
        }
        return getAllAnnotations(ofTypeElement(type), annotationClassName);
    }

    /**
     * Retrieves all {@link AnnotationMirror} instances of the specified annotation class name from the given
     * {@link Element}. If either the element or the annotation class name is {@code null},
     * this method returns an empty list.
     *
     * @param element             the annotated element to search for annotations, may be {@code null}
     * @param annotationClassName the fully qualified class name of the annotation to look for, may be {@code null}
     * @return a non-null immutable list of {@link AnnotationMirror} instances; never {@code null}
     */
    static List<AnnotationMirror> getAllAnnotations(Element element, CharSequence annotationClassName) {
        if (element == null || annotationClassName == null) {
            return emptyList();
        }
        return findAllAnnotations(element, annotation -> matchesAnnotationTypeName(annotation, annotationClassName));
    }

    /**
     * Retrieves all {@link AnnotationMirror} instances of the specified annotated type from the given
     * {@link ProcessingEnvironment}. If either the processing environment or the annotated type is {@code null},
     * this method returns an empty list.
     *
     * @param processingEnv the processing environment used to retrieve annotations, may be {@code null}
     * @param annotatedType the annotated type to search for annotations, may be {@code null}
     * @return a non-null immutable list of {@link AnnotationMirror} instances; never {@code null}
     */
    static List<AnnotationMirror> getAllAnnotations(ProcessingEnvironment processingEnv, Type annotatedType) {
        if (processingEnv == null || annotatedType == null) {
            return emptyList();
        }
        return findAllAnnotations(processingEnv, annotatedType, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves the first {@link AnnotationMirror} of the specified annotation class from the given
     * {@link TypeMirror}. If either the type or the annotation class is {@code null},
     * this method returns {@code null}.
     *
     * @param type            the type mirror to search for annotations, may be {@code null}
     * @param annotationClass the annotation class to look for, may be {@code null}
     * @return the first matching {@link AnnotationMirror}, or {@code null} if none found
     */
    static AnnotationMirror findAnnotation(TypeMirror type, Class<? extends Annotation> annotationClass) {
        if (type == null || annotationClass == null) {
            return null;
        }
        return findAnnotation(type, annotationClass.getTypeName());
    }

    /**
     * Retrieves the first {@link AnnotationMirror} of the specified annotation class name from the given
     * {@link TypeMirror}. If either the type or the annotation class name is {@code null},
     * this method returns {@code null}.
     *
     * @param type                the type mirror to search for annotations, may be {@code null}
     * @param annotationClassName the fully qualified class name of the annotation to look for, may be {@code null}
     * @return the first matching {@link AnnotationMirror}, or {@code null} if none found
     */
    static AnnotationMirror findAnnotation(TypeMirror type, CharSequence annotationClassName) {
        if (type == null || annotationClassName == null) {
            return null;
        }
        return findAnnotation(ofTypeElement(type), annotationClassName);
    }

    /**
     * Retrieves the first {@link AnnotationMirror} of the specified annotation class from the given
     * {@link Element}. If either the element or the annotation class is {@code null},
     * this method returns {@code null}.
     *
     * @param element         the annotated element to search for annotations, may be {@code null}
     * @param annotationClass the annotation class to look for, may be {@code null}
     * @return the first matching {@link AnnotationMirror}, or {@code null} if none found
     */
    static AnnotationMirror findAnnotation(Element element, Class<? extends Annotation> annotationClass) {
        if (element == null || annotationClass == null) {
            return null;
        }
        return findAnnotation(element, annotationClass.getTypeName());
    }

    /**
     * Retrieves the first {@link AnnotationMirror} of the specified annotation class name from the given
     * {@link Element}. If either the element or the annotation class name is {@code null},
     * this method returns {@code null}.
     *
     * @param element             the annotated element to search for annotations, may be {@code null}
     * @param annotationClassName the fully qualified class name of the annotation to look for, may be {@code null}
     * @return the first matching {@link AnnotationMirror}, or {@code null} if none found
     */
    static AnnotationMirror findAnnotation(Element element, CharSequence annotationClassName) {
        if (element == null || annotationClassName == null) {
            return null;
        }
        List<AnnotationMirror> annotations = findAllAnnotations(element, annotation -> matchesAnnotationTypeName(annotation, annotationClassName));
        return isEmpty(annotations) ? null : annotations.get(0);
    }

    /**
     * Retrieves the first meta-annotation of the specified annotation class from the given annotated element.
     * A meta-annotation is an annotation that is present on another annotation. If either the annotated element
     * or the meta-annotation class is {@code null}, this method returns {@code null}.
     *
     * @param annotatedConstruct  the annotated element to search for meta-annotations, may be {@code null}
     * @param metaAnnotationClass the annotation class to look for as a meta-annotation, may be {@code null}
     * @return the first matching meta-{@link AnnotationMirror}, or {@code null} if none found
     */
    static AnnotationMirror findMetaAnnotation(Element annotatedConstruct, Class<? extends Annotation> metaAnnotationClass) {
        if (annotatedConstruct == null || metaAnnotationClass == null) {
            return null;
        }
        return findMetaAnnotation(annotatedConstruct, metaAnnotationClass.getName());
    }

    /**
     * Retrieves the first meta-annotation of the specified meta-annotation class name from the given annotated element.
     * A meta-annotation is an annotation that is present on another annotation. If either the annotated element
     * or the meta-annotation class name is {@code null}, this method returns {@code null}.
     *
     * @param annotatedConstruct      the annotated element to search for meta-annotations, may be {@code null}
     * @param metaAnnotationClassName the fully qualified class name of the meta-annotation to look for, may be {@code null}
     * @return the first matching meta-{@link AnnotationMirror}, or {@code null} if none found
     */
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

    /**
     * Checks if the specified annotation is present on the given element, either directly or as a meta-annotation.
     *
     * @param element         the element to check for the presence of the annotation; may be {@code null}
     * @param annotationClass the annotation class to look for; may be {@code null}
     * @return {@code true} if the annotation is present (either directly or as a meta-annotation),
     * {@code false} otherwise or if either parameter is {@code null}
     */
    static boolean isAnnotationPresent(Element element, Class<? extends Annotation> annotationClass) {
        if (element == null || annotationClass == null) {
            return false;
        }
        return findAnnotation(element, annotationClass) != null || findMetaAnnotation(element, annotationClass) != null;
    }

    /**
     * Checks if the specified annotation (by class name) is present on the given element, either directly or as a meta-annotation.
     *
     * @param element             the element to check for the presence of the annotation; may be {@code null}
     * @param annotationClassName the fully qualified class name of the annotation to look for; may be {@code null}
     * @return {@code true} if the annotation is present (either directly or as a meta-annotation),
     * {@code false} otherwise or if either parameter is {@code null}
     */
    static boolean isAnnotationPresent(Element element, CharSequence annotationClassName) {
        if (element == null || annotationClassName == null) {
            return false;
        }
        return findAnnotation(element, annotationClassName) != null || findMetaAnnotation(element, annotationClassName) != null;
    }

    /**
     * Retrieves all {@link AnnotationMirror} instances from the given {@link AnnotatedConstruct}
     * that match the provided annotation filters.
     *
     * <p>If the annotated construct is {@code null}, this method returns an empty list.
     * If no annotation filters are provided, all annotations present on the construct are returned.
     * Otherwise, only annotations that satisfy all the provided predicates are included in the result.</p>
     *
     * @param annotatedConstruct the annotated construct to search for annotations, may be {@code null}
     * @param annotationFilters  a varargs array of predicates used to filter annotations; may be empty or {@code null}
     * @return a non-null immutable list of matching {@link AnnotationMirror} instances; never {@code null}
     */
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

    /**
     * Retrieves all {@link AnnotationMirror} instances from the given {@link TypeMirror}, applying the specified
     * annotation filters to narrow down the results. If the type mirror is {@code null}, this method returns an empty list.
     *
     * <p>This method delegates to {@link #findAllAnnotations(TypeElement, Predicate[])} after converting the
     * {@link TypeMirror} to a {@link TypeElement} using {@link #ofTypeElement(TypeMirror)}.
     *
     * @param type              the type mirror to search for annotations, may be {@code null}
     * @param annotationFilters a varargs array of predicates used to filter annotations; may be empty or {@code null}
     * @return a non-null immutable list of matching {@link AnnotationMirror} instances; never {@code null}
     */
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

    /**
     * Retrieves all {@link AnnotationMirror} instances from the given {@link Element}, applying the specified
     * annotation filters to narrow down the results. If the element is {@code null}, this method returns an empty list.
     *
     * <p>This method attempts to resolve the element into a {@link TypeElement}. If successful, it delegates to
     * {@link #findAllAnnotations(TypeElement, Predicate[])} to retrieve annotations from the type hierarchy.
     * Otherwise, it falls back to using {@link #findAnnotations(AnnotatedConstruct, Predicate[])} directly on the element.</p>
     *
     * @param element           the annotated element to search for annotations, may be {@code null}
     * @param annotationFilters a varargs array of predicates used to filter annotations; may be empty or {@code null}
     * @return a non-null immutable list of matching {@link AnnotationMirror} instances; never {@code null}
     */
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

    /**
     * Retrieves all {@link AnnotationMirror} instances of the specified annotated type from the given
     * {@link ProcessingEnvironment}. If either the processing environment or the annotated type is {@code null},
     * this method returns an empty list.
     *
     * <p>This method uses the fully qualified type name of the provided {@link Type} to locate the corresponding
     * annotations. It delegates to the overloaded method that accepts a {@link CharSequence} for the type name.</p>
     *
     * @param processingEnv     the processing environment used to retrieve annotations, may be {@code null}
     * @param annotatedType     the annotated type to search for annotations, may be {@code null}
     * @param annotationFilters a varargs array of predicates used to filter annotations; may be empty or {@code null}
     * @return a non-null immutable list of {@link AnnotationMirror} instances; never {@code null}
     */
    static List<AnnotationMirror> findAllAnnotations(ProcessingEnvironment processingEnv, Type annotatedType, Predicate<? super AnnotationMirror>... annotationFilters) {
        if (processingEnv == null || annotatedType == null) {
            return emptyList();
        }
        return findAllAnnotations(processingEnv, annotatedType.getTypeName(), annotationFilters);
    }

    /**
     * Retrieves all {@link AnnotationMirror} instances of the specified annotated type name from the given
     * {@link ProcessingEnvironment}. If either the processing environment or the annotated type name is {@code null},
     * this method returns an empty list.
     *
     * <p>This method resolves the annotated type by delegating to {@link #getTypeElement(ProcessingEnvironment, CharSequence)},
     * and then uses the resolved type to find annotations with the provided filters.</p>
     *
     * @param processingEnv     the processing environment used to retrieve annotations, may be {@code null}
     * @param annotatedTypeName the fully qualified class name of the annotation to look for, may be {@code null}
     * @param annotationFilters a varargs array of predicates used to filter annotations; may be empty or {@code null}
     * @return a non-null immutable list of matching {@link AnnotationMirror} instances; never {@code null}
     */
    static List<AnnotationMirror> findAllAnnotations(ProcessingEnvironment processingEnv, CharSequence annotatedTypeName, Predicate<? super AnnotationMirror>... annotationFilters) {
        if (processingEnv == null || annotatedTypeName == null) {
            return emptyList();
        }
        return findAllAnnotations(getTypeElement(processingEnv, annotatedTypeName), annotationFilters);
    }

    /**
     * Checks if the given annotation mirror has the same type as the specified {@link Type}.
     *
     * <p>This method compares the fully qualified name of the annotation mirror's type with
     * the provided type's name. Returns {@code false} if either parameter is {@code null}.</p>
     *
     * @param annotationMirror the annotation mirror to compare; may be {@code null}
     * @param annotationType   the target type to match against; may be {@code null}
     * @return {@code true} if both parameters are non-null and their types match by name;
     * {@code false} otherwise
     */
    static boolean matchesAnnotationType(AnnotationMirror annotationMirror, Type annotationType) {
        if (annotationMirror == null || annotationType == null) {
            return false;
        }
        return matchesAnnotationTypeName(annotationMirror, annotationType.getTypeName());
    }

    /**
     * Checks if the given annotation mirror has the same type as the specified annotation class name.
     *
     * <p>This method compares the fully qualified name of the annotation mirror's type with
     * the provided annotation class name. Returns {@code false} if either parameter is {@code null}.</p>
     *
     * @param annotationMirror   the annotation mirror to compare; may be {@code null}
     * @param annotationTypeName the target annotation class name to match against; may be {@code null}
     * @return {@code true} if both parameters are non-null and their types match by name;
     * {@code false} otherwise
     */
    static boolean matchesAnnotationTypeName(AnnotationMirror annotationMirror, CharSequence annotationTypeName) {
        if (annotationMirror == null || annotationTypeName == null) {
            return false;
        }
        return isSameType(annotationMirror.getAnnotationType(), annotationTypeName);
    }

    /**
     * Retrieves the name of the attribute method from the given {@link ExecutableElement}.
     *
     * <p>This method is typically used to extract the attribute name from an annotation method declaration.
     * The returned name corresponds to the method's simple name as defined in the annotation interface.</p>
     *
     * @param attributeMethod the executable element representing the annotation attribute method, may be {@code null}
     * @return the name of the attribute method, or {@code null} if the provided element is {@code null}
     */
    static String getAttributeName(ExecutableElement attributeMethod) {
        return getMethodName(attributeMethod);
    }

    /**
     * Checks if the provided executable element represents an annotation attribute method
     * with the specified name.
     *
     * @param attributeMethod the executable element to check, may be {@code null}
     * @param attributeName   the expected name of the attribute method, may be {@code null}
     * @return {@code true} if the method is not null and its name matches the given attribute name;
     * {@code false} otherwise
     */
    static boolean matchesAttributeMethod(ExecutableElement attributeMethod, String attributeName) {
        return attributeMethod != null && Objects.equals(getAttributeName(attributeMethod), attributeName);
    }

    /**
     * Checks if the value of the given {@link AnnotationValue} matches the specified attribute value.
     *
     * <p>This method compares the actual value extracted from the annotation value with the provided
     * attribute value. Returns {@code false} if either parameter is {@code null} or if the values do not match.</p>
     *
     * @param annotationValue the annotation value to compare; may be {@code null}
     * @param attributeValue  the target value to match against; may be {@code null}
     * @return {@code true} if both parameters are non-null and their values match;
     * {@code false} otherwise
     */
    static boolean matchesAttributeValue(AnnotationValue annotationValue, Object attributeValue) {
        return annotationValue != null && Objects.equals(annotationValue.getValue(), attributeValue);
    }

    /**
     * Get the attributes map from the specified annotation
     *
     * @param annotatedConstruct the annotated construct
     * @param annotationClass    the {@link Class class} of {@link Annotation annotation}
     * @return non-null read-only {@link Map}
     */
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

    /**
     * Retrieves the map of annotation attribute methods to their corresponding values from the specified
     * {@link AnnotatedConstruct} and annotation class. This method finds the first matching annotation on the construct
     * and returns all its declared attribute values, including default values if enabled.
     *
     * <p>If the annotated construct is {@code null} or the annotation class is not present, an empty map is returned.
     *
     * @param annotatedConstruct the annotated construct (e.g., a class, method, or field) that may contain the annotation,
     *                           may be {@code null}
     * @param annotationClass    the annotation class used to locate the annotation on the construct, must not be {@code null}
     * @return a non-null immutable map of executable elements (attribute methods) to their annotation values; never {@code null},
     * returns an empty map if no annotation is found or if the construct is {@code null}
     */
    static Map<ExecutableElement, AnnotationValue> getElementValues(AnnotatedConstruct annotatedConstruct, Class<? extends Annotation> annotationClass) {
        return getElementValues(annotatedConstruct, annotationClass, WITH_DEFAULT);
    }

    /**
     * Retrieves the map of annotation attribute methods to their corresponding values from the specified
     * {@link AnnotatedConstruct} and annotation class. This method finds the first matching annotation on the construct
     * and returns all its declared attribute values, including default values if enabled.
     *
     * <p>If the annotated construct is {@code null} or the annotation class is not present, an empty map is returned.
     *
     * @param annotatedConstruct the annotated construct (e.g., a class, method, or field) that may contain the annotation,
     *                           may be {@code null}
     * @param annotationClass    the annotation class used to locate the annotation on the construct, must not be {@code null}
     * @param withDefault        flag indicating whether to include default values for attributes that are not explicitly set;
     *                           if {@code true}, default values will be included where applicable
     * @return a non-null immutable map of executable elements (attribute methods) to their annotation values; never {@code null},
     * returns an empty map if no annotation is found or if the construct is {@code null}
     */
    static Map<ExecutableElement, AnnotationValue> getElementValues(AnnotatedConstruct annotatedConstruct, Class<? extends Annotation> annotationClass, boolean withDefault) {
        return getElementValues(getAnnotation(annotatedConstruct, annotationClass), withDefault);
    }

    /**
     * Retrieves a map of annotation attribute methods to their corresponding values from the specified
     * {@link AnnotationMirror}. This method includes both explicitly set values and default values for attributes.
     *
     * <p>If the provided annotation is {@code null}, an empty map is returned.</p>
     *
     * @param annotation the annotation mirror to extract attribute values from, may be {@code null}
     * @return a non-null immutable map of executable elements (attribute methods) to their annotation values;
     * never {@code null}, returns an empty map if the annotation is {@code null}
     */
    static Map<ExecutableElement, AnnotationValue> getElementValues(AnnotationMirror annotation) {
        return getElementValues(annotation, WITH_DEFAULT);
    }

    /**
     * Retrieves a map of annotation attribute methods to their corresponding values from the specified
     * {@link AnnotationMirror}. This method includes both explicitly set values and default values for attributes
     * if the {@code withDefault} flag is set to {@code true}.
     *
     * <p>If the provided annotation is {@code null}, an empty map is returned.</p>
     *
     * @param annotation  the annotation mirror to extract attribute values from, may be {@code null}
     * @param withDefault flag indicating whether to include default values for attributes that are not explicitly set;
     *                    if {@code true}, default values will be included where applicable
     * @return a non-null immutable map of executable elements (attribute methods) to their annotation values;
     * never {@code null}, returns an empty map if the annotation is {@code null}
     */
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

    /**
     * Retrieves the attribute method and its corresponding annotation value from the specified annotation
     * based on the given attribute name. If no explicit value is found and {@code withDefault} is true,
     * it attempts to find and return the default value for the attribute.
     *
     * <p>If the provided annotation is null or the attribute name is blank, this method returns null.</p>
     *
     * @param annotation    the annotation mirror to extract the attribute value from, may be {@code null}
     * @param attributeName the name of the attribute method to look for, may be blank
     * @param withDefault   flag indicating whether to include the default value if the attribute is not explicitly set;
     *                      if true, the method will attempt to find and return the default value
     * @return an entry containing the executable element (attribute method) and its corresponding annotation value;
     * returns null if the annotation is null, the attribute name is blank, or the attribute method cannot be found
     */
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

    /**
     * Retrieves the attribute method and its corresponding annotation value from the specified element values map
     * based on the given attribute name.
     *
     * <p>This method searches through the provided map of executable elements (attribute methods) to their annotation
     * values to find a matching attribute by name. If no match is found, it returns {@code null}.</p>
     *
     * @param elementValues the map of executable elements (attribute methods) to their annotation values;
     *                      may be {@code null} or empty
     * @param attributeName the name of the attribute method to look for; may be {@code null} or blank
     * @return an entry containing the executable element (attribute method) and its corresponding annotation value;
     * returns {@code null} if the element values map is empty, the attribute name is blank, or no matching
     * attribute is found
     */
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

    /**
     * Retrieves the value of the specified attribute from the given annotation.
     *
     * <p>This method attempts to find the attribute by name in the provided annotation. If the attribute is not explicitly set,
     * it will attempt to retrieve the default value associated with that attribute, depending on the implementation's behavior.</p>
     *
     * @param <T>           the type of the attribute value to return
     * @param annotation    the annotation mirror to extract the attribute value from; may be {@code null}
     * @param attributeName the name of the attribute method to look for; may be blank or {@code null}
     * @return the value of the specified attribute if found, or the default value if available;
     * returns {@code null} if the annotation is {@code null}, the attribute name is blank,
     * or the attribute cannot be resolved
     */
    static <T> T getAttribute(AnnotationMirror annotation, String attributeName) {
        return getAttribute(annotation, attributeName, WITH_DEFAULT);
    }

    /**
     * Retrieves the value of the specified attribute from the given annotation, optionally including the default value.
     *
     * <p>This method attempts to find the attribute by name in the provided annotation. If the attribute is not explicitly set,
     * and the {@code withDefault} flag is set to {@code true}, it will attempt to retrieve the default value associated
     * with that attribute. If the attribute cannot be resolved or no value is found, this method returns {@code null}.</p>
     *
     * @param <T>           the type of the attribute value to return
     * @param annotation    the annotation mirror to extract the attribute value from; may be {@code null}
     * @param attributeName the name of the attribute method to look for; may be blank or {@code null}
     * @param withDefault   flag indicating whether to include the default value if the attribute is not explicitly set;
     *                      if {@code true}, the method will attempt to find and return the default value
     * @return the value of the specified attribute if found, or the default value if available;
     * returns {@code null} if the annotation is {@code null}, the attribute name is blank,
     * or the attribute cannot be resolved
     */
    static <T> T getAttribute(AnnotationMirror annotation, String attributeName, boolean withDefault) {
        Entry<ExecutableElement, AnnotationValue> attributeEntry = getElementValue(annotation, attributeName, withDefault);
        return getAttribute(attributeEntry);
    }

    /**
     * Retrieves the value of the specified attribute from the provided entry containing the attribute method
     * and its corresponding annotation value.
     *
     * <p>If the entry is null or either the attribute method or annotation value is unresolved, this method returns {@code null}.
     * Otherwise, it delegates to the default {@link AnnotationValueVisitor} to extract the attribute value.</p>
     *
     * @param <T>          the expected type of the attribute value
     * @param elementValue an entry containing the attribute method and its corresponding annotation value;
     *                     may be {@code null}
     * @return the resolved value of the attribute if found; returns {@code null} if the entry is null,
     * or if either the attribute method or annotation value is unresolved
     */
    static <T> T getAttribute(Entry<ExecutableElement, AnnotationValue> elementValue) {
        if (elementValue == null) {
            return null;
        }

        ExecutableElement attributeMethod = elementValue.getKey();
        AnnotationValue annotationValue = elementValue.getValue();

        return (T) annotationValue.accept(DEFAULT_ANNOTATION_VALUE_VISITOR, attributeMethod);
    }

    /**
     * Retrieves the value of the specified annotation by accessing the default attribute method named {@code "value"}.
     *
     * <p>This method delegates to {@link #getAttribute(AnnotationMirror, String)} to obtain the value of the annotation's
     * {@code value()} method. If the annotation is null or the value cannot be resolved, this method returns null.</p>
     *
     * @param <T>        the expected type of the attribute value
     * @param annotation the annotation mirror to extract the value from; may be {@code null}
     * @return the resolved value of the annotation's {@code value()} method if found; returns {@code null} if the annotation
     * is {@code null} or the value cannot be resolved
     */
    static <T> T getValue(AnnotationMirror annotation) {
        return getAttribute(annotation, VALUE_ATTRIBUTE_NAME);
    }

    /**
     * Retrieves the {@link ElementType} array from the specified annotation.
     *
     * @param annotation the specified annotation, may be {@code null}
     * @return a non-null array of {@link ElementType}; never {@code null}, returns an empty array if the annotation is {@code null}
     */
    static ElementType[] getElementTypes(AnnotationMirror annotation) {
        return annotation == null ? EMPTY_ELEMENT_TYPE_ARRAY : getElementTypes(annotation.getAnnotationType());
    }

    /**
     * Retrieves the {@link ElementType} array from the specified annotation type by checking
     * the {@link Target} annotation associated with it. If the annotation type does not have a
     * {@link Target} annotation, an empty array is returned.
     *
     * @param annotationType the declared type of the annotation
     * @return a non-null array of {@link ElementType}; never {@code null}, returns an empty array if no {@link Target} annotation is present
     */
    static ElementType[] getElementTypes(DeclaredType annotationType) {
        AnnotationMirror targetAnnotation = findAnnotation(annotationType, Target.class);
        ElementType[] elementTypes = getValue(targetAnnotation);
        return elementTypes == null ? EMPTY_ELEMENT_TYPE_ARRAY : elementTypes;
    }
}
