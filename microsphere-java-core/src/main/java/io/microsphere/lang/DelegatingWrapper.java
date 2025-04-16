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
 * Delegating {@link Wrapper}
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
