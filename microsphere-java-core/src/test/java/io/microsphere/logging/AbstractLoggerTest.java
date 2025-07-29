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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.microsphere.util.ArrayUtils.EMPTY_OBJECT_ARRAY;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AbstractLogger} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractLogger
 * @since 1.0.0
 */
public abstract class AbstractLoggerTest {

    protected Logger logger;

    @BeforeEach
    void setUp() {
        this.logger = createLogger();
    }

    protected abstract Logger createLogger();

    @Test
    void testGetName() {
        assertNotNull(logger.getName());
    }

    @Test
    void testIsTraceEnabled() {
        assertTrue(logger.isTraceEnabled());
    }

    @Test
    void testTrace() {
        logger.trace("test");
        logger.trace("test", EMPTY_OBJECT_ARRAY);
        logger.trace("test : {}", "a");
        logger.trace("test : {}", "a", new Throwable());
        logger.trace("test", new Throwable());
    }

    @Test
    void testIsDebugEnabled() {
        assertTrue(logger.isDebugEnabled());
    }

    @Test
    void testDebug() {
        logger.debug("test");
        logger.debug("test", EMPTY_OBJECT_ARRAY);
        logger.debug("test : {}", "a");
        logger.debug("test : {}", "a", new Throwable());
        logger.debug("test", new Throwable());
    }

    @Test
    void testIsInfoEnabled() {
        assertTrue(logger.isInfoEnabled());
    }

    @Test
    void testInfo() {
        logger.info("test");
        logger.info("test", EMPTY_OBJECT_ARRAY);
        logger.info("test : {}", "a");
        logger.info("test : {}", "a", new Throwable());
        logger.info("test", new Throwable());
    }

    @Test
    void testIsWarnEnabled() {
        assertTrue(logger.isWarnEnabled());
    }

    @Test
    void testWarn() {
        logger.warn("test");
        logger.warn("test", EMPTY_OBJECT_ARRAY);
        logger.warn("test : {}", "a");
        logger.warn("test : {}", "a", new Throwable());
        logger.warn("test", new Throwable());
    }

    @Test
    void testIsErrorEnabled() {
        assertTrue(logger.isErrorEnabled());
    }

    @Test
    void testError() {
        logger.error("test");
        logger.error("test", EMPTY_OBJECT_ARRAY);
        logger.error("test : {}", "a");
        logger.error("test : {}", "a", new Throwable());
        logger.error("test", new Throwable());
    }

    @Test
    void testLog() {
    }

    @Test
    void testResolveMessage() {
    }
}
