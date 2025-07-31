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

package io.microsphere;

import io.microsphere.beans.ConfigurationProperty;
import io.microsphere.json.JSONObject;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JSONUtils for testing
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class JSONTestUtils {

    public static void assertConfigurationPropertyJSON(String json) throws Throwable {
        JSONObject jsonObject = new JSONObject(json);
        assertEquals("server.port", jsonObject.getString("name"));
        assertEquals("java.lang.Integer", jsonObject.getString("type"));
        assertEquals(8080, jsonObject.getInt("value"));
        assertEquals(8080, jsonObject.getInt("defaultValue"));
        assertEquals(true, jsonObject.getBoolean("required"));
        assertEquals("The port number for the server", jsonObject.getString("description"));
        JSONObject metadata = jsonObject.getJSONObject("metadata");
        assertEquals(1, metadata.getJSONArray("sources").length());
        assertEquals("application.properties", metadata.getJSONArray("sources").getString(0));
        assertEquals(1, metadata.getJSONArray("targets").length());
        assertEquals("server", metadata.getJSONArray("targets").getString(0));
        assertEquals("com.example.ServerConfig", metadata.getString("declaredClass"));
        assertEquals("port", metadata.getString("declaredField"));
    }

    public static ConfigurationProperty newConfigurationProperty() {
        ConfigurationProperty configurationProperty = new ConfigurationProperty("server.port", Integer.class);
        configurationProperty.setValue(8080);
        configurationProperty.setDefaultValue(8080);
        configurationProperty.setRequired(true);
        configurationProperty.setDescription("The port number for the server");

        ConfigurationProperty.Metadata metadata = configurationProperty.getMetadata();
        metadata.getSources().add("application.properties");
        metadata.getTargets().add("server");
        metadata.setDeclaredClass("com.example.ServerConfig");
        metadata.setDeclaredField("port");
        return configurationProperty;
    }
}
