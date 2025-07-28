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

package io.microsphere.metadata;

import io.microsphere.beans.BeanUtils;
import io.microsphere.beans.ConfigurationProperty;
import io.microsphere.json.JSONObject;

import java.util.Map;

import static io.microsphere.beans.BeanUtils.resolvePropertiesAsMap;
import static io.microsphere.util.Assert.assertNotNull;

/**
 * {@link ConfigurationPropertyJSONGenerator} class based on Java Reflection API
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationPropertyJSONGenerator
 * @see JSONObject
 * @see BeanUtils#resolvePropertiesAsMap(Object)
 * @since 1.0.0
 */
public class ReflectiveConfigurationPropertyJSONGenerator implements ConfigurationPropertyJSONGenerator {

    @Override
    public String generate(ConfigurationProperty configurationProperty) throws IllegalArgumentException {
        assertNotNull(configurationProperty, () -> "The 'ConfigurationProperty' argument must not be null");
        Map<String, Object> properties = resolvePropertiesAsMap(configurationProperty);
        JSONObject jsonObject = new JSONObject(properties);
        return jsonObject.toString();
    }
}
