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

import io.microsphere.annotation.ConfigurationProperty;

/**
 * The Constants for Microsphere Resource
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface ResourceConstants {

    /**
     * The root resource location of Metadata : "META-INF/"
     */
    String METADATA_RESOURCE = "META-INF/";

    /**
     * The root resource location of Microsphere Metadata : "META-INF/microsphere/"
     */
    String MICROSPHERE_METADATA_RESOURCE = METADATA_RESOURCE + "microsphere/";

    /**
     * The file name of {@link ConfigurationProperty configuration properties} Metadata
     */
    String CONFIGURATION_PROPERTY_METADATA_FILE_NAME = "configuration-properties.json";

    /**
     * The resource name of additional {@link ConfigurationProperty configuration properties} Metadata
     */
    String ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_FILE_NAME = "additional-configuration-properties.json";

    /**
     * The resource location of {@link ConfigurationProperty configuration properties} metadata :
     * "META-INF/microsphere/configuration-properties.json"
     */
    String  CONFIGURATION_PROPERTY_METADATA_RESOURCE = MICROSPHERE_METADATA_RESOURCE + CONFIGURATION_PROPERTY_METADATA_FILE_NAME;

    /**
     * The resource location of additional {@link ConfigurationProperty configuration properties} metadata :
     * "META-INF/microsphere/additional-configuration-properties.json"
     */
    String ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_RESOURCE = MICROSPHERE_METADATA_RESOURCE + ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_FILE_NAME;
}
