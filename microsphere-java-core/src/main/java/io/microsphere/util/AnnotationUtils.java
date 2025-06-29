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

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Native;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.MapUtils.immutableEntry;
import static io.microsphere.collection.MapUtils.toFixedMap;
import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.lang.function.Streams.filterAll;
import static io.microsphere.lang.function.Streams.filterFirst;
import static io.microsphere.reflect.MethodUtils.OBJECT_METHOD_PREDICATE;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.MethodUtils.findMethods;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.reflect.TypeUtils.NON_OBJECT_CLASS_FILTER;
import static io.microsphere.util.ArrayUtils.contains;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.ClassUtils.findAllInheritedClasses;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;

/**
 * {@link Annotation} Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class AnnotationUtils implements Utils {

    /**
     * A list of annotation types that are considered native to the Java language.
     * <p>
     * These annotations are defined by the Java platform and are commonly used for
     * structural or metadata purposes. They include:
     * <ul>
     *   <li>{@link Target}</li>
     *   <li>{@link Retention}</li>
     *   <li>{@link Documented}</li>
     *   <li>{@link Inherited}</li>
     *   <li>{@link Native}</li>
     *   <li>{@link Repeatable}</li>
     * </ul>
     */
    @Nonnull
    public final static List<Class<? extends Annotation>> NATIVE_ANNOTATION_TYPES = ofList(
            Target.class,
            Retention.class,
            Documented.class,
            Inherited.class,
            Native.class,
            Repeatable.class
    );

    static final Predicate<? super Method> NON_OBJECT_METHOD_PREDICATE = and(Objects::nonNull, OBJECT_METHOD_PREDICATE.negate());

    static final Predicate<? super Method> ANNOTATION_INTERFACE_METHOD_PREDICATE = AnnotationUtils::isAnnotationInterfaceMethod;

    static final Predicate<? super Method> NON_ANNOTATION_INTERFACE_METHOD_PREDICATE = ANNOTATION_INTERFACE_METHOD_PREDICATE.negate();

    static final Predicate<? super Method> ANNOTATION_DECLARED_METHOD_PREDICATE = and(NON_OBJECT_METHOD_PREDICATE, NON_ANNOTATION_INTERFACE_METHOD_PREDICATE);

    /**
     * The annotation class name of {@linkplain jdk.internal.reflect.CallerSensitive}
     */
    public static final String CALLER_SENSITIVE_ANNOTATION_CLASS_NAME = "jdk.internal.reflect.CallerSensitive";

    /**
     * The annotation {@link Class} of {@linkplain jdk.internal.reflect.CallerSensitive} that may be <code>null</code>
     */
    public static final Class<? extends Annotation> CALLER_SENSITIVE_ANNOTATION_CLASS = (Class<? extends Annotation>) resolveClass(CALLER_SENSITIVE_ANNOTATION_CLASS_NAME);

    /**
     * An empty immutable {@code Annotation} array
     */
    public static final Annotation[] EMPTY_ANNOTATION_ARRAY = ArrayUtils.EMPTY_ANNOTATION_ARRAY;

    /**
     * Checks whether the given {@link AnnotatedElement} is a Class type.
     *
     * <p>This method is useful when determining if an element corresponds to a class,
     * which can be important in annotation processing or reflection-based operations.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Class<?> clazz = String.class;
     * boolean result = AnnotationUtils.isType(clazz); // true
     * }</pre>
     *
     * @param annotatedElement the annotated element to check
     * @return {@code true} if the element is a {@link Class}, otherwise {@code false}
     * @see ElementType#TYPE
     */
    public static boolean isType(AnnotatedElement annotatedElement) {
        return annotatedElement instanceof Class;
    }

    /**
     * Checks whether the specified annotation is of the given annotation type.
     *
     * <p>This method compares the type of the provided annotation with the expected annotation type.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @Inherited
     * @Documented
     * public @interface ServiceMode {
     * }
     *
     * @Inherited
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @ServiceMode
     * @interface Monitored {
     * }
     *
     * @Inherited
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @Monitored
     * @interface DataAccess {
     * }
     *
     * @DataAccess
     * class A {
     * }
     *
     * class B extends A {
     * }
     *
     * DataAccess dataAccessOfA = A.class.getAnnotation(DataAccess.class);
     * DataAccess dataAccessOfB = B.class.getAnnotation(DataAccess.class);
     *
     * System.out.println(isSameType(dataAccessOfA, DataAccess.class)); // true
     * System.out.println(isSameType(dataAccessOfB, DataAccess.class)); // true; @DataAccess is an @Inherited annotation
     * }</pre>
     *
     * @param annotation     the annotation to check
     * @param annotationType the expected annotation type
     * @return {@code true} if the annotation is of the specified type; {@code false} otherwise
     */
    public static boolean isSameType(Annotation annotation, Class<? extends Annotation> annotationType) {
        if (annotation == null || annotationType == null) {
            return false;
        }
        return annotation.annotationType() == annotationType;
    }

    /**
     * Finds the annotation of the specified type that is directly or indirectly
     * present on the given {@link AnnotatedElement}.
     *
     * <p>This method searches for an annotation of the specified type, considering both
     * direct annotations and meta-annotations (annotations on annotations).</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @Inherited
     * @Documented
     * public @interface ServiceMode {
     * }
     *
     * @Inherited
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @ServiceMode
     * @interface Monitored {
     * }
     *
     * @Inherited
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @Monitored
     * @interface DataAccess {
     * }
     *
     * @DataAccess
     * class A {
     * }
     *
     * class B extends A {
     * }
     *
     * DataAccess dataAccessOfA = A.class.getAnnotation(DataAccess.class);
     * DataAccess dataAccessOfB = B.class.getAnnotation(DataAccess.class);
     *
     * System.out.println(findAnnotation(A.class, DataAccess.class)); // DataAccess
     * System.out.println(findAnnotation(B.class, DataAccess.class)); // DataAccess; @DataAccess is an @Inherited annotation
     * }</pre>
     *
     * <p>If either the annotated element or the annotation type is {@code null},
     * this method will return {@code null}.</p>
     *
     * @param annotatedElement the element to search for annotations on
     * @param annotationType   the type of annotation to look for
     * @param <A>              the type of the annotation to find
     * @return the first matching annotation of the specified type, or {@code null} if none is found
     */
    @Nullable
    public static <A extends Annotation> A findAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType) {
        return findAnnotation(annotatedElement, a -> isSameType(a, annotationType));
    }

    /**
     * Finds the first annotation of the specified {@link AnnotatedElement} that matches all the given filters.
     *
     * <p>This method is useful when searching for specific annotations based on custom filtering logic.
     * The search includes directly declared annotations but does not include meta-annotations.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Find an annotation by type using a predicate
     * @Target(ElementType.TYPE)
     * @Retention(RetentionPolicy.RUNTIME)
     * @interface CustomAnnotation {}
     *
     * @CustomAnnotation
     * class MyClass {}
     *
     * Annotation result = AnnotationUtils.findAnnotation(
     *     MyClass.class,
     *     a -> a.annotationType() == CustomAnnotation.class
     * );
     * System.out.println(result); // prints @CustomAnnotation
     *
     * // Example 2: Using multiple filters (e.g., filter by annotation type and additional logic)
     * Annotation filteredResult = AnnotationUtils.findAnnotation(
     *     MyClass.class,
     *     a -> a.annotationType() == CustomAnnotation.class,
     *     a -> someAdditionalCheck(a) // hypothetical additional check
     * );
     * }</pre>
     *
     * @param annotatedElement  the element to search for annotations on
     * @param annotationFilters one or more predicates used to filter the annotations;
     *                          if no filters are provided, the first available annotation will be returned
     * @param <A>               the type of the annotation to find
     * @return the first matching annotation based on the provided filters, or {@code null} if none match
     */
    @Nullable
    public static <A extends Annotation> A findAnnotation(AnnotatedElement annotatedElement,
                                                          Predicate<? super Annotation>... annotationFilters) {
        return (A) filterFirst(findAllDeclaredAnnotations(annotatedElement), annotationFilters);
    }

    /**
     * Checks whether the specified annotation is a meta-annotation, which means it's used to annotate other annotations.
     *
     * <p>A meta-annotation is an annotation that applies to another annotation type rather than directly to Java elements like classes or methods.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @Inherited
     * @Documented
     * public @interface ServiceMode {
     * }
     *
     * @Inherited
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @ServiceMode
     * @interface Monitored {
     * }
     *
     * @Inherited
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @Monitored
     * @interface DataAccess {
     * }
     *
     * @DataAccess
     * class A {
     * }
     *
     * DataAccess dataAccess = findAnnotation(A.class, DataAccess.class);
     * assertTrue(isMetaAnnotation(dataAccess, Monitored.class));        // true
     * assertTrue(isMetaAnnotation(dataAccess, ServiceMode.class));      // true
     *
     * }</pre>
     */
    /**
     * Checks whether the specified annotation is a meta-annotation targeting a specific type.
     *
     * <p>A meta-annotation is an annotation that applies to another annotation type rather than directly
     * to Java elements like classes or methods. This method checks if the provided annotation is used
     * as a meta-annotation on its annotation type.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @Inherited
     * @Documented
     * public @interface ServiceMode {
     * }
     *
     * @Inherited
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @ServiceMode
     * @interface Monitored {
     * }
     *
     * @Inherited
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @Monitored
     * @interface DataAccess {
     * }
     *
     * @DataAccess
     * class A {
     * }
     *
     * DataAccess dataAccess = A.class.getAnnotation(DataAccess.class);
     * System.out.println(AnnotationUtils.isMetaAnnotation(dataAccess, Monitored.class));   // true
     * System.out.println(AnnotationUtils.isMetaAnnotation(dataAccess, ServiceMode.class)); // true
     * }</pre>
     *
     * @param annotation         the annotation to check
     * @param metaAnnotationType the expected meta-annotation type
     * @return {@code true} if the annotation's type is annotated with the specified meta-annotation;
     * otherwise, {@code false}
     */
    public static boolean isMetaAnnotation(Annotation annotation,
                                           Class<? extends Annotation> metaAnnotationType) {
        if (annotation == null || metaAnnotationType == null) {
            return false;
        }
        return isMetaAnnotation(annotation.annotationType(), metaAnnotationType);
    }

    /**
     * Checks whether the specified annotation is a meta-annotation, which means it's used to annotate other annotations.
     *
     * <p>A meta-annotation is an annotation that applies to another annotation type rather than directly to Java elements like classes or methods.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @Inherited
     * @Documented
     * public @interface ServiceMode {
     * }
     *
     * @Inherited
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @ServiceMode
     * @interface Monitored {
     * }
     *
     * @Inherited
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @Monitored
     * @interface DataAccess {
     * }
     *
     * @DataAccess
     * class A {
     * }
     *
     * DataAccess dataAccess = findAnnotation(A.class, DataAccess.class);
     * assertTrue(isMetaAnnotation(dataAccess, Monitored.class));        // true
     * assertTrue(isMetaAnnotation(dataAccess, ServiceMode.class));      // true
     *
     * }</pre>
     *
     * @param annotation          the annotation to check
     * @param metaAnnotationTypes optional types of annotations to consider as meta-annotations;
     *                            if none are provided, all annotations will be considered
     * @return {@code true} if the specified annotation is a meta-annotation; otherwise, {@code false}
     */
    public static boolean isMetaAnnotation(Annotation annotation,
                                           Class<? extends Annotation>... metaAnnotationTypes) {
        if (annotation == null || isEmpty(metaAnnotationTypes)) {
            return false;
        }
        return isMetaAnnotation(annotation, ofList(metaAnnotationTypes));
    }

    /**
     * Checks whether the specified annotation is a meta-annotation, which means it's used to annotate other annotations.
     *
     * <p>A meta-annotation is an annotation that applies to another annotation type rather than directly to Java elements like classes or methods.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Checking if @Target is a meta-annotation
     * boolean result = AnnotationUtils.isMetaAnnotation(Target.class);
     * System.out.println(result); // false, because Target is not annotated with any meta-annotation
     *
     * // Example 2: Custom annotation scenario
     * // Assume we have the following annotations:
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @Inherited
     * @Documented
     * public @interface ServiceMode {
     * }
     *
     * @Inherited
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @ServiceMode
     * @interface Monitored {
     * }
     *
     * @Inherited
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @Monitored
     * @interface DataAccess {
     * }
     *
     * @DataAccess
     * class A {
     * }
     *
     * DataAccess dataAccess = findAnnotation(A.class, DataAccess.class);
     * System.out.println(isMetaAnnotation(dataAccess, Arrays.asList(Monitored.class))); // true
     * System.out.println(isMetaAnnotation(dataAccess, Arrays.asList(ServiceMode.class))); // true
     *
     * }</pre>
     *
     * @param annotation          the annotation to check
     * @param metaAnnotationTypes optional types of annotations to consider as meta-annotations;
     *                            if none are provided, all annotations will be considered
     * @return {@code true} if the specified annotation is a meta-annotation; otherwise, {@code false}
     */
    public static boolean isMetaAnnotation(Annotation annotation,
                                           Iterable<Class<? extends Annotation>> metaAnnotationTypes) {
        if (annotation == null) {
            return false;
        }
        return isMetaAnnotation(annotation.annotationType(), metaAnnotationTypes);
    }


    /**
     * Checks whether the specified annotation type is a meta-annotation.
     *
     * <p>A meta-annotation is an annotation that applies to another annotation type,
     * typically used to define composed annotations or custom structured annotations.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @Inherited
     * @Documented
     * public @interface ServiceMode {
     * }
     *
     * @Inherited
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @ServiceMode
     * @interface Monitored {
     * }
     *
     * @Inherited
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @Monitored
     * @interface DataAccess {
     * }
     *
     * @DataAccess
     * class A {
     * }
     *
     * DataAccess dataAccess = findAnnotation(A.class, DataAccess.class);
     * assertTrue(isMetaAnnotation(dataAccess, Monitored.class));        // true
     * assertTrue(isMetaAnnotation(dataAccess, ServiceMode.class));      // true
     *
     * }</pre>
     *
     * @param annotationType     the annotation type to check
     * @param metaAnnotationType the specific meta-annotation type to look for
     * @return {@code true} if the specified annotation is a meta-annotation; otherwise, {@code false}
     */
    public static boolean isMetaAnnotation(Class<? extends Annotation> annotationType,
                                           Class<? extends Annotation> metaAnnotationType) {
        if (annotationType == null || metaAnnotationType == null || NATIVE_ANNOTATION_TYPES.contains(annotationType)) {
            return false;
        }

        for (Annotation annotation : annotationType.getDeclaredAnnotations()) {
            if (isSameType(annotation, metaAnnotationType)) {
                return true;
            }
            if (isMetaAnnotation(annotation.annotationType(), metaAnnotationType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the specified annotation type is a meta-annotation, which means it's used to annotate other annotations.
     *
     * <p>A meta-annotation is an annotation that applies to another annotation type rather than directly to Java elements like classes or methods.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @Inherited
     * @Documented
     * public @interface ServiceMode {
     * }
     *
     * @Inherited
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @ServiceMode
     * @interface Monitored {
     * }
     *
     * @Inherited
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @Monitored
     * @interface DataAccess {
     * }
     *
     * System.out.println(AnnotationUtils.isMetaAnnotation(DataAccess.class, Monitored.class, ServiceMode.class)); // true
     * System.out.println(AnnotationUtils.isMetaAnnotation(DataAccess.class, ServiceMode.class, Monitored.class)); // true
     * }</pre>
     *
     * @param annotationType      the annotation type to check
     * @param metaAnnotationTypes optional types of annotations to consider as meta-annotations;
     *                            if none are provided, all annotations will be considered
     * @return {@code true} if the specified annotation is a meta-annotation; otherwise, {@code false}
     */
    public static boolean isMetaAnnotation(Class<? extends Annotation> annotationType,
                                           Class<? extends Annotation>... metaAnnotationTypes) {
        return isMetaAnnotation(annotationType, ofList(metaAnnotationTypes));
    }

    /**
     * Checks whether the specified annotation type is a meta-annotation, which means it's used to annotate other annotations.
     *
     * <p>A meta-annotation is an annotation that applies to another annotation type rather than directly to Java elements like classes or methods.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @Inherited
     * @Documented
     * public @interface ServiceMode {
     * }
     *
     * @Inherited
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @ServiceMode
     * @interface Monitored {
     * }
     *
     * @Inherited
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @Monitored
     * @interface DataAccess {
     * }
     *
     * System.out.println(AnnotationUtils.isMetaAnnotation(DataAccess.class, Arrays.asList(Monitored.class, ServiceMode.class)));   // true
     * System.out.println(AnnotationUtils.isMetaAnnotation(DataAccess.class, Arrays.asList(ServiceMode.class, Monitored.class)));   // true
     * }</pre>
     *
     * @param annotationType      the annotation type to check
     * @param metaAnnotationTypes optional types of annotations to consider as meta-annotations;
     *                            if none are provided, all annotations will be considered
     * @return {@code true} if the specified annotation is a meta-annotation; otherwise, {@code false}
     */
    public static boolean isMetaAnnotation(Class<? extends Annotation> annotationType,
                                           Iterable<Class<? extends Annotation>> metaAnnotationTypes) {
        if (annotationType == null || metaAnnotationTypes == null || NATIVE_ANNOTATION_TYPES.contains(annotationType)) {
            return false;
        }

        for (Class<? extends Annotation> metaAnnotationType : metaAnnotationTypes) {
            if (isMetaAnnotation(annotationType, metaAnnotationType)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get all directly declared annotations of the annotated element, not including
     * meta annotations.
     *
     * @param annotatedElement the annotated element
     * @return non-null read-only {@link List}
     */
    public static List<Annotation> getAllDeclaredAnnotations(AnnotatedElement annotatedElement) {
        return findAllDeclaredAnnotations(annotatedElement, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get the declared annotations that are <em>directly present</em> on this element.
     * This method ignores inherited annotations.
     *
     * @param annotatedElement the annotated element
     * @return non-null read-only {@link List}
     */
    public static List<Annotation> getDeclaredAnnotations(AnnotatedElement annotatedElement) {
        return findDeclaredAnnotations(annotatedElement, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Find all directly declared annotations of the annotated element with filters, not including
     * meta annotations.
     *
     * @param annotatedElement    the annotated element
     * @param annotationsToFilter the annotations to filter
     * @return non-null read-only {@link List}
     */
    public static List<Annotation> findAllDeclaredAnnotations(AnnotatedElement annotatedElement,
                                                              Predicate<? super Annotation>... annotationsToFilter) {
        if (isType(annotatedElement)) {
            return findAllDeclaredAnnotations((Class) annotatedElement, annotationsToFilter);
        } else {
            return findDeclaredAnnotations(annotatedElement, annotationsToFilter);
        }
    }

    /**
     * Get all directly declared annotations of the specified type and those all hierarchical types, not including
     * meta annotations.
     *
     * @param type                the specified type
     * @param annotationsToFilter the annotations to filter
     * @return non-null read-only {@link List}
     */
    public static List<Annotation> findAllDeclaredAnnotations(Class<?> type, Predicate<? super Annotation>... annotationsToFilter) {

        if (type == null) {
            return emptyList();
        }

        List<Annotation> allAnnotations = new LinkedList<>();
        List<Class<?>> allInheritedClasses = findAllInheritedClasses(type, NON_OBJECT_CLASS_FILTER);

        // Add the declared annotations
        allAnnotations.addAll(getDeclaredAnnotations(type));

        for (Class<?> inheritedClass : allInheritedClasses) {
            allAnnotations.addAll(getDeclaredAnnotations(inheritedClass));
        }

        return filterAnnotations(allAnnotations, annotationsToFilter);
    }

    /**
     * Find the declared annotations that are <em>directly present</em> on this element with filters.
     * This method ignores inherited annotations.
     *
     * @param annotatedElement    the annotated element
     * @param annotationsToFilter the annotations to filter
     * @return non-null read-only {@link List}
     */
    public static List<Annotation> findDeclaredAnnotations(AnnotatedElement annotatedElement,
                                                           Predicate<? super Annotation>... annotationsToFilter) {
        if (annotatedElement == null) {
            return emptyList();
        }

        return filterAnnotations(annotatedElement.getAnnotations(), annotationsToFilter);
    }

    public static List<Annotation> filterAnnotations(Annotation[] annotations,
                                                     Predicate<? super Annotation>... annotationsToFilter) {
        return isEmpty(annotations) ? emptyList() : filterAnnotations(ofList(annotations), annotationsToFilter);
    }

    public static List<Annotation> filterAnnotations(List<Annotation> annotations,
                                                     Predicate<? super Annotation>... annotationsToFilter) {
        if (isEmpty(annotations)) {
            return emptyList();
        }
        if (isEmpty(annotationsToFilter)) {
            return unmodifiableList(annotations);
        }
        List<Annotation> filteredAnnotations = filterAll(annotations, annotationsToFilter);
        return isEmpty(filteredAnnotations) ? emptyList() : filteredAnnotations;
    }

    /**
     * Find the attribute value from the specified annotations
     *
     * @param annotations   the annotations to be found
     * @param attributeName attribute name
     * @param <T>           attribute value type
     * @return attribute value if found, otherwise <code>null</code>
     */
    public static <T> T findAttributeValue(Annotation[] annotations, String attributeName) {
        T attributeValue = null;
        for (Annotation annotation : annotations) {
            if (annotation != null) {
                attributeValue = getAttributeValue(annotation, attributeName);
                if (attributeValue != null) {
                    break;
                }
            }
        }
        return attributeValue;
    }

    /**
     * Get the attribute value of the annotation
     *
     * @param annotation    annotation
     * @param attributeName attribute name
     * @param <T>           attribute value type
     * @return attribute value if found, otherwise <code>null</code>
     */
    public static <T> T getAttributeValue(Annotation annotation, String attributeName) {
        Class<?> annotationType = annotation.annotationType();
        Method attributeMethod = findMethod(annotationType, attributeName);
        return attributeMethod == null ? null : invokeMethod(annotation, attributeMethod);
    }

    /**
     * Get the attributes map from the specified annotation
     *
     * @param annotation the specified annotation
     * @return non-null read-only {@link Map}
     */
    public static Map<String, Object> getAttributesMap(Annotation annotation) {
        return findAttributesMap(annotation, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Find the attributes map from the specified annotation by the attribute names
     *
     * @param annotation             the specified annotation
     * @param attributeNamesToFilter the attribute names to filter
     * @return non-null read-only {@link Map}
     */
    public static Map<String, Object> findAttributesMap(Annotation annotation, String... attributeNamesToFilter) {
        return findAttributesMap(annotation, method -> contains(attributeNamesToFilter, method.getName()));
    }

    /**
     * Find the attributes map from the specified annotation by the {@link Method attribute method}
     *
     * @param annotation         the specified annotation
     * @param attributesToFilter the attribute methods to filter
     * @return non-null read-only {@link Map}
     */
    public static Map<String, Object> findAttributesMap(Annotation annotation, Predicate<? super Method>... attributesToFilter) {
        if (annotation == null) {
            return emptyMap();
        }

        Predicate<? super Method> predicate = and(ANNOTATION_DECLARED_METHOD_PREDICATE, and(attributesToFilter));

        List<Method> attributeMethods = findMethods(annotation.annotationType(), predicate);

        return toFixedMap(attributeMethods, method -> immutableEntry(method.getName(), invokeMethod(annotation, method)));
    }

    public static boolean exists(Annotation[] annotations, Class<? extends Annotation> annotationType) {
        return exists(ofList(annotations), annotationType);
    }

    public static boolean exists(Collection<Annotation> annotations, Class<? extends Annotation> annotationType) {
        if (isEmpty(annotations)) {
            return false;
        }
        return exists((Iterable) annotations, annotationType);
    }

    public static boolean exists(Iterable<Annotation> annotations, Class<? extends Annotation> annotationType) {
        if (annotations == null || annotationType == null) {
            return false;
        }
        boolean found = false;
        for (Annotation annotation : annotations) {
            if (Objects.equals(annotation.annotationType(), annotationType)) {
                found = true;
                break;
            }
        }
        return found;
    }

    public static boolean isAnnotationPresent(AnnotatedElement[] annotatedElements, Class<? extends Annotation> annotationType) {
        int length = length(annotatedElements);
        if (length < 1 || annotationType == null) {
            return false;
        }

        boolean annotated = false;
        for (int i = 0; i < length; i++) {
            if (isAnnotationPresent(annotatedElements[i], annotationType)) {
                annotated = true;
                break;
            }
        }

        return annotated;
    }

    public static boolean isAnnotationPresent(AnnotatedElement annotatedElement, Class<? extends Annotation> annotationType) {
        if (annotatedElement == null || annotationType == null) {
            return false;
        }
        // annotated directly
        return annotatedElement.isAnnotationPresent(annotationType);
    }

    public static boolean isAnnotationPresent(Annotation annotation, Class<? extends Annotation> annotationType) {
        if (annotation == null) {
            return false;
        }
        return isAnnotationPresent(annotation.annotationType(), annotationType);
    }

    public static boolean isAnnotationPresent(AnnotatedElement annotatedElement, Iterable<Class<? extends Annotation>> annotationTypes) {
        if (annotatedElement == null || annotationTypes == null) {
            return false;
        }
        boolean hasNext = false;
        boolean annotated = true;
        for (Class<? extends Annotation> annotationType : annotationTypes) {
            hasNext = true;
            if (!isAnnotationPresent(annotatedElement, annotationType)) {
                annotated = false;
                break;
            }
        }
        return hasNext & annotated;
    }

    public static boolean isAnnotationPresent(Annotation annotation, Iterable<Class<? extends Annotation>> annotationTypes) {
        if (annotation == null) {
            return false;
        }
        return isAnnotationPresent(annotation.annotationType(), annotationTypes);
    }

    /**
     * Is the specified method declared by the {@link Annotation} interface or not
     *
     * @param attributeMethod the attribute method
     * @return <code>true</code> if the specified method declared by the {@link Annotation} interface
     */
    public static boolean isAnnotationInterfaceMethod(Method attributeMethod) {
        return attributeMethod != null && Annotation.class == attributeMethod.getDeclaringClass();
    }

    /**
     * Is {@linkplain jdk.internal.reflect.CallerSensitive} class present or not
     *
     * @return <code>true</code> if {@linkplain jdk.internal.reflect.CallerSensitive} presents
     * @see #CALLER_SENSITIVE_ANNOTATION_CLASS
     */
    public static boolean isCallerSensitivePresent() {
        return CALLER_SENSITIVE_ANNOTATION_CLASS != null;
    }

    private AnnotationUtils() {
    }

}
