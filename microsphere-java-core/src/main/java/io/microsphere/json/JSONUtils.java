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

package io.microsphere.json;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.beans.BeanMetadata;
import io.microsphere.util.CharSequenceUtils;
import io.microsphere.util.ClassUtils;
import io.microsphere.util.Utils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import static io.microsphere.beans.BeanUtils.findWriteMethod;
import static io.microsphere.beans.BeanUtils.getBeanMetadata;
import static io.microsphere.beans.BeanUtils.resolvePropertiesAsMap;
import static io.microsphere.collection.EnumerationUtils.isEnumeration;
import static io.microsphere.collection.EnumerationUtils.ofEnumeration;
import static io.microsphere.collection.ListUtils.isList;
import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.ListUtils.ofList;
import static io.microsphere.collection.MapUtils.newFixedLinkedHashMap;
import static io.microsphere.collection.QueueUtils.isQueue;
import static io.microsphere.collection.QueueUtils.newArrayDeque;
import static io.microsphere.collection.SetUtils.isSet;
import static io.microsphere.collection.SetUtils.newFixedLinkedHashSet;
import static io.microsphere.constants.SymbolConstants.COLON_CHAR;
import static io.microsphere.constants.SymbolConstants.COMMA_CHAR;
import static io.microsphere.constants.SymbolConstants.DOUBLE_QUOTE_CHAR;
import static io.microsphere.constants.SymbolConstants.LEFT_CURLY_BRACE_CHAR;
import static io.microsphere.constants.SymbolConstants.LEFT_SQUARE_BRACKET_CHAR;
import static io.microsphere.constants.SymbolConstants.RIGHT_CURLY_BRACE_CHAR;
import static io.microsphere.constants.SymbolConstants.RIGHT_SQUARE_BRACKET_CHAR;
import static io.microsphere.convert.Converter.convertIfPossible;
import static io.microsphere.json.JSONObject.wrap;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.reflect.TypeUtils.asClass;
import static io.microsphere.reflect.TypeUtils.asParameterizedType;
import static io.microsphere.util.ClassUtils.isArray;
import static io.microsphere.util.ClassUtils.isAssignableFrom;
import static io.microsphere.util.ClassUtils.tryResolveWrapperType;
import static io.microsphere.util.ExceptionUtils.wrap;
import static io.microsphere.util.IterableUtils.isIterable;
import static io.microsphere.util.StringUtils.isNotBlank;
import static java.lang.String.format;
import static java.lang.reflect.Array.get;
import static java.lang.reflect.Array.getLength;
import static java.lang.reflect.Array.newInstance;
import static java.lang.reflect.Array.set;

/**
 * Utility class for generating and manipulating JSON strings.
 * <p>
 * This abstract class provides a set of static methods to append different types of data into a JSON-formatted string
 * using a {@link StringBuilder}. It supports primitive types, their wrapper classes, arrays (both primitive and object),
 * collections like {@link Map}, {@link Iterable}, and custom objects through recursive value appending.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * StringBuilder builder = new StringBuilder();
 *
 * // Appending simple key-value pairs:
 * JSONUtils.append(builder, "name", "John Doe");
 * // Result: {"name":"John Doe"}
 *
 * // Appending nested objects:
 * JSONUtils.append(builder, "user", Map.of("id", 1, "active", true));
 * // Result: {"user":{"id":1,"active":true}}
 *
 * // Appending arrays:
 * JSONUtils.append(builder, "numbers", new int[]{1, 2, 3});
 * // Result: {"numbers":[1,2,3]}
 *
 * // Appending collections:
 * JSONUtils.append(builder, "tags", List.of("java", "json", "utils"));
 * // Result: {"tags":["java","json","utils"]}
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see StringBuilder
 * @since 1.0.0
 */
public abstract class JSONUtils implements Utils {

    private static final String U2028 = "\\u2028";

    private static final String U2029 = "\\u2029";

    static final Class<?> UNKNOWN_CLASS = Void.class;

    /*
     * From RFC 7159, "All Unicode characters may be placed within the quotation marks
     * except for the characters that must be escaped: quotation mark, reverse solidus,
     * and the control characters (U+0000 through U+001F)."
     *
     * We also escape '\u2028' and '\u2029', which JavaScript interprets as newline
     * characters. This prevents eval() from failing with a syntax error.
     * https://github.com/google/gson/issues/341
     */
    private static final String[] REPLACEMENT_CHARS;

    static {
        REPLACEMENT_CHARS = new String[128];
        for (int i = 0; i <= 0x1f; i++) {
            REPLACEMENT_CHARS[i] = format("\\u%04x", (int) i);
        }
        REPLACEMENT_CHARS['"'] = "\\\"";
        REPLACEMENT_CHARS['\\'] = "\\\\";
        REPLACEMENT_CHARS['\t'] = "\\t";
        REPLACEMENT_CHARS['\b'] = "\\b";
        REPLACEMENT_CHARS['\n'] = "\\n";
        REPLACEMENT_CHARS['\r'] = "\\r";
        REPLACEMENT_CHARS['\f'] = "\\f";
    }

    public static void append(StringBuilder jsonBuilder, String name, boolean value) {
        appendName(jsonBuilder, name)
                .append(value);
    }

    public static void append(StringBuilder jsonBuilder, String name, byte value) {
        appendName(jsonBuilder, name)
                .append(value);
    }

    public static void append(StringBuilder jsonBuilder, String name, short value) {
        appendName(jsonBuilder, name)
                .append(value);
    }

    public static void append(StringBuilder jsonBuilder, String name, int value) {
        appendName(jsonBuilder, name)
                .append(value);
    }

    public static void append(StringBuilder jsonBuilder, String name, long value) {
        appendName(jsonBuilder, name)
                .append(value);
    }

    public static void append(StringBuilder jsonBuilder, String name, float value) {
        appendName(jsonBuilder, name)
                .append(value);
    }

    public static void append(StringBuilder jsonBuilder, String name, double value) {
        appendName(jsonBuilder, name)
                .append(value);
    }

    public static void append(StringBuilder jsonBuilder, String name, char value) {
        appendName(jsonBuilder, name)
                .append(DOUBLE_QUOTE_CHAR)
                .append(value)
                .append(DOUBLE_QUOTE_CHAR);
    }

    public static void append(StringBuilder jsonBuilder, String name, Boolean value) {
        appendName(jsonBuilder, name)
                .append(value);
    }

    public static void append(StringBuilder jsonBuilder, String name, Byte value) {
        appendName(jsonBuilder, name)
                .append(value);
    }

    public static void append(StringBuilder jsonBuilder, String name, Short value) {
        appendName(jsonBuilder, name)
                .append(value);
    }

    public static void append(StringBuilder jsonBuilder, String name, Integer value) {
        appendName(jsonBuilder, name)
                .append(value);
    }

    public static void append(StringBuilder jsonBuilder, String name, Long value) {
        appendName(jsonBuilder, name)
                .append(value);
    }

    public static void append(StringBuilder jsonBuilder, String name, Float value) {
        appendName(jsonBuilder, name)
                .append(value);
    }

    public static void append(StringBuilder jsonBuilder, String name, Double value) {
        appendName(jsonBuilder, name)
                .append(value);
    }

    public static void append(StringBuilder jsonBuilder, String name, Character value) {
        appendName(jsonBuilder, name)
                .append(DOUBLE_QUOTE_CHAR)
                .append(value)
                .append(DOUBLE_QUOTE_CHAR);
    }

    public static void append(StringBuilder jsonBuilder, String name, String value) {
        appendName(jsonBuilder, name)
                .append(DOUBLE_QUOTE_CHAR)
                .append(value)
                .append(DOUBLE_QUOTE_CHAR);
    }

    public static void append(StringBuilder jsonBuilder, String name, Type value) {
        append(jsonBuilder, name, value.getTypeName());
    }

    public static void append(StringBuilder jsonBuilder, String name, Object value) {
        appendName(jsonBuilder, name);
        appendValue(jsonBuilder, value);
    }

    public static void append(StringBuilder jsonBuilder, String name, boolean[] values) {
        appendName(jsonBuilder, name);
        jsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
        for (int i = 0; i < values.length; i++) {
            jsonBuilder.append(values[i]);
            if (i < values.length - 1) {
                jsonBuilder.append(COMMA_CHAR);
            }
        }
        jsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);
    }

    public static void append(StringBuilder jsonBuilder, String name, byte[] values) {
        appendName(jsonBuilder, name);
        jsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
        for (int i = 0; i < values.length; i++) {
            jsonBuilder.append(values[i]);
            if (i < values.length - 1) {
                jsonBuilder.append(COMMA_CHAR);
            }
        }
        jsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);
    }

    public static void append(StringBuilder jsonBuilder, String name, short[] values) {
        appendName(jsonBuilder, name);
        jsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
        for (int i = 0; i < values.length; i++) {
            jsonBuilder.append(values[i]);
            if (i < values.length - 1) {
                jsonBuilder.append(COMMA_CHAR);
            }
        }
        jsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);
    }

    public static void append(StringBuilder jsonBuilder, String name, int[] values) {
        appendName(jsonBuilder, name);
        jsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
        for (int i = 0; i < values.length; i++) {
            jsonBuilder.append(values[i]);
            if (i < values.length - 1) {
                jsonBuilder.append(COMMA_CHAR);
            }
        }
        jsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);
    }

    public static void append(StringBuilder jsonBuilder, String name, long[] values) {
        appendName(jsonBuilder, name);
        jsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
        for (int i = 0; i < values.length; i++) {
            jsonBuilder.append(values[i]);
            if (i < values.length - 1) {
                jsonBuilder.append(COMMA_CHAR);
            }
        }
        jsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);
    }

    public static void append(StringBuilder jsonBuilder, String name, float[] values) {
        appendName(jsonBuilder, name);
        jsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
        for (int i = 0; i < values.length; i++) {
            jsonBuilder.append(values[i]);
            if (i < values.length - 1) {
                jsonBuilder.append(COMMA_CHAR);
            }
        }
        jsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);
    }

    public static void append(StringBuilder jsonBuilder, String name, double[] values) {
        appendName(jsonBuilder, name);
        jsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
        for (int i = 0; i < values.length; i++) {
            jsonBuilder.append(values[i]);
            if (i < values.length - 1) {
                jsonBuilder.append(COMMA_CHAR);
            }
        }
        jsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);
    }

    public static void append(StringBuilder jsonBuilder, String name, char[] values) {
        appendName(jsonBuilder, name);
        jsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
        for (int i = 0; i < values.length; i++) {
            jsonBuilder.append(DOUBLE_QUOTE_CHAR)
                    .append(values[i])
                    .append(DOUBLE_QUOTE_CHAR);
            if (i < values.length - 1) {
                jsonBuilder.append(COMMA_CHAR);
            }
        }
        jsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);
    }

    public static void append(StringBuilder jsonBuilder, String name, String[] values) {
        appendName(jsonBuilder, name);
        jsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
        for (int i = 0; i < values.length; i++) {
            jsonBuilder.append(DOUBLE_QUOTE_CHAR)
                    .append(values[i])
                    .append(DOUBLE_QUOTE_CHAR);
            if (i < values.length - 1) {
                jsonBuilder.append(COMMA_CHAR);
            }
        }
        jsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);
    }

    public static void append(StringBuilder jsonBuilder, String name, Boolean[] values) {
        appendName(jsonBuilder, name);
        jsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
        for (int i = 0; i < values.length; i++) {
            jsonBuilder.append(values[i]);
            if (i < values.length - 1) {
                jsonBuilder.append(COMMA_CHAR);
            }
        }
        jsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);
    }

    public static void append(StringBuilder jsonBuilder, String name, Byte[] values) {
        appendName(jsonBuilder, name);
        jsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
        for (int i = 0; i < values.length; i++) {
            jsonBuilder.append(values[i]);
            if (i < values.length - 1) {
                jsonBuilder.append(COMMA_CHAR);
            }
        }
        jsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);
    }

    public static void append(StringBuilder jsonBuilder, String name, Short[] values) {
        appendName(jsonBuilder, name);
        jsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
        for (int i = 0; i < values.length; i++) {
            jsonBuilder.append(values[i]);
            if (i < values.length - 1) {
                jsonBuilder.append(COMMA_CHAR);
            }
        }
        jsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);
    }

    public static void append(StringBuilder jsonBuilder, String name, Integer[] values) {
        appendName(jsonBuilder, name);
        jsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
        for (int i = 0; i < values.length; i++) {
            jsonBuilder.append(values[i]);
            if (i < values.length - 1) {
                jsonBuilder.append(COMMA_CHAR);
            }
        }
        jsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);
    }

    public static void append(StringBuilder jsonBuilder, String name, Long[] values) {
        appendName(jsonBuilder, name);
        jsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
        for (int i = 0; i < values.length; i++) {
            jsonBuilder.append(values[i]);
            if (i < values.length - 1) {
                jsonBuilder.append(COMMA_CHAR);
            }
        }
        jsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);
    }

    public static void append(StringBuilder jsonBuilder, String name, Float[] values) {
        appendName(jsonBuilder, name);

        jsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
        for (int i = 0; i < values.length; i++) {
            jsonBuilder.append(values[i]);
            if (i < values.length - 1) {
                jsonBuilder.append(COMMA_CHAR);
            }
        }
        jsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);
    }

    public static void append(StringBuilder jsonBuilder, String name, Double[] values) {
        appendName(jsonBuilder, name);
        jsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
        for (int i = 0; i < values.length; i++) {
            jsonBuilder.append(values[i]);
            if (i < values.length - 1) {
                jsonBuilder.append(COMMA_CHAR);
            }
        }
        jsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);
    }

    public static void append(StringBuilder jsonBuilder, String name, Character[] values) {
        appendName(jsonBuilder, name);
        jsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
        for (int i = 0; i < values.length; i++) {
            jsonBuilder.append(DOUBLE_QUOTE_CHAR)
                    .append(values[i])
                    .append(DOUBLE_QUOTE_CHAR);
            if (i < values.length - 1) {
                jsonBuilder.append(COMMA_CHAR);
            }
        }
        jsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);
    }

    public static <T> void append(StringBuilder jsonBuilder, String name, T[] values) {
        appendName(jsonBuilder, name);
        jsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
        for (int i = 0; i < values.length; i++) {
            appendValue(jsonBuilder, values[i]);
            if (i < values.length - 1) {
                jsonBuilder.append(COMMA_CHAR);
            }
        }
        jsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);
    }

    public static void appendValue(StringBuilder jsonBuilder, Object value) {
        Class<?> valueClass = value.getClass();
        if (valueClass.isArray()) {
            appendArray(jsonBuilder, value);
        } else if (Map.class.isAssignableFrom(valueClass)) {
            appendMap(jsonBuilder, (Map<String, Object>) value);
        } else if (Iterable.class.isAssignableFrom(valueClass)) {
            appendIterable(jsonBuilder, (Iterable<?>) value);
        } else if (Character.class == valueClass
                || CharSequence.class.isAssignableFrom(valueClass)
                || Enum.class.isAssignableFrom(valueClass)) {
            appendString(jsonBuilder, value);
        } else if (Type.class.isAssignableFrom(valueClass)) {
            appendType(jsonBuilder, (Type) value);
        } else {
            jsonBuilder.append(value);
        }
    }

    static void appendArray(StringBuilder jsonBuilder, Object value) {
        jsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
        int length = getLength(value);
        for (int i = 0; i < length; i++) {
            appendValue(jsonBuilder, get(value, i));
            if (i < length - 1) {
                jsonBuilder.append(COMMA_CHAR);
            }
        }
        jsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);
    }

    public static StringBuilder appendName(StringBuilder jsonBuilder, String name) {
        if (isNotBlank(name)) {
            jsonBuilder.append(DOUBLE_QUOTE_CHAR)
                    .append(name)
                    .append(DOUBLE_QUOTE_CHAR)
                    .append(COLON_CHAR);
        }
        return jsonBuilder;
    }

    static boolean isUnknownClass(Class<?> targetClass) {
        return UNKNOWN_CLASS == targetClass;
    }

    /**
     * Returns the number of name/value mappings in the specified {@link JSONObject}.
     * <p>
     * This method returns the count of key-value pairs in the given {@code JSONObject}.
     * If the {@code JSONObject} is {@code null}, this method returns {@code 0}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * JSONObject obj = new JSONObject();
     * obj.put("name", "John");
     * obj.put("age", 30);
     * int length = JSONUtils.length(obj); // returns 2
     *
     * JSONObject nullObj = null;
     * int nullLength = JSONUtils.length(nullObj); // returns 0
     * }</pre>
     *
     * @param jsonObject the {@link JSONObject} whose length is to be determined
     * @return the number of name/value mappings in the {@link JSONObject}, or {@code 0} if the {@link JSONObject} is {@code null}
     * @see JSONObject#length()
     */
    public static int length(JSONObject jsonObject) {
        return jsonObject == null ? 0 : jsonObject.length();
    }

    /**
     * Returns the number of values in the specified {@link JSONArray}.
     * <p>
     * This method returns the count of elements in the given {@code JSONArray}.
     * If the {@code JSONArray} is {@code null}, this method returns {@code 0}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * JSONArray array = new JSONArray();
     * array.put("value1");
     * array.put("value2");
     * int length = JSONUtils.length(array); // returns 2
     *
     * JSONArray nullArray = null;
     * int nullLength = JSONUtils.length(nullArray); // returns 0
     * }</pre>
     *
     * @param jsonArray the {@link JSONArray} whose length is to be determined
     * @return the number of values in the {@link JSONArray}, or {@code 0} if the {@link JSONArray} is {@code null}
     * @see JSONArray#length()
     */
    public static int length(JSONArray jsonArray) {
        return jsonArray == null ? 0 : jsonArray.length();
    }

    /**
     * Checks if the given {@link JSONObject} is empty.
     * <p>
     * This method returns {@code true} if the provided {@code JSONObject} is {@code null} or contains no key-value mappings.
     * Otherwise, it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * JSONObject obj = new JSONObject();
     * boolean empty = JSONUtils.isEmpty(obj); // returns true
     *
     * obj.put("key", "value");
     * boolean notEmpty = JSONUtils.isEmpty(obj); // returns false
     *
     * JSONObject nullObj = null;
     * boolean nullEmpty = JSONUtils.isEmpty(nullObj); // returns true
     * }</pre>
     *
     * @param jsonObject the {@link JSONObject} to check
     * @return {@code true} if the {@link JSONObject} is {@code null} or empty, {@code false} otherwise
     * @see #length(JSONObject)
     * @see JSONObject#length()
     */
    public static boolean isEmpty(JSONObject jsonObject) {
        return length(jsonObject) == 0;
    }

    /**
     * Checks if the given {@link JSONArray} is empty.
     * <p>
     * This method returns {@code true} if the provided {@code JSONArray} is {@code null} or contains no elements.
     * Otherwise, it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * JSONArray array = new JSONArray();
     * boolean empty = JSONUtils.isEmpty(array); // returns true
     *
     * array.put("value");
     * boolean notEmpty = JSONUtils.isEmpty(array); // returns false
     *
     * JSONArray nullArray = null;
     * boolean nullEmpty = JSONUtils.isEmpty(nullArray); // returns true
     * }</pre>
     *
     * @param jsonArray the {@link JSONArray} to check
     * @return {@code true} if the {@link JSONArray} is {@code null} or empty, {@code false} otherwise
     * @see #length(JSONArray)
     * @see JSONArray#length()
     */
    public static boolean isEmpty(JSONArray jsonArray) {
        return length(jsonArray) == 0;
    }

    /**
     * Checks if the given {@link JSONObject} is not empty.
     * <p>
     * This method returns {@code true} if the provided {@code JSONObject} is not {@code null} and contains at least one key-value mapping.
     * Otherwise, it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * JSONObject obj = new JSONObject();
     * boolean empty = JSONUtils.isNotEmpty(obj); // returns false
     *
     * obj.put("key", "value");
     * boolean notEmpty = JSONUtils.isNotEmpty(obj); // returns true
     *
     * JSONObject nullObj = null;
     * boolean nullEmpty = JSONUtils.isNotEmpty(nullObj); // returns false
     * }</pre>
     *
     * @param jsonObject the {@link JSONObject} to check
     * @return {@code true} if the {@link JSONObject} is not {@code null} and not empty, {@code false} otherwise
     * @see #isEmpty(JSONObject)
     * @see JSONObject#length()
     */
    public static boolean isNotEmpty(JSONObject jsonObject) {
        return !isEmpty(jsonObject);
    }

    /**
     * Checks if the given {@link JSONArray} is not empty.
     * <p>
     * This method returns {@code true} if the provided {@code JSONArray} is not {@code null} and contains at least one element.
     * Otherwise, it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * JSONArray array = new JSONArray();
     * boolean empty = JSONUtils.isNotEmpty(array); // returns false
     *
     * array.put("value");
     * boolean notEmpty = JSONUtils.isNotEmpty(array); // returns true
     *
     * JSONArray nullArray = null;
     * boolean nullEmpty = JSONUtils.isNotEmpty(nullArray); // returns false
     * }</pre>
     *
     * @param jsonArray the {@link JSONArray} to check
     * @return {@code true} if the {@link JSONArray} is not {@code null} and not empty, {@code false} otherwise
     * @see #isEmpty(JSONArray)
     * @see JSONArray#length()
     */
    public static boolean isNotEmpty(JSONArray jsonArray) {
        return !isEmpty(jsonArray);
    }

    /**
     * Checks if the given object is null or equals to {@link JSONObject#NULL}.
     * <p>
     * This method returns {@code true} if the provided object is {@code null} or
     * is equal to {@link JSONObject#NULL}, which is a special sentinel value used
     * in JSON operations. Otherwise, it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean result1 = JSONUtils.isNull(null); // returns true
     * boolean result2 = JSONUtils.isNull(JSONObject.NULL); // returns true
     * boolean result3 = JSONUtils.isNull("some value"); // returns false
     * }</pre>
     *
     * @param object the object to check
     * @return {@code true} if the object is {@code null} or equals to {@link JSONObject#NULL}, {@code false} otherwise
     * @see JSONObject#NULL
     */
    public static boolean isNull(Object object) {
        return JSONObject.NULL.equals(object);
    }

    /**
     * Checks if the given object is not null and not equal to {@link JSONObject#NULL}.
     * <p>
     * This method returns {@code true} if the provided object is neither {@code null} nor
     * equal to {@link JSONObject#NULL}, which is a special sentinel value used in JSON operations.
     * Otherwise, it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean result1 = JSONUtils.isNotNull("some value"); // returns true
     * boolean result2 = JSONUtils.isNotNull(JSONObject.NULL); // returns false
     * boolean result3 = JSONUtils.isNotNull(null); // returns false
     * }</pre>
     *
     * @param object the object to check
     * @return {@code true} if the object is not {@code null} and not equal to {@link JSONObject#NULL}, {@code false} otherwise
     * @see JSONObject#NULL
     * @see #isNull(Object)
     */
    public static boolean isNotNull(Object object) {
        return !isNull(object);
    }

    /**
     * Checks if the given object is an instance of {@link JSONObject}.
     * <p>
     * This method returns {@code true} if the provided object is an instance of {@link JSONObject},
     * and {@code false} otherwise. It is useful for type checking before performing operations
     * specific to {@link JSONObject}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Object obj = new JSONObject();
     * boolean result = JSONUtils.isJSONObject(obj); // returns true
     *
     * Object str = "not a JSONObject";
     * boolean result2 = JSONUtils.isJSONObject(str); // returns false
     * }</pre>
     *
     * @param value the object to check
     * @return {@code true} if the object is an instance of {@link JSONObject}, {@code false} otherwise
     * @see JSONObject
     */
    public static boolean isJSONObject(Object value) {
        return value instanceof JSONObject;
    }

    /**
     * Checks if the given object is an instance of {@link JSONArray}.
     * <p>
     * This method returns {@code true} if the provided object is an instance of {@link JSONArray},
     * and {@code false} otherwise. It is useful for type checking before performing operations
     * specific to {@link JSONArray}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Object obj = new JSONArray();
     * boolean result = JSONUtils.isJSONArray(obj); // returns true
     *
     * Object str = "not a JSONArray";
     * boolean result2 = JSONUtils.isJSONArray(str); // returns false
     * }</pre>
     *
     * @param value the object to check
     * @return {@code true} if the object is an instance of {@link JSONArray}, {@code false} otherwise
     * @see JSONArray
     */
    public static boolean isJSONArray(Object value) {
        return value instanceof JSONArray;
    }

    /**
     * Parses a JSON string and returns a {@link JSONObject} representation of it.
     * <p>
     * This method takes a valid JSON string and converts it into a {@code JSONObject}.
     * If the string is not a valid JSON or cannot be parsed into a {@code JSONObject},
     * an {@link IllegalArgumentException} will be thrown with the underlying {@link JSONException}
     * as the cause.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String jsonString = "{\"name\":\"John\", \"age\":30}";
     * JSONObject jsonObject = JSONUtils.jsonObject(jsonString);
     * String name = jsonObject.getString("name"); // "John"
     * int age = jsonObject.getInt("age");        // 30
     *
     * // Invalid JSON example:
     * try {
     *     JSONUtils.jsonObject("{invalid json}");
     * } catch (IllegalArgumentException e) {
     *     // Handle parsing error
     * }
     * }</pre>
     *
     * @param json the JSON string to parse
     * @return a {@code JSONObject} representation of the parsed JSON string
     * @throws IllegalArgumentException if the string is not valid JSON or cannot be parsed
     */
    @Nonnull
    public static JSONObject jsonObject(String json) throws IllegalArgumentException {
        final JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            throw wrap(e, IllegalArgumentException.class);
        }
        return jsonObject;
    }

    /**
     * Parses a JSON string and returns a {@link JSONArray} representation of it.
     * <p>
     * This method takes a valid JSON string and converts it into a {@code JSONArray}.
     * If the string is not a valid JSON or cannot be parsed into a {@code JSONArray},
     * an {@link IllegalArgumentException} will be thrown with the underlying {@link JSONException}
     * as the cause.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String jsonString = "[\"apple\", \"banana\", \"cherry\"]";
     * JSONArray jsonArray = JSONUtils.jsonArray(jsonString);
     * String firstItem = jsonArray.getString(0); // "apple"
     * int length = jsonArray.length();           // 3
     *
     * // Invalid JSON example:
     * try {
     *     JSONUtils.jsonArray("[invalid json]");
     * } catch (IllegalArgumentException e) {
     *     // Handle parsing error
     * }
     * }</pre>
     *
     * @param json the JSON string to parse
     * @return a {@code JSONArray} representation of the parsed JSON string
     * @throws IllegalArgumentException if the string is not valid JSON or cannot be parsed
     * @see JSONArray
     */
    @Nonnull
    public static JSONArray jsonArray(String json) throws IllegalArgumentException {
        final JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(json);
        } catch (JSONException e) {
            throw wrap(e, IllegalArgumentException.class);
        }
        return jsonArray;
    }

    /**
     * Reads a JSON string and converts it into an instance of the specified target type.
     * <p>
     * This method parses the provided JSON string into a {@link JSONObject} and then maps its properties
     * to a new instance of the target type. It supports nested objects, collections, and type conversion
     * where necessary.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String json = "{\"name\":\"John Doe\",\"age\":30}";
     * Person person = JSONUtils.readValue(json, Person.class);
     * // person.getName() returns "John Doe"
     * // person.getAge() returns 30
     *
     * String nestedJson = "{\"user\":{\"name\":\"Jane\"},\"active\":true}";
     * MyBean bean = JSONUtils.readValue(nestedJson, MyBean.class);
     * // bean.getUser().getName() returns "Jane"
     * // bean.isActive() returns true
     * }</pre>
     *
     * @param json       the JSON string to parse and convert
     * @param targetType the class of the target type to which the JSON should be converted
     * @param <V>        the type of the target object
     * @return an instance of the target type populated with data from the JSON string
     * @throws IllegalArgumentException if the JSON string is invalid or cannot be converted to the target type
     * @see JSONObject
     * @see #readValue(JSONObject, Class)
     */
    @Nonnull
    public static <V> V readValue(String json, Class<V> targetType) {
        return readValue(jsonObject(json), targetType);
    }

    /**
     * Reads a {@link JSONObject} and converts it into an instance of the specified target type.
     * <p>
     * This method takes a {@code JSONObject} and maps its properties to a new instance of the target type.
     * It supports nested objects, collections, and type conversion where necessary.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * JSONObject jsonObject = new JSONObject("{\"name\":\"John Doe\",\"age\":30}");
     * Map<String, Object> map = JSONUtils.readValueAsMap(jsonObject);
     * // map.get("name") returns "John Doe"
     * // map.get("age") returns 30
     *
     * JSONObject nestedJsonObject = new JSONObject("{\"user\":{\"name\":\"Jane\"},\"active\":true}");
     * MyBean bean = JSONUtils.readValue(nestedJsonObject, MyBean.class);
     * // bean.getUser().getName() returns "Jane"
     * // bean.isActive() returns true
     * }</pre>
     *
     * @param jsonObject the {@code JSONObject} to parse and convert
     * @param targetType the class of the target type to which the JSON should be converted
     * @param <V>        the type of the target object
     * @return an instance of the target type populated with data from the {@code JSONObject}
     * @throws IllegalArgumentException if the {@code JSONObject} cannot be converted to the target type
     * @see JSONObject
     * @see #readValue(String, Class)
     */
    @Nonnull
    public static <V> V readValue(JSONObject jsonObject, Class<V> targetType) {
        if (isAssignableFrom(Map.class, targetType)) {
            return (V) readValueAsMap(jsonObject);
        }
        return readValueAsBean(jsonObject, targetType);
    }

    /**
     * Reads a {@link JSONObject} and converts it into a {@link Map} with {@link String} keys and {@link Object} values.
     * <p>
     * This method takes a {@code JSONObject} and maps its properties to a new {@code Map}. Each key-value pair
     * in the {@code JSONObject} is added to the map, with values being converted if necessary.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * JSONObject jsonObject = new JSONObject("{\"name\":\"John Doe\",\"age\":30}");
     * Map<String, Object> map = JSONUtils.readValueAsMap(jsonObject);
     * // map.get("name") returns "John Doe"
     * // map.get("age") returns 30
     *
     * JSONObject nestedJsonObject = new JSONObject("{\"user\":{\"name\":\"Jane\"},\"active\":true}");
     * Map<String, Object> nestedMap = JSONUtils.readValueAsMap(nestedJsonObject);
     * // nestedMap.get("user") returns a Map representing the user object
     * // nestedMap.get("active") returns true
     * }</pre>
     *
     * @param jsonObject the {@code JSONObject} to parse and convert
     * @return a {@code Map} populated with data from the {@code JSONObject}
     * @see JSONObject
     */
    public static Map<String, Object> readValueAsMap(JSONObject jsonObject) {
        Map<String, Object> map = newFixedLinkedHashMap(jsonObject.length());
        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = jsonObject.opt(key);
            map.put(key, convertValue(value, Map.class));
        }
        return map;
    }

    /**
     * Reads a {@link JSONObject} and converts it into an instance of the specified bean type.
     * <p>
     * This method takes a {@code JSONObject} and maps its properties to a new instance of the bean type.
     * It supports nested objects, collections, and type conversion where necessary.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * JSONObject jsonObject = new JSONObject("{\"name\":\"John Doe\",\"age\":30}");
     * Person person = JSONUtils.readValue(jsonObject, Person.class);
     * // person.getName() returns "John Doe"
     * // person.getAge() returns 30
     *
     * JSONObject nestedJsonObject = new JSONObject("{\"user\":{\"name\":\"Jane\"},\"active\":true}");
     * MyBean bean = JSONUtils.readValue(nestedJsonObject, MyBean.class);
     * // bean.getUser().getName() returns "Jane"
     * // bean.isActive() returns true
     * }</pre>
     *
     * @param jsonObject the {@code JSONObject} to parse and convert
     * @param beanClass  the class of the Bean to which the JSON should be converted
     * @param <V>        the type of the target object
     * @return an instance of the bean type populated with data from the {@code JSONObject}
     * @throws IllegalArgumentException if the {@code JSONObject} cannot be converted to the bean type
     * @see JSONObject
     * @see #readValue(String, Class)
     */
    @Nonnull
    public static <V> V readValueAsBean(JSONObject jsonObject, Class<V> beanClass) {
        BeanMetadata beanMetadata = getBeanMetadata(beanClass);
        V valueObject = ClassUtils.newInstance(beanClass);
        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = jsonObject.opt(key);
            if (isNotNull(value)) {
                Method writeMethod = findWriteMethod(beanMetadata, key);
                if (writeMethod != null) {
                    Type propertyType = writeMethod.getGenericParameterTypes()[0];
                    Object convertedValue = convertValue(value, propertyType);
                    if (convertedValue != null) {
                        invokeMethod(valueObject, writeMethod, convertedValue);
                    }
                }
            }
        }
        return valueObject;
    }

    /**
     * Reads a JSON array string and converts it into an instance of the specified collection or array type.
     * <p>
     * This method parses the provided JSON array string into a {@link JSONArray} and then maps its elements
     * to a new instance of the specified collection or array type. It supports arrays, {@link List}, {@link Set},
     * {@link Queue}, and {@link java.util.Enumeration}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String json = "[\"apple\", \"banana\", \"cherry\"]";
     *
     * // Convert to String array
     * String[] stringArray = (String[]) JSONUtils.readValues(json, String[].class, String.class);
     *
     * // Convert to List<String>
     * List<String> stringList = (List<String>) JSONUtils.readValues(json, List.class, String.class);
     *
     * // Convert to Set<String>
     * Set<String> stringSet = (Set<String>) JSONUtils.readValues(json, Set.class, String.class);
     * }</pre>
     *
     * @param json          the JSON array string to parse and convert
     * @param multipleClass the class of the target collection or array type to which the JSON should be converted
     * @param elementClass  the class of the elements in the target collection or array
     * @return an instance of the target collection or array type populated with data from the JSON array string,
     * or {@code null} if the target type is not supported
     * @throws IllegalArgumentException if the JSON string is invalid or cannot be parsed into a {@code JSONArray}
     * @see JSONArray
     * @see #readValues(JSONArray, Class, Class)
     */
    @Nullable
    public static <V> V readValues(String json, Class<V> multipleClass, Class<?> elementClass) {
        return readValues(jsonArray(json), multipleClass, elementClass);
    }

    /**
     * Reads a {@link JSONArray} and converts it into an instance of the specified target type.
     * <p>
     * This method takes a {@code JSONArray} and maps its elements to a new instance of the specified target type.
     * It supports arrays, {@link List}, {@link Set}, {@link Queue}, and {@link java.util.Enumeration}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * JSONArray jsonArray = new JSONArray("[\"apple\", \"banana\", \"cherry\"]");
     *
     * // Convert to String array
     * String[] stringArray = (String[]) JSONUtils.readValues(jsonArray, String[].class);
     *
     * // Convert to List<String>
     * List<String> stringList = (List<String>) JSONUtils.readValues(jsonArray, List.class);
     *
     * // Convert to Set<String>
     * Set<String> stringSet = (Set<String>) JSONUtils.readValues(jsonArray, Set.class);
     * }</pre>
     *
     * @param jsonArray  the {@code JSONArray} to parse and convert
     * @param targetType the target type to which the JSON should be converted, it can be an array, {@link List}, {@link Set}, {@link Queue}, or {@link java.util.Enumeration}
     * @return an instance of the target type populated with data from the {@code JSONArray},
     * or {@code null} if the target type is not supported or the {@code JSONArray} is empty
     * @see JSONArray
     * @see #readValues(JSONArray, Class, Class)
     */
    @Nullable
    public static Object readValues(JSONArray jsonArray, Type targetType) {
        if (isEmpty(jsonArray)) {
            return null;
        }
        ParameterizedType parameterizedType = asParameterizedType(targetType);
        Class<?> multipleClass = null;
        Class<?> elementClass;
        if (parameterizedType == null) { // If the target type is not parameterized
            elementClass = determineElementClass(jsonArray);
        } else {
            multipleClass = asClass(parameterizedType.getRawType());
            elementClass = asClass(parameterizedType.getActualTypeArguments()[0]);
        }
        if (multipleClass == null) {
            if (elementClass != null) {
                return readArray(jsonArray, elementClass);
            }
            return null;
        }
        return readValues(jsonArray, multipleClass, elementClass);
    }


    /**
     * Reads a {@link JSONArray} and converts it into an instance of the specified collection or array type.
     * <p>
     * This method takes a {@code JSONArray} and maps its elements to a new instance of the specified collection
     * or array type. It supports arrays, {@link List}, {@link Set}, {@link Queue}, and {@link java.util.Enumeration}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * JSONArray jsonArray = new JSONArray("[\"apple\", \"banana\", \"cherry\"]");
     *
     * // Convert to String array
     * String[] stringArray = (String[]) JSONUtils.readValues(jsonArray, String[].class, String.class);
     *
     * // Convert to List<String>
     * List<String> stringList = (List<String>) JSONUtils.readValues(jsonArray, List.class, String.class);
     *
     * // Convert to Set<String>
     * Set<String> stringSet = (Set<String>) JSONUtils.readValues(jsonArray, Set.class, String.class);
     * }</pre>
     *
     * @param jsonArray     the {@code JSONArray} to parse and convert
     * @param multipleClass the class of the target collection or array type to which the JSON should be converted
     * @param elementClass  the class of the elements in the target collection or array
     * @return an instance of the target collection or array type populated with data from the {@code JSONArray},
     * or {@code null} if the target type is not supported
     * @see JSONArray
     * @see #readValues(String, Class, Class)
     */
    @Nullable
    public static <V> V readValues(JSONArray jsonArray, Class<V> multipleClass, Class<?> elementClass) {
        if (isArray(multipleClass)) {
            return (V) readArray(jsonArray, multipleClass.getComponentType());
        } else if (isList(multipleClass)) {
            return (V) toList(jsonArray, elementClass);
        } else if (isSet(multipleClass)) {
            return (V) toSet(jsonArray, elementClass);
        } else if (isQueue(multipleClass)) {
            return (V) toQueue(jsonArray, elementClass);
        } else if (isEnumeration(multipleClass)) {
            return (V) toEnumeration(jsonArray, elementClass);
        } else if (isIterable(multipleClass)) {
            return (V) toList(jsonArray, elementClass);
        }
        return null;
    }

    /**
     * Reads a JSON array string and converts it into an array of the specified component type.
     * <p>
     * This method parses the provided JSON array string into a {@link JSONArray} and then maps its elements
     * to a new array of the specified component type. It supports arrays of any type, including primitives
     * and their wrapper classes.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String json = "[\"apple\", \"banana\", \"cherry\"]";
     * String[] stringArray = JSONUtils.readArray(json, String.class);
     * // stringArray[0] returns "apple"
     * // stringArray.length returns 3
     *
     * String numberJson = "[1, 2, 3]";
     * Integer[] integerArray = JSONUtils.readArray(numberJson, Integer.class);
     * // integerArray[0] returns 1
     * // integerArray.length returns 3
     * }</pre>
     *
     * @param json          the JSON array string to parse and convert
     * @param componentType the class of the component type of the array
     * @param <E>           the type of the elements in the array
     * @return an array of the specified component type populated with data from the JSON array string
     * @throws IllegalArgumentException if the JSON string is invalid or cannot be parsed into a {@code JSONArray}
     * @see JSONArray
     * @see #readArray(JSONArray, Class)
     */
    public static <E> E[] readArray(String json, Class<E> componentType) {
        return readArray(jsonArray(json), componentType);
    }

    /**
     * Reads a {@link JSONArray} and converts it into an array of the specified component type.
     * <p>
     * This method takes a {@code JSONArray} and maps its elements to a new array of the specified component type.
     * It supports arrays of any type, including primitives and their wrapper classes.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * JSONArray jsonArray = new JSONArray("[\"apple\", \"banana\", \"cherry\"]");
     * String[] stringArray = JSONUtils.readArray(jsonArray, String.class);
     * // stringArray[0] returns "apple"
     * // stringArray.length returns 3
     *
     * JSONArray numberJsonArray = new JSONArray("[1, 2, 3]");
     * Integer[] integerArray = JSONUtils.readArray(numberJsonArray, Integer.class);
     * // integerArray[0] returns 1
     * // integerArray.length returns 3
     * }</pre>
     *
     * @param jsonArray     the {@code JSONArray} to parse and convert
     * @param componentType the class of the component type of the array
     * @param <E>           the type of the elements in the array
     * @return an array of the specified component type populated with data from the {@code JSONArray}
     * @see JSONArray
     * @see #readArray(String, Class)
     */
    public static <E> E[] readArray(JSONArray jsonArray, Class<E> componentType) {
        int length = jsonArray.length();
        E[] array = (E[]) newInstance(componentType, length);
        for (int i = 0; i < length; i++) {
            Object value = jsonArray.opt(i);
            value = convertValue(value, componentType);
            set(array, i, value);
        }
        return array;
    }

    /**
     * Converts an object into its JSON string representation.
     * <p>
     * This method takes an object, wraps it using {@link JSONObject#wrap(Object)},
     * and then converts it to a JSON string if it is a {@link JSONObject} or {@link JSONArray}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String jsonString = JSONUtils.writeValueAsString(Map.of("name", "John", "age", 30));
     * // Result: {"name":"John","age":30}
     *
     * String arrayJson = JSONUtils.writeValueAsString(new String[]{"apple", "banana"});
     * // Result: ["apple","banana"]
     * }</pre>
     *
     * @param object the object to be converted to a JSON string.
     * @return a JSON string representation of the given object, or {@code null} if conversion is not possible.
     * @see JSONObject#wrap(Object)
     */
    @Nullable
    public static String writeValueAsString(Object object) {
        Object wrapper = wrap(object);
        if (isJSONObject(wrapper) || isJSONArray(wrapper)) {
            return wrapper.toString();
        }
        return null;
    }

    /**
     * Converts a JavaBean object into its JSON string representation.
     * <p>
     * This method takes a JavaBean object, resolves its properties into a map,
     * and then constructs a {@link JSONObject} from that map. The resulting
     * JSON object is then converted to a string.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Person {
     *     private String name;
     *     private int age;
     *
     *     // Getters and setters...
     * }
     *
     * Person person = new Person();
     * person.setName("John Doe");
     * person.setAge(30);
     *
     * String jsonString = JSONUtils.writeJavaBeanAsString(person);
     * // Result: {"name":"John Doe","age":30}
     * }</pre>
     *
     * @param javaBean the JavaBean object to be converted to a JSON string.
     * @return a JSON string representation of the given JavaBean.
     * @throws IllegalArgumentException if the input object is null.
     */
    @Nonnull
    public static String writeBeanAsString(Object javaBean) {
        Map<String, Object> properties = resolvePropertiesAsMap(javaBean);
        JSONObject jsonObject = new JSONObject(properties);
        return jsonObject.toString();
    }

    /**
     * Determines the common class of elements in the given {@link JSONArray}.
     * <p>
     * This method iterates through the elements of the provided {@code JSONArray} to find a common class.
     * If all elements are of the same class or one class is assignable from all others, that class is returned.
     * If no common class can be determined, {@code Object.class} is returned.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * JSONArray array1 = new JSONArray("[\"apple\", \"banana\", \"cherry\"]");
     * Class<?> commonClass1 = JSONUtils.determineElementClass(array1); // returns String.class
     *
     * JSONArray array2 = new JSONArray("[1, \"banana\", 3.14]");
     * Class<?> commonClass2 = JSONUtils.determineElementClass(array2); // returns Object.class
     *
     * JSONArray array3 = new JSONArray("[1, 2, 3]");
     * Class<?> commonClass3 = JSONUtils.determineElementClass(array3); // returns Integer.class
     * }</pre>
     *
     * @param jsonArray the {@link JSONArray} whose elements' common class is to be determined
     * @return the common class of the elements, or {@code Object.class} if no common class can be determined
     */
    @Nonnull
    public static Class<?> determineElementClass(JSONArray jsonArray) {
        int length = length(jsonArray);
        Class<?> targetClass = Object.class;
        for (int i = 0; i < length; i++) {
            Object element = jsonArray.opt(i);
            Class<?> elementClass = element.getClass();
            if (i == 0) {
                targetClass = elementClass;
            } else {
                if (elementClass == targetClass || targetClass.isAssignableFrom(elementClass)) {
                } else if (elementClass.isAssignableFrom(targetClass)) {
                    targetClass = elementClass;
                } else {
                    targetClass = Object.class;
                    break;
                }
            }
        }
        return targetClass;
    }


    /**
     * Escapes a string for JSON formatting.
     * <p>
     * This method takes a string and escapes characters that are not allowed in JSON strings,
     * such as quotation marks, reverse solidus, and control characters (U+0000 through U+001F).
     * It also escapes '\u2028' and '\u2029', which JavaScript interprets as newline characters.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String escaped = JSONUtils.escape("Hello\nWorld");  // returns "Hello\\nWorld"
     * String escaped2 = JSONUtils.escape("Quote: \"Hello\"");  // returns "Quote: \\\"Hello\\\""
     * String escaped3 = JSONUtils.escape("Backslash: \\");  // returns "Backslash: \\\\"
     * }</pre>
     *
     * @param v the string to escape, may be {@code null}
     * @return the escaped string, or an empty string if the input is {@code null}
     */
    public static String escape(@Nullable String v) {
        int length = CharSequenceUtils.length(v);
        if (length == 0) {
            return v;
        }

        int afterReplacement = 0;
        StringBuilder builder = null;
        for (int i = 0; i < length; i++) {
            char c = v.charAt(i);
            String replacement;
            if (c < 0x80) {
                replacement = REPLACEMENT_CHARS[c];
                if (replacement == null)
                    continue;
            } else if (c == '\u2028') {
                replacement = U2028;
            } else if (c == '\u2029') {
                replacement = U2029;
            } else {
                continue;
            }
            if (afterReplacement < i) { // write characters between the last replacement
                // and now
                if (builder == null)
                    builder = new StringBuilder(length);
                builder.append(v, afterReplacement, i);
            }
            if (builder == null)
                builder = new StringBuilder(length);
            builder.append(replacement);
            afterReplacement = i + 1;
        }
        if (builder == null) {
            return v; // then we didn't escape anything
        }

        if (afterReplacement < length) {
            builder.append(v, afterReplacement, length);
        }
        return builder.toString();
    }

    static void appendMap(StringBuilder jsonBuilder, Map<String, Object> map) {
        jsonBuilder.append(LEFT_CURLY_BRACE_CHAR);
        Set<Entry<String, Object>> entrySet = map.entrySet();
        int i = 0;
        int size = entrySet.size();
        for (Entry<String, Object> entry : entrySet) {
            String name = entry.getKey();
            Object value = entry.getValue();
            append(jsonBuilder, name, value);
            if (i++ < size - 1) {
                jsonBuilder.append(COMMA_CHAR);
            }
        }
        jsonBuilder.append(RIGHT_CURLY_BRACE_CHAR);
    }

    static void appendIterable(StringBuilder jsonBuilder, Iterable<?> values) {
        jsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
        List<?> list = ofList(values);
        int size = list.size();
        for (int i = 0; i < size; i++) {
            appendValue(jsonBuilder, list.get(i));
            if (i < size - 1) {
                jsonBuilder.append(COMMA_CHAR);
            }
        }
        jsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);
    }

    static void appendString(StringBuilder jsonBuilder, Object value) {
        jsonBuilder.append(DOUBLE_QUOTE_CHAR)
                .append(value)
                .append(DOUBLE_QUOTE_CHAR);
    }

    static void appendType(StringBuilder jsonBuilder, Type value) {
        appendString(jsonBuilder, value.getTypeName());
    }

    static Object convertValue(Object value, Type targetType) {
        if (isNull(value)) {
            return null;
        }
        Class<?> valueClass = asClass(targetType);
        if (isJSONObject(value)) {
            return readValue((JSONObject) value, valueClass);
        } else if (isJSONArray(value)) {
            JSONArray jsonArray = (JSONArray) value;
            return readValues(jsonArray, targetType);
        } else {
            valueClass = tryResolveWrapperType(valueClass);
            Object convertedValue = convertIfPossible(value, valueClass);
            return convertedValue != null ? convertedValue : value;
        }
    }

    static Enumeration<?> toEnumeration(JSONArray jsonArray, Class<?> elementClass) {
        Object[] array = readArray(jsonArray, elementClass);
        return ofEnumeration(array);
    }

    static List<?> toList(JSONArray jsonArray, Class<?> elementClass) {
        List<Object> list = newArrayList(jsonArray.length());
        addValues(jsonArray, list, elementClass);
        return list;
    }

    static Queue<Object> toQueue(JSONArray jsonArray, Class<?> elementClass) {
        Queue<Object> queue = newArrayDeque(jsonArray.length());
        addValues(jsonArray, queue, elementClass);
        return queue;
    }

    static Set<Object> toSet(JSONArray jsonArray, Class<?> elementClass) {
        int length = jsonArray.length();
        Set<Object> sets = newFixedLinkedHashSet(length);
        addValues(jsonArray, sets, elementClass);
        return sets;
    }

    static <C extends Collection> void addValues(JSONArray jsonArray, C collection, Class<?> elementClass) {
        int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            Object value = jsonArray.opt(i);
            value = convertValue(value, elementClass);
            collection.add(value);
        }
    }

    private JSONUtils() {
    }

}
