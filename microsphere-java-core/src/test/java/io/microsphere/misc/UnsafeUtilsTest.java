package io.microsphere.misc;

import io.microsphere.reflect.MemberUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Set;

import static io.microsphere.misc.UnsafeUtils.BOOLEAN_ARRAY_BASE_OFFSET;
import static io.microsphere.misc.UnsafeUtils.BOOLEAN_ARRAY_INDEX_SCALE;
import static io.microsphere.misc.UnsafeUtils.BYTE_ARRAY_BASE_OFFSET;
import static io.microsphere.misc.UnsafeUtils.BYTE_ARRAY_INDEX_SCALE;
import static io.microsphere.misc.UnsafeUtils.CHAR_ARRAY_BASE_OFFSET;
import static io.microsphere.misc.UnsafeUtils.CHAR_ARRAY_INDEX_SCALE;
import static io.microsphere.misc.UnsafeUtils.DOUBLE_ARRAY_BASE_OFFSET;
import static io.microsphere.misc.UnsafeUtils.DOUBLE_ARRAY_INDEX_SCALE;
import static io.microsphere.misc.UnsafeUtils.FLOAT_ARRAY_BASE_OFFSET;
import static io.microsphere.misc.UnsafeUtils.FLOAT_ARRAY_INDEX_SCALE;
import static io.microsphere.misc.UnsafeUtils.INT_ARRAY_BASE_OFFSET;
import static io.microsphere.misc.UnsafeUtils.INT_ARRAY_INDEX_SCALE;
import static io.microsphere.misc.UnsafeUtils.LONG_ARRAY_BASE_OFFSET;
import static io.microsphere.misc.UnsafeUtils.LONG_ARRAY_INDEX_SCALE;
import static io.microsphere.misc.UnsafeUtils.OBJECT_ARRAY_BASE_OFFSET;
import static io.microsphere.misc.UnsafeUtils.OBJECT_ARRAY_INDEX_SCALE;
import static io.microsphere.misc.UnsafeUtils.SHORT_ARRAY_BASE_OFFSET;
import static io.microsphere.misc.UnsafeUtils.SHORT_ARRAY_INDEX_SCALE;
import static io.microsphere.misc.UnsafeUtils.getLongVolatileFromArray;
import static io.microsphere.misc.UnsafeUtils.putLongVolatileIntoArray;
import static io.microsphere.misc.UnsafeUtils.putOrderedLongIntoArray;
import static io.microsphere.reflect.FieldUtils.findAllDeclaredFields;
import static io.microsphere.reflect.FieldUtils.getStaticFieldValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link UnsafeUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see UnsafeUtilsTest
 * @since 1.0.0
 */
class UnsafeUtilsTest {

    private Model model;

    @BeforeEach
    void setUp() {
        model = new Model();
    }

    @Test
    void testStaticFields() {
        assertEquals(16, LONG_ARRAY_BASE_OFFSET);
        assertEquals(16, INT_ARRAY_BASE_OFFSET);
        assertEquals(16, SHORT_ARRAY_BASE_OFFSET);
        assertEquals(16, BYTE_ARRAY_BASE_OFFSET);
        assertEquals(16, BOOLEAN_ARRAY_BASE_OFFSET);
        assertEquals(16, DOUBLE_ARRAY_BASE_OFFSET);
        assertEquals(16, FLOAT_ARRAY_BASE_OFFSET);
        assertEquals(16, CHAR_ARRAY_BASE_OFFSET);
        assertEquals(16, OBJECT_ARRAY_BASE_OFFSET);

        assertEquals(8, LONG_ARRAY_INDEX_SCALE);
        assertEquals(4, INT_ARRAY_INDEX_SCALE);
        assertEquals(2, SHORT_ARRAY_INDEX_SCALE);
        assertEquals(1, BYTE_ARRAY_INDEX_SCALE);
        assertEquals(1, BOOLEAN_ARRAY_INDEX_SCALE);
        assertEquals(8, DOUBLE_ARRAY_INDEX_SCALE);
        assertEquals(4, FLOAT_ARRAY_INDEX_SCALE);
        assertEquals(2, CHAR_ARRAY_INDEX_SCALE);
        assertEquals(4, OBJECT_ARRAY_INDEX_SCALE);

        Class<?> unsafeUtilsClass = UnsafeUtils.class;
        Set<Field> allDeclaredFields = findAllDeclaredFields(unsafeUtilsClass, MemberUtils::isStatic);
        for (Field field : allDeclaredFields) {
            Object value = getStaticFieldValue(true, field);
            assertNotNull(value, "The static field value of " + field + " should not be null");
        }
    }

    @Test
    void testPutLongAndGetLong() {
        String fieldName = "longValue";
        long value = Long.MAX_VALUE;
        UnsafeUtils.putLong(model, fieldName, value);
        long returnValue = UnsafeUtils.getLong(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.longValue, returnValue);

        value = Long.MIN_VALUE;
        UnsafeUtils.putLongVolatile(model, fieldName, value);
        returnValue = UnsafeUtils.getLongVolatile(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.longValue, returnValue);

        value = Long.MAX_VALUE;
        UnsafeUtils.putOrderedLong(model, fieldName, value);
        returnValue = UnsafeUtils.getLongVolatile(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.longValue, returnValue);
    }

    @Test
    void testPutIntAndGetInt() {
        String fieldName = "intValue";
        int value = 123;
        UnsafeUtils.putInt(model, fieldName, value);
        int returnValue = UnsafeUtils.getInt(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.intValue, returnValue);

        value = Integer.MAX_VALUE;
        UnsafeUtils.putIntVolatile(model, fieldName, value);
        returnValue = UnsafeUtils.getIntVolatile(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.intValue, returnValue);

        value = Integer.MIN_VALUE;
        UnsafeUtils.putOrderedInt(model, fieldName, value);
        returnValue = UnsafeUtils.getIntVolatile(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.intValue, returnValue);
    }

    @Test
    void testPutShortAndGetShort() {
        String fieldName = "shortValue";
        short value = Short.MAX_VALUE;
        UnsafeUtils.putShort(model, fieldName, value);
        short returnValue = UnsafeUtils.getShort(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.shortValue, returnValue);

        UnsafeUtils.putShortVolatile(model, fieldName, value);
        returnValue = UnsafeUtils.getShortVolatile(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.shortValue, returnValue);
    }

    @Test
    void testPutByteAndGetByte() {
        String fieldName = "byteValue";
        byte value = Byte.MAX_VALUE;
        UnsafeUtils.putByte(model, fieldName, value);
        byte returnValue = UnsafeUtils.getByte(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.byteValue, returnValue);

        UnsafeUtils.putByteVolatile(model, fieldName, value);
        returnValue = UnsafeUtils.getByteVolatile(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.byteValue, returnValue);
    }

    @Test
    void testPutBooleanAndGetBoolean() {
        String fieldName = "booleanValue";
        boolean value = Boolean.TRUE;
        UnsafeUtils.putBoolean(model, fieldName, value);
        boolean returnValue = UnsafeUtils.getBoolean(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.booleanValue, returnValue);

        UnsafeUtils.putBooleanVolatile(model, fieldName, value);
        returnValue = UnsafeUtils.getBooleanVolatile(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.booleanValue, returnValue);
    }

    @Test
    void testPutDoubleAndGetDouble() {
        String fieldName = "doubleValue";
        double value = Double.MAX_VALUE;
        UnsafeUtils.putDouble(model, fieldName, value);
        double returnValue = UnsafeUtils.getDouble(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.doubleValue, returnValue);

        UnsafeUtils.putDoubleVolatile(model, fieldName, value);
        returnValue = UnsafeUtils.getDoubleVolatile(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.doubleValue, returnValue);
    }

    @Test
    void testPutFloatAndGetFloat() {
        String fieldName = "floatValue";
        float value = Float.MAX_VALUE;
        UnsafeUtils.putFloat(model, fieldName, value);
        float returnValue = UnsafeUtils.getFloat(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.floatValue, returnValue);

        UnsafeUtils.putFloatVolatile(model, fieldName, value);
        returnValue = UnsafeUtils.getFloatVolatile(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.floatValue, returnValue);
    }

    @Test
    void testPutCharAndGetChar() {
        String fieldName = "charValue";
        char value = '@';
        UnsafeUtils.putChar(model, fieldName, value);
        char returnValue = UnsafeUtils.getChar(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.charValue, returnValue);

        UnsafeUtils.putCharVolatile(model, fieldName, value);
        returnValue = UnsafeUtils.getCharVolatile(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.charValue, returnValue);

    }

    @Test
    void testPutObjectAndGetObject() {
        String fieldName = "stringValue";
        Object value = "Test text";
        UnsafeUtils.putObject(model, fieldName, value);
        Object returnValue = UnsafeUtils.getObject(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.stringValue, returnValue);

        value = "AAAAAAAAAAAA";
        UnsafeUtils.putObjectVolatile(model, fieldName, value);
        returnValue = UnsafeUtils.getObjectVolatile(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.stringValue, returnValue);

        value = "BBBBBB";
        UnsafeUtils.putOrderedObject(model, fieldName, value);
        returnValue = UnsafeUtils.getObject(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.stringValue, returnValue);
    }

    @Test
    void testPutLongIntoArrayVolatileAndGetLongFromArrayVolatile() {
        String fieldName = "longArrayValue";
        long value = 123;
        int index = 2;
        putLongVolatileIntoArray(model, fieldName, index, value);
        long returnValue = getLongVolatileFromArray(model, fieldName, index);
        assertEquals(returnValue, value);
        assertEquals(model.longArrayValue[index], returnValue);

        value = Integer.MAX_VALUE;
        putOrderedLongIntoArray(model, fieldName, index, value);
        returnValue = getLongVolatileFromArray(model, fieldName, index);
        assertEquals(returnValue, value);
        assertEquals(model.longArrayValue[index], returnValue);
    }

    @Test
    void testPutIntIntoArrayVolatileAndGetIntFromArrayVolatile() {
        String fieldName = "intArrayValue";
        int value = 123;
        int index = 1;
        UnsafeUtils.putIntVolatileIntoArray(model, fieldName, index, value);
        int returnValue = UnsafeUtils.getIntVolatileFromArray(model, fieldName, index);
        assertEquals(returnValue, value);
        assertEquals(model.intArrayValue[index], returnValue);

        value = Integer.MAX_VALUE;
        UnsafeUtils.putOrderedIntIntoArray(model, fieldName, index, value);
        returnValue = UnsafeUtils.getIntVolatileFromArray(model, fieldName, index);
        assertEquals(returnValue, value);
        assertEquals(model.intArrayValue[index], returnValue);
    }

    @Test
    void testPutShortIntoArrayVolatileAndGetShortFromArrayVolatile() {
        String fieldName = "shortArrayValue";
        short value = 123;
        int index = 5;
        UnsafeUtils.putShortVolatileIntoArray(model, fieldName, index, value);
        short returnValue = UnsafeUtils.getShortVolatileFromArray(model, fieldName, index);
        assertEquals(returnValue, value);
        assertEquals(model.shortArrayValue[index], returnValue);
    }

    @Test
    void testPutByteIntoArrayVolatileAndGetByteFromArrayVolatile() {
        String fieldName = "byteArrayValue";
        byte value = 123;
        int index = 5;
        UnsafeUtils.putByteVolatileIntoArray(model, fieldName, index, value);
        byte returnValue = UnsafeUtils.getByteVolatileFromArray(model, fieldName, index);
        assertEquals(returnValue, value);
        assertEquals(model.byteArrayValue[index], returnValue);
    }

    @Test
    void testPutBooleanIntoArrayVolatileAndGetBooleanFromArrayVolatile() {
        String fieldName = "booleanArrayValue";
        boolean value = Boolean.TRUE;
        int index = 3;
        UnsafeUtils.putBooleanVolatileIntoArray(model, fieldName, index, value);
        boolean returnValue = UnsafeUtils.getBooleanVolatileFromArray(model, fieldName, index);
        assertEquals(returnValue, value);
        assertEquals(model.booleanArrayValue[index], returnValue);
    }

    @Test
    void testPutDoubleIntoArrayVolatileAndGetDoubleFromArrayVolatile() {
        String fieldName = "doubleArrayValue";
        double value = Double.MAX_VALUE;
        int index = 8;
        UnsafeUtils.putDoubleVolatileIntoArray(model, fieldName, index, value);
        double returnValue = UnsafeUtils.getDoubleVolatileFromArray(model, fieldName, index);
        assertEquals(returnValue, value);
        assertEquals(model.doubleArrayValue[index], returnValue);
    }

    @Test
    void testPutFloatIntoArrayVolatileAndGetFloatFromArrayVolatile() {
        String fieldName = "floatArrayValue";
        float value = Float.MAX_VALUE;
        int index = 7;
        UnsafeUtils.putFloatVolatileIntoArray(model, fieldName, index, value);
        float returnValue = UnsafeUtils.getFloatVolatileFromArray(model, fieldName, index);
        assertEquals(returnValue, value);
        assertEquals(model.floatArrayValue[index], returnValue);
    }

    @Test
    void testPutCharIntoArrayVolatileAndGetCharFromArrayVolatile() {
        String fieldName = "charArrayValue";
        char value = '@';
        int index = 9;
        UnsafeUtils.putCharVolatileIntoArray(model, fieldName, index, value);
        char returnValue = UnsafeUtils.getCharVolatileFromArray(model, fieldName, index);
        assertEquals(returnValue, value);
        assertEquals(model.charArrayValue[index], returnValue);
    }

    @Test
    void testPutObjectIntoArrayVolatileAndGetObjectFromArrayVolatile() {
        String fieldName = "objectArrayValue";
        Object value = "Test";
        int index = 5;
        UnsafeUtils.putObjectVolatileIntoArray(model, fieldName, index, value);
        Object returnValue = UnsafeUtils.getObjectVolatileFromArray(model, fieldName, index);
        assertEquals(returnValue, value);
        assertEquals(model.objectArrayValue[index], returnValue);

        UnsafeUtils.putOrderedObjectIntoArray(model, fieldName, index, value);
        returnValue = UnsafeUtils.getObjectVolatileFromArray(model, fieldName, index);
        assertEquals(returnValue, value);
        assertEquals(model.objectArrayValue[index], returnValue);

    }

    @Test
    void tesPutOnInvalidTypeValue() {
        String fieldName = "intValue";
        int value = Integer.MAX_VALUE;
        int index = 1;
        IllegalArgumentException exception;

        exception = null;
        assertNull(exception);
        try {
            UnsafeUtils.putLong(model, fieldName, value);
        } catch (IllegalArgumentException e) {
            exception = e;
        }
        assertNotNull(exception);

        exception = null;
        assertNull(exception);
        try {
            UnsafeUtils.putShort(model, fieldName, (short) 1);
        } catch (IllegalArgumentException e) {
            exception = e;
        }
        assertNotNull(exception);

        exception = null;
        assertNull(exception);
        try {
            UnsafeUtils.putChar(model, fieldName, (char) 1);
        } catch (IllegalArgumentException e) {
            exception = e;
        }
        assertNotNull(exception);


    }


    @Test
    void testOnObjectIsNull() {
        String fieldName = "aaaa";
        Object value = "value";

        NullPointerException exception = null;
        try {
            UnsafeUtils.putObject(null, fieldName, value);
        } catch (NullPointerException e) {
            exception = e;
        }
        assertNotNull(exception);
    }

    @Test
    void testOnIllegalArgumentException() {
        String fieldName = "aaaa";
        Object value = "value";

        NullPointerException exception = null;
        try {
            UnsafeUtils.putObject(model, fieldName, value);
        } catch (NullPointerException e) {
            exception = e;
        }
        assertNotNull(exception);
    }

    @Test
    void testGetStaticFieldOffset() throws Throwable {
//        Field field = findField(AccessibleObject.class, "override");
//        Lookup implLookup = getStaticFieldValue(Lookup.class, "IMPL_LOOKUP", true);
//
//        final MethodHandle overrideSetter = implLookup.findSetter(AccessibleObject.class, "override", boolean.class);
//        overrideSetter.invokeWithArguments(field, true);
//        String fieldName = "value";
//        long offset = getStaticFieldOffset(Integer.class, fieldName);
//        assertNotNull(offset);
    }

    private static class Model {
        private long longValue;
        private int intValue;
        private short shortValue;
        private byte byteValue;
        private boolean booleanValue;
        private float floatValue;
        private double doubleValue;
        private char charValue;
        private String stringValue;
        private long[] longArrayValue = new long[10];
        private int[] intArrayValue = new int[10];
        private short[] shortArrayValue = new short[10];
        private byte[] byteArrayValue = new byte[10];
        private boolean[] booleanArrayValue = new boolean[10];
        private double[] doubleArrayValue = new double[10];
        private float[] floatArrayValue = new float[10];
        private char[] charArrayValue = new char[10];
        private Object[] objectArrayValue = new Object[10];
    }

}
