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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.CharBuffer;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import static io.microsphere.collection.MapUtils.ofEntry;
import static io.microsphere.constants.SymbolConstants.SPACE;
import static io.microsphere.util.ArrayUtils.EMPTY_BOOLEAN_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_BYTE_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_CHAR_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_DOUBLE_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_FLOAT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_INT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_LONG_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_SHORT_ARRAY;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.ClassUtils.ARRAY_SUFFIX;
import static io.microsphere.util.ClassUtils.PRIMITIVE_TYPES;
import static io.microsphere.util.ClassUtils.arrayTypeEquals;
import static io.microsphere.util.ClassUtils.cast;
import static io.microsphere.util.ClassUtils.concreteClassCache;
import static io.microsphere.util.ClassUtils.findAllClasses;
import static io.microsphere.util.ClassUtils.getAllClasses;
import static io.microsphere.util.ClassUtils.getAllInterfaces;
import static io.microsphere.util.ClassUtils.getAllSuperClasses;
import static io.microsphere.util.ClassUtils.getClasses;
import static io.microsphere.util.ClassUtils.getSimpleName;
import static io.microsphere.util.ClassUtils.getTopComponentType;
import static io.microsphere.util.ClassUtils.getType;
import static io.microsphere.util.ClassUtils.getTypeName;
import static io.microsphere.util.ClassUtils.getTypes;
import static io.microsphere.util.ClassUtils.isAbstractClass;
import static io.microsphere.util.ClassUtils.isArray;
import static io.microsphere.util.ClassUtils.isAssignableFrom;
import static io.microsphere.util.ClassUtils.isCharSequence;
import static io.microsphere.util.ClassUtils.isClass;
import static io.microsphere.util.ClassUtils.isConcreteClass;
import static io.microsphere.util.ClassUtils.isDerived;
import static io.microsphere.util.ClassUtils.isFinal;
import static io.microsphere.util.ClassUtils.isGeneralClass;
import static io.microsphere.util.ClassUtils.isNumber;
import static io.microsphere.util.ClassUtils.isPrimitive;
import static io.microsphere.util.ClassUtils.isSimpleType;
import static io.microsphere.util.ClassUtils.isTopLevelClass;
import static io.microsphere.util.ClassUtils.isWrapperType;
import static io.microsphere.util.ClassUtils.newInstance;
import static io.microsphere.util.ClassUtils.resolveClassName;
import static io.microsphere.util.ClassUtils.resolvePackageName;
import static io.microsphere.util.ClassUtils.resolvePrimitiveClassForName;
import static io.microsphere.util.ClassUtils.resolvePrimitiveType;
import static io.microsphere.util.ClassUtils.resolveWrapperType;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ClassUtils} Test
 *
 * @author <a href="mercyblitz@gmail.com">Mercy<a/>
 * @see ClassUtilsTest
 * @since 1.0.0
 */
class ClassUtilsTest extends AbstractTestCase {

    @Test
    void testConstants() {
        assertEquals("[]", ARRAY_SUFFIX);
    }

    @Test
    void testIsArrayWithObject() {
        assertTrue(isArray(EMPTY_OBJECT_ARRAY));
        assertTrue(isArray(EMPTY_BOOLEAN_ARRAY));
        assertFalse(isArray((Object) null));
    }

    @Test
    void testIsArrayWithClass() {

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
    void testIsConcreteClass() {
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
    void testIsAbstractClass() {
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
    void testIsGeneralClass() {
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
        // test concrete class
        assertTrue(isGeneralClass(Object.class, null));
    }

    @Test
    void testIsTopLevelClass() {
        assertFalse(isTopLevelClass(null));
        assertTrue(isTopLevelClass(Object.class));
        assertTrue(isTopLevelClass(String.class));
        assertFalse(isTopLevelClass(Map.Entry.class));

        class A {

        }

        assertFalse(isTopLevelClass(A.class));
    }

    @Test
    void testIsPrimitive() {
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
    void testIsFinal() {
        assertTrue(isFinal(Boolean.TYPE));
        assertTrue(isFinal(Boolean.class));
        assertFalse(isFinal(null));
        assertFalse(isFinal(Object.class));
    }

    @Test
    void testIsSimpleType() {
        assertTrue(isSimpleType(Void.class));
        assertTrue(isSimpleType(Boolean.class));
        assertTrue(isSimpleType(Character.class));
        assertTrue(isSimpleType(Byte.class));
        assertTrue(isSimpleType(Short.class));
        assertTrue(isSimpleType(Integer.class));
        assertTrue(isSimpleType(Long.class));
        assertTrue(isSimpleType(Float.class));
        assertTrue(isSimpleType(Double.class));
        assertTrue(isSimpleType(String.class));
        assertTrue(isSimpleType(BigDecimal.class));
        assertTrue(isSimpleType(BigInteger.class));
        assertTrue(isSimpleType(Date.class));
        assertTrue(isSimpleType(Object.class));

        assertFalse(isSimpleType(StringBuilder.class));
        assertFalse(isSimpleType(null));
    }

    @Test
    void testIsCharSequence() {
        assertTrue(isCharSequence(CharSequence.class));
        assertTrue(isCharSequence(String.class));
        assertTrue(isCharSequence(StringBuilder.class));
        assertTrue(isCharSequence(StringBuffer.class));
        assertTrue(isCharSequence(CharBuffer.class));

        assertFalse(isCharSequence(Character.class));
        assertFalse(isCharSequence(null));
    }

    @Test
    void testIsNumber() {
        assertTrue(isNumber(Number.class));
        assertTrue(isNumber(Byte.class));
        assertTrue(isNumber(Short.class));
        assertTrue(isNumber(Integer.class));
        assertTrue(isNumber(Long.class));
        assertTrue(isNumber(Float.class));
        assertTrue(isNumber(Double.class));
        assertTrue(isNumber(BigDecimal.class));
        assertTrue(isNumber(BigInteger.class));

        assertFalse(isNumber(Character.class));
        assertFalse(isNumber(null));
    }

    @Test
    void testIsClass() {
        assertTrue(isClass(String.class));
        assertFalse(isClass("Hello"));
        assertFalse(isClass(null));
    }

    @Test
    void testResolvePrimitiveType() {
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
    void testResolveWrapperType() {
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
    void testIsWrapperType() {
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
    void testArrayTypeEquals() {
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
    void testResolvePrimitiveClassForName() {
        assertNull(resolvePrimitiveClassForName(null));
        assertNull(resolvePrimitiveClassForName(""));
        assertNull(resolvePrimitiveClassForName(SPACE));
        assertNull(resolvePrimitiveClassForName("java.lang.String"));
        assertEquals(boolean.class, resolvePrimitiveClassForName("boolean"));
        assertEquals(byte.class, resolvePrimitiveClassForName("byte"));
        assertEquals(char.class, resolvePrimitiveClassForName("char"));
        assertEquals(short.class, resolvePrimitiveClassForName("short"));
        assertEquals(int.class, resolvePrimitiveClassForName("int"));
        assertEquals(long.class, resolvePrimitiveClassForName("long"));
        assertEquals(float.class, resolvePrimitiveClassForName("float"));
        assertEquals(double.class, resolvePrimitiveClassForName("double"));
    }

    @Test
    void testResolvePackageName() {
        assertEquals("java.lang", resolvePackageName(Object.class));
    }

    @Test
    void testResolvePackageNameOnPrimitiveType() {
        PRIMITIVE_TYPES.forEach(t -> assertNull(resolvePackageName(t)));
    }

    @Test
    void testFindClassNamesInClassPath() {
        assertFindClassNamesMethod(ClassUtilsTest.class, ClassUtils::findClassNamesInClassPath);
        assertFindClassNamesMethod(Nonnull.class, ClassUtils::findClassNamesInClassPath);
    }

    @Test
    void testFindClassNamesInDirectory() {
        assertFindClassNamesMethod(ClassUtilsTest.class, ClassUtils::findClassNamesInDirectory);
    }

    @Test
    void testFindClassNamesInJarFile() {
        assertFindClassNamesMethod(Nonnull.class, ClassUtils::findClassNamesInJarFile);
    }

    @Test
    void testResolveClassName() {
        assertEquals("java.lang.String", resolveClassName("java/lang/String.class"));
    }

    @Test
    void testGetAllSuperClasses() {
        List<Class<?>> allSuperClasses = getAllSuperClasses(Object.class);
        assertSame(emptyList(), allSuperClasses);

        allSuperClasses = getAllSuperClasses(Map.class);
        assertSame(emptyList(), allSuperClasses);

    }

    @Test
    void testGetAllSuperClassesOnNull() {
        assertSame(emptyList(), getAllSuperClasses(null));
    }

    @Test
    void testGetAllSuperClassesOnPrimitiveType() {
        assertSame(emptyList(), getAllSuperClasses(int.class));
    }

    @Test
    void testGetAllInterfaces() {
        assertSame(emptyList(), getAllInterfaces(null));
        assertSame(emptyList(), getAllInterfaces(int.class));
    }

    @Test
    void testGetAllClasses() {
        List<Class<?>> allClasses = getAllClasses(Object.class);
        assertEquals(1, allClasses.size());
        assertTrue(allClasses.contains(Object.class));

        allClasses = getAllClasses(String.class);
        assertTrue(allClasses.size() >= 5);
        assertTrue(allClasses.contains(Object.class));
        assertTrue(allClasses.contains(Serializable.class));
        assertTrue(allClasses.contains(Comparable.class));
        assertTrue(allClasses.contains(CharSequence.class));
        assertTrue(allClasses.contains(String.class));

        allClasses = getAllClasses(TreeMap.class);
        assertTrue(allClasses.size() >= 8);
        assertTrue(allClasses.contains(Object.class));
        assertTrue(allClasses.contains(Cloneable.class));
        assertTrue(allClasses.contains(Serializable.class));
        assertTrue(allClasses.contains(Map.class));
        assertTrue(allClasses.contains(SortedMap.class));
        assertTrue(allClasses.contains(NavigableMap.class));
        assertTrue(allClasses.contains(AbstractMap.class));
        assertTrue(allClasses.contains(TreeMap.class));
    }

    @Test
    void testGetAllClassesOnNull() {
        assertSame(emptyList(), getAllClasses(null));
    }

    @Test
    void testGetAllClassesOnPrimitiveType() {
        PRIMITIVE_TYPES.forEach(t -> assertSame(emptyList(), getAllClasses(t)));
    }

    @Test
    void testFindAllClasses() {
        List<Class<?>> allClasses = findAllClasses(TreeMap.class, klass -> isAssignableFrom(Map.class, klass));
        assertTrue(allClasses.size() >= 5);
        assertTrue(allClasses.contains(Map.class));
        assertTrue(allClasses.contains(SortedMap.class));
        assertTrue(allClasses.contains(NavigableMap.class));
        assertTrue(allClasses.contains(AbstractMap.class));
        assertTrue(allClasses.contains(TreeMap.class));
    }

    @Test
    void testFindAllClassesOnNull() {
        assertSame(emptyList(), findAllClasses(null, t -> true));
    }

    @Test
    void testFindAllClassesOnPrimitiveType() {
        PRIMITIVE_TYPES.forEach(t -> assertSame(emptyList(), findAllClasses(t, a -> true)));
    }

    @Test
    void testGetTypeNameWithObject() {
        // a) Top level classes
        assertEquals("java.lang.String", getTypeName("Hello,World"));

        // b) Nested classes (static member classes)
        assertEquals("io.microsphere.collection.DefaultEntry", getTypeName(ofEntry("Key", "Value")));

        // c) Inner classes (non-static member classes)
        assertEquals("java.lang.Thread$State", getTypeName(Thread.State.NEW));

        // d) Local classes (named classes declared within a method)
        class LocalClass {
        }
        assertEquals("io.microsphere.util.ClassUtilsTest$1LocalClass", getTypeName(new LocalClass()));

        // e) Anonymous classes
        Serializable instance = new Serializable() {
        };
        assertEquals("io.microsphere.util.ClassUtilsTest$1", getTypeName(instance));

        // f) Array classes
        assertEquals("byte[]", getTypeName(EMPTY_BYTE_ARRAY));
        assertEquals("char[]", getTypeName(EMPTY_CHAR_ARRAY));
        assertEquals("short[]", getTypeName(EMPTY_SHORT_ARRAY));
        assertEquals("int[]", getTypeName(EMPTY_INT_ARRAY));
        assertEquals("long[]", getTypeName(EMPTY_LONG_ARRAY));
        assertEquals("float[]", getTypeName(EMPTY_FLOAT_ARRAY));
        assertEquals("double[]", getTypeName(EMPTY_DOUBLE_ARRAY));
    }

    @Test
    void testGetTypeNameWithClass() {
        // a) Top level classes
        assertEquals("java.lang.String", getTypeName(String.class));

        // b) Nested classes (static member classes)
        assertEquals("java.util.Map$Entry", getTypeName(Map.Entry.class));

        // c) Inner classes (non-static member classes)
        assertEquals("java.lang.Thread$State", getTypeName(Thread.State.class));

        // d) Local classes (named classes declared within a method)
        class LocalClass {
        }
        assertEquals("io.microsphere.util.ClassUtilsTest$2LocalClass", getTypeName(LocalClass.class));

        // e) Anonymous classes
        Serializable instance = new Serializable() {
        };
        assertEquals("io.microsphere.util.ClassUtilsTest$2", getTypeName(instance.getClass()));

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
    void testGetTypeNameWithNullClass() {
        assertNull(getTypeName(null));
    }

    @Test
    void testGetTypeNameWithNullObject() {
        assertNull(getTypeName((Object) null));
    }

    @Test
    void testGetSimpleName() {
        // a) Top level classes
        assertEquals("String", getSimpleName(String.class));

        // b) Nested classes (static member classes)
        assertEquals("Entry", getSimpleName(Map.Entry.class));

        // c) Inner classes (non-static member classes)
        assertEquals("State", getSimpleName(Thread.State.class));

        // d) Local classes (named classes declared within a method)
        class LocalClass {
        }
        assertEquals("LocalClass", getSimpleName(LocalClass.class));

        // e) Anonymous classes
        Serializable instance = new Serializable() {
        };
        assertEquals("", getSimpleName(instance.getClass()));

        // f) Array classes
        assertEquals("byte[]", getSimpleName(byte[].class));
        assertEquals("char[]", getSimpleName(char[].class));
        assertEquals("short[]", getSimpleName(short[].class));
        assertEquals("int[]", getSimpleName(int[].class));
        assertEquals("long[]", getSimpleName(long[].class));
        assertEquals("float[]", getSimpleName(float[].class));
        assertEquals("double[]", getSimpleName(double[].class));
    }

    @Test
    void testGetTopComponentType() {
        assertNull(getTopComponentType((Object) null));
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
    void testIsAssignableFrom() {
        assertFalse(isAssignableFrom(null, null));
        assertFalse(isAssignableFrom(String.class, null));
        assertFalse(isAssignableFrom(null, String.class));
        assertTrue(isAssignableFrom(Object.class, String.class));
        assertFalse(isAssignableFrom(String.class, Object.class));
    }

    @Test
    void testGetType() {
        assertNull(getType(null));
        assertSame(String.class, getType(""));
        assertSame(Class.class, getType(String.class));
    }

    @Test
    void testGetClass() {
        assertNull(ClassUtils.getClass(null));
        assertSame(String.class, ClassUtils.getClass(""));
        assertSame(Class.class, ClassUtils.getClass(String.class));
    }

    @Test
    void testGetTypes() {
        assertSame(EMPTY_CLASS_ARRAY, getTypes(null));
        assertSame(EMPTY_CLASS_ARRAY, getTypes());
        assertSame(EMPTY_CLASS_ARRAY, getTypes(EMPTY_OBJECT_ARRAY));

        assertArrayEquals(ofArray(String.class, Integer.class), getTypes("", Integer.valueOf((1))));
    }

    @Test
    void testGetClasses() {
        assertSame(EMPTY_CLASS_ARRAY, getClasses(null));
        assertSame(EMPTY_CLASS_ARRAY, getClasses());
        assertSame(EMPTY_CLASS_ARRAY, getClasses(EMPTY_OBJECT_ARRAY));

        assertArrayEquals(ofArray(String.class, Integer.class), getClasses("", Integer.valueOf((1))));
    }

    @Test
    void testIsDerived() {
        assertFalse(isDerived(null, null));
        assertFalse(isDerived(String.class, null));
        assertFalse(isDerived(null, String.class));
        assertFalse(isDerived(Object.class, String.class));
        assertTrue(isDerived(String.class, Object.class));
        assertTrue(isDerived(String.class, Object.class, Serializable.class, CharSequence.class));
    }

    @Test
    void testNewInstance() {
        assertEquals("test", newInstance(String.class, "test"));
        assertThrows(IllegalArgumentException.class, () -> newInstance(String.class, 1));
    }

    @Test
    void testCast() {
        assertNull(cast(null, Integer.class));
        assertNull(cast("test", null));
        assertNull(cast("test", Integer.class));
        assertEquals("test", cast("test", CharSequence.class));
    }

    private void assertFindClassNamesMethod(Class<?> targetClassInClassPath, BiFunction<File, Boolean, Set<String>> findClassNamesFunction) {
        // Null
        assertSame(emptySet(), findClassNamesFunction.apply(null, true));

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

