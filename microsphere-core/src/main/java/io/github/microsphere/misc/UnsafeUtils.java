package io.github.microsphere.misc;

import io.github.microsphere.reflect.ReflectionUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.github.microsphere.reflect.ReflectionUtils.assertArrayIndex;
import static io.github.microsphere.reflect.ReflectionUtils.assertFieldMatchType;

/**
 * {@link Unsafe} Utility class <p/> <b> Take case to  use those utility methods in order to the stability fo
 * JVM </b>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see UnsafeUtils
 * @since 1.0.0
 */
public abstract class UnsafeUtils {

    final static Unsafe unsafe;

    /**
     * <code>long</code> Array base index
     */
    static final int LONG_ARRAY_BASE_OFFSET;

    /**
     * <code>int</code> Array base index
     */
    static final int INT_ARRAY_BASE_OFFSET;

    /**
     * <code>short</code> Array base index
     */
    static final int SHORT_ARRAY_BASE_OFFSET;

    /**
     * <code>byte</code> Array base index
     */
    static final int BYTE_ARRAY_BASE_OFFSET;

    /**
     * <code>boolean</code> Array base index
     */
    static final int BOOLEAN_ARRAY_BASE_OFFSET;

    /**
     * <code>double</code> Array base index
     */
    static final int DOUBLE_ARRAY_BASE_OFFSET;

    /**
     * <code>float</code> Array base index
     */
    static final int FLOAT_ARRAY_BASE_OFFSET;

    /**
     * <code>char</code> Array base index
     */
    static final int CHAR_ARRAY_BASE_OFFSET;

    /**
     * <code>java.lang.Object</code> Array base index
     */
    static final int OBJECT_ARRAY_BASE_OFFSET;

    /**
     * <code>long</code> Array Index scale
     */
    static final int LONG_ARRAY_INDEX_SCALE;

    /**
     * <code>int</code> Array Index scale
     */
    static final int INT_ARRAY_INDEX_SCALE;

    /**
     * <code>short</code> Array Index scale
     */
    static final int SHORT_ARRAY_INDEX_SCALE;

    /**
     * <code>byte</code> Array Index scale
     */
    static final int BYTE_ARRAY_INDEX_SCALE;

    /**
     * <code>boolean</code> Array Index scale
     */
    static final int BOOLEAN_ARRAY_INDEX_SCALE;

    /**
     * <code>double</code> Array Index scale
     */
    static final int DOUBLE_ARRAY_INDEX_SCALE;

    /**
     * <code>float</code> Array Index scale
     */
    static final int FLOAT_ARRAY_INDEX_SCALE;

    /**
     * <code>char</code> Array Index scale
     */
    static final int CHAR_ARRAY_INDEX_SCALE;

    /**
     * <code>java.lang.Object</code> Array Index scale
     */
    static final int OBJECT_ARRAY_INDEX_SCALE;

    /**
     * Offset Cache,
     */
    private final static ConcurrentMap<String, Long> offsetCache = new ConcurrentHashMap<>();

    static {
        try {
            final PrivilegedExceptionAction<Unsafe> action = new PrivilegedExceptionAction<Unsafe>() {
                public Unsafe run() throws Exception {
                    Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                    theUnsafe.setAccessible(true);
                    return (Unsafe) theUnsafe.get(null);
                }
            };

            unsafe = AccessController.doPrivileged(action);

            if (unsafe == null) {
                throw new NullPointerException();
            }

            LONG_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(long[].class);
            INT_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(int[].class);
            SHORT_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(short[].class);
            BYTE_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(byte[].class);
            BOOLEAN_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(boolean[].class);
            DOUBLE_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(double[].class);
            FLOAT_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(float[].class);
            CHAR_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(char[].class);
            OBJECT_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(Object[].class);

            LONG_ARRAY_INDEX_SCALE = unsafe.arrayIndexScale(long[].class);
            INT_ARRAY_INDEX_SCALE = unsafe.arrayIndexScale(int[].class);
            SHORT_ARRAY_INDEX_SCALE = unsafe.arrayIndexScale(short[].class);
            BYTE_ARRAY_INDEX_SCALE = unsafe.arrayIndexScale(byte[].class);
            BOOLEAN_ARRAY_INDEX_SCALE = unsafe.arrayIndexScale(boolean[].class);
            DOUBLE_ARRAY_INDEX_SCALE = unsafe.arrayIndexScale(double[].class);
            FLOAT_ARRAY_INDEX_SCALE = unsafe.arrayIndexScale(float[].class);
            CHAR_ARRAY_INDEX_SCALE = unsafe.arrayIndexScale(char[].class);
            OBJECT_ARRAY_INDEX_SCALE = unsafe.arrayIndexScale(Object[].class);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Current JVM does not support sun.misc.Unsafe");
        }
    }

    /**
     * Calculate Array Index Offset
     *
     * @param index      Index
     * @param baseOffset {@link Unsafe#arrayBaseOffset(Class)}
     * @param indexScale {@link Unsafe#arrayIndexScale(Class)}
     * @return
     * @see java.util.concurrent.atomic.AtomicIntegerArray
     */
    protected static long arrayIndexOffset(int index, long baseOffset, long indexScale) {
        if (index < 0)
            throw new IndexOutOfBoundsException("index " + index);
        return baseOffset + (long) index * indexScale;
    }


    /**
     * Calculate the <code>long<code> type Array Index the relative offset
     *
     * @param index Array Index
     * @return the relative offset
     */
    protected static long longArrayIndexOffset(int index) {
        return arrayIndexOffset(index, LONG_ARRAY_BASE_OFFSET, LONG_ARRAY_INDEX_SCALE);
    }

    /**
     * Calculate <code>int<code> type Array Index the relative offset
     *
     * @param index Array Index
     * @return the relative offset
     */
    protected static long intArrayIndexOffset(int index) {
        return arrayIndexOffset(index, INT_ARRAY_BASE_OFFSET, INT_ARRAY_INDEX_SCALE);
    }

    /**
     * Calculate the <code>short<code> type Array Index the relative offset
     *
     * @param index Array Index
     * @return the relative offset
     */
    protected static long shortArrayIndexOffset(int index) {
        return arrayIndexOffset(index, SHORT_ARRAY_BASE_OFFSET, SHORT_ARRAY_INDEX_SCALE);
    }

    /**
     * Calculate the relative offset of Array Index of type <code>byte<code>
     *
     * @param index Array Index
     * @return the relative offset
     */
    protected static long byteArrayIndexOffset(int index) {
        return arrayIndexOffset(index, BYTE_ARRAY_BASE_OFFSET, BYTE_ARRAY_INDEX_SCALE);
    }

    /**
     * Calculate <code>boolean<code> type Array Index the relative offset
     *
     * @param index Array Index
     * @return the relative offset
     */
    protected static long booleanArrayIndexOffset(int index) {
        return arrayIndexOffset(index, BOOLEAN_ARRAY_BASE_OFFSET, BOOLEAN_ARRAY_INDEX_SCALE);
    }

    /**
     * Calculate the relative offset of Array Index of type <code>double<code>
     *
     * @param index Array Index
     * @return the relative offset
     */
    protected static long doubleArrayIndexOffset(int index) {
        return arrayIndexOffset(index, DOUBLE_ARRAY_BASE_OFFSET, DOUBLE_ARRAY_INDEX_SCALE);
    }

    /**
     * Calculate <code>float<code> type Array Index the relative offset
     *
     * @param index Array Index
     * @return the relative offset
     */
    protected static long floatArrayIndexOffset(int index) {
        return arrayIndexOffset(index, FLOAT_ARRAY_BASE_OFFSET, FLOAT_ARRAY_INDEX_SCALE);
    }

    /**
     * Calculate <code>char<code> type Array Index the relative offset
     *
     * @param index Array Index
     * @return the relative offset
     */
    protected static long charArrayIndexOffset(int index) {
        return arrayIndexOffset(index, CHAR_ARRAY_BASE_OFFSET, CHAR_ARRAY_INDEX_SCALE);
    }

    /**
     * Calculate <code>java.lang.Object<code> type Array Index the relative offset
     *
     * @param index Array Index
     * @return the relative offset
     */
    protected static long objectArrayIndexOffset(int index) {
        return arrayIndexOffset(index, OBJECT_ARRAY_BASE_OFFSET, OBJECT_ARRAY_INDEX_SCALE);
    }


    /**
     * Create Offset Cache Key
     *
     * @param type      the target type
     * @param fieldName the name of {@link Field}
     * @return The cache key of offset
     */
    protected static String createOffsetCacheKey(Class<?> type, String fieldName) {
        StringBuilder keyBuilder = new StringBuilder(type.getName()).append("#").append(fieldName);
        return keyBuilder.toString();
    }

    /**
     * Get Offset in cache
     *
     * @param type      the target type
     * @param fieldName the name of {@link Field}
     * @return Offset
     */
    protected static Long getOffsetFromCache(Class<?> type, String fieldName) {
        String key = createOffsetCacheKey(type, fieldName);
        return offsetCache.get(key);
    }

    /**
     * Save offset to cache
     *
     * @param type      the target type
     * @param fieldName the name of {@link Field}
     * @param offset    offset
     */
    protected static void putOffsetFromCache(Class<?> type, String fieldName, long offset) {
        String key = createOffsetCacheKey(type, fieldName);
        offsetCache.putIfAbsent(key, offset);
    }


    /**
     * Get the <code>long<code> value of the target Index in the Array field of the object
     *
     * @param object    Object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @return the <code>long<code> value
     */
    public static long getLongFromArrayVolatile(Object object, String fieldName, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = FieldUtils.readDeclaredField(object, fieldName, true);
        long offset = longArrayIndexOffset(index);
        return unsafe.getLongVolatile(array, offset);
    }

    /**
     * Get the value of the target Index in the object Array field
     *
     * @param object    Object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @return the <code>int<code> value
     */
    public static int getIntFromArrayVolatile(Object object, String fieldName, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = FieldUtils.readDeclaredField(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = intArrayIndexOffset(index);
        return unsafe.getIntVolatile(array, offset);
    }

    /**
     * Get the <code>short<code> value of the target Index in the Array field of the object
     *
     * @param object    Object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @return the <code>short<code> value
     */
    public static short getShortFromArrayVolatile(Object object, String fieldName, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = FieldUtils.readDeclaredField(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = shortArrayIndexOffset(index);
        return unsafe.getShortVolatile(array, offset);
    }

    /**
     * Get the <code>byte<code> value of the target Index in the object Array field
     *
     * @param object    Object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @return the <code>byte<code> value
     */
    public static byte getByteFromArrayVolatile(Object object, String fieldName, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = FieldUtils.readDeclaredField(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = byteArrayIndexOffset(index);
        return unsafe.getByteVolatile(array, offset);
    }

    /**
     * Get the <code>boolean<code> value of the target Index in the Array field of the object
     *
     * @param object    Object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @return the <code>boolean<code> value
     */
    public static boolean getBooleanFromArrayVolatile(Object object, String fieldName, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = FieldUtils.readDeclaredField(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = booleanArrayIndexOffset(index);
        return unsafe.getBooleanVolatile(array, offset);
    }

    /**
     * Get the <code>double<code> value of the target Index in the Array field of the object
     *
     * @param object    Object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @return the <code>double<code> value
     */
    public static double getDoubleFromArrayVolatile(Object object, String fieldName, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = FieldUtils.readDeclaredField(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = doubleArrayIndexOffset(index);
        return unsafe.getDoubleVolatile(array, offset);
    }

    /**
     * Get the <code>float<code> value of the target Index in the object Array field
     *
     * @param object    Object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @return the target index
     */
    public static float getFloatFromArrayVolatile(Object object, String fieldName, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = FieldUtils.readDeclaredField(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = floatArrayIndexOffset(index);
        return unsafe.getFloatVolatile(array, offset);
    }

    /**
     * Get the <code>char<code> value of the target Index in the object Array field
     *
     * @param object    Object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @return the <code>char<code> value
     * @throws IllegalArgumentException       See {@link ReflectionUtils#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static char getCharFromArrayVolatile(Object object, String fieldName, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = FieldUtils.readDeclaredField(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = charArrayIndexOffset(index);
        return unsafe.getCharVolatile(array, offset);
    }

    /**
     * Get the <code>java.lang.Object<code> value of the target Index in the object Array field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @return the <code>java.lang.Object<code> value
     * @throws IllegalArgumentException       see {@link ReflectionUtils#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static Object getObjectFromArrayVolatile(Object object, String fieldName, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = FieldUtils.readDeclaredField(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = objectArrayIndexOffset(index);
        return unsafe.getObjectVolatile(array, offset);
    }


    /**
     * Sets the given double value to the field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param value     double value
     */
    public static void putDouble(Object object, String fieldName, double value) {
        assertFieldMatchType(object, fieldName, double.class);
        long offset = getObjectFieldOffset(object, fieldName);
        unsafe.putDouble(object, offset, value);
    }

    /**
     * Sets the given float value to the field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param value     float value
     */
    public static void putFloat(Object object, String fieldName, float value) {
        assertFieldMatchType(object, fieldName, float.class);
        long offset = getObjectFieldOffset(object, fieldName);
        unsafe.putFloat(object, offset, value);
    }

    /**
     * Sets the given short value to the field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param value     short value
     */
    public static void putShort(Object object, String fieldName, short value) {
        assertFieldMatchType(object, fieldName, short.class);
        long offset = getObjectFieldOffset(object, fieldName);
        unsafe.putShort(object, offset, value);
    }

    /**
     * Sets the given byte value to the field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param value     byte value
     */
    public static void putByte(Object object, String fieldName, byte value) {
        assertFieldMatchType(object, fieldName, byte.class);
        long offset = getObjectFieldOffset(object, fieldName);
        unsafe.putByte(object, offset, value);
    }

    /**
     * Sets the given boolean value to the field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param value     boolean value
     */
    public static void putBoolean(Object object, String fieldName, boolean value) {
        assertFieldMatchType(object, fieldName, boolean.class);
        long offset = getObjectFieldOffset(object, fieldName);
        unsafe.putBoolean(object, offset, value);
    }

    /**
     * Sets the given char value to the field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param value     char value
     */
    public static void putChar(Object object, String fieldName, char value) {
        assertFieldMatchType(object, fieldName, char.class);
        long offset = getObjectFieldOffset(object, fieldName);
        unsafe.putChar(object, offset, value);
    }

    /**
     * Sets the given Object value to the field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param value     Object value
     */
    public static void putObject(Object object, String fieldName, Object value) {
        assertFieldMatchType(object, fieldName, Object.class);
        long offset = getObjectFieldOffset(object, fieldName);
        unsafe.putObject(object, offset, value);
    }

    /**
     * Sets the given long value to the field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param value     long value
     */
    public static void putLong(Object object, String fieldName, long value) {
        assertFieldMatchType(object, fieldName, long.class);
        long offset = getObjectFieldOffset(object, fieldName);
        unsafe.putLong(object, offset, value);
    }


    /**
     * Sets the given int value to the field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param value     int value
     */
    public static void putInt(Object object, String fieldName, int value) {
        assertFieldMatchType(object, fieldName, int.class);
        long offset = getObjectFieldOffset(object, fieldName);
        unsafe.putInt(object, offset, value);
    }

    /**
     * Sets the given int value to the field of the specified object (ensures writing order)
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param value     int value
     */
    public static void putOrderedInt(Object object, String fieldName, int value) {
        assertFieldMatchType(object, fieldName, int.class);
        long offset = getObjectFieldOffset(object, fieldName);
        unsafe.putOrderedInt(object, offset, value);
    }

    /**
     * Sets the given long value to the field of the specified object (ensures writing order)
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param value     long value
     */
    public static void putOrderedLong(Object object, String fieldName, long value) {
        assertFieldMatchType(object, fieldName, long.class);
        long offset = getObjectFieldOffset(object, fieldName);
        unsafe.putOrderedLong(object, offset, value);
    }

    /**
     * Set the given Object value to the field of the specified object (ensure write order)
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param value     Object value
     */
    public static void putOrderedObject(Object object, String fieldName, Object value) {
        assertFieldMatchType(object, fieldName, Object.class);
        long offset = getObjectFieldOffset(object, fieldName);
        unsafe.putOrderedObject(object, offset, value);
    }

    /**
     * Sets the given double value to the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param value     double value
     */
    public static void putDoubleVolatile(Object object, String fieldName, double value) {
        assertFieldMatchType(object, fieldName, double.class);
        long offset = getObjectFieldOffset(object, fieldName);
        unsafe.putDoubleVolatile(object, offset, value);
    }

    /**
     * Sets the given float value to the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param value     float value
     */
    public static void putFloatVolatile(Object object, String fieldName, float value) {
        assertFieldMatchType(object, fieldName, float.class);
        long offset = getObjectFieldOffset(object, fieldName);
        unsafe.putFloatVolatile(object, offset, value);
    }

    /**
     * Sets the given short value to the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param value     short value
     */
    public static void putShortVolatile(Object object, String fieldName, short value) {
        assertFieldMatchType(object, fieldName, short.class);
        long offset = getObjectFieldOffset(object, fieldName);
        unsafe.putShortVolatile(object, offset, value);
    }

    /**
     * Sets the given byte value to the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param value     byte value
     */
    public static void putByteVolatile(Object object, String fieldName, byte value) {
        assertFieldMatchType(object, fieldName, byte.class);
        long offset = getObjectFieldOffset(object, fieldName);
        unsafe.putByteVolatile(object, offset, value);
    }

    /**
     * Sets the given boolean value to the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param value     boolean value
     */
    public static void putBooleanVolatile(Object object, String fieldName, boolean value) {
        assertFieldMatchType(object, fieldName, boolean.class);
        long offset = getObjectFieldOffset(object, fieldName);
        unsafe.putBooleanVolatile(object, offset, value);
    }

    /**
     * Sets the given char value to the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param value     char value
     */
    public static void putCharVolatile(Object object, String fieldName, char value) {
        assertFieldMatchType(object, fieldName, char.class);
        long offset = getObjectFieldOffset(object, fieldName);
        unsafe.putCharVolatile(object, offset, value);
    }

    /**
     * Sets the given Object value to the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param value     Object value
     */
    public static void putObjectVolatile(Object object, String fieldName, Object value) {
        assertFieldMatchType(object, fieldName, Object.class);
        long offset = getObjectFieldOffset(object, fieldName);
        unsafe.putObjectVolatile(object, offset, value);
    }

    /**
     * Sets the given long value to the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param value     long value
     */
    public static void putLongVolatile(Object object, String fieldName, long value) {
        assertFieldMatchType(object, fieldName, long.class);
        long offset = getObjectFieldOffset(object, fieldName);
        unsafe.putLongVolatile(object, offset, value);
    }

    /**
     * Sets the given int value to the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param value     int value
     */
    public static void putIntVolatile(Object object, String fieldName, int value) {
        assertFieldMatchType(object, fieldName, int.class);
        long offset = getObjectFieldOffset(object, fieldName);
        unsafe.putIntVolatile(object, offset, value);
    }


    /**
     * Sets the given <code>long<code> value to a field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>long</code> value
     * @throws IllegalArgumentException       see {@link ReflectionUtils#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putLongIntoArrayVolatile(Object object, String fieldName, int index, long value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = FieldUtils.readDeclaredField(object, fieldName, true);
        ReflectionUtils.assertArrayIndex(array, index);
        long offset = longArrayIndexOffset(index);
        unsafe.putLongVolatile(array, offset, value);
    }

    /**
     * Sets the given <code>long<code> value to the fields of the specified object (sequential writing)
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>long</code> value
     * @throws IllegalArgumentException       see {@link ReflectionUtils#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putOrderedLongIntoArray(Object object, String fieldName, int index, long value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = FieldUtils.readDeclaredField(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = longArrayIndexOffset(index);
        unsafe.putOrderedLong(array, offset, value);
    }

    /**
     * Sets the given <code>int<code> value to a field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>int</code> value
     * @throws IllegalArgumentException       see {@link ReflectionUtils#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putIntIntoArrayVolatile(Object object, String fieldName, int index, int value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = FieldUtils.readDeclaredField(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = intArrayIndexOffset(index);
        unsafe.putIntVolatile(array, offset, value);
    }

    /**
     * Sets the given <code>int<code> value to a field of the specified object (sequential writing)
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>int</code> value
     * @throws IllegalArgumentException       see {@link ReflectionUtils#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putOrderedIntIntoArray(Object object, String fieldName, int index, int value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = FieldUtils.readDeclaredField(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = intArrayIndexOffset(index);
        unsafe.putOrderedInt(array, offset, value);
    }

    /**
     * Sets the given <code>short<code> value to a field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>short</code> value
     * @throws IllegalArgumentException       see {@link ReflectionUtils#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putShortIntoArrayVolatile(Object object, String fieldName, int index, short value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = FieldUtils.readDeclaredField(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = shortArrayIndexOffset(index);
        unsafe.putShortVolatile(array, offset, value);
    }

    /**
     * Sets the given <code>byte<code> value to a field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>byte</code> value
     * @throws IllegalArgumentException       see {@link ReflectionUtils#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putByteIntoArrayVolatile(Object object, String fieldName, int index, byte value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = FieldUtils.readDeclaredField(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = byteArrayIndexOffset(index);
        unsafe.putByteVolatile(array, offset, value);
    }

    /**
     * Sets the given <code>boolean<code> value to a field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>boolean</code> value
     * @throws IllegalArgumentException       see {@link ReflectionUtils#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putBooleanIntoArrayVolatile(Object object, String fieldName, int index, boolean value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = FieldUtils.readDeclaredField(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = booleanArrayIndexOffset(index);
        unsafe.putBooleanVolatile(array, offset, value);
    }

    /**
     * Sets the given <code>double<code> value to a field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>double</code> value
     * @throws IllegalArgumentException       see {@link ReflectionUtils#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putDoubleIntoArrayVolatile(Object object, String fieldName, int index, double value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = FieldUtils.readDeclaredField(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = doubleArrayIndexOffset(index);
        unsafe.putDoubleVolatile(array, offset, value);
    }

    /**
     * Sets the given <code>float<code> value to the field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>float</code> value
     * @throws IllegalArgumentException       see {@link ReflectionUtils#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putFloatIntoArrayVolatile(Object object, String fieldName, int index, float value) throws IllegalAccessException {
        Object array = FieldUtils.readDeclaredField(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = floatArrayIndexOffset(index);
        unsafe.putFloatVolatile(array, offset, value);
    }

    /**
     * Sets the given <code>char<code> value to a field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>char</code> value
     * @throws IllegalArgumentException       see {@link ReflectionUtils#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putCharIntoArrayVolatile(Object object, String fieldName, int index, char value) throws IllegalAccessException {
        Object array = FieldUtils.readDeclaredField(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = charArrayIndexOffset(index);
        unsafe.putCharVolatile(array, offset, value);
    }

    /**
     * Sets the given <code>java.lang.Object<code> value to a field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>java.lang.Object</code> value
     * @throws IllegalArgumentException       see {@link ReflectionUtils#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putObjectIntoArrayVolatile(Object object, String fieldName, int index, Object value) throws IllegalAccessException {
        Object array = FieldUtils.readDeclaredField(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = objectArrayIndexOffset(index);
        unsafe.putObjectVolatile(array, offset, value);
    }


    /**
     * Sets the given <code>java.lang.Object<code> value to the fields of the specified object (sequential writing)
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>java.lang.Object</code> value
     * @throws IllegalArgumentException       see {@link ReflectionUtils#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putOrderedObjectIntoArray(Object object, String fieldName, int index, Object value) throws IllegalAccessException {
        Object array = FieldUtils.readDeclaredField(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = objectArrayIndexOffset(index);
        unsafe.putOrderedObject(array, offset, value);
    }

    /**
     * Get the Object value of the specified object field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return Object value
     */
    public static Object getObject(Object object, String fieldName) {
        long offset = getObjectFieldOffset(object, fieldName);
        return unsafe.getObject(object, offset);
    }

    /**
     * Get the long value of the specified object field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return long value
     */
    public static long getLong(Object object, String fieldName) {
        long offset = getObjectFieldOffset(object, fieldName);
        return unsafe.getLong(object, offset);
    }

    /**
     * Get the double value of the specified object field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return double value
     */
    public static double getDouble(Object object, String fieldName) {
        long offset = getObjectFieldOffset(object, fieldName);
        return unsafe.getDouble(object, offset);
    }

    /**
     * Get the float value of the specified object field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return float value
     */
    public static float getFloat(Object object, String fieldName) {
        long offset = getObjectFieldOffset(object, fieldName);
        return unsafe.getFloat(object, offset);
    }

    /**
     * Get the short value of the specified object field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return short value
     */
    public static short getShort(Object object, String fieldName) {
        long offset = getObjectFieldOffset(object, fieldName);
        return unsafe.getShort(object, offset);
    }

    /**
     * Get the byte value of the specified object field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return byte value
     */
    public static byte getByte(Object object, String fieldName) {
        long offset = getObjectFieldOffset(object, fieldName);
        return unsafe.getByte(object, offset);
    }

    /**
     * Get the boolean value of the specified object field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return boolean value
     */
    public static boolean getBoolean(Object object, String fieldName) {
        long offset = getObjectFieldOffset(object, fieldName);
        return unsafe.getBoolean(object, offset);
    }

    /**
     * Get the char value of the specified object field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return char value
     */
    public static char getChar(Object object, String fieldName) {
        long offset = getObjectFieldOffset(object, fieldName);
        return unsafe.getChar(object, offset);
    }

    /**
     * Get the int value of the specified object field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return int value
     */
    public static int getInt(Object object, String fieldName) {
        long offset = getObjectFieldOffset(object, fieldName);
        return unsafe.getInt(object, offset);
    }

    /**
     * Get the Object value of the specified object <code>volatile<code> field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return Object value
     */
    public static Object getObjectVolatile(Object object, String fieldName) {
        long offset = getObjectFieldOffset(object, fieldName);
        return unsafe.getObjectVolatile(object, offset);
    }

    /**
     * Get the long value of the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return long value
     */
    public static long getLongVolatile(Object object, String fieldName) {
        long offset = getObjectFieldOffset(object, fieldName);
        return unsafe.getLongVolatile(object, offset);
    }

    /**
     * Get the double value of the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return double value
     */
    public static double getDoubleVolatile(Object object, String fieldName) {
        long offset = getObjectFieldOffset(object, fieldName);
        return unsafe.getDoubleVolatile(object, offset);
    }

    /**
     * Get the float value of the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return float value
     */
    public static float getFloatVolatile(Object object, String fieldName) {
        long offset = getObjectFieldOffset(object, fieldName);
        return unsafe.getFloatVolatile(object, offset);
    }

    /**
     * Get the short value of the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return short value
     */
    public static short getShortVolatile(Object object, String fieldName) {
        long offset = getObjectFieldOffset(object, fieldName);
        return unsafe.getShortVolatile(object, offset);
    }

    /**
     * Get the byte value of the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return byte value
     */
    public static byte getByteVolatile(Object object, String fieldName) {
        long offset = getObjectFieldOffset(object, fieldName);
        return unsafe.getByteVolatile(object, offset);
    }

    /**
     * Get the boolean value of the specified object field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return boolean value
     */
    public static boolean getBooleanVolatile(Object object, String fieldName) {
        long offset = getObjectFieldOffset(object, fieldName);
        return unsafe.getBooleanVolatile(object, offset);
    }

    /**
     * Get the char value of the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return char value
     */
    public static char getCharVolatile(Object object, String fieldName) {
        long offset = getObjectFieldOffset(object, fieldName);
        return unsafe.getCharVolatile(object, offset);
    }

    /**
     * Get the int value of the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return int value
     */
    public static int getIntVolatile(Object object, String fieldName) {
        long offset = getObjectFieldOffset(object, fieldName);
        return unsafe.getIntVolatile(object, offset);
    }

    /**
     * get the offset of the object field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return offset
     * @throws IllegalArgumentException If the class is null, or the field name is blank or empty or is matched at multiple places in the inheritance hierarchy
     * @throws NullPointerException     If any argument is <code>null</code>
     */
    protected static long getObjectFieldOffset(Object object, String fieldName) throws IllegalArgumentException, NullPointerException {
        Class<?> type = object.getClass();
        Long offsetFromCache = getOffsetFromCache(type, fieldName);
        if (offsetFromCache != null) {
            return offsetFromCache;
        }
        Field field = FieldUtils.getField(type, fieldName, true);
        long offset = unsafe.objectFieldOffset(field);
        putOffsetFromCache(type, fieldName, offset);
        return offset;
    }

    /**
     * Get the offset of a class static field
     *
     * @param type      the target type
     * @param fieldName the name of {@link Field}
     * @return offset
     */
    public static long getStaticFieldOffset(Class<?> type, String fieldName) {
        Field field = FieldUtils.getField(type, fieldName, true);
        return unsafe.staticFieldOffset(field);
    }
}
