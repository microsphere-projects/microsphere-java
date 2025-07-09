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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.function.Predicate;

import static io.microsphere.annotation.processor.util.ElementUtils.filterElements;
import static io.microsphere.annotation.processor.util.ElementUtils.hasModifiers;
import static io.microsphere.annotation.processor.util.ElementUtils.matchesElementKind;
import static io.microsphere.annotation.processor.util.MemberUtils.getDeclaredMembers;
import static io.microsphere.annotation.processor.util.TypeUtils.isEnumType;
import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static io.microsphere.lang.function.Streams.filterFirst;
import static java.util.Collections.emptyList;
import static javax.lang.model.element.ElementKind.ENUM_CONSTANT;
import static javax.lang.model.element.ElementKind.FIELD;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.util.ElementFilter.fieldsIn;

/**
 * The utilities class for the field in the package "javax.lang.model."
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface FieldUtils extends Utils {

    /**
     * Retrieves the declared field with the specified name from the given element.
     *
     * @param element   the element to search for the field; if null, null is returned
     * @param fieldName the name of the field to find
     * @return the VariableElement representing the declared field, or null if not found
     */
    static VariableElement getDeclaredField(Element element, String fieldName) {
        return element == null ? null : getDeclaredField(element.asType(), fieldName);
    }

    /**
     * Retrieves the declared field with the specified name from the given type.
     *
     * @param type      the type to search for the field; if null, null is returned
     * @param fieldName the name of the field to find
     * @return the VariableElement representing the declared field, or null if not found
     */
    static VariableElement getDeclaredField(TypeMirror type, String fieldName) {
        return filterFirst(findDeclaredFields(type, field -> fieldName.equals(field.getSimpleName().toString())));
    }

    /**
     * Retrieves all declared fields from the given element without any filtering.
     *
     * @param element the element to retrieve declared fields from
     * @return a list of VariableElement objects representing the declared fields,
     * or an empty list if the element is null or no fields are found
     */
    static List<VariableElement> getDeclaredFields(Element element) {
        return findDeclaredFields(element, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves all declared fields from the given type without any filtering.
     *
     * @param type the type to retrieve declared fields from
     * @return a list of VariableElement objects representing the declared fields,
     * or an empty list if the type is null or no fields are found
     * @see #findDeclaredFields(TypeMirror, Predicate[])
     */
    static List<VariableElement> getDeclaredFields(TypeMirror type) {
        return findDeclaredFields(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves all declared fields from the given element, including those from hierarchical types,
     * without any filtering.
     *
     * @param element the element to retrieve all declared fields from
     * @return a list of VariableElement objects representing all declared fields,
     * or an empty list if the element is null or no fields are found
     */
    static List<VariableElement> getAllDeclaredFields(Element element) {
        return findAllDeclaredFields(element, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves all declared fields from the given type, including those from hierarchical types,
     * without any filtering.
     *
     * @param type the type to retrieve all declared fields from
     * @return a list of VariableElement objects representing all declared fields,
     * or an empty list if the type is null or no fields are found
     */
    static List<VariableElement> getAllDeclaredFields(TypeMirror type) {
        return findAllDeclaredFields(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves a field with the specified name from the given element.
     *
     * @param element   the element to search for the field; if null, null is returned
     * @param fieldName the name of the field to find
     * @return the VariableElement representing the field, or null if not found
     */
    static VariableElement findField(Element element, String fieldName) {
        return element == null ? null : findField(element.asType(), fieldName);
    }

    /**
     * Retrieves the first matching field with the specified name from the given type.
     *
     * @param type      the type to search for fields; if null, null is returned
     * @param fieldName the name of the field to find
     * @return the VariableElement representing the first matching field, or null if not found
     */
    static VariableElement findField(TypeMirror type, String fieldName) {
        return filterFirst(findAllDeclaredFields(type, field -> equalsFieldName(field, fieldName)));
    }

    /**
     * Retrieves the declared fields from the given element after applying the provided filters.
     *
     * @param element      the element to retrieve declared fields from; if null, an empty list is returned
     * @param fieldFilters the predicates used to filter the fields
     * @return a list of VariableElement objects representing the filtered declared fields
     */
    static List<VariableElement> findDeclaredFields(Element element, Predicate<? super VariableElement>... fieldFilters) {
        return element == null ? emptyList() : findDeclaredFields(element.asType(), fieldFilters);
    }

    /**
     * Retrieves the declared fields from the given type after applying the provided filters.
     *
     * @param type         the type to retrieve declared fields from; if null, an empty list is returned
     * @param fieldFilters the predicates used to filter the fields
     * @return a list of VariableElement objects representing the filtered declared fields
     */
    static List<VariableElement> findDeclaredFields(TypeMirror type, Predicate<? super VariableElement>... fieldFilters) {
        return filterDeclaredFields(type, false, fieldFilters);
    }

    /**
     * Retrieves all declared fields from the given element, including those from hierarchical types,
     * after applying the provided filters.
     *
     * @param element      the element to retrieve all declared fields from; if null, an empty list is returned
     * @param fieldFilters the predicates used to filter the fields
     * @return a list of VariableElement objects representing the filtered declared fields
     */
    static List<VariableElement> findAllDeclaredFields(Element element, Predicate<? super VariableElement>... fieldFilters) {
        return element == null ? emptyList() : findAllDeclaredFields(element.asType(), fieldFilters);
    }

    /**
     * Retrieves all declared fields from the given type, including those from hierarchical types,
     * after applying the provided filters.
     *
     * @param type         the type to retrieve all declared fields from; if null, an empty list is returned
     * @param fieldFilters the predicates used to filter the fields
     * @return a list of VariableElement objects representing the filtered declared fields
     */
    static List<VariableElement> findAllDeclaredFields(TypeMirror type, Predicate<? super VariableElement>... fieldFilters) {
        return filterDeclaredFields(type, true, fieldFilters);
    }

    /**
     * Filters and retrieves the declared fields from the given type based on the provided criteria.
     *
     * @param type                     the type to retrieve declared fields from; if null, an empty list is returned
     * @param includeHierarchicalTypes whether to include fields from hierarchical types (e.g., superclasses)
     * @param fieldFilters             the predicates used to filter the fields; optional
     * @return a list of VariableElement objects representing the filtered declared fields
     */
    static List<VariableElement> filterDeclaredFields(TypeMirror type, boolean includeHierarchicalTypes, Predicate<? super VariableElement>... fieldFilters) {
        if (type == null) {
            return emptyList();
        }

        List<? extends Element> declaredMembers = getDeclaredMembers(type, includeHierarchicalTypes);
        if (isEmpty(declaredMembers)) {
            return emptyList();
        }

        List<VariableElement> fields = fieldsIn(declaredMembers);

        return filterElements(fields, fieldFilters);
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

    /**
     * Checks if the given field is a non-static field.
     *
     * @param field the VariableElement to check; may be null
     * @return true if the field is a valid field (as per {@link #isField(VariableElement)})
     * and does not have the 'static' modifier, false otherwise
     */
    static boolean isNonStaticField(VariableElement field) {
        return isField(field) && !hasModifiers(field, STATIC);
    }

    /**
     * Checks if the given element is a field or an enum constant.
     *
     * @param field the VariableElement to check; may be null
     * @return true if the element is a field ({@link javax.lang.model.element.ElementKind#FIELD})
     * or an enum constant ({@link javax.lang.model.element.ElementKind#ENUM_CONSTANT}), false otherwise
     */
    static boolean isField(VariableElement field) {
        return matchesElementKind(field, FIELD) || isEnumMemberField(field);
    }

    /**
     * Checks if the given element is a field or an enum constant, and also has all the specified modifiers.
     *
     * @param field     the VariableElement to check; may be null
     * @param modifiers the modifiers to match (e.g., Modifier.PUBLIC, Modifier.STATIC)
     * @return true if the element is a field ({@link javax.lang.model.element.ElementKind#FIELD})
     * or an enum constant ({@link javax.lang.model.element.ElementKind#ENUM_CONSTANT}),
     * and it has all of the specified modifiers, false otherwise
     */
    static boolean isField(VariableElement field, Modifier... modifiers) {
        return isField(field) && hasModifiers(field, modifiers);
    }

    /**
     * Retrieves all declared non-static fields from the given type.
     *
     * @param type the type to search for non-static fields; if null, an empty list is returned
     * @return a list of VariableElement objects representing the non-static fields,
     * or an empty list if the type is null or no non-static fields are found
     */
    static List<VariableElement> getNonStaticFields(TypeMirror type) {
        return findDeclaredFields(type, FieldUtils::isNonStaticField);
    }

    /**
     * Retrieves all declared non-static fields from the given element.
     *
     * @param element the element to search for non-static fields; if null, an empty list is returned
     * @return a list of VariableElement objects representing the non-static fields,
     * or an empty list if the element is null or no non-static fields are found
     */
    static List<VariableElement> getNonStaticFields(Element element) {
        return element == null ? emptyList() : getNonStaticFields(element.asType());
    }

    /**
     * Retrieves all non-static fields from the given type, including those from hierarchical types.
     *
     * @param type the type to search for non-static fields; if null, an empty list is returned
     * @return a list of VariableElement objects representing all non-static fields,
     * or an empty list if the type is null or no non-static fields are found
     */
    static List<VariableElement> getAllNonStaticFields(TypeMirror type) {
        return findAllDeclaredFields(type, FieldUtils::isNonStaticField);
    }

    /**
     * Retrieves all non-static fields from the given element, including those from hierarchical types.
     *
     * @param element the element to retrieve all non-static fields from; if null, an empty list is returned
     * @return a list of VariableElement objects representing all non-static fields,
     * or an empty list if the element is null or no non-static fields are found
     */
    static List<VariableElement> getAllNonStaticFields(Element element) {
        return element == null ? emptyList() : getAllNonStaticFields(element.asType());
    }

    /**
     * Checks if the simple name of the given field matches the specified field name.
     *
     * @param field     the VariableElement representing the field; may be null
     * @param fieldName the CharSequence representing the expected field name; may be null
     * @return true if both the field and fieldName are non-null and their string representations match, false otherwise
     */
    static boolean equalsFieldName(VariableElement field, CharSequence fieldName) {
        return field != null && fieldName != null && field.getSimpleName().toString().equals(fieldName.toString());
    }
}
