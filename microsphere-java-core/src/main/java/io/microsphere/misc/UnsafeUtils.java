package io.microsphere.misc;

import io.microsphere.util.Assert;
import io.microsphere.util.BaseUtils;

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
public abstract class UnsafeUtils extends BaseUtils {

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
    static final Method getAddressFromAddressMethod = findMethod(UNSAFE_CLASS, "getAddress", long.class);

    /**
     * @see {@link sun.misc.Unsafe#putAddress(long, long)}
     */
    static final Method putAddressToAddressMethod = findMethod(UNSAFE_CLASS, "putAddress", long.class, long.class);

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
    public static long getLongFromArrayVolatile(Object object, String fieldName, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = getFieldValue(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = longArrayIndexOffset(index);
        return invokeMethod(unsafe, getLongVolatileMethod, array, offset);
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
        Object array = getFieldValue(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = intArrayIndexOffset(index);
        return invokeMethod(unsafe, getIntVolatileMethod, array, offset);
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
        Object array = getFieldValue(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = shortArrayIndexOffset(index);
        return invokeMethod(unsafe, getShortVolatileMethod, array, offset);
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
        Object array = getFieldValue(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = byteArrayIndexOffset(index);
        return invokeMethod(unsafe, getByteVolatileMethod, array, offset);
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
        Object array = getFieldValue(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = booleanArrayIndexOffset(index);
        return invokeMethod(unsafe, getBooleanVolatileMethod, array, offset);
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
        Object array = getFieldValue(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = doubleArrayIndexOffset(index);
        return invokeMethod(unsafe, getDoubleVolatileMethod, array, offset);
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
        Object array = getFieldValue(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = floatArrayIndexOffset(index);
        return invokeMethod(unsafe, getFloatVolatileMethod, array, offset);
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
    public static char getCharFromArrayVolatile(Object object, String fieldName, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = getFieldValue(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = charArrayIndexOffset(index);
        return invokeMethod(unsafe, getCharVolatileMethod, array, offset);
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
    public static Object getObjectFromArrayVolatile(Object object, String fieldName, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = getFieldValue(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = objectArrayIndexOffset(index);
        return invokeMethod(unsafe, getObjectVolatileMethod, array, offset);
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
        invokeMethod(unsafe, putDoubleMethod, object, offset, value);
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
        invokeMethod(unsafe, putFloatMethod, object, offset, value);
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
        invokeMethod(unsafe, putShortMethod, object, offset, value);
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
        invokeMethod(unsafe, putByteMethod, object, offset, value);
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
        invokeMethod(unsafe, putBooleanMethod, object, offset, value);

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
        invokeMethod(unsafe, putCharMethod, object, offset, value);
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
        invokeMethod(unsafe, putObjectMethod, object, offset, value);
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
        invokeMethod(unsafe, putLongMethod, object, offset, value);
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
        invokeMethod(unsafe, putIntMethod, object, offset, value);
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
        invokeMethod(unsafe, putOrderedIntMethod, object, offset, value);
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
        invokeMethod(unsafe, putOrderedLongMethod, object, offset, value);
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
        invokeMethod(unsafe, putOrderedObjectMethod, object, offset, value);
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
        invokeMethod(unsafe, putDoubleVolatileMethod, object, offset, value);
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
        invokeMethod(unsafe, putFloatVolatileMethod, object, offset, value);
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
        invokeMethod(unsafe, putShortVolatileMethod, object, offset, value);
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
        invokeMethod(unsafe, putByteVolatileMethod, object, offset, value);
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
        invokeMethod(unsafe, putBooleanVolatileMethod, object, offset, value);
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
        invokeMethod(unsafe, putCharVolatileMethod, object, offset, value);
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
        invokeMethod(unsafe, putObjectVolatileMethod, object, offset, value);
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
        invokeMethod(unsafe, putLongVolatileMethod, object, offset, value);
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
        invokeMethod(unsafe, putIntVolatileMethod, object, offset, value);
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
    public static void putLongIntoArrayVolatile(Object object, String fieldName, int index, long value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = getFieldValue(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = longArrayIndexOffset(index);
        invokeMethod(unsafe, putLongVolatileMethod, array, offset, value);
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
    public static void putOrderedLongIntoArray(Object object, String fieldName, int index, long value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = getFieldValue(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = longArrayIndexOffset(index);
        invokeMethod(unsafe, putOrderedLongMethod, array, offset, value);
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
    public static void putIntIntoArrayVolatile(Object object, String fieldName, int index, int value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = getFieldValue(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = intArrayIndexOffset(index);
        invokeMethod(unsafe, putIntVolatileMethod, array, offset, value);
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
    public static void putOrderedIntIntoArray(Object object, String fieldName, int index, int value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = getFieldValue(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = intArrayIndexOffset(index);
        invokeMethod(unsafe, putOrderedIntMethod, array, offset, value);
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
    public static void putShortIntoArrayVolatile(Object object, String fieldName, int index, short value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = getFieldValue(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = shortArrayIndexOffset(index);
        invokeMethod(unsafe, putShortVolatileMethod, array, offset, value);
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
    public static void putByteIntoArrayVolatile(Object object, String fieldName, int index, byte value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = getFieldValue(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = byteArrayIndexOffset(index);
        invokeMethod(unsafe, putByteVolatileMethod, array, offset, value);
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
    public static void putBooleanIntoArrayVolatile(Object object, String fieldName, int index, boolean value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = getFieldValue(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = booleanArrayIndexOffset(index);
        invokeMethod(unsafe, putBooleanVolatileMethod, array, offset, value);
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
    public static void putDoubleIntoArrayVolatile(Object object, String fieldName, int index, double value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalAccessException {
        Object array = getFieldValue(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = doubleArrayIndexOffset(index);
        invokeMethod(unsafe, putDoubleVolatileMethod, array, offset, value);
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
    public static void putFloatIntoArrayVolatile(Object object, String fieldName, int index, float value) throws IllegalAccessException {
        Object array = getFieldValue(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = floatArrayIndexOffset(index);
        invokeMethod(unsafe, putFloatVolatileMethod, array, offset, value);
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
    public static void putCharIntoArrayVolatile(Object object, String fieldName, int index, char value) throws IllegalAccessException {
        Object array = getFieldValue(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = charArrayIndexOffset(index);
        invokeMethod(unsafe, putCharVolatileMethod, array, offset, value);
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
    public static void putObjectIntoArrayVolatile(Object object, String fieldName, int index, Object value) throws IllegalAccessException {
        Object array = getFieldValue(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = objectArrayIndexOffset(index);
        invokeMethod(unsafe, putObjectVolatileMethod, array, offset, value);
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
    public static void putOrderedObjectIntoArray(Object object, String fieldName, int index, Object value) throws IllegalAccessException {
        Object array = getFieldValue(object, fieldName, true);
        assertArrayIndex(array, index);
        long offset = objectArrayIndexOffset(index);
        invokeMethod(unsafe, putOrderedObjectMethod, array, offset, value);
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
        return invokeMethod(unsafe, getObjectMethod, object, offset);
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
        return invokeMethod(unsafe, getLongMethod, object, offset);
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
        return invokeMethod(unsafe, getDoubleMethod, object, offset);
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
        return invokeMethod(unsafe, getFloatMethod, object, offset);
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
        return invokeMethod(unsafe, getShortMethod, object, offset);
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
        return invokeMethod(unsafe, getByteMethod, object, offset);
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
        return invokeMethod(unsafe, getBooleanMethod, object, offset);
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
        return invokeMethod(unsafe, getCharMethod, object, offset);
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
        return invokeMethod(unsafe, getIntMethod, object, offset);
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
        return invokeMethod(unsafe, getObjectVolatileMethod, object, offset);
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
        return invokeMethod(unsafe, getLongVolatileMethod, object, offset);
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
        return invokeMethod(unsafe, getDoubleVolatileMethod, object, offset);
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
        return invokeMethod(unsafe, getFloatVolatileMethod, object, offset);
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
        return invokeMethod(unsafe, getShortVolatileMethod, object, offset);
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
        return invokeMethod(unsafe, getByteVolatileMethod, object, offset);
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
        return invokeMethod(unsafe, getBooleanVolatileMethod, object, offset);
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
        return invokeMethod(unsafe, getCharVolatileMethod, object, offset);
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
        return invokeMethod(unsafe, getIntVolatileMethod, object, offset);
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
    public static long getObjectFieldOffset(Object object, String fieldName) throws IllegalArgumentException, NullPointerException {
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
    public static long getStaticFieldOffset(Class<?> type, String fieldName) {
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
    public static long getStaticFieldBase(Class<?> type, String fieldName) {
        Field field = findField(type, fieldName);
        return invokeMethod(unsafe, staticFieldBaseMethod, field);
    }
}