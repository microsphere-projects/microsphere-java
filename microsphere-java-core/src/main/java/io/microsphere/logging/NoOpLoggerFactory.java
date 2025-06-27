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
 * A {@link LoggerFactory} implementation that provides instances of {@link NoOpLogger},
 * which perform no operations for logging calls. This factory is always available
 * and has the lowest possible priority to ensure it is used only when no other
 * logger implementations are available.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Get a NoOpLogger instance by class
 * Logger logger = NoOpLoggerFactory.getLogger(MyClass.class);
 *
 * // Get a NoOpLogger instance by name
 * Logger logger = NoOpLoggerFactory.getLogger("my.logger.name");
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see NoOpLogger
 * @see LoggerFactory
 * @since 1.0.0
 */
public class NoOpLoggerFactory extends LoggerFactory {

    @Override
    protected String getDelegateLoggerClassName() {
        throw new UnsupportedOperationException("This method should not be invoked here!");
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    public Logger createLogger(String name) {
        return new NoOpLogger(name);
    }

    @Override
    public int getPriority() {
        return MIN_PRIORITY;
    }
}
