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

import static io.microsphere.util.Assert.assertNotNull;

/**
 * A delegating implementation of {@link URLStreamHandlerFactory} that forwards all calls to a provided delegate factory.
 * <p>
 * This class is useful when you want to wrap or modify the behavior of an existing {@link URLStreamHandlerFactory}
 * instance, such as adding custom logic before or after delegation, without directly modifying its implementation.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Create a custom URLStreamHandlerFactory
 * URLStreamHandlerFactory customFactory = protocol -> {
 *     if ("http".equals(protocol)) {
 *         return new MyCustomHttpURLStreamHandler();
 *     }
 *     return null;
 * };
 *
 * // Wrap it with DelegatingURLStreamHandlerFactory
 * DelegatingURLStreamHandlerFactory delegatingFactory = new DelegatingURLStreamHandlerFactory(customFactory);
 *
 * // Set the delegating factory as the default
 * URL.setURLStreamHandlerFactory(delegatingFactory);
 * }</pre>
 *
 * <p>
 * In this example, any request for a URL handler will first go through the {@link DelegatingURLStreamHandlerFactory},
 * which in turn delegates the handling to the wrapped custom factory.
 * </p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class DelegatingURLStreamHandlerFactory implements URLStreamHandlerFactory {

    private final URLStreamHandlerFactory delegate;

    /**
     * Constructs a new instance with the specified delegate factory.
     *
     * @param delegate the delegate factory to which calls will be forwarded, must not be {@code null}
     * @throws IllegalArgumentException if the provided delegate is {@code null}
     */
    public DelegatingURLStreamHandlerFactory(URLStreamHandlerFactory delegate) {
        assertNotNull(delegate, () -> "The 'delegate' argument must not be null!");
        this.delegate = delegate;
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        return delegate.createURLStreamHandler(protocol);
    }

    /**
     * Get the delegate of {@link URLStreamHandlerFactory}
     *
     * @return non-null
     */
    protected final URLStreamHandlerFactory getDelegate() {
        return delegate;
    }
}
