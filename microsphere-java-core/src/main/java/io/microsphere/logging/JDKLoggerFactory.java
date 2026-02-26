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

import java.util.logging.Level;

import static java.util.logging.Level.ALL;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

/**
 * A {@link LoggerFactory} implementation that creates and manages instances of JDK logging ({@link java.util.logging.Logger}).
 *
 * <p>This factory provides a concrete implementation for creating JDK-based loggers with the required name.
 * It defines priority-based ordering by implementing the {@link Prioritized} interface, ensuring proper loading order
 * when multiple logger factories are present.
 *
 * <h3>Example Usage</h3>
 * The typical usage involves relying on the static methods provided by the base class:
 *
 * <pre>{@code
 * // Get a logger instance associated with a specific class
 * Logger logger = getLogger(MyClass.class);
 *
 * // Get a named logger instance
 * Logger namedLogger = getLogger("com.example.mylogger");
 * }</pre>
 *
 * <p>The factory is automatically loaded via the ServiceLoader mechanism if registered properly in the configuration files.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see LoggerFactory
 * @see java.util.logging.Logger
 * @since 1.0.0
 */
public class JDKLoggerFactory extends LoggerFactory {

    public static final String JDK_LOGGER_CLASS_NAME = "java.util.logging.Logger";

    @Override
    protected String getDelegateLoggerClassName() {
        return JDK_LOGGER_CLASS_NAME;
    }

    @Override
    public Logger createLogger(String name) {
        return new JDKLogger(name);
    }

    @Override
    public int getPriority() {
        return NORMAL_PRIORITY + 10;
    }

    static class JDKLogger extends AbstractLogger {

        private final java.util.logging.Logger logger;

        JDKLogger(String name) {
            super(name);
            this.logger = java.util.logging.Logger.getLogger(name);
        }

        @Override
        public boolean isTraceEnabled() {
            return isLoggable(ALL);
        }

        @Override
        public void trace(String message) {
            log(ALL, message);
        }

        @Override
        public void trace(String message, Throwable t) {
            log(ALL, message, t);
        }

        @Override
        public boolean isDebugEnabled() {
            return isLoggable(FINE);
        }

        @Override
        public void debug(String message) {
            log(FINE, message);
        }

        @Override
        public void debug(String message, Throwable t) {
            log(FINE, message, t);
        }

        @Override
        public boolean isInfoEnabled() {
            return this.isLoggable(INFO);
        }

        @Override
        public void info(String message) {
            log(INFO, message);
        }

        @Override
        public void info(String message, Throwable t) {
            log(INFO, message, t);
        }

        @Override
        public boolean isWarnEnabled() {
            return isLoggable(WARNING);
        }

        @Override
        public void warn(String message) {
            log(WARNING, message);
        }

        @Override
        public void warn(String message, Throwable t) {
            log(WARNING, message, t);
        }

        @Override
        public boolean isErrorEnabled() {
            return isLoggable(SEVERE);
        }

        @Override
        public void error(String message) {
            log(SEVERE, message);
        }

        @Override
        public void error(String message, Throwable t) {
            log(SEVERE, message, t);
        }

        boolean isLoggable(Level level) {
            return this.logger.isLoggable(level);
        }

        void log(Level level, String message) {
            logger.log(level, message);
        }

        void log(Level level, String msg, Throwable t) {
            logger.log(level, msg, t);
        }

    }
}
