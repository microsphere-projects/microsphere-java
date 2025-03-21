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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static io.microsphere.annotation.processor.util.FieldUtils.findField;
import static io.microsphere.annotation.processor.util.FieldUtils.getDeclaredFields;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllInterfaces;
import static io.microsphere.annotation.processor.util.TypeUtils.getAllSuperTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.getHierarchicalTypes;
import static io.microsphere.annotation.processor.util.TypeUtils.getInterfaces;
import static io.microsphere.annotation.processor.util.TypeUtils.getResource;
import static io.microsphere.annotation.processor.util.TypeUtils.getResourceName;
import static io.microsphere.annotation.processor.util.TypeUtils.getSuperType;
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
import static io.microsphere.collection.Lists.ofList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The {@link TypeUtils} Test
 *
 * @since 1.0.0
 */
public class TypeUtilsTest extends AbstractAnnotationProcessingTest {

    private TypeElement testType;

    @Override
    protected void addCompiledClasses(Set<Class<?>> classesToBeCompiled) {
        classesToBeCompiled.add(ArrayTypeModel.class);
        classesToBeCompiled.add(Color.class);
    }

    @Override
    protected void beforeEach() {
        testType = getType(TestServiceImpl.class);
    }

    @Test
    public void testIsSimpleType() {

        assertTrue(isSimpleType(getType(Void.class)));
        assertTrue(isSimpleType(getType(Boolean.class)));
        assertTrue(isSimpleType(getType(Character.class)));
        assertTrue(isSimpleType(getType(Byte.class)));
        assertTrue(isSimpleType(getType(Short.class)));
        assertTrue(isSimpleType(getType(Integer.class)));
        assertTrue(isSimpleType(getType(Long.class)));
        assertTrue(isSimpleType(getType(Float.class)));
        assertTrue(isSimpleType(getType(Double.class)));
        assertTrue(isSimpleType(getType(String.class)));
        assertTrue(isSimpleType(getType(BigDecimal.class)));
        assertTrue(isSimpleType(getType(BigInteger.class)));
        assertTrue(isSimpleType(getType(Date.class)));
        assertTrue(isSimpleType(getType(Object.class)));

        assertFalse(isSimpleType(getType(getClass())));
        assertFalse(isSimpleType((TypeElement) null));
        assertFalse(isSimpleType((TypeMirror) null));
    }

    @Test
    public void testIsSameType() {
        assertTrue(isSameType(getType(Void.class).asType(), "java.lang.Void"));
        assertFalse(isSameType(getType(String.class).asType(), "java.lang.Void"));

        assertFalse(isSameType(getType(Void.class).asType(), (Type) null));
        assertFalse(isSameType(null, (Type) null));

        assertFalse(isSameType(getType(Void.class).asType(), (String) null));
        assertFalse(isSameType(null, (String) null));
    }

    @Test
    public void testIsArrayType() {
        TypeElement type = getType(ArrayTypeModel.class);
        assertTrue(isArrayType(findField(type.asType(), "integers").asType()));
        assertTrue(isArrayType(findField(type.asType(), "strings").asType()));
        assertTrue(isArrayType(findField(type.asType(), "primitiveTypeModels").asType()));
        assertTrue(isArrayType(findField(type.asType(), "models").asType()));
        assertTrue(isArrayType(findField(type.asType(), "colors").asType()));

        assertFalse(isArrayType((Element) null));
        assertFalse(isArrayType((TypeMirror) null));
    }

    @Test
    public void testIsEnumType() {
        TypeElement type = getType(Color.class);
        assertTrue(isEnumType(type.asType()));

        type = getType(ArrayTypeModel.class);
        assertFalse(isEnumType(type.asType()));

        assertFalse(isEnumType((Element) null));
        assertFalse(isEnumType((TypeMirror) null));
    }

    @Test
    public void testIsClassType() {
        TypeElement type = getType(ArrayTypeModel.class);
        assertTrue(isClassType(type.asType()));

        type = getType(Model.class);
        assertTrue(isClassType(type.asType()));

        assertFalse(isClassType((Element) null));
        assertFalse(isClassType((TypeMirror) null));
    }

    @Test
    public void testIsPrimitiveType() {
        TypeElement type = getType(PrimitiveTypeModel.class);
        getDeclaredFields(type.asType())
                .stream()
                .map(VariableElement::asType)
                .forEach(t -> assertTrue(isPrimitiveType(t)));

        assertFalse(isPrimitiveType(getType(ArrayTypeModel.class)));

        assertFalse(isPrimitiveType((Element) null));
        assertFalse(isPrimitiveType((TypeMirror) null));
    }

    @Test
    public void testIsInterfaceType() {
        TypeElement type = getType(CharSequence.class);
        assertTrue(isInterfaceType(type));
        assertTrue(isInterfaceType(type.asType()));

        type = getType(Model.class);
        assertFalse(isInterfaceType(type));
        assertFalse(isInterfaceType(type.asType()));

        assertFalse(isInterfaceType((Element) null));
        assertFalse(isInterfaceType((TypeMirror) null));
    }

    @Test
    public void testIsAnnotationType() {
        TypeElement type = getType(Override.class);

        assertTrue(isAnnotationType(type));
        assertTrue(isAnnotationType(type.asType()));

        type = getType(Model.class);
        assertFalse(isAnnotationType(type));
        assertFalse(isAnnotationType(type.asType()));

        assertFalse(isAnnotationType((Element) null));
        assertFalse(isAnnotationType((TypeMirror) null));
    }

    @Test
    public void testGetHierarchicalTypes() {
        Set hierarchicalTypes = getHierarchicalTypes(testType.asType(), true, true, true);
        Iterator iterator = hierarchicalTypes.iterator();
        assertEquals(8, hierarchicalTypes.size());
        assertEquals("io.microsphere.annotation.processor.TestServiceImpl", iterator.next().toString());
        assertEquals("io.microsphere.annotation.processor.GenericTestService", iterator.next().toString());
        assertEquals("io.microsphere.annotation.processor.DefaultTestService", iterator.next().toString());
        assertEquals("java.lang.Object", iterator.next().toString());
        assertEquals("io.microsphere.annotation.processor.TestService", iterator.next().toString());
        assertEquals("java.lang.AutoCloseable", iterator.next().toString());
        assertEquals("java.io.Serializable", iterator.next().toString());
        assertEquals("java.util.EventListener", iterator.next().toString());

        hierarchicalTypes = getHierarchicalTypes(testType);
        iterator = hierarchicalTypes.iterator();
        assertEquals(8, hierarchicalTypes.size());
        assertEquals("io.microsphere.annotation.processor.TestServiceImpl", iterator.next().toString());
        assertEquals("io.microsphere.annotation.processor.GenericTestService", iterator.next().toString());
        assertEquals("io.microsphere.annotation.processor.DefaultTestService", iterator.next().toString());
        assertEquals("java.lang.Object", iterator.next().toString());
        assertEquals("io.microsphere.annotation.processor.TestService", iterator.next().toString());
        assertEquals("java.lang.AutoCloseable", iterator.next().toString());
        assertEquals("java.io.Serializable", iterator.next().toString());
        assertEquals("java.util.EventListener", iterator.next().toString());

        hierarchicalTypes = getHierarchicalTypes(testType.asType(), Object.class);
        iterator = hierarchicalTypes.iterator();
        assertEquals(7, hierarchicalTypes.size());
        assertEquals("io.microsphere.annotation.processor.TestServiceImpl", iterator.next().toString());
        assertEquals("io.microsphere.annotation.processor.GenericTestService", iterator.next().toString());
        assertEquals("io.microsphere.annotation.processor.DefaultTestService", iterator.next().toString());
        assertEquals("io.microsphere.annotation.processor.TestService", iterator.next().toString());
        assertEquals("java.lang.AutoCloseable", iterator.next().toString());
        assertEquals("java.io.Serializable", iterator.next().toString());
        assertEquals("java.util.EventListener", iterator.next().toString());

        hierarchicalTypes = getHierarchicalTypes(testType.asType(), true, true, false);
        iterator = hierarchicalTypes.iterator();
        assertEquals(4, hierarchicalTypes.size());
        assertEquals("io.microsphere.annotation.processor.TestServiceImpl", iterator.next().toString());
        assertEquals("io.microsphere.annotation.processor.GenericTestService", iterator.next().toString());
        assertEquals("io.microsphere.annotation.processor.DefaultTestService", iterator.next().toString());
        assertEquals("java.lang.Object", iterator.next().toString());

        hierarchicalTypes = getHierarchicalTypes(testType.asType(), true, false, true);
        iterator = hierarchicalTypes.iterator();
        assertEquals(5, hierarchicalTypes.size());
        assertEquals("io.microsphere.annotation.processor.TestServiceImpl", iterator.next().toString());
        assertEquals("io.microsphere.annotation.processor.TestService", iterator.next().toString());
        assertEquals("java.lang.AutoCloseable", iterator.next().toString());
        assertEquals("java.io.Serializable", iterator.next().toString());
        assertEquals("java.util.EventListener", iterator.next().toString());

        hierarchicalTypes = getHierarchicalTypes(testType.asType(), false, false, true);
        iterator = hierarchicalTypes.iterator();
        assertEquals(4, hierarchicalTypes.size());
        assertEquals("io.microsphere.annotation.processor.TestService", iterator.next().toString());
        assertEquals("java.lang.AutoCloseable", iterator.next().toString());
        assertEquals("java.io.Serializable", iterator.next().toString());
        assertEquals("java.util.EventListener", iterator.next().toString());

        hierarchicalTypes = getHierarchicalTypes(testType.asType(), true, false, false);
        iterator = hierarchicalTypes.iterator();
        assertEquals(1, hierarchicalTypes.size());
        assertEquals("io.microsphere.annotation.processor.TestServiceImpl", iterator.next().toString());

        hierarchicalTypes = getHierarchicalTypes(testType.asType(), false, false, false);
        assertEquals(0, hierarchicalTypes.size());

        assertTrue(getHierarchicalTypes((TypeElement) null).isEmpty());
        assertTrue(getHierarchicalTypes((TypeMirror) null).isEmpty());
    }


    @Test
    public void testGetInterfaces() {
        TypeElement type = getType(Model.class);
        List<TypeMirror> interfaces = getInterfaces(type);
        assertTrue(interfaces.isEmpty());

        interfaces = getInterfaces(testType.asType());

        assertEquals(3, interfaces.size());
        assertEquals("io.microsphere.annotation.processor.TestService", interfaces.get(0).toString());
        assertEquals("java.lang.AutoCloseable", interfaces.get(1).toString());
        assertEquals("java.io.Serializable", interfaces.get(2).toString());

        assertTrue(getInterfaces((TypeElement) null).isEmpty());
        assertTrue(getInterfaces((TypeMirror) null).isEmpty());
    }

    @Test
    public void testGetAllInterfaces() {
        Set<? extends TypeMirror> interfaces = getAllInterfaces(testType.asType());
        assertEquals(4, interfaces.size());
        Iterator<? extends TypeMirror> iterator = interfaces.iterator();
        assertEquals("io.microsphere.annotation.processor.TestService", iterator.next().toString());
        assertEquals("java.lang.AutoCloseable", iterator.next().toString());
        assertEquals("java.io.Serializable", iterator.next().toString());
        assertEquals("java.util.EventListener", iterator.next().toString());

        Set<TypeElement> allInterfaces = getAllInterfaces(testType);
        assertEquals(4, interfaces.size());

        Iterator<TypeElement> allIterator = allInterfaces.iterator();
        assertEquals("io.microsphere.annotation.processor.TestService", allIterator.next().toString());
        assertEquals("java.lang.AutoCloseable", allIterator.next().toString());
        assertEquals("java.io.Serializable", allIterator.next().toString());
        assertEquals("java.util.EventListener", allIterator.next().toString());

        assertTrue(getAllInterfaces((TypeElement) null).isEmpty());
        assertTrue(getAllInterfaces((TypeMirror) null).isEmpty());
    }

    @Test
    public void testGetType() {
        TypeElement element = TypeUtils.getType(processingEnv, String.class);
        assertEquals(element, TypeUtils.getType(processingEnv, element.asType()));
        assertEquals(element, TypeUtils.getType(processingEnv, "java.lang.String"));

        assertNull(TypeUtils.getType(processingEnv, (Type) null));
        assertNull(TypeUtils.getType(processingEnv, (TypeMirror) null));
        assertNull(TypeUtils.getType(processingEnv, (CharSequence) null));
        assertNull(TypeUtils.getType(null, (CharSequence) null));
    }

    @Test
    public void testGetSuperType() {
        TypeElement gtsTypeElement = getSuperType(testType);
        assertEquals(gtsTypeElement, getType(GenericTestService.class));
        TypeElement dtsTypeElement = getSuperType(gtsTypeElement);
        assertEquals(dtsTypeElement, getType(DefaultTestService.class));

        TypeMirror gtsType = getSuperType(testType.asType());
        assertEquals(gtsType, getType(GenericTestService.class).asType());
        TypeMirror dtsType = getSuperType(gtsType);
        assertEquals(dtsType, getType(DefaultTestService.class).asType());

        assertNull(getSuperType((TypeElement) null));
        assertNull(getSuperType((TypeMirror) null));
    }

    @Test
    public void testGetAllSuperTypes() {
        Set<?> allSuperTypes = getAllSuperTypes(testType);
        Iterator<?> iterator = allSuperTypes.iterator();
        assertEquals(3, allSuperTypes.size());
        assertEquals(iterator.next(), getType(GenericTestService.class));
        assertEquals(iterator.next(), getType(DefaultTestService.class));
        assertEquals(iterator.next(), getType(Object.class));

        allSuperTypes = getAllSuperTypes(testType);
        iterator = allSuperTypes.iterator();
        assertEquals(3, allSuperTypes.size());
        assertEquals(iterator.next(), getType(GenericTestService.class));
        assertEquals(iterator.next(), getType(DefaultTestService.class));
        assertEquals(iterator.next(), getType(Object.class));

        assertTrue(getAllSuperTypes((TypeElement) null).isEmpty());
        assertTrue(getAllSuperTypes((TypeMirror) null).isEmpty());
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
    public void testOfDeclaredType() {
        assertEquals(testType.asType(), ofDeclaredType(testType));
        assertEquals(testType.asType(), ofDeclaredType(testType.asType()));
        assertEquals(ofDeclaredType(testType), ofDeclaredType(testType.asType()));

        assertNull(ofDeclaredType((Element) null));
        assertNull(ofDeclaredType((TypeMirror) null));
    }

    @Test
    public void testIsTypeElement() {
        assertTrue(isTypeElement(testType));
        assertTrue(isTypeElement(testType.asType()));

        assertFalse(isTypeElement((Element) null));
        assertFalse(isTypeElement((TypeMirror) null));
    }

    @Test
    public void testOfTypeElement() {
        assertEquals(testType, ofTypeElement(testType));
        assertEquals(testType, ofTypeElement(testType.asType()));

        assertNull(ofTypeElement((Element) null));
        assertNull(ofTypeElement((TypeMirror) null));
    }

    @Test
    public void testOfDeclaredTypes() {
        Set<DeclaredType> declaredTypes = ofDeclaredTypes(ofList(getType(String.class), getType(TestServiceImpl.class), getType(Color.class)));
        assertTrue(declaredTypes.contains(getType(String.class).asType()));
        assertTrue(declaredTypes.contains(getType(TestServiceImpl.class).asType()));
        assertTrue(declaredTypes.contains(getType(Color.class).asType()));

        assertTrue(ofDeclaredTypes(null).isEmpty());
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
