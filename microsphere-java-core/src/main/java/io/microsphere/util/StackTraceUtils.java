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

import io.microsphere.annotation.Nonnull;

import static java.lang.Thread.currentThread;

/**
 * The utility class for Stack Trace
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see StackTraceElement
 * @since 1.0.0
 */
public abstract class StackTraceUtils implements Utils {

    /**
     * Get the {@link StackTraceElement} array on the current thread
     *
     * @return non-null
     */
    /**
     * Retrieves the stack trace elements for the current thread's call stack.
     *
     * <p>This method is useful for inspecting the sequence of method calls that led to the current point of execution.
     * It can be used for debugging, logging, or monitoring purposes.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * StackTraceElement[] stackTrace = StackTraceUtils.getStackTrace();
     * for (StackTraceElement element : stackTrace) {
     *     System.out.println(element);
     * }
     * }</pre>
     *
     * @return a non-null array of {@link StackTraceElement} representing the current thread's stack trace
     */
    @Nonnull
    public static StackTraceElement[] getStackTrace() {
        return currentThread().getStackTrace();
    }

    private StackTraceUtils() {
    }
}