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

package io.microsphere.annotation.processor;

import io.microsphere.beans.ConfigurationProperty;
import io.microsphere.beans.ConfigurationProperty.Metadata;
import io.microsphere.metadata.ConfigurationPropertyLoader;

import java.util.List;
import java.util.Random;

import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.util.ClassUtils.SIMPLE_TYPES;

/**
 * {@link ConfigurationPropertyLoader} for testing
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationPropertyLoader
 * @since 1.0.0
 */
public class TestConfigurationPropertyLoader implements ConfigurationPropertyLoader {

    @Override
    public List<ConfigurationProperty> load() throws Throwable {

        int size = SIMPLE_TYPES.size();
        List<ConfigurationProperty> configurationProperties = newArrayList(size);

        Random random = new Random();

        int i = 1;
        for (Class<?> simpleType : SIMPLE_TYPES) {
            String name = "test-name-" + i;
            ConfigurationProperty configurationProperty = new ConfigurationProperty(name);
            configurationProperty.setType(simpleType);
            configurationProperty.setValue("test-value-" + i);
            configurationProperty.setDefaultValue("default-test-value-" + i);
            configurationProperty.setRequired(true);
            configurationProperty.setDescription("This is a test configuration property for " + simpleType.getSimpleName());

            Metadata metadata = configurationProperty.getMetadata();
            for (int j = 1; j < random.nextInt(9); j++) {
                metadata.getSources().add("test-source-" + j);
            }
            for (int j = 1; j < random.nextInt(9); j++) {
                metadata.getTargets().add("test-target-" + j);
            }
            metadata.setDeclaredClass("io.microsphere.annotation.processor.TestConfigurationPropertyLoader");
            metadata.setDeclaredField("configurationProperties");
            configurationProperties.add(configurationProperty);
            i++;
        }
        return configurationProperties;
    }
}
