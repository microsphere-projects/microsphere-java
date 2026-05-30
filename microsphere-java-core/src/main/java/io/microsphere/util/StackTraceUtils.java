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

import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static java.lang.Thread.currentThread;

/**
 * The utility class for Stack Trace
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see StackTraceElement
 * @since 1.0.0
 */
public abstract class StackTraceUtils implements Utils {

    private static final Class<?> TYPE = StackTraceUtils.class;

    /**
     * {@link StackTraceElement} invocation frame offset
     */
    private static final int invocationFrameOffset;

    // Initialize java.lang.StackTraceElement
    static {
        int offset = 0;
        // Use java.lang.StackTraceElement to calculate frame
        StackTraceElement[] stackTraceElements = getStackTrace();
        for (; ; offset++) {
            StackTraceElement stackTraceElement = stackTraceElements[offset];
            String className = stackTraceElement.getClassName();
            if (TYPE.getName().equals(className)) {
                break;
            }
        }
        invocationFrameOffset = offset;
    }

    /**
     * Get caller class from {@link Thread#getStackTrace() stack traces}
     *
     * @return Caller Class
     * @see #getCallerClassInStatckTrace(int)
     */
    public static Class<?> getCallerClassInStatckTrace() {
        // Plus 1 , because Invocation getStackTrace() method was considered as increment invocation frame
        // Plus 1 , because Invocation getCallerClassNameInStackTrace(int) method was considered as increment invocation frame
        // Plus 1 , because Invocation getCallerClassInStatckTrace(int) method was considered as increment invocation frame
        return getCallerClassInStatckTrace(invocationFrameOffset + 3);
    }

    /**
     * General implementation, get the calling class name
     *
     * @return call class name
     * @see #getCallerClassNameInStackTrace(int)
     */
    public static String getCallerClassNameInStackTrace() {
        // Plus 1 , because Invocation getStackTrace() method was considered as increment invocation frame
        // Plus 1 , because Invocation getCallerClassNameInStackTrace() method was considered as increment invocation frame
        // Plus 1 , because Invocation getCallerClassNameInStackTrace(int) method was considered as increment invocation frame
        return getCallerClassNameInStackTrace(invocationFrameOffset + 3);
    }

    /**
     * Get caller class in General JVM
     *
     * @param invocationFrame invocation frame
     * @return caller class
     * @see #getCallerClassNameInStackTrace(int)
     */
    public static Class<?> getCallerClassInStatckTrace(int invocationFrame) {
        // Plus 1 , because Invocation getCallerClassNameInStackTrace(int) method was considered as increment invocation frame
        String className = getCallerClassNameInStackTrace(invocationFrame + 1);
        return className == null ? null : resolveClass(className);
    }

    /**
     * General implementation, get the calling class name by specifying the calling level value
     *
     * @param invocationFrame invocation frame
     * @return specified invocation frame class
     */
    public static String getCallerClassNameInStackTrace(int invocationFrame) throws IndexOutOfBoundsException {
        StackTraceElement[] elements = getStackTrace();
        if (invocationFrame < elements.length) {
            StackTraceElement targetStackTraceElement = elements[invocationFrame];
            return targetStackTraceElement.getClassName();
        }
        return null;
    }

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