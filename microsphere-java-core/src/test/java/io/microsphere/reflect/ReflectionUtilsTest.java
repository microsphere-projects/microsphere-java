package io.microsphere.reflect;

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.Test;
import io.microsphere.logging.Logger;
import io.microsphere.logging.LoggerFactory;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private static final Logger logger = LoggerFactory.getLogger(ReflectionUtilsTest.class);

    @Test
    public void testAssertArrayIndex() {
        int size = 10;
        Object array = Array.newInstance(int.class, size);

        for (int i = 0; i < size; i++) {
            ReflectionUtils.assertArrayIndex(array, i);
        }

        for (int i = size; i < size * 2; i++) {
            ArrayIndexOutOfBoundsException exception = null;
            try {
                ReflectionUtils.assertArrayIndex(array, i);
            } catch (ArrayIndexOutOfBoundsException e) {
                exception = e;
                System.err.println(e.getMessage());
            }
            assertNotNull(exception);
        }
    }

    @Test
    public void testAssertArrayTypeOnException() {
        IllegalArgumentException exception = null;
        try {
            ReflectionUtils.assertArrayType(new Object());
        } catch (IllegalArgumentException e) {
            exception = e;
            System.err.println(e.getMessage());
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
        Object array = Array.newInstance(type, 0);
        ReflectionUtils.assertArrayType(array);
    }

    @Test
    public void testGetCallerClassX() throws Exception {
        Class<?> expectedClass = ReflectionUtilsTest.class;

        Class<?> callerClass = ReflectionUtils.getCallerClass();
        Class<?> callerClassInSunJVM = ReflectionUtils.getCallerClassInSunJVM();
        Class<?> callerClassInGeneralJVM = ReflectionUtils.getCallerClassInGeneralJVM();

        assertEquals(expectedClass, callerClass);
        assertEquals(callerClassInSunJVM, callerClass);
        assertEquals(callerClassInGeneralJVM, callerClass);

    }

    @Test
    public void testGetCallerClassName() {
        String expectedClassName = ReflectionUtilsTest.class.getName();
        String callerClassName = ReflectionUtils.getCallerClassName();
        String callerClassNameInSunJVM = ReflectionUtils.getCallerClassNameInSunJVM();
        String callerClassNameInGeneralJVM = ReflectionUtils.getCallerClassNameInGeneralJVM();

        assertEquals(expectedClassName, callerClassName);
        assertEquals(callerClassNameInSunJVM, callerClassName);
        assertEquals(callerClassNameInGeneralJVM, callerClassName);
    }

    @Test
    public void testGetCallerPackage() {
        Class<?> expectedClass = ReflectionUtilsTest.class;
        Package expectedPackage = expectedClass.getPackage();

        assertEquals(expectedPackage, ReflectionUtils.getCallerPackage());
    }

    @Test
    public void testGetCallerClassNamePerformance() {

        for (int i = 0; i < 6; i++) {
            int times = (int) Math.pow(10 + .0, i + .0);
            testGetCallerClassNameInSunJVMPerformance(times);
            testGetCallerClassNameInGeneralJVMPerformance(times);
        }
    }

    private void testGetCallerClassNameInSunJVMPerformance(int times) {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            ReflectionUtils.getCallerClassNameInSunJVM();
        }
        long costTime = System.currentTimeMillis() - startTime;
        logger.info("It's cost to execute ReflectionUtils.getCallerClassNameInSunJVM() {} times : {} ms！", times, costTime);
    }

    private void testGetCallerClassNameInGeneralJVMPerformance(int times) {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            ReflectionUtils.getCallerClassNameInGeneralJVM();
        }
        long costTime = System.currentTimeMillis() - startTime;
        logger.info("It's cost to execute ReflectionUtils.getCallerClassNameInGeneralJVM() {} times : {} ms！", times, costTime);
    }

    @Test
    public void testToList() {
        int[] intArray = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        List<Integer> list = ReflectionUtils.toList(intArray);
        Object expectedList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertEquals(expectedList, list);


        int[][] intIntArray = new int[][]{{1, 2, 3}, {4, 5, 6,}, {7, 8, 9}};
        list = ReflectionUtils.toList(intIntArray);
        expectedList = Arrays.asList(Arrays.asList(1, 2, 3), Arrays.asList(4, 5, 6), Arrays.asList(7, 8, 9));
        assertEquals(expectedList, list);
    }

    @Test
    public void testReadFieldsAsMap() {
        Map<String, Object> map = ReflectionUtils.readFieldsAsMap(new String("abc"));
        info(map);

        map = ReflectionUtils.readFieldsAsMap(Arrays.asList(1, 2, 3, 4));
        info(map);

        Map<String, String> value = new HashMap(3);
        value.put("a", "a");
        value.put("b", "b");
        value.put("c", "c");
        map = ReflectionUtils.readFieldsAsMap(value);
        info(map);

    }
}
