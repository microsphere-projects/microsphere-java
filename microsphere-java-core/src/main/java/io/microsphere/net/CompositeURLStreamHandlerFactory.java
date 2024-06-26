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

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static java.util.Collections.emptyList;
import static java.util.Collections.sort;

/**
 * The composite {@link URLStreamHandlerFactory} delegates to one or more {@link URLStreamHandlerFactory URLStreamHandlerFactories}
 * {@link #getComparator() in order}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class CompositeURLStreamHandlerFactory implements URLStreamHandlerFactory {

    private final List<URLStreamHandlerFactory> factories;

    public CompositeURLStreamHandlerFactory() {
        this(emptyList());
    }

    public CompositeURLStreamHandlerFactory(Collection<URLStreamHandlerFactory> factories) {
        this((Iterable<URLStreamHandlerFactory>) factories);
    }

    public CompositeURLStreamHandlerFactory(Iterable<URLStreamHandlerFactory> factories) {
        List<URLStreamHandlerFactory> newFactories = newLinkedList(factories);
        sortFactories(newFactories);
        this.factories = newFactories;
    }

    @Override
    public final URLStreamHandler createURLStreamHandler(String protocol) {
        URLStreamHandler handler = null;
        List<URLStreamHandlerFactory> factories = getFactories();
        for (int i = 0; i < factories.size(); i++) {
            URLStreamHandlerFactory factory = factories.get(i);
            handler = factory.createURLStreamHandler(protocol);
            if (handler != null) {
                break;
            }
        }
        return handler;
    }

    /**
     * Add {@link URLStreamHandlerFactory}
     *
     * @param factory {@link URLStreamHandlerFactory}
     * @return
     */
    public CompositeURLStreamHandlerFactory addURLStreamHandlerFactory(URLStreamHandlerFactory factory) {
        if (factory != null && factory != this) {
            List<URLStreamHandlerFactory> factories = this.getFactories();
            if (factory instanceof CompositeURLStreamHandlerFactory) {
                for (URLStreamHandlerFactory element : ((CompositeURLStreamHandlerFactory) factory).getFactories()) {
                    addURLStreamHandlerFactory(element);
                }
            } else if (!factories.contains(factory)) {
                factories.add(factory);
            }
            sortFactories(factories);
        }
        return this;
    }

    /**
     * Get the {@link URLStreamHandlerFactory} delegates;
     *
     * @return non-null
     */
    protected List<URLStreamHandlerFactory> getFactories() {
        return this.factories;
    }

    /**
     * The {@link Comparator} to sort {@link URLStreamHandlerFactory URLStreamHandlerFactories}
     *
     * @return {@link Prioritized#COMPARATOR} as default
     */
    protected Comparator<? super URLStreamHandlerFactory> getComparator() {
        return Prioritized.COMPARATOR;
    }

    private void sortFactories(List<URLStreamHandlerFactory> factories) {
        sort(factories, getComparator());
    }

    @Override
    public String toString() {
        String sb = "CompositeURLStreamHandlerFactory{" + "factories=" + factories +
                '}';
        return sb;
    }
}
