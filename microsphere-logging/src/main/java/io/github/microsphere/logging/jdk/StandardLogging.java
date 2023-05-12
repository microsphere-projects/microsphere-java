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
package io.github.microsphere.logging.jdk;

import io.github.microsphere.logging.Logging;

import java.util.List;
import java.util.logging.LoggingMXBean;

import static java.util.logging.LogManager.getLoggingMXBean;

/**
 * The Standard JDK {@link Logging}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Logging
 * @since 1.0.0
 */
public class StandardLogging implements Logging {

    private final static LoggingMXBean loggingMXBean = getLoggingMXBean();

    @Override
    public List<String> getLoggerNames() {
        return loggingMXBean.getLoggerNames();
    }

    @Override
    public String getLoggerLevel(String loggerName) {
        return loggingMXBean.getLoggerLevel(loggerName);
    }

    @Override
    public void setLoggerLevel(String loggerName, String levelName) {
        loggingMXBean.setLoggerLevel(loggerName, levelName);
    }

    @Override
    public String getParentLoggerName(String loggerName) {
        return loggingMXBean.getParentLoggerName(loggerName);
    }
}
