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
package io.microsphere.process;

import io.microsphere.logging.Logger;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.reflect.MethodUtils.invokeStaticMethod;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;

/**
 * A {@link ProcessIdResolver} implementation for modern JDKs (Java 9+).
 *
 * <p>This class uses the {@link java.lang.ProcessHandle} API introduced in Java 9 to retrieve the current process ID.
 * It dynamically checks for the presence of the required classes and methods at runtime, ensuring compatibility with
 * different JDK versions.</p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * ProcessIdResolver resolver = new ModernProcessIdResolver();
 * if (resolver.supports()) {
 *     Long pid = resolver.current();
 *     System.out.println("Current Process ID: " + pid);
 * } else {
 *     System.out.println("Process ID resolution not supported on this JVM version.");
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ProcessIdResolver
 * @see java.lang.ProcessHandle
 * @since 1.0.0
 */
public class ModernProcessIdResolver implements ProcessIdResolver {

    private static final Logger logger = getLogger(ModernProcessIdResolver.class);

    private static final String PROCESS_HANDLE_CLASS_NAME = "java.lang.ProcessHandle";

    private static final Class<?> PROCESS_HANDLE_CLASS = resolveClass(PROCESS_HANDLE_CLASS_NAME);

    @Override
    public boolean supports() {
        return PROCESS_HANDLE_CLASS != null;
    }

    @Override
    public Long current() {
        Object processHandle = invokeStaticMethod(PROCESS_HANDLE_CLASS, "current");
        Long pid = invokeMethod(processHandle, PROCESS_HANDLE_CLASS, "pid");
        if (logger.isTraceEnabled()) {
            logger.trace("The PID was resolved from the method 'java.lang.ProcessHandle#pid()' : {}", pid);
        }
        return pid;
    }

    @Override
    public int getPriority() {
        return NORMAL_PRIORITY + 1;
    }
}
