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


/**
 * {@link Wrapper} interface
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface Wrapper {

    /**
     * Returns an object of the given class to allow access to
     * non-standard methods, or standard methods not exposed by the proxy.
     * <p>
     * If the receiver extends or implements the type then the result is the receiver
     * or a proxy for the receiver. If the receiver is a wrapper
     * and the wrapped object extends or implements the type then the result is the
     * wrapped object or a proxy for the wrapped object. Otherwise return the result of calling <code>unwrap</code>
     * recursively on the wrapped object or a proxy for that result. If the receiver is not a
     * wrapper and does not implement the type, then an {@link IllegalArgumentException} is thrown.
     *
     * @param type the wrapped type
     * @param <T>  the wrapped type
     * @return an object of the given class. Maybe a proxy for the actual implementing object.
     * @throws IllegalArgumentException
     */
    <T> T unwrap(Class<T> type) throws IllegalArgumentException;

    /**
     * Returns true if this either extends or implements the type argument or is directly or indirectly a wrapper
     * for an object that does. Returns false otherwise. If this extends or implements the type then return true,
     * else if this is a wrapper then return the result of recursively calling <code>isWrapperFor</code> on the wrapped
     * object. If this does not implement the type and is not a wrapper, return false.
     * This method should be implemented as a low-cost operation compared to <code>unwrap</code> so that
     * callers can use this method to avoid expensive <code>unwrap</code> calls that may fail. If this method
     * returns true then calling <code>unwrap</code> with the same argument should succeed.
     *
     * @param type the wrapped type
     * @return true if this extends or implements the type or directly or indirectly wraps an object that does
     */
    boolean isWrapperFor(Class<?> type);

    /**
     * Try to unwrap the specified object and target type
     *
     * @param object maybe {@link Wrapper}
     * @param type   target type
     * @param <T>    target type
     * @return unwrapped instance if possible, or the original object
     */
    static <T> T tryUnwrap(Object object, Class<T> type) {
        if (object instanceof Wrapper) {
            Wrapper wrapper = (Wrapper) object;
            if (wrapper.isWrapperFor(type)) {
                return wrapper.unwrap(type);
            }
        }
        return null;
    }
}

