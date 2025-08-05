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
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.constants.PropertyConstants.MICROSPHERE_PROPERTY_NAME_PREFIX;
import static io.microsphere.metadata.ConfigurationPropertyLoader.loadAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ConfigurationPropertyLoader} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationPropertyLoader
 * @since 1.0.0
 */
class ConfigurationPropertyLoaderTest {

    @Test
    void testLoad() throws Throwable {
        assertEquals(ofList(newConfigurationProperty()), new DefaultConfigurationPropertyLoader().load());
    }

    @Test
    void testLoadAll() {
        List<ConfigurationProperty> configurationProperties = loadAll();
        assertEquals(3, configurationProperties.size());
        assertEquals(newConfigurationProperty(), configurationProperties.get(2));
    }

    ConfigurationProperty newConfigurationProperty() {
        return new ConfigurationProperty(MICROSPHERE_PROPERTY_NAME_PREFIX + "test");
    }
}