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

import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.management.JmxUtils.getRuntimeMXBean;
import static io.microsphere.reflect.FieldUtils.findField;
import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static java.lang.Long.valueOf;

/**
 * A {@link ProcessIdResolver} implementation for retrieving the process ID using the SUN JVM internal APIs.
 *
 * <p>This resolver utilizes reflection to access the internal fields and methods of the JVM, specifically
 * targeting the {@code sun.management.VMManagementImpl} class which provides a method to retrieve the native
 * process ID.</p>
 *
 * <h3>How It Works</h3>
 * <p>The process ID is obtained via reflection by accessing the hidden "jvm" field in the {@link RuntimeMXBean}
 * instance, and then invoking the {@code getProcessId()} method on that internal object. This approach is
 * specific to the HotSpot JVM and may not be available on all JVM implementations.</p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * ProcessIdResolver resolver = new VirtualMachineProcessIdResolver();
 * if (resolver.supports()) {
 *     Long pid = resolver.current();
 *     System.out.println("Current Process ID: " + pid);
 * }
 * }</pre>
 *
 * <h3>Priority</h3>
 * <p>This resolver has a priority level of {@link #getPriority()}, making it preferred over some other resolvers
 * but not the highest priority. The priority helps determine which resolver should be used when multiple
 * resolvers are available.</p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ProcessIdResolver
 * @since 1.0.0
 */
public class VirtualMachineProcessIdResolver implements ProcessIdResolver {

    private static final Logger logger = getLogger(VirtualMachineProcessIdResolver.class);

    /**
     * "jvm" Field name
     */
    static final String JVM_FIELD_NAME = "jvm";

    /**
     * "getProcessId" Method name
     *
     * @see sun.management.VMManagementImpl#getProcessId()
     */
    final static String GET_PROCESS_ID_METHOD_NAME = "getProcessId";

    /**
     * The {@link Field} of "jvm"
     */
    final static Field JVM_FIELD = findField(getRuntimeMXBean(), JVM_FIELD_NAME);

    @Override
    public boolean supports() {
        return JVM_FIELD != null;
    }

    @Override
    public Long current() {
        RuntimeMXBean runtimeMXBean = getRuntimeMXBean();
        Object jvm = getFieldValue(runtimeMXBean, JVM_FIELD);
        Integer processId = invokeMethod(jvm, GET_PROCESS_ID_METHOD_NAME);
        if (logger.isTraceEnabled()) {
            logger.trace("The PID was resolved from the native method 'sun.management.VMManagementImpl#getProcessId()' : {}", processId);
        }
        return valueOf(processId.longValue());
    }

    @Override
    public int getPriority() {
        return NORMAL_PRIORITY + 5;
    }
}
