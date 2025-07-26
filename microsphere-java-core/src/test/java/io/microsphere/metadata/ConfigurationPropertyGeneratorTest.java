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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.microsphere.constants.PropertyConstants.MICROSPHERE_PROPERTY_NAME_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ConfigurationPropertyGenerator} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationPropertyGenerator
 * @since 1.0.0
 */
class ConfigurationPropertyGeneratorTest {

    private ConfigurationPropertyGenerator configurationPropertyGenerator;

    @BeforeEach
    void setUp() {
        this.configurationPropertyGenerator = new ConfigurationPropertyGenerator() {
            @Override
            public ConfigurationProperty generate() throws Throwable {
                return newConfigurationProperty();
            }
        };
    }

    @Test
    void testGenerate() throws Throwable {
        assertEquals(newConfigurationProperty(), configurationPropertyGenerator.generate());
    }

    ConfigurationProperty newConfigurationProperty() {
        return new ConfigurationProperty(MICROSPHERE_PROPERTY_NAME_PREFIX + "test");
    }
}