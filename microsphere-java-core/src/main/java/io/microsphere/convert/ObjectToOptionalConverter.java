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
package io.microsphere.convert;

import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * The class to convert {@link Object} to {@link Optional}
 *
 * @since 1.0.0
 */
public class ObjectToOptionalConverter implements Converter<Object, Optional> {

    /**
     * Singleton instance of {@link ObjectToOptionalConverter}.
     */
    public static final ObjectToOptionalConverter INSTANCE = new ObjectToOptionalConverter();

    @Override
    public Optional convert(Object source) {
        return ofNullable(source);
    }

    @Override
    public int getPriority() {
        return MIN_PRIORITY;
    }
}
