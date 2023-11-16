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

import javax.annotation.Nonnull;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static io.microsphere.net.URLUtils.EMPTY_URL_ARRAY;

/**
 * The handle interface for URL Class-Path
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ClassicURLClassPathHandle
 * @see ModernURLClassPathHandle
 * @since 1.0.0
 */
public interface URLClassPathHandle {

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
    default URL[] getURLs(ClassLoader classLoader) {
        if (classLoader instanceof URLClassLoader) {
            return ((URLClassLoader) classLoader).getURLs();
        }
        return EMPTY_URL_ARRAY;
    }

    /**
     * Remove the Class-Path {@link URL} from the specified {@link ClassLoader}
     *
     * @param classLoader the specified {@link ClassLoader}
     * @param url         the Class-Path {@link URL}
     * @return if removed, return <code>true</code>, otherwise <code>false</code>
     */
    boolean removeURL(ClassLoader classLoader, URL url);
}
