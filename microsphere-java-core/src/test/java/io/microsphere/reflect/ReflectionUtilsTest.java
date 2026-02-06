package io.microsphere.reflect;

import io.microsphere.AbstractTestCase;
import io.microsphere.test.Data;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.reflect.ReflectionUtils.INACCESSIBLE_OBJECT_EXCEPTION_CLASS;
import static io.microsphere.reflect.ReflectionUtils.INACCESSIBLE_OBJECT_EXCEPTION_CLASS_NAME;
import static io.microsphere.reflect.ReflectionUtils.getCallerClass;
import static io.microsphere.reflect.ReflectionUtils.getCallerClassInGeneralJVM;
import static io.microsphere.reflect.ReflectionUtils.getCallerClassInSunJVM;
import static io.microsphere.reflect.ReflectionUtils.getCallerClassName;
import static io.microsphere.reflect.ReflectionUtils.getCallerClassNameInGeneralJVM;
import static io.microsphere.reflect.ReflectionUtils.getCallerClassNameInSunJVM;
import static io.microsphere.reflect.ReflectionUtils.isInaccessibleObjectException;
import static io.microsphere.reflect.ReflectionUtils.isSupportedSunReflectReflection;
import static io.microsphere.reflect.ReflectionUtils.readFieldsAsMap;
import static io.microsphere.reflect.ReflectionUtils.toList;
import static io.microsphere.reflect.ReflectionUtils.toObject;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ReflectionUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ReflectionUtilsTest
 * @since 1.0.0
 */
class ReflectionUtilsTest extends AbstractTestCase {

    @Test
    void testGetCallerClassX() {
        Class<?> expectedClass = ReflectionUtilsTest.class;

        Class<?> callerClass = getCallerClass();
        assertEquals(expectedClass, callerClass);

        if (isSupportedSunReflectReflection()) {
            Class<?> callerClassInSunJVM = getCallerClassInSunJVM();
            assertEquals(callerClassInSunJVM, callerClass);
        }

        Class<?> callerClassInGeneralJVM = getCallerClassInGeneralJVM();
        assertEquals(callerClassInGeneralJVM, callerClass);

    }

    @Test
    void testGetCallerClassName() {
        String expectedClassName = ReflectionUtilsTest.class.getName();

        String callerClassName = getCallerClassName();
        assertEquals(expectedClassName, callerClassName);

        if (isSupportedSunReflectReflection()) {
            String callerClassNameInSunJVM = getCallerClassNameInSunJVM();
            assertEquals(callerClassNameInSunJVM, callerClassName);
        }

        String callerClassNameInGeneralJVM = getCallerClassNameInGeneralJVM();
        assertEquals(callerClassNameInGeneralJVM, callerClassName);
    }

    @Test
    void testToList() {
        int[] intArray = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        List<Integer> list = toList(intArray);
        Object expectedList = asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertEquals(expectedList, list);


        int[][] intIntArray = new int[][]{{1, 2, 3}, {4, 5, 6,}, {7, 8, 9}};
        list = toList(intIntArray);
        expectedList = asList(asList(1, 2, 3), asList(4, 5, 6), asList(7, 8, 9));
        assertEquals(expectedList, list);
    }

    @Test
    void testReadFieldsAsMap() {
        Map<String, Object> map = readFieldsAsMap(new String("abc"));
        assertFalse(map.isEmpty());

        map = readFieldsAsMap(asList(1, 2, 3, 4));
        assertFalse(map.isEmpty());

        Map<String, String> value = new HashMap(3);
        value.put("a", "a");
        value.put("b", "b");
        value.put("c", "c");
        map = readFieldsAsMap(value);
        assertFalse(map.isEmpty());

        T t = new T();
        t.self = t;
        t.other = new T();

        map = readFieldsAsMap(t);
        assertFalse(map.isEmpty());
    }

    @Test
    void testToObject() {
        assertNull(toObject(null));
        assertEquals("test", toObject("test"));
        assertEquals(ofList("a", "b", "c"), toObject(ofArray("a", "b", "c")));
    }

    @Test
    void testIsInaccessibleObjectException() {
        assertFalse(isInaccessibleObjectException(new RuntimeException()));
        assertFalse(isInaccessibleObjectException((Throwable) null));
        assertFalse(isInaccessibleObjectException((Class) null));
        assertFalse(isInaccessibleObjectException(Class.class));
        assertEquals(INACCESSIBLE_OBJECT_EXCEPTION_CLASS != null, isInaccessibleObjectException(INACCESSIBLE_OBJECT_EXCEPTION_CLASS));
        assertTrue(isInaccessibleObjectException(INACCESSIBLE_OBJECT_EXCEPTION_CLASS_NAME));
    }

    static class T extends Data {

        private T self;

        private T other;
    }
}
