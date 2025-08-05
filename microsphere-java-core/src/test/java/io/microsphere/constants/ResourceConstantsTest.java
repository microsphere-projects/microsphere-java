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

package io.microsphere.constants;


import org.junit.jupiter.api.Test;

import static io.microsphere.constants.ResourceConstants.ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_FILE_NAME;
import static io.microsphere.constants.ResourceConstants.ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_RESOURCE;
import static io.microsphere.constants.ResourceConstants.CONFIGURATION_PROPERTY_METADATA_FILE_NAME;
import static io.microsphere.constants.ResourceConstants.CONFIGURATION_PROPERTY_METADATA_RESOURCE;
import static io.microsphere.constants.ResourceConstants.METADATA_RESOURCE;
import static io.microsphere.constants.ResourceConstants.MICROSPHERE_METADATA_RESOURCE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ResourceConstants} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourceConstants
 * @since 1.0.0
 */
class ResourceConstantsTest {

    @Test
    void testConstants() {
        assertEquals("META-INF/", METADATA_RESOURCE);
        assertEquals("META-INF/microsphere/", MICROSPHERE_METADATA_RESOURCE);
        assertEquals("configuration-properties.json", CONFIGURATION_PROPERTY_METADATA_FILE_NAME);
        assertEquals("additional-configuration-properties.json", ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_FILE_NAME);
        assertEquals("META-INF/microsphere/configuration-properties.json", CONFIGURATION_PROPERTY_METADATA_RESOURCE);
        assertEquals("META-INF/microsphere/additional-configuration-properties.json", ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_RESOURCE);
    }
}