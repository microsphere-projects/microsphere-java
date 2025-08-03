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
import io.microsphere.util.ClassUtils;
import io.microsphere.util.Utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

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
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.util.ClassUtils.isArray;
import static io.microsphere.util.ClassUtils.tryResolveWrapperType;
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


    public static <V> V readValue(String json, Class<V> valueType) {
        return readValue(execute(() -> new JSONObject(json)), valueType);
    }

    public static <V> V readValue(JSONObject jsonObject, Class<V> valueType) {
        BeanMetadata beanMetadata = getBeanMetadata(valueType);
        V valueObject = ClassUtils.newInstance(valueType);
        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = jsonObject.opt(key);
            if (value != null) {
                PropertyDescriptor propertyDescriptor = beanMetadata.getPropertyDescriptor(key);
                Method writeMethod = propertyDescriptor.getWriteMethod();
                if (writeMethod == null) {
                    continue;
                }
                Class<?> propertyType = propertyDescriptor.getPropertyType();
                Object convertedValue = null;
                if (value instanceof JSONObject) {
                    convertedValue = readValue((JSONObject) value, propertyType);
                } else if (value instanceof JSONArray) {
                    JSONArray jsonArray = (JSONArray) value;
                    int length = jsonArray.length();

                } else {
                    propertyType = tryResolveWrapperType(propertyType);
                    convertedValue = convertIfPossible(value, propertyType);
                }
                if (convertedValue != null) {
                    invokeMethod(valueObject, writeMethod, convertedValue);
                }
            }
        }
        return valueObject;
    }

    public static Object readValue(Object value) {
        if (value == null) {
            return null;
        }
        return readValue(value, value.getClass());
    }

    public static Object readValue(Object value, Type valueType) {
        Object resolvedValue = null;
        if (value instanceof JSONObject) {
            resolvedValue = readValue((JSONObject) value, valueType);
        } else if (value instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) value;
            resolvedValue = readValues(jsonArray, valueType, valueType);
        } else {
            valueType = tryResolveWrapperType(valueType);
            resolvedValue = convertIfPossible(value, valueType);
        }
        return resolvedValue;
    }

    public static Object readValues(JSONArray jsonArray, Class<?> multipleType, Class<?> elementType) {
        if (isArray(multipleType)) {
            return readArray(jsonArray, elementType);
        } else if (isList(multipleType)) {
            return readList(jsonArray, elementType);
        } else if (isSet(multipleType)) {
            return readSet(jsonArray, elementType);
        } else if (isQueue(multipleType)) {
            return readQueue(jsonArray, elementType);
        } else if (isEnumeration(multipleType)) {
            return readEnumeration(jsonArray, elementType);
        }
        return null;
    }

    private static Object readEnumeration(JSONArray jsonArray, Class<?> elementType) {
        Object[] array = readArray(jsonArray, elementType);
        return ofEnumeration(array);
    }

    private static Object readList(JSONArray jsonArray, Class<?> elementType) {
        List<Object> list = newArrayList(jsonArray.length());
        addValues(jsonArray, list, elementType);
        return list;
    }

    private static Queue<Object> readQueue(JSONArray jsonArray, Class<?> elementType) {
        Queue<Object> queue = newArrayDeque(jsonArray.length());
        addValues(jsonArray, queue, elementType);
        return queue;
    }

    static Set<Object> readSet(JSONArray jsonArray, Class<?> elementType) {
        int length = jsonArray.length();
        Set<Object> sets = newFixedLinkedHashSet(length);
        addValues(jsonArray, sets, elementType);
        return sets;
    }

    static <C extends Collection> void addValues(JSONArray jsonArray, C collection, Class<?> elementType) {
        int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            Object value = jsonArray.opt(i);
            value = readValue(value, elementType);
            collection.add(value);
        }
    }


    public static <E, C extends Iterable<E>> C readValues(String json, Class<C> collectionType, Class<E> elementType) {
        return null;
    }

    public static <E> E[] readArray(JSONArray jsonArray, Class<E> componentType) {
        int length = jsonArray.length();
        E[] array = (E[]) newInstance(componentType, length);
        for (int i = 0; i < length; i++) {
            Object value = jsonArray.opt(i);
            value = readValue(value, componentType);
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


    private JSONUtils() {
    }

}
