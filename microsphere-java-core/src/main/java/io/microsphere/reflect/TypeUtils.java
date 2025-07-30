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

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.reflect.generics.TypeArgument;
import io.microsphere.util.ClassUtils;
import io.microsphere.util.Utils;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import static io.microsphere.annotation.ConfigurationProperty.SYSTEM_PROPERTIES_SOURCE;
import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.MapUtils.newConcurrentHashMap;
import static io.microsphere.collection.MapUtils.newLinkedHashMap;
import static io.microsphere.constants.PropertyConstants.MICROSPHERE_PROPERTY_NAME_PREFIX;
import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.lang.function.Streams.filterList;
import static io.microsphere.reflect.MultipleType.of;
import static io.microsphere.reflect.generics.TypeArgument.create;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.Assert.assertNoNullElements;
import static io.microsphere.util.TypeFinder.genericTypeFinder;
import static java.lang.Integer.getInteger;
import static java.lang.Integer.parseInt;
import static java.lang.Math.min;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;
import static java.util.stream.StreamSupport.stream;

/**
 * The utilities class for {@link Type}
 *
 * @since 1.0.0
 */
public abstract class TypeUtils implements Utils {

    public static final Predicate<? super Type> NON_OBJECT_TYPE_FILTER = t -> t != null && !isObjectType(t);

    public static final Predicate<? super Class<?>> NON_OBJECT_CLASS_FILTER = NON_OBJECT_TYPE_FILTER;

    public static final Predicate<? super Type> TYPE_VARIABLE_FILTER = TypeUtils::isTypeVariable;

    public static final Predicate<? super Type> PARAMETERIZED_TYPE_FILTER = TypeUtils::isParameterizedType;

    public static final Predicate<? super Type> WILDCARD_TYPE_FILTER = TypeUtils::isWildcardType;

    public static final Predicate<? super Type> GENERIC_ARRAY_TYPE_FILTER = TypeUtils::isGenericArrayType;

    /**
     * The property name for resolved generic types cache size : {@code "microsphere.reflect.resolved-generic-types.cache.size"}
     */
    public static final String RESOLVED_GENERIC_TYPES_CACHE_SIZE_PROPERTY_NAME = MICROSPHERE_PROPERTY_NAME_PREFIX + "reflect.resolved-generic-types.cache.size";

    /**
     * The default value of resolved generic types cache size : {@code "256"}
     */
    public static final String DEFAULT_RESOLVED_GENERIC_TYPES_CACHE_SIZE_PROPERTY_VALUE = "256";

    /**
     * The default size of resolved generic types cache
     */
    public static final int DEFAULT_RESOLVED_GENERIC_TYPES_CACHE_SIZE = parseInt(DEFAULT_RESOLVED_GENERIC_TYPES_CACHE_SIZE_PROPERTY_VALUE);

    /**
     * The size of resolved generic types cache
     */
    @ConfigurationProperty(
            name = RESOLVED_GENERIC_TYPES_CACHE_SIZE_PROPERTY_NAME,
            defaultValue = DEFAULT_RESOLVED_GENERIC_TYPES_CACHE_SIZE_PROPERTY_VALUE,
            description = "The size of resolved generic types cache",
            source = SYSTEM_PROPERTIES_SOURCE
    )
    public static final int RESOLVED_GENERIC_TYPES_CACHE_SIZE = getInteger(RESOLVED_GENERIC_TYPES_CACHE_SIZE_PROPERTY_NAME, DEFAULT_RESOLVED_GENERIC_TYPES_CACHE_SIZE);

    private static final ConcurrentMap<MultipleType, List<Type>> resolvedGenericTypesCache = newConcurrentHashMap(RESOLVED_GENERIC_TYPES_CACHE_SIZE);

    /**
     * Checks if the given object is an instance of {@link Class}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeUtils.isClass(String.class); // returns true
     * TypeUtils.isClass(Integer.TYPE); // returns true (for primitive types)
     * TypeUtils.isClass(new Object()); // returns false
     * TypeUtils.isClass(null); // returns false
     * }</pre>
     *
     * @param type the object to check
     * @return {@code true} if the object is a {@link Class}, {@code false} otherwise
     */
    public static boolean isClass(Object type) {
        return type instanceof Class;
    }

    /**
     * Checks if the given class is the {@link Object} class.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeUtils.isObjectClass(Object.class); // returns true
     * TypeUtils.isObjectClass(String.class); // returns false
     * TypeUtils.isObjectClass(null); // returns false
     * }</pre>
     *
     * @param klass the class to check
     * @return true if the class is exactly {@link Object}, false otherwise
     */
    public static boolean isObjectClass(Class<?> klass) {
        return isObjectType(klass);
    }

    /**
     * Checks if the given object is the {@link Object} type.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeUtils.isObjectType(Object.class); // returns true
     * TypeUtils.isObjectType(String.class); // returns false
     * TypeUtils.isObjectType(null); // returns false
     * }</pre>
     *
     * @param type the object to check
     * @return true if the object represents the exact {@link Object} class, false otherwise
     */
    public static boolean isObjectType(Object type) {
        return type == Object.class;
    }

    /**
     * Checks if the given object is an instance of {@link ParameterizedType}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeUtils.isParameterizedType(List.class); // returns false (raw type)
     * TypeUtils.isParameterizedType(ArrayList.class); // returns false (raw type)
     * TypeUtils.isParameterizedType(new ArrayList<String>().getClass().getGenericSuperclass()); // returns true
     * TypeUtils.isParameterizedType(null); // returns false
     * }</pre>
     *
     * @param type the object to check
     * @return true if the object is a {@link ParameterizedType}, false otherwise
     */
    public static boolean isParameterizedType(Object type) {
        return type instanceof ParameterizedType;
    }

    /**
     * Checks if the given object is an instance of {@link TypeVariable}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeVariable<?> typeVariable = List.class.getTypeParameters()[0]; // E
     * TypeUtils.isTypeVariable(typeVariable); // returns true
     *
     * TypeUtils.isTypeVariable(String.class); // returns false
     * TypeUtils.isTypeVariable(new Object()); // returns false
     * TypeUtils.isTypeVariable(null); // returns false
     * }</pre>
     *
     * @param type the object to check
     * @return true if the object is a {@link TypeVariable}, false otherwise
     */
    public static boolean isTypeVariable(Object type) {
        return type instanceof TypeVariable;
    }

    /**
     * Checks if the given object is an instance of {@link WildcardType}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeVariable<?> typeVariable = List.class.getTypeParameters()[0]; // E
     * WildcardType wildcardType = (WildcardType) typeVariable.getBounds()[0]; // ? extends Object
     * TypeUtils.isWildcardType(wildcardType); // returns true
     *
     * TypeUtils.isWildcardType(String.class); // returns false
     * TypeUtils.isWildcardType(new Object()); // returns false
     * TypeUtils.isWildcardType(null); // returns false
     * }</pre>
     *
     * @param type the object to check
     * @return true if the object is a {@link WildcardType}, false otherwise
     */
    public static boolean isWildcardType(Object type) {
        return type instanceof WildcardType;
    }

    /**
     * Checks if the given object is an instance of {@link GenericArrayType}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class Example {
     *    public <T> T[] valueOf(T... values) {
     *      return values;
     *    }
     * }
     *
     * Method method = Scratch.class.getMethod("valueOf", Object[].class);
     * TypeUtils.isGenericArrayType(method.getGenericReturnType()); // returns true
     * TypeUtils.isGenericArrayType(String[].class); // returns false (array class, not GenericArrayType)
     * TypeUtils.isGenericArrayType(null); // returns false
     * }</pre>
     *
     * @param type the object to check
     * @return true if the object is a {@link GenericArrayType}, false otherwise
     */
    public static boolean isGenericArrayType(Object type) {
        return type instanceof GenericArrayType;
    }

    /**
     * Checks if the given type is a concrete actual type, which means it is either a {@link Class} or a
     * {@link ParameterizedType}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class Example {
     *    public <T> T[] valueOf(T... values) {
     *      return values;
     *    }
     * }
     *
     * TypeUtils.isActualType(Example.class); // returns true (Class)
     *
     * ParameterizedType listType = (ParameterizedType) new ArrayList<String>().getClass().getGenericSuperclass();
     * TypeUtils.isActualType(listType); // returns true (ParameterizedType)
     *
     * TypeVariable<?> typeVar = List.class.getTypeParameters()[0];
     * TypeUtils.isActualType(typeVar); // returns false (TypeVariable)
     *
     * WildcardType wildcardType = (WildcardType) typeVar.getBounds()[0];
     * TypeUtils.isActualType(wildcardType); // returns false (WildcardType)
     *
     * GenericArrayType arrayType = (GenericArrayType) Example.class.getMethod("valueOf", Object[].class).getGenericReturnType();
     * TypeUtils.isActualType(arrayType); // returns false (GenericArrayType)
     *
     * TypeUtils.isActualType(null); // returns false (null value)
     * }</pre>
     *
     * @param type the type to check
     * @return true if the type is an instance of Class or ParameterizedType, false otherwise
     */
    public static boolean isActualType(Type type) {
        return isClass(type) || isParameterizedType(type);
    }

    /**
     * Gets the raw type of the specified {@link Type}, if it is a {@link ParameterizedType}.
     *
     * <p>If the given type is a parameterized type (e.g., {@code List<String>}), this method
     * returns its raw type (e.g., {@code List}). If the type is not a parameterized type,
     * it simply returns the original type.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeUtils.getRawType(List.class); // returns List.class
     *
     * ParameterizedType parameterizedType = (ParameterizedType) new ArrayList<String>().getClass().getGenericSuperclass();
     * TypeUtils.getRawType(parameterizedType); // returns List.class
     *
     * TypeUtils.getRawType(String.class); // returns String.class
     * TypeUtils.getRawType(null); // returns null
     * }</pre>
     *
     * @param type the type to get the raw type from
     * @return the raw type if the input is a {@link ParameterizedType}, otherwise returns the same type
     */
    @Nullable
    public static Type getRawType(Type type) {
        if (isParameterizedType(type)) {
            return ((ParameterizedType) type).getRawType();
        } else {
            return type;
        }
    }

    /**
     * Gets the raw class of the specified {@link Type}, if it is a {@link ParameterizedType} or a {@link Class}.
     *
     * <p>If the given type is a parameterized type (e.g., {@code List<String>}), this method
     * returns its raw class (e.g., {@code List.class}). If the type is a plain class (e.g., {@code String}),
     * it simply returns the same class. If the type does not represent a class, this method returns null.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeUtils.getRawClass(List.class); // returns List.class
     *
     * ParameterizedType parameterizedType = (ParameterizedType) new ArrayList<String>().getClass().getGenericSuperclass();
     * TypeUtils.getRawClass(parameterizedType); // returns List.class
     *
     * TypeUtils.getRawClass(String.class); // returns String.class
     * TypeUtils.getRawClass(null); // returns null
     * }</pre>
     *
     * @param type the type to get the raw class from
     * @return the raw class if the input represents a class or parameterized type, otherwise returns null
     */
    @Nullable
    public static Class<?> getRawClass(Type type) {
        Type rawType = getRawType(type);
        if (isClass(rawType)) {
            return (Class) rawType;
        }
        return null;
    }

    /**
     * Determines whether one type can be assigned from another type, similar to the semantics of
     * {@link Class#isAssignableFrom(Class)}. This method considers both raw types and parameterized types.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Type listType = new ArrayList<String>().getClass().getGenericSuperclass(); // ParameterizedType for List<String>
     * Type superType = List.class;
     *
     * boolean assignable1 = TypeUtils.isAssignableFrom(superType, listType); // returns true
     *
     * Type mapType = new HashMap<String, Integer>().getClass().getGenericSuperclass(); // ParameterizedType for AbstractMap<String, Integer>
     * Type superType2 = Map.class;
     *
     * boolean assignable2 = TypeUtils.isAssignableFrom(superType2, mapType); // returns true
     *
     * Type stringType = String.class;
     * Type integerType = Integer.class;
     *
     * boolean assignable3 = TypeUtils.isAssignableFrom(stringType, integerType); // returns false
     * }</pre>
     *
     * @param superType  the type to check as a supertype or base type
     * @param targetType the type to check as a subtype or implementation
     * @return true if the target type can be assigned to the super type, false otherwise
     */
    public static boolean isAssignableFrom(Type superType, Type targetType) {
        Class<?> superClass = asClass(superType);
        return isAssignableFrom(superClass, targetType);
    }

    /**
     * Checks whether the target type can be assigned to the given super class.
     *
     * <p>This method bridges between raw {@link Class} and more complex {@link Type} hierarchies,
     * delegating to a more specific implementation that handles type compatibility checks.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Class<?> superClass = List.class;
     * Type targetType = new ArrayList<String>().getClass().getGenericSuperclass(); // ParameterizedType for List<String>
     *
     * boolean isAssignable = TypeUtils.isAssignableFrom(superClass, targetType); // returns true
     *
     * // Primitive types:
     * boolean isIntAssignable = TypeUtils.isAssignableFrom(Number.class, Integer.TYPE); // returns true
     *
     * // Null handling:
     * boolean isNullAssignable = TypeUtils.isAssignableFrom(Object.class, null); // returns false
     * }</pre>
     *
     * @param superClass the class to check as a supertype or base type
     * @param targetType the type to check as a subtype or implementation
     * @return true if the target type can be assigned to the super class, false otherwise
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
     * Resolves the actual type arguments used in the specified {@code type} for a given base type (class or interface).
     *
     * <p>This method is useful when working with generic types and parameterized types, especially when trying to determine
     * the actual type parameters used in a class hierarchy or interface implementation.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class ExampleClass implements List<String> {
     *     // implementation details...
     * }
     *
     * Type type = ExampleClass.class.getGenericInterfaces()[0]; // ParameterizedType for List<String>
     * List<Type> args = TypeUtils.resolveActualTypeArguments(type, List.class);
     * System.out.println(args.get(0)); // prints: java.lang.String
     * }</pre>
     *
     * <p>In this example, we retrieve the actual type argument used for the {@link List} interface implemented by
     * the class.</p>
     *
     * @param type     the type from which to resolve the actual type arguments (e.g., a parameterized type)
     * @param baseType the base type (class or interface) whose type parameters are being resolved
     * @return an unmodifiable list containing the actual type arguments used in the given base type;
     * returns an empty list if no arguments can be resolved (e.g., raw type usage)
     * @throws IllegalArgumentException if either {@code type} or {@code baseType} is null or invalid
     */
    @Nonnull
    @Immutable
    public static List<Type> resolveActualTypeArguments(Type type, Type baseType) {
        return unmodifiableList(doResolveActualTypeArguments(type, baseType));
    }

    /**
     * Resolves the actual type argument at the specified index from the given type for a base type.
     *
     * <p>This method is useful when working with generic types and parameterized types, especially when trying to determine
     * the actual type parameters used in a class hierarchy or interface implementation.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class ExampleClass implements List<String> {
     *     // implementation details...
     * }
     *
     * Type type = ExampleClass.class.getGenericInterfaces()[0]; // ParameterizedType for List<String>
     * Type arg = TypeUtils.resolveActualTypeArgument(type, List.class, 0);
     * System.out.println(arg); // prints: java.lang.String
     * }</pre>
     *
     * @param type     the type from which to resolve the actual type argument (e.g., a parameterized type)
     * @param baseType the base type (class or interface) whose type parameters are being resolved
     * @param index    the index of the type argument to retrieve
     * @return the actual type argument at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @Nonnull
    public static Type resolveActualTypeArgument(Type type, Type baseType, int index) {
        return doResolveActualTypeArguments(type, baseType).get(index);
    }

    /**
     * Resolves the actual type argument classes used in the specified {@code type} for a given base type (class or interface).
     *
     * <p>This method resolves generic type information and returns the concrete {@link Class} representations of the type arguments.
     * If a type argument cannot be resolved to a class (e.g., it's a wildcard or type variable), it will be omitted from the result.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class ExampleClass implements Map<String, Integer> {
     *     // implementation details...
     * }
     *
     * Type type = ExampleClass.class.getGenericInterfaces()[0]; // ParameterizedType for Map<String, Integer>
     * List<Class> args = TypeUtils.resolveActualTypeArgumentClasses(type, Map.class);
     * System.out.println(args); // prints: [class java.lang.String, class java.lang.Integer]
     * }</pre>
     *
     * @param type     the type from which to resolve the actual type arguments (e.g., a parameterized type)
     * @param baseType the base type (class or interface) whose type parameters are being resolved
     * @return an unmodifiable list containing the actual type argument classes used in the given base type;
     * returns an empty list if no arguments can be resolved (e.g., raw type usage or unresolved type parameters)
     */
    @Nonnull
    @Immutable
    public static List<Class> resolveActualTypeArgumentClasses(Type type, Type baseType) {
        return unmodifiableList(doResolveActualTypeArgumentClasses(type, baseType));
    }

    /**
     * Resolves and returns the actual type argument class at the specified index from the given type for a base type.
     *
     * <p>This method resolves generic type information and returns the concrete {@link Class} representation
     * of the type argument at the specified index. If the type argument cannot be resolved to a class (e.g., it's
     * a wildcard or type variable), this method will return null.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class ExampleClass implements List<String> {
     *     // implementation details...
     * }
     *
     * Type type = ExampleClass.class.getGenericInterfaces()[0]; // ParameterizedType for List<String>
     * Class<?> argClass = TypeUtils.resolveActualTypeArgumentClass(type, List.class, 0);
     * System.out.println(argClass); // prints: class java.lang.String
     * }</pre>
     *
     * @param type     the type from which to resolve the actual type argument (e.g., a parameterized type)
     * @param baseType the base type (class or interface) whose type parameters are being resolved
     * @param index    the index of the type argument to retrieve
     * @return the actual type argument class at the specified index, or null if it cannot be resolved
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @Nullable
    public static Class resolveActualTypeArgumentClass(Type type, Class baseType, int index) {
        return asClass(resolveActualTypeArgument(type, baseType, index));
    }

    protected static List<Class> doResolveActualTypeArgumentClasses(Type type, Type baseType) {
        return doResolveActualTypeArguments(type, baseType).stream()
                .map(TypeUtils::asClass)
                .filter(Objects::nonNull)
                .collect(toList());
    }

    protected static List<Type> doResolveActualTypeArguments(Type type, Type baseType) {
        Class baseClass = asClass(baseType);
        return doResolveActualTypeArguments(type, baseClass);
    }

    /**
     * Resolves the actual type arguments used in the specified {@code type} for a given base class.
     *
     * <p>This method is useful when working with generic types and parameterized types, especially when trying to determine
     * the actual type parameters used in a class hierarchy or interface implementation.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class ExampleClass implements List<String> {
     *     // implementation details...
     * }
     *
     * Type type = ExampleClass.class.getGenericInterfaces()[0]; // ParameterizedType for List<String>
     * List<Type> args = TypeUtils.resolveActualTypeArguments(type, List.class);
     * System.out.println(args.get(0)); // prints: java.lang.String
     * }</pre>
     *
     * <p>In this example, we retrieve the actual type argument used for the {@link List} interface implemented by
     * the class.</p>
     *
     * @param type      the type from which to resolve the actual type arguments (e.g., a parameterized type)
     * @param baseClass the base class (class or interface) whose type parameters are being resolved
     * @return an unmodifiable list containing the actual type arguments used in the given base class;
     * returns an empty list if no arguments can be resolved (e.g., raw type usage)
     * @throws IllegalArgumentException if either {@code type} or {@code baseClass} is null or invalid
     */
    @Nonnull
    @Immutable
    public static List<Type> resolveActualTypeArguments(Type type, Class baseClass) {
        return unmodifiableList(doResolveActualTypeArguments(type, baseClass));
    }

    protected static List<Type> doResolveActualTypeArguments(Type type, Class baseClass) {
        if (type == null || baseClass == null) { // the raw class of type or baseType is null
            return emptyList();
        }
        return resolvedGenericTypesCache.computeIfAbsent(of(type, baseClass), mt -> {

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

    @Nonnull
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

        int length = min(actualTypeArgumentsLength, baseTypeArgumentsLength);

        int actualTypesCount = 0;

        for (int i = 0; i < length; i++) {
            Type actualTypeArgument = actualTypeArguments[i];
            if (isActualType(actualTypeArgument)) {
                actualTypesCount++;
                typeArguments[i] = create(actualTypeArgument, i);
            }
        }

        if (klass == baseClass) { // 'klass' is same as baseClass
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
     * Retrieves all generic superclasses of the given type, including its hierarchical superclasses.
     *
     * <p>This method is useful when analyzing generic type information for classes that extend other generic classes.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class ExampleClass extends HashMap<String, Integer> {}
     *
     * Type type = ExampleClass.class;
     * List<Type> superclasses = TypeUtils.getAllGenericSuperclasses(type);
     *
     * // The list will include:
     * // - ParameterizedType for java.util.HashMap<java.lang.String, java.lang.Integer>
     * // - Type for java.util.AbstractMap
     * // - Type for java.lang.Object
     * }</pre>
     *
     * @param type the type to retrieve the generic superclasses from
     * @return a non-null read-only list of generic superclasses
     */
    @Nonnull
    @Immutable
    public static List<Type> getAllGenericSuperclasses(Type type) {
        return findAllGenericSuperclasses(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves all generic interfaces implemented by the given type, including those inherited from its superclasses.
     *
     * <p>This method is useful when analyzing generic type information for classes that implement generic interfaces.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class ExampleClass implements List<String>, Map<String, Integer> {}
     *
     * Type type = ExampleClass.class;
     * List<Type> interfaces = TypeUtils.getAllGenericInterfaces(type);
     *
     * // The list will include:
     * // - ParameterizedType for java.util.List<java.lang.String>
     * // - ParameterizedType for java.util.Map<java.lang.String, java.lang.Integer>
     * }</pre>
     *
     * @param type the type to retrieve the generic interfaces from
     * @return a non-null read-only list of generic interfaces
     */
    @Nonnull
    @Immutable
    public static List<Type> getAllGenericInterfaces(Type type) {
        return findAllGenericInterfaces(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves the parameterized types directly associated with the specified type,
     * including the type itself if it is a {@link ParameterizedType}, and any interfaces or superclasses
     * that are parameterized.
     *
     * <p>This method does not include hierarchical types — only the immediate generic information
     * related to the given type is considered.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class ExampleClass implements List<String>, Map<String, Integer> {}
     *
     * Type type = ExampleClass.class;
     * List<ParameterizedType> result = TypeUtils.getParameterizedTypes(type);
     *
     * // The list will include:
     * // - ParameterizedType for java.util.List<java.lang.String>
     * // - ParameterizedType for java.util.Map<java.lang.String, java.lang.Integer>
     * }</pre>
     *
     * @param type the type to retrieve the parameterized types from
     * @return a non-null read-only list of parameterized types directly associated with the given type
     */
    @Nonnull
    @Immutable
    public static List<ParameterizedType> getParameterizedTypes(Type type) {
        return findParameterizedTypes(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves all parameterized types associated with the specified type, including those from its hierarchical superclasses and interfaces.
     *
     * <p>This method is useful when analyzing generic type information for a class and its entire inheritance hierarchy,
     * especially when dealing with complex generic structures.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class ExampleClass implements List<String>, Map<String, Integer> {}
     *
     * Type type = ExampleClass.class;
     * List<ParameterizedType> result = TypeUtils.getAllParameterizedTypes(type);
     *
     * // The list will include:
     * // - ParameterizedType for java.util.List<java.lang.String>
     * // - ParameterizedType for java.util.Map<java.lang.String, java.lang.Integer>
     * }</pre>
     *
     * @param type the type to retrieve all parameterized types from
     * @return a non-null read-only list of parameterized types from the given type and its hierarchy
     */
    @Nonnull
    @Immutable
    public static List<ParameterizedType> getAllParameterizedTypes(Type type) {
        return findAllParameterizedTypes(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves all hierarchical generic types associated with the given type, including its superclasses and interfaces.
     *
     * <p>This method collects all generic superclasses and implemented interfaces for the provided type,
     * traversing up through its hierarchy. It's particularly useful when dealing with complex generic structures.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class ExampleClass extends HashMap<String, Integer> implements List<String> {}
     *
     * Type type = ExampleClass.class;
     * List<Type> hierarchicalTypes = TypeUtils.getHierarchicalTypes(type);
     *
     * // The list may include:
     * // - ParameterizedType for java.util.HashMap<java.lang.String, java.lang.Integer>
     * // - Type for java.util.AbstractMap
     * // - ParameterizedType for java.util.List<java.lang.String>
     * // - Other related generic types from the class hierarchy
     * }</pre>
     *
     * @param type the type to retrieve hierarchical generic types from
     * @return a non-null read-only list containing all hierarchical generic types associated with the given type
     */
    @Nonnull
    @Immutable
    public static List<Type> getHierarchicalTypes(Type type) {
        return findHierarchicalTypes(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Gets all generic types associated with the given type, including:
     * - The type itself (if not a raw Object class)
     * - All generic superclasses
     * - All implemented interfaces (including those from superclasses)
     *
     * <p>This method is useful when analyzing complete generic type information for a class,
     * including its entire inheritance hierarchy.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class ExampleClass extends HashMap<String, Integer> implements List<String> {}
     *
     * Type type = ExampleClass.class;
     * List<Type> allTypes = TypeUtils.getAllTypes(type);
     *
     * // The list will include:
     * // - ExampleClass.class (raw type)
     * // - ParameterizedType for java.util.HashMap<java.lang.String, java.lang.Integer>
     * // - Type for java.util.AbstractMap
     * // - ParameterizedType for java.util.List<java.lang.String>
     * // - Other related generic types from the class hierarchy
     * }</pre>
     *
     * @param type the type to retrieve all associated generic types from
     * @return a non-null read-only list containing all associated generic types
     */
    @Nonnull
    @Immutable
    public static List<Type> getAllTypes(Type type) {
        return findAllTypes(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Retrieves all generic superclasses of the given type, including its hierarchical superclasses.
     *
     * <p>This method is useful when analyzing generic type information for classes that extend other generic classes.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class ExampleClass extends HashMap<String, Integer> {}
     *
     * Type type = ExampleClass.class;
     * List<Type> superclasses = TypeUtils.findAllGenericSuperclasses(type);
     *
     * // The list will include:
     * // - ParameterizedType for java.util.HashMap<java.lang.String, java.lang.Integer>
     * // - Type for java.util.AbstractMap
     * // - Type for java.lang.Object
     * }</pre>
     *
     * @param type        the type to retrieve the generic superclasses from
     * @param typeFilters the filters for type (optional)
     * @return a non-null read-only list of generic superclasses
     */
    @Nonnull
    @Immutable
    public static List<Type> findAllGenericSuperclasses(Type type, Predicate<? super Type>... typeFilters) {
        return findTypes(type, false, true, true, false, typeFilters);
    }

    /**
     * Retrieves all generic interfaces implemented by the given type, including those inherited from its superclasses.
     *
     * <p>This method is useful when analyzing generic type information for classes that implement generic interfaces.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class ExampleClass implements List<String>, Map<String, Integer> {}
     *
     * Type type = ExampleClass.class;
     * List<Type> interfaces = TypeUtils.findAllGenericInterfaces(type);
     *
     * // The list will include:
     * // - ParameterizedType for java.util.List<java.lang.String>
     * // - ParameterizedType for java.util.Map<java.lang.String, java.lang.Integer>
     * }</pre>
     *
     * @param type        the type to retrieve the generic interfaces from
     * @param typeFilters the filters for type (optional)
     * @return a non-null read-only list of generic interfaces
     */
    @Nonnull
    @Immutable
    public static List<Type> findAllGenericInterfaces(Type type, Predicate<? super Type>... typeFilters) {
        return findTypes(type, false, true, false, true, typeFilters);
    }

    /**
     * Finds the immediate parameterized types associated with the specified type,
     * including those from its direct superclass and implemented interfaces.
     *
     * <p>This method does not traverse up the class hierarchy — only the immediate generic information
     * directly related to the given type is considered.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class ExampleClass implements List<String>, Map<String, Integer> {}
     *
     * Type type = ExampleClass.class;
     * List<ParameterizedType> result = TypeUtils.findParameterizedTypes(type);
     *
     * // The list will include:
     * // - ParameterizedType for java.util.List<java.lang.String>
     * // - ParameterizedType for java.util.Map<java.lang.String, java.lang.Integer>
     * }</pre>
     *
     * @param type        the type to retrieve the parameterized types from
     * @param typeFilters one or more predicates to filter the parameterized types (optional)
     * @return a non-null read-only list of immediate parameterized types associated with the given type
     */
    @Nonnull
    @Immutable
    public static List<ParameterizedType> findParameterizedTypes(Type type, Predicate<? super ParameterizedType>... typeFilters) {
        return findTypes(type, true, false, true, true, parameterizedTypePredicate(typeFilters));
    }

    /**
     * Retrieves all parameterized types associated with the specified type, including those from its hierarchical superclasses and interfaces.
     *
     * <p>This method is useful when analyzing generic type information for a class and its entire inheritance hierarchy,
     * especially when dealing with complex generic structures.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class ExampleClass extends HashMap<String, Integer> implements List<String> {}
     *
     * Type type = ExampleClass.class;
     * List<ParameterizedType> result = TypeUtils.findAllParameterizedTypes(type);
     *
     * // The list will include:
     * // - ParameterizedType for java.util.HashMap<java.lang.String, java.lang.Integer>
     * // - ParameterizedType for java.util.List<java.lang.String>
     * }</pre>
     *
     * @param type        the type to retrieve all parameterized types from
     * @param typeFilters one or more predicates to filter the parameterized types (optional)
     * @return a non-null read-only list of parameterized types from the given type and its hierarchy
     */
    @Nonnull
    @Immutable
    public static List<ParameterizedType> findAllParameterizedTypes(Type type, Predicate<? super ParameterizedType>... typeFilters) {
        return findAllTypes(type, parameterizedTypePredicate(typeFilters));
    }

    /**
     * Retrieves all hierarchical generic types associated with the given type, including its superclasses and interfaces.
     *
     * <p>This method collects all generic superclasses and implemented interfaces for the provided type,
     * traversing up through its hierarchy. It's particularly useful when dealing with complex generic structures.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class ExampleClass extends HashMap<String, Integer> implements List<String> {}
     *
     * Type type = ExampleClass.class;
     * List<Type> hierarchicalTypes = TypeUtils.findHierarchicalTypes(type);
     *
     * // The list may include:
     * // - ParameterizedType for java.util.HashMap<java.lang.String, java.lang.Integer>
     * // - Type for java.util.AbstractMap
     * // - ParameterizedType for java.util.List<java.lang.String>
     * // - Other related generic types from the class hierarchy
     * }</pre>
     *
     * @param type        the type to retrieve hierarchical generic types from
     * @param typeFilters one or more predicates to filter the types (optional)
     * @return a non-null read-only list containing all hierarchical generic types associated with the given type
     */
    @Nonnull
    @Immutable
    public static List<Type> findHierarchicalTypes(Type type, Predicate<? super Type>... typeFilters) {
        return findTypes(type, false, true, true, true, typeFilters);
    }

    /**
     * Retrieves all types associated with the given type, including:
     * - The type itself (if not a raw Object class)
     * - All generic superclasses
     * - All implemented interfaces (including those from superclasses)
     *
     * <p>This method is useful when analyzing complete generic type information for a class,
     * including its entire inheritance hierarchy.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class ExampleClass extends HashMap<String, Integer> implements List<String> {}
     *
     * Type type = ExampleClass.class;
     * List<Type> allTypes = TypeUtils.findAllTypes(type);
     *
     * // The list will include:
     * // - ExampleClass.class (raw type)
     * // - ParameterizedType for java.util.HashMap<java.lang.String, java.lang.Integer>
     * // - Type for java.util.AbstractMap
     * // - ParameterizedType for java.util.List<java.lang.String>
     * // - Other related generic types from the class hierarchy
     * }</pre>
     *
     * @param type        the type to retrieve all associated types from
     * @param typeFilters one or more predicates to filter the types (optional)
     * @return a non-null read-only list containing all associated types
     */
    @Nonnull
    @Immutable
    public static List<Type> findAllTypes(Type type, Predicate<? super Type>... typeFilters) {
        return findTypes(type, true, true, true, true, typeFilters);
    }

    protected static Predicate parameterizedTypePredicate(Predicate<? super ParameterizedType>... predicates) {
        Predicate predicate = and(predicates);
        return PARAMETERIZED_TYPE_FILTER.and(predicate);
    }

    @Nonnull
    @Immutable
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

    /**
     * Gets the fully qualified name of the class represented by the given {@link Type}.
     *
     * <p>If the type is a parameterized type, this method returns the name of its raw type.
     * If the type is an array, it will return the JVM-style array type descriptor (e.g., "[Ljava.lang.String;").</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeUtils.getClassName(String.class); // returns "java.lang.String"
     *
     * ParameterizedType listType = (ParameterizedType) new ArrayList<String>().getClass().getGenericSuperclass();
     * TypeUtils.getClassName(listType); // returns "java.util.List"
     *
     * TypeUtils.getClassName(Integer.TYPE); // returns "int"
     * TypeUtils.getClassName(int[].class); // returns "[I"
     * TypeUtils.getClassName(String[].class); // returns "[Ljava.lang.String;"
     *
     * TypeUtils.getClassName(null); // returns null
     * }</pre>
     *
     * @param type the type to get the class name from
     * @return the fully qualified name of the class, or null if the input is null
     */
    @Nullable
    public static String getClassName(Type type) {
        Type rawType = getRawType(type);
        if (rawType == null) {
            return null;
        }
        return rawType.getTypeName();
    }

    /**
     * Retrieves the fully qualified class names for all types in the provided iterable.
     *
     * <p>This method is useful when working with collections of generic types and parameterized types,
     * especially when trying to obtain readable class names from complex type information.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ParameterizedType listType = (ParameterizedType) new ArrayList<String>().getClass().getGenericSuperclass();
     * ParameterizedType mapType = (ParameterizedType) new HashMap<String, Integer>().getClass().getGenericSuperclass();
     *
     * Set<Type> types = new HashSet<>();
     * types.add(listType);
     * types.add(mapType);
     * types.add(String.class);
     *
     * Set<String> classNames = TypeUtils.getClassNames(types);
     * // Possible output: ["java.util.List", "java.util.Map", "java.lang.String"]
     * }</pre>
     *
     * @param types an iterable collection of types to extract class names from
     * @return a read-only set containing the fully qualified class names of all types in the input
     * @throws IllegalArgumentException if any element in the input is null
     */
    @Nonnull
    @Immutable
    public static Set<String> getClassNames(Iterable<? extends Type> types) {
        return unmodifiableSet(stream(types.spliterator(), false)
                .map(TypeUtils::getClassName)
                .collect(toSet()));
    }

    /**
     * Resolves the actual type arguments from the generic superclass and interfaces for the given target class.
     *
     * <p>This method traverses up the class hierarchy to collect all type arguments defined in generic superclasses
     * and implemented interfaces. It is particularly useful when working with classes that extend or implement
     * parameterized types.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class ExampleClass extends HashMap<String, Integer> implements List<String> {}
     *
     * List<Type> typeArguments = TypeUtils.resolveTypeArguments(ExampleClass.class);
     * // The list will contain:
     * // - java.lang.String (from HashMap's first type parameter)
     * // - java.lang.Integer (from HashMap's second type parameter)
     * // - java.lang.String (from List's type parameter)
     * }</pre>
     *
     * <p>In this example, we retrieve all type arguments used in both the generic superclass {@link HashMap} and
     * the implemented interface {@link List}.</p>
     *
     * @param targetClass the class to resolve type arguments from
     * @return an unmodifiable list containing all resolved type arguments; returns an empty list if no arguments can be resolved
     * @throws IllegalArgumentException if the targetClass is null or represents a primitive/array type
     */
    @Nonnull
    @Immutable
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

    /**
     * Resolves and returns the concrete class representations of type arguments used in the given target class.
     *
     * <p>This method traverses up the class hierarchy to collect all type arguments defined in generic superclasses
     * and implemented interfaces, then attempts to resolve them into actual {@link Class} objects. Type arguments that
     * cannot be resolved to a concrete class (e.g., wildcards or unresolved type variables) are omitted from the result.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class ExampleClass extends HashMap<String, Integer> implements List<String> {}
     *
     * List<Class<?>> typeArgumentClasses = TypeUtils.resolveTypeArgumentClasses(ExampleClass.class);
     * // The list will contain:
     * // - java.lang.String (from HashMap's first type parameter)
     * // - java.lang.Integer (from HashMap's second type parameter)
     * // - java.lang.String (from List's type parameter)
     * }</pre>
     *
     * @param targetClass the class to resolve type argument classes from
     * @return an unmodifiable list containing the resolved class representations of type arguments;
     * returns an empty list if no type arguments can be resolved
     * @throws IllegalArgumentException if the targetClass is null or represents a primitive/array type
     */
    @Nonnull
    @Immutable
    public static List<Class<?>> resolveTypeArgumentClasses(Class<?> targetClass) {
        List<Type> typeArguments = resolveTypeArguments(targetClass);
        return unmodifiableList(typeArguments.stream()
                .map(TypeUtils::asClass)
                .filter(Objects::nonNull)
                .collect(toList()));
    }

    /**
     * Converts the given {@link Type} to a {@link Class} if possible.
     *
     * <p>If the type is a {@link ParameterizedType}, this method attempts to convert its raw type.
     * Returns null if the type cannot be converted to a class.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Convert a simple class type
     * Class<?> stringClass = TypeUtils.asClass(String.class);
     * System.out.println(stringClass); // Output: class java.lang.String
     *
     * // Convert a parameterized type's raw type
     * List<String> list = new ArrayList<>();
     * ParameterizedType parameterizedType = (ParameterizedType) list.getClass().getGenericSuperclass();
     * Class<?> listClass = TypeUtils.asClass(parameterizedType);
     * System.out.println(listClass); // Output: interface java.util.List
     *
     * // Attempting to convert a wildcard type returns null
     * WildcardType wildcardType = ((WildcardType) ((TypeVariable<?>) List.class.getTypeParameters()[0]).getBounds()[0]);
     * Class<?> wildcardClass = TypeUtils.asClass(wildcardType);
     * System.out.println(wildcardClass); // Output: null
     *
     * // Null handling
     * Class<?> nullClass = TypeUtils.asClass(null);
     * System.out.println(nullClass); // Output: null
     * }</pre>
     *
     * @param type the type to convert, may be null or any valid {@link Type}
     * @return the corresponding {@link Class} if conversion is possible, otherwise null
     */
    @Nullable
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

    /**
     * Converts the given {@link Type} to a {@link GenericArrayType} if possible.
     *
     * <p>If the type is a {@link GenericArrayType}, it is returned directly.
     * Returns null if the type cannot be converted to a generic array type.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class Example {
     *    public <T> T[] valueOf(T... values) {
     *      return values;
     *    }
     * }
     *
     * Method method = Example.class.getMethod("valueOf", Object[].class);
     * Type genericReturnType = method.getGenericReturnType(); // This will be a GenericArrayType
     *
     * GenericArrayType arrayType = TypeUtils.asGenericArrayType(genericReturnType);
     * System.out.println(arrayType != null); // Output: true
     *
     * // Trying to convert a raw array class
     * GenericArrayType rawArrayType = TypeUtils.asGenericArrayType(String[].class);
     * System.out.println(rawArrayType == null); // Output: true (String[].class is not a GenericArrayType)
     *
     * // Null handling
     * GenericArrayType nullArrayType = TypeUtils.asGenericArrayType(null);
     * System.out.println(nullArrayType == null); // Output: true
     * }</pre>
     *
     * @param type the type to convert, may be null or any valid {@link Type}
     * @return the corresponding {@link GenericArrayType} if conversion is possible, otherwise null
     */
    @Nullable
    public static GenericArrayType asGenericArrayType(Type type) {
        if (type instanceof GenericArrayType) {
            return (GenericArrayType) type;
        }
        return null;
    }

    /**
     * Converts the given {@link Type} to a {@link ParameterizedType} if possible.
     *
     * <p>If the type is a parameterized type, it is returned directly.
     * Returns null if the type cannot be converted to a parameterized type.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<String> list = new ArrayList<>();
     * ParameterizedType parameterizedType = (ParameterizedType) list.getClass().getGenericSuperclass();
     *
     * // Convert a parameterized type
     * ParameterizedType result = TypeUtils.asParameterizedType(parameterizedType);
     * System.out.println(result != null); // Output: true
     *
     * // Trying to convert a raw class
     * ParameterizedType fromRawClass = TypeUtils.asParameterizedType(List.class);
     * System.out.println(fromRawClass == null); // Output: true
     *
     * // Trying to convert a wildcard type
     * WildcardType wildcardType = ((WildcardType) ((TypeVariable<?>) List.class.getTypeParameters()[0]).getBounds()[0]);
     * ParameterizedType fromWildcard = TypeUtils.asParameterizedType(wildcardType);
     * System.out.println(fromWildcard == null); // Output: true
     *
     * // Null handling
     * ParameterizedType nullResult = TypeUtils.asParameterizedType(null);
     * System.out.println(nullResult == null); // Output: true
     * }</pre>
     *
     * @param type the type to convert, may be null or any valid {@link Type}
     * @return the corresponding {@link ParameterizedType} if conversion is possible, otherwise null
     */
    @Nullable
    public static ParameterizedType asParameterizedType(Type type) {
        if (isParameterizedType(type)) {
            return (ParameterizedType) type;
        }
        return null;
    }

    /**
     * Converts the given {@link Type} to a {@link TypeVariable} if possible.
     *
     * <p>If the type is a {@link TypeVariable}, it is returned directly.
     * Returns null if the type cannot be converted to a type variable.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Get a TypeVariable from a generic class
     * TypeVariable<?> typeVariable = List.class.getTypeParameters()[0]; // E
     * TypeVariable result = TypeUtils.asTypeVariable(typeVariable);
     * System.out.println(result != null); // Output: true
     *
     * // Trying to convert a raw class returns null
     * TypeVariable fromRawClass = TypeUtils.asTypeVariable(String.class);
     * System.out.println(fromRawClass == null); // Output: true
     *
     * // Trying to convert a ParameterizedType returns null
     * List<String> list = new ArrayList<>();
     * ParameterizedType parameterizedType = (ParameterizedType) list.getClass().getGenericSuperclass();
     * TypeVariable fromParameterized = TypeUtils.asTypeVariable(parameterizedType);
     * System.out.println(fromParameterized == null); // Output: true
     *
     * // Null handling
     * TypeVariable nullResult = TypeUtils.asTypeVariable(null);
     * System.out.println(nullResult == null); // Output: true
     * }</pre>
     *
     * @param type the type to convert, may be null or any valid {@link Type}
     * @return the corresponding {@link TypeVariable} if conversion is possible, otherwise null
     */
    @Nullable
    public static TypeVariable asTypeVariable(Type type) {
        if (isTypeVariable(type)) {
            return (TypeVariable) type;
        }
        return null;
    }

    /**
     * Converts the given {@link Type} to a {@link WildcardType} if possible.
     *
     * <p>If the type is a {@link WildcardType}, it is returned directly.
     * Returns null if the type cannot be converted to a wildcard type.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Get a WildcardType from a TypeVariable's bound
     * TypeVariable<?> typeVariable = List.class.getTypeParameters()[0]; // E
     * WildcardType wildcardType = (WildcardType) typeVariable.getBounds()[0]; // ? extends Object
     * WildcardType result = TypeUtils.asWildcardType(wildcardType);
     * System.out.println(result != null); // Output: true
     *
     * // Trying to convert a raw class returns null
     * WildcardType fromRawClass = TypeUtils.asWildcardType(String.class);
     * System.out.println(fromRawClass == null); // Output: true
     *
     * // Trying to convert a ParameterizedType returns null
     * List<String> list = new ArrayList<>();
     * ParameterizedType parameterizedType = (ParameterizedType) list.getClass().getGenericSuperclass();
     * WildcardType fromParameterized = TypeUtils.asWildcardType(parameterizedType);
     * System.out.println(fromParameterized == null); // Output: true
     *
     * // Null handling
     * WildcardType nullResult = TypeUtils.asWildcardType(null);
     * System.out.println(nullResult == null); // Output: true
     * }</pre>
     *
     * @param type the type to convert, may be null or any valid {@link Type}
     * @return the corresponding {@link WildcardType} if conversion is possible, otherwise null
     */
    @Nullable
    public static WildcardType asWildcardType(Type type) {
        if (isWildcardType(type)) {
            return (WildcardType) type;
        }
        return null;
    }

    /**
     * Gets the component type of the specified type, if it represents an array type.
     *
     * <p>This method handles both generic array types ({@link GenericArrayType}) and raw array classes
     * ({@link Class} with array type). For a generic array type, it returns the component type from
     * the {@link GenericArrayType}. For a raw array class, it returns the result of
     * {@link Class#getComponentType()}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // With a generic array type
     * public <T> T[] createArray() { return null; }
     *
     * Method method = ExampleClass.class.getMethod("createArray");
     * Type genericReturnType = method.getGenericReturnType(); // This will be a GenericArrayType
     *
     * Type componentType = TypeUtils.getComponentType(genericReturnType);
     * System.out.println(componentType); // Output: T (the component type of T[])
     *
     * // With a raw array class
     * componentType = TypeUtils.getComponentType(String[].class);
     * System.out.println(componentType); // Output: class java.lang.String
     *
     * // With a non-array type
     * componentType = TypeUtils.getComponentType(Integer.class);
     * System.out.println(componentType); // Output: null
     *
     * // Null handling
     * componentType = TypeUtils.getComponentType(null);
     * System.out.println(componentType); // Output: null
     * }</pre>
     *
     * @param type the type to get the component type from, may be null
     * @return the component type if the input is an array type, otherwise null
     */
    @Nullable
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
     * Gets the fully qualified type name of the given {@link Type}.
     *
     * <p>This method handles various types including raw classes, parameterized types,
     * generic arrays, and primitive types. Returns null if the input is null.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * TypeUtils.getTypeName(String.class); // returns "java.lang.String"
     *
     * ParameterizedType listType = (ParameterizedType) new ArrayList<String>().getClass().getGenericSuperclass();
     * TypeUtils.getTypeName(listType); // returns "java.util.List<java.lang.String>"
     *
     * TypeUtils.getTypeName(Integer.TYPE); // returns "int"
     * TypeUtils.getTypeName(int[].class); // returns "[I"
     * TypeUtils.getTypeName(String[].class); // returns "[Ljava.lang.String;"
     *
     * TypeUtils.getTypeName(null); // returns null
     * }</pre>
     *
     * @param type the type to get the fully qualified name from
     * @return the fully qualified type name as a string, or null if the input is null
     */
    @Nullable
    public static String getTypeName(@Nullable Type type) {
        return type == null ? null : type.getTypeName();
    }

    /**
     * Gets the fully qualified type names for all types in the provided array.
     *
     * <p>This method handles various types including raw classes, parameterized types,
     * generic arrays, and primitive types. It is useful when working with collections
     * of generic types to obtain readable type names.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Type listType = new ArrayList<String>().getClass().getGenericSuperclass(); // ParameterizedType for List<String>
     * Type mapType = new HashMap<String, Integer>().getClass().getGenericSuperclass(); // ParameterizedType for AbstractMap<String, Integer>
     *
     * String[] typeNames = TypeUtils.getTypeNames(listType, mapType, String.class);
     * // Possible output: ["java.util.List<java.lang.String>", "java.util.AbstractMap<java.lang.String, java.lang.Integer>", "java.lang.String"]
     * }</pre>
     *
     * @param types the array of types to extract type names from
     * @return a non-null array containing the fully qualified type names of all types in the input
     * @throws IllegalArgumentException if any element in the input array is null
     */
    @Nonnull
    public static String[] getTypeNames(@Nullable Type... types) throws IllegalArgumentException {
        if (isEmpty(types)) {
            return EMPTY_STRING_ARRAY;
        }
        assertNoNullElements(types, "Any element of 'types' must not be null");
        return of(types).map(TypeUtils::getTypeName).toArray(String[]::new);
    }

    private TypeUtils() {
    }

}
