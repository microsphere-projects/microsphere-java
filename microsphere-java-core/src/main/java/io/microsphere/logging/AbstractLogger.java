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

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ArrayUtils.length;

/**
 * The abstract class of {@link Logger}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Logger
 * @since 1.0.0
 */
public abstract class AbstractLogger implements Logger {

    private final String name;

    protected AbstractLogger(String name) {
        this.name = name;
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public void trace(String format, Object... arguments) {
        log(this::trace, this::trace, format, arguments);
    }

    @Override
    public void debug(String format, Object... arguments) {
        log(this::debug, this::debug, format, arguments);
    }

    @Override
    public void info(String format, Object... arguments) {
        log(this::info, this::info, format, arguments);
    }

    @Override
    public void warn(String format, Object... arguments) {
        log(this::warn, this::warn, format, arguments);
    }

    @Override
    public void error(String format, Object... arguments) {
        log(this::error, this::error, format, arguments);
    }

    /**
     * @param messageHandler          only message to log
     * @param messageThrowableHandler message and {@link Throwable} to log
     * @param format                  the format message or regular message
     * @param arguments               zero or more arguments for the format pattern
     */
    protected void log(Consumer<String> messageHandler, BiConsumer<String, Throwable> messageThrowableHandler, String format, Object... arguments) {
        final String message;
        int length = length(arguments);
        if (length < 1) {
            message = format;
        } else {
            message = resolveMessage(format, arguments);
            Object lastArgument = arguments[length - 1];
            if (lastArgument instanceof Throwable) {
                messageThrowableHandler.accept(message, (Throwable) lastArgument);
            }
        }
        messageHandler.accept(message);
    }

    /**
     * Resolve the format message
     *
     * @param format    the format message
     * @param arguments zero or more arguments for the format pattern
     * @return non-null
     */
    protected String resolveMessage(String format, Object... arguments) {
        return format(format, arguments);
    }
}
