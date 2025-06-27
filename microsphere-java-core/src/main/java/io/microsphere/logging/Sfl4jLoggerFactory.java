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

import io.microsphere.lang.Prioritized;

/**
 * The {@link LoggerFactory} implementation for creating and managing SLF4J-based {@link Logger} instances.
 *
 * <p>SLF4J (Simple Logging Facade for Java) is a popular logging abstraction that allows the end-user to plug in their desired logging framework at deployment time.
 * This factory checks for the availability of the SLF4J logging infrastructure and creates corresponding logger instances.
 *
 * <h3>Configuration</h3>
 * <p>This factory has a fixed priority level of {@link Prioritized#NORMAL_PRIORITY}, meaning it will be used if no higher-priority logger factory is available.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Get a logger instance by class
 * Logger logger = LoggerFactory.getLogger(MyClass.class);
 *
 * // Log a message
 * logger.info("This is an info message");
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see LoggerFactory
 * @see org.slf4j.Logger
 * @since 1.0.0
 */
public class Sfl4jLoggerFactory extends LoggerFactory {

    public static final String SLF4J_LOGGER_CLASS_NAME = "org.slf4j.Logger";

    @Override
    protected String getDelegateLoggerClassName() {
        return SLF4J_LOGGER_CLASS_NAME;
    }

    @Override
    public Logger createLogger(String name) {
        return new Sfl4jLogger(name);
    }

    @Override
    public int getPriority() {
        return NORMAL_PRIORITY;
    }

    static class Sfl4jLogger extends AbstractLogger {

        private final org.slf4j.Logger logger;

        protected Sfl4jLogger(String name) {
            super(name);
            this.logger = org.slf4j.LoggerFactory.getLogger(name);
        }

        @Override
        public boolean isTraceEnabled() {
            return logger.isTraceEnabled();
        }

        @Override
        public void trace(String message) {
            logger.trace(message);
        }

        @Override
        public void trace(String format, Object... arguments) {
            logger.trace(format, arguments);
        }

        @Override
        public void trace(String message, Throwable t) {
            logger.trace(message, t);
        }

        @Override
        public boolean isDebugEnabled() {
            return logger.isDebugEnabled();
        }

        @Override
        public void debug(String message) {
            logger.debug(message);
        }

        @Override
        public void debug(String format, Object... arguments) {
            logger.debug(format, arguments);
        }

        @Override
        public void debug(String message, Throwable t) {
            logger.debug(message, t);
        }

        @Override
        public boolean isInfoEnabled() {
            return logger.isInfoEnabled();
        }

        @Override
        public void info(String message) {
            logger.info(message);
        }

        @Override
        public void info(String format, Object... arguments) {
            logger.info(format, arguments);
        }

        @Override
        public void info(String message, Throwable t) {
            logger.info(message, t);
        }

        @Override
        public boolean isWarnEnabled() {
            return logger.isWarnEnabled();
        }

        @Override
        public void warn(String message) {
            logger.warn(message);
        }

        @Override
        public void warn(String format, Object... arguments) {
            logger.warn(format, arguments);
        }

        @Override
        public void warn(String message, Throwable t) {
            logger.warn(message, t);
        }

        @Override
        public boolean isErrorEnabled() {
            return logger.isErrorEnabled();
        }

        @Override
        public void error(String message) {
            logger.error(message);
        }

        @Override
        public void error(String format, Object... arguments) {
            logger.error(format, arguments);
        }

        @Override
        public void error(String message, Throwable t) {
            logger.error(message, t);
        }

    }
}
