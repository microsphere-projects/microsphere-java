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
package io.microsphere.logging;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.lang.Prioritized;

import java.util.List;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static java.util.Collections.sort;
import static java.util.ServiceLoader.load;

/**
 * The factory class for {@link Logger}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Logger
 * @since 1.0.0
 */
public abstract class LoggerFactory implements Prioritized {

    private static final ClassLoader classLoader = LoggerFactory.class.getClassLoader();

    @Nullable
    private static final LoggerFactory factory = loadFactory();

    @Nullable
    private static LoggerFactory loadFactory() {
        List<LoggerFactory> availableFactories = loadAvailableFactories();
        return availableFactories.get(0);
    }

    static List<LoggerFactory> loadAvailableFactories() {
        List<LoggerFactory> factories = loadFactories();
        factories.removeIf(factory -> !factory.isAvailable());
        return factories;
    }

    static List<LoggerFactory> loadFactories() {
        List<LoggerFactory> factories = newLinkedList(load(LoggerFactory.class, classLoader));
        sort(factories, Prioritized.COMPARATOR);
        return factories;
    }

    /**
     * Get an instance of {@link Logger} by type
     *
     * @param type the type
     * @return non-null
     */
    @Nonnull
    public static Logger getLogger(Class<?> type) {
        return getLogger(type.getName());
    }

    /**
     * Get an instance of {@link Logger} by name
     *
     * @param name the name of {@link Logger}
     * @return {@link Logger}
     */
    @Nonnull
    public static Logger getLogger(String name) {
        return factory.createLogger(name);
    }

    /**
     * Current {@link LoggerFactory} is available or not
     *
     * @return <code>true</code> if available
     */
    protected boolean isAvailable() {
        return getDelegateLoggerClass() != null;
    }

    /**
     * The class of delegate Logger
     *
     * @return <code>null</code> if not found
     */
    private Class<?> getDelegateLoggerClass() {
        String className = getDelegateLoggerClassName();
        Class<?> delegateLoggerClass = null;
        try {
            delegateLoggerClass = classLoader.loadClass(className);
        } catch (Throwable e) {
        }
        return delegateLoggerClass;
    }

    /**
     * The class name of delegate Logger
     *
     * @return non-null
     */
    protected abstract String getDelegateLoggerClassName();

    /**
     * Create a new {@link Logger}
     *
     * @param name the name of {@link Logger }
     * @return non-null
     */
    public abstract Logger createLogger(String name);

}
