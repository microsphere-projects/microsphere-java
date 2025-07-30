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
import io.microsphere.util.Utils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
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
 * A utility interface for working with fields in the context of Java annotation processing.
 * <p>
 * This interface provides a collection of static methods to retrieve, filter, and inspect
 * {@link VariableElement fields} within a class or interface. It supports operations such as:
 * </p>
 *
 * <ul>
 *     <li>Retrieving fields by name</li>
 *     <li>Finding fields with specific modifiers (e.g., static, public)</li>
 *     <li>Filtering fields based on custom predicates</li>
 *     <li>Checking if a field is an enum constant or a non-static field</li>
 *     <li>Getting all declared or non-static fields from a type or element</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Get a specific field by name
 * VariableElement field = FieldUtils.getDeclaredField(element, "myField");
 *
 * // Get all non-static fields
 * List<VariableElement> nonStaticFields = FieldUtils.getNonStaticFields(type);
 *
 * // Find a field by name in the current class and its superclasses
 * VariableElement fieldInHierarchy = FieldUtils.findField(type, "myField");
 *
 * // Check if a field is non-static
 * boolean isNonStatic = FieldUtils.isNonStaticField(field);
 *
 * // Get all fields, including those from superclasses
 * List<VariableElement> allFields = FieldUtils.getAllDeclaredFields(element);
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface FieldUtils extends Utils {

    /**
     * Retrieves the declared field with the specified name from the given element.
     *
     * <p>If the provided {@link Element} is null, this method returns null. It's typically used
     * to retrieve a specific field from a class or interface element.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Element element = ...; // A class or interface element
     * String fieldName = "myField";
     * VariableElement field = getDeclaredField(element, fieldName);
     * if (field != null) {
     *     System.out.println("Found field: " + field.getSimpleName());
     * } else {
     *     System.out.println("Field not found.");
     * }
     * }</pre>
     *
     * @param element   the element to search for the field; if null, null is returned
     * @param fieldName the name of the field to find
     * @return the VariableElement representing the declared field, or null if not found
     */
    @Nullable
    static VariableElement getDeclaredField(Element element, String fieldName) {
        return element == null ? null : getDeclaredField(element.asType(), fieldName);
    }

    /**
     * Retrieves the declared field with the specified name from the given type.
     *
     * <p>If the provided {@link TypeMirror} is null, this method returns null.
     * It searches for a field with the exact specified name in the given type,
     * and returns the first match found.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror type = element.asType(); // A valid type from an element
     * String fieldName = "myField";
     * VariableElement field = getDeclaredField(type, fieldName);
     * if (field != null) {
     *     System.out.println("Found field: " + field.getSimpleName());
     * } else {
     *     System.out.println("Field not found.");
     * }
     * }</pre>
     *
     * @param type      the type to search for the field; if null, null is returned
     * @param fieldName the name of the field to find
     * @return the VariableElement representing the declared field, or null if not found
     */
    @Nullable
    static VariableElement getDeclaredField(TypeMirror type, String fieldName) {
        return filterFirst(findDeclaredFields(type, field -> fieldName.equals(field.getSimpleName().toString())));
    }

    /**
     * Retrieves all declared fields from the given element without any filtering.
     *
     * <p>If the provided {@link Element} is null, this method returns an empty list.
     * It's typically used to get all fields declared in a class or interface,
     * excluding fields from superclasses or interfaces.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Element element = ...; // A class or interface element
     * List<VariableElement> fields = getDeclaredFields(element);
     * for (VariableElement field : fields) {
     *     System.out.println("Field: " + field.getSimpleName());
     * }
     * }</pre>
     *
     * @param element the element to retrieve declared fields from
     * @return a list of VariableElement objects representing the declared fields,
     * or an empty list if the element is null or no fields are found
     */
    @Nonnull
    @Immutable
    static List<VariableElement> getDeclaredFields(Element element) {
        return findDeclaredFields(element, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves all declared fields from the given type without any filtering.
     *
     * <p>If the provided {@link TypeMirror} is null, this method returns an empty list.
     * It's typically used to get all fields declared directly within a class or interface,
     * excluding fields from superclasses or interfaces.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror type = element.asType(); // A valid type from an element
     * List<VariableElement> fields = getDeclaredFields(type);
     * for (VariableElement field : fields) {
     *     System.out.println("Declared field: " + field.getSimpleName());
     * }
     * }</pre>
     *
     * @param type the type to retrieve declared fields from
     * @return a list of VariableElement objects representing the declared fields,
     * or an empty list if the type is null or no fields are found
     */
    @Nonnull
    @Immutable
    static List<VariableElement> getDeclaredFields(TypeMirror type) {
        return findDeclaredFields(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves all declared fields from the given element, including those from hierarchical types (e.g., superclasses),
     * without any filtering.
     *
     * <p>If the provided {@link Element} is null, this method returns an empty list. It's typically used
     * to get all fields declared directly within a class or interface, as well as those inherited from superclasses.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Element element = ...; // A class or interface element
     * List<VariableElement> fields = getAllDeclaredFields(element);
     * for (VariableElement field : fields) {
     *     System.out.println("Declared field (including hierarchical): " + field.getSimpleName());
     * }
     * }</pre>
     *
     * @param element the element to retrieve all declared fields from
     * @return a list of VariableElement objects representing all declared fields,
     * or an empty list if the element is null or no fields are found
     */
    @Nonnull
    @Immutable
    static List<VariableElement> getAllDeclaredFields(Element element) {
        return findAllDeclaredFields(element, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves all declared fields from the given type, including those from hierarchical types (e.g., superclasses),
     * without any filtering.
     *
     * <p>If the provided {@link TypeMirror} is null, this method returns an empty list. It's typically used
     * to get all fields declared directly within a class or interface, as well as those inherited from superclasses.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror type = element.asType(); // A valid type from an element
     * List<VariableElement> fields = getAllDeclaredFields(type);
     * for (VariableElement field : fields) {
     *     System.out.println("Declared field (including hierarchical): " + field.getSimpleName());
     * }
     * }</pre>
     *
     * @param type the type to retrieve all declared fields from
     * @return a list of VariableElement objects representing all declared fields,
     * or an empty list if the type is null or no fields are found
     */
    @Nonnull
    @Immutable
    static List<VariableElement> getAllDeclaredFields(TypeMirror type) {
        return findAllDeclaredFields(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves the first matching field with the specified name from the given element.
     *
     * <p>If the provided {@link Element} is null, this method returns null.
     * It searches for a field with the exact specified name in the given element
     * and returns the first match found.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Element element = ...; // A valid class or interface element
     * String fieldName = "myField";
     * VariableElement field = findField(element, fieldName);
     * if (field != null) {
     *     System.out.println("Found field: " + field.getSimpleName());
     * } else {
     *     System.out.println("Field not found.");
     * }
     * }</pre>
     *
     * @param element   the element to search for the field; if null, null is returned
     * @param fieldName the name of the field to find
     * @return the VariableElement representing the first matching field, or null if not found
     */
    @Nullable
    static VariableElement findField(Element element, String fieldName) {
        return element == null ? null : findField(element.asType(), fieldName);
    }

    /**
     * Retrieves the first matching field with the specified name from the given type.
     *
     * <p>This method searches for a field with the exact specified name in the given type,
     * including all hierarchical types (e.g., superclasses), and returns the first match found.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror type = element.asType(); // A valid type from an element
     * String fieldName = "myField";
     * VariableElement field = findField(type, fieldName);
     * if (field != null) {
     *     System.out.println("Found field: " + field.getSimpleName());
     * } else {
     *     System.out.println("Field not found.");
     * }
     * }</pre>
     *
     * @param type      the type to search for fields; if null, null is returned
     * @param fieldName the name of the field to find
     * @return the VariableElement representing the first matching field, or null if not found
     */
    @Nullable
    static VariableElement findField(TypeMirror type, String fieldName) {
        return filterFirst(findAllDeclaredFields(type, field -> equalsFieldName(field, fieldName)));
    }

    /**
     * Retrieves the declared fields from the given element after applying the provided filters.
     *
     * <p>If the provided {@link Element} is null, this method returns an empty list. It searches
     * for fields directly declared in the given element (excluding fields from superclasses or interfaces)
     * and applies the specified filters to narrow down the results.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Element element = ...; // A class or interface element
     * List<VariableElement> allFields = findDeclaredFields(element); // Get all declared fields
     *
     * // Get only non-static fields
     * List<VariableElement> nonStaticFields = findDeclaredFields(element, FieldUtils::isNonStaticField);
     *
     * // Get fields matching a specific name
     * String fieldName = "myField";
     * List<VariableElement> matchingFields = findDeclaredFields(element, field -> fieldName.equals(field.getSimpleName().toString()));
     *
     * // Get fields with multiple filters (e.g., non-static and public)
     * List<VariableElement> publicNonStaticFields = findDeclaredFields(element,
     *     field -> field.getModifiers().contains(Modifier.PUBLIC),
     *     FieldUtils::isNonStaticField
     * );
     * }</pre>
     *
     * @param element      the element to retrieve declared fields from; if null, an empty list is returned
     * @param fieldFilters the predicates used to filter the fields; optional
     * @return a list of VariableElement objects representing the filtered declared fields
     */
    @Nonnull
    @Immutable
    static List<VariableElement> findDeclaredFields(Element element, Predicate<? super VariableElement>... fieldFilters) {
        return element == null ? emptyList() : findDeclaredFields(element.asType(), fieldFilters);
    }

    /**
     * Retrieves the declared fields from the given type after applying the provided filters.
     *
     * <p>This method searches for fields directly declared in the given type,
     * excluding fields from superclasses or interfaces. It allows filtering
     * the fields using the provided predicates to narrow down the results.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror type = element.asType(); // A valid type from an element
     *
     * // Get all declared fields
     * List<VariableElement> allFields = findDeclaredFields(type);
     *
     * // Get only non-static fields
     * List<VariableElement> nonStaticFields = findDeclaredFields(type, FieldUtils::isNonStaticField);
     *
     * // Get fields matching a specific name
     * String fieldName = "myField";
     * List<VariableElement> matchingFields = findDeclaredFields(type, field -> "myField".equals(field.getSimpleName().toString()));
     *
     * // Get fields with multiple filters (e.g., non-static and public)
     * List<VariableElement> publicNonStaticFields = findDeclaredFields(type,
     *     field -> field.getModifiers().contains(Modifier.PUBLIC),
     *     FieldUtils::isNonStaticField
     * );
     * }</pre>
     *
     * @param type         the type to retrieve declared fields from; if null, an empty list is returned
     * @param fieldFilters the predicates used to filter the fields
     * @return a list of VariableElement objects representing the filtered declared fields
     */
    @Nonnull
    @Immutable
    static List<VariableElement> findDeclaredFields(TypeMirror type, Predicate<? super VariableElement>... fieldFilters) {
        return filterDeclaredFields(type, false, fieldFilters);
    }

    /**
     * Retrieves all declared fields from the given element, including those from hierarchical types (e.g., superclasses),
     * after applying the provided filters.
     *
     * <p>This method processes the given element and searches for all fields declared directly within it
     * as well as those inherited from superclasses. The fields can be filtered using one or more predicates
     * to narrow down the results.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Element element = ...; // A valid class or interface element
     *
     * // Get all declared fields (including from superclasses)
     * List<VariableElement> allFields = findAllDeclaredFields(element);
     *
     * // Get only non-static fields
     * List<VariableElement> nonStaticFields = findAllDeclaredFields(element, FieldUtils::isNonStaticField);
     *
     * // Get fields matching a specific name
     * String fieldName = "myField";
     * List<VariableElement> matchingFields = findAllDeclaredFields(element, field -> "myField".equals(field.getSimpleName().toString()));
     *
     * // Get fields with multiple filters (e.g., non-static and public)
     * List<VariableElement> publicNonStaticFields = findAllDeclaredFields(element,
     *     field -> field.getModifiers().contains(Modifier.PUBLIC),
     *     FieldUtils::isNonStaticField
     * );
     * }</pre>
     *
     * @param element      the element to retrieve all declared fields from; if null, an empty list is returned
     * @param fieldFilters the predicates used to filter the fields; optional
     * @return a list of VariableElement objects representing the filtered declared fields
     */
    @Nonnull
    @Immutable
    static List<VariableElement> findAllDeclaredFields(Element element, Predicate<? super VariableElement>... fieldFilters) {
        return element == null ? emptyList() : findAllDeclaredFields(element.asType(), fieldFilters);
    }

    /**
     * Retrieves all declared fields from the given type, including those from hierarchical types (e.g., superclasses),
     * after applying the provided filters.
     *
     * <p>This method searches for fields directly declared in the given type as well as those inherited
     * from superclasses. It allows filtering the fields using the provided predicates to narrow down the results.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror type = element.asType(); // A valid type from an element
     *
     * // Get all declared fields (including hierarchical)
     * List<VariableElement> allFields = findAllDeclaredFields(type);
     *
     * // Get only non-static fields
     * List<VariableElement> nonStaticFields = findAllDeclaredFields(type, FieldUtils::isNonStaticField);
     *
     * // Get fields matching a specific name
     * String fieldName = "myField";
     * List<VariableElement> matchingFields = findAllDeclaredFields(type, field -> "myField".equals(field.getSimpleName().toString()));
     *
     * // Get fields with multiple filters (e.g., non-static and public)
     * List<VariableElement> publicNonStaticFields = findAllDeclaredFields(type,
     *     field -> field.getModifiers().contains(Modifier.PUBLIC),
     *     FieldUtils::isNonStaticField
     * );
     * }</pre>
     *
     * @param type         the type to retrieve all declared fields from; if null, an empty list is returned
     * @param fieldFilters the predicates used to filter the fields
     * @return a list of VariableElement objects representing the filtered declared fields
     */
    @Nonnull
    @Immutable
    static List<VariableElement> findAllDeclaredFields(TypeMirror type, Predicate<? super VariableElement>... fieldFilters) {
        return filterDeclaredFields(type, true, fieldFilters);
    }

    /**
     * Filters and retrieves the declared fields from the given type based on the provided criteria.
     *
     * <p>This method is used to retrieve fields declared in the given type, optionally including fields
     * from its superclasses or interfaces. The fields can be further filtered using one or more predicates
     * to narrow down the results.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror type = element.asType(); // A valid type from an element
     *
     * // Get all declared fields (excluding hierarchical)
     * List<VariableElement> declaredFields = filterDeclaredFields(type, false);
     *
     * // Get all declared fields including hierarchical ones
     * List<VariableElement> allFields = filterDeclaredFields(type, true);
     *
     * // Get only non-static fields
     * List<VariableElement> nonStaticFields = filterDeclaredFields(type, false, FieldUtils::isNonStaticField);
     *
     * // Get fields matching a specific name
     * String fieldName = "myField";
     * List<VariableElement> matchingFields = filterDeclaredFields(type, false,
     *     field -> "myField".equals(field.getSimpleName().toString()));
     *
     * // Get fields with multiple filters (e.g., non-static and public)
     * List<VariableElement> publicNonStaticFields = filterDeclaredFields(type, true,
     *     field -> field.getModifiers().contains(Modifier.PUBLIC),
     *     FieldUtils::isNonStaticField
     * );
     * }</pre>
     *
     * @param type                     the type to retrieve declared fields from; if null, an empty list is returned
     * @param includeHierarchicalTypes whether to include fields from hierarchical types (e.g., superclasses)
     * @param fieldFilters             the predicates used to filter the fields; optional
     * @return a list of VariableElement objects representing the filtered declared fields
     */
    @Nonnull
    @Immutable
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
     * Determines whether the given field is an enum member field.
     *
     * <p>An enum member field is typically a public static final field that represents
     * a constant within an enum declaration. This method checks if the field's enclosing
     * element is an enum and if the field's kind matches {@link ElementKind#ENUM_CONSTANT}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * VariableElement field = ...; // A valid field element
     * boolean isEnumMember = FieldUtils.isEnumMemberField(field);
     * if (isEnumMember) {
     *     System.out.println("The field is an enum member field.");
     * } else {
     *     System.out.println("The field is not an enum member field.");
     * }
     * }</pre>
     *
     * @param field the field to check; may be null
     * @return true if the field is an enum member field, false otherwise
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
     * <p>This method verifies whether the provided {@link VariableElement} represents a field
     * that is not declared with the {@code static} modifier. It first checks if the element is
     * a valid field (including enum constants) using the {@link #isField(VariableElement)} method,
     * and then ensures that the field does not have the static modifier.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * VariableElement field = ...; // A valid field element
     * boolean isNonStatic = FieldUtils.isNonStaticField(field);
     * if (isNonStatic) {
     *     System.out.println("The field is non-static.");
     * } else {
     *     System.out.println("The field is static or not a valid field.");
     * }
     * }</pre>
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
     * <p>This method determines whether the provided {@link VariableElement} represents a valid field
     * or an enum constant. It returns {@code true} if the element's kind is either
     * {@link ElementKind#FIELD} or {@link ElementKind#ENUM_CONSTANT}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * VariableElement field = ...; // A valid field element
     * boolean isValidField = FieldUtils.isField(field);
     * if (isValidField) {
     *     System.out.println("The element is a valid field or enum constant.");
     * } else {
     *     System.out.println("The element is not a field or enum constant.");
     * }
     * }</pre>
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
     * <p>This method extends the {@link #isField(VariableElement)} method by additionally verifying that
     * the field has all of the specified modifiers. It returns {@code true} only if the element is a valid field
     * (including enum constants) and contains all the provided modifiers.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * VariableElement field = ...; // A valid field element
     *
     * // Check if it's a public static field
     * boolean isPublicStaticField = FieldUtils.isField(field, Modifier.PUBLIC, Modifier.STATIC);
     * if (isPublicStaticField) {
     *     System.out.println("The field is a public static field.");
     * } else {
     *     System.out.println("The field is not a public static field.");
     * }
     *
     * // Check if it's a private field
     * boolean isPrivateField = FieldUtils.isField(field, Modifier.PRIVATE);
     * if (isPrivateField) {
     *     System.out.println("The field is a private field.");
     * } else {
     *     System.out.println("The field is not a private field.");
     * }
     * }</pre>
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
     * <p>This method returns a list of fields that are declared directly within the given type
     * and are not marked as static. It does not include fields from superclasses or interfaces.
     * If the provided {@link TypeMirror} is null, this method returns an empty list.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror type = element.asType(); // A valid type from an element
     * List<VariableElement> nonStaticFields = FieldUtils.getNonStaticFields(type);
     * for (VariableElement field : nonStaticFields) {
     *     System.out.println("Non-static field: " + field.getSimpleName());
     * }
     * }</pre>
     *
     * @param type the type to search for non-static fields; if null, an empty list is returned
     * @return a list of VariableElement objects representing the non-static fields,
     * or an empty list if the type is null or no non-static fields are found
     */
    @Nonnull
    @Immutable
    static List<VariableElement> getNonStaticFields(TypeMirror type) {
        return findDeclaredFields(type, FieldUtils::isNonStaticField);
    }

    /**
     * Retrieves all declared non-static fields from the given element.
     *
     * <p>This method processes the provided element and retrieves all fields directly declared
     * within it that are not marked as static. It excludes fields from superclasses or interfaces.
     * If the provided element is null, this method returns an empty list.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Element element = ...; // A valid class or interface element
     * List<VariableElement> nonStaticFields = FieldUtils.getNonStaticFields(element);
     * for (VariableElement field : nonStaticFields) {
     *     System.out.println("Non-static field: " + field.getSimpleName());
     * }
     *
     * // Handling null case
     * List<VariableElement> safeList = FieldUtils.getNonStaticFields(null);
     * System.out.println(safeList.isEmpty()); // true
     * }</pre>
     *
     * @param element the element to search for non-static fields; if null, an empty list is returned
     * @return a list of VariableElement objects representing the non-static fields,
     * or an empty list if the element is null or no non-static fields are found
     */
    @Nonnull
    @Immutable
    static List<VariableElement> getNonStaticFields(Element element) {
        return element == null ? emptyList() : getNonStaticFields(element.asType());
    }

    /**
     * Retrieves all non-static fields from the given type, including those from hierarchical types (e.g., superclasses).
     *
     * <p>This method searches for all fields declared directly within the given type, as well as those inherited
     * from its superclasses or interfaces, and filters out only the non-static fields. If the provided {@link TypeMirror}
     * is null, this method returns an empty list.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror type = element.asType(); // A valid type from an element
     * List<VariableElement> nonStaticFields = FieldUtils.getAllNonStaticFields(type);
     * for (VariableElement field : nonStaticFields) {
     *     System.out.println("Non-static field (including hierarchical): " + field.getSimpleName());
     * }
     *
     * // Handling null case
     * List<VariableElement> safeList = FieldUtils.getAllNonStaticFields(null);
     * System.out.println(safeList.isEmpty()); // true
     * }</pre>
     *
     * @param type the type to retrieve all non-static fields from; if null, an empty list is returned
     * @return a list of VariableElement objects representing all non-static fields,
     * or an empty list if the type is null or no non-static fields are found
     */
    @Nonnull
    @Immutable
    static List<VariableElement> getAllNonStaticFields(TypeMirror type) {
        return findAllDeclaredFields(type, FieldUtils::isNonStaticField);
    }

    /**
     * Retrieves all non-static fields from the given element, including those from superclasses and interfaces.
     *
     * <p>This method processes the provided element and retrieves all fields declared directly within it,
     * as well as those inherited from superclasses or interfaces. It filters out only the non-static fields.
     * If the provided element is null, this method returns an empty list.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Element element = ...; // A valid class or interface element
     * List<VariableElement> nonStaticFields = FieldUtils.getAllNonStaticFields(element);
     * for (VariableElement field : nonStaticFields) {
     *     System.out.println("Non-static field: " + field.getSimpleName());
     * }
     *
     * // Handling null case
     * List<VariableElement> safeList = FieldUtils.getAllNonStaticFields(null);
     * System.out.println(safeList.isEmpty()); // true
     * }</pre>
     *
     * @param element the element to retrieve all non-static fields from; if null, an empty list is returned
     * @return a list of VariableElement objects representing all non-static fields,
     * or an empty list if the element is null or no non-static fields are found
     */
    @Nonnull
    @Immutable
    static List<VariableElement> getAllNonStaticFields(Element element) {
        return element == null ? emptyList() : getAllNonStaticFields(element.asType());
    }

    /**
     * Checks if the simple name of the given field matches the specified field name.
     *
     * <p>This method ensures both the {@link VariableElement} and the field name are non-null
     * before comparing their string representations for equality.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * VariableElement field = ...; // A valid field element
     * CharSequence fieldName = "myField";
     * boolean isMatch = equalsFieldName(field, fieldName);
     * if (isMatch) {
     *     System.out.println("Field name matches: " + fieldName);
     * } else {
     *     System.out.println("Field name does not match.");
     * }
     * }</pre>
     *
     * @param field     the VariableElement representing the field; may be null
     * @param fieldName the CharSequence representing the expected field name; may be null
     * @return true if both the field and fieldName are non-null and their string representations match, false otherwise
     */
    static boolean equalsFieldName(VariableElement field, CharSequence fieldName) {
        return field != null && fieldName != null && field.getSimpleName().toString().equals(fieldName.toString());
    }
}
