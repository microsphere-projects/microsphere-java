package io.microsphere.reflect;

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.microsphere.reflect.ReflectionUtils.assertArrayIndex;
import static io.microsphere.reflect.ReflectionUtils.assertArrayType;
import static io.microsphere.reflect.ReflectionUtils.getCallerClass;
import static io.microsphere.reflect.ReflectionUtils.getCallerClassInGeneralJVM;
import static io.microsphere.reflect.ReflectionUtils.getCallerClassInSunJVM;
import static io.microsphere.reflect.ReflectionUtils.getCallerClassName;
import static io.microsphere.reflect.ReflectionUtils.getCallerClassNameInGeneralJVM;
import static io.microsphere.reflect.ReflectionUtils.getCallerClassNameInSunJVM;
import static io.microsphere.reflect.ReflectionUtils.isSupportedSunReflectReflection;
import static io.microsphere.reflect.ReflectionUtils.readFieldsAsMap;
import static io.microsphere.reflect.ReflectionUtils.toList;
import static java.lang.reflect.Array.newInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link ReflectionUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see ReflectionUtilsTest
 * @since 1.0.0
 */
public class ReflectionUtilsTest extends AbstractTestCase {

    @Test
    public void testAssertArrayIndex() {
        int size = 10;
        Object array = newInstance(int.class, size);
        for (int i = 0; i < size; i++) {
            assertArrayIndex(array, i);
        }

        for (int i = size; i < size * 2; i++) {
            ArrayIndexOutOfBoundsException exception = null;
            try {
                assertArrayIndex(array, i);
            } catch (ArrayIndexOutOfBoundsException e) {
                exception = e;
                logger.error(e.getMessage());
            }
            assertNotNull(exception);
        }
    }

    @Test
    public void testAssertArrayTypeOnException() {
        IllegalArgumentException exception = null;
        try {
            assertArrayType(new Object());
        } catch (IllegalArgumentException e) {
            exception = e;
            logger.error(e.getMessage());
        }
        assertNotNull(exception);
    }

    @Test
    public void testAssertArrayType() {
        testAssertArrayType(long.class);
        testAssertArrayType(int.class);
        testAssertArrayType(short.class);
        testAssertArrayType(byte.class);
        testAssertArrayType(boolean.class);
        testAssertArrayType(double.class);
        testAssertArrayType(float.class);
        testAssertArrayType(char.class);
        testAssertArrayType(String.class);
        testAssertArrayType(Object.class);
    }

    private void testAssertArrayType(Class<?> type) {
        Object array = newInstance(type, 0);
        assertArrayType(array);
    }

    @Test
    public void testGetCallerClassX() throws Exception {
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
    public void testGetCallerClassName() {
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
    public void testGetCallerClassNamePerformance() {
        if (isSupportedSunReflectReflection()) {
            for (int i = 0; i < 6; i++) {
                int times = (int) Math.pow(10 + .0, i + .0);
                testGetCallerClassNameInSunJVMPerformance(times);
                testGetCallerClassNameInGeneralJVMPerformance(times);
            }
        }
    }

    private void testGetCallerClassNameInSunJVMPerformance(int times) {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            getCallerClassNameInSunJVM();
        }
        long costTime = System.currentTimeMillis() - startTime;
        logger.info("It's cost to execute ReflectionUtils.getCallerClassNameInSunJVM() {} times : {} ms！", times, costTime);
    }

    private void testGetCallerClassNameInGeneralJVMPerformance(int times) {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            getCallerClassNameInGeneralJVM();
        }
        long costTime = System.currentTimeMillis() - startTime;
        logger.info("It's cost to execute ReflectionUtils.getCallerClassNameInGeneralJVM() {} times : {} ms！", times, costTime);
    }

    @Test
    public void testToList() {
        int[] intArray = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        List<Integer> list = toList(intArray);
        Object expectedList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertEquals(expectedList, list);


        int[][] intIntArray = new int[][]{{1, 2, 3}, {4, 5, 6,}, {7, 8, 9}};
        list = toList(intIntArray);
        expectedList = Arrays.asList(Arrays.asList(1, 2, 3), Arrays.asList(4, 5, 6), Arrays.asList(7, 8, 9));
        assertEquals(expectedList, list);
    }

    @Test
    public void testReadFieldsAsMap() {
        Map<String, Object> map = readFieldsAsMap(new String("abc"));
        assertFalse(map.isEmpty());

        map = readFieldsAsMap(Arrays.asList(1, 2, 3, 4));
        assertFalse(map.isEmpty());

        Map<String, String> value = new HashMap(3);
        value.put("a", "a");
        value.put("b", "b");
        value.put("c", "c");
        map = readFieldsAsMap(value);
        assertFalse(map.isEmpty());
    }
}
