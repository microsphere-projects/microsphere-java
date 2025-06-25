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
package io.microsphere.classloading;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.lang.Prioritized;

import java.net.URL;
import java.net.URLClassLoader;

import static io.microsphere.net.URLUtils.EMPTY_URL_ARRAY;
import static io.microsphere.util.ClassLoaderUtils.findURLClassLoader;

/**
 * A strategy interface for handling URL Class-Path entries in a {@link ClassLoader}.
 * Implementations of this interface can provide custom logic for interacting with
 * URLs related to class loading, such as retrieving or removing URLs from a class loader.
 *
 * <p>
 * This interface extends {@link Prioritized}, allowing implementations to define their
 * priority order. The default implementation of {@link #getPriority()} returns
 * {@link Prioritized#MIN_PRIORITY}, making it the lowest priority unless overridden.
 * </p>
 *
 * <h2>Example Implementation</h2>
 * <pre>{@code
 * public class ClassicURLClassPathHandle implements URLClassPathHandle {
 *
 *     private final int priority;
 *
 *     public ClassicURLClassPathHandle(int priority) {
 *         this.priority = priority;
 *     }
 *
 *     public boolean supports() {
 *         return true; // Always support classic handling
 *     }
 *
 *     public boolean removeURL(ClassLoader classLoader, URL url) {
 *         // Custom logic to remove a URL from the class loader
 *         return false; // Placeholder implementation
 *     }
 *
 *     public int getPriority() {
 *         return priority; // Custom priority
 *     }
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ClassicURLClassPathHandle
 * @see ModernURLClassPathHandle
 * @see Prioritized
 * @since 1.0.0
 */
public interface URLClassPathHandle extends Prioritized {

    /**
     * Supports or not
     *
     * @return if supports, return <code>true</code>, otherwise <code>false</code>
     */
    boolean supports();

    /**
     * Get the Class-Path URLs from the specified {@link ClassLoader}
     *
     * @param classLoader the specified {@link ClassLoader}
     * @return the non-null array of {@link URL URLs}
     */
    @Nonnull
    default URL[] getURLs(@Nullable ClassLoader classLoader) {
        URLClassLoader urlClassLoader = findURLClassLoader(classLoader);
        if (urlClassLoader == null) {
            return EMPTY_URL_ARRAY;
        }
        return urlClassLoader.getURLs();
    }

    /**
     * Initialize the loaders of URL Class-Path from {@link URLClassLoader}
     *
     * @param classLoader {@link ClassLoader}
     * @return <code>true</code> if initialized, otherwise <code>false</code>
     */
    default boolean initializeLoaders(@Nullable ClassLoader classLoader) {
        URLClassLoader urlClassLoader = findURLClassLoader(classLoader);
        if (urlClassLoader == null) {
            return false;
        }
        urlClassLoader.findResource("just-for-initializing-loaders");
        return true;
    }

    /**
     * Remove the Class-Path {@link URL} from the specified {@link ClassLoader}
     *
     * @param classLoader the specified {@link ClassLoader}
     * @param url         the Class-Path {@link URL}
     * @return if removed, return <code>true</code>, otherwise <code>false</code>
     */
    boolean removeURL(@Nullable ClassLoader classLoader, @Nullable URL url);

    /**
     * Get the priority
     *
     * @return the default value is {@link Prioritized#MIN_PRIORITY}
     */
    default int getPriority() {
        return MIN_PRIORITY;
    }
}
