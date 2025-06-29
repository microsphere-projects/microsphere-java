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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static io.microsphere.annotation.processor.util.ElementUtils.filterElements;
import static io.microsphere.annotation.processor.util.ElementUtils.isPublicNonStatic;
import static io.microsphere.annotation.processor.util.ElementUtils.matchParameterTypeNames;
import static io.microsphere.annotation.processor.util.ElementUtils.matchParameterTypes;
import static io.microsphere.annotation.processor.util.MemberUtils.getDeclaredMembers;
import static io.microsphere.annotation.processor.util.TypeUtils.isSameType;
import static io.microsphere.annotation.processor.util.TypeUtils.ofDeclaredType;
import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.lang.function.Streams.filterFirst;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_TYPE_ARRAY;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.util.ElementFilter.methodsIn;

/**
 * The utilities class for method in the package "javax.lang.model."
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface MethodUtils extends Utils {


    /**
     * Get all declared methods of the specified type element.
     *
     * @param type the specified type element, may be null
     * @return a list of executable elements representing all declared methods of the specified type,
     *         or an empty list if the input type is null
     */
    static List<ExecutableElement> getDeclaredMethods(TypeElement type) {
        return findDeclaredMethods(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all declared methods of the specified type mirror.
     *
     * @param type the specified type mirror, may be null
     * @return a list of executable elements representing all declared methods of the specified type,
     *         or an empty list if the input type is null
     */
    static List<ExecutableElement> getDeclaredMethods(TypeMirror type) {
        return findDeclaredMethods(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all declared methods of the specified type element, including those inherited from superclasses and interfaces.
     *
     * @param type the specified type element, may be null
     * @return a list of executable elements representing all declared methods of the specified type,
     *         or an empty list if the input type is null
     */
    static List<ExecutableElement> getAllDeclaredMethods(TypeElement type) {
        return findAllDeclaredMethods(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all declared methods of the specified type mirror, including those inherited from superclasses and interfaces.
     *
     * @param type the specified type mirror, may be null
     * @return a list of executable elements representing all declared methods of the specified type,
     *         or an empty list if the input type is null
     */
    static List<ExecutableElement> getAllDeclaredMethods(TypeMirror type) {
        return findAllDeclaredMethods(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Find the declared methods of the specified type element.
     *
     * @param type            the specified type element, may be null
     * @param methodFilters   the filters for method elements
     * @return a list of executable elements representing all declared methods of the specified type,
     *         or an empty list if the input type is null
     */
    static List<ExecutableElement> findDeclaredMethods(TypeElement type, Predicate<? super ExecutableElement>... methodFilters) {
        return type == null ? emptyList() : findDeclaredMethods(type.asType(), methodFilters);
    }

    /**
     * Find the declared methods of the specified type mirror.
     *
     * @param type            the specified type mirror, may be null
     * @param methodFilters   the filters for method elements
     * @return a list of executable elements representing all declared methods of the specified type,
     *         or an empty list if the input type is null
     */
    static List<ExecutableElement> findDeclaredMethods(TypeMirror type, Predicate<? super ExecutableElement>... methodFilters) {
        return filterDeclaredMethods(type, false, methodFilters);
    }

    /**
     * Find all declared methods of the specified type element, including those inherited from superclasses and interfaces,
     * and exclude methods declared in the specified excluded types.
     *
     * @param type          the specified type element, may be null
     * @param excludedTypes the types whose methods should be excluded from the result, optional
     * @return a list of executable elements representing all declared methods of the specified type,
     *         excluding those declared in the excluded types, or an empty list if the input type is null
     */
    static List<ExecutableElement> findAllDeclaredMethods(TypeElement type, Type... excludedTypes) {
        return type == null ? emptyList() : findAllDeclaredMethods(type.asType(), excludedTypes);
    }

    /**
     * Find all declared methods of the specified type mirror, including those inherited from superclasses and interfaces,
     * and exclude methods declared in the specified excluded types.
     *
     * @param type          the specified type mirror, may be null
     * @param excludedTypes the types whose methods should be excluded from the result, optional
     * @return a list of executable elements representing all declared methods of the specified type,
     *         excluding those declared in the excluded types, or an empty list if the input type is null
     */
    static List<ExecutableElement> findAllDeclaredMethods(TypeMirror type, Type... excludedTypes) {
        if (type == null) {
            return emptyList();
        }
        return findAllDeclaredMethods(type, methodPredicateForExcludedTypes(excludedTypes));
    }

    /**
     * Find all public non-static methods declared in the specified type element, excluding those inherited from superclasses or interfaces.
     *
     * @param type          the specified type element, may be null
     * @param excludedTypes the types whose methods should be excluded from the result, optional
     * @return a list of executable elements representing all public non-static methods declared in the specified type,
     *         excluding those declared in the excluded types, or an empty list if the input type is null
     */
    static List<ExecutableElement> findPublicNonStaticMethods(TypeElement type, Type... excludedTypes) {
        return type == null ? emptyList() : findPublicNonStaticMethods(ofDeclaredType(type), excludedTypes);
    }

    /**
     * Find all public non-static methods declared in the specified type mirror, excluding those inherited from superclasses or interfaces,
     * and optionally exclude methods declared in the specified excluded types.
     *
     * @param type          the specified type mirror, may be null
     * @param excludedTypes the types whose methods should be excluded from the result, optional
     * @return a list of executable elements representing all public non-static methods declared in the specified type,
     *         excluding those declared in the excluded types, or an empty list if the input type is null
     */
    static List<ExecutableElement> findPublicNonStaticMethods(TypeMirror type, Type... excludedTypes) {
        if (type == null) {
            return emptyList();
        }

        Predicate predicate = and(methodPredicateForExcludedTypes(excludedTypes), MethodUtils::isPublicNonStaticMethod);

        return findAllDeclaredMethods(type, predicate);
    }

    /**
     * Find all declared methods of the specified type element, including those inherited from superclasses and interfaces,
     * and optionally filter them using the provided predicates.
     *
     * @param type           the specified type element, may be null
     * @param methodFilters  the filters for method elements, optional
     * @return a list of executable elements representing all declared methods of the specified type,
     *         including those inherited from superclasses and interfaces, or an empty list if the input type is null
     */
    static List<ExecutableElement> findAllDeclaredMethods(TypeElement type, Predicate<? super ExecutableElement>... methodFilters) {
        return type == null ? emptyList() : findAllDeclaredMethods(type.asType(), methodFilters);
    }

    /**
     * Find all declared methods of the specified type mirror, including those inherited from superclasses and interfaces,
     * and optionally filter them using the provided predicates.
     *
     * @param type           the specified type mirror, may be null
     * @param methodFilters  the filters for method elements, optional
     * @return a list of executable elements representing all declared methods of the specified type,
     *         including those inherited from superclasses and interfaces, or an empty list if the input type is null
     */
    static List<ExecutableElement> findAllDeclaredMethods(TypeMirror type, Predicate<? super ExecutableElement>... methodFilters) {
        return filterDeclaredMethods(type, true, methodFilters);
    }

    /**
     * Filters the declared methods of the specified type based on the given predicates.
     *
     * @param type                     the type whose declared methods are to be filtered
     * @param includeHierarchicalTypes whether to include methods from superclasses and interfaces
     * @param methodFilters            the predicates used to filter the methods
     * @return a list of executable elements representing the filtered methods,
     * or an empty list if the input type is null or no methods match the filters
     */
    static List<ExecutableElement> filterDeclaredMethods(TypeMirror type, boolean includeHierarchicalTypes, Predicate<? super ExecutableElement>... methodFilters) {
        if (type == null) {
            return emptyList();
        }

        List<? extends Element> declaredMembers = getDeclaredMembers(type, includeHierarchicalTypes);
        if (isEmpty(declaredMembers)) {
            return emptyList();
        }

        List<ExecutableElement> methods = methodsIn(declaredMembers);

        return filterElements(methods, methodFilters);
    }

    /**
     * Checks if the given executable element is a method.
     *
     * @param method the executable element to check
     * @return true if the element is a method, false otherwise
     */
    static boolean isMethod(ExecutableElement method) {
        return method != null && METHOD.equals(method.getKind());
    }

    /**
     * Checks whether the given method is a public non-static method.
     *
     * @param method the executable element to check
     * @return true if the method is a public non-static method, false otherwise
     */
    static boolean isPublicNonStaticMethod(ExecutableElement method) {
        return isMethod(method) && isPublicNonStatic(method);
    }

    /**
     * Finds a method with the specified name in the given type, using an empty parameter type array as default.
     *
     * @param type       the type element to search for the method
     * @param methodName the name of the method to find
     * @return the first matching executable element representing the method, or null if none is found
     */
    static ExecutableElement findMethod(TypeElement type, String methodName) {
        return findMethod(type, methodName, EMPTY_TYPE_ARRAY);
    }

    /**
     * Finds a method with the specified name in the given type, using an empty parameter type array as default.
     *
     * @param type       the type mirror to search for the method
     * @param methodName the name of the method to find
     * @return the first matching executable element representing the method, or null if none is found
     */
    static ExecutableElement findMethod(TypeMirror type, String methodName) {
        return findMethod(type, methodName, EMPTY_TYPE_ARRAY);
    }

    /**
     * Finds a method with the specified name and parameter types in the given type element.
     *
     * @param type           the type element to search for the method
     * @param methodName     the name of the method to find
     * @param parameterTypes the parameter types of the method to match
     * @return the first matching executable element representing the method, or null if none is found
     */
    static ExecutableElement findMethod(TypeElement type, String methodName, Type... parameterTypes) {
        return type == null ? null : findMethod(type.asType(), methodName, parameterTypes);
    }

    /**
     * Finds a method with the specified name and parameter types in the given type mirror.
     *
     * @param type           the type mirror to search for the method
     * @param methodName     the name of the method to find
     * @param parameterTypes the parameter types of the method to match
     * @return the first matching executable element representing the method, or null if none is found
     */
    static ExecutableElement findMethod(TypeMirror type, String methodName, Type... parameterTypes) {
        if (type == null || methodName == null || parameterTypes == null) {
            return null;
        }
        List<ExecutableElement> allDeclaredMethods = findAllDeclaredMethods(type, method -> matches(method, methodName, parameterTypes));
        return allDeclaredMethods.isEmpty() ? null : allDeclaredMethods.get(0);
    }

    /**
     * Finds a method with the specified name and parameter type names in the given type element.
     *
     * @param type               the type element to search for the method
     * @param methodName         the name of the method to find
     * @param parameterTypeNames the names of the parameter types of the method to match
     * @return the first matching executable element representing the method, or null if none is found
     */
    static ExecutableElement findMethod(TypeElement type, String methodName, CharSequence... parameterTypeNames) {
        return type == null ? null : findMethod(type.asType(), methodName, parameterTypeNames);
    }

    /**
     * Finds a method with the specified name and parameter type names in the given type mirror.
     *
     * @param type               the type mirror to search for the method
     * @param methodName         the name of the method to find
     * @param parameterTypeNames the names of the parameter types of the method to match
     * @return the first matching executable element representing the method, or null if none is found
     */
    static ExecutableElement findMethod(TypeMirror type, String methodName, CharSequence... parameterTypeNames) {
        if (type == null || methodName == null || parameterTypeNames == null) {
            return null;
        }
        List<ExecutableElement> allDeclaredMethods = findAllDeclaredMethods(type, method -> matches(method, methodName, parameterTypeNames));
        return allDeclaredMethods.isEmpty() ? null : allDeclaredMethods.get(0);
    }

    /**
     * Finds an overridden method in the given type that corresponds to the provided declaring method.
     *
     * @param processingEnv   the processing environment used to get element utilities
     * @param type            the type element in which to search for the overridden method
     * @param declaringMethod the method element whose override is to be found
     * @return the overridden method in the specified type, or null if no such method exists
     */
    static ExecutableElement getOverrideMethod(ProcessingEnvironment processingEnv, TypeElement type, ExecutableElement declaringMethod) {
        Elements elements = processingEnv.getElementUtils();
        return filterFirst(getAllDeclaredMethods(type), method -> elements.overrides(method, declaringMethod, type));
    }

    /**
     * Filters the given list of executable elements (methods) based on the provided predicates.
     *
     * @param methods       the list of executable elements (methods) to be filtered
     * @param methodFilters the array of predicates used for filtering the methods
     * @return a filtered list of executable elements matching all the provided predicates,
     * or an empty list if the input list is null or empty, or no methods match the filters
     */
    static List<ExecutableElement> filterMethods(List<ExecutableElement> methods, Predicate<? super ExecutableElement>... methodFilters) {
        if (isEmpty(methods)) {
            return emptyList();
        }

        List<ExecutableElement> filteredMethods = methods;
        if (isNotEmpty(methodFilters)) {
            Predicate predicate = and(methodFilters);
            filteredMethods = (List) methods.stream().filter(predicate).collect(toList());
        }

        return filteredMethods.isEmpty() ? emptyList() : filteredMethods;
    }

    /**
     * Gets the simple name of the method as a string.
     *
     * @param method the executable element representing the method, may be null
     * @return the simple name of the method, or null if the method is null
     */
    static String getMethodName(ExecutableElement method) {
        return method == null ? null : method.getSimpleName().toString();
    }

    /**
     * Gets the return type name of the given method.
     *
     * @param method the executable element representing the method
     * @return the return type name of the method, or null if the method is null
     */
    static String getReturnTypeName(ExecutableElement method) {
        return method == null ? null : TypeUtils.toString(method.getReturnType());
    }

    /**
     * Gets the parameter type mirrors of the given method.
     *
     * @param method the executable element representing the method
     * @return a list of type mirrors representing the parameter types of the method,
     * or an empty list if the method is null or has no parameters
     */
    static List<TypeMirror> getMethodParameterTypeMirrors(ExecutableElement method) {
        if (method == null) {
            return emptyList();
        }

        List<? extends VariableElement> parameters = method.getParameters();
        if (parameters.isEmpty()) {
            return emptyList();
        }

        List<TypeMirror> parameterTypes = parameters.stream()
                .map(VariableElement::asType)
                .collect(toList());

        return parameterTypes;
    }

    /**
     * Gets the parameter type names of the given method.
     *
     * @param method the executable element
     * @return the parameter type names of the given method
     */
    static String[] getMethodParameterTypeNames(ExecutableElement method) {
        List<TypeMirror> parameterTypes = getMethodParameterTypeMirrors(method);
        return parameterTypes.isEmpty() ? EMPTY_STRING_ARRAY : parameterTypes.stream().map(TypeUtils::toString).toArray(String[]::new);
    }

    /**
     * Checks if the given method matches the specified method name and parameter types.
     *
     * @param method         the executable element representing the method to check
     * @param methodName     the name of the method to match
     * @param parameterTypes the parameter types of the method to match
     * @return true if the method matches the given name and parameter types, false otherwise
     */
    static boolean matches(ExecutableElement method, String methodName, Type... parameterTypes) {
        return matchesMethod(method, methodName, parameterTypes);
    }

    /**
     * Checks if the given method matches the specified method name and parameter type names.
     *
     * @param method             the executable element representing the method to check
     * @param methodName         the name of the method to match
     * @param parameterTypeNames the names of the parameter types to match
     * @return true if the method matches the given name and parameter type names, false otherwise
     */
    static boolean matches(ExecutableElement method, String methodName, CharSequence... parameterTypeNames) {
        return matchesMethod(method, methodName, parameterTypeNames);
    }

    /**
     * Checks if the given method has the specified method name.
     *
     * @param method     the executable element representing the method to check
     * @param methodName the name of the method to match
     * @return true if the method's name matches the given method name, false otherwise
     */
    static boolean matchesMethodName(ExecutableElement method, String methodName) {
        return Objects.equals(getMethodName(method), methodName);
    }

    /**
     * Checks if the given method matches the specified method name and parameter types.
     *
     * @param method         the executable element representing the method to check
     * @param methodName     the name of the method to match
     * @param parameterTypes the parameter types of the method to match
     * @return true if the method matches the given name and parameter types, false otherwise
     */
    static boolean matchesMethod(ExecutableElement method, String methodName, Type... parameterTypes) {
        if (method == null || methodName == null || parameterTypes == null) {
            return false;
        }

        // matches the name of method
        if (!matchesMethodName(method, methodName)) {
            return false;
        }

        if (!matchParameterTypes(method, parameterTypes)) {
            return false;
        }

        return true;
    }

    /**
     * Checks if the given method matches the specified method name and parameter type names.
     *
     * @param method             the executable element representing the method to check
     * @param methodName         the name of the method to match
     * @param parameterTypeNames the names of the parameter types to match
     * @return true if the method matches the given name and parameter type names, false otherwise
     */
    static boolean matchesMethod(ExecutableElement method, String methodName, CharSequence... parameterTypeNames) {
        if (method == null || methodName == null || parameterTypeNames == null) {
            return false;
        }

        // matches the name of method
        if (!Objects.equals(getMethodName(method), methodName)) {
            return false;
        }

        if (!matchParameterTypeNames(method.getParameters(), parameterTypeNames)) {
            return false;
        }

        return true;
    }

    /**
     * Returns the class or interface defining the executable.
     *
     * @param method {@link ExecutableElement}
     * @return <code>null</code> if <code>method</code> is <code>null</code>
     */
    static Element getEnclosingElement(ExecutableElement method) {
        return method == null ? null : method.getEnclosingElement();
    }

    /**
     * Creates a predicate that filters out methods declared in the specified excluded types.
     *
     * @param excludedTypes the types whose methods should be excluded from the result
     * @return a predicate that returns {@code true} for methods not declared in any of the excluded types
     */
    static Predicate<? super ExecutableElement> methodPredicateForExcludedTypes(Type... excludedTypes) {
        return method -> {
            boolean excluded = true;
            Element declaredType = getEnclosingElement(method);
            for (Type excludedType : excludedTypes) {
                if (isSameType(declaredType, excludedType)) {
                    excluded = false;
                    break;
                }
            }
            return excluded;
        };
    }
}
