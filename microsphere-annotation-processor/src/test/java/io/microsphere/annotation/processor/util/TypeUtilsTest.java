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
import io.microsphere.annotation.processor.model.Color;
import io.microsphere.annotation.processor.model.Model;
import io.microsphere.annotation.processor.model.PrimitiveTypeModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.EventListener;
import java.util.List;
import java.util.Set;

import static io.microsphere.annotation.processor.util.FieldUtils.findField;
import static io.microsphere.annotation.processor.util.FieldUtils.getDeclaredFields;
import static io.microsphere.annotation.processor.util.TypeUtils.findAllDeclaredTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.findAllDeclaredTypesOfInterfaces;
import static io.microsphere.annotation.processor.util.TypeUtils.findAllDeclaredTypesOfSuperTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.findAllDeclaredTypesOfSuperclasses;
import static io.microsphere.annotation.processor.util.TypeUtils.findAllTypeElementsOfInterfaces;
import static io.microsphere.annotation.processor.util.TypeUtils.findAllTypeElementsOfSuperclasses;
import static io.microsphere.annotation.processor.util.TypeUtils.findDeclaredTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.findDeclaredTypesOfInterfaces;
import static io.microsphere.annotation.processor.util.TypeUtils.findTypeElements;
import static io.microsphere.annotation.processor.util.TypeUtils.findTypeElementsOfInterfaces;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllDeclaredTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllDeclaredTypesOfInterfaces;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllDeclaredTypesOfSuperTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllDeclaredTypesOfSuperclasses;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllTypeElements;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllTypeElementsOfInterfaces;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllTypeElementsOfSuperTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllTypeElementsOfSuperclasses;
import static io.microsphere.annotation.processor.util.TypeUtils.getDeclaredTypeOfSuperclass;
import static io.microsphere.annotation.processor.util.TypeUtils.getDeclaredTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.getDeclaredTypesOfInterfaces;
import static io.microsphere.annotation.processor.util.TypeUtils.getResource;
import static io.microsphere.annotation.processor.util.TypeUtils.getResourceName;
import static io.microsphere.annotation.processor.util.TypeUtils.getTypeElementOfSuperclass;
import static io.microsphere.annotation.processor.util.TypeUtils.getTypeElementsOfInterfaces;
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
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.lang.function.Predicates.alwaysFalse;
import static io.microsphere.lang.function.Predicates.alwaysTrue;
import static io.microsphere.util.ArrayUtils.combine;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    private static final TypeMirror NULL_TYPE_MIRROR = null;

    private static final TypeMirror[] EMPTY_TYPE_MIRROR_ARRAY = new TypeMirror[0];

    private static final TypeMirror[] NULL_TYPE_MIRROR_ARRAY = null;

    private static final Collection[] EMPTY_COLLECTION_ARRAY = new Collection[0];

    private static final Collection NULL_COLLECTION = null;

    private static final Element NULL_ELEMENT = null;

    private static final Element[] EMPTY_ELEMENT_ARRAY = new Element[0];

    private static final Element[] NULL_ELEMENT_ARRAY = null;

    private static final TypeElement NULL_TYPE_ELEMENT = null;

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

    private Class<?> testClass;

    private String testClassName;

    private TypeElement testTypeElement;

    private TypeMirror testTypeMirror;

    private DeclaredType testDeclaredType;

    @Override
    protected void addCompiledClasses(Set<Class<?>> compiledClasses) {
        compiledClasses.add(ArrayTypeModel.class);
        compiledClasses.add(Color.class);
    }

    @Override
    protected void beforeTest() {
        this.testClass = SELF_TYPE;
        this.testClassName = SELF_TYPE.getName();
        this.testTypeElement = getTypeElement(testClass);
        this.testTypeMirror = this.testTypeElement.asType();
        this.testDeclaredType = ofDeclaredType(this.testTypeElement);
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
        assertFalse(isSimpleType((TypeElement) null));
        assertFalse(isSimpleType(NULL_TYPE_MIRROR));
    }

    @Test
    public void testIsSameType() {
        assertIsSameType(testTypeElement, testClass);

        assertFalse(isSameType(getDeclaredType(String.class), "java.lang.Void"));
    }

    private void assertIsSameType(Element typeElement, Type type) {
        assertTrue(isSameType(typeElement, type));
        assertTrue(isSameType(typeElement, type.getTypeName()));
        assertTrue(isSameType(typeElement.asType(), type));
        assertTrue(isSameType(typeElement.asType(), type.getTypeName()));
    }

    @Test
    public void testIsSameTypeOnNull() {
        assertFalse(isSameType(NULL_TYPE_MIRROR, testClass));
        assertFalse(isSameType(NULL_TYPE_MIRROR, testClassName));
        assertFalse(isSameType(NULL_ELEMENT, testClass));
        assertFalse(isSameType(NULL_ELEMENT, testClassName));

        assertFalse(isSameType(testTypeElement, (Type) null));
        assertFalse(isSameType(testTypeElement, (String) null));

        assertFalse(isSameType(testTypeMirror, (Type) null));
        assertFalse(isSameType(testTypeMirror, (String) null));

        assertFalse(isSameType(NULL_TYPE_MIRROR, (Type) null));
        assertFalse(isSameType(NULL_TYPE_MIRROR, (String) null));
        assertFalse(isSameType(NULL_ELEMENT, (Type) null));
        assertFalse(isSameType(NULL_ELEMENT, (String) null));
    }

    @Test
    public void testIsArrayTypeOnTypeMirror() {
        assertIsArrayType(getDeclaredType(ArrayTypeModel.class));
    }

    @Test
    public void testIsArrayTypeOnElement() {
        assertIsArrayType(getTypeElement(ArrayTypeModel.class));
    }

    private void assertIsArrayType(TypeMirror type) {
        assertTrue(isArrayType(findField(type, "integers").asType()));
        assertTrue(isArrayType(findField(type, "strings").asType()));
        assertTrue(isArrayType(findField(type, "primitiveTypeModels").asType()));
        assertTrue(isArrayType(findField(type, "models").asType()));
        assertTrue(isArrayType(findField(type, "colors").asType()));
    }

    private void assertIsArrayType(Element element) {
        assertTrue(isArrayType(findField(element, "integers").asType()));
        assertTrue(isArrayType(findField(element, "strings").asType()));
        assertTrue(isArrayType(findField(element, "primitiveTypeModels").asType()));
        assertTrue(isArrayType(findField(element, "models").asType()));
        assertTrue(isArrayType(findField(element, "colors").asType()));
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
        assertTrue(isClassType(getTypeElement(ArrayTypeModel.class)));
        assertTrue(isClassType(getDeclaredType(Model.class)));
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

    private void assertOfTypeMirrors(Class<?>... types) {
        Element[] elements = getElements(types);
        assertEquals(getTypeMirrors(types), ofTypeMirrors(elements));
    }

    @Test
    public void testOfTypeMirrorsOnNull() {
        assertTrue(ofTypeMirrors(EMPTY_ELEMENT_ARRAY).isEmpty());
        assertTrue(ofTypeMirrors(NULL_COLLECTION).isEmpty());
    }

    @Test
    public void testOfTypeMirrorsOnEmpty() {
        assertTrue(ofTypeMirrors(EMPTY_ELEMENT_ARRAY).isEmpty());
        assertTrue(ofTypeMirrors(emptyList()).isEmpty());
    }

    @Test
    public void testOfTypeElements() {
        assertOfTypeElements(String.class, SELF_TYPE, Color.class);
    }

    private void assertOfTypeElements(Class<?>... types) {
        List<TypeMirror> typesList = getTypeMirrors(types);
        List<TypeElement> typeElements = ofTypeElements(typesList);
        for (TypeMirror typeMirror : typesList) {
            assertTrue(typeElements.contains(ofTypeElement(typeMirror)));
        }
    }

    @Test
    public void testOfTypeElementsOnNull() {
        assertTrue(ofTypeElements(NULL_TYPE_MIRROR_ARRAY).isEmpty());
        assertTrue(ofTypeElements(NULL_COLLECTION).isEmpty());
    }

    @Test
    public void testOfTypeElementsOnEmpty() {
        assertTrue(ofTypeElements(EMPTY_TYPE_MIRROR_ARRAY).isEmpty());
        assertTrue(ofTypeElements(emptyList()).isEmpty());
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

    private void assertOfDeclaredTypes(Class<?>... types) {
        Element[] elements = getElements(types);
        List<DeclaredType> declaredTypes = ofDeclaredTypes(elements);
        assertDeclaredTypes(declaredTypes, types);
    }

    @Test
    public void testOfDeclaredTypesOnNull() {
        assertTrue(ofDeclaredTypes(NULL_ELEMENT_ARRAY).isEmpty());
        assertTrue(ofDeclaredTypes(NULL_COLLECTION).isEmpty());
    }

    @Test
    public void testOfDeclaredTypesOnEmpty() {
        assertTrue(ofDeclaredTypes(emptyList()).isEmpty());
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
        assertNull(getTypeElementOfSuperclass(null));
    }

    @Test
    public void testGetAllTypeElementsOfSuperclasses() {
        List<TypeElement> allSuperTypes = getAllTypeElementsOfSuperclasses(testTypeElement);
        assertTypeElements(allSuperTypes, ALL_SUPER_CLASSES);
    }

    @Test
    public void testGetAllTypeElementsOfSuperclassesOnNull() {
        assertTrue(getAllTypeElementsOfSuperclasses(null).isEmpty());
    }

    @Test
    public void testGetTypeElementsOfInterfaces() {
        List<TypeElement> typeElements = getTypeElementsOfInterfaces(testTypeElement);
        assertTypeElements(typeElements, SUPER_INTERFACES);
    }

    @Test
    public void testGetTypeElementsOfInterfacesOnNull() {
        assertTrue(getTypeElementsOfInterfaces(null).isEmpty());
    }

    @Test
    public void testGetAllTypeElementsOfInterfaces() {
        List<TypeElement> typeElements = getAllTypeElementsOfInterfaces(testTypeElement);
        assertTypeElements(typeElements, ALL_SUPER_INTERFACES);
    }

    @Test
    public void testGetAllTypeElementsOfInterfacesOnNull() {
        assertTrue(getAllTypeElementsOfInterfaces(null).isEmpty());
    }

    @Test
    public void testGetAllTypeElements() {
        List<TypeElement> allTypeElements = getAllTypeElements(testTypeElement);
        assertTypeElements(allTypeElements, ALL_TYPES);
    }

    @Test
    public void testGetAllTypeElementsOnNull() {
        assertTrue(getAllTypeElements(null).isEmpty());
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
        assertSame(emptyList(), typeElements);

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
        assertSame(emptyList(), typeElements);
    }

    @Test
    public void testGetTypeElementsOnNull() {
        assertTrue(TypeUtils.getTypeElements(null, true, true, true, true).isEmpty());
        assertTrue(TypeUtils.getTypeElements(null, true, true, true, false).isEmpty());
        assertTrue(TypeUtils.getTypeElements(null, true, true, false, true).isEmpty());
        assertTrue(TypeUtils.getTypeElements(null, true, true, false, false).isEmpty());

        assertTrue(TypeUtils.getTypeElements(null, true, false, true, true).isEmpty());
        assertTrue(TypeUtils.getTypeElements(null, false, false, true, false).isEmpty());
        assertTrue(TypeUtils.getTypeElements(null, false, false, false, true).isEmpty());
        assertTrue(TypeUtils.getTypeElements(null, false, false, false, false).isEmpty());

        assertTrue(TypeUtils.getTypeElements(null, false, true, true, true).isEmpty());
        assertTrue(TypeUtils.getTypeElements(null, false, true, true, false).isEmpty());
        assertTrue(TypeUtils.getTypeElements(null, false, true, false, true).isEmpty());
        assertTrue(TypeUtils.getTypeElements(null, false, true, false, false).isEmpty());

        assertTrue(TypeUtils.getTypeElements(null, false, false, true, true).isEmpty());
        assertTrue(TypeUtils.getTypeElements(null, false, false, true, false).isEmpty());
        assertTrue(TypeUtils.getTypeElements(null, false, false, false, true).isEmpty());
        assertTrue(TypeUtils.getTypeElements(null, false, false, false, false).isEmpty());
    }

    @Test
    public void testFindAllTypeElementsOfSuperclasses() {
        List<TypeElement> typeElements = findAllTypeElementsOfSuperclasses(testTypeElement, alwaysTrue());
        assertTypeElements(typeElements, ALL_SUPER_CLASSES);
        assertEquals(getAllTypeElementsOfSuperclasses(testTypeElement), typeElements);

        assertSame(emptyList(), findAllTypeElementsOfSuperclasses(testTypeElement, alwaysFalse()));
    }

    @Test
    public void testFindAllTypeElementsOfSuperclassesOnNull() {
        assertSame(emptyList(), findAllTypeElementsOfSuperclasses(null, alwaysTrue()));
        assertSame(emptyList(), findAllTypeElementsOfSuperclasses(null, alwaysFalse()));
    }

    @Test
    public void testFindAllTypeElementsOfInterfaces() {
        List<TypeElement> typeElements = findAllTypeElementsOfInterfaces(testTypeElement, alwaysTrue());
        assertTypeElements(typeElements, ALL_SUPER_INTERFACES);
        assertEquals(getAllTypeElementsOfInterfaces(testTypeElement), typeElements);

        assertSame(emptyList(), findAllTypeElementsOfInterfaces(testTypeElement, alwaysFalse()));
    }

    @Test
    public void testFindAllTypeElementsOfInterfacesOnNull() {
        assertSame(emptyList(), findAllTypeElementsOfInterfaces(null, alwaysTrue()));
        assertSame(emptyList(), findAllTypeElementsOfInterfaces(null, alwaysFalse()));
    }

    @Test
    public void testFindTypeElementsOfInterfaces() {
        List<TypeElement> typeElements = findTypeElementsOfInterfaces(testTypeElement, alwaysTrue());
        assertTypeElements(typeElements, SUPER_INTERFACES);
        assertEquals(getTypeElementsOfInterfaces(testTypeElement), typeElements);

        assertSame(emptyList(), findTypeElementsOfInterfaces(testTypeElement, alwaysFalse()));
    }

    @Test
    public void testFindTypeElementsOfInterfacesOnNull() {
        assertSame(emptyList(), findTypeElementsOfInterfaces(null, alwaysTrue()));
        assertSame(emptyList(), findTypeElementsOfInterfaces(null, alwaysFalse()));
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
        assertSame(emptyList(), typeElements);

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
        assertSame(emptyList(), typeElements);
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
        assertTrue(getDeclaredTypesOfInterfaces(NULL_ELEMENT).isEmpty());
        assertTrue(getDeclaredTypesOfInterfaces(NULL_TYPE_MIRROR).isEmpty());
    }

    @Test
    public void testGetAllDeclaredTypesOfSuperclasses() {
        List<DeclaredType> declaredTypes = getAllDeclaredTypesOfSuperclasses(testTypeMirror);
        assertDeclaredTypes(declaredTypes, ALL_SUPER_CLASSES);
    }

    @Test
    public void testGetAllDeclaredTypesOfSuperclassesOnNull() {
        assertTrue(getAllDeclaredTypesOfSuperclasses(NULL_ELEMENT).isEmpty());
        assertTrue(getAllDeclaredTypesOfSuperclasses(NULL_TYPE_MIRROR).isEmpty());
    }

    @Test
    public void testGetAllDeclaredTypesOfInterfaces() {
        List<DeclaredType> declaredTypes = getAllDeclaredTypesOfInterfaces(testTypeMirror);
        assertDeclaredTypes(declaredTypes, ALL_SUPER_INTERFACES);
    }

    @Test
    public void testGetAllDeclaredTypesOfInterfacesOnNull() {
        assertTrue(getAllDeclaredTypesOfInterfaces(NULL_ELEMENT).isEmpty());
        assertTrue(getAllDeclaredTypesOfInterfaces(NULL_TYPE_MIRROR).isEmpty());
    }

    @Test
    public void testGetAllDeclaredTypesOfSuperTypes() {
        List<DeclaredType> declaredTypes = getAllDeclaredTypesOfSuperTypes(testTypeMirror);
        assertDeclaredTypes(declaredTypes, ALL_SUPER_TYPES);
    }

    @Test
    public void testGetAllDeclaredTypesOfSuperTypesOnNull() {
        assertTrue(getAllDeclaredTypesOfSuperTypes(NULL_ELEMENT).isEmpty());
        assertTrue(getAllDeclaredTypesOfSuperTypes(NULL_TYPE_MIRROR).isEmpty());
    }

    @Test
    public void testGetAllDeclaredTypes() {
        List<DeclaredType> declaredTypes = getAllDeclaredTypes(testTypeMirror);
        assertDeclaredTypes(declaredTypes, ALL_TYPES);
    }

    @Test
    public void testGetAllDeclaredTypesOnNull() {
        assertTrue(getAllDeclaredTypes(NULL_ELEMENT).isEmpty());
        assertTrue(getAllDeclaredTypes(NULL_TYPE_MIRROR).isEmpty());
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
        assertSame(emptyList(), declaredTypes);

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
        assertSame(emptyList(), declaredTypes);
    }

    @Test
    public void testFindDeclaredTypesOfInterfaces() {
        List<DeclaredType> declaredTypes = findDeclaredTypesOfInterfaces(testTypeMirror, alwaysTrue());
        assertDeclaredTypes(declaredTypes, SUPER_INTERFACES);

        declaredTypes = findDeclaredTypesOfInterfaces(testTypeMirror, alwaysFalse());
        assertSame(emptyList(), declaredTypes);
    }

    @Test
    public void testFindDeclaredTypesOfInterfacesOnNull() {
        assertTrue(findDeclaredTypesOfInterfaces(NULL_ELEMENT, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypesOfInterfaces(NULL_ELEMENT, alwaysFalse()).isEmpty());
        assertTrue(findDeclaredTypesOfInterfaces(NULL_TYPE_MIRROR, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypesOfInterfaces(NULL_TYPE_MIRROR, alwaysTrue()).isEmpty());
    }

    @Test
    public void testFindAllDeclaredTypesOfSuperclasses() {
        List<DeclaredType> declaredTypes = findAllDeclaredTypesOfSuperclasses(testTypeMirror, alwaysTrue());
        assertDeclaredTypes(declaredTypes, ALL_SUPER_CLASSES);

        declaredTypes = findAllDeclaredTypesOfSuperclasses(testTypeMirror, alwaysFalse());
        assertSame(emptyList(), declaredTypes);
    }

    @Test
    public void testFindAllDeclaredTypesOfSuperclassesOnNull() {
        assertTrue(findAllDeclaredTypesOfSuperclasses(NULL_ELEMENT, alwaysTrue()).isEmpty());
        assertTrue(findAllDeclaredTypesOfSuperclasses(NULL_ELEMENT, alwaysFalse()).isEmpty());
        assertTrue(findAllDeclaredTypesOfSuperclasses(NULL_TYPE_MIRROR, alwaysTrue()).isEmpty());
        assertTrue(findAllDeclaredTypesOfSuperclasses(NULL_TYPE_MIRROR, alwaysFalse()).isEmpty());
    }

    @Test
    public void testFindAllDeclaredTypesOfInterfaces() {
        List<DeclaredType> declaredTypes = findAllDeclaredTypesOfInterfaces(testTypeMirror, alwaysTrue());
        assertDeclaredTypes(declaredTypes, ALL_SUPER_INTERFACES);

        declaredTypes = findAllDeclaredTypesOfInterfaces(testTypeMirror, alwaysFalse());
        assertSame(emptyList(), declaredTypes);
    }

    @Test
    public void testFindAllDeclaredTypesOfInterfacesOnNull() {
        assertTrue(findAllDeclaredTypesOfInterfaces(NULL_ELEMENT, alwaysTrue()).isEmpty());
        assertTrue(findAllDeclaredTypesOfInterfaces(NULL_ELEMENT, alwaysFalse()).isEmpty());
        assertTrue(findAllDeclaredTypesOfInterfaces(NULL_TYPE_MIRROR, alwaysTrue()).isEmpty());
        assertTrue(findAllDeclaredTypesOfInterfaces(NULL_TYPE_MIRROR, alwaysFalse()).isEmpty());
    }

    @Test
    public void testFindAllDeclaredTypesOfSuperTypes() {
        List<DeclaredType> declaredTypes = findAllDeclaredTypesOfSuperTypes(testTypeMirror, alwaysTrue());
        assertDeclaredTypes(declaredTypes, ALL_SUPER_TYPES);

        declaredTypes = findAllDeclaredTypesOfSuperTypes(testTypeMirror, alwaysFalse());
        assertSame(emptyList(), declaredTypes);
    }

    @Test
    public void testFindAllDeclaredTypesOfSuperTypesOnNull() {
        assertTrue(findAllDeclaredTypesOfSuperTypes(NULL_ELEMENT).isEmpty());
        assertTrue(findAllDeclaredTypesOfSuperTypes(NULL_TYPE_MIRROR).isEmpty());
    }

    @Test
    public void testFindAllDeclaredTypes() {
        List<DeclaredType> declaredTypes = findAllDeclaredTypes(testTypeMirror, alwaysTrue());
        assertDeclaredTypes(declaredTypes, ALL_TYPES);

        declaredTypes = findAllDeclaredTypes(testTypeMirror, alwaysFalse());
        assertSame(emptyList(), declaredTypes);
    }

    @Test
    public void testFindAllDeclaredTypesOnNull() {
        assertTrue(findAllDeclaredTypes(NULL_ELEMENT, alwaysTrue()).isEmpty());
        assertTrue(findAllDeclaredTypes(NULL_ELEMENT, alwaysFalse()).isEmpty());
        assertTrue(findAllDeclaredTypes(NULL_TYPE_MIRROR, alwaysTrue()).isEmpty());
        assertTrue(findAllDeclaredTypes(NULL_TYPE_MIRROR, alwaysFalse()).isEmpty());
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
        assertSame(emptyList(), declaredTypes);

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
        assertSame(emptyList(), declaredTypes);
    }

    @Test
    public void testFindDeclaredTypesOnNull() {
        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, true, true, true, true, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, true, true, true, true, alwaysFalse()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, true, true, true, true, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, true, true, true, true, alwaysFalse()).isEmpty());

        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, true, true, true, false, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, true, true, true, false, alwaysFalse()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, true, true, true, false, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, true, true, true, false, alwaysFalse()).isEmpty());

        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, true, true, false, true, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, true, true, false, true, alwaysFalse()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, true, true, false, true, alwaysFalse()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, true, true, false, true, alwaysFalse()).isEmpty());

        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, true, true, false, false, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, true, true, false, false, alwaysFalse()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, true, true, false, false, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, true, true, false, false, alwaysFalse()).isEmpty());

        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, true, false, true, true, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, true, false, true, true, alwaysFalse()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, true, false, true, true, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, true, false, true, true, alwaysFalse()).isEmpty());

        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, true, false, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, true, false, alwaysFalse()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, true, false, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, true, false, alwaysFalse()).isEmpty());

        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, false, true, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, false, true, alwaysFalse()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, false, true, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, false, true, alwaysFalse()).isEmpty());

        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, false, false, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, false, false, alwaysFalse()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, false, false, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, false, false, alwaysFalse()).isEmpty());

        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, false, true, true, true, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, false, true, true, true, alwaysFalse()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, false, true, true, true, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, false, true, true, true, alwaysFalse()).isEmpty());

        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, false, true, true, false, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, false, true, true, false, alwaysFalse()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, false, true, true, false, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, false, true, true, false, alwaysFalse()).isEmpty());

        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, false, true, false, true, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, false, true, false, true, alwaysFalse()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, false, true, false, true, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, false, true, false, true, alwaysFalse()).isEmpty());

        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, false, true, false, false, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, false, true, false, false, alwaysFalse()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, false, true, false, false, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, false, true, false, false, alwaysFalse()).isEmpty());

        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, true, true, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, true, true, alwaysFalse()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, true, true, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, true, true, alwaysFalse()).isEmpty());

        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, true, false, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, true, false, alwaysFalse()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, true, false, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, true, false, alwaysFalse()).isEmpty());

        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, false, true, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, false, true, alwaysFalse()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, false, true, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, false, true, alwaysFalse()).isEmpty());

        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, false, false, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_ELEMENT, false, false, false, false, alwaysFalse()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, false, false, alwaysTrue()).isEmpty());
        assertTrue(findDeclaredTypes(NULL_TYPE_MIRROR, false, false, false, false, alwaysFalse()).isEmpty());
    }

    @Test
    public void testFindInterfaceTypeMirror() {
        // TODO
    }

    @Test
    public void testFindInterfaceTypeMirrorOnNull() {
        // TODO
    }

    @Test
    public void testGetTypeMirrors() {
        // TODO
    }

    @Test
    public void testGetTypeMirrorsOnNull() {
        // TODO
    }

    @Test
    public void testGetTypeElementsWithProcessingEnvironment() {
        // TODO
    }

    @Test
    public void testGetTypeElementsWithProcessingEnvironmentOnNull() {
        // TODO
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
        assertEquals(testTypeElement, TypeUtils.getTypeElement(processingEnv, TestServiceImpl.class));
    }

    @Test
    public void testGetTypeElementOnNull() {
        assertNull(TypeUtils.getTypeElement(processingEnv, (Type) null));
        assertNull(TypeUtils.getTypeElement(processingEnv, NULL_TYPE_MIRROR));
        assertNull(TypeUtils.getTypeElement(processingEnv, (CharSequence) null));
        assertNull(TypeUtils.getTypeElement(null, (CharSequence) null));
    }

    @Test
    public void testGetDeclaredType() {
        // TODO
    }

    @Test
    public void testGetDeclaredTypeOnNull() {
        // TODO
    }

    @Test
    @Disabled("Failed due to github action env problem")
    public void testGetResource() throws URISyntaxException {
        URL resource = getResource(processingEnv, testTypeElement);
        assertNotNull(resource);
        assertTrue(new File(resource.toURI()).exists());
        assertEquals(resource, getResource(processingEnv, testTypeMirror));
        assertEquals(resource, getResource(processingEnv, testClassName));

        assertThrows(RuntimeException.class, () -> getResource(processingEnv, "NotFound"));
    }

    @Test
    public void testGetResourceName() {
        assertEquals("java/lang/String.class", getResourceName("java.lang.String"));
        assertNull(getResourceName(null));
    }

    @Test
    public void testToString() {
        // TODO
    }

    @Test
    public void testToStringOnNull() {
        // TODO
    }

    @Test
    public void testTypeElementFinderOnNull() {

    }

    private void assertDeclaredTypes(List<DeclaredType> declaredTypes, Type... types) {
        int length = types.length;
        assertEquals(length, declaredTypes.size());
        for (int i = 0; i < length; i++) {
            assertSame(declaredTypes.get(i), getDeclaredType(types[i]));
        }
    }

    private void assertTypeElements(List<TypeElement> typeElements, Type... types) {
        int length = types.length;
        assertEquals(length, typeElements.size());
        for (int i = 0; i < length; i++) {
            assertSame(typeElements.get(i), getTypeElement(types[i]));
        }
    }
}
