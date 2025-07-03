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
package io.microsphere.net;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import static io.microsphere.collection.MapUtils.immutableEntry;
import static io.microsphere.collection.MapUtils.toFixedMap;
import static io.microsphere.net.URLUtils.attachURLStreamHandlerFactory;
import static io.microsphere.util.ServiceLoaderUtils.loadServicesList;
import static java.util.Collections.emptyMap;

/**
 * A {@link URLStreamHandlerFactory} implementation that uses the JDK's {@link ServiceLoader}
 * to load and compose multiple delegates for creating {@link URLStreamHandler} instances.
 *
 * <p>{@link ServiceLoaderURLStreamHandlerFactory} extends from {@link DelegatingURLStreamHandlerFactory},
 * delegating to a composite chain of factories and handlers discovered via service loading.
 * It ensures extensibility by allowing custom protocol handling through service provider implementations.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Register the factory globally with the URL class
 * ServiceLoaderURLStreamHandlerFactory.attach();
 * }</pre>
 *
 * <h3>How It Works</h3>
 * 1. Loads all available {@link URLStreamHandlerFactory} implementations using the service loader mechanism.<br>
 * 2. Composes them in prioritized order using a {@link CompositeURLStreamHandlerFactory}.<br>
 * 3. Adds a fallback handler for extendable protocols, if any are found.<br>
 * 4. Delegates creation of stream handlers to this composed factory chain.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see URLStreamHandlerFactory
 * @see ServiceLoader
 * @see CompositeURLStreamHandlerFactory
 * @see ExtendableProtocolURLStreamHandler
 * @since 1.0.0
 */
public class ServiceLoaderURLStreamHandlerFactory extends DelegatingURLStreamHandlerFactory {

    /**
     * Attach {@link ServiceLoaderURLStreamHandlerFactory} into {@link URL}
     * {@link URLUtils#attachURLStreamHandlerFactory(URLStreamHandlerFactory)}
     */
    public static void attach() {
        ServiceLoaderURLStreamHandlerFactory factory = new ServiceLoaderURLStreamHandlerFactory();
        attachURLStreamHandlerFactory(factory);
    }

    public ServiceLoaderURLStreamHandlerFactory() {
        super(createDelegate());
    }

    private static URLStreamHandlerFactory createDelegate() {
        Iterable<URLStreamHandlerFactory> factories = loadServicesList(URLStreamHandlerFactory.class);
        MutableURLStreamHandlerFactory fallbackFactory = new MutableURLStreamHandlerFactory(loadHandlers());
        CompositeURLStreamHandlerFactory compositeFactory = new CompositeURLStreamHandlerFactory(factories);
        compositeFactory.addURLStreamHandlerFactory(fallbackFactory);
        return compositeFactory;
    }

    private static Map<String, ExtendableProtocolURLStreamHandler> loadHandlers() {
        List<ExtendableProtocolURLStreamHandler> handlers = loadServicesList(ExtendableProtocolURLStreamHandler.class);

        int size = handlers.size();
        if (size < 1) {
            return emptyMap();
        }

        Map<String, ExtendableProtocolURLStreamHandler> handlersMap = toFixedMap(
                handlers, handler -> immutableEntry(handler.getProtocol(), handler));

        return handlersMap;

    }
}
