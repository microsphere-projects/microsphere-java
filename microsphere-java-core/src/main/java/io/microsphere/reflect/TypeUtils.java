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

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.reflect.generics.TypeArgument;
import io.microsphere.util.ClassUtils;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.MapUtils.newConcurrentHashMap;
import static io.microsphere.collection.MapUtils.newLinkedHashMap;
import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.lang.function.Streams.filterList;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.Assert.assertNoNullElements;
import static io.microsphere.util.TypeFinder.genericTypeFinder;
import static java.lang.Integer.getInteger;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;
import static java.util.stream.StreamSupport.stream;

/**
 * The utilities class for {@link Type}
 *
 * @since 1.0.0
 */
public abstract class TypeUtils {

    public static final Predicate<? super Type> NON_OBJECT_TYPE_FILTER = t -> t != null && !isObjectType(t);

    public static final Predicate<? super Class<?>> NON_OBJECT_CLASS_FILTER = NON_OBJECT_TYPE_FILTER;

    public static final Predicate<? super Type> TYPE_VARIABLE_FILTER = TypeUtils::isTypeVariable;

    public static final Predicate<? super Type> PARAMETERIZED_TYPE_FILTER = TypeUtils::isParameterizedType;

    public static final Predicate<? super Type> WILDCARD_TYPE_FILTER = TypeUtils::isWildcardType;

    public static final Predicate<? super Type> GENERIC_ARRAY_TYPE_FILTER = TypeUtils::isGenericArrayType;

    public static final String RESOLVED_GENERIC_TYPES_CACHE_SIZE_PROPERTY_NAME = "microsphere.reflect.resolved-generic-types.cache.size";

    private static final ConcurrentMap<MultipleType, List<Type>> resolvedGenericTypesCache = newConcurrentHashMap(getInteger(RESOLVED_GENERIC_TYPES_CACHE_SIZE_PROPERTY_NAME, 256));

    public static boolean isClass(Object type) {
        return type instanceof Class;
    }

    public static boolean isObjectClass(Class<?> klass) {
        return isObjectType(klass);
    }

    public static boolean isObjectType(Object type) {
        return type == Object.class;
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

    public static boolean isActualType(Type type) {
        return isClass(type) || isParameterizedType(type);
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
        return unmodifiableList(doResolveActualTypeArguments(type, baseType));
    }

    /**
     * Resolve the actual type parameters from the specified type based on some type
     *
     * @param type     the type to be resolved
     * @param baseType The base class or interface of <code>type</code>
     * @return the actual type parameters
     */
    public static Type resolveActualTypeArgument(Type type, Type baseType, int index) {
        return doResolveActualTypeArguments(type, baseType).get(index);
    }

    /**
     * Resolve the classes of actual type parameters from the specified type based on some type
     *
     * @param type     the type to be resolved
     * @param baseType The base class or interface of <code>type</code>
     * @return the read-only {@link List} classes of the actual type parameters
     */
    public static List<Class> resolveActualTypeArgumentClasses(Type type, Type baseType) {
        return unmodifiableList(doResolveActualTypeArgumentClasses(type, baseType));
    }

    /**
     * Resolve the class of actual type parameter from the specified type and index based on some type
     *
     * @param type     the type to be resolved
     * @param baseType The base class or interface of <code>type</code>
     * @param index    the index of actual type parameter
     * @return the read-only {@link List} classes of the actual type parameters
     */
    public static Class resolveActualTypeArgumentClass(Type type, Class baseType, int index) {
        return asClass(resolveActualTypeArgument(type, baseType, index));
    }

    protected static List<Class> doResolveActualTypeArgumentClasses(Type type, Type baseType) {
        return doResolveActualTypeArguments(type, baseType).stream().map(TypeUtils::asClass).filter(Objects::nonNull).collect(toList());
    }

    protected static List<Type> doResolveActualTypeArguments(Type type, Type baseType) {
        Class baseClass = asClass(baseType);
        return doResolveActualTypeArguments(type, baseClass);
    }

    public static List<Type> resolveActualTypeArguments(Type type, Class baseClass) {
        return unmodifiableList(doResolveActualTypeArguments(type, baseClass));
    }

    protected static List<Type> doResolveActualTypeArguments(Type type, Class baseClass) {
        if (type == null || baseClass == null) { // the raw class of type or baseType is null
            return emptyList();
        }
        return resolvedGenericTypesCache.computeIfAbsent(MultipleType.of(type, baseClass), mt -> {

            TypeVariable<Class>[] baseTypeParameters = baseClass.getTypeParameters();
            int baseTypeParametersLength = baseTypeParameters.length;
            if (baseTypeParametersLength == 0) { // No type-parameter in the base class
                return emptyList();
            }

            Class klass = asClass(type);

            boolean sameClass = baseClass.equals(klass);

            if (sameClass) { // Fast Path
                return doResolveActualTypeArgumentsInFastPath(type);
            } else if (!baseClass.isAssignableFrom(klass)) {
                // No hierarchical relationship between type and baseType
                return emptyList();
            }

            Predicate<? super Type> baseClassFilter = t -> isAssignableFrom(baseClass, t);

            List<Type> hierarchicalTypes = filterList(doGetHierarchicalTypes(type), baseClassFilter);

            int hierarchicalTypesSize = hierarchicalTypes.size();

            Map<Class, TypeArgument[]> typeArgumentsMap = resolveTypeArgumentsMap(type, hierarchicalTypes, hierarchicalTypesSize, baseClass, baseTypeParameters);

            Type[] actualTypeArguments = new Type[baseTypeParametersLength];

            int actualTypeArgumentsCount = 0;

            for (TypeArgument[] typeArguments : typeArgumentsMap.values()) {
                for (int i = 0; i < baseTypeParametersLength; i++) {
                    TypeArgument typeArgument = typeArguments[i];
                    if (typeArgument != null) {
                        Type actualTypeArgument = typeArgument.getType();
                        actualTypeArguments[i] = actualTypeArgument;
                        actualTypeArgumentsCount++;
                    }
                }
                if (actualTypeArgumentsCount == baseTypeParametersLength) {
                    break;
                }
            }

            return ofList(actualTypeArguments);
        });
    }

    static List<Type> doResolveActualTypeArgumentsInFastPath(Type type) {
        ParameterizedType pType = asParameterizedType(type);
        if (pType != null) {
            Type[] actualTypeArguments = pType.getActualTypeArguments();
            int actualTypeArgumentsLength = actualTypeArguments.length;
            List<Type> actualTypeArgumentsList = newArrayList(actualTypeArgumentsLength);
            for (int i = 0; i < actualTypeArgumentsLength; i++) {
                Type actualTypeArgument = actualTypeArguments[i];
                if (isActualType(actualTypeArgument)) {
                    actualTypeArgumentsList.add(actualTypeArgument);
                }
            }
            return actualTypeArgumentsList;
        }
        return emptyList();
    }

    private static Map<Class, TypeArgument[]> resolveTypeArgumentsMap(Type type, List<Type> hierarchicalTypes, int hierarchicalTypesSize, Class baseClass, TypeVariable<Class>[] baseTypeParameters) {

        int size = hierarchicalTypesSize + 1;

        Map<Class, TypeArgument[]> typeArgumentsMap = newLinkedHashMap(size);

        for (int i = hierarchicalTypesSize - 1; i > -1; i--) {
            Type hierarchicalType = hierarchicalTypes.get(i);
            resolveTypeArgumentsMap(hierarchicalType, hierarchicalTypes, i, hierarchicalTypesSize, typeArgumentsMap, baseClass, baseTypeParameters);
        }

        resolveTypeArgumentsMap(type, hierarchicalTypes, -1, hierarchicalTypesSize, typeArgumentsMap, baseClass, baseTypeParameters);


        return typeArgumentsMap;
    }

    private static void resolveTypeArgumentsMap(Type type, List<Type> hierarchicalTypes, int index, int hierarchicalTypesSize, Map<Class, TypeArgument[]> typeArgumentsMap, Class baseClass, TypeVariable<Class>[] baseTypeParameters) {
        ParameterizedType pType = asParameterizedType(type);
        if (pType != null) {
            resolveTypeArgumentsMap(pType, hierarchicalTypes, index, hierarchicalTypesSize, typeArgumentsMap, baseClass, baseTypeParameters);
        }
    }

    private static void resolveTypeArgumentsMap(ParameterizedType type, List<Type> hierarchicalTypes, int index, int hierarchicalTypesSize, Map<Class, TypeArgument[]> typeArgumentsMap, Class baseClass, TypeVariable<Class>[] baseTypeParameters) {
        Class klass = asClass(type);

        int baseTypeArgumentsLength = baseTypeParameters.length;
        TypeArgument[] typeArguments = newTypeArguments(klass, typeArgumentsMap, baseTypeArgumentsLength);

        Type[] actualTypeArguments = type.getActualTypeArguments();
        int actualTypeArgumentsLength = actualTypeArguments.length;

        int length = Math.min(actualTypeArgumentsLength, baseTypeArgumentsLength);

        int actualTypesCount = 0;

        for (int i = 0; i < length; i++) {
            Type actualTypeArgument = actualTypeArguments[i];
            if (isActualType(actualTypeArgument)) {
                actualTypesCount++;
                typeArguments[i] = TypeArgument.create(actualTypeArgument, i);
            }
        }

        if (klass.equals(baseClass)) { // 'klass' is same as baseClass
            return;
        }

        if (actualTypesCount < baseTypeArgumentsLength) { // To find actual types from the hierarchical types of 'type'
            TypeVariable<Class>[] typeParameters = klass.getTypeParameters();
            int typeParametersLength = typeParameters.length;
            for (int superTypeIndex = index + 1; superTypeIndex < hierarchicalTypesSize; superTypeIndex++) {
                Type superType = hierarchicalTypes.get(superTypeIndex);
                Class superClass = asClass(superType);
                TypeVariable<Class>[] superTypeParameters = superClass.getTypeParameters();
                int superTypeParametersLength = superTypeParameters.length;
                TypeArgument[] superTypeArguments = getTypeArguments(superClass, typeArgumentsMap);
                if (superTypeArguments != null) {
                    for (int typeParameterIndex = 0; typeParameterIndex < typeParametersLength; typeParameterIndex++) {
                        TypeVariable<Class> typeParameter = typeParameters[typeParameterIndex];
                        String typeParameterName = typeParameter.getName();
                        for (int superTypeParameterIndex = 0; superTypeParameterIndex < superTypeParametersLength; superTypeParameterIndex++) {
                            TypeVariable<Class> superTypeParameter = superTypeParameters[superTypeParameterIndex];
                            String superTypeParameterName = superTypeParameter.getName();
                            if (typeParameterName.equals(superTypeParameterName)) {
                                superTypeArguments[superTypeParameterIndex] = typeArguments[typeParameterIndex];
                            }
                        }
                    }
                }
            }
        }
    }

    private static TypeArgument[] newTypeArguments(Class klass, Map<Class, TypeArgument[]> typeArgumentsMap, int typeArgumentsLength) {
        return typeArgumentsMap.computeIfAbsent(klass, t -> new TypeArgument[typeArgumentsLength]);
    }

    private static TypeArgument[] getTypeArguments(Class klass, Map<Class, TypeArgument[]> typeArgumentsMap) {
        return typeArgumentsMap.get(klass);
    }

    /**
     * Get all generic super types from the specified type
     *
     * @param type the specified type
     * @return a non-null read-only {@link List} of {@link Type types}
     * @see Class#getGenericSuperclass()
     */
    public static List<Type> getAllGenericSuperclasses(Type type) {
        return findAllGenericSuperclasses(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all generic super interfaces from the specified type
     *
     * @param type the specified type
     * @return a non-null read-only {@link List} of {@link Type types}
     * @see Class#getGenericInterfaces()
     */
    public static List<Type> getAllGenericInterfaces(Type type) {
        return findAllGenericInterfaces(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get the {@link ParameterizedType parameterized types}(including self-type, super classes and interfaces)
     *
     * @param type the specified type
     * @return a non-null read-only {@link List} of types, whose type only is {@link ParameterizedType}
     */
    @Nonnull
    public static List<ParameterizedType> getParameterizedTypes(Type type) {
        return findParameterizedTypes(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all {@link ParameterizedType parameterized types}(including self-type, super classes and interfaces) hierarchically
     *
     * @param type the specified type
     * @return a non-null read-only {@link List} of types, whose type only is {@link ParameterizedType}
     */
    @Nonnull
    public static List<ParameterizedType> getAllParameterizedTypes(Type type) {
        return findAllParameterizedTypes(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all generic super types(including super classes and interfaces) hierarchically
     *
     * @param type
     * @return a non-null read-only {@link List} of {@link Type types}, which contains
     * {@link #getAllGenericSuperclasses(Type)} + {@link #getAllGenericInterfaces(Type)}
     */
    public static List<Type> getHierarchicalTypes(Type type) {
        return findHierarchicalTypes(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all generic types(including self-type, super classes and interfaces) hierarchically.
     *
     * @param type the specified type
     * @return a non-null read-only {@link List} of {@link Type types} , which contains {@code type} +
     * {@link #getAllGenericSuperclasses(Type)} + {@link #getAllGenericInterfaces(Type)}
     */
    @Nonnull
    public static List<Type> getAllTypes(Type type) {
        return findAllTypes(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Find all generic super types from the specified type, which are filtered by {@code typeFilters}
     *
     * @param type        the specified type
     * @param typeFilters the filters for type (optional)
     * @return non-null read-only {@link Set}
     * @see Class#getGenericSuperclass()
     */
    public static List<Type> findAllGenericSuperclasses(Type type, Predicate<? super Type>... typeFilters) {
        return findTypes(type, false, true, true, false, typeFilters);
    }

    /**
     * Find all super interfaces from the specified type
     *
     * @param type        the specified type
     * @param typeFilters the filters for type
     * @return non-null read-only {@link Set}
     * @see Class#getGenericInterfaces()
     */
    public static List<Type> findAllGenericInterfaces(Type type, Predicate<? super Type>... typeFilters) {
        return findTypes(type, false, true, false, true, typeFilters);
    }

    /**
     * Find the specified types' generic types(including super classes and interfaces) that are assignable from {@link ParameterizedType} interface
     *
     * @param type        the specified type
     * @param typeFilters one or more {@link Predicate}s to filter the {@link ParameterizedType} instance
     * @return non-null read-only {@link List}
     */
    public static List<ParameterizedType> findParameterizedTypes(Type type, Predicate<? super ParameterizedType>... typeFilters) {
        return findTypes(type, true, false, true, true, parameterizedTypePredicate(typeFilters));
    }

    /**
     * Find all specified types' generic types(including super classes and interfaces) hierarchically that are assignable from {@link ParameterizedType} interface
     *
     * @param type        the specified type
     * @param typeFilters one or more {@link Predicate}s to filter the {@link ParameterizedType} instance
     * @return non-null read-only {@link List}
     */
    public static List<ParameterizedType> findAllParameterizedTypes(Type type, Predicate<? super ParameterizedType>... typeFilters) {
        return findAllTypes(type, parameterizedTypePredicate(typeFilters));
    }

    public static List<Type> findHierarchicalTypes(Type type, Predicate<? super Type>... typeFilters) {
        return findTypes(type, false, true, true, true, typeFilters);
    }

    public static List<Type> findAllTypes(Type type, Predicate<? super Type>... typeFilters) {
        return findTypes(type, true, true, true, true, typeFilters);
    }

    protected static Predicate parameterizedTypePredicate(Predicate<? super ParameterizedType>... predicates) {
        Predicate predicate = and(predicates);
        return PARAMETERIZED_TYPE_FILTER.and(predicate);
    }

    protected static List<Type> findTypes(Type type, boolean includeSelf, boolean includeHierarchicalTypes,
                                          boolean includeGenericSuperclass, boolean includeGenericInterfaces,
                                          Predicate<? super Type>... typeFilters) {
        if (type == null || isObjectType(type)) {
            return emptyList();
        }
        return genericTypeFinder(type, includeSelf, includeHierarchicalTypes, includeGenericSuperclass, includeGenericInterfaces).findTypes(typeFilters);
    }

    protected static List<Type> doGetHierarchicalTypes(Type type) {
        return genericTypeFinder(type, false, true, true, true).findTypes(EMPTY_PREDICATE_ARRAY);
    }

    public static String getClassName(Type type) {
        return getRawType(type).getTypeName();
    }

    public static Set<String> getClassNames(Iterable<? extends Type> types) {
        return stream(types.spliterator(), false)
                .map(TypeUtils::getClassName)
                .collect(toSet());
    }

    public static List<Type> resolveTypeArguments(Class<?> targetClass) {
        if (targetClass == null || targetClass.isPrimitive() || targetClass.isArray()) {
            return emptyList();
        }
        List<Type> typeArguments = newLinkedList();
        while (targetClass != null && targetClass != Object.class) {
            typeArguments.addAll(resolveTypeArguments(targetClass.getGenericSuperclass()));
            typeArguments.addAll(resolveTypeArguments(targetClass.getGenericInterfaces()));
            targetClass = targetClass.getSuperclass();
        }
        return typeArguments.isEmpty() ? emptyList() : unmodifiableList(typeArguments);
    }

    protected static List<Type> resolveTypeArguments(Type... types) {
        int length = length(types);
        if (length < 1) {
            return emptyList();
        }
        List<Type> typeArguments = newLinkedList();
        for (int i = 0; i < length; i++) {
            typeArguments.addAll(getActualTypeArguments(types[i]));
        }
        return typeArguments;
    }

    protected static List<Type> getActualTypeArguments(Type type) {
        if (isObjectType(type)) {
            return emptyList();
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            return ofList(pType.getActualTypeArguments());
        }
        return emptyList();
    }

    public static List<Class<?>> resolveTypeArgumentClasses(Class<?> targetClass) {
        List<Type> typeArguments = resolveTypeArguments(targetClass);
        return unmodifiableList(typeArguments.stream()
                .map(TypeUtils::asClass)
                .filter(Objects::nonNull)
                .collect(toList()));
    }

    public static Class<?> asClass(Type type) {
        Class targetClass = asClass0(type);
        if (targetClass == null) { // try to cast a ParameterizedType if possible
            ParameterizedType parameterizedType = asParameterizedType(type);
            if (parameterizedType != null) {
                targetClass = asClass(parameterizedType.getRawType());
            }
        }
        return targetClass;
    }

    private static Class<?> asClass0(Type type) {
        return isClass(type) ? (Class<?>) type : null;
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
     * Get the type name safely
     *
     * @param type the {@link Type}
     * @return <code>null</code> if <code>type</code> is null
     */
    public static String getTypeName(@Nullable Type type) {
        return type == null ? null : type.getTypeName();
    }

    /**
     * Get the type names of the specified <code>types</code>
     *
     * @param types the {@link Type} array
     * @return non-null
     * @throws IllegalArgumentException if any element of <code>types</code> is <code>null</code>
     */
    @Nonnull
    public static String[] getTypeNames(@Nullable Type... types) throws IllegalArgumentException {
        if (isEmpty(types)) {
            return EMPTY_STRING_ARRAY;
        }
        assertNoNullElements(types, "Any element of 'types' must not be null");
        return of(types).map(TypeUtils::getTypeName).toArray(String[]::new);
    }

}
