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
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;
import java.util.Set;

import static io.microsphere.reflect.TypeUtils.NON_OBJECT_CLASS_FILTER;
import static io.microsphere.reflect.TypeUtils.NON_OBJECT_TYPE_FILTER;
import static io.microsphere.reflect.TypeUtils.PARAMETERIZED_TYPE_FILTER;
import static io.microsphere.reflect.TypeUtils.TYPE_VARIABLE_FILTER;
import static io.microsphere.reflect.TypeUtils.asClass;
import static io.microsphere.reflect.TypeUtils.findAllHierarchicalTypes;
import static io.microsphere.reflect.TypeUtils.getAllInterfaces;
import static io.microsphere.reflect.TypeUtils.getAllSuperTypes;
import static io.microsphere.reflect.TypeUtils.getAllTypes;
import static io.microsphere.reflect.TypeUtils.resolveActualTypeArguments;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    public void testFindAllHierarchicalTypes() {
        List<Type> types = findAllHierarchicalTypes(A.class);
        assertTypes(types, Object.class, Serializable.class);

        types = findAllHierarchicalTypes(B.class);
        assertTypes(types, A.class, Comparable.class, Object.class, Serializable.class);

        types = findAllHierarchicalTypes(C.class);
        assertTypes(types, B.class, RandomAccess.class, A.class, Comparable.class, Object.class, Serializable.class);

        types = findAllHierarchicalTypes(D.class);
        assertTypes(types, C.class, B.class, RandomAccess.class, A.class, Comparable.class, Object.class, Serializable.class);

        types = findAllHierarchicalTypes(D.class);
        assertTypes(types, C.class, B.class, RandomAccess.class, A.class, Comparable.class, Object.class, Serializable.class);

        types = findAllHierarchicalTypes(E.class);
        assertTypes(types, C.class, Serializable.class, B.class, RandomAccess.class, A.class, Comparable.class, Object.class, Serializable.class);
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

        actualTypeArguments = resolveActualTypeArguments(StringToIntegerConverter.class, Converter.class);
        assertTypes(actualTypeArguments, String.class, Integer.class);

    }


    private void assertTypes(List<Type> types, Type... expectedTypes) {
        assertTypes(types, expectedTypes.length, expectedTypes);
    }

    private void assertTypes(List<Type> types, int expectedSize, Type... expectedTypes) {
        assertEquals(expectedSize, types.size());
        for (int i = 0; i < expectedSize; i++) {
            assertType(expectedTypes[i], types.get(i));
        }
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


}
