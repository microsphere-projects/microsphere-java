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

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;

import java.util.Objects;
import java.util.Set;

import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.util.Assert.assertNotNull;

/**
 * Represents a configuration property with metadata, value, and type information.
 * <p>
 * A {@link ConfigurationProperty} instance holds the name, type, and value of a configuration property,
 * along with additional metadata such as description and targets. It is typically used to manage and
 * validate configuration properties in a structured way.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Creating a simple configuration property with a name and default type (String.class)
 * ConfigurationProperty property = new ConfigurationProperty("my.property.name");
 *
 * // Setting a custom type and value
 * property.setType(Integer.class);
 * property.setValue(42);
 *
 * // Accessing metadata to add a description
 * property.getMetadata().setDescription("This is an example property");
 * property.getMetadata().getTargets().add("example.target");
 *
 * // Checking equality
 * ConfigurationProperty another = new ConfigurationProperty("my.property.name", Integer.class);
 * boolean isEqual = property.equals(another); // true if all fields match
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Metadata
 * @since 1.0.0
 */
public class ConfigurationProperty {

    /**
     * The name of the property
     */
    @Nonnull
    private final String name;

    /**
     * The type of the property
     */
    @Nonnull
    private Class<?> type;

    /**
     * The value of the property
     */
    @Nullable
    private Object value;

    /**
     * The default value of the property
     */
    @Nullable
    private Object defaultValue;

    /**
     * Whether the property is required
     */
    private boolean required;

    /**
     * The metadata of the property
     */
    @Nonnull
    private final Metadata metadata;

    public ConfigurationProperty(String name) {
        this(name, String.class);
    }

    public ConfigurationProperty(String name, Class<?> type) {
        assertNotNull(name, () -> "the property name must not null");
        this.name = name;
        setType(type);
        this.metadata = new Metadata();
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public Class<?> getType() {
        return type;
    }

    public void setType(@Nonnull Class<?> type) {
        assertNotNull(type, () -> "the property type must not null");
        this.type = type;
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    public void setValue(@Nullable Object value) {
        this.value = value;
    }

    @Nullable
    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(@Nullable Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Nonnull
    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof ConfigurationProperty)) return false;

        ConfigurationProperty that = (ConfigurationProperty) o;

        return isRequired() == that.isRequired()
                && getName().equals(that.getName())
                && getType().equals(that.getType())
                && Objects.equals(getValue(), that.getValue())
                && Objects.equals(getDefaultValue(), that.getDefaultValue())
                && Objects.equals(getMetadata(), that.getMetadata());
    }

    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + getType().hashCode();
        result = 31 * result + Objects.hashCode(getValue());
        result = 31 * result + Objects.hashCode(getDefaultValue());
        result = 31 * result + Boolean.hashCode(isRequired());
        result = 31 * result + Objects.hashCode(getMetadata());
        return result;
    }

    @Override
    public String toString() {
        return "ConfigurationProperty{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", value=" + value +
                ", defaultValue=" + defaultValue +
                ", required=" + required +
                ", metadata=" + metadata +
                '}';
    }

    /**
     * The metadata class of the Spring Configuration Property
     */
    public static class Metadata {

        /**
         * The description of the property
         */
        private String description;

        /**
         * The sources of the property
         */
        private Set<String> sources;

        /**
         * The targets of the property
         */
        private Set<String> targets;

        @Nonnull
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        /**
         * Retrieves the set of sources associated with this configuration property.
         * If the sources set is null, it will be initialized with an empty linked hash set.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * ConfigurationProperty.Metadata metadata = new ConfigurationProperty.Metadata();
         * metadata.getSources().add("source1");
         * metadata.getSources().add("source2");
         *
         * // Retrieving the sources
         * Set<String> sources = metadata.getSources(); // contains "source1", "source2"
         * }</pre>
         *
         * @return the set of sources; never null
         */
        @Nonnull
        public Set<String> getSources() {
            if (sources == null) {
                sources = newLinkedHashSet(2);
            }
            return sources;
        }

        /**
         * Retrieves the set of targets associated with this configuration property.
         * If the targets set is null, it will be initialized with an empty linked hash set.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * ConfigurationProperty.Metadata metadata = new ConfigurationProperty.Metadata();
         * metadata.getTargets().add("target1");
         * metadata.getTargets().add("target2");
         *
         * // Retrieving the targets
         * Set<String> targets = metadata.getTargets(); // contains "target1", "target2"
         * }</pre>
         *
         * @return the set of targets; never null
         */
        @Nonnull
        public Set<String> getTargets() {
            if (targets == null) {
                targets = newLinkedHashSet(2);
            }
            return targets;
        }

        @Override
        public final boolean equals(Object o) {
            if (!(o instanceof Metadata)) return false;

            Metadata metadata = (Metadata) o;

            return Objects.equals(description, metadata.description)
                    && Objects.equals(getSources(), metadata.getSources())
                    && Objects.equals(getTargets(), metadata.getTargets());
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(description);
            result = 31 * result + Objects.hashCode(getSources());
            result = 31 * result + Objects.hashCode(getTargets());
            return result;
        }

        @Override
        public String toString() {
            return "Metadata{" +
                    "description='" + description + '\'' +
                    ", sources=" + getSources() +
                    ", targets=" + getTargets() +
                    '}';
        }
    }
}
