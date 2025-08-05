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

import static io.microsphere.constants.ResourceConstants.ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_RESOURCE;
import static io.microsphere.util.ClassLoaderUtils.getClassLoader;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ClassPathResourceConfigurationPropertyLoader} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ClassPathResourceConfigurationPropertyLoader
 * @since 1.0.0
 */
class ClassPathResourceConfigurationPropertyLoaderTest {

    @Test
    void testLoadWithSingleResource() throws Throwable {
        ClassPathResourceConfigurationPropertyLoader loader = new ClassPathResourceConfigurationPropertyLoader(ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_RESOURCE) {
        };
        assertConfigurationProperties(loader);
    }

    @Test
    void testLoadWithClassLoader() throws Throwable {
        ClassPathResourceConfigurationPropertyLoader loader = new ClassPathResourceConfigurationPropertyLoader(ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_RESOURCE, getClassLoader(this.getClass())) {
        };
        assertConfigurationProperties(loader);
    }


    @Test
    void testLoadWithAllResources() throws Throwable {
        ClassPathResourceConfigurationPropertyLoader loader = new ClassPathResourceConfigurationPropertyLoader(ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_RESOURCE, true) {
        };
        assertConfigurationProperties(loader);
    }

    void assertConfigurationProperties(ClassPathResourceConfigurationPropertyLoader loader) throws Throwable {
        List<ConfigurationProperty> configurationProperties = loader.load();
        assertEquals(2, configurationProperties.size());
    }
}