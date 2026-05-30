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
import io.microsphere.lang.Prioritized;

/**
 * {@code ConfigurationPropertyGenerator} interface can be implemented by objects that
 * generate string representations (e.g., JSON, XML, Properties) of {@link ConfigurationProperty}.
 *
 * <p>Implementing classes should typically also implement the {@link Prioritized#getPriority()} method
 * to define their priority value. The priority is used to determine ordering via the
 * {@link Prioritized#compareTo(Prioritized)} method.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class JsonPropertyGenerator implements ConfigurationPropertyGenerator {
 *     public int getPriority() {
 *         return 1;
 *     }
 *
 *     public String generate(ConfigurationProperty property) {
 *         // Implementation to generate JSON representation
 *         return "{\"" + property.getName() + "\":\"" + property.getValue() + "\"}";
 *     }
 * }
 *
 * // Registering and using generators
 * List<ConfigurationPropertyGenerator> generators = new ArrayList<>();
 * generators.add(new JsonPropertyGenerator());
 * Collections.sort(generators); // Sort by priority
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationProperty
 * @see Prioritized
 * @since 1.0.0
 */
public interface ConfigurationPropertyGenerator extends Prioritized {

    /**
     * Generates a content of the given {@link ConfigurationProperty}.
     *
     * @param configurationProperty the configuration property to be the some content.
     * @return a JSON string representation of the configuration property.
     * @throws IllegalArgumentException if the configurationProperty is null.
     */
    @Nonnull
    String generate(ConfigurationProperty configurationProperty) throws IllegalArgumentException;
}
