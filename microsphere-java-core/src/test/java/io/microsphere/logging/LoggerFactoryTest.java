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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.logging.LogManager;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.logging.LoggerFactory.loadAvailableFactories;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link LoggerFactory}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see LoggerFactory
 * @since 1.0.0
 */
public class LoggerFactoryTest {

    @BeforeAll
    public static void init() throws IOException {
        URL resource = LoggerFactoryTest.class.getResource("/META-INF/logging.properties");
        try (InputStream inputStream = resource.openStream()) {
            LogManager.getLogManager().readConfiguration(inputStream);
        }
    }

    @Test
    public void testGetLogger() {
        Logger logger = getLogger(LoggerFactoryTest.class);
        log(logger);
    }

    @Test
    public void testLoadAvailableFactories() {
        loadAvailableFactories().forEach(this::testLoggerFactory);
    }

    private void testLoggerFactory(LoggerFactory loggerFactory) {
        Logger logger = loggerFactory.createLogger("test");
        log(logger);
    }

    private void log(Logger logger) {
        assertLevel(logger);
        log(logger, "Hello,World");
        log(logger, "Hello,World {}", Calendar.getInstance().get(Calendar.YEAR));
        log(logger, "Hello,World", new Throwable("Testing"));
    }

    private void assertLevel(Logger logger) {
        assertTrue(logger.isTraceEnabled());
        assertTrue(logger.isDebugEnabled());
        assertTrue(logger.isInfoEnabled());
        assertTrue(logger.isWarnEnabled());
        assertTrue(logger.isErrorEnabled());
    }

    private void log(Logger logger, String message) {
        logger.trace(message);
        logger.debug(message);
        logger.info(message);
        logger.warn(message);
        logger.error(message);
    }

    private void log(Logger logger, String format, Object... arguments) {
        logger.trace(format, arguments);
        logger.debug(format, arguments);
        logger.info(format, arguments);
        logger.warn(format, arguments);
        logger.error(format, arguments);
    }

    private void log(Logger logger, String message, Throwable t) {
        logger.trace(message, t);
        logger.debug(message, t);
        logger.info(message, t);
        logger.warn(message, t);
        logger.error(message, t);
    }
}
