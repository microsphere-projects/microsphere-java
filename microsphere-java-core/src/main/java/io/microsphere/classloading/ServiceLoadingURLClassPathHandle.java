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

import java.net.URL;
import java.util.List;

import static io.microsphere.util.ArrayUtils.isEmpty;
import static io.microsphere.util.ServiceLoaderUtils.loadServicesList;

/**
 * {@link URLClassPathHandle} implementation based on the Service Loading mechanism
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ServiceLoadingURLClassPathHandle implements URLClassPathHandle {

    private URLClassPathHandle delegate;

    public ServiceLoadingURLClassPathHandle() {
        List<URLClassPathHandle> urlClassPathHandles = loadServicesList(URLClassPathHandle.class);
        for (URLClassPathHandle urlClassPathHandle : urlClassPathHandles) {
            if (urlClassPathHandle.supports()) {
                this.delegate = urlClassPathHandle;
                break;
            }
        }
    }

    @Override
    public boolean supports() {
        return delegate != null;
    }

    @Override
    public URL[] getURLs(ClassLoader classLoader) {
        URL[] urls = delegate.getURLs(classLoader);
        return isEmpty(urls) ? URLClassPathHandle.super.getURLs(classLoader) : urls;
    }

    @Override
    public boolean removeURL(ClassLoader classLoader, URL url) {
        return delegate.removeURL(classLoader, url);
    }
}
