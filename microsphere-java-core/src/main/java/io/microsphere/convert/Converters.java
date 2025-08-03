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

package io.microsphere.convert;

import io.microsphere.util.Utils;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static io.microsphere.collection.ListUtils.first;
import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.MapUtils.immutableEntry;
import static io.microsphere.collection.MapUtils.newConcurrentHashMap;
import static io.microsphere.util.ClassLoaderUtils.getClassLoader;
import static io.microsphere.util.ServiceLoaderUtils.loadServicesList;

/**
 * The utility class of {@link Converter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Converter
 * @since 1.0.0
 */
public class Converters implements Utils {

    private static final ClassLoader classLoader = getClassLoader(Converters.class);

    private static final ConcurrentMap<Entry<Class<?>, Class<?>>, List<Converter>> convertersCache = initConvertersCache();

    static ConcurrentMap<Entry<Class<?>, Class<?>>, List<Converter>> initConvertersCache() {
        // sorted and cached converters
        List<Converter> convertersList = loadConvertersList();
        int size = convertersList.size();
        ConcurrentMap<Entry<Class<?>, Class<?>>, List<Converter>> convertersCache = newConcurrentHashMap(size);
        for (int i = 0; i < size; i++) {
            Converter converter = convertersList.get(i);
            Entry<Class<?>, Class<?>> key = immutableEntry(converter.getSourceType(), converter.getTargetType());
            List<Converter> converters = convertersCache.computeIfAbsent(key, k -> newArrayList(4));
            converters.add(converter);
        }
        return convertersCache;
    }

    static <S, T> Converter<S, T> findConverter(Class<S> sourceType, Class<T> targetType) {
        Entry<Class<?>, Class<?>> key = immutableEntry(sourceType, targetType);
        List<Converter> converters = convertersCache.computeIfAbsent(key, k -> loadConvertersList()
                .stream()
                .filter(c -> c.accept(sourceType, targetType))
                .collect(Collectors.toList()));
        return (Converter<S, T>) first(converters);
    }

    static List<Converter> loadConvertersList() {
        return loadServicesList(Converter.class, classLoader, true);
    }

    private Converters() {
    }
}
