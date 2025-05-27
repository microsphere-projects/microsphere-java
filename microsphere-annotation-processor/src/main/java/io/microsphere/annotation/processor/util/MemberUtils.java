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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static io.microsphere.annotation.processor.util.ElementUtils.filterElements;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllDeclaredTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.ofTypeElement;
import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * The utilities class for the members in the package "javax.lang.model.", such as "field", "method", "constructor"
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface MemberUtils extends Utils {

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

    static List<? extends Element> getDeclaredMembers(TypeMirror type, boolean all) {
        return all ? getAllDeclaredMembers(type) : getDeclaredMembers(type);
    }

    static List<? extends Element> getDeclaredMembers(TypeElement type, boolean all) {
        return all ? getAllDeclaredMembers(type) : getDeclaredMembers(type);
    }

    static List<? extends Element> findDeclaredMembers(TypeMirror type, Predicate<? super Element>... memberFilters) {
        return type == null ? emptyList() : findDeclaredMembers(ofTypeElement(type), memberFilters);
    }

    static List<? extends Element> findDeclaredMembers(TypeElement type, Predicate<? super Element>... memberFilters) {
        if (type == null) {
            return emptyList();
        }
        return filterElements(type.getEnclosedElements(), memberFilters);
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
        return filterElements(declaredMembers, memberFilters);
    }

    static List<? extends Element> findDeclaredMembers(TypeMirror type, boolean all, Predicate<? super Element>... memberFilters) {
        return all ? findAllDeclaredMembers(type, memberFilters) : findDeclaredMembers(type, memberFilters);
    }

    static List<? extends Element> findDeclaredMembers(TypeElement type, boolean all, Predicate<? super Element>... memberFilters) {
        return all ? findAllDeclaredMembers(type, memberFilters) : findDeclaredMembers(type, memberFilters);
    }

}
