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
package io.microsphere.reflect;

import io.microsphere.lang.function.ThrowableConsumer;
import io.microsphere.lang.function.ThrowableFunction;
import io.microsphere.lang.function.ThrowableSupplier;
import io.microsphere.util.BaseUtils;

import java.lang.reflect.AccessibleObject;

/**
 * The utilities class of {@link AccessibleObject}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AccessibleObject
 * @since 1.0.0
 */
public abstract class AccessibleObjectUtils extends BaseUtils {

    /**
     * Executes the {@link AccessibleObject}
     *
     * @param accessibleObject {@link AccessibleObject}
     * @param supplier         {@link ThrowableConsumer}
     * @throws RuntimeException if execution failed
     */
    public static <R> R execute(AccessibleObject accessibleObject, ThrowableSupplier<R> supplier) {
        boolean accessible = accessibleObject.isAccessible();
        R result = null;
        try {
            accessibleObject.setAccessible(true);
            result = supplier.execute();
        } finally {
            accessibleObject.setAccessible(accessible);
        }
        return result;
    }
}
