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

import static io.microsphere.collection.ListUtils.addIfAbsent;
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
import static io.microsphere.util.ArrayUtils.length;
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

    /**
     * Constructs a new {@code TypeFinder} with the specified type and configuration options.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   TypeFinder<Class<?>> finder = new TypeFinder<>(
     *       ArrayList.class,
     *       (Function) Class::getSuperclass,
     *       (Function) Class::getInterfaces,
     *       true,  // includeSelf
     *       true,  // includeHierarchicalTypes
     *       true,  // includeSuperclass
     *       true   // includeInterfaces
     *   );
     * }</pre>
     *
     * @param type                     the root type to find related types from
     * @param getSuperClassFunction    a function to retrieve the superclass of a given type
     * @param getInterfacesFunction    a function to retrieve the interfaces of a given type
     * @param includeSelf              whether to include the type itself in the results
     * @param includeHierarchicalTypes whether to recursively include types from the hierarchy
     * @param includeSuperclass        whether to include the direct superclass
     * @param includeInterfaces        whether to include the directly implemented interfaces
     * @since 1.0.0
     */
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

    /**
     * Returns all related types based on the configuration of this {@code TypeFinder},
     * without applying any filters.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   TypeFinder<Class<?>> finder = TypeFinder.classFinder(ArrayList.class, TypeFinder.Include.HIERARCHICAL);
     *   List<Class<?>> types = finder.getTypes();
     *   // types contains AbstractList, AbstractCollection, Object, List, Collection, Iterable, ...
     * }</pre>
     *
     * @return an immutable list of related types, or an empty list if none are found
     * @since 1.0.0
     */
    @Nonnull
    @Immutable
    public List<T> getTypes() {
        return findTypes(EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Finds related types that match all the specified filters.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   TypeFinder<Class<?>> finder = TypeFinder.classFinder(HashMap.class, TypeFinder.Include.HIERARCHICAL);
     *   List<Class<?>> interfaces = finder.findTypes(Class::isInterface);
     *   // interfaces contains Map, Cloneable, Serializable, ...
     * }</pre>
     *
     * @param typeFilters zero or more predicates to filter the found types; all predicates must match
     * @return an immutable list of matching types, or an empty list if none match
     * @since 1.0.0
     */
    @Nonnull
    @Immutable
    public List<T> findTypes(Predicate<? super T>... typeFilters) {
        List<T> types = doFindTypes(typeFilters);
        return types.isEmpty() ? emptyList() : unmodifiableList(types);
    }

    /**
     * Performs the actual type finding logic by collecting related types and applying filters.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Typically invoked internally by findTypes:
     *   TypeFinder<Class<?>> finder = TypeFinder.classFinder(LinkedList.class, TypeFinder.Include.HIERARCHICAL);
     *   List<Class<?>> types = finder.findTypes(t -> t != Object.class);
     * }</pre>
     *
     * @param typeFilters an array of predicates to filter the found types
     * @return a mutable list of types matching the filters
     * @since 1.0.0
     */
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

    /**
     * Retrieves the direct super types (superclass and/or interfaces) of the given type.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   TypeFinder<Class<?>> finder = TypeFinder.classFinder(ArrayList.class, TypeFinder.Include.HIERARCHICAL);
     *   // Internally called as:
     *   // getSuperTypes(ArrayList.class, true, true)
     *   // returns [AbstractList, List, RandomAccess, Cloneable, Serializable]
     * }</pre>
     *
     * @param type                     the type whose super types are to be retrieved
     * @param includeSuperclass        whether to include the direct superclass
     * @param includedGenericInterfaces whether to include the directly implemented interfaces
     * @return a list of direct super types, or an empty list if none are found
     * @since 1.0.0
     */
    protected List<T> getSuperTypes(T type, boolean includeSuperclass, boolean includedGenericInterfaces) {

        T superclass = includeSuperclass && type != null ? getSuperClass(type) : null;

        boolean hasSuperclass = superclass != null;

        if (!hasSuperclass && !includedGenericInterfaces) {
            return emptyList();
        }

        T[] interfaceTypes = includedGenericInterfaces ? getInterfaces(type) : (T[]) EMPTY_TYPE_ARRAY;
        int interfaceTypesLength = length(interfaceTypes);

        int size = interfaceTypesLength + (hasSuperclass ? 1 : 0);

        if (size == 0) {
            return emptyList();
        }

        List<T> types = newArrayList(size);

        if (hasSuperclass) {
            types.add(superclass);
        }

        for (int i = 0; i < interfaceTypesLength; i++) {
            T interfaceType = interfaceTypes[i];
            addIfAbsent(types, interfaceType);
        }
        return types;
    }

    /**
     * Returns the superclass of the given type using the configured function.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   TypeFinder<Class<?>> finder = TypeFinder.classFinder(HashMap.class, TypeFinder.Include.SUPER_CLASS);
     *   // Internally: getSuperClass(HashMap.class) returns AbstractMap.class
     * }</pre>
     *
     * @param type the type whose superclass is to be retrieved
     * @return the superclass of the given type, or {@code null} if none exists
     * @since 1.0.0
     */
    protected T getSuperClass(T type) {
        return (T) getSuperClassFunction.apply(type);
    }

    /**
     * Returns the interfaces directly implemented by the given type using the configured function.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   TypeFinder<Class<?>> finder = TypeFinder.classFinder(ArrayList.class, TypeFinder.Include.INTERFACES);
     *   // Internally: getInterfaces(ArrayList.class) returns [List, RandomAccess, Cloneable, Serializable]
     * }</pre>
     *
     * @param type the type whose interfaces are to be retrieved
     * @return an array of interfaces implemented by the given type
     * @since 1.0.0
     */
    protected T[] getInterfaces(T type) {
        return (T[]) getInterfacesFunction.apply(type);
    }

    /**
     * Recursively adds the super types of the given type to the accumulated list.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   TypeFinder<Class<?>> finder = TypeFinder.classFinder(LinkedList.class, TypeFinder.Include.HIERARCHICAL);
     *   // Internally, addSuperTypes is called recursively to collect:
     *   // AbstractSequentialList, AbstractList, AbstractCollection, Object, List, Deque, Queue, ...
     * }</pre>
     *
     * @param allTypes               the accumulated list of types to add to
     * @param type                   the current type whose super types are being added
     * @param includeHierarchicalTypes whether to recursively traverse the type hierarchy
     * @param includeSuperclass      whether to include superclasses
     * @param includeInterfaces      whether to include interfaces
     * @since 1.0.0
     */
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
            addIfAbsent(allTypes, superType);

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

    /**
     * Creates a {@code TypeFinder} for {@link Class} types using the specified include options.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   TypeFinder<Class<?>> finder = TypeFinder.classFinder(ArrayList.class,
     *       TypeFinder.Include.SELF, TypeFinder.Include.INTERFACES);
     *   List<Class<?>> types = finder.getTypes();
     *   // types contains ArrayList plus its directly implemented interfaces
     * }</pre>
     *
     * @param type     the class to find related types from
     * @param includes one or more {@link Include} options specifying which types to include
     * @return a new {@code TypeFinder} configured for the given class and options
     * @since 1.0.0
     */
    public static TypeFinder<Class<?>> classFinder(Class type, TypeFinder.Include... includes) {
        assertNotEmpty(includes, () -> "The 'includes' must not be empty");
        assertNoNullElements(includes, () -> "The 'includes' must not contain null element");
        return classFinder(type, contains(includes, SELF), contains(includes, HIERARCHICAL),
                contains(includes, SUPER_CLASS), contains(includes, INTERFACES));
    }

    /**
     * Creates a {@code TypeFinder} for {@link Class} types with explicit boolean configuration.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   TypeFinder<Class<?>> finder = TypeFinder.classFinder(HashMap.class,
     *       true,  // includeSelf
     *       true,  // includeHierarchicalTypes
     *       true,  // includeSuperclass
     *       false  // includeInterfaces
     *   );
     *   List<Class<?>> types = finder.getTypes();
     *   // types contains HashMap, AbstractMap, Object
     * }</pre>
     *
     * @param type                     the class to find related types from
     * @param includeSelf              whether to include the class itself
     * @param includeHierarchicalTypes whether to recursively include types from the hierarchy
     * @param includeSuperclass        whether to include superclasses
     * @param includeInterfaces        whether to include interfaces
     * @return a new {@code TypeFinder} configured for the given class and options
     * @since 1.0.0
     */
    public static TypeFinder<Class<?>> classFinder(Class type, boolean includeSelf, boolean includeHierarchicalTypes,
                                                   boolean includeSuperclass, boolean includeInterfaces) {
        return new TypeFinder(type, classGetSuperClassFunction, classGetInterfacesFunction, includeSelf,
                includeHierarchicalTypes, includeSuperclass, includeInterfaces);
    }

    /**
     * Creates a {@code TypeFinder} for generic {@link Type} instances using the specified include options.
     * Unlike {@link #classFinder(Class, Include...)}, this method resolves generic superclass and
     * generic interface types.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   TypeFinder<Type> finder = TypeFinder.genericTypeFinder(ArrayList.class,
     *       TypeFinder.Include.SELF, TypeFinder.Include.HIERARCHICAL);
     *   List<Type> types = finder.getTypes();
     *   // types contains generic types such as AbstractList<E>, List<E>, Collection<E>, Iterable<E>, ...
     * }</pre>
     *
     * @param type     the type to find related generic types from
     * @param includes one or more {@link Include} options specifying which types to include
     * @return a new {@code TypeFinder} configured for the given generic type and options
     * @since 1.0.0
     */
    public static TypeFinder<Type> genericTypeFinder(Type type, TypeFinder.Include... includes) {
        assertNotEmpty(includes, () -> "The 'includes' must not be empty");
        assertNoNullElements(includes, () -> "The 'includes' must not contain null element");
        return genericTypeFinder(type, contains(includes, SELF), contains(includes, HIERARCHICAL),
                contains(includes, SUPER_CLASS), contains(includes, INTERFACES));
    }

    /**
     * Creates a {@code TypeFinder} for generic {@link Type} instances with explicit boolean configuration.
     * Unlike {@link #classFinder(Class, boolean, boolean, boolean, boolean)}, this method resolves
     * generic superclass and generic interface types.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   TypeFinder<Type> finder = TypeFinder.genericTypeFinder(HashMap.class,
     *       true,  // includeSelf
     *       false, // includeHierarchicalTypes
     *       true,  // includeSuperclass
     *       true   // includeInterfaces
     *   );
     *   List<Type> types = finder.getTypes();
     *   // types contains HashMap, AbstractMap<K,V>, Map<K,V>, Cloneable, Serializable
     * }</pre>
     *
     * @param type                     the type to find related generic types from
     * @param includeSelf              whether to include the type itself
     * @param includeHierarchicalTypes whether to recursively include types from the hierarchy
     * @param includeSuperclass        whether to include superclasses
     * @param includeInterfaces        whether to include interfaces
     * @return a new {@code TypeFinder} configured for the given generic type and options
     * @since 1.0.0
     */
    public static TypeFinder<Type> genericTypeFinder(Type type, boolean includeSelf, boolean includeHierarchicalTypes,
                                                     boolean includeSuperclass, boolean includeInterfaces) {
        return new TypeFinder(type, genericTypeGetSuperClassFunction, genericTypeGetInterfacesFunction, includeSelf,
                includeHierarchicalTypes, includeSuperclass, includeInterfaces);
    }
}