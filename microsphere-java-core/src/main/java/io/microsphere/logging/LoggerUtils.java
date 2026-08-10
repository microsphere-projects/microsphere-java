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

import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.microsphere.constants.SymbolConstants.DOLLAR;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ClassUtils.getTypeName;
import static io.microsphere.util.StringUtils.substringBefore;

/**
 * The Utilities class of Logger
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Logger
 * @see LoggerFactory
 * @since 1.0.0
 */
public abstract class LoggerUtils {

    /**
     * Log the message when TRACE level is enabled
     *
     * @param loggerConsumer the {@link Logger} consumer
     */
    public static void trace(Consumer<Logger> loggerConsumer) {
        log(Logger::isTraceEnabled, loggerConsumer);
    }

    /**
     * Log the message when DEBUG level is enabled
     *
     * @param loggerConsumer the {@link Logger} consumer
     */
    public static void debug(Consumer<Logger> loggerConsumer) {
        log(Logger::isDebugEnabled, loggerConsumer);
    }

    /**
     * Log the message when INFO level is enabled
     *
     * @param loggerConsumer the {@link Logger} consumer
     */
    public static void info(Consumer<Logger> loggerConsumer) {
        log(Logger::isInfoEnabled, loggerConsumer);
    }

    /**
     * Log the message when WARN level is enabled
     *
     * @param loggerConsumer the {@link Logger} consumer
     */
    public static void warn(Consumer<Logger> loggerConsumer) {
        log(Logger::isWarnEnabled, loggerConsumer);
    }

    /**
     * Log the message when ERROR level is enabled
     *
     * @param loggerConsumer the {@link Logger} consumer
     */
    public static void error(Consumer<Logger> loggerConsumer) {
        log(Logger::isErrorEnabled, loggerConsumer);
    }

    /**
     * Log the message when the given {@link Predicate} is true
     *
     * @param loggerPredicate the {@link Logger} predicate
     * @param loggerConsumer  the {@link Logger} consumer
     */
    static void log(Predicate<Logger> loggerPredicate, Consumer<Logger> loggerConsumer) {
        Logger logger = logger(loggerConsumer);
        if (loggerPredicate.test(logger)) {
            loggerConsumer.accept(logger);
        }
    }

    private static Logger logger(Consumer<Logger> loggerConsumer) {
        String loggerName = getLoggerName(loggerConsumer);
        return getLogger(loggerName);
    }

    private static String getLoggerName(Consumer<Logger> loggerConsumer) {
        return substringBefore(getTypeName(loggerConsumer), DOLLAR);
    }

    private LoggerUtils() {
    }
}