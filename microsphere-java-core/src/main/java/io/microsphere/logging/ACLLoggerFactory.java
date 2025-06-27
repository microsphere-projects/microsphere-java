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

/**
 * {@link LoggerFactory} implementation for Apache Commons Logging (ACL).
 *
 * <p>{@code ACLLoggerFactory} provides a bridge to the Apache Commons Logging framework,
 * creating and managing instances of the {@link Logger} interface backed by ACL's
 * {@link org.apache.commons.logging.Log}.
 *
 * <h3>Usage Example</h3>
 * Normally, this factory is automatically discovered and used when Apache Commons Logging
 * is present on the classpath. However, it can also be explicitly set as the logging
 * provider:
 *
 * <pre>{@code
 * // Explicitly setting ACLLoggerFactory as the logging provider
 * LoggerFactory factory = new ACLLoggerFactory();
 * Logger logger = factory.createLogger("com.example.MyClass");
 * logger.info("This is an info message.");
 * }</pre>
 *
 * <h3>Integration with Prioritized Interface</h3>
 * This factory has a defined priority level ({@link #getPriority()}) that determines its
 * precedence among other available logging frameworks. The higher the number, the lower
 * the priority.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see LoggerFactory
 * @see org.apache.commons.logging.Log
 * @since 1.0.0
 */
public class ACLLoggerFactory extends LoggerFactory {

    public static final String ACL_LOG_CLASS_NAME = "org.apache.commons.logging.Log";

    @Override
    protected String getDelegateLoggerClassName() {
        return ACL_LOG_CLASS_NAME;
    }

    @Override
    public Logger createLogger(String name) {
        return new ACLLogger(name);
    }

    @Override
    public int getPriority() {
        return NORMAL_PRIORITY + 5;
    }

    static class ACLLogger extends AbstractLogger {

        private final org.apache.commons.logging.Log logger;

        ACLLogger(String name) {
            super(name);
            this.logger = org.apache.commons.logging.LogFactory.getLog(name);
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
        public void error(String message, Throwable t) {
            logger.error(message, t);
        }
    }
}
