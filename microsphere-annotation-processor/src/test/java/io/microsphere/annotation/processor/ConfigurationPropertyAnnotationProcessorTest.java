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

package io.microsphere.annotation.processor;

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.json.JSONArray;
import io.microsphere.test.annotation.processing.AbstractAnnotationProcessingTest;

import org.junit.jupiter.api.Test;

import static io.microsphere.annotation.ConfigurationProperty.SYSTEM_PROPERTIES_SOURCE;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ConfigurationPropertyAnnotationProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationPropertyAnnotationProcessor
 * @since 1.0.0
 */
class ConfigurationPropertyAnnotationProcessorTest extends AbstractAnnotationProcessingTest {

    /**
     * The test field annotated with {@link ConfigurationProperty @ConfigurationProperty}
     */
    @ConfigurationProperty(
            name = "test.metadata.property.name",
            type = String.class,
            defaultValue = "test-property-default-value",
            required = true,
            description = "The test property",
            source = SYSTEM_PROPERTIES_SOURCE
    )
    private String testProperty;

    @Test
    void testResolveMetadataOnEmptySet() {
        ConfigurationPropertyAnnotationProcessor processor = newProcessor();
        processor.resolveMetadata(emptySet());
        assertEquals("[]", processor.toJSON());
    }

    /**
     * Simulates multiple annotation processing rounds — e.g. when another annotation processor
     * (Lombok, AutoService, ...) generates new source files, javac runs additional rounds and
     * {@link ConfigurationPropertyAnnotationProcessor#resolveMetadata(Set)} is invoked once per round.
     * <p>
     * The old implementation opened and closed the JSON array ('[' / ']') in every round and appended
     * the SPI-generated metadata on every round, producing corrupted JSON like {@code [{...},][]}]}
     * and duplicated SPI entries. The JSON must instead stay a valid array with exactly one entry
     * per annotated element, no matter how many rounds occur.
     */
    @Test
    void testResolveMetadataOnMultipleRounds() throws Exception {
        ConfigurationPropertyAnnotationProcessor processor = newProcessor();

        // Round 1 : the root elements, e.g. the test class being compiled
        processor.resolveMetadata(singleton(this.testTypeElement));
        // Round 2 : a round with no root elements
        processor.resolveMetadata(emptySet());
        // Round 3 : the same root elements again (e.g. re-resolved after generated sources)
        processor.resolveMetadata(singleton(this.testTypeElement));

        String json = processor.toJSON();

        // The JSON must remain a valid array across rounds
        JSONArray jsonArray = new JSONArray(json);
        // Exactly one entry per (element, round) pair, and NO SPI-generated metadata
        // (TestConfigurationPropertyLoader) must be appended during the rounds
        assertEquals(2, jsonArray.length());
        assertEquals("test.metadata.property.name", jsonArray.getJSONObject(0).getString("name"));
        assertEquals("test.metadata.property.name", jsonArray.getJSONObject(1).getString("name"));
    }

    private ConfigurationPropertyAnnotationProcessor newProcessor() {
        ConfigurationPropertyAnnotationProcessor processor = new ConfigurationPropertyAnnotationProcessor();
        processor.init(super.processingEnv);
        return processor;
    }
}
