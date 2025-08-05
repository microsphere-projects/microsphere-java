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

import java.util.List;
import java.util.Map;

import static io.microsphere.collection.CollectionUtils.addAll;
import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.json.JSONUtils.readValues;
import static java.util.Collections.unmodifiableList;

/**
 * {@link ConfigurationPropertyReader} based on JSON content.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationPropertyReader
 * @since 1.0.0
 */
public class DefaultConfigurationPropertyReader implements ConfigurationPropertyReader {

    @Override
    public List<ConfigurationProperty> load(String content) throws Throwable {
        List<Map<String, Object>> configurationPropertiesMaps = readValues(content, List.class, Map.class);
        int size = configurationPropertiesMaps.size();
        List<ConfigurationProperty> configurationProperties = newArrayList(size);
        for (int i = 0; i < size; i++) {
            Map<String, Object> configurationPropertyMap = configurationPropertiesMaps.get(i);
            ConfigurationProperty configurationProperty = readConfigurationProperty(configurationPropertyMap);
            configurationProperties.add(configurationProperty);
        }
        return unmodifiableList(configurationProperties);
    }

    @Override
    public int getPriority() {
        return MIN_PRIORITY;
    }

    ConfigurationProperty readConfigurationProperty(Map<String, Object> configurationPropertyMap) {
        String name = (String) configurationPropertyMap.get("name");
        String type = (String) configurationPropertyMap.get("type");
        Object value = configurationPropertyMap.get("value");
        Object defaultValue = configurationPropertyMap.get("defaultValue");
        boolean required = (Boolean) configurationPropertyMap.get("required");
        String description = (String) configurationPropertyMap.get("description");
        ConfigurationProperty configurationProperty = new ConfigurationProperty(name);
        configurationProperty.setType(type);
        configurationProperty.setValue(value);
        configurationProperty.setDefaultValue(defaultValue);
        configurationProperty.setRequired(required);
        configurationProperty.setDescription(description);

        Metadata metadata = configurationProperty.getMetadata();
        initMetadata(metadata, configurationPropertyMap);
        return configurationProperty;
    }

    void initMetadata(Metadata metadata, Map<String, Object> configurationPropertyMap) {
        Map<String, Object> metadataMap = (Map<String, Object>) configurationPropertyMap.get("metadata");
        String[] sources = (String[]) metadataMap.get("sources");
        String[] targets = (String[]) metadataMap.get("targets");
        String declaredClass = (String) metadataMap.get("declaredClass");
        String declaredField = (String) metadataMap.get("declaredField");

        addAll(metadata.getSources(), sources);
        addAll(metadata.getSources(), targets);
        metadata.setDeclaredClass(declaredClass);
        metadata.setDeclaredField(declaredField);
    }
}
