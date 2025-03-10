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
import static io.microsphere.util.StringUtils.substringBefore;
import static java.lang.Long.valueOf;

/**
 * {@link ProcessIdResolver} class for classic JDK(5 - 8)
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ProcessIdResolver
 * @since 1.0.0
 */
public class ClassicProcessIdResolver implements ProcessIdResolver {

    private static final Logger logger = getLogger(ClassicProcessIdResolver.class);

    @Override
    public Long current() {
        RuntimeMXBean runtimeMXBean = getRuntimeMXBean();
        String name = runtimeMXBean.getName();
        Long processId = null;
        try {
            String processIdValue = substringBefore(name, AT);
            processId = valueOf(processIdValue);
            if (logger.isTraceEnabled()) {
                logger.trace("The PID was resolved from the method 'java.lang.management.RuntimeMXBean#getName()' = {} : {}", name, processId);
            }
        } catch (Throwable e) {
            logger.warn("The PID can't be resolved from the method 'java.lang.management.RuntimeMXBean#getName()' = {} : {}", name, e);
        }
        return processId;
    }

    @Override
    public int getPriority() {
        return NORMAL_PRIORITY + 9;
    }
}
