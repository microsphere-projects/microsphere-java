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

    /**
     * Creates an array from the provided elements.
     *
     * <p>This method returns an array containing the specified elements. The type of the returned array is inferred
     * from the type of the first element passed, or from the context if the method is used in a typed assignment.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] strings = ArrayUtils.of("one", "two", "three");
     * Integer[] integers = ArrayUtils.of(1, 2, 3);
     * }</pre>
     *
     * @param values the elements to be included in the resulting array
     * @param <T>    the class of the objects in the array
     * @return an array containing the specified elements
     */
    public static <T> T[] of(T... values) {
        return ofArray(values);
    }

    /**
     * Creates a new {@code boolean} array from the provided elements.
     *
     * <p>This method returns an array containing the specified elements. The type of the returned array is explicitly
     * defined by the method's return type.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean[] booleans = ArrayUtils.ofBooleans(true, false, true);
     * }</pre>
     *
     * @param values the elements to be included in the resulting array
     * @return a newly created array containing the specified elements
     */
    public static boolean[] ofBooleans(boolean... values) {
        return values;
    }

    /**
     * Creates a new {@code byte} array from the provided elements.
     *
     * <p>This method returns an array containing the specified elements. The type of the returned array is explicitly
     * defined by the method's return type.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * byte[] bytes = ArrayUtils.ofBytes((byte) 1, (byte) 2, (byte) 3);
     * }</pre>
     *
     * @param values the elements to be included in the resulting array
     * @return a newly created array containing the specified elements
     */
    public static byte[] ofBytes(byte... values) {
        return values;
    }

    /**
     * Creates a new {@code char} array from the provided elements.
     *
     * <p>This method returns an array containing the specified elements. The type of the returned array is explicitly
     * defined by the method's return type.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * char[] chars = ArrayUtils.ofChars('a', 'b', 'c');
     * }</pre>
     *
     * @param values the elements to be included in the resulting array
     * @return a newly created array containing the specified elements
     */
    public static char[] ofChars(char... values) {
        return values;
    }

    /**
     * Creates a new {@code short} array from the provided elements.
     *
     * <p>This method returns an array containing the specified elements. The type of the returned array is explicitly
     * defined by the method's return type.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * short[] shorts = ArrayUtils.ofShorts((short) 1, (short) 2, (short) 3);
     * }</pre>
     *
     * @param values the elements to be included in the resulting array
     * @return a newly created array containing the specified elements
     */
    public static short[] ofShorts(short... values) {
        return values;
    }

    /**
     * Creates a new {@code int} array from the provided elements.
     *
     * <p>This method returns an array containing the specified elements. The type of the returned array is explicitly
     * defined by the method's return type.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * int[] ints = ArrayUtils.ofInts(1, 2, 3);
     * }</pre>
     *
     * @param values the elements to be included in the resulting array
     * @return a newly created array containing the specified elements
     */
    public static int[] ofInts(int... values) {
        return values;
    }

    /**
     * Creates a new {@code long} array from the provided elements.
     *
     * <p>This method returns an array containing the specified elements. The type of the returned array is explicitly
     * defined by the method's return type.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * long[] longs = ArrayUtils.ofLongs(1L, 2L, 3L);
     * }</pre>
     *
     * @param values the elements to be included in the resulting array
     * @return a newly created array containing the specified elements
     */
    public static long[] ofLongs(long... values) {
        return values;
    }

    /**
     * Creates a new {@code float} array from the provided elements.
     *
     * <p>This method returns an array containing the specified elements. The type of the returned array is explicitly
     * defined by the method's return type.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * float[] floats = ArrayUtils.ofFloats(1.0f, 2.0f, 3.0f);
     * }</pre>
     *
     * @param values the elements to be included in the resulting array
     * @return a newly created array containing the specified elements
     */
    public static float[] ofFloats(float... values) {
        return values;
    }

    /**
     * Creates a new {@code double} array from the provided elements.
     *
     * <p>This method returns an array containing the specified elements. The type of the returned array is explicitly
     * defined by the method's return type.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * double[] doubles = ArrayUtils.ofDoubles(1.0, 2.0, 3.0);
     * }</pre>
     *
     * @param values the elements to be included in the resulting array
     * @return a newly created array containing the specified elements
     */
    public static double[] ofDoubles(double... values) {
        return values;
    }

    /**
     * Creates an array from the provided elements.
     *
     * <p>This method returns an array containing the specified elements. The type of the returned array is inferred
     * from the type of the first element passed, or from the context if the method is used in a typed assignment.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] strings = ArrayUtils.ofArray("one", "two", "three");
     * Integer[] integers = ArrayUtils.ofArray(1, 2, 3);
     * }</pre>
     *
     * @param values the elements to be included in the resulting array
     * @param <T>    the class of the objects in the array
     * @return an array containing the specified elements
     */
    public static <T> T[] ofArray(T... values) {
        return values;
    }

    /**
     * Returns the length of the provided array.
     *
     * <p>If the array is {@code null}, this method returns {@code 0}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * int length = ArrayUtils.length(new boolean[] { true, false }); // returns 2
     * int nullArrayLength = ArrayUtils.length((boolean[]) null); // returns 0
     * }</pre>
     *
     * @param values the array to determine the length of
     * @return the length of the array, or {@code 0} if the array is {@code null}
     */
    public static int length(boolean[] values) {
        return values == null ? 0 : values.length;
    }

    /**
     * Returns the length of the provided byte array.
     *
     * <p>If the array is {@code null}, this method returns {@code 0}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * int length = ArrayUtils.length(new byte[] { 1, 2 }); // returns 2
     * int nullArrayLength = ArrayUtils.length((byte[]) null); // returns 0
     * }</pre>
     *
     * @param values the array to determine the length of
     * @return the length of the array, or {@code 0} if the array is {@code null}
     */
    public static int length(byte[] values) {
        return values == null ? 0 : values.length;
    }

    /**
     * Returns the length of the provided char array.
     *
     * <p>If the array is {@code null}, this method returns {@code 0}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * int length = ArrayUtils.length(new char[] { 'a', 'b' }); // returns 2
     * int nullArrayLength = ArrayUtils.length((char[]) null); // returns 0
     * }</pre>
     *
     * @param values the array to determine the length of
     * @return the length of the array, or {@code 0} if the array is {@code null}
     */
    public static int length(char[] values) {
        return values == null ? 0 : values.length;
    }

    /**
     * Returns the length of the provided short array.
     *
     * <p>If the array is {@code null}, this method returns {@code 0}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * int length = ArrayUtils.length(new short[] { 1, 2 }); // returns 2
     * int nullArrayLength = ArrayUtils.length((short[]) null); // returns 0
     * }</pre>
     *
     * @param values the array to determine the length of
     * @return the length of the array, or {@code 0} if the array is {@code null}
     */
    public static int length(short[] values) {
        return values == null ? 0 : values.length;
    }

    /**
     * Returns the length of the provided int array.
     *
     * <p>If the array is {@code null}, this method returns {@code 0}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * int length = ArrayUtils.length(new int[] { 1, 2, 3 }); // returns 3
     * int nullArrayLength = ArrayUtils.length((int[]) null); // returns 0
     * }</pre>
     *
     * @param values the array to determine the length of
     * @return the length of the array, or {@code 0} if the array is {@code null}
     */
    public static int length(int[] values) {
        return values == null ? 0 : values.length;
    }

    /**
     * Returns the length of the provided long array.
     *
     * <p>If the array is {@code null}, this method returns {@code 0}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * int length = ArrayUtils.length(new long[] { 1L, 2L }); // returns 2
     * int nullArrayLength = ArrayUtils.length((long[]) null); // returns 0
     * }</pre>
     *
     * @param values the array to determine the length of
     * @return the length of the array, or {@code 0} if the array is {@code null}
     */
    public static int length(long[] values) {
        return values == null ? 0 : values.length;
    }

    /**
     * Returns the length of the provided float array.
     *
     * <p>If the array is {@code null}, this method returns {@code 0}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * int length = ArrayUtils.length(new float[] { 1.0f, 2.0f }); // returns 2
     * int nullArrayLength = ArrayUtils.length((float[]) null); // returns 0
     * }</pre>
     *
     * @param values the array to determine the length of
     * @return the length of the array, or {@code 0} if the array is {@code null}
     */
    public static int length(float[] values) {
        return values == null ? 0 : values.length;
    }

    /**
     * Returns the length of the provided double array.
     *
     * <p>If the array is {@code null}, this method returns {@code 0}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * int length = ArrayUtils.length(new double[] { 1.0, 2.0 }); // returns 2
     * int nullArrayLength = ArrayUtils.length((double[]) null); // returns 0
     * }</pre>
     *
     * @param values the array to determine the length of
     * @return the length of the array, or {@code 0} if the array is {@code null}
     */
    public static int length(double[] values) {
        return values == null ? 0 : values.length;
    }

    /**
     * Returns the length of the provided array.
     *
     * <p>If the array is {@code null}, this method returns {@code 0}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Integer[] integers = {1, 2, 3};
     * int length = ArrayUtils.length(integers); // returns 3
     *
     * String[] strings = null;
     * int nullArrayLength = ArrayUtils.length(strings); // returns 0
     * }</pre>
     *
     * @param values the array to determine the length of
     * @param <T>    the class of the objects in the array
     * @return the length of the array, or {@code 0} if the array is {@code null}
     */
    public static <T> int length(T[] values) {
        return values == null ? 0 : values.length;
    }

    /**
     * Returns the length of the provided array.
     *
     * <p>If the array is {@code null}, this method returns {@code 0}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Integer[] integers = {1, 2, 3};
     * int size = ArrayUtils.size(integers); // returns 3
     *
     * String[] strings = null;
     * int nullArraySize = ArrayUtils.size(strings); // returns 0
     * }</pre>
     *
     * @param values the array to determine the length of
     * @param <T>    the class of the objects in the array
     * @return the length of the array, or {@code 0} if the array is {@code null}
     */
    public static <T> int size(T[] values) {
        return length(values);
    }

    /**
     * Checks if the provided boolean array is empty.
     *
     * <p>This method returns {@code true} if the specified array has zero elements or is {@code null}.
     * It provides a convenient way to check for emptiness without explicitly handling null cases.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean[] emptyArray = {};
     * boolean result1 = ArrayUtils.isEmpty(emptyArray); // returns true
     *
     * boolean[] nonEmptyArray = {true, false};
     * boolean result2 = ArrayUtils.isEmpty(nonEmptyArray); // returns false
     *
     * boolean result3 = ArrayUtils.isEmpty((boolean[]) null); // returns true
     * }</pre>
     *
     * @param values the array to check for emptiness
     * @return {@code true} if the array is null or has no elements, otherwise {@code false}
     */
    public static boolean isEmpty(boolean[] values) {
        return length(values) == 0;
    }

    /**
     * Checks if the provided byte array is empty.
     *
     * <p>This method returns {@code true} if the specified array has zero elements or is {@code null}.
     * It provides a convenient way to check for emptiness without explicitly handling null cases.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * byte[] emptyArray = {};
     * boolean result1 = ArrayUtils.isEmpty(emptyArray); // returns true
     *
     * byte[] nonEmptyArray = {1, 2};
     * boolean result2 = ArrayUtils.isEmpty(nonEmptyArray); // returns false
     *
     * boolean result3 = ArrayUtils.isEmpty((byte[]) null); // returns true
     * }</pre>
     *
     * @param values the array to check for emptiness
     * @return {@code true} if the array is null or has no elements, otherwise {@code false}
     */
    public static boolean isEmpty(byte[] values) {
        return length(values) == 0;
    }

    /**
     * Checks if the provided char array is empty.
     *
     * <p>This method returns {@code true} if the specified array has zero elements or is {@code null}.
     * It provides a convenient way to check for emptiness without explicitly handling null cases.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * char[] emptyArray = {};
     * boolean result1 = ArrayUtils.isEmpty(emptyArray); // returns true
     *
     * char[] nonEmptyArray = {'a', 'b'};
     * boolean result2 = ArrayUtils.isEmpty(nonEmptyArray); // returns false
     *
     * boolean result3 = ArrayUtils.isEmpty((char[]) null); // returns true
     * }</pre>
     *
     * @param values the array to check for emptiness
     * @return {@code true} if the array is null or has no elements, otherwise {@code false}
     */
    public static boolean isEmpty(char[] values) {
        return length(values) == 0;
    }

    /**
     * Checks if the provided short array is empty.
     *
     * <p>This method returns {@code true} if the specified array has zero elements or is {@code null}.
     * It provides a convenient way to check for emptiness without explicitly handling null cases.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * short[] emptyArray = {};
     * boolean result1 = ArrayUtils.isEmpty(emptyArray); // returns true
     *
     * short[] nonEmptyArray = {1, 2};
     * boolean result2 = ArrayUtils.isEmpty(nonEmptyArray); // returns false
     *
     * boolean result3 = ArrayUtils.isEmpty((short[]) null); // returns true
     * }</pre>
     *
     * @param values the array to check for emptiness
     * @return {@code true} if the array is null or has no elements, otherwise {@code false}
     */
    public static boolean isEmpty(short[] values) {
        return length(values) == 0;
    }

    /**
     * Checks if the provided int array is empty.
     *
     * <p>This method returns {@code true} if the specified array has zero elements or is {@code null}.
     * It provides a convenient way to check for emptiness without explicitly handling null cases.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * int[] emptyArray = {};
     * boolean result1 = ArrayUtils.isEmpty(emptyArray); // returns true
     *
     * int[] nonEmptyArray = {1, 2};
     * boolean result2 = ArrayUtils.isEmpty(nonEmptyArray); // returns false
     *
     * boolean result3 = ArrayUtils.isEmpty((int[]) null); // returns true
     * }</pre>
     *
     * @param values the array to check for emptiness
     * @return {@code true} if the array is null or has no elements, otherwise {@code false}
     */
    public static boolean isEmpty(int[] values) {
        return length(values) == 0;
    }

    /**
     * Checks if the provided long array is empty.
     *
     * <p>This method returns {@code true} if the specified array has zero elements or is {@code null}.
     * It provides a convenient way to check for emptiness without explicitly handling null cases.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * long[] emptyArray = {};
     * boolean result1 = ArrayUtils.isEmpty(emptyArray); // returns true
     *
     * long[] nonEmptyArray = {1L, 2L};
     * boolean result2 = ArrayUtils.isEmpty(nonEmptyArray); // returns false
     *
     * boolean result3 = ArrayUtils.isEmpty((long[]) null); // returns true
     * }</pre>
     *
     * @param values the array to check for emptiness
     * @return {@code true} if the array is null or has no elements, otherwise {@code false}
     */
    public static boolean isEmpty(long[] values) {
        return length(values) == 0;
    }

    /**
     * Checks if the provided float array is empty.
     *
     * <p>This method returns {@code true} if the specified array has zero elements or is {@code null}.
     * It provides a convenient way to check for emptiness without explicitly handling null cases.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * float[] emptyArray = {};
     * boolean result1 = ArrayUtils.isEmpty(emptyArray); // returns true
     *
     * float[] nonEmptyArray = {1.0f, 2.0f};
     * boolean result2 = ArrayUtils.isEmpty(nonEmptyArray); // returns false
     *
     * boolean result3 = ArrayUtils.isEmpty((float[]) null); // returns true
     * }</pre>
     *
     * @param values the array to check for emptiness
     * @return {@code true} if the array is null or has no elements, otherwise {@code false}
     */
    public static boolean isEmpty(float[] values) {
        return length(values) == 0;
    }

    /**
     * Checks if the provided double array is empty.
     *
     * <p>This method returns {@code true} if the specified array has zero elements or is {@code null}.
     * It provides a convenient way to check for emptiness without explicitly handling null cases.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * double[] emptyArray = {};
     * boolean result1 = ArrayUtils.isEmpty(emptyArray); // returns true
     *
     * double[] nonEmptyArray = {1.0, 2.0};
     * boolean result2 = ArrayUtils.isEmpty(nonEmptyArray); // returns false
     *
     * boolean result3 = ArrayUtils.isEmpty((double[]) null); // returns true
     * }</pre>
     *
     * @param values the array to check for emptiness
     * @return {@code true} if the array is null or has no elements, otherwise {@code false}
     */
    public static boolean isEmpty(double[] values) {
        return length(values) == 0;
    }

    /**
     * Checks if the provided array is empty.
     *
     * <p>This method returns {@code true} if the specified array has zero elements or is {@code null}.
     * It provides a convenient way to check for emptiness without explicitly handling null cases.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Integer[] integers = {};
     * boolean result1 = ArrayUtils.isEmpty(integers); // returns true
     *
     * String[] strings = {"one", "two"};
     * boolean result2 = ArrayUtils.isEmpty(strings); // returns false
     *
     * Object[] objects = null;
     * boolean result3 = ArrayUtils.isEmpty(objects); // returns true
     * }</pre>
     *
     * @param values the array to check for emptiness
     * @param <T>    the class of the objects in the array
     * @return {@code true} if the array is null or has no elements, otherwise {@code false}
     */
    public static <T> boolean isEmpty(T[] values) {
        return length(values) == 0;
    }

    /**
     * Checks if the provided boolean array is not empty.
     *
     * <p>This method returns {@code true} if the specified array has at least one element.
     * It provides a convenient way to check for non-emptiness without explicitly handling null cases.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean[] emptyArray = {};
     * boolean result1 = ArrayUtils.isNotEmpty(emptyArray); // returns false
     *
     * boolean[] nonEmptyArray = {true, false};
     * boolean result2 = ArrayUtils.isNotEmpty(nonEmptyArray); // returns true
     *
     * boolean result3 = ArrayUtils.isNotEmpty((boolean[]) null); // returns false
     * }</pre>
     *
     * @param values the array to check for non-emptiness
     * @return {@code true} if the array has at least one element, otherwise {@code false}
     */
    public static boolean isNotEmpty(boolean[] values) {
        return !isEmpty(values);
    }

    /**
     * Checks if the provided byte array is not empty.
     *
     * <p>This method returns {@code true} if the specified array has at least one element.
     * It provides a convenient way to check for non-emptiness without explicitly handling null cases.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * byte[] emptyArray = {};
     * boolean result1 = ArrayUtils.isNotEmpty(emptyArray); // returns false
     *
     * byte[] nonEmptyArray = {1, 2};
     * boolean result2 = ArrayUtils.isNotEmpty(nonEmptyArray); // returns true
     *
     * boolean result3 = ArrayUtils.isNotEmpty((byte[]) null); // returns false
     * }</pre>
     *
     * @param values the array to check for non-emptiness
     * @return {@code true} if the array has at least one element, otherwise {@code false}
     */
    public static boolean isNotEmpty(byte[] values) {
        return !isEmpty(values);
    }

    /**
     * Checks if the provided char array is not empty.
     *
     * <p>This method returns {@code true} if the specified array has at least one element.
     * It provides a convenient way to check for non-emptiness without explicitly handling null cases.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * char[] emptyArray = {};
     * boolean result1 = ArrayUtils.isNotEmpty(emptyArray); // returns false
     *
     * char[] nonEmptyArray = {'a', 'b'};
     * boolean result2 = ArrayUtils.isNotEmpty(nonEmptyArray); // returns true
     *
     * boolean result3 = ArrayUtils.isNotEmpty((char[]) null); // returns false
     * }</pre>
     *
     * @param values the array to check for non-emptiness
     * @return {@code true} if the array has at least one element, otherwise {@code false}
     */
    public static boolean isNotEmpty(char[] values) {
        return !isEmpty(values);
    }

    /**
     * Checks if the provided short array is not empty.
     *
     * <p>This method returns {@code true} if the specified array has at least one element.
     * It provides a convenient way to check for non-emptiness without explicitly handling null cases.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * short[] emptyArray = {};
     * boolean result1 = ArrayUtils.isNotEmpty(emptyArray); // returns false
     *
     * short[] nonEmptyArray = {1, 2};
     * boolean result2 = ArrayUtils.isNotEmpty(nonEmptyArray); // returns true
     *
     * boolean result3 = ArrayUtils.isNotEmpty((short[]) null); // returns false
     * }</pre>
     *
     * @param values the array to check for non-emptiness
     * @return {@code true} if the array has at least one element, otherwise {@code false}
     */
    public static boolean isNotEmpty(short[] values) {
        return !isEmpty(values);
    }

    /**
     * Checks if the provided int array is not empty.
     *
     * <p>This method returns {@code true} if the specified array has at least one element.
     * It provides a convenient way to check for non-emptiness without explicitly handling null cases.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * int[] emptyArray = {};
     * boolean result1 = ArrayUtils.isNotEmpty(emptyArray); // returns false
     *
     * int[] nonEmptyArray = {1, 2};
     * boolean result2 = ArrayUtils.isNotEmpty(nonEmptyArray); // returns true
     *
     * boolean result3 = ArrayUtils.isNotEmpty((int[]) null); // returns false
     * }</pre>
     *
     * @param values the array to check for non-emptiness
     * @return {@code true} if the array has at least one element, otherwise {@code false}
     */
    public static boolean isNotEmpty(int[] values) {
        return !isEmpty(values);
    }

    /**
     * Checks if the provided long array is not empty.
     *
     * <p>This method returns {@code true} if the specified array has at least one element.
     * It provides a convenient way to check for non-emptiness without explicitly handling null cases.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * long[] emptyArray = {};
     * boolean result1 = ArrayUtils.isNotEmpty(emptyArray); // returns false
     *
     * long[] nonEmptyArray = {1L, 2L};
     * boolean result2 = ArrayUtils.isNotEmpty(nonEmptyArray); // returns true
     *
     * boolean result3 = ArrayUtils.isNotEmpty((long[]) null); // returns false
     * }</pre>
     *
     * @param values the array to check for non-emptiness
     * @return {@code true} if the array has at least one element, otherwise {@code false}
     */
    public static boolean isNotEmpty(long[] values) {
        return !isEmpty(values);
    }

    /**
     * Checks if the provided float array is not empty.
     *
     * <p>This method returns {@code true} if the specified array has at least one element.
     * It provides a convenient way to check for non-emptiness without explicitly handling null cases.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * float[] emptyArray = {};
     * boolean result1 = ArrayUtils.isNotEmpty(emptyArray); // returns false
     *
     * float[] nonEmptyArray = {1.0f, 2.0f};
     * boolean result2 = ArrayUtils.isNotEmpty(nonEmptyArray); // returns true
     *
     * boolean result3 = ArrayUtils.isNotEmpty((float[]) null); // returns false
     * }</pre>
     *
     * @param values the array to check for non-emptiness
     * @return {@code true} if the array has at least one element, otherwise {@code false}
     */
    public static boolean isNotEmpty(float[] values) {
        return !isEmpty(values);
    }

    /**
     * Checks if the provided double array is not empty.
     *
     * <p>This method returns {@code true} if the specified array has at least one element.
     * It provides a convenient way to check for non-emptiness without explicitly handling null cases.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * double[] emptyArray = {};
     * boolean result1 = ArrayUtils.isNotEmpty(emptyArray); // returns false
     *
     * double[] nonEmptyArray = {1.0, 2.0};
     * boolean result2 = ArrayUtils.isNotEmpty(nonEmptyArray); // returns true
     *
     * boolean result3 = ArrayUtils.isNotEmpty((double[]) null); // returns false
     * }</pre>
     *
     * @param values the array to check for non-emptiness
     * @return {@code true} if the array has at least one element, otherwise {@code false}
     */
    public static boolean isNotEmpty(double[] values) {
        return !isEmpty(values);
    }

    /**
     * Checks if the provided array is not empty.
     *
     * <p>This method returns {@code true} if the specified array has at least one element.
     * It provides a convenient way to check for non-emptiness without explicitly handling null cases.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Integer[] nonEmptyArray = {1, 2, 3};
     * boolean result1 = ArrayUtils.isNotEmpty(nonEmptyArray); // returns true
     *
     * String[] emptyArray = {};
     * boolean result2 = ArrayUtils.isNotEmpty(emptyArray); // returns false
     *
     * Object[] nullArray = null;
     * boolean result3 = ArrayUtils.isNotEmpty(nullArray); // returns false
     * }</pre>
     *
     * @param values the array to check for non-emptiness
     * @param <T>    the class of the objects in the array
     * @return {@code true} if the array has at least one element, otherwise {@code false}
     */
    public static <T> boolean isNotEmpty(T[] values) {
        return !isEmpty(values);
    }

    /**
     * Compares two boolean arrays for equality.
     *
     * <p>This method checks if the two provided arrays are equal by comparing each element. The arrays are considered equal
     * if they have the same length and all corresponding elements are equal.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean[] array1 = {true, false, true};
     * boolean[] array2 = {true, false, true};
     * boolean result1 = ArrayUtils.arrayEquals(array1, array2); // returns true
     *
     * boolean[] array3 = {true, false, false};
     * boolean result2 = ArrayUtils.arrayEquals(array1, array3); // returns false
     *
     * boolean result3 = ArrayUtils.arrayEquals(null, null); // returns true
     * boolean result4 = ArrayUtils.arrayEquals(array1, null); // returns false
     * }</pre>
     *
     * @param a the first array to compare
     * @param b the second array to compare
     * @return {@code true} if both arrays are equal; otherwise, {@code false}
     */
    public static boolean arrayEquals(boolean[] a, boolean[] b) {
        return Arrays.equals(a, b);
    }

    /**
     * Compares two byte arrays for equality.
     *
     * <p>This method checks if the two provided arrays are equal by comparing each element. The arrays are considered equal
     * if they have the same length and all corresponding elements are equal.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * byte[] array1 = {1, 2, 3};
     * byte[] array2 = {1, 2, 3};
     * boolean result1 = ArrayUtils.arrayEquals(array1, array2); // returns true
     *
     * byte[] array3 = {1, 2, 4};
     * boolean result2 = ArrayUtils.arrayEquals(array1, array3); // returns false
     *
     * boolean result3 = ArrayUtils.arrayEquals(null, null); // returns true
     * boolean result4 = ArrayUtils.arrayEquals(array1, null); // returns false
     * }</pre>
     *
     * @param a the first array to compare
     * @param b the second array to compare
     * @return {@code true} if both arrays are equal; otherwise, {@code false}
     */
    public static boolean arrayEquals(byte[] a, byte[] b) {
        return Arrays.equals(a, b);
    }

    /**
     * Compares two char arrays for equality.
     *
     * <p>This method checks if the two provided arrays are equal by comparing each element. The arrays are considered equal
     * if they have the same length and all corresponding elements are equal.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * char[] array1 = {'a', 'b', 'c'};
     * char[] array2 = {'a', 'b', 'c'};
     * boolean result1 = ArrayUtils.arrayEquals(array1, array2); // returns true
     *
     * char[] array3 = {'a', 'b', 'd'};
     * boolean result2 = ArrayUtils.arrayEquals(array1, array3); // returns false
     *
     * boolean result3 = ArrayUtils.arrayEquals(null, null); // returns true
     * boolean result4 = ArrayUtils.arrayEquals(array1, null); // returns false
     * }</pre>
     *
     * @param a the first array to compare
     * @param b the second array to compare
     * @return {@code true} if both arrays are equal; otherwise, {@code false}
     */
    public static boolean arrayEquals(char[] a, char[] b) {
        return Arrays.equals(a, b);
    }

    /**
     * Compares two short arrays for equality.
     *
     * <p>This method checks if the two provided arrays are equal by comparing each element. The arrays are considered equal
     * if they have the same length and all corresponding elements are equal.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * short[] array1 = {1, 2, 3};
     * short[] array2 = {1, 2, 3};
     * boolean result1 = ArrayUtils.arrayEquals(array1, array2); // returns true
     *
     * short[] array3 = {1, 2, 4};
     * boolean result2 = ArrayUtils.arrayEquals(array1, array3); // returns false
     *
     * boolean result3 = ArrayUtils.arrayEquals(null, null); // returns true
     * boolean result4 = ArrayUtils.arrayEquals(array1, null); // returns false
     * }</pre>
     *
     * @param a the first array to compare
     * @param b the second array to compare
     * @return {@code true} if both arrays are equal; otherwise, {@code false}
     */
    public static boolean arrayEquals(short[] a, short[] b) {
        return Arrays.equals(a, b);
    }

    /**
     * Compares two int arrays for equality.
     *
     * <p>This method checks if the two provided arrays are equal by comparing each element. The arrays are considered equal
     * if they have the same length and all corresponding elements are equal.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * int[] array1 = {1, 2, 3};
     * int[] array2 = {1, 2, 3};
     * boolean result1 = ArrayUtils.arrayEquals(array1, array2); // returns true
     *
     * int[] array3 = {1, 2, 4};
     * boolean result2 = ArrayUtils.arrayEquals(array1, array3); // returns false
     *
     * boolean result3 = ArrayUtils.arrayEquals(null, null); // returns true
     * boolean result4 = ArrayUtils.arrayEquals(array1, null); // returns false
     * }</pre>
     *
     * @param a the first array to compare
     * @param b the second array to compare
     * @return {@code true} if both arrays are equal; otherwise, {@code false}
     */
    public static boolean arrayEquals(int[] a, int[] b) {
        return Arrays.equals(a, b);
    }

    /**
     * Compares two long arrays for equality.
     *
     * <p>This method checks if the two provided arrays are equal by comparing each element. The arrays are considered equal
     * if they have the same length and all corresponding elements are equal.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * long[] array1 = {1L, 2L, 3L};
     * long[] array2 = {1L, 2L, 3L};
     * boolean result1 = ArrayUtils.arrayEquals(array1, array2); // returns true
     *
     * long[] array3 = {1L, 2L, 4L};
     * boolean result2 = ArrayUtils.arrayEquals(array1, array3); // returns false
     *
     * boolean result3 = ArrayUtils.arrayEquals(null, null); // returns true
     * boolean result4 = ArrayUtils.arrayEquals(array1, null); // returns false
     * }</pre>
     *
     * @param a the first array to compare
     * @param b the second array to compare
     * @return {@code true} if both arrays are equal; otherwise, {@code false}
     */
    public static boolean arrayEquals(long[] a, long[] b) {
        return Arrays.equals(a, b);
    }

    /**
     * Compares two float arrays for equality.
     *
     * <p>This method checks if the two provided arrays are equal by comparing each element. The arrays are considered equal
     * if they have the same length and all corresponding elements are equal.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * float[] array1 = {1.0f, 2.0f, 3.0f};
     * float[] array2 = {1.0f, 2.0f, 3.0f};
     * boolean result1 = ArrayUtils.arrayEquals(array1, array2); // returns true
     *
     * float[] array3 = {1.0f, 2.0f, 4.0f};
     * boolean result2 = ArrayUtils.arrayEquals(array1, array3); // returns false
     *
     * boolean result3 = ArrayUtils.arrayEquals(null, null); // returns true
     * boolean result4 = ArrayUtils.arrayEquals(array1, null); // returns false
     * }</pre>
     *
     * @param a the first array to compare
     * @param b the second array to compare
     * @return {@code true} if both arrays are equal; otherwise, {@code false}
     */
    public static boolean arrayEquals(float[] a, float[] b) {
        return Arrays.equals(a, b);
    }

    /**
     * Compares two double arrays for equality.
     *
     * <p>This method checks if the two provided arrays are equal by comparing each element. The arrays are considered equal
     * if they have the same length and all corresponding elements are equal.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * double[] array1 = {1.0, 2.0, 3.0};
     * double[] array2 = {1.0, 2.0, 3.0};
     * boolean result1 = ArrayUtils.arrayEquals(array1, array2); // returns true
     *
     * double[] array3 = {1.0, 2.0, 4.0};
     * boolean result2 = ArrayUtils.arrayEquals(array1, array3); // returns false
     *
     * boolean result3 = ArrayUtils.arrayEquals(null, null); // returns true
     * boolean result4 = ArrayUtils.arrayEquals(array1, null); // returns false
     * }</pre>
     *
     * @param a the first array to compare
     * @param b the second array to compare
     * @return {@code true} if both arrays are equal; otherwise, {@code false}
     */
    public static boolean arrayEquals(double[] a, double[] b) {
        return Arrays.equals(a, b);
    }

    /**
     * Compares two object arrays for equality.
     *
     * <p>This method checks if the two provided arrays are equal by comparing each element. The arrays are considered equal
     * if they have the same length and all corresponding elements are equal according to their own {@code equals} methods.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] array1 = {"one", "two", "three"};
     * String[] array2 = {"one", "two", "three"};
     * boolean result1 = ArrayUtils.arrayEquals(array1, array2); // returns true
     *
     * String[] array3 = {"one", "two", "four"};
     * boolean result2 = ArrayUtils.arrayEquals(array1, array3); // returns false
     *
     * Integer[] array4 = {1, 2, 3};
     * Integer[] array5 = {1, 2, 3};
     * boolean result3 = ArrayUtils.arrayEquals(array4, array5); // returns true
     *
     * boolean result4 = ArrayUtils.arrayEquals(null, null); // returns true
     * boolean result5 = ArrayUtils.arrayEquals(array1, null); // returns false
     * }</pre>
     *
     * @param a the first array to compare
     * @param b the second array to compare
     * @return {@code true} if both arrays are equal; otherwise, {@code false}
     */
    public static <T> boolean arrayEquals(T[] a, T[] b) {
        return Arrays.equals(a, b);
    }

    /**
     * Converts the specified {@link Enumeration} to an array of the given component type.
     *
     * <p>This method uses the provided {@link Enumeration} and converts it into an array. The type of the returned array
     * is determined by the specified component type.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Vector<String> vector = new Vector<>(Arrays.asList("one", "two", "three"));
     * String[] array = ArrayUtils.asArray(vector.elements(), String.class);
     * }</pre>
     *
     * @param enumeration   the enumeration to convert
     * @param componentType the type of the array components
     * @param <E>           the type of elements in the enumeration
     * @return an array containing all elements from the enumeration
     */
    public static <E> E[] asArray(Enumeration<E> enumeration, Class<?> componentType) {
        return asArray(list(enumeration), componentType);
    }

    /**
     * Converts the specified {@link Iterable} to an array of the given component type.
     *
     * <p>This method uses the provided {@link Iterable} and converts it into an array. The type of the returned array
     * is determined by the specified component type. Internally, it delegates to {@link #asArray(Collection, Class)}
     * after converting the iterable to a collection.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<String> list = Arrays.asList("one", "two", "three");
     * String[] array = ArrayUtils.asArray(list, String.class);
     * }</pre>
     *
     * @param elements      the iterable containing elements to convert
     * @param componentType the type of the array components
     * @param <E>           the type of elements in the iterable
     * @return an array containing all elements from the iterable
     */
    public static <E> E[] asArray(Iterable<E> elements, Class<?> componentType) {
        return asArray(newArrayList(elements), componentType);
    }

    /**
     * Converts the specified {@link Collection} to an array of the given component type.
     *
     * <p>This method uses the provided {@link Collection} and converts it into an array. The type of the returned array
     * is determined by the specified component type. Internally, it uses reflection to create a new array instance
     * of the desired type and populates it with elements from the collection.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<String> list = Arrays.asList("one", "two", "three");
     * String[] array = ArrayUtils.asArray(list, String.class);
     * }</pre>
     *
     * @param collection    the collection containing elements to convert
     * @param componentType the type of the array components
     * @param <E>           the type of elements in the collection
     * @return an array containing all elements from the collection
     */
    public static <E> E[] asArray(Collection<E> collection, Class<?> componentType) {
        return collection.toArray(newArray(componentType, 0));
    }

    /**
     * Creates a new array with the specified component type and length.
     *
     * <p>This method uses reflection to create a new array instance of the specified component type and length.
     * It provides a flexible way to instantiate arrays dynamically at runtime.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] stringArray = ArrayUtils.newArray(String.class, 3);
     * Integer[] integerArray = ArrayUtils.newArray(Integer.class, 5);
     * }</pre>
     *
     * @param componentType the type of the array components
     * @param length        the length of the array to be created
     * @param <E>           the type of the array elements
     * @return a newly created array of the specified component type and length
     */
    public static <E> E[] newArray(Class<?> componentType, int length) {
        return (E[]) newInstance(componentType, length);
    }

    /**
     * Combines a single element and an array of additional elements into a new array.
     *
     * <p>This method creates a new array by combining the provided single element with the elements from the 'others' array.
     * If the 'one' element is not an array, it will be placed as the first element in the resulting array followed by the elements from 'others'.
     * If the 'one' element is an array, this method delegates to {@link #combineArray(Object[], Object[]...)}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String single = "first";
     * String[] others = {"second", "third"};
     * String[] result = ArrayUtils.combine(single, others);
     * // result contains ["first", "second", "third"]
     *
     * Integer[] arrayOne = {1, 2};
     * Integer[] othersArray = {3, 4};
     * Integer[] resultArray = ArrayUtils.combine(arrayOne, othersArray);
     * // resultArray contains [1, 2, 3, 4]
     * }</pre>
     *
     * @param one    the single element to combine. If it's an array, it will be combined with the elements from 'others'.
     * @param others the array of additional elements to combine
     * @return a new array containing the combined elements
     */
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

    /**
     * Combines the given array with additional arrays into a single new array.
     *
     * <p>This method takes an initial array and a variable number of additional arrays, combining all into
     * a single new array. It is useful for merging multiple arrays efficiently while maintaining type safety.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] array1 = {"one", "two"};
     * String[] array2 = {"three", "four"};
     * String[] combined = ArrayUtils.combine(array1, array2);
     * // combined contains ["one", "two", "three", "four"]
     *
     * Integer[] arrayA = {1, 2};
     * Integer[] arrayB = {3, 4};
     * Integer[] arrayC = {5, 6};
     * Integer[] combinedArrays = ArrayUtils.combine(arrayA, arrayB, arrayC);
     * // combinedArrays contains [1, 2, 3, 4, 5, 6]
     * }</pre>
     *
     * @param one    the initial array to combine
     * @param others a variable number of additional arrays to merge with the initial array
     * @param <E>    the type of elements in the arrays
     * @return a new array containing all elements from the provided arrays
     */
    public static <E> E[] combine(E[] one, E[]... others) {
        return combineArray(one, others);
    }

    /**
     * Combines the given array with additional arrays into a single new array.
     *
     * <p>This method takes an initial array and a variable number of additional arrays, combining all into
     * a single new array. It is useful for merging multiple arrays efficiently while maintaining type safety.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] array1 = {"one", "two"};
     * String[] array2 = {"three", "four"};
     * String[] combined = ArrayUtils.combineArray(array1, array2);
     * // combined contains ["one", "two", "three", "four"]
     *
     * Integer[] arrayA = {1, 2};
     * Integer[] arrayB = {3, 4};
     * Integer[] arrayC = {5, 6};
     * Integer[] combinedArrays = ArrayUtils.combineArray(arrayA, arrayB, arrayC);
     * // combinedArrays contains [1, 2, 3, 4, 5, 6]
     * }</pre>
     *
     * @param one    the initial array to combine
     * @param others a variable number of additional arrays to merge with the initial array
     * @param <E>    the type of elements in the arrays
     * @return a new array containing all elements from the provided arrays
     */
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

    /**
     * Iterates over each element of the provided boolean array, applying the given consumer to each element.
     *
     * <p>This method is a convenience utility that allows for functional-style iteration over primitive arrays.
     * The consumer receives each element in turn; if the array is null or empty, no action is performed.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean[] booleans = {true, false, true};
     * ArrayUtils.forEach(booleans, value -> System.out.println("Value: " + value));
     * }</pre>
     *
     * @param values   the boolean array to iterate over
     * @param consumer the operation to perform on each element
     */
    public static void forEach(boolean[] values, Consumer<Boolean> consumer) {
        forEach(values, (i, e) -> consumer.accept(e));
    }

    /**
     * Iterates over each element of the provided byte array, applying the given consumer to each element.
     *
     * <p>This method is a convenience utility that allows for functional-style iteration over primitive arrays.
     * The consumer receives each element in turn; if the array is null or empty, no action is performed.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * byte[] bytes = {(byte) 1, (byte) 2, (byte) 3};
     * ArrayUtils.forEach(bytes, value -> System.out.println("Value: " + value));
     * }</pre>
     *
     * @param values   the byte array to iterate over
     * @param consumer the operation to perform on each element
     */
    public static void forEach(byte[] values, Consumer<Byte> consumer) {
        forEach(values, (i, e) -> consumer.accept(e));
    }

    /**
     * Iterates over each element of the provided char array, applying the given consumer to each element.
     *
     * <p>This method is a convenience utility that allows for functional-style iteration over primitive arrays.
     * The consumer receives each element in turn; if the array is null or empty, no action is performed.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * char[] chars = {'a', 'b', 'c'};
     * ArrayUtils.forEach(chars, value -> System.out.println("Value: " + value));
     * }</pre>
     *
     * @param values   the char array to iterate over
     * @param consumer the operation to perform on each element
     */
    public static void forEach(char[] values, Consumer<Character> consumer) {
        forEach(values, (i, e) -> consumer.accept(e));
    }

    /**
     * Iterates over each element of the provided short array, applying the given consumer to each element.
     *
     * <p>This method is a convenience utility that allows for functional-style iteration over primitive arrays.
     * The consumer receives each element in turn; if the array is null or empty, no action is performed.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * short[] shorts = {(short) 1, (short) 2, (short) 3};
     * ArrayUtils.forEach(shorts, value -> System.out.println("Value: " + value));
     * }</pre>
     *
     * @param values   the short array to iterate over
     * @param consumer the operation to perform on each element
     */
    public static void forEach(short[] values, Consumer<Short> consumer) {
        forEach(values, (i, e) -> consumer.accept(e));
    }

    /**
     * Iterates over each element of the provided int array, applying the given consumer to each element.
     *
     * <p>This method is a convenience utility that allows for functional-style iteration over primitive arrays.
     * The consumer receives each element in turn; if the array is null or empty, no action is performed.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * int[] ints = {1, 2, 3};
     * ArrayUtils.forEach(ints, value -> System.out.println("Value: " + value));
     * }</pre>
     *
     * @param values   the int array to iterate over
     * @param consumer the operation to perform on each element
     */
    public static void forEach(int[] values, Consumer<Integer> consumer) {
        forEach(values, (i, e) -> consumer.accept(e));
    }

    /**
     * Iterates over each element of the provided long array, applying the given consumer to each element.
     *
     * <p>This method is a convenience utility that allows for functional-style iteration over primitive arrays.
     * The consumer receives each element in turn; if the array is null or empty, no action is performed.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * long[] longs = {1L, 2L, 3L};
     * ArrayUtils.forEach(longs, value -> System.out.println("Value: " + value));
     * }</pre>
     *
     * @param values   the long array to iterate over
     * @param consumer the operation to perform on each element
     */
    public static void forEach(long[] values, Consumer<Long> consumer) {
        forEach(values, (i, e) -> consumer.accept(e));
    }

    /**
     * Iterates over each element of the provided float array, applying the given consumer to each element.
     *
     * <p>This method is a convenience utility that allows for functional-style iteration over primitive arrays.
     * The consumer receives each element in turn; if the array is null or empty, no action is performed.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * float[] floats = {1.0f, 2.0f, 3.0f};
     * ArrayUtils.forEach(floats, value -> System.out.println("Value: " + value));
     * }</pre>
     *
     * @param values   the float array to iterate over
     * @param consumer the operation to perform on each element
     */
    public static void forEach(float[] values, Consumer<Float> consumer) {
        forEach(values, (i, e) -> consumer.accept(e));
    }

    /**
     * Iterates over each element of the provided double array, applying the given consumer to each element.
     *
     * <p>This method is a convenience utility that allows for functional-style iteration over primitive arrays.
     * The consumer receives each element in turn; if the array is null or empty, no action is performed.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * double[] doubles = {1.0, 2.0, 3.0};
     * ArrayUtils.forEach(doubles, value -> System.out.println("Value: " + value));
     * }</pre>
     *
     * @param values   the double array to iterate over
     * @param consumer the operation to perform on each element
     */
    public static void forEach(double[] values, Consumer<Double> consumer) {
        forEach(values, (i, e) -> consumer.accept(e));
    }

    /**
     * Iterates over each element of the provided array, applying the given consumer to each element.
     *
     * <p>This method is a convenience utility that allows for functional-style iteration over arrays.
     * The consumer receives each element in turn; if the array is null or empty, no action is performed.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] strings = {"one", "two", "three"};
     * ArrayUtils.forEach(strings, value -> System.out.println("Value: " + value));
     * }</pre>
     *
     * @param values   the array to iterate over
     * @param consumer the operation to perform on each element
     * @param <T>      the type of elements in the array
     */
    public static <T> void forEach(T[] values, Consumer<T> consumer) {
        forEach(values, (i, e) -> consumer.accept(e));
    }

    /**
     * Iterates over each element of the provided boolean array, applying the given BiConsumer to each element with its index.
     *
     * <p>This method allows for functional-style iteration over a primitive boolean array, where both the index and the element
     * can be used in the operation. If the array is null or empty, no action is performed.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean[] booleans = {true, false, true};
     * ArrayUtils.forEach(booleans, (index, value) -> System.out.println("Index: " + index + ", Value: " + value));
     * }</pre>
     *
     * @param values                 the boolean array to iterate over
     * @param indexedElementConsumer the operation to perform on each element, taking the index and the element as arguments
     */
    public static void forEach(boolean[] values, BiConsumer<Integer, Boolean> indexedElementConsumer) {
        int length = length(values);
        for (int i = 0; i < length; i++) {
            Boolean value = values[i];
            indexedElementConsumer.accept(i, value);
        }
    }

    /**
     * Iterates over each element of the provided byte array, applying the given BiConsumer to each element with its index.
     *
     * <p>This method allows for functional-style iteration over a primitive byte array, where both the index and the element
     * can be used in the operation. If the array is null or empty, no action is performed.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * byte[] bytes = {(byte) 1, (byte) 2, (byte) 3};
     * ArrayUtils.forEach(bytes, (index, value) -> System.out.println("Index: " + index + ", Value: " + value));
     * }</pre>
     *
     * @param values                 the byte array to iterate over
     * @param indexedElementConsumer the operation to perform on each element, taking the index and the element as arguments
     */
    public static void forEach(byte[] values, BiConsumer<Integer, Byte> indexedElementConsumer) {
        int length = length(values);
        for (int i = 0; i < length; i++) {
            Byte value = values[i];
            indexedElementConsumer.accept(i, value);
        }
    }

    /**
     * Iterates over each element of the provided char array, applying the given BiConsumer to each element with its index.
     *
     * <p>This method allows for functional-style iteration over a primitive char array, where both the index and the element
     * can be used in the operation. If the array is null or empty, no action is performed.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * char[] chars = {'a', 'b', 'c'};
     * ArrayUtils.forEach(chars, (index, value) -> System.out.println("Index: " + index + ", Value: " + value));
     * }</pre>
     *
     * @param values                 the char array to iterate over
     * @param indexedElementConsumer the operation to perform on each element, taking the index and the element as arguments
     */
    public static void forEach(char[] values, BiConsumer<Integer, Character> indexedElementConsumer) {
        int length = length(values);
        for (int i = 0; i < length; i++) {
            Character value = values[i];
            indexedElementConsumer.accept(i, value);
        }
    }

    /**
     * Iterates over each element of the provided short array, applying the given BiConsumer to each element with its index.
     *
     * <p>This method allows for functional-style iteration over a primitive short array, where both the index and the element
     * can be used in the operation. If the array is null or empty, no action is performed.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * short[] shorts = {(short) 1, (short) 2, (short) 3};
     * ArrayUtils.forEach(shorts, (index, value) -> System.out.println("Index: " + index + ", Value: " + value));
     * }</pre>
     *
     * @param values                 the short array to iterate over
     * @param indexedElementConsumer the operation to perform on each element, taking the index and the element as arguments
     */
    public static void forEach(short[] values, BiConsumer<Integer, Short> indexedElementConsumer) {
        int length = length(values);
        for (int i = 0; i < length; i++) {
            Short value = values[i];
            indexedElementConsumer.accept(i, value);
        }
    }

    /**
     * Iterates over each element of the provided int array, applying the given BiConsumer to each element with its index.
     *
     * <p>This method allows for functional-style iteration over a primitive int array, where both the index and the element
     * can be used in the operation. If the array is null or empty, no action is performed.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * int[] ints = {1, 2, 3};
     * ArrayUtils.forEach(ints, (index, value) -> System.out.println("Index: " + index + ", Value: " + value));
     * }</pre>
     *
     * @param values                 the int array to iterate over
     * @param indexedElementConsumer the operation to perform on each element, taking the index and the element as arguments
     */
    public static void forEach(int[] values, BiConsumer<Integer, Integer> indexedElementConsumer) {
        int length = length(values);
        for (int i = 0; i < length; i++) {
            Integer value = values[i];
            indexedElementConsumer.accept(i, value);
        }
    }

    /**
     * Iterates over each element of the provided long array, applying the given BiConsumer to each element with its index.
     *
     * <p>This method allows for functional-style iteration over a primitive long array, where both the index and the element
     * can be used in the operation. If the array is null or empty, no action is performed.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * long[] longs = {1L, 2L, 3L};
     * ArrayUtils.forEach(longs, (index, value) -> System.out.println("Index: " + index + ", Value: " + value));
     * }</pre>
     *
     * @param values                 the long array to iterate over
     * @param indexedElementConsumer the operation to perform on each element, taking the index and the element as arguments
     */
    public static void forEach(long[] values, BiConsumer<Integer, Long> indexedElementConsumer) {
        int length = length(values);
        for (int i = 0; i < length; i++) {
            Long value = values[i];
            indexedElementConsumer.accept(i, value);
        }
    }

    /**
     * Iterates over each element of the provided float array, applying the given BiConsumer to each element with its index.
     *
     * <p>This method allows for functional-style iteration over a primitive float array, where both the index and the element
     * can be used in the operation. If the array is null or empty, no action is performed.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * float[] floats = {1.0f, 2.0f, 3.0f};
     * ArrayUtils.forEach(floats, (index, value) -> System.out.println("Index: " + index + ", Value: " + value));
     * }</pre>
     *
     * @param values                 the float array to iterate over
     * @param indexedElementConsumer the operation to perform on each element, taking the index and the element as arguments
     */
    public static void forEach(float[] values, BiConsumer<Integer, Float> indexedElementConsumer) {
        int length = length(values);
        for (int i = 0; i < length; i++) {
            Float value = values[i];
            indexedElementConsumer.accept(i, value);
        }
    }

    /**
     * Iterates over each element of the provided double array, applying the given BiConsumer to each element with its index.
     *
     * <p>This method allows for functional-style iteration over a primitive double array, where both the index and the element
     * can be used in the operation. If the array is null or empty, no action is performed.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * double[] doubles = {1.0, 2.0, 3.0};
     * ArrayUtils.forEach(doubles, (index, value) -> System.out.println("Index: " + index + ", Value: " + value));
     * }</pre>
     *
     * @param values                 the double array to iterate over
     * @param indexedElementConsumer the operation to perform on each element, taking the index and the element as arguments
     */
    public static void forEach(double[] values, BiConsumer<Integer, Double> indexedElementConsumer) {
        int length = length(values);
        for (int i = 0; i < length; i++) {
            Double value = values[i];
            indexedElementConsumer.accept(i, value);
        }
    }

    /**
     * Iterates over each element of the provided array, applying the given BiConsumer to each element with its index.
     *
     * <p>This method allows for functional-style iteration over an array, where both the index and the element
     * can be used in the operation. If the array is null or empty, no action is performed.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] strings = {"apple", "banana", "cherry"};
     * ArrayUtils.forEach(strings, (index, value) -> System.out.println("Index: " + index + ", Value: " + value));
     * }</pre>
     *
     * @param values                 the array to iterate over
     * @param indexedElementConsumer the operation to perform on each element, taking the index and the element as arguments
     * @param <T>                    the type of elements in the array
     */
    public static <T> void forEach(T[] values, BiConsumer<Integer, T> indexedElementConsumer) {
        int length = length(values);
        for (int i = 0; i < length; i++) {
            T value = values[i];
            indexedElementConsumer.accept(i, value);
        }
    }

    /**
     * Checks if the specified boolean array contains the given value.
     *
     * <p>This method iterates through the array and returns {@code true} as soon as it finds an element
     * that matches the specified value according to the equality check for primitive booleans. If no match
     * is found, or if the array is null or empty, it returns {@code false}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean[] array = {true, false, true};
     * boolean result1 = ArrayUtils.contains(array, true);  // returns true
     * boolean result2 = ArrayUtils.contains(array, false); // returns true
     *
     * boolean[] emptyArray = {};
     * boolean result3 = ArrayUtils.contains(emptyArray, true); // returns false
     *
     * boolean result4 = ArrayUtils.contains(null, false); // returns false
     * }</pre>
     *
     * @param values the boolean array to search within
     * @param value  the value to search for
     * @return {@code true} if the array contains the specified value, otherwise {@code false}
     */
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

    /**
     * Checks if the specified char array contains the given value using binary search.
     *
     * <p>This method assumes that the array is sorted and uses the binary search algorithm to determine
     * whether the array contains the specified value. If the array is not sorted, the result may be incorrect.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * char[] sortedArray = {'a', 'b', 'c', 'd'};
     * boolean result1 = ArrayUtils.contains(sortedArray, 'b'); // returns true
     * boolean result2 = ArrayUtils.contains(sortedArray, 'e'); // returns false
     *
     * char[] emptyArray = {};
     * boolean result3 = ArrayUtils.contains(emptyArray, 'a'); // returns false
     *
     * boolean result4 = ArrayUtils.contains(null, 'a'); // returns false
     * }</pre>
     *
     * @param values the char array to search within (must be sorted)
     * @param value  the value to search for
     * @return {@code true} if the array contains the specified value, otherwise {@code false}
     */
    public static boolean contains(char[] values, char value) {
        return binarySearch(values, value) > -1;
    }

    /**
     * Checks if the specified short array contains the given value using binary search.
     *
     * <p>This method assumes that the array is sorted and uses the binary search algorithm to determine
     * whether the array contains the specified value. If the array is not sorted, the result may be incorrect.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * short[] sortedArray = {1, 2, 3, 4};
     * boolean result1 = ArrayUtils.contains(sortedArray, (short) 2); // returns true
     * boolean result2 = ArrayUtils.contains(sortedArray, (short) 5); // returns false
     *
     * short[] emptyArray = {};
     * boolean result3 = ArrayUtils.contains(emptyArray, (short) 1); // returns false
     *
     * boolean result4 = ArrayUtils.contains(null, (short) 1); // returns false
     * }</pre>
     *
     * @param values the short array to search within (must be sorted)
     * @param value  the value to search for
     * @return {@code true} if the array contains the specified value, otherwise {@code false}
     */
    public static boolean contains(short[] values, short value) {
        return binarySearch(values, value) > -1;
    }

    /**
     * Checks if the specified int array contains the given value using binary search.
     *
     * <p>This method assumes that the array is sorted and uses the binary search algorithm to determine
     * whether the array contains the specified value. If the array is not sorted, the result may be incorrect.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * int[] sortedArray = {1, 2, 3, 4};
     * boolean result1 = ArrayUtils.contains(sortedArray, 2); // returns true
     * boolean result2 = ArrayUtils.contains(sortedArray, 5); // returns false
     *
     * int[] emptyArray = {};
     * boolean result3 = ArrayUtils.contains(emptyArray, 1); // returns false
     *
     * boolean result4 = ArrayUtils.contains(null, 1); // returns false
     * }</pre>
     *
     * @param values the int array to search within (must be sorted)
     * @param value  the value to search for
     * @return {@code true} if the array contains the specified value, otherwise {@code false}
     */
    public static boolean contains(int[] values, int value) {
        return binarySearch(values, value) > -1;
    }

    /**
     * Checks if the specified long array contains the given value using binary search.
     *
     * <p>This method assumes that the array is sorted and uses the binary search algorithm to determine
     * whether the array contains the specified value. If the array is not sorted, the result may be incorrect.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * long[] sortedArray = {1L, 2L, 3L, 4L};
     * boolean result1 = ArrayUtils.contains(sortedArray, 2L); // returns true
     * boolean result2 = ArrayUtils.contains(sortedArray, 5L); // returns false
     *
     * long[] emptyArray = {};
     * boolean result3 = ArrayUtils.contains(emptyArray, 1L); // returns false
     *
     * boolean result4 = ArrayUtils.contains(null, 1L); // returns false
     * }</pre>
     *
     * @param values the long array to search within (must be sorted)
     * @param value  the value to search for
     * @return {@code true} if the array contains the specified value, otherwise {@code false}
     */
    public static boolean contains(long[] values, long value) {
        return binarySearch(values, value) > -1;
    }

    /**
     * Checks if the specified float array contains the given value using binary search.
     *
     * <p>This method assumes that the array is sorted and uses the binary search algorithm to determine
     * whether the array contains the specified value. If the array is not sorted, the result may be incorrect.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * float[] sortedArray = {1.0f, 2.0f, 3.0f, 4.0f};
     * boolean result1 = ArrayUtils.contains(sortedArray, 2.0f); // returns true
     * boolean result2 = ArrayUtils.contains(sortedArray, 5.0f); // returns false
     *
     * float[] emptyArray = {};
     * boolean result3 = ArrayUtils.contains(emptyArray, 1.0f); // returns false
     *
     * boolean result4 = ArrayUtils.contains(null, 1.0f); // returns false
     * }</pre>
     *
     * @param values the float array to search within (must be sorted)
     * @param value  the value to search for
     * @return {@code true} if the array contains the specified value, otherwise {@code false}
     */
    public static boolean contains(float[] values, float value) {
        return binarySearch(values, value) > -1;
    }

    /**
     * Checks if the specified double array contains the given value using binary search.
     *
     * <p>This method assumes that the array is sorted and uses the binary search algorithm to determine
     * whether the array contains the specified value. If the array is not sorted, the result may be incorrect.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * double[] sortedArray = {1.0, 2.0, 3.0, 4.0};
     * boolean result1 = ArrayUtils.contains(sortedArray, 2.0); // returns true
     * boolean result2 = ArrayUtils.contains(sortedArray, 5.0); // returns false
     *
     * double[] emptyArray = {};
     * boolean result3 = ArrayUtils.contains(emptyArray, 1.0); // returns false
     *
     * boolean result4 = ArrayUtils.contains(null, 1.0); // returns false
     * }</pre>
     *
     * @param values the double array to search within (must be sorted)
     * @param value  the value to search for
     * @return {@code true} if the array contains the specified value, otherwise {@code false}
     */
    public static boolean contains(double[] values, double value) {
        return binarySearch(values, value) > -1;
    }

    /**
     * Checks if the specified object array contains the given value.
     *
     * <p>This method iterates through the array and returns {@code true} as soon as it finds an element
     * that matches the specified value according to the equality check defined by {@link Object#equals(Object)}.
     * If no match is found, or if the array is null or empty, it returns {@code false}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] array = {"apple", "banana", "cherry"};
     * boolean result1 = ArrayUtils.contains(array, "banana");  // returns true
     *
     * Integer[] numbers = {1, 2, 3};
     * boolean result2 = ArrayUtils.contains(numbers, 2); // returns true
     *
     * String[] emptyArray = {};
     * boolean result3 = ArrayUtils.contains(emptyArray, "apple"); // returns false
     *
     * boolean result4 = ArrayUtils.contains(null, "apple"); // returns false
     * }</pre>
     *
     * @param values the object array to search within
     * @param value  the value to search for
     * @return {@code true} if the array contains the specified value, otherwise {@code false}
     */
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
     * Converts the specified array to a string representation.
     *
     * <p>This method delegates to {@link Arrays#toString(Object[])} to generate a string representation
     * of the provided array. If the array is null, it returns "null".</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] strings = {"apple", "banana", "cherry"};
     * String result1 = ArrayUtils.arrayToString(strings);
     * // result1 contains "[apple, banana, cherry]"
     *
     * Integer[] numbers = {1, 2, 3};
     * String result2 = ArrayUtils.arrayToString(numbers);
     * // result2 contains "[1, 2, 3]"
     *
     * String result3 = ArrayUtils.arrayToString(null);
     * // result3 is "null"
     * }</pre>
     *
     * @param array the array to convert to a string
     * @param <T>   the type of elements in the array
     * @return a string representation of the array, or "null" if the array is null
     */
    public static <T> String arrayToString(T[] array) {
        return Arrays.toString(array);
    }

    private ArrayUtils() {
    }
}
