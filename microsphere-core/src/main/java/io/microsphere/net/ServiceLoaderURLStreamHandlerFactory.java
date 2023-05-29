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

import io.microsphere.lang.Prioritized;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import static io.microsphere.collection.MapUtils.ofEntry;
import static io.microsphere.collection.MapUtils.toFixedMap;
import static io.microsphere.net.URLUtils.attachURLStreamHandlerFactory;
import static io.microsphere.util.ServiceLoaderUtils.loadServicesList;

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
            return Collections.emptyMap();
        }

        // sort
        Collections.sort(handlers, Prioritized.COMPARATOR);

        Map<String, ExtendableProtocolURLStreamHandler> handlersMap = toFixedMap(
                handlers, handler -> ofEntry(handler.getProtocol(), handler));

        return handlersMap;

    }
}
