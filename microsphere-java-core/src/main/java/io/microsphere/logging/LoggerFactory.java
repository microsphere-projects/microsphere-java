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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static io.microsphere.reflect.TypeUtils.getClassName;
import static io.microsphere.util.ClassLoaderUtils.getClassLoader;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.ServiceLoaderUtils.loadServicesList;

/**
 * The factory class for {@link Logger}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Logger
 * @since 1.0.0
 */
public abstract class LoggerFactory {


    private static final ClassLoader classLoader = getClassLoader(LoggerFactory.class);

    @Nullable
    private static final LoggerFactory factory = loadFactory();

    @Nullable
    private static LoggerFactory loadFactory() {
        Iterable<LoggerFactory> factories = loadServicesList(LoggerFactory.class, classLoader, false);
        LoggerFactory availableFactory = null;
        for (LoggerFactory factory : factories) {
            if (factory.isAvailable()) {
                availableFactory = factory;
                break;
            }
        }
        return availableFactory;
    }

    /**
     * Get an instance of {@link Logger} by type
     *
     * @param type the type
     * @return non-null
     */
    @Nonnull
    public static Logger getLogger(Class<?> type) {
        return getLogger(getClassName(type));
    }

    /**
     * Get an instance of {@link Logger} by name
     *
     * @param name the name of {@link Logger}
     * @return {@link Logger}
     */
    @Nonnull
    public static Logger getLogger(String name) {
        if (factory == null) {
            return new NoOpLogger(name);
        }
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
        return resolveClass(getDelegateLoggerClassName(), classLoader);
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
