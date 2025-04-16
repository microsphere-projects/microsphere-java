/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.util;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.util.ClassUtils.isArray;
import static java.lang.System.arraycopy;
import static java.lang.reflect.Array.newInstance;
import static java.util.Arrays.binarySearch;
import static java.util.Collections.list;

/**
 * The utilities class for {@link Array}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class ArrayUtils implements Utils {

    /**
     * An empty immutable {@code boolean} array.
     */
    public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];

    /**
     * An empty immutable {@code char} array.
     */
    public static final char[] EMPTY_CHAR_ARRAY = new char[0];

    /**
     * An empty immutable {@code byte} array.
     */
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * An empty immutable {@code short} array.
     */
    public static final short[] EMPTY_SHORT_ARRAY = new short[0];

    /**
     * An empty immutable {@code int} array.
     */
    public static final int[] EMPTY_INT_ARRAY = new int[0];

    /**
     * An empty immutable {@code long} array.
     */
    public static final long[] EMPTY_LONG_ARRAY = new long[0];

    /**
     * An empty immutable {@code float} array.
     */
    public static final float[] EMPTY_FLOAT_ARRAY = new float[0];

    /**
     * An empty immutable {@code double} array.
     */
    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

    /**
     * An empty immutable {@code Object} array.
     */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * An empty immutable {@code Boolean} array.
     */
    public static final Boolean[] EMPTY_BOOLEAN_OBJECT_ARRAY = new Boolean[0];

    /**
     * An empty immutable {@code Byte} array.
     */
    public static final Byte[] EMPTY_BYTE_OBJECT_ARRAY = new Byte[0];

    /**
     * An empty immutable {@code Character} array.
     */
    public static final Character[] EMPTY_CHARACTER_OBJECT_ARRAY = new Character[0];

    /**
     * An empty immutable {@code Short} array.
     */
    public static final Short[] EMPTY_SHORT_OBJECT_ARRAY = new Short[0];

    /**
     * An empty immutable {@code Integer} array.
     */
    public static final Integer[] EMPTY_INTEGER_OBJECT_ARRAY = new Integer[0];

    /**
     * An empty immutable {@code Long} array.
     */
    public static final Long[] EMPTY_LONG_OBJECT_ARRAY = new Long[0];

    /**
     * An empty immutable {@code Float} array.
     */
    public static final Float[] EMPTY_FLOAT_OBJECT_ARRAY = new Float[0];

    /**
     * An empty immutable {@code Double} array.
     */
    public static final Double[] EMPTY_DOUBLE_OBJECT_ARRAY = new Double[0];

    /**
     * An empty immutable {@code Class} array.
     */
    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];

    /**
     * An empty immutable {@code String} array.
     */
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * An empty immutable {@code File} array.
     */
    public static final File[] EMPTY_FILE_ARRAY = new File[0];

    /**
     * An empty immutable {@code URL} array.
     */
    public static final URL[] EMPTY_URL_ARRAY = new URL[0];

    /**
     * An empty immutable {@code Parameter} array.
     */
    public static final Parameter[] EMPTY_PARAMETER_ARRAY = new Parameter[0];

    /**
     * An empty immutable {@code Type} array
     */
    public static final Type[] EMPTY_TYPE_ARRAY = new Type[0];

    /**
     * An empty immutable {@code Annotation} array
     */
    public static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

    public static <T> T[] of(T... values) {
        return ofArray(values);
    }

    public static boolean[] ofBooleans(boolean... values) {
        return values;
    }

    public static byte[] ofBytes(byte... values) {
        return values;
    }

    public static char[] ofChars(char... values) {
        return values;
    }

    public static short[] ofShorts(short... values) {
        return values;
    }

    public static int[] ofInts(int... values) {
        return values;
    }

    public static long[] ofLongs(long... values) {
        return values;
    }

    public static float[] ofFloats(float... values) {
        return values;
    }

    public static double[] ofDoubles(double... values) {
        return values;
    }

    public static <T> T[] ofArray(T... values) {
        return values;
    }

    public static int length(boolean[] values) {
        return values == null ? 0 : values.length;
    }

    public static int length(byte[] values) {
        return values == null ? 0 : values.length;
    }

    public static int length(char[] values) {
        return values == null ? 0 : values.length;
    }

    public static int length(short[] values) {
        return values == null ? 0 : values.length;
    }

    public static int length(int[] values) {
        return values == null ? 0 : values.length;
    }

    public static int length(long[] values) {
        return values == null ? 0 : values.length;
    }

    public static int length(float[] values) {
        return values == null ? 0 : values.length;
    }

    public static int length(double[] values) {
        return values == null ? 0 : values.length;
    }

    public static <T> int length(T[] values) {
        return values == null ? 0 : values.length;
    }

    public static <T> int size(T[] values) {
        return length(values);
    }

    public static boolean isEmpty(boolean[] values) {
        return length(values) == 0;
    }

    public static boolean isEmpty(byte[] values) {
        return length(values) == 0;
    }

    public static boolean isEmpty(char[] values) {
        return length(values) == 0;
    }

    public static boolean isEmpty(short[] values) {
        return length(values) == 0;
    }

    public static boolean isEmpty(int[] values) {
        return length(values) == 0;
    }

    public static boolean isEmpty(long[] values) {
        return length(values) == 0;
    }

    public static boolean isEmpty(float[] values) {
        return length(values) == 0;
    }

    public static boolean isEmpty(double[] values) {
        return length(values) == 0;
    }

    public static <T> boolean isEmpty(T[] values) {
        return length(values) == 0;
    }

    public static boolean isNotEmpty(boolean[] values) {
        return !isEmpty(values);
    }

    public static boolean isNotEmpty(byte[] values) {
        return !isEmpty(values);
    }

    public static boolean isNotEmpty(char[] values) {
        return !isEmpty(values);
    }

    public static boolean isNotEmpty(short[] values) {
        return !isEmpty(values);
    }

    public static boolean isNotEmpty(int[] values) {
        return !isEmpty(values);
    }

    public static boolean isNotEmpty(long[] values) {
        return !isEmpty(values);
    }

    public static boolean isNotEmpty(float[] values) {
        return !isEmpty(values);
    }

    public static boolean isNotEmpty(double[] values) {
        return !isEmpty(values);
    }

    public static <T> boolean isNotEmpty(T[] values) {
        return !isEmpty(values);
    }

    public static boolean arrayEquals(boolean[] a, boolean[] b) {
        return Arrays.equals(a, b);
    }

    public static boolean arrayEquals(byte[] a, byte[] b) {
        return Arrays.equals(a, b);
    }

    public static boolean arrayEquals(char[] a, char[] b) {
        return Arrays.equals(a, b);
    }

    public static boolean arrayEquals(short[] a, short[] b) {
        return Arrays.equals(a, b);
    }

    public static boolean arrayEquals(int[] a, int[] b) {
        return Arrays.equals(a, b);
    }

    public static boolean arrayEquals(long[] a, long[] b) {
        return Arrays.equals(a, b);
    }

    public static boolean arrayEquals(float[] a, float[] b) {
        return Arrays.equals(a, b);
    }

    public static boolean arrayEquals(double[] a, double[] b) {
        return Arrays.equals(a, b);
    }

    public static <T> boolean arrayEquals(T[] a, T[] b) {
        return Arrays.equals(a, b);
    }

    public static <E> E[] asArray(Enumeration<E> enumeration, Class<?> componentType) {
        return asArray(list(enumeration), componentType);
    }

    public static <E> E[] asArray(Iterable<E> elements, Class<?> componentType) {
        return asArray(newArrayList(elements), componentType);
    }

    public static <E> E[] asArray(Collection<E> collection, Class<?> componentType) {
        return collection.toArray(newArray(componentType, 0));
    }

    public static <E> E[] newArray(Class<?> componentType, int length) {
        return (E[]) newInstance(componentType, length);
    }

    public static <E> E[] combine(E one, E... others) {
        int othersLength = length(others);
        Class<?> oneType = one.getClass();
        boolean oneIsArray = isArray(oneType);

        if (oneIsArray) {
            return combineArray((E[]) oneType.cast(one), others);
        } else {
            Class<?> componentType = oneType;
            int length = 1 + othersLength;
            E[] values = newArray(componentType, length);
            values[0] = one;
            arraycopy(others, 0, values, 1, othersLength);
            return values;
        }
    }

    public static <E> E[] combine(E[] one, E[]... others) {
        return combineArray(one, others);
    }

    public static <E> E[] combineArray(E[] one, E[]... others) {
        int othersSize = length(others);
        if (othersSize < 1) {
            return one;
        }
        int oneSize = length(one);
        int size = oneSize;

        for (int i = 0; i < othersSize; i++) {
            E[] other = others[i];
            int otherLength = length(other);
            size += otherLength;
        }

        Class<?> componentType = one.getClass().getComponentType();
        E[] newArray = newArray(componentType, size);

        int pos = 0;
        arraycopy(one, 0, newArray, pos, oneSize);
        pos += oneSize;

        for (int i = 0; i < othersSize; i++) {
            E[] other = others[i];
            int otherLength = length(other);
            if (otherLength > 0) {
                arraycopy(other, 0, newArray, pos, otherLength);
                pos += otherLength;
            }
        }
        return newArray;
    }

    public static void forEach(boolean[] values, Consumer<Boolean> consumer) {
        forEach(values, (i, e) -> consumer.accept(e));
    }

    public static void forEach(byte[] values, Consumer<Byte> consumer) {
        forEach(values, (i, e) -> consumer.accept(e));
    }

    public static void forEach(char[] values, Consumer<Character> consumer) {
        forEach(values, (i, e) -> consumer.accept(e));
    }

    public static void forEach(short[] values, Consumer<Short> consumer) {
        forEach(values, (i, e) -> consumer.accept(e));
    }

    public static void forEach(int[] values, Consumer<Integer> consumer) {
        forEach(values, (i, e) -> consumer.accept(e));
    }

    public static void forEach(long[] values, Consumer<Long> consumer) {
        forEach(values, (i, e) -> consumer.accept(e));
    }

    public static void forEach(float[] values, Consumer<Float> consumer) {
        forEach(values, (i, e) -> consumer.accept(e));
    }

    public static void forEach(double[] values, Consumer<Double> consumer) {
        forEach(values, (i, e) -> consumer.accept(e));
    }

    public static <T> void forEach(T[] values, Consumer<T> consumer) {
        forEach(values, (i, e) -> consumer.accept(e));
    }

    public static void forEach(boolean[] values, BiConsumer<Integer, Boolean> indexedElementConsumer) {
        int length = length(values);
        for (int i = 0; i < length; i++) {
            Boolean value = values[i];
            indexedElementConsumer.accept(i, value);
        }
    }

    public static void forEach(byte[] values, BiConsumer<Integer, Byte> indexedElementConsumer) {
        int length = length(values);
        for (int i = 0; i < length; i++) {
            Byte value = values[i];
            indexedElementConsumer.accept(i, value);
        }
    }

    public static void forEach(char[] values, BiConsumer<Integer, Character> indexedElementConsumer) {
        int length = length(values);
        for (int i = 0; i < length; i++) {
            Character value = values[i];
            indexedElementConsumer.accept(i, value);
        }
    }

    public static void forEach(short[] values, BiConsumer<Integer, Short> indexedElementConsumer) {
        int length = length(values);
        for (int i = 0; i < length; i++) {
            Short value = values[i];
            indexedElementConsumer.accept(i, value);
        }
    }

    public static void forEach(int[] values, BiConsumer<Integer, Integer> indexedElementConsumer) {
        int length = length(values);
        for (int i = 0; i < length; i++) {
            Integer value = values[i];
            indexedElementConsumer.accept(i, value);
        }
    }

    public static void forEach(long[] values, BiConsumer<Integer, Long> indexedElementConsumer) {
        int length = length(values);
        for (int i = 0; i < length; i++) {
            Long value = values[i];
            indexedElementConsumer.accept(i, value);
        }
    }

    public static void forEach(float[] values, BiConsumer<Integer, Float> indexedElementConsumer) {
        int length = length(values);
        for (int i = 0; i < length; i++) {
            Float value = values[i];
            indexedElementConsumer.accept(i, value);
        }
    }

    public static void forEach(double[] values, BiConsumer<Integer, Double> indexedElementConsumer) {
        int length = length(values);
        for (int i = 0; i < length; i++) {
            Double value = values[i];
            indexedElementConsumer.accept(i, value);
        }
    }

    public static <T> void forEach(T[] values, BiConsumer<Integer, T> indexedElementConsumer) {
        int length = length(values);
        for (int i = 0; i < length; i++) {
            T value = values[i];
            indexedElementConsumer.accept(i, value);
        }
    }

    public static boolean contains(boolean[] values, boolean value) {
        int length = length(values);
        for (int i = 0; i < length; i++) {
            if (values[i] == value) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(byte[] values, byte value) {
        return binarySearch(values, value) > -1;
    }

    public static boolean contains(char[] values, char value) {
        return binarySearch(values, value) > -1;
    }

    public static boolean contains(short[] values, short value) {
        return binarySearch(values, value) > -1;
    }

    public static boolean contains(int[] values, int value) {
        return binarySearch(values, value) > -1;
    }

    public static boolean contains(long[] values, long value) {
        return binarySearch(values, value) > -1;
    }

    public static boolean contains(float[] values, float value) {
        return binarySearch(values, value) > -1;
    }

    public static boolean contains(double[] values, double value) {
        return binarySearch(values, value) > -1;
    }

    public static boolean contains(Object[] values, Object value) {
        int length = length(values);
        for (int i = 0; i < length; i++) {
            if (Objects.equals(values[i], value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convert the specified array to a string
     *
     * @param array the specified array
     * @param <T>   the element type of array
     * @return {@link Arrays#toString}
     */
    public static <T> String arrayToString(T[] array) {
        return Arrays.toString(array);
    }

    private ArrayUtils() {
    }
}
