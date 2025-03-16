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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import java.util.function.BiFunction;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.SetUtils.newHashSet;
import static io.microsphere.reflect.TypeUtils.GENERIC_ARRAY_TYPE_FILTER;
import static io.microsphere.reflect.TypeUtils.NON_OBJECT_CLASS_FILTER;
import static io.microsphere.reflect.TypeUtils.NON_OBJECT_TYPE_FILTER;
import static io.microsphere.reflect.TypeUtils.PARAMETERIZED_TYPE_FILTER;
import static io.microsphere.reflect.TypeUtils.TYPE_VARIABLE_FILTER;
import static io.microsphere.reflect.TypeUtils.WILDCARD_TYPE_FILTER;
import static io.microsphere.reflect.TypeUtils.asClass;
import static io.microsphere.reflect.TypeUtils.doResolveActualTypeArguments;
import static io.microsphere.reflect.TypeUtils.doResolveActualTypeArgumentsInFastPath;
import static io.microsphere.reflect.TypeUtils.findAllHierarchicalTypes;
import static io.microsphere.reflect.TypeUtils.findAllTypes;
import static io.microsphere.reflect.TypeUtils.getAllGenericInterfaces;
import static io.microsphere.reflect.TypeUtils.getAllGenericSuperClasses;
import static io.microsphere.reflect.TypeUtils.getAllGenericTypes;
import static io.microsphere.reflect.TypeUtils.getAllInterfaces;
import static io.microsphere.reflect.TypeUtils.getAllSuperTypes;
import static io.microsphere.reflect.TypeUtils.getAllTypes;
import static io.microsphere.reflect.TypeUtils.getClassName;
import static io.microsphere.reflect.TypeUtils.getClassNames;
import static io.microsphere.reflect.TypeUtils.getGenericTypes;
import static io.microsphere.reflect.TypeUtils.getRawClass;
import static io.microsphere.reflect.TypeUtils.isAssignableFrom;
import static io.microsphere.reflect.TypeUtils.isParameterizedType;
import static io.microsphere.reflect.TypeUtils.resolveActualTypeArgument;
import static io.microsphere.reflect.TypeUtils.resolveActualTypeArgumentClass;
import static io.microsphere.reflect.TypeUtils.resolveActualTypeArgumentClasses;
import static io.microsphere.reflect.TypeUtils.resolveActualTypeArguments;
import static io.microsphere.reflect.generics.ParameterizedTypeImpl.of;
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
    public void testGetRawClass() {
        assertEquals(Object.class, getRawClass(Object.class));
        assertEquals(C.class, getRawClass(D.class.getGenericSuperclass()));
    }

    @Test
    public void testGetRawClassOnNull() {
        assertNull(getRawClass(null));
    }

    @Test
    public void testIsAssignableFrom() {
        assertTrue(isAssignableFrom(Object.class, Object.class));
        assertTrue(isAssignableFrom(C.class, D.class.getGenericSuperclass()));
        assertTrue(isAssignableFrom(B.class, D.class.getGenericSuperclass()));
        assertTrue(isAssignableFrom(D.class.getGenericSuperclass(), D.class.getGenericSuperclass()));
        assertTrue(isAssignableFrom(Comparable.class, B.class.getGenericInterfaces()[0]));
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
    public void testResolveActualTypeArgument() {
        assertSame(String.class, resolveActualTypeArgument(D.class, C.class, 0));
        assertSame(B.class, resolveActualTypeArgument(D.class, Comparable.class, 0));
    }

    @Test
    public void testFindAllHierarchicalTypes() {
        List<Type> types = findAllHierarchicalTypes(A.class, NON_OBJECT_TYPE_FILTER);
        assertTypes(types, Serializable.class);

        types = findAllHierarchicalTypes(E.class, WILDCARD_TYPE_FILTER);
        assertTrue(types.isEmpty());

        types = findAllHierarchicalTypes(D.class, GENERIC_ARRAY_TYPE_FILTER);
        assertTrue(types.isEmpty());
    }

    @Test
    public void testFindAllHierarchicalTypesWithoutPredicate() {
        List<Type> types = findAllHierarchicalTypes(A.class);
        assertTypes(types, Object.class, Serializable.class);

        types = findAllHierarchicalTypes(B.class);
        assertTypes(types, A.class, of(Comparable.class, B.class), Object.class, Serializable.class);

        types = findAllHierarchicalTypes(C.class);
        assertTypes(types, B.class, RandomAccess.class, A.class, of(Comparable.class, B.class), Object.class, Serializable.class);

        types = findAllHierarchicalTypes(D.class);
        assertTypes(types, of(C.class, String.class), B.class, RandomAccess.class, A.class, of(Comparable.class, B.class), Object.class, Serializable.class);

        types = findAllHierarchicalTypes(D.class);
        assertTypes(types, of(C.class, String.class), B.class, RandomAccess.class, A.class, of(Comparable.class, B.class), Object.class, Serializable.class);

        types = findAllHierarchicalTypes(E.class);
        assertTypes(types, C.class, Serializable.class, B.class, RandomAccess.class, A.class, of(Comparable.class, B.class), Object.class);
    }

    @Test
    public void testGetAllGenericTypes() {
        List<ParameterizedType> genericTypes = getAllGenericTypes(E.class);

        assertEquals(1, genericTypes.size());

        ParameterizedType genericType = genericTypes.get(0);
        assertEquals(Comparable.class, genericType.getRawType());
        assertEquals(B.class, genericType.getActualTypeArguments()[0]);

        genericTypes = getAllGenericTypes(D.class);

        assertEquals(2, genericTypes.size());
        assertEquals(C.class, genericTypes.get(0).getRawType());
        assertEquals(String.class, genericTypes.get(0).getActualTypeArguments()[0]);

        assertEquals(Comparable.class, genericTypes.get(1).getRawType());
        assertEquals(B.class, genericTypes.get(1).getActualTypeArguments()[0]);
    }

    @Test
    public void testGetAllGenericSuperClasses() {
        List<ParameterizedType> genericSuperClasses = getAllGenericSuperClasses(E.class, TypeUtils::isParameterizedType);
        assertEquals(0, genericSuperClasses.size());
    }

    @Test
    public void testGetAllGenericSuperClassesWithoutPredicate() {
        List<ParameterizedType> genericSuperClasses = getAllGenericSuperClasses(E.class);
        assertEquals(0, genericSuperClasses.size());

        genericSuperClasses = getAllGenericSuperClasses(D.class);
        assertEquals(1, genericSuperClasses.size());
        assertEquals(C.class, genericSuperClasses.get(0).getRawType());
        assertEquals(String.class, genericSuperClasses.get(0).getActualTypeArguments()[0]);
    }

    @Test
    public void testGetAllGenericSuperClassesOnNullType() {
        assertSame(emptyList(), getAllGenericSuperClasses(null));
    }

    @Test
    public void testGetAllGenericSuperClassesOnInterfaceType() {
        assertSame(emptyList(), getAllGenericSuperClasses(Comparable.class));
    }

    @Test
    public void testGetAllGenericInterfaces() {
        List<ParameterizedType> genericInterfaces = getAllGenericInterfaces(E.class);
        assertEquals(1, genericInterfaces.size());
        assertEquals(Comparable.class, genericInterfaces.get(0).getRawType());
        assertEquals(B.class, genericInterfaces.get(0).getActualTypeArguments()[0]);
    }

    @Test
    public void testGetAllGenericInterfacesOnNull() {
        assertSame(emptyList(), getAllGenericInterfaces(null));
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
    public void testGetGenericTypes() {
        List<ParameterizedType> genericTypes = getGenericTypes(D.class, type -> isParameterizedType(type));
        assertEquals(1, genericTypes.size());
        assertEquals(C.class, genericTypes.get(0).getRawType());
        assertEquals(String.class, genericTypes.get(0).getActualTypeArguments()[0]);
    }

    @Test
    public void testGetGenericTypesWithoutPredicate() {
        List<ParameterizedType> genericTypes = getGenericTypes(D.class);
        assertEquals(1, genericTypes.size());
        assertEquals(C.class, genericTypes.get(0).getRawType());
        assertEquals(String.class, genericTypes.get(0).getActualTypeArguments()[0]);
    }

    @Test
    public void testFindAllTypesWithout() {
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
    public void testGetGenericTypesOnNull() {
        assertSame(emptyList(), getGenericTypes(null));
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


    @Test
    public void testGetAllSuperTypes() {
        Set<Type> types = getAllSuperTypes(E.class);
        assertEquals(4, types.size());
        assertTrue(types.contains(A.class));
        assertTrue(types.contains(B.class));
        assertTrue(types.contains(C.class));
        assertTrue(types.contains(Object.class));


        types = getAllSuperTypes(D.class);

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

        types = getAllSuperTypes(D.class, TypeUtils::isParameterizedType);
        assertEquals(1, types.size());

        // null
        types = getAllSuperTypes(null);
        assertTrue(types.isEmpty());
    }

    @Test
    public void testGetAllInterfaces() {
        Set<Type> types = getAllInterfaces(C.class);
        assertEquals(3, types.size());

        types = getAllInterfaces(C.class, TypeUtils::isParameterizedType);

        Iterator<Type> iterator = types.iterator();
        while (iterator.hasNext()) {
            Type type = iterator.next();
            ParameterizedType parameterizedType = (ParameterizedType) type;
            assertEquals(Comparable.class, parameterizedType.getRawType());
            assertEquals(B.class, parameterizedType.getActualTypeArguments()[0]);
        }
    }

    @Test
    public void testGetAllTypes() {
        Set<Type> types = getAllTypes(E.class);
        assertEquals(8, types.size());
        assertTrue(types.contains(E.class));
        assertTrue(types.contains(C.class));
        assertTrue(types.contains(B.class));
        assertTrue(types.contains(A.class));
        assertTrue(types.contains(Object.class));
        assertTrue(types.contains(Serializable.class));
        assertFalse(types.contains(Comparable.class));
        assertTrue(types.contains(RandomAccess.class));

        types = getAllTypes(D.class, TypeUtils::isParameterizedType);
        assertEquals(2, types.size());

        types = getAllTypes(E.class, TypeUtils::isParameterizedType);
        assertEquals(1, types.size());

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
