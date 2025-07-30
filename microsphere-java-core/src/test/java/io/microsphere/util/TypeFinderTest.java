package io.microsphere.util;

import io.microsphere.AbstractTestCase;
import io.microsphere.test.BF3;
import io.microsphere.test.StringBF2;
import io.microsphere.test.StringIntegerF1;
import io.microsphere.test.StringIntegerToBoolean;
import io.microsphere.test.StringIntegerToBooleanClass;
import io.microsphere.util.TypeFinder.Include;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.function.BiFunction;

import static io.microsphere.reflect.TypeUtils.NON_OBJECT_TYPE_FILTER;
import static io.microsphere.util.TypeFinder.Include.HIERARCHICAL;
import static io.microsphere.util.TypeFinder.Include.INTERFACES;
import static io.microsphere.util.TypeFinder.Include.SUPER_CLASS;
import static io.microsphere.util.TypeFinder.classFinder;
import static io.microsphere.util.TypeFinder.genericTypeFinder;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TypeFinder} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see io.microsphere.util.ClassFinder
 * @since 1.0.0
 */
class TypeFinderTest extends AbstractTestCase {

    @Test
    void testConstructorOnNullType() {
        assertThrows(IllegalArgumentException.class, () -> classFinder(null, true, true, true, true));
        assertThrows(IllegalArgumentException.class, () -> classFinder(null, Include.values()));
        assertThrows(IllegalArgumentException.class, () -> classFinder(null, true, true, true, true));
        assertThrows(IllegalArgumentException.class, () -> genericTypeFinder(null, Include.values()));
        assertThrows(IllegalArgumentException.class, () -> genericTypeFinder(null, true, true, true, true));
    }

    @Test
    void testConstructorOnNullIncludes() {
        assertThrows(IllegalArgumentException.class, () -> classFinder(StringIntegerToBoolean.class, null));
        assertThrows(IllegalArgumentException.class, () -> genericTypeFinder(StringIntegerToBoolean.class, null));
    }

    @Test
    void testConstructorOnEmptyIncludes() {
        assertThrows(IllegalArgumentException.class, () -> classFinder(StringIntegerToBoolean.class, new Include[0]));
        assertThrows(IllegalArgumentException.class, () -> genericTypeFinder(StringIntegerToBoolean.class, new Include[0]));
    }

    @Test
    void testConstructorOnNullTypeAndIncludes() {
        assertThrows(IllegalArgumentException.class, () -> classFinder(StringIntegerToBoolean.class, new Include[]{null}));
        assertThrows(IllegalArgumentException.class, () -> genericTypeFinder(StringIntegerToBoolean.class, new Include[]{null}));
    }

    @Test
    void testGetAllSuperClasses() {
        TypeFinder<Class<?>> classFinder = classFinder(StringIntegerToBooleanClass.class, HIERARCHICAL, SUPER_CLASS);
        List<Class<?>> types = classFinder.getTypes();
        assertValues(types, Object.class);
    }

    @Test
    void testGetAllInterfaces() {
        TypeFinder<Class<?>> classFinder = classFinder(StringIntegerToBooleanClass.class, HIERARCHICAL, INTERFACES);
        List<Class<?>> types = classFinder.getTypes();
        assertValues(types, StringIntegerToBoolean.class, StringIntegerF1.class, StringBF2.class, BF3.class, BiFunction.class);
    }

    @Test
    void testGetAllInheritedClasses() {
        TypeFinder<Class<?>> classFinder = classFinder(StringIntegerToBooleanClass.class, HIERARCHICAL, SUPER_CLASS, INTERFACES);
        List<Class<?>> types = classFinder.getTypes();
        assertValues(types, Object.class, StringIntegerToBoolean.class, StringIntegerF1.class, StringBF2.class, BF3.class, BiFunction.class);
    }

    @Test
    void testGetAllClasses() {
        TypeFinder<Class<?>> classFinder = classFinder(StringIntegerToBooleanClass.class, Include.values());
        List<Class<?>> types = classFinder.getTypes();
        assertValues(types, StringIntegerToBooleanClass.class, Object.class, StringIntegerToBoolean.class, StringIntegerF1.class, StringBF2.class, BF3.class, BiFunction.class);
    }

    @Test
    void testFindAllSuperClasses() {
        TypeFinder<Class<?>> classFinder = classFinder(StringIntegerToBooleanClass.class, HIERARCHICAL, SUPER_CLASS);
        List<Class<?>> types = classFinder.findTypes(NON_OBJECT_TYPE_FILTER);
        assertSame(emptyList(), types);
    }

    @Test
    void testFindAllInterfaces() {
        TypeFinder<Class<?>> classFinder = classFinder(StringIntegerToBooleanClass.class, HIERARCHICAL, INTERFACES);
        List<Class<?>> types = classFinder.findTypes(NON_OBJECT_TYPE_FILTER);
        assertValues(types, StringIntegerToBoolean.class, StringIntegerF1.class, StringBF2.class, BF3.class, BiFunction.class);
    }

    @Test
    void testFindAllInheritedClasses() {
        TypeFinder<Class<?>> classFinder = classFinder(StringIntegerToBooleanClass.class, HIERARCHICAL, SUPER_CLASS, INTERFACES);
        List<Class<?>> types = classFinder.findTypes(NON_OBJECT_TYPE_FILTER);
        assertValues(types, StringIntegerToBoolean.class, StringIntegerF1.class, StringBF2.class, BF3.class, BiFunction.class);
    }

    @Test
    void testFindAllClasses() {
        TypeFinder<Class<?>> classFinder = classFinder(StringIntegerToBooleanClass.class, Include.values());
        List<Class<?>> types = classFinder.findTypes(NON_OBJECT_TYPE_FILTER);
        assertValues(types, StringIntegerToBooleanClass.class, StringIntegerToBoolean.class, StringIntegerF1.class, StringBF2.class, BF3.class, BiFunction.class);
    }

    @Test
    void testGetAllGenericSuperClasses() {
        TypeFinder<Type> genericTypeFinder = genericTypeFinder(StringIntegerToBooleanClass.class, HIERARCHICAL, SUPER_CLASS);
        List<Type> types = genericTypeFinder.getTypes();
        assertValues(types, Object.class);
    }

    @Test
    void testGetAllGenericInterfaces() {
        TypeFinder<Type> genericTypeFinder = genericTypeFinder(StringIntegerToBooleanClass.class, HIERARCHICAL, INTERFACES);
        List<Type> types = genericTypeFinder.getTypes();
        assertEquals(5, types.size());
        assertGenericInterfaces(types);
    }

    @Test
    void testGetAllGenericInheritedClasses() {
        TypeFinder<Type> genericTypeFinder = genericTypeFinder(StringIntegerToBooleanClass.class, HIERARCHICAL, SUPER_CLASS, INTERFACES);
        List<Type> types = genericTypeFinder.getTypes();
        assertEquals(6, types.size());
        assertEquals(Object.class, types.get(0));
        assertGenericInterfaces(types);
    }

    @Test
    void testGetAllGenericClasses() {
        TypeFinder<Type> genericTypeFinder = genericTypeFinder(StringIntegerToBooleanClass.class, Include.values());
        List<Type> types = genericTypeFinder.getTypes();
        assertEquals(7, types.size());
        assertEquals(StringIntegerToBooleanClass.class, types.get(0));
        assertEquals(Object.class, types.get(1));
        assertGenericInterfaces(types);
    }

    @Test
    void testFindAllGenericSuperClasses() {
        TypeFinder<Type> genericTypeFinder = genericTypeFinder(StringIntegerToBooleanClass.class, HIERARCHICAL, SUPER_CLASS);
        List<Type> types = genericTypeFinder.findTypes(NON_OBJECT_TYPE_FILTER);
        assertSame(emptyList(), types);
    }

    @Test
    void testFindAllGenericInterfaces() {
        TypeFinder<Type> genericTypeFinder = genericTypeFinder(StringIntegerToBooleanClass.class, HIERARCHICAL, INTERFACES);
        List<Type> types = genericTypeFinder.findTypes(NON_OBJECT_TYPE_FILTER);
        assertEquals(5, types.size());
        assertGenericInterfaces(types);
    }

    @Test
    void testFindAllGenericInheritedClasses() {
        TypeFinder<Type> genericTypeFinder = genericTypeFinder(StringIntegerToBooleanClass.class, HIERARCHICAL, SUPER_CLASS, INTERFACES);
        List<Type> types = genericTypeFinder.findTypes(NON_OBJECT_TYPE_FILTER);
        assertEquals(5, types.size());
        assertGenericInterfaces(types);
    }

    @Test
    void testFindAllGenericClasses() {
        TypeFinder<Type> genericTypeFinder = genericTypeFinder(StringIntegerToBooleanClass.class, Include.values());
        List<Type> types = genericTypeFinder.findTypes(NON_OBJECT_TYPE_FILTER);
        assertEquals(6, types.size());
        assertEquals(StringIntegerToBooleanClass.class, types.get(0));
        assertGenericInterfaces(types);
    }

    private void assertGenericInterfaces(List<Type> types) {
        assertEquals(StringIntegerToBoolean.class, types.get(types.size() - 5));
        assertStringIntegerF1(types.get(types.size() - 4));
        assertStringBF2(types.get(types.size() - 3));
        assertBF3(types.get(types.size() - 2));
        assertBiFunction(types.get(types.size() - 1));
    }

    private void assertStringIntegerF1(Type type) {
        assertNotNull(type);
        assertTrue(type instanceof ParameterizedType);
        ParameterizedType parameterizedType = (ParameterizedType) type;
        assertEquals(StringIntegerF1.class, parameterizedType.getRawType());
        assertEquals(Boolean.class, parameterizedType.getActualTypeArguments()[0]);
    }

    private void assertBF3(Type type) {
        assertNotNull(type);
        assertTrue(type instanceof ParameterizedType);
        ParameterizedType parameterizedType = (ParameterizedType) type;
        assertEquals(BF3.class, parameterizedType.getRawType());
        assertEquals(String.class, parameterizedType.getActualTypeArguments()[0]);

        Type type1 = parameterizedType.getActualTypeArguments()[1];
        assertTrue(type1 instanceof TypeVariable);
        TypeVariable typeVariable1 = (TypeVariable) type1;
        assertEquals("U", typeVariable1.getName());
        assertEquals(1, typeVariable1.getBounds().length);
        assertEquals(Object.class, typeVariable1.getBounds()[0]);

        Type type2 = parameterizedType.getActualTypeArguments()[2];
        assertTrue(type2 instanceof TypeVariable);
        TypeVariable typeVariable2 = (TypeVariable) type2;
        assertEquals("R", typeVariable2.getName());
        assertEquals(1, typeVariable2.getBounds().length);
        assertEquals(Object.class, typeVariable2.getBounds()[0]);
    }

    private void assertStringBF2(Type type) {
        assertNotNull(type);
        assertTrue(type instanceof ParameterizedType);
        ParameterizedType parameterizedType = (ParameterizedType) type;
        assertEquals(StringBF2.class, parameterizedType.getRawType());
        assertEquals(Integer.class, parameterizedType.getActualTypeArguments()[0]);
        Type type1 = parameterizedType.getActualTypeArguments()[1];
        assertTrue(type1 instanceof TypeVariable);
        TypeVariable typeVariable = (TypeVariable) type1;
        assertEquals("R", typeVariable.getName());
        assertEquals(Object.class, typeVariable.getBounds()[0]);
    }

    private void assertBiFunction(Type type) {
        assertNotNull(type);
        assertTrue(type instanceof ParameterizedType);
        ParameterizedType parameterizedType = (ParameterizedType) type;
        assertEquals(BiFunction.class, parameterizedType.getRawType());

        Type type1 = parameterizedType.getActualTypeArguments()[0];
        assertTrue(type1 instanceof TypeVariable);
        TypeVariable typeVariable = (TypeVariable) type1;
        assertEquals("T", typeVariable.getName());
        assertEquals(1, typeVariable.getBounds().length);
        assertEquals(Object.class, typeVariable.getBounds()[0]);

        Type type2 = parameterizedType.getActualTypeArguments()[1];
        assertTrue(type2 instanceof TypeVariable);
        TypeVariable typeVariable1 = (TypeVariable) type2;
        assertEquals("U", typeVariable1.getName());
        assertEquals(1, typeVariable1.getBounds().length);
        assertEquals(Object.class, typeVariable1.getBounds()[0]);

        Type type3 = parameterizedType.getActualTypeArguments()[2];
        assertTrue(type3 instanceof TypeVariable);
        TypeVariable typeVariable2 = (TypeVariable) type3;
        assertEquals("R", typeVariable2.getName());
        assertEquals(1, typeVariable2.getBounds().length);
        assertEquals(Object.class, typeVariable2.getBounds()[0]);
    }

}