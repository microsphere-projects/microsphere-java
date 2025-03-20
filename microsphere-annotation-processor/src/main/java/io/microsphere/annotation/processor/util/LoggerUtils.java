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
package io.microsphere.annotation.processor.util;


import io.microsphere.logging.Logger;

import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * Logger Utils
 *
 * @since 1.0.0
 */
public interface LoggerUtils {

    Logger LOGGER = getLogger("microsphere-annotation-processor");

    static void trace(String format, Object... args) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(format, args);
        }
    }

    static void debug(String format, Object... args) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(format, args);
        }
    }

    static void info(String format, Object... args) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(format, args);
        }
    }

    static void warn(String format, Object... args) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(format, args);
        }
    }

    static void error(String format, Object... args) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(format, args);
        }
    }
}
