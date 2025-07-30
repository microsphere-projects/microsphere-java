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

import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
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
import static io.microsphere.collection.MapUtils.immutableEntry;
import static io.microsphere.collection.MapUtils.isEmpty;
import static io.microsphere.collection.MapUtils.newFixedLinkedHashMap;
import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static io.microsphere.lang.function.Streams.filterAll;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static io.microsphere.util.StringUtils.isBlank;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;

/**
 * A utility interface for working with annotations in the {@code javax.lang.model.*} package.
 * <p>
 * This interface provides methods to retrieve, filter, and check annotations and their attributes
 * from various constructs such as {@link Element}, {@link TypeMirror}, and others commonly used
 * in annotation processing.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Retrieve a specific annotation from an element
 * AnnotationMirror annotation = AnnotationUtils.getAnnotation(element, MyAnnotation.class);
 *
 * // Retrieve all annotations from a type
 * List<AnnotationMirror> annotations = AnnotationUtils.getAnnotations(typeMirror);
 *
 * // Check if an annotation is present on an element
 * boolean present = AnnotationUtils.isAnnotationPresent(element, MyAnnotation.class);
 *
 * // Find meta-annotations on an annotation
 * AnnotationMirror metaAnnotation = AnnotationUtils.findMetaAnnotation(annotation, MyMetaAnnotation.class);
 * }</pre>
 *
 * <p>
 * This interface is intended to be implemented by classes that provide static utility methods
 * for handling annotations during annotation processing. It helps in organizing and reusing
 * common operations related to annotations.
 * </p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
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
    @Immutable
    ElementType[] EMPTY_ELEMENT_TYPE_ARRAY = new ElementType[0];

    boolean WITH_DEFAULT = true;

    /**
     * Retrieves the first {@link AnnotationMirror} of the specified annotation class from the given
     * {@link AnnotatedConstruct}. If either the construct or the annotation class is {@code null},
     * this method returns {@code null}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Get an annotation directly present on a class
     * TypeElement typeElement = ...; // obtain a TypeElement
     * AnnotationMirror annotation = AnnotationUtils.getAnnotation(typeElement, MyAnnotation.class);
     *
     * // Get an annotation from an element that may inherit annotations from its parent
     * Element element = ...; // obtain an Element
     * annotation = AnnotationUtils.getAnnotation(element, MyAnnotation.class);
     * }</pre>
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Get an annotation by its class name from an element
     * TypeElement typeElement = ...; // obtain a TypeElement
     * String annotationClassName = "com.example.MyAnnotation";
     * AnnotationMirror annotation = AnnotationUtils.getAnnotation(typeElement, annotationClassName);
     *
     * // Get an annotation from a method element
     * Element methodElement = ...; // obtain a method Element
     * annotation = AnnotationUtils.getAnnotation(methodElement, "com.example.AnotherAnnotation");
     * }</pre>
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Retrieve all annotations from a class element
     * TypeElement typeElement = ...; // obtain a TypeElement
     * List<AnnotationMirror> annotations = AnnotationUtils.getAnnotations(typeElement);
     *
     * // Retrieve annotations from a method element
     * Element methodElement = ...; // obtain a method Element
     * annotations = AnnotationUtils.getAnnotations(methodElement);
     * }</pre>
     *
     * @param annotatedConstruct the annotated construct to search for annotations, may be {@code null}
     * @return a non-null immutable list of {@link AnnotationMirror} instances; never {@code null}
     */
    @Nonnull
    @Immutable
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Retrieve all annotations of a specific type from a class element
     * TypeElement typeElement = ...; // obtain a TypeElement
     * List<AnnotationMirror> annotations = AnnotationUtils.getAnnotations(typeElement, MyAnnotation.class);
     *
     * // Retrieve annotations from a method element
     * Element methodElement = ...; // obtain a method Element
     * annotations = AnnotationUtils.getAnnotations(methodElement, AnotherAnnotation.class);
     * }</pre>
     *
     * @param annotatedConstruct the annotated construct to search for annotations, may be {@code null}
     * @param annotationClass    the annotation class to look for, may be {@code null}
     * @return a non-null immutable list of {@link AnnotationMirror} instances; never {@code null}
     */
    @Nonnull
    @Immutable
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Retrieve annotations by their class name from a class element
     * TypeElement typeElement = ...; // obtain a TypeElement
     * String annotationClassName = "com.example.MyAnnotation";
     * List<AnnotationMirror> annotations = AnnotationUtils.getAnnotations(typeElement, annotationClassName);
     *
     * // Retrieve annotations from a method element
     * Element methodElement = ...; // obtain a method Element
     * annotations = AnnotationUtils.getAnnotations(methodElement, "com.example.AnotherAnnotation");
     * }</pre>
     *
     * @param annotatedConstruct  the annotated construct to search for annotations, may be {@code null}
     * @param annotationClassName the fully qualified class name of the annotation to look for, may be {@code null}
     * @return a non-null immutable list of {@link AnnotationMirror} instances; never {@code null}
     */
    @Nonnull
    @Immutable
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Retrieve all annotations from a type mirror
     * TypeMirror type = ...; // obtain a TypeMirror
     * List<AnnotationMirror> annotations = AnnotationUtils.getAllAnnotations(type);
     *
     * // Handle cases where the type might be null
     * annotations = AnnotationUtils.getAllAnnotations(null); // returns empty list
     * }</pre>
     *
     * @param type the type mirror to search for annotations, may be {@code null}
     * @return a non-null immutable list of {@link AnnotationMirror} instances; never {@code null}
     */
    @Nonnull
    @Immutable
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Retrieve all annotations from a class element
     * TypeElement typeElement = ...; // obtain a TypeElement
     * List<AnnotationMirror> annotations = AnnotationUtils.getAllAnnotations(typeElement);
     *
     * // Retrieve annotations from a method element
     * Element methodElement = ...; // obtain a method Element
     * annotations = AnnotationUtils.getAllAnnotations(methodElement);
     *
     * // Handle cases where the element might be null
     * annotations = AnnotationUtils.getAllAnnotations(null); // returns empty list
     * }</pre>
     *
     * @param element the annotated element to search for annotations, may be {@code null}
     * @return a non-null immutable list of {@link AnnotationMirror} instances; never {@code null}
     */
    @Nonnull
    @Immutable
    static List<AnnotationMirror> getAllAnnotations(Element element) {
        if (element == null) {
            return emptyList();
        }
        return findAllAnnotations(element, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves all {@link AnnotationMirror} instances of the specified annotation class from the given
     * {@link TypeMirror}. If either the type or the annotation class is {@code null}, this method returns an empty list.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Retrieve annotations from a type mirror
     * TypeMirror type = ...; // obtain a TypeMirror
     * List<AnnotationMirror> annotations = AnnotationUtils.getAllAnnotations(type, MyAnnotation.class);
     *
     * // Handle cases where the type might be null
     * annotations = AnnotationUtils.getAllAnnotations(null, MyAnnotation.class); // returns empty list
     *
     * // Handle cases where the annotation class might be null
     * annotations = AnnotationUtils.getAllAnnotations(type, null); // returns empty list
     * }</pre>
     *
     * @param type            the type mirror to search for annotations, may be {@code null}
     * @param annotationClass the annotation class to look for, may be {@code null}
     * @return a non-null immutable list of {@link AnnotationMirror} instances; never {@code null}
     */
    @Nonnull
    @Immutable
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Retrieve annotations from a class element
     * TypeElement typeElement = ...; // obtain a TypeElement
     * List<AnnotationMirror> annotations = AnnotationUtils.getAllAnnotations(typeElement, MyAnnotation.class);
     *
     * // Retrieve annotations from a method element
     * Element methodElement = ...; // obtain a method Element
     * annotations = AnnotationUtils.getAllAnnotations(methodElement, AnotherAnnotation.class);
     *
     * // Handle cases where the element might be null
     * annotations = AnnotationUtils.getAllAnnotations(null, MyAnnotation.class); // returns empty list
     *
     * // Handle cases where the annotation class might be null
     * annotations = AnnotationUtils.getAllAnnotations(typeElement, null); // returns empty list
     * }</pre>
     *
     * @param element         the annotated element to search for annotations, may be {@code null}
     * @param annotationClass the annotation class to look for, may be {@code null}
     * @return a non-null immutable list of {@link AnnotationMirror} instances; never {@code null}
     */
    @Nonnull
    @Immutable
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Retrieve annotations from a type mirror
     * TypeMirror type = ...; // obtain a TypeMirror
     * String annotationClassName = "com.example.MyAnnotation";
     * List<AnnotationMirror> annotations = AnnotationUtils.getAllAnnotations(type, annotationClassName);
     *
     * // Handle cases where the type might be null
     * annotations = AnnotationUtils.getAllAnnotations(null, annotationClassName); // returns empty list
     *
     * // Handle cases where the annotation class name might be null
     * annotations = AnnotationUtils.getAllAnnotations(type, null); // returns empty list
     * }</pre>
     *
     * @param type                the type mirror to search for annotations, may be {@code null}
     * @param annotationClassName the fully qualified class name of the annotation to look for, may be {@code null}
     * @return a non-null immutable list of {@link AnnotationMirror} instances; never {@code null}
     */
    @Nonnull
    @Immutable
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Retrieve annotations by their class name from a class element
     * TypeElement typeElement = ...; // obtain a TypeElement
     * String annotationClassName = "com.example.MyAnnotation";
     * List<AnnotationMirror> annotations = AnnotationUtils.getAllAnnotations(typeElement, annotationClassName);
     *
     * // Retrieve annotations from a method element
     * Element methodElement = ...; // obtain a method Element
     * annotations = AnnotationUtils.getAllAnnotations(methodElement, "com.example.AnotherAnnotation");
     *
     * // Handle cases where the element might be null
     * annotations = AnnotationUtils.getAllAnnotations(null, annotationClassName); // returns empty list
     *
     * // Handle cases where the annotation class name might be null
     * annotations = AnnotationUtils.getAllAnnotations(typeElement, null); // returns empty list
     * }</pre>
     *
     * @param element             the annotated element to search for annotations, may be {@code null}
     * @param annotationClassName the fully qualified class name of the annotation to look for, may be {@code null}
     * @return a non-null immutable list of {@link AnnotationMirror} instances; never {@code null}
     */
    @Nonnull
    @Immutable
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Retrieve annotations for a specific type from the processing environment
     * ProcessingEnvironment processingEnv = ...; // obtain a ProcessingEnvironment instance
     * Type annotatedType = MyAnnotation.class; // specify the annotation type
     * List<AnnotationMirror> annotations = AnnotationUtils.getAllAnnotations(processingEnv, annotatedType);
     *
     * // Handle cases where the processing environment might be null
     * annotations = AnnotationUtils.getAllAnnotations(null, annotatedType); // returns empty list
     *
     * // Handle cases where the annotated type might be null
     * annotations = AnnotationUtils.getAllAnnotations(processingEnv, null); // returns empty list
     * }</pre>
     *
     * @param processingEnv the processing environment used to retrieve annotations, may be {@code null}
     * @param annotatedType the annotated type to search for annotations, may be {@code null}
     * @return a non-null immutable list of {@link AnnotationMirror} instances; never {@code null}
     */
    @Nonnull
    @Immutable
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Retrieve an annotation from a type mirror
     * TypeMirror type = ...; // obtain a TypeMirror instance
     * AnnotationMirror annotation = AnnotationUtils.findAnnotation(type, MyAnnotation.class);
     *
     * // Handle cases where the type might be null
     * annotation = AnnotationUtils.findAnnotation(null, MyAnnotation.class); // returns null
     *
     * // Handle cases where the annotation class might be null
     * annotation = AnnotationUtils.findAnnotation(type, null); // returns null
     * }</pre>
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Retrieve an annotation by its class name from a type mirror
     * TypeMirror type = ...; // obtain a TypeMirror instance
     * String annotationClassName = "com.example.MyAnnotation";
     * AnnotationMirror annotation = AnnotationUtils.findAnnotation(type, annotationClassName);
     *
     * // Handle cases where the type might be null
     * annotation = AnnotationUtils.findAnnotation(null, annotationClassName); // returns null
     *
     * // Handle cases where the annotation class name might be null
     * annotation = AnnotationUtils.findAnnotation(type, null); // returns null
     * }</pre>
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Retrieve an annotation from a class element
     * TypeElement typeElement = ...; // obtain a TypeElement
     * AnnotationMirror annotation = AnnotationUtils.findAnnotation(typeElement, MyAnnotation.class);
     *
     * // Retrieve an annotation from a method element
     * Element methodElement = ...; // obtain a method Element
     * annotation = AnnotationUtils.findAnnotation(methodElement, AnotherAnnotation.class);
     *
     * // Handle cases where the element might be null
     * annotation = AnnotationUtils.findAnnotation(null, MyAnnotation.class); // returns null
     *
     * // Handle cases where the annotation class might be null
     * annotation = AnnotationUtils.findAnnotation(typeElement, null); // returns null
     * }</pre>
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Retrieve an annotation by its class name from a class element
     * TypeElement typeElement = ...; // obtain a TypeElement
     * String annotationClassName = "com.example.MyAnnotation";
     * AnnotationMirror annotation = AnnotationUtils.findAnnotation(typeElement, annotationClassName);
     *
     * // Retrieve an annotation from a method element
     * Element methodElement = ...; // obtain a method Element
     * annotation = AnnotationUtils.findAnnotation(methodElement, "com.example.AnotherAnnotation");
     *
     * // Handle cases where the element might be null
     * annotation = AnnotationUtils.findAnnotation(null, annotationClassName); // returns null
     *
     * // Handle cases where the annotation class name might be null
     * annotation = AnnotationUtils.findAnnotation(typeElement, null); // returns null
     * }</pre>
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
     * Retrieves the first meta-annotation of the specified meta-annotation class from the given annotated element.
     * A meta-annotation is an annotation that is present on another annotation. If either the annotated element
     * or the meta-annotation class is {@code null}, this method returns {@code null}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Retrieve a meta-annotation from an annotation on a class element
     * TypeElement typeElement = ...; // obtain a TypeElement
     * AnnotationMirror metaAnnotation = AnnotationUtils.findMetaAnnotation(typeElement, MyMetaAnnotation.class);
     *
     * // Retrieve a meta-annotation from an annotation on a method element
     * Element methodElement = ...; // obtain a method Element
     * metaAnnotation = AnnotationUtils.findMetaAnnotation(methodElement, AnotherMetaAnnotation.class);
     *
     * // Handle cases where the element might be null
     * metaAnnotation = AnnotationUtils.findMetaAnnotation(null, MyMetaAnnotation.class); // returns null
     *
     * // Handle cases where the meta-annotation class might be null
     * metaAnnotation = AnnotationUtils.findMetaAnnotation(typeElement, null); // returns null
     * }</pre>
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Retrieve a meta-annotation by its class name from a class element
     * TypeElement typeElement = ...; // obtain a TypeElement
     * String metaAnnotationClassName = "com.example.MyMetaAnnotation";
     * AnnotationMirror metaAnnotation = AnnotationUtils.findMetaAnnotation(typeElement, metaAnnotationClassName);
     *
     * // Retrieve a meta-annotation from a method element
     * Element methodElement = ...; // obtain a method Element
     * metaAnnotation = AnnotationUtils.findMetaAnnotation(methodElement, "com.example.AnotherMetaAnnotation");
     *
     * // Handle cases where the element might be null
     * metaAnnotation = AnnotationUtils.findMetaAnnotation(null, metaAnnotationClassName); // returns null
     *
     * // Handle cases where the meta-annotation class name might be null
     * metaAnnotation = AnnotationUtils.findMetaAnnotation(typeElement, null); // returns null
     * }</pre>
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Check if an annotation is present on a class element
     * TypeElement typeElement = ...; // obtain a TypeElement
     * boolean present = AnnotationUtils.isAnnotationPresent(typeElement, MyAnnotation.class);
     *
     * // Check if an annotation is present on a method element
     * Element methodElement = ...; // obtain a method Element
     * present = AnnotationUtils.isAnnotationPresent(methodElement, AnotherAnnotation.class);
     *
     * // Handle cases where the element might be null
     * present = AnnotationUtils.isAnnotationPresent(null, MyAnnotation.class); // returns false
     *
     * // Handle cases where the annotation class might be null
     * present = AnnotationUtils.isAnnotationPresent(typeElement, null); // returns false
     * }</pre>
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Check if an annotation is present on a class element
     * TypeElement typeElement = ...; // obtain a TypeElement
     * String annotationClassName = "com.example.MyAnnotation";
     * boolean present = AnnotationUtils.isAnnotationPresent(typeElement, annotationClassName);
     *
     * // Check if an annotation is present on a method element
     * Element methodElement = ...; // obtain a method Element
     * present = AnnotationUtils.isAnnotationPresent(methodElement, "com.example.AnotherAnnotation");
     *
     * // Handle cases where the element might be null
     * present = AnnotationUtils.isAnnotationPresent(null, annotationClassName); // returns false
     *
     * // Handle cases where the annotation class name might be null
     * present = AnnotationUtils.isAnnotationPresent(typeElement, null); // returns false
     * }</pre>
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Retrieve all annotations from a class element
     * TypeElement typeElement = ...; // obtain a TypeElement
     * List<AnnotationMirror> allAnnotations = AnnotationUtils.findAnnotations(typeElement);
     *
     * // Retrieve annotations that match a specific condition
     * List<AnnotationMirror> filteredAnnotations = AnnotationUtils.findAnnotations(typeElement,
     *     annotation -> "com.example.MyAnnotation".contentEquals(annotation.getAnnotationType().asElement().toString()));
     *
     * // Retrieve annotations that match multiple conditions
     * List<AnnotationMirror> multiFilteredAnnotations = AnnotationUtils.findAnnotations(typeElement,
     *     annotation -> isAnnotationPresent(annotation, "com.example.MetaAnnotation"),
     *     annotation -> annotation.getElementValues().size() > 1);
     *
     * // Handle cases where the annotated construct is null
     * List<AnnotationMirror> annotations = AnnotationUtils.findAnnotations(null); // returns empty list
     * }</pre>
     *
     * @param annotatedConstruct the annotated construct to search for annotations, may be {@code null}
     * @param annotationFilters  a varargs array of predicates used to filter annotations; may be empty or {@code null}
     * @return a non-null immutable list of matching {@link AnnotationMirror} instances; never {@code null}
     */
    @Nonnull
    @Immutable
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

        return annotations.isEmpty() ? emptyList() : unmodifiableList(annotations);
    }

    /**
     * Retrieves all {@link AnnotationMirror} instances from the given {@link TypeMirror}, applying the specified
     * annotation filters to narrow down the results. If the type mirror is {@code null}, this method returns an empty list.
     *
     * <p>This method delegates to {@link #findAllAnnotations(TypeElement, Predicate[])} after converting the
     * {@link TypeMirror} to a {@link TypeElement} using {@link TypeUtils#ofTypeElement(TypeMirror)}.
     *
     * @param type              the type mirror to search for annotations, may be {@code null}
     * @param annotationFilters a varargs array of predicates used to filter annotations; may be empty or {@code null}
     * @return a non-null immutable list of matching {@link AnnotationMirror} instances; never {@code null}
     */
    @Nonnull
    @Immutable
    static List<AnnotationMirror> findAllAnnotations(TypeMirror type, Predicate<? super AnnotationMirror>... annotationFilters) {
        if (type == null) {
            return emptyList();
        }
        return findAllAnnotations(ofTypeElement(type), annotationFilters);
    }

    @Nonnull
    @Immutable
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

        return isEmpty(annotations) ? emptyList() : unmodifiableList(annotations);
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
    @Nonnull
    @Immutable
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
    @Nonnull
    @Immutable
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
     * <p>This method resolves the annotated type by delegating to {@link TypeUtils#getTypeElement(ProcessingEnvironment, CharSequence)},
     * and then uses the resolved type to find annotations with the provided filters.</p>
     *
     * @param processingEnv     the processing environment used to retrieve annotations, may be {@code null}
     * @param annotatedTypeName the fully qualified class name of the annotation to look for, may be {@code null}
     * @param annotationFilters a varargs array of predicates used to filter annotations; may be empty or {@code null}
     * @return a non-null immutable list of matching {@link AnnotationMirror} instances; never {@code null}
     */
    @Nonnull
    @Immutable
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * AnnotationMirror annotationMirror = ...; // obtain an AnnotationMirror instance
     * Type annotationType = ...; // obtain a Type instance
     *
     * // Check if both are non-null and the types match
     * boolean isMatch = AnnotationUtils.matchesAnnotationType(annotationMirror, annotationType);
     *
     * // Handle cases where the annotation mirror might be null
     * isMatch = AnnotationUtils.matchesAnnotationType(null, annotationType); // returns false
     *
     * // Handle cases where the annotation type might be null
     * isMatch = AnnotationUtils.matchesAnnotationType(annotationMirror, null); // returns false
     * }</pre>
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * AnnotationMirror annotationMirror = ...; // obtain an AnnotationMirror instance
     * String annotationClassName = "com.example.MyAnnotation";
     *
     * // Check if both are non-null and the types match
     * boolean isMatch = AnnotationUtils.matchesAnnotationTypeName(annotationMirror, annotationClassName);
     *
     * // Handle cases where the annotation mirror might be null
     * isMatch = AnnotationUtils.matchesAnnotationTypeName(null, annotationClassName); // returns false
     *
     * // Handle cases where the annotation class name might be null
     * isMatch = AnnotationUtils.matchesAnnotationTypeName(annotationMirror, null); // returns false
     * }</pre>
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Given an annotation interface:
     * public @interface MyAnnotation {
     *     String value();
     *     int priority() default 0;
     * }
     *
     * // Retrieve attribute names from methods
     * ExecutableElement valueMethod = ...; // method representing 'value()'
     * ExecutableElement priorityMethod = ...; // method representing 'priority()'
     *
     * String valueName = AnnotationUtils.getAttributeName(valueMethod); // returns "value"
     * String priorityName = AnnotationUtils.getAttributeName(priorityMethod); // returns "priority"
     *
     * // Handle cases where the executable element is null
     * String name = AnnotationUtils.getAttributeName(null); // returns null
     * }</pre>
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Given an annotation interface:
     * public @interface MyAnnotation {
     *     String value();
     *     int priority() default 0;
     * }
     *
     * // Check if the executable element corresponds to the "value" method
     * ExecutableElement valueMethod = ...; // method representing 'value()'
     * boolean isValueMethod = AnnotationUtils.matchesAttributeMethod(valueMethod, "value"); // returns true
     *
     * // Check if the executable element corresponds to the "priority" method
     * ExecutableElement priorityMethod = ...; // method representing 'priority()'
     * boolean isPriorityMethod = AnnotationUtils.matchesAttributeMethod(priorityMethod, "priority"); // returns true
     *
     * // Handle cases where the executable element is null
     * boolean result = AnnotationUtils.matchesAttributeMethod(null, "value"); // returns false
     *
     * // Handle cases where the attribute name is null or blank
     * result = AnnotationUtils.matchesAttributeMethod(valueMethod, null); // returns false
     * result = AnnotationUtils.matchesAttributeMethod(valueMethod, ""); // returns false
     * }</pre>
     *
     * @param attributeMethod the executable element to check, may be {@code null}
     * @param attributeName   the expected name of the attribute method, may be {@code null} or blank
     * @return {@code true} if the method is not null and its name matches the given attribute name;
     * {@code false} otherwise
     */
    static boolean matchesAttributeMethod(ExecutableElement attributeMethod, String attributeName) {
        return attributeMethod != null && Objects.equals(getAttributeName(attributeMethod), attributeName);
    }

    /**
     * Checks if two given {@link AnnotationValue} instances are equal by comparing their values.
     *
     * <p>This method performs a deep comparison of the values contained within the annotation values.
     * If both values are {@code null}, they are considered equal. If only one is {@code null}, they are not equal.
     * Otherwise, the method compares the actual values using {@link Objects#equals(Object, Object)}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * AnnotationValue value1 = ...; // obtain an AnnotationValue instance
     * AnnotationValue value2 = ...; // obtain another AnnotationValue instance
     *
     * // Check if both annotation values are equal
     * boolean isEqual = AnnotationUtils.matchesAttributeValue(value1, value2);
     *
     * // Handle cases where either value is null
     * isEqual = AnnotationUtils.matchesAttributeValue(null, value2); // returns false
     * isEqual = AnnotationUtils.matchesAttributeValue(value1, null); // returns false
     * }</pre>
     *
     * @param one     the first annotation value to compare; may be {@code null}
     * @param another the second annotation value to compare; may be {@code null}
     * @return {@code true} if both annotation values are either {@code null} or their contents are equal;
     * {@code false} otherwise
     */
    static boolean matchesAttributeValue(AnnotationValue one, AnnotationValue another) {
        if (one == another) {
            return true;
        }
        if (one == null || another == null) {
            return false;
        }
        Object oneValue = one.getValue();
        Object anotherValue = another.getValue();
        return Objects.equals(oneValue, anotherValue);
    }

    /**
     * Checks if the value of the given {@link AnnotationValue} matches the specified attribute value.
     *
     * <p>This method compares the actual value extracted from the annotation value with the provided
     * attribute value. Returns {@code false} if either parameter is {@code null} or if the values do not match.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * AnnotationValue annotationValue = ...; // obtain an AnnotationValue instance
     * String expectedValue = "example";
     * boolean isMatch = AnnotationUtils.matchesAttributeValue(annotationValue, expectedValue); // returns true if values match
     *
     * // Handle cases where the annotation value is null
     * boolean result = AnnotationUtils.matchesAttributeValue(null, expectedValue); // returns false
     *
     * // Handle cases where the attribute value is null
     * result = AnnotationUtils.matchesAttributeValue(annotationValue, null); // returns false
     * }</pre>
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
     * Checks if the provided annotation value matches the default value of the specified attribute method.
     *
     * <p>This method is useful when determining if an attribute value in an annotation is explicitly set or
     * if it falls back to the default value declared in the annotation interface.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public @interface MyAnnotation {
     *     String value() default "default";
     * }
     *
     * AnnotationMirror annotation = ...; // obtained from an annotated element
     * ExecutableElement attributeMethod = getAttributeMethod(annotation, "value");
     * AnnotationValue annotationValue = getAttributeValue(annotation, "value");
     *
     * boolean isDefaultValue = matchesDefaultAttributeValue(attributeMethod, annotationValue);
     * }</pre></p>
     *
     * @param attributeMethod the executable element representing the annotation attribute method, may be {@code null}
     * @param annotationValue the annotation value to compare against the default, may be {@code null}
     * @return {@code true} if both the attribute method and annotation value are non-null and the value matches the default;
     * {@code false} otherwise
     */
    static boolean matchesDefaultAttributeValue(ExecutableElement attributeMethod, AnnotationValue annotationValue) {
        return attributeMethod != null && matchesAttributeValue(attributeMethod.getDefaultValue(), annotationValue);
    }

    /**
     * Retrieves a map of attribute names to their corresponding values from the first matching annotation of the specified class
     * on the given annotated construct. This includes both explicitly set values and default values for attributes.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Given an annotation interface:
     * public @interface MyAnnotation {
     *     String value() default "default";
     *     int priority() default 0;
     * }
     *
     * // Retrieve attributes from an annotated class
     * TypeElement typeElement = ...; // obtain a TypeElement
     * Map<String, Object> attributes = AnnotationUtils.getAttributesMap(typeElement, MyAnnotation.class);
     *
     * // Example output if MyAnnotation is applied with value="custom"
     * // attributes will contain:
     * // {
     * //     "value" = "custom",
     * //     "priority" = 0
     * // }
     *
     * // Handle cases where the annotated construct is null
     * attributes = AnnotationUtils.getAttributesMap(null, MyAnnotation.class); // returns empty map
     *
     * // Handle cases where the annotation class is null
     * attributes = AnnotationUtils.getAttributesMap(typeElement, null); // returns empty map
     * }</pre>
     *
     * @param annotatedConstruct the annotated construct (e.g., a class, method, or field) that may contain the annotation,
     *                           may be {@code null}
     * @param annotationClass    the annotation class used to locate the annotation on the construct, must not be {@code null}
     * @return a non-null immutable map of attribute names to their corresponding values; never {@code null},
     * returns an empty map if no annotation is found, the construct is {@code null}, or the annotation class is {@code null}
     */
    @Nonnull
    @Immutable
    static Map<String, Object> getAttributesMap(AnnotatedConstruct annotatedConstruct, Class<? extends Annotation> annotationClass) {
        return getAttributesMap(annotatedConstruct, annotationClass, WITH_DEFAULT);
    }

    /**
     * Retrieves a map of attribute names to their corresponding values from the first matching annotation of the specified class
     * on the given annotated construct. This includes both explicitly set values and default values for attributes if the
     * {@code withDefault} flag is set to {@code true}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Given an annotation interface:
     * public @interface MyAnnotation {
     *     String value() default "default";
     *     int priority() default 0;
     * }
     *
     * // Retrieve attributes from an annotated class
     * TypeElement typeElement = ...; // obtain a TypeElement
     * Map<String, Object> attributes = AnnotationUtils.getAttributesMap(typeElement, MyAnnotation.class, true);
     *
     * // Example output if MyAnnotation is applied with value="custom"
     * // attributes will contain:
     * // {
     * //     "value" = "custom",
     * //     "priority" = 0
     * // }
     *
     * // Handle cases where the annotated construct is null
     * attributes = AnnotationUtils.getAttributesMap(null, MyAnnotation.class, true); // returns empty map
     *
     * // Handle cases where the annotation class is null
     * attributes = AnnotationUtils.getAttributesMap(typeElement, null, true); // returns empty map
     * }</pre>
     *
     * @param annotatedConstruct the annotated construct (e.g., a class, method, or field) that may contain the annotation,
     *                           may be {@code null}
     * @param annotationClass    the annotation class used to locate the annotation on the construct, must not be {@code null}
     * @param withDefault        flag indicating whether to include default values for attributes that are not explicitly set;
     *                           if {@code true}, default values will be included where applicable
     * @return a non-null immutable map of attribute names to their corresponding values; never {@code null},
     * returns an empty map if no annotation is found, the construct is {@code null}, or the annotation class is {@code null}
     */
    @Nonnull
    @Immutable
    static Map<String, Object> getAttributesMap(AnnotatedConstruct annotatedConstruct, Class<? extends Annotation> annotationClass, boolean withDefault) {
        return getAttributesMap(getAnnotation(annotatedConstruct, annotationClass), withDefault);
    }

    /**
     * Retrieves a map of attribute names to their corresponding values from the specified annotation.
     * This includes both explicitly set values and default values for attributes.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Given an annotation interface:
     * public @interface MyAnnotation {
     *     String value() default "default";
     *     int priority() default 0;
     * }
     *
     * // Retrieve attributes from an annotation instance
     * AnnotationMirror annotation = ...; // obtain an AnnotationMirror instance
     * Map<String, Object> attributes = AnnotationUtils.getAttributesMap(annotation);
     *
     * // Example output if MyAnnotation is applied with value="custom"
     * // attributes will contain:
     * // {
     * //     "value" = "custom",
     * //     "priority" = 0
     * // }
     *
     * // Handle cases where the annotation is null
     * Map<String, Object> emptyMap = AnnotationUtils.getAttributesMap(null); // returns empty map
     * }</pre>
     *
     * @param annotation the specified annotation, may be {@code null}
     * @return a non-null immutable map of attribute names to their corresponding values; never {@code null},
     * returns an empty map if the annotation is {@code null}
     */
    @Nonnull
    @Immutable
    static Map<String, Object> getAttributesMap(AnnotationMirror annotation) {
        return getAttributesMap(annotation, WITH_DEFAULT);
    }

    /**
     * Retrieves a map of attribute names to their corresponding values from the specified annotation.
     * This includes both explicitly set values and default values for attributes if the
     * {@code withDefault} flag is set to {@code true}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Given an annotation interface:
     * public @interface MyAnnotation {
     *     String value() default "default";
     *     int priority() default 0;
     * }
     *
     * // Retrieve attributes from an annotation instance with default values
     * AnnotationMirror annotation = ...; // obtain an AnnotationMirror instance
     * Map<String, Object> attributes = AnnotationUtils.getAttributesMap(annotation, true);
     *
     * // Example output if MyAnnotation is applied with value="custom"
     * // attributes will contain:
     * // {
     * //     "value" = "custom",
     * //     "priority" = 0
     * // }
     *
     * // Retrieve attributes without including default values
     * Map<String, Object> explicitAttributes = AnnotationUtils.getAttributesMap(annotation, false);
     *
     * // Handle cases where the annotation is null
     * Map<String, Object> emptyMap = AnnotationUtils.getAttributesMap(null, true); // returns empty map
     * }</pre>
     *
     * @param annotation  the specified annotation, may be {@code null}
     * @param withDefault flag indicating whether to include default values for attributes that are not explicitly set;
     *                    if {@code true}, default values will be included where applicable
     * @return a non-null immutable map of attribute names to their corresponding values; never {@code null},
     * returns an empty map if the annotation is {@code null}
     */
    @Nonnull
    @Immutable
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
        return unmodifiableMap(attributesMap);
    }

    /**
     * Retrieves the map of annotation attribute methods to their corresponding values from the specified
     * {@link AnnotatedConstruct} and annotation class. This method finds the first matching annotation on the construct
     * and returns all its declared attribute values, including default values.
     *
     * <p>This method is a convenience overload that defaults to including default values. If the annotated construct
     * is {@code null} or the annotation class is not present, an empty map is returned.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Given an annotation interface:
     * public @interface MyAnnotation {
     *     String value() default "default";
     *     int priority() default 0;
     * }
     *
     * // Retrieve attribute values from an annotated class
     * TypeElement typeElement = ...; // obtain a TypeElement
     * Map<ExecutableElement, AnnotationValue> attributes = AnnotationUtils.getElementValues(typeElement, MyAnnotation.class);
     *
     * // Example output if MyAnnotation is applied with value="custom"
     * // attributes will contain:
     * // {
     * //     value(): "custom",
     * //     priority(): 0
     * // }
     *
     * // Handle cases where the annotated construct is null
     * Map<ExecutableElement, AnnotationValue> emptyMap = AnnotationUtils.getElementValues(null, MyAnnotation.class); // returns empty map
     *
     * // Handle cases where the annotation class is null
     * emptyMap = AnnotationUtils.getElementValues(typeElement, null); // returns empty map
     * }</pre>
     *
     * @param annotatedConstruct the annotated construct (e.g., a class, method, or field) that may contain the annotation,
     *                           may be {@code null}
     * @param annotationClass    the annotation class used to locate the annotation on the construct, must not be {@code null}
     * @return a non-null immutable map of executable elements (attribute methods) to their annotation values; never {@code null},
     * returns an empty map if no annotation is found or if the construct is {@code null}
     */
    @Nonnull
    @Immutable
    static Map<ExecutableElement, AnnotationValue> getElementValues(AnnotatedConstruct annotatedConstruct, Class<? extends Annotation> annotationClass) {
        return getElementValues(annotatedConstruct, annotationClass, WITH_DEFAULT);
    }

    /**
     * Retrieves the map of annotation attribute methods to their corresponding values from the specified
     * {@link AnnotatedConstruct} and annotation class. This method finds the first matching annotation on the construct
     * and returns all its declared attribute values, including default values if enabled.
     *
     * <p>If the annotated construct is {@code null} or the annotation class is not present, an empty map is returned.
     * If the {@code withDefault} flag is set to {@code true}, this method includes default values for attributes
     * that are not explicitly set.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Given an annotation interface:
     * public @interface MyAnnotation {
     *     String value() default "default";
     *     int priority() default 0;
     * }
     *
     * // Retrieve attribute values from an annotated class with default values
     * TypeElement typeElement = ...; // obtain a TypeElement
     * Map<ExecutableElement, AnnotationValue> attributes = AnnotationUtils.getElementValues(typeElement, MyAnnotation.class, true);
     *
     * // Example output if MyAnnotation is applied with value="custom"
     * // attributes will contain:
     * // {
     * //     value(): "custom",
     * //     priority(): 0
     * // }
     *
     * // Retrieve attribute values without including default values
     * Map<ExecutableElement, AnnotationValue> explicitAttributes = AnnotationUtils.getElementValues(typeElement, MyAnnotation.class, false);
     *
     * // Handle cases where the annotated construct is null
     * Map<ExecutableElement, AnnotationValue> emptyMap = AnnotationUtils.getElementValues(null, MyAnnotation.class, true); // returns empty map
     *
     * // Handle cases where the annotation class is null
     * emptyMap = AnnotationUtils.getElementValues(typeElement, null, true); // returns empty map
     * }</pre>
     *
     * @param annotatedConstruct the annotated construct (e.g., a class, method, or field) that may contain the annotation,
     *                           may be {@code null}
     * @param annotationClass    the annotation class used to locate the annotation on the construct, must not be {@code null}
     * @param withDefault        flag indicating whether to include default values for attributes that are not explicitly set;
     *                           if {@code true}, default values will be included where applicable
     * @return a non-null immutable map of executable elements (attribute methods) to their annotation values; never {@code null},
     * returns an empty map if no annotation is found or if the construct is {@code null}
     */
    @Nonnull
    @Immutable
    static Map<ExecutableElement, AnnotationValue> getElementValues(AnnotatedConstruct annotatedConstruct, Class<? extends Annotation> annotationClass, boolean withDefault) {
        return getElementValues(getAnnotation(annotatedConstruct, annotationClass), withDefault);
    }

    /**
     * Retrieves a map of annotation attribute methods to their corresponding values from the specified
     * {@link AnnotationMirror}. This method includes both explicitly set values and default values for attributes.
     *
     * <p>If the provided annotation is {@code null}, an empty map is returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Given an annotation interface:
     * public @interface MyAnnotation {
     *     String value() default "default";
     *     int priority() default 0;
     * }
     *
     * // Retrieve attribute values from an annotation instance
     * AnnotationMirror annotation = ...; // obtain an AnnotationMirror instance
     * Map<ExecutableElement, AnnotationValue> attributes = AnnotationUtils.getElementValues(annotation);
     *
     * // Example output if MyAnnotation is applied with value="custom"
     * // attributes will contain:
     * // {
     * //     value(): "custom",
     * //     priority(): 0
     * // }
     *
     * // Handle cases where the annotation is null
     * Map<ExecutableElement, AnnotationValue> emptyMap = AnnotationUtils.getElementValues(null); // returns empty map
     * }</pre>
     *
     * @param annotation the annotation mirror to extract attribute values from, may be {@code null}
     * @return a non-null immutable map of executable elements (attribute methods) to their annotation values;
     * never {@code null}, returns an empty map if the annotation is {@code null}
     */
    @Nonnull
    @Immutable
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Given an annotation interface:
     * public @interface MyAnnotation {
     *     String value() default "default";
     *     int priority() default 0;
     * }
     *
     * // Retrieve attribute values from an annotation instance with default values
     * AnnotationMirror annotation = ...; // obtain an AnnotationMirror instance
     * Map<ExecutableElement, AnnotationValue> attributes = AnnotationUtils.getElementValues(annotation, true);
     *
     * // Example output if MyAnnotation is applied with value="custom"
     * // attributes will contain:
     * // {
     * //     value(): "custom",
     * //     priority(): 0
     * // }
     *
     * // Retrieve attribute values without including default values
     * Map<ExecutableElement, AnnotationValue> explicitAttributes = AnnotationUtils.getElementValues(annotation, false);
     *
     * // Handle cases where the annotation is null
     * Map<ExecutableElement, AnnotationValue> emptyMap = AnnotationUtils.getElementValues(null, true); // returns empty map
     * }</pre>
     *
     * @param annotation  the annotation mirror to extract attribute values from, may be {@code null}
     * @param withDefault flag indicating whether to include default values for attributes that are not explicitly set;
     *                    if {@code true}, default values will be included where applicable
     * @return a non-null immutable map of executable elements (attribute methods) to their annotation values;
     * never {@code null}, returns an empty map if the annotation is {@code null}
     */
    @Nonnull
    @Immutable
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
        return unmodifiableMap(attributes);
    }

    /**
     * Retrieves the attribute method and its corresponding annotation value from the specified annotation
     * based on the given attribute name. If no explicit value is found and {@code withDefault} is true,
     * it attempts to find and return the default value for the attribute.
     *
     * <p>If the provided annotation is null or the attribute name is blank, this method returns null.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * AnnotationMirror annotation = ...; // obtain an AnnotationMirror instance
     * String attributeName = "value"; // the name of the attribute to retrieve
     *
     * // Retrieve an attribute with default value lookup enabled
     * Map.Entry<ExecutableElement, AnnotationValue> attributeEntry = AnnotationUtils.getElementValue(annotation, attributeName, true);
     *
     * if (attributeEntry != null) {
     *     ExecutableElement attributeMethod = attributeEntry.getKey();
     *     AnnotationValue annotationValue = attributeEntry.getValue();
     *     // process attribute method and value
     * }
     *
     * // Retrieve an attribute without default value lookup
     * attributeEntry = AnnotationUtils.getElementValue(annotation, attributeName, false);
     *
     * // Handle cases where the annotation is null
     * attributeEntry = AnnotationUtils.getElementValue(null, attributeName, true); // returns null
     *
     * // Handle cases where the attribute name is blank or null
     * attributeEntry = AnnotationUtils.getElementValue(annotation, null, true); // returns null
     * }</pre>
     *
     * @param annotation    the annotation mirror to extract the attribute value from, may be {@code null}
     * @param attributeName the name of the attribute method to look for, may be blank
     * @param withDefault   flag indicating whether to include the default value if the attribute is not explicitly set;
     *                      if true, the method will attempt to find and return the default value
     * @return an entry containing the executable element (attribute method) and its corresponding annotation value;
     * returns null if the annotation is null, the attribute name is blank, or the attribute method cannot be found
     */
    @Nullable
    @Immutable
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

        return immutableEntry(attributeMethod, annotationValue);
    }

    /**
     * Retrieves the attribute method and its corresponding annotation value from the specified element values map
     * based on the given attribute name.
     *
     * <p>This method searches through the provided map of executable elements (attribute methods) to their annotation
     * values to find a matching attribute by name. If no match is found, it returns {@code null}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Given an annotation interface:
     * public @interface MyAnnotation {
     *     String value() default "default";
     *     int priority() default 0;
     * }
     *
     * // Assume elementValues contains:
     * // {
     * //     value(): "custom",
     * //     priority(): 5
     * // }
     *
     * // Retrieve an existing attribute
     * Entry<ExecutableElement, AnnotationValue> entry = AnnotationUtils.getElementValue(elementValues, "value");
     * if (entry != null) {
     *     String value = (String) entry.getValue().getValue(); // returns "custom"
     * }
     *
     * // Retrieve a non-existent attribute
     * entry = AnnotationUtils.getElementValue(elementValues, "nonExistent"); // returns null
     *
     * // Handle cases where the element values map is null or empty
     * entry = AnnotationUtils.getElementValue(null, "value"); // returns null
     * }</pre>
     *
     * @param elementValues the map of executable elements (attribute methods) to their annotation values;
     *                      may be {@code null} or empty
     * @param attributeName the name of the attribute method to look for; may be {@code null} or blank
     * @return an entry containing the executable element (attribute method) and its corresponding annotation value;
     * returns {@code null} if the element values map is empty, the attribute name is blank, or no matching
     * attribute is found
     */
    @Nullable
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
     * it will attempt to retrieve the default value associated with that attribute. If the attribute cannot be resolved or no value is found,
     * this method returns {@code null}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Given an annotation interface:
     * public @interface MyAnnotation {
     *     String value() default "default";
     *     int priority() default 0;
     * }
     *
     * // Retrieve the value of the "value" attribute
     * AnnotationMirror annotation = ...; // obtain an AnnotationMirror instance
     * String value = AnnotationUtils.getAttribute(annotation, "value"); // returns "default" or explicit value
     *
     * // Retrieve the value of the "priority" attribute
     * int priority = AnnotationUtils.getAttribute(annotation, "priority"); // returns 0 or explicit value
     *
     * // Handle cases where the annotation is null
     * String result = AnnotationUtils.getAttribute(null, "value"); // returns null
     *
     * // Handle cases where the attribute name is blank or null
     * result = AnnotationUtils.getAttribute(annotation, null); // returns null
     * }</pre>
     *
     * @param <T>           the type of the attribute value to return
     * @param annotation    the annotation mirror to extract the attribute value from; may be {@code null}
     * @param attributeName the name of the attribute method to look for; may be blank or {@code null}
     * @return the value of the specified attribute if found, or the default value if available;
     * returns {@code null} if the annotation is {@code null}, the attribute name is blank,
     * or the attribute cannot be resolved
     */
    @Nullable
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Given an annotation interface:
     * public @interface MyAnnotation {
     *     String value() default "default";
     *     int priority() default 0;
     * }
     *
     * // Retrieve the value of the "value" attribute including default
     * AnnotationMirror annotation = ...; // obtain an AnnotationMirror instance
     * String value = AnnotationUtils.getAttribute(annotation, "value", true); // returns "default" or explicit value
     *
     * // Retrieve the value of the "priority" attribute without default lookup
     * int priority = AnnotationUtils.getAttribute(annotation, "priority", false); // returns 0 only if explicitly set
     *
     * // Handle cases where the annotation is null
     * String result = AnnotationUtils.getAttribute(null, "value", true); // returns null
     *
     * // Handle cases where the attribute name is blank or null
     * result = AnnotationUtils.getAttribute(annotation, null, true); // returns null
     * }</pre>
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
    @Nullable
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Given an annotation interface:
     * public @interface MyAnnotation {
     *     String value() default "default";
     *     int priority() default 0;
     * }
     *
     * // Assume elementValue contains the entry for "value" attribute
     * Entry<ExecutableElement, AnnotationValue> elementValue = ...; // obtain from AnnotationUtils.getElementValue()
     * String value = AnnotationUtils.getAttribute(elementValue); // returns "default" or explicit value
     *
     * // Handle cases where the element value entry is null
     * String result = AnnotationUtils.getAttribute(null); // returns null
     * }</pre>
     *
     * @param <T>          the expected type of the attribute value
     * @param elementValue an entry containing the attribute method and its corresponding annotation value;
     *                     may be {@code null}
     * @return the resolved value of the attribute if found; returns {@code null} if the entry is null,
     * or if either the attribute method or annotation value is unresolved
     */
    @Nullable
    static <T> T getAttribute(Entry<ExecutableElement, AnnotationValue> elementValue) {
        if (elementValue == null) {
            return null;
        }

        ExecutableElement attributeMethod = elementValue.getKey();
        AnnotationValue annotationValue = elementValue.getValue();

        return (T) annotationValue.accept(DEFAULT_ANNOTATION_VALUE_VISITOR, attributeMethod);
    }

    /**
     * Retrieves the value of the default attribute method named {@code "value"} from the specified annotation.
     *
     * <p>This method delegates to {@link #getAttribute(AnnotationMirror, String)} to obtain the value of the annotation's
     * {@code value()} method. Returns {@code null} if the annotation is {@code null} or if the value cannot be resolved.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Given an annotation interface:
     * public @interface MyAnnotation {
     *     String value() default "default";
     * }
     *
     * // Retrieve the "value" attribute from an annotation instance
     * AnnotationMirror annotation = ...; // obtain an AnnotationMirror instance
     * String value = AnnotationUtils.getValue(annotation); // returns "default" or explicit value
     *
     * // Handle cases where the annotation is null
     * String result = AnnotationUtils.getValue(null); // returns null
     * }</pre>
     *
     * @param <T>        the expected type of the attribute value
     * @param annotation the annotation mirror to extract the value from; may be {@code null}
     * @return the resolved value of the annotation's {@code value()} method if found;
     * returns {@code null} if the annotation is {@code null} or the value cannot be resolved
     */
    @Nullable
    static <T> T getValue(AnnotationMirror annotation) {
        return getAttribute(annotation, VALUE_ATTRIBUTE_NAME);
    }

    /**
     * Retrieves the {@link ElementType} array from the specified annotation.
     *
     * @param annotation the specified annotation, may be {@code null}
     * @return a non-null array of {@link ElementType}; never {@code null}, returns an empty array if the annotation is {@code null}
     */
    /**
     * Retrieves the {@link ElementType} array from the specified annotation.
     *
     * <p>This method checks the annotation's type for the presence of a {@link Target} annotation,
     * which defines the element types the annotation can be applied to. If the provided annotation
     * is {@code null}, this method returns an empty array.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * AnnotationMirror annotation = ...; // obtain an AnnotationMirror instance
     * ElementType[] elementTypes = AnnotationUtils.getElementTypes(annotation);
     *
     * // Handle cases where the annotation is null
     * ElementType[] emptyTypes = AnnotationUtils.getElementTypes(null); // returns empty array
     * }</pre>
     *
     * @param annotation the specified annotation, may be {@code null}
     * @return a non-null array of {@link ElementType}; never {@code null}, returns an empty array if the annotation is {@code null}
     */
    @Nonnull
    static ElementType[] getElementTypes(AnnotationMirror annotation) {
        return annotation == null ? EMPTY_ELEMENT_TYPE_ARRAY : getElementTypes(annotation.getAnnotationType());
    }

    /**
     * Retrieves the {@link ElementType} array from the specified annotation type by checking
     * the {@link Target} annotation associated with it. If the annotation type does not have a
     * {@link Target} annotation, an empty array is returned.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Given an annotation type:
     * public @interface MyAnnotation {
     * }
     *
     * DeclaredType annotationType = ...; // obtain a DeclaredType for MyAnnotation
     * ElementType[] elementTypes = AnnotationUtils.getElementTypes(annotationType);
     *
     * // If MyAnnotation is annotated with @Target(ElementType.TYPE)
     * // elementTypes will contain: { ElementType.TYPE }
     *
     * // Handle cases where the annotation type does not have a @Target annotation
     * ElementType[] emptyTypes = AnnotationUtils.getElementTypes(annotationType); // returns empty array
     * }</pre>
     *
     * @param annotationType the declared type of the annotation
     * @return a non-null immutable array of {@link ElementType}; never {@code null},
     * returns an empty array if no {@link Target} annotation is present
     */
    @Nonnull
    static ElementType[] getElementTypes(DeclaredType annotationType) {
        AnnotationMirror targetAnnotation = findAnnotation(annotationType, Target.class);
        ElementType[] elementTypes = getValue(targetAnnotation);
        return elementTypes == null ? EMPTY_ELEMENT_TYPE_ARRAY : elementTypes;
    }
}
