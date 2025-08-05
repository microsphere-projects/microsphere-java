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

import java.util.List;

import static io.microsphere.lang.Prioritized.MIN_PRIORITY;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link MetadataResourceConfigurationPropertyLoader} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MetadataResourceConfigurationPropertyLoader
 * @since 1.0.0
 */
class MetadataResourceConfigurationPropertyLoaderTest {

    private MetadataResourceConfigurationPropertyLoader configurationPropertyLoader;

    @BeforeEach
    void setUp() {
        this.configurationPropertyLoader = new MetadataResourceConfigurationPropertyLoader();
    }

    @Test
    void testLoad() throws Throwable {
        List<ConfigurationProperty> configurationProperties = configurationPropertyLoader.load();
        assertEquals(6, configurationProperties.size());
    }

    @Test
    void testGetPriority() {
        assertEquals(MIN_PRIORITY, configurationPropertyLoader.getPriority());
    }
}