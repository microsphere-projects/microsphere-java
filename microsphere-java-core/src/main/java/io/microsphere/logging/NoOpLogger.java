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
 * No-Operation {@link Logger}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Logger
 * @since 1.0.0
 */
final class NoOpLogger extends AbstractLogger {

    NoOpLogger(String name) {
        super(name);
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