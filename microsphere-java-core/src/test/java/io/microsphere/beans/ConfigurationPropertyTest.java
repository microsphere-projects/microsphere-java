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
public class ConfigurationPropertyTest {

    private static final String name = "test-name";

    private static final String value = "test-value";

    private static final String defaultValue = "default-value";

    private static final Class<?> type = Integer.class;

    private ConfigurationProperty property;

    @BeforeEach
    public void before() {
        this.property = new ConfigurationProperty(name);
    }

    @Test
    public void testNull() {
        assertThrows(IllegalArgumentException.class, () -> new ConfigurationProperty(null));
        assertThrows(IllegalArgumentException.class, () -> new ConfigurationProperty("test").setType(null));
    }

    @Test
    public void testName() {
        assertEquals(name, this.property.getName());
    }

    @Test
    public void testType() {
        assertEquals(String.class, this.property.getType());
        this.property.setType(type);
        assertEquals(type, this.property.getType());
    }

    @Test
    public void testValue() {
        assertNull(this.property.getValue());
        this.property.setValue(value);
        assertEquals(value, this.property.getValue());
    }

    @Test
    public void testDefaultValue() {
        assertNull(this.property.getDefaultValue());
        this.property.setDefaultValue(defaultValue);
        assertEquals(defaultValue, this.property.getDefaultValue());
    }

    @Test
    public void testRequired() {
        assertFalse(this.property.isRequired());
        this.property.setRequired(true);
        assertTrue(this.property.isRequired());
    }

    @Test
    public void testMetadata() {
        Metadata metadata = this.property.getMetadata();
        assertNotNull(metadata);
        assertNull(metadata.getDescription());
        assertNotNull(metadata.getTargets());

        Metadata metadata1 = new Metadata();

        metadata.setDescription("description");
        assertNotEquals(metadata, metadata1);
        metadata1.setDescription("description");
        assertEquals(metadata1.getDescription(), metadata.getDescription());
        assertEquals(metadata, metadata1);

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

        assertNotEquals(metadata, new Object());

        assertEquals(metadata.hashCode(), metadata1.hashCode());
        assertEquals(metadata.toString(), metadata1.toString());
    }

    @Test
    public void testHashCode() {
        ConfigurationProperty property = new ConfigurationProperty(name);
        assertEquals(this.property.hashCode(), property.hashCode());

        this.property.setType(type);
        property.setType(type);
        assertEquals(this.property.hashCode(), property.hashCode());

        this.property.setValue(value);
        property.setValue(value);
        assertEquals(this.property.hashCode(), property.hashCode());

        this.property.setDefaultValue(defaultValue);
        property.setDefaultValue(defaultValue);
        assertEquals(this.property.hashCode(), property.hashCode());

        this.property.setRequired(true);
        property.setRequired(true);
        assertEquals(this.property.hashCode(), property.hashCode());

        this.property.getMetadata().setDescription("description");
        property.getMetadata().setDescription("description");
        assertEquals(this.property.hashCode(), property.hashCode());

        this.property.getMetadata().getTargets().add("target");
        property.getMetadata().getTargets().add("target");
        assertEquals(this.property.hashCode(), property.hashCode());
    }

    @Test
    public void testEquals() {
        ConfigurationProperty property = new ConfigurationProperty(name);
        assertEquals(this.property, property);
        assertNotEquals(this.property, new ConfigurationProperty("_" + name));
        assertNotEquals(this.property, new Object());

        this.property.setType(type);
        assertNotEquals(this.property, property);
        property.setType(type);
        assertEquals(this.property, property);

        this.property.setValue(value);
        assertNotEquals(this.property, property);
        property.setValue(value);
        assertEquals(this.property, property);

        this.property.setDefaultValue(defaultValue);
        assertNotEquals(this.property, property);
        property.setDefaultValue(defaultValue);
        assertEquals(this.property, property);

        this.property.setRequired(true);
        assertNotEquals(this.property, property);
        property.setRequired(true);
        assertEquals(this.property, property);

        this.property.getMetadata().setDescription("description");
        assertNotEquals(this.property, property);
        property.getMetadata().setDescription("description");
        assertEquals(this.property, property);

        this.property.getMetadata().getTargets().add("target");
        assertNotEquals(this.property, property);
        property.getMetadata().getTargets().add("target");
        assertEquals(this.property, property);
    }

    @Test
    public void testToString() {
        ConfigurationProperty property = new ConfigurationProperty(name);
        assertEquals(this.property.toString(), property.toString());

        this.property.setType(type);
        property.setType(type);
        assertEquals(this.property.toString(), property.toString());

        this.property.setValue(value);
        property.setValue(value);
        assertEquals(this.property.toString(), property.toString());

        this.property.setDefaultValue(defaultValue);
        property.setDefaultValue(defaultValue);
        assertEquals(this.property.toString(), property.toString());

        this.property.setRequired(true);
        property.setRequired(true);
        assertEquals(this.property.toString(), property.toString());

        this.property.getMetadata().setDescription("description");
        property.getMetadata().setDescription("description");
        assertEquals(this.property.toString(), property.toString());

        this.property.getMetadata().getTargets().add("target");
        property.getMetadata().getTargets().add("target");
        assertEquals(this.property.toString(), property.toString());
    }
}
