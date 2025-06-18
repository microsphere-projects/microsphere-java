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

import io.microsphere.util.TypeFinder;
import io.microsphere.util.Utils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.constants.SymbolConstants.GREATER_THAN_CHAR;
import static io.microsphere.constants.SymbolConstants.LESS_THAN_CHAR;
import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.lang.function.Streams.filterFirst;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.reflect.TypeUtils.getTypeNames;
import static io.microsphere.util.ArrayUtils.contains;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static io.microsphere.util.Assert.assertNoNullElements;
import static io.microsphere.util.ClassUtils.SIMPLE_TYPES;
import static java.lang.String.valueOf;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static javax.lang.model.element.ElementKind.ANNOTATION_TYPE;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.ENUM;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.type.TypeKind.ARRAY;

/**
 * The utilities class for type in the package "javax.lang.model.*"
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface TypeUtils extends Utils {

    /**
     * A list of names representing simple types in Java.
     * Simple types include primitive types, void, and commonly used basic classes like String, Number, etc.
     */
    List<String> SIMPLE_TYPE_NAMES = ofList(
            SIMPLE_TYPES
                    .stream()
                    .map(Class::getName)
                    .toArray(String[]::new)
    );

    /**
     * Get the superclass of the specified type
     */
    Function<TypeElement, TypeElement> TYPE_ELEMENT_GET_SUPERCLASS = type -> ofTypeElement(type.getSuperclass());

    /**
     * Get the interfaces of the specified type
     */
    Function<TypeElement, TypeElement[]> TYPE_ELEMENT_GET_INTERFACES = type -> type.getInterfaces()
            .stream()
            .map(TypeUtils::ofTypeElement)
            .toArray(TypeElement[]::new);


    /**
     * Check if the given element is a simple type.
     * A simple type is defined as a basic data type that can be directly represented without further resolution.
     *
     * @param element the element to check
     * @return true if the element is a simple type, false otherwise
     */
    static boolean isSimpleType(Element element) {
        return element != null && isSimpleType(element.asType());
    }

    /**
     * Checks whether the given {@link TypeMirror} represents a simple type.
     * A simple type is one that can be directly represented without further resolution,
     * such as primitive types, built-in types like void, or commonly recognized basic types.
     *
     * @param type the TypeMirror to check, may be null
     * @return true if the type is a simple type; false otherwise
     */
    static boolean isSimpleType(TypeMirror type) {
        return type != null && SIMPLE_TYPE_NAMES.contains(type.toString());
    }

    /**
     * Checks if the given Element and Type represent the same type.
     * 
     * @param element the Element to compare, may be null
     * @param type    the Type to compare, may be null
     * @return true if both represent the same type; false otherwise
     */
    static boolean isSameType(Element element, Type type) {
        return isSameType(element == null ? null : element.asType(), type);
    }

    /**
     * Checks if the given Element and type name represent the same type.
     *
     * @param type      the Element to compare, may be null
     * @param typeName  the fully qualified class name to compare, may be null
     * @return true if both represent the same type; false otherwise
     */
    static boolean isSameType(Element type, CharSequence typeName) {
        return isSameType(type == null ? null : type.asType(), typeName);
    }

    /**
     * Checks if the given TypeMirror and Type represent the same type.
     *
     * @param typeMirror the TypeMirror to compare, may be null
     * @param type       the Type to compare, may be null
     * @return true if both represent the same type; false otherwise
     */
    static boolean isSameType(TypeMirror typeMirror, Type type) {
        return isSameType(typeMirror, type == null ? null : type.getTypeName());
    }

    /**
     * Checks if the given TypeMirror and type name represent the same type.
     *
     * @param type     the TypeMirror to compare, may be null
     * @param typeName the fully qualified class name to compare, may be null
     * @return true if both represent the same type; false otherwise
     */
    static boolean isSameType(TypeMirror type, CharSequence typeName) {
        if (type == null && typeName == null) {
            return true;
        }
        return Objects.equals(valueOf(type), valueOf(typeName));
    }

    /**
     * Checks whether the given {@link TypeMirror} represents an array type.
     *
     * @param type the TypeMirror to check, may be null
     * @return true if the type is an array type; false otherwise
     */
    static boolean isArrayType(TypeMirror type) {
        return type != null && ARRAY == type.getKind();
    }

    /**
     * Checks whether the given Element represents an array type.
     *
     * @param element the Element to check, may be null
     * @return true if the element represents an array type; false otherwise
     */
    static boolean isArrayType(Element element) {
        return element != null && isArrayType(element.asType());
    }

    /**
     * Checks whether the given {@link TypeMirror} represents an enum type.
     *
     * @param type the TypeMirror to check, may be null
     * @return true if the type is an enum type; false otherwise
     */
    static boolean isEnumType(TypeMirror type) {
        DeclaredType declaredType = ofDeclaredType(type);
        return declaredType != null && ENUM == declaredType.asElement().getKind();
    }

    /**
     * Checks whether the given Element represents an enum type.
     *
     * @param element the Element to check, may be null
     * @return true if the element represents an enum type; false otherwise
     */
    static boolean isEnumType(Element element) {
        return element != null && isEnumType(element.asType());
    }

    /**
     * Checks whether the given {@link TypeMirror} represents a class type.
     * A class type is determined by checking if its corresponding element has the kind of a class.
     *
     * @param type the TypeMirror to check, may be null
     * @return true if the type is a class type; false otherwise
     */
    static boolean isClassType(TypeMirror type) {
        DeclaredType declaredType = ofDeclaredType(type);
        return declaredType != null && isClassType(declaredType.asElement());
    }

    /**
     * Checks whether the given Element represents a class type.
     * A class type is determined by checking if its corresponding element has the kind of a class.
     *
     * @param element the Element to check, may be null
     * @return true if the element represents a class type; false otherwise
     */
    static boolean isClassType(Element element) {
        return element != null && CLASS == element.getKind();
    }

    /**
     * Checks whether the given {@link TypeMirror} represents a primitive type.
     * A primitive type is one of the predefined types in Java such as int, boolean, etc.
     *
     * @param type the TypeMirror to check, may be null
     * @return true if the type is a primitive type; false otherwise
     */
    static boolean isPrimitiveType(TypeMirror type) {
        return type != null && type.getKind().isPrimitive();
    }

    /**
     * Checks whether the given Element represents a primitive type.
     * A primitive type is one of the predefined types in Java such as int, boolean, etc.
     *
     * @param element the Element to check, may be null
     * @return true if the element represents a primitive type; false otherwise
     */
    static boolean isPrimitiveType(Element element) {
        return element != null && isPrimitiveType(element.asType());
    }

    /**
     * Checks whether the given {@link TypeMirror} represents an interface type.
     * An interface type is determined by checking if its corresponding element has the kind of an interface.
     *
     * @param type the TypeMirror to check, may be null
     * @return true if the type is an interface type; false otherwise
     */
    static boolean isInterfaceType(TypeMirror type) {
        DeclaredType declaredType = ofDeclaredType(type);
        return declaredType != null && isInterfaceType(declaredType.asElement());
    }

    /**
     * Checks whether the given Element represents an interface type.
     * An interface type is determined by checking if its corresponding element has the kind of an interface.
     *
     * @param element the Element to check, may be null
     * @return true if the element represents an interface type; false otherwise
     */
    static boolean isInterfaceType(Element element) {
        return element != null && INTERFACE == element.getKind();
    }

    /**
     * Checks whether the given {@link TypeMirror} represents an annotation type.
     * An annotation type is determined by checking if its corresponding element has the kind of an annotation.
     *
     * @param type the TypeMirror to check, may be null
     * @return true if the type is an annotation type; false otherwise
     */
    static boolean isAnnotationType(TypeMirror type) {
        DeclaredType declaredType = ofDeclaredType(type);
        return declaredType != null && isAnnotationType(declaredType.asElement());
    }

    /**
     * Checks whether the given Element represents an annotation type.
     * An annotation type is determined by checking if its corresponding element has the kind of an annotation.
     *
     * @param element the Element to check, may be null
     * @return true if the element represents an annotation type; false otherwise
     */
    static boolean isAnnotationType(Element element) {
        return element != null && ANNOTATION_TYPE == element.getKind();
    }

    /**
     * Checks if the given Element is a TypeElement.
     *
     * @param element The element to check, may be null.
     * @return true if the element is a TypeElement; false otherwise.
     */
    static boolean isTypeElement(Element element) {
        return element instanceof TypeElement;
    }

    /**
     * Checks if the given TypeMirror represents a TypeElement.
     *
     * @param type the TypeMirror to check, may be null
     * @return true if the TypeMirror represents a TypeElement; false otherwise
     */
    static boolean isTypeElement(TypeMirror type) {
        DeclaredType declaredType = ofDeclaredType(type);
        return declaredType != null && isTypeElement(declaredType.asElement());
    }

    /**
     * Checks if the given Element represents a DeclaredType.
     * A DeclaredType typically refers to a type that is explicitly declared in the code,
     * such as classes, interfaces, enums, or annotation types.
     *
     * @param element the Element to check, may be null
     * @return true if the element represents a DeclaredType; false otherwise
     */
    static boolean isDeclaredType(Element element) {
        return element != null && isDeclaredType(element.asType());
    }

    /**
     * Checks whether the given {@link TypeMirror} represents a declared type.
     * A declared type is a type that corresponds to a class, interface, enum, or annotation type
     * that has been explicitly defined in the source code.
     *
     * @param type the TypeMirror to check, may be null
     * @return true if the type is a declared type; false otherwise
     */
    static boolean isDeclaredType(TypeMirror type) {
        return type instanceof DeclaredType;
    }

    /**
     * Converts the given Element to a TypeElement if it is an instance of TypeElement.
     *
     * @param element The Element to convert, may be null.
     * @return The converted TypeElement, or null if the element is not a TypeElement or is null.
     */
    static TypeElement ofTypeElement(Element element) {
        return isTypeElement(element) ? (TypeElement) element : null;
    }

    /**
     * Converts the given TypeMirror to a TypeElement if it represents a declared type.
     *
     * @param type The TypeMirror to convert, may be null.
     * @return The corresponding TypeElement if the TypeMirror represents a declared type;
     *         otherwise, null if the type is null or not a DeclaredType.
     */
    static TypeElement ofTypeElement(TypeMirror type) {
        DeclaredType declaredType = ofDeclaredType(type);
        return ofTypeElement(declaredType);
    }

    /**
     * Converts the given DeclaredType to a TypeElement if it is not null.
     *
     * @param declaredType the DeclaredType to convert, may be null
     * @return the corresponding TypeElement if the DeclaredType is not null;
     *         otherwise, null
     */
    static TypeElement ofTypeElement(DeclaredType declaredType) {
        if (declaredType != null) {
            return ofTypeElement(declaredType.asElement());
        }
        return null;
    }

    /**
     * Converts the given Element to a DeclaredType by first converting it to a TypeElement.
     * If the element is null or cannot be converted to a DeclaredType, returns null.
     *
     * @param element The Element to convert, may be null.
     * @return The corresponding DeclaredType if the element is valid and represents a declared type;
     *         otherwise, null if the element is null or conversion fails.
     */
    static DeclaredType ofDeclaredType(Element element) {
        return element == null ? null : ofDeclaredType(element.asType());
    }

    /**
     * Converts the given TypeMirror to a DeclaredType if it represents a declared type.
     * A declared type is a type that corresponds to a class, interface, enum, or annotation type
     * that has been explicitly defined in the source code.
     *
     * @param type The TypeMirror to convert, may be null.
     * @return The corresponding DeclaredType if the TypeMirror represents a declared type;
     *         otherwise, null if the type is null or not a DeclaredType.
     */
    static DeclaredType ofDeclaredType(TypeMirror type) {
        return isDeclaredType(type) ? (DeclaredType) type : null;
    }

    /**
     * Converts an array of Elements to a List of TypeMirrors.
     * If the input array is null or empty, returns an empty list.
     *
     * @param elements the array of Elements to convert
     * @return a List of TypeMirrors derived from the given Elements
     */
    static List<TypeMirror> ofTypeMirrors(Element... elements) {
        return ofTypeMirrors(ofList(elements));
    }

    /**
     * Converts a collection of Elements to a list of TypeMirrors.
     * Optionally applies an array of predicates to filter the resulting TypeMirrors.
     *
     * @param elements The collection of Elements to convert. Must not be null.
     * @return A list of TypeMirrors derived from the given Elements.
     */
    static List<TypeMirror> ofTypeMirrors(Collection<? extends Element> elements) {
        return ofTypeMirrors(elements, EMPTY_PREDICATE_ARRAY);
    }

    static List<TypeMirror> ofTypeMirrors(Collection<? extends Element> elements, Predicate<? super TypeMirror>... typeFilters) {
        return isEmpty(elements) ? emptyList() :
                elements.stream().map(Element::asType).filter(and(typeFilters)).collect(toList());
    }

    /**
     * Converts an array of TypeMirrors to a List of TypeElements.
     * If the input array is null or empty, returns an empty list.
     *
     * @param types The array of TypeMirrors to convert. May be null or contain null elements.
     * @return A List of TypeElements derived from the given TypeMirrors.
     */
    static List<TypeElement> ofTypeElements(TypeMirror... types) {
        return ofTypeElements(ofList(types));
    }

    /**
     * Converts a collection of TypeMirrors to a list of TypeElements.
     * If the input collection is null or empty, returns an empty list.
     *
     * @param types The collection of TypeMirrors to convert. Must not be null.
     * @return A list of TypeElements derived from the given TypeMirrors.
     */
    static List<TypeElement> ofTypeElements(Collection<? extends TypeMirror> types) {
        return ofTypeElements(types, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Converts a collection of TypeMirrors to a list of TypeElements.
     * Optionally applies an array of predicates to filter the resulting TypeElements.
     *
     * @param types       The collection of TypeMirrors to convert. Must not be null.
     * @param typeFilters Optional predicates to filter the TypeElements. May be null or empty.
     * @return A list of TypeElements derived from the given TypeMirrors, filtered by the provided predicates.
     */
    static List<TypeElement> ofTypeElements(Collection<? extends TypeMirror> types, Predicate<? super TypeElement>... typeFilters) {
        return isEmpty(types) ? emptyList() :
                types.stream().map(TypeUtils::ofTypeElement).filter(Objects::nonNull).filter(and(typeFilters))
                        .collect(toList());
    }

    /**
     * Converts an array of Elements to a List of DeclaredTypes.
     * If the input array is null or empty, returns an empty list.
     *
     * @param elements the array of Elements to convert
     * @return a List of DeclaredTypes derived from the given Elements
     */
    static List<DeclaredType> ofDeclaredTypes(Element... elements) {
        return ofDeclaredTypes(ofList(elements));
    }

    /**
     * Converts a collection of Elements to a list of DeclaredTypes.
     * Optionally applies an array of predicates to filter the resulting DeclaredTypes.
     *
     * @param elements The collection of Elements to convert. Must not be null.
     * @return A list of DeclaredTypes derived from the given Elements.
     */
    static List<DeclaredType> ofDeclaredTypes(Collection<? extends Element> elements) {
        return ofDeclaredTypes(elements, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Converts a collection of Elements to a list of DeclaredTypes.
     * Optionally applies an array of predicates to filter the resulting DeclaredTypes.
     *
     * @param elements    The collection of Elements to convert. Must not be null.
     * @param typeFilters Optional predicates to filter the DeclaredTypes. May be null or empty.
     * @return A list of DeclaredTypes derived from the given Elements, filtered by the provided predicates.
     */
    static List<DeclaredType> ofDeclaredTypes(Collection<? extends Element> elements,
                                              Predicate<? super DeclaredType>... typeFilters) {

        if (isEmpty(elements)) {
            return emptyList();
        }

        List<DeclaredType> declaredTypes = elements.stream()
                .map(TypeUtils::ofTypeElement)
                .filter(Objects::nonNull)
                .map(Element::asType)
                .map(TypeUtils::ofDeclaredType)
                .filter(Objects::nonNull)
                .filter(and(typeFilters))
                .collect(toList());

        return declaredTypes.isEmpty() ? emptyList() : declaredTypes;
    }

    /**
     * Gets the TypeElement representing the superclass of the given TypeElement.
     *
     * @param type the TypeElement whose superclass is to be retrieved, may be null
     * @return the TypeElement of the superclass if available; otherwise, null
     */
    static TypeElement getTypeElementOfSuperclass(TypeElement type) {
        return type == null ? null : ofTypeElement(type.getSuperclass());
    }

    /**
     * Retrieves all super type elements (superclasses and interfaces) of the given TypeElement,
     * including those from its entire hierarchy.
     *
     * @param type The TypeElement whose super types are to be retrieved, may be null
     * @return A list of TypeElements representing the super types of the given TypeElement.
     *         Returns an empty list if the input is null or no super types exist.
     */
    static List<TypeElement> getAllTypeElementsOfSuperTypes(TypeElement type) {
        return findAllTypeElementsOfSuperTypes(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves all TypeElements representing the superclasses in the hierarchy of the given TypeElement.
     * This includes direct and indirect superclasses, but excludes interfaces.
     *
     * @param type The TypeElement whose superclass hierarchy is to be explored, may be null.
     * @return A list of TypeElements representing all superclasses in the hierarchy of the provided TypeElement.
     *         Returns an empty list if the input is null or no superclasses exist in the hierarchy.
     */
    static List<TypeElement> getAllTypeElementsOfSuperclasses(TypeElement type) {
        return findAllTypeElementsOfSuperclasses(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves the direct interface types implemented by the given TypeElement.
     * This method does not include superinterfaces from the entire hierarchy,
     * and only returns interfaces directly declared on the specified type.
     *
     * @param type The TypeElement whose directly implemented interfaces are to be retrieved, may be null.
     * @return A list of TypeElements representing the directly implemented interfaces.
     *         Returns an empty list if the input is null or no interfaces are directly implemented.
     */
    static List<TypeElement> getTypeElementsOfInterfaces(TypeElement type) {
        return findTypeElementsOfInterfaces(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves all TypeElements representing the interfaces implemented in the entire hierarchy of the given TypeElement.
     * This includes both directly and indirectly implemented interfaces from superclasses and superinterfaces.
     *
     * @param type The TypeElement whose interface hierarchy is to be explored, may be null.
     * @return A list of TypeElements representing all implemented interfaces in the hierarchy of the provided TypeElement.
     *         Returns an empty list if the input is null or no interfaces are found in the hierarchy.
     */
    static List<TypeElement> getAllTypeElementsOfInterfaces(TypeElement type) {
        return findAllTypeElementsOfInterfaces(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves the type elements directly associated with the given TypeElement.
     * This includes its own type, any directly implemented interfaces,
     * and the immediate superclass if present.
     *
     * @param type The TypeElement to retrieve associated types from, may be null.
     * @return A list of TypeElements representing the directly associated types.
     *         Returns an empty list if the input is null or no types are found.
     */
    static List<TypeElement> getTypeElements(TypeElement type) {
        return getTypeElements(type, true, false, true, true);
    }

    /**
     * Retrieves all TypeElements associated with the given TypeElement, including:
     * - The type itself
     * - Direct and hierarchical superclasses
     * - Direct and hierarchical interfaces
     *
     * @param type The TypeElement to retrieve all associated types from, may be null.
     * @return A list of TypeElements representing all associated types in the hierarchy.
     *         Returns an empty list if the input is null or no types are found.
     */
    static List<TypeElement> getAllTypeElements(TypeElement type) {
        return getTypeElements(type, true, true, true, true);
    }

    /**
     * Retrieves a list of TypeElements associated with the given TypeElement based on the specified inclusion criteria.
     *
     * @param type                  The TypeElement to find associated types for, may be null.
     * @param includeSelf           Whether to include the type itself in the result.
     * @param includeHierarchicalTypes Whether to include types from the entire hierarchy (e.g., superclasses and interfaces).
     * @param includeSuperClasses   Whether to include direct or hierarchical superclasses based on includeHierarchicalTypes.
     * @param includeSuperInterfaces Whether to include direct or hierarchical interfaces based on includeHierarchicalTypes.
     * @return A list of TypeElements representing the associated types according to the inclusion criteria.
     *         Returns an empty list if the input type is null or no matching types are found.
     */
    static List<TypeElement> getTypeElements(TypeElement type,
                                             boolean includeSelf,
                                             boolean includeHierarchicalTypes,
                                             boolean includeSuperClasses,
                                             boolean includeSuperInterfaces) {
        return findTypeElements(type, includeSelf, includeHierarchicalTypes, includeSuperClasses, includeSuperInterfaces, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves the direct interface types implemented by the given TypeElement.
     * This method only returns interfaces directly declared on the specified type,
     * and does not include superinterfaces from the hierarchy.
     *
     * @param type          The TypeElement whose directly implemented interfaces are to be retrieved, may be null.
     * @param interfaceFilters Optional predicates to filter the resulting TypeElements. May be null or empty.
     * @return A list of TypeElements representing the directly implemented interfaces.
     *         Returns an empty list if the input is null or no interfaces are directly implemented.
     */
    static List<TypeElement> findTypeElementsOfInterfaces(TypeElement type, Predicate<? super TypeElement>... interfaceFilters) {
        return findTypeElements(type, false, false, false, true, interfaceFilters);
    }

    /**
     * Retrieves all TypeElements representing the superclasses in the hierarchy of the given TypeElement.
     * This includes direct and indirect superclasses, but excludes interfaces.
     *
     * @param type       The TypeElement whose superclass hierarchy is to be explored, may be null.
     * @param typeFilters Optional predicates to filter the resulting TypeElements. May be null or empty.
     * @return A list of TypeElements representing all superclasses in the hierarchy of the provided TypeElement.
     *         Returns an empty list if the input is null or no superclasses exist in the hierarchy.
     */
    static List<TypeElement> findAllTypeElementsOfSuperclasses(TypeElement type, Predicate<? super TypeElement>... typeFilters) {
        return findTypeElements(type, false, true, true, false, typeFilters);
    }

    /**
     * Retrieves all TypeElements representing the interfaces implemented in the entire hierarchy of the given TypeElement.
     * This includes both directly and indirectly implemented interfaces from superclasses and superinterfaces.
     *
     * @param type The TypeElement whose interface hierarchy is to be explored, may be null.
     * @return A list of TypeElements representing all implemented interfaces in the hierarchy of the provided TypeElement.
     *         Returns an empty list if the input is null or no interfaces are found in the hierarchy.
     */
    static List<TypeElement> findAllTypeElementsOfInterfaces(TypeElement type, Predicate<? super TypeElement>... interfaceFilters) {
        return findTypeElements(type, false, true, false, true, interfaceFilters);
    }

    /**
     * Retrieves all TypeElements representing the superclasses and interfaces in the hierarchy of the given TypeElement.
     * This includes both direct and indirect superclasses as well as implemented interfaces from the entire hierarchy.
     *
     * @param type       The TypeElement whose hierarchy is to be explored, may be null.
     * @param typeFilters Optional predicates to filter the resulting TypeElements. May be null or empty.
     * @return A list of TypeElements representing all superclasses and interfaces in the hierarchy of the provided TypeElement.
     *         Returns an empty list if the input is null or no types are found in the hierarchy.
     */
    static List<TypeElement> findAllTypeElementsOfSuperTypes(TypeElement type, Predicate<? super TypeElement>... typeFilters) {
        return findTypeElements(type, false, true, true, true, typeFilters);
    }

    /**
     * Finds and returns a list of TypeElements based on the specified criteria.
     *
     * @param type                  The TypeElement to start the search from, may be null.
     * @param includeSelf           Whether to include the type itself in the result.
     * @param includeHierarchicalTypes Whether to include types from the entire hierarchy (e.g., superclasses and interfaces).
     * @param includeSuperclass     Whether to include direct or hierarchical superclasses based on includeHierarchicalTypes.
     * @param includeSuperInterfaces Whether to include direct or hierarchical interfaces based on includeHierarchicalTypes.
     * @param typeFilters           Optional predicates to filter the resulting TypeElements. May be null or empty.
     * @return A list of TypeElements matching the specified criteria.
     *         Returns an empty list if the input type is null or no matching types are found.
     * @throws IllegalArgumentException if any element of 'typeFilters' array is null.
     */
    static List<TypeElement> findTypeElements(TypeElement type,
                                              boolean includeSelf,
                                              boolean includeHierarchicalTypes,
                                              boolean includeSuperclass,
                                              boolean includeSuperInterfaces,
                                              Predicate<? super TypeElement>... typeFilters) throws IllegalArgumentException {
        if (type == null) {
            return emptyList();
        }
        assertNoNullElements(typeFilters, () -> "Any element of 'typeFilters' array must not be null");
        return typeElementFinder(type, includeSelf, includeHierarchicalTypes, includeSuperclass, includeSuperInterfaces).findTypes(typeFilters);
    }

    /**
     * Retrieves the declared type of the superclass for the given Element.
     * If the provided Element is null or does not represent a type with a superclass,
     * this method will return null.
     *
     * @param typeElement the Element to retrieve the superclass declared type from, may be null
     * @return the DeclaredType representing the superclass of the given Element, or null if none exists
     */
    static DeclaredType getDeclaredTypeOfSuperclass(Element typeElement) {
        return typeElement == null ? null : getDeclaredTypeOfSuperclass(typeElement.asType());
    }

    /**
     * Retrieves the declared type of the superclass for the given TypeMirror.
     * If the provided TypeMirror is null or does not represent a type with a superclass,
     * this method will return null.
     *
     * @param type the TypeMirror to retrieve the superclass declared type from, may be null
     * @return the DeclaredType representing the superclass of the given TypeMirror, or null if none exists
     */
    static DeclaredType getDeclaredTypeOfSuperclass(TypeMirror type) {
        TypeElement superType = getTypeElementOfSuperclass(ofTypeElement(type));
        return superType == null ? null : ofDeclaredType(superType.asType());
    }

    /**
     * Retrieves a list of declared types representing the interfaces directly implemented by the given Element.
     * This method only returns interfaces that are directly declared on the specified type,
     * and does not include interfaces from superclasses or superinterfaces.
     *
     * @param element The Element whose directly implemented interfaces are to be retrieved, may be null.
     * @return A list of DeclaredTypes representing the interfaces directly implemented by the given Element.
     *         Returns an empty list if the input is null or no interfaces are directly implemented.
     */
    static List<DeclaredType> getDeclaredTypesOfInterfaces(Element element) {
        return element == null ? emptyList() : findDeclaredTypesOfInterfaces(element.asType(), EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves a list of declared types representing the interfaces directly implemented by the given TypeMirror.
     * This method only returns interfaces that are directly declared on the specified type,
     * and does not include interfaces from superclasses or superinterfaces.
     *
     * @param type The TypeMirror whose directly implemented interfaces are to be retrieved, may be null.
     * @return A list of DeclaredTypes representing the interfaces directly implemented by the given TypeMirror.
     *         Returns an empty list if the input is null or no interfaces are directly implemented.
     */
    static List<DeclaredType> getDeclaredTypesOfInterfaces(TypeMirror type) {
        return findDeclaredTypesOfInterfaces(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves a list of DeclaredTypes representing all superclasses in the hierarchy of the given Element.
     * This includes both direct and indirect superclasses, traversing up through the entire type hierarchy.
     * If the provided Element is null or does not have any superclasses, an empty list is returned.
     *
     * @param type The Element to retrieve superclass declared types from, may be null.
     * @return A list of DeclaredTypes representing all superclasses in the hierarchy of the provided Element.
     *         Returns an empty list if the input is null or no superclasses exist in the hierarchy.
     */
    static List<DeclaredType> getAllDeclaredTypesOfSuperclasses(Element type) {
        return type == null ? emptyList() : findAllDeclaredTypesOfSuperclasses(type.asType(), EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves a list of DeclaredTypes representing all superclasses in the hierarchy of the given TypeMirror.
     * This includes both direct and indirect superclasses, traversing up through the entire type hierarchy.
     *
     * @param type The TypeMirror whose superclass hierarchy is to be explored, may be null.
     * @return A list of DeclaredTypes representing all superclasses in the hierarchy of the provided TypeMirror.
     *         Returns an empty list if the input is null or no superclasses exist in the hierarchy.
     */
    static List<DeclaredType> getAllDeclaredTypesOfSuperclasses(TypeMirror type) {
        return findAllDeclaredTypesOfSuperclasses(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves a list of DeclaredTypes representing all interfaces implemented in the entire hierarchy of the given Element.
     * This includes both directly and indirectly implemented interfaces from superclasses and superinterfaces.
     *
     * @param type The Element whose interface hierarchy is to be explored, may be null.
     * @return A list of DeclaredTypes representing all implemented interfaces in the hierarchy of the provided Element.
     *         Returns an empty list if the input is null or no interfaces are found in the hierarchy.
     */
    static List<DeclaredType> getAllDeclaredTypesOfInterfaces(Element type) {
        return type == null ? emptyList() : findAllDeclaredTypesOfInterfaces(type.asType(), EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves a list of DeclaredTypes representing all interfaces implemented in the entire hierarchy 
     * of the given TypeMirror. This includes both directly and indirectly implemented interfaces from 
     * superclasses and superinterfaces.
     *
     * @param type The TypeMirror whose interface hierarchy is to be explored, may be null.
     * @return A list of DeclaredTypes representing all implemented interfaces in the hierarchy of 
     *         the provided TypeMirror. Returns an empty list if the input is null or no interfaces 
     *         are found in the hierarchy.
     */
    static List<DeclaredType> getAllDeclaredTypesOfInterfaces(TypeMirror type) {
        return findAllDeclaredTypesOfInterfaces(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves a list of DeclaredTypes representing all superclasses and interfaces in the hierarchy 
     * of the given Element. This includes both direct and indirect superclasses as well as implemented 
     * interfaces from the entire hierarchy.
     *
     * @param type The Element to retrieve super types from, may be null.
     * @return A list of DeclaredTypes representing all superclasses and interfaces in the hierarchy 
     *         of the provided Element. Returns an empty list if the input is null or no types are found.
     */
    static List<DeclaredType> getAllDeclaredTypesOfSuperTypes(Element type) {
        return type == null ? emptyList() : findAllDeclaredTypesOfSuperTypes(type.asType(), EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves a list of DeclaredTypes representing all superclasses and interfaces in the hierarchy 
     * of the given TypeMirror. This includes both direct and indirect superclasses as well as 
     * implemented interfaces from the entire hierarchy.
     *
     * @param type The TypeMirror whose hierarchy is to be explored, may be null.
     * @return A list of DeclaredTypes representing all superclasses and interfaces in the hierarchy 
     *         of the provided TypeMirror. Returns an empty list if the input is null or no types are found.
     */
    static List<DeclaredType> getAllDeclaredTypesOfSuperTypes(TypeMirror type) {
        return findAllDeclaredTypesOfSuperTypes(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves a list of DeclaredTypes representing all associated types of the given Element.
     * This includes:
     * - The type itself (if it is a declared type)
     * - Direct and hierarchical superclasses
     * - Direct and hierarchical interfaces implemented by the type
     *
     * @param type The Element to retrieve associated DeclaredTypes from, may be null.
     * @return A list of DeclaredTypes derived from the given Element. Returns an empty list if
     *         the input is null or no DeclaredTypes are found.
     */
    static List<DeclaredType> getAllDeclaredTypes(Element type) {
        return type == null ? emptyList() : findAllDeclaredTypes(type.asType(), EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves a list of DeclaredTypes representing all associated types in the hierarchy of the given TypeMirror.
     * This includes:
     * - The type itself (if it is a declared type)
     * - Direct and hierarchical superclasses
     * - Direct and hierarchical interfaces implemented by the type
     *
     * @param type The TypeMirror to retrieve associated DeclaredTypes from, may be null.
     * @return A list of DeclaredTypes representing all associated types in the hierarchy of the provided TypeMirror.
     *         Returns an empty list if the input is null or no DeclaredTypes are found.
     */
    static List<DeclaredType> getAllDeclaredTypes(TypeMirror type) {
        return findAllDeclaredTypes(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves a list of DeclaredTypes associated with the given Element based on the specified inclusion criteria.
     * A DeclaredType typically refers to a type that is explicitly declared in the code, such as classes,
     * interfaces, enums, or annotation types.
     *
     * @param type                  The Element to retrieve DeclaredTypes from, may be null.
     * @param includeSelf           Whether to include the type itself in the result.
     * @param includeHierarchicalTypes Whether to include types from the entire hierarchy (e.g., superclasses and interfaces).
     * @param includeSuperClasses   Whether to include direct or hierarchical superclasses based on includeHierarchicalTypes.
     * @param includeSuperInterfaces Whether to include direct or hierarchical interfaces based on includeHierarchicalTypes.
     * @return A list of DeclaredTypes derived from the given Element according to the inclusion criteria.
     *         Returns an empty list if the input is null or no DeclaredTypes are found.
     */
    static List<DeclaredType> getDeclaredTypes(Element type,
                                               boolean includeSelf,
                                               boolean includeHierarchicalTypes,
                                               boolean includeSuperClasses,
                                               boolean includeSuperInterfaces) {
        return getDeclaredTypes(type.asType(), includeSelf, includeHierarchicalTypes, includeSuperClasses, includeSuperInterfaces);
    }

    /**
     * Retrieves a list of DeclaredTypes associated with the given TypeMirror based on the specified inclusion criteria.
     * A DeclaredType typically refers to a type that is explicitly declared in the code, such as classes,
     * interfaces, enums, or annotation types.
     *
     * @param type                  The TypeMirror to retrieve DeclaredTypes from, may be null.
     * @param includeSelf           Whether to include the type itself in the result.
     * @param includeHierarchicalTypes Whether to include types from the entire hierarchy (e.g., superclasses and interfaces).
     * @param includeSuperClasses   Whether to include direct or hierarchical superclasses based on includeHierarchicalTypes.
     * @param includeSuperInterfaces Whether to include direct or hierarchical interfaces based on includeHierarchicalTypes.
     * @return A list of DeclaredTypes derived from the given TypeMirror according to the inclusion criteria.
     *         Returns an empty list if the input is null or no DeclaredTypes are found.
     */
    static List<DeclaredType> getDeclaredTypes(TypeMirror type,
                                               boolean includeSelf,
                                               boolean includeHierarchicalTypes,
                                               boolean includeSuperClasses,
                                               boolean includeSuperInterfaces) {
        return findDeclaredTypes(type, includeSelf, includeHierarchicalTypes, includeSuperClasses, includeSuperInterfaces, EMPTY_PREDICATE_ARRAY);
    }


    /**
     * Finds and returns a list of DeclaredTypes associated with the given TypeMirror, 
     * excluding any types that match the specified excludedTypes.
     *
     * @param type          The TypeMirror to find associated DeclaredTypes from, may be null.
     * @param excludedTypes The array of Types to exclude from the result. May be null or empty.
     * @return A list of DeclaredTypes derived from the given TypeMirror, excluding the specified types.
     *         Returns an empty list if the input type is null or no matching DeclaredTypes are found.
     */
    static List<DeclaredType> findDeclaredTypes(TypeMirror type, Type... excludedTypes) {
        return type == null ? emptyList() : findDeclaredTypes(ofTypeElement(type), excludedTypes);
    }

    /**
     * Finds and returns a list of DeclaredTypes associated with the given Element,
     * excluding any types that match the specified excludedTypes.
     *
     * @param type          The Element to find associated DeclaredTypes from, may be null.
     * @param excludedTypes The array of Types to exclude from the result. May be null or empty.
     * @return A list of DeclaredTypes derived from the given Element, excluding the specified types.
     *         Returns an empty list if the input type is null or no matching DeclaredTypes are found.
     */
    static List<DeclaredType> findDeclaredTypes(Element type, Type... excludedTypes) {
        return type == null ? emptyList() : findDeclaredTypes(type, getTypeNames(excludedTypes));
    }

    /**
     * Finds and returns a list of DeclaredTypes associated with the given TypeMirror,
     * excluding any types whose names match the specified excludedTypeNames.
     *
     * @param type              The TypeMirror to find associated DeclaredTypes from, may be null.
     * @param excludedTypeNames An array of fully qualified type names to exclude from the result.
     *                          May be null or empty.
     * @return A list of DeclaredTypes derived from the given TypeMirror, excluding the specified types.
     *         Returns an empty list if the input type is null or no matching DeclaredTypes are found.
     */
    static List<DeclaredType> findDeclaredTypes(TypeMirror type, CharSequence... excludedTypeNames) {
        return type == null ? emptyList() : findDeclaredTypes(ofTypeElement(type), excludedTypeNames);
    }

    /**
     * Finds and returns a list of DeclaredTypes associated with the given Element,
     * excluding any types whose names match the specified excludedTypeNames.
     *
     * @param type              The Element to find associated DeclaredTypes from, may be null.
     * @param excludedTypeNames An array of fully qualified type names to exclude from the result.
     *                          May be null or empty.
     * @return A list of DeclaredTypes derived from the given Element, excluding the specified types.
     *         Returns an empty list if the input is null or no matching DeclaredTypes are found.
     */
    static List<DeclaredType> findDeclaredTypes(Element type, CharSequence... excludedTypeNames) {
        return type == null ? emptyList() : findDeclaredTypes(type, false, false, true, true, t -> !contains(excludedTypeNames, t.toString()));
    }

    /**
     * Retrieves a list of DeclaredTypes representing the interfaces directly implemented by the given Element.
     * This method only returns interfaces that are directly declared on the specified type,
     * and does not include interfaces from superclasses or superinterfaces.
     *
     * @param type        The Element whose directly implemented interfaces are to be retrieved, may be null.
     * @param typeFilters Optional predicates to filter the resulting DeclaredTypes. May be null or empty.
     * @return A list of DeclaredTypes representing the interfaces directly implemented by the given Element.
     *         Returns an empty list if the input is null or no interfaces are directly implemented.
     */
    static List<DeclaredType> findDeclaredTypesOfInterfaces(Element type, Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : findDeclaredTypesOfInterfaces(type.asType(), typeFilters);
    }

    /**
     * Retrieves a list of DeclaredTypes representing the interfaces directly implemented by the given TypeMirror.
     * This method only returns interfaces that are directly declared on the specified type,
     * and does not include interfaces from superclasses or superinterfaces.
     *
     * @param type       The TypeMirror whose directly implemented interfaces are to be retrieved, may be null.
     * @param typeFilters Optional predicates to filter the resulting DeclaredTypes. May be null or empty.
     * @return A list of DeclaredTypes representing the interfaces directly implemented by the given TypeMirror.
     *         Returns an empty list if the input is null or no interfaces are directly implemented.
     */
    static List<DeclaredType> findDeclaredTypesOfInterfaces(TypeMirror type, Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : ofDeclaredTypes(getTypeElementsOfInterfaces(ofTypeElement(type)), typeFilters);
    }

    /**
     * Retrieves a list of DeclaredTypes representing all superclasses in the hierarchy of the given Element.
     * This includes both direct and indirect superclasses, traversing up through the entire type hierarchy.
     * Optionally applies an array of predicates to filter the resulting DeclaredTypes.
     *
     * @param type       The Element to retrieve superclass declared types from, may be null.
     * @param typeFilters Optional predicates to filter the resulting DeclaredTypes. May be null or empty.
     * @return A list of DeclaredTypes representing all superclasses in the hierarchy of the provided Element.
     *         Returns an empty list if the input is null or no superclasses exist in the hierarchy.
     */
    static List<DeclaredType> findAllDeclaredTypesOfSuperclasses(Element type, Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : findAllDeclaredTypesOfSuperclasses(type.asType(), typeFilters);
    }

    /**
     * Retrieves a list of DeclaredTypes representing all superclasses in the hierarchy of the given TypeMirror.
     * This includes both direct and indirect superclasses, traversing up through the entire type hierarchy.
     * Optionally applies an array of predicates to filter the resulting DeclaredTypes.
     *
     * @param type       The TypeMirror whose superclass hierarchy is to be explored, may be null.
     * @param typeFilters Optional predicates to filter the resulting DeclaredTypes. May be null or empty.
     * @return A list of DeclaredTypes representing all superclasses in the hierarchy of the provided TypeMirror,
     *         filtered by the provided predicates. Returns an empty list if the input is null or no superclasses
     *         exist in the hierarchy.
     */
    static List<DeclaredType> findAllDeclaredTypesOfSuperclasses(TypeMirror type, Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : ofDeclaredTypes(getAllTypeElementsOfSuperclasses(ofTypeElement(type)), typeFilters);
    }

    /**
     * Retrieves a list of DeclaredTypes representing all interfaces implemented in the entire hierarchy 
     * of the given Element. This includes both directly and indirectly implemented interfaces from 
     * superclasses and superinterfaces. Optionally applies an array of predicates to filter the resulting DeclaredTypes.
     *
     * @param type       The Element whose interface hierarchy is to be explored, may be null.
     * @param typeFilters Optional predicates to filter the resulting DeclaredTypes. May be null or empty.
     * @return A list of DeclaredTypes representing all implemented interfaces in the hierarchy of 
     *         the provided Element, filtered by the provided predicates. Returns an empty list if 
     *         the input is null or no interfaces are found in the hierarchy.
     */
    static List<DeclaredType> findAllDeclaredTypesOfInterfaces(Element type, Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : findAllDeclaredTypesOfInterfaces(type.asType(), typeFilters);
    }

    /**
     * Retrieves a list of DeclaredTypes representing all interfaces implemented in the entire hierarchy 
     * of the given TypeMirror. This includes both directly and indirectly implemented interfaces from 
     * superclasses and superinterfaces. Optionally applies an array of predicates to filter the resulting DeclaredTypes.
     *
     * @param type       The TypeMirror whose interface hierarchy is to be explored, may be null.
     * @param typeFilters Optional predicates to filter the resulting DeclaredTypes. May be null or empty.
     * @return A list of DeclaredTypes representing all implemented interfaces in the hierarchy of 
     *         the provided TypeMirror, filtered by the provided predicates. Returns an empty list if 
     *         the input is null or no interfaces are found in the hierarchy.
     */
    static List<DeclaredType> findAllDeclaredTypesOfInterfaces(TypeMirror type, Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : ofDeclaredTypes(getAllTypeElementsOfInterfaces(ofTypeElement(type)), typeFilters);
    }

    /**
     * Retrieves a list of DeclaredTypes representing all superclasses and interfaces in the hierarchy 
     * of the given Element. This includes both direct and indirect superclasses as well as implemented 
     * interfaces from the entire hierarchy. Optionally applies an array of predicates to filter the resulting DeclaredTypes.
     *
     * @param type       The Element to retrieve superclass and interface declared types from, may be null.
     * @param typeFilters Optional predicates to filter the resulting DeclaredTypes. May be null or empty.
     * @return A list of DeclaredTypes representing all superclasses and interfaces in the hierarchy of 
     *         the provided Element, filtered by the provided predicates. Returns an empty list if the 
     *         input is null or no matching DeclaredTypes are found.
     */
    static List<DeclaredType> findAllDeclaredTypesOfSuperTypes(Element type, Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : findAllDeclaredTypesOfSuperTypes(type.asType(), typeFilters);
    }

    /**
     * Retrieves a list of DeclaredTypes representing all superclasses and interfaces in the hierarchy 
     * of the given TypeMirror. This includes both direct and indirect superclasses as well as implemented 
     * interfaces from the entire hierarchy. Optionally applies an array of predicates to filter the resulting DeclaredTypes.
     *
     * @param type       The TypeMirror whose superclass and interface hierarchy is to be explored, may be null.
     * @param typeFilters Optional predicates to filter the resulting DeclaredTypes. May be null or empty.
     * @return A list of DeclaredTypes representing all superclasses and interfaces in the hierarchy of 
     *         the provided TypeMirror, filtered by the provided predicates. Returns an empty list if the 
     *         input is null or no matching DeclaredTypes are found.
     */
    static List<DeclaredType> findAllDeclaredTypesOfSuperTypes(TypeMirror type, Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : ofDeclaredTypes(getAllTypeElementsOfSuperTypes(ofTypeElement(type)), typeFilters);
    }

    /**
     * Retrieves a list of DeclaredTypes representing all associated types in the hierarchy 
     * of the given TypeMirror, excluding any types that match the specified excludedTypes.
     * This includes:
     * - The type itself (if it is a declared type)
     * - Direct and hierarchical superclasses
     * - Direct and hierarchical interfaces implemented by the type
     *
     * @param type          The TypeMirror to retrieve associated DeclaredTypes from, may be null.
     * @param excludedTypes The array of Types to exclude from the result. May be null or empty.
     * @return A list of DeclaredTypes derived from the given TypeMirror, excluding the specified types.
     *         Returns an empty list if the input is null or no matching DeclaredTypes are found.
     */
    static List<DeclaredType> findAllDeclaredTypes(TypeMirror type, Type... excludedTypes) {
        return type == null ? emptyList() : findAllDeclaredTypes(ofTypeElement(type), excludedTypes);
    }

    /**
     * Retrieves a list of DeclaredTypes representing all associated types in the hierarchy 
     * of the given Element, excluding any types that match the specified excludedTypes.
     * This includes:
     * - The type itself (if it is a declared type)
     * - Direct and hierarchical superclasses
     * - Direct and hierarchical interfaces implemented by the type
     *
     * @param type          The Element to retrieve associated DeclaredTypes from, may be null.
     * @param excludedTypes The array of Types to exclude from the result. May be null or empty.
     * @return A list of DeclaredTypes derived from the given Element, excluding the specified types.
     *         Returns an empty list if the input is null or no matching DeclaredTypes are found.
     */
    static List<DeclaredType> findAllDeclaredTypes(Element type, Type... excludedTypes) {
        return type == null ? emptyList() : findAllDeclaredTypes(type, getTypeNames(excludedTypes));
    }

    /**
     * Retrieves a list of DeclaredTypes representing all associated types in the hierarchy 
     * of the given TypeMirror, excluding any types whose names match the specified excludedTypeNames.
     * This includes:
     * - The type itself (if it is a declared type)
     * - Direct and hierarchical superclasses
     * - Direct and hierarchical interfaces implemented by the type
     *
     * @param type              The TypeMirror to retrieve associated DeclaredTypes from, may be null.
     * @param excludedTypeNames An array of fully qualified type names to exclude from the result.
     *                          May be null or empty.
     * @return A list of DeclaredTypes derived from the given TypeMirror, excluding the specified types.
     *         Returns an empty list if the input type is null or no matching DeclaredTypes are found.
     */
    static List<DeclaredType> findAllDeclaredTypes(TypeMirror type, CharSequence... excludedTypeNames) {
        return type == null ? emptyList() : findAllDeclaredTypes(ofTypeElement(type), excludedTypeNames);
    }

    /**
     * Retrieves a list of DeclaredTypes representing all associated types in the hierarchy 
     * of the given Element, excluding any types whose names match the specified excludedTypeNames.
     * This includes:
     * - The type itself (if it is a declared type)
     * - Direct and hierarchical superclasses
     * - Direct and hierarchical interfaces implemented by the type
     *
     * @param type              The Element to retrieve associated DeclaredTypes from, may be null.
     * @param excludedTypeNames An array of fully qualified type names to exclude from the result.
     *                          May be null or empty.
     * @return A list of DeclaredTypes derived from the given Element, excluding the specified types.
     *         Returns an empty list if the input is null or no matching DeclaredTypes are found.
     */
    static List<DeclaredType> findAllDeclaredTypes(Element type, CharSequence... excludedTypeNames) {
        return type == null ? emptyList() : findAllDeclaredTypes(type, t -> !contains(excludedTypeNames, t.toString()));
    }

    /**
     * Retrieves a list of DeclaredTypes representing all associated types in the hierarchy 
     * of the given Element, filtered by the provided predicates.
     * This includes:
     * - The type itself (if it is a declared type)
     * - Direct and hierarchical superclasses
     * - Direct and hierarchical interfaces implemented by the type
     *
     * @param type       The Element to retrieve associated DeclaredTypes from, may be null.
     * @param typeFilters Optional predicates to filter the resulting DeclaredTypes. May be null or empty.
     * @return A list of DeclaredTypes derived from the given Element, filtered by the provided predicates.
     *         Returns an empty list if the input is null or no matching DeclaredTypes are found.
     */
    static List<DeclaredType> findAllDeclaredTypes(Element type, Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : findAllDeclaredTypes(type.asType(), typeFilters);
    }

    /**
     * Retrieves a list of DeclaredTypes representing all associated types in the hierarchy 
     * of the given TypeMirror, filtered by the provided predicates.
     * This includes:
     * - The type itself (if it is a declared type)
     * - Direct and hierarchical superclasses
     * - Direct and hierarchical interfaces implemented by the type
     *
     * @param type       The TypeMirror to retrieve associated DeclaredTypes from, may be null.
     * @param typeFilters Optional predicates to filter the resulting DeclaredTypes. May be null or empty.
     * @return A list of DeclaredTypes derived from the given TypeMirror, filtered by the provided predicates.
     *         Returns an empty list if the input is null or no matching DeclaredTypes are found.
     */
    static List<DeclaredType> findAllDeclaredTypes(TypeMirror type, Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : ofDeclaredTypes(getAllTypeElements(ofTypeElement(type)), typeFilters);
    }

    /**
     * Finds and returns a list of DeclaredTypes associated with the given Element based on the specified criteria.
     *
     * <p>A DeclaredType typically refers to a type that is explicitly declared in the code, such as classes,
     * interfaces, enums, or annotation types. This method allows filtering based on whether to include:
     * - The type itself
     * - Direct or hierarchical superclasses (based on the includeHierarchicalTypes flag)
     * - Direct or hierarchical interfaces (based on the includeHierarchicalTypes flag)
     * 
     * @param type                  The Element to find associated DeclaredTypes from. May be null.
     * @param includeSelf           Whether to include the type itself in the result.
     * @param includeHierarchicalTypes Whether to include types from the entire hierarchy (e.g., superclasses and interfaces).
     * @param includeSuperClasses   Whether to include direct or hierarchical superclasses based on includeHierarchicalTypes.
     * @param includeSuperInterfaces Whether to include direct or hierarchical interfaces based on includeHierarchicalTypes.
     * @param typeFilters           Optional predicates to filter the resulting DeclaredTypes. May be null or empty.
     * @return A list of DeclaredTypes matching the specified criteria.
     *         Returns an empty list if the input type is null or no matching types are found.
     * @throws IllegalArgumentException if any element of 'typeFilters' array is null.
     */
    static List<DeclaredType> findDeclaredTypes(Element type,
                                                boolean includeSelf,
                                                boolean includeHierarchicalTypes,
                                                boolean includeSuperClasses,
                                                boolean includeSuperInterfaces,
                                                Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : findDeclaredTypes(type.asType(), includeSelf, includeHierarchicalTypes, includeSuperClasses, includeSuperInterfaces, typeFilters);
    }

    /**
     * Finds and returns a list of DeclaredTypes associated with the given TypeMirror based on the specified criteria.
     *
     * <p>A DeclaredType typically refers to a type that is explicitly declared in the code, such as classes,
     * interfaces, enums, or annotation types. This method allows filtering based on whether to include:
     * - The type itself
     * - Direct or hierarchical superclasses (based on the includeHierarchicalTypes flag)
     * - Direct or hierarchical interfaces (based on the includeHierarchicalTypes flag)
     *
     * @param type                  The TypeMirror to find associated DeclaredTypes from. May be null.
     * @param includeSelf           Whether to include the type itself in the result.
     * @param includeHierarchicalTypes Whether to include types from the entire hierarchy (e.g., superclasses and interfaces).
     * @param includeSuperClasses   Whether to include direct or hierarchical superclasses based on includeHierarchicalTypes.
     * @param includeSuperInterfaces Whether to include direct or hierarchical interfaces based on includeHierarchicalTypes.
     * @param typeFilters           Optional predicates to filter the resulting DeclaredTypes. May be null or empty.
     * @return A list of DeclaredTypes matching the specified criteria.
     *         Returns an empty list if the input type is null or no matching types are found.
     * @throws IllegalArgumentException if any element of 'typeFilters' array is null.
     */
    static List<DeclaredType> findDeclaredTypes(TypeMirror type,
                                                boolean includeSelf,
                                                boolean includeHierarchicalTypes,
                                                boolean includeSuperClasses,
                                                boolean includeSuperInterfaces,
                                                Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : ofDeclaredTypes(getTypeElements(ofTypeElement(type), includeSelf, includeHierarchicalTypes, includeSuperClasses, includeSuperInterfaces), typeFilters);
    }

    /**
     * Retrieves a list of TypeMirrors representing the interfaces directly implemented by the given TypeMirror.
     * This method only returns interfaces that are directly declared on the specified type,
     * and does not include interfaces from superclasses or superinterfaces.
     *
     * @param type The TypeMirror whose directly implemented interfaces are to be retrieved, may be null.
     * @return A list of TypeMirrors representing the interfaces directly implemented by the given TypeMirror.
     *         Returns an empty list if the input is null or no interfaces are directly implemented.
     */
    static List<TypeMirror> getTypeMirrorsOfInterfaces(TypeMirror type) {
        return type == null ? emptyList() : findTypeMirrorsOfInterfaces(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves a list of TypeMirrors representing the interfaces directly implemented by the given TypeElement.
     * This method only returns interfaces that are directly declared on the specified type,
     * and does not include interfaces from superclasses or superinterfaces.
     *
     * @param type The TypeElement whose directly implemented interfaces are to be retrieved, may be null.
     * @return A list of TypeMirrors representing the interfaces directly implemented by the given TypeElement.
     *         Returns an empty list if the input is null or no interfaces are directly implemented.
     */
    static List<TypeMirror> getTypeMirrorsOfInterfaces(TypeElement type) {
        return type == null ? emptyList() : findTypeMirrorsOfInterfaces(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves a list of TypeMirrors representing the interfaces directly implemented by the given TypeMirror.
     * This method only returns interfaces that are directly declared on the specified type,
     * and does not include interfaces from superclasses or superinterfaces.
     *
     * @param type           The TypeMirror whose directly implemented interfaces are to be retrieved, may be null.
     * @param interfaceFilters Optional predicates to filter the resulting TypeMirrors. May be null or empty.
     * @return A list of TypeMirrors representing the interfaces directly implemented by the given TypeMirror,
     *         filtered by the provided predicates. Returns an empty list if the input is null or no interfaces
     *         are directly implemented.
     */
    static List<TypeMirror> findTypeMirrorsOfInterfaces(TypeMirror type, Predicate<TypeMirror>... interfaceFilters) {
        return type == null ? emptyList() : findTypeMirrorsOfInterfaces(ofTypeElement(type), interfaceFilters);
    }

    /**
     * Retrieves a list of TypeMirrors representing the interfaces directly implemented by the given TypeElement.
     * This method only returns interfaces that are directly declared on the specified type,
     * and does not include interfaces from superclasses or superinterfaces.
     *
     * @param type           The TypeElement whose directly implemented interfaces are to be retrieved, may be null.
     * @param interfaceFilters Optional predicates to filter the resulting TypeMirrors. May be null or empty.
     * @return A list of TypeMirrors representing the interfaces directly implemented by the given TypeElement,
     *         filtered by the provided predicates. Returns an empty list if the input is null or no interfaces
     *         are directly implemented.
     */
    static List<TypeMirror> findTypeMirrorsOfInterfaces(TypeElement type, Predicate<TypeMirror>... interfaceFilters) {
        if (type == null) {
            return emptyList();
        }
        List<TypeMirror> typeMirrors = getTypeElementsOfInterfaces(type).stream()
                .map(TypeElement::asType)
                .filter(and(interfaceFilters))
                .collect(toList());
        return typeMirrors.isEmpty() ? emptyList() : typeMirrors;
    }

    /**
     * Retrieves a list of TypeMirrors representing all interfaces implemented in the entire hierarchy 
     * of the given TypeMirror. This includes both directly and indirectly implemented interfaces from 
     * superclasses and superinterfaces.
     *
     * @param type The TypeMirror whose interface hierarchy is to be explored, may be null.
     * @return A list of TypeMirrors representing all implemented interfaces in the hierarchy of 
     *         the provided TypeMirror. Returns an empty list if the input is null or no interfaces 
     *         are found in the hierarchy.
     */
    static List<TypeMirror> getAllTypeMirrorsOfInterfaces(TypeMirror type) {
        return findAllTypeMirrorsOfInterfaces(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves a list of TypeMirrors representing all interfaces implemented in the entire hierarchy 
     * of the given TypeElement. This includes both directly and indirectly implemented interfaces from 
     * superclasses and superinterfaces.
     *
     * @param type The TypeElement whose interface hierarchy is to be explored, may be null.
     * @return A list of TypeMirrors representing all implemented interfaces in the hierarchy of 
     *         the provided TypeElement. Returns an empty list if the input is null or no interfaces 
     *         are found in the hierarchy.
     */
    static List<TypeMirror> getAllTypeMirrorsOfInterfaces(TypeElement type) {
        return findAllTypeMirrorsOfInterfaces(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves a list of TypeMirrors representing all interfaces implemented in the entire hierarchy 
     * of the given TypeMirror. This includes both directly and indirectly implemented interfaces from 
     * superclasses and superinterfaces. Optionally applies an array of predicates to filter the resulting TypeMirrors.
     *
     * @param type           The TypeMirror whose interface hierarchy is to be explored, may be null.
     * @param interfaceFilters Optional predicates to filter the resulting TypeMirrors. May be null or empty.
     * @return A list of TypeMirrors representing all implemented interfaces in the hierarchy of 
     *         the provided TypeMirror, filtered by the provided predicates. Returns an empty list if 
     *         the input is null or no interfaces are found in the hierarchy.
     */
    static List<TypeMirror> findAllTypeMirrorsOfInterfaces(TypeMirror type, Predicate<TypeMirror>... interfaceFilters) {
        return type == null ? emptyList() : findAllTypeMirrorsOfInterfaces(ofTypeElement(type), interfaceFilters);
    }

    /**
     * Retrieves a list of TypeMirrors representing all interfaces implemented in the entire hierarchy 
     * of the given TypeElement. This includes both directly and indirectly implemented interfaces from 
     * superclasses and superinterfaces. Optionally applies an array of predicates to filter the resulting TypeMirrors.
     *
     * @param type           The TypeElement whose interface hierarchy is to be explored, may be null.
     * @param interfaceFilters Optional predicates to filter the resulting TypeMirrors. May be null or empty.
     * @return A list of TypeMirrors representing all implemented interfaces in the hierarchy of 
     *         the provided TypeElement, filtered by the provided predicates. Returns an empty list if 
     *         the input is null or no interfaces are found in the hierarchy.
     */
    static List<TypeMirror> findAllTypeMirrorsOfInterfaces(TypeElement type, Predicate<TypeMirror>... interfaceFilters) {
        if (type == null) {
            return emptyList();
        }
        List<TypeMirror> typeMirrors = getAllTypeElementsOfInterfaces(type).stream()
                .map(TypeElement::asType)
                .filter(and(interfaceFilters))
                .collect(toList());
        return typeMirrors.isEmpty() ? emptyList() : typeMirrors;
    }

    /**
     * Finds and returns the TypeMirror representing the specified interface type from the given Element.
     * If the provided Element or interfaceType is null, returns null.
     *
     * @param type          The Element to find the interface TypeMirror from. May be null.
     * @param interfaceType The Type representing the interface to search for. May be null.
     * @return The TypeMirror of the specified interface if found; otherwise, null.
     */
    static TypeMirror findInterfaceTypeMirror(Element type, Type interfaceType) {
        return findInterfaceTypeMirror(type, interfaceType.getTypeName());
    }

    /**
     * Finds and returns the TypeMirror representing the specified interface type from the given TypeMirror.
     * If the provided TypeMirror or interfaceType is null, returns null.
     *
     * @param type          The TypeMirror to find the interface TypeMirror from. May be null.
     * @param interfaceType The Type representing the interface to search for. May be null.
     * @return The TypeMirror of the specified interface if found; otherwise, null.
     */
    static TypeMirror findInterfaceTypeMirror(TypeMirror type, Type interfaceType) {
        return findInterfaceTypeMirror(type, interfaceType.getTypeName());
    }

    /**
     * Finds and returns the TypeMirror representing the specified interface type from the given Element.
     * If the provided Element or interfaceClassName is null, returns null.
     *
     * @param type              The Element to find the interface TypeMirror from. May be null.
     * @param interfaceClassName The fully qualified class name of the interface to search for. May be null.
     * @return The TypeMirror of the specified interface if found; otherwise, null.
     */
    static TypeMirror findInterfaceTypeMirror(Element type, CharSequence interfaceClassName) {
        return type == null ? null : findInterfaceTypeMirror(type.asType(), interfaceClassName);
    }

    /**
     * Finds and returns the TypeMirror representing the specified interface type from the given TypeMirror.
     * If the provided TypeMirror or interfaceClassName is null, returns null.
     * 
     * <p>This method searches through all interfaces implemented in the entire hierarchy of the given TypeMirror,
     * including both directly and indirectly implemented interfaces from superclasses and superinterfaces.
     *
     * @param type              The TypeMirror to find the interface TypeMirror from. May be null.
     * @param interfaceClassName The fully qualified class name of the interface to search for. May be null.
     * @return The TypeMirror of the specified interface if found; otherwise, null.
     */
    static TypeMirror findInterfaceTypeMirror(TypeMirror type, CharSequence interfaceClassName) {
        return filterFirst(getAllTypeMirrorsOfInterfaces(type), t -> isSameType(t, interfaceClassName));
    }

    /**
     * Converts an array of Type objects to a list of TypeMirror instances using the provided ProcessingEnvironment.
     * If the input array is null or empty, returns an empty list.
     *
     * @param processingEnv The ProcessingEnvironment used to resolve TypeMirrors. Must not be null.
     * @param types         The array of Type objects to convert. May contain null elements which will be ignored.
     * @return A list of TypeMirror instances derived from the given Types. Returns an empty list if the input array is null or empty,
     *         or if no valid TypeMirror instances could be resolved.
     */
    static List<TypeMirror> getTypeMirrors(ProcessingEnvironment processingEnv, Type... types) {
        if (isEmpty(types)) {
            return emptyList();
        }
        List<TypeMirror> typeMirrors = of(types)
                .filter(Objects::nonNull)
                .map(t -> getTypeMirror(processingEnv, t))
                .filter(Objects::nonNull)
                .collect(toList());
        return typeMirrors.isEmpty() ? emptyList() : typeMirrors;
    }

    /**
     * Converts the given Type to a TypeMirror using the provided ProcessingEnvironment.
     *
     * <p>If the provided Type is null, this method returns null. Otherwise, it attempts
     * to resolve the Type into a TypeElement and then retrieves its corresponding TypeMirror.
     *
     * @param processingEnv The ProcessingEnvironment used to resolve TypeMirrors. Must not be null.
     * @param type          The Type to convert to a TypeMirror. May be null.
     * @return The resolved TypeMirror if available; otherwise, null.
     */
    static TypeMirror getTypeMirror(ProcessingEnvironment processingEnv, Type type) {
        TypeElement typeElement = getTypeElement(processingEnv, type);
        return typeElement == null ? null : typeElement.asType();
    }

    /**
     * Converts an array of Type objects to a list of TypeElement instances using the provided ProcessingEnvironment.
     * If the input array is null or empty, returns an empty list.
     *
     * @param processingEnv The ProcessingEnvironment used to resolve TypeElements. Must not be null.
     * @param types         The array of Type objects to convert. May contain null elements which will be ignored.
     * @return A list of TypeElement instances derived from the given Types. Returns an empty list if the input array is null or empty,
     *         or if no valid TypeElement instances could be resolved.
     */
    static List<TypeElement> getTypeElements(ProcessingEnvironment processingEnv, Type... types) {
        if (isEmpty(types)) {
            return emptyList();
        }
        List<TypeElement> typeElements = of(types)
                .filter(Objects::nonNull)
                .map(t -> getTypeElement(processingEnv, t))
                .filter(Objects::nonNull)
                .collect(toList());
        return typeElements.isEmpty() ? emptyList() : typeElements;
    }

    /**
     * Retrieves the TypeElement corresponding to the given Type using the provided ProcessingEnvironment.
     *
     * <p>If the provided {@link Type} is null, this method returns null. Otherwise, it attempts
     * to resolve the Type into a TypeElement by first obtaining its fully qualified type name
     * and then using the ElementUtils from the ProcessingEnvironment.
     *
     * @param processingEnv The ProcessingEnvironment used to resolve TypeElements. Must not be null.
     * @param type          The Type to convert to a TypeElement. May be null.
     * @return The resolved TypeElement if available; otherwise, null.
     */
    static TypeElement getTypeElement(ProcessingEnvironment processingEnv, Type type) {
        return type == null ? null : getTypeElement(processingEnv, type.getTypeName());
    }

    /**
     * Retrieves the TypeElement corresponding to the given TypeMirror using the provided ProcessingEnvironment.
     *
     * <p>If the provided {@link TypeMirror} is null, this method returns null. Otherwise, it attempts
     * to resolve the TypeMirror into a TypeElement by first obtaining its fully qualified type name
     * and then using the ElementUtils from the ProcessingEnvironment.
     *
     * @param processingEnv The ProcessingEnvironment used to resolve TypeElements. Must not be null.
     * @param type          The TypeMirror to convert to a TypeElement. May be null.
     * @return The resolved TypeElement if available; otherwise, null.
     */
    static TypeElement getTypeElement(ProcessingEnvironment processingEnv, TypeMirror type) {
        return type == null ? null : getTypeElement(processingEnv, type.toString());
    }

    /**
     * Retrieves the TypeElement corresponding to the given type name using the provided ProcessingEnvironment.
     *
     * <p>If the provided {@link ProcessingEnvironment} or typeName is null, this method returns null.
     * Otherwise, it uses the ElementUtils from the ProcessingEnvironment to find and return the TypeElement.
     *
     * @param processingEnv The ProcessingEnvironment used to resolve TypeElements. Must not be null.
     * @param typeName      The fully qualified class name of the type to search for. May be null.
     * @return The resolved TypeElement if available; otherwise, null.
     */
    static TypeElement getTypeElement(ProcessingEnvironment processingEnv, CharSequence typeName) {
        if (processingEnv == null || typeName == null) {
            return null;
        }
        Elements elements = processingEnv.getElementUtils();
        return elements.getTypeElement(typeName);
    }

    /**
     * Retrieves the DeclaredType corresponding to the given Type using the provided ProcessingEnvironment.
     *
     * <p>If the provided {@link Type} is null, this method returns null. Otherwise, it attempts
     * to resolve the Type into a DeclaredType by first obtaining its fully qualified type name
     * and then using the ElementUtils from the ProcessingEnvironment to find the corresponding TypeElement.
     *
     * @param processingEnv The ProcessingEnvironment used to resolve DeclaredTypes. Must not be null.
     * @param type          The Type to convert to a DeclaredType. May be null.
     * @return The resolved DeclaredType if available; otherwise, null.
     */
    static DeclaredType getDeclaredType(ProcessingEnvironment processingEnv, Type type) {
        return type == null ? null : getDeclaredType(processingEnv, type.getTypeName());
    }

    /**
     * Retrieves the DeclaredType corresponding to the given TypeMirror using the provided ProcessingEnvironment.
     *
     * <p>If the provided {@link TypeMirror} is null, this method returns null. Otherwise, it attempts
     * to resolve the TypeMirror into a DeclaredType by first obtaining its fully qualified type name
     * and then using the ElementUtils from the ProcessingEnvironment to find the corresponding TypeElement.
     *
     * @param processingEnv The ProcessingEnvironment used to resolve DeclaredTypes. Must not be null.
     * @param type          The TypeMirror to convert to a DeclaredType. May be null.
     * @return The resolved DeclaredType if available; otherwise, null.
     */
    static DeclaredType getDeclaredType(ProcessingEnvironment processingEnv, TypeMirror type) {
        return type == null ? null : getDeclaredType(processingEnv, type.toString());
    }

    /**
     * Retrieves the DeclaredType corresponding to the given type name using the provided ProcessingEnvironment.
     *
     * <p>If the provided {@link ProcessingEnvironment} or typeName is null, this method returns null.
     * Otherwise, it uses the ElementUtils from the ProcessingEnvironment to find and return the TypeElement,
     * then converts it to a DeclaredType.
     *
     * @param processingEnv The ProcessingEnvironment used to resolve DeclaredTypes. Must not be null.
     * @param typeName      The fully qualified class name of the type to search for. May be null.
     * @return The resolved DeclaredType if available; otherwise, null.
     */
    static DeclaredType getDeclaredType(ProcessingEnvironment processingEnv, CharSequence typeName) {
        return ofDeclaredType(getTypeElement(processingEnv, typeName));
    }

    /**
     * Converts the given TypeMirror to its string representation.
     * This method is typically used to obtain a readable and fully qualified name of the type,
     * including any type parameters if present.
     *
     * @param type The TypeMirror to convert to a string, may be null.
     * @return The string representation of the TypeMirror, or null if the input is null.
     */
    static String toString(TypeMirror type) {
        return getTypeName(type);
    }

    /**
     * Gets the fully qualified name of the given TypeMirror, including type parameters if present.
     *
     * @param type The TypeMirror to get the name from, may be null.
     * @return The fully qualified name of the type including type parameters, or null if the input is null.
     */
    static String getTypeName(TypeMirror type) {
        if (type == null) {
            return null;
        }
        TypeElement element = ofTypeElement(type);
        if (element != null) {
            List<? extends TypeParameterElement> typeParameterElements = element.getTypeParameters();
            int typeParameterElementsSize = typeParameterElements.size();
            if (typeParameterElementsSize > 0) {
                List<TypeMirror> typeMirrors = invokeMethod(type, "getTypeArguments");
                int size = typeMirrors.size();
                if (size > 0) {
                    StringBuilder typeBuilder = new StringBuilder(element.toString());
                    typeBuilder.append(LESS_THAN_CHAR)
                            .append(typeMirrors)
                            .append(GREATER_THAN_CHAR);
                    return typeBuilder.toString();
                }
            }
        }
        return type.toString();
    }

    /**
     * Creates a TypeFinder instance for the specified TypeElement with configurable inclusion options.
     *
     * <p>This method allows searching for associated types based on the provided criteria:
     *
     * <ul>
     *   <li>{@code includeSelf} - Whether to include the type itself in the result.</li>
     *   <li>{@code includeHierarchicalTypes} - Whether to include types from the entire hierarchy (e.g., superclasses and interfaces).</li>
     *   <li>{@code includeSuperclass} - Whether to include direct or hierarchical superclasses based on includeHierarchicalTypes.</li>
     *   <li>{@code includeInterfaces} - Whether to include direct or hierarchical interfaces based on includeHierarchicalTypes.</li>
     * </ul>
     *
     * @param typeElement            The TypeElement to start the search from. Must not be null.
     * @param includeSelf            Whether to include the type itself in the result.
     * @param includeHierarchicalTypes Whether to include types from the entire hierarchy.
     * @param includeSuperclass      Whether to include direct or hierarchical superclasses.
     * @param includeInterfaces      Whether to include direct or hierarchical interfaces.
     * @return A TypeFinder instance configured with the given parameters.
     * @throws IllegalArgumentException if any parameter is invalid or if assertions fail.
     */
    static TypeFinder<TypeElement> typeElementFinder(TypeElement typeElement, boolean includeSelf,
                                                     boolean includeHierarchicalTypes, boolean includeSuperclass, boolean includeInterfaces) {
        return new TypeFinder(typeElement, TYPE_ELEMENT_GET_SUPERCLASS, TYPE_ELEMENT_GET_INTERFACES, includeSelf,
                includeHierarchicalTypes, includeSuperclass, includeInterfaces);
    }
}