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
     * Gets all declared methods of the specified {@link TypeElement}.
     *
     * <p>This method returns a list of methods directly declared in the given type element,
     * excluding inherited methods. If the provided type element is {@code null}, an empty list is returned.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeElement typeElement = ...; // Obtain a valid TypeElement
     * List<ExecutableElement> methods = getDeclaredMethods(typeElement);
     * for (ExecutableElement method : methods) {
     *     System.out.println("Declared Method: " + method.getSimpleName());
     * }
     * }</pre>
     *
     * @param type the specified type element, may be null
     * @return a list of executable elements representing all declared methods of the specified type,
     * or an empty list if the input type is null
     */
    static List<ExecutableElement> getDeclaredMethods(TypeElement type) {
        return findDeclaredMethods(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Gets all declared methods of the specified {@link TypeMirror}.
     *
     * <p>This method returns a list of methods directly declared in the given type mirror,
     * excluding inherited methods. If the provided type mirror is {@code null}, an empty list is returned.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror typeMirror = ...; // Obtain a valid TypeMirror
     * List<ExecutableElement> methods = getDeclaredMethods(typeMirror);
     * for (ExecutableElement method : methods) {
     *     System.out.println("Declared Method: " + method.getSimpleName());
     * }
     * }</pre>
     *
     * @param type the specified type mirror, may be null
     * @return a list of executable elements representing all declared methods of the specified type,
     * or an empty list if the input type is null
     */
    static List<ExecutableElement> getDeclaredMethods(TypeMirror type) {
        return findDeclaredMethods(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all declared methods of the specified type element, including those inherited from superclasses and interfaces.
     *
     * <p>This method returns a list of methods directly declared in the given type element, including those
     * inherited from superclasses and interfaces. If the provided type element is {@code null}, an empty list is returned.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeElement typeElement = ...; // Obtain a valid TypeElement
     * List<ExecutableElement> methods = getAllDeclaredMethods(typeElement);
     * for (ExecutableElement method : methods) {
     *     System.out.println("Declared Method (including inherited): " + method.getSimpleName());
     * }
     * }</pre>
     *
     * @param type the specified type element, may be null
     * @return a list of executable elements representing all declared methods of the specified type,
     * including those inherited from superclasses and interfaces, or an empty list if the input type is null
     */
    static List<ExecutableElement> getAllDeclaredMethods(TypeElement type) {
        return findAllDeclaredMethods(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all declared methods of the specified type mirror, including those inherited from superclasses and interfaces.
     *
     * <p>This method returns a list of methods directly declared in the given type mirror, including those
     * inherited from superclasses and interfaces. If the provided type mirror is {@code null}, an empty list is returned.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror typeMirror = ...; // Obtain a valid TypeMirror
     * List<ExecutableElement> methods = getAllDeclaredMethods(typeMirror);
     * for (ExecutableElement method : methods) {
     *     System.out.println("Declared Method (including inherited): " + method.getSimpleName());
     * }
     * }</pre>
     *
     * @param type the specified type mirror, may be null
     * @return a list of executable elements representing all declared methods of the specified type,
     * including those inherited from superclasses and interfaces, or an empty list if the input type is null
     */
    static List<ExecutableElement> getAllDeclaredMethods(TypeMirror type) {
        return findAllDeclaredMethods(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Find the declared methods of the specified {@link TypeElement}.
     *
     * <p>This method returns a list of methods directly declared in the given type element,
     * excluding inherited methods. If the provided type element is {@code null}, an empty list is returned.
     * Additional filters can be applied to narrow down the list of methods based on custom criteria.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeElement typeElement = ...; // Obtain a valid TypeElement
     * List<ExecutableElement> methods = findDeclaredMethods(typeElement);
     * for (ExecutableElement method : methods) {
     *     System.out.println("Declared Method: " + method.getSimpleName());
     * }
     * }</pre>
     *
     * <p>Filtering usage example:
     * <pre>{@code
     * List<ExecutableElement> publicNonStaticMethods = findDeclaredMethods(typeElement, MethodUtils::isPublicNonStaticMethod);
     * for (ExecutableElement method : publicNonStaticMethods) {
     *     System.out.println("Public Non-Static Method: " + method.getSimpleName());
     * }
     * }</pre>
     *
     * @param type          the specified type element, may be null
     * @param methodFilters the filters for method elements
     * @return a list of executable elements representing all declared methods of the specified type,
     * or an empty list if the input type is null
     */
    static List<ExecutableElement> findDeclaredMethods(TypeElement type, Predicate<? super ExecutableElement>... methodFilters) {
        return type == null ? emptyList() : findDeclaredMethods(type.asType(), methodFilters);
    }

    /**
     * Find the declared methods of the specified type mirror.
     *
     * <p>This method returns a list of methods directly declared in the given type mirror,
     * excluding inherited methods. If the provided type mirror is {@code null}, an empty list is returned.
     * Additional filters can be applied to narrow down the list of methods based on custom criteria.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror typeMirror = ...; // Obtain a valid TypeMirror
     * List<ExecutableElement> methods = findDeclaredMethods(typeMirror);
     * for (ExecutableElement method : methods) {
     *     System.out.println("Declared Method: " + method.getSimpleName());
     * }
     * }</pre>
     *
     * <p>Filtering usage example:
     * <pre>{@code
     * List<ExecutableElement> publicNonStaticMethods = findDeclaredMethods(typeMirror, MethodUtils::isPublicNonStaticMethod);
     * for (ExecutableElement method : publicNonStaticMethods) {
     *     System.out.println("Public Non-Static Method: " + method.getSimpleName());
     * }
     * }</pre>
     *
     * @param type          the specified type mirror, may be null
     * @param methodFilters the filters for method elements
     * @return a list of executable elements representing all declared methods of the specified type,
     * or an empty list if the input type is null
     */
    static List<ExecutableElement> findDeclaredMethods(TypeMirror type, Predicate<? super ExecutableElement>... methodFilters) {
        return filterDeclaredMethods(type, false, methodFilters);
    }

    /**
     * Find all declared methods of the specified type element, including those inherited from superclasses and interfaces,
     * and exclude methods declared in the specified excluded types.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeElement typeElement = ...; // Obtain a valid TypeElement
     * List<ExecutableElement> methods = findAllDeclaredMethods(typeElement);
     * for (ExecutableElement method : methods) {
     *     System.out.println("Declared Method (including inherited): " + method.getSimpleName());
     * }
     * }</pre>
     *
     * <p>Excluding methods from specific types:
     * <pre>{@code
     * List<ExecutableElement> methodsExcludingObject = findAllDeclaredMethods(typeElement, Object.class);
     * for (ExecutableElement method : methodsExcludingObject) {
     *     System.out.println("Method excluding Object: " + method.getSimpleName());
     * }
     * }</pre>
     *
     * @param type          the specified type element, may be null
     * @param excludedTypes the types whose methods should be excluded from the result, optional
     * @return a list of executable elements representing all declared methods of the specified type,
     * including those inherited from superclasses and interfaces, but excluding those declared
     * in the excluded types, or an empty list if the input type is null
     */
    static List<ExecutableElement> findAllDeclaredMethods(TypeElement type, Type... excludedTypes) {
        return type == null ? emptyList() : findAllDeclaredMethods(type.asType(), excludedTypes);
    }

    /**
     * Find all declared methods of the specified type mirror, including those inherited from superclasses and interfaces,
     * and exclude methods declared in the specified excluded types.
     *
     * <p>This method returns a list of methods directly declared in the given type mirror,
     * including those inherited from superclasses and interfaces. If the provided type mirror is {@code null},
     * an empty list is returned. Additional filtering can be applied to exclude methods declared in specific types.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror typeMirror = ...; // Obtain a valid TypeMirror
     * List<ExecutableElement> methods = findAllDeclaredMethods(typeMirror);
     * for (ExecutableElement method : methods) {
     *     System.out.println("Declared Method (including inherited): " + method.getSimpleName());
     * }
     * }</pre>
     *
     * <p>Excluding methods from specific types:
     * <pre>{@code
     * List<ExecutableElement> methodsExcludingObject = findAllDeclaredMethods(typeMirror, Object.class);
     * for (ExecutableElement method : methodsExcludingObject) {
     *     System.out.println("Method excluding Object: " + method.getSimpleName());
     * }
     * }</pre>
     *
     * @param type          the specified type mirror, may be null
     * @param excludedTypes the types whose methods should be excluded from the result, optional
     * @return a list of executable elements representing all declared methods of the specified type,
     * excluding those declared in the excluded types, or an empty list if the input type is null
     */
    static List<ExecutableElement> findAllDeclaredMethods(TypeMirror type, Type... excludedTypes) {
        if (type == null) {
            return emptyList();
        }
        return findAllDeclaredMethods(type, methodPredicateForExcludedTypes(excludedTypes));
    }

    /**
     * Finds all public non-static methods declared in the specified {@link TypeElement}, excluding those inherited from superclasses or interfaces,
     * and optionally excludes methods declared in the specified excluded types.
     *
     * <p>This method returns a list of executable elements representing public non-static methods directly declared in the given type element.
     * If the provided type element is {@code null}, an empty list is returned.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeElement typeElement = ...; // Obtain a valid TypeElement
     * List<ExecutableElement> methods = findPublicNonStaticMethods(typeElement);
     * for (ExecutableElement method : methods) {
     *     System.out.println("Public Non-Static Method: " + method.getSimpleName());
     * }
     * }</pre>
     *
     * <p>Excluding methods from specific types:
     * <pre>{@code
     * List<ExecutableElement> methodsExcludingObject = findPublicNonStaticMethods(typeElement, Object.class);
     * for (ExecutableElement method : methodsExcludingObject) {
     *     System.out.println("Public Non-Static Method (excluding Object): " + method.getSimpleName());
     * }
     * }</pre>
     *
     * @param type          the specified type element, may be null
     * @param excludedTypes the types whose methods should be excluded from the result, optional
     * @return a list of executable elements representing all public non-static methods declared in the specified type,
     * or an empty list if the input type is null
     */
    static List<ExecutableElement> findPublicNonStaticMethods(TypeElement type, Type... excludedTypes) {
        return type == null ? emptyList() : findPublicNonStaticMethods(ofDeclaredType(type), excludedTypes);
    }

    /**
     * Find all public non-static methods declared in the specified type mirror, excluding those inherited from superclasses or interfaces,
     * and optionally exclude methods declared in the specified excluded types.
     *
     * <p>This method returns a list of executable elements representing public non-static methods directly declared in the given type.
     * If the provided type is {@code null}, an empty list is returned.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror typeMirror = ...; // Obtain a valid TypeMirror
     * List<ExecutableElement> methods = findPublicNonStaticMethods(typeMirror);
     * for (ExecutableElement method : methods) {
     *     System.out.println("Public Non-Static Method: " + method.getSimpleName());
     * }
     * }</pre>
     *
     * <p>Excluding methods from specific types:
     * <pre>{@code
     * List<ExecutableElement> methodsExcludingObject = findPublicNonStaticMethods(typeMirror, Object.class);
     * for (ExecutableElement method : methodsExcludingObject) {
     *     System.out.println("Public Non-Static Method (excluding Object): " + method.getSimpleName());
     * }
     * }</pre>
     *
     * @param type          the specified type mirror, may be null
     * @param excludedTypes the types whose methods should be excluded from the result, optional
     * @return a list of executable elements representing all public non-static methods declared in the specified type,
     * excluding those declared in the excluded types, or an empty list if the input type is null
     */
    static List<ExecutableElement> findPublicNonStaticMethods(TypeMirror type, Type... excludedTypes) {
        if (type == null) {
            return emptyList();
        }

        Predicate predicate = and(methodPredicateForExcludedTypes(excludedTypes), MethodUtils::isPublicNonStaticMethod);

        return findAllDeclaredMethods(type, predicate);
    }

    /**
     * Find all declared methods of the specified {@link TypeElement}, including those inherited from superclasses and interfaces,
     * and optionally filter them using the provided predicates.
     *
     * <p>This method returns a list of methods directly declared in the given type element,
     * including those inherited from superclasses and interfaces. If the provided type element is {@code null},
     * an empty list is returned. Additional filters can be applied to narrow down the list of methods based on custom criteria.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeElement typeElement = ...; // Obtain a valid TypeElement
     * List<ExecutableElement> methods = findAllDeclaredMethods(typeElement);
     * for (ExecutableElement method : methods) {
     *     System.out.println("Declared Method (including inherited): " + method.getSimpleName());
     * }
     * }</pre>
     *
     * <p>Filtering usage example:
     * <pre>{@code
     * List<ExecutableElement> publicNonStaticMethods = findAllDeclaredMethods(typeElement, MethodUtils::isPublicNonStaticMethod);
     * for (ExecutableElement method : publicNonStaticMethods) {
     *     System.out.println("Public Non-Static Method: " + method.getSimpleName());
     * }
     * }</pre>
     *
     * @param type          the specified type element, may be null
     * @param methodFilters the filters for method elements, optional
     * @return a list of executable elements representing all declared methods of the specified type,
     * including those inherited from superclasses and interfaces, or an empty list if the input type is null
     */
    static List<ExecutableElement> findAllDeclaredMethods(TypeElement type, Predicate<? super ExecutableElement>... methodFilters) {
        return type == null ? emptyList() : findAllDeclaredMethods(type.asType(), methodFilters);
    }

    /**
     * Finds all declared methods of the specified type mirror, including those inherited from superclasses and interfaces,
     * and optionally filters them using the provided predicates.
     *
     * <p>This method returns a list of methods directly declared in the given type mirror, including those
     * inherited from superclasses and interfaces. If the provided type mirror is {@code null}, an empty list is returned.
     * Additional filters can be applied to narrow down the list of methods based on custom criteria.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror typeMirror = ...; // Obtain a valid TypeMirror
     * List<ExecutableElement> methods = findAllDeclaredMethods(typeMirror);
     * for (ExecutableElement method : methods) {
     *     System.out.println("Declared Method (including inherited): " + method.getSimpleName());
     * }
     * }</pre>
     *
     * <p>Filtering usage example:
     * <pre>{@code
     * List<ExecutableElement> publicNonStaticMethods = findAllDeclaredMethods(typeMirror, MethodUtils::isPublicNonStaticMethod);
     * for (ExecutableElement method : publicNonStaticMethods) {
     *     System.out.println("Public Non-Static Method: " + method.getSimpleName());
     * }
     * }</pre>
     *
     * @param type          the specified type mirror, may be null
     * @param methodFilters the filters for method elements, optional
     * @return a list of executable elements representing all declared methods of the specified type,
     * including those inherited from superclasses and interfaces, or an empty list if the input type is null
     */
    static List<ExecutableElement> findAllDeclaredMethods(TypeMirror type, Predicate<? super ExecutableElement>... methodFilters) {
        return filterDeclaredMethods(type, true, methodFilters);
    }

    /**
     * Filters the declared methods of the specified type based on the given predicates.
     *
     * <p>This method returns a list of methods directly declared in the given type,
     * optionally including those inherited from superclasses and interfaces. If the provided type is {@code null},
     * an empty list is returned. Additional filters can be applied to narrow down the list of methods based on custom criteria.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror typeMirror = ...; // Obtain a valid TypeMirror
     * List<ExecutableElement> methods = filterDeclaredMethods(typeMirror, false);
     * for (ExecutableElement method : methods) {
     *     System.out.println("Declared Method: " + method.getSimpleName());
     * }
     * }</pre>
     *
     * <p>Filtering usage example:
     * <pre>{@code
     * List<ExecutableElement> publicNonStaticMethods = filterDeclaredMethods(typeMirror, false, MethodUtils::isPublicNonStaticMethod);
     * for (ExecutableElement method : publicNonStaticMethods) {
     *     System.out.println("Public Non-Static Method: " + method.getSimpleName());
     * }
     * }</pre>
     *
     * @param type                     the type whose declared methods are to be filtered, may be null
     * @param includeHierarchicalTypes whether to include methods from superclasses and interfaces
     * @param methodFilters            the predicates used to filter the methods, optional
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
     * <p>This method determines whether the provided executable element represents a method.
     * If the element is {@code null}, the method returns {@code false}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ExecutableElement executableElement = ...; // Obtain a valid ExecutableElement
     * boolean isMethod = MethodUtils.isMethod(executableElement);
     * if (isMethod) {
     *     System.out.println("The element is a method.");
     * } else {
     *     System.out.println("The element is not a method.");
     * }
     * }</pre>
     *
     * @param method the executable element to check, may be null
     * @return true if the element is a method, false otherwise
     */
    static boolean isMethod(ExecutableElement method) {
        return method != null && METHOD.equals(method.getKind());
    }

    /**
     * Checks whether the given method is a public non-static method.
     *
     * <p>This method verifies if the provided executable element represents a method that is both public and non-static.
     * If the method is {@code null}, the method returns {@code false}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ExecutableElement method = ...; // Obtain a valid ExecutableElement
     * boolean isPublicNonStatic = MethodUtils.isPublicNonStaticMethod(method);
     * if (isPublicNonStatic) {
     *     System.out.println("The method is a public non-static method.");
     * } else {
     *     System.out.println("The method is not a public non-static method.");
     * }
     * }</pre>
     *
     * @param method the executable element to check, may be null
     * @return true if the method is a public non-static method, false otherwise
     */
    static boolean isPublicNonStaticMethod(ExecutableElement method) {
        return isMethod(method) && isPublicNonStatic(method);
    }

    /**
     * Finds a method with the specified name in the given type element, using an empty parameter type array as default.
     *
     * <p>This method searches for a method with the specified name in the given type element.
     * If no method with the specified name is found, it returns {@code null}.
     * The search considers methods declared directly in the type, excluding inherited methods.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeElement typeElement = ...; // Obtain a valid TypeElement
     * String methodName = "myMethod";
     * ExecutableElement method = MethodUtils.findMethod(typeElement, methodName);
     *
     * if (method != null) {
     *     System.out.println("Found method: " + method.getSimpleName());
     * } else {
     *     System.out.println("Method not found.");
     * }
     * }</pre>
     *
     * @param type       the type element to search for the method
     * @param methodName the name of the method to find
     * @return the first matching executable element representing the method, or null if none is found
     */
    static ExecutableElement findMethod(TypeElement type, String methodName) {
        return findMethod(type, methodName, EMPTY_TYPE_ARRAY);
    }

    /**
     * Finds a method with the specified name in the given type mirror, using an empty parameter type array as default.
     *
     * <p>This method searches for a method with the specified name in the given type mirror.
     * If no method with the specified name is found, it returns {@code null}.
     * The search considers methods declared directly in the type, excluding inherited methods.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror typeMirror = ...; // Obtain a valid TypeMirror
     * String methodName = "myMethod";
     * ExecutableElement method = MethodUtils.findMethod(typeMirror, methodName);
     *
     * if (method != null) {
     *     System.out.println("Found method: " + method.getSimpleName());
     * } else {
     *     System.out.println("Method not found.");
     * }
     * }</pre>
     *
     * @param type       the type mirror to search for the method
     * @param methodName the name of the method to find
     * @return the first matching executable element representing the method, or null if none is found
     */
    static ExecutableElement findMethod(TypeMirror type, String methodName) {
        return findMethod(type, methodName, EMPTY_TYPE_ARRAY);
    }

    /**
     * Finds a method with the specified name and parameter types in the given {@link TypeElement}.
     *
     * <p>This method searches for a method with the specified name and exact parameter types
     * directly declared in the provided type element, excluding inherited methods.
     * If no matching method is found, it returns {@code null}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeElement typeElement = ...; // Obtain a valid TypeElement
     * String methodName = "myMethod";
     * Type[] parameterTypes = new Type[] { String.class, int.class };
     * ExecutableElement method = MethodUtils.findMethod(typeElement, methodName, parameterTypes);
     *
     * if (method != null) {
     *     System.out.println("Found method: " + method.getSimpleName());
     * } else {
     *     System.out.println("Method not found.");
     * }
     * }</pre>
     *
     * @param type           the specified type element, may be null
     * @param methodName     the name of the method to find, must not be null
     * @param parameterTypes the parameter types of the method to match, must not be null
     * @return the first matching executable element representing the method, or null if none is found
     */
    static ExecutableElement findMethod(TypeElement type, String methodName, Type... parameterTypes) {
        return type == null ? null : findMethod(type.asType(), methodName, parameterTypes);
    }

    /**
     * Finds a method with the specified name and parameter types in the given type mirror.
     *
     * <p>This method searches for a method with the specified name and exact parameter types
     * directly declared in the provided type mirror, excluding inherited methods.
     * If no matching method is found, it returns {@code null}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror typeMirror = ...; // Obtain a valid TypeMirror
     * String methodName = "myMethod";
     * Type[] parameterTypes = new Type[] { String.class, int.class };
     * ExecutableElement method = MethodUtils.findMethod(typeMirror, methodName, parameterTypes);
     *
     * if (method != null) {
     *     System.out.println("Found method: " + method.getSimpleName());
     * }
     * }</pre>
     *
     * @param type           the type mirror to search for the method, may be null
     * @param methodName     the name of the method to find, must not be null
     * @param parameterTypes the parameter types of the method to match, must not be null
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
     * Finds a method with the specified name and parameter type names in the given {@link TypeElement}.
     *
     * <p>This method searches for a method with the specified name and exact parameter type names
     * directly declared in the provided type element, excluding inherited methods.
     * If no matching method is found, it returns {@code null}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeElement typeElement = ...; // Obtain a valid TypeElement
     * String methodName = "myMethod";
     * CharSequence[] paramTypeNames = new CharSequence[] { "java.lang.String", "int" };
     * ExecutableElement method = MethodUtils.findMethod(typeElement, methodName, paramTypeNames);
     *
     * if (method != null) {
     *     System.out.println("Found method: " + method.getSimpleName());
     * } else {
     *     System.out.println("Method not found.");
     * }
     * }</pre>
     *
     * @param type               the type element to search for the method, may be null
     * @param methodName         the name of the method to find, must not be null
     * @param parameterTypeNames the names of the parameter types of the method to match, must not be null
     * @return the first matching executable element representing the method, or null if none is found
     */
    static ExecutableElement findMethod(TypeElement type, String methodName, CharSequence... parameterTypeNames) {
        return type == null ? null : findMethod(type.asType(), methodName, parameterTypeNames);
    }

    /**
     * Finds a method with the specified name and parameter type names in the given type mirror.
     *
     * <p>This method searches for a method with the specified name and exact parameter type names
     * directly declared in the provided type mirror, excluding inherited methods.
     * If no matching method is found, it returns {@code null}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeMirror typeMirror = ...; // Obtain a valid TypeMirror
     * String methodName = "myMethod";
     * CharSequence[] paramTypeNames = new CharSequence[] { "java.lang.String", "int" };
     * ExecutableElement method = MethodUtils.findMethod(typeMirror, methodName, paramTypeNames);
     *
     * if (method != null) {
     *     System.out.println("Found method: " + method.getSimpleName());
     * }
     * }</pre>
     *
     * @param type               the type mirror to search for the method, may be null
     * @param methodName         the name of the method to find, must not be null
     * @param parameterTypeNames the names of the parameter types of the method to match, must not be null
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
     * Finds the overridden method in the specified type that corresponds to the given declaring method.
     *
     * <p>This method searches for a method in the provided type element that overrides the specified
     * declaring method. It utilizes the processing environment to determine method overriding using
     * the {@link Elements#overrides(ExecutableElement, ExecutableElement, TypeElement)} method.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ProcessingEnvironment processingEnv = ...; // Obtain a valid ProcessingEnvironment
     * TypeElement typeElement = ...; // The type in which to find the overridden method
     * ExecutableElement declaringMethod = ...; // The method being overridden
     *
     * ExecutableElement overriddenMethod = getOverrideMethod(processingEnv, typeElement, declaringMethod);
     *
     * if (overriddenMethod != null) {
     *     System.out.println("Overridden Method Found: " + overriddenMethod.getSimpleName());
     * } else {
     *     System.out.println("No overridden method found.");
     * }
     * }</pre>
     *
     * @param processingEnv   the processing environment used to determine overriding
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
     * <p>This method applies a set of filtering predicates to a list of methods and returns a new list
     * containing only the methods that satisfy all the provided conditions. If no method matches,
     * an empty list is returned. If no filters are provided, the original list is returned unchanged.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<ExecutableElement> methods = MethodUtils.getDeclaredMethods(typeElement);
     * List<ExecutableElement> publicNonStaticMethods = MethodUtils.filterMethods(methods, MethodUtils::isPublicNonStaticMethod);
     *
     * for (ExecutableElement method : publicNonStaticMethods) {
     *     System.out.println("Public Non-Static Method: " + method.getSimpleName());
     * }
     * }</pre>
     *
     * <p>Multiple filters can be combined:
     * <pre>{@code
     * Predicate<ExecutableElement> isPublic = method -> method.getModifiers().contains(Modifier.PUBLIC);
     * Predicate<ExecutableElement> isVoidReturnType = method -> "void".equals(TypeUtils.toString(method.getReturnType()));
     *
     * List<ExecutableElement> filteredMethods = MethodUtils.filterMethods(methods, isPublic, isVoidReturnType);
     * for (ExecutableElement method : filteredMethods) {
     *     System.out.println("Public Method with Void Return Type: " + method.getSimpleName());
     * }
     * }</pre>
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
            Predicate<? super ExecutableElement> combinedPredicate = and(methodFilters);
            filteredMethods = methods.stream()
                    .filter(combinedPredicate)
                    .collect(toList());
        }

        return filteredMethods.isEmpty() ? emptyList() : filteredMethods;
    }

    /**
     * Gets the simple name of the method as a string.
     *
     * <p>If the provided method is null, this method returns null.
     * Otherwise, it returns the simple name of the method as a string.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ExecutableElement method = ...; // Obtain a valid ExecutableElement representing a method
     * String methodName = MethodUtils.getMethodName(method);
     * if (methodName != null) {
     *     System.out.println("Method Name: " + methodName);
     * } else {
     *     System.out.println("Method is null.");
     * }
     * }</pre>
     *
     * @param method the executable element representing the method, may be null
     * @return the simple name of the method as a string, or null if the method is null
     */
    static String getMethodName(ExecutableElement method) {
        return method == null ? null : method.getSimpleName().toString();
    }

    /**
     * Gets the return type name of the given method.
     *
     * <p>If the provided method is null, this method returns null.
     * Otherwise, it returns the fully qualified name of the method's return type.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ExecutableElement method = ...; // Obtain a valid ExecutableElement representing a method
     * String returnTypeName = MethodUtils.getReturnTypeName(method);
     * if (returnTypeName != null) {
     *     System.out.println("Return Type Name: " + returnTypeName);
     * } else {
     *     System.out.println("Method is null or has no return type.");
     * }
     * }</pre>
     *
     * @param method the executable element representing the method, may be null
     * @return the fully qualified name of the method's return type as a string, or null if the method is null
     */
    static String getReturnTypeName(ExecutableElement method) {
        return method == null ? null : TypeUtils.toString(method.getReturnType());
    }

    /**
     * Gets the parameter type mirrors of the given method.
     *
     * <p>This method returns a list of type mirrors representing the parameter types of the provided method.
     * If the method is {@code null} or has no parameters, an empty list is returned.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ExecutableElement method = ...; // Obtain a valid ExecutableElement representing a method
     * List<TypeMirror> parameterTypeMirrors = MethodUtils.getMethodParameterTypeMirrors(method);
     *
     * if (!parameterTypeMirrors.isEmpty()) {
     *     for (TypeMirror typeMirror : parameterTypeMirrors) {
     *         System.out.println("Parameter Type: " + TypeUtils.toString(typeMirror));
     *     }
     * } else {
     *     System.out.println("Method is null or has no parameters.");
     * }
     * }</pre>
     *
     * @param method the executable element representing the method, may be null
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
     * <p>This method returns an array of strings representing the fully qualified names
     * of the parameter types of the provided method. If the method is {@code null} or has no parameters,
     * an empty array is returned.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ExecutableElement method = ...; // Obtain a valid ExecutableElement representing a method
     * String[] parameterTypeNames = MethodUtils.getMethodParameterTypeNames(method);
     *
     * if (parameterTypeNames.length > 0) {
     *     for (String typeName : parameterTypeNames) {
     *         System.out.println("Parameter Type: " + typeName);
     *     }
     * } else {
     *     System.out.println("Method is null or has no parameters.");
     * }
     * }</pre>
     *
     * @param method the executable element representing the method, may be null
     * @return an array of strings representing the fully qualified names of the parameter types,
     * or an empty array if the method is null or has no parameters
     */
    static String[] getMethodParameterTypeNames(ExecutableElement method) {
        List<TypeMirror> parameterTypes = getMethodParameterTypeMirrors(method);
        return parameterTypes.isEmpty() ? EMPTY_STRING_ARRAY : parameterTypes.stream().map(TypeUtils::toString).toArray(String[]::new);
    }

    /**
     * Checks if the given method matches the specified method name and parameter types.
     *
     * <p>This method determines whether the provided executable element (method) has the same name
     * and parameter types as specified. If the method is null or any of the parameters are null,
     * the result will be false.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ExecutableElement method = ...; // Obtain a valid ExecutableElement
     * boolean isMatch = matches(method, "myMethod", String.class, int.class);
     * if (isMatch) {
     *     System.out.println("The method matches the specified name and parameter types.");
     * } else {
     *     System.out.println("The method does not match.");
     * }
     * }</pre>
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
     * <p>This method determines whether the provided executable element (method) has the same name
     * and parameter type names as specified. If the method is null or any of the parameters are null,
     * the result will be false.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ExecutableElement method = ...; // Obtain a valid ExecutableElement
     * boolean isMatch = matches(method, "myMethod", "java.lang.String", "int");
     * if (isMatch) {
     *     System.out.println("The method matches the specified name and parameter type names.");
     * } else {
     *     System.out.println("The method does not match.");
     * }
     * }</pre>
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
     * <p>This method compares the simple name of the provided executable element (method)
     * with the given method name. If either the method or the method name is {@code null},
     * the comparison is performed using {@link Objects#equals(Object, Object)}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ExecutableElement method = ...; // Obtain a valid ExecutableElement
     * boolean isMatch = MethodUtils.matchesMethodName(method, "myMethod");
     *
     * if (isMatch) {
     *     System.out.println("The method name matches.");
     * } else {
     *     System.out.println("The method name does not match.");
     * }
     * }</pre>
     *
     * @param method     the executable element representing the method to check
     * @param methodName the name of the method to match, may be null
     * @return true if the method's name matches the given method name, false otherwise
     */
    static boolean matchesMethodName(ExecutableElement method, String methodName) {
        return Objects.equals(getMethodName(method), methodName);
    }

    /**
     * Checks if the given method matches the specified method name and parameter types.
     *
     * <p>This method determines whether the provided executable element (method) has the same name
     * and parameter types as specified. If the method is null or any of the parameters are null,
     * the result will be false.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ExecutableElement method = ...; // Obtain a valid ExecutableElement
     * boolean isMatch = MethodUtils.matchesMethod(method, "myMethod", String.class, int.class);
     * if (isMatch) {
     *     System.out.println("The method matches the specified name and parameter types.");
     * } else {
     *     System.out.println("The method does not match.");
     * }
     * }</pre>
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

        // Check if the method name matches
        if (!matchesMethodName(method, methodName)) {
            return false;
        }

        // Check if the parameter types match
        if (!matchParameterTypes(method, parameterTypes)) {
            return false;
        }

        return true;
    }

    /**
     * Checks if the given method matches the specified method name and parameter type names.
     *
     * <p>This method determines whether the provided executable element (method) has the same name
     * and parameter type names as specified. If the method is null or any of the parameters are null,
     * the result will be false.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ExecutableElement method = ...; // Obtain a valid ExecutableElement
     * boolean isMatch = MethodUtils.matchesMethod(method, "myMethod", "java.lang.String", "int");
     * if (isMatch) {
     *     System.out.println("The method matches the specified name and parameter type names.");
     * } else {
     *     System.out.println("The method does not match.");
     * }
     * }</pre>
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
     * Returns the enclosing element of the given executable method.
     *
     * <p>This method retrieves the element that directly encloses the provided method.
     * If the method is {@code null}, the method returns {@code null}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ExecutableElement method = ...; // Obtain a valid ExecutableElement
     * Element enclosingElement = MethodUtils.getEnclosingElement(method);
     * if (enclosingElement != null) {
     *     System.out.println("Enclosing Element: " + enclosingElement.getSimpleName());
     * } else {
     *     System.out.println("Method is null or has no enclosing element.");
     * }
     * }</pre>
     *
     * @param method the executable element representing the method, may be null
     * @return the enclosing element of the method, or null if the method is null
     */
    static Element getEnclosingElement(ExecutableElement method) {
        return method == null ? null : method.getEnclosingElement();
    }

    /**
     * Creates a predicate that filters out methods declared in the specified excluded types.
     *
     * <p>This predicate can be used to exclude methods that are declared in certain types,
     * such as standard Java types like {@link Object}, when searching or filtering through methods.
     * If no excluded types are provided, the predicate will allow all methods.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Predicate<ExecutableElement> excludeObjectMethods = methodPredicateForExcludedTypes(Object.class);
     * List<ExecutableElement> filteredMethods = findDeclaredMethods(typeMirror, excludeObjectMethods);
     *
     * for (ExecutableElement method : filteredMethods) {
     *     System.out.println("Filtered Method: " + method.getSimpleName());
     * }
     * }</pre>
     *
     * @param excludedTypes the types whose methods should be excluded from the result, optional
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
