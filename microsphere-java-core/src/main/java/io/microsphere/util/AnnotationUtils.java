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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.lang.function.Streams.filterAll;
import static io.microsphere.lang.function.Streams.filterFirst;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.reflect.MethodUtils.OBJECT_METHOD_PREDICATE;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.reflect.TypeUtils.NON_OBJECT_CLASS_FILTER;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.ClassUtils.findAllInheritedClasses;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Stream.of;

/**
 * {@link Annotation} Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class AnnotationUtils extends BaseUtils {

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
     * Is the specified type a generic {@link Class type}
     *
     * @param annotatedElement the annotated element
     * @return if <code>annotatedElement</code> is the {@link Class}, return <code>true</code>, or <code>false</code>
     * @see ElementType#TYPE
     */
    static boolean isType(AnnotatedElement annotatedElement) {
        return annotatedElement instanceof Class;
    }

    /**
     * Is the type of specified annotation same to the expected type?
     *
     * @param annotation     the specified {@link Annotation}
     * @param annotationType the expected annotation type
     * @return if same, return <code>true</code>, or <code>false</code>
     */
    static boolean isSameType(Annotation annotation, Class<? extends Annotation> annotationType) {
        if (annotation == null || annotationType == null) {
            return false;
        }
        return annotation.annotationType() == annotationType;
    }

    /**
     * Find the annotation that is annotated on the specified element may be a meta-annotation
     *
     * @param annotatedElement the annotated element
     * @param annotationType   the type of annotation
     * @param <A>              the required type of annotation
     * @return If found, return first matched-type {@link Annotation annotation}, or <code>null</code>
     */
    public static <A extends Annotation> A findAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType) {
        return findAnnotation(annotatedElement, a -> isSameType(a, annotationType));
    }

    /**
     * Find the annotation that is annotated on the specified element may be a meta-annotation
     *
     * @param annotatedElement  the annotated element
     * @param annotationFilters the filters of annotations
     * @param <A>               the required type of annotation
     * @return If found, return first matched-type {@link Annotation annotation}, or <code>null</code>
     */
    public static <A extends Annotation> A findAnnotation(AnnotatedElement annotatedElement,
                                                          Predicate<? super Annotation>... annotationFilters) {
        return (A) filterFirst(findAllDeclaredAnnotations(annotatedElement), annotationFilters);
    }

    public static boolean isMetaAnnotation(Annotation annotation,
                                           Class<? extends Annotation>... metaAnnotationTypes) {
        if (metaAnnotationTypes == null) {
            return false;
        }
        return isMetaAnnotation(annotation, ofList(metaAnnotationTypes));
    }

    public static boolean isMetaAnnotation(Annotation annotation,
                                           Iterable<Class<? extends Annotation>> metaAnnotationTypes) {
        if (annotation == null) {
            return false;
        }
        return isMetaAnnotation(annotation.annotationType(), metaAnnotationTypes);
    }

    public static boolean isMetaAnnotation(Class<? extends Annotation> annotationType,
                                           Class<? extends Annotation>... metaAnnotationTypes) {
        return isMetaAnnotation(annotationType, ofList(metaAnnotationTypes));
    }

    public static boolean isMetaAnnotation(Class<? extends Annotation> annotationType,
                                           Iterable<Class<? extends Annotation>> metaAnnotationTypes) {

        if (NATIVE_ANNOTATION_TYPES.contains(annotationType)) {
            return false;
        }

        if (isAnnotationPresent(annotationType, metaAnnotationTypes)) {
            return true;
        }

        boolean annotated = false;
        for (Annotation annotation : annotationType.getDeclaredAnnotations()) {
            if (isMetaAnnotation(annotation, metaAnnotationTypes)) {
                annotated = true;
                break;
            }
        }

        return annotated;
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
        Set<Class<?>> allInheritedClasses = findAllInheritedClasses(type, NON_OBJECT_CLASS_FILTER);

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

    public static Map<String, Object> getAttributesMap(Annotation annotation) {
        return findAttributesMap(annotation, EMPTY_PREDICATE_ARRAY);
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


    public static boolean existsAnnotated(AnnotatedElement[] annotatedElements, Class<? extends Annotation> annotationType) {
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

        boolean annotated = true;
        for (Class<? extends Annotation> annotationType : annotationTypes) {
            if (!isAnnotationPresent(annotatedElement, annotationType)) {
                annotated = false;
                break;
            }
        }
        return annotated;
    }

    public static boolean isAnnotationPresent(Annotation annotation, Iterable<Class<? extends Annotation>> annotationTypes) {
        if (annotation == null) {
            return false;
        }
        return isAnnotationPresent(annotation.annotationType(), annotationTypes);
    }

    public static Map<String, Object> findAttributesMap(Annotation annotation, Predicate<Method>... attributesToFilter) {
        Map<String, Object> attributesMap = new LinkedHashMap<>();



        getAttributeMethods(annotation, attributesToFilter)
                .forEach(method -> {
                    Object value = execute(() -> method.invoke(annotation));
                    attributesMap.put(method.getName(), value);
                });
        return attributesMap.isEmpty() ? emptyMap() : unmodifiableMap(attributesMap);
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

    private static Stream<Method> getAttributeMethods(Annotation annotation, Predicate<? super Method>... attributesToFilter) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        return of(annotationType.getMethods())
                .filter(and(ANNOTATION_DECLARED_METHOD_PREDICATE, and(attributesToFilter)));
    }

}
