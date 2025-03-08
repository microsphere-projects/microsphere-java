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

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Mutable {@link URLStreamHandlerFactory} that is not thread-safe extends {@link URLStreamHandler}.
 *
 * @param <H> The type of {@link URLStreamHandler} or the subtype of {@link URLStreamHandler}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see URLStreamHandlerFactory
 * @see URLStreamHandler
 * @since 1.0.0
 */
public class MutableURLStreamHandlerFactory<H extends URLStreamHandler> implements URLStreamHandlerFactory {

    private final Map<String, H> handlers;

    public MutableURLStreamHandlerFactory() {
        this(null);
    }

    public MutableURLStreamHandlerFactory(Map<String, H> handlers) {
        this.handlers = handlers == null ? new HashMap<>() : new HashMap<>(handlers);
    }

    public MutableURLStreamHandlerFactory addURLStreamHandler(String protocol, H handler) {
        this.handlers.put(protocol, handler);
        return this;
    }

    public H removeURLStreamHandler(String protocol) {
        return this.handlers.remove(protocol);
    }

    public H getURLStreamHandler(String protocol) {
        return this.handlers.get(protocol);
    }

    public Collection<H> getHandlers() {
        return this.handlers.values();
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        return getURLStreamHandler(protocol);
    }

    public MutableURLStreamHandlerFactory<H> clearHandlers() {
        this.handlers.clear();
        return this;
    }
}
