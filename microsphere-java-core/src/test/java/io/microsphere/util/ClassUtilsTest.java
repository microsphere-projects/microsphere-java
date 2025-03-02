/**
 *
 */
package io.microsphere.util;

import io.microsphere.AbstractTestCase;
import io.microsphere.lang.ClassDataRepository;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static io.microsphere.util.ClassUtils.arrayTypeEquals;
import static io.microsphere.util.ClassUtils.concreteClassCache;
import static io.microsphere.util.ClassUtils.findClassNamesInDirectory;
import static io.microsphere.util.ClassUtils.findClassNamesInJarFile;
import static io.microsphere.util.ClassUtils.getTopComponentType;
import static io.microsphere.util.ClassUtils.getTypeName;
import static io.microsphere.util.ClassUtils.isAbstractClass;
import static io.microsphere.util.ClassUtils.isArray;
import static io.microsphere.util.ClassUtils.isConcreteClass;
import static io.microsphere.util.ClassUtils.isFinal;
import static io.microsphere.util.ClassUtils.isGeneralClass;
import static io.microsphere.util.ClassUtils.isPrimitive;
import static io.microsphere.util.ClassUtils.isTopLevelClass;
import static io.microsphere.util.ClassUtils.isWrapperType;
import static io.microsphere.util.ClassUtils.resolvePrimitiveClassName;
import static io.microsphere.util.ClassUtils.resolvePrimitiveType;
import static io.microsphere.util.ClassUtils.resolveWrapperType;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ClassUtils} Test
 *
 * @author <a href="mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see ClassUtilsTest
 * @since 1.0.0
 */
public class ClassUtilsTest extends AbstractTestCase {

    @Test
    public void testIsConcreteClass() {
        assertFalse(isConcreteClass(null));
        assertTrue(isConcreteClass(Object.class));
        assertTrue(isConcreteClass(Object.class));
        assertTrue(isConcreteClass(String.class));
        assertTrue(concreteClassCache.containsKey(Object.class));
        assertTrue(concreteClassCache.containsKey(String.class));
        assertEquals(2, concreteClassCache.size());

        assertFalse(isConcreteClass(CharSequence.class));
        assertFalse(isConcreteClass(AbstractCollection.class));
        assertFalse(isConcreteClass(int.class));
        assertFalse(isConcreteClass(int[].class));
        assertFalse(isConcreteClass(Object[].class));
    }


    @Test
    public void testIsAbstractClass() {
        // test null
        assertFalse(isAbstractClass(null));
        // test interface
        assertFalse(isAbstractClass(Collection.class));
        // test annotation
        assertFalse(isAbstractClass(Override.class));
        // test enum
        assertFalse(isAbstractClass(TimeUnit.class));
        // test primitive
        assertFalse(isAbstractClass(int.class));
        // test array
        assertFalse(isAbstractClass(Object[].class));
        // test abstract class
        assertTrue(isAbstractClass(AbstractCollection.class));
        // test concrete class
        assertFalse(isAbstractClass(Object.class));
    }

    @Test
    public void testIsGeneralClass() {
        // test null
        assertFalse(isGeneralClass(null));
        // test interface
        assertFalse(isGeneralClass(Collection.class));
        // test annotation
        assertFalse(isGeneralClass(Override.class));
        // test enum
        assertFalse(isGeneralClass(TimeUnit.class));
        // test primitive
        assertFalse(isGeneralClass(int.class));
        // test array
        assertFalse(isGeneralClass(Object[].class));
        // test abstract class
        assertFalse(isGeneralClass(AbstractCollection.class));
        // test concrete class
        assertTrue(isGeneralClass(Object.class));
    }

    @Test
    public void testIsTopLevelClass() {
        assertFalse(isTopLevelClass(null));
        assertTrue(isTopLevelClass(Object.class));
        assertTrue(isTopLevelClass(String.class));
        assertFalse(isTopLevelClass(Map.Entry.class));

        class A {

        }

        assertFalse(isTopLevelClass(A.class));
    }

    @Test
    public void testIsPrimitive() {
        assertTrue(isPrimitive(void.class));
        assertTrue(isPrimitive(Void.TYPE));

        assertTrue(isPrimitive(boolean.class));
        assertTrue(isPrimitive(Boolean.TYPE));

        assertTrue(isPrimitive(byte.class));
        assertTrue(isPrimitive(Byte.TYPE));

        assertTrue(isPrimitive(char.class));
        assertTrue(isPrimitive(Character.TYPE));

        assertTrue(isPrimitive(short.class));
        assertTrue(isPrimitive(Short.TYPE));

        assertTrue(isPrimitive(int.class));
        assertTrue(isPrimitive(Integer.TYPE));

        assertTrue(isPrimitive(long.class));
        assertTrue(isPrimitive(Long.TYPE));

        assertTrue(isPrimitive(float.class));
        assertTrue(isPrimitive(Float.TYPE));

        assertTrue(isPrimitive(double.class));
        assertTrue(isPrimitive(Double.TYPE));

        assertFalse(isPrimitive(null));
        assertFalse(isPrimitive(Object.class));
    }

    @Test
    public void testIsArray() {

        // Primitive-Type array
        assertTrue(isArray(int[].class));

        // Object-Type array
        assertTrue(isArray(Object[].class));

        // Dynamic-Type array
        assertTrue(isArray(Array.newInstance(int.class, 0).getClass()));
        assertTrue(isArray(Array.newInstance(Object.class, 0).getClass()));

        // Dynamic multiple-dimension array
        assertTrue(isArray(Array.newInstance(int.class, 0, 3).getClass()));
        assertTrue(isArray(Array.newInstance(Object.class, 0, 3).getClass()));

        // non-array
        assertFalse(isArray(Object.class));
        assertFalse(isArray(int.class));
    }

    @Test
    public void testIsFinal() {
        assertTrue(isFinal(Boolean.TYPE));
        assertTrue(isFinal(Boolean.class));
        assertFalse(isFinal(null));
        assertFalse(isFinal(Object.class));
    }

    @Test
    public void testResolvePrimitiveType() {
        assertEquals(Boolean.TYPE, resolvePrimitiveType(Boolean.TYPE));
        assertEquals(Boolean.TYPE, resolvePrimitiveType(Boolean.class));

        assertEquals(Byte.TYPE, resolvePrimitiveType(Byte.TYPE));
        assertEquals(Byte.TYPE, resolvePrimitiveType(Byte.class));

        assertEquals(Character.TYPE, resolvePrimitiveType(Character.TYPE));
        assertEquals(Character.TYPE, resolvePrimitiveType(Character.class));

        assertEquals(Short.TYPE, resolvePrimitiveType(Short.TYPE));
        assertEquals(Short.TYPE, resolvePrimitiveType(Short.class));

        assertEquals(Integer.TYPE, resolvePrimitiveType(Integer.TYPE));
        assertEquals(Integer.TYPE, resolvePrimitiveType(Integer.class));

        assertEquals(Long.TYPE, resolvePrimitiveType(Long.TYPE));
        assertEquals(Long.TYPE, resolvePrimitiveType(Long.class));

        assertEquals(Float.TYPE, resolvePrimitiveType(Float.TYPE));
        assertEquals(Float.TYPE, resolvePrimitiveType(Float.class));

        assertEquals(Double.TYPE, resolvePrimitiveType(Double.TYPE));
        assertEquals(Double.TYPE, resolvePrimitiveType(Double.class));
    }

    @Test
    public void testResolveWrapperType() {
        assertEquals(Boolean.class, resolveWrapperType(Boolean.TYPE));
        assertEquals(Boolean.class, resolveWrapperType(Boolean.class));

        assertEquals(Byte.class, resolveWrapperType(Byte.TYPE));
        assertEquals(Byte.class, resolveWrapperType(Byte.class));

        assertEquals(Character.class, resolveWrapperType(Character.TYPE));
        assertEquals(Character.class, resolveWrapperType(Character.class));

        assertEquals(Short.class, resolveWrapperType(Short.TYPE));
        assertEquals(Short.class, resolveWrapperType(Short.class));

        assertEquals(Integer.class, resolveWrapperType(Integer.TYPE));
        assertEquals(Integer.class, resolveWrapperType(Integer.class));

        assertEquals(Long.class, resolveWrapperType(Long.TYPE));
        assertEquals(Long.class, resolveWrapperType(Long.class));

        assertEquals(Float.class, resolveWrapperType(Float.TYPE));
        assertEquals(Float.class, resolveWrapperType(Float.class));

        assertEquals(Double.class, resolveWrapperType(Double.TYPE));
        assertEquals(Double.class, resolveWrapperType(Double.class));
    }

    @Test
    public void testIsWrapperType() {
        assertFalse(isWrapperType(Boolean.TYPE));
        assertTrue(isWrapperType(Boolean.class));

        assertFalse(isWrapperType(Byte.TYPE));
        assertTrue(isWrapperType(Byte.class));

        assertFalse(isWrapperType(Character.TYPE));
        assertTrue(isWrapperType(Character.class));

        assertFalse(isWrapperType(Short.TYPE));
        assertTrue(isWrapperType(Short.class));

        assertFalse(isWrapperType(Integer.TYPE));
        assertTrue(isWrapperType(Integer.class));

        assertFalse(isWrapperType(Long.TYPE));
        assertTrue(isWrapperType(Long.class));

        assertFalse(isWrapperType(Float.TYPE));
        assertTrue(isWrapperType(Float.class));

        assertFalse(isWrapperType(Double.TYPE));
        assertTrue(isWrapperType(Double.class));

        assertFalse(isWrapperType(null));
    }

    @Test
    public void testResolvePrimitiveClassName() {
        assertNull(resolvePrimitiveClassName(null));
        assertNull(resolvePrimitiveClassName(""));
        assertNull(resolvePrimitiveClassName(" "));
        assertNull(resolvePrimitiveClassName("java.lang.String"));
        assertEquals(boolean.class, resolvePrimitiveClassName("boolean"));
        assertEquals(byte.class, resolvePrimitiveClassName("byte"));
        assertEquals(char.class, resolvePrimitiveClassName("char"));
        assertEquals(short.class, resolvePrimitiveClassName("short"));
        assertEquals(int.class, resolvePrimitiveClassName("int"));
        assertEquals(long.class, resolvePrimitiveClassName("long"));
        assertEquals(float.class, resolvePrimitiveClassName("float"));
        assertEquals(double.class, resolvePrimitiveClassName("double"));
    }

    @Test
    public void testArrayTypeEquals() {
        assertFalse(arrayTypeEquals(null, null));
        assertFalse(arrayTypeEquals(Object.class, Object.class));
        assertFalse(arrayTypeEquals(Object[].class, Object.class));

        Class<?> oneArrayType = int[].class;
        Class<?> anotherArrayType = int[].class;

        assertTrue(arrayTypeEquals(oneArrayType, anotherArrayType));

        oneArrayType = int[][].class;
        anotherArrayType = int[][].class;
        assertTrue(arrayTypeEquals(oneArrayType, anotherArrayType));

        oneArrayType = int[][][].class;
        anotherArrayType = int[][][].class;
        assertTrue(arrayTypeEquals(oneArrayType, anotherArrayType));

        oneArrayType = int[][][].class;
        anotherArrayType = int[].class;
        assertFalse(arrayTypeEquals(oneArrayType, anotherArrayType));
    }

    @Test
    public void testTypeName() {
        // a) Top level classes
        assertEquals("java.lang.String", getTypeName(String.class));

        // b) Nested classes (static member classes)
        assertEquals("java.util.Map$Entry", getTypeName(Map.Entry.class));

        // c) Inner classes (non-static member classes)
        assertEquals("java.lang.Thread$State", getTypeName(Thread.State.class));

        // d) Local classes (named classes declared within a method)
        class LocalClass {
        }
        assertEquals("io.microsphere.util.ClassUtilsTest$1LocalClass", getTypeName(LocalClass.class));

        // e) Anonymous classes
        Serializable instance = new Serializable() {
        };
        assertEquals("io.microsphere.util.ClassUtilsTest$1", getTypeName(instance.getClass()));

        // f) Array classes
        assertEquals("byte[]", getTypeName(byte[].class));
        assertEquals("char[]", getTypeName(char[].class));
        assertEquals("short[]", getTypeName(short[].class));
        assertEquals("int[]", getTypeName(int[].class));
        assertEquals("long[]", getTypeName(long[].class));
        assertEquals("float[]", getTypeName(float[].class));
        assertEquals("double[]", getTypeName(double[].class));
    }

    @Test
    public void testGetTopComponentType() {
        assertNull(getTopComponentType(null));
        assertNull(getTopComponentType(int.class));
        assertEquals(int.class, getTopComponentType(int[].class));
        assertEquals(int.class, getTopComponentType(int[][].class));
        assertEquals(int.class, getTopComponentType(int[][][].class));
        assertEquals(int.class, getTopComponentType(int[][][][].class));
        assertEquals(int.class, getTopComponentType(int[][][][][].class));
        assertEquals(int.class, getTopComponentType(int[][][][][][].class));
        assertEquals(int.class, getTopComponentType(int[][][][][][][].class));
    }

    @Test
    public void testFindClassNamesInDirectory() {
        assertFindClassNamesMethod(ClassUtilsTest.class, ClassUtils::findClassNamesInDirectory);
    }

    @Test
    public void testFindClassNamesInJarFile() {
        assertFindClassNamesMethod(Nonnull.class, ClassUtils::findClassNamesInJarFile);
    }

    private void assertFindClassNamesMethod(Class<?> targetClassInClassPath, BiFunction<File, Boolean, Set<String>> findClassNamesFunction) {
        // Not exists
        assertSame(emptySet(), findClassNamesFunction.apply(new File("not-exists"), true));

        // Not Found
        assertSame(emptySet(), findClassNamesFunction.apply(new File(SystemUtils.USER_DIR), false));

        // Found
        ClassDataRepository classDataRepository = ClassDataRepository.INSTANCE;
        String path = classDataRepository.findClassPath(targetClassInClassPath);
        assertFalse(findClassNamesFunction.apply(new File(path), true).isEmpty());
    }

}

