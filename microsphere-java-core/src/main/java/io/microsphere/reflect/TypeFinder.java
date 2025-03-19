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
package io.microsphere.reflect;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Predicate;

import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.lang.function.Streams.filterList;
import static io.microsphere.reflect.TypeFinder.Include.HIERARCHICAL;
import static io.microsphere.reflect.TypeFinder.Include.INTERFACES;
import static io.microsphere.reflect.TypeFinder.Include.SELF;
import static io.microsphere.reflect.TypeFinder.Include.SUPER_CLASS;
import static io.microsphere.reflect.TypeUtils.asClass;
import static io.microsphere.reflect.TypeUtils.isObjectType;
import static io.microsphere.util.ArrayUtils.EMPTY_TYPE_ARRAY;
import static io.microsphere.util.ArrayUtils.contains;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static io.microsphere.util.Assert.assertNoNullElements;
import static io.microsphere.util.Assert.assertNotEmpty;
import static io.microsphere.util.Assert.assertNotNull;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

/**
 * The finder class for {@link Type}
 *
 * @param <T> the type of {@link Type}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Type
 * @since 1.0.0
 */
public class TypeFinder<T extends Type> {

    private final T type;

    private final boolean generic;

    private final boolean includeSelf;

    private final boolean includeHierarchicalTypes;

    private final boolean includeSuperclass;

    private final boolean includeInterfaces;

    protected TypeFinder(T type, Include[] includes) {
        this(type, false, includes);
    }

    protected TypeFinder(T type, boolean generic, Include[] includes) {
        assertNotNull(type, () -> "The 'type' must not be null");
        assertNotEmpty(includes, () -> "The 'includes' must not be empty");
        assertNoNullElements(includes, () -> "The 'includes' must not contain null element");
        this.type = type;
        this.generic = generic;
        this.includeSelf = contains(includes, SELF);
        this.includeHierarchicalTypes = contains(includes, HIERARCHICAL);
        this.includeSuperclass = contains(includes, SUPER_CLASS);
        this.includeInterfaces = contains(includes, INTERFACES);
    }

    protected TypeFinder(T type, boolean generic, boolean includeSelf, boolean includeHierarchicalTypes, boolean includeSuperclass, boolean includeInterfaces) {
        assertNotNull(type, () -> "The 'type' must not be null");
        this.type = type;
        this.generic = generic;
        this.includeSelf = includeSelf;
        this.includeHierarchicalTypes = includeHierarchicalTypes;
        this.includeSuperclass = includeSuperclass;
        this.includeInterfaces = includeInterfaces;
    }

    public List<T> getTypes() {
        return findTypes(EMPTY_PREDICATE_ARRAY);
    }

    public List<T> findTypes(Predicate<? super T>... typeFilters) {
        List<T> types = doFindTypes(typeFilters);
        return types.isEmpty() ? emptyList() : unmodifiableList(types);
    }

    protected List<T> doFindTypes(Predicate<? super T>[] typeFilters) {

        List<T> allTypes = newLinkedList();

        if (includeSelf) {
            // add self
            allTypes.add(type);
        }

        // Add all hierarchical types in declaration order
        addSuperTypes(allTypes, type, includeHierarchicalTypes, includeSuperclass, includeInterfaces);

        if (isNotEmpty(typeFilters)) {
            // filter types by the chain
            allTypes = filterList(allTypes, and(typeFilters));
        }

        return allTypes;
    }

    protected List<T> getSuperTypes(Type type, boolean includeSuperclass, boolean includedGenericInterfaces) {

        Class<?> klass = asClass(type);

        T superclass = includeSuperclass && klass != null ? getSuperClass(klass) : null;

        boolean hasSuperclass = superclass != null;

        if (!hasSuperclass && !includedGenericInterfaces) {
            return emptyList();
        }

        T[] interfaceTypes = includedGenericInterfaces ? getInterfaces(klass) : (T[]) EMPTY_TYPE_ARRAY;
        int interfaceTypesLength = interfaceTypes.length;

        int size = interfaceTypesLength + (hasSuperclass ? 1 : 0);

        if (size == 0) {
            return emptyList();
        }

        List<T> types = newArrayList(size);

        if (hasSuperclass) {
            if (!types.contains(superclass)) {
                types.add(superclass);
            }
        }

        for (int i = 0; i < interfaceTypesLength; i++) {
            T interfaceType = interfaceTypes[i];
            if (!types.contains(interfaceType)) {
                types.add(interfaceType);
            }
        }
        return types;
    }

    protected T getSuperClass(Class<?> klass) {
        return generic ? (T) klass.getGenericSuperclass() : (T) klass.getSuperclass();
    }

    protected T[] getInterfaces(Class<?> klass) {
        return generic ? (T[]) klass.getGenericInterfaces() : (T[]) klass.getInterfaces();
    }

    protected void addSuperTypes(List<T> allTypes, Type type, boolean includeHierarchicalTypes, boolean includeSuperclass, boolean includeInterfaces) {
        if (isObjectType(type)) {
            return;
        }

        List<T> superTypes = getSuperTypes(type, includeSuperclass, includeInterfaces);

        int superTypesSize = superTypes.size();

        if (!includeSuperclass && includeHierarchicalTypes) { // add super types recursively if necessary
            List<T> parentTypes = getSuperTypes(type, true, false);
            int size = parentTypes.size();
            for (int i = 0; i < size; i++) {
                addSuperTypes(allTypes, parentTypes.get(i), true, false, includeInterfaces);
            }
        }

        if (superTypesSize < 1) {
            return;
        }

        for (int i = 0; i < superTypesSize; i++) {
            T superType = superTypes.get(i);
            if (!allTypes.contains(superType)) {
                allTypes.add(superType);
            }
            if (includeHierarchicalTypes) {
                addSuperTypes(allTypes, superType, true, includeSuperclass, includeInterfaces);
            }
        }
    }

    public static TypeFinder<Class<?>> classFinder(Class<?> type, Include... includes) {
        return new TypeFinder<>(type, includes);
    }

    public static TypeFinder<Class<?>> classFinder(Class<?> type, boolean includeSelf, boolean includeHierarchicalTypes, boolean includeSuperclass, boolean includeInterfaces) {
        return of(type, false, includeSelf, includeHierarchicalTypes, includeSuperclass, includeInterfaces);
    }

    public static TypeFinder<Type> genericTypeFinder(Type type, Include... includes) {
        return new TypeFinder<>(type, true, includes);
    }

    public static <T extends Type> TypeFinder<T> genericTypeFinder(T type, boolean includeSelf, boolean includeHierarchicalTypes, boolean includeSuperclass, boolean includeInterfaces) {
        return of(type, true, includeSelf, includeHierarchicalTypes, includeSuperclass, includeInterfaces);
    }

    public static <T extends Type> TypeFinder<T> of(T type, boolean generic, boolean includeSelf, boolean includeHierarchicalTypes, boolean includeSuperclass, boolean includeInterfaces) {
        return new TypeFinder(type, generic, includeSelf, includeHierarchicalTypes, includeSuperclass, includeInterfaces);
    }

    /**
     * The enumeration for the type finder includes
     */
    public enum Include {

        SELF,

        HIERARCHICAL,

        SUPER_CLASS,

        INTERFACES
    }
}
