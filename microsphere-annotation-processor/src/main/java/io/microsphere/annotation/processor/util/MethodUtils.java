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
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static io.microsphere.annotation.processor.util.MemberUtils.getDeclaredMembers;
import static io.microsphere.annotation.processor.util.ElementUtils.isPublicNonStatic;
import static io.microsphere.annotation.processor.util.ElementUtils.matchParameterTypeNames;
import static io.microsphere.annotation.processor.util.ElementUtils.matchParameterTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllDeclaredTypes;
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

    static List<ExecutableElement> getDeclaredMethods(TypeElement type) {
        return findDeclaredMethods(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<ExecutableElement> getDeclaredMethods(TypeMirror type) {
        return findDeclaredMethods(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<ExecutableElement> getAllDeclaredMethods(TypeElement type) {
        return findAllDeclaredMethods(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<ExecutableElement> getAllDeclaredMethods(TypeMirror type) {
        return findAllDeclaredMethods(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<ExecutableElement> findDeclaredMethods(TypeElement type, Predicate<? super ExecutableElement>... methodFilters) {
        if (type == null) {
            return emptyList();
        }
        return findDeclaredMethods(type.asType(), methodFilters);
    }

    static List<ExecutableElement> findDeclaredMethods(TypeMirror type, Predicate<? super ExecutableElement>... methodFilters) {
        if (type == null) {
            return emptyList();
        }

        List<? extends Element> declaredMembers = getDeclaredMembers(type);
        if (declaredMembers.isEmpty()) {
            return emptyList();
        }

        List<ExecutableElement> declaredMethods = methodsIn(declaredMembers);
        if (declaredMethods.isEmpty()) {
            return emptyList();
        }

        return filterMethods(declaredMethods, methodFilters);
    }

    static List<ExecutableElement> findAllDeclaredMethods(TypeElement type, Type... excludedTypes) {
        return type == null ? emptyList() : findAllDeclaredMethods(type.asType(), excludedTypes);
    }

    static List<ExecutableElement> findAllDeclaredMethods(TypeMirror type, Type... excludedTypes) {
        if (type == null) {
            return emptyList();
        }
        return findAllDeclaredMethods(type, methodPredicateForExcludedTypes(excludedTypes));
    }

    static List<ExecutableElement> findPublicNonStaticMethods(TypeElement type, Type... excludedTypes) {
        return type == null ? emptyList() : findPublicNonStaticMethods(ofDeclaredType(type), excludedTypes);
    }

    static List<ExecutableElement> findPublicNonStaticMethods(TypeMirror type, Type... excludedTypes) {
        if (type == null) {
            return emptyList();
        }

        Predicate predicate = and(methodPredicateForExcludedTypes(excludedTypes), MethodUtils::isPublicNonStaticMethod);

        return findAllDeclaredMethods(type, predicate);
    }

    static List<ExecutableElement> findAllDeclaredMethods(TypeElement type, Predicate<? super ExecutableElement>... methodFilters) {
        return type == null ? emptyList() : findAllDeclaredMethods(type.asType(), methodFilters);
    }

    static List<ExecutableElement> findAllDeclaredMethods(TypeMirror type, Predicate<? super ExecutableElement>... methodFilters) {
        if (type == null) {
            return emptyList();
        }

        List<ExecutableElement> allDeclaredMethods = getAllDeclaredTypes(type).stream()
                .map(MethodUtils::getDeclaredMethods)
                .flatMap(Collection::stream)
                .collect(toList());

        if (allDeclaredMethods.isEmpty()) {
            return emptyList();
        }

        return filterMethods(allDeclaredMethods, methodFilters);
    }

    static boolean isMethod(ExecutableElement method) {
        return method != null && METHOD.equals(method.getKind());
    }

    static boolean isPublicNonStaticMethod(ExecutableElement method) {
        return isMethod(method) && isPublicNonStatic(method);
    }

    static ExecutableElement findMethod(TypeElement type, String methodName) {
        return findMethod(type, methodName, EMPTY_TYPE_ARRAY);
    }

    static ExecutableElement findMethod(TypeMirror type, String methodName) {
        return findMethod(type, methodName, EMPTY_TYPE_ARRAY);
    }

    static ExecutableElement findMethod(TypeElement type, String methodName, Type... parameterTypes) {
        return type == null ? null : findMethod(type.asType(), methodName, parameterTypes);
    }

    static ExecutableElement findMethod(TypeMirror type, String methodName, Type... parameterTypes) {
        if (type == null || methodName == null || parameterTypes == null) {
            return null;
        }
        List<ExecutableElement> allDeclaredMethods = findAllDeclaredMethods(type, method -> matches(method, methodName, parameterTypes));
        return allDeclaredMethods.isEmpty() ? null : allDeclaredMethods.get(0);
    }

    static ExecutableElement findMethod(TypeElement type, String methodName, CharSequence... parameterTypeNames) {
        return type == null ? null : findMethod(type.asType(), methodName, parameterTypeNames);
    }

    static ExecutableElement findMethod(TypeMirror type, String methodName, CharSequence... parameterTypeNames) {
        if (type == null || methodName == null || parameterTypeNames == null) {
            return null;
        }
        List<ExecutableElement> allDeclaredMethods = findAllDeclaredMethods(type, method -> matches(method, methodName, parameterTypeNames));
        return allDeclaredMethods.isEmpty() ? null : allDeclaredMethods.get(0);
    }

    static ExecutableElement getOverrideMethod(ProcessingEnvironment processingEnv, TypeElement type, ExecutableElement declaringMethod) {
        Elements elements = processingEnv.getElementUtils();
        return filterFirst(getAllDeclaredMethods(type), method -> elements.overrides(method, declaringMethod, type));
    }

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

    static String getMethodName(ExecutableElement method) {
        return method == null ? null : method.getSimpleName().toString();
    }

    static String getReturnTypeName(ExecutableElement method) {
        return method == null ? null : TypeUtils.toString(method.getReturnType());
    }

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

    static String[] getMethodParameterTypeNames(ExecutableElement method) {
        List<TypeMirror> parameterTypes = getMethodParameterTypeMirrors(method);
        return parameterTypes.isEmpty() ? EMPTY_STRING_ARRAY : parameterTypes.stream().map(TypeUtils::toString).toArray(String[]::new);
    }

    static boolean matches(ExecutableElement method, String methodName, Type... parameterTypes) {
        return matchesMethod(method, methodName, parameterTypes);
    }

    static boolean matches(ExecutableElement method, String methodName, CharSequence... parameterTypeNames) {
        return matchesMethod(method, methodName, parameterTypeNames);
    }

    static boolean matchesMethod(ExecutableElement method, String methodName, Type... parameterTypes) {
        if (method == null || methodName == null || parameterTypes == null) {
            return false;
        }

        // matches the name of method
        if (!Objects.equals(getMethodName(method), methodName)) {
            return false;
        }

        if (!matchParameterTypes(method.getParameters(), parameterTypes)) {
            return false;
        }

        return true;
    }

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
