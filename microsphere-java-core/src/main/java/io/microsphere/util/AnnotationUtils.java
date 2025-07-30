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

import io.microsphere.annotation.Immutable;
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
    @Immutable
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
    @Immutable
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
     * Retrieves all declared annotations from the specified {@link AnnotatedElement}, including those from its hierarchy,
     * but excluding meta-annotations (annotations on annotations).
     *
     * <p>This method is particularly useful when you need to inspect all annotations directly applied
     * to a class, method, or field, including those inherited from superclasses.</p>
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
     * System.out.println(getAllDeclaredAnnotations(A.class)); // Outputs: [ @DataAccess ]
     * System.out.println(getAllDeclaredAnnotations(DataAccess.class)); // Outputs: [ @Inherited , @Target , @Retention , @Monitored ]
     * }</pre>
     *
     * @param annotatedElement the element to retrieve annotations from
     * @return a read-only list of all declared annotations, not including meta-annotations
     */
    @Nonnull
    @Immutable
    public static List<Annotation> getAllDeclaredAnnotations(AnnotatedElement annotatedElement) {
        return findAllDeclaredAnnotations(annotatedElement, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves the annotations that are <em>directly declared</em> on the specified {@link AnnotatedElement}.
     *
     * <p>This method returns only the annotations directly present on the element itself, excluding any inherited annotations or meta-annotations.</p>
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
     * @Since(module = "microsphere-java-core", value = "1.0.0")
     * class B extends A {
     * }
     *
     * System.out.println(getDeclaredAnnotations(A.class)); // Outputs: [ @DataAccess ]
     * System.out.println(getDeclaredAnnotations(B.class)); // Outputs: [ @DataAccess , @Since ]
     * }</pre>
     *
     * @param annotatedElement the element to retrieve annotations from
     * @return a read-only list of annotations directly declared on the element
     */
    @Nonnull
    @Immutable
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
    /**
     * Retrieves all declared annotations from the specified {@link AnnotatedElement}, including those from its hierarchy,
     * but excluding meta-annotations (annotations on annotations).
     *
     * <p>This method is particularly useful when you need to inspect all annotations directly applied
     * to a class, method, or field, including those inherited from superclasses.</p>
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
     * @Since(module = "microsphere-java-core", value = "1.0.0")
     * class B extends A {
     * }
     *
     * System.out.println(findAllDeclaredAnnotations(A.class, annotation -> true)); // Outputs: [ @DataAccess ]
     * System.out.println(findAllDeclaredAnnotations(A.class)); // Outputs: [ @DataAccess ]
     * System.out.println(findAllDeclaredAnnotations(B.class, annotation -> true)); // Outputs: [ @DataAccess , @Since , @DataAccess ]
     * System.out.println(findAllDeclaredAnnotations(B.class)); // Outputs: [ @DataAccess , @Since , @DataAccess ]
     *
     * }</pre>
     *
     * @param annotatedElement    the element to search for annotations on
     * @param annotationsToFilter one or more predicates used to filter the annotations;
     *                            if no filters are provided, all annotations will be returned
     * @return a read-only list of annotations matching the criteria
     */
    @Nonnull
    @Immutable
    public static List<Annotation> findAllDeclaredAnnotations(AnnotatedElement annotatedElement,
                                                              Predicate<? super Annotation>... annotationsToFilter) {
        if (isType(annotatedElement)) {
            return findAllDeclaredAnnotations((Class) annotatedElement, annotationsToFilter);
        } else {
            return findDeclaredAnnotations(annotatedElement, annotationsToFilter);
        }
    }

    /**
     * Retrieves all declared annotations from the specified {@link Class}, including those from its hierarchy,
     * but excluding meta-annotations (annotations on annotations).
     *
     * <p>This method is particularly useful when you need to inspect all annotations directly applied
     * to a class, including those inherited from superclasses. It ensures that each annotation is only included once,
     * even if it appears in multiple levels of the class hierarchy.</p>
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
     * @DataAccess
     * class A {
     * }
     *
     * @Monitored
     * class B extends A {
     * }
     *
     * System.out.println(findAllDeclaredAnnotations(A.class, annotation -> true)); // Outputs: [ @DataAccess ]
     * System.out.println(findAllDeclaredAnnotations(A.class)); // Outputs: [ @DataAccess ]
     * System.out.println(findAllDeclaredAnnotations(B.class, annotation -> true)); // Outputs: [ @DataAccess , @Since , @DataAccess ]
     * System.out.println(findAllDeclaredAnnotations(B.class)); // Outputs: [ @DataAccess , @Since , @DataAccess ]
     * System.out.println(findAllDeclaredAnnotations(null)); // Outputs: [ ]
     * System.out.println(findAllDeclaredAnnotations(A.class, annotation -> false)); // Outputs: [ ]
     * }</pre>
     *
     * @param type                the class to retrieve annotations from
     * @param annotationsToFilter one or more predicates used to filter the annotations;
     *                            if no filters are provided, all annotations will be returned
     * @return a read-only list of annotations matching the criteria
     */
    @Nonnull
    @Immutable
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
     * Retrieves the annotations that are <em>directly declared</em> on the specified {@link AnnotatedElement}.
     *
     * <p>This method returns only the annotations directly present on the element itself, excluding any inherited annotations or meta-annotations.</p>
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
     * @Since(module = "microsphere-java-core", value = "1.0.0")
     * class B extends A {
     * }
     *
     * System.out.println(findDeclaredAnnotations(A.class, annotation -> true)); // Outputs: [ @DataAccess ]
     * System.out.println(findDeclaredAnnotations(A.class)); // Outputs: [ @DataAccess ]
     * System.out.println(findDeclaredAnnotations(B.class, annotation -> true)); // Outputs: [ @DataAccess , @Since ]
     * System.out.println(findDeclaredAnnotations(B.class)); // Outputs: [ @DataAccess , @Since ]
     * System.out.println(findDeclaredAnnotations(null, annotation -> true))); // Outputs: [ ]
     * System.out.println(findDeclaredAnnotations(A.class, annotation -> false))); // Outputs: [ ]
     *
     * }</pre>
     *
     * @param annotatedElement    the element to retrieve annotations from
     * @param annotationsToFilter one or more predicates used to filter the annotations;
     *                            if no filters are provided, all directly declared annotations will be returned
     * @return a read-only list of annotations directly declared on the element
     */
    @Nonnull
    @Immutable
    public static List<Annotation> findDeclaredAnnotations(AnnotatedElement annotatedElement,
                                                           Predicate<? super Annotation>... annotationsToFilter) {
        if (annotatedElement == null) {
            return emptyList();
        }

        return filterAnnotations(annotatedElement.getAnnotations(), annotationsToFilter);
    }

    /**
     * Filters the given array of annotations based on the provided predicates.
     *
     * <p>This method converts the input array into a list and applies the filter using the
     * {@link #filterAnnotations(List, Predicate[])} method. If the input array is empty or null,
     * it returns an empty list.</p>
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
     * @Since(module = "microsphere-java-core", value = "1.0.0")
     * class B extends A {
     * }
     *
     * Annotation[] annotationsOfA = A.class.getAnnotations();
     * System.out.println(filterAnnotations(annotationsOfA, annotation -> true)); // Outputs: [ @DataAccess ]
     * System.out.println(filterAnnotations(annotationsOfA, annotation -> false)); // Outputs: [  ]
     * System.out.println(filterAnnotations((Annotation[]) null, annotation -> false)); // Outputs: [  ]
     * System.out.println(filterAnnotations(new Annotation[0], annotation -> false)); // Outputs: [  ]
     *
     * Annotation[] annotationsOfB = B.class.getAnnotations();
     * System.out.println(filterAnnotations(annotationsOfB, annotation -> true)); // Outputs: [ @Since, @DataAccess ]
     * System.out.println(filterAnnotations(annotationsOfB, annotation -> false)); // Outputs: [  ]
     *
     * }</pre>
     *
     * @param annotations         the array of annotations to be filtered
     * @param annotationsToFilter one or more predicates used to filter the annotations;
     *                            if no filters are provided, all annotations will be returned
     * @return a read-only list of annotations matching the criteria
     */
    @Nonnull
    @Immutable
    public static List<Annotation> filterAnnotations(Annotation[] annotations,
                                                     Predicate<? super Annotation>... annotationsToFilter) {
        return isEmpty(annotations) ? emptyList() : filterAnnotations(ofList(annotations), annotationsToFilter);
    }

    /**
     * Filters the given list of annotations based on the provided predicates.
     *
     * <p>This method applies each predicate in the array to filter the input list of annotations.
     * If no filters are provided, it returns an unmodifiable view of the original list.
     * If any of the filters reject an annotation, it is excluded from the result.</p>
     *
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
     * @Since(module = "microsphere-java-core", value = "1.0.0")
     * class B extends A {
     * }
     *
     * Annotation[] annotationsOfA = A.class.getAnnotations();
     * System.out.println(filterAnnotations(ofList(annotationsOfA), annotation -> true)); // Outputs: [ @DataAccess ]
     * System.out.println(filterAnnotations(ofList(annotationsOfA), annotation -> false)); // Outputs: [  ]
     * System.out.println(filterAnnotations((List) null, annotation -> false)); // Outputs: [  ]
     * System.out.println(filterAnnotations(emptyList(), annotation -> false)); // Outputs: [  ]
     *
     * Annotation[] annotationsOfB = B.class.getAnnotations();
     * System.out.println(filterAnnotations(ofList(annotationsOfB), annotation -> true)); // Outputs: [ @Since, @DataAccess ]
     * System.out.println(filterAnnotations(ofList(annotationsOfB), annotation -> false)); // Outputs: [  ]
     *
     * }</pre>
     *
     * @param annotations         the list of annotations to be filtered
     * @param annotationsToFilter one or more predicates used to filter the annotations;
     *                            if no filters are provided, all annotations will be returned as an unmodifiable list
     * @return a read-only list of annotations matching the criteria
     */
    @Nonnull
    @Immutable
    public static List<Annotation> filterAnnotations(List<Annotation> annotations,
                                                     Predicate<? super Annotation>... annotationsToFilter) {
        if (isEmpty(annotations)) {
            return emptyList();
        }
        if (isEmpty(annotationsToFilter)) {
            return unmodifiableList(annotations);
        }
        List<Annotation> filteredAnnotations = filterAll(annotations, annotationsToFilter);
        return isEmpty(filteredAnnotations) ? emptyList() : unmodifiableList(filteredAnnotations);
    }

    /**
     * Finds the value of the specified attribute from the given array of annotations.
     *
     * <p>This method iterates through the provided annotations, attempting to retrieve the value of the
     * named attribute from each. It returns the first non-null value found.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @Target(TYPE)
     * @Retention(RUNTIME)
     * @Inherited
     * @Documented
     * public @interface ServiceMode {
     *     String name() default "default";
     * }
     *
     * @ServiceMode(name = "custom")
     * class A {
     * }
     *
     * Annotation[] annotations = A.class.getAnnotations();
     * String name = AnnotationUtils.findAttributeValue(annotations, "name");
     * System.out.println(name); // Outputs: custom
     * }</pre>
     *
     * @param annotations   the array of annotations to search within
     * @param attributeName the name of the attribute to find
     * @param <T>           the type of the attribute value
     * @return the value of the attribute if found; otherwise, {@code null}
     */
    @Nullable
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
     * Retrieves the value of the specified attribute from the given annotation.
     *
     * <p>This method uses reflection to find the method with the matching name in the annotation's type,
     * and then invokes it on the provided annotation instance to get the attribute value.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @Retention(RetentionPolicy.RUNTIME)
     * @Target(ElementType.TYPE)
     * public @interface CustomAnnotation {
     *     String value();
     *     int count() default 1;
     * }
     *
     * @CustomAnnotation(value = "example", count = 5)
     * class ExampleClass {}
     *
     * Annotation annotation = ExampleClass.class.getAnnotation(CustomAnnotation.class);
     * String value = AnnotationUtils.getAttributeValue(annotation, "value"); // returns "example"
     * Integer count = AnnotationUtils.getAttributeValue(annotation, "count"); // returns 5
     * }</pre>
     *
     * @param annotation    the annotation instance to retrieve the attribute value from
     * @param attributeName the name of the attribute whose value is to be retrieved
     * @param <T>           the expected type of the attribute value
     * @return the value of the attribute if found; otherwise, {@code null}
     */
    @Nullable
    public static <T> T getAttributeValue(Annotation annotation, String attributeName) {
        Class<?> annotationType = annotation.annotationType();
        Method attributeMethod = findMethod(annotationType, attributeName);
        return attributeMethod == null ? null : invokeMethod(annotation, attributeMethod);
    }

    /**
     * Retrieves a map of attribute names to their corresponding values from the specified annotation.
     *
     * <p>This method uses reflection to extract all declared methods in the annotation's type that are not defined
     * in the {@link Object} or {@link Annotation} interfaces. These methods represent the attributes of the annotation,
     * and their return values are obtained by invoking them on the provided annotation instance.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @Retention(RetentionPolicy.RUNTIME)
     * @Target(ElementType.TYPE)
     * public @interface CustomAnnotation {
     *     String value() default "default";
     *     int count() default 1;
     * }
     *
     * @CustomAnnotation(value = "example", count = 5)
     * class ExampleClass {}
     *
     * Annotation annotation = ExampleClass.class.getAnnotation(CustomAnnotation.class);
     * Map<String, Object> attributesMap = AnnotationUtils.getAttributesMap(annotation);
     *
     * System.out.println(attributesMap.get("value"));  // Outputs: example
     * System.out.println(attributesMap.get("count"));   // Outputs: 5
     * }</pre>
     *
     * @param annotation the annotation instance to retrieve the attribute map from
     * @return a non-null read-only map containing attribute names as keys and their corresponding values
     */
    @Nonnull
    @Immutable
    public static Map<String, Object> getAttributesMap(Annotation annotation) {
        return findAttributesMap(annotation, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves a map of attributes from the specified annotation, filtering by attribute names.
     *
     * <p>This method extracts only the attributes whose names match those provided in the
     * {@code attributeNamesToFilter} array. If no attribute names are provided, it returns an empty map.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @Retention(RetentionPolicy.RUNTIME)
     * @Target(ElementType.TYPE)
     * public @interface CustomAnnotation {
     *     String value() default "default";
     *     int count() default 1;
     * }
     *
     * @CustomAnnotation(value = "example", count = 5)
     * class ExampleClass {}
     *
     * Annotation annotation = ExampleClass.class.getAnnotation(CustomAnnotation.class);
     * Map<String, Object> filteredAttributes = AnnotationUtils.findAttributesMap(annotation, name -> "value".equals(name));
     *
     * System.out.println(filteredAttributes.get("value"));  // Outputs: example
     * System.out.println(filteredAttributes.containsKey("count"));  // Outputs: false
     *
     * System.out.println(AnnotationUtils.findAttributesMap(annotation, name -> false)); // Outputs : {}
     * System.out.println(AnnotationUtils.findAttributesMap(null, name -> true)); // Outputs : {}
     *
     * }</pre>
     *
     * @param annotation             the annotation instance to retrieve the attribute map from
     * @param attributeNamesToFilter the names of the attributes to include in the result map;
     *                               if none are provided, all attributes will be included
     * @return a non-null read-only map containing filtered attribute names and their corresponding values
     */
    @Nonnull
    @Immutable
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
    @Nonnull
    @Immutable
    public static Map<String, Object> findAttributesMap(Annotation annotation, Predicate<? super Method>... attributesToFilter) {
        if (annotation == null) {
            return emptyMap();
        }

        Predicate<? super Method> predicate = and(ANNOTATION_DECLARED_METHOD_PREDICATE, and(attributesToFilter));

        List<Method> attributeMethods = findMethods(annotation.annotationType(), predicate);

        return toFixedMap(attributeMethods, method -> immutableEntry(method.getName(), invokeMethod(annotation, method)));
    }

    /**
     * Checks whether any annotation in the provided array matches the specified annotation type.
     *
     * <p>This method is useful when verifying the presence of a specific annotation among an array,
     * especially for processing annotations on classes, methods, or fields.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @Retention(RetentionPolicy.RUNTIME)
     * @Target(ElementType.TYPE)
     * public @interface CustomAnnotation {
     * }
     *
     * @CustomAnnotation
     * class ExampleClass {}
     *
     * Annotation[] annotations = ExampleClass.class.getAnnotations();
     * boolean result = AnnotationUtils.exists(annotations, CustomAnnotation.class);
     * System.out.println(result); // true
     *
     * // When annotations are null or empty
     * System.out.println(AnnotationUtils.exists((Annotation[]) null, CustomAnnotation.class)); // false
     * System.out.println(AnnotationUtils.exists(new Annotation[0], CustomAnnotation.class)); // false
     *
     * // When annotation type is null
     * System.out.println(AnnotationUtils.exists(annotations, null)); // false
     * }</pre>
     *
     * @param annotations    the array of annotations to check
     * @param annotationType the type of annotation to look for
     * @return {@code true} if at least one annotation matches the specified type; otherwise, {@code false}
     */
    public static boolean exists(Annotation[] annotations, Class<? extends Annotation> annotationType) {
        return exists(ofList(annotations), annotationType);
    }

    /**
     * Checks whether any annotation in the provided collection matches the specified annotation type.
     *
     * <p>This method is useful when verifying the presence of a specific annotation among a collection,
     * especially for processing annotations on classes, methods, or fields.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @Retention(RetentionPolicy.RUNTIME)
     * @Target(ElementType.TYPE)
     * public @interface CustomAnnotation {
     * }
     *
     * @CustomAnnotation
     * class ExampleClass {}
     *
     * Collection<Annotation> annotations = Arrays.asList(ExampleClass.class.getAnnotations());
     * boolean result = AnnotationUtils.exists(annotations, CustomAnnotation.class);
     * System.out.println(result); // true
     *
     * // When annotations are null or empty
     * System.out.println(AnnotationUtils.exists((Collection<Annotation>) null, CustomAnnotation.class)); // false
     * System.out.println(AnnotationUtils.exists(Collections.emptyList(), CustomAnnotation.class)); // false
     *
     * // When annotation type is null
     * System.out.println(AnnotationUtils.exists(annotations, null)); // false
     * }</pre>
     *
     * @param annotations    the collection of annotations to check
     * @param annotationType the type of annotation to look for
     * @return {@code true} if at least one annotation matches the specified type; otherwise, {@code false}
     */
    public static boolean exists(Collection<Annotation> annotations, Class<? extends Annotation> annotationType) {
        if (isEmpty(annotations)) {
            return false;
        }
        return exists((Iterable) annotations, annotationType);
    }

    /**
     * Checks whether any annotation in the provided {@link Iterable} matches the specified annotation type.
     *
     * <p>This method is useful when verifying the presence of a specific annotation among an iterable collection,
     * especially for processing annotations on classes, methods, or fields.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @Retention(RetentionPolicy.RUNTIME)
     * @Target(ElementType.TYPE)
     * public @interface CustomAnnotation {
     * }
     *
     * @CustomAnnotation
     * class ExampleClass {}
     *
     * Collection<Annotation> annotations = Arrays.asList(ExampleClass.class.getAnnotations());
     * boolean result = AnnotationUtils.exists(annotations, CustomAnnotation.class);
     * System.out.println(result); // true
     *
     * // When annotations are null or empty
     * System.out.println(AnnotationUtils.exists((Iterable<Annotation>) null, CustomAnnotation.class)); // false
     * System.out.println(AnnotationUtils.exists(Collections.emptyList(), CustomAnnotation.class)); // false
     *
     * // When annotation type is null
     * System.out.println(AnnotationUtils.exists(annotations, null)); // false
     * }</pre>
     *
     * @param annotations    the iterable collection of annotations to check
     * @param annotationType the type of annotation to look for
     * @return {@code true} if at least one annotation matches the specified type; otherwise, {@code false}
     */
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

    /**
     * Checks whether any of the specified {@link AnnotatedElement} instances contains the given annotation type.
     *
     * <p>This method is particularly useful when dealing with multiple elements (e.g., classes, methods)
     * and you want to determine if at least one of them has a specific annotation.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @Retention(RetentionPolicy.RUNTIME)
     * @Target(ElementType.TYPE)
     * public @interface CustomAnnotation {
     * }
     *
     * @CustomAnnotation
     * class A {}
     *
     * class B {}
     *
     * AnnotatedElement[] elements = new AnnotatedElement[]{A.class, B.class};
     * boolean result = AnnotationUtils.isAnnotationPresent(elements, CustomAnnotation.class);
     * System.out.println(result); // true
     *
     * // When no element has the annotation
     * result = AnnotationUtils.isAnnotationPresent(elements, Deprecated.class);
     * System.out.println(result); // false
     *
     * // Handling null or empty input
     * result = AnnotationUtils.isAnnotationPresent(null, CustomAnnotation.class);
     * System.out.println(result); // false
     *
     * result = AnnotationUtils.isAnnotationPresent(new AnnotatedElement[0], CustomAnnotation.class);
     * System.out.println(result); // false
     * }</pre>
     *
     * @param annotatedElements an array of elements to check for annotations
     * @param annotationType    the type of annotation to look for
     * @return {@code true} if at least one element contains the specified annotation; otherwise, {@code false}
     */
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

    /**
     * Checks whether the specified {@link AnnotatedElement} has an annotation of the given type directly present on it.
     *
     * <p>This method only checks for annotations that are explicitly declared on the element and does not search meta-annotations
     * or inherited annotations. It is useful when you need to verify if a specific annotation exists directly on a class,
     * method, or field.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @Retention(RetentionPolicy.RUNTIME)
     * @Target(ElementType.TYPE)
     * public @interface CustomAnnotation {
     * }
     *
     * @CustomAnnotation
     * class ExampleClass {}
     *
     * boolean result = AnnotationUtils.isAnnotationPresent(ExampleClass.class, CustomAnnotation.class);
     * System.out.println(result); // true
     *
     * // When the annotated element is null
     * System.out.println(AnnotationUtils.isAnnotationPresent(null, CustomAnnotation.class)); // false
     *
     * // When the annotation type is null
     * System.out.println(AnnotationUtils.isAnnotationPresent(ExampleClass.class, null)); // false
     * }</pre>
     *
     * @param annotatedElement the element to check for the presence of an annotation
     * @param annotationType   the type of annotation to look for
     * @return {@code true} if the annotation is directly present on the element; otherwise, {@code false}
     */
    public static boolean isAnnotationPresent(AnnotatedElement annotatedElement, Class<? extends Annotation> annotationType) {
        if (annotatedElement == null || annotationType == null) {
            return false;
        }
        // annotated directly
        return annotatedElement.isAnnotationPresent(annotationType);
    }

    /**
     * Checks whether the specified annotation is directly present on the given annotation type.
     *
     * <p>This method is useful when determining if one annotation is used as a meta-annotation
     * on another annotation. It only checks for direct presence and does not search through
     * inherited or nested annotations.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @Retention(RetentionPolicy.RUNTIME)
     * @Target(ElementType.TYPE)
     * public @interface CustomAnnotation {
     * }
     *
     * @CustomAnnotation
     * @interface MetaAnnotated {
     * }
     *
     * Annotation annotation = MetaAnnotated.class.getAnnotation(CustomAnnotation.class);
     * boolean result = AnnotationUtils.isAnnotationPresent(annotation, CustomAnnotation.class);
     * System.out.println(result); // true
     *
     * // When the annotation is null
     * System.out.println(AnnotationUtils.isAnnotationPresent(null, CustomAnnotation.class)); // false
     *
     * // When the annotation type is null
     * System.out.println(AnnotationUtils.isAnnotationPresent(annotation, null)); // false
     * }</pre>
     *
     * @param annotation     the annotation to check
     * @param annotationType the type of annotation to look for
     * @return {@code true} if the annotation is directly present on the annotation type;
     * otherwise, {@code false}
     */
    public static boolean isAnnotationPresent(Annotation annotation, Class<? extends Annotation> annotationType) {
        if (annotation == null || annotationType == null) {
            return false;
        }
        return isAnnotationPresent(annotation.annotationType(), annotationType);
    }

    /**
     * Checks whether all specified annotation types are directly present on the given {@link AnnotatedElement}.
     *
     * <p>This method iterates through the provided collection of annotation types and verifies that each one
     * is directly present on the element. It does not consider meta-annotations or inherited annotations.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @Retention(RetentionPolicy.RUNTIME)
     * @Target(ElementType.TYPE)
     * public @interface CustomAnnotation1 {
     * }
     *
     * @Retention(RetentionPolicy.RUNTIME)
     * @Target(ElementType.TYPE)
     * public @interface CustomAnnotation2 {
     * }
     *
     * @CustomAnnotation1
     * @CustomAnnotation2
     * class ExampleClass {}
     *
     * List<Class<? extends Annotation>> annotationTypes = Arrays.asList(CustomAnnotation1.class, CustomAnnotation2.class);
     * boolean result = AnnotationUtils.isAnnotationPresent(ExampleClass.class, annotationTypes);
     * System.out.println(result); // true
     *
     * // When one of the annotations is missing
     * annotationTypes = Arrays.asList(CustomAnnotation1.class, Deprecated.class);
     * result = AnnotationUtils.isAnnotationPresent(ExampleClass.class, annotationTypes);
     * System.out.println(result); // false
     *
     * // Handling null inputs
     * result = AnnotationUtils.isAnnotationPresent(null, annotationTypes);
     * System.out.println(result); // false
     *
     * result = AnnotationUtils.isAnnotationPresent(ExampleClass.class, null);
     * System.out.println(result); // false
     * }</pre>
     *
     * @param annotatedElement the element to check for the presence of annotations
     * @param annotationTypes  the iterable collection of annotation types to verify
     * @return {@code true} if all specified annotation types are directly present on the element;
     * otherwise, {@code false}
     */
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

    /**
     * Checks whether all specified annotation types are directly present on the given {@link Annotation}.
     *
     * <p>This method iterates through the provided collection of annotation types and verifies that each one
     * is directly present on the annotation's type. It does not consider meta-annotations or inherited annotations.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @Retention(RetentionPolicy.RUNTIME)
     * @Target(ElementType.TYPE)
     * public @interface CustomAnnotation1 {
     * }
     *
     * @Retention(RetentionPolicy.RUNTIME)
     * @Target(ElementType.TYPE)
     * public @interface CustomAnnotation2 {
     * }
     *
     * @CustomAnnotation1
     * @CustomAnnotation2
     * @interface ComposedAnnotation {
     * }
     *
     * Annotation annotation = ComposedAnnotation.class.getAnnotation(ComposedAnnotation.class);
     * List<Class<? extends Annotation>> annotationTypes = Arrays.asList(CustomAnnotation1.class, CustomAnnotation2.class);
     * boolean result = AnnotationUtils.isAnnotationPresent(annotation, annotationTypes);
     * System.out.println(result); // true
     *
     * // When one of the annotations is missing
     * annotationTypes = Arrays.asList(CustomAnnotation1.class, Deprecated.class);
     * result = AnnotationUtils.isAnnotationPresent(annotation, annotationTypes);
     * System.out.println(result); // false
     *
     * // Handling null input
     * result = AnnotationUtils.isAnnotationPresent(null, annotationTypes);
     * System.out.println(result); // false
     *
     * result = AnnotationUtils.isAnnotationPresent(annotation, null);
     * System.out.println(result); // false
     * }</pre>
     *
     * @param annotation      the annotation to check for the presence of other annotations
     * @param annotationTypes the iterable collection of annotation types to verify
     * @return {@code true} if all specified annotation types are directly present on the annotation;
     * otherwise, {@code false}
     */
    public static boolean isAnnotationPresent(Annotation annotation, Iterable<Class<? extends Annotation>> annotationTypes) {
        if (annotation == null) {
            return false;
        }
        return isAnnotationPresent(annotation.annotationType(), annotationTypes);
    }

    /**
     * Checks whether the specified method is declared by the {@link Annotation} interface.
     *
     * <p>This method is useful when determining if a given method represents an attribute of an annotation,
     * as methods defined in the {@link Annotation} interface are common to all annotation types.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Method[] methods = Override.class.getMethods();
     * for (Method method : methods) {
     *     if (AnnotationUtils.isAnnotationInterfaceMethod(method)) {
     *         System.out.println(method.getName() + " is part of the Annotation interface.");
     *     } else {
     *         System.out.println(method.getName() + " is defined by the annotation type itself.");
     *     }
     * }
     * }</pre>
     *
     * @param attributeMethod the method to check
     * @return {@code true} if the method is declared by the {@link Annotation} interface;
     * otherwise, {@code false}
     */
    public static boolean isAnnotationInterfaceMethod(Method attributeMethod) {
        return attributeMethod != null && Annotation.class == attributeMethod.getDeclaringClass();
    }

    /**
     * Checks whether the {@link jdk.internal.reflect.CallerSensitive} annotation is present in the current runtime environment.
     *
     * <p>This method returns {@code true} if the annotation class can be resolved, indicating that the JVM supports it;
     * otherwise, it returns {@code false}, which may suggest that the annotation is not available or not accessible.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean isPresent = AnnotationUtils.isCallerSensitivePresent();
     * if (isPresent) {
     *     System.out.println("CallerSensitive annotation is available.");
     * } else {
     *     System.out.println("CallerSensitive annotation is not available.");
     * }
     * }</pre>
     *
     * @return {@code true} if the {@link jdk.internal.reflect.CallerSensitive} annotation is present; {@code false} otherwise
     */
    public static boolean isCallerSensitivePresent() {
        return CALLER_SENSITIVE_ANNOTATION_CLASS != null;
    }

    private AnnotationUtils() {
    }

}
