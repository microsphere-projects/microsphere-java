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

package io.microsphere.annotation;

import org.junit.jupiter.api.Test;

import static io.microsphere.annotation.ConfigurationProperty.APPLICATION_SOURCE;
import static io.microsphere.annotation.ConfigurationProperty.ENVIRONMENT_VARIABLES_SOURCE;
import static io.microsphere.annotation.ConfigurationProperty.SYSTEM_PROPERTIES_SOURCE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ConfigurationProperty} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationProperty
 * @since 1.0.0
 */
public class ConfigurationPropertyTest {

    @Test
    void testSources() {
        assertEquals("system-properties", SYSTEM_PROPERTIES_SOURCE);
        assertEquals("environment-variables", ENVIRONMENT_VARIABLES_SOURCE);
        assertEquals("application", APPLICATION_SOURCE);
    }
}
