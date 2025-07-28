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

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.beans.ConfigurationProperty;
import io.microsphere.logging.Logger;

import java.util.List;

import static io.microsphere.collection.CollectionUtils.isNotEmpty;
import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ClassUtils.getTypeName;
import static io.microsphere.util.ServiceLoaderUtils.loadServicesList;
import static java.util.Collections.unmodifiableList;

/**
 * The Java SPI of the loader of {@link ConfigurationProperty} that will be {@link #loadAll loaded by}
 * Microsphere Annotation Processor module.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationProperty
 * @see io.microsphere.annotation.ConfigurationProperty
 * @since 1.0.0
 */
public interface ConfigurationPropertyLoader {

    /**
     * Loads a list of {@link ConfigurationProperty} instances.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ConfigurationPropertyLoader loader = ...; // Obtain an instance of a concrete implementation
     * List<ConfigurationProperty> properties = loader.generate();
     * for (ConfigurationProperty property : properties) {
     *     System.out.println("Property Name: " + property.getName());
     *     System.out.println("Property Type: " + property.getType());
     *     System.out.println("Property Value: " + property.getValue());
     * }
     * }</pre>
     *
     * @return a list of loaded {@link ConfigurationProperty} instances, or an empty list or <code>null</code> if no properties are loaded
     * @throws Throwable if any error occurs during generation
     * @see ConfigurationProperty
     */
    @Nullable
    List<ConfigurationProperty> load() throws Throwable;

    /**
     * Loads all {@link ConfigurationProperty} instances by loading and executing all available
     * {@link ConfigurationPropertyLoader} services via Java SPI mechanism.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<ConfigurationProperty> properties = ConfigurationPropertyLoader.generateAll();
     * for (ConfigurationProperty property : properties) {
     *     System.out.println("Property Name: " + property.getName());
     *     System.out.println("Property Type: " + property.getType());
     *     System.out.println("Property Value: " + property.getValue());
     * }
     * }</pre>
     *
     * @return a list of loaded {@link ConfigurationProperty} instances, or an empty list if no loaders are found
     * @throws Throwable if any error occurs during generation
     * @see #load()
     * @see io.microsphere.util.ServiceLoaderUtils#loadServicesList(Class)
     */
    @Nonnull
    static List<ConfigurationProperty> loadAll() {
        Logger logger = getLogger(ConfigurationPropertyLoader.class);
        List<ConfigurationPropertyLoader> loaders = loadServicesList(ConfigurationPropertyLoader.class);
        List<ConfigurationProperty> configurationProperties = newLinkedList();
        for (ConfigurationPropertyLoader loader : loaders) {
            try {
                List<ConfigurationProperty> loadedProperties = loader.load();
                if (isNotEmpty(loadedProperties)) {
                    configurationProperties.addAll(loadedProperties);
                }
            } catch (Throwable e) {
                logger.error("Failed to load the instances of configuration property via {}", getTypeName(loader.getClass()), e);
            }
        }
        return unmodifiableList(configurationProperties);
    }
}
