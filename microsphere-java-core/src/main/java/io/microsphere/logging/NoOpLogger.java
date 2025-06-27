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
package io.microsphere.logging;

import static io.microsphere.util.StringUtils.isBlank;

/**
 * A no-operation implementation of the {@link Logger} interface.
 *
 * <p>This logger performs no actions when logging methods are called, effectively silencing all log output.
 * It is useful in scenarios where logging is required to be completely disabled without modifying the application's
 * logging configuration.</p>
 *
 * <h3>Usage Example</h3>
 * <pre>{@code
 * // Create a NoOpLogger instance
 * Logger logger = new NoOpLogger("exampleLogger");
 *
 * // Logging messages will have no effect
 * logger.info("This message will not be logged.");
 * logger.error("An error occurred", new Exception("Sample exception"));
 * }</pre>
 *
 * <p><b>Note:</b> This class is thread-safe and can be used concurrently from multiple threads.</p>
 */
final class NoOpLogger extends AbstractLogger {

    NoOpLogger(String name) {
        super(isBlank(name) ? "" : name);
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public void trace(String message) {
    }

    @Override
    public void trace(String message, Throwable t) {
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public void debug(String message) {
    }

    @Override
    public void debug(String message, Throwable t) {
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(String message) {
    }

    @Override
    public void info(String message, Throwable t) {
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(String message) {
    }

    @Override
    public void warn(String message, Throwable t) {
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(String message) {
    }

    @Override
    public void error(String message, Throwable t) {
    }
}