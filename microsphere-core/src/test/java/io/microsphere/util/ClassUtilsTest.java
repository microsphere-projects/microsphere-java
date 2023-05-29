/**
 *
 */
package io.microsphere.util;

import io.microsphere.AbstractTestCase;
import io.microsphere.reflect.ReflectionUtils;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.AbstractCollection;
import java.util.Map;
import java.util.Set;

import static io.microsphere.util.ClassUtils.arrayTypeEquals;
import static io.microsphere.util.ClassUtils.concreteClassCache;
import static io.microsphere.util.ClassUtils.getTypeName;
import static io.microsphere.util.ClassUtils.isArray;
import static io.microsphere.util.ClassUtils.isConcreteClass;
import static io.microsphere.util.ClassUtils.isPrimitive;
import static io.microsphere.util.ClassUtils.isTopLevelClass;
import static io.microsphere.util.ClassUtils.resolvePrimitiveType;
import static io.microsphere.util.ClassUtils.resolveWrapperType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link ClassUtils} {@link TestCase}
 *
 * @author <a href="mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see ClassUtilsTest
 * @since 1.0.0
 */
public class ClassUtilsTest extends AbstractTestCase {

    @Test
    public void testIsConcreteClass() {
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
    public void testIsTopLevelClass() {
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
    public void testArrayTypeEquals() {
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
    public void testGetClassNamesInClassPath() {
        Set<String> classPaths = ClassPathUtils.getClassPaths();
        for (String classPath : classPaths) {
            Set<String> classNames = ClassUtils.getClassNamesInClassPath(classPath, true);
            Assert.assertNotNull(classNames);
        }
    }

    @Test
    public void testGetClassNamesInPackage() {
        Set<String> packageNames = ClassUtils.getAllPackageNamesInClassPaths();
        for (String packageName : packageNames) {
            Set<String> classNames = ClassUtils.getClassNamesInPackage(packageName);
            Assert.assertFalse(classNames.isEmpty());
            Assert.assertNotNull(classNames);
            info(packageName);
            info("\t" + classNames);
        }
    }


    @Test
    public void testGetAllPackageNamesInClassPaths() {
        Set<String> packageNames = ClassUtils.getAllPackageNamesInClassPaths();
        Assert.assertNotNull(packageNames);
        info(packageNames);
    }

    @Test
    public void testFindClassPath() {
        String classPath = ClassUtils.findClassPath(ReflectionUtils.class);
        Assert.assertNotNull(classPath);

        classPath = ClassUtils.findClassPath(String.class);
        Assert.assertNotNull(classPath);
    }

    @Test
    public void testGetAllClassNamesMapInClassPath() {
        Map<String, Set<String>> allClassNamesMapInClassPath = ClassUtils.getClassPathToClassNamesMap();
        Assert.assertFalse(allClassNamesMapInClassPath.isEmpty());
    }

    @Test
    public void testGetAllClassNamesInClassPath() {
        Set<String> allClassNames = ClassUtils.getAllClassNamesInClassPaths();
        Assert.assertFalse(allClassNames.isEmpty());
    }

    @Test
    public void testGetCodeSourceLocation() throws IOException {
        URL codeSourceLocation = null;
        Assert.assertNull(codeSourceLocation);

        codeSourceLocation = ClassUtils.getCodeSourceLocation(ClassUtilsTest.class);
        info("codeSourceLocation : " + codeSourceLocation);
        Assert.assertNotNull(codeSourceLocation);

        codeSourceLocation = ClassUtils.getCodeSourceLocation(String.class);
        info("codeSourceLocation : " + codeSourceLocation);
        Assert.assertNotNull(codeSourceLocation);


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

}
