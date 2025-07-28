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
import io.microsphere.beans.ConfigurationProperty;

/**
 * ConfigurationPropertyJSONGenerator is an interface that defines the contract for generating
 * JSON representations of {@link ConfigurationProperty} objects.
 *
 * <p>Implementations of this interface are responsible for converting a {@link ConfigurationProperty}
 * into a JSON string. This can be useful for serialization, logging, or transmitting configuration
 * metadata in a standardized format.
 *
 * <h3>Example Usage</h3>
 * <pre>
 * {@code
 * ConfigurationProperty property = new ConfigurationProperty("server.port", "8080", "The port number");
 * ConfigurationPropertyJSONGenerator generator = new SomeConcreteGenerator();
 * String json = generator.generate(property);
 * // json might look like: {"name":"server.port","value":"8080","description":"The port number"}
 * }
 * </pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationProperty
 * @since 1.0.0
 */
public interface ConfigurationPropertyJSONGenerator {

    /**
     * Generates a JSON representation of the given {@link ConfigurationProperty}.
     *
     * @param configurationProperty the configuration property to be converted to JSON.
     * @return a JSON string representation of the configuration property.
     * @throws IllegalArgumentException if the configurationProperty is null.
     */
    @Nonnull
    String generate(ConfigurationProperty configurationProperty) throws IllegalArgumentException;
}
