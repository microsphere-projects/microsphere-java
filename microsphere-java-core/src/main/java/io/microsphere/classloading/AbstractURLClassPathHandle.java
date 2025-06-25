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
import io.microsphere.lang.Prioritized;
import io.microsphere.logging.Logger;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.net.URLUtils.EMPTY_URL_ARRAY;
import static io.microsphere.net.URLUtils.resolveBasePath;
import static io.microsphere.reflect.FieldUtils.findField;
import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static java.lang.ClassLoader.getSystemClassLoader;

/**
 * Abstract implementation of {@link URLClassPathHandle} that provides a base for handling URL Class-Path entries.
 *
 * <p>This class offers reflective access to internal structures like the {@code ucp}, {@code urls}, and
 * {@code loaders} fields used by the JVM's class loading mechanism. It is primarily designed for advanced use cases
 * where fine-grained control over class loader URLs is required, such as dynamic removal or modification of URLs from
 * a class loader.
 *
 * <h2>Key Features:</h2>
 * <ul>
 *   <li>Supports reflective manipulation of internal JVM class loading structures.</li>
 *   <li>Provides abstraction over different versions of JVM internals (e.g., classic vs modern JDK).</li>
 *   <li>Implements the {@link Prioritized} interface to allow prioritization among multiple handlers.</li>
 * </ul>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * public class ClassicURLClassPathHandle extends AbstractURLClassPathHandle {
 *
 *     public ClassicURLClassPathHandle() {
 *         // Default constructor
 *     }
 *
 *     protected String getURLClassPathClassName() {
 *         return "sun.misc.URLClassPath";
 *     }
 *
 *     protected String getUrlsFieldName() {
 *         return "urls"; // Classic field name
 *     }
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ClassicURLClassPathHandle
 * @see ModernURLClassPathHandle
 * @see URLClassPathHandle
 * @see Prioritized
 * @since 1.0.0
 */
public abstract class AbstractURLClassPathHandle implements URLClassPathHandle, Prioritized {

    /**
     * The default priority is {@link Prioritized#MAX_PRIORITY} + 99999
     */
    public static final int DEFAULT_PRIORITY = MAX_PRIORITY + 99999;

    private final Logger logger = getLogger(getClass());

    private Class<?> urlClassPathClass;

    /**
     * {@link sun.misc.URLClassPath#path}
     */
    private Field pathField;

    /**
     * {@link sun.misc.URLClassPath#urls}
     */
    private Field urlsField;

    /**
     * {@link sun.misc.URLClassPath#loaders}
     */
    private Field loadersField;

    private Class<?> loaderClass;

    private Field baseField;

    private int priority;

    public AbstractURLClassPathHandle() {
        setPriority(DEFAULT_PRIORITY);
    }

    @Override
    public boolean supports() {
        return getUrlClassPathClass() != null;
    }

    @Nonnull
    @Override
    public URL[] getURLs(ClassLoader classLoader) {
        if (classLoader == null) {
            return EMPTY_URL_ARRAY;
        }
        Object ucp = getFieldValue(classLoader, findUcpField(classLoader));
        return ucp == null ? EMPTY_URL_ARRAY : invokeMethod(ucp, "getURLs");
    }

    @Override
    public final boolean removeURL(ClassLoader classLoader, URL url) {
        if (classLoader == null || url == null) {
            return false;
        }

        initializeLoaders(classLoader);

        Object ucp = getFieldValue(classLoader, findUcpField(classLoader));
        Collection<URL> urls = getFieldValue(ucp, getUrlsField());
        Collection<URL> path = getFieldValue(ucp, getPathField());
        Collection<Object> loaders = getFieldValue(ucp, getLoadersField());

        String basePath = resolveBasePath(url);

        boolean removed = false;

        synchronized (urls) {

            Iterator<Object> iterator = loaders.iterator();
            while (iterator.hasNext()) {
                Object loader = iterator.next();
                URL base = getFieldValue(loader, getBaseField());
                String basePath_ = resolveBasePath(base);
                if (Objects.equals(basePath_, basePath)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Remove the Class-Path URL：{}", url);
                    }
                    iterator.remove();
                    urls.remove(url);
                    path.remove(url);
                    removed = true;
                    break;
                }
            }
        }
        return removed;
    }

    public final void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public final int getPriority() {
        return priority;
    }

    protected final Class<?> getUrlClassPathClass() {
        Class<?> urlClassPathClass = this.urlClassPathClass;
        if (urlClassPathClass == null) {
            ClassLoader classLoader = getSystemClassLoader();
            urlClassPathClass = resolveClass(getURLClassPathClassName(), classLoader);
            this.urlClassPathClass = urlClassPathClass;
        }
        return urlClassPathClass;
    }

    protected final Class<?> getLoaderClass() {
        Class<?> loaderClass = this.loaderClass;
        if (loaderClass == null) {
            ClassLoader classLoader = getSystemClassLoader();
            loaderClass = resolveClass(getURLClassPathClassName() + "$Loader", classLoader);
            this.loaderClass = loaderClass;
        }
        return loaderClass;
    }

    protected final Field findUcpField(ClassLoader classLoader) {
        return findField(classLoader, "ucp");
    }

    protected final Field getPathField() {
        Field pathField = this.pathField;
        if (pathField == null) {
            pathField = findField(getUrlClassPathClass(), "path");
            this.pathField = pathField;
        }
        return pathField;
    }

    protected final Field getUrlsField() {
        Field urlsField = this.urlsField;
        if (urlsField == null) {
            urlsField = findField(getUrlClassPathClass(), getUrlsFieldName());
            this.urlsField = urlsField;
        }
        return urlsField;
    }

    protected final Field getLoadersField() {
        Field loadersField = this.loadersField;
        if (loadersField == null) {
            loadersField = findField(getUrlClassPathClass(), "loaders");
            this.loadersField = loadersField;
        }
        return loadersField;
    }

    protected final Field getBaseField() {
        Field baseField = this.baseField;
        if (baseField == null) {
            baseField = findField(getLoaderClass(), "base");
            this.baseField = baseField;
        }
        return baseField;
    }

    protected abstract String getURLClassPathClassName();

    protected abstract String getUrlsFieldName();
}
