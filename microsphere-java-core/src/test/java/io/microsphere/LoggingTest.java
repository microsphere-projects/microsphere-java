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

package io.microsphere;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

import static ch.qos.logback.classic.Level.valueOf;
import static org.slf4j.LoggerFactory.getILoggerFactory;

/**
 * The abstract class for loggging with repeated levels
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LoggerContext
 * @since 1.0.0
 */
@ParameterizedClass
@ValueSource(strings = {"INFO", "TRACE"})
@Disabled
public abstract class LoggingTest {

    private static final LoggerContext loggerContext = (LoggerContext) getILoggerFactory();

    @Parameter
    private String logLevel;

    private Level orginalLevel;

    @BeforeEach
    final void setLoggingLevel() {
        Logger logger = getTargetLogger();
        this.orginalLevel = logger.getLevel();
        Level level = valueOf(logLevel);
        logger.setLevel(level);
    }

    @AfterEach
    final void resetLoggingLevel() {
        Logger logger = getTargetLogger();
        logger.setLevel(this.orginalLevel);
    }

    protected Logger getTargetLogger() {
        return loggerContext.getLogger(getClass().getPackage().getName());
    }
}
