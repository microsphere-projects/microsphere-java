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
 * A mutable and non-thread-safe implementation of {@link URLStreamHandlerFactory} that allows dynamic
 * registration and retrieval of {@link URLStreamHandler} instances for specific protocols.
 *
 * <p>
 * This class provides methods to add, remove, and retrieve URL stream handlers dynamically at runtime,
 * making it suitable for environments where protocol handling needs to be modified or extended
 * programmatically.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * MutableURLStreamHandlerFactory factory = new MutableURLStreamHandlerFactory();
 *
 * // Add a custom handler for the "myproto" protocol
 * factory.addURLStreamHandler("myproto", new MyCustomURLStreamHandler());
 *
 * // Retrieve a registered handler
 * URLStreamHandler handler = factory.getURLStreamHandler("myproto");
 *
 * // Remove a handler
 * factory.removeURLStreamHandler("myproto");
 *
 * // Clear all handlers
 * factory.clearHandlers();
 * }</pre>
 *
 * <p>
 * Note: This class is not thread-safe. Concurrent access from multiple threads should be externally
 * synchronized.
 * </p>
 *
 * @param <H> The type of {@link URLStreamHandler} or subtype thereof
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see URLStreamHandlerFactory
 * @see URLStreamHandler
 * @since 1.0.0
 */
public class MutableURLStreamHandlerFactory<H extends URLStreamHandler> implements URLStreamHandlerFactory {

    private final Map<String, H> handlers;

    /**
     * Constructs a new instance
     */
    public MutableURLStreamHandlerFactory() {
        this(null);
    }

    /**
     * Constructs a new instance with the specified handlers
     *
     * @param handlers the handlers
     */
    public MutableURLStreamHandlerFactory(Map<String, H> handlers) {
        this.handlers = handlers == null ? new HashMap<>() : new HashMap<>(handlers);
    }

    /**
     * Adds a new handler for the specified protocol
     *
     * @param protocol the protocol
     * @param handler  the handler
     * @return this instance
     */
    public MutableURLStreamHandlerFactory addURLStreamHandler(String protocol, H handler) {
        this.handlers.put(protocol, handler);
        return this;
    }

    /**
     * Removes the handler for the specified protocol
     *
     * @param protocol the protocol
     * @return the removed handler
     */
    public H removeURLStreamHandler(String protocol) {
        return this.handlers.remove(protocol);
    }

    /**
     * Retrieves the handler for the specified protocol
     *
     * @param protocol the protocol
     * @return the handler
     */
    public H getURLStreamHandler(String protocol) {
        return this.handlers.get(protocol);
    }

    /**
     * Retrieves all handlers
     *
     * @return the handlers
     */
    public Collection<H> getHandlers() {
        return this.handlers.values();
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        return getURLStreamHandler(protocol);
    }

    /**
     * Clears all handlers
     *
     * @return this instance
     */
    public MutableURLStreamHandlerFactory<H> clearHandlers() {
        this.handlers.clear();
        return this;
    }
}
