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

import io.microsphere.lang.Prioritized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static io.microsphere.net.URLUtils.resolveBasePath;
import static io.microsphere.reflect.FieldUtils.findField;
import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static java.lang.ClassLoader.getSystemClassLoader;

/**
 * Classic {@link URLClassPathHandle} for {@link sun.misc.URLClassPath}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see sun.misc.URLClassPath
 * @see URLClassPathHandle
 * @since 1.0.0
 */
public class ClassicURLClassPathHandle implements URLClassPathHandle, Prioritized {

    private static final Logger logger = LoggerFactory.getLogger(ClassicURLClassPathHandle.class);

    private static final String URL_CLASS_PATH_CLASS_NAME = "sun.misc.URLClassPath";

    private static final Class<?> URL_CLASS_PATH_CLASS;

    /**
     * {@link URLClassLoader#ucp}
     */
    private static final Field ucpField;

    /**
     * {@link sun.misc.URLClassPath#path}
     */
    private static final Field pathField;

    /**
     * {@link sun.misc.URLClassPath#urls}
     */
    private static final Field urlsField;

    /**
     * {@link sun.misc.URLClassPath#loaders}
     */
    private static final Field loadersField;

    private static final Class<?> loaderClass;

    private static final Field baseField;

    private static final boolean supported;

    static {

        ClassLoader classLoader = getSystemClassLoader();

        URL_CLASS_PATH_CLASS = resolveClass(URL_CLASS_PATH_CLASS_NAME, classLoader);
        ucpField = findField(URLClassLoader.class, "ucp");
        pathField = findField(URL_CLASS_PATH_CLASS, "path");
        urlsField = findField(URL_CLASS_PATH_CLASS, "urls");
        loadersField = findField(URL_CLASS_PATH_CLASS, "loaders");
        loaderClass = URL_CLASS_PATH_CLASS == null ? null : resolveClass(URL_CLASS_PATH_CLASS.getName() + "$Loader", classLoader);
        baseField = findField(loaderClass, "base");

        supported = URL_CLASS_PATH_CLASS != null &&
                ucpField != null &&
                pathField != null &&
                urlsField != null &&
                loadersField != null &&
                loaderClass != null &&
                baseField != null;
    }

    @Override
    public boolean supports() {
        return supported;
    }

    @Override
    public boolean removeURL(URLClassLoader urlClassLoader, URL url) {
        Object urlClassPath = getFieldValue(urlClassLoader, ucpField);
        List<URL> urls = getFieldValue(urlClassPath, urlsField);
        List<URL> path = getFieldValue(urlClassPath, pathField);
        List<Object> loaders = getFieldValue(urlClassPath, loadersField);

        String basePath = resolveBasePath(url);

        boolean removed = false;

        synchronized (urls) {
            urls.remove(url);
            path.remove(url);

            Iterator<Object> iterator = loaders.iterator();
            while (iterator.hasNext()) {
                Object loader = iterator.next();
                URL base = getFieldValue(loader, baseField);
                String basePath_ = resolveBasePath(base);
                if (Objects.equals(basePath_, basePath)) {
                    logger.debug("Remove the Class-Path URL：{}", url);
                    iterator.remove();
                    removed = true;
                    break;
                }
            }
        }
        return removed;
    }

    @Override
    public int getPriority() {
        return MAX_PRIORITY + 99999;
    }
}
