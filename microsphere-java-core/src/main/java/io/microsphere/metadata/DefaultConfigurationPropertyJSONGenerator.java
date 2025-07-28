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

package io.microsphere.metadata;

import io.microsphere.beans.ConfigurationProperty;

import static io.microsphere.constants.SymbolConstants.LEFT_CURLY_BRACE_CHAR;
import static io.microsphere.constants.SymbolConstants.RIGHT_CURLY_BRACE_CHAR;
import static io.microsphere.json.JSONUtils.append;
import static io.microsphere.util.ClassUtils.getTypeName;

/**
 * The default implementation class of {@link ConfigurationPropertyJSONGenerator}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationPropertyJSONGenerator
 * @see ConfigurationProperty
 * @since 1.0.0
 */
public class DefaultConfigurationPropertyJSONGenerator {

    public String generate(ConfigurationProperty configurationProperty) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append(LEFT_CURLY_BRACE_CHAR);

        String name = configurationProperty.getName();
        append(jsonBuilder, "name", name);

        String type = getTypeName(configurationProperty.getType());
        append(jsonBuilder, "type", type);

        Object value = configurationProperty.getValue();
        if (value != null) {
            append(jsonBuilder, "value", value);
        }

        Object defaultValue = configurationProperty.getDefaultValue();
        if (defaultValue != null) {
            append(jsonBuilder, "defaultValue", defaultValue);
        }

        append(jsonBuilder, "required", configurationProperty.isRequired());

        String description = configurationProperty.getDescription();
        if (description != null) {
            append(jsonBuilder, "description", description);
        }

        ConfigurationProperty.Metadata metadata = configurationProperty.getMetadata();


        jsonBuilder.append(RIGHT_CURLY_BRACE_CHAR);

        return jsonBuilder.toString();
    }
}
