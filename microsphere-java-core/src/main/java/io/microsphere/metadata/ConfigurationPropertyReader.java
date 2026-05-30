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

import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.beans.ConfigurationProperty;
import io.microsphere.lang.Prioritized;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import static io.microsphere.io.IOUtils.copyToString;
import static io.microsphere.nio.charset.CharsetUtils.DEFAULT_CHARSET;

/**
 * The SPI to read the list of {@link ConfigurationProperty configuration properties} from various sources.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationProperty
 * @see io.microsphere.annotation.ConfigurationProperty
 * @since 1.0.0
 */
public interface ConfigurationPropertyReader extends Prioritized {

    /**
     * Reads a list of {@link ConfigurationProperty} objects from the provided {@link InputStream}.
     * <p>
     * This method reads configuration properties from the given input stream, using the default charset
     * for decoding the content. It internally delegates to {@link #read(Reader)} after wrapping the
     * input stream into an {@link InputStreamReader}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ConfigurationPropertiesReader reader = ...; // Obtain an instance
     * try (InputStream inputStream = new FileInputStream("config.properties")) {
     *     List<ConfigurationProperty> properties = reader.load(inputStream);
     *     // Process the loaded properties
     * }
     * }</pre>
     *
     * @param inputStream the input stream to read from; must not be {@code null}
     * @return a list of {@link ConfigurationProperty} objects loaded from the input stream
     * @throws Throwable if any error occurs during loading
     * @see #read(Reader)
     */
    @Nonnull
    @Immutable
    default List<ConfigurationProperty> read(InputStream inputStream) throws Throwable {
        return read(new InputStreamReader(inputStream, DEFAULT_CHARSET));
    }

    /**
     * Reads a list of {@link ConfigurationProperty} objects from the provided {@link Reader}.
     * <p>
     * This method reads configuration properties from the given reader by first copying its content
     * into a string using {@link io.microsphere.io.IOUtils#copyToString(Reader)}, then delegating
     * to {@link #read(String)} to perform the actual parsing and loading.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ConfigurationPropertiesReader reader = ...; // Obtain an instance
     * try (Reader fileReader = new FileReader("config.properties")) {
     *     List<ConfigurationProperty> properties = reader.load(fileReader);
     *     // Process the loaded properties
     * }
     * }</pre>
     *
     * @param reader the reader to read from; must not be {@code null}
     * @return a list of {@link ConfigurationProperty} objects loaded from the reader
     * @throws Throwable if any error occurs during loading
     * @see #read(String)
     * @see io.microsphere.io.IOUtils#copyToString(Reader)
     */
    @Nonnull
    @Immutable
    default List<ConfigurationProperty> read(Reader reader) throws Throwable {
        return read(copyToString(reader));
    }

    /**
     * Reads a list of {@link ConfigurationProperty} objects from the provided content string.
     * <p>
     * This method parses and loads configuration properties from the given string content.
     * The format of the content is determined by the specific implementation of this interface.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ConfigurationPropertiesReader reader = ...; // Obtain an instance
     * String content = "property1=value1\nproperty2=value2";
     * List<ConfigurationProperty> properties = reader.load(content);
     * // Process the loaded properties
     * }</pre>
     *
     * @param content the string content to parse and load from; must not be {@code null}
     * @return a list of {@link ConfigurationProperty} objects loaded from the content
     * @throws Throwable if any error occurs during loading
     * @see ConfigurationProperty
     */
    @Nonnull
    @Immutable
    List<ConfigurationProperty> read(String content) throws Throwable;

}
