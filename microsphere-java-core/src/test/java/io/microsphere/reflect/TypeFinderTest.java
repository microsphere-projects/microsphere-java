package io.microsphere.reflect;

import io.microsphere.AbstractTestCase;
import io.microsphere.test.BF3;
import io.microsphere.test.StringBF2;
import io.microsphere.test.StringIntegerF1;
import io.microsphere.test.StringIntegerToBoolean;
import io.microsphere.test.StringIntegerToBooleanClass;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiFunction;

import static io.microsphere.reflect.TypeFinder.Include.HIERARCHICAL;
import static io.microsphere.reflect.TypeFinder.Include.INTERFACES;
import static io.microsphere.reflect.TypeFinder.Include.SUPER_CLASS;
import static io.microsphere.reflect.TypeFinder.classFinder;
import static io.microsphere.reflect.TypeFinder.genericTypeFinder;
import static io.microsphere.reflect.TypeFinder.of;
import static io.microsphere.reflect.TypeUtils.NON_OBJECT_TYPE_FILTER;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TypeFinder} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see TypeFinder
 * @since 1.0.0
 */
public class TypeFinderTest extends AbstractTestCase {

    @Test
    public void testConstructorOnNullType() {
        assertThrows(IllegalArgumentException.class, () -> of(null, true, true, true, true, true));
        assertThrows(IllegalArgumentException.class, () -> classFinder(null, TypeFinder.Include.values()));
        assertThrows(IllegalArgumentException.class, () -> classFinder(null, true, true, true, true));
        assertThrows(IllegalArgumentException.class, () -> genericTypeFinder(null, TypeFinder.Include.values()));
        assertThrows(IllegalArgumentException.class, () -> genericTypeFinder(null, true, true, true, true));
    }

    @Test
    public void testConstructorOnNullIncludes() {
        assertThrows(IllegalArgumentException.class, () -> classFinder(StringIntegerToBoolean.class, null));
        assertThrows(IllegalArgumentException.class, () -> genericTypeFinder(StringIntegerToBoolean.class, null));
    }

    @Test
    public void testConstructorOnEmptyIncludes() {
        assertThrows(IllegalArgumentException.class, () -> classFinder(StringIntegerToBoolean.class, new TypeFinder.Include[0]));
        assertThrows(IllegalArgumentException.class, () -> genericTypeFinder(StringIntegerToBoolean.class, new TypeFinder.Include[0]));
    }

    @Test
    public void testConstructorOnNullTypeAndIncludes() {
        assertThrows(IllegalArgumentException.class, () -> classFinder(StringIntegerToBoolean.class, new TypeFinder.Include[]{null}));
        assertThrows(IllegalArgumentException.class, () -> genericTypeFinder(StringIntegerToBoolean.class, new TypeFinder.Include[]{null}));
    }

    @Test
    public void testGetAllSuperClasses() {
        TypeFinder<Class<?>> classFinder = classFinder(StringIntegerToBooleanClass.class, HIERARCHICAL, SUPER_CLASS);
        List<Class<?>> types = classFinder.getTypes();
        assertValues(types, Object.class);
    }

    @Test
    public void testGetAllInterfaces() {
        TypeFinder<Class<?>> classFinder = classFinder(StringIntegerToBooleanClass.class, HIERARCHICAL, INTERFACES);
        List<Class<?>> types = classFinder.getTypes();
        assertValues(types, StringIntegerToBoolean.class, StringIntegerF1.class, StringBF2.class, BF3.class, BiFunction.class);
    }

    @Test
    public void testGetAllInheritedClasses() {
        TypeFinder<Class<?>> classFinder = classFinder(StringIntegerToBooleanClass.class, HIERARCHICAL, SUPER_CLASS, INTERFACES);
        List<Class<?>> types = classFinder.getTypes();
        assertValues(types, Object.class, StringIntegerToBoolean.class, StringIntegerF1.class, StringBF2.class, BF3.class, BiFunction.class);
    }

    @Test
    public void testGetAllClasses() {
        TypeFinder<Class<?>> classFinder = classFinder(StringIntegerToBooleanClass.class, TypeFinder.Include.values());
        List<Class<?>> types = classFinder.getTypes();
        assertValues(types, StringIntegerToBooleanClass.class, Object.class, StringIntegerToBoolean.class, StringIntegerF1.class, StringBF2.class, BF3.class, BiFunction.class);
    }

    @Test
    public void testFindAllSuperClasses() {
        TypeFinder<Class<?>> classFinder = classFinder(StringIntegerToBooleanClass.class, HIERARCHICAL, SUPER_CLASS);
        List<Class<?>> types = classFinder.findTypes(NON_OBJECT_TYPE_FILTER);
        assertSame(emptyList(), types);
    }

    @Test
    public void testFindAllInterfaces() {
        TypeFinder<Class<?>> classFinder = classFinder(StringIntegerToBooleanClass.class, HIERARCHICAL, INTERFACES);
        List<Class<?>> types = classFinder.findTypes(NON_OBJECT_TYPE_FILTER);
        assertValues(types, StringIntegerToBoolean.class, StringIntegerF1.class, StringBF2.class, BF3.class, BiFunction.class);
    }

    @Test
    public void testFindAllInheritedClasses() {
        TypeFinder<Class<?>> classFinder = classFinder(StringIntegerToBooleanClass.class, HIERARCHICAL, SUPER_CLASS, INTERFACES);
        List<Class<?>> types = classFinder.findTypes(NON_OBJECT_TYPE_FILTER);
        assertValues(types, StringIntegerToBoolean.class, StringIntegerF1.class, StringBF2.class, BF3.class, BiFunction.class);
    }

    @Test
    public void testFindAllClasses() {
        TypeFinder<Class<?>> classFinder = classFinder(StringIntegerToBooleanClass.class, TypeFinder.Include.values());
        List<Class<?>> types = classFinder.findTypes(NON_OBJECT_TYPE_FILTER);
        assertValues(types, StringIntegerToBooleanClass.class, StringIntegerToBoolean.class, StringIntegerF1.class, StringBF2.class, BF3.class, BiFunction.class);
    }

}