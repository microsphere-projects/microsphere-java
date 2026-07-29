package io.microsphere.misc;

import io.microsphere.util.Assert;
import io.microsphere.util.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.reflect.FieldUtils.findField;
import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.reflect.FieldUtils.getStaticFieldValue;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.util.Assert.assertArrayIndex;
import static io.microsphere.util.Assert.assertFieldMatchType;
import static io.microsphere.util.ClassLoaderUtils.loadClass;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.security.AccessController.doPrivileged;

/**
 * {@link sun.misc.Unsafe} Utility class <p/> <b> Take case to use those utility methods in order to the stability fo
 * JVM </b>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see sun.misc.Unsafe
 * @since 1.0.0
 */
public abstract class UnsafeUtils implements Utils {

    static final String UNSAFE_CLASS_NAME = "sun.misc.Unsafe";

    static final Class<?> UNSAFE_CLASS = loadClass(getSystemClassLoader(), UNSAFE_CLASS_NAME);

    static final Object unsafe = getUnsafe();

    // Peek and Poke operations | (compilers should optimize these to memory ops)
    // These work on object fields in the Java heap.
    // They will not work on elements of packed arrays.

    /**
     * @see {@link sun.misc.Unsafe#getInt(Object, long)}
     */
    static final Method getIntMethod = findMethod(UNSAFE_CLASS, "getInt", Object.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#putInt(Object, long, int)}
     */
    static final Method putIntMethod = findMethod(UNSAFE_CLASS, "putInt", Object.class, long.class, int.class);

    /**
     * @see {@link sun.misc.Unsafe#getObject(Object, long)}
     */
    static final Method getObjectMethod = findMethod(UNSAFE_CLASS, "getObject", Object.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#putObject(Object, long, Object)}
     */
    static final Method putObjectMethod = findMethod(UNSAFE_CLASS, "putObject", Object.class, long.class, Object.class);

    /**
     * @see {@link sun.misc.Unsafe#getBoolean(Object, long)}
     */
    static final Method getBooleanMethod = findMethod(UNSAFE_CLASS, "getBoolean", Object.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#putBoolean(Object, long, boolean)}
     */
    static final Method putBooleanMethod = findMethod(UNSAFE_CLASS, "putBoolean", Object.class, long.class, boolean.class);

    /**
     * @see {@link sun.misc.Unsafe#getByte(Object, long)}
     */
    static final Method getByteMethod = findMethod(UNSAFE_CLASS, "getByte", Object.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#putByte(Object, long, byte)}
     */
    static final Method putByteMethod = findMethod(UNSAFE_CLASS, "putByte", Object.class, long.class, byte.class);

    /**
     * @see {@link sun.misc.Unsafe#getShort(Object, long)}
     */
    static final Method getShortMethod = findMethod(UNSAFE_CLASS, "getShort", Object.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#putShort(Object, long, short)}
     */
    static final Method putShortMethod = findMethod(UNSAFE_CLASS, "putShort", Object.class, long.class, short.class);

    /**
     * @see {@link sun.misc.Unsafe#getChar(Object, long)}
     */
    static final Method getCharMethod = findMethod(UNSAFE_CLASS, "getChar", Object.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#putChar(Object, long, char)}
     */
    static final Method putCharMethod = findMethod(UNSAFE_CLASS, "putChar", Object.class, long.class, char.class);

    /**
     * @see {@link sun.misc.Unsafe#getLong(Object, long)}
     */
    static final Method getLongMethod = findMethod(UNSAFE_CLASS, "getLong", Object.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#putLong(Object, long, long)}
     */
    static final Method putLongMethod = findMethod(UNSAFE_CLASS, "putLong", Object.class, long.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#getFloat(Object, long)}
     */
    static final Method getFloatMethod = findMethod(UNSAFE_CLASS, "getFloat", Object.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#putFloat(Object, long, float)}
     */
    static final Method putFloatMethod = findMethod(UNSAFE_CLASS, "putFloat", Object.class, long.class, float.class);

    /**
     * @see {@link sun.misc.Unsafe#getDouble(Object, long)}
     */
    static final Method getDoubleMethod = findMethod(UNSAFE_CLASS, "getDouble", Object.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#putDouble(Object, long, double)}
     */
    static final Method putDoubleMethod = findMethod(UNSAFE_CLASS, "putDouble", Object.class, long.class, double.class);

    // These work on values in the C heap.

    /**
     * @see {@link sun.misc.Unsafe#getByte(long)}
     */
    static final Method getByteFromAddressMethod = findMethod(UNSAFE_CLASS, "getByte", long.class);

    /**
     * @see {@link sun.misc.Unsafe#putByte(long, byte)}
     */
    static final Method putByteToAddressMethod = findMethod(UNSAFE_CLASS, "putByte", long.class, byte.class);

    /**
     * @see {@link sun.misc.Unsafe#getShort(long)}
     */
    static final Method getShortFromAddressMethod = findMethod(UNSAFE_CLASS, "getShort", long.class);

    /**
     * @see {@link sun.misc.Unsafe#putShort(long, short)}
     */
    static final Method putShortToAddressMethod = findMethod(UNSAFE_CLASS, "putShort", long.class, short.class);

    /**
     * @see {@link sun.misc.Unsafe#getChar(long)}
     */
    static final Method getCharFromAddressMethod = findMethod(UNSAFE_CLASS, "getChar", long.class);

    /**
     * @see {@link sun.misc.Unsafe#putChar(long, char)}
     */
    static final Method putCharToAddressMethod = findMethod(UNSAFE_CLASS, "putChar", long.class, char.class);

    /**
     * @see {@link sun.misc.Unsafe#getInt(long)}
     */
    static final Method getIntFromAddressMethod = findMethod(UNSAFE_CLASS, "getInt", long.class);

    /**
     * @see {@link sun.misc.Unsafe#putInt(long, int)}
     */
    static final Method putIntToAddressMethod = findMethod(UNSAFE_CLASS, "putInt", long.class, int.class);

    /**
     * @see {@link sun.misc.Unsafe#getLong(long)}
     */
    static final Method getLongFromAddressMethod = findMethod(UNSAFE_CLASS, "getLong", long.class);

    /**
     * @see {@link sun.misc.Unsafe#putLong(long, long)}
     */
    static final Method putLongToAddressMethod = findMethod(UNSAFE_CLASS, "putLong", long.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#getFloat(long)}
     */
    static final Method getFloatFromAddressMethod = findMethod(UNSAFE_CLASS, "getFloat", long.class);

    /**
     * @see {@link sun.misc.Unsafe#putFloat(long, float)}
     */
    static final Method putFloatToAddressMethod = findMethod(UNSAFE_CLASS, "putFloat", long.class, float.class);

    /**
     * @see {@link sun.misc.Unsafe#getDouble(long)}
     */
    static final Method getDoubleFromAddressMethod = findMethod(UNSAFE_CLASS, "getDouble", long.class);

    /**
     * @see {@link sun.misc.Unsafe#putDouble(long, double)}
     */
    static final Method putDoubleToAddressMethod = findMethod(UNSAFE_CLASS, "putDouble", long.class, double.class);

    /**
     * @see {@link sun.misc.Unsafe#getAddress(long)}
     */
    static final Method getAddressMethod = findMethod(UNSAFE_CLASS, "getAddress", long.class);

    /**
     * @see {@link sun.misc.Unsafe#putAddress(long, long)}
     */
    static final Method putAddressMethod = findMethod(UNSAFE_CLASS, "putAddress", long.class, long.class);

    // Wrappers for malloc, realloc, free:

    /**
     * @see {@link sun.misc.Unsafe#allocateMemory(long)}
     */
    static final Method allocateMemoryMethod = findMethod(UNSAFE_CLASS, "allocateMemory", long.class);

    /**
     * @see {@link sun.misc.Unsafe#reallocateMemory(long, long)}
     */
    static final Method reallocateMemoryMethod = findMethod(UNSAFE_CLASS, "reallocateMemory", long.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#setMemory(Object, long, long, byte)}
     */
    static final Method setMemoryMethod = findMethod(UNSAFE_CLASS, "setMemory", Object.class, long.class, long.class, byte.class);

    /**
     * @see {@link sun.misc.Unsafe#setMemory(long, long, byte)}
     */
    static final Method setMemoryToAddressMethod = findMethod(UNSAFE_CLASS, "setMemory", long.class, long.class, byte.class);

    /**
     * @see {@link sun.misc.Unsafe#copyMemory(Object, long, Object, long, long)}
     */
    static final Method copyMemoryMethod = findMethod(UNSAFE_CLASS, "copyMemory", Object.class, long.class, Object.class, long.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#copyMemory(long, long, long)}
     */
    static final Method copyMemoryFromAddressMethod = findMethod(UNSAFE_CLASS, "copyMemory", long.class, long.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#freeMemory(long)}
     */
    static final Method freeMemoryMethod = findMethod(UNSAFE_CLASS, "freeMemory", long.class);

    // Random queries

    /**
     * @see {@link sun.misc.Unsafe#objectFieldOffset(Field)}
     */
    static final Method objectFieldOffsetMethod = findMethod(UNSAFE_CLASS, "objectFieldOffset", Field.class);

    /**
     * @see {@link sun.misc.Unsafe#staticFieldOffset(Field)}
     */
    static final Method staticFieldOffsetMethod = findMethod(UNSAFE_CLASS, "staticFieldOffset", Field.class);

    /**
     * @see {@link sun.misc.Unsafe#staticFieldBase(Field)}
     */
    static final Method staticFieldBaseMethod = findMethod(UNSAFE_CLASS, "staticFieldBase", Field.class);

    /**
     * @see {@link sun.misc.Unsafe#arrayBaseOffset(Class)}
     */
    static final Method arrayBaseOffsetMethod = findMethod(UNSAFE_CLASS, "arrayBaseOffset", Class.class);

    /**
     * @see {@link sun.misc.Unsafe#arrayIndexScale(Class)}
     */
    static final Method arrayIndexScaleMethod = findMethod(UNSAFE_CLASS, "arrayIndexScale", Class.class);

    /**
     * @see {@link sun.misc.Unsafe#addressSize()}
     */
    static final Method addressSizeMethod = findMethod(UNSAFE_CLASS, "addressSize");

    /**
     * @see {@link sun.misc.Unsafe#pageSize()}
     */
    static final Method pageSizeMethod = findMethod(UNSAFE_CLASS, "pageSize");

    // Random trusted operations from JNI:

    /**
     * @see {@link sun.misc.Unsafe#allocateInstance(Class)}
     */
    static final Method allocateInstanceMethod = findMethod(UNSAFE_CLASS, "allocateInstance", Class.class);

    /**
     * @see {@link sun.misc.Unsafe#throwException(Throwable)}
     */
    static final Method throwExceptionMethod = findMethod(UNSAFE_CLASS, "throwException", Throwable.class);

    /**
     * @see {@link sun.misc.Unsafe#compareAndSwapObject(Object, long, Object, Object)}
     */
    static final Method compareAndSwapObjectMethod = findMethod(UNSAFE_CLASS, "compareAndSwapObject", Object.class, long.class, Object.class, Object.class);

    /**
     * @see {@link sun.misc.Unsafe#compareAndSwapInt(Object, long, int, int)}
     */
    static final Method compareAndSwapIntMethod = findMethod(UNSAFE_CLASS, "compareAndSwapInt", Object.class, long.class, int.class, int.class);

    /**
     * @see {@link sun.misc.Unsafe#compareAndSwapLong(Object, long, long, long)}
     */
    static final Method compareAndSwapLongMethod = findMethod(UNSAFE_CLASS, "compareAndSwapLong", Object.class, long.class, long.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#getObjectVolatile(Object, long)}
     */
    static final Method getObjectVolatileMethod = findMethod(UNSAFE_CLASS, "getObjectVolatile", Object.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#putObjectVolatile(Object, long, Object)}
     */
    static final Method putObjectVolatileMethod = findMethod(UNSAFE_CLASS, "putObjectVolatile", Object.class, long.class, Object.class);

    /**
     * @see {@link sun.misc.Unsafe#getIntVolatile(Object, long)}
     */
    static final Method getIntVolatileMethod = findMethod(UNSAFE_CLASS, "getIntVolatile", Object.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#putIntVolatile(Object, long, int)}
     */
    static final Method putIntVolatileMethod = findMethod(UNSAFE_CLASS, "putIntVolatile", Object.class, long.class, int.class);

    /**
     * @see {@link sun.misc.Unsafe#getBooleanVolatile(Object, long)}
     */
    static final Method getBooleanVolatileMethod = findMethod(UNSAFE_CLASS, "getBooleanVolatile", Object.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#putBooleanVolatile(Object, long, boolean)}
     */
    static final Method putBooleanVolatileMethod = findMethod(UNSAFE_CLASS, "putBooleanVolatile", Object.class, long.class, boolean.class);

    /**
     * @see {@link sun.misc.Unsafe#getByteVolatile(Object, long)}
     */
    static final Method getByteVolatileMethod = findMethod(UNSAFE_CLASS, "getByteVolatile", Object.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#putByteVolatile(Object, long, byte)}
     */
    static final Method putByteVolatileMethod = findMethod(UNSAFE_CLASS, "putByteVolatile", Object.class, long.class, byte.class);

    /**
     * @see {@link sun.misc.Unsafe#getShortVolatile(Object, long)}
     */
    static final Method getShortVolatileMethod = findMethod(UNSAFE_CLASS, "getShortVolatile", Object.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#putShortVolatile(Object, long, short)}
     */
    static final Method putShortVolatileMethod = findMethod(UNSAFE_CLASS, "putShortVolatile", Object.class, long.class, short.class);

    /**
     * @see {@link sun.misc.Unsafe#getCharVolatile(Object, long)}
     */
    static final Method getCharVolatileMethod = findMethod(UNSAFE_CLASS, "getCharVolatile", Object.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#putCharVolatile(Object, long, char)}
     */
    static final Method putCharVolatileMethod = findMethod(UNSAFE_CLASS, "putCharVolatile", Object.class, long.class, char.class);

    /**
     * @see {@link sun.misc.Unsafe#getLongVolatile(Object, long)}
     */
    static final Method getLongVolatileMethod = findMethod(UNSAFE_CLASS, "getLongVolatile", Object.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#putLongVolatile(Object, long, long)}
     */
    static final Method putLongVolatileMethod = findMethod(UNSAFE_CLASS, "putLongVolatile", Object.class, long.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#getFloatVolatile(Object, long)}
     */
    static final Method getFloatVolatileMethod = findMethod(UNSAFE_CLASS, "getFloatVolatile", Object.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#putFloatVolatile(Object, long, float)}
     */
    static final Method putFloatVolatileMethod = findMethod(UNSAFE_CLASS, "putFloatVolatile", Object.class, long.class, float.class);

    /**
     * @see {@link sun.misc.Unsafe#getDoubleVolatile(Object, long)}
     */
    static final Method getDoubleVolatileMethod = findMethod(UNSAFE_CLASS, "getDoubleVolatile", Object.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#putDoubleVolatile(Object, long, double)}
     */
    static final Method putDoubleVolatileMethod = findMethod(UNSAFE_CLASS, "putDoubleVolatile", Object.class, long.class, double.class);

    /**
     * @see {@link sun.misc.Unsafe#putOrderedObject(Object, long, Object)}
     */
    static final Method putOrderedObjectMethod = findMethod(UNSAFE_CLASS, "putOrderedObject", Object.class, long.class, Object.class);

    /**
     * @see {@link sun.misc.Unsafe#putOrderedInt(Object, long, int)}
     */
    static final Method putOrderedIntMethod = findMethod(UNSAFE_CLASS, "putOrderedInt", Object.class, long.class, int.class);

    /**
     * @see {@link sun.misc.Unsafe#putOrderedLong(Object, long, long)}
     */
    static final Method putOrderedLongMethod = findMethod(UNSAFE_CLASS, "putOrderedLong", Object.class, long.class, long.class);

    // The following contain CAS-based Java implementations used on platforms not supporting native instructions

    /**
     * @see {@link sun.misc.Unsafe#getAndAddInt(Object, long, int)}
     */
    static final Method getAndAddIntMethod = findMethod(UNSAFE_CLASS, "getAndAddInt", Object.class, long.class, int.class);

    /**
     * @see {@link sun.misc.Unsafe#getAndAddLong(Object, long, long)}
     */
    static final Method getAndAddLongMethod = findMethod(UNSAFE_CLASS, "getAndAddLong", Object.class, long.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#getAndSetInt(Object, long, int)}
     */
    static final Method getAndSetIntMethod = findMethod(UNSAFE_CLASS, "getAndSetInt", Object.class, long.class, int.class);

    /**
     * @see {@link sun.misc.Unsafe#getAndSetLong(Object, long, long)}
     */
    static final Method getAndSetLongMethod = findMethod(UNSAFE_CLASS, "getAndSetLong", Object.class, long.class, long.class);

    /**
     * @see {@link sun.misc.Unsafe#getAndSetObject(Object, long, Object)}
     */
    static final Method getAndSetObjectMethod = findMethod(UNSAFE_CLASS, "getAndSetObject", Object.class, long.class, Object.class);

    /**
     * @see {@link sun.misc.Unsafe#loadFence()}
     */
    static final Method loadFenceMethod = findMethod(UNSAFE_CLASS, "loadFence");

    /**
     * @see {@link sun.misc.Unsafe#storeFence()}
     */
    static final Method storeFenceMethod = findMethod(UNSAFE_CLASS, "storeFence");

    /**
     * @see {@link sun.misc.Unsafe#fullFence()}
     */
    static final Method fullFenceMethod = findMethod(UNSAFE_CLASS, "fullFence");

    /**
     * <code>long</code> Array base index
     *
     * @see {@link sun.misc.Unsafe#ARRAY_LONG_BASE_OFFSET}
     */
    public static final int LONG_ARRAY_BASE_OFFSET = getStaticFieldValue(UNSAFE_CLASS, "ARRAY_LONG_BASE_OFFSET");

    /**
     * <code>int</code> Array base index
     */
    public static final int INT_ARRAY_BASE_OFFSET = getStaticFieldValue(UNSAFE_CLASS, "ARRAY_INT_BASE_OFFSET");

    /**
     * <code>short</code> Array base index
     */
    public static final int SHORT_ARRAY_BASE_OFFSET = getStaticFieldValue(UNSAFE_CLASS, "ARRAY_SHORT_BASE_OFFSET");

    /**
     * <code>byte</code> Array base index
     */
    public static final int BYTE_ARRAY_BASE_OFFSET = getStaticFieldValue(UNSAFE_CLASS, "ARRAY_BYTE_BASE_OFFSET");

    /**
     * <code>boolean</code> Array base index
     */
    public static final int BOOLEAN_ARRAY_BASE_OFFSET = getStaticFieldValue(UNSAFE_CLASS, "ARRAY_BOOLEAN_BASE_OFFSET");

    /**
     * <code>double</code> Array base index
     */
    public static final int DOUBLE_ARRAY_BASE_OFFSET = getStaticFieldValue(UNSAFE_CLASS, "ARRAY_DOUBLE_BASE_OFFSET");

    /**
     * <code>float</code> Array base index
     */
    public static final int FLOAT_ARRAY_BASE_OFFSET = getStaticFieldValue(UNSAFE_CLASS, "ARRAY_FLOAT_BASE_OFFSET");

    /**
     * <code>char</code> Array base index
     */
    public static final int CHAR_ARRAY_BASE_OFFSET = getStaticFieldValue(UNSAFE_CLASS, "ARRAY_CHAR_BASE_OFFSET");

    /**
     * <code>java.lang.Object</code> Array base index
     */
    public static final int OBJECT_ARRAY_BASE_OFFSET = getStaticFieldValue(UNSAFE_CLASS, "ARRAY_OBJECT_BASE_OFFSET");

    /**
     * <code>long</code> Array Index scale
     */
    public static final int LONG_ARRAY_INDEX_SCALE = getStaticFieldValue(UNSAFE_CLASS, "ARRAY_LONG_INDEX_SCALE");

    /**
     * <code>int</code> Array Index scale
     */
    public static final int INT_ARRAY_INDEX_SCALE = getStaticFieldValue(UNSAFE_CLASS, "ARRAY_INT_INDEX_SCALE");

    /**
     * <code>short</code> Array Index scale
     */
    public static final int SHORT_ARRAY_INDEX_SCALE = getStaticFieldValue(UNSAFE_CLASS, "ARRAY_SHORT_INDEX_SCALE");

    /**
     * <code>byte</code> Array Index scale
     */
    public static final int BYTE_ARRAY_INDEX_SCALE = getStaticFieldValue(UNSAFE_CLASS, "ARRAY_BYTE_INDEX_SCALE");

    /**
     * <code>boolean</code> Array Index scale
     */
    public static final int BOOLEAN_ARRAY_INDEX_SCALE = getStaticFieldValue(UNSAFE_CLASS, "ARRAY_BOOLEAN_INDEX_SCALE");

    /**
     * <code>double</code> Array Index scale
     */
    public static final int DOUBLE_ARRAY_INDEX_SCALE = getStaticFieldValue(UNSAFE_CLASS, "ARRAY_DOUBLE_INDEX_SCALE");

    /**
     * <code>float</code> Array Index scale
     */
    public static final int FLOAT_ARRAY_INDEX_SCALE = getStaticFieldValue(UNSAFE_CLASS, "ARRAY_FLOAT_INDEX_SCALE");

    /**
     * <code>char</code> Array Index scale
     */
    public static final int CHAR_ARRAY_INDEX_SCALE = getStaticFieldValue(UNSAFE_CLASS, "ARRAY_CHAR_INDEX_SCALE");

    /**
     * <code>java.lang.Object</code> Array Index scale
     */
    public static final int OBJECT_ARRAY_INDEX_SCALE = getStaticFieldValue(UNSAFE_CLASS, "ARRAY_OBJECT_INDEX_SCALE");

    /**
     * Offset Cache,
     */
    private final static ConcurrentMap<String, Long> offsetCache = new ConcurrentHashMap<>();

    /**
     * @return the {@link sun.misc.Unsafe} instance
     */
    static Object getUnsafe() {
        final PrivilegedExceptionAction<Object> action = () -> {
            Field theUnsafe = UNSAFE_CLASS.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return theUnsafe.get(null);
        };
        return execute(() -> doPrivileged(action), e -> {
            throw new UnsupportedOperationException("Current JVM does not support sun.misc.Unsafe", e);
        });
    }

    /**
     * Calculate Array Index Offset
     *
     * @param index      Index
     * @param baseOffset {@link sun.misc.Unsafe#arrayBaseOffset(Class)}
     * @param indexScale {@link sun.misc.Unsafe#arrayIndexScale(Class)}
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
        return type.getName() + "#" + fieldName;
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
    public static long getLongVolatileFromArray(Object object, String fieldName, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, NullPointerException {
        Object array = getFieldValue(true, object, fieldName);
        assertArrayIndex(array, index);
        long offset = longArrayIndexOffset(index);
        return getLongVolatile(array, offset);
    }

    /**
     * Get the value of the target Index in the object Array field
     *
     * @param object    Object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @return the <code>int<code> value
     */
    public static int getIntVolatileFromArray(Object object, String fieldName, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, NullPointerException {
        Object array = getFieldValue(true, object, fieldName);
        assertArrayIndex(array, index);
        long offset = intArrayIndexOffset(index);
        return getIntVolatile(array, offset);
    }

    /**
     * Get the <code>short<code> value of the target Index in the Array field of the object
     *
     * @param object    Object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @return the <code>short<code> value
     */
    public static short getShortVolatileFromArray(Object object, String fieldName, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, NullPointerException {
        Object array = getFieldValue(true, object, fieldName);
        assertArrayIndex(array, index);
        long offset = shortArrayIndexOffset(index);
        return getShortVolatile(array, offset);
    }

    /**
     * Get the <code>byte<code> value of the target Index in the object Array field
     *
     * @param object    Object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @return the <code>byte<code> value
     */
    public static byte getByteVolatileFromArray(Object object, String fieldName, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, NullPointerException {
        Object array = getFieldValue(true, object, fieldName);
        assertArrayIndex(array, index);
        long offset = byteArrayIndexOffset(index);
        return getByteVolatile(array, offset);
    }

    /**
     * Get the <code>boolean<code> value of the target Index in the Array field of the object
     *
     * @param object    Object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @return the <code>boolean<code> value
     */
    public static boolean getBooleanVolatileFromArray(Object object, String fieldName, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, NullPointerException {
        Object array = getFieldValue(true, object, fieldName);
        assertArrayIndex(array, index);
        long offset = booleanArrayIndexOffset(index);
        return getBooleanVolatile(array, offset);
    }

    /**
     * Get the <code>double<code> value of the target Index in the Array field of the object
     *
     * @param object    Object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @return the <code>double<code> value
     */
    public static double getDoubleVolatileFromArray(Object object, String fieldName, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, NullPointerException {
        Object array = getFieldValue(true, object, fieldName);
        assertArrayIndex(array, index);
        long offset = doubleArrayIndexOffset(index);
        return getDoubleVolatile(array, offset);
    }

    /**
     * Get the <code>float<code> value of the target Index in the object Array field
     *
     * @param object    Object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @return the target index
     */
    public static float getFloatVolatileFromArray(Object object, String fieldName, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, NullPointerException {
        Object array = getFieldValue(true, object, fieldName);
        assertArrayIndex(array, index);
        long offset = floatArrayIndexOffset(index);
        return getFloatVolatile(array, offset);
    }

    /**
     * Get the <code>char<code> value of the target Index in the object Array field
     *
     * @param object    Object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @return the <code>char<code> value
     * @throws IllegalArgumentException       See {@link Assert#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static char getCharVolatileFromArray(Object object, String fieldName, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, NullPointerException {
        Object array = getFieldValue(true, object, fieldName);
        assertArrayIndex(array, index);
        long offset = charArrayIndexOffset(index);
        return getCharVolatile(array, offset);
    }

    /**
     * Get the <code>java.lang.Object<code> value of the target Index in the object Array field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @return the <code>java.lang.Object<code> value
     * @throws IllegalArgumentException       see {@link Assert#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static Object getObjectVolatileFromArray(Object object, String fieldName, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, NullPointerException {
        Object array = getFieldValue(true, object, fieldName);
        assertArrayIndex(array, index);
        long offset = objectArrayIndexOffset(index);
        return getObjectVolatile(array, offset);
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
        long offset = objectFieldOffset(object, fieldName);
        putDouble(object, offset, value);
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
        long offset = objectFieldOffset(object, fieldName);
        putFloat(object, offset, value);
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
        long offset = objectFieldOffset(object, fieldName);
        putShort(object, offset, value);
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
        long offset = objectFieldOffset(object, fieldName);
        putByte(object, offset, value);
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
        long offset = objectFieldOffset(object, fieldName);
        putBoolean(object, offset, value);
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
        long offset = objectFieldOffset(object, fieldName);
        putChar(object, offset, value);
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
        long offset = objectFieldOffset(object, fieldName);
        putObject(object, offset, value);
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
        long offset = objectFieldOffset(object, fieldName);
        putLong(object, offset, value);
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
        long offset = objectFieldOffset(object, fieldName);
        putInt(object, offset, value);
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
        long offset = objectFieldOffset(object, fieldName);
        putOrderedInt(object, offset, value);
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
        long offset = objectFieldOffset(object, fieldName);
        putOrderedLong(object, offset, value);
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
        long offset = objectFieldOffset(object, fieldName);
        putOrderedObject(object, offset, value);
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
        long offset = objectFieldOffset(object, fieldName);
        putDoubleVolatile(object, offset, value);
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
        long offset = objectFieldOffset(object, fieldName);
        putFloatVolatile(object, offset, value);
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
        long offset = objectFieldOffset(object, fieldName);
        putShortVolatile(object, offset, value);
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
        long offset = objectFieldOffset(object, fieldName);
        putByteVolatile(object, offset, value);
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
        long offset = objectFieldOffset(object, fieldName);
        putBooleanVolatile(object, offset, value);
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
        long offset = objectFieldOffset(object, fieldName);
        putCharVolatile(object, offset, value);
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
        long offset = objectFieldOffset(object, fieldName);
        putObjectVolatile(object, offset, value);
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
        long offset = objectFieldOffset(object, fieldName);
        putLongVolatile(object, offset, value);
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
        long offset = objectFieldOffset(object, fieldName);
        putIntVolatile(object, offset, value);
    }

    /**
     * Sets the given <code>long<code> value to a field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>long</code> value
     * @throws IllegalArgumentException       see {@link Assert#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putLongVolatileIntoArray(Object object, String fieldName, int index, long value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, NullPointerException {
        Object array = getFieldValue(true, object, fieldName);
        assertArrayIndex(array, index);
        long offset = longArrayIndexOffset(index);
        putLongVolatile(array, offset, value);
    }

    /**
     * Sets the given <code>long<code> value to the fields of the specified object (sequential writing)
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>long</code> value
     * @throws IllegalArgumentException       see {@link Assert#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putOrderedLongIntoArray(Object object, String fieldName, int index, long value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, NullPointerException {
        Object array = getFieldValue(true, object, fieldName);
        assertArrayIndex(array, index);
        long offset = longArrayIndexOffset(index);
        putOrderedLong(array, offset, value);
    }

    /**
     * Sets the given <code>int<code> value to a field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>int</code> value
     * @throws IllegalArgumentException       see {@link Assert#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putIntVolatileIntoArray(Object object, String fieldName, int index, int value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, NullPointerException {
        Object array = getFieldValue(true, object, fieldName);
        assertArrayIndex(array, index);
        long offset = intArrayIndexOffset(index);
        putIntVolatile(array, offset, value);
    }

    /**
     * Sets the given <code>int<code> value to a field of the specified object (sequential writing)
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>int</code> value
     * @throws IllegalArgumentException       see {@link Assert#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putOrderedIntIntoArray(Object object, String fieldName, int index, int value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, NullPointerException {
        Object array = getFieldValue(true, object, fieldName);
        assertArrayIndex(array, index);
        long offset = intArrayIndexOffset(index);
        putOrderedInt(array, offset, value);
    }

    /**
     * Sets the given <code>short<code> value to a field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>short</code> value
     * @throws IllegalArgumentException       see {@link Assert#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putShortVolatileIntoArray(Object object, String fieldName, int index, short value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, NullPointerException {
        Object array = getFieldValue(true, object, fieldName);
        assertArrayIndex(array, index);
        long offset = shortArrayIndexOffset(index);
        putShortVolatile(array, offset, value);
    }

    /**
     * Sets the given <code>byte<code> value to a field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>byte</code> value
     * @throws IllegalArgumentException       see {@link Assert#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putByteVolatileIntoArray(Object object, String fieldName, int index, byte value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, NullPointerException {
        Object array = getFieldValue(true, object, fieldName);
        assertArrayIndex(array, index);
        long offset = byteArrayIndexOffset(index);
        putByteVolatile(array, offset, value);
    }

    /**
     * Sets the given <code>boolean<code> value to a field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>boolean</code> value
     * @throws IllegalArgumentException       see {@link Assert#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putBooleanVolatileIntoArray(Object object, String fieldName, int index, boolean value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, NullPointerException {
        Object array = getFieldValue(true, object, fieldName);
        assertArrayIndex(array, index);
        long offset = booleanArrayIndexOffset(index);
        putBooleanVolatile(array, offset, value);
    }

    /**
     * Sets the given <code>double<code> value to a field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>double</code> value
     * @throws IllegalArgumentException       see {@link Assert#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putDoubleVolatileIntoArray(Object object, String fieldName, int index, double value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, NullPointerException {
        Object array = getFieldValue(true, object, fieldName);
        assertArrayIndex(array, index);
        long offset = doubleArrayIndexOffset(index);
        putDoubleVolatile(array, offset, value);
    }

    /**
     * Sets the given <code>float<code> value to the field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>float</code> value
     * @throws IllegalArgumentException       see {@link Assert#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putFloatVolatileIntoArray(Object object, String fieldName, int index, float value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, NullPointerException {
        Object array = getFieldValue(true, object, fieldName);
        assertArrayIndex(array, index);
        long offset = floatArrayIndexOffset(index);
        putFloatVolatile(array, offset, value);
    }

    /**
     * Sets the given <code>char<code> value to a field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>char</code> value
     * @throws IllegalArgumentException       see {@link Assert#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putCharVolatileIntoArray(Object object, String fieldName, int index, char value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, NullPointerException {
        Object array = getFieldValue(true, object, fieldName);
        assertArrayIndex(array, index);
        long offset = charArrayIndexOffset(index);
        putCharVolatile(array, offset, value);
    }

    /**
     * Sets the given <code>java.lang.Object<code> value to a field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>java.lang.Object</code> value
     * @throws IllegalArgumentException       see {@link Assert#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putObjectVolatileIntoArray(Object object, String fieldName, int index, Object value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, NullPointerException {
        Object array = getFieldValue(true, object, fieldName);
        assertArrayIndex(array, index);
        long offset = objectArrayIndexOffset(index);
        putObjectVolatile(array, offset, value);
    }

    /**
     * Sets the given <code>java.lang.Object<code> value to the fields of the specified object (sequential writing)
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param index     the index
     * @param value     <code>java.lang.Object</code> value
     * @throws IllegalArgumentException       see {@link Assert#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index<code> is less than 0, or greater than or equal to the Array length
     */
    public static void putOrderedObjectIntoArray(Object object, String fieldName, int index, Object value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, NullPointerException {
        Object array = getFieldValue(true, object, fieldName);
        assertArrayIndex(array, index);
        long offset = objectArrayIndexOffset(index);
        putOrderedObject(array, offset, value);
    }

    /**
     * Get the Object value of the specified object field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return Object value
     */
    public static Object getObject(Object object, String fieldName) {
        long offset = objectFieldOffset(object, fieldName);
        return getObject(object, offset);
    }

    /**
     * Get the long value of the specified object field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return long value
     */
    public static long getLong(Object object, String fieldName) {
        long offset = objectFieldOffset(object, fieldName);
        return getLong(object, offset);
    }

    /**
     * Get the double value of the specified object field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return double value
     */
    public static double getDouble(Object object, String fieldName) {
        long offset = objectFieldOffset(object, fieldName);
        return getDouble(object, offset);
    }

    /**
     * Get the float value of the specified object field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return float value
     */
    public static float getFloat(Object object, String fieldName) {
        long offset = objectFieldOffset(object, fieldName);
        return getFloat(object, offset);
    }

    /**
     * Get the short value of the specified object field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return short value
     */
    public static short getShort(Object object, String fieldName) {
        long offset = objectFieldOffset(object, fieldName);
        return getShort(object, offset);
    }

    /**
     * Get the byte value of the specified object field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return byte value
     */
    public static byte getByte(Object object, String fieldName) {
        long offset = objectFieldOffset(object, fieldName);
        return getByte(object, offset);
    }

    /**
     * Get the boolean value of the specified object field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return boolean value
     */
    public static boolean getBoolean(Object object, String fieldName) {
        long offset = objectFieldOffset(object, fieldName);
        return getBoolean(object, offset);
    }

    /**
     * Get the char value of the specified object field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return char value
     */
    public static char getChar(Object object, String fieldName) {
        long offset = objectFieldOffset(object, fieldName);
        return getChar(object, offset);
    }

    /**
     * Get the int value of the specified object field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return int value
     */
    public static int getInt(Object object, String fieldName) {
        long offset = objectFieldOffset(object, fieldName);
        return getInt(object, offset);
    }

    /**
     * Get the Object value of the specified object <code>volatile<code> field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return Object value
     */
    public static Object getObjectVolatile(Object object, String fieldName) {
        long offset = objectFieldOffset(object, fieldName);
        return getObjectVolatile(object, offset);
    }

    /**
     * Get the long value of the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return long value
     */
    public static long getLongVolatile(Object object, String fieldName) {
        long offset = objectFieldOffset(object, fieldName);
        return getLongVolatile(object, offset);
    }

    /**
     * Get the double value of the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return double value
     */
    public static double getDoubleVolatile(Object object, String fieldName) {
        long offset = objectFieldOffset(object, fieldName);
        return getDoubleVolatile(object, offset);
    }

    /**
     * Get the float value of the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return float value
     */
    public static float getFloatVolatile(Object object, String fieldName) {
        long offset = objectFieldOffset(object, fieldName);
        return getFloatVolatile(object, offset);
    }

    /**
     * Get the short value of the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return short value
     */
    public static short getShortVolatile(Object object, String fieldName) {
        long offset = objectFieldOffset(object, fieldName);
        return getShortVolatile(object, offset);
    }

    /**
     * Get the byte value of the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return byte value
     */
    public static byte getByteVolatile(Object object, String fieldName) {
        long offset = objectFieldOffset(object, fieldName);
        return getByteVolatile(object, offset);
    }

    /**
     * Get the boolean value of the specified object field
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return boolean value
     */
    public static boolean getBooleanVolatile(Object object, String fieldName) {
        long offset = objectFieldOffset(object, fieldName);
        return getBooleanVolatile(object, offset);
    }

    /**
     * Get the char value of the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return char value
     */
    public static char getCharVolatile(Object object, String fieldName) {
        long offset = objectFieldOffset(object, fieldName);
        return getCharVolatile(object, offset);
    }

    /**
     * Get the int value of the <code>volatile<code> field of the specified object
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @return int value
     */
    public static int getIntVolatile(Object object, String fieldName) {
        long offset = objectFieldOffset(object, fieldName);
        return getIntVolatile(object, offset);
    }

    /**
     * Atomically sets the field of the specified object to the given updated value
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param expected  the expected value
     * @param newValue  the new value
     * @return {@code true} if successful, {@code false} otherwise
     */
    public static boolean compareAndSwapObject(Object object, String fieldName, Object expected, Object newValue) {
        long offset = objectFieldOffset(object, fieldName);
        return compareAndSwapObject(object, offset, expected, newValue);
    }

    /**
     * Atomically sets the field of the specified object to the given updated value
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param expected  the expected value
     * @param newValue  the new value
     * @return {@code true} if successful, {@code false} otherwise
     */
    public static boolean compareAndSwapInt(Object object, String fieldName, int expected, int newValue) {
        long offset = objectFieldOffset(object, fieldName);
        return compareAndSwapInt(object, offset, expected, newValue);
    }

    /**
     * Atomically sets the field of the specified object to the given updated value
     *
     * @param object    the target object
     * @param fieldName the name of {@link Field}
     * @param expected  the expected value
     * @param newValue  the new value
     * @return {@code true} if successful, {@code false} otherwise
     */
    public static boolean compareAndSwapLong(Object object, String fieldName, long expected, long newValue) {
        long offset = objectFieldOffset(object, fieldName);
        return compareAndSwapLong(object, offset, expected, newValue);
    }

    //| peek and poke operations
    //| (compilers should optimize these to memory ops)

    // These work on object fields in the Java heap.
    // They will not work on elements of packed arrays.

    /**
     * Fetches a value from a given Java variable.
     * More specifically, fetches a field or array element within the given
     * object {@code o} at the given offset, or (if {@code o} is null)
     * from the memory address whose numerical value is the given offset.
     * <p>
     * The results are undefined unless one of the following cases is true:
     * <ul>
     * <li>The offset was obtained from {@link #objectFieldOffset} on
     * the {@link java.lang.reflect.Field} of some Java field and the object
     * referred to by {@code o} is of a class compatible with that
     * field's class.
     *
     * <li>The offset and object reference {@code o} (either null or
     * non-null) were both obtained via {@link #staticFieldOffset}
     * and {@link #staticFieldBase} (respectively) from the
     * reflective {@link Field} representation of some Java field.
     *
     * <li>The object referred to by {@code o} is an array, and the offset
     * is an integer of the form {@code B+N*S}, where {@code N} is
     * a valid index into the array, and {@code B} and {@code S} are
     * the values obtained by {@link #arrayBaseOffset} and {@link
     * #arrayIndexScale} (respectively) from the array's class.  The value
     * referred to is the {@code N}<em>th</em> element of the array.
     *
     * </ul>
     * <p>
     * If one of the above cases is true, the call references a specific Java
     * variable (field or array element).  However, the results are undefined
     * if that variable is not in fact of the type returned by this method.
     * <p>
     * This method refers to a variable by means of two parameters, and so
     * it provides (in effect) a <em>double-register</em> addressing mode
     * for Java variables.  When the object reference is null, this method
     * uses its offset as an absolute address.  This is similar in operation
     * to methods such as {@link #getInt(long)}, which provide (in effect) a
     * <em>single-register</em> addressing mode for non-Java variables.
     * However, because Java variables may have a different layout in memory
     * from non-Java variables, programmers should not assume that these
     * two addressing modes are ever equivalent.  Also, programmers should
     * remember that offsets from the double-register addressing mode cannot
     * be portably confused with longs used in the single-register addressing
     * mode.
     *
     * @param o      Java heap object in which the variable resides, if any, else
     *               null
     * @param offset indication of where the variable resides in a Java heap
     *               object, if any, else a memory address locating the variable
     *               statically
     * @return the value fetched from the indicated Java variable
     * @throws RuntimeException No defined exceptions are thrown, not even
     *                          {@link NullPointerException}
     * @deprecated Use {@link java.lang.invoke.VarHandle#get(Object...)} or
     * {@link java.lang.foreign.java.lang.foreign.MemorySegment#get(java.lang.foreign.java.lang.foreign.ValueLayout.OfInt, long)} instead.
     */
    public static int getInt(Object o, long offset) {
        return invokeMethod(unsafe, getIntMethod, o, offset);
    }

    /**
     * Stores a value into a given Java variable.
     * <p>
     * The first two parameters are interpreted exactly as with
     * {@link #getInt(Object, long)} to refer to a specific
     * Java variable (field or array element).  The given value
     * is stored into that variable.
     * <p>
     * The variable must be of the same type as the method
     * parameter {@code x}.
     *
     * @param o      Java heap object in which the variable resides, if any, else
     *               null
     * @param offset indication of where the variable resides in a Java heap
     *               object, if any, else a memory address locating the variable
     *               statically
     * @param x      the value to store into the indicated Java variable
     * @throws RuntimeException No defined exceptions are thrown, not even
     *                          {@link NullPointerException}
     * @deprecated Use {@link java.lang.invoke.VarHandle#set(Object...)} or
     * {@link java.lang.foreign.java.lang.foreign.MemorySegment#set(java.lang.foreign.java.lang.foreign.ValueLayout.OfInt, long, int)} instead.
     */
    public static void putInt(Object o, long offset, int x) {
        invokeMethod(unsafe, putIntMethod, o, offset, x);
    }

    /**
     * Fetches a reference value from a given Java variable.
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#get(Object...)} instead.
     */
    public static Object getObject(Object o, long offset) {
        return invokeMethod(unsafe, getObjectMethod, o, offset);
    }

    /**
     * Stores a reference value into a given Java variable.
     * <p>
     * Unless the reference {@code x} being stored is either null
     * or matches the field type, the results are undefined.
     * If the reference {@code o} is non-null, card marks or
     * other store barriers for that object (if the VM requires them)
     * are updated.
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#set(Object...)} instead.
     */
    public static void putObject(Object o, long offset, Object x) {
        invokeMethod(unsafe, putObjectMethod, o, offset, x);
    }

    /**
     * @see #getInt(Object, long)
     * @deprecated Use {@link java.lang.invoke.VarHandle#get(Object...)} or
     * {@link java.lang.foreign.java.lang.foreign.MemorySegment#get(java.lang.foreign.java.lang.foreign.ValueLayout.OfBoolean, long)} instead.
     */
    public static boolean getBoolean(Object o, long offset) {
        return invokeMethod(unsafe, getBooleanMethod, o, offset);
    }

    /**
     * @see #putInt(Object, long, int)
     * @deprecated Use {@link java.lang.invoke.VarHandle#set(Object...)} or
     * {@link java.lang.foreign.java.lang.foreign.MemorySegment#set(java.lang.foreign.java.lang.foreign.ValueLayout.OfBoolean, long, boolean)} instead.
     */
    public static void putBoolean(Object o, long offset, boolean x) {
        invokeMethod(unsafe, putBooleanMethod, o, offset, x);
    }

    /**
     * @see #getInt(Object, long)
     * @deprecated Use {@link java.lang.invoke.VarHandle#get(Object...)} or
     * {@link java.lang.foreign.MemorySegment#get(java.lang.foreign.ValueLayout.OfByte, long)} instead.
     */
    public static byte getByte(Object o, long offset) {
        return invokeMethod(unsafe, getByteMethod, o, offset);
    }

    /**
     * @see #putInt(Object, long, int)
     * @deprecated Use {@link java.lang.invoke.VarHandle#set(Object...)} or
     * {@link java.lang.foreign.MemorySegment#set(java.lang.foreign.ValueLayout.OfByte, long, byte)} instead.
     */
    public static void putByte(Object o, long offset, byte x) {
        invokeMethod(unsafe, putByteMethod, o, offset, x);
    }

    /**
     * @see #getInt(Object, long)
     * @deprecated Use {@link java.lang.invoke.VarHandle#get(Object...)} or
     * {@link java.lang.foreign.MemorySegment#get(java.lang.foreign.ValueLayout.OfShort, long)} instead.
     */
    public static short getShort(Object o, long offset) {
        return invokeMethod(unsafe, getShortMethod, o, offset);
    }

    /**
     * @see #putInt(Object, long, int)
     * @deprecated Use {@link java.lang.invoke.VarHandle#set(Object...)} or
     * {@link java.lang.foreign.MemorySegment#set(java.lang.foreign.ValueLayout.OfShort, long, short)} instead.
     */
    public static void putShort(Object o, long offset, short x) {
        invokeMethod(unsafe, putShortMethod, o, offset, x);
    }

    /**
     * @see #getInt(Object, long)
     * @deprecated Use {@link java.lang.invoke.VarHandle#get(Object...)} or
     * {@link java.lang.foreign.MemorySegment#get(java.lang.foreign.ValueLayout.OfChar, long)} instead.
     */
    public static char getChar(Object o, long offset) {
        return invokeMethod(unsafe, getCharMethod, o, offset);
    }

    /**
     * @see #putInt(Object, long, int)
     * @deprecated Use {@link java.lang.invoke.VarHandle#set(Object...)} or
     * {@link java.lang.foreign.MemorySegment#set(java.lang.foreign.ValueLayout.OfChar, long, char)} instead.
     */
    public static void putChar(Object o, long offset, char x) {
        invokeMethod(unsafe, putCharMethod, o, offset, x);
    }

    /**
     * @see #getInt(Object, long)
     * @deprecated Use {@link java.lang.invoke.VarHandle#get(Object...)} or
     * {@link java.lang.foreign.MemorySegment#get(java.lang.foreign.ValueLayout.OfLong, long)} instead.
     */
    public static long getLong(Object o, long offset) {
        return invokeMethod(unsafe, getLongMethod, o, offset);
    }

    /**
     * @see #putInt(Object, long, int)
     * @deprecated Use {@link java.lang.invoke.VarHandle#set(Object...)} or
     * {@link java.lang.foreign.MemorySegment#set(java.lang.foreign.ValueLayout.OfLong, long, long)} instead.
     */
    public static void putLong(Object o, long offset, long x) {
        invokeMethod(unsafe, putLongMethod, o, offset, x);
    }

    /**
     * @see #getInt(Object, long)
     * @deprecated Use {@link java.lang.invoke.VarHandle#get(Object...)} or
     * {@link java.lang.foreign.MemorySegment#get(java.lang.foreign.ValueLayout.OfFloat, long)} instead.
     */
    public static float getFloat(Object o, long offset) {
        return invokeMethod(unsafe, getFloatMethod, o, offset);
    }

    /**
     * @see #putInt(Object, long, int)
     * @deprecated Use {@link java.lang.invoke.VarHandle#set(Object...)} or
     * {@link java.lang.foreign.MemorySegment#set(java.lang.foreign.ValueLayout.OfFloat, long, float)} instead.
     */
    public static void putFloat(Object o, long offset, float x) {
        invokeMethod(unsafe, putFloatMethod, o, offset, x);
    }

    /**
     * @see #getInt(Object, long)
     * @deprecated Use {@link java.lang.invoke.VarHandle#get(Object...)} or
     * {@link java.lang.foreign.MemorySegment#get(java.lang.foreign.ValueLayout.OfDouble, long)} instead.
     */
    public static double getDouble(Object o, long offset) {
        return invokeMethod(unsafe, getDoubleMethod, o, offset);
    }

    /**
     * @see #putInt(Object, long, int)
     * @deprecated Use {@link java.lang.invoke.VarHandle#set(Object...)} or
     * {@link java.lang.foreign.MemorySegment#set(java.lang.foreign.ValueLayout.OfDouble, long, double)} instead.
     */
    public static void putDouble(Object o, long offset, double x) {
        invokeMethod(unsafe, putDoubleMethod, o, offset, x);
    }

    // These work on values in the C heap.

    /**
     * Fetches a value from a given memory address.  If the address is zero, or
     * does not point into a block obtained from {@link #allocateMemory}, the
     * results are undefined.
     *
     * @see #allocateMemory
     * @see {@link sun.misc.Unsafe#getByte(Object, long)}
     * @deprecated Use {@link java.lang.foreign} to access off-heap memory.
     */
    public static byte getByte(long address) {
        return invokeMethod(unsafe, getByteFromAddressMethod, address);
    }

    /**
     * Stores a value into a given memory address.  If the address is zero, or
     * does not point into a block obtained from {@link #allocateMemory}, the
     * results are undefined.
     *
     * @see #getByte(long)
     * @deprecated Use {@link java.lang.foreign} to access off-heap memory.
     */
    public static void putByte(long address, byte x) {
        invokeMethod(unsafe, putByteToAddressMethod, address, x);
    }

    /**
     * @see #getByte(long)
     * @deprecated Use {@link java.lang.foreign} to access off-heap memory.
     */
    public static short getShort(long address) {
        return invokeMethod(unsafe, getShortFromAddressMethod, address);
    }

    /**
     * @see #putByte(long, byte)
     * @deprecated Use {@link java.lang.foreign} to access off-heap memory.
     */
    public static void putShort(long address, short x) {
        invokeMethod(unsafe, putShortToAddressMethod, address, x);
    }

    /**
     * @see #getByte(long)
     * @deprecated Use {@link java.lang.foreign} to access off-heap memory.
     */
    public static char getChar(long address) {
        return invokeMethod(unsafe, getCharFromAddressMethod, address);
    }

    /**
     * @see #putByte(long, byte)
     * @deprecated Use {@link java.lang.foreign} to access off-heap memory.
     */
    public static void putChar(long address, char x) {
        invokeMethod(unsafe, putCharToAddressMethod, address, x);
    }

    /**
     * @see #getByte(long)
     * @deprecated Use {@link java.lang.foreign} to access off-heap memory.
     */
    public static int getInt(long address) {
        return invokeMethod(unsafe, getIntFromAddressMethod, address);
    }

    /**
     * @see #putByte(long, byte)
     * @deprecated Use {@link java.lang.foreign} to access off-heap memory.
     */
    public static void putInt(long address, int x) {
        invokeMethod(unsafe, putIntToAddressMethod, address, x);
    }

    /**
     * @see #getByte(long)
     * @deprecated Use {@link java.lang.foreign} to access off-heap memory.
     */
    public static long getLong(long address) {
        return invokeMethod(unsafe, getLongFromAddressMethod, address);
    }

    /**
     * @see #putByte(long, byte)
     * @deprecated Use {@link java.lang.foreign} to access off-heap memory.
     */
    public static void putLong(long address, long x) {
        invokeMethod(unsafe, putLongToAddressMethod, address, x);
    }

    /**
     * @see #getByte(long)
     * @deprecated Use {@link java.lang.foreign} to access off-heap memory.
     */
    public static float getFloat(long address) {
        return invokeMethod(unsafe, getFloatFromAddressMethod, address);
    }

    /**
     * @see #putByte(long, byte)
     * @deprecated Use {@link java.lang.foreign} to access off-heap memory.
     */
    public static void putFloat(long address, float x) {
        invokeMethod(unsafe, putFloatToAddressMethod, address, x);
    }

    /**
     * @see #getByte(long)
     * @deprecated Use {@link java.lang.foreign} to access off-heap memory.
     */
    public static double getDouble(long address) {
        return invokeMethod(unsafe, getDoubleFromAddressMethod, address);
    }

    /**
     * @see #putByte(long, byte)
     * @deprecated Use {@link java.lang.foreign} to access off-heap memory.
     */
    public static void putDouble(long address, double x) {
        invokeMethod(unsafe, putDoubleToAddressMethod, address, x);
    }

    /**
     * Fetches a native pointer from a given memory address.  If the address is
     * zero, or does not point into a block obtained from {@link
     * #allocateMemory}, the results are undefined.
     *
     * <p>If the native pointer is less than 64 bits wide, it is extended as
     * an unsigned number to a Java long.  The pointer may be indexed by any
     * given byte offset, simply by adding that offset (as a simple integer) to
     * the long representing the pointer.  The number of bytes actually read
     * from the target address may be determined by consulting {@link
     * #addressSize}.
     *
     * @see #allocateMemory
     * @deprecated Use {@link java.lang.foreign} to access off-heap memory.
     */
    public static long getAddress(long address) {
        return invokeMethod(unsafe, getAddressMethod, address);
    }

    /**
     * Stores a native pointer into a given memory address.  If the address is
     * zero, or does not point into a block obtained from {@link
     * #allocateMemory}, the results are undefined.
     *
     * <p>The number of bytes actually written at the target address may be
     * determined by consulting {@link #addressSize}.
     *
     * @see #getAddress(long)
     * @deprecated Use {@link java.lang.foreign} to access off-heap memory.
     */
    public static void putAddress(long address, long x) {
        invokeMethod(unsafe, putAddressMethod, address, x);
    }

    //| wrappers for malloc, realloc, free:

    /**
     * Allocates a new block of native memory, of the given size in bytes.  The
     * contents of the memory are uninitialized; they will generally be
     * garbage.  The resulting native pointer will be zero if and only if the
     * requested size is zero.  The resulting native pointer will be aligned for
     * all value types.   Dispose of this memory by calling {@link #freeMemory}
     * or resize it with {@link #reallocateMemory}.
     *
     * <em>Note:</em> It is the responsibility of the caller to make
     * sure arguments are checked before the methods are called. While
     * some rudimentary checks are performed on the input, the checks
     * are best effort and when performance is an overriding priority,
     * as when methods of this class are optimized by the runtime
     * compiler, some or all checks (if any) may be elided. Hence, the
     * caller must not rely on the checks and corresponding
     * exceptions!
     *
     * @throws RuntimeException if the size is negative or too large
     *                          for the native size_t type
     * @throws OutOfMemoryError if the allocation is refused by the system
     * @see #getByte(long)
     * @see #putByte(long, byte)
     * @deprecated Use {@link java.lang.foreign} to allocate off-heap memory.
     */
    public static long allocateMemory(long bytes) {
        return invokeMethod(unsafe, allocateMemoryMethod, bytes);
    }

    /**
     * Resizes a new block of native memory, to the given size in bytes.  The
     * contents of the new block past the size of the old block are
     * uninitialized; they will generally be garbage.  The resulting native
     * pointer will be zero if and only if the requested size is zero.  The
     * resulting native pointer will be aligned for all value types.  Dispose
     * of this memory by calling {@link #freeMemory}, or resize it with {@link
     * #reallocateMemory}.  The address passed to this method may be null, in
     * which case an allocation will be performed.
     *
     * <em>Note:</em> It is the responsibility of the caller to make
     * sure arguments are checked before the methods are called. While
     * some rudimentary checks are performed on the input, the checks
     * are best effort and when performance is an overriding priority,
     * as when methods of this class are optimized by the runtime
     * compiler, some or all checks (if any) may be elided. Hence, the
     * caller must not rely on the checks and corresponding
     * exceptions!
     *
     * @throws RuntimeException if the size is negative or too large
     *                          for the native size_t type
     * @throws OutOfMemoryError if the allocation is refused by the system
     * @see #allocateMemory
     * @deprecated Use {@link java.lang.foreign} to allocate off-heap memory.
     */
    public static long reallocateMemory(long address, long bytes) {
        return invokeMethod(unsafe, reallocateMemoryMethod, address, bytes);
    }

    /**
     * Sets all bytes in a given block of memory to a fixed value
     * (usually zero).
     *
     * <p>This method determines a block's base address by means of two parameters,
     * and so it provides (in effect) a <em>double-register</em> addressing mode,
     * as discussed in {@link #getInt(Object, long)}.  When the object reference is null,
     * the offset supplies an absolute base address.
     *
     * <p>The stores are in coherent (atomic) units of a size determined
     * by the address and length parameters.  If the effective address and
     * length are all even modulo 8, the stores take place in 'long' units.
     * If the effective address and length are (resp.) even modulo 4 or 2,
     * the stores take place in units of 'int' or 'short'.
     *
     * <em>Note:</em> It is the responsibility of the caller to make
     * sure arguments are checked before the methods are called. While
     * some rudimentary checks are performed on the input, the checks
     * are best effort and when performance is an overriding priority,
     * as when methods of this class are optimized by the runtime
     * compiler, some or all checks (if any) may be elided. Hence, the
     * caller must not rely on the checks and corresponding
     * exceptions!
     *
     * @throws RuntimeException if any of the arguments is invalid
     * @since JDK 1.7
     * @deprecated {@link java.lang.foreign.java.lang.foreign.MemorySegment#fill(byte)} fills the contents of a memory
     * segment with a given value.
     */
    public static void setMemory(Object o, long offset, long bytes, byte value) {
        invokeMethod(unsafe, setMemoryMethod, o, offset, bytes, value);
    }

    /**
     * Sets all bytes in a given block of memory to a fixed value
     * (usually zero).  This provides a <em>single-register</em> addressing mode,
     * as discussed in {@link #getInt(Object, long)}.
     *
     * <p>Equivalent to {@code setMemory(null, address, bytes, value)}.
     *
     * @deprecated {@link java.lang.foreign.java.lang.foreign.MemorySegment#fill(byte)} fills the contents of a memory
     * segment with a given value.
     * <p>
     * Use {@link java.lang.foreign.java.lang.foreign.MemorySegment} and its bulk copy methods instead.
     */
    public static void setMemory(long address, long bytes, byte value) {
        invokeMethod(unsafe, setMemoryToAddressMethod, address, bytes, value);
    }

    /**
     * Sets all bytes in a given block of memory to a copy of another
     * block.
     *
     * <p>This method determines each block's base address by means of two parameters,
     * and so it provides (in effect) a <em>double-register</em> addressing mode,
     * as discussed in {@link #getInt(Object, long)}.  When the object reference is null,
     * the offset supplies an absolute base address.
     *
     * <p>The transfers are in coherent (atomic) units of a size determined
     * by the address and length parameters.  If the effective addresses and
     * length are all even modulo 8, the transfer takes place in 'long' units.
     * If the effective addresses and length are (resp.) even modulo 4 or 2,
     * the transfer takes place in units of 'int' or 'short'.
     *
     * <em>Note:</em> It is the responsibility of the caller to make
     * sure arguments are checked before the methods are called. While
     * some rudimentary checks are performed on the input, the checks
     * are best effort and when performance is an overriding priority,
     * as when methods of this class are optimized by the runtime
     * compiler, some or all checks (if any) may be elided. Hence, the
     * caller must not rely on the checks and corresponding
     * exceptions!
     *
     * @throws RuntimeException if any of the arguments is invalid
     * @since JDK 1.7
     * @deprecated Use {@link java.lang.foreign.java.lang.foreign.MemorySegment} and its bulk copy methods instead.
     */
    public static void copyMemory(Object srcBase, long srcOffset, Object destBase, long destOffset, long bytes) {
        invokeMethod(unsafe, copyMemoryMethod, srcBase, srcOffset, destBase, destOffset, bytes);
    }

    /**
     * Sets all bytes in a given block of memory to a copy of another
     * block.  This provides a <em>single-register</em> addressing mode,
     * as discussed in {@link #getInt(Object, long)}.
     * <p>
     * Equivalent to {@code copyMemory(null, srcAddress, null, destAddress, bytes)}.
     *
     * @deprecated Use {@link java.lang.foreign.java.lang.foreign.MemorySegment} and its bulk copy methods instead.
     */
    public static void copyMemory(long srcAddress, long destAddress, long bytes) {
        invokeMethod(unsafe, copyMemoryFromAddressMethod, srcAddress, destAddress, bytes);
    }

    /**
     * Disposes of a block of native memory, as obtained from {@link
     * #allocateMemory} or {@link #reallocateMemory}.  The address passed to
     * this method may be null, in which case no action is taken.
     *
     * <em>Note:</em> It is the responsibility of the caller to make
     * sure arguments are checked before the methods are called. While
     * some rudimentary checks are performed on the input, the checks
     * are best effort and when performance is an overriding priority,
     * as when methods of this class are optimized by the runtime
     * compiler, some or all checks (if any) may be elided. Hence, the
     * caller must not rely on the checks and corresponding
     * exceptions!
     *
     * @throws RuntimeException if any of the arguments is invalid
     * @see #allocateMemory
     * @deprecated Use {@link java.lang.foreign} to allocate and free off-heap memory.
     */
    public static void freeMemory(long address) {
        invokeMethod(unsafe, freeMemoryMethod, address);
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
    public static long objectFieldOffset(Object object, String fieldName) throws IllegalArgumentException, NullPointerException {
        Class<?> type = object.getClass();
        Long offsetFromCache = getOffsetFromCache(type, fieldName);
        if (offsetFromCache != null) {
            return offsetFromCache;
        }
        Field field = findField(type, fieldName);
        long offset = invokeMethod(unsafe, objectFieldOffsetMethod, field);
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
    public static long staticFieldOffset(Class<?> type, String fieldName) {
        Field field = findField(type, fieldName);
        return invokeMethod(unsafe, staticFieldOffsetMethod, field);
    }

    /**
     * Get the base of a class static field
     *
     * @param type      the target type
     * @param fieldName the name of {@link Field}
     * @return base
     */
    public static long staticFieldBase(Class<?> type, String fieldName) {
        Field field = findField(type, fieldName);
        return invokeMethod(unsafe, staticFieldBaseMethod, field);
    }

    /**
     * Reports the offset of the first element in the storage allocation of a
     * given array class.  If {@link #arrayIndexScale} returns a non-zero value
     * for the same class, you may use that scale factor, together with this
     * base offset, to form new offsets to access elements of arrays of the
     * given class.
     *
     * @see #getInt(Object, long)
     * @see #putInt(Object, long, int)
     * @deprecated Not needed when using {@link java.lang.invoke.VarHandle} or {@link java.lang.foreign}.
     */
    public static int arrayBaseOffset(Class<?> arrayClass) {
        return invokeMethod(unsafe, arrayBaseOffsetMethod, arrayClass);
    }

    /**
     * Reports the scale factor for addressing elements in the storage
     * allocation of a given array class.  However, arrays of "narrow" types
     * will generally not work properly with accessors like {@link
     * #getByte(Object, long)}, so the scale factor for such classes is reported
     * as zero.
     *
     * @see #arrayBaseOffset
     * @see #getInt(Object, long)
     * @see #putInt(Object, long, int)
     * @deprecated Not needed when using {@link java.lang.invoke.VarHandle} or {@link java.lang.foreign}.
     */
    public static int arrayIndexScale(Class<?> arrayClass) {
        return invokeMethod(unsafe, arrayIndexScaleMethod, arrayClass);
    }

    /**
     * Reports the size in bytes of a native pointer, as stored via {@link
     * #putAddress}.  This value will be either 4 or 8.  Note that the sizes of
     * other primitive types (as stored in native memory blocks) is determined
     * fully by their information content.
     *
     * @deprecated Use {@link java.lang.foreign.java.lang.foreign.ValueLayout#ADDRESS}.{@link java.lang.foreign.MemoryLayout#byteSize()} instead.
     */
    public static int addressSize() {
        return invokeMethod(unsafe, addressSizeMethod);
    }

    /**
     * Reports the size in bytes of a native memory page (whatever that is).
     * This value will always be a power of two.
     */
    public static int pageSize() {
        return invokeMethod(unsafe, pageSizeMethod);
    }

    //| random trusted operations from JNI:

    /**
     * Allocates an instance but does not run any constructor.
     * Initializes the class if it has not yet been.
     */
    public static Object allocateInstance(Class<?> cls) {
        return invokeMethod(unsafe, allocateInstanceMethod, cls);
    }

    /**
     * Throws the exception without telling the verifier.
     */
    public static void throwException(Throwable ee) {
        invokeMethod(unsafe, throwExceptionMethod, ee);
    }

    /**
     * Atomically updates Java variable to {@code x} if it is currently
     * holding {@code expected}.
     *
     * <p>This operation has memory semantics of a {@code volatile} read
     * and write.  Corresponds to C11 atomic_compare_exchange_strong.
     *
     * @return {@code true} if successful
     * @deprecated Use {@link java.lang.invoke.VarHandle#compareAndExchange(Object...)} instead.
     */
    public static boolean compareAndSwapObject(Object o, long offset, Object expected, Object x) {
        return invokeMethod(unsafe, compareAndSwapObjectMethod, o, offset, expected, x);
    }

    /**
     * Atomically updates Java variable to {@code x} if it is currently
     * holding {@code expected}.
     *
     * <p>This operation has memory semantics of a {@code volatile} read
     * and write.  Corresponds to C11 atomic_compare_exchange_strong.
     *
     * @return {@code true} if successful
     * @deprecated Use {@link java.lang.invoke.VarHandle#compareAndExchange(Object...)} instead.
     */
    public static boolean compareAndSwapInt(Object o, long offset, int expected, int x) {
        return invokeMethod(unsafe, compareAndSwapIntMethod, o, offset, expected, x);
    }

    /**
     * Atomically updates Java variable to {@code x} if it is currently
     * holding {@code expected}.
     *
     * <p>This operation has memory semantics of a {@code volatile} read
     * and write.  Corresponds to C11 atomic_compare_exchange_strong.
     *
     * @return {@code true} if successful
     * @deprecated Use {@link java.lang.invoke.VarHandle#compareAndExchange(Object...)} instead.
     */
    public static boolean compareAndSwapLong(Object o, long offset, long expected, long x) {
        return invokeMethod(unsafe, compareAndSwapLongMethod, o, offset, expected, x);
    }

    /**
     * Fetches a reference value from a given Java variable, with volatile
     * load semantics. Otherwise identical to {@link #getObject(Object, long)}
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#getVolatile(Object...)} instead.
     */
    public static Object getObjectVolatile(Object o, long offset) {
        return invokeMethod(unsafe, getObjectVolatileMethod, o, offset);
    }

    /**
     * Stores a reference value into a given Java variable, with
     * volatile store semantics. Otherwise identical to {@link #putObject(Object, long, Object)}
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#setVolatile(Object...)} instead.
     */
    public static void putObjectVolatile(Object o, long offset, Object x) {
        invokeMethod(unsafe, putObjectVolatileMethod, o, offset, x);
    }

    /**
     * Volatile version of {@link #getInt(Object, long)}.
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#getVolatile(Object...)} instead.
     */
    public static int getIntVolatile(Object o, long offset) {
        return invokeMethod(unsafe, getIntVolatileMethod, o, offset);
    }

    /**
     * Volatile version of {@link #putInt(Object, long, int)}.
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#setVolatile(Object...)} instead.
     */
    public static void putIntVolatile(Object o, long offset, int x) {
        invokeMethod(unsafe, putIntVolatileMethod, o, offset, x);
    }

    /**
     * Volatile version of {@link #getBoolean(Object, long)}.
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#getVolatile(Object...)} instead.
     */
    public static boolean getBooleanVolatile(Object o, long offset) {
        return invokeMethod(unsafe, getBooleanVolatileMethod, o, offset);
    }

    /**
     * Volatile version of {@link #putBoolean(Object, long, boolean)}.
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#setVolatile(Object...)} instead.
     */
    public static void putBooleanVolatile(Object o, long offset, boolean x) {
        invokeMethod(unsafe, putBooleanVolatileMethod, o, offset, x);
    }

    /**
     * Volatile version of {@link #getByte(Object, long)}.
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#getVolatile(Object...)}
     * instead.
     */
    public static byte getByteVolatile(Object o, long offset) {
        return invokeMethod(unsafe, getByteVolatileMethod, o, offset);
    }

    /**
     * Volatile version of {@link #putByte(Object, long, byte)}.
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#setVolatile(Object...)} instead.
     */
    public static void putByteVolatile(Object o, long offset, byte x) {
        invokeMethod(unsafe, putByteVolatileMethod, o, offset, x);
    }

    /**
     * Volatile version of {@link #getShort(Object, long)}.
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#getVolatile(Object...)} instead.
     */
    public static short getShortVolatile(Object o, long offset) {
        return invokeMethod(unsafe, getShortVolatileMethod, o, offset);
    }

    /**
     * Volatile version of {@link #putShort(Object, long, short)}.
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#setVolatile(Object...)} instead.
     */
    public static void putShortVolatile(Object o, long offset, short x) {
        invokeMethod(unsafe, putShortVolatileMethod, o, offset, x);
    }

    /**
     * Volatile version of {@link #getChar(Object, long)}.
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#getVolatile(Object...)} instead.
     */
    public static char getCharVolatile(Object o, long offset) {
        return invokeMethod(unsafe, getCharVolatileMethod, o, offset);
    }

    /**
     * Volatile version of {@link #putChar(Object, long, char)}.
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#setVolatile(Object...)} instead.
     */
    public static void putCharVolatile(Object o, long offset, char x) {
        invokeMethod(unsafe, putCharVolatileMethod, o, offset, x);
    }

    /**
     * Volatile version of {@link #getLong(Object, long)}.
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#getVolatile(Object...)} instead.
     */
    public static long getLongVolatile(Object o, long offset) {
        return invokeMethod(unsafe, getLongVolatileMethod, o, offset);
    }

    /**
     * Volatile version of {@link #putLong(Object, long, long)}.
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#setVolatile(Object...)} instead.
     */
    public static void putLongVolatile(Object o, long offset, long x) {
        invokeMethod(unsafe, putLongVolatileMethod, o, offset, x);
    }

    /**
     * Volatile version of {@link #getFloat(Object, long)}.
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#getVolatile(Object...)} instead.
     */
    public static float getFloatVolatile(Object o, long offset) {
        return invokeMethod(unsafe, getFloatVolatileMethod, o, offset);
    }

    /**
     * Volatile version of {@link #putFloat(Object, long, float)}.
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#setVolatile(Object...)} instead.
     */
    public static void putFloatVolatile(Object o, long offset, float x) {
        invokeMethod(unsafe, putFloatVolatileMethod, o, offset, x);
    }

    /**
     * Volatile version of {@link #getDouble(Object, long)}.
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#getVolatile(Object...)} instead.
     */
    public static double getDoubleVolatile(Object o, long offset) {
        return invokeMethod(unsafe, getDoubleVolatileMethod, o, offset);
    }

    /**
     * Volatile version of {@link #putDouble(Object, long, double)}.
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#setVolatile(Object...)} instead.
     */
    public static void putDoubleVolatile(Object o, long offset, double x) {
        invokeMethod(unsafe, putDoubleVolatileMethod, o, offset, x);
    }

    /**
     * Version of {@link #putObjectVolatile(Object, long, Object)}
     * that does not guarantee immediate visibility of the store to
     * other threads. This method is generally only useful if the
     * underlying field is a Java volatile (or if an array cell, one
     * that is otherwise only accessed using volatile accesses).
     * <p>
     * Corresponds to C11 atomic_store_explicit(..., memory_order_release).
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#setRelease(Object...)} instead.
     */
    public static void putOrderedObject(Object o, long offset, Object x) {
        invokeMethod(unsafe, putOrderedObjectMethod, o, offset, x);
    }

    /**
     * Ordered/Lazy version of {@link #putIntVolatile(Object, long, int)}.
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#setRelease(Object...)} instead.
     */
    public static void putOrderedInt(Object o, long offset, int x) {
        invokeMethod(unsafe, putOrderedIntMethod, o, offset, x);
    }

    /**
     * Ordered/Lazy version of {@link #putLongVolatile(Object, long, long)}.
     *
     * @deprecated Use {@link java.lang.invoke.VarHandle#setRelease(Object...)} instead.
     */
    public static void putOrderedLong(Object o, long offset, long x) {
        invokeMethod(unsafe, putOrderedLongMethod, o, offset, x);
    }

    // The following contain CAS-based Java implementations used on
    // platforms not supporting native instructions

    /**
     * Atomically adds the given value to the current value of a field
     * or array element within the given object {@code o}
     * at the given {@code offset}.
     *
     * @param o      object/array to update the field/element in
     * @param offset field/element offset
     * @param delta  the value to add
     * @return the previous value
     * @since JDK 1.8
     * @deprecated Use {@link java.lang.invoke.VarHandle#getAndAdd(Object...)} instead.
     */
    public static int getAndAddInt(Object o, long offset, int delta) {
        return invokeMethod(unsafe, getAndAddIntMethod, o, offset, delta);
    }

    /**
     * Atomically adds the given value to the current value of a field
     * or array element within the given object {@code o}
     * at the given {@code offset}.
     *
     * @param o      object/array to update the field/element in
     * @param offset field/element offset
     * @param delta  the value to add
     * @return the previous value
     * @since JDK 1.8
     * @deprecated Use {@link java.lang.invoke.VarHandle#getAndAdd(Object...)} instead.
     */
    public static long getAndAddLong(Object o, long offset, long delta) {
        return invokeMethod(unsafe, getAndAddLongMethod, o, offset, delta);
    }

    /**
     * Atomically exchanges the given value with the current value of
     * a field or array element within the given object {@code o}
     * at the given {@code offset}.
     *
     * @param o        object/array to update the field/element in
     * @param offset   field/element offset
     * @param newValue new value
     * @return the previous value
     * @since JDK 1.8
     * @deprecated Use {@link java.lang.invoke.VarHandle#getAndAdd(Object...)} instead.
     */
    public static int getAndSetInt(Object o, long offset, int newValue) {
        return invokeMethod(unsafe, getAndSetIntMethod, o, offset, newValue);
    }

    /**
     * Atomically exchanges the given value with the current value of
     * a field or array element within the given object {@code o}
     * at the given {@code offset}.
     *
     * @param o        object/array to update the field/element in
     * @param offset   field/element offset
     * @param newValue new value
     * @return the previous value
     * @since JDK 1.8
     * @deprecated Use {@link java.lang.invoke.VarHandle#getAndAdd(Object...)} instead.
     */
    public static long getAndSetLong(Object o, long offset, long newValue) {
        return invokeMethod(unsafe, getAndSetLongMethod, o, offset, newValue);
    }

    /**
     * Atomically exchanges the given reference value with the current
     * reference value of a field or array element within the given
     * object {@code o} at the given {@code offset}.
     *
     * @param o        object/array to update the field/element in
     * @param offset   field/element offset
     * @param newValue new value
     * @return the previous value
     * @since JDK 1.8
     * @deprecated Use {@link java.lang.invoke.VarHandle#getAndAdd(Object...)} instead.
     */
    public static Object getAndSetObject(Object o, long offset, Object newValue) {
        return invokeMethod(unsafe, getAndSetObjectMethod, o, offset, newValue);
    }

    /**
     * Ensures that loads before the fence will not be reordered with loads and
     * stores after the fence; a "LoadLoad plus LoadStore barrier".
     * <p>
     * Corresponds to C11 atomic_thread_fence(memory_order_acquire)
     * (an "acquire fence").
     * <p>
     * A pure LoadLoad fence is not provided, since the addition of LoadStore
     * is almost always desired, and most current hardware instructions that
     * provide a LoadLoad barrier also provide a LoadStore barrier for free.
     *
     * @since JDK 1.8
     * @deprecated Use {@link java.lang.invoke.VarHandle#acquireFence()} instead.
     */
    public static void loadFence() {
        invokeMethod(unsafe, loadFenceMethod);
    }

    /**
     * Ensures that loads and stores before the fence will not be reordered with
     * stores after the fence; a "StoreStore plus LoadStore barrier".
     * <p>
     * Corresponds to C11 atomic_thread_fence(memory_order_release)
     * (a "release fence").
     * <p>
     * A pure StoreStore fence is not provided, since the addition of LoadStore
     * is almost always desired, and most current hardware instructions that
     * provide a StoreStore barrier also provide a LoadStore barrier for free.
     *
     * @since JDK 1.8
     * @deprecated Use {@link java.lang.invoke.VarHandle#releaseFence()} instead.
     */
    public static void storeFence() {
        invokeMethod(unsafe, storeFenceMethod);
    }

    /**
     * Ensures that loads and stores before the fence will not be reordered
     * with loads and stores after the fence.  Implies the effects of both
     * loadFence() and storeFence(), and in addition, the effect of a StoreLoad
     * barrier.
     * <p>
     * Corresponds to C11 atomic_thread_fence(memory_order_seq_cst).
     *
     * @since JDK 1.8
     * @deprecated Use {@link java.lang.invoke.VarHandle#fullFence()} instead.
     */
    public static void fullFence() {
        invokeMethod(unsafe, fullFenceMethod);
    }

    private UnsafeUtils() {
    }
}