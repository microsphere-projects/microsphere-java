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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static io.microsphere.collection.MapUtils.isEmpty;
import static io.microsphere.constants.SymbolConstants.DOT;
import static java.util.Collections.unmodifiableMap;

/**
 * The utilities class for {@link Properties}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class PropertiesUtils {

    /**
     * Get the flatten the specified {@link Map properties}
     *
     * @param properties the specified {@link Map properties}
     * @return the read-only flatten {@link Map properties} if present
     */
    public static Map<String, Object> flatProperties(Map<String, Object> properties) {
        if (isEmpty(properties)) {
            return properties;
        }
        Map<String, Object> flattenProperties = new LinkedHashMap<>();
        flatProperties(properties, null, flattenProperties);
        return unmodifiableMap(flattenProperties);
    }

    static void flatProperties(Map<String, Object> properties, String propertyNamePrefix,
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

    private PropertiesUtils() {
    }
}
