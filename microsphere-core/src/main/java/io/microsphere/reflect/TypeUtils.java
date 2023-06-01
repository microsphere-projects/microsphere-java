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

import io.microsphere.reflect.generics.TypeArgument;
import io.microsphere.util.ClassUtils;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.lang.function.Streams.filterAll;
import static io.microsphere.lang.function.Streams.filterList;
import static io.microsphere.util.ArrayUtils.of;
import static io.microsphere.util.ClassUtils.getAllSuperClasses;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

/**
 * The utilities class for {@link Type}
 *
 * @since 1.0.0
 */
public abstract class TypeUtils {

    public static final Predicate<Type> NON_OBJECT_TYPE_FILTER = t -> t != null && !isObjectType(t);

    public static final Predicate<Class<?>> NON_OBJECT_CLASS_FILTER = (Predicate) NON_OBJECT_TYPE_FILTER;

    public static final Predicate<Type> TYPE_VARIABLE_FILTER = TypeUtils::isTypeVariable;

    public static final Predicate<Type> PARAMETERIZED_TYPE_FILTER = TypeUtils::isParameterizedType;

    public static final Predicate<Type> WILDCARD_TYPE_FILTER = TypeUtils::isWildcardType;

    public static final Predicate<Type> GENERIC_ARRAY_TYPE_FILTER = TypeUtils::isGenericArrayType;

    public static boolean isClass(Object type) {
        return type instanceof Class;
    }

    public static boolean isObjectType(Object type) {
        return Object.class.equals(type);
    }

    public static boolean isParameterizedType(Object type) {
        return type instanceof ParameterizedType;
    }

    public static boolean isTypeVariable(Object type) {
        return type instanceof TypeVariable;
    }

    public static boolean isWildcardType(Object type) {
        return type instanceof WildcardType;
    }

    public static boolean isGenericArrayType(Object type) {
        return type instanceof GenericArrayType;
    }

    public static Type getRawType(Type type) {
        if (isParameterizedType(type)) {
            return ((ParameterizedType) type).getRawType();
        } else {
            return type;
        }
    }

    public static Class<?> getRawClass(Type type) {
        Type rawType = getRawType(type);
        if (isClass(rawType)) {
            return (Class) rawType;
        }
        return null;
    }

    /**
     * the semantics is same as {@link Class#isAssignableFrom(Class)}
     *
     * @param superType  the super type
     * @param targetType the target type
     * @return see {@link Class#isAssignableFrom(Class)}
     */
    public static boolean isAssignableFrom(Type superType, Type targetType) {
        Class<?> superClass = asClass(superType);
        return isAssignableFrom(superClass, targetType);
    }

    /**
     * the semantics is same as {@link Class#isAssignableFrom(Class)}
     *
     * @param superClass the super class
     * @param targetType the target type
     * @return see {@link Class#isAssignableFrom(Class)}
     */
    public static boolean isAssignableFrom(Class<?> superClass, Type targetType) {
        Class<?> targetClass = asClass(targetType);
        return isAssignableFrom(superClass, targetClass);
    }

    /**
     * the semantics is same as {@link Class#isAssignableFrom(Class)}
     *
     * @param superType  the super type
     * @param targetType the target type
     * @return see {@link Class#isAssignableFrom(Class)}
     */
    protected static boolean isAssignableFrom(Class<?> superType, Class<?> targetType) {
        return ClassUtils.isAssignableFrom(superType, targetType);
    }

    /**
     * Resolve the actual type parameters from the specified type based on some type
     *
     * @param type     the type to be resolved
     * @param baseType The base class or interface of <code>type</code>
     * @return the actual type parameters
     */
    public static List<Type> resolveActualTypeArguments(Type type, Type baseType) {
        Class targetClass = asClass(type);
        Class baseClass = asClass(baseType);
        return resolveActualTypeArguments(targetClass, baseClass);
    }

    /**
     * Resolve the actual type parameters from the specified type based on some type
     *
     * @param targetClass the type to be resolved
     * @param baseClass   The base class or interface of <code>targetClass</code>
     * @return the read-only {@link List} of the actual type parameters
     */
    public static List<Type> resolveActualTypeArguments(Class targetClass, Class baseClass) {
        return unmodifiableList(doResolveActualTypeArguments(targetClass, baseClass));
    }

    /**
     * Resolve the actual type parameter from the specified type and index based on some type
     *
     * @param targetClass the type to be resolved
     * @param baseClass   The base class or interface of <code>targetClass</code>
     * @param index       the index of type parameters
     * @return the read-only {@link List} of the actual type parameters
     */
    public static Type resolveActualTypeArgument(Class targetClass, Class baseClass, int index) {
        return doResolveActualTypeArguments(targetClass, baseClass).get(index);
    }

    /**
     * Resolve the classes of actual type parameters from the specified type based on some type
     *
     * @param targetClass the type to be resolved
     * @param baseClass   The base class or interface of <code>targetClass</code>
     * @return the read-only {@link List} classes of the actual type parameters
     */
    public static List<Type> resolveActualTypeArgumentClasses(Class targetClass, Class baseClass) {
        return unmodifiableList(doResolveActualTypeArgumentClasses(targetClass, baseClass));
    }

    /**
     * Resolve the class of actual type parameter from the specified type and index based on some type
     *
     * @param targetClass the type to be resolved
     * @param baseClass   The base class or interface of <code>targetClass</code>
     * @param index       the index of actual type parameter
     * @return the read-only {@link List} classes of the actual type parameters
     */
    public static Class resolveActualTypeArgumentClass(Class targetClass, Class baseClass, int index) {
        return asClass(resolveActualTypeArgument(targetClass, baseClass, index));
    }

    protected static List<Class> doResolveActualTypeArgumentClasses(Class targetClass, Class baseClass) {
        return doResolveActualTypeArguments(targetClass, baseClass)
                .stream()
                .map(TypeUtils::asClass)
                .filter(Objects::nonNull)
                .collect(toList());
    }

    protected static List<Type> doResolveActualTypeArguments(Class targetClass, Class baseClass) {

        Set<TypeArgument> actualTypeArgumentsSet = doResolveActualTypeArgumentsSet(targetClass, baseClass);

        int size = actualTypeArgumentsSet.size();

        if (actualTypeArgumentsSet.isEmpty()) {
            return emptyList();
        }

        List<Type> actualTypeArguments = new ArrayList<>(size);

        for (TypeArgument actualTypeArgument : actualTypeArgumentsSet) {
            Type typeArgument = actualTypeArgument.getType();
            Integer index = actualTypeArgument.getIndex();
            actualTypeArguments.add(index, typeArgument);
        }

        return actualTypeArguments;
    }

    protected static Set<TypeArgument> doResolveActualTypeArgumentsSet(Class targetClass, Class baseClass) {

        if (!isAssignableFrom(baseClass, targetClass)) { // No hierarchical relationship between type and baseType
            return emptySet();
        }

        TypeVariable<Class>[] baseTypeParameters = baseClass.getTypeParameters();
        int baseTypeParametersLength = baseTypeParameters.length;
        if (baseTypeParametersLength == 0) { // No type-parameter in the base class
            return emptySet();
        }

        Set<TypeArgument> typeArguments = newLinkedHashSet();

        Predicate<Type> typeFilter = t -> isAssignableFrom(baseClass, t);

        for (int i = 0; i < baseTypeParametersLength; i++) {
            typeArguments.add(TypeArgument.create(baseTypeParameters[i], i));
        }

        doResolveActualTypeArgumentsSetHierarchically(typeArguments, targetClass, baseClass, baseTypeParameters, typeFilter);

        Set<TypeArgument> actualTypeArguments = newLinkedHashSet(baseTypeParametersLength);

        for (TypeArgument typeArgument : typeArguments) {
            Type actualTypeArgument = typeArgument.getType();
            if (isClass(actualTypeArgument) || isParameterizedType(actualTypeArgument)) {
                actualTypeArguments.add(typeArgument);
            }
        }

        typeArguments.clear();

        return actualTypeArguments;
    }


    protected static void doResolveActualTypeArgumentsSet(Set<TypeArgument> typeArguments,
                                                          Type type,
                                                          Class baseClass,
                                                          TypeVariable<Class>[] baseTypeParameters,
                                                          Predicate<Type>... typeFilters) {
        ParameterizedType parameterizedType = asParameterizedType(type);
        if (parameterizedType != null) { // ParameterizedType case
            doResolveActualTypeArgumentsSet(typeArguments, parameterizedType, baseClass, baseTypeParameters, typeFilters);
        } else {
            doResolveActualTypeArgumentsSetHierarchically(typeArguments, type, baseClass, baseTypeParameters, typeFilters);
        }
    }

    protected static void doResolveActualTypeArgumentsSet(Set<TypeArgument> typeArgumentsSet,
                                                          ParameterizedType parameterizedType,
                                                          Class baseClass,
                                                          TypeVariable<Class>[] baseTypeParameters,
                                                          Predicate<Type>... typeFilters) {
        int baseTypeParametersLength = baseTypeParameters.length;

        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        Class currentClass = getRawClass(parameterizedType);

        if (Objects.equals(baseClass, currentClass)) { // If same class
            for (int i = 0; i < baseTypeParametersLength; i++) {
                Type actualTypeArgument = actualTypeArguments[i];
                typeArgumentsSet.add(TypeArgument.create(actualTypeArgument, i));
            }
        } else {
            doResolveActualTypeArgumentsSetHierarchically(typeArgumentsSet, parameterizedType, baseClass, baseTypeParameters, typeFilters);
        }

        // Replace TypeVariable to actual type
        TypeVariable<Class>[] typeParameters = currentClass.getTypeParameters();
        for (int i = 0; i < typeParameters.length; i++) {
            TypeVariable<Class> typeParameter = typeParameters[i];
            Integer index = findTypeArgumentIndex(typeArgumentsSet, typeParameter);
            if (index != null) {
                Type typeArgument = actualTypeArguments[i];
                typeArgumentsSet.add(TypeArgument.create(typeArgument, index));
            }
        }

    }

    private static Integer findTypeArgumentIndex(Set<TypeArgument> typeArgumentsSet, TypeVariable<Class> typeParameter) {
        Integer index = null;
        for (TypeArgument typeArgument : typeArgumentsSet) {
            Type type = typeArgument.getType();
            if (Objects.equals(type, typeParameter)) {
                index = typeArgument.getIndex();
            }
        }
        return index;
    }

    protected static void doResolveActualTypeArgumentsSetHierarchically(Set<TypeArgument> typeArgumentsSet,
                                                                        Type type,
                                                                        Class baseClass,
                                                                        TypeVariable<Class>[] baseTypeParameters,
                                                                        Predicate<Type>... typeFilters) {
        List<Type> hierarchicalTypes = doFindHierarchicalTypes(type, typeFilters);
        for (Type hierarchicalType : hierarchicalTypes) {
            doResolveActualTypeArgumentsSet(typeArgumentsSet, hierarchicalType, baseClass, baseTypeParameters, typeFilters);
        }
    }

    /**
     * Get the specified types' generic types(including super classes and interfaces) that are assignable from {@link ParameterizedType} interface
     *
     * @param type        the specified type
     * @param typeFilters one or more {@link Predicate}s to filter the {@link ParameterizedType} instance
     * @return non-null read-only {@link List}
     */
    public static List<ParameterizedType> getGenericTypes(Type type, Predicate<ParameterizedType>... typeFilters) {

        Class<?> rawClass = getRawClass(type);

        if (rawClass == null) {
            return emptyList();
        }

        List<Type> genericTypes = new LinkedList<>();

        genericTypes.add(rawClass.getGenericSuperclass());
        genericTypes.addAll(asList(rawClass.getGenericInterfaces()));

        return unmodifiableList(filterList(genericTypes, TypeUtils::isParameterizedType).stream().map(ParameterizedType.class::cast).filter(and(typeFilters)).collect(toList()));
    }

    public static List<Type> findAllTypes(Type type, Predicate<Type>... typeFilters) {
        List<Type> allGenericTypes = newLinkedList();
        Predicate filter = and(typeFilters);
        if (filter.test(type)) {
            // add self
            allGenericTypes.add(type);
        }
        // Add all hierarchical types in declaration order
        addAllHierarchicalTypes(allGenericTypes, type, filter);
        return unmodifiableList(allGenericTypes);

    }

    public static List<Type> findHierarchicalTypes(Type type) {
        return findHierarchicalTypes(type, of());
    }

    public static List<Type> findHierarchicalTypes(Type type, Predicate<Type>... typeFilters) {
        return unmodifiableList(doFindHierarchicalTypes(type, typeFilters));
    }

    protected static List<Type> doFindHierarchicalTypes(Type type, Predicate<Type>... typeFilters) {
        if (isObjectType(type)) {
            return emptyList();
        }

        Class<?> klass = asClass(type);
        if (klass == null) {
            return emptyList();
        }

        LinkedList<Type> types = newLinkedList();

        Predicate<? super Type> filter = and(typeFilters);

        Type superType = klass.getGenericSuperclass();

        if (superType != null && filter.test(superType)) { // interface type will return null
            types.add(superType);
        }

        Type[] interfaceTypes = klass.getGenericInterfaces();
        for (Type interfaceType : interfaceTypes) {
            if (filter.test(interfaceType)) {
                types.add(interfaceType);
            }
        }
        return types;
    }


    public static List<Type> findAllHierarchicalTypes(Type type) {
        return findAllHierarchicalTypes(type, of());
    }

    public static List<Type> findAllHierarchicalTypes(Type type, Predicate<Type>... typeFilters) {
        return unmodifiableList(doFindAllHierarchicalTypes(type, typeFilters));
    }

    protected static LinkedList<Type> doFindAllHierarchicalTypes(Type type, Predicate<Type>... typeFilters) {
        LinkedList<Type> allTypes = newLinkedList();
        addAllHierarchicalTypes(allTypes, type, typeFilters);
        return allTypes;
    }

    protected static void addAllHierarchicalTypes(List<Type> allTypes, Type type, Predicate<Type>... typeFilters) {

        List<Type> currentTypes = doFindHierarchicalTypes(type, typeFilters);

        allTypes.addAll(currentTypes);

        for (Type currentType : currentTypes) {
            addAllHierarchicalTypes(allTypes, currentType, typeFilters);
        }
    }

    /**
     * Get all generic types(including super classes and interfaces) that are assignable from {@link ParameterizedType} interface
     *
     * @param type        the specified type
     * @param typeFilters one or more {@link Predicate}s to filter the {@link ParameterizedType} instance
     * @return non-null read-only {@link List}
     */
    public static List<ParameterizedType> getAllGenericTypes(Type type, Predicate<ParameterizedType>... typeFilters) {
        List<ParameterizedType> allGenericTypes = new LinkedList<>();
        // Add generic super classes
        allGenericTypes.addAll(getAllGenericSuperClasses(type, typeFilters));
        // Add generic super interfaces
        allGenericTypes.addAll(getAllGenericInterfaces(type, typeFilters));
        // wrap unmodifiable object
        return unmodifiableList(allGenericTypes);
    }

    /**
     * Get all generic super classes that are assignable from {@link ParameterizedType} interface
     *
     * @param type        the specified type
     * @param typeFilters one or more {@link Predicate}s to filter the {@link ParameterizedType} instance
     * @return non-null read-only {@link List}
     */
    public static List<ParameterizedType> getAllGenericSuperClasses(Type type, Predicate<ParameterizedType>... typeFilters) {

        Class<?> rawClass = getRawClass(type);

        if (rawClass == null || rawClass.isInterface()) {
            return emptyList();
        }

        List<Class<?>> allTypes = new LinkedList<>();
        // Add current class
        allTypes.add(rawClass);
        // Add all super classes
        allTypes.addAll(getAllSuperClasses(rawClass, NON_OBJECT_CLASS_FILTER));

        List<ParameterizedType> allGenericSuperClasses = allTypes.stream().map(Class::getGenericSuperclass).filter(TypeUtils::isParameterizedType).map(ParameterizedType.class::cast).collect(Collectors.toList());

        return unmodifiableList(filterAll(allGenericSuperClasses, typeFilters));
    }

    /**
     * Get all generic interfaces that are assignable from {@link ParameterizedType} interface
     *
     * @param type        the specified type
     * @param typeFilters one or more {@link Predicate}s to filter the {@link ParameterizedType} instance
     * @return non-null read-only {@link List}
     */
    public static List<ParameterizedType> getAllGenericInterfaces(Type type, Predicate<ParameterizedType>... typeFilters) {

        Class<?> rawClass = getRawClass(type);

        if (rawClass == null) {
            return emptyList();
        }

        List<Class<?>> allTypes = new LinkedList<>();
        // Add current class
        allTypes.add(rawClass);
        // Add all super classes
        allTypes.addAll(getAllSuperClasses(rawClass, NON_OBJECT_CLASS_FILTER));
        // Add all super interfaces
        allTypes.addAll(ClassUtils.getAllInterfaces(rawClass));

        List<ParameterizedType> allGenericInterfaces = allTypes.stream().map(Class::getGenericInterfaces).map(Arrays::asList).flatMap(Collection::stream).map(TypeUtils::asParameterizedType).filter(Objects::nonNull).collect(toList());

        return unmodifiableList(filterAll(allGenericInterfaces, typeFilters));
    }

    public static String getClassName(Type type) {
        return getRawType(type).getTypeName();
    }

    public static Set<String> getClassNames(Iterable<? extends Type> types) {
        return stream(types.spliterator(), false).map(TypeUtils::getClassName).collect(toSet());
    }

    public static List<Class<?>> resolveTypeArguments(Class<?> targetClass) {
        List<Class<?>> typeArguments = emptyList();
        while (targetClass != null) {
            typeArguments = resolveTypeArgumentsFromInterfaces(targetClass);
            if (!typeArguments.isEmpty()) {
                break;
            }

            Type superType = targetClass.getGenericSuperclass();
            if (superType instanceof ParameterizedType) {
                typeArguments = resolveTypeArgumentsFromType(superType);
            }

            if (!typeArguments.isEmpty()) {
                break;
            }
            // recursively
            targetClass = targetClass.getSuperclass();
        }

        return typeArguments;
    }

    public static List<Class<?>> resolveTypeArgumentsFromInterfaces(Class<?> type) {
        List<Class<?>> typeArguments = emptyList();
        for (Type superInterface : type.getGenericInterfaces()) {
            typeArguments = resolveTypeArgumentsFromType(superInterface);
            if (typeArguments != null && !typeArguments.isEmpty()) {
                break;
            }
        }
        return typeArguments;
    }

    public static List<Class<?>> resolveTypeArgumentsFromType(Type type) {
        List<Class<?>> typeArguments = emptyList();
        if (type instanceof ParameterizedType) {
            typeArguments = new LinkedList<>();
            ParameterizedType pType = (ParameterizedType) type;
            if (pType.getRawType() instanceof Class) {
                for (Type argument : pType.getActualTypeArguments()) {
                    Class<?> typeArgument = asClass(argument);
                    if (typeArgument != null) {
                        typeArguments.add(typeArgument);
                    }
                }
            }
        }
        return typeArguments;
    }

    public static Class<?> asClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return asClass(parameterizedType.getRawType());
        } else if (type instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable) type;
            return asClass(typeVariable.getBounds()[0]);
        }
        return null;
    }

    public static GenericArrayType asGenericArrayType(Type type) {
        if (type instanceof GenericArrayType) {
            return (GenericArrayType) type;
        }
        return null;
    }

    public static ParameterizedType asParameterizedType(Type type) {
        if (isParameterizedType(type)) {
            return (ParameterizedType) type;
        }
        return null;
    }

    public static TypeVariable asTypeVariable(Type type) {
        if (isTypeVariable(type)) {
            return (TypeVariable) type;
        }
        return null;
    }

    public static WildcardType asWildcardType(Type type) {
        if (isWildcardType(type)) {
            return (WildcardType) type;
        }
        return null;
    }

    public static Type getComponentType(Type type) {
        GenericArrayType genericArrayType = asGenericArrayType(type);
        if (genericArrayType != null) {
            return genericArrayType.getGenericComponentType();
        } else {
            Class klass = asClass(type);
            return klass != null ? klass.getComponentType() : null;
        }
    }

    /**
     * Get all super types from the specified type
     *
     * @param type        the specified type
     * @param typeFilters the filters for type
     * @return non-null read-only {@link Set}
     * @since 1.0.0
     */
    public static Set<Type> getAllSuperTypes(Type type, Predicate<Type>... typeFilters) {

        Class<?> rawClass = getRawClass(type);

        if (rawClass == null) {
            return emptySet();
        }

        if (rawClass.isInterface()) {
            return unmodifiableSet(filterAll(singleton(Object.class), typeFilters));
        }

        Set<Type> allSuperTypes = new LinkedHashSet<>();


        Type superType = rawClass.getGenericSuperclass();
        while (superType != null) {
            // add current super class
            allSuperTypes.add(superType);
            Class<?> superClass = getRawClass(superType);
            superType = superClass.getGenericSuperclass();
        }

        return filterAll(allSuperTypes, typeFilters);
    }

    /**
     * Get all super interfaces from the specified type
     *
     * @param type        the specified type
     * @param typeFilters the filters for type
     * @return non-null read-only {@link Set}
     * @since 1.0.0
     */
    public static Set<Type> getAllInterfaces(Type type, Predicate<Type>... typeFilters) {

        Class<?> rawClass = getRawClass(type);

        if (rawClass == null) {
            return emptySet();
        }

        Set<Type> allSuperInterfaces = new LinkedHashSet<>();

        Type[] interfaces = rawClass.getGenericInterfaces();

        // find direct interfaces recursively
        for (Type interfaceType : interfaces) {
            allSuperInterfaces.add(interfaceType);
            allSuperInterfaces.addAll(getAllInterfaces(interfaceType, typeFilters));
        }

        // find super types recursively
        for (Type superType : getAllSuperTypes(type, typeFilters)) {
            allSuperInterfaces.addAll(getAllInterfaces(superType));
        }

        return filterAll(allSuperInterfaces, typeFilters);
    }

    public static Set<Type> getAllTypes(Type type, Predicate<Type>... typeFilters) {

        Set<Type> allTypes = new LinkedHashSet<>();

        // add the specified type
        allTypes.add(type);
        // add all super types
        allTypes.addAll(getAllSuperTypes(type));
        // add all super interfaces
        allTypes.addAll(getAllInterfaces(type));

        return filterAll(allTypes, typeFilters);
    }

    public static Set<ParameterizedType> findParameterizedTypes(Class<?> sourceClass) {
        // Add Generic Interfaces
        List<Type> genericTypes = new LinkedList<>(asList(sourceClass.getGenericInterfaces()));
        // Add Generic Super Class
        genericTypes.add(sourceClass.getGenericSuperclass());

        Set<ParameterizedType> parameterizedTypes = genericTypes.stream().filter(type -> type instanceof ParameterizedType)// filter ParameterizedType
                .map(ParameterizedType.class::cast)  // cast to ParameterizedType
                .collect(Collectors.toSet());

        if (parameterizedTypes.isEmpty()) { // If not found, try to search super types recursively
            genericTypes.stream().filter(type -> type instanceof Class).map(Class.class::cast).forEach(superClass -> parameterizedTypes.addAll(findParameterizedTypes(superClass)));
        }

        return unmodifiableSet(parameterizedTypes);                     // build as a Set

    }
}
