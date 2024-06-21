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
 * Microsphere Logger Interface
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface Logger {

    /**
     * Return the name of this <code>Logger</code> instance.
     *
     * @return name of this logger instance
     */
    String getName();

    /**
     * Is the logger instance enabled for the TRACE level?
     *
     * @return True if this Logger is enabled for the TRACE level,
     * false otherwise.
     */
    boolean isTraceEnabled();

    /**
     * Log a message at the TRACE level.
     *
     * @param message the message string to be logged
     */
    void trace(String message);

    /**
     * Log a message at the TRACE level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the TRACE level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for TRACE.
     *
     * @param format    the format string
     * @param arguments the arguments
     */
    void trace(String format, Object... arguments);

    /**
     * Log an exception (throwable) at the TRACE level with an
     * accompanying message.
     *
     * @param message the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    void trace(String message, Throwable t);

    /**
     * Is the logger instance enabled for the DEBUG level?
     *
     * @return True if this Logger is enabled for the DEBUG level,
     * false otherwise.
     */
    boolean isDebugEnabled();

    /**
     * Log a message at the DEBUG level.
     *
     * @param message the message string to be logged
     */
    void debug(String message);

    /**
     * Log a message at the DEBUG level according to the specified format
     * and arguments.
     *
     * @param format    the format string
     * @param arguments the arguments
     */
    void debug(String format, Object... arguments);

    /**
     * Log an exception (throwable) at the DEBUG level with an
     * accompanying message.
     *
     * @param message the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    void debug(String message, Throwable t);

    /**
     * Is the logger instance enabled for the INFO level?
     *
     * @return True if this Logger is enabled for the INFO level,
     * false otherwise.
     */
    boolean isInfoEnabled();

    /**
     * Log a message at the INFO level.
     *
     * @param message the message string to be logged
     */
    void info(String message);

    /**
     * Log a message at the INFO level according to the specified format
     * and arguments.
     *
     * @param format    the format string
     * @param arguments the arguments
     */
    void info(String format, Object... arguments);

    /**
     * Log an exception (throwable) at the INFO level with an
     * accompanying message.
     *
     * @param message the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    void info(String message, Throwable t);

    /**
     * Is the logger instance enabled for the WARN level?
     *
     * @return True if this Logger is enabled for the WARN level,
     * false otherwise.
     */
    boolean isWarnEnabled();

    /**
     * Log a message at the WARN level.
     *
     * @param message the message string to be logged
     */
    void warn(String message);

    /**
     * Log a message at the WARN level according to the specified format
     * and arguments.
     *
     * @param format    the format string
     * @param arguments the arguments
     */
    void warn(String format, Object... arguments);

    /**
     * Log an exception (throwable) at the WARN level with an
     * accompanying message.
     *
     * @param message the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    void warn(String message, Throwable t);

    /**
     * Is the logger instance enabled for the ERROR level?
     *
     * @return True if this Logger is enabled for the ERROR level,
     * false otherwise.
     */
    boolean isErrorEnabled();

    /**
     * Log a message at the ERROR level.
     *
     * @param message the message string to be logged
     */
    void error(String message);

    /**
     * Log a message at the ERROR level according to the specified format
     * and arguments.
     *
     * @param format    the format string
     * @param arguments the arguments
     */
    void error(String format, Object... arguments);

    /**
     * Log an exception (throwable) at the ERROR level with an
     * accompanying message.
     *
     * @param message the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    void error(String message, Throwable t);

}
