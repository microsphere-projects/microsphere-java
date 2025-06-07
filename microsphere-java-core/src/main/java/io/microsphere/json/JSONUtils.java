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

import io.microsphere.util.Utils;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static io.microsphere.collection.ListUtils.ofList;
import static io.microsphere.constants.SymbolConstants.COLON_CHAR;
import static io.microsphere.constants.SymbolConstants.COMMA_CHAR;
import static io.microsphere.constants.SymbolConstants.DOUBLE_QUOTE_CHAR;
import static io.microsphere.constants.SymbolConstants.LEFT_CURLY_BRACE_CHAR;
import static io.microsphere.constants.SymbolConstants.LEFT_SQUARE_BRACKET_CHAR;
import static io.microsphere.constants.SymbolConstants.RIGHT_CURLY_BRACE_CHAR;
import static io.microsphere.constants.SymbolConstants.RIGHT_SQUARE_BRACKET_CHAR;
import static io.microsphere.util.StringUtils.isNotBlank;
import static java.lang.reflect.Array.get;
import static java.lang.reflect.Array.getLength;

/**
 * The utility class for JSON
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
