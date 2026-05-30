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

import io.microsphere.lang.Prioritized;
import io.microsphere.logging.Logger;

import static io.microsphere.constants.SymbolConstants.AT;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.management.JmxUtils.getRuntimeMXBean;
import static io.microsphere.util.StringUtils.isNumeric;
import static io.microsphere.util.StringUtils.substringBefore;
import static java.lang.Long.valueOf;

/**
 * A {@link ProcessIdResolver} implementation for classic JDK versions (5 - 8).
 *
 * <p>This class resolves the current process ID using the name obtained from
 * {@link java.lang.management.RuntimeMXBean#getName()}.
 * The format of the returned name is usually:
 * <ul>
 *     <li>{@code pid@hostname}</li>
 * </ul>
 * where {@code pid} represents the process ID. This resolver extracts and returns the numeric part before the '{@code @}' symbol.</p>
 *
 * <h3>Supported Environments</h3>
 * <p>This resolver is designed to work on Java 5 through Java 8, where newer APIs like
 * {@link java.lang.ProcessHandle#current()} are not available.</p>
 *
 * <h3>Example Output</h3>
 * <pre>{@code
 * // Assuming RuntimeMXBean.getName() returns "12345@localhost"
 * ClassicProcessIdResolver resolver = new ClassicProcessIdResolver();
 * if (resolver.supports()) {
 *     Long pid = resolver.current();
 *     System.out.println("Current PID: " + pid); // Outputs: Current PID: 12345
 * }
 * }</pre>
 *
 * <h3>Logging Behavior</h3>
 * <p>If trace logging is enabled via the underlying logger ({@link io.microsphere.logging.Logger}), this resolver logs the resolved PID along with the raw value obtained from the runtime MXBean:</p>
 *
 * <pre>{@code
 * [TRACE] The PID was resolved from the method 'java.lang.management.RuntimeMXBean#getName()' = 12345@localhost : 12345
 * }</pre>
 *
 * <h3>Priority</h3>
 * <p>This resolver has a priority level set to {@link Prioritized#NORMAL_PRIORITY} plus 9,
 * which means it will be considered after resolvers with higher priority but before those with lower.</p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ProcessIdResolver
 * @since 1.0.0
 */
public class ClassicProcessIdResolver implements ProcessIdResolver {

    private static final Logger logger = getLogger(ClassicProcessIdResolver.class);

    private static final String runtimeName = getRuntimeMXBean().getName();

    private static final String processIdValue = substringBefore(runtimeName, AT);

    @Override
    public boolean supports() {
        return isNumeric(processIdValue);
    }

    @Override
    public Long current() {
        Long processId = valueOf(processIdValue);
        if (logger.isTraceEnabled()) {
            logger.trace("The PID was resolved from the method 'java.lang.management.RuntimeMXBean#getName()' = {} : {}", runtimeName, processId);
        }
        return processId;
    }

    @Override
    public int getPriority() {
        return NORMAL_PRIORITY + 9;
    }
}
