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
import static io.microsphere.constants.SymbolConstants.COMMA_CHAR;
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
public interface TypeUtils {

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


    static boolean isSimpleType(Element element) {
        return element != null && isSimpleType(element.asType());
    }

    static boolean isSimpleType(TypeMirror type) {
        return type != null && SIMPLE_TYPE_NAMES.contains(type.toString());
    }

    static boolean isSameType(Element element, Type type) {
        return isSameType(element == null ? null : element.asType(), type);
    }

    static boolean isSameType(Element type, CharSequence typeName) {
        return isSameType(type == null ? null : type.asType(), typeName);
    }

    static boolean isSameType(TypeMirror typeMirror, Type type) {
        return isSameType(typeMirror, type == null ? null : type.getTypeName());
    }

    static boolean isSameType(TypeMirror type, CharSequence typeName) {
        if (type == null && typeName == null) {
            return true;
        }
        return Objects.equals(valueOf(type), valueOf(typeName));
    }

    static boolean isArrayType(TypeMirror type) {
        return type != null && ARRAY == type.getKind();
    }

    static boolean isArrayType(Element element) {
        return element != null && isArrayType(element.asType());
    }

    static boolean isEnumType(TypeMirror type) {
        DeclaredType declaredType = ofDeclaredType(type);
        return declaredType != null && ENUM == declaredType.asElement().getKind();
    }

    static boolean isEnumType(Element element) {
        return element != null && isEnumType(element.asType());
    }

    static boolean isClassType(TypeMirror type) {
        DeclaredType declaredType = ofDeclaredType(type);
        return declaredType != null && isClassType(declaredType.asElement());
    }

    static boolean isClassType(Element element) {
        return element != null && CLASS == element.getKind();
    }

    static boolean isPrimitiveType(TypeMirror type) {
        return type != null && type.getKind().isPrimitive();
    }

    static boolean isPrimitiveType(Element element) {
        return element != null && isPrimitiveType(element.asType());
    }

    static boolean isInterfaceType(TypeMirror type) {
        DeclaredType declaredType = ofDeclaredType(type);
        return declaredType != null && isInterfaceType(declaredType.asElement());
    }

    static boolean isInterfaceType(Element element) {
        return element != null && INTERFACE == element.getKind();
    }

    static boolean isAnnotationType(TypeMirror type) {
        DeclaredType declaredType = ofDeclaredType(type);
        return declaredType != null && isAnnotationType(declaredType.asElement());
    }

    static boolean isAnnotationType(Element element) {
        return element != null && ANNOTATION_TYPE == element.getKind();
    }

    static boolean isTypeElement(Element element) {
        return element instanceof TypeElement;
    }

    static boolean isTypeElement(TypeMirror type) {
        DeclaredType declaredType = ofDeclaredType(type);
        return declaredType != null && isTypeElement(declaredType.asElement());
    }

    static boolean isDeclaredType(Element element) {
        return element != null && isDeclaredType(element.asType());
    }

    static boolean isDeclaredType(TypeMirror type) {
        return type instanceof DeclaredType;
    }

    static TypeElement ofTypeElement(Element element) {
        return isTypeElement(element) ? (TypeElement) element : null;
    }

    static TypeElement ofTypeElement(TypeMirror type) {
        DeclaredType declaredType = ofDeclaredType(type);
        return ofTypeElement(declaredType);
    }

    static TypeElement ofTypeElement(DeclaredType declaredType) {
        if (declaredType != null) {
            return ofTypeElement(declaredType.asElement());
        }
        return null;
    }

    static DeclaredType ofDeclaredType(Element element) {
        return element == null ? null : ofDeclaredType(element.asType());
    }

    static DeclaredType ofDeclaredType(TypeMirror type) {
        return isDeclaredType(type) ? (DeclaredType) type : null;
    }

    static List<TypeMirror> ofTypeMirrors(Element... elements) {
        return ofTypeMirrors(ofList(elements));
    }

    static List<TypeMirror> ofTypeMirrors(Collection<? extends Element> elements) {
        return ofTypeMirrors(elements, EMPTY_PREDICATE_ARRAY);
    }

    static List<TypeMirror> ofTypeMirrors(Collection<? extends Element> elements, Predicate<? super TypeMirror>... typeFilters) {
        return isEmpty(elements) ? emptyList() :
                elements.stream().map(Element::asType).filter(and(typeFilters)).collect(toList());
    }

    static List<TypeElement> ofTypeElements(TypeMirror... types) {
        return ofTypeElements(ofList(types));
    }

    static List<TypeElement> ofTypeElements(Collection<? extends TypeMirror> types) {
        return ofTypeElements(types, EMPTY_PREDICATE_ARRAY);
    }

    static List<TypeElement> ofTypeElements(Collection<? extends TypeMirror> types, Predicate<? super TypeElement>... typeFilters) {
        return isEmpty(types) ? emptyList() :
                types.stream().map(TypeUtils::ofTypeElement).filter(Objects::nonNull).filter(and(typeFilters))
                        .collect(toList());
    }

    static List<DeclaredType> ofDeclaredTypes(Element... elements) {
        return ofDeclaredTypes(ofList(elements));
    }

    static List<DeclaredType> ofDeclaredTypes(Collection<? extends Element> elements) {
        return ofDeclaredTypes(elements, EMPTY_PREDICATE_ARRAY);
    }

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

    static TypeElement getTypeElementOfSuperclass(TypeElement type) {
        return type == null ? null : ofTypeElement(type.getSuperclass());
    }

    static List<TypeElement> getAllTypeElementsOfSuperTypes(TypeElement type) {
        return findAllTypeElementsOfSuperTypes(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<TypeElement> getAllTypeElementsOfSuperclasses(TypeElement type) {
        return findAllTypeElementsOfSuperclasses(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<TypeElement> getTypeElementsOfInterfaces(TypeElement type) {
        return findTypeElementsOfInterfaces(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<TypeElement> getAllTypeElementsOfInterfaces(TypeElement type) {
        return findAllTypeElementsOfInterfaces(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<TypeElement> getTypeElements(TypeElement type) {
        return getTypeElements(type, true, false, true, true);
    }

    static List<TypeElement> getAllTypeElements(TypeElement type) {
        return getTypeElements(type, true, true, true, true);
    }

    static List<TypeElement> getTypeElements(TypeElement type,
                                             boolean includeSelf,
                                             boolean includeHierarchicalTypes,
                                             boolean includeSuperClasses,
                                             boolean includeSuperInterfaces) {
        return findTypeElements(type, includeSelf, includeHierarchicalTypes, includeSuperClasses, includeSuperInterfaces, EMPTY_PREDICATE_ARRAY);
    }

    static List<TypeElement> findTypeElementsOfInterfaces(TypeElement type, Predicate<? super TypeElement>... interfaceFilters) {
        return findTypeElements(type, false, false, false, true, interfaceFilters);
    }

    static List<TypeElement> findAllTypeElementsOfSuperclasses(TypeElement type, Predicate<? super TypeElement>... typeFilters) {
        return findTypeElements(type, false, true, true, false, typeFilters);
    }

    static List<TypeElement> findAllTypeElementsOfInterfaces(TypeElement type, Predicate<? super TypeElement>... interfaceFilters) {
        return findTypeElements(type, false, true, false, true, interfaceFilters);
    }

    static List<TypeElement> findAllTypeElementsOfSuperTypes(TypeElement type, Predicate<? super TypeElement>... typeFilters) {
        return findTypeElements(type, false, true, true, true, typeFilters);
    }

    /**
     * @throws IllegalArgumentException if Any element of <code>typeFilters</code> array must not be null
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

    static DeclaredType getDeclaredTypeOfSuperclass(Element typeElement) {
        return typeElement == null ? null : getDeclaredTypeOfSuperclass(typeElement.asType());
    }

    static DeclaredType getDeclaredTypeOfSuperclass(TypeMirror type) {
        TypeElement superType = getTypeElementOfSuperclass(ofTypeElement(type));
        return superType == null ? null : ofDeclaredType(superType.asType());
    }

    static List<DeclaredType> getDeclaredTypesOfInterfaces(Element element) {
        return element == null ? emptyList() : findDeclaredTypesOfInterfaces(element.asType(), EMPTY_PREDICATE_ARRAY);
    }

    static List<DeclaredType> getDeclaredTypesOfInterfaces(TypeMirror type) {
        return findDeclaredTypesOfInterfaces(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<DeclaredType> getAllDeclaredTypesOfSuperclasses(Element type) {
        return type == null ? emptyList() : findAllDeclaredTypesOfSuperclasses(type.asType(), EMPTY_PREDICATE_ARRAY);
    }

    static List<DeclaredType> getAllDeclaredTypesOfSuperclasses(TypeMirror type) {
        return findAllDeclaredTypesOfSuperclasses(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<DeclaredType> getAllDeclaredTypesOfInterfaces(Element type) {
        return type == null ? emptyList() : findAllDeclaredTypesOfInterfaces(type.asType(), EMPTY_PREDICATE_ARRAY);
    }

    static List<DeclaredType> getAllDeclaredTypesOfInterfaces(TypeMirror type) {
        return findAllDeclaredTypesOfInterfaces(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<DeclaredType> getAllDeclaredTypesOfSuperTypes(Element type) {
        return type == null ? emptyList() : findAllDeclaredTypesOfSuperTypes(type.asType(), EMPTY_PREDICATE_ARRAY);
    }

    static List<DeclaredType> getAllDeclaredTypesOfSuperTypes(TypeMirror type) {
        return findAllDeclaredTypesOfSuperTypes(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<DeclaredType> getAllDeclaredTypes(Element type) {
        return type == null ? emptyList() : findAllDeclaredTypes(type.asType(), EMPTY_PREDICATE_ARRAY);
    }

    static List<DeclaredType> getAllDeclaredTypes(TypeMirror type) {
        return findAllDeclaredTypes(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<DeclaredType> getDeclaredTypes(Element type,
                                               boolean includeSelf,
                                               boolean includeHierarchicalTypes,
                                               boolean includeSuperClasses,
                                               boolean includeSuperInterfaces) {
        return getDeclaredTypes(type.asType(), includeSelf, includeHierarchicalTypes, includeSuperClasses, includeSuperInterfaces);
    }

    static List<DeclaredType> getDeclaredTypes(TypeMirror type,
                                               boolean includeSelf,
                                               boolean includeHierarchicalTypes,
                                               boolean includeSuperClasses,
                                               boolean includeSuperInterfaces) {
        return findDeclaredTypes(type, includeSelf, includeHierarchicalTypes, includeSuperClasses, includeSuperInterfaces, EMPTY_PREDICATE_ARRAY);
    }


    static List<DeclaredType> findDeclaredTypes(TypeMirror type, Type... excludedTypes) {
        return type == null ? emptyList() : findDeclaredTypes(ofTypeElement(type), excludedTypes);
    }

    static List<DeclaredType> findDeclaredTypes(Element type, Type... excludedTypes) {
        return type == null ? emptyList() : findDeclaredTypes(type, getTypeNames(excludedTypes));
    }

    static List<DeclaredType> findDeclaredTypes(TypeMirror type, CharSequence... excludedTypeNames) {
        return type == null ? emptyList() : findDeclaredTypes(ofTypeElement(type), excludedTypeNames);
    }

    static List<DeclaredType> findDeclaredTypes(Element type, CharSequence... excludedTypeNames) {
        return type == null ? emptyList() : findDeclaredTypes(type, false, false, true, true, t -> !contains(excludedTypeNames, t.toString()));
    }

    static List<DeclaredType> findDeclaredTypesOfInterfaces(Element type, Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : findDeclaredTypesOfInterfaces(type.asType(), typeFilters);
    }

    static List<DeclaredType> findDeclaredTypesOfInterfaces(TypeMirror type, Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : ofDeclaredTypes(getTypeElementsOfInterfaces(ofTypeElement(type)), typeFilters);
    }

    static List<DeclaredType> findAllDeclaredTypesOfSuperclasses(Element type, Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : findAllDeclaredTypesOfSuperclasses(type.asType(), typeFilters);
    }

    static List<DeclaredType> findAllDeclaredTypesOfSuperclasses(TypeMirror type, Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : ofDeclaredTypes(getAllTypeElementsOfSuperclasses(ofTypeElement(type)), typeFilters);
    }

    static List<DeclaredType> findAllDeclaredTypesOfInterfaces(Element type, Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : findAllDeclaredTypesOfInterfaces(type.asType(), typeFilters);
    }

    static List<DeclaredType> findAllDeclaredTypesOfInterfaces(TypeMirror type, Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : ofDeclaredTypes(getAllTypeElementsOfInterfaces(ofTypeElement(type)), typeFilters);
    }

    static List<DeclaredType> findAllDeclaredTypesOfSuperTypes(Element type, Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : findAllDeclaredTypesOfSuperTypes(type.asType(), typeFilters);
    }

    static List<DeclaredType> findAllDeclaredTypesOfSuperTypes(TypeMirror type, Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : ofDeclaredTypes(getAllTypeElementsOfSuperTypes(ofTypeElement(type)), typeFilters);
    }

    static List<DeclaredType> findAllDeclaredTypes(TypeMirror type, Type... excludedTypes) {
        return type == null ? emptyList() : findAllDeclaredTypes(ofTypeElement(type), excludedTypes);
    }

    static List<DeclaredType> findAllDeclaredTypes(Element type, Type... excludedTypes) {
        return type == null ? emptyList() : findAllDeclaredTypes(type, getTypeNames(excludedTypes));
    }

    static List<DeclaredType> findAllDeclaredTypes(TypeMirror type, CharSequence... excludedTypeNames) {
        return type == null ? emptyList() : findAllDeclaredTypes(ofTypeElement(type), excludedTypeNames);
    }

    static List<DeclaredType> findAllDeclaredTypes(Element type, CharSequence... excludedTypeNames) {
        return type == null ? emptyList() : findAllDeclaredTypes(type, t -> !contains(excludedTypeNames, t.toString()));
    }

    static List<DeclaredType> findAllDeclaredTypes(Element type, Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : findAllDeclaredTypes(type.asType(), typeFilters);
    }

    static List<DeclaredType> findAllDeclaredTypes(TypeMirror type, Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : ofDeclaredTypes(getAllTypeElements(ofTypeElement(type)), typeFilters);
    }

    static List<DeclaredType> findDeclaredTypes(Element type,
                                                boolean includeSelf,
                                                boolean includeHierarchicalTypes,
                                                boolean includeSuperClasses,
                                                boolean includeSuperInterfaces,
                                                Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : findDeclaredTypes(type.asType(), includeSelf, includeHierarchicalTypes, includeSuperClasses, includeSuperInterfaces, typeFilters);
    }

    static List<DeclaredType> findDeclaredTypes(TypeMirror type,
                                                boolean includeSelf,
                                                boolean includeHierarchicalTypes,
                                                boolean includeSuperClasses,
                                                boolean includeSuperInterfaces,
                                                Predicate<? super DeclaredType>... typeFilters) {
        return type == null ? emptyList() : ofDeclaredTypes(getTypeElements(ofTypeElement(type), includeSelf, includeHierarchicalTypes, includeSuperClasses, includeSuperInterfaces), typeFilters);
    }

    static List<TypeMirror> getTypeMirrorsOfInterfaces(TypeMirror type) {
        return type == null ? emptyList() : findTypeMirrorsOfInterfaces(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<TypeMirror> getTypeMirrorsOfInterfaces(TypeElement type) {
        return type == null ? emptyList() : findTypeMirrorsOfInterfaces(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<TypeMirror> findTypeMirrorsOfInterfaces(TypeMirror type, Predicate<TypeMirror>... interfaceFilters) {
        return type == null ? emptyList() : findTypeMirrorsOfInterfaces(ofTypeElement(type), interfaceFilters);
    }

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

    static List<TypeMirror> getAllTypeMirrorsOfInterfaces(TypeMirror type) {
        return findAllTypeMirrorsOfInterfaces(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<TypeMirror> getAllTypeMirrorsOfInterfaces(TypeElement type) {
        return findAllTypeMirrorsOfInterfaces(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<TypeMirror> findAllTypeMirrorsOfInterfaces(TypeMirror type, Predicate<TypeMirror>... interfaceFilters) {
        return type == null ? emptyList() : findAllTypeMirrorsOfInterfaces(ofTypeElement(type), interfaceFilters);
    }

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

    static TypeMirror findInterfaceTypeMirror(Element type, Type interfaceType) {
        return findInterfaceTypeMirror(type, interfaceType.getTypeName());
    }

    static TypeMirror findInterfaceTypeMirror(TypeMirror type, Type interfaceType) {
        return findInterfaceTypeMirror(type, interfaceType.getTypeName());
    }

    static TypeMirror findInterfaceTypeMirror(Element type, CharSequence interfaceClassName) {
        return type == null ? null : findInterfaceTypeMirror(type.asType(), interfaceClassName);
    }

    static TypeMirror findInterfaceTypeMirror(TypeMirror type, CharSequence interfaceClassName) {
        return filterFirst(getAllTypeMirrorsOfInterfaces(type), t -> isSameType(t, interfaceClassName));
    }

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

    static TypeMirror getTypeMirror(ProcessingEnvironment processingEnv, Type type) {
        TypeElement typeElement = getTypeElement(processingEnv, type);
        return typeElement == null ? null : typeElement.asType();
    }

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

    static TypeElement getTypeElement(ProcessingEnvironment processingEnv, Type type) {
        return type == null ? null : getTypeElement(processingEnv, type.getTypeName());
    }

    static TypeElement getTypeElement(ProcessingEnvironment processingEnv, TypeMirror type) {
        return type == null ? null : getTypeElement(processingEnv, type.toString());
    }

    static TypeElement getTypeElement(ProcessingEnvironment processingEnv, CharSequence typeName) {
        if (processingEnv == null || typeName == null) {
            return null;
        }
        Elements elements = processingEnv.getElementUtils();
        return elements.getTypeElement(typeName);
    }

    static DeclaredType getDeclaredType(ProcessingEnvironment processingEnv, Type type) {
        return type == null ? null : getDeclaredType(processingEnv, type.getTypeName());
    }

    static DeclaredType getDeclaredType(ProcessingEnvironment processingEnv, TypeMirror type) {
        return type == null ? null : getDeclaredType(processingEnv, type.toString());
    }

    static DeclaredType getDeclaredType(ProcessingEnvironment processingEnv, CharSequence typeName) {
        return ofDeclaredType(getTypeElement(processingEnv, typeName));
    }

    static String toString(TypeMirror type) {
        return getTypeName(type);
    }

    static String getTypeName(TypeMirror type) {
        if (type == null) {
            return null;
        }
        TypeElement element = ofTypeElement(type);
        if (element != null) {
            List<? extends TypeParameterElement> typeParameterElements = element.getTypeParameters();
            if (!typeParameterElements.isEmpty()) {
                List<TypeMirror> typeMirrors = invokeMethod(type, "getTypeArguments");
                StringBuilder typeBuilder = new StringBuilder(element.toString());
                typeBuilder.append("<");
                for (int i = 0; i < typeMirrors.size(); i++) {
                    if (i > 0) {
                        typeBuilder.append(COMMA_CHAR);
                    }
                    typeBuilder.append(toString(typeMirrors.get(i)));
                }
                typeBuilder.append(">");
                return typeBuilder.toString();
            }
        }
        return type.toString();
    }

    static TypeFinder<TypeElement> typeElementFinder(TypeElement typeElement, boolean includeSelf,
                                                     boolean includeHierarchicalTypes, boolean includeSuperclass, boolean includeInterfaces) {
        return new TypeFinder(typeElement, TYPE_ELEMENT_GET_SUPERCLASS, TYPE_ELEMENT_GET_INTERFACES, includeSelf,
                includeHierarchicalTypes, includeSuperclass, includeInterfaces);
    }
}