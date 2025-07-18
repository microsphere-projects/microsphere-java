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

import io.microsphere.util.Utils;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static io.microsphere.annotation.processor.util.ElementUtils.filterElements;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllDeclaredTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.ofTypeElement;
import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * A utility interface for handling members (such as fields, methods, constructors) from types
 * in the {@link javax.lang.model} package.
 *
 * <h3>Overview</h3>
 * <p>
 * This interface provides a set of static methods to retrieve and filter members of a type,
 * including direct members and those inherited from superclasses and interfaces.
 * It is designed to simplify the process of working with type elements and their enclosed elements
 * during annotation processing.
 * </p>
 *
 * <h3>Key Features</h3>
 * <ul>
 *     <li>{@link #getDeclaredMembers(TypeMirror) getDeclaredMembers(TypeMirror)} - Retrieves directly declared members of a type.</li>
 *     <li>{@link #getAllDeclaredMembers(TypeMirror) getAllDeclaredMembers(TypeMirror)} - Retrieves all declared members, including those from superclasses and interfaces.</li>
 *     <li>{@link #findDeclaredMembers(TypeMirror, Predicate...) findDeclaredMembers(TypeMirror, Predicate...)} - Retrieves and filters declared members based on provided predicates.</li>
 *     <li>{@link #findAllDeclaredMembers(TypeMirror, Predicate...) findAllDeclaredMembers(TypeMirror, Predicate...)} - Retrieves all declared members and applies filtering via predicates.</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Retrieve all declared methods from a TypeMirror
 * List<ExecutableElement> methods = findDeclaredMembers(typeMirror,
 *     element -> element.getKind() == ElementKind.METHOD
 * );
 * }</pre>
 *
 * <pre>{@code
 * // Retrieve all fields that start with "m_"
 * List<VariableElement> filteredFields = findDeclaredMembers(typeElement,
 *     element -> element.getKind() == ElementKind.FIELD,
 *     element -> element.getSimpleName().toString().startsWith("m_")
 * );
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface MemberUtils extends Utils {

    /**
     * Returns the directly declared members of the provided {@link TypeMirror}.
     * If the given type is {@code null}, an empty list will be returned.
     *
     * <h3>Example Usage</h3>
     * <pre>
     * TypeMirror typeMirror = ...; // Obtain a valid TypeMirror instance
     * List<? extends Element> members = getDeclaredMembers(typeMirror);
     * if (!members.isEmpty()) {
     *     for (Element member : members) {
     *         System.out.println("Declared member: " + member);
     *     }
     * }
     * </pre>
     *
     * @param type the type mirror to retrieve declared members from
     * @return a list of directly declared members, or an empty list if the type is {@code null}
     */
    static List<? extends Element> getDeclaredMembers(TypeMirror type) {
        return type == null ? emptyList() : getDeclaredMembers(ofTypeElement(type));
    }

    /**
     * Returns the directly declared members of the provided {@link TypeElement}.
     * If the given type is {@code null}, an empty list will be returned.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeElement typeElement = ...; // Obtain a valid TypeElement instance
     * List<? extends Element> members = getDeclaredMembers(typeElement);
     * if (!members.isEmpty()) {
     *     for (Element member : members) {
     *         System.out.println("Declared member: " + member);
     *     }
     * }
     * }</pre>
     *
     * @param type the type element to retrieve declared members from
     * @return a list of directly declared members, or an empty list if the type is {@code null}
     */
    static List<? extends Element> getDeclaredMembers(TypeElement type) {
        return type == null ? emptyList() : findDeclaredMembers(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Returns all declared members (including those from superclasses and interfaces) of the provided {@link TypeMirror}.
     * If the given type is {@code null}, an empty list will be returned.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror typeMirror = ...; // Obtain a valid TypeMirror instance
     * List<? extends Element> allMembers = getAllDeclaredMembers(typeMirror);
     * if (!allMembers.isEmpty()) {
     *     for (Element member : allMembers) {
     *         System.out.println("All declared member: " + member);
     *     }
     * }
     * }</pre>
     *
     * @param type the type mirror to retrieve all declared members from
     * @return a list of all declared members, or an empty list if the type is {@code null}
     */
    static List<? extends Element> getAllDeclaredMembers(TypeMirror type) {
        return type == null ? emptyList() : findAllDeclaredMembers(ofTypeElement(type), EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Returns all declared members (including those from superclasses and interfaces) of the provided {@link TypeElement}.
     * If the given type is {@code null}, an empty list will be returned.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeElement typeElement = ...; // Obtain a valid TypeElement instance
     * List<? extends Element> allMembers = getAllDeclaredMembers(typeElement);
     * if (!allMembers.isEmpty()) {
     *     for (Element member : allMembers) {
     *         System.out.println("All declared member: " + member);
     *     }
     * }
     * }</pre>
     *
     * @param type the type element to retrieve all declared members from
     * @return a list of all declared members, or an empty list if the type is {@code null}
     */
    static List<? extends Element> getAllDeclaredMembers(TypeElement type) {
        return type == null ? emptyList() : findAllDeclaredMembers(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Returns the declared members of the provided {@link TypeMirror}, optionally including
     * members from superclasses and interfaces.
     *
     * <p>If the given type is {@code null}, an empty list will be returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror typeMirror = ...; // Obtain a valid TypeMirror instance
     * boolean includeSuperMembers = true; // Include members from superclasses/interfaces
     * List<? extends Element> members = getDeclaredMembers(typeMirror, includeSuperMembers);
     * for (Element member : members) {
     *     System.out.println("Member: " + member);
     * }
     * }</pre>
     *
     * @param type                     the type mirror to retrieve declared members from
     * @param includeHierarchicalTypes whether to include members from superclasses and interfaces
     * @return a list of declared members, or an empty list if the type is {@code null}
     */
    static List<? extends Element> getDeclaredMembers(TypeMirror type, boolean includeHierarchicalTypes) {
        return includeHierarchicalTypes ? getAllDeclaredMembers(type) : getDeclaredMembers(type);
    }

    /**
     * Returns the declared members of the provided {@link TypeElement}, optionally including
     * members from superclasses and interfaces.
     *
     * <p>If the given type is {@code null}, an empty list will be returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeElement typeElement = ...; // Obtain a valid TypeElement instance
     * boolean includeSuperMembers = true; // Include members from superclasses/interfaces
     * List<? extends Element> members = getDeclaredMembers(typeElement, includeSuperMembers);
     * for (Element member : members) {
     *     System.out.println("Member: " + member);
     * }
     * }</pre>
     *
     * @param type                     the type element to retrieve declared members from
     * @param includeHierarchicalTypes whether to include members from superclasses and interfaces
     * @return a list of declared members, or an empty list if the type is {@code null}
     */
    static List<? extends Element> getDeclaredMembers(TypeElement type, boolean includeHierarchicalTypes) {
        return includeHierarchicalTypes ? getAllDeclaredMembers(type) : getDeclaredMembers(type);
    }

    /**
     * Returns the declared members of the provided {@link TypeMirror}, optionally filtered by one or more predicates.
     *
     * <p>If the given type is {@code null}, an empty list will be returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror typeMirror = ...; // Obtain a valid TypeMirror instance
     * List<ExecutableElement> methods = findDeclaredMembers(typeMirror,
     *     element -> element.getKind() == ElementKind.METHOD,
     *     element -> element.getSimpleName().toString().startsWith("get")
     * );
     * for (Element method : methods) {
     *     System.out.println("Matching method: " + method);
     * }
     * }</pre>
     *
     * @param <T>           the type of elements to filter
     * @param type          the type mirror to retrieve declared members from
     * @param memberFilters the predicates used to filter members
     * @return a list of declared members matching the filters, or an empty list if the type is {@code null}
     */
    static <T extends Element> List<T> findDeclaredMembers(TypeMirror type, Predicate<? super T>... memberFilters) {
        return type == null ? emptyList() : findDeclaredMembers(ofTypeElement(type), memberFilters);
    }

    /**
     * Returns the directly declared members of the provided {@link TypeElement}, optionally filtered by one or more predicates.
     *
     * <p>If the given type is {@code null}, an empty list will be returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeElement typeElement = ...; // Obtain a valid TypeElement instance
     * List<VariableElement> fields = findDeclaredMembers(typeElement,
     *     element -> element.getKind() == ElementKind.FIELD,
     *     element -> element.getSimpleName().toString().startsWith("m_")
     * );
     * for (Element field : fields) {
     *     System.out.println("Matching field: " + field);
     * }
     * }</pre>
     *
     * @param <T>           the type of elements to filter
     * @param type          the type element to retrieve declared members from
     * @param memberFilters the predicates used to filter members
     * @return a list of declared members matching the filters, or an empty list if the type is {@code null}
     */
    static <T extends Element> List<T> findDeclaredMembers(TypeElement type, Predicate<? super T>... memberFilters) {
        if (type == null) {
            return emptyList();
        }
        return filterElements((List<T>) type.getEnclosedElements(), memberFilters);
    }

    /**
     * Returns all declared members (including those from superclasses and interfaces) of the provided {@link TypeMirror},
     * optionally filtered by one or more predicates.
     *
     * <p>If the given type is {@code null}, an empty list will be returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror typeMirror = ...; // Obtain a valid TypeMirror instance
     * List<ExecutableElement> methods = findAllDeclaredMembers(typeMirror,
     *     element -> element.getKind() == ElementKind.METHOD,
     *     element -> element.getSimpleName().toString().startsWith("get")
     * );
     * for (Element method : methods) {
     *     System.out.println("Matching method: " + method);
     * }
     * }</pre>
     *
     * @param <T>           the type of elements to filter
     * @param type          the type mirror to retrieve all declared members from
     * @param memberFilters the predicates used to filter members
     * @return a list of all declared members matching the filters, or an empty list if the type is {@code null}
     */
    static <T extends Element> List<T> findAllDeclaredMembers(TypeMirror type, Predicate<? super T>... memberFilters) {
        return type == null ? emptyList() : findAllDeclaredMembers(ofTypeElement(type), memberFilters);
    }

    /**
     * Retrieves all declared members (fields, methods, constructors, etc.) from the given {@link TypeElement},
     * including those inherited from superclasses and implemented interfaces.
     *
     * <p>This method collects all declared members by traversing the type hierarchy and applying
     * the provided filters to narrow down the results.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeElement typeElement = ...; // Obtain a valid TypeElement instance
     * List<VariableElement> fields = findAllDeclaredMembers(typeElement,
     *     element -> element.getKind() == ElementKind.FIELD
     * );
     * for (Element field : fields) {
     *     System.out.println("Field: " + field);
     * }
     * }</pre>
     *
     * <pre>{@code
     * List<ExecutableElement> methods = findAllDeclaredMembers(typeElement,
     *     element -> element.getKind() == ElementKind.METHOD,
     *     element -> element.getSimpleName().toString().startsWith("get")
     * );
     * for (Element method : methods) {
     *     System.out.println("Getter method: " + method);
     * }
     * }</pre>
     *
     * @param <T>           the type of elements to filter
     * @param type          the type element to retrieve all declared members from
     * @param memberFilters the predicates used to filter members
     * @return a list of all declared members matching the filters, or an empty list if the type is {@code null}
     */
    static <T extends Element> List<T> findAllDeclaredMembers(TypeElement type, Predicate<? super T>... memberFilters) {
        if (type == null) {
            return emptyList();
        }
        List<T> declaredMembers = (List<T>) getAllDeclaredTypes(type)
                .stream()
                .map(MemberUtils::getDeclaredMembers)
                .flatMap(Collection::stream)
                .collect(toList());
        return filterElements(declaredMembers, memberFilters);
    }

    /**
     * Returns the declared members of the provided {@link TypeMirror}, optionally including
     * members from superclasses and interfaces, and filtered by one or more predicates.
     *
     * <p>If the given type is {@code null}, an empty list will be returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror typeMirror = ...; // Obtain a valid TypeMirror instance
     * boolean includeSuperMembers = true; // Include members from superclasses/interfaces
     * List<ExecutableElement> methods = findDeclaredMembers(typeMirror, includeSuperMembers,
     *     element -> element.getKind() == ElementKind.METHOD,
     *     element -> element.getSimpleName().toString().startsWith("get")
     * );
     * for (Element method : methods) {
     *     System.out.println("Matching method: " + method);
     * }
     * }</pre>
     *
     * @param <T>                      the type of elements to filter
     * @param type                     the type mirror to retrieve declared members from
     * @param includeHierarchicalTypes whether to include members from superclasses and interfaces
     * @param memberFilters            the predicates used to filter members
     * @return a list of declared members matching the filters, or an empty list if the type is {@code null}
     */
    static <T extends Element> List<T> findDeclaredMembers(TypeMirror type, boolean includeHierarchicalTypes, Predicate<? super T>... memberFilters) {
        return includeHierarchicalTypes ? findAllDeclaredMembers(type, memberFilters) : findDeclaredMembers(type, memberFilters);
    }

    /**
     * Returns the declared members of the provided {@link TypeElement}, optionally including
     * members from superclasses and interfaces, and filtered by one or more predicates.
     *
     * <p>If the given type is {@code null}, an empty list will be returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeElement typeElement = ...; // Obtain a valid TypeElement instance
     * boolean includeSuperMembers = true; // Include members from superclasses/interfaces
     * List<VariableElement> fields = findDeclaredMembers(typeElement, includeSuperMembers,
     *     element -> element.getKind() == ElementKind.FIELD,
     *     element -> element.getSimpleName().toString().startsWith("m_")
     * );
     * for (Element field : fields) {
     *     System.out.println("Matching field: " + field);
     * }
     * }</pre>
     *
     * @param <T>           the type of elements to filter
     * @param type          the type element to retrieve declared members from
     * @param all           whether to include members from superclasses and interfaces
     * @param memberFilters the predicates used to filter members
     * @return a list of declared members matching the filters, or an empty list if the type is {@code null}
     */
    static <T extends Element> List<T> findDeclaredMembers(TypeElement type, boolean all, Predicate<? super T>... memberFilters) {
        return all ? findAllDeclaredMembers(type, memberFilters) : findDeclaredMembers(type, memberFilters);
    }

}
