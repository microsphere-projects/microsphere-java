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
import io.microsphere.json.JSONArray;
import io.microsphere.json.JSONException;
import io.microsphere.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.microsphere.JSONTestUtils.assertConfigurationPropertyJSON;
import static io.microsphere.JSONTestUtils.newConfigurationProperty;
import static io.microsphere.lang.Prioritized.MIN_PRIORITY;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DefaultConfigurationPropertyGenerator} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DefaultConfigurationPropertyGenerator
 * @since 1.0.0
 */
class DefaultConfigurationPropertyGeneratorTest {

    private DefaultConfigurationPropertyGenerator generator;

    @BeforeEach
    void setUp() {
        this.generator = new DefaultConfigurationPropertyGenerator();
    }

    @Test
    void testGenerate() throws JSONException {
        ConfigurationProperty configurationProperty = newConfigurationProperty();
        String json = generator.generate(configurationProperty);
        assertConfigurationPropertyJSON(json);
    }

    @Test
    void testGenerateWithConfigurationProperty() throws JSONException {
        ConfigurationProperty configurationProperty = new ConfigurationProperty("server.port", Integer.class);
        String json = generator.generate(configurationProperty);
        JSONObject jsonObject = new JSONObject(json);
        assertEquals("server.port", jsonObject.getString("name"));
        assertEquals("java.lang.Integer", jsonObject.getString("type"));

        configurationProperty.setValue(8080);
        json = generator.generate(configurationProperty);
        jsonObject = new JSONObject(json);
        assertEquals("server.port", jsonObject.getString("name"));
        assertEquals("java.lang.Integer", jsonObject.getString("type"));
        assertEquals(8080, jsonObject.getInt("value"));

        configurationProperty.setDefaultValue(8080);
        json = generator.generate(configurationProperty);
        jsonObject = new JSONObject(json);
        assertEquals("server.port", jsonObject.getString("name"));
        assertEquals("java.lang.Integer", jsonObject.getString("type"));
        assertEquals(8080, jsonObject.getInt("value"));
        assertEquals(8080, jsonObject.getInt("defaultValue"));

        configurationProperty.setRequired(true);
        json = generator.generate(configurationProperty);
        jsonObject = new JSONObject(json);
        assertEquals("server.port", jsonObject.getString("name"));
        assertEquals("java.lang.Integer", jsonObject.getString("type"));
        assertEquals(8080, jsonObject.getInt("value"));
        assertEquals(8080, jsonObject.getInt("defaultValue"));
        assertTrue(jsonObject.getBoolean("required"));

        configurationProperty.setDescription("The server port to listen on");
        json = generator.generate(configurationProperty);
        jsonObject = new JSONObject(json);
        assertEquals("server.port", jsonObject.getString("name"));
        assertEquals("java.lang.Integer", jsonObject.getString("type"));
        assertEquals(8080, jsonObject.getInt("value"));
        assertEquals(8080, jsonObject.getInt("defaultValue"));
        assertTrue(jsonObject.getBoolean("required"));
        assertEquals("The server port to listen on", jsonObject.getString("description"));
    }

    @Test
    void testGenerateWithMetadata() throws JSONException {
        Metadata metadata = new Metadata();
        String json = generator.generate(metadata);
        JSONObject jsonObject = new JSONObject(json);
        assertEquals("{}", json);
        assertNull(jsonObject.opt("sources"));
        assertNull(jsonObject.opt("targets"));
        assertNull(jsonObject.opt("declaredClass"));
        assertNull(jsonObject.opt("declaredField"));

        metadata.getSources().add("application.properties");
        json = generator.generate(metadata);
        jsonObject = new JSONObject(json);
        assertEquals(new JSONArray(ofArray("application.properties")), jsonObject.getJSONArray("sources"));

        metadata.getTargets().add("server-1");
        metadata.getTargets().add("server-2");
        json = generator.generate(metadata);
        jsonObject = new JSONObject(json);
        assertEquals(new JSONArray(ofArray("application.properties")), jsonObject.getJSONArray("sources"));
        assertEquals(new JSONArray(ofArray("server-1", "server-2")), jsonObject.getJSONArray("targets"));

        metadata.setDeclaredClass("io.microsphere.ConfigurationProperty");
        json = generator.generate(metadata);
        jsonObject = new JSONObject(json);
        assertEquals(new JSONArray(ofArray("application.properties")), jsonObject.getJSONArray("sources"));
        assertEquals(new JSONArray(ofArray("server-1", "server-2")), jsonObject.getJSONArray("targets"));
        assertEquals("io.microsphere.ConfigurationProperty", jsonObject.getString("declaredClass"));

        metadata.setDeclaredField("configurationProperties");
        json = generator.generate(metadata);
        jsonObject = new JSONObject(json);
        assertEquals(new JSONArray(ofArray("application.properties")), jsonObject.getJSONArray("sources"));
        assertEquals(new JSONArray(ofArray("server-1", "server-2")), jsonObject.getJSONArray("targets"));
        assertEquals("io.microsphere.ConfigurationProperty", jsonObject.getString("declaredClass"));
        assertEquals("configurationProperties", jsonObject.getString("declaredField"));
    }

    @Test
    void testGetPriority() {
        int priority = generator.getPriority();
        assertEquals(MIN_PRIORITY, priority);
    }
}