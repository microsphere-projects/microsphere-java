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

import io.microsphere.logging.Logger;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.reflect.MethodUtils.invokeStaticMethod;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.VersionUtils.CURRENT_JAVA_VERSION;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_9;
import static java.lang.Thread.currentThread;

/**
 * The utility class for Stack Trace
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see StackTraceElement
 * @since 1.0.0
 */
public class StackTraceUtils extends BaseUtils {

    private static final Class<?> TYPE = StackTraceUtils.class;

    private static final Logger logger = getLogger(TYPE);

    private static final boolean IS_JDK_9_OR_LATER = CURRENT_JAVA_VERSION.ge(JAVA_VERSION_9);

    /**
     * The class name of {@linkplain java.lang.StackWalker} that was introduced in JDK 9.
     */
    public static final String STACK_WALKER_CLASS_NAME = "java.lang.StackWalker";

    /**
     * The class name of {@linkplain java.lang.StackWalker.Option} that was introduced in JDK 9.
     */
    public static final String STACK_WALKER_OPTION_CLASS_NAME = "java.lang.StackWalker$Option";

    /**
     * The class name of {@linkplain java.lang.StackWalker.StackFrame} that was introduced in JDK 9.
     */
    public static final String STACK_WALKER_STACK_FRAME_CLASS_NAME = "java.lang.StackWalker$StackFrame";

    /**
     * The {@link Class} of {@linkplain java.lang.StackWalker} that was introduced in JDK 9.
     * (optional)
     */
    public static final @Nullable Class<?> STACK_WALKER_CLASS;

    /**
     * The {@link Class} of {@linkplain java.lang.StackWalker.Option} that was introduced in JDK 9.
     * (optional)
     */
    public static final @Nullable Class<?> STACK_WALKER_OPTION_CLASS;

    /**
     * The {@link Class} of {@linkplain java.lang.StackWalker.StackFrame} that was introduced in JDK 9.
     * (optional)
     */
    public static final @Nullable Class<?> STACK_WALKER_STACK_FRAME_CLASS;

    /**
     * The name of {@linkplain java.lang.StackWalker.Option#RETAIN_CLASS_REFERENCE}
     */
    static final String RETAIN_CLASS_REFERENCE_OPTION_NAME = "RETAIN_CLASS_REFERENCE";

    /**
     * The name of {@linkplain java.lang.StackWalker.Option#SHOW_REFLECT_FRAMES}
     */
    static final String SHOW_REFLECT_FRAMES_OPTION_NAME = "SHOW_REFLECT_FRAMES";

    /**
     * The name of {@linkplain java.lang.StackWalker.Option#SHOW_HIDDEN_FRAMES}
     */
    static final String SHOW_HIDDEN_FRAMES_OPTION_NAME = "SHOW_HIDDEN_FRAMES";

    /**
     * The {@link Method method} name of {@linkplain java.lang.StackWalker#getInstance()}
     */
    static final String GET_INSTANCE_METHOD_NAME = "getInstance";

    /**
     * The {@link Method method} name of {{@linkplain java.lang.StackWalker#walk(java.util.function.Function)}
     */
    static final String WALK_METHOD_NAME = "walk";

    /**
     * The {@link Method method} name of {@linkplain java.lang.StackWalker.StackFrame#getClassName()}
     */
    static final String GET_CLASS_NAME_METHOD_NAME = "getClassName";

    static final Method WALK_METHOD;

    static final Method GET_CLASS_NAME_METHOD;

    private static final Object stackWalkerInstance;

    private static final Function<Stream<?>, Object> getClassNamesFunction = StackTraceUtils::getCallerClassNames;

    /**
     * {@link StackTraceElement} invocation frame.
     */
    private static final int stackTraceElementInvocationFrame;

    /**
     * {@linkplain java.lang.StackWalker} invocation frame.
     */
    private static final int stackWalkerInvocationFrame;

    // Initialize java.lang.StackWalker
    static {
        Class<?> stackWalkerClass = null;
        Class<?> stackWalkerOptionClass = null;
        Class<?> stackWalkerStackFrameClass = null;
        Method walkMethod = null;
        Method getClassNameMethod = null;
        Object stackWalker = null;

        if (IS_JDK_9_OR_LATER) {
            stackWalkerClass = resolveClass(STACK_WALKER_CLASS_NAME);
            stackWalkerOptionClass = resolveClass(STACK_WALKER_OPTION_CLASS_NAME);
            stackWalkerStackFrameClass = resolveClass(STACK_WALKER_STACK_FRAME_CLASS_NAME);
            walkMethod = findMethod(stackWalkerClass, WALK_METHOD_NAME, Function.class);
            walkMethod.setAccessible(true);
            getClassNameMethod = findMethod(stackWalkerStackFrameClass, GET_CLASS_NAME_METHOD_NAME);
            getClassNameMethod.setAccessible(true);
            stackWalker = invokeStaticMethod(stackWalkerClass, GET_INSTANCE_METHOD_NAME);
        }

        STACK_WALKER_CLASS = stackWalkerClass;
        STACK_WALKER_OPTION_CLASS = stackWalkerOptionClass;
        STACK_WALKER_STACK_FRAME_CLASS = stackWalkerStackFrameClass;
        WALK_METHOD = walkMethod;
        GET_CLASS_NAME_METHOD = getClassNameMethod;
        stackWalkerInstance = stackWalker;

        int invocationFrame = 0;

        if (IS_JDK_9_OR_LATER) {
            List<String> stackFrameClassNames = getCallerClassNames();
            for (String stackFrameClassName : stackFrameClassNames) {
                if (TYPE.getName().equals(stackFrameClassName)) {
                    break;
                }
                invocationFrame++;
            }
        }
        stackWalkerInvocationFrame = invocationFrame + 2;
    }

    // Initialize java.lang.StackTraceElement
    static {
        int invocationFrame = 0;
        // Use java.lang.StackTraceElement to calculate frame
        StackTraceElement[] stackTraceElements = getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            String className = stackTraceElement.getClassName();
            if (TYPE.getName().equals(className)) {
                break;
            }
            invocationFrame++;
        }
        // Plugs 1 , because Invocation getStackTrace() method was considered as increment invocation frame
        // Plugs 1 , because Invocation getCallerClassName() method was considered as increment invocation frame
        // Plugs 1 , because Invocation getCallerClassNameInGeneralJVM() method was considered as increment invocation frame
        // Plugs 1 , because Invocation getCallerClassNameInGeneralJVM(int) method was considered as increment invocation frame
        stackTraceElementInvocationFrame = invocationFrame + 4;
    }

    /**
     * Get the {@link StackTraceElement} array on the current thread
     *
     * @return non-null
     */
    public static StackTraceElement[] getStackTrace() {
        return currentThread().getStackTrace();
    }

    public static String getCallerClassName() {
        if (stackWalkerInstance == null) {
            return getCallerClassNameInGeneralJVM();
        }
        List<String> callerClassNames = getCallerClassNames();
        String className = callerClassNames.get(stackWalkerInvocationFrame);
        return className;
    }

    static List<String> getCallerClassNames() {
        return invokeMethod(stackWalkerInstance, WALK_METHOD, getClassNamesFunction);
    }

    private static List<String> getCallerClassNames(Stream<?> stackFrames) {
        return stackFrames.limit(5)
                .map(StackTraceUtils::getClassName)
                .collect(Collectors.toList());
    }

    private static String getClassName(Object stackFrame) {
        return invokeMethod(stackFrame, GET_CLASS_NAME_METHOD);
    }

    /**
     * General implementation, get the calling class name
     *
     * @return call class name
     * @see #getCallerClassNameInGeneralJVM(int)
     */
    static String getCallerClassNameInGeneralJVM() {
        return getCallerClassNameInGeneralJVM(stackTraceElementInvocationFrame);
    }

    /**
     * General implementation, get the calling class name by specifying the calling level value
     *
     * @param invocationFrame invocation frame
     * @return specified invocation frame class
     */
    static String getCallerClassNameInGeneralJVM(int invocationFrame) throws IndexOutOfBoundsException {
        StackTraceElement[] elements = getStackTrace();
        if (invocationFrame < elements.length) {
            StackTraceElement targetStackTraceElement = elements[invocationFrame];
            return targetStackTraceElement.getClassName();
        }
        return null;
    }
}
