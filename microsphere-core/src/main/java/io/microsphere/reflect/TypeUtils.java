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

import io.microsphere.util.ClassUtils;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.lang.function.Streams.filterAll;
import static io.microsphere.lang.function.Streams.filterList;
import static io.microsphere.util.ClassUtils.getAllSuperClasses;
import static io.microsphere.util.ClassUtils.isAssignableFrom;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

/**
 * The utilities class for {@link Type}
 *
 * @since 1.0.0
 */
public abstract class TypeUtils {

    public static final Predicate<Class<?>> NON_OBJECT_TYPE_FILTER = t -> !Objects.equals(Object.class, t);

    public static final Predicate<Type> TYPE_VARIABLE_FILTER = type -> type instanceof TypeVariable;

    public static final Predicate<Type> PARAMETERIZED_TYPE_FILTER = TypeUtils::isParameterizedType;

    public static boolean isClass(Type type) {
        return type instanceof Class;
    }

    public static boolean isParameterizedType(Type type) {
        return type instanceof ParameterizedType;
    }

    public static boolean isTypeVariable(Type type) {
        return type instanceof TypeVariable;
    }

    public static boolean isGenericArrayType(Type type) {
        return type instanceof GenericArrayType;
    }

    public static boolean isWildcardType(Type type) {
        return type instanceof WildcardType;
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

    public static Type findActualTypeArgument(Type type, Class<?> interfaceClass, int index) {
        return findActualTypeArguments(type, interfaceClass).get(index);
    }

    public static List<Type> findActualTypeArguments(Type type, Class<?> interfaceClass) {
        List<Type> actualTypeArguments = new LinkedList<>();
        getAllGenericTypes(type, t -> isAssignableFrom(interfaceClass, getRawClass(t)))
                .forEach(parameterizedType -> {
                    Class<?> rawClass = getRawClass(parameterizedType);
                    Type[] typeArguments = parameterizedType.getActualTypeArguments();
                    actualTypeArguments.addAll(asList(typeArguments));
                    Class<?> superClass = rawClass.getSuperclass();
                    if (superClass != null) {
                        actualTypeArguments.addAll(findActualTypeArguments(superClass, interfaceClass));
                    }
                });

        return unmodifiableList(actualTypeArguments);
    }

    public static <T> Class<T> findActualTypeArgumentClass(Type type, Class<?> interfaceClass, int index) {
        return (Class<T>) findActualTypeArgumentClasses(type, interfaceClass).get(index);
    }

    public static List<Class<?>> findActualTypeArgumentClasses(Type type, Class<?> interfaceClass) {

        List<Type> actualTypeArguments = findActualTypeArguments(type, interfaceClass);

        List<Class<?>> actualTypeArgumentClasses = new LinkedList<>();

        for (Type actualTypeArgument : actualTypeArguments) {
            Class<?> rawClass = getRawClass(actualTypeArgument);
            if (rawClass != null) {
                actualTypeArgumentClasses.add(rawClass);
            }
        }

        return unmodifiableList(actualTypeArgumentClasses);
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
        allTypes.addAll(getAllSuperClasses(rawClass, NON_OBJECT_TYPE_FILTER));

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
        allTypes.addAll(getAllSuperClasses(rawClass, NON_OBJECT_TYPE_FILTER));
        // Add all super interfaces
        allTypes.addAll(ClassUtils.getAllInterfaces(rawClass));

        List<ParameterizedType> allGenericInterfaces = allTypes.stream().map(Class::getGenericInterfaces)
                .map(Arrays::asList)
                .flatMap(Collection::stream)
                .map(TypeUtils::asParameterizedType)
                .filter(Objects::nonNull)
                .collect(toList());

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
        } else if (type instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable) type;
            return asClass(typeVariable.getBounds()[0]);
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return asClass(parameterizedType.getRawType());
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

        Set<ParameterizedType> parameterizedTypes = genericTypes.stream()
                .filter(type -> type instanceof ParameterizedType)// filter ParameterizedType
                .map(ParameterizedType.class::cast)  // cast to ParameterizedType
                .collect(Collectors.toSet());

        if (parameterizedTypes.isEmpty()) { // If not found, try to search super types recursively
            genericTypes.stream()
                    .filter(type -> type instanceof Class)
                    .map(Class.class::cast)
                    .forEach(superClass -> parameterizedTypes.addAll(findParameterizedTypes(superClass)));
        }

        return unmodifiableSet(parameterizedTypes);                     // build as a Set

    }
}
