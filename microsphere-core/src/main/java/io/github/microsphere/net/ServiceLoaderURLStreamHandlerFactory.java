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
package io.github.microsphere.net;

import io.github.microsphere.lang.Prioritized;
import io.github.microsphere.util.ClassLoaderUtils;
import io.github.microsphere.util.CollectionUtils;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import static java.util.Collections.unmodifiableMap;
import static java.util.ServiceLoader.load;

/**
 * Decorating {@link URLStreamHandlerFactory} class delegates the composite of {@link URLStreamHandlerFactory} and
 * {@link URLStreamHandler} instances that are loaded by the JDK's {@link ServiceLoader}.
 * <p>
 * First, the {@link #createURLStreamHandler(String)} method tries to create an instance of {@link URLStreamHandler}
 * vid each {@link URLStreamHandlerFactory} delegate in the {@link Prioritized prioritized} order, once some one returns
 * a non-null result, it will be taken. Otherwise, {@link ExtendableProtocolURLStreamHandler} delegates will be used to resolve the
 * result if possible.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Prioritized
 * @since 1.0.0
 */
public class ServiceLoaderURLStreamHandlerFactory extends DelegatingURLStreamHandlerFactory {

    private final Map<String, ExtendableProtocolURLStreamHandler> handlers;

    public ServiceLoaderURLStreamHandlerFactory() {
        super(createDelegate());
        ClassLoader classLoader = getClass().getClassLoader();
        this.handlers = loadHandlers(classLoader);
    }

    private static URLStreamHandlerFactory createDelegate() {
        ClassLoader classLoader = ClassLoaderUtils.getClassLoader();
        Iterable<URLStreamHandlerFactory> factories = load(URLStreamHandlerFactory.class, classLoader);
        return new CompositeURLStreamHandlerFactory(factories);
    }

    private Map<String, ExtendableProtocolURLStreamHandler> loadHandlers(ClassLoader classLoader) {
        List<ExtendableProtocolURLStreamHandler> handlers = CollectionUtils.toList(load(ExtendableProtocolURLStreamHandler.class, classLoader));
        int size = handlers.size();
        if (size < 1) {
            return Collections.emptyMap();
        }

        // sort
        Collections.sort(handlers, Prioritized.COMPARATOR);

        Map<String, ExtendableProtocolURLStreamHandler> handlersMap = new HashMap<>(size, Float.MIN_NORMAL);
        for (int i = 0; i < size; i++) {
            ExtendableProtocolURLStreamHandler handler = handlers.get(i);
            handlersMap.put(handler.getProtocol(), handler);
        }

        return unmodifiableMap(handlersMap);
    }

    @Override
    protected URLStreamHandler findURLStreamHandler(String protocol) {
        return handlers.get(protocol);
    }
}
