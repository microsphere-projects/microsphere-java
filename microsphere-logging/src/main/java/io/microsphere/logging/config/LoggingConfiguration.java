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
package io.microsphere.logging.config;

import org.springframework.core.env.Environment;

import javax.servlet.ServletContext;

import static io.microsphere.logging.servlet.ServletConstants.DEFAULT_LOGGING_FILTER_LOCATION;
import static io.microsphere.logging.servlet.ServletConstants.LOGGING_FILTER_LOCATION_KEY;

/**
 * Logging Configuration
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since TODO
 */
public class LoggingConfiguration {

    public String getLoggingFiltersLocation(ServletContext servletContext) {
        String paramValue = servletContext.getInitParameter(LOGGING_FILTER_LOCATION_KEY);
        return paramValue == null ? DEFAULT_LOGGING_FILTER_LOCATION : paramValue;
    }

    public String getLoggingFiltersLocation(Environment environment) {
        return environment.getProperty(LOGGING_FILTER_LOCATION_KEY, DEFAULT_LOGGING_FILTER_LOCATION);
    }
}
