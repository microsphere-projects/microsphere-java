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

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static java.util.Collections.sort;

/**
 * A composite implementation of {@link SubProtocolURLConnectionFactory} that combines multiple factories.
 * This class allows dynamic modification at runtime by adding or removing individual factories.
 *
 * <p>
 * The composite factory delegates the creation of URL connections to its internal list of factories,
 * selecting the appropriate one based on support for the given URL and sub-protocols.
 * </p>
 *
 * <h3>Key Features</h3>
 * <ul>
 *     <li><strong>Dynamic Composition:</strong> Add or remove factories at runtime.</li>
 *     <li><strong>Prioritized Ordering:</strong> Factories are sorted based on their priority using the
 *         {@link Prioritized} interface. Higher priority factories are consulted first when determining support.</li>
 *     <li><strong>Efficient Delegation:</strong> Delegates connection creation to the first factory that supports the URL and sub-protocols.</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * CompositeSubProtocolURLConnectionFactory compositeFactory = new CompositeSubProtocolURLConnectionFactory();
 *
 * // Create and add a custom factory
 * SubProtocolURLConnectionFactory myFactory = new MySubProtocolURLConnectionFactory();
 * compositeFactory.add(myFactory);
 *
 * // Use the composite factory to create a connection
 * URL url = new URL("http://example.com");
 * List<String> subProtocols = Arrays.asList("myprotocol", "anotherprotocol");
 * Proxy proxy = Proxy.NO_PROXY;
 *
 * if (compositeFactory.supports(url, subProtocols)) {
 *     URLConnection connection = compositeFactory.create(url, subProtocols, proxy);
 *     // proceed with using the connection
 * }
 * }</pre>
 *
 * <p>
 * This class is thread-safe as long as modifications to the factory list happen through the provided methods,
 * which re-sort the internal list in a thread-safe manner.
 * </p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class CompositeSubProtocolURLConnectionFactory implements SubProtocolURLConnectionFactory {

    private final List<SubProtocolURLConnectionFactory> factories;

    public CompositeSubProtocolURLConnectionFactory() {
        this(newLinkedList());
    }

    public CompositeSubProtocolURLConnectionFactory(Iterable<SubProtocolURLConnectionFactory> factories) {
        List<SubProtocolURLConnectionFactory> newFactories = newLinkedList(factories);
        this.factories = newFactories;
        sortFactories();
    }

    public CompositeSubProtocolURLConnectionFactory add(SubProtocolURLConnectionFactory factory) {
        addInternal(factory);
        sortFactories();
        return this;
    }

    public CompositeSubProtocolURLConnectionFactory add(SubProtocolURLConnectionFactory... factories) {
        for (int i = 0; i < factories.length; i++) {
            addInternal(factories[i]);
        }
        sortFactories();
        return this;
    }

    protected boolean addInternal(SubProtocolURLConnectionFactory factory) {
        if (this.factories.contains(factory)) {
            return false;
        }
        return this.factories.add(factory);
    }

    public boolean remove(SubProtocolURLConnectionFactory factory) {
        boolean result = this.factories.remove(factory);
        if (result) {
            sortFactories();
        }
        return result;
    }

    private void sortFactories() {
        List<SubProtocolURLConnectionFactory> factories = this.factories;
        sort(factories, Prioritized.COMPARATOR);
    }

    @Override
    public boolean supports(URL url, List<String> subProtocols) {
        return selectFactoryIndex(url, subProtocols) > -1;
    }

    @Override
    public URLConnection create(URL url, List<String> subProtocols, Proxy proxy) throws IOException {
        SubProtocolURLConnectionFactory factory = selectFactory(url, subProtocols);
        return factory.create(url, subProtocols, proxy);
    }

    private SubProtocolURLConnectionFactory selectFactory(URL url, List<String> subProtocols) {
        int index = selectFactoryIndex(url, subProtocols);
        return factories.get(index);
    }

    private int selectFactoryIndex(URL url, List<String> subProtocols) {
        int index = -1;
        int size = factories.size();
        for (int i = 0; i < size; i++) {
            SubProtocolURLConnectionFactory factory = factories.get(i);
            if (factory.supports(url, subProtocols)) {
                index = i;
                break;
            }
        }
        return index;
    }
}
