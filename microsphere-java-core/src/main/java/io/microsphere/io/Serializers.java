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
 * A utility class for managing and retrieving {@link Serializer} instances.
 * <p>
 * This class provides methods to load serializers via SPI (Service Provider Interface),
 * retrieve the most compatible serializer for a given type, and get serializers based on priority.
 * </p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * Serializers serializers = new Serializers();
 * serializers.loadSPI(); // Load all available serializers from SPI
 *
 * // Get the highest priority serializer for String
 * Serializer<String> highestPrioritySerializer = serializers.getHighestPriority(String.class);
 *
 * // Get the lowest priority serializer for String
 * Serializer<String> lowestPrioritySerializer = serializers.getLowestPriority(String.class);
 *
 * // Get the most compatible serializer for String
 * Serializer<?> compatibleSerializer = serializers.getMostCompatible(String.class);
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class Serializers {

    private final Map<Class<?>, List<Serializer>> typedSerializers = new HashMap<>();

    private final ClassLoader classLoader;

    public Serializers(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Serializers() {
        this(getDefaultClassLoader());
    }

    public void loadSPI() {
        for (Serializer serializer : loadServicesList(Serializer.class, classLoader)) {
            List<Class<?>> typeArguments = resolveTypeArgumentClasses(serializer.getClass());
            Class<?> targetClass = first(typeArguments);
            List<Serializer> serializers = typedSerializers.computeIfAbsent(targetClass, k -> new LinkedList());
            serializers.add(serializer);
        }
    }

    /**
     * Get the most compatible instance of {@link Serializer} by the specified deserialized type
     *
     * @param serializedType the type to be serialized
     * @return <code>null</code> if not found
     */
    public Serializer<?> getMostCompatible(Class<?> serializedType) {
        Serializer<?> serializer = getHighestPriority(serializedType);
        if (serializer == null) {
            serializer = getLowestPriority(Object.class);
        }
        return serializer;
    }

    /**
     * Get the highest priority instance of {@link Serializer} by the specified serialized type
     *
     * @param serializedType the type to be serialized
     * @param <S>            the type to be serialized
     * @return <code>null</code> if not found
     */
    public <S> Serializer<S> getHighestPriority(Class<S> serializedType) {
        List<Serializer<S>> serializers = get(serializedType);
        return first(serializers);
    }


    /**
     * Get the lowest priority instance of {@link Serializer} by the specified serialized type
     *
     * @param serializedType the type to be serialized
     * @param <S>            the type to be serialized
     * @return <code>null</code> if not found
     */
    public <S> Serializer<S> getLowestPriority(Class<S> serializedType) {
        List<Serializer<S>> serializers = get(serializedType);
        return last(serializers);
    }

    /**
     * Get all instances of {@link Serializer} by the specified serialized type
     *
     * @param serializedType the type to be serialized
     * @param <S>            the type to be serialized
     * @return non-null {@link List}
     */
    public <S> List<Serializer<S>> get(Class<S> serializedType) {
        return (List) typedSerializers.getOrDefault(serializedType, emptyList());
    }
}
