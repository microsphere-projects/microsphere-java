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
package io.microsphere.annotation.processor.util;

import io.microsphere.annotation.processor.AbstractAnnotationProcessingTest;
import io.microsphere.annotation.processor.DefaultTestService;
import io.microsphere.annotation.processor.GenericTestService;
import io.microsphere.annotation.processor.TestService;
import io.microsphere.annotation.processor.TestServiceImpl;
import io.microsphere.annotation.processor.model.ArrayTypeModel;
import io.microsphere.annotation.processor.model.CollectionTypeModel;
import io.microsphere.annotation.processor.model.Color;
import io.microsphere.annotation.processor.model.MapTypeModel;
import io.microsphere.annotation.processor.model.Model;
import io.microsphere.annotation.processor.model.PrimitiveTypeModel;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.EventListener;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static io.microsphere.annotation.processor.util.FieldUtils.findField;
import static io.microsphere.annotation.processor.util.FieldUtils.getDeclaredFields;
import static io.microsphere.annotation.processor.util.TypeUtils.findAllDeclaredTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.findAllDeclaredTypesOfInterfaces;
import static io.microsphere.annotation.processor.util.TypeUtils.findAllDeclaredTypesOfSuperTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.findAllDeclaredTypesOfSuperclasses;
import static io.microsphere.annotation.processor.util.TypeUtils.findAllTypeElementsOfInterfaces;
import static io.microsphere.annotation.processor.util.TypeUtils.findAllTypeElementsOfSuperclasses;
import static io.microsphere.annotation.processor.util.TypeUtils.findAllTypeMirrorsOfInterfaces;
import static io.microsphere.annotation.processor.util.TypeUtils.findDeclaredTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.findDeclaredTypesOfInterfaces;
import static io.microsphere.annotation.processor.util.TypeUtils.findInterfaceTypeMirror;
import static io.microsphere.annotation.processor.util.TypeUtils.findTypeElements;
import static io.microsphere.annotation.processor.util.TypeUtils.findTypeElementsOfInterfaces;
import static io.microsphere.annotation.processor.util.TypeUtils.findTypeMirrorsOfInterfaces;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllDeclaredTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllDeclaredTypesOfInterfaces;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllDeclaredTypesOfSuperTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllDeclaredTypesOfSuperclasses;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllTypeElements;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllTypeElementsOfInterfaces;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllTypeElementsOfSuperTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllTypeElementsOfSuperclasses;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllTypeMirrorsOfInterfaces;
import static io.microsphere.annotation.processor.util.TypeUtils.getDeclaredTypeOfSuperclass;
import static io.microsphere.annotation.processor.util.TypeUtils.getDeclaredTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.getDeclaredTypesOfInterfaces;
import static io.microsphere.annotation.processor.util.TypeUtils.getTypeElementOfSuperclass;
import static io.microsphere.annotation.processor.util.TypeUtils.getTypeElementsOfInterfaces;
import static io.microsphere.annotation.processor.util.TypeUtils.getTypeMirrorsOfInterfaces;
import static io.microsphere.annotation.processor.util.TypeUtils.isAnnotationType;
import static io.microsphere.annotation.processor.util.TypeUtils.isArrayType;
import static io.microsphere.annotation.processor.util.TypeUtils.isClassType;
import static io.microsphere.annotation.processor.util.TypeUtils.isDeclaredType;
import static io.microsphere.annotation.processor.util.TypeUtils.isEnumType;
import static io.microsphere.annotation.processor.util.TypeUtils.isInterfaceType;
import static io.microsphere.annotation.processor.util.TypeUtils.isPrimitiveType;
import static io.microsphere.annotation.processor.util.TypeUtils.isSameType;
import static io.microsphere.annotation.processor.util.TypeUtils.isSimpleType;
import static io.microsphere.annotation.processor.util.TypeUtils.isTypeElement;
import static io.microsphere.annotation.processor.util.TypeUtils.ofDeclaredType;
import static io.microsphere.annotation.processor.util.TypeUtils.ofDeclaredTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.ofTypeElement;
import static io.microsphere.annotation.processor.util.TypeUtils.ofTypeElements;
import static io.microsphere.annotation.processor.util.TypeUtils.ofTypeMirrors;
import static io.microsphere.annotation.processor.util.TypeUtils.typeElementFinder;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.lang.function.Predicates.alwaysFalse;
import static io.microsphere.lang.function.Predicates.alwaysTrue;
import static io.microsphere.reflect.TypeUtils.getTypeNames;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.combine;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The {@link TypeUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class TypeUtilsTest extends AbstractAnnotationProcessingTest {

    /**
     * self type
     */
    private static final Class<?> SELF_TYPE = TestServiceImpl.class;

    /**
     * super class
     */
    private static final Class<?> SUPER_CLASS = GenericTestService.class;

    /**
     * all types
     */
    private static final Type[] ALL_TYPES = ofArray(SELF_TYPE, SUPER_CLASS, DefaultTestService.class, Object.class, TestService.class, EventListener.class, AutoCloseable.class, Serializable.class);

    /**
     * all super types
     */
    private static final Type[] ALL_SUPER_TYPES = ofArray(SUPER_CLASS, DefaultTestService.class, Object.class, TestService.class, EventListener.class, AutoCloseable.class, Serializable.class);

    /**
     * all super classes
     */
    private static final Type[] ALL_SUPER_CLASSES = ofArray(SUPER_CLASS, DefaultTestService.class, Object.class);

    /**
     * all super interfaces
     */
    private static final Type[] ALL_SUPER_INTERFACES = ofArray(TestService.class, EventListener.class, AutoCloseable.class, Serializable.class);

    /**
     * super interfaces
     */
    private static final Type[] SUPER_INTERFACES = ofArray(TestService.class, AutoCloseable.class, Serializable.class);

    /**
     * super class + super interfaces
     */
    private static final Type[] SUPER_TYPES = combine(SUPER_CLASS, SUPER_INTERFACES);

    /**
     * self type + all super types = all types
     */
    private static final Type[] SELF_TYPE_PLUS_ALL_SUPER_TYPES = combine(SELF_TYPE, ALL_SUPER_TYPES);

    /**
     * self type + all super classes
     */
    private static final Type[] SELF_TYPE_PLUS_ALL_SUPER_CLASSES = combine(SELF_TYPE, ALL_SUPER_CLASSES);

    /**
     * self type + all super interfaces
     */
    private static final Type[] SELF_TYPE_PLUS_ALL_SUPER_INTERFACES = combine(SELF_TYPE, ALL_SUPER_INTERFACES);

    /**
     * self type + super class
     */
    private static final Type[] SELF_TYPE_PLUS_SUPER_CLASS = ofArray(SELF_TYPE, SUPER_CLASS);

    /**
     * self type + super interfaces
     */
    private static final Type[] SELF_TYPE_PLUS_SUPER_INTERFACES = combine(SELF_TYPE, SUPER_INTERFACES);

    /**
     * self type + super class + super interfaces
     */
    private static final Type[] SELF_TYPE_PLUS_SUPER_CLASS_PLUS_SUPER_INTERFACES = combine(SELF_TYPE, SUPER_TYPES);

    @Override
    protected void addCompiledClasses(Set<Class<?>> compiledClasses) {
        compiledClasses.add(ArrayTypeModel.class);
        compiledClasses.add(CollectionTypeModel.class);
        compiledClasses.add(Color.class);
        compiledClasses.add(MapTypeModel.class);
    }

    @Test
    public void testIsSimpleType() {
        assertTrue(isSimpleType(getTypeElement(Void.class)));
        assertTrue(isSimpleType(getTypeElement(Boolean.class)));
        assertTrue(isSimpleType(getTypeElement(Character.class)));
        assertTrue(isSimpleType(getTypeElement(Byte.class)));
        assertTrue(isSimpleType(getTypeElement(Short.class)));
        assertTrue(isSimpleType(getTypeElement(Integer.class)));
        assertTrue(isSimpleType(getTypeElement(Long.class)));
        assertTrue(isSimpleType(getTypeElement(Float.class)));
        assertTrue(isSimpleType(getTypeElement(Double.class)));
        assertTrue(isSimpleType(getTypeElement(String.class)));
        assertTrue(isSimpleType(getTypeElement(BigDecimal.class)));
        assertTrue(isSimpleType(getTypeElement(BigInteger.class)));
        assertTrue(isSimpleType(getTypeElement(Date.class)));
        assertTrue(isSimpleType(getTypeElement(Object.class)));

        assertFalse(isSimpleType(getTypeElement(getClass())));
    }

    @Test
    public void testIsSimpleTypeOnNull() {
        assertFalse(isSimpleType(NULL_TYPE_ELEMENT));
        assertFalse(isSimpleType(NULL_TYPE_MIRROR));
    }

    @Test
    public void testIsSameType() {
        assertIsSameType(testTypeElement, testClass);

        assertFalse(isSameType(getDeclaredType(String.class), "java.lang.Void"));
    }

    @Test
    public void testIsSameTypeOnNull() {
        assertFalse(isSameType(NULL_TYPE_MIRROR, testClass));
        assertFalse(isSameType(NULL_TYPE_MIRROR, testClassName));
        assertFalse(isSameType(NULL_ELEMENT, testClass));
        assertFalse(isSameType(NULL_ELEMENT, testClassName));

        assertFalse(isSameType(testTypeElement, NULL_TYPE));
        assertFalse(isSameType(testTypeElement, NULL_STRING));

        assertFalse(isSameType(testTypeMirror, NULL_TYPE));
        assertFalse(isSameType(testTypeMirror, NULL_STRING));

        assertTrue(isSameType(NULL_TYPE_MIRROR, NULL_TYPE));
        assertTrue(isSameType(NULL_TYPE_MIRROR, NULL_STRING));
        assertTrue(isSameType(NULL_ELEMENT, NULL_TYPE));
        assertTrue(isSameType(NULL_ELEMENT, NULL_STRING));
    }

    @Test
    public void testIsArrayTypeOnTypeMirror() {
        assertIsArrayType(ArrayTypeModel.class);

        assertFalse(isArrayType(getTypeMirror(Color.class)));
        assertFalse(isArrayType(getTypeMirror(ArrayTypeModel.class)));
    }

    @Test
    public void testIsArrayTypeOnElement() {
        assertIsArrayType(getTypeElement(ArrayTypeModel.class));

        assertFalse(isArrayType(getTypeElement(Color.class)));
        assertFalse(isArrayType(getTypeElement(ArrayTypeModel.class)));
    }

    @Test
    public void testIsArrayTypeOnNull() {
        assertFalse(isArrayType(NULL_ELEMENT));
        assertFalse(isArrayType(NULL_TYPE_MIRROR));
    }

    @Test
    public void testIsEnumType() {
        assertTrue(isEnumType(getDeclaredType(Color.class)));
        assertFalse(isEnumType(getTypeElement(ArrayTypeModel.class)));
    }

    @Test
    public void testIsEnumTypeOnNull() {
        assertFalse(isEnumType(NULL_ELEMENT));
        assertFalse(isEnumType(NULL_TYPE_MIRROR));
    }

    @Test
    public void testIsClassType() {
        // class
        assertTrue(isClassType(getTypeElement(ArrayTypeModel.class)));
        assertTrue(isClassType(getDeclaredType(ArrayTypeModel.class)));

        assertTrue(isClassType(getTypeElement(Model.class)));
        assertTrue(isClassType(getDeclaredType(Model.class)));

        // enum
        assertFalse(isClassType(getTypeElement(TimeUnit.class)));
        assertFalse(isClassType(getDeclaredType(TimeUnit.class)));

        // interface
        assertFalse(isClassType(getTypeElement(Serializable.class)));
        assertFalse(isClassType(getDeclaredType(Serializable.class)));
    }

    @Test
    public void testIsClassTypeOnNull() {
        assertFalse(isClassType(NULL_ELEMENT));
        assertFalse(isClassType(NULL_TYPE_MIRROR));
    }

    @Test
    public void testIsPrimitiveType() {
        TypeElement type = getTypeElement(PrimitiveTypeModel.class);
        getDeclaredFields(type.asType())
                .stream()
                .map(VariableElement::asType)
                .forEach(t -> assertTrue(isPrimitiveType(t)));

        assertFalse(isPrimitiveType(getTypeElement(ArrayTypeModel.class)));
    }

    @Test
    public void testIsPrimitiveTypeOnNull() {
        assertFalse(isPrimitiveType(NULL_ELEMENT));
        assertFalse(isPrimitiveType(NULL_TYPE_MIRROR));
    }

    @Test
    public void testIsInterfaceType() {
        assertTrue(isInterfaceType(getTypeElement(CharSequence.class)));
        assertTrue(isInterfaceType(getDeclaredType(CharSequence.class)));

        assertFalse(isInterfaceType(getTypeElement(Model.class)));
        assertFalse(isInterfaceType(getDeclaredType(Model.class)));
    }

    @Test
    public void testIsInterfaceTypeOnNull() {
        assertFalse(isInterfaceType(NULL_ELEMENT));
        assertFalse(isInterfaceType(NULL_TYPE_MIRROR));
    }

    @Test
    public void testIsAnnotationType() {
        assertTrue(isAnnotationType(getTypeElement(Override.class)));
        assertTrue(isAnnotationType(getDeclaredType(Override.class)));

        assertFalse(isAnnotationType(getTypeElement(Model.class)));
        assertFalse(isAnnotationType(getDeclaredType(Model.class)));
    }

    @Test
    public void testIsAnnotationTypeOnNull() {
        assertFalse(isAnnotationType(NULL_ELEMENT));
        assertFalse(isAnnotationType(NULL_TYPE_MIRROR));
    }

    @Test
    public void testIsTypeElement() {
        assertTrue(isTypeElement(testTypeElement));
        assertTrue(isTypeElement(testTypeMirror));
    }

    @Test
    public void testIsTypeElementOnNull() {
        assertFalse(isTypeElement(NULL_ELEMENT));
        assertFalse(isTypeElement(NULL_TYPE_MIRROR));
    }

    @Test
    public void testIsDeclaredType() {
        assertTrue(isDeclaredType(testTypeElement));
        assertTrue(isDeclaredType(testTypeMirror));
        assertFalse(isDeclaredType(NULL_ELEMENT));
        assertFalse(isDeclaredType(NULL_TYPE_MIRROR));
        assertFalse(isDeclaredType(types.getNullType()));
        assertFalse(isDeclaredType(types.getPrimitiveType(TypeKind.BYTE)));
        assertFalse(isDeclaredType(types.getArrayType(types.getPrimitiveType(TypeKind.BYTE))));
    }

    @Test
    public void testIsDeclaredTypeOnNull() {
        assertFalse(isDeclaredType(NULL_ELEMENT));
        assertFalse(isDeclaredType(NULL_TYPE_MIRROR));
    }

    @Test
    public void testOfTypeElement() {
        assertEquals(testTypeElement, ofTypeElement(testTypeElement));
        assertEquals(testTypeElement, ofTypeElement(testTypeMirror));
    }

    @Test
    public void testOfTypeElementOnNull() {
        assertNull(ofTypeElement(NULL_ELEMENT));
        assertNull(ofTypeElement(NULL_TYPE_MIRROR));
    }

    @Test
    public void testOfDeclaredType() {
        assertEquals(testTypeMirror, testDeclaredType);
        assertEquals(testTypeMirror, ofDeclaredType(testTypeMirror));
        assertEquals(testDeclaredType, ofDeclaredType(testTypeMirror));
    }

    @Test
    public void testOfDeclaredTypeOnNull() {
        assertNull(ofDeclaredType(NULL_ELEMENT));
        assertNull(ofDeclaredType(NULL_TYPE_MIRROR));
    }

    @Test
    public void testOfTypeMirrors() {
        assertOfTypeMirrors(String.class, SELF_TYPE, Color.class);
    }

    @Test
    public void testOfTypeMirrorsOnNull() {
        assertEmptyList(ofTypeMirrors(EMPTY_ELEMENT_ARRAY));
        assertEmptyList(ofTypeMirrors(NULL_COLLECTION));
    }

    @Test
    public void testOfTypeMirrorsOnEmpty() {
        assertEmptyList(ofTypeMirrors(EMPTY_ELEMENT_ARRAY));
        assertEmptyList(ofTypeMirrors(emptyList()));
    }

    @Test
    public void testOfTypeElements() {
        assertOfTypeElements(String.class, SELF_TYPE, Color.class);
    }

    @Test
    public void testOfTypeElementsOnNull() {
        assertEmptyList(ofTypeElements(NULL_TYPE_MIRROR_ARRAY));
        assertEmptyList(ofTypeElements(NULL_COLLECTION));
    }

    @Test
    public void testOfTypeElementsOnEmpty() {
        assertEmptyList(ofTypeElements(EMPTY_TYPE_MIRROR_ARRAY));
        assertEmptyList(ofTypeElements(emptyList()));
    }

    @Test
    public void testOfDeclaredTypes() {
        assertOfDeclaredTypes(String.class, SELF_TYPE, Color.class);
    }

    @Test
    public void testOfDeclaredTypesWithFilter() {
        List<DeclaredType> declaredTypes = ofDeclaredTypes(ofList(getTypeElement(String.class), getTypeElement(TestServiceImpl.class), getTypeElement(Color.class)), t -> true);
        assertDeclaredTypes(declaredTypes, String.class, SELF_TYPE, Color.class);
    }

    @Test
    public void testOfDeclaredTypesOnNull() {
        assertEmptyList(ofDeclaredTypes(NULL_ELEMENT_ARRAY));
        assertEmptyList(ofDeclaredTypes(NULL_COLLECTION));
    }

    @Test
    public void testOfDeclaredTypesOnEmpty() {
        assertEmptyList(ofDeclaredTypes(emptyList()));
    }

    @Test
    public void testGetTypeElementOfSuperclass() {
        TypeElement superTypeElement = getTypeElementOfSuperclass(testTypeElement);
        assertEquals(getTypeElement(GenericTestService.class), superTypeElement);

        superTypeElement = getTypeElementOfSuperclass(superTypeElement);
        assertEquals(getTypeElement(DefaultTestService.class), superTypeElement);

        superTypeElement = getTypeElementOfSuperclass(superTypeElement);
        assertEquals(getTypeElement(Object.class), superTypeElement);

        assertNull(getTypeElementOfSuperclass(superTypeElement));
    }

    @Test
    public void testGetTypeElementOfSuperclassOnNull() {
        assertNull(getTypeElementOfSuperclass(NULL_TYPE_ELEMENT));
    }

    @Test
    public void testGetAllTypeElementsOfSuperclasses() {
        List<TypeElement> allSuperTypes = getAllTypeElementsOfSuperclasses(testTypeElement);
        assertTypeElements(allSuperTypes, ALL_SUPER_CLASSES);
    }

    @Test
    public void testGetAllTypeElementsOfSuperclassesOnNull() {
        assertEmptyList(getAllTypeElementsOfSuperclasses(NULL_TYPE_ELEMENT));
    }

    @Test
    public void testGetTypeElementsOfInterfaces() {
        List<TypeElement> typeElements = getTypeElementsOfInterfaces(testTypeElement);
        assertTypeElements(typeElements, SUPER_INTERFACES);
    }

    @Test
    public void testGetTypeElementsOfInterfacesOnNull() {
        assertEmptyList(getTypeElementsOfInterfaces(NULL_TYPE_ELEMENT));
    }

    @Test
    public void testGetAllTypeElementsOfInterfaces() {
        List<TypeElement> typeElements = getAllTypeElementsOfInterfaces(testTypeElement);
        assertTypeElements(typeElements, ALL_SUPER_INTERFACES);
    }

    @Test
    public void testGetAllTypeElementsOfInterfacesOnNull() {
        assertEmptyList(getAllTypeElementsOfInterfaces(NULL_TYPE_ELEMENT));
    }

    @Test
    public void testGetAllTypeElements() {
        List<TypeElement> allTypeElements = getAllTypeElements(testTypeElement);
        assertTypeElements(allTypeElements, ALL_TYPES);
    }

    @Test
    public void testGetAllTypeElementsOnNull() {
        assertEmptyList(getAllTypeElements(NULL_TYPE_ELEMENT));
    }

    @Test
    public void testGetTypeElementsWithNoArgument() {
        List<TypeElement> typeElements = TypeUtils.getTypeElements(testTypeElement);
        assertTypeElements(typeElements, SELF_TYPE_PLUS_SUPER_CLASS_PLUS_SUPER_INTERFACES);
    }

    @Test
    public void testGetTypeElements() {
        // true true true true : all types
        List<TypeElement> typeElements = TypeUtils.getTypeElements(testTypeElement, true, true, true, true);
        assertTypeElements(typeElements, ALL_TYPES);
        assertEquals(getAllTypeElements(testTypeElement), typeElements);

        // true true true false : self type + all super classes
        typeElements = TypeUtils.getTypeElements(testTypeElement, true, true, true, false);
        assertTypeElements(typeElements, SELF_TYPE_PLUS_ALL_SUPER_CLASSES);

        // true true false true : self type + all super interfaces
        typeElements = TypeUtils.getTypeElements(testTypeElement, true, true, false, true);
        assertTypeElements(typeElements, SELF_TYPE_PLUS_ALL_SUPER_INTERFACES);

        // true true false false : self type
        typeElements = TypeUtils.getTypeElements(testTypeElement, true, true, false, false);
        assertTypeElements(typeElements, SELF_TYPE);

        // true false true true : self type + super class + super interfaces
        typeElements = TypeUtils.getTypeElements(testTypeElement, true, false, true, true);
        assertTypeElements(typeElements, SELF_TYPE_PLUS_SUPER_CLASS_PLUS_SUPER_INTERFACES);

        // true false true false : self type + super class
        typeElements = TypeUtils.getTypeElements(testTypeElement, true, false, true, false);
        assertTypeElements(typeElements, SELF_TYPE_PLUS_SUPER_CLASS);

        // true false false true : self type + super interfaces
        typeElements = TypeUtils.getTypeElements(testTypeElement, true, false, false, true);
        assertTypeElements(typeElements, SELF_TYPE_PLUS_SUPER_INTERFACES);

        // true false false false : self type
        typeElements = TypeUtils.getTypeElements(testTypeElement, true, false, false, false);
        assertTypeElements(typeElements, SELF_TYPE);

        // false true true true : all super types
        typeElements = TypeUtils.getTypeElements(testTypeElement, false, true, true, true);
        assertTypeElements(typeElements, ALL_SUPER_TYPES);
        assertEquals(getAllTypeElementsOfSuperTypes(testTypeElement), typeElements);

        // false true true false : all super classes
        typeElements = TypeUtils.getTypeElements(testTypeElement, false, true, true, false);
        assertTypeElements(typeElements, ALL_SUPER_CLASSES);
        assertEquals(getAllTypeElementsOfSuperclasses(testTypeElement), typeElements);

        // false true false true : all super interfaces
        typeElements = TypeUtils.getTypeElements(testTypeElement, false, true, false, true);
        assertTypeElements(typeElements, ALL_SUPER_INTERFACES);
        assertEquals(getAllTypeElementsOfInterfaces(testTypeElement), typeElements);

        // false true false false : nothing
        typeElements = TypeUtils.getTypeElements(testTypeElement, false, true, false, false);
        assertTypeElements(typeElements);
        assertEmptyList(typeElements);

        // false false true true : super class + super interfaces
        typeElements = TypeUtils.getTypeElements(testTypeElement, false, false, true, true);
        assertTypeElements(typeElements, SUPER_TYPES);

        // false false true false : super class
        typeElements = TypeUtils.getTypeElements(testTypeElement, false, false, true, false);
        assertTypeElements(typeElements, SUPER_CLASS);
        assertEquals(ofList(getTypeElementOfSuperclass(testTypeElement)), typeElements);

        // false false false true : super interfaces
        typeElements = TypeUtils.getTypeElements(testTypeElement, false, false, false, true);
        assertTypeElements(typeElements, SUPER_INTERFACES);
        assertEquals(ofList(getTypeElementsOfInterfaces(testTypeElement)), typeElements);

        // false false false false : nothing
        typeElements = TypeUtils.getTypeElements(testTypeElement, false, false, false, false);
        assertTypeElements(typeElements);
        assertEmptyList(typeElements);
    }

    @Test
    public void testGetTypeElementsOnNull() {
        assertEmptyList(TypeUtils.getTypeElements(NULL_TYPE_ELEMENT, true, true, true, true));
        assertEmptyList(TypeUtils.getTypeElements(NULL_TYPE_ELEMENT, true, true, true, false));
        assertEmptyList(TypeUtils.getTypeElements(NULL_TYPE_ELEMENT, true, true, false, true));
        assertEmptyList(TypeUtils.getTypeElements(NULL_TYPE_ELEMENT, true, true, false, false));

        assertEmptyList(TypeUtils.getTypeElements(NULL_TYPE_ELEMENT, true, false, true, true));
        assertEmptyList(TypeUtils.getTypeElements(NULL_TYPE_ELEMENT, false, false, true, false));
        assertEmptyList(TypeUtils.getTypeElements(NULL_TYPE_ELEMENT, false, false, false, true));
        assertEmptyList(TypeUtils.getTypeElements(NULL_TYPE_ELEMENT, false, false, false, false));

        assertEmptyList(TypeUtils.getTypeElements(NULL_TYPE_ELEMENT, false, true, true, true));
        assertEmptyList(TypeUtils.getTypeElements(NULL_TYPE_ELEMENT, false, true, true, false));
        assertEmptyList(TypeUtils.getTypeElements(NULL_TYPE_ELEMENT, false, true, false, true));
        assertEmptyList(TypeUtils.getTypeElements(NULL_TYPE_ELEMENT, false, true, false, false));

        assertEmptyList(TypeUtils.getTypeElements(NULL_TYPE_ELEMENT, false, false, true, true));
        assertEmptyList(TypeUtils.getTypeElements(NULL_TYPE_ELEMENT, false, false, true, false));
        assertEmptyList(TypeUtils.getTypeElements(NULL_TYPE_ELEMENT, false, false, false, true));
        assertEmptyList(TypeUtils.getTypeElements(NULL_TYPE_ELEMENT, false, false, false, false));
    }

    @Test
    public void testFindAllTypeElementsOfSuperclasses() {
        List<TypeElement> typeElements = findAllTypeElementsOfSuperclasses(testTypeElement, alwaysTrue());
        assertTypeElements(typeElements, ALL_SUPER_CLASSES);
        assertEquals(getAllTypeElementsOfSuperclasses(testTypeElement), typeElements);

        assertEmptyList(findAllTypeElementsOfSuperclasses(testTypeElement, alwaysFalse()));
    }

    @Test
    public void testFindAllTypeElementsOfSuperclassesOnNull() {
        assertEmptyList(findAllTypeElementsOfSuperclasses(NULL_TYPE_ELEMENT, alwaysTrue()));
        assertEmptyList(findAllTypeElementsOfSuperclasses(NULL_TYPE_ELEMENT, alwaysFalse()));
    }

    @Test
    public void testFindAllTypeElementsOfInterfaces() {
        List<TypeElement> typeElements = findAllTypeElementsOfInterfaces(testTypeElement, alwaysTrue());
        assertTypeElements(typeElements, ALL_SUPER_INTERFACES);
        assertEquals(getAllTypeElementsOfInterfaces(testTypeElement), typeElements);

        assertEmptyList(findAllTypeElementsOfInterfaces(testTypeElement, alwaysFalse()));
    }

    @Test
    public void testFindAllTypeElementsOfInterfacesOnNull() {
        assertEmptyList(findAllTypeElementsOfInterfaces(NULL_TYPE_ELEMENT, alwaysTrue()));
        assertEmptyList(findAllTypeElementsOfInterfaces(NULL_TYPE_ELEMENT, alwaysFalse()));
    }

    @Test
    public void testFindTypeElementsOfInterfaces() {
        List<TypeElement> typeElements = findTypeElementsOfInterfaces(testTypeElement, alwaysTrue());
        assertTypeElements(typeElements, SUPER_INTERFACES);
        assertEquals(getTypeElementsOfInterfaces(testTypeElement), typeElements);

        assertEmptyList(findTypeElementsOfInterfaces(testTypeElement, alwaysFalse()));
    }

    @Test
    public void testFindTypeElementsOfInterfacesOnNull() {
        assertEmptyList(findTypeElementsOfInterfaces(NULL_TYPE_ELEMENT, alwaysTrue()));
        assertEmptyList(findTypeElementsOfInterfaces(NULL_TYPE_ELEMENT, alwaysFalse()));
    }

    @Test
    public void testFindTypeElements() {
        // true true true true : all types
        List<TypeElement> typeElements = findTypeElements(testTypeElement, true, true, true, true, alwaysTrue());
        assertTypeElements(typeElements, ALL_TYPES);
        assertEquals(getAllTypeElements(testTypeElement), typeElements);

        // true true true false : self type + all super classes
        typeElements = findTypeElements(testTypeElement, true, true, true, false, alwaysTrue());
        assertTypeElements(typeElements, SELF_TYPE_PLUS_ALL_SUPER_CLASSES);

        // true true false true : self type + all super interfaces
        typeElements = findTypeElements(testTypeElement, true, true, false, true, alwaysTrue());
        assertTypeElements(typeElements, SELF_TYPE_PLUS_ALL_SUPER_INTERFACES);

        // true true false false : self type
        typeElements = findTypeElements(testTypeElement, true, true, false, false, alwaysTrue());
        assertTypeElements(typeElements, SELF_TYPE);

        // true false true true : self type + super class + super interfaces
        typeElements = findTypeElements(testTypeElement, true, false, true, true, alwaysTrue());
        assertTypeElements(typeElements, SELF_TYPE_PLUS_SUPER_CLASS_PLUS_SUPER_INTERFACES);

        // true false true false : self type + super class
        typeElements = findTypeElements(testTypeElement, true, false, true, false, alwaysTrue());
        assertTypeElements(typeElements, SELF_TYPE_PLUS_SUPER_CLASS);

        // true false false true : self type + super interfaces
        typeElements = findTypeElements(testTypeElement, true, false, false, true, alwaysTrue());
        assertTypeElements(typeElements, SELF_TYPE_PLUS_SUPER_INTERFACES);

        // true false false false : self type
        typeElements = findTypeElements(testTypeElement, true, false, false, false, alwaysTrue());
        assertTypeElements(typeElements, SELF_TYPE);

        // false true true true : all super types
        typeElements = findTypeElements(testTypeElement, false, true, true, true, alwaysTrue());
        assertTypeElements(typeElements, ALL_SUPER_TYPES);
        assertEquals(getAllTypeElementsOfSuperTypes(testTypeElement), typeElements);

        // false true true false : all super classes
        typeElements = findTypeElements(testTypeElement, false, true, true, false, alwaysTrue());
        assertTypeElements(typeElements, ALL_SUPER_CLASSES);
        assertEquals(getAllTypeElementsOfSuperclasses(testTypeElement), typeElements);

        // false true false true : all super interfaces
        typeElements = findTypeElements(testTypeElement, false, true, false, true, alwaysTrue());
        assertTypeElements(typeElements, ALL_SUPER_INTERFACES);
        assertEquals(getAllTypeElementsOfInterfaces(testTypeElement), typeElements);

        // false true false false : nothing
        typeElements = findTypeElements(testTypeElement, false, true, false, false, alwaysTrue());
        assertTypeElements(typeElements);
        assertEmptyList(typeElements);

        // false false true true : super types
        typeElements = findTypeElements(testTypeElement, false, false, true, true, alwaysTrue());
        assertTypeElements(typeElements, SUPER_TYPES);

        // false false true false : super class
        typeElements = findTypeElements(testTypeElement, false, false, true, false, alwaysTrue());
        assertTypeElements(typeElements, SUPER_CLASS);
        assertEquals(ofList(getTypeElementOfSuperclass(testTypeElement)), typeElements);

        // false false false true : super interfaces
        typeElements = findTypeElements(testTypeElement, false, false, false, true, alwaysTrue());
        assertTypeElements(typeElements, SUPER_INTERFACES);
        assertEquals(ofList(getTypeElementsOfInterfaces(testTypeElement)), typeElements);

        // false false false false : nothing
        typeElements = findTypeElements(testTypeElement, false, false, false, false, alwaysTrue());
        assertTypeElements(typeElements);
        assertEmptyList(typeElements);
    }

    @Test
    public void testGetDeclaredTypeOfSuperclass() {
        DeclaredType superDeclaredType = getDeclaredTypeOfSuperclass(testTypeMirror);
        assertEquals(superDeclaredType, getTypeElement(GenericTestService.class).asType());

        superDeclaredType = getDeclaredTypeOfSuperclass(superDeclaredType);
        assertEquals(superDeclaredType, getTypeElement(DefaultTestService.class).asType());

        superDeclaredType = getDeclaredTypeOfSuperclass(superDeclaredType);
        assertEquals(superDeclaredType, getTypeElement(Object.class).asType());

        assertNull(getDeclaredTypeOfSuperclass(superDeclaredType));
    }

    @Test
    public void testGetDeclaredTypeOfSuperclassOnNull() {
        assertNull(getDeclaredTypeOfSuperclass(NULL_ELEMENT));
        assertNull(getDeclaredTypeOfSuperclass(NULL_TYPE_MIRROR));
    }

    @Test
    public void testGetDeclaredTypesOfInterfaces() {
        List<DeclaredType> declaredTypes = getDeclaredTypesOfInterfaces(testTypeMirror);
        assertDeclaredTypes(declaredTypes, SUPER_INTERFACES);
    }

    @Test
    public void testGetDeclaredTypesOfInterfacesOnNull() {
        assertEmptyList(getDeclaredTypesOfInterfaces(NULL_ELEMENT));
        assertEmptyList(getDeclaredTypesOfInterfaces(NULL_TYPE_MIRROR));
    }

    @Test
    public void testGetAllDeclaredTypesOfSuperclasses() {
        List<DeclaredType> declaredTypes = getAllDeclaredTypesOfSuperclasses(testTypeMirror);
        assertDeclaredTypes(declaredTypes, ALL_SUPER_CLASSES);
    }

    @Test
    public void testGetAllDeclaredTypesOfSuperclassesOnNull() {
        assertEmptyList(getAllDeclaredTypesOfSuperclasses(NULL_ELEMENT));
        assertEmptyList(getAllDeclaredTypesOfSuperclasses(NULL_TYPE_MIRROR));
    }

    @Test
    public void testGetAllDeclaredTypesOfInterfaces() {
        List<DeclaredType> declaredTypes = getAllDeclaredTypesOfInterfaces(testTypeMirror);
        assertDeclaredTypes(declaredTypes, ALL_SUPER_INTERFACES);
    }

    @Test
    public void testGetAllDeclaredTypesOfInterfacesOnNull() {
        assertEmptyList(getAllDeclaredTypesOfInterfaces(NULL_ELEMENT));
        assertEmptyList(getAllDeclaredTypesOfInterfaces(NULL_TYPE_MIRROR));
    }

    @Test
    public void testGetAllDeclaredTypesOfSuperTypes() {
        List<DeclaredType> declaredTypes = getAllDeclaredTypesOfSuperTypes(testTypeMirror);
        assertDeclaredTypes(declaredTypes, ALL_SUPER_TYPES);
    }

    @Test
    public void testGetAllDeclaredTypesOfSuperTypesOnNull() {
        assertEmptyList(getAllDeclaredTypesOfSuperTypes(NULL_ELEMENT));
        assertEmptyList(getAllDeclaredTypesOfSuperTypes(NULL_TYPE_MIRROR));
    }

    @Test
    public void testGetAllDeclaredTypes() {
        List<DeclaredType> declaredTypes = getAllDeclaredTypes(testTypeMirror);
        assertDeclaredTypes(declaredTypes, ALL_TYPES);
    }

    @Test
    public void testGetAllDeclaredTypesOnNull() {
        assertEmptyList(getAllDeclaredTypes(NULL_ELEMENT));
        assertEmptyList(getAllDeclaredTypes(NULL_TYPE_MIRROR));
    }

    @Test
    public void testGetDeclaredTypes() {
        // true true true true : all types
        List<DeclaredType> declaredTypes = getDeclaredTypes(testTypeElement, true, true, true, true);
        assertDeclaredTypes(declaredTypes, ALL_TYPES);
        assertEquals(getAllDeclaredTypes(testTypeElement), declaredTypes);

        // true true true false : self type + all super classes
        declaredTypes = getDeclaredTypes(testTypeElement, true, true, true, false);
        assertDeclaredTypes(declaredTypes, SELF_TYPE_PLUS_ALL_SUPER_CLASSES);

        // true true false true : self type + all super interfaces
        declaredTypes = getDeclaredTypes(testTypeElement, true, true, false, true);
        assertDeclaredTypes(declaredTypes, SELF_TYPE_PLUS_ALL_SUPER_INTERFACES);

        // true true false false : self type
        declaredTypes = getDeclaredTypes(testTypeElement, true, true, false, false);
        assertDeclaredTypes(declaredTypes, SELF_TYPE);

        // true false true true : self type + super class + super interfaces
        declaredTypes = getDeclaredTypes(testTypeElement, true, false, true, true);
        assertDeclaredTypes(declaredTypes, SELF_TYPE_PLUS_SUPER_CLASS_PLUS_SUPER_INTERFACES);

        // true false true false : self type + super class
        declaredTypes = getDeclaredTypes(testTypeElement, true, false, true, false);
        assertDeclaredTypes(declaredTypes, SELF_TYPE_PLUS_SUPER_CLASS);

        // true false false true : self type + super interfaces
        declaredTypes = getDeclaredTypes(testTypeElement, true, false, false, true);
        assertDeclaredTypes(declaredTypes, SELF_TYPE_PLUS_SUPER_INTERFACES);

        // true false false false : self type
        declaredTypes = getDeclaredTypes(testTypeElement, true, false, false, false);
        assertDeclaredTypes(declaredTypes, SELF_TYPE);

        // false true true true : all super types
        declaredTypes = getDeclaredTypes(testTypeElement, false, true, true, true);
        assertDeclaredTypes(declaredTypes, ALL_SUPER_TYPES);
        assertEquals(getAllDeclaredTypesOfSuperTypes(testTypeElement), declaredTypes);

        // false true true false : all super classes
        declaredTypes = getDeclaredTypes(testTypeElement, false, true, true, false);
        assertDeclaredTypes(declaredTypes, ALL_SUPER_CLASSES);
        assertEquals(getAllDeclaredTypesOfSuperclasses(testTypeElement), declaredTypes);

        // false true false true : all super interfaces
        declaredTypes = getDeclaredTypes(testTypeElement, false, true, false, true);
        assertDeclaredTypes(declaredTypes, ALL_SUPER_INTERFACES);
        assertEquals(getAllDeclaredTypesOfInterfaces(testTypeElement), declaredTypes);

        // false true false false : nothing
        declaredTypes = getDeclaredTypes(testTypeElement, false, true, false, false);
        assertDeclaredTypes(declaredTypes);
        assertEmptyList(declaredTypes);

        // false false true true : super class + super interfaces
        declaredTypes = getDeclaredTypes(testTypeElement, false, false, true, true);
        assertDeclaredTypes(declaredTypes, SUPER_TYPES);

        // false false true false : super class
        declaredTypes = getDeclaredTypes(testTypeElement, false, false, true, false);
        assertDeclaredTypes(declaredTypes, SUPER_CLASS);
        assertEquals(ofList(getDeclaredTypeOfSuperclass(testTypeElement)), declaredTypes);

        // false false false true : super interfaces
        declaredTypes = getDeclaredTypes(testTypeElement, false, false, false, true);
        assertDeclaredTypes(declaredTypes, SUPER_INTERFACES);
        assertEquals(ofList(getDeclaredTypesOfInterfaces(testTypeElement)), declaredTypes);

        // false false false false : nothing
        declaredTypes = getDeclaredTypes(testTypeElement, false, false, false, false);
        assertDeclaredTypes(declaredTypes);
        assertEmptyList(declaredTypes);
    }

    @Test
    public void testFindDeclaredTypesWithExcludedTypes() {
        List<DeclaredType> declaredTypes = findDeclaredTypes(testTypeElement, SUPER_CLASS);
        assertDeclaredTypes(declaredTypes, SUPER_INTERFACES);

        declaredTypes = findDeclaredTypes(testTypeElement, getTypeNames(SUPER_CLASS));
        assertDeclaredTypes(declaredTypes, SUPER_INTERFACES);

        declaredTypes = findDeclaredTypes(testTypeElement, SUPER_INTERFACES);
        assertDeclaredTypes(declaredTypes, SUPER_CLASS);

        declaredTypes = findDeclaredTypes(testTypeElement, getTypeNames(SUPER_INTERFACES));
        assertDeclaredTypes(declaredTypes, SUPER_CLASS);

        declaredTypes = findDeclaredTypes(testTypeMirror, SUPER_CLASS);
        assertDeclaredTypes(declaredTypes, SUPER_INTERFACES);

        declaredTypes = findDeclaredTypes(testTypeMirror, getTypeNames(SUPER_CLASS));
        assertDeclaredTypes(declaredTypes, SUPER_INTERFACES);

        declaredTypes = findDeclaredTypes(testTypeMirror, SUPER_INTERFACES);
        assertDeclaredTypes(declaredTypes, SUPER_CLASS);

        declaredTypes = findDeclaredTypes(testTypeMirror, getTypeNames(SUPER_INTERFACES));
        assertDeclaredTypes(declaredTypes, SUPER_CLASS);
    }

    @Test
    public void testFindDeclaredTypesWithExcludedTypesOnNull() {
        assertEmptyList(findDeclaredTypes(NULL_ELEMENT, NULL_TYPE_ARRAY));
        assertEmptyList(findDeclaredTypes(NULL_ELEMENT, EMPTY_TYPE_ARRAY));
        assertEmptyList(findDeclaredTypes(NULL_ELEMENT, ALL_TYPES));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, NULL_TYPE_ARRAY));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, EMPTY_TYPE_ARRAY));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, ALL_TYPES));
    }

    @Test
    public void testFindDeclaredTypesWithExcludedTypeNamesOnNull() {
        assertEmptyList(findDeclaredTypes(NULL_ELEMENT, NULL_STRING_ARRAY));
        assertEmptyList(findDeclaredTypes(NULL_ELEMENT, EMPTY_STRING_ARRAY));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, NULL_STRING_ARRAY));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, EMPTY_STRING_ARRAY));
    }

    @Test
    public void testFindDeclaredTypesOfInterfaces() {
        List<DeclaredType> declaredTypes = findDeclaredTypesOfInterfaces(testTypeMirror, alwaysTrue());
        assertDeclaredTypes(declaredTypes, SUPER_INTERFACES);

        findDeclaredTypesOfInterfaces(testTypeElement, alwaysTrue());
        assertDeclaredTypes(declaredTypes, SUPER_INTERFACES);

        declaredTypes = findDeclaredTypesOfInterfaces(testTypeMirror, alwaysFalse());
        assertEmptyList(declaredTypes);

        declaredTypes = findDeclaredTypesOfInterfaces(testTypeElement, alwaysFalse());
        assertEmptyList(declaredTypes);
    }

    @Test
    public void testFindDeclaredTypesOfInterfacesOnNull() {
        assertEmptyList(findDeclaredTypesOfInterfaces(NULL_ELEMENT, alwaysTrue()));
        assertEmptyList(findDeclaredTypesOfInterfaces(NULL_ELEMENT, alwaysFalse()));
        assertEmptyList(findDeclaredTypesOfInterfaces(NULL_TYPE_MIRROR, alwaysTrue()));
        assertEmptyList(findDeclaredTypesOfInterfaces(NULL_TYPE_MIRROR, alwaysTrue()));
    }

    @Test
    public void testFindAllDeclaredTypesOfSuperclasses() {
        List<DeclaredType> declaredTypes = findAllDeclaredTypesOfSuperclasses(testTypeElement, alwaysTrue());
        assertDeclaredTypes(declaredTypes, ALL_SUPER_CLASSES);

        declaredTypes = findAllDeclaredTypesOfSuperclasses(testTypeMirror, alwaysFalse());
        assertEmptyList(declaredTypes);
    }

    @Test
    public void testFindAllDeclaredTypesOfSuperclassesOnNull() {
        assertEmptyList(findAllDeclaredTypesOfSuperclasses(NULL_ELEMENT, alwaysTrue()));
        assertEmptyList(findAllDeclaredTypesOfSuperclasses(NULL_ELEMENT, alwaysFalse()));
        assertEmptyList(findAllDeclaredTypesOfSuperclasses(NULL_TYPE_MIRROR, alwaysTrue()));
        assertEmptyList(findAllDeclaredTypesOfSuperclasses(NULL_TYPE_MIRROR, alwaysFalse()));
    }

    @Test
    public void testFindAllDeclaredTypesOfInterfaces() {
        List<DeclaredType> declaredTypes = findAllDeclaredTypesOfInterfaces(testTypeElement, alwaysTrue());
        assertDeclaredTypes(declaredTypes, ALL_SUPER_INTERFACES);

        declaredTypes = findAllDeclaredTypesOfInterfaces(testTypeMirror, alwaysFalse());
        assertEmptyList(declaredTypes);
    }

    @Test
    public void testFindAllDeclaredTypesOfInterfacesOnNull() {
        assertEmptyList(findAllDeclaredTypesOfInterfaces(NULL_ELEMENT, alwaysTrue()));
        assertEmptyList(findAllDeclaredTypesOfInterfaces(NULL_ELEMENT, alwaysFalse()));
        assertEmptyList(findAllDeclaredTypesOfInterfaces(NULL_TYPE_MIRROR, alwaysTrue()));
        assertEmptyList(findAllDeclaredTypesOfInterfaces(NULL_TYPE_MIRROR, alwaysFalse()));
    }

    @Test
    public void testFindAllDeclaredTypesOfSuperTypes() {
        List<DeclaredType> declaredTypes = findAllDeclaredTypesOfSuperTypes(testTypeMirror, alwaysTrue());
        assertDeclaredTypes(declaredTypes, ALL_SUPER_TYPES);

        findAllDeclaredTypesOfSuperTypes(testTypeElement, alwaysTrue());
        assertDeclaredTypes(declaredTypes, ALL_SUPER_TYPES);

        declaredTypes = findAllDeclaredTypesOfSuperTypes(testTypeMirror, alwaysFalse());
        assertEmptyList(declaredTypes);

        declaredTypes = findAllDeclaredTypesOfSuperTypes(testTypeElement, alwaysFalse());
        assertEmptyList(declaredTypes);
    }

    @Test
    public void testFindAllDeclaredTypesOfSuperTypesOnNull() {
        assertEmptyList(findAllDeclaredTypesOfSuperTypes(NULL_ELEMENT));
        assertEmptyList(findAllDeclaredTypesOfSuperTypes(NULL_TYPE_MIRROR));
    }

    @Test
    public void testFindAllDeclaredTypes() {
        List<DeclaredType> declaredTypes = findAllDeclaredTypes(testTypeElement, alwaysTrue());
        assertDeclaredTypes(declaredTypes, ALL_TYPES);

        declaredTypes = findAllDeclaredTypes(testTypeMirror, alwaysFalse());
        assertEmptyList(declaredTypes);
    }

    @Test
    public void testFindAllDeclaredTypesOnNull() {
        assertEmptyList(findAllDeclaredTypes(NULL_ELEMENT, alwaysTrue()));
        assertEmptyList(findAllDeclaredTypes(NULL_ELEMENT, alwaysFalse()));
        assertEmptyList(findAllDeclaredTypes(NULL_TYPE_MIRROR, alwaysTrue()));
        assertEmptyList(findAllDeclaredTypes(NULL_TYPE_MIRROR, alwaysFalse()));
    }

    @Test
    public void testFindAllDeclaredTypesWithExcludedTypes() {
        List<DeclaredType> declaredTypes = findAllDeclaredTypes(testTypeElement, testClass);
        assertDeclaredTypes(declaredTypes, ALL_SUPER_TYPES);


        declaredTypes = findAllDeclaredTypes(testTypeMirror, testClass);
        assertDeclaredTypes(declaredTypes, ALL_SUPER_TYPES);
    }

    @Test
    public void testFindAllDeclaredTypesWithExcludedTypeNames() {
        List<DeclaredType> declaredTypes = findAllDeclaredTypes(testTypeElement, testClassName);
        assertDeclaredTypes(declaredTypes, ALL_SUPER_TYPES);

        declaredTypes = findAllDeclaredTypes(testTypeMirror, testClassName);
        assertDeclaredTypes(declaredTypes, ALL_SUPER_TYPES);
    }

    @Test
    public void testFindAllDeclaredTypesWithExcludedTypesOnNull() {
        assertEmptyList(findAllDeclaredTypes(NULL_ELEMENT, NULL_TYPE_ARRAY));
        assertEmptyList(findAllDeclaredTypes(NULL_ELEMENT, EMPTY_TYPE_ARRAY));
        assertEmptyList(findAllDeclaredTypes(NULL_ELEMENT, ALL_TYPES));
        assertEmptyList(findAllDeclaredTypes(NULL_TYPE_MIRROR, NULL_TYPE_ARRAY));
        assertEmptyList(findAllDeclaredTypes(NULL_TYPE_MIRROR, EMPTY_TYPE_ARRAY));
        assertEmptyList(findAllDeclaredTypes(NULL_TYPE_MIRROR, ALL_TYPES));
    }

    @Test
    public void testFindAllDeclaredTypesWithExcludedTypeNamesOnNull() {
        assertEmptyList(findAllDeclaredTypes(NULL_ELEMENT, NULL_STRING_ARRAY));
        assertEmptyList(findAllDeclaredTypes(NULL_ELEMENT, EMPTY_STRING_ARRAY));
        assertEmptyList(findAllDeclaredTypes(NULL_TYPE_MIRROR, NULL_STRING_ARRAY));
        assertEmptyList(findAllDeclaredTypes(NULL_TYPE_MIRROR, EMPTY_STRING_ARRAY));
    }

    @Test
    public void testFindDeclaredTypes() {
        // true true true true : all types
        List<DeclaredType> declaredTypes = findDeclaredTypes(testTypeElement, true, true, true, true, alwaysTrue());
        assertDeclaredTypes(declaredTypes, ALL_TYPES);
        assertEquals(getAllDeclaredTypes(testTypeElement), declaredTypes);

        // true true true false : self type + all super classes
        declaredTypes = findDeclaredTypes(testTypeElement, true, true, true, false, alwaysTrue());
        assertDeclaredTypes(declaredTypes, SELF_TYPE_PLUS_ALL_SUPER_CLASSES);

        // true true false true : self type + all super interfaces
        declaredTypes = findDeclaredTypes(testTypeElement, true, true, false, true, alwaysTrue());
        assertDeclaredTypes(declaredTypes, SELF_TYPE_PLUS_ALL_SUPER_INTERFACES);

        // true true false false : self type
        declaredTypes = findDeclaredTypes(testTypeElement, true, true, false, false, alwaysTrue());
        assertDeclaredTypes(declaredTypes, SELF_TYPE);

        // true false true true : self type + super class + super interfaces
        declaredTypes = findDeclaredTypes(testTypeElement, true, false, true, true, alwaysTrue());
        assertDeclaredTypes(declaredTypes, SELF_TYPE_PLUS_SUPER_CLASS_PLUS_SUPER_INTERFACES);

        // true false true false : self type + super class
        declaredTypes = findDeclaredTypes(testTypeElement, true, false, true, false, alwaysTrue());
        assertDeclaredTypes(declaredTypes, SELF_TYPE_PLUS_SUPER_CLASS);

        // true false false true : self type + super interfaces
        declaredTypes = findDeclaredTypes(testTypeElement, true, false, false, true, alwaysTrue());
        assertDeclaredTypes(declaredTypes, SELF_TYPE_PLUS_SUPER_INTERFACES);

        // true false false false : self type
        declaredTypes = findDeclaredTypes(testTypeElement, true, false, false, false, alwaysTrue());
        assertDeclaredTypes(declaredTypes, SELF_TYPE);

        // false true true true : all super types
        declaredTypes = findDeclaredTypes(testTypeElement, false, true, true, true, alwaysTrue());
        assertDeclaredTypes(declaredTypes, ALL_SUPER_TYPES);
        assertEquals(getAllDeclaredTypesOfSuperTypes(testTypeElement), declaredTypes);

        // false true true false : all super classes
        declaredTypes = findDeclaredTypes(testTypeElement, false, true, true, false, alwaysTrue());
        assertDeclaredTypes(declaredTypes, ALL_SUPER_CLASSES);
        assertEquals(getAllDeclaredTypesOfSuperclasses(testTypeElement), declaredTypes);

        // false true false true : all super interfaces
        declaredTypes = findDeclaredTypes(testTypeElement, false, true, false, true, alwaysTrue());
        assertDeclaredTypes(declaredTypes, ALL_SUPER_INTERFACES);
        assertEquals(getAllDeclaredTypesOfInterfaces(testTypeElement), declaredTypes);

        // false true false false : nothing
        declaredTypes = findDeclaredTypes(testTypeElement, false, true, false, false, alwaysTrue());
        assertDeclaredTypes(declaredTypes);
        assertEmptyList(declaredTypes);

        // false false true true : super class + super interfaces
        declaredTypes = findDeclaredTypes(testTypeElement, false, false, true, true, alwaysTrue());
        assertDeclaredTypes(declaredTypes, SUPER_TYPES);

        // false false true false : super class
        declaredTypes = findDeclaredTypes(testTypeElement, false, false, true, false, alwaysTrue());
        assertDeclaredTypes(declaredTypes, SUPER_CLASS);
        assertEquals(ofList(getDeclaredTypeOfSuperclass(testTypeElement)), declaredTypes);

        // false false false true : super interfaces
        declaredTypes = findDeclaredTypes(testTypeElement, false, false, false, true, alwaysTrue());
        assertDeclaredTypes(declaredTypes, SUPER_INTERFACES);
        assertEquals(ofList(getDeclaredTypesOfInterfaces(testTypeElement)), declaredTypes);

        // false false false false : nothing
        declaredTypes = findDeclaredTypes(testTypeElement, false, false, false, false, alwaysTrue());
        assertDeclaredTypes(declaredTypes);
        assertEmptyList(declaredTypes);
    }

    @Test
    public void testFindDeclaredTypesOnNull() {
        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, true, true, true, true, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, true, true, true, true, alwaysFalse()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, true, true, true, true, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, true, true, true, true, alwaysFalse()));

        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, true, true, true, false, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, true, true, true, false, alwaysFalse()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, true, true, true, false, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, true, true, true, false, alwaysFalse()));

        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, true, true, false, true, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, true, true, false, true, alwaysFalse()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, true, true, false, true, alwaysFalse()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, true, true, false, true, alwaysFalse()));

        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, true, true, false, false, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, true, true, false, false, alwaysFalse()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, true, true, false, false, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, true, true, false, false, alwaysFalse()));

        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, true, false, true, true, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, true, false, true, true, alwaysFalse()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, true, false, true, true, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, true, false, true, true, alwaysFalse()));

        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, true, false, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, true, false, alwaysFalse()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, true, false, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, true, false, alwaysFalse()));

        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, false, true, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, false, true, alwaysFalse()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, false, true, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, false, true, alwaysFalse()));

        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, false, false, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, false, false, alwaysFalse()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, false, false, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, false, false, alwaysFalse()));

        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, false, true, true, true, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, false, true, true, true, alwaysFalse()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, false, true, true, true, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, false, true, true, true, alwaysFalse()));

        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, false, true, true, false, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, false, true, true, false, alwaysFalse()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, false, true, true, false, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, false, true, true, false, alwaysFalse()));

        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, false, true, false, true, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, false, true, false, true, alwaysFalse()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, false, true, false, true, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, false, true, false, true, alwaysFalse()));

        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, false, true, false, false, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, false, true, false, false, alwaysFalse()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, false, true, false, false, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, false, true, false, false, alwaysFalse()));

        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, true, true, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, true, true, alwaysFalse()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, true, true, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, true, true, alwaysFalse()));

        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, true, false, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, true, false, alwaysFalse()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, true, false, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, true, false, alwaysFalse()));

        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, false, true, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, false, true, alwaysFalse()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, false, true, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, false, true, alwaysFalse()));

        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, false, false, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, false, false, alwaysFalse()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, false, false, alwaysTrue()));
        assertEmptyList(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, false, false, alwaysFalse()));
    }

    @Test
    public void testGetTypeMirrorsOfInterfaces() {
        List<TypeMirror> typeMirrors = getTypeMirrorsOfInterfaces(testTypeMirror);
        assertTypeMirrors(typeMirrors, SUPER_INTERFACES);

        typeMirrors = getTypeMirrorsOfInterfaces(testTypeElement);
        assertTypeMirrors(typeMirrors, SUPER_INTERFACES);

        typeMirrors = getTypeMirrorsOfInterfaces(getTypeElement(Object.class));
        assertEmptyList(typeMirrors);

        typeMirrors = getTypeMirrorsOfInterfaces(getTypeMirror(Object.class));
        assertSame(typeMirrors, typeMirrors);
    }

    @Test
    public void testGetTypeMirrorsOfInterfacesOnNull() {
        List<TypeMirror> typeMirrors = getTypeMirrorsOfInterfaces(NULL_TYPE_MIRROR);
        assertEmptyList(typeMirrors);

        typeMirrors = getTypeMirrorsOfInterfaces(NULL_TYPE_ELEMENT);
        assertEmptyList(typeMirrors);
    }

    @Test
    public void testFindTypeMirrorsOfInterfaces() {
        List<TypeMirror> typeMirrors = findTypeMirrorsOfInterfaces(testTypeMirror, alwaysTrue());
        assertTypeMirrors(typeMirrors, SUPER_INTERFACES);

        typeMirrors = findTypeMirrorsOfInterfaces(testTypeElement, alwaysTrue());
        assertTypeMirrors(typeMirrors, SUPER_INTERFACES);

        typeMirrors = findTypeMirrorsOfInterfaces(testTypeMirror, alwaysFalse());
        assertEmptyList(typeMirrors);

        typeMirrors = findTypeMirrorsOfInterfaces(testTypeElement, alwaysFalse());
        assertEmptyList(typeMirrors);

        typeMirrors = findTypeMirrorsOfInterfaces(getTypeElement(Object.class), alwaysTrue());
        assertEmptyList(typeMirrors);

        typeMirrors = findTypeMirrorsOfInterfaces(getTypeElement(Object.class), alwaysFalse());
        assertEmptyList(typeMirrors);

        typeMirrors = findTypeMirrorsOfInterfaces(getTypeMirror(Object.class), alwaysTrue());
        assertSame(typeMirrors, typeMirrors);

        typeMirrors = findTypeMirrorsOfInterfaces(getTypeMirror(Object.class), alwaysFalse());
        assertSame(typeMirrors, typeMirrors);
    }

    @Test
    public void testFindTypeMirrorsOfInterfacesOnNull() {
        List<TypeMirror> typeMirrors = findTypeMirrorsOfInterfaces(NULL_TYPE_MIRROR, alwaysTrue());
        assertEmptyList(typeMirrors);

        typeMirrors = findTypeMirrorsOfInterfaces(NULL_TYPE_ELEMENT, alwaysTrue());
        assertEmptyList(typeMirrors);

        typeMirrors = findTypeMirrorsOfInterfaces(NULL_TYPE_MIRROR, alwaysFalse());
        assertEmptyList(typeMirrors);

        typeMirrors = findTypeMirrorsOfInterfaces(NULL_TYPE_ELEMENT, alwaysFalse());
        assertEmptyList(typeMirrors);
    }

    @Test
    public void testGetAllTypeMirrorsOfInterfaces() {
        List<TypeMirror> typeMirrors = getAllTypeMirrorsOfInterfaces(testTypeMirror);
        assertTypeMirrors(typeMirrors, ALL_SUPER_INTERFACES);

        typeMirrors = getAllTypeMirrorsOfInterfaces(testTypeElement);
        assertTypeMirrors(typeMirrors, ALL_SUPER_INTERFACES);

        typeMirrors = getAllTypeMirrorsOfInterfaces(getTypeElement(Object.class));
        assertEmptyList(typeMirrors);

        typeMirrors = getAllTypeMirrorsOfInterfaces(getTypeMirror(Object.class));
        assertSame(typeMirrors, typeMirrors);
    }

    @Test
    public void testGetAllTypeMirrorsOfInterfacesOnNull() {
        List<TypeMirror> typeMirrors = getAllTypeMirrorsOfInterfaces(NULL_TYPE_MIRROR);
        assertEmptyList(typeMirrors);

        typeMirrors = getAllTypeMirrorsOfInterfaces(NULL_TYPE_ELEMENT);
        assertEmptyList(typeMirrors);
    }

    @Test
    public void testFindAllTypeMirrorsOfInterfaces() {
        List<TypeMirror> typeMirrors = findAllTypeMirrorsOfInterfaces(testTypeMirror, alwaysTrue());
        assertTypeMirrors(typeMirrors, ALL_SUPER_INTERFACES);

        typeMirrors = findAllTypeMirrorsOfInterfaces(testTypeElement, alwaysTrue());
        assertTypeMirrors(typeMirrors, ALL_SUPER_INTERFACES);

        typeMirrors = findAllTypeMirrorsOfInterfaces(testTypeMirror, alwaysFalse());
        assertEmptyList(typeMirrors);

        typeMirrors = findAllTypeMirrorsOfInterfaces(testTypeElement, alwaysFalse());
        assertEmptyList(typeMirrors);

        typeMirrors = findAllTypeMirrorsOfInterfaces(getTypeElement(Object.class), alwaysTrue());
        assertEmptyList(typeMirrors);

        typeMirrors = findAllTypeMirrorsOfInterfaces(getTypeElement(Object.class), alwaysFalse());
        assertEmptyList(typeMirrors);

        typeMirrors = findAllTypeMirrorsOfInterfaces(getTypeMirror(Object.class), alwaysTrue());
        assertSame(typeMirrors, typeMirrors);

        typeMirrors = findAllTypeMirrorsOfInterfaces(getTypeMirror(Object.class), alwaysFalse());
        assertSame(typeMirrors, typeMirrors);
    }

    @Test
    public void testFindAllTypeMirrorsOfInterfacesOnNull() {
        List<TypeMirror> typeMirrors = findAllTypeMirrorsOfInterfaces(NULL_TYPE_MIRROR, alwaysTrue());
        assertEmptyList(typeMirrors);

        typeMirrors = findAllTypeMirrorsOfInterfaces(NULL_TYPE_MIRROR, alwaysFalse());
        assertEmptyList(typeMirrors);

        typeMirrors = findAllTypeMirrorsOfInterfaces(NULL_TYPE_ELEMENT, alwaysTrue());
        assertEmptyList(typeMirrors);

        typeMirrors = findAllTypeMirrorsOfInterfaces(NULL_TYPE_MIRROR, alwaysFalse());
        assertEmptyList(typeMirrors);
    }

    @Test
    public void testFindInterfaceTypeMirror() {
        for (Type interfaceType : ALL_SUPER_INTERFACES) {
            assertInterfaceTypeMirror(testTypeElement, interfaceType);
        }

        for (Type superClass : ALL_SUPER_CLASSES) {
            assertNull(findInterfaceTypeMirror(testTypeElement, superClass));
            assertNull(findInterfaceTypeMirror(testTypeMirror, superClass));
        }
    }

    @Test
    public void testFindInterfaceTypeMirrorOnNull() {
        for (Type type : ALL_TYPES) {
            assertNull(findInterfaceTypeMirror(NULL_ELEMENT, type));
            assertNull(findInterfaceTypeMirror(NULL_TYPE_MIRROR, type));
        }
    }

    @Test
    public void testGetTypeMirrors() {
        assertGetTypeMirrors(NULL_TYPE);
        assertGetTypeMirrors(SELF_TYPE);
        assertGetTypeMirrors(SUPER_CLASS);
        assertGetTypeMirrors(ALL_TYPES);
        assertGetTypeMirrors(ALL_SUPER_TYPES);
        assertGetTypeMirrors(ALL_SUPER_CLASSES);
        assertGetTypeMirrors(ALL_SUPER_INTERFACES);
        assertGetTypeMirrors(SUPER_INTERFACES);
        assertGetTypeMirrors(SUPER_TYPES);

        assertGetTypeMirrors(SELF_TYPE_PLUS_ALL_SUPER_TYPES);
        assertGetTypeMirrors(SELF_TYPE_PLUS_ALL_SUPER_CLASSES);
        assertGetTypeMirrors(SELF_TYPE_PLUS_ALL_SUPER_INTERFACES);
        assertGetTypeMirrors(SELF_TYPE_PLUS_SUPER_CLASS);
        assertGetTypeMirrors(SELF_TYPE_PLUS_SUPER_INTERFACES);
        assertGetTypeMirrors(SELF_TYPE_PLUS_SUPER_CLASS_PLUS_SUPER_INTERFACES);
    }

    @Test
    public void testGetTypeMirrorsOnNull() {
        assertGetTypeMirrors(NULL_TYPE);
        assertGetTypeMirrors(NULL_TYPE_ARRAY);
        assertGetTypeMirrors(EMPTY_TYPE_ARRAY);

        TypeUtils.getTypeMirrors(NULL_PROCESSING_ENVIRONMENT, SELF_TYPE);
        TypeUtils.getTypeMirrors(NULL_PROCESSING_ENVIRONMENT, SUPER_CLASS);
        TypeUtils.getTypeMirrors(NULL_PROCESSING_ENVIRONMENT, ALL_TYPES);
        TypeUtils.getTypeMirrors(NULL_PROCESSING_ENVIRONMENT, ALL_SUPER_TYPES);
        TypeUtils.getTypeMirrors(NULL_PROCESSING_ENVIRONMENT, ALL_SUPER_CLASSES);
        TypeUtils.getTypeMirrors(NULL_PROCESSING_ENVIRONMENT, ALL_SUPER_INTERFACES);
        TypeUtils.getTypeMirrors(NULL_PROCESSING_ENVIRONMENT, SUPER_INTERFACES);
        TypeUtils.getTypeMirrors(NULL_PROCESSING_ENVIRONMENT, SUPER_TYPES);

        TypeUtils.getTypeMirrors(NULL_PROCESSING_ENVIRONMENT, SELF_TYPE_PLUS_ALL_SUPER_TYPES);
        TypeUtils.getTypeMirrors(NULL_PROCESSING_ENVIRONMENT, SELF_TYPE_PLUS_ALL_SUPER_CLASSES);
        TypeUtils.getTypeMirrors(NULL_PROCESSING_ENVIRONMENT, SELF_TYPE_PLUS_ALL_SUPER_INTERFACES);
        TypeUtils.getTypeMirrors(NULL_PROCESSING_ENVIRONMENT, SELF_TYPE_PLUS_SUPER_CLASS);
        TypeUtils.getTypeMirrors(NULL_PROCESSING_ENVIRONMENT, SELF_TYPE_PLUS_SUPER_INTERFACES);
        TypeUtils.getTypeMirrors(NULL_PROCESSING_ENVIRONMENT, SELF_TYPE_PLUS_SUPER_CLASS_PLUS_SUPER_INTERFACES);
    }

    @Test
    public void testGetTypeMirror() {
        assertGetTypeMirror(NULL_TYPE);
        assertGetTypeMirror(SELF_TYPE);
        assertGetTypeMirror(SUPER_CLASS);
        assertGetTypeMirror(ALL_TYPES);
        assertGetTypeMirror(ALL_SUPER_TYPES);
        assertGetTypeMirror(ALL_SUPER_CLASSES);
        assertGetTypeMirror(ALL_SUPER_INTERFACES);
        assertGetTypeMirror(SUPER_INTERFACES);
        assertGetTypeMirror(SUPER_TYPES);

        assertGetTypeMirror(SELF_TYPE_PLUS_ALL_SUPER_TYPES);
        assertGetTypeMirror(SELF_TYPE_PLUS_ALL_SUPER_CLASSES);
        assertGetTypeMirror(SELF_TYPE_PLUS_ALL_SUPER_INTERFACES);
        assertGetTypeMirror(SELF_TYPE_PLUS_SUPER_CLASS);
        assertGetTypeMirror(SELF_TYPE_PLUS_SUPER_INTERFACES);
        assertGetTypeMirror(SELF_TYPE_PLUS_SUPER_CLASS_PLUS_SUPER_INTERFACES);
    }

    @Test
    public void testGetTypeMirrorOnNull() {
        assertNull(TypeUtils.getTypeMirror(this.processingEnv, NULL_TYPE));
        assertNull(getTypeMirror(NULL_TYPE));

        assertGetTypeMirrorOnNullProcessingEnvironment(SELF_TYPE);
        assertGetTypeMirrorOnNullProcessingEnvironment(SUPER_CLASS);
        assertGetTypeMirrorOnNullProcessingEnvironment(ALL_TYPES);
        assertGetTypeMirrorOnNullProcessingEnvironment(ALL_SUPER_TYPES);
        assertGetTypeMirrorOnNullProcessingEnvironment(ALL_SUPER_CLASSES);
        assertGetTypeMirrorOnNullProcessingEnvironment(ALL_SUPER_INTERFACES);
        assertGetTypeMirrorOnNullProcessingEnvironment(SUPER_INTERFACES);
        assertGetTypeMirrorOnNullProcessingEnvironment(SUPER_TYPES);

        assertGetTypeMirrorOnNullProcessingEnvironment(SELF_TYPE_PLUS_ALL_SUPER_TYPES);
        assertGetTypeMirrorOnNullProcessingEnvironment(SELF_TYPE_PLUS_ALL_SUPER_CLASSES);
        assertGetTypeMirrorOnNullProcessingEnvironment(SELF_TYPE_PLUS_ALL_SUPER_INTERFACES);
        assertGetTypeMirrorOnNullProcessingEnvironment(SELF_TYPE_PLUS_SUPER_CLASS);
        assertGetTypeMirrorOnNullProcessingEnvironment(SELF_TYPE_PLUS_SUPER_INTERFACES);
        assertGetTypeMirrorOnNullProcessingEnvironment(SELF_TYPE_PLUS_SUPER_CLASS_PLUS_SUPER_INTERFACES);
    }

    @Test
    public void testGetTypeElementsWithProcessingEnvironment() {
        assertGetTypeElementsWithProcessingEnvironment(SELF_TYPE);
        assertGetTypeElementsWithProcessingEnvironment(SUPER_CLASS);
        assertGetTypeElementsWithProcessingEnvironment(ALL_TYPES);
        assertGetTypeElementsWithProcessingEnvironment(ALL_SUPER_TYPES);
        assertGetTypeElementsWithProcessingEnvironment(ALL_SUPER_CLASSES);
        assertGetTypeElementsWithProcessingEnvironment(ALL_SUPER_INTERFACES);
        assertGetTypeElementsWithProcessingEnvironment(SUPER_INTERFACES);
        assertGetTypeElementsWithProcessingEnvironment(SUPER_TYPES);

        assertGetTypeElementsWithProcessingEnvironment(SELF_TYPE_PLUS_ALL_SUPER_TYPES);
        assertGetTypeElementsWithProcessingEnvironment(SELF_TYPE_PLUS_ALL_SUPER_CLASSES);
        assertGetTypeElementsWithProcessingEnvironment(SELF_TYPE_PLUS_ALL_SUPER_INTERFACES);
        assertGetTypeElementsWithProcessingEnvironment(SELF_TYPE_PLUS_SUPER_CLASS);
        assertGetTypeElementsWithProcessingEnvironment(SELF_TYPE_PLUS_SUPER_INTERFACES);
        assertGetTypeElementsWithProcessingEnvironment(SELF_TYPE_PLUS_SUPER_CLASS_PLUS_SUPER_INTERFACES);
    }

    @Test
    public void testGetTypeElementsWithProcessingEnvironmentOnNull() {
        assertEmptyList(TypeUtils.getTypeElements(this.processingEnv, NULL_TYPE));
        assertEmptyList(TypeUtils.getTypeElements(this.processingEnv, NULL_TYPE_ARRAY));
        assertEmptyList(TypeUtils.getTypeElements(this.processingEnv, EMPTY_TYPE_ARRAY));

        assertGetTypeElementsOnNullProcessingEnvironment(SELF_TYPE);
        assertGetTypeElementsOnNullProcessingEnvironment(SUPER_CLASS);
        assertGetTypeElementsOnNullProcessingEnvironment(ALL_TYPES);
        assertGetTypeElementsOnNullProcessingEnvironment(ALL_SUPER_TYPES);
        assertGetTypeElementsOnNullProcessingEnvironment(ALL_SUPER_CLASSES);
        assertGetTypeElementsOnNullProcessingEnvironment(ALL_SUPER_INTERFACES);
        assertGetTypeElementsOnNullProcessingEnvironment(SUPER_INTERFACES);
        assertGetTypeElementsOnNullProcessingEnvironment(SUPER_TYPES);

        assertGetTypeElementsOnNullProcessingEnvironment(SELF_TYPE_PLUS_ALL_SUPER_TYPES);
        assertGetTypeElementsOnNullProcessingEnvironment(SELF_TYPE_PLUS_ALL_SUPER_CLASSES);
        assertGetTypeElementsOnNullProcessingEnvironment(SELF_TYPE_PLUS_ALL_SUPER_INTERFACES);
        assertGetTypeElementsOnNullProcessingEnvironment(SELF_TYPE_PLUS_SUPER_CLASS);
        assertGetTypeElementsOnNullProcessingEnvironment(SELF_TYPE_PLUS_SUPER_INTERFACES);
        assertGetTypeElementsOnNullProcessingEnvironment(SELF_TYPE_PLUS_SUPER_CLASS_PLUS_SUPER_INTERFACES);
    }

    @Test
    public void testGetTypeElement() {
        assertEquals(testTypeElement, TypeUtils.getTypeElement(processingEnv, testClassName));
    }

    @Test
    public void testGetTypeElementOnTypeMirror() {
        assertEquals(testTypeElement, TypeUtils.getTypeElement(processingEnv, testTypeMirror));
    }

    @Test
    public void testGetTypeElementOnType() {
        assertEquals(testTypeElement, TypeUtils.getTypeElement(processingEnv, SELF_TYPE));
    }

    @Test
    public void testGetTypeElementOnNull() {
        assertNull(TypeUtils.getTypeElement(processingEnv, NULL_TYPE));
        assertNull(TypeUtils.getTypeElement(processingEnv, NULL_TYPE_MIRROR));
        assertNull(TypeUtils.getTypeElement(processingEnv, NULL_STRING));
        assertNull(TypeUtils.getTypeElement(NULL_PROCESSING_ENVIRONMENT, NULL_STRING));
    }

    @Test
    public void testGetDeclaredType() {
        assertGetDeclaredType(NULL_TYPE);
        assertGetDeclaredType(SELF_TYPE);
        assertGetDeclaredType(SUPER_CLASS);
        assertGetDeclaredType(ALL_TYPES);
        assertGetDeclaredType(ALL_SUPER_TYPES);
        assertGetDeclaredType(ALL_SUPER_CLASSES);
        assertGetDeclaredType(ALL_SUPER_INTERFACES);
        assertGetDeclaredType(SUPER_INTERFACES);
        assertGetDeclaredType(SUPER_TYPES);

        assertGetDeclaredType(SELF_TYPE_PLUS_ALL_SUPER_TYPES);
        assertGetDeclaredType(SELF_TYPE_PLUS_ALL_SUPER_CLASSES);
        assertGetDeclaredType(SELF_TYPE_PLUS_ALL_SUPER_INTERFACES);
        assertGetDeclaredType(SELF_TYPE_PLUS_SUPER_CLASS);
        assertGetDeclaredType(SELF_TYPE_PLUS_SUPER_INTERFACES);
        assertGetDeclaredType(SELF_TYPE_PLUS_SUPER_CLASS_PLUS_SUPER_INTERFACES);
    }

    @Test
    public void testGetDeclaredTypeOnNull() {
        assertNull(TypeUtils.getDeclaredType(this.processingEnv, NULL_TYPE));
        assertNull(TypeUtils.getDeclaredType(this.processingEnv, NULL_TYPE_MIRROR));
        assertNull(TypeUtils.getDeclaredType(this.processingEnv, NULL_STRING));

        assertNull(getDeclaredType(NULL_TYPE));

        assertGetDeclaredTypeOnNullProcessingEnvironment(SELF_TYPE);
        assertGetDeclaredTypeOnNullProcessingEnvironment(SUPER_CLASS);
        assertGetDeclaredTypeOnNullProcessingEnvironment(ALL_TYPES);
        assertGetDeclaredTypeOnNullProcessingEnvironment(ALL_SUPER_TYPES);
        assertGetDeclaredTypeOnNullProcessingEnvironment(ALL_SUPER_CLASSES);
        assertGetDeclaredTypeOnNullProcessingEnvironment(ALL_SUPER_INTERFACES);
        assertGetDeclaredTypeOnNullProcessingEnvironment(SUPER_INTERFACES);
        assertGetDeclaredTypeOnNullProcessingEnvironment(SUPER_TYPES);

        assertGetDeclaredTypeOnNullProcessingEnvironment(SELF_TYPE_PLUS_ALL_SUPER_TYPES);
        assertGetDeclaredTypeOnNullProcessingEnvironment(SELF_TYPE_PLUS_ALL_SUPER_CLASSES);
        assertGetDeclaredTypeOnNullProcessingEnvironment(SELF_TYPE_PLUS_ALL_SUPER_INTERFACES);
        assertGetDeclaredTypeOnNullProcessingEnvironment(SELF_TYPE_PLUS_SUPER_CLASS);
        assertGetDeclaredTypeOnNullProcessingEnvironment(SELF_TYPE_PLUS_SUPER_INTERFACES);
        assertGetDeclaredTypeOnNullProcessingEnvironment(SELF_TYPE_PLUS_SUPER_CLASS_PLUS_SUPER_INTERFACES);
    }

    @Test
    public void testToStringOnClasses() {
        assertToStringOnClasses();
    }

    @Test
    public void testToStringOnArrayTypes() {
        assertToStringOnArrayTypes();
    }

    @Test
    public void testToStringOnCollectionTypes() {
        assertToStringOnCollectionTypes();
    }

    @Test
    public void testToStringMapTypes() {
        assertToStringOnMapTypes();
    }

    @Test
    public void testToStringOnNull() {
        assertNull(TypeUtils.toString(NULL_TYPE_MIRROR));
    }

    @Test
    public void testTypeElementFinderOnNull() {
        assertThrows(IllegalArgumentException.class, () -> typeElementFinder(NULL_TYPE_ELEMENT, true, true, true, true));

        assertThrows(IllegalArgumentException.class, () -> typeElementFinder(NULL_TYPE_ELEMENT, true, true, true, false));

        assertThrows(IllegalArgumentException.class, () -> typeElementFinder(NULL_TYPE_ELEMENT, true, true, false, true));

        assertThrows(IllegalArgumentException.class, () -> typeElementFinder(NULL_TYPE_ELEMENT, true, true, false, false));

        assertThrows(IllegalArgumentException.class, () -> typeElementFinder(NULL_TYPE_ELEMENT, true, false, true, true));

        assertThrows(IllegalArgumentException.class, () -> typeElementFinder(NULL_TYPE_ELEMENT, false, false, true, false));

        assertThrows(IllegalArgumentException.class, () -> typeElementFinder(NULL_TYPE_ELEMENT, false, false, false, true));

        assertThrows(IllegalArgumentException.class, () -> typeElementFinder(NULL_TYPE_ELEMENT, false, false, false, false));

        assertThrows(IllegalArgumentException.class, () -> typeElementFinder(NULL_TYPE_ELEMENT, false, true, true, true));

        assertThrows(IllegalArgumentException.class, () -> typeElementFinder(NULL_TYPE_ELEMENT, false, true, true, false));

        assertThrows(IllegalArgumentException.class, () -> typeElementFinder(NULL_TYPE_ELEMENT, false, true, false, true));

        assertThrows(IllegalArgumentException.class, () -> typeElementFinder(NULL_TYPE_ELEMENT, false, true, false, false));

        assertThrows(IllegalArgumentException.class, () -> typeElementFinder(NULL_TYPE_ELEMENT, false, false, true, true));

        assertThrows(IllegalArgumentException.class, () -> typeElementFinder(NULL_TYPE_ELEMENT, false, false, true, false));

        assertThrows(IllegalArgumentException.class, () -> typeElementFinder(NULL_TYPE_ELEMENT, false, false, false, true));

        assertThrows(IllegalArgumentException.class, () -> typeElementFinder(NULL_TYPE_ELEMENT, false, false, false, false));
    }

    private void assertIsSameType(Element typeElement, Type type) {
        assertTrue(isSameType(typeElement, type));
        assertTrue(isSameType(typeElement, type.getTypeName()));
        assertTrue(isSameType(typeElement.asType(), type));
        assertTrue(isSameType(typeElement.asType(), type.getTypeName()));
    }

    private void assertOfTypeMirrors(Class<?>... types) {
        Element[] elements = getElements(types);
        assertEquals(getTypeMirrors(types), ofTypeMirrors(elements));
    }

    private void assertOfTypeElements(Class<?>... types) {
        List<TypeMirror> typesList = getTypeMirrors(types);
        List<TypeElement> typeElements = ofTypeElements(typesList);
        for (TypeMirror typeMirror : typesList) {
            assertTrue(typeElements.contains(ofTypeElement(typeMirror)));
        }
    }

    private void assertOfDeclaredTypes(Class<?>... types) {
        Element[] elements = getElements(types);
        List<DeclaredType> declaredTypes = ofDeclaredTypes(elements);
        assertDeclaredTypes(declaredTypes, types);
    }

    private void assertIsArrayType(Type type) {
        assertTrue(isArrayType(getFieldType(type, "integers")));
        assertTrue(isArrayType(getFieldType(type, "strings")));
        assertTrue(isArrayType(getFieldType(type, "primitiveTypeModels")));
        assertTrue(isArrayType(getFieldType(type, "models")));
        assertTrue(isArrayType(getFieldType(type, "colors")));
    }

    private void assertIsArrayType(Element element) {
        assertTrue(isArrayType(getFieldType(element, "integers")));
        assertTrue(isArrayType(getFieldType(element, "strings")));
        assertTrue(isArrayType(getFieldType(element, "primitiveTypeModels")));
        assertTrue(isArrayType(getFieldType(element, "models")));
        assertTrue(isArrayType(getFieldType(element, "colors")));
    }

    private void assertTypeMirrors(List<TypeMirror> typeMirrors, Type... types) {
        int length = length(types);
        for (int i = 0; i < length; i++) {
            if (types[i] == null) {
                length--;
            }
        }
        assertEquals(length, typeMirrors.size());
        for (int i = 0; i < length; i++) {
            assertSame(typeMirrors.get(i), getTypeMirror(types[i]));
        }
    }

    private void assertTypeElements(List<TypeElement> typeElements, Type... types) {
        int length = length(types);
        for (int i = 0; i < length; i++) {
            if (types[i] == null) {
                length--;
            }
        }
        assertEquals(length, typeElements.size());
        for (int i = 0; i < length; i++) {
            assertSame(typeElements.get(i), getTypeElement(types[i]));
        }
    }

    private void assertDeclaredTypes(List<DeclaredType> declaredTypes, Type... types) {
        int length = length(types);
        for (int i = 0; i < length; i++) {
            if (types[i] == null) {
                length--;
            }
        }
        assertEquals(length, declaredTypes.size());
        for (int i = 0; i < length; i++) {
            assertSame(declaredTypes.get(i), getDeclaredType(types[i]));
        }
    }

    private void assertInterfaceTypeMirror(Element type, Type interfaceType) {
        TypeMirror typeMirror = findInterfaceTypeMirror(type, interfaceType);
        assertTrue(isSameType(typeMirror, interfaceType));

        typeMirror = findInterfaceTypeMirror(type.asType(), interfaceType);
        assertTrue(isSameType(typeMirror, interfaceType));
    }

    private void assertGetTypeMirrors(Type... types) {
        List<TypeMirror> typeMirrors = TypeUtils.getTypeMirrors(processingEnv, types);
        assertEquals(typeMirrors, getTypeMirrors(types));
        assertTypeMirrors(typeMirrors, types);
    }

    private void assertGetTypeMirror(Type... types) {
        int length = length(types);
        for (int i = 0; i < length; i++) {
            Type type = types[i];
            TypeMirror typeMirror = TypeUtils.getTypeMirror(processingEnv, type);
            assertSame(getTypeMirror(type), typeMirror);
            assertTrue(isSameType(typeMirror, type));
        }
    }

    private void assertGetTypeMirrorOnNullProcessingEnvironment(Type... types) {
        int length = length(types);
        for (int i = 0; i < length; i++) {
            Type type = types[i];
            TypeMirror typeMirror = TypeUtils.getTypeMirror(NULL_PROCESSING_ENVIRONMENT, type);
            assertNull(typeMirror);
        }
    }

    private void assertGetTypeElementsWithProcessingEnvironment(Type... types) {
        List<TypeElement> typeElements = TypeUtils.getTypeElements(this.processingEnv, types);
        assertEquals(getTypeElements(types), typeElements);
        assertTypeElements(typeElements, types);
    }

    private void assertGetTypeElementsOnNullProcessingEnvironment(Type... types) {
        List<TypeElement> typeElements = TypeUtils.getTypeElements(NULL_PROCESSING_ENVIRONMENT, types);
        assertEmptyList(typeElements);
    }

    private void assertGetDeclaredType(Type... types) {
        int length = length(types);
        for (int i = 0; i < length; i++) {
            Type type = types[i];
            DeclaredType declaredType = TypeUtils.getDeclaredType(processingEnv, type);
            assertSame(getDeclaredType(type), declaredType);
            assertSame(getDeclaredType(type), TypeUtils.getDeclaredType(processingEnv, declaredType));
            assertTrue(isSameType(declaredType, type));
        }
    }

    private void assertGetDeclaredTypeOnNullProcessingEnvironment(Type... types) {
        int length = length(types);
        for (int i = 0; i < length; i++) {
            Type type = types[i];
            TypeMirror typeMirror = TypeUtils.getDeclaredType(NULL_PROCESSING_ENVIRONMENT, type);
            assertNull(typeMirror);
        }
    }

    private void assertToStringOnMapTypes() {
        assertToString(getFieldType(MapTypeModel.class, "strings"));
        assertToString(getFieldType(MapTypeModel.class, "colors"));
        assertToString(getFieldType(MapTypeModel.class, "primitiveTypeModels"));
        assertToString(getFieldType(MapTypeModel.class, "models"));
        assertToString(getFieldType(MapTypeModel.class, "modelArrays"));
    }

    private void assertToStringOnCollectionTypes() {
        assertToString(getFieldType(CollectionTypeModel.class, "strings"));
        assertToString(getFieldType(CollectionTypeModel.class, "colors"));
        assertToString(getFieldType(CollectionTypeModel.class, "primitiveTypeModels"));
        assertToString(getFieldType(CollectionTypeModel.class, "models"));
        assertToString(getFieldType(CollectionTypeModel.class, "modelArrays"));
    }

    private void assertToStringOnArrayTypes() {
        assertToString(getFieldType(ArrayTypeModel.class, "integers"));
        assertToString(getFieldType(ArrayTypeModel.class, "strings"));
        assertToString(getFieldType(ArrayTypeModel.class, "primitiveTypeModels"));
        assertToString(getFieldType(ArrayTypeModel.class, "models"));
        assertToString(getFieldType(ArrayTypeModel.class, "colors"));
    }

    private TypeMirror getFieldType(Type type, String fieldName) {
        TypeMirror typeMirror = getTypeMirror(type);
        return findField(typeMirror, fieldName).asType();
    }

    private TypeMirror getFieldType(Element element, String fieldName) {
        return findField(element, fieldName).asType();
    }

    private void assertToStringOnClasses() {
        assertToString(NULL_TYPE);
        assertToString(SELF_TYPE);
        assertToString(SUPER_CLASS);
        assertToString(ALL_TYPES);
        assertToString(ALL_SUPER_TYPES);
        assertToString(ALL_SUPER_CLASSES);
        assertToString(ALL_SUPER_INTERFACES);
        assertToString(SUPER_INTERFACES);
        assertToString(SUPER_TYPES);

        assertToString(SELF_TYPE_PLUS_ALL_SUPER_TYPES);
        assertToString(SELF_TYPE_PLUS_ALL_SUPER_CLASSES);
        assertToString(SELF_TYPE_PLUS_ALL_SUPER_INTERFACES);
        assertToString(SELF_TYPE_PLUS_SUPER_CLASS);
        assertToString(SELF_TYPE_PLUS_SUPER_INTERFACES);
        assertToString(SELF_TYPE_PLUS_SUPER_CLASS_PLUS_SUPER_INTERFACES);
    }

    private void assertToString(Type... types) {
        int length = length(types);
        for (int i = 0; i < length; i++) {
            if (types[i] == null) {
                length--;
            }
        }
        for (int i = 0; i < length; i++) {
            TypeMirror typeMirror = getTypeMirror(types[i]);
            assertEquals(types[i].getTypeName(), TypeUtils.toString(typeMirror));
        }
    }

    private void assertToString(TypeMirror type) {
        assertEquals(type.toString(), TypeUtils.toString(type));
    }

    private void assertEmptyList(List<?> list) {
        assertSame(emptyList(), list);
    }
}