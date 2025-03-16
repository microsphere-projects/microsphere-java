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

import io.microsphere.convert.Converter;
import io.microsphere.convert.StringToIntegerConverter;
import io.microsphere.convert.StringToStringConverter;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.SetUtils.newHashSet;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.TypeUtils.GENERIC_ARRAY_TYPE_FILTER;
import static io.microsphere.reflect.TypeUtils.NON_OBJECT_CLASS_FILTER;
import static io.microsphere.reflect.TypeUtils.NON_OBJECT_TYPE_FILTER;
import static io.microsphere.reflect.TypeUtils.PARAMETERIZED_TYPE_FILTER;
import static io.microsphere.reflect.TypeUtils.TYPE_VARIABLE_FILTER;
import static io.microsphere.reflect.TypeUtils.WILDCARD_TYPE_FILTER;
import static io.microsphere.reflect.TypeUtils.asClass;
import static io.microsphere.reflect.TypeUtils.asGenericArrayType;
import static io.microsphere.reflect.TypeUtils.asParameterizedType;
import static io.microsphere.reflect.TypeUtils.asTypeVariable;
import static io.microsphere.reflect.TypeUtils.asWildcardType;
import static io.microsphere.reflect.TypeUtils.doResolveActualTypeArguments;
import static io.microsphere.reflect.TypeUtils.doResolveActualTypeArgumentsInFastPath;
import static io.microsphere.reflect.TypeUtils.findAllGenericInterfaces;
import static io.microsphere.reflect.TypeUtils.findAllGenericSuperclasses;
import static io.microsphere.reflect.TypeUtils.findAllParameterizedTypes;
import static io.microsphere.reflect.TypeUtils.findAllTypes;
import static io.microsphere.reflect.TypeUtils.findHierarchicalTypes;
import static io.microsphere.reflect.TypeUtils.findParameterizedTypes;
import static io.microsphere.reflect.TypeUtils.getAllGenericInterfaces;
import static io.microsphere.reflect.TypeUtils.getAllGenericSuperclasses;
import static io.microsphere.reflect.TypeUtils.getAllParameterizedTypes;
import static io.microsphere.reflect.TypeUtils.getAllTypes;
import static io.microsphere.reflect.TypeUtils.getClassName;
import static io.microsphere.reflect.TypeUtils.getClassNames;
import static io.microsphere.reflect.TypeUtils.getComponentType;
import static io.microsphere.reflect.TypeUtils.getHierarchicalTypes;
import static io.microsphere.reflect.TypeUtils.getParameterizedTypes;
import static io.microsphere.reflect.TypeUtils.getRawClass;
import static io.microsphere.reflect.TypeUtils.getRawType;
import static io.microsphere.reflect.TypeUtils.isActualType;
import static io.microsphere.reflect.TypeUtils.isAssignableFrom;
import static io.microsphere.reflect.TypeUtils.isClass;
import static io.microsphere.reflect.TypeUtils.isGenericArrayType;
import static io.microsphere.reflect.TypeUtils.isObjectClass;
import static io.microsphere.reflect.TypeUtils.isObjectType;
import static io.microsphere.reflect.TypeUtils.isParameterizedType;
import static io.microsphere.reflect.TypeUtils.isTypeVariable;
import static io.microsphere.reflect.TypeUtils.isWildcardType;
import static io.microsphere.reflect.TypeUtils.resolveActualTypeArgument;
import static io.microsphere.reflect.TypeUtils.resolveActualTypeArgumentClass;
import static io.microsphere.reflect.TypeUtils.resolveActualTypeArgumentClasses;
import static io.microsphere.reflect.TypeUtils.resolveActualTypeArguments;
import static io.microsphere.reflect.TypeUtils.resolveTypeArgumentClasses;
import static io.microsphere.reflect.TypeUtils.resolveTypeArguments;
import static io.microsphere.reflect.generics.ParameterizedTypeImpl.of;
import static io.microsphere.util.ClassUtils.PRIMITIVE_TYPES;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TypeUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class TypeUtilsTest {

    @Test
    public void testConstantFilters() {
        assertFalse(NON_OBJECT_TYPE_FILTER.test(null));
        assertFalse(NON_OBJECT_CLASS_FILTER.test(null));

        assertFalse(NON_OBJECT_TYPE_FILTER.test(Object.class));
        assertFalse(NON_OBJECT_CLASS_FILTER.test(Object.class));

        assertTrue(NON_OBJECT_TYPE_FILTER.test(String.class));
        assertTrue(NON_OBJECT_CLASS_FILTER.test(String.class));

        assertFalse(PARAMETERIZED_TYPE_FILTER.test(null));
        assertFalse(PARAMETERIZED_TYPE_FILTER.test(Object.class));
        assertFalse(PARAMETERIZED_TYPE_FILTER.test(D.class));

        assertTrue(PARAMETERIZED_TYPE_FILTER.test(D.class.getGenericSuperclass()));

        assertFalse(TYPE_VARIABLE_FILTER.test(null));
        assertFalse(TYPE_VARIABLE_FILTER.test(null));
        assertFalse(TYPE_VARIABLE_FILTER.test(Object.class));
        assertFalse(TYPE_VARIABLE_FILTER.test(D.class));
        assertFalse(TYPE_VARIABLE_FILTER.test(D.class.getGenericSuperclass()));

        assertTrue(TYPE_VARIABLE_FILTER.test(Comparable.class.getTypeParameters()[0]));
    }

    @Test
    public void testIsClass() {
        assertTrue(isClass(String.class));
    }

    @Test
    public void testIsClassOnNull() {
        assertFalse(isClass(null));
    }

    @Test
    public void testIsClassOnObject() {
        assertFalse(isClass(1));
        assertFalse(isClass("test"));
    }

    @Test
    public void testIsObjectClass() {
        assertTrue(isObjectClass(Object.class));
    }

    @Test
    public void testIsObjectClassOnNull() {
        assertFalse(isObjectClass(null));
    }

    @Test
    public void testIsObjectClassOnNotObjectClass() {
        assertFalse(isObjectClass(Integer.class));
        assertFalse(isObjectClass(String.class));
    }


    @Test
    public void testIsObjectType() {
        assertTrue(isObjectType(Object.class));
    }

    @Test
    public void testIsObjectTypeOnNull() {
        assertFalse(isObjectType(null));
    }

    @Test
    public void testIsObjectTypeOnNotObjectClass() {
        assertFalse(isObjectType(Integer.class));
        assertFalse(isObjectType(String.class));
    }

    @Test
    public void testIsParameterizedType() {
        assertTrue(isParameterizedType(D.class.getGenericSuperclass()));
        assertTrue(isParameterizedType(of(C.class, String.class)));
    }

    @Test
    public void testIsParameterizedTypeOnNull() {
        assertFalse(isParameterizedType(null));
    }

    @Test
    public void testIsParameterizedTypeOnNotParameterizedType() {
        assertFalse(isParameterizedType(Object.class));
        assertFalse(isParameterizedType(String.class));
    }

    @Test
    public void testIsTypeVariable() {
        assertTrue(isTypeVariable(getTypeVariable()));
    }

    @Test
    public void testIsTypeVariableOnNull() {
        assertFalse(isTypeVariable(null));
    }

    @Test
    public void testIsTypeVariableOnNotTypeVariable() {
        assertFalse(isTypeVariable(Object.class));
        assertFalse(isTypeVariable(String.class));
    }

    @Test
    public void testIsWildcardType() {
        assertTrue(isWildcardType(getWildcardType()));
    }

    @Test
    public void testIsWildcardTypeOnNull() {
        assertFalse(isWildcardType(null));
    }

    @Test
    public void testIsWildcardTypeOnNotWildcardType() {
        assertFalse(isWildcardType(Object.class));
        assertFalse(isWildcardType(String.class));
    }

    @Test
    public void testIsGenericArrayType() {
        Type genericArrayType = getGenericArrayType();
        assertTrue(isGenericArrayType(genericArrayType));
    }

    @Test
    public void testIsGenericArrayTypeOnNull() {
        assertFalse(isGenericArrayType(null));
    }

    @Test
    public void testIsGenericArrayTypeOnNotGenericArrayType() {
        assertFalse(isGenericArrayType(Object.class));
        assertFalse(isGenericArrayType(String.class));
    }

    @Test
    public void testIsActualTypeOnClass() {
        assertTrue(isActualType(int.class));
        assertTrue(isActualType(String.class));
        assertTrue(isActualType(String[].class));
        assertTrue(isActualType(String[][].class));
        assertTrue(isActualType(String[][][].class));
        assertTrue(isActualType(String[][][][].class));
        assertTrue(isActualType(String[][][][][].class));
    }

    @Test
    public void testIsActualTypeOnParameterizedType() {
        assertTrue(isActualType(of(C.class, String.class)));
        assertTrue(isActualType(D.class.getGenericSuperclass()));
    }

    @Test
    public void testIsActualTypeOnNull() {
        assertFalse(isActualType(null));
    }

    @Test
    public void testIsActualTypeOnNotActualType() {
        assertFalse(isActualType(Comparable.class.getTypeParameters()[0]));
    }

    @Test
    public void testGetRawTypeOnParameterizedType() {
        assertEquals(C.class, getRawType(D.class.getGenericSuperclass()));
    }

    @Test
    public void testGetRawTypeOnNotParameterizedType() {
        assertEquals(int.class, getRawType(int.class));
        assertEquals(Object.class, getRawType(Object.class));
    }

    @Test
    public void testGetRawTypeOnNull() {
        assertNull(getRawType(null));
    }

    @Test
    public void testGetRawClass() {
        assertEquals(Object.class, getRawClass(Object.class));
        assertEquals(C.class, getRawClass(D.class.getGenericSuperclass()));
    }

    @Test
    public void testGetRawClassOnNull() {
        assertNull(getRawClass(null));
    }

    @Test
    public void testIsAssignableFromOnType() {
        assertTrue(isAssignableFrom(D.class.getGenericSuperclass(), D.class.getGenericSuperclass()));
    }

    @Test
    public void testIsAssignableFromOnClassAndParameterizedType() {
        assertTrue(isAssignableFrom(C.class, D.class.getGenericSuperclass()));
        assertTrue(isAssignableFrom(B.class, D.class.getGenericSuperclass()));
        assertTrue(isAssignableFrom(Comparable.class, B.class.getGenericInterfaces()[0]));
    }

    @Test
    public void testIsAssignableFromOnClass() {
        assertTrue(isAssignableFrom(Object.class, Object.class));
        assertTrue(isAssignableFrom(Object.class, String.class));
    }

    @Test
    public void testResolveActualTypeArguments() {
        List<Type> actualTypeArguments = resolveActualTypeArguments(B.class, Comparable.class);
        assertTypes(actualTypeArguments, B.class);

        actualTypeArguments = resolveActualTypeArguments(C.class, Comparable.class);
        assertTypes(actualTypeArguments, B.class);

        actualTypeArguments = resolveActualTypeArguments(D.class, C.class);
        assertTypes(actualTypeArguments, String.class);

        actualTypeArguments = resolveActualTypeArguments(E.class, Comparable.class);
        assertTypes(actualTypeArguments, B.class);

        actualTypeArguments = resolveActualTypeArguments(StringToStringConverter.class, Converter.class);
        assertTypes(actualTypeArguments, String.class, String.class);

        actualTypeArguments = resolveActualTypeArguments(StringIntegerBooleanHashMap.class, Map.class);
        assertTypes(actualTypeArguments, String.class, Integer.class);
    }

    @Test
    public void testResolveActualTypeArgument() {
        assertSame(String.class, resolveActualTypeArgument(D.class, C.class, 0));
        assertSame(B.class, resolveActualTypeArgument(D.class, Comparable.class, 0));
    }

    @Test
    public void testResolveActualTypeArgumentClasses() {
        List<Class> actualTypeArgumentClasses = resolveActualTypeArgumentClasses(D.class, C.class);
        assertEquals(1, actualTypeArgumentClasses.size());
        assertSame(String.class, actualTypeArgumentClasses.get(0));

        actualTypeArgumentClasses = resolveActualTypeArgumentClasses(D.class, B.class);
        assertTrue(actualTypeArgumentClasses.isEmpty());

        actualTypeArgumentClasses = resolveActualTypeArgumentClasses(D.class, Comparable.class);
        assertEquals(1, actualTypeArgumentClasses.size());
        assertSame(B.class, actualTypeArgumentClasses.get(0));
    }

    @Test
    public void testResolveActualTypeArgumentClass() {
        assertSame(String.class, resolveActualTypeArgumentClass(D.class, C.class, 0));
        assertSame(B.class, resolveActualTypeArgumentClass(D.class, Comparable.class, 0));
    }

    @Test
    public void testGetAllGenericSuperclasses() {
        List<Type> types = getAllGenericSuperclasses(A.class);
        assertEquals(1, types.size());
        assertAGenericSuperclasses(types);

        types = getAllGenericSuperclasses(B.class);
        assertEquals(2, types.size());
        assertBGenericSuperclasses(types);

        types = getAllGenericSuperclasses(C.class);
        assertEquals(3, types.size());
        assertCGenericSuperclasses(types);

        types = getAllGenericSuperclasses(D.class);
        assertEquals(4, types.size());
        assertDGenericSuperclasses(types);

        types = getAllGenericSuperclasses(E.class);
        assertEquals(4, types.size());
        assertEGenericSuperclasses(types);
    }


    @Test
    public void testGetAllGenericInterfaces() {
        List<Type> types = getAllGenericInterfaces(A.class);
        assertEquals(1, types.size());
        assertAGenericInterfaces(types);

        types = getAllGenericInterfaces(B.class);
        assertEquals(2, types.size());
        assertBGenericInterfaces(types);

        types = getAllGenericInterfaces(C.class);
        assertEquals(3, types.size());
        assertCGenericInterfaces(types);

        types = getAllGenericInterfaces(D.class);
        assertEquals(3, types.size());
        assertDGenericInterfaces(types);

        types = getAllGenericInterfaces(E.class);
        assertEquals(3, types.size());
        assertEGenericInterfaces(types);
    }

    @Test
    public void testGetParameterizedTypes() {
        List<ParameterizedType> genericTypes = getParameterizedTypes(A.class);
        assertSame(emptyList(), genericTypes);

        genericTypes = getParameterizedTypes(B.class);
        assertEquals(1, genericTypes.size());
        assertEquals(of(Comparable.class, B.class), genericTypes.get(0));

        genericTypes = getParameterizedTypes(C.class);
        assertSame(emptyList(), genericTypes);

        genericTypes = getParameterizedTypes(D.class);
        assertEquals(1, genericTypes.size());
        assertEquals(of(C.class, String.class), genericTypes.get(0));

        genericTypes = getParameterizedTypes(E.class);
        assertSame(emptyList(), genericTypes);
    }

    @Test
    public void testGetParameterizedTypesOnNull() {
        assertSame(emptyList(), getParameterizedTypes(null));
    }

    @Test
    public void testGetParameterizedTypesOnObjectClass() {
        assertSame(emptyList(), getParameterizedTypes(Object.class));
    }

    @Test
    public void testGetAllParameterizedTypes() {
        List<ParameterizedType> genericTypes = getAllParameterizedTypes(A.class);
        assertSame(emptyList(), genericTypes);

        genericTypes = getAllParameterizedTypes(B.class);
        assertEquals(1, genericTypes.size());
        assertEquals(of(Comparable.class, B.class), genericTypes.get(0));

        genericTypes = getAllParameterizedTypes(C.class);
        assertEquals(1, genericTypes.size());
        assertEquals(of(Comparable.class, B.class), genericTypes.get(0));

        genericTypes = getAllParameterizedTypes(D.class);
        assertEquals(2, genericTypes.size());
        assertEquals(of(C.class, String.class), genericTypes.get(0));
        assertEquals(of(Comparable.class, B.class), genericTypes.get(1));

        genericTypes = getAllParameterizedTypes(E.class);
        assertEquals(1, genericTypes.size());
        assertEquals(of(Comparable.class, B.class), genericTypes.get(0));
    }

    @Test
    public void testGetAllParameterizedTypesOnNull() {
        assertSame(emptyList(), getAllParameterizedTypes(null));
    }

    @Test
    public void testGetAllParameterizedTypesOnObjectClass() {
        assertSame(emptyList(), getAllParameterizedTypes(Object.class));
    }

    @Test
    public void testGetHierarchicalTypes() {
        List<Type> types = getHierarchicalTypes(A.class);
        assertTypes(types, Object.class, Serializable.class);

        types = getHierarchicalTypes(B.class);
        assertTypes(types, A.class, of(Comparable.class, B.class), Object.class, Serializable.class);

        types = getHierarchicalTypes(C.class);
        assertTypes(types, B.class, RandomAccess.class, A.class, of(Comparable.class, B.class), Object.class, Serializable.class);

        types = getHierarchicalTypes(D.class);
        assertTypes(types, of(C.class, String.class), B.class, RandomAccess.class, A.class, of(Comparable.class, B.class), Object.class, Serializable.class);

        types = getHierarchicalTypes(D.class);
        assertTypes(types, of(C.class, String.class), B.class, RandomAccess.class, A.class, of(Comparable.class, B.class), Object.class, Serializable.class);

        types = getHierarchicalTypes(E.class);
        assertTypes(types, C.class, Serializable.class, B.class, RandomAccess.class, A.class, of(Comparable.class, B.class), Object.class);
    }

    @Test
    public void testGetAllTypes() {
        List<Type> types = getAllTypes(E.class);
        assertEquals(8, types.size());
        assertTrue(types.contains(E.class));
        assertTrue(types.contains(C.class));
        assertTrue(types.contains(B.class));
        assertTrue(types.contains(A.class));
        assertTrue(types.contains(Object.class));
        assertTrue(types.contains(Serializable.class));
        assertFalse(types.contains(Comparable.class));
        assertTrue(types.contains(RandomAccess.class));
    }


    @Test
    public void testFindAllGenericSuperclasses() {
        List<Type> types = findAllGenericSuperclasses(E.class);
        assertEquals(4, types.size());
        assertTrue(types.contains(A.class));
        assertTrue(types.contains(B.class));
        assertTrue(types.contains(C.class));
        assertTrue(types.contains(Object.class));


        types = findAllGenericSuperclasses(D.class);

        assertEquals(4, types.size());
        assertTrue(types.contains(A.class));
        assertTrue(types.contains(B.class));
        assertFalse(types.contains(C.class));
        assertTrue(types.contains(Object.class));

        Iterator<Type> iterator = types.iterator();
        while (iterator.hasNext()) {
            Type type = iterator.next();
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                assertEquals(C.class, parameterizedType.getRawType());
                assertEquals(String.class, parameterizedType.getActualTypeArguments()[0]);
            }
        }

        types = findAllGenericSuperclasses(D.class, TypeUtils::isParameterizedType);
        assertEquals(1, types.size());

        // null
        types = findAllGenericSuperclasses(null);
        assertTrue(types.isEmpty());
    }

    @Test
    public void testFindAllGenericInterfaces() {
        List<Type> types = findAllGenericInterfaces(C.class);
        assertEquals(3, types.size());

        types = findAllGenericInterfaces(C.class, TypeUtils::isParameterizedType);

        Iterator<Type> iterator = types.iterator();
        while (iterator.hasNext()) {
            Type type = iterator.next();
            ParameterizedType parameterizedType = (ParameterizedType) type;
            assertEquals(Comparable.class, parameterizedType.getRawType());
            assertEquals(B.class, parameterizedType.getActualTypeArguments()[0]);
        }
    }

    @Test
    public void testFindAllGenericInterfacesOnNull() {
        assertSame(emptyList(), findAllGenericInterfaces(null));
    }

    @Test
    public void testFindParameterizedTypes() {
        List<ParameterizedType> genericTypes = findParameterizedTypes(A.class, PARAMETERIZED_TYPE_FILTER);
        assertSame(emptyList(), genericTypes);

        genericTypes = findParameterizedTypes(B.class, PARAMETERIZED_TYPE_FILTER);
        assertEquals(1, genericTypes.size());
        assertEquals(of(Comparable.class, B.class), genericTypes.get(0));

        genericTypes = findParameterizedTypes(C.class, PARAMETERIZED_TYPE_FILTER);
        assertSame(emptyList(), genericTypes);

        genericTypes = findParameterizedTypes(D.class, PARAMETERIZED_TYPE_FILTER);
        assertEquals(1, genericTypes.size());
        assertEquals(of(C.class, String.class), genericTypes.get(0));

        genericTypes = findParameterizedTypes(E.class, PARAMETERIZED_TYPE_FILTER);
        assertSame(emptyList(), genericTypes);
    }

    @Test
    public void testFindParameterizedTypesOnNull() {
        assertSame(emptyList(), findParameterizedTypes(null, PARAMETERIZED_TYPE_FILTER));
    }

    @Test
    public void testFindParameterizedTypesOnObjectClass() {
        assertSame(emptyList(), findParameterizedTypes(Object.class, PARAMETERIZED_TYPE_FILTER));
    }

    @Test
    public void testFindAllParameterizedTypes() {
        List<ParameterizedType> genericTypes = findAllParameterizedTypes(A.class, PARAMETERIZED_TYPE_FILTER);
        assertSame(emptyList(), genericTypes);

        genericTypes = findAllParameterizedTypes(B.class, PARAMETERIZED_TYPE_FILTER);
        assertEquals(1, genericTypes.size());
        assertEquals(of(Comparable.class, B.class), genericTypes.get(0));

        genericTypes = findAllParameterizedTypes(C.class, PARAMETERIZED_TYPE_FILTER);
        assertEquals(1, genericTypes.size());
        assertEquals(of(Comparable.class, B.class), genericTypes.get(0));

        genericTypes = findAllParameterizedTypes(D.class, PARAMETERIZED_TYPE_FILTER);
        assertEquals(2, genericTypes.size());
        assertEquals(of(C.class, String.class), genericTypes.get(0));
        assertEquals(of(Comparable.class, B.class), genericTypes.get(1));

        genericTypes = findAllParameterizedTypes(E.class, PARAMETERIZED_TYPE_FILTER);
        assertEquals(1, genericTypes.size());
        assertEquals(of(Comparable.class, B.class), genericTypes.get(0));
    }

    @Test
    public void testFindAllParameterizedTypesOnNull() {
        assertSame(emptyList(), findAllParameterizedTypes(null, PARAMETERIZED_TYPE_FILTER));
    }

    @Test
    public void testFindAllParameterizedTypesOnObjectClass() {
        assertSame(emptyList(), findAllParameterizedTypes(Object.class, PARAMETERIZED_TYPE_FILTER));
    }

    @Test
    public void testFindHierarchicalTypes() {
        List<Type> types = findHierarchicalTypes(A.class, NON_OBJECT_TYPE_FILTER);
        assertTypes(types, Serializable.class);

        types = findHierarchicalTypes(E.class, WILDCARD_TYPE_FILTER);
        assertTrue(types.isEmpty());

        types = findHierarchicalTypes(D.class, GENERIC_ARRAY_TYPE_FILTER);
        assertTrue(types.isEmpty());
    }


    @Test
    public void testFindAllTypes() {
        List<Type> types = findAllTypes(D.class, TypeUtils::isParameterizedType);
        assertEquals(2, types.size());
        assertTrue(types.contains(of(C.class, String.class)));
        assertTrue(types.contains(of(Comparable.class, B.class)));
    }

    @Test
    public void testFindAllTypesWithoutPredicate() {
        List<Type> types = findAllTypes(D.class);
        assertEquals(8, types.size());
        assertTrue(types.contains(D.class));
        assertTrue(types.contains(of(C.class, String.class)));
        assertTrue(types.contains(RandomAccess.class));
        assertTrue(types.contains(B.class));
        assertTrue(types.contains(of(Comparable.class, B.class)));
        assertTrue(types.contains(A.class));
        assertTrue(types.contains(Serializable.class));
        assertTrue(types.contains(Object.class));
    }

    @Test
    public void testFindAllTypesOnNull() {
        List<Type> types = findAllTypes(null);
        assertSame(emptyList(), types);
    }

    @Test
    public void testGetClassName() {
        assertEquals("java.lang.String", getClassName(String.class));
    }

    @Test
    public void testGetClassNameOnNull() {
        assertThrows(NullPointerException.class, () -> getClassName(null));
    }

    @Test
    public void testGetClassNames() {
        Set<String> classNames = getClassNames(ofList(String.class, Integer.class));
        assertEquals(2, classNames.size());
        assertTrue(classNames.contains("java.lang.String"));
        assertTrue(classNames.contains("java.lang.Integer"));
    }

    @Test
    public void testResolveTypeArguments() {
        List<Type> typeArguments = resolveTypeArguments(A.class);
        assertTrue(typeArguments.isEmpty());

        typeArguments = resolveTypeArguments(B.class);
        assertEquals(1, typeArguments.size());
        assertEquals(B.class, typeArguments.get(0));

        typeArguments = resolveTypeArguments(C.class);
        assertEquals(1, typeArguments.size());
        assertEquals(B.class, typeArguments.get(0));

        typeArguments = resolveTypeArguments(D.class);
        assertEquals(2, typeArguments.size());
        assertEquals(String.class, typeArguments.get(0));
        assertEquals(B.class, typeArguments.get(1));

        typeArguments = resolveTypeArguments(E.class);
        assertEquals(1, typeArguments.size());
        assertEquals(B.class, typeArguments.get(0));
    }

    @Test
    public void testResolveTypeArgumentsOnNull() {
        assertSame(emptyList(), resolveTypeArguments((Class) null));
    }

    @Test
    public void testResolveTypeArgumentsOnPrimitiveType() {
        PRIMITIVE_TYPES.forEach(primitiveType -> {
            assertSame(emptyList(), resolveTypeArguments(primitiveType));
        });
    }

    @Test
    public void testResolveTypeArgumentsOnArrayType() {
        assertSame(emptyList(), resolveTypeArguments(int[].class));
        assertSame(emptyList(), resolveTypeArguments(String[].class));
        assertSame(emptyList(), resolveTypeArguments(String[][].class));
    }

    @Test
    public void testResolveTypeArgumentClasses() {
        List<Class<?>> typeArguments = resolveTypeArgumentClasses(A.class);
        assertTrue(typeArguments.isEmpty());

        typeArguments = resolveTypeArgumentClasses(B.class);
        assertEquals(1, typeArguments.size());
        assertEquals(B.class, typeArguments.get(0));

    }

    @Test
    public void testDoResolveActualTypeArguments() {
        List<Type> actualTypeArguments = null;

        actualTypeArguments = doResolveActualTypeArguments(StringIntegerToBoolean.class, BiFunction.class);
        assertTypes(actualTypeArguments, String.class, Integer.class, Boolean.class);

        actualTypeArguments = doResolveActualTypeArguments(StringBooleanToInteger.class, BiFunction.class);
        assertTypes(actualTypeArguments, String.class, Boolean.class, Integer.class);

        actualTypeArguments = doResolveActualTypeArguments(StringToIntegerConverter.class, Converter.class);
        assertTypes(actualTypeArguments, String.class, Integer.class);

        actualTypeArguments = doResolveActualTypeArguments(B.class, Comparable.class);
        assertTypes(actualTypeArguments, B.class);

        actualTypeArguments = doResolveActualTypeArguments(C.class, Comparable.class);
        assertTypes(actualTypeArguments, B.class);

        actualTypeArguments = doResolveActualTypeArguments(D.class, C.class);
        assertTypes(actualTypeArguments, String.class);

        actualTypeArguments = doResolveActualTypeArguments(E.class, Comparable.class);
        assertTypes(actualTypeArguments, B.class);

        actualTypeArguments = doResolveActualTypeArguments(StringIntegerBooleanHashMap.class, Map.class);
        assertTypes(actualTypeArguments, String.class, Integer.class);


        actualTypeArguments = doResolveActualTypeArguments(StringToStringConverter.class, Converter.class);
        assertTypes(actualTypeArguments, String.class, String.class);

    }

    @Test
    public void testDoResolveActualTypeArgumentsOnNull() {
        assertSame(emptyList(), doResolveActualTypeArguments(null, BiFunction.class));
        assertSame(emptyList(), doResolveActualTypeArguments(BiFunction.class, null));
    }

    @Test
    public void testDoResolveActualTypeArgumentsInFastPathOnNull() {
        assertSame(emptyList(), doResolveActualTypeArgumentsInFastPath(null));
    }

    @Test
    public void testAsClassOnClass() {
        assertSame(String.class, asClass(String.class));
    }

    @Test
    public void testAsClassOnParameterizedType() {
        assertSame(Comparable.class, asClass(of(Comparable.class, String.class)));
        assertSame(Comparable.class, asClass(B.class.getGenericInterfaces()[0]));
        assertSame(C.class, asClass(D.class.getGenericSuperclass()));
    }

    @Test
    public void testAsClassOnNull() {
        assertNull(asClass(null));
    }

    @Test
    public void testAsClassOnGenericArrayType() {
        assertNull(asClass(getGenericArrayType()));
    }

    @Test
    public void testAsClassOnTypeVariable() {
        assertNull(asClass(getTypeVariable()));
    }

    @Test
    public void testAsClassOnWildcardType() {
        assertNull(asClass(getWildcardType()));
    }

    @Test
    public void testAsGenericArrayTypeOnClass() {
        assertNull(asGenericArrayType(String.class));
    }

    @Test
    public void testAsGenericArrayTypeOnParameterizedType() {
        assertNull(asGenericArrayType(of(Comparable.class, String.class)));
        assertNull(asGenericArrayType(B.class.getGenericInterfaces()[0]));
        assertNull(asGenericArrayType(D.class.getGenericSuperclass()));
    }

    @Test
    public void testAsGenericArrayTypeOnNull() {
        assertNull(asGenericArrayType(null));
    }

    @Test
    public void testAsGenericArrayTypeOnGenericArrayType() {
        assertSame(getGenericArrayType(), asGenericArrayType(getGenericArrayType()));
    }

    @Test
    public void testAsGenericArrayTypeOnTypeVariable() {
        assertNull(asGenericArrayType(getTypeVariable()));
    }

    @Test
    public void testAsGenericArrayTypeOnWildcardType() {
        assertNull(asGenericArrayType(getWildcardType()));
    }

    @Test
    public void testAsParameterizedTypeOnClass() {
        assertNull(asParameterizedType(String.class));
    }

    @Test
    public void testAsParameterizedTypeOnParameterizedType() {
        assertEquals(of(Comparable.class, String.class), asParameterizedType(of(Comparable.class, String.class)));
        assertEquals(of(Comparable.class, B.class), asParameterizedType(B.class.getGenericInterfaces()[0]));
        assertEquals(of(C.class, String.class), asParameterizedType(D.class.getGenericSuperclass()));
    }

    @Test
    public void testAsParameterizedTypeOnNull() {
        assertNull(asParameterizedType(null));
    }

    @Test
    public void testAsParameterizedTypeOnGenericArrayType() {
        assertNull(asParameterizedType(getGenericArrayType()));
    }

    @Test
    public void testAsParameterizedTypeOnTypeVariable() {
        assertNull(asParameterizedType(getTypeVariable()));
    }

    @Test
    public void testAsParameterizedTypeOnWildcardType() {
        assertNull(asParameterizedType(getWildcardType()));
    }

    @Test
    public void testAsTypeVariableOnClass() {
        assertNull(asTypeVariable(String.class));
    }

    @Test
    public void testAsTypeVariableOnParameterizedType() {
        assertNull(asTypeVariable(of(Comparable.class, String.class)));
        assertNull(asTypeVariable(B.class.getGenericInterfaces()[0]));
        assertNull(asTypeVariable(D.class.getGenericSuperclass()));
    }

    @Test
    public void testAsTypeVariableOnNull() {
        assertNull(asTypeVariable(null));
    }

    @Test
    public void testAsTypeVariableOnGenericArrayType() {
        assertNull(asTypeVariable(getGenericArrayType()));
    }

    @Test
    public void testAsTypeVariableOnTypeVariable() {
        assertSame(getTypeVariable(), asTypeVariable(getTypeVariable()));
    }

    @Test
    public void testAsTypeVariableOnWildcardType() {
        assertNull(asTypeVariable(getWildcardType()));
    }

    @Test
    public void testAsWildcardTypeOnClass() {
        assertNull(asWildcardType(String.class));
    }

    @Test
    public void testAsWildcardTypeOnParameterizedType() {
        assertNull(asWildcardType(of(Comparable.class, String.class)));
        assertNull(asWildcardType(B.class.getGenericInterfaces()[0]));
        assertNull(asWildcardType(D.class.getGenericSuperclass()));
    }

    @Test
    public void testAsWildcardTypeOnNull() {
        assertNull(asWildcardType(null));
    }

    @Test
    public void testAsWildcardTypeOnGenericArrayType() {
        assertNull(asWildcardType(getGenericArrayType()));
    }

    @Test
    public void testAsWildcardTypeOnTypeVariable() {
        assertNull(asWildcardType(getTypeVariable()));
    }

    @Test
    public void testAsWildcardTypeOnWildcardType() {
        assertSame(getWildcardType(), asWildcardType(getWildcardType()));
    }

    @Test
    public void testGetComponentTypeOnClass() {
        assertNull(getComponentType(String.class));
    }

    @Test
    public void testGetComponentTypeOnClassArray() {
        assertSame(String.class, getComponentType(String[].class));
    }

    @Test
    public void testGetComponentTypeOnParameterizedType() {
        assertNull(getComponentType(of(Comparable.class, String.class)));
        assertNull(getComponentType(B.class.getGenericInterfaces()[0]));
        assertNull(getComponentType(D.class.getGenericSuperclass()));
    }

    @Test
    public void testGetComponentTypeOnNull() {
        assertNull(getComponentType(null));
    }

    @Test
    public void testGetComponentTypeOnGenericArrayType() {
        assertTrue(isTypeVariable(getComponentType(getGenericArrayType())));
    }

    @Test
    public void testGetComponentTypeOnTypeVariable() {
        assertNull(getComponentType(getTypeVariable()));
    }

    @Test
    public void testGetComponentTypeOnWildcardType() {
        assertNull(getComponentType(getWildcardType()));
    }

    private Type getGenericArrayType() {
        Method toArrayMethod = findMethod(Collection.class, "toArray", Object[].class);
        return toArrayMethod.getGenericReturnType();
    }

    private Type getTypeVariable() {
        return Comparable.class.getTypeParameters()[0];
    }

    private Type getWildcardType() {
        Method andMethod = findMethod(Predicate.class, "and", Predicate.class);
        Type[] genericParameterTypes = andMethod.getGenericParameterTypes();
        Type genericParameterType = genericParameterTypes[0];
        ParameterizedType parameterizedType = (ParameterizedType) genericParameterType;
        return parameterizedType.getActualTypeArguments()[0];
    }

    private void assertAGenericSuperclasses(List<Type> types) {
        assertTrue(types.contains(Object.class));
    }

    private void assertBGenericSuperclasses(List<Type> types) {
        assertAGenericSuperclasses(types);
        assertTrue(types.contains(A.class));
    }

    private void assertCGenericSuperclasses(List<Type> types) {
        assertBGenericSuperclasses(types);
        assertTrue(types.contains(B.class));
    }

    private void assertDGenericSuperclasses(List<Type> types) {
        assertCGenericSuperclasses(types);
        assertTrue(types.contains(of(C.class, String.class)));
    }

    private void assertEGenericSuperclasses(List<Type> types) {
        assertCGenericSuperclasses(types);
        assertTrue(types.contains(C.class));
    }

    private void assertAGenericInterfaces(List<Type> types) {
        assertTrue(types.contains(Serializable.class));
    }

    private void assertBGenericInterfaces(List<Type> types) {
        assertAGenericInterfaces(types);
        assertTrue(types.contains(of(Comparable.class, B.class)));
    }

    private void assertCGenericInterfaces(List<Type> types) {
        assertBGenericInterfaces(types);
        assertTrue(types.contains(RandomAccess.class));
    }


    private void assertDGenericInterfaces(List<Type> types) {
        assertCGenericInterfaces(types);
    }

    private void assertEGenericInterfaces(List<Type> types) {
        assertDGenericInterfaces(types);
    }

    private void assertTypes(List<Type> types, Type... expectedTypes) {
        assertTypes(types, expectedTypes.length, expectedTypes);
    }

    private void assertTypes(List<Type> types, int expectedSize, Type... expectedTypes) {
        assertEquals(expectedSize, types.size());
        assertEquals(newHashSet(expectedTypes), newHashSet(types));
    }

    private void assertType(Type expect, Type actual) {
        assertEquals(asClass(expect), asClass(actual));
    }
}

class A implements Serializable {
}

class B extends A implements Comparable<B> {
    @Override
    public int compareTo(B o) {
        return 0;
    }
}

class C<T> extends B implements RandomAccess {
}

class D extends C<String> {
}

class E extends C implements Serializable {
}

class MyHashMap<A, B extends Serializable, C> extends HashMap<A, B> implements Map<A, B> {

}

class StringIntegerHashMap extends HashMap<String, Integer> {

}

class StringIntegerBooleanHashMap extends MyHashMap<String, Integer, Boolean> {

}

interface BF3<T, U, R> extends BiFunction<T, U, R> {

}

interface StringBF2<U, R> extends BF3<String, U, R> {

}

interface StringIntegerF1<R> extends StringBF2<Integer, R> {

}

interface StringToIntegerF1<U> extends StringBF2<U, Integer> {

}

interface StringBooleanToInteger extends StringToIntegerF1<Boolean> {

}

interface StringIntegerToBoolean extends StringIntegerF1<Boolean> {

}


// MyHashMap<A, B> -> HashMap<A, B>
