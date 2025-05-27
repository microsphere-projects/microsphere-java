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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Predicate;

import static io.microsphere.annotation.processor.util.ElementUtils.filterElements;
import static io.microsphere.annotation.processor.util.ElementUtils.matchParameterTypes;
import static io.microsphere.annotation.processor.util.MemberUtils.getDeclaredMembers;
import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.collection.ListUtils.first;
import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static java.util.Collections.emptyList;
import static javax.lang.model.util.ElementFilter.constructorsIn;

/**
 * The utils class for {@link Constructor constructor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Constructor
 * @see ExecutableElement
 * @see ElementKind#CONSTRUCTOR
 * @since 1.0.0
 */
public interface ConstructorUtils extends Utils {

    /**
     * Retrieves the list of declared constructors from the provided {@link TypeElement}.
     * <p>
     * This method provides a null-safe way to obtain the constructors of a given type.
     * If the input {@code type} is {@code null}, an empty list is returned.
     * </p>
     *
     * @param type the {@link TypeElement} representing the type to retrieve constructors from
     * @return a {@link List} of {@link ExecutableElement} objects representing the declared constructors;
     * never {@code null}, but may be empty if no constructors are found or if the input is {@code null}
     */
    static List<ExecutableElement> getDeclaredConstructors(TypeElement type) {
        return type == null ? emptyList() : getDeclaredConstructors(type.asType());
    }

    /**
     * Retrieves the list of declared constructors from the provided {@link TypeMirror}.
     * <p>
     * This method provides a null-safe way to obtain the constructors of a given type.
     * If the input {@code type} is {@code null}, an empty list is returned.
     * </p>
     *
     * @param type the {@link TypeMirror} representing the type to retrieve constructors from
     * @return a {@link List} of {@link ExecutableElement} objects representing the declared constructors;
     * never {@code null}, but may be empty if no constructors are found or if the input is {@code null}
     */
    static List<ExecutableElement> getDeclaredConstructors(TypeMirror type) {
        return type == null ? emptyList() : findDeclaredConstructors(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Finds a declared constructor in the specified {@link TypeElement} that matches the given parameter types.
     * <p>
     * This method provides a null-safe way to locate a constructor based on its parameter types.
     * If the input {@code type} is {@code null}, or no matching constructor is found, {@code null} is returned.
     * </p>
     *
     * @param type           the {@link TypeElement} representing the type to search for constructors
     * @param parameterTypes the array of {@link Type} objects representing the parameter types to match
     * @return the matched {@link ExecutableElement} representing the constructor; may be {@code null}
     */
    static ExecutableElement findDeclaredConstructor(TypeElement type, Type... parameterTypes) {
        return type == null ? null : findDeclaredConstructor(type.asType(), parameterTypes);
    }

    /**
     * Finds a declared constructor in the specified {@link TypeMirror} that matches the given parameter types.
     * <p>
     * This method provides a null-safe way to locate a constructor based on its parameter types.
     * If the input {@code type} is {@code null}, or no matching constructor is found, {@code null} is returned.
     * </p>
     *
     * @param type           the {@link TypeMirror} representing the type to search for constructors
     * @param parameterTypes the array of {@link Type} objects representing the parameter types to match
     * @return the matched {@link ExecutableElement} representing the constructor; may be {@code null}
     */
    static ExecutableElement findDeclaredConstructor(TypeMirror type, Type... parameterTypes) {
        if (type == null) {
            return null;
        }
        return first(findDeclaredConstructors(type, constructor -> matchParameterTypes(constructor, parameterTypes)));
    }

    /**
     * Retrieves and filters the list of declared constructors from the provided {@link TypeElement}.
     * <p>
     * This method provides a null-safe way to obtain the constructors of a given type.
     * If the input {@code type} is {@code null}, an empty list is returned.
     * The provided filters can be used to selectively include only those constructors that match the criteria.
     * </p>
     *
     * @param type               the {@link TypeElement} representing the type to retrieve constructors from
     * @param constructorFilters optional predicates to filter the constructors; if none are provided, all constructors are included
     * @return a {@link List} of {@link ExecutableElement} objects representing the filtered declared constructors;
     * never {@code null}, but may be empty if no matching constructors are found or if the input is {@code null}
     */
    static List<ExecutableElement> findDeclaredConstructors(TypeElement type, Predicate<? super ExecutableElement>... constructorFilters) {
        return type == null ? emptyList() : findDeclaredConstructors(type.asType(), constructorFilters);
    }

    /**
     * Retrieves and filters the list of declared constructors from the provided {@link TypeMirror}.
     * <p>
     * This method provides a null-safe way to obtain the constructors of a given type.
     * If the input {@code type} is {@code null}, an empty list is returned.
     * The provided filters can be used to selectively include only those constructors that match the criteria.
     * </p>
     *
     * @param type               the {@link TypeMirror} representing the type to retrieve constructors from
     * @param constructorFilters optional predicates to filter the constructors; if none are provided, all constructors are included
     * @return a {@link List} of {@link ExecutableElement} objects representing the filtered declared constructors;
     * never {@code null}, but may be empty if no matching constructors are found or if the input is {@code null}
     */
    static List<ExecutableElement> findDeclaredConstructors(TypeMirror type, Predicate<? super ExecutableElement>... constructorFilters) {
        return filterDeclaredConstructors(type, constructorFilters);
    }

    /**
     * Filters and retrieves the list of declared constructors from the provided {@link TypeMirror}.
     * <p>
     * This method is responsible for extracting all declared constructors from the given type
     * and applying the specified filters to narrow down the results. If the input {@code type}
     * is {@code null}, an empty list is returned.
     * </p>
     *
     * @param type               the {@link TypeMirror} representing the type to retrieve constructors from
     * @param constructorFilters optional predicates to filter the constructors; if none are provided, all constructors are included
     * @return a {@link List} of {@link ExecutableElement} objects representing the filtered declared constructors;
     * never {@code null}, but may be empty if no matching constructors are found or if the input is {@code null}
     */
    static List<ExecutableElement> filterDeclaredConstructors(TypeMirror type, Predicate<? super ExecutableElement>... constructorFilters) {
        if (type == null) {
            return emptyList();
        }

        List<? extends Element> declaredMembers = getDeclaredMembers(type, false);
        if (isEmpty(declaredMembers)) {
            return emptyList();
        }

        List<ExecutableElement> constructors = constructorsIn(declaredMembers);

        return filterElements(constructors, constructorFilters);
    }

}
