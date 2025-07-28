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
import io.microsphere.beans.ConfigurationProperty.Metadata;

import java.util.Set;

import static io.microsphere.constants.SymbolConstants.COMMA_CHAR;
import static io.microsphere.constants.SymbolConstants.LEFT_CURLY_BRACE_CHAR;
import static io.microsphere.constants.SymbolConstants.RIGHT_CURLY_BRACE_CHAR;
import static io.microsphere.json.JSONUtils.append;
import static io.microsphere.json.JSONUtils.appendName;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.ClassUtils.getTypeName;

/**
 * The default implementation class of {@link ConfigurationPropertyJSONGenerator}.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * ConfigurationProperty property = new ConfigurationProperty("server.port", Integer.class);
 * property.setValue(8080);
 * property.setDefaultValue(8080);
 * property.setRequired(true);
 * property.setDescription("The port number for the server");
 *
 * ConfigurationProperty.Metadata metadata = property.getMetadata();
 * metadata.getSources().add("application.properties");
 * metadata.getTargets().add("server");
 * metadata.setDeclaredClass("com.example.ServerConfig");
 * metadata.setDeclaredField("port");
 * }</pre>
 *
 * The JSON representation of the above ConfigurationProperty object would be:
 * <pre>{@code
 * {
 *   "name": "server.port",
 *   "type": "java.lang.Integer",
 *   "value": 8080,
 *   "defaultValue": 8080,
 *   "required": true,
 *   "description": "The port number for the server",
 *   "metadata": {
 *     "sources": [
 *       "application.properties"
 *     ],
 *     "targets": [
 *       "server"
 *     ],
 *     "declaredClass": "com.example.ServerConfig",
 *     "declaredField": "port"
 *   }
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationPropertyJSONGenerator
 * @see ConfigurationProperty
 * @since 1.0.0
 */
public class DefaultConfigurationPropertyJSONGenerator implements ConfigurationPropertyJSONGenerator {

    @Override
    public String generate(ConfigurationProperty configurationProperty) {
        assertNotNull(configurationProperty, () -> "The 'ConfigurationProperty' argument must not be null");

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append(LEFT_CURLY_BRACE_CHAR);

        String name = configurationProperty.getName();
        append(jsonBuilder, "name", name);

        String type = getTypeName(configurationProperty.getType());
        jsonBuilder.append(COMMA_CHAR);
        append(jsonBuilder, "type", type);

        Object value = configurationProperty.getValue();
        if (value != null) {
            jsonBuilder.append(COMMA_CHAR);
            append(jsonBuilder, "value", value);
        }

        Object defaultValue = configurationProperty.getDefaultValue();
        if (defaultValue != null) {
            jsonBuilder.append(COMMA_CHAR);
            append(jsonBuilder, "defaultValue", defaultValue);
        }

        jsonBuilder.append(COMMA_CHAR);
        append(jsonBuilder, "required", configurationProperty.isRequired());

        String description = configurationProperty.getDescription();
        if (description != null) {
            jsonBuilder.append(COMMA_CHAR);
            append(jsonBuilder, "description", description);
        }

        jsonBuilder.append(COMMA_CHAR);
        appendName(jsonBuilder, "metadata");
        jsonBuilder.append(generate(configurationProperty.getMetadata()));

        jsonBuilder.append(RIGHT_CURLY_BRACE_CHAR);
        return jsonBuilder.toString();
    }

    String generate(Metadata metadata) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append(LEFT_CURLY_BRACE_CHAR);

        Set<String> sources = metadata.getSources();
        append(jsonBuilder, "sources", sources);

        Set<String> targets = metadata.getTargets();
        jsonBuilder.append(COMMA_CHAR);
        append(jsonBuilder, "targets", targets);

        String declaredClass = metadata.getDeclaredClass();
        if (declaredClass != null) {
            jsonBuilder.append(COMMA_CHAR);
            append(jsonBuilder, "declaredClass", declaredClass);
        }

        String declaredField = metadata.getDeclaredField();
        if (declaredField != null) {
            jsonBuilder.append(COMMA_CHAR);
            append(jsonBuilder, "declaredField", declaredField);
        }

        jsonBuilder.append(RIGHT_CURLY_BRACE_CHAR);
        return jsonBuilder.toString();
    }
}
