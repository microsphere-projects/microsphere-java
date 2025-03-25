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

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static io.microsphere.annotation.processor.util.TypeUtils.getAllDeclaredTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.isSameType;
import static io.microsphere.annotation.processor.util.TypeUtils.ofTypeElement;
import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.reflect.TypeUtils.getTypeNames;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static io.microsphere.util.ArrayUtils.length;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * The utilities class for the members in the package "javax.lang.model.", such as "field", "method", "constructor"
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface MemberUtils {

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

    static List<? extends Element> getDeclaredMembers(TypeMirror type) {
        return type == null ? emptyList() : getDeclaredMembers(ofTypeElement(type));
    }

    static List<? extends Element> getDeclaredMembers(TypeElement type) {
        return type == null ? emptyList() : findDeclaredMembers(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<? extends Element> getAllDeclaredMembers(TypeMirror type) {
        return type == null ? emptyList() : findAllDeclaredMembers(ofTypeElement(type), EMPTY_PREDICATE_ARRAY);
    }

    static List<? extends Element> getAllDeclaredMembers(TypeElement type) {
        return type == null ? emptyList() : findAllDeclaredMembers(type, EMPTY_PREDICATE_ARRAY);
    }

    static List<? extends Element> findDeclaredMembers(TypeMirror type, Predicate<? super Element>... memberFilters) {
        return type == null ? emptyList() : findDeclaredMembers(ofTypeElement(type), memberFilters);
    }

    static List<? extends Element> findDeclaredMembers(TypeElement type, Predicate<? super Element>... memberFilters) {
        if (type == null) {
            return emptyList();
        }
        return filterMembers(type.getEnclosedElements(), memberFilters);
    }

    static List<? extends Element> findAllDeclaredMembers(TypeMirror type, Predicate<? super Element>... memberFilters) {
        return type == null ? emptyList() : findAllDeclaredMembers(ofTypeElement(type), memberFilters);
    }

    static List<? extends Element> findAllDeclaredMembers(TypeElement type, Predicate<? super Element>... memberFilters) {
        if (type == null) {
            return emptyList();
        }
        List<? extends Element> declaredMembers = getAllDeclaredTypes(type)
                .stream()
                .map(MemberUtils::getDeclaredMembers)
                .flatMap(Collection::stream)
                .collect(toList());
        return filterMembers(declaredMembers, memberFilters);
    }

    static List<? extends Element> filterMembers(List<? extends Element> members, Predicate<? super Element>... memberFilters) {
        if (isEmpty(members)) {
            return emptyList();
        }
        if (isNotEmpty(memberFilters)) {
            Predicate predicate = and(memberFilters);
            members = (List) members.stream().filter(predicate).collect(toList());
        }
        return members.isEmpty() ? emptyList() : members;
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
