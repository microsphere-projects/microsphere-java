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
import io.microsphere.lang.function.ThrowableSupplier;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.util.Assert.assertNotEmpty;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static java.util.Collections.unmodifiableList;

/**
 * {@link ConfigurationPropertyLoader} to load the target Class-Path resource.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationPropertyLoader
 * @see ConfigurationPropertyReader
 * @see DefaultConfigurationPropertyReader
 * @since 1.0.0
 */
public abstract class ClassPathResourceConfigurationPropertyLoader implements ConfigurationPropertyLoader {

    protected final String resourceName;

    protected final ClassLoader classLoader;

    protected final boolean loadedAll;

    protected final ConfigurationPropertyReader configurationPropertyReader;

    protected ClassPathResourceConfigurationPropertyLoader(String resourceName) throws IllegalArgumentException {
        this(resourceName, false);
    }

    protected ClassPathResourceConfigurationPropertyLoader(@Nonnull String resourceName, @Nullable ClassLoader classLoader) {
        this(resourceName, classLoader, false);
    }

    protected ClassPathResourceConfigurationPropertyLoader(@Nonnull String resourceName, boolean loadedAll) {
        this(resourceName, null, loadedAll);
    }

    protected ClassPathResourceConfigurationPropertyLoader(@Nonnull String resourceName, @Nullable ClassLoader classLoader, boolean loadedAll) {
        assertNotEmpty(resourceName, () -> "The Class-Path resource must not be empty: " + resourceName);
        this.resourceName = resourceName;
        this.classLoader = classLoader == null ? getDefaultClassLoader() : classLoader;
        this.loadedAll = loadedAll;
        this.configurationPropertyReader = new DefaultConfigurationPropertyReader();
    }

    @Override
    public final List<ConfigurationProperty> load() throws Throwable {
        List<ConfigurationProperty> configurationProperties = newLinkedList();
        if (loadedAll) {
            Enumeration<URL> urls = this.classLoader.getResources(this.resourceName);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                load(configurationProperties, url::openStream);
            }
        } else {
            load(configurationProperties, () -> this.classLoader.getResourceAsStream(this.resourceName));
        }
        return unmodifiableList(configurationProperties);
    }

    void load(List<ConfigurationProperty> configurationProperties, ThrowableSupplier<InputStream> streamSupplier) throws Throwable {
        try (InputStream inputStream = streamSupplier.get()) {
            configurationProperties.addAll(this.configurationPropertyReader.load(inputStream));
        }
    }
}
