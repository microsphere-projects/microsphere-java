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

import java.io.InputStream;
import java.util.List;

import static io.microsphere.constants.ResourceConstants.ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_RESOURCE;
import static io.microsphere.lang.Prioritized.MIN_PRIORITY;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link DefaultConfigurationPropertyReader} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DefaultConfigurationPropertyReader
 * @since 1.0.0
 */
class DefaultConfigurationPropertyReaderTest {

    private DefaultConfigurationPropertyReader configurationPropertyReader;

    @BeforeEach
    void setUp() {
        this.configurationPropertyReader = new DefaultConfigurationPropertyReader();
    }

    @Test
    void testRead() throws Throwable {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_RESOURCE)) {
            List<ConfigurationProperty> configurationProperties = this.configurationPropertyReader.read(inputStream);
            assertEquals(2, configurationProperties.size());
        }

    }

    @Test
    void testGetPriority() {
        assertEquals(MIN_PRIORITY, configurationPropertyReader.getPriority());
    }
}