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
import io.microsphere.logging.Logger;
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
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.reflect.TypeUtils.asClass;
import static io.microsphere.reflect.TypeUtils.asParameterizedType;
import static io.microsphere.util.ClassUtils.isArray;
import static io.microsphere.util.ClassUtils.tryResolveWrapperType;
import static io.microsphere.util.ExceptionUtils.wrap;
import static io.microsphere.util.IterableUtils.isIterable;
import static io.microsphere.util.StringUtils.isNotBlank;
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

    private static final Logger logger = getLogger(JSONUtils.class);

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
     * @param targetType the class of the target type to which the JSON should be converted
     * @param <V>        the type of the target object
     * @return an instance of the target type populated with data from the {@code JSONObject}
     * @throws IllegalArgumentException if the {@code JSONObject} cannot be converted to the target type
     * @see JSONObject
     * @see #readValue(String, Class)
     */
    @Nonnull
    public static <V> V readValue(JSONObject jsonObject, Class<V> targetType) {
        BeanMetadata beanMetadata = getBeanMetadata(targetType);
        V valueObject = ClassUtils.newInstance(targetType);
        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = jsonObject.opt(key);
            if (value != null) {
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
     * @param json         the JSON array string to parse and convert
     * @param multipleType the class of the target collection or array type to which the JSON should be converted
     * @param elementType  the class of the elements in the target collection or array
     * @return an instance of the target collection or array type populated with data from the JSON array string,
     * or {@code null} if the target type is not supported
     * @throws IllegalArgumentException if the JSON string is invalid or cannot be parsed into a {@code JSONArray}
     * @see JSONArray
     * @see #readValues(JSONArray, Class, Class)
     */
    @Nullable
    public static <V> V readValues(String json, Class<V> multipleType, Class<?> elementType) {
        return readValues(jsonArray(json), multipleType, elementType);
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
     * @param jsonArray    the {@code JSONArray} to parse and convert
     * @param multipleType the class of the target collection or array type to which the JSON should be converted
     * @param elementType  the class of the elements in the target collection or array
     * @return an instance of the target collection or array type populated with data from the {@code JSONArray},
     * or {@code null} if the target type is not supported
     * @see JSONArray
     * @see #readValues(String, Class, Class)
     */
    @Nullable
    public static <V> V readValues(JSONArray jsonArray, Class<V> multipleType, Class<?> elementType) {
        if (isArray(multipleType)) {
            return (V) readArray(jsonArray, multipleType.getComponentType());
        } else if (isList(multipleType)) {
            return (V) toList(jsonArray, elementType);
        } else if (isSet(multipleType)) {
            return (V) toSet(jsonArray, elementType);
        } else if (isQueue(multipleType)) {
            return (V) toQueue(jsonArray, elementType);
        } else if (isEnumeration(multipleType)) {
            return (V) toEnumeration(jsonArray, elementType);
        } else if (isIterable(multipleType)) {
            return (V) toList(jsonArray, elementType);
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
        if (wrapper instanceof JSONObject || wrapper instanceof JSONArray) {
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
        if (value == null) {
            return null;
        }
        Class<?> valueClass = asClass(targetType);
        if (value instanceof JSONObject) {
            return readValue((JSONObject) value, valueClass);
        } else if (value instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) value;
            ParameterizedType parameterizedType = asParameterizedType(targetType);
            Class<?> elementType;
            if (parameterizedType == null) {
                elementType = Object.class;
            } else {
                elementType = asClass(parameterizedType.getActualTypeArguments()[0]);
            }
            return readValues(jsonArray, valueClass, elementType);
        } else {
            valueClass = tryResolveWrapperType(valueClass);
            Object convertedValue = convertIfPossible(value, valueClass);
            return convertedValue != null ? convertedValue : value;
        }
    }

    static Enumeration<?> toEnumeration(JSONArray jsonArray, Class<?> elementType) {
        Object[] array = readArray(jsonArray, elementType);
        return ofEnumeration(array);
    }

    static List<?> toList(JSONArray jsonArray, Class<?> elementType) {
        List<Object> list = newArrayList(jsonArray.length());
        addValues(jsonArray, list, elementType);
        return list;
    }

    static Queue<Object> toQueue(JSONArray jsonArray, Class<?> elementType) {
        Queue<Object> queue = newArrayDeque(jsonArray.length());
        addValues(jsonArray, queue, elementType);
        return queue;
    }

    static Set<Object> toSet(JSONArray jsonArray, Class<?> elementType) {
        int length = jsonArray.length();
        Set<Object> sets = newFixedLinkedHashSet(length);
        addValues(jsonArray, sets, elementType);
        return sets;
    }

    static <C extends Collection> void addValues(JSONArray jsonArray, C collection, Class<?> elementType) {
        int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            Object value = jsonArray.opt(i);
            value = convertValue(value, elementType);
            collection.add(value);
        }
    }

    private JSONUtils() {
    }

}
