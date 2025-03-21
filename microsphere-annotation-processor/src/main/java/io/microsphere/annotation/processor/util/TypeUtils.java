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
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.lang.function.Streams.filterAll;
import static io.microsphere.lang.function.Streams.filterFirst;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static io.microsphere.util.ClassUtils.SIMPLE_TYPES;
import static java.lang.String.valueOf;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
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

    static boolean isSameType(TypeMirror type, CharSequence typeName) {
        if (type == null || typeName == null) {
            return false;
        }
        return Objects.equals(valueOf(type), valueOf(typeName));
    }

    static boolean isSameType(TypeMirror typeMirror, Type type) {
        return type != null && isSameType(typeMirror, type.getTypeName());
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

    static boolean isDeclaredType(Element element) {
        return element != null && isDeclaredType(element.asType());
    }

    static boolean isDeclaredType(TypeMirror type) {
        return type instanceof DeclaredType;
    }

    static DeclaredType ofDeclaredType(Element element) {
        return element == null ? null : ofDeclaredType(element.asType());
    }

    static DeclaredType ofDeclaredType(TypeMirror type) {
        return isDeclaredType(type) ? (DeclaredType) type : null;
    }

    static boolean isTypeElement(Element element) {
        return element instanceof TypeElement;
    }

    static boolean isTypeElement(TypeMirror type) {
        DeclaredType declaredType = ofDeclaredType(type);
        return declaredType != null && isTypeElement(declaredType.asElement());
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

    static List<DeclaredType> ofDeclaredTypes(Element... elements) {
        return ofDeclaredTypes(ofList(elements));
    }

    static List<DeclaredType> ofDeclaredTypes(Collection<? extends Element> elements) {
        return ofDeclaredTypes(elements, EMPTY_PREDICATE_ARRAY);
    }

    static List<DeclaredType> ofDeclaredTypes(Collection<? extends Element> elements,
                                              Predicate<? super DeclaredType>... typeFilters) {
        return isEmpty(elements) ? emptyList() :
                elements.stream()
                        .map(TypeUtils::ofTypeElement)
                        .filter(Objects::nonNull)
                        .map(Element::asType)
                        .map(TypeUtils::ofDeclaredType)
                        .filter(Objects::nonNull)
                        .filter(and(typeFilters))
                        .collect(toList());
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

    static List<TypeElement> getTypeElements(ProcessingEnvironment processingEnv, Type... types) {
        return isEmpty(types) ? emptyList() : of(types).map(t -> getTypeElement(processingEnv, t)).collect(toList());
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

    static TypeElement getSuperTypeElement(TypeElement type) {
        return type == null ? null : ofTypeElement(type.getSuperclass());
    }

    static List<TypeElement> getAllSuperTypeElements(TypeElement type) {
        return findAllSuperTypeElements(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<TypeElement> getAllTypeElements(TypeElement type) {
        return getTypeElements(type, true, true, true, true);
    }

    static List<TypeElement> getInterfaceTypeElements(TypeElement type) {
        return findInterfaceTypeElements(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<TypeElement> getAllInterfaceTypeElements(TypeElement type) {
        return findAllInterfaceTypeElements(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<TypeElement> getTypeElements(TypeElement type,
                                             boolean includeSelf,
                                             boolean includeHierarchicalTypes,
                                             boolean includeSuperTypes,
                                             boolean includeSuperInterfaces) {
        return findTypeElements(type, includeSelf, includeHierarchicalTypes, includeSuperTypes, includeSuperInterfaces, EMPTY_PREDICATE_ARRAY);
    }


    static List<TypeElement> findInterfaceTypeElements(TypeElement type, Predicate<? super TypeElement>... interfaceFilters) {
        return findTypeElements(type, false, false, false, true, interfaceFilters);
    }

    static List<TypeElement> findAllInterfaceTypeElements(TypeElement type, Predicate<? super TypeElement>... interfaceFilters) {
        return findTypeElements(type, false, true, false, true, interfaceFilters);
    }


    static List<TypeElement> findAllSuperTypeElements(TypeElement type, Predicate<? super TypeElement>... typeFilters) {
        return findTypeElements(type, false, true, true, true, typeFilters);
    }

    static List<TypeElement> findTypeElements(TypeElement type,
                                              boolean includeSelf,
                                              boolean includeHierarchicalTypes,
                                              boolean includeSuperTypes,
                                              boolean includeSuperInterfaces,
                                              Predicate<? super TypeElement>... typeFilters) {
        return typeElementFinder(type, includeSelf, includeHierarchicalTypes, includeSuperTypes, includeSuperInterfaces).findTypes(typeFilters);
    }

    static List<DeclaredType> getAllDeclaredTypes(TypeMirror type) {
        return findAllDeclaredTypes(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<DeclaredType> findAllDeclaredTypes(TypeMirror type, Type... excludedTypes) {
        return findAllDeclaredTypes(type, of(excludedTypes).map(Type::getTypeName).toArray(String[]::new));
    }

    static List<DeclaredType> findAllDeclaredTypes(TypeMirror type, Predicate<? super DeclaredType>... typeFilters) {
        return filterAll(ofDeclaredTypes(getAllTypeElements(ofTypeElement(type))), typeFilters);
    }

    static List<DeclaredType> findAllDeclaredTypes(TypeMirror type, CharSequence... excludedTypeNames) {
        Set<String> typeNames = of(excludedTypeNames).map(CharSequence::toString).collect(toSet());
        return findAllDeclaredTypes(type, t -> !typeNames.contains(t.toString()));
    }

    static List<DeclaredType> getDeclaredTypes(TypeElement type,
                                               boolean includeSelf,
                                               boolean includeHierarchicalTypes,
                                               boolean includeSuperTypes,
                                               boolean includeSuperInterfaces) {
        return getDeclaredTypes(type.asType(), includeSelf, includeHierarchicalTypes, includeSuperTypes, includeSuperInterfaces);
    }

    static List<DeclaredType> getDeclaredTypes(TypeMirror type,
                                               boolean includeSelf,
                                               boolean includeHierarchicalTypes,
                                               boolean includeSuperTypes,
                                               boolean includeSuperInterfaces) {
        return findDeclaredTypes(type, includeSelf, includeHierarchicalTypes, includeSuperTypes, includeSuperInterfaces, EMPTY_PREDICATE_ARRAY);
    }

    static List<DeclaredType> findDeclaredTypes(TypeElement type,
                                                boolean includeSelf,
                                                boolean includeHierarchicalTypes,
                                                boolean includeSuperTypes,
                                                boolean includeSuperInterfaces,
                                                Predicate<? super DeclaredType>... typeFilters) {
        return findDeclaredTypes(type.asType(), includeSelf, includeHierarchicalTypes, includeSuperTypes, includeSuperInterfaces, typeFilters);
    }

    static List<DeclaredType> findDeclaredTypes(TypeMirror type,
                                                boolean includeSelf,
                                                boolean includeHierarchicalTypes,
                                                boolean includeSuperTypes,
                                                boolean includeSuperInterfaces,
                                                Predicate<? super DeclaredType>... typeFilters) {
        return ofDeclaredTypes(getTypeElements(ofTypeElement(type), includeSelf, includeHierarchicalTypes, includeSuperTypes, includeSuperInterfaces), typeFilters);
    }

    static DeclaredType getSuperDeclaredType(TypeMirror type) {
        TypeElement superType = getSuperTypeElement(ofTypeElement(type));
        return superType == null ? null : ofDeclaredType(superType.asType());
    }

    static List<DeclaredType> getAllSuperDeclaredTypes(TypeMirror type) {
        return getAllSuperDeclaredTypes(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<DeclaredType> getAllSuperDeclaredTypes(TypeMirror type, Predicate<? super DeclaredType>... typeFilters) {
        return filterAll(ofDeclaredTypes(getAllSuperTypeElements(ofTypeElement(type))), typeFilters);
    }

    static List<TypeMirror> getInterfaceTypeMirrors(TypeElement type, Predicate<TypeMirror>... interfaceFilters) {
        return type == null ? emptyList() : filterAll((List<TypeMirror>) ofTypeElement(type).getInterfaces(), interfaceFilters);
    }

    static List<TypeMirror> getInterfaceTypeMirrors(TypeMirror type, Predicate<TypeMirror>... interfaceFilters) {
        return getInterfaceTypeMirrors(ofTypeElement(type), interfaceFilters);
    }

    static List<? extends TypeMirror> getAllInterfaces(TypeMirror type) {
        return findAllInterfaces(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<? extends TypeMirror> findAllInterfaces(TypeMirror type, Predicate<TypeMirror>... interfaceFilters) {
        if (type == null) {
            return emptyList();
        }
        List<TypeElement> interfaces = getTypeElements(ofTypeElement(type), false, true, false, true);
        return interfaces.stream()
                .map(TypeElement::asType)
                .filter(and(interfaceFilters))
                .collect(toList());
    }

    static TypeMirror findInterfaceTypeMirror(TypeMirror type, CharSequence interfaceClassName) {
        return filterFirst(getAllInterfaces(type), t -> isSameType(t, interfaceClassName));
    }

    static List<TypeMirror> getTypeMirrors(ProcessingEnvironment processingEnv, Type... types) {
        return isEmpty(types) ? emptyList() : of(types).map(t -> getTypeMirror(processingEnv, t)).collect(toList());
    }

    static TypeMirror getTypeMirror(ProcessingEnvironment processingEnv, Type type) {
        TypeElement typeElement = getTypeElement(processingEnv, type);
        return typeElement == null ? null : typeElement.asType();
    }

    static List<DeclaredType> listDeclaredTypes(Collection<? extends Element> elements) {
        return new ArrayList<>(ofDeclaredTypes(elements));
    }

    static List<TypeElement> listTypeElements(Collection<? extends TypeMirror> types) {
        return new ArrayList<>(ofTypeElements(types));
    }

    static URL getResource(ProcessingEnvironment processingEnv, Element type) {
        return getResource(processingEnv, ofDeclaredType(type));
    }

    static URL getResource(ProcessingEnvironment processingEnv, TypeMirror type) {
        return type == null ? null : getResource(processingEnv, type.toString());
    }

    static URL getResource(ProcessingEnvironment processingEnv, CharSequence type) {
        String relativeName = getResourceName(type);
        URL resource = null;
        try {
            if (relativeName != null) {
                FileObject fileObject = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", relativeName);
                resource = fileObject.toUri().toURL();
                // try to open it
                resource.getContent();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return resource;
    }

    static String getResourceName(CharSequence type) {
        return type == null ? null : type.toString().replace('.', '/').concat(".class");
    }

    static String toString(TypeMirror type) {
        TypeElement element = ofTypeElement(type);
        if (element != null) {
            List<? extends TypeParameterElement> typeParameterElements = element.getTypeParameters();
            if (!typeParameterElements.isEmpty()) {
                List<TypeMirror> typeMirrors = invokeMethod(type, "getTypeArguments");
                StringBuilder typeBuilder = new StringBuilder(element.toString());
                typeBuilder.append("<");
                for (int i = 0; i < typeMirrors.size(); i++) {
                    if (i > 0) {
                        typeBuilder.append(", ");
                    }
                    typeBuilder.append(typeMirrors.get(i).toString());
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