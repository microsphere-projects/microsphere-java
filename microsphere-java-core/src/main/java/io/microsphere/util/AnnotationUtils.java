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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.lang.function.Streams.filterAll;
import static io.microsphere.lang.function.Streams.filterFirst;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.reflect.MethodUtils.OBJECT_PUBLIC_METHODS;
import static io.microsphere.reflect.MethodUtils.overrides;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.ClassUtils.getAllInheritedTypes;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;

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

    static final Predicate<Method> INHERITED_OBJECT_METHOD_PREDICATE = AnnotationUtils::isInheritedObjectMethod;

    static final Predicate<Method> NON_INHERITED_OBJECT_METHOD_PREDICATE = INHERITED_OBJECT_METHOD_PREDICATE.negate();

    static final Predicate<Method> ANNOTATION_METHOD_PREDICATE = AnnotationUtils::isAnnotationMethod;

    static final Predicate<Method> NON_ANNOTATION_METHOD_PREDICATE = ANNOTATION_METHOD_PREDICATE.negate();

    /**
     * The annotation class name of {@linkplain jdk.internal.reflect.CallerSensitive}
     */
    public static final String CALLER_SENSITIVE_ANNOTATION_CLASS_NAME = "jdk.internal.reflect.CallerSensitive";

    /**
     * The annotation {@link Class} of {@linkplain jdk.internal.reflect.CallerSensitive} that may be <code>null</code>
     */
    public static final Class<? extends Annotation> CALLER_SENSITIVE_ANNOTATION_CLASS = (Class<? extends Annotation>) resolveClass(CALLER_SENSITIVE_ANNOTATION_CLASS_NAME);

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
        return Objects.equals(annotation.annotationType(), annotationType);
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
                                                          Predicate<Annotation>... annotationFilters) {
        return (A) filterFirst(getAllDeclaredAnnotations(annotatedElement), annotationFilters);
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
     * Get all directly declared annotations of the the annotated element, not including
     * meta annotations.
     *
     * @param annotatedElement    the annotated element
     * @param annotationsToFilter the annotations to filter
     * @return non-null read-only {@link List}
     */
    public static List<Annotation> getAllDeclaredAnnotations(AnnotatedElement annotatedElement,
                                                             Predicate<Annotation>... annotationsToFilter) {
        if (isType(annotatedElement)) {
            return getAllDeclaredAnnotations((Class) annotatedElement, annotationsToFilter);
        } else {
            return getDeclaredAnnotations(annotatedElement, annotationsToFilter);
        }
    }

    public static <S extends Iterable<Annotation>> Optional<Annotation> filterAnnotation(S annotations,
                                                                                         Predicate<Annotation>... annotationsToFilter) {
        return ofNullable(filterFirst(annotations, annotationsToFilter));
    }

    public static List<Annotation> filterAnnotations(Annotation[] annotations,
                                                     Predicate<Annotation>... annotationsToFilter) {
        return filterAnnotations(ofList(annotations), annotationsToFilter);
    }

    public static <S extends Iterable<Annotation>> S filterAnnotations(S annotations,
                                                                       Predicate<Annotation>... annotationsToFilter) {
        return filterAll(annotations, annotationsToFilter);
    }

    /**
     * Get all directly declared annotations of the specified type and its' all hierarchical types, not including
     * meta annotations.
     *
     * @param type                the specified type
     * @param annotationsToFilter the annotations to filter
     * @return non-null read-only {@link List}
     */
    public static List<Annotation> getAllDeclaredAnnotations(Class<?> type, Predicate<Annotation>... annotationsToFilter) {

        if (type == null) {
            return emptyList();
        }

        List<Annotation> allAnnotations = new LinkedList<>();

        // All types
        Set<Class<?>> allTypes = new LinkedHashSet<>();
        // Add current type
        allTypes.add(type);
        // Add all inherited types
        allTypes.addAll(getAllInheritedTypes(type, t -> !Object.class.equals(t)));

        for (Class<?> t : allTypes) {
            allAnnotations.addAll(getDeclaredAnnotations(t));
        }

        return filterAll(allAnnotations, annotationsToFilter);
    }

    /**
     * Get annotations that are <em>directly present</em> on this element.
     * This method ignores inherited annotations.
     *
     * @param annotatedElement    the annotated element
     * @param annotationsToFilter the annotations to filter
     * @return non-null read-only {@link List}
     */
    public static List<Annotation> getDeclaredAnnotations(AnnotatedElement annotatedElement,
                                                          Predicate<Annotation>... annotationsToFilter) {
        if (annotatedElement == null) {
            return emptyList();
        }

        return filterAll(ofList(annotatedElement.getAnnotations()), annotationsToFilter);
    }

    public static <T> T getAttributeValue(Annotation[] annotations, String attributeName, Class<T> returnType) {
        T attributeValue = null;
        for (Annotation annotation : annotations) {
            if (annotation != null) {
                attributeValue = getAttributeValue(annotation, attributeName, returnType);
                if (attributeValue != null) {
                    break;
                }
            }
        }
        return attributeValue;
    }

    public static <T> T getAttributeValue(Annotation annotation, String attributeName, Class<T> returnType) {
        Class<?> annotationType = annotation.annotationType();
        T attributeValue = null;
        try {
            Method method = annotationType.getMethod(attributeName);
            Object value = method.invoke(annotation);
            attributeValue = returnType.cast(value);
        } catch (Exception ignored) {
            attributeValue = null;
        }
        return attributeValue;
    }

    public static boolean contains(Collection<Annotation> annotations, Class<? extends Annotation> annotationType) {
        if (annotations == null || annotations.isEmpty()) {
            return false;
        }
        boolean contained = false;
        for (Annotation annotation : annotations) {
            if (Objects.equals(annotationType, annotation.annotationType())) {
                contained = true;
                break;
            }
        }
        return contained;
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

    public static boolean exists(Annotation[] annotations, Class<? extends Annotation> annotationType) {
        int length = length(annotations);
        if (length < 1 || annotationType == null) {
            return false;
        }

        boolean found = false;
        for (int i = 0; i < length; i++) {
            if (Objects.equals(annotations[i].annotationType(), annotationType)) {
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

    public static Object[] getAttributeValues(Annotation annotation, Predicate<Method>... attributesToFilter) {
        return getAttributeMethods(annotation, attributesToFilter)
                .map(method -> execute(() -> method.invoke(annotation)))
                .toArray(Object[]::new);
    }

    public static Map<String, Object> getAttributesMap(Annotation annotation, Predicate<Method>... attributesToFilter) {
        Map<String, Object> attributesMap = new LinkedHashMap<>();
        getAttributeMethods(annotation, attributesToFilter)
                .forEach(method -> {
                    Object value = execute(() -> method.invoke(annotation));
                    attributesMap.put(method.getName(), value);
                });
        return attributesMap.isEmpty() ? emptyMap() : unmodifiableMap(attributesMap);
    }

    public static boolean isAnnotationMethod(Method attributeMethod) {
        return attributeMethod != null && Objects.equals(Annotation.class, attributeMethod.getDeclaringClass());
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

    private static boolean isInheritedObjectMethod(Method attributeMethod) {
        boolean inherited = false;

        for (Method method : OBJECT_PUBLIC_METHODS) {
            if (overrides(attributeMethod, method)) {
                inherited = true;
                break;
            }
        }

        return inherited;
    }

    private static Stream<Method> getAttributeMethods(Annotation annotation, Predicate<Method>... attributesToFilter) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        return Stream.of(annotationType.getMethods())
                .filter(NON_INHERITED_OBJECT_METHOD_PREDICATE
                        .and(NON_ANNOTATION_METHOD_PREDICATE)
                        .and(and(attributesToFilter)));
    }

}
