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
     * Returns {@code true} if the specified {@link ElementKind} represents a class-like element,
     * including:
     * <ul>
     *   <li>{@link ElementKind#CLASS}</li>
     *   <li>{@link ElementKind#ENUM}</li>
     *   <li>{@link ElementKind#RECORD}</li>
     * </ul>
     *
     * <p>This method serves as a convenience wrapper around {@link ElementKind#isClass()}.
     * It also guards against null input by returning {@code false} when the argument is null.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ElementKind classKind = ElementKind.CLASS;
     * boolean result = isClass(classKind); // returns true
     *
     * ElementKind methodKind = ElementKind.METHOD;
     * result = isClass(methodKind); // returns false
     *
     * result = isClass(null); // returns false
     * }</pre>
     *
     * @param kind the ElementKind to check, may be null
     * @return {@code true} if the kind is a class-like element, {@code false} otherwise
     * @see ElementKind#isClass()
     */
    static boolean isClass(ElementKind kind) {
        return kind != null && kind.isClass();
    }

    /**
     * Returns {@code true} if this is a kind of interface:
     * either {@code INTERFACE} or {@code ANNOTATION_TYPE}.
     *
     * <p>
     * This method serves as a convenience wrapper around {@link ElementKind#isInterface()}.
     * It also guards against null input by returning {@code false} when the argument is null.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ElementKind interfaceKind = ElementKind.INTERFACE;
     * boolean result = isInterface(interfaceKind); // returns true
     *
     * ElementKind annotationKind = ElementKind.ANNOTATION_TYPE;
     * result = isInterface(annotationKind); // returns true
     *
     * ElementKind classKind = ElementKind.CLASS;
     * result = isInterface(classKind); // returns false
     *
     * result = isInterface(null); // returns false
     * }</pre>
     *
     * @param kind the ElementKind to check, may be null
     * @return {@code true} if the kind is an interface-like element, {@code false} otherwise
     * @see ElementKind#isInterface()
     */
    static boolean isInterface(ElementKind kind) {
        return kind != null && kind.isInterface();
    }

    /**
     * Returns {@code true} if the specified {@link ElementKind} represents a declared type,
     * which includes both {@linkplain #isClass(ElementKind) class-like} and
     * {@linkplain #isInterface(ElementKind) interface-like} elements.
     *
     * <p>This method serves as a convenience wrapper that combines the checks for class and interface kinds.
     * It also guards against null input by safely delegating to the respective methods.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ElementKind classKind = ElementKind.CLASS;
     * boolean result = isDeclaredType(classKind); // returns true
     *
     * ElementKind interfaceKind = ElementKind.INTERFACE;
     * result = isDeclaredType(interfaceKind); // returns true
     *
     * ElementKind methodKind = ElementKind.METHOD;
     * result = isDeclaredType(methodKind); // returns false
     *
     * result = isDeclaredType(null); // returns false
     * }</pre>
     *
     * @param kind the ElementKind to check, may be null
     * @return {@code true} if the kind is a declared type, {@code false} otherwise
     * @see #isClass(ElementKind)
     * @see #isInterface(ElementKind)
     */
    static boolean isDeclaredType(ElementKind kind) {
        return isClass(kind) || isInterface(kind);
    }

    /**
     * Returns {@code true} if this is a kind of field:
     * either {@code FIELD} or {@code ENUM_CONSTANT}.
     *
     * <p>
     * This method serves as a convenience wrapper around {@link ElementKind#isField()}.
     * It also guards against null input by returning {@code false} when the argument is null.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ElementKind fieldKind = ElementKind.FIELD;
     * boolean result = isField(fieldKind); // returns true
     *
     * ElementKind enumConstantKind = ElementKind.ENUM_CONSTANT;
     * result = isField(enumConstantKind); // returns true
     *
     * ElementKind methodKind = ElementKind.METHOD;
     * result = isField(methodKind); // returns false
     *
     * result = isField(null); // returns false
     * }</pre>
     *
     * @param kind the ElementKind to check, may be null
     * @return {@code true} if the kind is a field-like element, {@code false} otherwise
     * @see ElementKind#isField()
     */
    static boolean isField(ElementKind kind) {
        return kind != null && kind.isField();
    }

    /**
     * Returns {@code true} if this is a kind of executable: either
     * {@code METHOD}, {@code CONSTRUCTOR}, {@code STATIC_INIT}, or
     * {@code INSTANCE_INIT}.
     *
     * <p>This method serves as a convenience wrapper around {@link ElementKind#isExecutable()}.
     * It also guards against null input by returning {@code false} when the argument is null.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ElementKind methodKind = ElementKind.METHOD;
     * boolean result = isExecutable(methodKind); // returns true
     *
     * ElementKind constructorKind = ElementKind.CONSTRUCTOR;
     * result = isExecutable(constructorKind); // returns true
     *
     * ElementKind staticInitKind = ElementKind.STATIC_INIT;
     * result = isExecutable(staticInitKind); // returns true
     *
     * ElementKind instanceInitKind = ElementKind.INSTANCE_INIT;
     * result = isExecutable(instanceInitKind); // returns true
     *
     * ElementKind classKind = ElementKind.CLASS;
     * result = isExecutable(classKind); // returns false
     *
     * result = isExecutable(null); // returns false
     * }</pre>
     *
     * @param kind the ElementKind to check, may be null
     * @return {@code true} if the kind is an executable-like element, {@code false} otherwise
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
     * Returns {@code true} if the specified {@link ElementKind} represents a member element,
     * which includes both {@linkplain #isField(ElementKind) field-like} and
     * {@linkplain #isExecutable(ElementKind) executable-like} elements.
     *
     * <p>This method serves as a convenience wrapper that combines the checks for field and executable kinds.
     * It also guards against null input by safely delegating to the respective methods.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ElementKind fieldKind = ElementKind.FIELD;
     * boolean result = isMember(fieldKind); // returns true
     *
     * ElementKind methodKind = ElementKind.METHOD;
     * result = isMember(methodKind); // returns true
     *
     * ElementKind classKind = ElementKind.CLASS;
     * result = isMember(classKind); // returns false
     *
     * result = isMember(null); // returns false
     * }</pre>
     *
     * @param kind the ElementKind to check, may be null
     * @return {@code true} if the kind is a member-like element, {@code false} otherwise
     * @see #isField(ElementKind)
     * @see #isExecutable(ElementKind)
     */
    static boolean isMember(ElementKind kind) {
        return isField(kind) || isExecutable(kind);
    }

    /**
     * Returns {@code true} if the specified {@link ElementKind} represents an initializer,
     * either {@code STATIC_INIT} or {@code INSTANCE_INIT}.
     *
     * <p>
     * This method serves as a convenience wrapper around {@link ElementKind#isInitializer()}.
     * It also guards against null input by returning {@code false} when the argument is null.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ElementKind staticInitKind = ElementKind.STATIC_INIT;
     * boolean result = isInitializer(staticInitKind); // returns true
     *
     * ElementKind instanceInitKind = ElementKind.INSTANCE_INIT;
     * result = isInitializer(instanceInitKind); // returns true
     *
     * ElementKind methodKind = ElementKind.METHOD;
     * result = isInitializer(methodKind); // returns false
     *
     * result = isInitializer(null); // returns false
     * }</pre>
     *
     * @param kind the ElementKind to check, may be null
     * @return {@code true} if the kind is an initializer-like element, {@code false} otherwise
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
     * Returns {@code true} if the specified {@link ElementKind} represents a variable-like element,
     * including:
     * <ul>
     *   <li>{@link ElementKind#ENUM_CONSTANT}</li>
     *   <li>{@link ElementKind#FIELD}</li>
     *   <li>{@link ElementKind#PARAMETER}</li>
     *   <li>{@link ElementKind#LOCAL_VARIABLE}</li>
     *   <li>{@link ElementKind#EXCEPTION_PARAMETER}</li>
     *   <li>{@link ElementKind#RESOURCE_VARIABLE}</li>
     *   <li>{@link ElementKind#BINDING_VARIABLE}</li>
     * </ul>
     *
     * <p>This method serves as a convenience wrapper around {@link ElementKind#isVariable()}.
     * It also guards against null input by returning {@code false} when the argument is null.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ElementKind enumConstantKind = ElementKind.ENUM_CONSTANT;
     * boolean result = isVariable(enumConstantKind); // returns true
     *
     * ElementKind fieldKind = ElementKind.FIELD;
     * result = isVariable(fieldKind); // returns true
     *
     * ElementKind parameterKind = ElementKind.PARAMETER;
     * result = isVariable(parameterKind); // returns true
     *
     * ElementKind localVariableKind = ElementKind.LOCAL_VARIABLE;
     * result = isVariable(localVariableKind); // returns true
     *
     * ElementKind exceptionParameterKind = ElementKind.EXCEPTION_PARAMETER;
     * result = isVariable(exceptionParameterKind); // returns true
     *
     * ElementKind resourceVariableKind = ElementKind.RESOURCE_VARIABLE;
     * result = isVariable(resourceVariableKind); // returns true
     *
     * ElementKind bindingVariableKind = ElementKind.BINDING_VARIABLE;
     * result = isVariable(bindingVariableKind); // returns true
     *
     * ElementKind methodKind = ElementKind.METHOD;
     * result = isVariable(methodKind); // returns false
     *
     * result = isVariable(null); // returns false
     * }</pre>
     *
     * @param kind the ElementKind to check, may be null
     * @return {@code true} if the kind is a variable-like element, {@code false} otherwise
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
     *
     * <p>
     * This method maps the provided {@link ElementType} to a corresponding {@link ElementKind}.
     * If the provided {@code elementType} is {@code null}, this method returns {@link ElementKind#OTHER}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ElementType typeElement = ElementType.TYPE;
     * ElementKind result = toElementKind(typeElement); // returns ElementKind.CLASS
     *
     * ElementType typeUseElement = ElementType.TYPE_USE;
     * result = toElementKind(typeUseElement); // returns ElementKind.CLASS
     *
     * ElementType fieldElement = ElementType.FIELD;
     * result = toElementKind(fieldElement); // returns ElementKind.FIELD
     *
     * result = toElementKind(null); // returns ElementKind.OTHER
     * }</pre>
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
     * <p>
     * This method compares the provided {@link ElementKind} with the result of converting the
     * {@link ElementType} to an equivalent {@link ElementKind} using {@link #toElementKind(ElementType)}.
     * If either argument is null, the comparison will return {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ElementKind classKind = ElementKind.CLASS;
     * ElementType typeElement = ElementType.TYPE;
     * boolean result = matchesElementType(classKind, typeElement); // returns true
     *
     * ElementKind fieldKind = ElementKind.FIELD;
     * ElementType typeUseElement = ElementType.TYPE_USE;
     * result = matchesElementType(fieldKind, typeUseElement); // returns false
     *
     * result = matchesElementType(null, ElementType.TYPE); // returns false
     * result = matchesElementType(ElementKind.CLASS, null); // returns false
     * }</pre>
     *
     * @param elementKind the ElementKind to check, may be {@code null}
     * @param elementType the ElementType to compare against, may be {@code null}
     * @return {@code true} if the ElementKind matches the converted ElementType, {@code false} otherwise
     * @see #toElementKind(ElementType)
     */
    static boolean matchesElementType(ElementKind elementKind, ElementType elementType) {
        return elementKind == toElementKind(elementType);
    }

    /**
     * Checks whether the specified {@link ElementKind} matches any of the specified {@link ElementType}s.
     *
     * <p>
     * This method converts each {@link ElementType} to its corresponding {@link ElementKind}
     * using {@link #toElementKind(ElementType)}, and then compares it with the provided
     * {@link ElementKind}. If any match is found, the method returns {@code true}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ElementKind classKind = ElementKind.CLASS;
     * boolean result = matchesElementType(classKind, ElementType.TYPE, ElementType.METHOD); // returns true
     *
     * result = matchesElementType(classKind, ElementType.FIELD, ElementType.CONSTRUCTOR); // returns false
     *
     * result = matchesElementType(null, ElementType.TYPE); // returns false
     *
     * result = matchesElementType(ElementKind.FIELD, (ElementType[]) null); // returns false
     * }</pre>
     *
     * @param elementKind  the ElementKind to check, may be {@code null}
     * @param elementTypes the array of ElementTypes to match against, may be {@code null}
     * @return {@code true} if the ElementKind matches any of the converted ElementTypes, {@code false} otherwise
     * @see #toElementKind(ElementType)
     * @see #matchesElementType(ElementKind, ElementType)
     */
    static boolean matchesElementType(ElementKind elementKind, ElementType... elementTypes) {
        int length = length(elementTypes);
        if (length < 1) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (matchesElementType(elementKind, elementTypes[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the specified {@link Element} matches any of the specified {@link ElementType}s.
     *
     * <p>
     * This method determines if the provided {@link Element} has a kind that matches any of the
     * converted {@link ElementKind} values derived from the given {@link ElementType}s.
     * It delegates to {@link #matchesElementType(ElementKind, ElementType...)} for the actual comparison.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Element element = ...; // an Element of kind CLASS
     * boolean result = matchesElementType(element, ElementType.TYPE, ElementType.TYPE_USE); // returns true
     *
     * result = matchesElementType(element, ElementType.FIELD, ElementType.METHOD); // returns false
     *
     * result = matchesElementType(null, ElementType.TYPE); // returns false
     *
     * result = matchesElementType(element, (ElementType[]) null); // returns false
     * }</pre>
     *
     * @param element      the Element to check, may be {@code null}
     * @param elementTypes the array of ElementTypes to match against, may be {@code null}
     * @return {@code true} if the element matches any of the converted ElementTypes, {@code false} otherwise
     * @see #matchesElementType(ElementKind, ElementType...)
     */
    static boolean matchesElementType(Element element, ElementType... elementTypes) {
        return element != null && matchesElementType(element.getKind(), elementTypes);
    }

    /**
     * Checks whether the specified {@link Element} matches the specified {@link ElementKind}.
     *
     * <p>
     * This method returns {@code false} if either the element or the kind is {@code null}.
     * Otherwise, it compares the kind of the element with the provided {@link ElementKind}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Element element = ...; // an Element of kind METHOD
     * ElementKind methodKind = ElementKind.METHOD;
     * boolean result = matchesElementKind(element, methodKind); // returns true
     *
     * result = matchesElementKind(null, methodKind); // returns false
     * result = matchesElementKind(element, null); // returns false
     * }</pre>
     *
     * @param member the Element to check, may be {@code null}
     * @param kind   the ElementKind to match, may be {@code null}
     * @return {@code true} if the element is not null and its kind matches the specified kind; otherwise, {@code false}
     */
    static boolean matchesElementKind(Element member, ElementKind kind) {
        return member == null || kind == null ? false : kind.equals(member.getKind());
    }

    /**
     * Checks whether the specified {@link Element} is public and non-static.
     *
     * <p>This method verifies if the provided element has the {@link Modifier#PUBLIC} modifier
     * and does not have the {@link Modifier#STATIC} modifier.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Element methodElement = ...; // an Element with PUBLIC and non-STATIC modifiers
     * boolean result = isPublicNonStatic(methodElement); // returns true
     *
     * Element staticMethodElement = ...; // an Element with PUBLIC and STATIC modifiers
     * result = isPublicNonStatic(staticMethodElement); // returns false
     *
     * result = isPublicNonStatic(null); // returns false
     * }</pre>
     *
     * @param member the Element to check, may be null
     * @return {@code true} if the element is public and non-static; otherwise, {@code false}
     */
    static boolean isPublicNonStatic(Element member) {
        return hasModifiers(member, PUBLIC) && !hasModifiers(member, STATIC);
    }

    /**
     * Checks whether the specified {@link Element} has all of the specified {@link Modifier}s.
     *
     * <p>
     * This method returns {@code false} if the element is {@code null} or if the modifiers array is {@code null}.
     * Otherwise, it verifies that the element contains all the provided modifiers.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Element methodElement = ...; // an Element with PUBLIC and STATIC modifiers
     * boolean result = hasModifiers(methodElement, Modifier.PUBLIC, Modifier.STATIC); // returns true
     *
     * result = hasModifiers(methodElement, Modifier.PRIVATE); // returns false
     *
     * result = hasModifiers(null, Modifier.PUBLIC); // returns false
     *
     * result = hasModifiers(methodElement, (Modifier[]) null); // returns false
     * }</pre>
     *
     * @param member    the {@link Element} to check, may be {@code null}
     * @param modifiers the array of {@link Modifier}s to match, may be {@code null}
     * @return {@code true} if the element is not null and contains all specified modifiers; otherwise, {@code false}
     */
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

    /**
     * Filters the provided list of {@link Element} objects based on the given array of {@link Predicate} conditions.
     *
     * <p>This method applies a logical AND combination of all provided predicates to filter the elements.
     * If the input list is empty or the predicates array is null or empty, an empty list is returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<Element> elements = ...; // a list of Elements
     *
     * // Filter public and static methods
     * List<Element> filtered = filterElements(elements,
     *     element -> ElementUtils.hasModifiers(element, Modifier.PUBLIC),
     *     element -> ElementUtils.hasModifiers(element, Modifier.STATIC)
     * );
     *
     * // Returns an empty list if no elements match
     * filtered = filterElements(elements,
     *     element -> ElementUtils.hasModifiers(element, Modifier.PRIVATE)
     * );
     *
     * // Returns an empty list if input is null or predicates are null
     * filtered = filterElements(null, (Predicate[]) null); // returns empty list
     * }</pre>
     *
     * @param elements          The list of elements to be filtered, may be {@code null}
     * @param elementPredicates An array of predicates used to filter the elements, may be {@code null}
     * @param <E>               The type of the elements, which must be a subclass of {@link Element}
     * @return A filtered list of elements that match all the provided predicates. Returns an empty list if no elements match,
     * or if the input list or predicate array is invalid.
     */
    static <E extends Element> List<E> filterElements(List<E> elements, Predicate<? super E>... elementPredicates) {
        if (isEmpty(elements) || elementPredicates == null) {
            return emptyList();
        }
        if (isNotEmpty(elementPredicates)) {
            Predicate predicate = and(elementPredicates);
            elements = (List) elements.stream().filter(predicate).collect(toList());
        }
        return elements.isEmpty() ? emptyList() : elements;
    }

    /**
     * Checks whether the parameter types of the given {@link ExecutableElement} match the specified {@link Type types}.
     *
     * <p>
     * This method compares the types of the parameters declared in the executable element with the provided expected types.
     * It uses {@link TypeUtils#isSameType(Element, CharSequence)} to perform type comparison.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ExecutableElement methodElement = ...; // an executable element with parameters
     * boolean result = matchParameterTypes(methodElement, String.class, int.class); // returns true if parameter types match
     *
     * result = matchParameterTypes(null, String.class); // returns false
     * result = matchParameterTypes(methodElement, (Type[]) null); // returns false
     * }</pre>
     *
     * @param executableElement the executable element whose parameters are to be checked, may be {@code null}
     * @param parameterTypes    the expected parameter types, may be {@code null}
     * @return {@code true} if the parameter types match; {@code false} otherwise
     */
    static boolean matchParameterTypes(ExecutableElement executableElement, Type... parameterTypes) {
        return executableElement == null || parameterTypes == null ? false :
                matchParameterTypeNames(executableElement.getParameters(), getTypeNames(parameterTypes));
    }

    /**
     * Checks whether the parameter types of the given list of {@link VariableElement} parameters match the specified {@link Type types}.
     *
     * <p>
     * This method compares each parameter's type with the corresponding expected type by their fully qualified type names.
     * It uses {@link TypeUtils#isSameType(Element, CharSequence)} to perform the type comparison.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<? extends VariableElement> parameters = executableElement.getParameters();
     * boolean result = matchParameterTypes(parameters, String.class, int.class); // returns true if types match
     *
     * result = matchParameterTypes(null, String.class); // returns false
     * result = matchParameterTypes(parameters, (Type[]) null); // returns false
     * }</pre>
     *
     * @param parameters     the list of variable elements representing the parameters, may be {@code null}
     * @param parameterTypes the expected parameter types, may be {@code null}
     * @return {@code true} if all parameter types match their corresponding expected types; otherwise, {@code false}
     */
    static boolean matchParameterTypes(List<? extends VariableElement> parameters, Type... parameterTypes) {
        return parameters == null || parameterTypes == null ? false : matchParameterTypeNames(parameters, getTypeNames(parameterTypes));
    }

    /**
     * Checks whether the parameter types of the given list of {@link VariableElement} parameters match the specified type names.
     *
     * <p>
     * This method compares each parameter's type with the corresponding expected type name using
     * {@link TypeUtils#isSameType(Element, CharSequence)}. It ensures that both the number of parameters and their respective
     * types match the provided array of type names.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<? extends VariableElement> parameters = executableElement.getParameters();
     *
     * // Check if the parameters match String and int
     * boolean result = matchParameterTypeNames(parameters, "java.lang.String", "int"); // returns true if match
     *
     * // Returns false if either parameter list or type names are null
     * result = matchParameterTypeNames(null, "java.lang.String"); // returns false
     * result = matchParameterTypeNames(parameters, (CharSequence[]) null); // returns false
     *
     * // Returns false if the size of parameters and type names do not match
     * result = matchParameterTypeNames(Arrays.asList(param1, param2), "java.lang.String"); // returns false
     * }</pre>
     *
     * @param parameters         the list of variable elements representing the parameters, may be {@code null}
     * @param parameterTypeNames the expected fully qualified type names of the parameters, may be {@code null}
     * @return {@code true} if all parameter types match their corresponding type names; otherwise, {@code false}
     */
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
