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

import io.microsphere.beans.BeanUtils;
import io.microsphere.beans.ConfigurationProperty;
import io.microsphere.json.JSONObject;

import static io.microsphere.json.JSONUtils.writeBeanAsString;
import static io.microsphere.util.Assert.assertNotNull;

/**
 * {@link ConfigurationPropertyGenerator} class based on Java Reflection API
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
 * <p>
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
 * @see ConfigurationPropertyGenerator
 * @see JSONObject
 * @see BeanUtils#resolvePropertiesAsMap(Object)
 * @since 1.0.0
 */
public class ReflectiveConfigurationPropertyGenerator implements ConfigurationPropertyGenerator {

    @Override
    public String generate(ConfigurationProperty configurationProperty) throws IllegalArgumentException {
        assertNotNull(configurationProperty, () -> "The 'ConfigurationProperty' argument must not be null");
        return writeBeanAsString(configurationProperty);
    }
}
