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
package io.microsphere.collection;

import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.util.Utils;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static io.microsphere.collection.MapUtils.isEmpty;
import static io.microsphere.collection.MapUtils.newLinkedHashMap;
import static io.microsphere.constants.SeparatorConstants.FILE_SEPARATOR;
import static io.microsphere.constants.SeparatorConstants.LINE_SEPARATOR;
import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.lang.function.ThrowableAction.execute;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.StringUtils.arrayToString;
import static java.util.Collections.unmodifiableMap;

/**
 * The utilities class for {@link Properties}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class PropertiesUtils implements Utils {

    /**
     * Flattens a nested map of properties into a single-level map.
     *
     * <p>If the input map is empty or null, the same map instance is returned.</p>
     *
     * <p>For example, given the following input:
     * <pre>{@code
     * {
     *   "a": "1",
     *   "b": {
     *     "c": "2",
     *     "d": {
     *       "e": "3"
     *     }
     *   }
     * }
     * }</pre>
     * The resulting flattened map would be:
     * <pre>{@code
     * {
     *   "a": "1",
     *   "b.c": "2",
     *   "b.d.e": "3"
     * }
     * }</pre>
     *
     * @param properties The map containing potentially nested properties to be flattened.
     * @return A new unmodifiable map with all properties flattened to a single level.
     */
    @Nonnull
    @Immutable
    public static Map<String, Object> flatProperties(Map<String, Object> properties) {
        if (isEmpty(properties)) {
            return properties;
        }
        LinkedHashMap<String, Object> flattenProperties = newLinkedHashMap();
        flatProperties(properties, null, flattenProperties);
        return unmodifiableMap(flattenProperties);
    }

    /**
     * Recursively flattens the given properties map into a single-level map.
     *
     * @param properties         The map containing properties to be flattened.
     * @param propertyNamePrefix The prefix for property names used during flattening.
     * @param flattenProperties  The target map where flattened properties are stored.
     */
    protected static void flatProperties(Map<String, Object> properties, String propertyNamePrefix,
                                         Map<String, Object> flattenProperties) {
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String propertyName = normalizePropertyName(propertyNamePrefix, entry.getKey());
            Object propertyValue = entry.getValue();
            if (propertyValue instanceof String) {
                flattenProperties.put(propertyName, propertyValue);
            } else if (propertyValue instanceof Map) {
                Map subProperties = (Map) propertyValue;
                flatProperties(subProperties, propertyName, flattenProperties);
            }
        }
    }

    private static String normalizePropertyName(String propertyNamePrefix, String propertyName) {
        return propertyNamePrefix == null ? propertyName : propertyNamePrefix + DOT + propertyName;
    }

    /**
     * Creates a new empty {@link Properties} instance.
     *
     * <p>This method provides a convenient way to create an empty properties object.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Properties props = PropertiesUtils.newProperties();
     *     System.out.println(props.isEmpty()); // Output: true
     *
     *     props.setProperty("key", "value");
     *     System.out.println(props); // Output: {key=value}
     * }</pre>
     *
     * @return a new, empty {@link Properties}
     */
    @Nonnull
    public static Properties newProperties() {
        return new Properties();
    }

    /**
     * Creates a new {@link Properties} instance with the specified defaults.
     *
     * <p>This method provides a convenient way to create a properties object with default properties.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     Properties defaults = new Properties();
     *     defaults.setProperty("default.key", "default.value");
     *     Properties props = PropertiesUtils.newProperties(defaults);
     *     System.out.println(props.getProperty("default.key")); // Output: default.value
     * }</pre>
     *
     * @param defaults the default properties
     * @return a new {@link Properties} with the specified defaults
     */
    @Nonnull
    public static Properties newProperties(Properties defaults) {
        return new Properties(defaults);
    }

    /**
     * Loads properties from the given string values.
     *
     * <p>The provided string values are joined using the system file separator to form a single content string,
     * which is then parsed as a standard Java properties format.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     // Load properties from multiple lines
     *     Properties props = PropertiesUtils.loadProperties(
     *         "key1=value1",
     *         "key2=value2"
     *     );
     *     System.out.println(props.getProperty("key1")); // Output: value1
     *     System.out.println(props.getProperty("key2")); // Output: value2
     *
     *     // Load empty properties if no arguments are provided
     *     Properties emptyProps = PropertiesUtils.loadProperties();
     *     System.out.println(emptyProps.isEmpty()); // Output: true
     * }</pre>
     *
     * @param propertiesValue the string values representing properties content
     * @return a new {@link Properties} instance loaded from the given values
     */
    @Nonnull
    public static Properties loadProperties(String... propertiesValue) {
        int length = length(propertiesValue);
        Properties properties = newProperties();
        if (length > 0) {
            String content = arrayToString(propertiesValue, LINE_SEPARATOR);
            execute(() -> {
                try (StringReader reader = new StringReader(content)) {
                    properties.load(reader);
                }
            });
        }
        return properties;
    }

    private PropertiesUtils() {
    }
}
