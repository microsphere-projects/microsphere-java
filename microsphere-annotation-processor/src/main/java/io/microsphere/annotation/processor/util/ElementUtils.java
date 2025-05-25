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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.lang.annotation.ElementType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static io.microsphere.annotation.processor.util.TypeUtils.isSameType;
import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.reflect.TypeUtils.getTypeNames;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static io.microsphere.util.ArrayUtils.length;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.OTHER;
import static javax.lang.model.element.ElementKind.valueOf;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * The utility class for {@link Element}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Element
 * @see Enum
 * @since 1.0.0
 */
public interface ElementUtils extends Utils {

    /**
     * Returns {@code true} if this is a kind of class:
     * either {@code CLASS} or {@code ENUM} or {@code RECORD}.
     *
     * @return {@code true} if this is a kind of class
     * @see ElementKind#isClass()
     */
    static boolean isClass(ElementKind kind) {
        return kind != null && kind.isClass();
    }

    /**
     * Returns {@code true} if this is a kind of interface:
     * either {@code INTERFACE} or {@code ANNOTATION_TYPE}.
     *
     * @param kind {@link ElementKind}
     * @return {@code true} if this is a kind of interface
     */
    static boolean isInterface(ElementKind kind) {
        return kind != null && kind.isInterface();
    }

    /**
     * {@return {@code true} if this is a kind of declared type, a {@linkplain ElementKind#isClass() class} or
     * an {@linkplain ElementKind#isInterface() interface}, and {@code false} otherwise}
     *
     * @param kind {@link ElementKind}
     * @see ElementKind#isDeclaredType()
     */
    static boolean isDeclaredType(ElementKind kind) {
        return isClass(kind) || isInterface(kind);
    }

    /**
     * Returns {@code true} if this is a kind of field:
     * either {@code FIELD} or {@code ENUM_CONSTANT}.
     *
     * @param kind {@link ElementKind}
     * @return {@code true} if this is a kind of field
     * @see ElementKind#isField()
     */
    static boolean isField(ElementKind kind) {
        return kind != null && kind.isField();
    }

    /**
     * Returns {@code true} if this is a kind of executable: either
     * {@code METHOD} or {@code CONSTRUCTOR} or {@code STATIC_INIT} or
     * {@code INSTANCE_INIT}.
     *
     * @param kind {@link ElementKind}
     * @return {@code true} if this is a kind of executable
     * @see ElementKind#isExecutable()
     */
    static boolean isExecutable(ElementKind kind) {
        if (kind != null) {
            switch (kind) {
                case METHOD:
                case CONSTRUCTOR:
                case STATIC_INIT:
                case INSTANCE_INIT:
                    return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if this is a kind of initializer: either
     * {@code STATIC_INIT} or {@code INSTANCE_INIT}.
     *
     * @param kind {@link ElementKind}
     * @return {@code true} if this is a kind of initializer
     * @see ElementKind#isInitializer()
     */
    static boolean isInitializer(ElementKind kind) {
        if (kind != null) {
            switch (kind) {
                case STATIC_INIT:
                case INSTANCE_INIT:
                    return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if this is a kind of variable: including
     * {@code ENUM_CONSTANT}, {@code FIELD}, {@code PARAMETER},
     * {@code LOCAL_VARIABLE}, {@code EXCEPTION_PARAMETER},
     * {@code RESOURCE_VARIABLE}, and {@code BINDING_VARIABLE}.
     *
     * @param kind {@link ElementKind}
     * @return {@code true} if this is a kind of variable
     * @see ElementKind#isVariable()
     */
    static boolean isVariable(ElementKind kind) {
        if (kind != null) {
            switch (kind) {
                case ENUM_CONSTANT:
                case FIELD:
                case PARAMETER:
                case LOCAL_VARIABLE:
                case EXCEPTION_PARAMETER:
                case RESOURCE_VARIABLE:
                    return true;
            }
            // To be compatible with JDK 16
            return "BINDING_VARIABLE".equals(kind.name());
        }
        return false;
    }

    /**
     * Converts the specified {@link ElementType} to an equivalent {@link ElementKind}.
     * <p>
     * If the provided {@code elementType} is {@code null}, this method returns {@link ElementKind#OTHER}.
     * </p>
     *
     * @param elementType the ElementType to convert, may be {@code null}
     * @return the corresponding ElementKind, never {@code null}
     */
    static ElementKind toElementKind(ElementType elementType) {
        if (elementType == null) {
            return OTHER;
        }
        switch (elementType) {
            case TYPE:
            case TYPE_USE:
                return CLASS;
            default:
                return valueOf(elementType.name());
        }
    }

    /**
     * Checks whether the specified {@link ElementKind} matches the specified {@link ElementType}.
     *
     * @param elementKind the ElementKind to check
     * @param elementType the ElementType to check
     * @return {@code true} if the ElementKind matches the ElementType, {@code false} otherwise
     */
    static boolean matches(ElementKind elementKind, ElementType elementType) {
        return elementKind == toElementKind(elementType);
    }

    /**
     * Checks whether the specified {@link ElementKind} matches any of the specified {@link ElementType}s.
     *
     * @param elementKind  the ElementKind to check
     * @param elementTypes the ElementTypes to check
     * @return {@code true} if the ElementKind matches any of the ElementTypes, {@code false} otherwise
     */
    static boolean matches(ElementKind elementKind, ElementType... elementTypes) {
        int length = length(elementTypes);
        if (length < 1) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (matches(elementKind, elementTypes[i])) {
                return true;
            }
        }
        return false;
    }

    static boolean matchesElementKind(Element member, ElementKind kind) {
        return member == null || kind == null ? false : kind.equals(member.getKind());
    }

    static boolean isPublicNonStatic(Element member) {
        return hasModifiers(member, PUBLIC) && !hasModifiers(member, STATIC);
    }

    static boolean hasModifiers(Element member, Modifier... modifiers) {
        if (member == null || modifiers == null) {
            return false;
        }
        Set<Modifier> actualModifiers = member.getModifiers();
        for (Modifier modifier : modifiers) {
            if (!actualModifiers.contains(modifier)) {
                return false;
            }
        }
        return true;
    }

    static List<? extends Element> filterElements(List<? extends Element> elements, Predicate<? super Element>... elementPredicates) {
        if (isEmpty(elements)) {
            return emptyList();
        }
        if (isNotEmpty(elementPredicates)) {
            Predicate predicate = and(elementPredicates);
            elements = (List) elements.stream().filter(predicate).collect(toList());
        }
        return elements.isEmpty() ? emptyList() : elements;
    }

    static boolean matchParameterTypes(List<? extends VariableElement> parameters, Type... parameterTypes) {
        return parameters == null || parameterTypes == null ? false : matchParameterTypeNames(parameters, getTypeNames(parameterTypes));
    }

    static boolean matchParameterTypeNames(List<? extends VariableElement> parameters, CharSequence... parameterTypeNames) {
        if (parameters == null || parameterTypeNames == null) {
            return false;
        }

        int length = length(parameterTypeNames);
        int size = parameters.size();

        if (size != length) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            VariableElement parameter = parameters.get(i);
            if (!isSameType(parameter, parameterTypeNames[i])) {
                return false;
            }
        }
        return true;
    }
}
