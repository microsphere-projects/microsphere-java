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
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static io.microsphere.annotation.processor.util.FieldUtils.findField;
import static io.microsphere.annotation.processor.util.FieldUtils.getDeclaredFields;
import static io.microsphere.annotation.processor.util.TypeUtils.findDeclaredTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllInterfaces;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllTypeElements;
import static io.microsphere.annotation.processor.util.TypeUtils.getDeclaredTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.getInterfaceTypeMirrors;
import static io.microsphere.annotation.processor.util.TypeUtils.getResource;
import static io.microsphere.annotation.processor.util.TypeUtils.getResourceName;
import static io.microsphere.annotation.processor.util.TypeUtils.getSuperDeclaredType;
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
import static io.microsphere.annotation.processor.util.TypeUtils.listDeclaredTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.listTypeElements;
import static io.microsphere.annotation.processor.util.TypeUtils.ofDeclaredType;
import static io.microsphere.annotation.processor.util.TypeUtils.ofDeclaredTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.ofTypeElement;
import static io.microsphere.annotation.processor.util.TypeUtils.ofTypeElements;
import static io.microsphere.annotation.processor.util.TypeUtils.ofTypeMirrors;
import static io.microsphere.collection.Lists.ofList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The {@link TypeUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class TypeUtilsTest extends AbstractAnnotationProcessingTest {

    private TypeElement testType;

    @Override
    protected void addCompiledClasses(Set<Class<?>> compiledClasses) {
        compiledClasses.add(ArrayTypeModel.class);
        compiledClasses.add(Color.class);
    }

    @Override
    protected void beforeTest() {
        testType = getTypeElement(TestServiceImpl.class);
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
        assertFalse(isSimpleType((TypeMirror) null));
    }

    @Test
    public void testIsSameType() {
        assertTrue(isSameType(getTypeElement(Void.class).asType(), "java.lang.Void"));
        assertFalse(isSameType(getTypeElement(String.class).asType(), "java.lang.Void"));
    }

    @Test
    public void testIsSameTypeOnNull() {
        assertFalse(isSameType(getTypeElement(Void.class).asType(), (Type) null));
        assertFalse(isSameType(null, (Type) null));

        assertFalse(isSameType(getTypeElement(Void.class).asType(), (String) null));
        assertFalse(isSameType(null, (String) null));
    }

    @Test
    public void testIsArrayType() {
        TypeElement type = getTypeElement(ArrayTypeModel.class);
        assertTrue(isArrayType(findField(type.asType(), "integers").asType()));
        assertTrue(isArrayType(findField(type.asType(), "strings").asType()));
        assertTrue(isArrayType(findField(type.asType(), "primitiveTypeModels").asType()));
        assertTrue(isArrayType(findField(type.asType(), "models").asType()));
        assertTrue(isArrayType(findField(type.asType(), "colors").asType()));
    }

    @Test
    public void testIsArrayTypeOnNull() {
        assertFalse(isArrayType((Element) null));
        assertFalse(isArrayType((TypeMirror) null));
    }

    @Test
    public void testIsEnumType() {
        TypeElement type = getTypeElement(Color.class);
        assertTrue(isEnumType(type.asType()));

        type = getTypeElement(ArrayTypeModel.class);
        assertFalse(isEnumType(type.asType()));
    }

    @Test
    public void testIsEnumTypeOnNull() {
        assertFalse(isEnumType((Element) null));
        assertFalse(isEnumType((TypeMirror) null));
    }

    @Test
    public void testIsClassType() {
        TypeElement type = getTypeElement(ArrayTypeModel.class);
        assertTrue(isClassType(type.asType()));

        type = getTypeElement(Model.class);
        assertTrue(isClassType(type.asType()));
    }

    @Test
    public void testIsClassTypeOnNull() {
        assertFalse(isClassType((Element) null));
        assertFalse(isClassType((TypeMirror) null));
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
        assertFalse(isPrimitiveType((Element) null));
        assertFalse(isPrimitiveType((TypeMirror) null));
    }

    @Test
    public void testIsInterfaceType() {
        TypeElement type = getTypeElement(CharSequence.class);
        assertTrue(isInterfaceType(type));
        assertTrue(isInterfaceType(type.asType()));

        type = getTypeElement(Model.class);
        assertFalse(isInterfaceType(type));
        assertFalse(isInterfaceType(type.asType()));
    }

    @Test
    public void testIsInterfaceTypeOnNull() {
        assertFalse(isInterfaceType((Element) null));
        assertFalse(isInterfaceType((TypeMirror) null));
    }

    @Test
    public void testIsAnnotationType() {
        TypeElement type = getTypeElement(Override.class);

        assertTrue(isAnnotationType(type));
        assertTrue(isAnnotationType(type.asType()));

        type = getTypeElement(Model.class);
        assertFalse(isAnnotationType(type));
        assertFalse(isAnnotationType(type.asType()));
    }

    @Test
    public void testIsAnnotationTypeOnNull() {
        assertFalse(isAnnotationType((Element) null));
        assertFalse(isAnnotationType((TypeMirror) null));
    }

    @Test
    public void testIsDeclaredType() {
        assertTrue(isDeclaredType(testType));
        assertTrue(isDeclaredType(testType.asType()));
        assertFalse(isDeclaredType((Element) null));
        assertFalse(isDeclaredType((TypeMirror) null));
        assertFalse(isDeclaredType(types.getNullType()));
        assertFalse(isDeclaredType(types.getPrimitiveType(TypeKind.BYTE)));
        assertFalse(isDeclaredType(types.getArrayType(types.getPrimitiveType(TypeKind.BYTE))));
    }

    @Test
    public void testIsDeclaredTypeOnNull() {
        assertFalse(isDeclaredType((Element) null));
        assertFalse(isDeclaredType((TypeMirror) null));
    }

    @Test
    public void testOfDeclaredType() {
        assertEquals(testType.asType(), ofDeclaredType(testType));
        assertEquals(testType.asType(), ofDeclaredType(testType.asType()));
        assertEquals(ofDeclaredType(testType), ofDeclaredType(testType.asType()));
    }

    @Test
    public void testOfDeclaredTypeOnNull() {
        assertNull(ofDeclaredType((Element) null));
        assertNull(ofDeclaredType((TypeMirror) null));
    }

    @Test
    public void testIsTypeElement() {
        assertTrue(isTypeElement(testType));
        assertTrue(isTypeElement(testType.asType()));
    }

    @Test
    public void testIsTypeElementOnNull() {
        assertFalse(isTypeElement((Element) null));
        assertFalse(isTypeElement((TypeMirror) null));
    }

    @Test
    public void testOfTypeElement() {
        assertEquals(testType, ofTypeElement(testType));
        assertEquals(testType, ofTypeElement(testType.asType()));
    }

    @Test
    public void testOfTypeElementOnNull() {
        assertNull(ofTypeElement((Element) null));
        assertNull(ofTypeElement((TypeMirror) null));
    }

    @Test
    public void testOfDeclaredTypes() {
        assertOfDeclaredTypes(String.class, TestServiceImpl.class, Color.class);
    }

    @Test
    public void testOfDeclaredTypesWithFilter() {
        List<DeclaredType> declaredTypes = ofDeclaredTypes(ofList(getTypeElement(String.class), getTypeElement(TestServiceImpl.class), getTypeElement(Color.class)), t -> true);
        assertTrue(declaredTypes.contains(getTypeElement(String.class).asType()));
        assertTrue(declaredTypes.contains(getTypeElement(TestServiceImpl.class).asType()));
        assertTrue(declaredTypes.contains(getTypeElement(Color.class).asType()));
    }

    private void assertOfDeclaredTypes(Class<?>... types) {
        Element[] elements = getElements(types);
        List<DeclaredType> declaredTypes = ofDeclaredTypes(elements);
        int length = elements.length;
        assertEquals(length, declaredTypes.size());
        for (int i = 0; i < length; i++) {
            assertEquals(elements[i].asType(), declaredTypes.get(i));
        }
    }

    @Test
    public void testOfDeclaredTypesOnNull() {
        assertTrue(ofDeclaredTypes((Element[]) null).isEmpty());
        assertTrue(ofDeclaredTypes((Collection) null).isEmpty());
    }

    @Test
    public void testOfDeclaredTypesOnEmpty() {
        assertTrue(ofDeclaredTypes(emptyList()).isEmpty());
    }

    @Test
    public void testOfTypeElements() {
        assertOfTypeElements(String.class, TestServiceImpl.class, Color.class);
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
        assertTrue(ofTypeElements((TypeMirror[]) null).isEmpty());
        assertTrue(ofTypeElements((Collection) null).isEmpty());
    }

    @Test
    public void testOfTypeElementsOnEmpty() {
        assertTrue(ofTypeElements(new TypeMirror[0]).isEmpty());
        assertTrue(ofTypeElements(emptyList()).isEmpty());
    }

    @Test
    public void testOfTypeMirrors() {
        assertOfTypeMirrors(String.class, TestServiceImpl.class, Color.class);
    }

    private void assertOfTypeMirrors(Class<?>... types) {
        Element[] elements = getElements(types);
        assertEquals(getTypeMirrors(types), ofTypeMirrors(elements));
    }

    @Test
    public void testOfTypeMirrorsOnNull() {
        assertTrue(ofTypeMirrors((Element[]) null).isEmpty());
        assertTrue(ofTypeMirrors((Collection) null).isEmpty());
    }

    @Test
    public void testOfTypeMirrorsOnEmpty() {
        assertTrue(ofTypeMirrors(new Element[0]).isEmpty());
        assertTrue(ofTypeMirrors(emptyList()).isEmpty());
    }

    @Test
    public void testGetDeclaredTypes() {
        List declaredTypes = getDeclaredTypes(testType, true, true, true, true);
        Iterator iterator = declaredTypes.iterator();
        assertEquals(8, declaredTypes.size());
        assertEquals("io.microsphere.annotation.processor.TestServiceImpl", iterator.next().toString());
        assertEquals("io.microsphere.annotation.processor.GenericTestService", iterator.next().toString());
        assertEquals("io.microsphere.annotation.processor.DefaultTestService", iterator.next().toString());
        assertEquals("java.lang.Object", iterator.next().toString());
        assertEquals("io.microsphere.annotation.processor.TestService", iterator.next().toString());
        assertEquals("java.util.EventListener", iterator.next().toString());
        assertEquals("java.lang.AutoCloseable", iterator.next().toString());
        assertEquals("java.io.Serializable", iterator.next().toString());
    }

    @Test
    public void testFindDeclaredTypes() {
        List declaredTypes = findDeclaredTypes(testType, true, true, true, true, TypeUtils::isInterfaceType);
        Iterator iterator = declaredTypes.iterator();
        assertEquals(4, declaredTypes.size());
        assertEquals("io.microsphere.annotation.processor.TestService", iterator.next().toString());
        assertEquals("java.util.EventListener", iterator.next().toString());
        assertEquals("java.lang.AutoCloseable", iterator.next().toString());
        assertEquals("java.io.Serializable", iterator.next().toString());
    }

    @Test
    public void testGetAllTypeElements() {
        List<TypeElement> allTypeElements = getAllTypeElements(testType);
        Iterator<TypeElement> iterator = allTypeElements.iterator();
        assertEquals(8, allTypeElements.size());
        assertEquals("io.microsphere.annotation.processor.TestServiceImpl", iterator.next().toString());
        assertEquals("io.microsphere.annotation.processor.GenericTestService", iterator.next().toString());
        assertEquals("io.microsphere.annotation.processor.DefaultTestService", iterator.next().toString());
        assertEquals("java.lang.Object", iterator.next().toString());
        assertEquals("io.microsphere.annotation.processor.TestService", iterator.next().toString());
        assertEquals("java.lang.AutoCloseable", iterator.next().toString());
        assertEquals("java.io.Serializable", iterator.next().toString());
        assertEquals("java.util.EventListener", iterator.next().toString());
    }

//    {
//
//        allTypeElements = TypeUtils.findAllDeclaredTypes(testType.asType(), Object.class);
//        iterator = allTypeElements.iterator();
//        assertEquals(7, allTypeElements.size());
//        assertEquals("io.microsphere.annotation.processor.TestServiceImpl", iterator.next().toString());
//        assertEquals("io.microsphere.annotation.processor.GenericTestService", iterator.next().toString());
//        assertEquals("io.microsphere.annotation.processor.DefaultTestService", iterator.next().toString());
//        assertEquals("io.microsphere.annotation.processor.TestService", iterator.next().toString());
//        assertEquals("java.lang.AutoCloseable", iterator.next().toString());
//        assertEquals("java.io.Serializable", iterator.next().toString());
//        assertEquals("java.util.EventListener", iterator.next().toString());
//
//        allTypeElements = getDeclaredTypes(testType.asType(), true, true, true, false);
//        iterator = allTypeElements.iterator();
//        assertEquals(4, allTypeElements.size());
//        assertEquals("io.microsphere.annotation.processor.TestServiceImpl", iterator.next().toString());
//        assertEquals("io.microsphere.annotation.processor.GenericTestService", iterator.next().toString());
//        assertEquals("io.microsphere.annotation.processor.DefaultTestService", iterator.next().toString());
//        assertEquals("java.lang.Object", iterator.next().toString());
//
//        allTypeElements = getDeclaredTypes(testType.asType(), true, true, false, true);
//        iterator = allTypeElements.iterator();
//        assertEquals(5, allTypeElements.size());
//        assertEquals("io.microsphere.annotation.processor.TestServiceImpl", iterator.next().toString());
//        assertEquals("io.microsphere.annotation.processor.TestService", iterator.next().toString());
//        assertEquals("java.lang.AutoCloseable", iterator.next().toString());
//        assertEquals("java.io.Serializable", iterator.next().toString());
//        assertEquals("java.util.EventListener", iterator.next().toString());
//
//        allTypeElements = getDeclaredTypes(testType.asType(), false, true, false, true);
//        iterator = allTypeElements.iterator();
//        assertEquals(4, allTypeElements.size());
//        assertEquals("io.microsphere.annotation.processor.TestService", iterator.next().toString());
//        assertEquals("java.lang.AutoCloseable", iterator.next().toString());
//        assertEquals("java.io.Serializable", iterator.next().toString());
//        assertEquals("java.util.EventListener", iterator.next().toString());
//
//        allTypeElements = getDeclaredTypes(testType.asType(), true, true, false, false);
//        iterator = allTypeElements.iterator();
//        assertEquals(1, allTypeElements.size());
//        assertEquals("io.microsphere.annotation.processor.TestServiceImpl", iterator.next().toString());
//
//        allTypeElements = getDeclaredTypes(testType.asType(), false, true, false, false);
//        assertEquals(0, allTypeElements.size());
//
//        assertTrue(getAllTypeElements((TypeElement) null).isEmpty());
//        assertTrue(getAllDeclaredTypes((TypeMirror) null).isEmpty());
//    }


    @Test
    public void testGetInterfaceTypeMirrors() {
        TypeElement type = getTypeElement(Model.class);
        List<TypeMirror> interfaces = TypeUtils.getInterfaceTypeMirrors(type);
        assertTrue(interfaces.isEmpty());

        interfaces = getInterfaceTypeMirrors(testType.asType());

        assertEquals(3, interfaces.size());
        assertEquals("io.microsphere.annotation.processor.TestService", interfaces.get(0).toString());
        assertEquals("java.lang.AutoCloseable", interfaces.get(1).toString());
        assertEquals("java.io.Serializable", interfaces.get(2).toString());

        assertTrue(TypeUtils.getInterfaceTypeMirrors((TypeElement) null).isEmpty());
        assertTrue(getInterfaceTypeMirrors((TypeMirror) null).isEmpty());
    }

    @Test
    public void testGetAllInterfaces() {
        List<? extends TypeMirror> interfaces = getAllInterfaces(testType.asType());
        assertEquals(4, interfaces.size());
        Iterator<? extends TypeMirror> iterator = interfaces.iterator();
        assertEquals("io.microsphere.annotation.processor.TestService", iterator.next().toString());
        assertEquals("java.lang.AutoCloseable", iterator.next().toString());
        assertEquals("java.io.Serializable", iterator.next().toString());
        assertEquals("java.util.EventListener", iterator.next().toString());

        List<TypeElement> allInterfaces = TypeUtils.getAllInterfaceTypeElements(testType);
        assertEquals(4, interfaces.size());

        Iterator<TypeElement> allIterator = allInterfaces.iterator();
        assertEquals("io.microsphere.annotation.processor.TestService", allIterator.next().toString());
        assertEquals("java.lang.AutoCloseable", allIterator.next().toString());
        assertEquals("java.io.Serializable", allIterator.next().toString());
        assertEquals("java.util.EventListener", allIterator.next().toString());

        assertTrue(TypeUtils.getAllInterfaceTypeElements((TypeElement) null).isEmpty());
        assertTrue(getAllInterfaces((TypeMirror) null).isEmpty());
    }

    @Test
    public void testGetTypeElement() {
        TypeElement element = TypeUtils.getTypeElement(processingEnv, String.class);
        assertEquals(element, TypeUtils.getTypeElement(processingEnv, element.asType()));
        assertEquals(element, TypeUtils.getTypeElement(processingEnv, "java.lang.String"));

        assertNull(TypeUtils.getTypeElement(processingEnv, (Type) null));
        assertNull(TypeUtils.getTypeElement(processingEnv, (TypeMirror) null));
        assertNull(TypeUtils.getTypeElement(processingEnv, (CharSequence) null));
        assertNull(TypeUtils.getTypeElement(null, (CharSequence) null));
    }

    @Test
    public void testGetSuperDeclaredType() {
        TypeElement gtsTypeElement = TypeUtils.getSuperTypeElement(testType);
        assertEquals(gtsTypeElement, getTypeElement(GenericTestService.class));
        TypeElement dtsTypeElement = TypeUtils.getSuperTypeElement(gtsTypeElement);
        assertEquals(dtsTypeElement, getTypeElement(DefaultTestService.class));

        TypeMirror gtsType = getSuperDeclaredType(testType.asType());
        assertEquals(gtsType, getTypeElement(GenericTestService.class).asType());
        TypeMirror dtsType = getSuperDeclaredType(gtsType);
        assertEquals(dtsType, getTypeElement(DefaultTestService.class).asType());

        assertNull(TypeUtils.getSuperTypeElement((TypeElement) null));
        assertNull(getSuperDeclaredType((TypeMirror) null));
    }

    @Test
    public void testGetAllSuperTypeElements() {
        List<?> allSuperTypes = TypeUtils.getAllSuperTypeElements(testType);
        Iterator<?> iterator = allSuperTypes.iterator();
        assertEquals(3, allSuperTypes.size());
        assertEquals(iterator.next(), getTypeElement(GenericTestService.class));
        assertEquals(iterator.next(), getTypeElement(DefaultTestService.class));
        assertEquals(iterator.next(), getTypeElement(Object.class));

        allSuperTypes = TypeUtils.getAllSuperTypeElements(testType);
        iterator = allSuperTypes.iterator();
        assertEquals(3, allSuperTypes.size());
        assertEquals(iterator.next(), getTypeElement(GenericTestService.class));
        assertEquals(iterator.next(), getTypeElement(DefaultTestService.class));
        assertEquals(iterator.next(), getTypeElement(Object.class));

        assertTrue(TypeUtils.getAllSuperTypeElements((TypeElement) null).isEmpty());
        assertTrue(TypeUtils.getAllSuperDeclaredTypes((TypeMirror) null).isEmpty());
    }

    @Test
    public void testListDeclaredTypes() {
        List<DeclaredType> types = listDeclaredTypes(ofList(testType, testType, testType));
        assertEquals(1, types.size());
        assertEquals(ofDeclaredType(testType), types.get(0));

        types = listDeclaredTypes(ofList(new Element[]{null}));
        assertTrue(types.isEmpty());
    }

    @Test
    public void testListTypeElements() {
        List<TypeElement> typeElements = listTypeElements(ofList(testType.asType(), ofDeclaredType(testType)));
        assertEquals(1, typeElements.size());
        assertEquals(testType, typeElements.get(0));

        typeElements = listTypeElements(ofList(types.getPrimitiveType(TypeKind.BYTE), types.getNullType(), types.getNoType(TypeKind.NONE)));
        assertTrue(typeElements.isEmpty());

        typeElements = listTypeElements(ofList(new TypeMirror[]{null}));
        assertTrue(typeElements.isEmpty());

        typeElements = listTypeElements(null);
        assertTrue(typeElements.isEmpty());
    }

    @Test
    @Disabled("Failed due to github action env problem")
    public void testGetResource() throws URISyntaxException {
        URL resource = getResource(processingEnv, testType);
        assertNotNull(resource);
        assertTrue(new File(resource.toURI()).exists());
        assertEquals(resource, getResource(processingEnv, testType.asType()));
        assertEquals(resource, getResource(processingEnv, "io.microsphere.annotation.processor.TestServiceImpl"));

        assertThrows(RuntimeException.class, () -> getResource(processingEnv, "NotFound"));
    }

    @Test
    public void testGetResourceName() {
        assertEquals("java/lang/String.class", getResourceName("java.lang.String"));
        assertNull(getResourceName(null));
    }
}
