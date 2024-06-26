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
import java.util.Collections;
import java.util.List;

import static io.microsphere.collection.ListUtils.newLinkedList;

/**
 * The composite {@link SubProtocolURLConnectionFactory} class supports modified dynamically at runtime.
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
        sort();
    }

    public CompositeSubProtocolURLConnectionFactory add(SubProtocolURLConnectionFactory factory) {
        addInternal(factory);
        sort();
        return this;
    }

    public CompositeSubProtocolURLConnectionFactory add(SubProtocolURLConnectionFactory... factories) {
        for (int i = 0; i < factories.length; i++) {
            addInternal(factories[i]);
        }
        sort();
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
            sort();
        }
        return result;
    }

    private void sort() {
        List<SubProtocolURLConnectionFactory> factories = this.factories;
        Collections.sort(factories, Prioritized.COMPARATOR);
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
