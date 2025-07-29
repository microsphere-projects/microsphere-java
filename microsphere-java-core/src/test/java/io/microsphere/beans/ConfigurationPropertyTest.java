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
package io.microsphere.beans;

import io.microsphere.beans.ConfigurationProperty.Metadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ConfigurationProperty} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConfigurationProperty
 * @since 1.0.0
 */
class ConfigurationPropertyTest {

    private static final String TEST_NAME = "test-name";

    private static final Class<?> TEST_TYPE = Integer.class;
    
    private static final String TEST_VALUE = "test-value";

    private static final String TEST_DEFAULT_VALUE = "default-value";

    private static final String TEST_DESCRIPTION = "test-description";

    private static final String TEST_TARGET = "target";
    
    private ConfigurationProperty property;

    @BeforeEach
    void setUp() {
        this.property = new ConfigurationProperty(TEST_NAME);
    }

    @Test
    void testNull() {
        assertThrows(IllegalArgumentException.class, () -> new ConfigurationProperty(null));
        assertThrows(IllegalArgumentException.class, () -> new ConfigurationProperty("test").setType((String) null));
    }

    @Test
    void testName() {
        assertEquals(TEST_NAME, this.property.getName());
    }

    @Test
    void testType() {
        assertEquals(String.class.getName(), this.property.getType());
        this.property.setType(TEST_TYPE);
        assertEquals(TEST_TYPE.getName(), this.property.getType());
    }

    @Test
    void testValue() {
        assertNull(this.property.getValue());
        this.property.setValue(TEST_VALUE);
        assertEquals(TEST_VALUE, this.property.getValue());
    }

    @Test
    void testDefaultValue() {
        assertNull(this.property.getDefaultValue());
        this.property.setDefaultValue(TEST_DEFAULT_VALUE);
        assertEquals(TEST_DEFAULT_VALUE, this.property.getDefaultValue());
    }

    @Test
    void testRequired() {
        assertFalse(this.property.isRequired());
        this.property.setRequired(true);
        assertTrue(this.property.isRequired());
    }

    @Test
    void testMetadata() {
        Metadata metadata = this.property.getMetadata();
        assertNotNull(metadata);
        assertNotNull(metadata.getSources());
        assertNotNull(metadata.getTargets());
        assertNull(metadata.getDeclaredClass());
        assertNull(metadata.getDeclaredField());

        Metadata metadata1 = new Metadata();

        metadata.getSources().add("source");
        assertNotEquals(metadata, metadata1);
        metadata1.getSources().add("source");
        assertEquals(metadata.getSources(), metadata1.getSources());
        assertEquals(metadata, metadata1);

        metadata.getTargets().add("target");
        assertNotEquals(metadata, metadata1);
        metadata1.getTargets().add("target");
        assertEquals(metadata.getTargets(), metadata1.getTargets());
        assertEquals(metadata, metadata1);

        metadata.setDeclaredClass("io.microsphere.beans.ConfigurationProperty");
        assertNotEquals(metadata, metadata1);
        metadata1.setDeclaredClass("io.microsphere.beans.ConfigurationProperty");
        assertEquals(metadata, metadata1);

        metadata.setDeclaredField("name");
        assertNotEquals(metadata, metadata1);
        metadata1.setDeclaredField("name");
        assertEquals(metadata, metadata1);

        assertNotEquals(metadata, new Object());

        assertEquals(metadata.hashCode(), metadata1.hashCode());
        assertEquals(metadata.toString(), metadata1.toString());
    }

    @Test
    void testHashCode() {
        ConfigurationProperty property = new ConfigurationProperty(TEST_NAME);
        assertEquals(this.property.hashCode(), property.hashCode());

        this.property.setType(TEST_TYPE);
        property.setType(TEST_TYPE);
        assertEquals(this.property.hashCode(), property.hashCode());

        this.property.setValue(TEST_VALUE);
        property.setValue(TEST_VALUE);
        assertEquals(this.property.hashCode(), property.hashCode());

        this.property.setDefaultValue(TEST_DEFAULT_VALUE);
        property.setDefaultValue(TEST_DEFAULT_VALUE);
        assertEquals(this.property.hashCode(), property.hashCode());

        this.property.setRequired(true);
        property.setRequired(true);
        assertEquals(this.property.hashCode(), property.hashCode());

        this.property.setDescription(TEST_DESCRIPTION);
        property.setDescription(TEST_DESCRIPTION);
        assertEquals(this.property.hashCode(), property.hashCode());

        this.property.getMetadata().getTargets().add("target");
        property.getMetadata().getTargets().add("target");
        assertEquals(this.property.hashCode(), property.hashCode());
    }

    @Test
    void testEquals() {
        ConfigurationProperty property = new ConfigurationProperty(TEST_NAME);
        assertEquals(this.property, property);
        assertNotEquals(this.property, new ConfigurationProperty("_" + TEST_NAME));
        assertNotEquals(this.property, new Object());

        this.property.setType(TEST_TYPE);
        assertNotEquals(this.property, property);
        property.setType(TEST_TYPE);
        assertEquals(this.property, property);

        this.property.setValue(TEST_VALUE);
        assertNotEquals(this.property, property);
        property.setValue(TEST_VALUE);
        assertEquals(this.property, property);

        this.property.setDefaultValue(TEST_DEFAULT_VALUE);
        assertNotEquals(this.property, property);
        property.setDefaultValue(TEST_DEFAULT_VALUE);
        assertEquals(this.property, property);

        this.property.setRequired(true);
        assertNotEquals(this.property, property);
        property.setRequired(true);
        assertEquals(this.property, property);

        this.property.setDescription(TEST_DESCRIPTION);
        assertNotEquals(this.property, property);
        property.setDescription(TEST_DESCRIPTION);
        assertEquals(this.property, property);

        this.property.getMetadata().getTargets().add("target");
        assertNotEquals(this.property, property);
        property.getMetadata().getTargets().add("target");
        assertEquals(this.property, property);
    }

    @Test
    void testToString() {
        ConfigurationProperty property = new ConfigurationProperty(TEST_NAME);
        assertEquals(this.property.toString(), property.toString());

        this.property.setType(TEST_TYPE);
        property.setType(TEST_TYPE);
        assertEquals(this.property.toString(), property.toString());

        this.property.setValue(TEST_VALUE);
        property.setValue(TEST_VALUE);
        assertEquals(this.property.toString(), property.toString());

        this.property.setDefaultValue(TEST_DEFAULT_VALUE);
        property.setDefaultValue(TEST_DEFAULT_VALUE);
        assertEquals(this.property.toString(), property.toString());

        this.property.setRequired(true);
        property.setRequired(true);
        assertEquals(this.property.toString(), property.toString());

        this.property.setDescription(TEST_DESCRIPTION);
        property.setDescription(TEST_DESCRIPTION);
        assertEquals(this.property.toString(), property.toString());

        this.property.getMetadata().getTargets().add(TEST_TARGET);
        property.getMetadata().getTargets().add(TEST_TARGET);
        assertEquals(this.property.toString(), property.toString());
    }
}
