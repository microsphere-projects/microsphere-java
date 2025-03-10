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

import static io.microsphere.constants.SymbolConstants.AT;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.management.JmxUtils.getRuntimeMXBean;
import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.util.StringUtils.substringBefore;
import static java.lang.Long.valueOf;

/**
 * {@link ProcessIdResolver} class for SUN JVM
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
     * {@link RuntimeMXBean}
     */
    final static RuntimeMXBean runtimeMXBean = getRuntimeMXBean();

    /**
     * sun.management.ManagementFactory.jvm
     */
    final static Object jvm = findJvm();

    private static Object findJvm() {
        Object jvm = null;
        if (runtimeMXBean != null) {
            try {
                jvm = getFieldValue(runtimeMXBean, JVM_FIELD_NAME);
            } catch (Throwable e) {
                logger.error("The Field[name : '{}'] can't be found in RuntimeMXBean class : '{}'!", JVM_FIELD_NAME, runtimeMXBean.getClass(), e);
            }
        }
        return jvm;
    }

    @Override
    public Long current() {
        Integer processId = null;
        try {
            processId = invokeMethod(jvm, GET_PROCESS_ID_METHOD_NAME);
            if (logger.isTraceEnabled()) {
                logger.trace("The PID was resolved from the native method 'sun.management.VMManagementImpl#getProcessId()' : {}", processId);
            }
        } catch (Throwable e) {
            logger.warn("It's failed to invoke the native method 'sun.management.VMManagementImpl#getProcessId()'", e);
        }
        return new Long(processId.longValue());
    }

    @Override
    public int getPriority() {
        return NORMAL_PRIORITY + 5;
    }
}
