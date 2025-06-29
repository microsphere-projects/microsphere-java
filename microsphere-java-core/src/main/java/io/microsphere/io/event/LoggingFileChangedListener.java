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
package io.microsphere.io.event;

import io.microsphere.logging.Logger;

import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * A {@link FileChangedListener} implementation that logs file change events at the debug level.
 *
 * <p>This class provides a simple way to monitor and log file system changes using the logging framework.
 * It implements all the abstract methods of the {@link FileChangedListener} interface, delegating each
 * event to a common logging method. The logging is performed only if the underlying logger is enabled for
 * the debug level, which helps reduce unnecessary overhead in production environments.</p>
 *
 * <h3>Example Usage</h3>
 * Here's how you can create and register a {@code LoggingFileChangedListener} instance:
 *
 * <pre>{@code
 * FileChangedListener listener = new LoggingFileChangedListener();
 * fileMonitor.addListener(listener);
 * }</pre>
 *
 * <p>In this example, any file creation, modification, or deletion events monitored by the
 * {@code fileMonitor} will be logged at the debug level by the {@code LoggingFileChangedListener}.</p>
 *
 * <p><strong>Note:</strong> This class assumes that the underlying logging framework (e.g., SLF4J, Log4j)
 * is configured properly. If no logging framework is available or configured, logging behavior is undefined.</p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see FileChangedListener
 * @see Logger
 * @since 1.0.0
 */
public class LoggingFileChangedListener implements FileChangedListener {

    private static final Logger logger = getLogger(LoggingFileChangedListener.class);

    @Override
    public void onFileCreated(FileChangedEvent event) {
        log(event);
    }

    @Override
    public void onFileModified(FileChangedEvent event) {
        log(event);
    }

    @Override
    public void onFileDeleted(FileChangedEvent event) {
        log(event);
    }

    private void log(FileChangedEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug(event.toString());
        }
    }
}
