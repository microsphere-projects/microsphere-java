package io.microsphere.misc;

import io.microsphere.io.serializer.Serializer;
import io.microsphere.io.serializer.Serializers;
import io.microsphere.reflect.MemberUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
import static io.microsphere.misc.UnsafeUtils.addressSize;
import static io.microsphere.misc.UnsafeUtils.allocateInstance;
import static io.microsphere.misc.UnsafeUtils.allocateMemory;
import static io.microsphere.misc.UnsafeUtils.arrayBaseOffset;
import static io.microsphere.misc.UnsafeUtils.arrayIndexScale;
import static io.microsphere.misc.UnsafeUtils.compareAndSwapInt;
import static io.microsphere.misc.UnsafeUtils.compareAndSwapLong;
import static io.microsphere.misc.UnsafeUtils.compareAndSwapObject;
import static io.microsphere.misc.UnsafeUtils.freeMemory;
import static io.microsphere.misc.UnsafeUtils.getAndAddInt;
import static io.microsphere.misc.UnsafeUtils.getAndAddLong;
import static io.microsphere.misc.UnsafeUtils.getAndSetInt;
import static io.microsphere.misc.UnsafeUtils.getAndSetLong;
import static io.microsphere.misc.UnsafeUtils.getAndSetObject;
import static io.microsphere.misc.UnsafeUtils.getBoolean;
import static io.microsphere.misc.UnsafeUtils.getBooleanVolatile;
import static io.microsphere.misc.UnsafeUtils.getBooleanVolatileFromArray;
import static io.microsphere.misc.UnsafeUtils.getByte;
import static io.microsphere.misc.UnsafeUtils.getByteVolatile;
import static io.microsphere.misc.UnsafeUtils.getByteVolatileFromArray;
import static io.microsphere.misc.UnsafeUtils.getChar;
import static io.microsphere.misc.UnsafeUtils.getCharVolatile;
import static io.microsphere.misc.UnsafeUtils.getCharVolatileFromArray;
import static io.microsphere.misc.UnsafeUtils.getDouble;
import static io.microsphere.misc.UnsafeUtils.getDoubleVolatile;
import static io.microsphere.misc.UnsafeUtils.getDoubleVolatileFromArray;
import static io.microsphere.misc.UnsafeUtils.getFloat;
import static io.microsphere.misc.UnsafeUtils.getFloatVolatile;
import static io.microsphere.misc.UnsafeUtils.getFloatVolatileFromArray;
import static io.microsphere.misc.UnsafeUtils.getInt;
import static io.microsphere.misc.UnsafeUtils.getIntVolatile;
import static io.microsphere.misc.UnsafeUtils.getIntVolatileFromArray;
import static io.microsphere.misc.UnsafeUtils.getLong;
import static io.microsphere.misc.UnsafeUtils.getLongVolatile;
import static io.microsphere.misc.UnsafeUtils.getLongVolatileFromArray;
import static io.microsphere.misc.UnsafeUtils.getObject;
import static io.microsphere.misc.UnsafeUtils.getObjectVolatile;
import static io.microsphere.misc.UnsafeUtils.getObjectVolatileFromArray;
import static io.microsphere.misc.UnsafeUtils.getShort;
import static io.microsphere.misc.UnsafeUtils.getShortVolatile;
import static io.microsphere.misc.UnsafeUtils.getShortVolatileFromArray;
import static io.microsphere.misc.UnsafeUtils.pageSize;
import static io.microsphere.misc.UnsafeUtils.putBoolean;
import static io.microsphere.misc.UnsafeUtils.putBooleanVolatile;
import static io.microsphere.misc.UnsafeUtils.putBooleanVolatileIntoArray;
import static io.microsphere.misc.UnsafeUtils.putByte;
import static io.microsphere.misc.UnsafeUtils.putByteVolatile;
import static io.microsphere.misc.UnsafeUtils.putByteVolatileIntoArray;
import static io.microsphere.misc.UnsafeUtils.putChar;
import static io.microsphere.misc.UnsafeUtils.putCharVolatile;
import static io.microsphere.misc.UnsafeUtils.putCharVolatileIntoArray;
import static io.microsphere.misc.UnsafeUtils.putDouble;
import static io.microsphere.misc.UnsafeUtils.putDoubleVolatile;
import static io.microsphere.misc.UnsafeUtils.putDoubleVolatileIntoArray;
import static io.microsphere.misc.UnsafeUtils.putFloat;
import static io.microsphere.misc.UnsafeUtils.putFloatVolatile;
import static io.microsphere.misc.UnsafeUtils.putFloatVolatileIntoArray;
import static io.microsphere.misc.UnsafeUtils.putInt;
import static io.microsphere.misc.UnsafeUtils.putIntVolatile;
import static io.microsphere.misc.UnsafeUtils.putIntVolatileIntoArray;
import static io.microsphere.misc.UnsafeUtils.putLong;
import static io.microsphere.misc.UnsafeUtils.putLongVolatile;
import static io.microsphere.misc.UnsafeUtils.putLongVolatileIntoArray;
import static io.microsphere.misc.UnsafeUtils.putObject;
import static io.microsphere.misc.UnsafeUtils.putObjectVolatile;
import static io.microsphere.misc.UnsafeUtils.putObjectVolatileIntoArray;
import static io.microsphere.misc.UnsafeUtils.putOrderedInt;
import static io.microsphere.misc.UnsafeUtils.putOrderedIntIntoArray;
import static io.microsphere.misc.UnsafeUtils.putOrderedLong;
import static io.microsphere.misc.UnsafeUtils.putOrderedLongIntoArray;
import static io.microsphere.misc.UnsafeUtils.putOrderedObject;
import static io.microsphere.misc.UnsafeUtils.putOrderedObjectIntoArray;
import static io.microsphere.misc.UnsafeUtils.putShort;
import static io.microsphere.misc.UnsafeUtils.putShortVolatile;
import static io.microsphere.misc.UnsafeUtils.putShortVolatileIntoArray;
import static io.microsphere.misc.UnsafeUtils.reallocateMemory;
import static io.microsphere.misc.UnsafeUtils.throwException;
import static io.microsphere.reflect.FieldUtils.findAllDeclaredFields;
import static io.microsphere.reflect.FieldUtils.getStaticFieldValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link UnsafeUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see UnsafeUtilsTest
 * @since 1.0.0
 */
class UnsafeUtilsTest {

    private Model model;

    private Serializers serializers;

    @BeforeEach
    void setUp() {
        this.model = new Model();
        this.serializers = new Serializers();
        this.serializers.loadSPI();
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
    void testLongOps() {
        String fieldName = "longValue";
        long value = Long.MAX_VALUE;
        putLong(model, fieldName, value);
        long returnValue = getLong(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.longValue, returnValue);

        value = Long.MIN_VALUE;
        putLongVolatile(model, fieldName, value);
        returnValue = getLongVolatile(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.longValue, returnValue);

        value = Long.MAX_VALUE;
        putOrderedLong(model, fieldName, value);
        returnValue = getLongVolatile(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.longValue, returnValue);

        assertTrue(compareAndSwapLong(model, fieldName, value, Long.MIN_VALUE));
        returnValue = getLong(model, fieldName);
        assertEquals(returnValue, Long.MIN_VALUE);

        assertFalse(compareAndSwapLong(model, fieldName, value, Long.MAX_VALUE));

        long oldValue = getAndAddLong(model, fieldName, 1);
        assertEquals(oldValue, returnValue);
        returnValue = getLong(model, fieldName);
        assertEquals(returnValue, Long.MIN_VALUE + 1);

        assertEquals(returnValue, getAndSetLong(model, fieldName, Long.MIN_VALUE));
        returnValue = getLong(model, fieldName);
        assertEquals(returnValue, Long.MIN_VALUE);
    }

    @Test
    void testIntOps() {
        String fieldName = "intValue";
        int value = 123;
        putInt(model, fieldName, value);
        int returnValue = getInt(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.intValue, returnValue);

        value = Integer.MAX_VALUE;
        putIntVolatile(model, fieldName, value);
        returnValue = getIntVolatile(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.intValue, returnValue);

        value = Integer.MIN_VALUE;
        putOrderedInt(model, fieldName, value);
        returnValue = getIntVolatile(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.intValue, returnValue);

        assertTrue(compareAndSwapInt(model, fieldName, value, Integer.MAX_VALUE));
        returnValue = getInt(model, fieldName);
        assertEquals(returnValue, Integer.MAX_VALUE);

        assertFalse(compareAndSwapInt(model, fieldName, value, Integer.MIN_VALUE));

        int oldValue = getAndAddInt(model, fieldName, 1);
        assertEquals(oldValue, returnValue);
        returnValue = getInt(model, fieldName);
        assertEquals(returnValue, Integer.MAX_VALUE + 1);

        assertEquals(returnValue, getAndSetInt(model, fieldName, Integer.MIN_VALUE));
        returnValue = getInt(model, fieldName);
        assertEquals(returnValue, Integer.MIN_VALUE);
    }

    @Test
    void testShortOps() {
        String fieldName = "shortValue";
        short value = Short.MAX_VALUE;
        putShort(model, fieldName, value);
        short returnValue = getShort(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.shortValue, returnValue);

        putShortVolatile(model, fieldName, value);
        returnValue = getShortVolatile(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.shortValue, returnValue);
    }

    @Test
    void testByteOps() {
        String fieldName = "byteValue";
        byte value = Byte.MAX_VALUE;
        putByte(model, fieldName, value);
        byte returnValue = getByte(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.byteValue, returnValue);

        putByteVolatile(model, fieldName, value);
        returnValue = getByteVolatile(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.byteValue, returnValue);
    }

    @Test
    void testBooleanOps() {
        String fieldName = "booleanValue";
        boolean value = true;
        putBoolean(model, fieldName, value);
        boolean returnValue = getBoolean(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.booleanValue, returnValue);

        putBooleanVolatile(model, fieldName, value);
        returnValue = getBooleanVolatile(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.booleanValue, returnValue);
    }

    @Test
    void testDoubleOps() {
        String fieldName = "doubleValue";
        double value = Double.MAX_VALUE;
        putDouble(model, fieldName, value);
        double returnValue = getDouble(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.doubleValue, returnValue);

        putDoubleVolatile(model, fieldName, value);
        returnValue = getDoubleVolatile(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.doubleValue, returnValue);
    }

    @Test
    void testFloatOps() {
        String fieldName = "floatValue";
        float value = Float.MAX_VALUE;
        putFloat(model, fieldName, value);
        float returnValue = getFloat(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.floatValue, returnValue);

        putFloatVolatile(model, fieldName, value);
        returnValue = getFloatVolatile(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.floatValue, returnValue);
    }

    @Test
    void testCharOps() {
        String fieldName = "charValue";
        char value = '@';
        putChar(model, fieldName, value);
        char returnValue = getChar(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.charValue, returnValue);

        putCharVolatile(model, fieldName, value);
        returnValue = getCharVolatile(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.charValue, returnValue);
    }

    @Test
    void testObjectOps() {
        String fieldName = "stringValue";
        Object value = "Test text";
        putObject(model, fieldName, value);
        Object returnValue = getObject(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.stringValue, returnValue);

        value = "A";
        putObjectVolatile(model, fieldName, value);
        returnValue = getObjectVolatile(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.stringValue, returnValue);

        value = "B";
        putOrderedObject(model, fieldName, value);
        returnValue = getObject(model, fieldName);
        assertEquals(returnValue, value);
        assertEquals(model.stringValue, returnValue);

        value = "C";
        assertTrue(compareAndSwapObject(model, fieldName, returnValue, value));
        returnValue = getObject(model, fieldName);
        assertEquals(returnValue, value);

        assertFalse(compareAndSwapObject(model, fieldName, "returnValue", value));

        value = "D";
        assertEquals(returnValue, getAndSetObject(model, fieldName, value));
        returnValue = getObject(model, fieldName);
        assertEquals(returnValue, value);
    }

    @Test
    void testLongArrayVolatileOps() {
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
    void testIntArrayVolatileOps() {
        String fieldName = "intArrayValue";
        int value = 123;
        int index = 1;
        putIntVolatileIntoArray(model, fieldName, index, value);
        int returnValue = getIntVolatileFromArray(model, fieldName, index);
        assertEquals(returnValue, value);
        assertEquals(model.intArrayValue[index], returnValue);

        value = Integer.MAX_VALUE;
        putOrderedIntIntoArray(model, fieldName, index, value);
        returnValue = getIntVolatileFromArray(model, fieldName, index);
        assertEquals(returnValue, value);
        assertEquals(model.intArrayValue[index], returnValue);
    }

    @Test
    void testShortArrayVolatileOps() {
        String fieldName = "shortArrayValue";
        short value = 123;
        int index = 5;
        putShortVolatileIntoArray(model, fieldName, index, value);
        short returnValue = getShortVolatileFromArray(model, fieldName, index);
        assertEquals(returnValue, value);
        assertEquals(model.shortArrayValue[index], returnValue);
    }

    @Test
    void testByteArrayVolatileOps() {
        String fieldName = "byteArrayValue";
        byte value = 123;
        int index = 5;
        putByteVolatileIntoArray(model, fieldName, index, value);
        byte returnValue = getByteVolatileFromArray(model, fieldName, index);
        assertEquals(returnValue, value);
        assertEquals(model.byteArrayValue[index], returnValue);
    }

    @Test
    void testBooleanArrayVolatileOps() {
        String fieldName = "booleanArrayValue";
        boolean value = true;
        int index = 3;
        putBooleanVolatileIntoArray(model, fieldName, index, value);
        boolean returnValue = getBooleanVolatileFromArray(model, fieldName, index);
        assertEquals(returnValue, value);
        assertEquals(model.booleanArrayValue[index], returnValue);
    }

    @Test
    void testDoubleArrayVolatileOps() {
        String fieldName = "doubleArrayValue";
        double value = Double.MAX_VALUE;
        int index = 8;
        putDoubleVolatileIntoArray(model, fieldName, index, value);
        double returnValue = getDoubleVolatileFromArray(model, fieldName, index);
        assertEquals(returnValue, value);
        assertEquals(model.doubleArrayValue[index], returnValue);
    }

    @Test
    void testFloatArrayVolatileOps() {
        String fieldName = "floatArrayValue";
        float value = Float.MAX_VALUE;
        int index = 7;
        putFloatVolatileIntoArray(model, fieldName, index, value);
        float returnValue = getFloatVolatileFromArray(model, fieldName, index);
        assertEquals(returnValue, value);
        assertEquals(model.floatArrayValue[index], returnValue);
    }

    @Test
    void testCharArrayVolatileOps() {
        String fieldName = "charArrayValue";
        char value = '@';
        int index = 9;
        putCharVolatileIntoArray(model, fieldName, index, value);
        char returnValue = getCharVolatileFromArray(model, fieldName, index);
        assertEquals(returnValue, value);
        assertEquals(model.charArrayValue[index], returnValue);
    }

    @Test
    void testObjectArrayVolatileOps() {
        String fieldName = "objectArrayValue";
        Object value = "Test";
        int index = 5;
        putObjectVolatileIntoArray(model, fieldName, index, value);
        Object returnValue = getObjectVolatileFromArray(model, fieldName, index);
        assertEquals(returnValue, value);
        assertEquals(model.objectArrayValue[index], returnValue);

        putOrderedObjectIntoArray(model, fieldName, index, value);
        returnValue = getObjectVolatileFromArray(model, fieldName, index);
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
            putLong(model, fieldName, value);
        } catch (IllegalArgumentException e) {
            exception = e;
        }
        assertNotNull(exception);

        exception = null;
        assertNull(exception);
        try {
            putShort(model, fieldName, (short) 1);
        } catch (IllegalArgumentException e) {
            exception = e;
        }
        assertNotNull(exception);

        exception = null;
        assertNull(exception);
        try {
            putChar(model, fieldName, (char) 1);
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
            putObject(null, fieldName, value);
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
            putObject(model, fieldName, value);
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

    @Test
    void testByteOpsInOffHeap() {
        testInOffHeap(Byte.MAX_VALUE, UnsafeUtils::putByte, UnsafeUtils::getByte);
    }

    @Test
    void testShortOpsInOffHeap() {
        testInOffHeap(Short.MAX_VALUE, UnsafeUtils::putShort, UnsafeUtils::getShort);
    }

    @Test
    void testCharOpsInOffHeap() {
        testInOffHeap(Character.MAX_VALUE, UnsafeUtils::putChar, UnsafeUtils::getChar);
    }

    @Test
    void testIntegerOpsInOffHeap() {
        testInOffHeap(Integer.MAX_VALUE, UnsafeUtils::putInt, UnsafeUtils::getInt);
    }

    @Test
    void testLongOpsInOffHeap() {
        testInOffHeap(Long.MAX_VALUE, UnsafeUtils::putLong, UnsafeUtils::getLong);
    }

    @Test
    void testFloatOpsInOffHeap() {
        testInOffHeap(Float.MAX_VALUE, UnsafeUtils::putFloat, UnsafeUtils::getFloat);
    }

    @Test
    void testDoubleOpsInOffHeap() {
        testInOffHeap(Double.MAX_VALUE, UnsafeUtils::putDouble, UnsafeUtils::getDouble);
    }

    <N> void testInOffHeap(N number, BiConsumer<Long, N> addressConsumer, Function<Long, N> addressFunction) {
        long address = -1L;
        Class<N> numberType = (Class<N>) number.getClass();
        Serializer<N> serializer = (Serializer<N>) this.serializers.getMostCompatible(numberType);
        try {
            byte[] bytes = serializer.serialize(number);
            address = allocateMemory(bytes.length);
            addressConsumer.accept(address, number);
            N returnValue = addressFunction.apply(address);
            assertEquals(number, returnValue);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (address != -1L) {
                freeMemory(address);
            }
        }
    }

    @Test
    void testArrayBaseOffset() {
        assertEquals(LONG_ARRAY_BASE_OFFSET, arrayBaseOffset(long[].class));
        assertEquals(INT_ARRAY_BASE_OFFSET, arrayBaseOffset(int[].class));
        assertEquals(SHORT_ARRAY_BASE_OFFSET, arrayBaseOffset(short[].class));
        assertEquals(BYTE_ARRAY_BASE_OFFSET, arrayBaseOffset(byte[].class));
        assertEquals(BOOLEAN_ARRAY_BASE_OFFSET, arrayBaseOffset(boolean[].class));
        assertEquals(DOUBLE_ARRAY_BASE_OFFSET, arrayBaseOffset(double[].class));
        assertEquals(FLOAT_ARRAY_BASE_OFFSET, arrayBaseOffset(float[].class));
        assertEquals(CHAR_ARRAY_BASE_OFFSET, arrayBaseOffset(char[].class));
        assertEquals(OBJECT_ARRAY_BASE_OFFSET, arrayBaseOffset(Object[].class));
    }

    @Test
    void testArrayIndexScale() {
        assertEquals(LONG_ARRAY_INDEX_SCALE, arrayIndexScale(long[].class));
        assertEquals(INT_ARRAY_INDEX_SCALE, arrayIndexScale(int[].class));
        assertEquals(SHORT_ARRAY_INDEX_SCALE, arrayIndexScale(short[].class));
        assertEquals(BYTE_ARRAY_INDEX_SCALE, arrayIndexScale(byte[].class));
        assertEquals(BOOLEAN_ARRAY_INDEX_SCALE, arrayIndexScale(boolean[].class));
        assertEquals(DOUBLE_ARRAY_INDEX_SCALE, arrayIndexScale(double[].class));
        assertEquals(FLOAT_ARRAY_INDEX_SCALE, arrayIndexScale(float[].class));
        assertEquals(CHAR_ARRAY_INDEX_SCALE, arrayIndexScale(char[].class));
        assertEquals(OBJECT_ARRAY_INDEX_SCALE, arrayIndexScale(Object[].class));
    }

    @Test
    void testAddressSize() {
        assertTrue(addressSize() > 0);
    }

    @Test
    void testPageSize() {
        assertTrue(pageSize() > 0);
    }

    @Test
    void testAllocateInstance() {
        assertNotNull(allocateInstance(Model.class));
    }

    @Test
    void testThrowException() {
        assertThrows(RuntimeException.class, () -> throwException(new RuntimeException()));
    }

    @Test
    void testMemoryOps() {
        long address = allocateMemory(8);
        long newAddress = reallocateMemory(address, 16);
        assertEquals(newAddress, address);
        freeMemory(address);
    }

    @Test
    void testFenceOps() {
        assertDoesNotThrow(UnsafeUtils::loadFence);
        assertDoesNotThrow(UnsafeUtils::storeFence);
        assertDoesNotThrow(UnsafeUtils::fullFence);
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