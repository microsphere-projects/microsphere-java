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
package io.microsphere.io;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.microsphere.collection.ListUtils.first;
import static io.microsphere.collection.ListUtils.last;
import static io.microsphere.reflect.TypeUtils.resolveTypeArgumentClasses;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static io.microsphere.util.ServiceLoaderUtils.loadServicesList;
import static java.util.Collections.emptyList;

/**
 * A utility class for managing and retrieving {@link Deserializer} instances based on the deserialized type.
 * <p>
 * This class allows registration and lookup of deserializers via SPI (Service Provider Interface),
 * supporting priority-based resolution to determine the most or least specific deserializer available.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Create an instance with the default class loader
 * Deserializers deserializers = new Deserializers();
 *
 * // Load deserializers via SPI
 * deserializers.loadSPI();
 *
 * // Retrieve the highest priority deserializer for a specific type
 * Deserializer<MyType> deserializer = deserializers.getHighestPriority(MyType.class);
 *
 * // Deserialize an object using the retrieved deserializer
 * MyType obj = deserializer.deserialize(data);
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class Deserializers {

    private final Map<Class<?>, List<Deserializer>> typedDeserializers = new HashMap<>();

    private final ClassLoader classLoader;

    public Deserializers(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Deserializers() {
        this(getDefaultClassLoader());
    }

    public void loadSPI() {
        for (Deserializer deserializer : loadServicesList(Deserializer.class, classLoader)) {
            List<Class<?>> typeArguments = resolveTypeArgumentClasses(deserializer.getClass());
            Class<?> targetClass = first(typeArguments);
            List<Deserializer> deserializers = typedDeserializers.computeIfAbsent(targetClass, k -> new LinkedList());
            deserializers.add(deserializer);
        }
    }

    /**
     * Get the most compatible instance of {@link Deserializer} by the specified deserialized type
     *
     * @param deserializedType the type to be deserialized
     * @return <code>null</code> if not found
     */
    public Deserializer<?> getMostCompatible(Class<?> deserializedType) {
        Deserializer<?> deserializer = getHighestPriority(deserializedType);
        if (deserializer == null) {
            deserializer = getLowestPriority(Object.class);
        }
        return deserializer;
    }

    /**
     * Get the highest priority instance of {@link Deserializer} by the specified deserialized type
     *
     * @param deserializedType the type to be deserialized
     * @param <T>              the type to be serialized
     * @return <code>null</code> if not found
     */
    public <T> Deserializer<T> getHighestPriority(Class<?> deserializedType) {
        List<Deserializer<T>> serializers = get(deserializedType);
        return first(serializers);
    }

    /**
     * Get the lowest priority instance of {@link Deserializer} by the specified deserialized type
     *
     * @param deserializedType the type to be deserialized
     * @param <T>              the type to be serialized
     * @return <code>null</code> if not found
     */
    public <T> Deserializer<T> getLowestPriority(Class<?> deserializedType) {
        List<Deserializer<T>> serializers = get(deserializedType);
        return last(serializers);
    }

    /**
     * Get all instances of {@link Deserializer} by the specified deserialized type
     *
     * @param deserializedType the type to be deserialized
     * @param <T>              the type to be serialized
     * @return non-null {@link List}
     */
    public <T> List<Deserializer<T>> get(Class<?> deserializedType) {
        return (List) typedDeserializers.getOrDefault(deserializedType, emptyList());
    }
}
