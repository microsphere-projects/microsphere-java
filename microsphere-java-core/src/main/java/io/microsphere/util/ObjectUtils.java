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

package io.microsphere.util;

import io.microsphere.annotation.Nullable;

/**
 * The utility class for {@link Object}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Object
 * @since 1.0.0
 */
public abstract class ObjectUtils implements Utils {

    /**
     * Returns the given object if it is non-null, otherwise returns the default value.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String result = ObjectUtils.defaultIfNull(null, "default"); // returns "default"
     * String result2 = ObjectUtils.defaultIfNull("value", "default"); // returns "value"
     * }</pre>
     * <p>
     * The source from org.apache.commons.lang3.ObjectUtils#defaultIfNull(Object, Object)
     *
     * @param object       the object to check
     * @param defaultValue the default value to return if the object is null
     * @param <T>          the type of the object
     * @return the object if non-null, otherwise the default value
     */
    public static <T> T defaultIfNull(@Nullable T object, @Nullable T defaultValue) {
        return object != null ? object : defaultValue;
    }

    private ObjectUtils() {
    }
}