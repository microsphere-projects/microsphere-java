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

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.microsphere.annotation.processor.util.MemberUtils.getDeclaredMembers;
import static io.microsphere.annotation.processor.util.MemberUtils.hasModifiers;
import static io.microsphere.annotation.processor.util.MemberUtils.matches;
import static io.microsphere.annotation.processor.util.TypeUtils.getHierarchicalTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.isEnumType;
import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static io.microsphere.lang.function.Streams.filterAll;
import static io.microsphere.lang.function.Streams.filterFirst;
import static java.util.Collections.emptyList;
import static javax.lang.model.element.ElementKind.ENUM_CONSTANT;
import static javax.lang.model.element.ElementKind.FIELD;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.util.ElementFilter.fieldsIn;

/**
 * The utilities class for the field in the package "javax.lang.model."
 *
 * @since 1.0.0
 */
public interface FieldUtils {

    static List<VariableElement> getDeclaredFields(Element element) {
        return findDeclaredFields(element, EMPTY_PREDICATE_ARRAY);
    }

    static List<VariableElement> getDeclaredFields(TypeMirror type) {
        return findDeclaredFields(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<VariableElement> getAllDeclaredFields(Element element) {
        return findAllDeclaredFields(element, EMPTY_PREDICATE_ARRAY);
    }

    static List<VariableElement> getAllDeclaredFields(TypeMirror type) {
        return findAllDeclaredFields(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<VariableElement> findDeclaredFields(Element element, Predicate<? super VariableElement>... fieldFilters) {
        return element == null ? emptyList() : findDeclaredFields(element.asType(), fieldFilters);
    }

    static List<VariableElement> findDeclaredFields(TypeMirror type, Predicate<? super VariableElement>... fieldFilters) {
        return filterAll(fieldsIn(getDeclaredMembers(type)), fieldFilters);
    }

    static List<VariableElement> findAllDeclaredFields(Element element, Predicate<? super VariableElement>... fieldFilters) {
        return element == null ? emptyList() : findAllDeclaredFields(element.asType(), fieldFilters);
    }

    static List<VariableElement> findAllDeclaredFields(TypeMirror type, Predicate<? super VariableElement>... fieldFilters) {
        return getHierarchicalTypes(type)
                .stream()
                .map(t -> findDeclaredFields(t, fieldFilters))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    static VariableElement getDeclaredField(Element element, String fieldName) {
        return element == null ? null : getDeclaredField(element.asType(), fieldName);
    }

    static VariableElement getDeclaredField(TypeMirror type, String fieldName) {
        return filterFirst(findDeclaredFields(type, field -> fieldName.equals(field.getSimpleName().toString())));
    }

    static VariableElement findField(Element element, String fieldName) {
        return element == null ? null : findField(element.asType(), fieldName);
    }

    static VariableElement findField(TypeMirror type, String fieldName) {
        return filterFirst(findAllDeclaredFields(type, field -> equals(field, fieldName)));
    }

    /**
     * is Enum's member field or not
     *
     * @param field {@link VariableElement} must be public static final fields
     * @return if field is public static final, return <code>true</code>, or <code>false</code>
     */
    static boolean isEnumMemberField(VariableElement field) {
        if (field == null || !isEnumType(field.getEnclosingElement())) {
            return false;
        }
        return ENUM_CONSTANT.equals(field.getKind());
    }

    static boolean isNonStaticField(VariableElement field) {
        return isField(field) && !hasModifiers(field, STATIC);
    }

    static boolean isField(VariableElement field) {
        return matches(field, FIELD) || isEnumMemberField(field);
    }

    static boolean isField(VariableElement field, Modifier... modifiers) {
        return isField(field) && hasModifiers(field, modifiers);
    }

    static List<VariableElement> getNonStaticFields(TypeMirror type) {
        return findDeclaredFields(type, FieldUtils::isNonStaticField);
    }

    static List<VariableElement> getNonStaticFields(Element element) {
        return element == null ? emptyList() : getNonStaticFields(element.asType());
    }

    static List<VariableElement> getAllNonStaticFields(TypeMirror type) {
        return findAllDeclaredFields(type, FieldUtils::isNonStaticField);
    }

    static List<VariableElement> getAllNonStaticFields(Element element) {
        return element == null ? emptyList() : getAllNonStaticFields(element.asType());
    }

    static boolean equals(VariableElement field, CharSequence fieldName) {
        return field != null && fieldName != null && field.getSimpleName().toString().equals(fieldName.toString());
    }

}
