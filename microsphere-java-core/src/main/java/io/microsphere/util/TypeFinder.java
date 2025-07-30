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
package io.microsphere.util;

import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.lang.function.Streams.filterList;
import static io.microsphere.reflect.TypeUtils.asClass;
import static io.microsphere.reflect.TypeUtils.isObjectType;
import static io.microsphere.util.ArrayUtils.EMPTY_TYPE_ARRAY;
import static io.microsphere.util.ArrayUtils.contains;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static io.microsphere.util.Assert.assertNoNullElements;
import static io.microsphere.util.Assert.assertNotEmpty;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.TypeFinder.Include.HIERARCHICAL;
import static io.microsphere.util.TypeFinder.Include.INTERFACES;
import static io.microsphere.util.TypeFinder.Include.SELF;
import static io.microsphere.util.TypeFinder.Include.SUPER_CLASS;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

/**
 * A utility class for finding related types based on a given type.
 * <p>
 * This class allows searching for various related types such as:
 * <ul>
 *     <li>Self (the type itself)</li>
 *     <li>Direct superclass</li>
 *     <li>Interfaces directly implemented by the type</li>
 *     <li>Hierarchical types (recursive search through superclasses and interfaces)</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Find all hierarchical types of String.class including itself
 * TypeFinder<Class<?>> finder = TypeFinder.classFinder(String.class, TypeFinder.Include.HIERARCHICAL);
 * List<Class<?>> types = finder.getTypes();
 *
 * // Find only interfaces implemented by ArrayList.class
 * TypeFinder<Class<?>> finder = TypeFinder.classFinder(ArrayList.class, TypeFinder.Include.INTERFACES);
 * List<Class<?>> interfaceTypes = finder.getTypes();
 *
 * // Custom combination: include self and interfaces but not superclass
 * TypeFinder<Class<?>> finder = new TypeFinder<>(MyClass.class,
 *     TypeFinder.classGetSuperClassFunction,
 *     TypeFinder.classGetInterfacesFunction,
 *     true,  // includeSelf
 *     false, // includeHierarchicalTypes
 *     false, // includeSuperclass
 *     true); // includeInterfaces
 * }</pre>
 *
 * @param <T> the specific type to find related types from
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Type
 * @since 1.0.0
 */
public class TypeFinder<T> {

    /**
     * The enumeration for the type finder includes
     */
    public static enum Include {

        SELF,

        HIERARCHICAL,

        SUPER_CLASS,

        INTERFACES
    }

    private final T type;

    private final boolean includeSelf;

    private final boolean includeHierarchicalTypes;

    private final boolean includeSuperclass;

    private final boolean includeInterfaces;

    private final Function<T, ? super T> getSuperClassFunction;

    private final Function<T, ? super T[]> getInterfacesFunction;

    public TypeFinder(T type, Function<T, T> getSuperClassFunction,
                      Function<T, T[]> getInterfacesFunction, boolean includeSelf,
                      boolean includeHierarchicalTypes, boolean includeSuperclass, boolean includeInterfaces) {
        assertNotNull(type, () -> "The 'type' must not be null");
        assertNotNull(getSuperClassFunction, () -> "The 'getSuperClassFunction' must not be null");
        assertNotNull(getInterfacesFunction, () -> "The 'getInterfacesFunction' must not be null");
        this.type = type;
        this.getSuperClassFunction = getSuperClassFunction;
        this.getInterfacesFunction = getInterfacesFunction;
        this.includeSelf = includeSelf;
        this.includeHierarchicalTypes = includeHierarchicalTypes;
        this.includeSuperclass = includeSuperclass;
        this.includeInterfaces = includeInterfaces;
    }

    @Nonnull
    @Immutable
    public List<T> getTypes() {
        return findTypes(EMPTY_PREDICATE_ARRAY);
    }

    @Nonnull
    @Immutable
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

    protected List<T> getSuperTypes(T type, boolean includeSuperclass, boolean includedGenericInterfaces) {

        T superclass = includeSuperclass && type != null ? getSuperClass(type) : null;

        boolean hasSuperclass = superclass != null;

        if (!hasSuperclass && !includedGenericInterfaces) {
            return emptyList();
        }

        T[] interfaceTypes = includedGenericInterfaces ? getInterfaces(type) : (T[]) EMPTY_TYPE_ARRAY;
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

    protected T getSuperClass(T type) {
        return (T) getSuperClassFunction.apply(type);
    }

    protected T[] getInterfaces(T type) {
        return (T[]) getInterfacesFunction.apply(type);
    }

    protected void addSuperTypes(List<T> allTypes, T type, boolean includeHierarchicalTypes, boolean includeSuperclass, boolean includeInterfaces) {
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

    final static Function<Class, Class> classGetSuperClassFunction = Class::getSuperclass;

    final static Function<Class, Class[]> classGetInterfacesFunction = Class::getInterfaces;

    final static Function<Type, Type> genericTypeGetSuperClassFunction = type -> {
        Class<?> klass = asClass(type);
        return klass == null ? null : klass.getGenericSuperclass();
    };

    final static Function<Type, Type[]> genericTypeGetInterfacesFunction = type -> {
        Class<?> klass = asClass(type);
        return klass == null ? null : klass.getGenericInterfaces();
    };

    public static TypeFinder<Class<?>> classFinder(Class type, TypeFinder.Include... includes) {
        assertNotEmpty(includes, () -> "The 'includes' must not be empty");
        assertNoNullElements(includes, () -> "The 'includes' must not contain null element");
        return classFinder(type, contains(includes, SELF), contains(includes, HIERARCHICAL),
                contains(includes, SUPER_CLASS), contains(includes, INTERFACES));
    }

    public static TypeFinder<Class<?>> classFinder(Class type, boolean includeSelf, boolean includeHierarchicalTypes,
                                                   boolean includeSuperclass, boolean includeInterfaces) {
        return new TypeFinder(type, classGetSuperClassFunction, classGetInterfacesFunction, includeSelf,
                includeHierarchicalTypes, includeSuperclass, includeInterfaces);
    }

    public static TypeFinder<Type> genericTypeFinder(Type type, TypeFinder.Include... includes) {
        assertNotEmpty(includes, () -> "The 'includes' must not be empty");
        assertNoNullElements(includes, () -> "The 'includes' must not contain null element");
        return genericTypeFinder(type, contains(includes, SELF), contains(includes, HIERARCHICAL),
                contains(includes, SUPER_CLASS), contains(includes, INTERFACES));
    }

    public static TypeFinder<Type> genericTypeFinder(Type type, boolean includeSelf, boolean includeHierarchicalTypes,
                                                     boolean includeSuperclass, boolean includeInterfaces) {
        return new TypeFinder(type, genericTypeGetSuperClassFunction, genericTypeGetInterfacesFunction, includeSelf,
                includeHierarchicalTypes, includeSuperclass, includeInterfaces);
    }

}
