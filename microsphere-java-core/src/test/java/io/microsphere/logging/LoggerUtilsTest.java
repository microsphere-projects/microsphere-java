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


import org.junit.jupiter.api.Test;

import static io.microsphere.logging.LoggerUtils.debug;
import static io.microsphere.logging.LoggerUtils.error;
import static io.microsphere.logging.LoggerUtils.info;
import static io.microsphere.logging.LoggerUtils.log;
import static io.microsphere.logging.LoggerUtils.trace;
import static io.microsphere.logging.LoggerUtils.warn;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * {@link LoggerUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LoggerUtils
 * @since 1.0.0
 */
class LoggerUtilsTest {

    @Test
    void testTrace() {
        assertDoesNotThrow(() -> trace(logger -> logger.trace("Hello,World")));
        assertDoesNotThrow(() -> trace(logger -> logger.trace("Hello,{}", "World")));
        assertDoesNotThrow(() -> trace(logger -> logger.trace("{},{}", "Hello", "World")));
    }

    @Test
    void testDebug() {
        assertDoesNotThrow(() -> debug(logger -> logger.debug("Hello,World")));
        assertDoesNotThrow(() -> debug(logger -> logger.debug("Hello,{}", "World")));
        assertDoesNotThrow(() -> debug(logger -> logger.debug("{},{}", "Hello", "World")));
    }

    @Test
    void testInfo() {
        assertDoesNotThrow(() -> info(logger -> logger.info("Hello,World")));
        assertDoesNotThrow(() -> info(logger -> logger.info("Hello,{}", "World")));
        assertDoesNotThrow(() -> info(logger -> logger.info("{},{}", "Hello", "World")));
    }

    @Test
    void testWarn() {
        assertDoesNotThrow(() -> warn(logger -> logger.warn("Hello,World")));
        assertDoesNotThrow(() -> warn(logger -> logger.warn("Hello,{}", "World")));
        assertDoesNotThrow(() -> warn(logger -> logger.warn("{},{}", "Hello", "World")));
    }

    @Test
    void testError() {
        assertDoesNotThrow(() -> error(logger -> logger.error("Hello,World")));
        assertDoesNotThrow(() -> error(logger -> logger.error("Hello,{}", "World")));
        assertDoesNotThrow(() -> error(logger -> logger.error("{},{}", "Hello", "World")));
    }

    @Test
    void testLog() {
        assertDoesNotThrow(() -> log(logger -> false, logger -> logger.info("Hello,World")));
    }
}