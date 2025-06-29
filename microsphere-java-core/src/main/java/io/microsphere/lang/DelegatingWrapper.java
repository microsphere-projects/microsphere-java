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
package io.microsphere.lang;

import static io.microsphere.constants.SymbolConstants.QUOTE;

/**
 * A delegating {@link Wrapper} interface that provides default implementations for unwrapping and checking
 * wrapped object types. This interface is designed to be extended by classes that want to provide a delegated
 * implementation of the wrapper pattern.
 *
 * <p>
 * The main purpose of this interface is to simplify the implementation of the {@link Wrapper} interface by
 * delegating the actual wrapping and unwrapping logic to the {@link #getDelegate()} method, which must be
 * implemented by subclasses.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class MyWrapper implements DelegatingWrapper {
 *     private final Object delegate;
 *
 *     public MyWrapper(Object delegate) {
 *         this.delegate = delegate;
 *     }
 *
 *     @Override
 *     public Object getDelegate() {
 *         return delegate;
 *     }
 * }
 * }</pre>
 *
 * <p>
 * In this example, the {@code MyWrapper} class implements the {@code DelegatingWrapper} interface and provides
 * a concrete implementation of the {@link #getDelegate()} method. This allows it to inherit the default
 * implementations of the {@link #unwrap(Class)} and {@link #isWrapperFor(Class)} methods from the
 * {@code DelegatingWrapper} interface.
 * </p>
 *
 * <p>
 * If you have an instance of a class that implements this interface, you can use it to unwrap to a specific type:
 * </p>
 *
 * <pre>{@code
 * MyWrapper wrapper = new MyWrapper(new SomeImplementation());
 * SomeImplementation impl = wrapper.unwrap(SomeImplementation.class);
 * }</pre>
 *
 * <p>
 * You can also check if the wrapped object is or can be unwrapped to a specific type:
 * </p>
 *
 * <pre>{@code
 * if (wrapper.isWrapperFor(SomeImplementation.class)) {
 *     SomeImplementation impl = wrapper.unwrap(SomeImplementation.class);
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface DelegatingWrapper extends Wrapper {

    /**
     * Get the delegate
     *
     * @return the delegate
     */
    Object getDelegate();

    @Override
    default <T> T unwrap(Class<T> type) throws IllegalArgumentException {
        if (getClass().equals(type)) {
            return (T) this;
        } else {
            if (isWrapperFor(type)) {
                return (T) getDelegate();
            }
        }
        throw new IllegalArgumentException(getClass().getName() + " can't unwrap the given type '" + type.getName() + QUOTE);
    }

    @Override
    default boolean isWrapperFor(Class<?> type) {
        Object delegate = getDelegate();
        return type.isInstance(delegate);
    }
}
